/**
 * 
 */
package com.main.activitys.domain;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

/**
 * @author Simen
 *
 */
public class Login {
	public static String PREFS_NAME = "loginInfo";
	
	public static void setUserId(SharedPreferences loginSettings, int id){
		Editor editor = loginSettings.edit();
		editor.putInt("userId", id);
		editor.commit();
	}
	
	public static int getUserId(SharedPreferences loginSettings){
		return loginSettings.getInt("userId", 0);
	}
	
	public static String getStoredPassword(SharedPreferences loginSettings){
		return loginSettings.getString("password", "null");
	}
	
	public static void storeSessionId(SharedPreferences loginSettings, String sessionId){
		Editor editor = loginSettings.edit();
		editor.putString("sessionId", sessionId);
		editor.commit();
	}
	
	public static String getSessionId(SharedPreferences loginSettings){
		String tmp = loginSettings.getString("sessionId", "null");
		if(tmp.equals("no"))
			return "error";
		return tmp;
	}
	
	public static void setRegistered(SharedPreferences loginSettings){
		Editor editor = loginSettings.edit();
		editor.putBoolean("regged", true);
		editor.commit();
	}
	
	public static boolean isRegistered(SharedPreferences loginSettings){
		return loginSettings.getBoolean("regged", false);
	}
	
	public static void setEmail(SharedPreferences loginSettings, String email){
		Editor editor = loginSettings.edit();
		editor.putString("email", email);
		editor.commit();
	}
	
	public static void setLoggedInRightNow(SharedPreferences loginSettings){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		String currentDateandTime = sdf.format(new Date());
		
		Editor editor = loginSettings.edit();
		editor.putString("time", currentDateandTime);
		editor.commit();
	}
	
	public static boolean isSessionExpired(SharedPreferences loginSettings){
		String timeSinceLoggedIn = loginSettings.getString("time", "null");
		
		if(timeSinceLoggedIn.equals("null"))
			return true;
		
		Log.i("timeSinceLoggedIn", timeSinceLoggedIn);
		
		String [] tmp = timeSinceLoggedIn.split("_");
		Date timeSinceDate = new Date(Integer.parseInt(tmp[0]), Integer.parseInt(tmp[1]), Integer.parseInt(tmp[2]), Integer.parseInt(tmp[3]), Integer.parseInt(tmp[4]), Integer.parseInt(tmp[5]));
		Log.i("timeSinceLoggedIn", timeSinceDate.toString());

		long difference = timeSinceDate.compareTo(new Date());
		int days = (int) (difference / (1000 * 60 * 60 * 24));
		int hours = (int) ((difference- (1000 * 60 * 60 * 24 * days)) / (1000 * 60 * 60));
		int min = (int) (difference- (1000 * 60 * 60 * 24 * days) - (1000 * 60 * 60 * hours))/ (1000 * 60);		
		Log.i("days", Integer.toString(days));
		Log.i("hours", Integer.toString(hours));

		Log.i("min", Integer.toString(min));
		if(days > 0 || hours > 1 || min >= 60)
			return true;
		return false;
	}
}

