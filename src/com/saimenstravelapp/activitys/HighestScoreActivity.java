package com.saimenstravelapp.activitys;

import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import com.saimenstravelapp.*;
import com.saimenstravelapp.activitys.domain.Login;
import com.saimenstravelapp.activitys.domain.Player;
import com.saimenstravelapp.helper.CommonFunctions;
import com.saimenstravelapp.helper.HighScoreAdapter;
import com.saimenstravelapp.helper.HighestScoreAdapter;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import async.httprequest.AsynchronousHttpClient;
import async.httprequest.ResponseListener;

/**
 * @author Simen
 *
 */
public class HighestScoreActivity extends ListActivity {
	public ResponseListener getHighestScoreList;
	SharedPreferences loginSettings;
	Context context = this;
	private ProgressBar mProgress;
	IntentFilter gcmFilter;
	private ArrayList<Player> highScoreList = new ArrayList<Player>();
	private BroadcastReceiver gcmReceiver = null;
	public static String GET_HIGHSCORE_URL = "http://restfulserver.herokuapp.com/highestscore";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.highscore_list);

		loginSettings = getSharedPreferences(Login.PREFS_NAME, 0);
		mProgress = (ProgressBar) findViewById(R.id.progressBar);
		gcmReceiver = CommonFunctions.createBroadCastReceiver(context, loginSettings, CommonFunctions.FROM_STANDARD_ACTIVITY);

		gcmFilter = new IntentFilter();
		gcmFilter.addAction("GCM_RECEIVED_ACTION");

		ActionBar actionBar = (ActionBar)findViewById(R.id.actionbar);
		actionBar.setTitle("Highest Single Score");

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

		getHighestScoreList = new ResponseListener() {

			@Override
			public void onResponseReceived(HttpResponse response, String message) {
				Log.i("onResponseReceived", message);
				evaulateResponse(message);
				mProgress.setVisibility(View.GONE);				
			}
		};

		getHighestScoreData(Login.getUserId(loginSettings));
	}

	protected void onPause(){
		super.onPause();
		unregisterReceiver(gcmReceiver);
	}

	protected void onResume(){
		super.onResume();
		registerReceiver(gcmReceiver, gcmFilter);
	}

	private void getHighestScoreData(int UID){
		HttpGet httpGet = new HttpGet(GET_HIGHSCORE_URL);

		AsynchronousHttpClient a = new AsynchronousHttpClient();
		a.sendRequest(httpGet, getHighestScoreList, loginSettings);
	}
	
	private void evaulateResponse(String response){
		try {
			JSONArray array = new JSONArray(response);
			
			for(int index = 0; index < array.length(); index++){
				Player player = new Player();
				JSONObject obj = array.getJSONObject(index);
				
				player.setImageId(obj.getInt("image"));
				player.setScore(obj.getInt("score"));
				player.setAverageScore(obj.getInt("averageScore"));
				player.setUsername(obj.getString("username"));
				highScoreList.add(index, player);
			}
			setListAdapter(new HighestScoreAdapter(context, highScoreList));
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
