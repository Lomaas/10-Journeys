package com.main.activitys;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;
import uit.nfc.AsynchronousHttpClient;
import uit.nfc.ResponseListener;
import android.os.Bundle;
import android.os.Handler;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.SessionEvents.AuthListener;
import com.facebook.android.SessionEvents.LogoutListener;
import com.facebook.android.BaseRequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.SessionEvents;
import com.facebook.android.SessionStore;
import com.facebook.android.Utility;
import com.main.*;
import com.main.helper.*;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Window;

public class SettingsActivity extends PreferenceActivity {
	boolean CheckboxPreference;
	public String ListPreference;
	public String editTextPreference;
	public String ringtonePreference;
	public String secondEditTextPreference;
	public String customPref;
	private Handler mHandler;

	private ProgressDialogClass progDialog;
	private ResponseListener responseListener;
	public String PREFS_NAME = "loginInfo";
	public SharedPreferences loginSettings;
	private SharedPreferences prefs;
	private String password;
	private Context ctx;
	String[] mPermissions = { "offline_access", "user_photos"};

	final static int AUTHORIZE_ACTIVITY_RESULT_CODE = 0;

	private EditTextPreference editTextEmail;
	private EditTextPreference editTextUsername;
	private EditTextPreference editTextPassword;
	private CheckBoxPreference setFacebookLogin;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		addPreferencesFromResource(R.xml.preferences);

		loginSettings = getSharedPreferences(PREFS_NAME, 0);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		ctx = this;
		mHandler = new Handler();

		String email = prefs.getString("email", "Nothing has been entered");
		String username = prefs.getString("username", "Nothing has been entered");

		editTextEmail = (EditTextPreference) findPreference("email");
		editTextEmail.setSummary(email);

		editTextUsername = (EditTextPreference) findPreference("username");
		editTextUsername.setSummary(username);

		editTextPassword = (EditTextPreference) findPreference("pw");
		editTextPassword.setSummary("*******");

		setFacebookLogin = (CheckBoxPreference) findPreference("setFacebookLogin");

		Resources res = getResources();
		Utility.mFacebook = new Facebook(res.getString(R.string.APP_ID));
		Utility.mAsyncRunner = new AsyncFacebookRunner(Utility.mFacebook);

		SessionStore.restore(Utility.mFacebook, this);
		SessionEvents.addAuthListener(new FbAPIsAuthListener());
		SessionEvents.addLogoutListener(new FbAPIsLogoutListener());

		if(Utility.mFacebook.isSessionValid()){
			setFacebookLogin.setSummary("Remove Facebook connection");
			setFacebookLogin.setChecked(true);
			requestUserData();
		}
		else {
			setFacebookLogin.setChecked(false);
		}
		setFacebookLogin.setOnPreferenceClickListener(new OnPreferenceClickListener(){
			@Override
			public boolean onPreferenceClick(Preference preference) {
				if (Utility.mFacebook.isSessionValid()) {
					SessionEvents.onLogoutBegin();
					AsyncFacebookRunner asyncRunner = new AsyncFacebookRunner(Utility.mFacebook);
					asyncRunner.logout(ctx, new LogoutRequestListener());
				} else {
					Utility.mFacebook.authorize((SettingsActivity)ctx, mPermissions, AUTHORIZE_ACTIVITY_RESULT_CODE, new LoginDialogListener());
				}
				return false;
			}
		});


