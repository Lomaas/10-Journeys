/**
 * 
 */
package com.main.activitys;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uit.nfc.AsynchronousHttpClient;
import uit.nfc.ResponseListener;

import com.main.*;
import com.main.helper.Alert;
import com.main.helper.BuildHttpRequest;
import com.main.helper.CommonFunctions;
import com.main.helper.ProgressDialogClass;
import com.main.activitys.domain.Friend;
import com.main.activitys.domain.Login;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

/**
 * @author Simen
 *
 */
public class GetAddedFriendsActivity extends ListActivity {
	private ProgressDialogClass progDialog;
	private ResponseListener responseGetFriendsListener;
	private ResponseListener responseStartNewGameListener;
	private ResponseListener addFriendListener;

	private String getFriendsUrl = "http://restfulserver.herokuapp.com/user/get_friends";
	private String getNewGameUrl = "http://restfulserver.herokuapp.com/game/new_uid";
	public static String sendFriendRequest = "http://restfulserver.herokuapp.com/user/friend_request";
	private String name = null;
	private ProgressBar mProgress;

	public SharedPreferences loginSettings;
	private ArrayList<Friend> friendsList;
	private ArrayAdapter<String> adapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.invite_friend);

		friendsList = new ArrayList<Friend>();
		loginSettings = getSharedPreferences("loginInfo", 0);

		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
		setListAdapter(adapter);

		mProgress = (ProgressBar) findViewById(R.id.progressBar);

		responseGetFriendsListener = new ResponseListener() {
			@Override
			public void onResponseReceived(HttpResponse response, String message) {
				Log.i("Response", response.toString());
				mProgress.setVisibility(View.GONE);
				confirmResponse(message);
			}
		};
		responseStartNewGameListener = new ResponseListener() {
			@Override
			public void onResponseReceived(HttpResponse response, String message) {
				Log.i("Response", response.toString());
				progDialog.dissMissProgressDialog();
				evaluateStartGame(message);
			}
		};

		addFriendListener= new ResponseListener() {
			@Override
			public void onResponseReceived(HttpResponse response, String message) {
				Log.i("Response", response.toString());
				progDialog.dissMissProgressDialog();
				evaluateAddFriend(message);
			}
		};
		getFriends();
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

	@Override
	protected void onListItemClick(ListView list, View view, int position, long id) {
		super.onListItemClick(list, view, position, id);
		Log.d("onclick", "click");
		sendToServer(friendsList.get(position).getUid());
	}

	public void confirmResponse(String response){
		try {
			JSONArray array = new JSONArray(response);
			int i;
			for(i=0; i<array.length(); i++){				
				JSONObject obj = array.getJSONObject(i);

				Friend friend = new Friend();
				friend.setUid(CommonFunctions.safeLongToInt(obj.getLong("fid")));
				friend.setUsername(obj.getString("username"));
				friendsList.add(friend);
				adapter.add(friend.getUsername());
			}
		}
		catch(JSONException e){ e.printStackTrace(); }

		adapter.notifyDataSetChanged();
	}

	public void addNewFriend(View v){
		Log.d("onclick", "addNewFriend");
		specialAlert("Add friend", "Enter your friends email/username", this, new EditText(this));
	}

	public void getFriends(){
		mProgress.setVisibility(View.VISIBLE);
		HttpGet httpGet = null;

		try {
			httpGet = new HttpGet(new URI(getFriendsUrl));
			AsynchronousHttpClient a = new AsynchronousHttpClient();
			a.sendRequest(httpGet, responseGetFriendsListener, loginSettings);
		}
		catch (URISyntaxException e) { e.printStackTrace(); }
	}

	private void sendToServer(int UID){
		JSONObject postBody = new JSONObject();

		try {
			postBody.put("UID", UID);
			postBody.put("type", InviteFriendTabActivity.type);
		}

		catch (JSONException e) { e.printStackTrace(); }

		HttpPost httpPost = BuildHttpRequest.setEntity(postBody, getNewGameUrl);

		AsynchronousHttpClient a = new AsynchronousHttpClient();
		a.sendRequest(httpPost, responseStartNewGameListener, loginSettings);
		progDialog = new 
				ProgressDialogClass(this, 
						"Creating game", 
						"Creating game, please wait...");

		progDialog.run();
	}

	private void evaluateStartGame(String message){
		Log.i("evaluateResponse", message);

		try {
			JSONObject response = new JSONObject(message);

			if(response.has("error")){
				new Alert("Not found", response.getString("error"), this);
			}
			else{
				Intent data = new Intent();

				if (getParent() == null) {
					setResult(Activity.RESULT_OK, data);
				} else {
					getParent().setResult(Activity.RESULT_OK, data);
				}
				Log.i("evaluateResponse", message);
				finish();
			}
		}
		catch (JSONException e) { e.printStackTrace(); }
	}

	private void evaluateAddFriend(String message){
		Log.i("evaluateResponse", message);
		EditText editText = new EditText(this);

		try {
			JSONObject response = new JSONObject(message);

			if(response.has("error")){
				editText.setText(name);
				specialAlert("Not found", response.getString("error"), this, editText);
			}
			else if(response.has("sendingRequest")){
				Toast.makeText(this, "A friend request is sent", Toast.LENGTH_LONG).show();
			}
			else{
				Friend friend = new Friend();
				friend.setUid(CommonFunctions.safeLongToInt(response.getLong("fid")));
				friend.setUsername(response.getString("username"));
				friendsList.add(friend);
				adapter.add(friend.getUsername());
			}
		}
		catch (JSONException e) { 
			e.printStackTrace();
			specialAlert("Error", "An error occured. Try again", this, editText);
		}
	}

	public void sendFriendRequest(String key, String tmp, Context context){
		JSONObject postBody = new JSONObject();
		name = tmp;
		try {
			postBody.put(key, tmp);
		}

		catch (JSONException e) { e.printStackTrace(); }
		Resources res = context.getResources();

		HttpPost httpPost = BuildHttpRequest.setEntity(postBody, res.getString(R.string.sendFriendRequestUrl));

		AsynchronousHttpClient a = new AsynchronousHttpClient();
		a.sendRequest(httpPost, addFriendListener, loginSettings);
	}

	private void specialAlert(String title, String message, final Context context, final EditText input){

		new AlertDialog.Builder(this)
		.setTitle(title)
		.setMessage(message)
		.setView(input)
		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				Editable value = input.getText();
				if(value.toString().contains("@"))
					sendFriendRequest("email", value.toString(), context);
				else
					sendFriendRequest("username", value.toString(), context);

				progDialog = new 
						ProgressDialogClass(GetAddedFriendsActivity.this, 
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

}

