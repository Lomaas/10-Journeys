/**
 * 
 */
package com.saimenstravelapp.activitys;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONException;
import org.json.JSONObject;

import com.saimenstravelapp.*;
import com.saimenstravelapp.activitys.domain.Login;
import com.saimenstravelapp.helper.Alert;
import com.saimenstravelapp.helper.BuildHttpRequest;
import com.saimenstravelapp.helper.CommonFunctions;
import com.saimenstravelapp.helper.ProgressDialogClass;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.Window;

import android.widget.EditText;
import async.httprequest.AsynchronousHttpClient;
import async.httprequest.ResponseListener;

/**
 * @author Simen
 *
 */
public class NewGameActivity extends Activity {
	public static String randomUrl = "http://restfulserver.herokuapp.com/game/new_random";
	public static String usernameUrl = "http://restfulserver.herokuapp.com/game/new_username";
	public static String emailUrl = "http://restfulserver.herokuapp.com/game/new_email";

	Context ctx = this;
	IntentFilter gcmFilter;
	private BroadcastReceiver gcmReceiver = null;

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
		loginSettings = getSharedPreferences(Login.PREFS_NAME, 0);
		gcmReceiver = CommonFunctions.createBroadCastReceiver(ctx, loginSettings, CommonFunctions.FROM_STANDARD_ACTIVITY);
		gcmFilter = new IntentFilter();
		gcmFilter.addAction("GCM_RECEIVED_ACTION");
		
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
		registerReceiver(gcmReceiver, gcmFilter);

		loginSettings = getSharedPreferences(Login.PREFS_NAME, 0);
		if(Login.isSessionExpired(loginSettings)){
			Intent loginIntent = new Intent(this, LoginActivity.class);
			startActivity(loginIntent);
			finish();
		}
	}
	
	protected void onPause(){
		super.onPause();
		unregisterReceiver(gcmReceiver);
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
				else if(response.has("gameRequestSent")) {
					alert("Game request sent", "A game request has been sent to " + response.getString("opponent"), this);
				}
//				else{
//					returnIntent.putExtra("gameInfo", response.toString());
//					Log.d("gameinfo", response.toString());
//					resultCode = Activity.RESULT_OK;
//				}
//				if (getParent() == null) {
//					setResult(resultCode, returnIntent);
//				} else {
//					getParent().setResult(resultCode, returnIntent);
//				}
//				finish();
			}
		}

		catch (JSONException e) { e.printStackTrace(); }
	}

	public void findOpponentByFriendList(View v){
		Intent intent = new Intent().setClass(this, GetAddedFriendsActivity.class);
		intent.putExtra("type", 1);
		startActivityForResult(intent, AllGamesActivity.RANDOM_GAMEREQ_RESP);
	}

	public void findOpponentByEmailUsername(View v){
		specialAlert("Create game", "Enter your opponents email/username", this, new EditText(this));
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

		alert("Finding opponent", "Searching for an opponent to play against. This can take some minutes...", this);
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
								"Sending game request", 
								"Looking up user and sending game request, please wait a moment...",
								15000);

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
