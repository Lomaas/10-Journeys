/**
 * 
 */
package com.saimenstravelapp.activitys;

import java.math.BigDecimal;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import async.httprequest.AsynchronousHttpClient;
import async.httprequest.ResponseListener;
import com.saimenstravelapp.R;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.saimenstravelapp.activitys.domain.Extrainfo;
import com.saimenstravelapp.activitys.domain.Game;
import com.saimenstravelapp.activitys.domain.Login;
import com.saimenstravelapp.helper.Alert;
import com.saimenstravelapp.helper.CommonFunctions;
import com.saimenstravelapp.helper.Constants;
import com.saimenstravelapp.helper.ProgressDialogClass;


/**
 * @author Simen
 *
 */
public class GameFinishActivity extends Activity{
	public ResponseListener startGameListener;
	public ResponseListener getStatsListener;

	SharedPreferences loginSettings;
	Context context = this;
	IntentFilter gcmFilter;
	private BroadcastReceiver gcmReceiver =null;
	private Game gameObject = new Game();

	public String opponent;
	public int opponentId;
	public int gameId;

	public ProgressDialogClass progDialog;


	Bundle extras = null;
	private ProgressBar mProgress1;
	private ProgressBar mProgress2;
	private ProgressBar mProgress3;
	private ProgressBar mProgress4;
	public static String GET_STATS_URL = "http://restfulserver.herokuapp.com/finish/get_stat/";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.finish_game);

		loginSettings = getSharedPreferences(Login.PREFS_NAME, 0);
		gcmReceiver = CommonFunctions.createBroadCastReceiver(context, loginSettings, CommonFunctions.FROM_FINISH_GAME_ACTIVITY);
		gcmFilter = new IntentFilter();
		gcmFilter.addAction("GCM_RECEIVED_ACTION");

		extras = getIntent().getExtras();
		JSONArray cards = null;
		String strCards = extras.getString("cards");
		String action = extras.getString("action");

		Button textViewFinishGame = (Button)findViewById(R.id.buttonAddToFriendList);
		Button textViewNewGame = (Button)findViewById(R.id.buttonFindByUsername);
		GridView MyGrid = (GridView)findViewById(R.id.MyGrid);

		opponent =  extras.getString("opponent");
		gameId = extras.getInt(GameActivity.SELECTED_GAME_ID);
		Log.i("opponentId", Integer.toString(extras.getInt("opponentId")));

		opponentId = extras.getInt("opponentId");

		textViewFinishGame.setText("Add " + opponent + " as friend");
		textViewNewGame.setText("Rematch");

		TextView stats = (TextView) findViewById(R.id.textShowStats);
		stats.setText("Your stats vs " + extras.getString("opponent"));
		
		TextView score = (TextView) findViewById(R.id.textScoreThisGame);
		
		if(sign(extras.getInt("score")) == 1){
			score.setText("+" + Integer.toString(extras.getInt("score")));
			score.setTextColor(getResources().getColor(R.color.green));
		}
		else if(sign(extras.getInt("score")) == 0){
			score.setText(Integer.toString(extras.getInt("score")));
			score.setTextColor(getResources().getColor(R.color.white));
		}
		else {
			score.setText(Integer.toString(extras.getInt("score")));
			score.setTextColor(getResources().getColor(R.color.red));
		}
		
		if(extras.getInt("playersTurn") == opponentId){
			Button but = (Button) findViewById(R.id.buttonSeeLastGameView);
			but.setText("How close were you?");
		}
		
		mProgress1 = (ProgressBar) findViewById(R.id.progressBar1);
		mProgress2 = (ProgressBar) findViewById(R.id.progressBar2);
		mProgress3 = (ProgressBar) findViewById(R.id.progressBar3);
		mProgress4 = (ProgressBar) findViewById(R.id.progressBar4);

		final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle(action);

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

		if(Extrainfo.isNewChatMsg(context.getSharedPreferences(Extrainfo.PREFS_NAME, 0), Integer.toString(gameId)))
			setChatIconRed();
		else
			setChatIconWhite();

		try {
			cards = new JSONArray(strCards);
			MyGrid.setAdapter(new ImgAdapter(this, cards));
		}
		catch (JSONException e) { e.printStackTrace(); finish(); }

		getStatsListener = new ResponseListener() {
			@Override
			public void onResponseReceived(HttpResponse response, String message) {
				Log.i("onResponseReceived", message);
				evaulateResponse(message);
				setProgressBarGone();
			}
		};

		if(action.contains("gave up")){
			TextView winnerRoute = (TextView) findViewById(R.id.textWinnerRoute);
			
			LinearLayout layout = (LinearLayout) findViewById(R.id.linearLayoutFinishedGame1);
//			ScrollView scrollView = (ScrollView)findViewById(R.id.scrollView1);
			
			layout.removeView(findViewById(R.id.linearLayoutFinishedGame6));
			layout.removeView(MyGrid);
//			layout.removeView(winnerRoute);
//			layout.removeViewInLayout(scrollView);
//
//			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
//			lp.addRule(RelativeLayout.BELOW, R.id.actionbar);
//			layout.addView(scrollView, lp);
		}
		else{
			MyGrid.setAdapter(new ImgAdapter(this, cards));
		}

		if(extras.containsKey("wins_player1") && extras.containsKey("wins_player2")){
			setStats(extras.getInt("player1"), extras.getInt("player2"), 
					extras.getInt("wins_player1"), extras.getInt("wins_player2"), extras.getInt("opponentId"));
		}
		else {
			// Fire Off Request About stat vs opponent
			getStatsData(extras.getInt("opponentId"));
		}


	}
	
	int sign(int i) {
    if (i == 0) return 0;
    if (i >> 31 != 0) return -1;
    return +1;
}

	protected void onResume(){
		super.onResume();
		registerReceiver(gcmReceiver, gcmFilter);
	}

	protected void onPause(){
		super.onPause();
		unregisterReceiver(gcmReceiver);
	}
	
	private void setProgressBarGone(){
		mProgress1.setVisibility(View.GONE);		
		mProgress2.setVisibility(View.GONE);
		mProgress3.setVisibility(View.GONE);
		mProgress4.setVisibility(View.GONE);
	}

	public void setGoToChatActivity(){
		Intent intent = new Intent().setClass(this, ChatActivity.class);
		intent.putExtra("opponentUsername", opponent);
		intent.putExtra("gameId", Integer.toString(gameId));
		intent.putExtra("opponentId", Integer.toString(opponentId));
		
		Log.i("gameID", Integer.toString(gameId));

		startActivity(intent);
		final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.removeActionAt(0);
		setChatIconWhite();
	}

	public void setChatIconWhite(){
		final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);

		actionBar.addAction(new Action(){
			@Override
			public int getDrawable() {
				return R.drawable.chat_icon_actionbar_white;
			}
			@Override
			public void performAction(View view) {
				((GameFinishActivity) context).setGoToChatActivity();
			}
		});
	}

	public void setChatIconRed(){
		final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);

		actionBar.addAction(new Action(){
			@Override
			public int getDrawable() {
				return R.drawable.chat_icon_actionbar_red;
			}
			@Override
			public void performAction(View view) {
				((GameFinishActivity) context).setGoToChatActivity();
			}
		});
	}

	private void getStatsData(int UID) {
		HttpGet httpGet = new HttpGet(GET_STATS_URL + Integer.toString(UID));

		AsynchronousHttpClient a = new AsynchronousHttpClient();
		a.sendRequest(httpGet, getStatsListener, loginSettings);
	}

	public void startNewGame(final View v) {
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
						Button but = (Button) v;
						but.setText("Sending game request..");

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
		CommonFunctions.startGameFromUsername(this, extras.getString("opponent"), extras.getInt("type"), null, loginSettings);

		Button but = (Button) v;
		but.setText("Sending 	request..");
		but.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.d("Do nothing.", "User trying to create new game.. already has");
			}
		});
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


	public void seeLastGameActivity(View v){
		Intent gameIntent = new Intent(this, GameActivity.class);

		gameIntent.putExtra(GameActivity.SELECTED_GAME_ID, extras.getInt("gameId"));
		gameIntent.putExtra("STATE", GameActivity.OPPONENTS_TURN);
		gameIntent.putExtra("openCard", 0);
		gameIntent.putExtra("action", extras.getString("action"));
		gameIntent.putExtra("YOUR_CARDS", extras.getString("YOUR_CARDS"));	
		gameIntent.putExtra("openCards", extras.getString("openCards"));
		gameIntent.putExtra("opponentId", extras.getInt("opponentId"));
		gameIntent.putExtra("opponent", extras.getString("opponent"));
		gameIntent.putExtra("type", extras.getInt("type"));
		gameIntent.putExtra("playersTurn", 0);
		gameIntent.putExtra("last_updated",  extras.getString("last_updated"));
		gameIntent.putExtra("openCardParents", "[0, 0, 0]");
		gameIntent.putExtra("date_created", "0");
		gameIntent.putExtra("image", 1);
		gameIntent.putExtra("finished", true);

		startActivity(gameIntent);
	}

	public void setStats(int player1, int player2, int wins_player1, int wins_player2, int opponentId){
		setProgressBarGone();
		TextView totalWins = (TextView) findViewById(R.id.textTotalWins);
		TextView totalLoss = (TextView) findViewById(R.id.textTotaLoss);
		TextView totalGames = (TextView) findViewById(R.id.textTotalGames);
		TextView winPercentage = (TextView) findViewById(R.id.textWinPercentage);

		int wins = 0;
		int loss = 0;
		int games = 0;

		if(opponentId == player1){
			Log.i("opponent is player1", "player1");
			wins = wins_player2;
			loss = wins_player1;
			games = wins + loss;
		}
		else {
			Log.i("opponent is player2", "player2");
			wins = wins_player1;
			loss = wins_player2;
			games = wins + loss;
		}
		float percentage = 0;

		if(games != 0){
			percentage = CommonFunctions.round((float)wins/games, 3, BigDecimal.ROUND_HALF_UP) * 100;
		}

		Log.i("wins", "Total wins: " + Integer.toString(wins));
		Log.i("loss", "Total loss: " + Integer.toString(loss));
		Log.i("games", "Total games: " + Integer.toString(games));


		totalWins.setText (Integer.toString(wins));
		totalLoss.setText (Integer.toString(loss));
		totalGames.setText(Integer.toString(games));
		winPercentage.setText(Float.toString(percentage) +"%");
	}

	public class ImgAdapter extends BaseAdapter
	{
		Context MyContext;
		private JSONArray cards = null;

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

				imageView.setImageResource(Constants.imageArray[cardId]);

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
