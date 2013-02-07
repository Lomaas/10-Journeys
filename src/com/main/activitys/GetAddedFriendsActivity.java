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
import com.main.activitys.domain.Game;
import com.main.activitys.domain.Login;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
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
	Context ctx = this;
	IntentFilter gcmFilter;
	private BroadcastReceiver gcmReceiver = null;
	public static int type = 0;


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
		
		Bundle extras = getIntent().getExtras();
		type = (Integer) extras.get("type");
		
		Resources res = getResources();
		
		final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("Invite friend");
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
		
		gcmReceiver = CommonFunctions.createBroadCastReceiver(ctx, loginSettings, CommonFunctions.FROM_STANDARD_ACTIVITY);
		gcmFilter = new IntentFilter();
		gcmFilter.addAction("GCM_RECEIVED_ACTION");
		
		if(gcmReceiver == null){
			Log.i("Response", "ZERO");
			Toast.makeText(this, "GCMRECEVIER ZERO", Toast.LENGTH_LONG).show();
		}
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
		setListAdapter(adapter);

		ListView lv = getListView(); 
		registerForContextMenu(lv);
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

	@Override
	protected void onListItemClick(ListView list, View view, int position, long id) {
		super.onListItemClick(list, view, position, id);
		Log.d("onclick", "click");
		sendToServer(friendsList.get(position).getUid());
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		Log.i("oncreatecontextMenu", "inside here");
		Log.i("ID", Integer.toString(v.getId()));

		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		Log.i("ID", Integer.toString(info.position));

		menu.setHeaderTitle("Options");  
		menu.add(0, 1, 0, "Remove friend");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		Log.i("ID", Integer.toString(info.position));

		Friend game = friendsList.get(info.position);

		switch (item.getItemId()) {
		case 1:
			removeFriend(game.getUid());
			removeFriendFromList(info.position, game.getUsername());
			
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	public void confirmResponse(String response){
		try {
			JSONArray array = new JSONArray(response);
			int i;
			
			if(array.length() == 0){
				TextView textView = (TextView)findViewById(R.id.textViewNoFriends);
				textView.setText("You have no friends yet");
				textView.setVisibility(View.VISIBLE);
			}
			for(i=0; i<array.length(); i++){				
				JSONObject obj = array.getJSONObject(i);
				addFriendToList(CommonFunctions.safeLongToInt(obj.getLong("fid")), obj.getString("username"));
			}
		}
		catch(JSONException e){ e.printStackTrace(); }

		adapter.notifyDataSetChanged();
	}
	
	public void removeFriend(int opponentId){
		ResponseListener addFriendListener= new ResponseListener() {
			@Override
			public void onResponseReceived(HttpResponse response, String message) {
				Log.i("Response", response.toString());
				evaluateRemoveFriend(message);
			}
		};

		JSONObject postBody = new JSONObject();

		try {
			postBody.put("opponentId", opponentId);
		}
		catch (JSONException e) { e.printStackTrace(); }
		Resources res = this.getResources();

		HttpPost httpPost = BuildHttpRequest.setEntity(postBody, res.getString(R.string.removeFriendUrl));
		AsynchronousHttpClient a = new AsynchronousHttpClient();
		a.sendRequest(httpPost, addFriendListener, loginSettings);
	}
	
	public void evaluateRemoveFriend(String message){
		try {
			JSONObject response = new JSONObject(message);

			if(response.has("error")){
				new Alert("Not found", response.getString("error"), this);
			}
			else{
				Toast.makeText(this, "Friend removed", Toast.LENGTH_SHORT).show();
			}
		}
		catch (JSONException e) { e.printStackTrace(); }
	}
	
	public void removeFriendFromList(int position, String username){
		adapter.remove(username);
		friendsList.remove(position);
	}
	public void addFriendToList(int fid, String username){
		Friend friend = new Friend();
		friend.setUid(fid);
		friend.setUsername(username);
		friendsList.add(friend);
		adapter.add(friend.getUsername());
		
		TextView textView = (TextView)findViewById(R.id.textViewNoFriends);
		textView.setText("");
		textView.setVisibility(View.GONE);
	}

//	public void startFacebook(View v){
//		Bundle params = new Bundle();
//		params.putString("message", getString(R.string.sendAppRequest));
//		Utility.mFacebook.dialog(GetAddedFriendsActivity.this, "apprequests", params,
//				new AppRequestsListener());
//	}

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
			postBody.put("type", type);
		}

		catch (JSONException e) { e.printStackTrace(); }

		HttpPost httpPost = BuildHttpRequest.setEntity(postBody, getNewGameUrl);

		AsynchronousHttpClient a = new AsynchronousHttpClient();
		a.sendRequest(httpPost, responseStartNewGameListener, loginSettings);
		
		progDialog = new 
				ProgressDialogClass(GetAddedFriendsActivity.this, 
						"Sending game request", 
						"Looking up user and sending game request, please wait a moment...",
						15000);

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
				if(response.has("error"))
					new Alert("Uups", response.getString("error"), this);
				else if(response.has("gameRequestSent")) {
					alert("Game request sent", "A game request has been sent to " + response.getString("opponent"), this);
				}
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
				TextView textView = (TextView)findViewById(R.id.textViewNoFriends);
				textView.setText("");
				textView.setVisibility(View.GONE);
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

		HttpPost httpPost = BuildHttpRequest.setEntity(postBody, res.getString(R.string.sendAddFriendUrl));

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
								"Looking up user, please wait a moment...",
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
				
				Intent data = new Intent();
				
				if (getParent() == null) {
					setResult(Activity.RESULT_OK, data);
				} else {
					getParent().setResult(Activity.RESULT_OK, data);
				}
				finish();
			}
		})
		.show();
	}
	
	/*
	 * callback for the apprequests dialog which sends an app request to user's
	 * friends.
	 */
//	public class AppRequestsListener extends BaseDialogListener {
//		@Override
//		public void onComplete(Bundle values) {
//			Toast toast = Toast.makeText(getApplicationContext(), "App request sent",
//					Toast.LENGTH_SHORT);
//			toast.show();
//		}
//
//		@Override
//		public void onFacebookError(FacebookError error) {
//			Toast.makeText(getApplicationContext(), "Facebook Error: " + error.getMessage(),
//					Toast.LENGTH_SHORT).show();
//		}
//
//		@Override
//		public void onCancel() {
//			//          Toast toast = Toast.makeText(getApplicationContext(), "App request cancelled",
//			//                  Toast.LENGTH_SHORT);
//			//          toast.show();
//		}
//	}
}