		editTextEmail.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				postLoginInfo("Email", newValue.toString(), "http://restfulserver.herokuapp.com/user/change_email");
				return false;
			}
		});

		editTextUsername.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				postLoginInfo("Username", newValue.toString(), "http://restfulserver.herokuapp.com/user/change_username");
				return false;
			}
		});

		editTextPassword.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				password = newValue.toString();
				postLoginInfo("Password", password, "http://restfulserver.herokuapp.com/user/change_password");
				return false;
			}
		});

		editTextUsername.setOnPreferenceClickListener(new OnPreferenceClickListener(){
			@Override
			public boolean onPreferenceClick(Preference preference) {
				return true;
			}

		});
		responseListener = new ResponseListener() {
			@Override
			public void onResponseReceived(HttpResponse response, String message) {
				Log.i("Response", response.toString());
				progDialog.dissMissProgressDialog();
				evaluateResponse(message);
			}
		};
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	public void onResume() {
		super.onResume();
		if (!Utility.mFacebook.isSessionValid()) {
			setFacebookLogin.setChecked(false);
		} else {
			Utility.mFacebook.extendAccessTokenIfNeeded(this, null);
			setFacebookLogin.setChecked(true);
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		/*
		 * if this is the activity result from authorization flow, do a call
		 * back to authorizeCallback Source Tag: login_tag
		 */
		case AUTHORIZE_ACTIVITY_RESULT_CODE: {
			Utility.mFacebook.authorizeCallback(requestCode, resultCode, data);
			break;
		}      
		}
	}


	public void evaluateResponse(String message){
		Log.i("ConfirmLogin", message);
		JSONObject response = null;

		try {
			response = new JSONObject(message);

			if(response.has("id")){
				Editor editor = prefs.edit();

				if(response.has("username")){
					String username = response.getString("username");
					editor.putString("username", username);
					editor.commit();
					editTextUsername.setSummary(username);
				}
				else if(response.has("email")){
					String email = response.getString("email");
					editor.putString("email", email);
					editor.commit();
					editTextEmail.setSummary(email);
				}
				else if(response.has("password")){
					editor.putString("password", password);
					editor.commit();
				}
			}
			else {
				new Alert("Ups..", message, this);
			}
		}
		catch (JSONException e) { 
			new Alert("Uups..", message, this);
			Editor editor = prefs.edit();
			editor.putString("pw", " ");
			e.printStackTrace();
		}
	}

	public void postLoginInfo(String tag, String value, String URL){
		progDialog = new 
				ProgressDialogClass(this, 
						"Updating", 
						"Updating your profile, please wait a moment");

		progDialog.run();

		HttpPost httpPost = null;

		try {
			JSONObject registerInfo = new JSONObject();
			httpPost = new HttpPost(new URI(URL));
			registerInfo.put(tag, value);

			StringEntity se = new StringEntity(registerInfo.toString());
			se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

			httpPost.setEntity(se);
		}
		catch (URISyntaxException e) { e.printStackTrace(); }
		catch (JSONException e) { e.printStackTrace(); }
		catch (UnsupportedEncodingException e) { e.printStackTrace(); }

		AsynchronousHttpClient a = new AsynchronousHttpClient();
		a.sendRequest(httpPost, responseListener, loginSettings);
	}


	/*
	 * The Callback for notifying the application when authorization succeeds or
	 * fails.
	 */

	public class FbAPIsAuthListener implements AuthListener {

		@Override
		public void onAuthSucceed() {
			requestUserData();
		}

		@Override
		public void onAuthFail(String error) {
			setFacebookLogin.setSummary("Login Failed: " + error);
			setFacebookLogin.setChecked(false);
		}
	}

	/*
	 * The Callback for notifying the application when log out starts and
	 * finishes.
	 */
	public class FbAPIsLogoutListener implements LogoutListener {
		@Override
		public void onLogoutBegin() {
		}

		@Override
		public void onLogoutFinish() {
			setFacebookLogin.setSummary("You have logged out! Press to login again...");
		}
	}

	/*
	 * Callback for fetching current user's name, picture, uid.
	 */
	public class UserRequestListener extends BaseRequestListener {

		@Override
		public void onComplete(final String response, final Object state) {
			JSONObject jsonObject;
			try {
				jsonObject = new JSONObject(response);
				Log.i("onComplete", response);
				final String name = jsonObject.getString("name");
				Utility.userUID = jsonObject.getString("id");

				mHandler.post(new Runnable() {
					@Override
					public void run() {
						setFacebookLogin.setSummary("Logged in as " + name);
					}
				});

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}

	public class LogoutRequestListener extends BaseRequestListener {

		/* (non-Javadoc)
		 * @see com.facebook.android.AsyncFacebookRunner.RequestListener#onComplete(java.lang.String, java.lang.Object)
		 */
		@Override
		public void onComplete(String response, Object state) {

			mHandler.post(new Runnable() {
				@Override
				public void run() {
					SessionEvents.onLogoutFinish();
				}
			});
		}

	}

	private final class LoginDialogListener implements DialogListener {
		@Override
		public void onComplete(Bundle values) {
			SessionEvents.onLoginSuccess();
		}

		@Override
		public void onFacebookError(FacebookError error) {
			SessionEvents.onLoginError(error.getMessage());
		}

		@Override
		public void onError(DialogError error) {
			SessionEvents.onLoginError(error.getMessage());
		}

		@Override
		public void onCancel() {
			SessionEvents.onLoginError("Action Canceled");
		}
	}


	/*
	 * Request user name, and picture to show on the main screen.
	 */
	public void requestUserData() {
		setFacebookLogin.setSummary("Fetching user name...");
		Bundle params = new Bundle();
		params.putString("fields", "name");
		Utility.mAsyncRunner.request("me", params, new UserRequestListener());
	}
}