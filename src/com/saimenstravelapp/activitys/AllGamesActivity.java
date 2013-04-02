package com.saimenstravelapp.activitys;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.markupartist.android.widget.ActionBar.IntentAction;
import com.saimenstravelapp.*;
import com.saimenstravelapp.activitys.domain.Extrainfo;
import com.saimenstravelapp.activitys.domain.Game;
import com.saimenstravelapp.activitys.domain.Login;
import com.saimenstravelapp.helper.*;
import com.saimenstravelapp.service.TimerService;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.app.Activity;
import android.app.ListActivity;
import android.content.BroadcastReceiver;

import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.google.ads.InterstitialAd;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import async.httprequest.AsynchronousHttpClient;
import async.httprequest.ResponseListener;



public class AllGamesActivity extends ListActivity {
	private Intent serviceintent;
	private SharedPreferences loginSettings;
	private SharedPreferences extraInfoSettings;
	public ResponseListener giveUpListener;
	public ResponseListener startGameListener;
	public Context context = this;

	public static String NOTIFICATION_FRIEND_REQUEST = "NOTIFICATION_FRIEND_REQUEST";
	public static String NOTIFICATION_YOUR_TURN = "NOTIFICATION_YOUR_TURN";
	public static String NOTIFICATION_NEW_GAME= "NOTIFICATION_NEW_GAME";

	public static int RANDOM_GAMEREQ_RESP = 1;
	public static int BACK_FROM_GAMER = 2;

	public static String GIVE_UP_URL = "http://restfulserver.herokuapp.com/finish/give_up";
	public static String REG_ID_URL = "http://restfulserver.herokuapp.com/user/reg_id";

	private SeparatedListAdapter adapter = null;
	private ArrayList<Game> mineTurnList;
	private ArrayList<Game> opponentTurnList;
	private ArrayList<Game> finishedGamesList;

	public static String opponentTurnListSection = "Opponents turn";
	public static String yourTurnListSection = "Your turn";
	public static String finishedTurnListSection = "Finished games";

