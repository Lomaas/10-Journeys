/**
 * 
 */
package com.saimenstravelapp.helper;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;
import async.httprequest.AsynchronousHttpClient;
import async.httprequest.ResponseListener;

import com.google.android.gcm.GCMRegistrar;
import com.saimenstravelapp.*;
import com.saimenstravelapp.activitys.AllGamesActivity;
import com.saimenstravelapp.activitys.ChatActivity;
import com.saimenstravelapp.activitys.GameActivity;
import com.saimenstravelapp.activitys.GameFinishActivity;
import com.saimenstravelapp.activitys.NewGameActivity;
import com.saimenstravelapp.activitys.domain.Extrainfo;
import com.saimenstravelapp.activitys.domain.Login;
import com.saimenstravelapp.activitys.domain.Message;
import com.markupartist.android.widget.ActionBar;


public class CommonFunctions {
	public static String START_GAME_URL = "http://restfulserver.herokuapp.com/game/respond_to_game_request";
	public static int FROM_STANDARD_ACTIVITY = 1;
	public static int FROM_CHAT_ACTIVITY = 2;
	public static int FROM_GAME_ACTIVITY = 3;
	public static int FROM_FINISH_GAME_ACTIVITY = 4;
	public static int FROM_ALL_GAMESACTIVITY = 5;


