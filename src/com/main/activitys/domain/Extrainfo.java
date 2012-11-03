/**
 * 
 */
package com.main.activitys.domain;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * @author Simen
 *
 */
public class Extrainfo {
	public static String PREFS_NAME = "extraInfo";

	public static void setAllGamesUrl(SharedPreferences extraInfoSettings, String url){
		Editor editor = extraInfoSettings.edit();
		editor.putString("AllGamesUrl", url);
		editor.commit();
	}
	
	public static String getAllGamesUrl(SharedPreferences extraInfoSettings){
		return extraInfoSettings.getString("AllGamesUrl", "null");
	}
	
	public static void setGameUrl(SharedPreferences extraInfoSettings, String url){
		Editor editor = extraInfoSettings.edit();
		editor.putString("gameUrl", url);
		editor.commit();
	}
	
	public static String getGameUrl(SharedPreferences extraInfoSettings){
		return extraInfoSettings.getString("gameUrl", "null");
	}	
}
