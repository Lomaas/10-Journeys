/**
 * 
 */
package com.main.activitys;

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

import com.google.android.gcm.GCMRegistrar;
import com.main.helper.*;
import com.main.activitys.domain.Login;
import com.main.helper.ProgressDialogClass;
import com.main.*;
import uit.nfc.AsynchronousHttpClient;
import uit.nfc.ResponseListener;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

/**
 * The Class LoginActivity. Main class for logging in
 * to become a user. Logging means that other users 
 * can see that you are at the event.
 */
public class LoginActivity extends Activity {
//	Facebook facebook = new Facebook("271971842906436");
//	AsyncFacebookRunner mAsyncRunner = new AsyncFacebookRunner(facebook);

	public String PREFS_NAME = "loginInfo";
	private String loginUrl = "http://restfulserver.herokuapp.com/user/login";

	String pword = null;
	private TextView username;
	private TextView password;
	private ProgressDialogClass progDialog;
	private ResponseListener responseListener;
	public SharedPreferences loginSettings;
	private SharedPreferences mPrefs;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.login);

		loginSettings = getSharedPreferences(PREFS_NAME, 0);
		Bundle extras = getIntent().getExtras();
		username = (TextView) findViewById(R.id.eTextLoginUsername);
		password = (TextView) findViewById(R.id.ePassword);

		Log.d("password", Login.getStoredPassword(loginSettings));

		responseListener = new ResponseListener() {
			@Override
			public void onResponseReceived(HttpResponse response, String message) {
				Log.i("Response", response.toString());
				progDialog.dissMissProgressDialog();
				confirmLogin(message);
			}
		};

		mPrefs = getPreferences(MODE_PRIVATE);
				

		if(extras != null && extras.containsKey("fromAllGamesActivity")){
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			username.setText(prefs.getString("username", "null"));
			password.setText(prefs.getString("password", "null"));
			postLoginInfo(prefs.getString("username", "null"), prefs.getString("password", "null"));
			return;
		}
		
		// if not session expired login!
		if(!Login.isSessionExpired(loginSettings) ){
			Log.i("sessionNOtExpired", "not expired");
			Intent allGamesActivity = new Intent().setClass(this, AllGamesActivity.class);
			startActivity(allGamesActivity);
			finish();			// can't return to this activity when signed in
			return;
		}

		/* Post loginInfo if registered */
		if(Login.isRegistered(loginSettings)){
			Log.i("isRegistered", "just posts info");
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			username.setText(prefs.getString("username", "null"));
			password.setText(prefs.getString("password", "null"));
			postLoginInfo(prefs.getString("username", "null"), prefs.getString("password", "null"));
		}
		else if(extras != null && extras.getBoolean("fromReg")){
			Log.d("fromReg", "fromReg");
		}
		else{
			Intent i = new Intent().setClass(this, RegisterActivity.class);
			startActivity(i);
			finish();
		}
	}

	public void evaluateLoginInfo(View v){
		if(username.getText().length() > 0 && password.getText().length() > 0){
			postLoginInfo(username.getText().toString(), password.getText().toString());
		}
		else {
			new Alert("Error", 
					"The username/password field has to be filled out", 
					LoginActivity.this);
		}
	}

	public void toRegisterActivity(View v){
		Intent registerAct = new Intent().setClass(this, RegisterActivity.class);
		startActivity(registerAct);
		finish();
	}

	public void postLoginInfo(String uname, String pw){
		progDialog = new 
				ProgressDialogClass(this, 
						"Signing in", 
						"Verifying, please wait...",
						15000);

		progDialog.run();

		HttpPost httpPost = null;

		try {
			JSONObject registerInfo = new JSONObject();

			httpPost = new HttpPost(new URI(loginUrl));

			registerInfo.put("Username", uname);
			registerInfo.put("Password", pw);

			pword = pw;

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

	public void confirmLogin(String responseBody){
		Log.i("ConfirmLogin", responseBody);

		JSONObject response = null;

		try {
			response = new JSONObject(responseBody);

			if(response.has("UID")){
				Login.setUserId(loginSettings, response.getInt("UID"));
				Login.setLoggedInRightNow(loginSettings);

				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

				Editor editor = prefs.edit();
				editor.putString("username", response.getString("Username"));
				editor.putString("email", response.getString("Email"));				
				editor.putString("password", pword);
				editor.commit();

				Intent allGamesActivity = new Intent().setClass(this, AllGamesActivity.class);
				startActivity(allGamesActivity);
				finish();			// can't return to this activity when signed in
			}
			else {
				new Alert("Error",  response.getString("error"),this);
			}
		}
		catch (JSONException e) { 
			e.printStackTrace(); 
			new Alert("Ups",  "An error occured, please try to log in again", this);

		}


	}
}