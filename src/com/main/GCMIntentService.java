/**
 * 
 */
package com.main;

import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ActivityManager;
import com.google.android.gcm.GCMBaseIntentService;
import com.main.activitys.AllGamesActivity;
import com.main.helper.CommonFunctions;

/**
 * @author Simen
 *
 */
public class GCMIntentService extends GCMBaseIntentService{
	private int SIMPLE_NOTFICATION_ID;
	public static String TAG = "GCMIntentService";

	public static final String SENDER_ID = "84214609772";

	public GCMIntentService(){
		super(SENDER_ID);
	}

	@Override
	protected void onError(Context arg0, String arg1) {
		Log.i("gcmIntentService", "onerror");
	}

	@SuppressLint("NewApi")
	@Override
	protected void onMessage(Context arg0, Intent arg1) {
		Log.i("gcmIntentService", "onMsg sfjkslfjksljfkslfjksl jfklsjfklsjf klsjflkj");
		Resources res = arg0.getResources();
		
		Bundle extras = arg1.getExtras();
		String opponentUsername = extras.getString("opponentUsername");
		String fid = extras.getString("fid");

		Log.i("opponentUsername", opponentUsername);
		Log.i("fid", fid);

		// IF app is not screened. Show notification
		if (!arg0.getPackageName().equalsIgnoreCase(((ActivityManager)arg0.getSystemService(arg0.ACTIVITY_SERVICE)).getRunningTasks(1).get(0).topActivity.getPackageName())){
			Intent notificationIntent = new Intent(arg0, AllGamesActivity.class);
			
			notificationIntent.putExtra(AllGamesActivity.NOTIFICATION_FRIEND_REQUEST, true);
			notificationIntent.putExtra("fid", Integer.parseInt(fid));
			notificationIntent.putExtra("opponentUsername", opponentUsername);
			
			PendingIntent contentIntent = PendingIntent.getActivity(arg0,
					SIMPLE_NOTFICATION_ID, notificationIntent,
					PendingIntent.FLAG_CANCEL_CURRENT);

			NotificationManager nm = (NotificationManager) arg0
					.getSystemService(Context.NOTIFICATION_SERVICE);

			Notification.Builder builder = new Notification.Builder(arg0);

			builder.setContentIntent(contentIntent)
			.setSmallIcon(R.drawable.ic_launcher)
			.setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.cards_serbia))
			.setTicker(res.getString(R.string.ticker_new_friend))
			.setWhen(System.currentTimeMillis())
			.setAutoCancel(true)
			.setContentTitle(res.getString(R.string.ticker_new_friend_title))
			.setContentText(res.getString(R.string.ticker_new_friend_text));
			Notification n = builder.getNotification();

			nm.notify(SIMPLE_NOTFICATION_ID, n);
			Toast.makeText(this, "test", Toast.LENGTH_LONG).show();
		}
		else {
//			Context mContext=getApplicationContext();
//			this.get
//
//			specialAlert(res.getString(R.string.ticker_new_friend_title), Integer.parseInt(fid), 
//					opponentUsername + " wants to be friend with you. Do you confirm?", mContext.ge, getSharedPreferences("loginInfo", 0));
		}
	}

	@Override
	protected void onRegistered(Context arg0, String arg1) {
		Log.i("gcmIntentService", "onReg");		

	}

	@Override
	protected void onUnregistered(Context arg0, String arg1) {
		Log.i("gcmIntentService", "onUreg");		
	}

//	public void createDialog(Context context, String message){
//		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//		Notification note = new Notification(R.drawable.ic_launcher, "App Notification", System.currentTimeMillis());
//		Intent notificationIntent = new Intent(context, Main.class);
//		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
//				Intent.FLAG_ACTIVITY_SINGLE_TOP);
//		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
//		note.setLatestEventInfo(context, "App Notification", message, pendingIntent);
//		note.number = count++;
//		note.defaults |= Notification.DEFAULT_SOUND;
//		note.defaults |= Notification.DEFAULT_VIBRATE;
//		note.defaults |= Notification.DEFAULT_LIGHTS;
//		note.flags |= Notification.FLAG_AUTO_CANCEL;
//		notificationManager.notify(0, note);
//	}
}
