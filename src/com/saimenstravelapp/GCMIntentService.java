/**
 * 
 */
package com.saimenstravelapp;


import java.util.Random;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ActivityManager;
import com.google.android.gcm.GCMBaseIntentService;
import com.saimenstravelapp.activitys.AllGamesActivity;
import com.saimenstravelapp.activitys.domain.Extrainfo;
import com.saimenstravelapp.activitys.domain.Login;
import com.saimenstravelapp.helper.CommonFunctions;
import com.saimenstravelapp.helper.DbAdapter;

/**
 * @author Simen
 *
 */
public class GCMIntentService extends GCMBaseIntentService{
	public static String TAG = "GCMIntentService";
	private static final String TOKEN =
      Long.toBinaryString(new Random().nextLong());

	public static final String SENDER_ID = "84214609772";
	private static final int APP_ID = 0;

	public GCMIntentService(){
		super(SENDER_ID);
	}

	@Override
	protected void onError(Context context, String error) {
		Log.i("gcmIntentService", "error" + error);
		SharedPreferences loginSettings = getSharedPreferences(Login.PREFS_NAME, Activity.MODE_PRIVATE);
		
		if (error != null) {
			 if ("SERVICE_NOT_AVAILABLE".equals(error)) {
			   long backoffTimeMs = Login.getBackOffTime(loginSettings);
			   long nextAttempt = SystemClock.elapsedRealtime() + backoffTimeMs;
			   
			   Intent retryIntent = new Intent("com.example.gcm.intent.RETRY");
			   retryIntent.putExtra("token", TOKEN);
			   
			   PendingIntent retryPendingIntent =
			       PendingIntent.getBroadcast(context, 0, retryIntent, 0);
			   AlarmManager am = (AlarmManager)   
			       context.getSystemService(Context.ALARM_SERVICE);
			   am.set(AlarmManager.ELAPSED_REALTIME, nextAttempt, retryPendingIntent);
			   backoffTimeMs *= 2; // Next retry should wait longer.
			   
			   Login.storeBackOffTime(loginSettings, backoffTimeMs); // update back-off time on shared preferences
			 } else {
			   // Unrecoverable error, log it
			   Log.i(TAG, "Received error: " + error);
			}
		}
	}

	@Override
	protected void onRegistered(Context arg0, String arg1) {
		Log.i("gcmIntentService - onRegId:", "onReg" + arg1);
		Login.storeGoogleRegistrationId(getSharedPreferences(Login.PREFS_NAME, Activity.MODE_PRIVATE), arg1);
		sendGCMIntent(arg0, "successRegWithGoogle", "jepp", null, null, null, null);
	}	
	

	@Override
	protected void onUnregistered(Context arg0, String arg1) {
		Log.i("gcmIntentService", "onUreg");
		Login.setNotReggedPush(getSharedPreferences(Login.PREFS_NAME, Activity.MODE_PRIVATE)); 
	}