	IntentFilter gcmFilter;
	private BroadcastReceiver gcmReceiver = null;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.all_games);
		context = this;
		loginSettings = getSharedPreferences(Login.PREFS_NAME, Activity.MODE_PRIVATE);
		gcmReceiver = CommonFunctions.createBroadCastReceiver(context, loginSettings, CommonFunctions.FROM_STANDARD_ACTIVITY);

		extraInfoSettings = getSharedPreferences(Extrainfo.PREFS_NAME, 0);

		gcmFilter = new IntentFilter();
		gcmFilter.addAction("GCM_RECEIVED_ACTION");

		Bundle extras = getIntent().getExtras();
		
		final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle(R.string.app_name);

		final Action oppontentStatAction = new IntentAction(this, new Intent(this, OpponentStatsActivity.class), R.drawable.stat);
		actionBar.addAction(oppontentStatAction);

		final Action highscoreAction = new IntentAction(this, new Intent(this, HighscoreActivity.class), R.drawable.rank);
		actionBar.addAction(highscoreAction);

		final Action settingsAction = new IntentAction(this, new Intent(this, SettingsActivity.class), R.drawable.ic_menu_settings);
		actionBar.addAction(settingsAction);

		final Action newGameAction = new IntentAction(this, new Intent(this, NewGameActivity.class), R.drawable.ic_menu_add);
		actionBar.addAction(newGameAction);

		mineTurnList = new ArrayList<Game>();
		opponentTurnList = new ArrayList<Game>();
		finishedGamesList = new ArrayList<Game>();

		serviceintent = new Intent("com.saimenstravelapp.service.TimerService");
		serviceintent.putExtra(TimerService.URL, Extrainfo.getAllGamesUrl(extraInfoSettings));

		ListView lv = getListView(); 
		registerForContextMenu(lv);

		giveUpListener = new ResponseListener() {
			@Override
			public void onResponseReceived(HttpResponse response, String message) {
				startService();
				evaluateResponse(message);
			}
		};
		startGameListener = new ResponseListener() {
			@Override
			public void onResponseReceived(HttpResponse response, String message) {
				evaluateResponse(message);
			}
		};

		if(extras != null && extras.getBoolean(NOTIFICATION_FRIEND_REQUEST)){
			Resources res = getResources();

			CommonFunctions.alertForAddFriend(res.getString(R.string.ticker_new_friend_title), extras.getInt("fid"), 
					extras.getString("opponentUsername") + " wants to add you to his friendslist. Confirm?", this, loginSettings);
		}
		if(extras != null && extras.getBoolean(NOTIFICATION_FRIEND_REQUEST)){
			Resources res = getResources();

			CommonFunctions.alertForAddFriend(res.getString(R.string.ticker_new_friend_title), extras.getInt("fid"), 
					extras.getString("opponentUsername") + " wants to add you to his friendslist. Confirm?", this, loginSettings);
		}
		if(!Login.isReggedForPush(loginSettings)){
			CommonFunctions.regWithGoogleServer(this, loginSettings);
		}
		else
			CommonFunctions.checkIfRegIdIsExpired(this, loginSettings);
		
		if(extras != null && extras.containsKey("fromRegisterActivity")){
			Intent intent = new Intent().setClass(this, IntroductionActivity.class);
			extras.remove("fromRegisterActivity");
			startActivity(intent);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;

		Game game = (Game) adapter.getItem(info.position);

		if(game.isFinished() == 1){
			menu.setHeaderTitle("Options");  
			menu.add(0, 1, 0, "Add as friend");
			menu.add(0, 3, 0, "New game w/opponent");
			menu.add(0, 6, 0, "Chat");
			menu.add(0, 4, 0, "Remove from list");
			menu.add(0, 5, 0, "Remove all finished games");
		}
		else {
			menu.setHeaderTitle("Options");  
			menu.add(0, 1, 0, "Add as friend");
			menu.add(0, 6, 0, "Chat");
			menu.add(0, 2, 0, "Give up");
			menu.add(0, 3, 0, "New game w/opponent");
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		Game game = (Game) adapter.getItem(info.position);

		switch (item.getItemId()) {
		case 1:
			CommonFunctions.sendFriendRequest("username", game.getOpponentsUsername().get(0), loginSettings, context);
			return true;
		case 2:
			CommonFunctions.alertForGiveUp(context, game.getGameId(), giveUpListener, loginSettings, CommonFunctions.FROM_ALL_GAMESACTIVITY);
			return true;
		case 3:
			CommonFunctions.startGameFromUsername(this, game.getOpponentsUsername().get(0), game.getType(), null, loginSettings);
			return true;
		case 4:
			removeFromList(game.getGameId());
			return true;
		case 5:
			removeAllFinishedGames();
			return true;
		case 6:
			startChatActivity(Integer.toString(game.getOpponentId()), Integer.toString(game.getGameId()), game.getOpponentsUsername().get(0));
			Extrainfo.setNewChatMsg(getSharedPreferences(Extrainfo.PREFS_NAME, 0), Integer.toString(game.getGameId()), false);
			adapter.notifyDataSetChanged();
		default:
			return super.onContextItemSelected(item);
		}
	}
	
	public void startChatActivity(String opponentId, String gameId, String oppUsername){
		Intent intent = new Intent().setClass(this, ChatActivity.class);
		intent.putExtra("opponentId", opponentId);
		intent.putExtra("gameId", gameId);
		intent.putExtra("opponentUsername", oppUsername);

		startActivity(intent);
	}

	@Override
	public void onResume() {
		super.onResume();
		registerReceiver(gcmReceiver, gcmFilter);
		
		// if session expired
		if(Login.isSessionExpired(loginSettings) ){
			Intent allGamesActivity = new Intent().setClass(this, LoginActivity.class);
			startActivity(allGamesActivity);
			finish();			// can't return to this activity when signed in
			return;
		}
		
		startService();
		CommonFunctions.findNewGameRequests(this, loginSettings);

    AdView adView = (AdView)this.findViewById(R.id.adView);
    adView.loadAd(new AdRequest());
	}

	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(gcmReceiver);
		stopService();
	}
	
	public void startService(){
		serviceintent.putExtra(TimerService.URL, Extrainfo.getAllGamesUrl(extraInfoSettings));
		serviceintent.putExtra("broadcast", TimerService.BROADCAST_ACTION_GAMES);
		startService(serviceintent);
		registerReceiver(broadcastReceiver, new IntentFilter(TimerService.BROADCAST_ACTION_GAMES));
	}
	public void stopService(){
		if(broadcastReceiver != null || serviceintent != null){
			try{
				unregisterReceiver(broadcastReceiver);
			}
			catch(IllegalArgumentException e) { Log.d("IllegalArgumentException", "broadcastReceiver not regged"); }
		
			stopService(serviceintent);
		}
	}

	public void findAndRemoveGameObj(int gameId){
		int pos = 0;
		if((pos = findPosInListFromGameId(mineTurnList, gameId)) >= 0){
			removeGameObj(mineTurnList, pos, yourTurnListSection);
		}
		else if((pos = findPosInListFromGameId(opponentTurnList, gameId)) >= 0){
			removeGameObj(opponentTurnList, pos, opponentTurnListSection);
		}
		adapter.notifyDataSetChanged();
	}

	@Override
	protected void onListItemClick(ListView list, View view, int position, long id) {
		super.onListItemClick(list, view, position, id);
		Game game = (Game) adapter.getItem(position);	

		if(game.isFinished() == 1) {
			Intent finishIntent = new Intent(this, GameFinishActivity.class);
			finishIntent.putExtra(GameActivity.SELECTED_GAME_ID, game.getGameId());
			finishIntent.putExtra("cards", game.getOpenCardParents().toString());
			finishIntent.putExtra("action", game.getLastAction());
			finishIntent.putExtra("opponent", game.getOpponentsUsername().get(0));
			finishIntent.putExtra("type", game.getType());
			finishIntent.putExtra("opponentId", game.getOpponentId());
			finishIntent.putExtra("last_updated", game.getTimeSinceLastMove());
			finishIntent.putExtra("date_created", game.getDateCreated());
			finishIntent.putExtra("image", game.getImageId());
			finishIntent.putExtra("playersTurn", game.getPlayersTurn());
			finishIntent.putExtra("YOUR_CARDS", game.getYourCards().toString());	
			finishIntent.putExtra("openCards", game.getOpenCards().toString());
			finishIntent.putExtra("openCardParents", game.getOpenCardParents().toString());
			finishIntent.putExtra("score", game.getScore());

			startActivity(finishIntent);
		}
		else {
			Intent gameIntent = new Intent(this, GameActivity.class);
			gameIntent.putExtra(GameActivity.SELECTED_GAME_ID, game.getGameId());
			gameIntent.putExtra("STATE", game.getState());
			gameIntent.putExtra("openCard", game.getOpenCard());
			gameIntent.putExtra("action", game.getLastAction());
			gameIntent.putExtra("YOUR_CARDS", game.getYourCards().toString());	
			gameIntent.putExtra("openCards", game.getOpenCards().toString());
			gameIntent.putExtra("opponentId", game.getOpponentId());
			gameIntent.putExtra("opponent", game.getOpponentsUsername().get(0));
			gameIntent.putExtra("type", game.getType());
			gameIntent.putExtra("playersTurn", game.getPlayersTurn());
			gameIntent.putExtra("last_updated", game.getTimeSinceLastMove());
			gameIntent.putExtra("openCardParents", game.getOpenCardParents().toString());
			gameIntent.putExtra("date_created", game.getDateCreated());
			gameIntent.putExtra("image", game.getImageId());


			startActivityForResult(gameIntent, BACK_FROM_GAMER);
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == BACK_FROM_GAMER){
			if(resultCode == RESULT_OK){
				String result= data.getStringExtra("gameInfo");
				Log.d("reslt from gameactiviy", result);
				try {
					JSONObject gameData = new JSONObject(result);
					createGameObjAndAddToView(gameData, true);
				}
				catch(JSONException e) { e.printStackTrace(); }
			}
		}
	}

	/* Removes a game from finished game list */
	private void removeFromList(int gameId){
		int pos = 0;
		
		stopService();

		if ((pos = findPosInListFromGameId(finishedGamesList, gameId)) >= 0){
			ResponseListener responseListener = new ResponseListener() {				
				@Override
				public void onResponseReceived(HttpResponse response, String message) {
					Toast.makeText(context, "Game removed", Toast.LENGTH_LONG).show();
					startService();
				}
			};
			removeGameFromUsersGames(finishedGamesList.get(pos).getGameId(), responseListener, loginSettings);
			removeGameObj(finishedGamesList, pos, finishedTurnListSection);
			adapter.notifyDataSetChanged();
		}
	}

	private void removeAllFinishedGames(){
		ResponseListener responseListener = new ResponseListener() {				
			@Override
			public void onResponseReceived(HttpResponse response, String message) {
				Toast.makeText(context, "Games removed", Toast.LENGTH_LONG).show();
				startService();
			}
		};
		stopService();
		Iterator<Game> iter = finishedGamesList.iterator();
		JSONArray jArray = new JSONArray();

		while(iter.hasNext()){
			Game game = (Game) iter.next();
			jArray.put(game.getGameId());
		}
		removeAllGamesFromFinished(jArray, responseListener, loginSettings);

		finishedGamesList.clear();
		adapter.notifyDataSetChanged();
	}
	
	public void testClickOnImage(){
		Toast.makeText(this, "hallais", Toast.LENGTH_LONG).show();
	}

	public void evaluateResponse(String message){
		if(message.equals("[]")){
			Button button = (Button) findViewById(R.id.startNewGameButton);
			button.setVisibility(View.VISIBLE);
			return;
		}

		try {
			JSONObject response = new JSONObject(message);

			if(response.has("error"))
				new Alert("Uups", response.getString("error"), this);
			else{
				createGameObjAndAddToView(response, true);
			}
		}
		catch (JSONException e) { 
			e.printStackTrace();
			new Alert("Uups", "An error occured", this);
		}
	}

	public void startNewGame(View v){
		Intent newGameIntent = new Intent(this, NewGameActivity.class);
		startActivityForResult(newGameIntent, RANDOM_GAMEREQ_RESP);
	}

	public void startSettingsActivity(View v){
		Intent newSettingsIntent = new Intent(this, SettingsActivity.class);
		startActivity(newSettingsIntent);
	}

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateUI(intent);
		}
	};

	private void updateUI(Intent intent) {
		try {
			if(intent.getExtras().getString("data").equals("Session expired")){
				Log.d("UPDATEUI", "SESSION EXPIRED");
				Intent loginIntent = new Intent(this, LoginActivity.class).putExtra("fromAllGamesActivity", true);
				startActivity(loginIntent);
				finish();
				return;
			}

			JSONArray jarray = new JSONArray((String)intent.getExtras().getString("data"));

			for(int counter = 0; counter < jarray.length(); counter ++){
				createGameObjAndAddToView((JSONObject) jarray.get(counter), false);
			}
			// Update view with new adapter and stuff
			setNewView();
		}
		catch (JSONException e) { e.printStackTrace(); }
	}

	private void createGameObjAndAddToView(JSONObject obj, boolean setNewViews){
		Game game = new Game();

		try {
			int gameId = obj.getInt("GID");
			int userId = Login.getUserId(loginSettings);
			int playersTurn = 0;

			game.setGameId(gameId);
			game.setOpponentsUsername(obj.getString("opponent"));
			game.setOpponentId(obj.getInt("opponentId"));
			game.setType(obj.getInt("type"));
			game.setDateCreated(obj.getString("date_created"));
			game.setYourCards(obj.getJSONArray("yourCards"));
			game.setOpenCards(obj.getJSONArray("openCards"));
			
			playersTurn = CommonFunctions.safeLongToInt(obj.getLong("playersTurn"));
			game.setPlayersTurn(playersTurn);
			
			if(obj.has("image"))
				game.setImageId(obj.getInt("image"));

			if (obj.getInt("finished") == 1){
				game.setState(obj.getInt("state"));
				game.setLastAction(obj.getString("action"));
				game.setOpenCardParents(obj.getJSONArray("cards"));
				game.setFinished(1);
				game.setScore(obj.getInt("score"));
			}
			else {
	

				if(obj.getInt("openCard") < 0){
					game.setState(GameActivity.OPPONENTS_TURN);
					game.setLastAction("Waiting for " + obj.getString("opponent") + " to finish start up");
				}
				else if(playersTurn != userId && obj.getInt("state") != GameActivity.INIT){
					game.setState(GameActivity.OPPONENTS_TURN);
					game.setLastAction(obj.getString("action"));
				}
				else{
					game.setState(obj.getInt("state"));
					game.setLastAction(obj.getString("action"));
				}
				game.setOpenCard(obj.getInt("openCard"));
				game.setOpenCardParents(obj.getJSONArray("openCardParents"));
			}
			game.setTimeSinceLastMove(obj.getString("last_updated"));

			addToList(game, playersTurn, userId, gameId, obj.getInt("finished"), game.getState());

			if(setNewViews)
				setNewView();
		}
		catch (JSONException e) { e.printStackTrace(); }
	}

	private void setNewView(){
		boolean setAdapter = false;

		if(adapter == null){
			adapter = new SeparatedListAdapter(this);
			setAdapter = true;
		}
		
		adapter.removeSection(yourTurnListSection);
		adapter.removeSection(opponentTurnListSection);
		adapter.removeSection(finishedTurnListSection);

		if(!mineTurnList.isEmpty()){
			adapter.addSection(yourTurnListSection, new GameAdapter (this, android.R.layout.activity_list_item, mineTurnList));
		}
		if(!opponentTurnList.isEmpty()){
			adapter.addSection(opponentTurnListSection, new GameAdapter (this, android.R.layout.activity_list_item, opponentTurnList)); 
		}
		if(!finishedGamesList.isEmpty())
			adapter.addSection(finishedTurnListSection, new GameAdapter (this, android.R.layout.activity_list_item, finishedGamesList));

		if(adapter.getSectionsCount() < 1)
			setNewgameButtonVisible();
		else
			setNewgameButtonUnVisible();
		
		if(setAdapter)
			setListAdapter(adapter);
		else
			adapter.notifyDataSetChanged();
	}

	private void addToList(Game game, int playersTurn, int userId, int gameId, int finish, int state){
		int pos;

		if(finish > 0){
			if((pos = findPosInListFromGameId(opponentTurnList, gameId)) >= 0)
				removeGameObj(opponentTurnList, pos, opponentTurnListSection);

			if((pos = findPosInListFromGameId(mineTurnList, gameId)) >= 0)
				removeGameObj(mineTurnList, pos, yourTurnListSection);
			
			if((pos = findPosInListFromGameId(finishedGamesList, gameId)) >= 0)
				removeGameObj(finishedGamesList, pos, finishedTurnListSection);
			
			finishedGamesList = addGameObjToList(finishedGamesList, game);
			return;
		}
		
		// If init add in my turn anyway
		if(state == GameActivity.INIT){
			if((pos = findPosInListFromGameId(mineTurnList, gameId)) >= 0)
				removeGameObj(mineTurnList, pos, yourTurnListSection);
			
			ArrayList<Game> tmpList = new ArrayList<Game>();
			tmpList = addGameObjToList(mineTurnList, game);
			
			mineTurnList = tmpList;
			return;
		}

		if(playersTurn == userId && state != GameActivity.OPPONENTS_TURN){
			if((pos = findPosInListFromGameId(opponentTurnList, gameId)) >= 0)
				removeGameObj(opponentTurnList, pos, opponentTurnListSection);

			if((pos = findPosInListFromGameId(mineTurnList, gameId)) >= 0)
				removeGameObj(mineTurnList, pos, yourTurnListSection);
			
			mineTurnList = addGameObjToList(mineTurnList, game);
			return;
		}

		// If it is not the users turn for this game. Update opponentsTurnList
		// and remove from mineTurnList if exists
		if((pos = findPosInListFromGameId(mineTurnList, gameId)) >= 0)
			removeGameObj(mineTurnList, pos, yourTurnListSection);

		if((pos = findPosInListFromGameId(opponentTurnList, gameId)) >= 0)
			removeGameObj(opponentTurnList, pos, opponentTurnListSection);
		
		opponentTurnList = addGameObjToList(opponentTurnList, game);
	}

	private int findPosInListFromGameId(ArrayList<Game> gamesList, int gameId){
		Game gameObjInList;

		for(int iter = 0; iter < gamesList.size(); iter++){
			gameObjInList = gamesList.get(iter);

			if(gameId == gameObjInList.getGameId())
				return iter;
		}
		return -1;
	}

	private void removeGameObj(ArrayList<Game> list, int pos, String section){
		list.remove(pos);
	}
	
	private ArrayList<Game> addGameObjToList(ArrayList<Game> list, Game game){
		if(!list.isEmpty()){
			for(int index=0; index < list.size(); index ++){
      	String timeLast = list.get(index).getTimeSinceLastMove();
      	String tmpInput = game.getTimeSinceLastMove();
      	
				DateFormat myDateFormat = new SimpleDateFormat("HH:mm:ss");
				
      	try {
					Date tempLastDate = myDateFormat.parse(timeLast);
					long millisecondsLast = tempLastDate.getTime();
					tempLastDate.setTime(millisecondsLast);
					
					Date tempInputDate = myDateFormat.parse(tmpInput);
					long millisecondsDate = tempInputDate.getTime();
					tempInputDate.setTime(millisecondsDate);	

					if(tempInputDate.compareTo(tempLastDate) < 0){
						list.add(index, game);
						return list;
					}
				}
				catch (ParseException e) { e.printStackTrace(); }
			}
			list.add(game);
		}
		else{
			list.add(game);
		}
		return list;
	}

	private void setNewgameButtonVisible(){
		Button button = (Button) findViewById(R.id.startNewGameButton);
		button.setVisibility(View.VISIBLE);
	}

	private void setNewgameButtonUnVisible(){
		Button button = (Button) findViewById(R.id.startNewGameButton);
		button.setVisibility(View.GONE);
	}

	public static void removeAllGamesFromFinished(JSONArray gameIds, ResponseListener responseListener, SharedPreferences loginSettings){
		JSONObject postBody = new JSONObject();

		try {
			postBody.put("gids", gameIds);
		}

		catch (JSONException e) { e.printStackTrace(); }

		HttpPost httpPost = BuildHttpRequest.setEntity(postBody, "http://restfulserver.herokuapp.com/game/remove_games");

		AsynchronousHttpClient a = new AsynchronousHttpClient();
		a.sendRequest(httpPost, responseListener, loginSettings);
	}

	public static void removeGameFromUsersGames(int gameId, ResponseListener responseListener, SharedPreferences loginSettings){
		JSONObject postBody = new JSONObject();

		try {
			postBody.put("gid", gameId);
		}

		catch (JSONException e) { e.printStackTrace(); }

		HttpPost httpPost = BuildHttpRequest.setEntity(postBody, "http://restfulserver.herokuapp.com/game/remove_game");

		AsynchronousHttpClient a = new AsynchronousHttpClient();
		a.sendRequest(httpPost, responseListener, loginSettings);
	}
}