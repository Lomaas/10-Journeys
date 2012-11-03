/**
 * 
 */
package com.main.activitys;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONException;
import org.json.JSONObject;
import uit.nfc.AsynchronousHttpClient;
import uit.nfc.ResponseListener;

import com.main.helper.Alert;
import com.main.helper.BuildHttpRequest;
import com.main.helper.CommonFunctions;
import com.main.helper.DbAdapter;
import com.main.activitys.domain.Login;
import com.main.helper.ProgressDialogClass;
import com.main.*;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import com.markupartist.android.widget.ActionBar.IntentAction;

import android.widget.EditText;

/**
 * @author Simen
 *
 */
public class NewGameActivity extends Activity {
	public static String randomUrl = "http://restfulserver.herokuapp.com/game/new_random";
	public static String usernameUrl = "http://restfulserver.herokuapp.com/game/new_username";
	public static String emailUrl = "http://restfulserver.herokuapp.com/game/new_email";

	private String username;
	private String email;

	private ResponseListener responseListener;
	private ResponseListener randomResponseListener;
	public SharedPreferences loginSettings;

	public ProgressDialogClass progDialog;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.new_game);

		final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("New game");
		actionBar.setHomeAction(new Action() {

			@Override
			public void performAction(View view) {
				finish();
			}

			@Override
			public int getDrawable() {
				return R.drawable.arrow_left;
			}
		});

		loginSettings = getSharedPreferences(Login.PREFS_NAME, 0);

		responseListener = new ResponseListener() {
			@Override
			public void onResponseReceived(HttpResponse response, String message) {
				Log.i("Response", response.toString());
				progDialog.dissMissProgressDialog();
				evaluateResponse(message);
			}
		};

		randomResponseListener = new ResponseListener() {
			@Override
			public void onResponseReceived(HttpResponse response, String message) {
				Log.i("Response", response.toString());
				evaluateResponse(message);
			}
		};
	}

	protected void onResume(){
		super.onResume();

		loginSettings = getSharedPreferences(Login.PREFS_NAME, 0);
		if(Login.isSessionExpired(loginSettings)){
			Intent loginIntent = new Intent(this, LoginActivity.class);
			startActivity(loginIntent);
			finish();
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d("result ok", Integer.toString(RESULT_OK));

		Log.d("resultCode", Integer.toString(resultCode));
		Log.d("requestCode", Integer.toString(requestCode));

		if (requestCode == 1) {
			Log.d("ONACTIVITYRESULT","TEST");

			if(resultCode == RESULT_OK){
				Log.d("ONACTIVITYRESULT","inside resultcode random gamereq resp");
				finish();
			}
		}
	}

	private void evaluateResponse(String message){
		Log.i("evaluateResponse", message);

		try {
			JSONObject response = new JSONObject(message);
			EditText editText = new EditText(this);

			if(response.has("error") && response.has("username")){
				editText.setText(username);
				specialAlert("Create game", response.getString("error"), this, editText);
			}

			else if(response.has("error") && response.has("email")){
				editText.setText(email);
				specialAlert("Create game", response.getString("error"), this, editText);
			}
			else if(response.has("warning")){
				Intent i = new Intent();

				Log.i("evaluateResponse", message);

				setResult(AllGamesActivity.RANDOM_GAMEREQ_RESP, i);
				finish();
			}
			else{
				int resultCode = Activity.RESULT_CANCELED;
				Intent returnIntent = new Intent();

				if(response.has("error"))
					new Alert("Uups", response.getString("error"), this);

				else{
					returnIntent.putExtra("gameInfo", response.toString());
					Log.d("gameinfo", response.toString());
					resultCode = Activity.RESULT_OK;
				}
				if (getParent() == null) {
					setResult(resultCode, returnIntent);
				} else {
					getParent().setResult(resultCode, returnIntent);
				}
				finish();
			}
		}

		catch (JSONException e) { e.printStackTrace(); }
	}

	public void findOpponentByFriendList(View v){
		Intent intent = new Intent().setClass(this, InviteFriendTabActivity.class);
		intent.putExtra("type", 1);
		startActivityForResult(intent, AllGamesActivity.RANDOM_GAMEREQ_RESP);
	}

	public void findOpponentByEmailUsername(View v){
		specialAlert("Create game", "Enter your opponents email/username address", this, new EditText(this));
	}	

	public void findOpponentRandom(View v){
		JSONObject postBody = new JSONObject();

		try {
			postBody.put("userId", Login.getUserId(loginSettings));
			postBody.put("type", 1);
		}
		catch (JSONException e) { e.printStackTrace(); }

		HttpPost httpPost = BuildHttpRequest.setEntity(postBody, randomUrl);

		AsynchronousHttpClient a = new AsynchronousHttpClient();
		a.sendRequest(httpPost, randomResponseListener, loginSettings);

		alert("Finding opponent", "Searching for an opponent to play against. This can take a while...", this);
	}

	private void sendEmail(String eMail){
		email = eMail;
		JSONObject postBody = new JSONObject();

		try {
			postBody.put("userId", Login.getUserId(loginSettings));
			postBody.put("email", eMail);
			postBody.put("type", 1);
		}

		catch (JSONException e) { e.printStackTrace(); }

		HttpPost httpPost = BuildHttpRequest.setEntity(postBody, emailUrl);

		AsynchronousHttpClient a = new AsynchronousHttpClient();
		a.sendRequest(httpPost, responseListener, loginSettings);
	}

	private void sendUsername(String us){
		username = us;
		JSONObject postBody = new JSONObject();

		try {
			postBody.put("userId", Login.getUserId(loginSettings));
			postBody.put("opnUsername", us);
			postBody.put("type", 1);
		}

		catch (JSONException e) { e.printStackTrace(); }

		HttpPost httpPost = BuildHttpRequest.setEntity(postBody, usernameUrl);

		AsynchronousHttpClient a = new AsynchronousHttpClient();
		a.sendRequest(httpPost, responseListener, loginSettings);
	}

	private void specialAlert(String title, String message, Context context, final EditText input){

		new AlertDialog.Builder(this)
		.setTitle(title)
		.setMessage(message)
		.setView(input)
		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				Editable value = input.getText();

				if(value.toString().contains("@"))
					sendEmail(value.toString());
				else
					sendUsername(value.toString());

				progDialog = new 
						ProgressDialogClass(NewGameActivity.this, 
								"Finding user", 
								"Looking up user, please wait a moment...");

				progDialog.run();
			}
		}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Do nothing.
			}
		}).show();
	}

	private void alert(String title, String message, Context context){
		new AlertDialog.Builder(context)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle(title)
		.setMessage(message)
		.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which){
				dialog.cancel();
				finish();
			}
		})
		.show();
	}
}
