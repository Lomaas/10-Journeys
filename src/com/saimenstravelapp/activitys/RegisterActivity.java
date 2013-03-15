package com.saimenstravelapp.activitys;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gcm.GCMRegistrar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import async.httprequest.AsynchronousHttpClient;
import async.httprequest.ResponseListener;

import com.saimenstravelapp.*;
import com.saimenstravelapp.activitys.domain.Extrainfo;
import com.saimenstravelapp.activitys.domain.Login;
import com.saimenstravelapp.helper.*;

/**
 * The Class RegisterActivity. User registration
 * 
 */
public class RegisterActivity extends Activity {

	private String registerUrl = "http://restfulserver.herokuapp.com/user/new";
	protected String password;
	private String username;

	private EditText editEmail;
	private EditText editUsername;

	private ResponseListener responseListener;
	public SharedPreferences loginSettings;
	public SharedPreferences extraInfoSettings;
	public ProgressDialogClass progDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		loginSettings = getSharedPreferences(Login.PREFS_NAME, 0);
		setContentView(R.layout.register);

		editEmail = (EditText) findViewById(R.id.eTextRegEmail);
		editUsername = (EditText) findViewById(R.id.eTextRegUsername);

		extraInfoSettings = getSharedPreferences(Extrainfo.PREFS_NAME, 0);

		Extrainfo.setAllGamesUrl(extraInfoSettings, "http://restfulserver.herokuapp.com/games");
		Extrainfo.setGameUrl(extraInfoSettings, "http://restfulserver.herokuapp.com/game/");
		
		responseListener = new ResponseListener() {
			@Override
			public void onResponseReceived(HttpResponse response, String message) {
				Log.i("Response", message);
				Header tmp;
				HeaderIterator iter = response.headerIterator();

				while(iter.hasNext()){
					tmp = iter.nextHeader();
					Log.i("header-Name", tmp.getName());
					Log.i("header- Value", tmp.getValue());
				}
				progDialog.dissMissProgressDialog();
				confirmRegistration(message);
			}
		};
	}

	/**
	 * Confirms that the registration went OK
	 * @param responseBody
	 */

	public void confirmRegistration(String responseBody){
		Log.i("ConfirmRegistartion", responseBody);

		JSONObject response = null;

		try {
			response = new JSONObject(responseBody);

			if(!response.has("error") && response.has("id")){

				Login.setUserId(loginSettings, response.getInt("id"));
				Login.setRegistered(loginSettings);

				Login.storeUsername(loginSettings, response.getString("Username"));
				Login.setEmail(loginSettings, response.getString("Email"));
				Login.storePassword(loginSettings, password);
				Login.setLoggedInRightNow(loginSettings);
				Login.setNotReggedPush(loginSettings);
				//GCMRegistrar.unregister(this);
				
				startAllGamesActivity(this);
				finish();
			}
			else {
				new Alert("Unvalid registration", response.getString("error"), this);
			}
		}
		catch(Exception e) { 
			new Alert("Email or username taken", "The email or username you typed in is already occupied", this);
			e.printStackTrace(); 
		}
	}

	public void toLoginActivity(View v){
		Intent toLogin = new Intent().setClass(this, LoginActivity.class);
		toLogin.putExtra("fromReg", true);
		startActivity(toLogin);
		finish();
	}

	/**
	 * Starts the all games activity
	 *
	 */
	public void startAllGamesActivity(Context c){
		Intent allGamesActivity = new Intent().setClass(c, AllGamesActivity.class);
		allGamesActivity.putExtra("fromRegisterActivity", true);
		startActivity(allGamesActivity);
		finish();			// can't return to this activity when signed in
	}


	/**
	 * Called when a user press the register button
	 * @param v
	 */

	public void postRegisterInfo(View v){
		JSONObject postBody = new JSONObject();

		try {
			if(!validInputData())
				return;

			postBody.put("Username", username);
			postBody.put("Email", editEmail.getText());

			password = Secure.getString(getBaseContext().getContentResolver(),
					Secure.ANDROID_ID);

			postBody.put("Password", password);

			HttpPost httpPost = BuildHttpRequest.setEntity(postBody, registerUrl);

			progDialog = new 
					ProgressDialogClass(this, 
							"Signing in", 
							"Creating user, please wait...",
							15000);

			progDialog.run();


			AsynchronousHttpClient a = new AsynchronousHttpClient();
			a.sendRequest(httpPost, responseListener, loginSettings);
		}

		catch (JSONException e) { e.printStackTrace(); }
	}


	public final static boolean isValidEmail(CharSequence target) {
		try {
			return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
		}
		catch(NullPointerException exception) { return false; }
	}

	public boolean validInputData(){

		if((editUsername.getText().length() > 0) && (editEmail.getText().length() > 0)){
			if(!isValidEmail(editEmail.getText().toString())){
				new Alert("Email not valid", "The email you typed in is not valid. Please type in a valid email address", this);
				return false;
			}

			username = editUsername.getText().toString().trim();

			if(username.contains(" ")){
				new Alert("Username not valid", "The username you typed in contatins whitespaces Please remove them and try again", this);
				return false;
			}
		}
		else {
			new Alert("Warning", "Both email and username has to be filled out", this);
			return false;
		}

		return true;
	}
}

