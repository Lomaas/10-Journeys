/**
 * 
 */
package com.main.activitys.domain;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.format.Time;
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
	
	
	public static void setIsReggedPush(SharedPreferences loginSettings){
		Editor editor = loginSettings.edit();
		editor.putBoolean("regPush", true);
		editor.commit();
	}
	
	public static void setNotReggedPush(SharedPreferences loginSettings){
		Editor editor = loginSettings.edit();
		editor.putBoolean("regPush", false);
		editor.commit();
	}
	
	public static boolean isReggedForPush(SharedPreferences loginSettings){
		return loginSettings.getBoolean("regPush", false);
	}
	
	public static String getGoogleRegistrationId(SharedPreferences loginSettings){
		return loginSettings.getString("registrationId", null);
	}
	
	public static void storeGoogleRegistrationId(SharedPreferences loginSettings, String regId){
		Editor editor = loginSettings.edit();
		editor.putString("registrationId", regId);
		editor.commit();
	}
	
	public static int getProfileId(SharedPreferences loginSettings){
		return loginSettings.getInt("imageId", 0);
	}
	
	public static void storeProfileId(SharedPreferences loginSettings, int imageId){
		Editor editor = loginSettings.edit();
		editor.putInt("imageId", imageId);
		editor.commit();
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
		Time now = new Time();
  	now.setToNow();
  	long milliseconds = now.toMillis(false) + 10800000;		// 3 hours expired sessions
  	
		Editor editor = loginSettings.edit();
		editor.putLong("timeLoggedIn", milliseconds);
		editor.commit();
	}
	
	public static boolean isSessionExpired(SharedPreferences loginSettings){
		Time now = new Time();
  	now.setToNow();
  	long milliseconds = now.toMillis(false);
  	
		long milliSessionExpires = loginSettings.getLong("timeLoggedIn", 0);
		
		if(milliseconds >= milliSessionExpires){
			Log.i("isSessionExpired", "yes");
			return true;
		}
		else{
			Log.i("isSessionExpired", "not");
			return false;
		}
	}
	
	
	public static long getBackOffTime(SharedPreferences loginSettings){
		return loginSettings.getLong("backOffTime", 0);
	}
	
	public static void storeBackOffTime(SharedPreferences loginSettings, long backOffTime){
		Editor editor = loginSettings.edit();
		editor.putLong("backOffTime", backOffTime);
		editor.commit();
	}
	
	public static void setTimeReggedPush(SharedPreferences logPreferences, long time){
		Editor editor = logPreferences.edit();
		editor.putLong("timeSinceReggedPush", time);
		editor.commit();
	}
	
	public static long getTimeSinceReggedForPush(SharedPreferences logPreferences){
		return logPreferences.getLong("timeSinceReggedPush", 0);
	}
	
}

