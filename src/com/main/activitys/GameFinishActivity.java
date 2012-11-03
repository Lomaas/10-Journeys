/**
 * 
 */
package com.main.activitys;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uit.nfc.AsynchronousHttpClient;
import uit.nfc.ResponseListener;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.main.*;
import com.main.activitys.domain.Login;
import com.main.helper.Alert;
import com.main.helper.CommonFunctions;
import com.main.helper.ImageAdapter;


/**
 * @author Simen
 *
 */
public class GameFinishActivity extends Activity{
	public ResponseListener startGameListener;
	public ResponseListener getStatsListener;

	SharedPreferences loginSettings;
	Context context = this;
	Bundle extras = null;
	private ProgressBar mProgress;
	public static String GET_STATS_URL = "http://restfulserver.herokuapp.com/finish/get_stat/";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.finish_game);

		loginSettings = getSharedPreferences(Login.PREFS_NAME, 0);

		extras = getIntent().getExtras();
		String strCards = extras.getString("cards");
		String action = extras.getString("action");
		TextView textViewAction = (TextView)findViewById(R.id.textActionFinishGame);

		Button textViewFinishGame = (Button)findViewById(R.id.buttonAddToFriendList);
		Button textViewNewGame = (Button)findViewById(R.id.buttonFindByUsername);
		GridView MyGrid = (GridView)findViewById(R.id.MyGrid);
		
		textViewFinishGame.setText("Add " + extras.getString("opponent") + " to your friendslist");
		textViewNewGame.setText("New game with " + extras.getString("opponent"));
		
		TextView stats = (TextView) findViewById(R.id.textShowStats);
		stats.setText("Your stats vs " + extras.getString("opponent"));
		stats.setPaintFlags(stats.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
		
		mProgress = (ProgressBar) findViewById(R.id.progressBar);

		getStatsListener = new ResponseListener() {
			@Override
			public void onResponseReceived(HttpResponse response, String message) {
				Log.i("onResponseReceived", message);
				evaulateResponse(message);
				mProgress.setVisibility(View.GONE);				
			}
		};

		if(extras.containsKey("wins_player1") && extras.containsKey("wins_player2")){
			setStats(extras.getInt("player1"), extras.getInt("player2"), 
					extras.getInt("wins_player1"), extras.getInt("wins_player2"), extras.getInt("opponentId"));
		}
		else {
			// Fire Off Request About stat vs opponent
			getStatsData(extras.getInt("opponentId"));
		}
		
		if(action.contains("gave up")){
			textViewAction.setText(action);
			TextView winnerRoute = (TextView) findViewById(R.id.textWinnerRoute);

			LinearLayout layout = (LinearLayout) findViewById(R.id.linearLayoutFinishedGame1);
			layout.removeView(MyGrid);
			layout.removeView(winnerRoute);
		}
		else{
			textViewAction.setText(action);
  		try {
  			JSONArray cards = new JSONArray(strCards);
  			MyGrid.setAdapter(new ImgAdapter(this, cards));
  		}
  		catch (JSONException e) { e.printStackTrace(); finish(); }
		}
	}
	
	private void getStatsData(int UID) {
		HttpGet httpGet = new HttpGet(GET_STATS_URL + Integer.toString(UID));

		AsynchronousHttpClient a = new AsynchronousHttpClient();
		a.sendRequest(httpGet, getStatsListener, loginSettings);
	}

	public void startNewGame(View v) {
		ResponseListener startGameListener = new ResponseListener() {
			@Override
			public void onResponseReceived(HttpResponse httpResponse, String message) {
				Log.d("Response", httpResponse.toString());

				try {
					JSONObject response = new JSONObject(message);
					int resultCode = Activity.RESULT_CANCELED;
					Intent returnIntent = new Intent();

					if(response.has("error"))
						new Alert("Uups", response.getString("error"), context);

					else{
						returnIntent.putExtra("gameInfo", response.toString());
						Log.d("gameinfo", response.toString());
						resultCode = Activity.RESULT_OK;
					}
					if (getParent() == null) {
						setResult(resultCode, returnIntent);
					} else {
						getParent().setResult(resultCode, returnIntent);
					}
					finish();
				}
				catch(JSONException e){e.printStackTrace();}
			}
		};
		Bundle extras = getIntent().getExtras();
		Log.i("Type", Integer.toString(extras.getInt("type")));
		CommonFunctions.startGameFromUsername(extras.getString("opponent"), extras.getInt("type"), startGameListener, loginSettings);
	}
	
	public void evaulateResponse(String message){
		Log.i("message", message);
		
		try{
			JSONObject obj = new JSONObject(message);
			 setStats(obj.getInt("player1"), obj.getInt("player2"), obj.getInt("wins_player1"), obj.getInt("wins_player2"), extras.getInt("opponentId"));
		}
		catch(JSONException e){ e.printStackTrace(); }
		
	}

	public void addToFriendList(View v){
		Bundle extras = getIntent().getExtras();
		CommonFunctions.sendFriendRequest("username", extras.getString("opponent"), loginSettings, context);
	}
	
	public void setStats(int player1, int player2, int wins_player1, int wins_player2, int opponentId){
		mProgress.setVisibility(View.GONE);				
		TextView totalWins = (TextView) findViewById(R.id.textTotalWins);
		TextView totalLoss = (TextView) findViewById(R.id.textTotaLoss);
		TextView totalGames = (TextView) findViewById(R.id.textTotalGames);
		TextView winPercentage = (TextView) findViewById(R.id.textWinPercentage);
		
		int wins = 0;
		int loss = 0;
		int games = 0;
		
		if(opponentId == player1){
			Log.i("opponent is player1", "player1");
			wins = wins_player1;
			loss = wins_player2;
			games = wins + loss;
		}
		else {
			Log.i("opponent is player2", "player2");
			wins = wins_player2;
			loss = wins_player1;
			games = wins + loss;
		}
		float percentage = ((float)wins/(float)games) *100;

		Log.i("wins", "Total wins: " + Integer.toString(wins));
		Log.i("loss", "Total loss: " + Integer.toString(loss));
		Log.i("games", "Total games: " + Integer.toString(games));

		
		totalWins.setText("Total wins - " + Integer.toString(wins));
		totalLoss.setText("Total loss - " + Integer.toString(loss));
		totalGames.setText("Total games - " + Integer.toString(games));
		winPercentage.setText("Win-percent - " + Float.toString(percentage) +"%");
	}

	public class ImgAdapter extends BaseAdapter
	{
		Context MyContext;
		private JSONArray cards = null;
		// references to our images
		private Integer[] mThumbIds = {
				R.drawable.uncorrect, R.drawable.confirm,
				R.drawable.cards_sweden, R.drawable.cards_norway,
				R.drawable.cards_finaland, R.drawable.cards_russia,
				R.drawable.cards_iceland, R.drawable.cards_ireland,
				R.drawable.cards_uk, R.drawable.cards_estonia,
				R.drawable.cards_lativa, R.drawable.cards_lithuania,
				R.drawable.cards_poland, R.drawable.cards_germany,
				R.drawable.cards_denmark, R.drawable.cards_netherlands,
				R.drawable.cards_belgia,R.drawable.cards_italy,
				R.drawable.cards_slovenia, R.drawable.cards_croatia,
				R.drawable.cards_czech_republic, R.drawable.cards_slovakia,
				R.drawable.cards_belarus, R.drawable.cards_ukranie,
				R.drawable.cards_austria, R.drawable.cards_switzerland,
				R.drawable.cards_france, R.drawable.cards_luxemburg,
				R.drawable.cards_andorra, R.drawable.cards_spain,
				R.drawable.cards_portugal, R.drawable.cards_bosnia_herzegovina,
				R.drawable.cards_hungary, R.drawable.cards_kosovo,
				R.drawable.cards_turkey, R.drawable.cards_serbia,
				R.drawable.cards_albania, R.drawable.cards_greece,
				R.drawable.cards_motenegro, R.drawable.cards_macedonia,
				R.drawable.cards_bulgaria, R.drawable.cards_moldova, 
				R.drawable.cards_airplane_blue, R.drawable.cards_airplane_green,
				R.drawable.cards_airplane_brown, R.drawable.cards_airplane_red,
				R.drawable.cards_airplane_yellow, R.drawable.cards_atlantic_sea,
				R.drawable.cards_mediterrean_sea, R.drawable.cards_baltic_sea
		};

		public ImgAdapter(Context _MyContext, JSONArray cards)
		{
			MyContext = _MyContext;
			this.cards = cards;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View MyView = convertView;

			if ( convertView == null )
			{
				/*we define the view that will display on the grid*/

				//Inflate the layout
				LayoutInflater li = getLayoutInflater();
				MyView = li.inflate(R.layout.grid_finishgame, null);

				// Add The Image!!!           
				ImageView imageView = (ImageView)MyView.findViewById(R.id.grid_item_image);

				int cardId = 0;
				try {
					cardId = cards.getInt(position);

				}
				catch (JSONException e) {  e.printStackTrace(); }

				if(cardId > 2)
					cardId += 1;

				imageView.setImageResource(mThumbIds[cardId]);

				// Add The Text!!!
				int journeyDay = position + 1;
				TextView tv = (TextView)MyView.findViewById(R.id.grid_item_text);
				tv.setText("Journey "+ journeyDay);
			}

			return MyView;
		}

		public int getCount() {
			return cards.length();
		}

		public Object getItem(int position) {
			return null;
		}

		public long getItemId(int position) {
			return 0;
		}

	}

}
