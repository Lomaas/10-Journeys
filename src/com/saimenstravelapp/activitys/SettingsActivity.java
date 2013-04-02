package com.saimenstravelapp.activitys;


import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.saimenstravelapp.*;
import com.saimenstravelapp.activitys.domain.Login;
import com.saimenstravelapp.helper.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;
import async.httprequest.AsynchronousHttpClient;
import async.httprequest.ResponseListener;

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
	public SharedPreferences loginSettings;
	private String password;
	private Context ctx = this;
	String[] mPermissions = { "offline_access", "user_photos" };

	final static int AUTHORIZE_ACTIVITY_RESULT_CODE = 0;

	IntentFilter gcmFilter;
	private BroadcastReceiver gcmReceiver = null;
	private static final int PHOTO_SELECTED = 1;

	private boolean isRunning;


	private EditTextPreference editTextEmail;
	private EditTextPreference editTextUsername;
	private EditTextPreference editTextPassword;
	private Preference profilePicture;
	private Preference introductionCarusell;
	private CheckBoxPreference notifications;
	private Preference seeCardStock;

	private Handler handler = new Handler();
	private Runnable runnable = new Runnable() {        
			@Override
			public void run() {
				try {
					Thread.sleep(100);
				}
				catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ImageView profileImage = (ImageView)findViewById(R.id.preferecneimageview);
				int profileId = Login.getProfileId(loginSettings);
				if(profileImage != null){
					Log.i("imageNotNulaaaaafasfsfsfal", "y");
					profileImage.setImageResource(Constants.profileArray[profileId]);
				}
				else
					Log.i("imageNullaaaaaafsfsfsfaa", "y");
				Log.i("profile", Integer.toString(profileId));				
			}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		addPreferencesFromResource(R.xml.preferences);

		loginSettings = getSharedPreferences(Login.PREFS_NAME, 0);
		gcmReceiver = CommonFunctions.createBroadCastReceiver(ctx, loginSettings, CommonFunctions.FROM_STANDARD_ACTIVITY);
		gcmFilter = new IntentFilter();

		gcmFilter.addAction("GCM_RECEIVED_ACTION");

		mHandler = new Handler();

		String email = Login.getEmail(loginSettings);
		String username = Login.getUsername(loginSettings);
		seeCardStock = (Preference) findPreference("seeCardStock");

		editTextEmail = (EditTextPreference) findPreference("email");
		editTextEmail.setSummary(email);

		editTextUsername = (EditTextPreference) findPreference("username");
		editTextUsername.setSummary(username);

		editTextPassword = (EditTextPreference) findPreference("pw");
		editTextPassword.setSummary("*******");

		notifications = (CheckBoxPreference) findPreference("wantNotifications");

		profilePicture = (Preference) findPreference("profilePicture");

		introductionCarusell = (Preference)findPreference("introductionCarusell");
		
		Preference fullIntro = (Preference)findPreference("fullIntro");
		
		Preference tryOutGame = (Preference)findPreference("tryOutGame");

		tryOutGame.setOnPreferenceClickListener(new OnPreferenceClickListener(){
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(ctx, TryOutGame.class);
				startActivity(intent);
				return true;
			}
		});
		
		fullIntro.setOnPreferenceClickListener(new OnPreferenceClickListener(){
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(ctx, FullRuleset.class);
				startActivity(intent);
				return true;
			}
		});

		introductionCarusell.setOnPreferenceClickListener(new OnPreferenceClickListener(){
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(ctx, IntroductionActivity.class);
				startActivity(intent);
				return true;
			}
		});

		profilePicture.setOnPreferenceClickListener(new OnPreferenceClickListener(){
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(ctx, SelectProfileActivity.class);
				startActivity(intent);
				return true;
			}
		});

		notifications.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

				Editor editor = prefs.edit();
				editor.putBoolean("wantNotifications",(Boolean) newValue);
				editor.commit();
				return true;
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

		seeCardStock.setOnPreferenceClickListener(new OnPreferenceClickListener(){
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent();
				intent.setClass(ctx, SeeCardStockActivity.class);
				intent.putExtra("type", "1");
				startActivity(intent);
				return true;
			}
		});
		handler.postDelayed(runnable, 100);

	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	public void onResume() {
		super.onResume();
		isRunning = true;
		registerReceiver(gcmReceiver, gcmFilter);

		ImageView profileImage = (ImageView)findViewById(R.id.preferecneimageview);
		int profileId = Login.getProfileId(loginSettings);
		if(profileImage != null){
			profileImage.setImageResource(Constants.profileArray[profileId]);
		}
	}

	protected void onPause(){
		super.onPause();
		isRunning = false;

		unregisterReceiver(gcmReceiver);
	}

	public void evaluateResponse(String message){
		JSONObject response = null;

		try {
			response = new JSONObject(message);

			if(response.has("id")){
				if(response.has("username")){
					String username = response.getString("username");
					Login.storeUsername(loginSettings, username);
					editTextUsername.setSummary(username);
					Toast.makeText(this, "Username updated", Toast.LENGTH_LONG).show();
				}
				else if(response.has("email")){
					String email = response.getString("email");
					Login.setEmail(loginSettings, email);
					editTextEmail.setSummary(email);
					Toast.makeText(this, "Email updated", Toast.LENGTH_LONG).show();

				}
				else if(response.has("password")){
					Login.storePassword(loginSettings, password);
					Toast.makeText(this, "Password updated", Toast.LENGTH_LONG).show();
				}
			}
			else {
				new Alert("Ups..", response.getString("error"), this);
			}
		}
		catch (JSONException e) { 
			new Alert("Uups..", message, this);
			//Editor editor = prefs.edit();
			//editor.putString("pw", " ");
			e.printStackTrace();
		}
	}


	public void postLoginInfo(String tag, String value, String URL){
		progDialog = new 
				ProgressDialogClass(this, 
						"Updating", 
						"Updating your profile, please wait a moment",
						15000);

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
}