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
	
	public static String getStartUpCards(SharedPreferences extraInfoSettings, String gameId){
		return extraInfoSettings.getString("cards-" + gameId, null);
	}
	
	public static void setStartUpCards(SharedPreferences extraInfoSettings, String cards, String gameId){
		Editor editor = extraInfoSettings.edit();
		editor.putString("cards-" + gameId, cards);
		editor.commit();
	}
	
	public static void setCardsIndex(SharedPreferences extraInfoSettings, int index, String gameId){
		Editor editor = extraInfoSettings.edit();
		editor.putInt("index-" + gameId, index);
		editor.commit();
	}
	
	public static int getCardsIndex(SharedPreferences extraInfoSettings, String gameId){
		return extraInfoSettings.getInt("index-" + gameId, -1);
	}
	
	public static void setYourCards(SharedPreferences extraInfo, String gameId, String yourCards){
		Editor editor = extraInfo.edit();
		editor.putString("yourCards-" + gameId, yourCards);
		editor.commit();
	}
	
	public static int getYourCards(SharedPreferences extraInfoSettings, String gameId){
		return extraInfoSettings.getInt("index-" + gameId, -1);
	}
	
	public static void setNewChatMsg(SharedPreferences extraInfo, String gameId, boolean input){
		Editor editor = extraInfo.edit();
		editor.putBoolean("newChatMsg-" + gameId, input);
		editor.commit();
	}
	
	public static boolean isNewChatMsg(SharedPreferences extraInfoSettings, String gameId){
		return extraInfoSettings.getBoolean("newChatMsg-" + gameId, false);
	}
	
	public static boolean isFirstGame(SharedPreferences extraInfo){
		return extraInfo.getBoolean("isFirstGame", false);
	}
	public static void setHavePlayedFirstGame(SharedPreferences extraInfo){
		Editor editor = extraInfo.edit();
		editor.putBoolean("isFirstGame", true);
		editor.commit();
	}
}