	@Override
	protected void onMessage(Context arg0, Intent arg1) {
		Log.i("gcmIntentService", "onMsg sfjkslfjksljfkslfjksl jfklsjfklsjf klsjflkj");
		Resources res = arg0.getResources();
		Bundle extras = arg1.getExtras();
		

		// IF app is not screened. Show notification
		if (!arg0.getPackageName().equalsIgnoreCase(((ActivityManager)arg0.getSystemService(arg0.ACTIVITY_SERVICE)).getRunningTasks(1).get(0).topActivity.getPackageName()) &&
				wantsNotfications()){
			Intent notificationIntent = new Intent(arg0, AllGamesActivity.class);
			
			if(extras.containsKey("userIdTo")){
				String userIdTo = extras.getString("userIdTo");
				Log.d("userIdTo", userIdTo);
				if(Integer.parseInt(userIdTo) != Login.getUserId(getSharedPreferences(Login.PREFS_NAME, Activity.MODE_PRIVATE)))
					return;
			}
			
			if(extras.containsKey("fid")){
				// Friend request
				String opponentUsername = extras.getString("opponentUsername");
				String fid = extras.getString("fid");

				Log.i("opponentUsername", opponentUsername);
				Log.i("fid", fid);

				notificationIntent.putExtra(AllGamesActivity.NOTIFICATION_FRIEND_REQUEST, true);
				notificationIntent.putExtra("fid", Integer.parseInt(fid));
				notificationIntent.putExtra("opponentUsername", opponentUsername);

				launchNotfication(arg0, "Friend request", "Friend request from " + opponentUsername, notificationIntent);
			}
			else if(extras.containsKey("yourTurn")){
				// Your turn
				String yourTurn = extras.getString("yourTurn");
				String action = extras.getString("action");
				Log.i("gcmIntentServiceOnMsg", yourTurn);

				notificationIntent.putExtra(AllGamesActivity.NOTIFICATION_YOUR_TURN, true);
				launchNotfication(arg0, "Your turn", "It is your turn. " + action, notificationIntent);
			}
			else if(extras.containsKey("newGame")){
				Log.i("gcmIntentServiceOnMsg", "newGame");

				// Your turn
				String opponent = extras.getString("opponent");
				String action = extras.getString("action");
				String type = extras.getString("type");

				notificationIntent.putExtra(AllGamesActivity.NOTIFICATION_NEW_GAME, true);

				launchNotfication(arg0, "New game", "New game created vs " + opponent + ". Map: " + type, notificationIntent);
			}
			else if(extras.containsKey("newChatMsg")){
				Extrainfo.setNewChatMsg(arg0.getSharedPreferences(Extrainfo.PREFS_NAME, 0), extras.getString("gameId"), true);
			}
			else if(extras.containsKey("gameRequest")){
				String type = extras.getString("type");
				launchNotfication(arg0, "New game request", extras.getString("opponent") + " has invited you to a game ("+ type + ")", notificationIntent);
				Log.i("opponetId-notfication", extras.getString("opponentId"));
				DbAdapter adapter = new DbAdapter(arg0);
				adapter.open();
				adapter.insertNewGameRequest(Integer.parseInt(extras.getString("opponentId")), extras.getString("type"), extras.getString("opponent"));
				adapter.close();
			}
			else if(extras.containsKey("cardDeckReset")){
				CommonFunctions.setGameInfo(Integer.parseInt(extras.getString("gameId")), "The card deck has been reset", arg0);
			}
		}
		else {
			// IF app is screened show popup msg
			if(extras.containsKey("userIdTo")){
				String userIdTo = extras.getString("userIdTo");
				Log.d("userIdTo", userIdTo);
				if(Integer.parseInt(userIdTo) != Login.getUserId(getSharedPreferences(Login.PREFS_NAME, Activity.MODE_PRIVATE)))
					return;
			}
			
			if(extras.containsKey("friendAdded")){
				Log.d("gcmIntentService", "friend add");
				sendGCMIntent(arg0, "friendAdded", "true", "opponentUsername", extras.getString("opponentUsername"), null, null);
			}
			else if(extras.containsKey("fid")){
				String fid = extras.getString("fid");
				String opponentUsername = extras.getString("opponentUsername");
				sendGCMIntent(arg0, "fid", fid, "opponentUsername", opponentUsername, null, null);
			}
			else if(extras.containsKey("generalMessage")){
				sendGCMIntent(arg0, "generalMessage", extras.getString("generalMessage"), null, null, null, null);
			}
			else if(extras.containsKey("newChatMsg")){
				sendGCMIntent(arg0, "newChatMsg", extras.getString("gameId"), "userId", extras.getString("userId"), "msg", extras.getString("msg"));
			}
			else if(extras.containsKey("gameRequest")){
				Log.i("got GCM msg", extras.getString("opponent"));
				Log.i("got GCM msg", extras.getString("opponentId"));
				
				DbAdapter adapter = new DbAdapter(arg0);
				adapter.open();
				adapter.insertNewGameRequest(Integer.parseInt(extras.getString("opponentId")), extras.getString("type"), extras.getString("opponent"));
				adapter.close();

				String type = extras.getString("type");
				sendGCMIntent(arg0, "gameRequest", extras.getString("opponent"), "opponentId", extras.getString("opponentId"), "type", type);
			}
			else if(extras.containsKey("responseOnGameRequest")){
				sendGCMIntent(arg0, "responseOnGameRequest", extras.getString("responseOnGameRequest"), "opponent", extras.getString("opponent"), null, null);
			}
			else if(extras.containsKey("tryedToValidateRoute")){
				sendGCMIntent(arg0, "tryedToValidateRoute", "yes", "cards", extras.getString("cards"), null, null);
			}
			else if(extras.containsKey("cardDeckReset")){
				sendGCMIntent(arg0, "cardDeckReset", "", "gameId", extras.getString("gameId"), null, null);
			}
		}
	}

	public void launchNotfication(Context ctx, String header, String mainText, Intent notificationIntent){
		NotificationManager mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		Notification notification = new Notification(R.drawable.ic_launcher,
				header, System.currentTimeMillis());
		notification.setLatestEventInfo(ctx,
				getResources().getString(R.string.app_name), mainText,
				PendingIntent.getActivity(this.getBaseContext(), 0, notificationIntent,
						PendingIntent.FLAG_CANCEL_CURRENT));
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.defaults = Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE;
		mManager.notify(APP_ID, notification);
	}
	
	private void handleMessage( final Intent intent )
	{
		Thread t = new Thread()
		{
			public void run()
			{
				Bundle extras = intent.getExtras();
				Message myMessage = new Message();
				Bundle resBundle = new Bundle();

				if(extras.containsKey("friendAdded")){
					Log.d("gcmIntentService", "friend add");
					resBundle.putBoolean( "friendAdded", true);
				}
				else if(extras.containsKey("fid")){
					String fid = extras.getString("fid");
					String opponentUsername = extras.getString("opponentUsername");
					resBundle.putString("fid", fid);
					resBundle.putString("opponentUsername", opponentUsername);
				}

				myMessage.setData( resBundle );
				handler.sendMessage( myMessage );
			}
		};
		t.start();
	}

	private Handler handler = new Handler()
	{

		public void handleMessage( Message msg )
		{
			Log.i("handleMsg", "handle it!");
			Bundle extras = msg.getData();

			if(extras.containsKey("friendAdded")){
				Log.d("gcmIntentService", "friend add");

				Toast.makeText(getBaseContext(), extras.getString("opponentUsername") + " accepted your friend request", Toast.LENGTH_LONG).show();
			}
			else if(extras.containsKey("fid")){
				Log.d("f", "a");
			}
		}
	};

	private void sendGCMIntent(Context ctx, String key1, String msg1, String key2, String msg2, String key3, String msg3) {

		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction("GCM_RECEIVED_ACTION");
		
		if(key1 != null)
			broadcastIntent.putExtra(key1, msg1);
		
		if(key2 != null)
			broadcastIntent.putExtra(key2, msg2);
		
		if(key3 != null)
			broadcastIntent.putExtra(key3, msg3);

		ctx.sendBroadcast(broadcastIntent);
	}
	
	private boolean wantsNotfications(){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		return prefs.getBoolean("wantNotifications", true);
	}
}