	public static int safeLongToInt(long l) {
		if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
			throw new IllegalArgumentException
			(l + " cannot be cast to int without changing its value.");
		}
		return (int) l;
	}

	public static float round(float unrounded, int precision, int roundingMode){
		BigDecimal bd = new BigDecimal(unrounded);
		BigDecimal rounded = bd.setScale(precision, roundingMode);
		return rounded.floatValue();
	}

	public static String getMapFromType(int type){
		switch(type){
		case 1:
			return "Europe";
		case 2:
			return "America";
		default:
			return "Europe";
		}
	}

	public static int getMapFromString(String type){
		if(type.equals("Europe"))
			return 1;
		else if(type.equals("America"))
			return 2;

		return 0;	// if something weird happends!
	}


	public static void returnImageDrawableFromId(int imageId){

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
		try {
			JSONObject response = new JSONObject(message);

			if(response.has("error"))
				new Alert("Uups", response.getString("error"), context);
			else
				Toast.makeText(context, "Added " + response.getString("username") +  " to your friendslist", Toast.LENGTH_LONG).show(); 
		}
		catch (JSONException e) { 
			e.printStackTrace();
			new Alert("Uups", "Ups, something fishy happpend", context);
		}
	}

	public static void startGameFromUsername(final Context ctx, String username,  int type, ResponseListener responseListener, SharedPreferences loginSettings){
		if(responseListener == null){
			responseListener = new ResponseListener() {

				@Override
				public void onResponseReceived(HttpResponse response, String message) {
					try {
						JSONObject input = new JSONObject(message);

						if(input.getBoolean("gameRequestSent")){
							new Alert("Request sent", "Game request sent", ctx);
						}
					}
					catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			};
		}

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

	public static void postAnswerToGameRequest(final Context ctx, final int opponentId, int type, final SharedPreferences loginSettings, boolean accept){

		ResponseListener responseListener = new ResponseListener() {
			@Override
			public void onResponseReceived(HttpResponse response, String message) {
				// Fack you
				DbAdapter adapter = new DbAdapter(ctx);
				adapter.open();
				adapter.deleteGameRequest(opponentId);
				adapter.close();
			}
		};

		JSONObject postBody = new JSONObject();

		try {
			postBody.put("opponentId", opponentId);
			postBody.put("type", type);
			postBody.put("accept", accept);
		}

		catch (JSONException e) { e.printStackTrace(); }

		HttpPost httpPost = BuildHttpRequest.setEntity(postBody, START_GAME_URL);

		AsynchronousHttpClient a = new AsynchronousHttpClient();
		a.sendRequest(httpPost, responseListener, loginSettings);
	}


	public static void findNewGameRequests(Context ctx, SharedPreferences loginPreferences){
		DbAdapter adapter = new DbAdapter(ctx);
		adapter.open();
		Cursor cursor = adapter.fetchAllGameRequests();

		for (boolean hasItem = cursor.moveToFirst(); hasItem; hasItem = cursor.moveToNext()) {
			String message =  cursor.getString(2) + " has sent you an game invite. Confirm? (" + cursor.getString(1) + ")";
			specialAlertForGameRequest("Game request", message, cursor.getInt(0), CommonFunctions.getMapFromString(cursor.getString(1)), ctx, loginPreferences);
			//adapter.deleteGameRequest(cursor.getInt(0));
		}
		adapter.close();
	}


	public static void evaluateAddFriend(String message, Context context){
		try {
			JSONObject response = new JSONObject(message);

			if(response.has("error"))
				new Alert("Uups", response.getString("error"), context);
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
				evaluateAddFriend(message, context);
			}
		};

		JSONObject postBody = new JSONObject();

		try {
			postBody.put(key, friend);
		}
		catch (JSONException e) { e.printStackTrace(); }
		Resources res = context.getResources();

		HttpPost httpPost = BuildHttpRequest.setEntity(postBody, res.getString(R.string.sendAddFriendUrl));
		AsynchronousHttpClient a = new AsynchronousHttpClient();
		a.sendRequest(httpPost, addFriendListener, loginSettings);
	}

	public static void sendAddFriend(int friend, SharedPreferences loginSettings, final Context context){
		ResponseListener addFriendListener= new ResponseListener() {
			@Override
			public void onResponseReceived(HttpResponse response, String message) {
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

	public static void alertForGiveUp(final Context context, final int gameId, final ResponseListener giveUpListener, final SharedPreferences loginSettings, final int fromActivity){
		new AlertDialog.Builder(context)
		.setTitle("Confirm giveup")
		.setMessage("Do you want to continue?")
		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				CommonFunctions.giveUp(gameId, giveUpListener, loginSettings);

				if(fromActivity == FROM_ALL_GAMESACTIVITY){
					AllGamesActivity act = (AllGamesActivity) context;
					act.findAndRemoveGameObj(gameId);
					act.stopService();
				}
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

	public static void specialAlertForGameRequest(String title, String message, final int opponentId, final int type, final Context context, final SharedPreferences loginSettings){
		new AlertDialog.Builder(context)
		.setTitle(title)
		.setMessage(message)
		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				Log.i("opponentID", Integer.toString(opponentId));

				CommonFunctions.postAnswerToGameRequest(context, opponentId, type, loginSettings, true);
			}
		}).setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				CommonFunctions.postAnswerToGameRequest(context, opponentId, type, loginSettings, false);
			}
		}).show();
	}

	public static BroadcastReceiver createBroadCastReceiver(Context context, final SharedPreferences loginPreferences, final int fromActivity){
		return new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				Bundle extras = intent.getExtras();
				boolean shouldSetGameInfo = true;
				
				if(fromActivity == CommonFunctions.FROM_STANDARD_ACTIVITY){
					if(extras.containsKey("newChatMsg")){
						Extrainfo.setNewChatMsg(context.getSharedPreferences(Extrainfo.PREFS_NAME, 0), extras.getString("newChatMsg"), true);
					}
				}
				else if(fromActivity == CommonFunctions.FROM_CHAT_ACTIVITY){
					if(extras.containsKey("newChatMsg")){
						ChatActivity chatActivity = (ChatActivity) context;

						if(chatActivity.getGameId().equals(extras.getString("newChatMsg"))){
							chatActivity.addNewMessage(new Message(extras.getString("msg"), false));
						}
						else
							Extrainfo.setNewChatMsg(context.getSharedPreferences(Extrainfo.PREFS_NAME, 0), extras.getString("newChatMsg"), true);
					}
				}

				else if(fromActivity == CommonFunctions.FROM_GAME_ACTIVITY){
					GameActivity gameActivity = (GameActivity) context;

					if(extras.containsKey("cardDeckReset")){
						if(gameActivity.gameId == Integer.parseInt(extras.getString("gameId"))){
							new Alert("Card deck reset", "The card deck has been reset", context);
						}
						else {
							shouldSetGameInfo = false;
							CommonFunctions.setGameInfo(Integer.parseInt(extras.getString("gameId")), "The card deck has been reset", context);
						}
					}
					if(extras.containsKey("newChatMsg")){
						if(gameActivity.gameId == Integer.parseInt(extras.getString("newChatMsg")))
							gameActivity.setGotNewMessage();

						Extrainfo.setNewChatMsg(context.getSharedPreferences(Extrainfo.PREFS_NAME, 0), extras.getString("newChatMsg"), true);
					}
				}

				else if(fromActivity == CommonFunctions.FROM_FINISH_GAME_ACTIVITY){
					if(extras.containsKey("newChatMsg")){
						final GameFinishActivity gameFinishActivity = (GameFinishActivity) context;

						if(gameFinishActivity.gameId == Integer.parseInt(extras.getString("newChatMsg"))){
							ActionBar actionBar = (ActionBar) gameFinishActivity.findViewById(R.id.actionbar);
							actionBar.removeActionAt(0);
							gameFinishActivity.setChatIconRed();
						}

						Extrainfo.setNewChatMsg(context.getSharedPreferences(Extrainfo.PREFS_NAME, 0), extras.getString("newChatMsg"), true);
					}
				}


				if(extras.containsKey("generalMessage"))
					new Alert("System message", extras.getString("generalMessage"), context);

				else if(extras.containsKey("successRegWithGoogle"))
					CommonFunctions.postRegId(context, context.getSharedPreferences(Login.PREFS_NAME, 0));

				else if(extras.containsKey("gameRequest")){
					String message = extras.getString("gameRequest") + " has sent you an game invite. Confirm? (" + extras.getString("type") + ")";
					specialAlertForGameRequest("Game request", message, Integer.parseInt(extras.getString("opponentId")), CommonFunctions.getMapFromString(extras.getString("type")), context, loginPreferences);
				}

				else if(extras.containsKey("responseOnGameRequest")){
					if(extras.getString("responseOnGameRequest").equals("True"))
						new Alert("Game is being created",extras.getString("opponent") + " accepted your game request and a new game is being created", context);
					else
						new Alert("Decline",extras.getString("opponent") + " declined your game request", context);
				}
				else if(extras.containsKey("cardDeckReset")){
					if(shouldSetGameInfo == true){
						CommonFunctions.setGameInfo(Integer.parseInt(extras.getString("gameId")), "The card deck has been reset", context);
					}
				}
			}
		};
	}
	
	public static void setGameInfo(int gameId, String info, Context ctx){
		DbAdapter adapter = new DbAdapter(ctx);
		adapter.open();
		adapter.insertGameInfo(gameId, info);
	}
	
	public static void getGameInfo(int gameId, Context ctx){
		DbAdapter adapter = new DbAdapter(ctx);
		adapter.open();

		Cursor cursor = adapter.fetchAllGameInfo(gameId);
		
		for (boolean hasItem = cursor.moveToFirst(); hasItem; hasItem = cursor.moveToNext()) {
			if(cursor.getInt(0) == gameId){
				new Alert("Card deck reset", "The card deck has been reset", ctx);
				adapter.deleteGameInfo(gameId);
			}
		}
	}
	
	
	public static void checkIfRegIdIsExpired(Context ctx, SharedPreferences loginSettings){
  	Time now = new Time();
  	now.setToNow();
  	long milliseconds = now.toMillis(false);
	
  	if(Login.getTimeSinceReggedForPush(loginSettings) <= milliseconds){
  		GCMRegistrar.register(ctx, "84214609772");
			String regId = GCMRegistrar.getRegistrationId(ctx);
				
			milliseconds += GCMRegistrar.getRegisterOnServerLifespan(ctx);
	  	Login.setTimeReggedPush(loginSettings, milliseconds);
  	}
	}

	public static void regWithGoogleServer(Context ctx, SharedPreferences loginSettings){
		GCMRegistrar.checkDevice(ctx);
		GCMRegistrar.checkManifest(ctx);
//		Log.d("GCM", "regWithGoogleServer");

		String regId = GCMRegistrar.getRegistrationId(ctx);
		
		if (regId.equals("")) {
//			Log.d("GCM", "registration");

			GCMRegistrar.register(ctx, "84214609772");
			regId = GCMRegistrar.getRegistrationId(ctx);
//			Log.d("GCM", "regid" + regId);

	  	Time now = new Time();
	  	now.setToNow();
	  	long milliseconds = now.toMillis(false);
	  	milliseconds += GCMRegistrar.getRegisterOnServerLifespan(ctx);
	  	Login.setTimeReggedPush(loginSettings, milliseconds);
		} else {
//			Log.d("GCM", "isRegistrated");
			postRegId(ctx, loginSettings);

//			Toast.makeText(ctx, "already registered", Toast.LENGTH_LONG).show();
		}
		
	}

	public static void postRegId(final Context context, final SharedPreferences loginSettings){
		ResponseListener responseListener = new ResponseListener() {
			@Override
			public void onResponseReceived(HttpResponse response, String message) {
				try {
//					Log.d("regResponse", "response" + message);
					JSONObject obj = new JSONObject(message);
					if(obj.has("registred")){
						Login.setIsReggedPush(loginSettings);
					}
					else {
						try {
							Thread.sleep(10000);
						}
						catch (InterruptedException e) {
							e.printStackTrace();
						}
						postRegId(context, loginSettings);
					}
				}
				catch(JSONException e){ e.printStackTrace(); }
			}
		};

		HttpPost httpPost = null;
		StringEntity se = null;

		try {
			httpPost = new HttpPost(new URI(AllGamesActivity.REG_ID_URL));
			JSONObject post = new JSONObject();

			post.put("regId", Login.getGoogleRegistrationId(loginSettings));
//			Log.d("regId", "REGID: " + Login.getGoogleRegistrationId(loginSettings));
			se = new StringEntity(post.toString());
		}
		catch (URISyntaxException e1) { e1.printStackTrace(); }
		catch (UnsupportedEncodingException e) { e.printStackTrace(); }
		catch (JSONException e) { e.printStackTrace(); }

		se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
		httpPost.setEntity(se);

		AsynchronousHttpClient a = new AsynchronousHttpClient();
		a.sendRequest(httpPost, responseListener, loginSettings);
	}
	
	public static String getWinPercent(int totalGames, int totalWins){
		if(totalGames == 0)
			return "-";
		else{
			float fardin = CommonFunctions.round((float)totalWins/totalGames, 3, BigDecimal.ROUND_HALF_UP);
			float test = 100;
			String tmp = Float.toString(fardin*test);

			if(tmp.length() > 3){
				return tmp.substring(0, 4)+"%";
			}
			else{
				return tmp+"%";
			}
		}
	}
}
