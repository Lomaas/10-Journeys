/**
 * 
 */
package com.saimenstravelapp.activitys;

import java.math.BigDecimal;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import com.saimenstravelapp.*;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.saimenstravelapp.activitys.domain.Login;
import com.saimenstravelapp.helper.CommonFunctions;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnTouchListener;
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
public class OpponentStatsActivity extends Activity {
	public ResponseListener getOpponentStatsListener;
	SharedPreferences loginSettings;
	Context context = this;
	private ProgressBar mProgress;
	
	Context ctx = this;
	IntentFilter gcmFilter;
	private BroadcastReceiver gcmReceiver = null;
	public static String GET_OPPONENTSTATS_URL = "http://restfulserver.herokuapp.com/opponentstat";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.grid_opponentstats);

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
		GridView myGrid = (GridView) findViewById(R.id.highscoreGrid);
		try {
			 JSONArray arrayResponse = new JSONArray(response);
			if(arrayResponse.isNull(0)){
				TextView noFriends = (TextView) findViewById(R.id.textNoFriends);
				noFriends.setVisibility(View.VISIBLE);
			}
			else
				myGrid.setAdapter(new GridAdapter(this, arrayResponse));
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public class GridAdapter extends BaseAdapter {
		private Context mContext;
		private JSONArray scores = null;
		private int numberOfColumns = 5;

		public GridAdapter(Context c, JSONArray scores) {
			mContext = c;
			this.scores = scores;
		}

		public int getCount() {
			return scores.length() * numberOfColumns + numberOfColumns;
		}

		public Object getItem(int position) {
			return null;
		}

		public long getItemId(int position) {
			return 0;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View MyView = convertView;

			if ( convertView == null )
			{
				/*we define the view that will display on the grid*/

				//Inflate the layout
				LayoutInflater li = getLayoutInflater();
				MyView = li.inflate(R.layout.grid_item_highscore, null);

				JSONObject obj;
				try {
					TextView textView = (TextView)MyView.findViewById(R.id.grid_item_text_highscore);
					if(position < numberOfColumns){
						textView.setTypeface(null, Typeface.BOLD);
  					if(position == 0){
  						textView.setText("Name");
  					}
  					else if(position == 1){
  						textView.setText("Games");
  					}
  					else if(position == 2){
  						textView.setText("Wins");
  					}
  					else if(position == 3){
  						textView.setText("Loss");
  					}
  					else if(position == 4){
  						textView.setText("Win-%");
  					}
					}
					else {
  					int posInScores = position / numberOfColumns;
  					int posInGrid = position % numberOfColumns;
  					Log.i("pos in Scores", Integer.toString(posInScores));
  					
  					Log.i("pos in grid", Integer.toString(posInGrid));
  					
  					obj = scores.getJSONObject(posInScores - 1);
						Integer totalWins = obj.getInt("total_wins");
						Integer totalGames = obj.getInt("total_games");
  				
  					if(posInGrid == 0)
  						textView.setText(obj.getString("username"));
  					else if(posInGrid == 1)
  						textView.setText(Integer.toString(totalGames));
  					else if(posInGrid == 2)
  						textView.setText(Integer.toString(totalWins));
  					else if(posInGrid == 3){
  						textView.setText(Integer.toString(totalGames - totalWins));
  					}
  					else if(posInGrid == 4){
  						if(totalGames == 0)
    						textView.setText("-");
  						else{
  							Log.i("float", Float.toString(CommonFunctions.round((float)totalWins/totalGames, 3, BigDecimal.ROUND_HALF_UP)));
  							float fardin = CommonFunctions.round((float)totalWins/totalGames, 3, BigDecimal.ROUND_HALF_UP);
  							float test = 100;
  							Log.i("float", Float.toString(fardin*test));
  							String tmp = Float.toString(fardin*test);
  							Log.d("length", Integer.toString(tmp.length()));

  							if(tmp.length() > 3){
    							textView.setText(tmp.substring(0, 4)+"%");
  							}
  							else
  								textView.setText(tmp+"%");
  						}
  					}
					}
				}
				catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			return MyView;
		}

	}


}
