/**
 * 
 */
package com.main.helper;

import java.util.ArrayList;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import uit.nfc.AsynchronousHttpClient;
import uit.nfc.ResponseListener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.Facebook.DialogListener;
import com.main.R;
import com.main.activitys.AllGamesActivity;
import com.main.activitys.GetAddedFriendsActivity;
import com.main.activitys.NewGameActivity;
import com.main.activitys.domain.Login;

/**
 * @author Simen
 *
 */
public class CommonFunctions {
	public static int safeLongToInt(long l) {
		if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
			throw new IllegalArgumentException
			(l + " cannot be cast to int without changing its value.");
		}
		return (int) l;
	}

	public static void giveUp(int gameId, ResponseListener addFriendListener, SharedPreferences loginSettings){
		JSONObject postBody = new JSONObject();

		try {
			postBody.put("GID", gameId);
		}
		catch (JSONException e) { e.printStackTrace(); }

		HttpPost httpPost = BuildHttpRequest.setEntity(postBody, AllGamesActivity.GIVE_UP_URL);
		AsynchronousHttpClient a = new AsynchronousHttpClient();
		a.sendRequest(httpPost, addFriendListener, loginSettings);
	}

	public static void evaluateResponseGiveUp(Context context, String message){

		Log.i("evaluateResponse", message);

		try {
			JSONObject response = new JSONObject(message);

			if(response.has("error")){
				new Alert("Uups", response.getString("error"), context);
			}
			else{
				Toast.makeText(context, "Added " + response.getString("username") +  " to your friendslist", Toast.LENGTH_LONG).show(); 
			}
		}
		catch (JSONException e) { 
			e.printStackTrace();
			new Alert("Uups", "Ups, something fishy happpend", context);
		}
	}
	
	public static void startGameFromUsername(String username,  int type, ResponseListener responseListener, SharedPreferences loginSettings){
		JSONObject postBody = new JSONObject();

		try {
			postBody.put("userId", Login.getUserId(loginSettings));
			postBody.put("opnUsername", username);
			postBody.put("type", type);
		}

		catch (JSONException e) { e.printStackTrace(); }
		
		HttpPost httpPost = BuildHttpRequest.setEntity(postBody, NewGameActivity.usernameUrl);

		AsynchronousHttpClient a = new AsynchronousHttpClient();
		a.sendRequest(httpPost, responseListener, loginSettings);
	}
	
	public static void insertRecentPlayers(Context context, int fid, String username){
		DbAdapter mDbHelper = new DbAdapter(context);
		mDbHelper.open();
		mDbHelper.insertNewPlayerImageRecent(fid, username);
		mDbHelper.close();
	}
	
	public static void insertRecentPlayer(Context context, String username){
		SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
		String recent = prefs.getString("recent", "null");
		
		Log.d("insertRecentPlayer", recent);
		
		try {
			ArrayList<String> listRecent = new ArrayList<String>();
			JSONArray jarray = new JSONArray(recent);
			
  		for(int i=0; i < jarray.length(); i++){
  			String nameInList = jarray.getString(i);
  			
  			if(nameInList.equals(username)){
  				listRecent.add(0, username);
  			}
  			else {
  				listRecent.add(nameInList);
  			}
  		}
  		Editor e = prefs.edit();
  		e.putString("recent", new JSONArray(listRecent).toString());
  		e.commit();
		}
		catch(JSONException e){ e.printStackTrace(); }
	}

	public static void evaluateAddFriend(String message, Context context){
		Log.i("evaluateResponse", message);
		try {
			JSONObject response = new JSONObject(message);

			if(response.has("error"))
				new Alert("Uups", response.getString("error"), context);
			else if(response.has("sendingRequest")){
				Toast.makeText(context, "Friend request sent", Toast.LENGTH_LONG).show();
			}
			else
				Toast.makeText(context, "Added " + response.getString("username") +  " to your friendslist", Toast.LENGTH_LONG).show(); 
		}
		catch (JSONException e) { 
			e.printStackTrace();
			new Alert("Uups", "Ups, something fishy happpend", context);
		}
	}
	
	public static void sendFriendRequest(String key, String friend, SharedPreferences loginSettings, final Context context){
		ResponseListener addFriendListener= new ResponseListener() {
			@Override
			public void onResponseReceived(HttpResponse response, String message) {
				Log.i("Response", response.toString());
				evaluateAddFriend(message, context);
			}
		};

		JSONObject postBody = new JSONObject();
		Log.i("friend", friend);
		
		try {
			postBody.put(key, friend);
		}
		catch (JSONException e) { e.printStackTrace(); }
		Resources res = context.getResources();

		HttpPost httpPost = BuildHttpRequest.setEntity(postBody, res.getString(R.string.sendFriendRequestUrl));
		AsynchronousHttpClient a = new AsynchronousHttpClient();
		a.sendRequest(httpPost, addFriendListener, loginSettings);
	}
	
	public static void sendAddFriend(int friend, SharedPreferences loginSettings, final Context context){
		ResponseListener addFriendListener= new ResponseListener() {
			@Override
			public void onResponseReceived(HttpResponse response, String message) {
				Log.i("Response", response.toString());
				evaluateAddFriend(message, context);
			}
		};

		JSONObject postBody = new JSONObject();
		
		try {
			postBody.put("opponentId", friend);
		}
		catch (JSONException e) { e.printStackTrace(); }
		
		Resources res = context.getResources();

		HttpPost httpPost = BuildHttpRequest.setEntity(postBody, res.getString(R.string.sendAddFriendUrl));
		AsynchronousHttpClient a = new AsynchronousHttpClient();
		a.sendRequest(httpPost, addFriendListener, loginSettings);
	}
	
	@SuppressLint("NewApi")
	public static Notification createNotification(Context arg0, int notficationId, String ticker, String title, String contextText, int drawableIdSmall, int drawableIdBig){
		Intent notificationIntent = new Intent(arg0, AllGamesActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(arg0,
				notficationId, notificationIntent,
		        PendingIntent.FLAG_CANCEL_CURRENT);

		Resources res = arg0.getResources();
		Notification.Builder builder = new Notification.Builder(arg0);

		builder.setContentIntent(contentIntent)
		            .setSmallIcon(drawableIdSmall)
		            .setLargeIcon(BitmapFactory.decodeResource(res, drawableIdBig))
		            .setTicker(ticker)
		            .setWhen(System.currentTimeMillis())
		            .setAutoCancel(true)
		            .setContentTitle(title)
		            .setContentText(contextText);
		Notification n = builder.getNotification();
		return n;
	}
	
	public static void specialAlert(String title, final int friend, String message, final Context context, final SharedPreferences loginSettings){
		new AlertDialog.Builder(context)
		.setTitle(title)
		.setMessage(message)
		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				CommonFunctions.sendAddFriend(friend, loginSettings, context);
			}
		}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Do nothing.
			}
		}).show();
	}
	
	public static void alertForAddFriend(String title, final int friend, 
			String message, final Context context, final SharedPreferences loginSettings){

		new AlertDialog.Builder(context)
		.setTitle(title)
		.setMessage(message)
		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				CommonFunctions.sendAddFriend(friend, loginSettings, context);
			}
		}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Do nothing.
			}
		}).show();
	}
	
	public static Facebook setFacebookToken(Facebook facebook, SharedPreferences mPrefs){
		String access_token = mPrefs.getString("access_token", null);
		long expires = mPrefs.getLong("access_expires", 0);

		if(access_token != null) {
			facebook.setAccessToken(access_token);
		}
		if(expires != 0) {
			facebook.setAccessExpires(expires);
		}

		return facebook;
	}
	
	public static void loginToFacebook(Context ctx, final Facebook facebook, final SharedPreferences mPrefs){
		facebook.authorize((Activity) ctx, new String[] {}, new DialogListener() {
			@Override
			public void onComplete(Bundle values) {
				SharedPreferences.Editor editor = mPrefs.edit();
				editor.putString("access_token", facebook.getAccessToken());
				editor.putLong("access_expires", facebook.getAccessExpires());
				editor.commit();
				Log.i("onComplete", "complete");
			}

			@Override
			public void onFacebookError(FacebookError error) {}

			@Override
			public void onError(DialogError e) {}

			@Override
			public void onCancel() {}
		});
	}
}
