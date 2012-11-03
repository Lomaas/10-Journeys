/**
 * 
 */
package com.main.activitys;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uit.nfc.AsynchronousHttpClient;
import uit.nfc.ResponseListener;

import com.main.R;
import com.main.activitys.GameFinishActivity.ImgAdapter;
import com.main.activitys.domain.Login;
import com.main.helper.BuildHttpRequest;
import com.main.helper.ProgressDialogClass;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

/**
 * @author Simen
 *
 */
public class HighscoreActivity extends Activity {
	public ResponseListener getHighScoreList;
	SharedPreferences loginSettings;
	Context context = this;
	private ProgressBar mProgress;
	public static String GET_HIGHSCORE_URL = "http://restfulserver.herokuapp.com/highscore";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.grid_highscore);

		loginSettings = getSharedPreferences(Login.PREFS_NAME, 0);
		mProgress = (ProgressBar) findViewById(R.id.progressBar);

		getHighScoreList = new ResponseListener() {

			@Override
			public void onResponseReceived(HttpResponse response, String message) {
				Log.i("onResponseReceived", message);
				evaulateResponse(message);
				mProgress.setVisibility(View.GONE);				
			}
		};

		getHighScoreData(Login.getUserId(loginSettings));
	}

	private void getHighScoreData(int UID){
		HttpGet httpGet = new HttpGet(GET_HIGHSCORE_URL);

		AsynchronousHttpClient a = new AsynchronousHttpClient();
		a.sendRequest(httpGet, getHighScoreList, loginSettings);
	}

	private void evaulateResponse(String response){
		GridView myGrid = (GridView) findViewById(R.id.highscoreGrid);
		try {
			myGrid.setAdapter(new GridAdapter(this, new JSONArray(response)));
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public class GridAdapter extends BaseAdapter {
		private Context mContext;
		private JSONArray scores = null;
		private int numberOfColumns = 6;

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
  					if(position == 0){
  						textView.setText("Rank");
  					}
  					else if(position == 1){
  						textView.setText("Name");
  					}
  					else if(position == 2){
  						textView.setText("Games");
  					}
  					else if(position == 3){
  						textView.setText("Wins");
  					}
  					else if(position == 4){
  						textView.setText("Loss");
  					}
  					else if(position == 5){
  						textView.setText("Score");
  					}
					}
					else {
  					int posInScores = position / numberOfColumns;
  					int posInGrid = position % numberOfColumns;
  					Log.i("pos in Scores", Integer.toString(posInScores));
  					
  					Log.i("pos in grid", Integer.toString(posInGrid));
  					
  					obj = scores.getJSONObject(posInScores - 1);
  					
  					if(posInGrid == 0)
  						textView.setText(Integer.toString(posInScores));
  					else if(posInGrid == 1)
  						textView.setText(obj.getString("username"));
  					else if(posInGrid == 2)
  						textView.setText(Integer.toString(obj.getInt("total_games")));
  					else if(posInGrid == 3)
  						textView.setText(Integer.toString(obj.getInt("total_wins")));
  					else if(posInGrid == 4){
  						Integer totalWins = obj.getInt("total_wins");
  						Integer totalGames = obj.getInt("total_games");
  						
  						textView.setText(Integer.toString(totalGames - totalWins));
  					}
  					else if(posInGrid == 5)
  						textView.setText(Integer.toString(obj.getInt("score")));
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
