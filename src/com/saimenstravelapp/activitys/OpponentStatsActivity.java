/**
 * 
 */
package com.saimenstravelapp.activitys;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.saimenstravelapp.*;
import com.saimenstravelapp.activitys.domain.Login;
import com.saimenstravelapp.activitys.domain.Player;
import com.saimenstravelapp.helper.CommonFunctions;
import com.saimenstravelapp.helper.FriendsStatisticAdapter;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import async.httprequest.AsynchronousHttpClient;
import async.httprequest.ResponseListener;

/**
 * @author Simen
 *
 */
public class OpponentStatsActivity extends ListActivity {
	public ResponseListener getOpponentStatsListener;
	SharedPreferences loginSettings;
	Context context = this;
	private ProgressBar mProgress;

	Context ctx = this;
	IntentFilter gcmFilter;
	private BroadcastReceiver gcmReceiver = null;
	private ArrayList<Player> friendsStatsList = new ArrayList<Player>();
	public static String GET_OPPONENTSTATS_URL = "http://restfulserver.herokuapp.com/opponentstat";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.list_opponentstats);

		loginSettings = getSharedPreferences(Login.PREFS_NAME, 0);
		mProgress = (ProgressBar) findViewById(R.id.progressBar);
		gcmReceiver = CommonFunctions.createBroadCastReceiver(ctx, loginSettings, CommonFunctions.FROM_STANDARD_ACTIVITY);

		gcmFilter = new IntentFilter();
		gcmFilter.addAction("GCM_RECEIVED_ACTION");

		getOpponentStatsListener = new ResponseListener() {
			@Override
			public void onResponseReceived(HttpResponse response, String message) {
				Log.i("onResponseReceived", message);
				evaulateResponse(message);
				mProgress.setVisibility(View.GONE);
			}
		};

		ActionBar actionBar = (ActionBar)findViewById(R.id.actionbar);
		actionBar.setTitle("Friends statistics");

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

		getOpponentStatsData();
	}

	protected void onPause(){
		super.onPause();
		unregisterReceiver(gcmReceiver);
	}

	protected void onResume(){
		super.onResume();
		registerReceiver(gcmReceiver, gcmFilter);
	}

	private void getOpponentStatsData(){
		HttpGet httpGet = new HttpGet(GET_OPPONENTSTATS_URL);

		AsynchronousHttpClient a = new AsynchronousHttpClient();
		a.sendRequest(httpGet, getOpponentStatsListener, loginSettings);
	}

	private void evaulateResponse(String response){
		try {
			JSONArray array = new JSONArray(response);

			if(array.isNull(0)){
				TextView noFriends = (TextView) findViewById(R.id.textNoFriends);
				noFriends.setVisibility(View.VISIBLE);
			}
			else{
				for(int index = 0; index < array.length(); index++){
					Player player = new Player();
					JSONObject obj = array.getJSONObject(index);

					Integer totalWins = obj.getInt("total_wins");
					Integer totalGames = obj.getInt("total_games");

					player.setLoss(totalGames - totalWins);
					player.setWins(totalWins);
					player.setUsername(obj.getString("username"));
					player.setImageId(obj.getInt("image"));

					friendsStatsList.add(index, player);
				}
				setListAdapter(new FriendsStatisticAdapter(context, friendsStatsList));			
			}
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
