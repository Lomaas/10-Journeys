package com.main.activitys;

import java.util.ArrayList;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.markupartist.android.widget.ActionBar.IntentAction;
import com.main.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uit.nfc.AsynchronousHttpClient;
import uit.nfc.ResponseListener;
import com.main.activitys.domain.Extrainfo;
import com.main.activitys.domain.Game;
import com.main.helper.*;
import com.main.activitys.domain.Login;
import com.main.service.TimerService;
import android.app.ListActivity;
import android.content.BroadcastReceiver;

import com.facebook.android.Facebook;
import com.google.android.gcm.GCMRegistrar;
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

public class AllGamesActivity extends ListActivity {
	private Intent serviceintent;
	private SharedPreferences loginSettings;
	private SharedPreferences extraInfoSettings;
	public ResponseListener giveUpListener;
	public ResponseListener startGameListener;
	public Context context;
	public Facebook facebook = new Facebook("271971842906436");
	
	public static String NOTIFICATION_FRIEND_REQUEST = "NOTIFICATION_FRIEND_REQUEST";
	public static int RANDOM_GAMEREQ_RESP = 1;
	public static int BACK_FROM_GAMER = 2;
	private boolean resetSections = false;

	public static String GIVE_UP_URL = "http://restfulserver.herokuapp.com/finish/give_up";

	//	private DbAdapter mDbHelper;
	public static Integer[] imageArray = {
		R.drawable.ic_launcher,
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

	private SeparatedListAdapter adapter; 
	private ArrayList<Game> mineTurnList;
	private ArrayList<Game> opponentTurnList;
	private ArrayList<Game> finishedGamesList;

	public static String opponentTurnListSection = "Opponents turn";
	public static String yourTurnListSection = "Your turn";
	public static String finishedTurnListSection = "Finished games";


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.all_games);
		context = this;
		loginSettings = getSharedPreferences(Login.PREFS_NAME, 0);
		extraInfoSettings = getSharedPreferences(Extrainfo.PREFS_NAME, 0);
		//		updateDatabase();
		
		Bundle extras = getIntent().getExtras();
		
		if(extras != null && extras.getBoolean(NOTIFICATION_FRIEND_REQUEST)){
			Resources res = getResources();

			CommonFunctions.alertForAddFriend(res.getString(R.string.ticker_new_friend_title), extras.getInt("fid"), 
				extras.getString("opponentUsername") + " wants to add you to his friendslist. Confirm?", this, loginSettings);
		}
		
		final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle(R.string.app_name);
		
		final Action highscoreAction = new IntentAction(this, new Intent(this, HighscoreActivity.class), R.drawable.ic_launcher);
		actionBar.addAction(highscoreAction);
		
		final Action settingsAction = new IntentAction(this, new Intent(this, SettingsActivity.class), R.drawable.ic_menu_settings);
		actionBar.addAction(settingsAction);

		final Action newGameAction = new IntentAction(this, new Intent(this, NewGameActivity.class), R.drawable.ic_menu_add);
		actionBar.addAction(newGameAction);

		mineTurnList = new ArrayList<Game>();
		opponentTurnList = new ArrayList<Game>();
		finishedGamesList = new ArrayList<Game>();

		adapter = new SeparatedListAdapter(this);

		setListAdapter(adapter);
		serviceintent = new Intent("com.main.service.TimerService");
		serviceintent.putExtra(TimerService.URL, Extrainfo.getAllGamesUrl(extraInfoSettings));

		ListView lv = getListView(); 
		registerForContextMenu(lv);

		giveUpListener = new ResponseListener() {
			@Override
			public void onResponseReceived(HttpResponse response, String message) {
				Log.d("Response", response.toString());
				evaluateResponse(message);
			}
		};
		startGameListener = new ResponseListener() {
			@Override
			public void onResponseReceived(HttpResponse response, String message) {
				Log.d("Response", response.toString());
				evaluateResponse(message);
			}
		};
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		Log.i("oncreatecontextMenu", "inside here");
		Log.i("ID", Integer.toString(v.getId()));

		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		Log.i("ID", Integer.toString(info.position));

		Game game = (Game) adapter.getItem(info.position);

		if(game.isFinished() == 1){
			menu.setHeaderTitle("Options");  
			menu.add(0, 1, 0, "Add as friend");
			menu.add(0, 3, 0, "New game w/opponent");
			menu.add(0, 4, 0, "Remove from list");

		}
		else {
			menu.setHeaderTitle("Options");  
			menu.add(0, 1, 0, "Add as friend");
			menu.add(0, 2, 0, "Give up");
			menu.add(0, 3, 0, "New game w/opponent");
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		Log.i("ID", Integer.toString(info.position));

		Game game = (Game) adapter.getItem(info.position);
		Log.i("LastAction", game.getLastAction());
		Log.i("contextItemSelceted ID: ", Integer.toString(item.getItemId()));

		switch (item.getItemId()) {
		case 1:
			CommonFunctions.sendFriendRequest("username", game.getOpponentsUsername().get(0), loginSettings, context);
			return true;
		case 2:
			CommonFunctions.giveUp(game.getGameId(), giveUpListener, loginSettings);
			return true;
		case 3:
			CommonFunctions.startGameFromUsername(game.getOpponentsUsername().get(0), game.getType(), startGameListener, loginSettings);
			return true;
		case 4:
			removeFromList(game.getGameId());
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		//		mDbHelper.open();
    facebook.extendAccessTokenIfNeeded(this, null);
    
		Log.d("OnResume", "are youuuuuuu here");
		serviceintent.putExtra(TimerService.URL, Extrainfo.getAllGamesUrl(extraInfoSettings));
		serviceintent.putExtra("broadcast", TimerService.BROADCAST_ACTION_GAMES);
		startService(serviceintent);
		registerReceiver(broadcastReceiver, new IntentFilter(TimerService.BROADCAST_ACTION_GAMES));
	}

	@Override
	public void onPause() {
		super.onPause();
		//		mDbHelper.close();
		stopService();
	}

	public void stopService(){
		unregisterReceiver(broadcastReceiver);
		stopService(serviceintent);
	}

	@Override
	protected void onListItemClick(ListView list, View view, int position, long id) {
		super.onListItemClick(list, view, position, id);
		Log.d("adapter", adapter.getItem(position).toString());
		Game game = (Game) adapter.getItem(position);

		//		mDbHelper.close();

		if(game.isFinished() == 1){
			Intent finishIntent = new Intent(this, GameFinishActivity.class);
			finishIntent.putExtra("cards", game.getOpenCards().toString());
			finishIntent.putExtra("action", game.getLastAction());
			finishIntent.putExtra("opponent", game.getOpponentsUsername().get(0));
			finishIntent.putExtra("type", game.getType());
			finishIntent.putExtra("opponentId", game.getOpponentId());

			startActivityForResult(finishIntent, BACK_FROM_GAMER);
		}
		else{
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

			startActivityForResult(gameIntent, BACK_FROM_GAMER);
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d("ALLGAMES RESULTCODE", Integer.toString(resultCode));
		Log.d("ALLGAMES REQUESTCODE", Integer.toString(requestCode));

		if (requestCode == BACK_FROM_GAMER){
			if(resultCode == RESULT_OK){
				String result= data.getStringExtra("gameInfo");
				Log.d("RESULT FROM GAME.PY", result);

				try{
					JSONObject gameData = new JSONObject(result);
					createGameObjAndAddToView(gameData, true);
				}
				catch(JSONException e) { e.printStackTrace(); }
			}
		}
	}

	private void removeFromList(int gameId){
		int pos = 0;
		Log.i("e du her?", "jeg");
		Log.i("gamedID", Integer.toString(findPosInListFromGameId(finishedGamesList, gameId)));
		Log.i(finishedGamesList.get(0).getLastAction(), "tets");
		if ((pos = findPosInListFromGameId(finishedGamesList, gameId)) >= 0){
			ResponseListener responseListener = new ResponseListener() {				
				@Override
				public void onResponseReceived(HttpResponse response, String message) {
					Toast.makeText(context, "Game removed", Toast.LENGTH_LONG).show();
				}
			};
			removeGameFromUsersGames(finishedGamesList.get(pos).getGameId(), responseListener, loginSettings);
			removeGameObj(finishedGamesList, pos, finishedTurnListSection);
			adapter.notifyDataSetChanged();
			Log.i("e du her?", "jeg1");

		}
		Log.i("e du her?", "jeg2");

	}

	public void evaluateResponse(String message){
		Log.i("evaluateResponse", message);
		if(message.equals("[]")){
			Log.i("evaulateResponse", "empty response!");
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
			Log.d("UPDATEUI", intent.getExtras().getString("data"));

			if(intent.getExtras().getString("data").equals("Session expired")){
				Log.d("UPDATEUI", "SESSION EXPIRED");
				Intent loginIntent = new Intent(this, LoginActivity.class);
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
			long playersTurn = 0;

			game.setGameId(gameId);
			game.setOpponentsUsername(obj.getString("opponent"));
			game.setOpponentId(obj.getInt("opponentId"));

			//	CommonFunctions.insertRecentPlayer(this, obj.getString("opponent"));

			if (obj.getInt("finished") == 1){
				Log.i("finish", "isFinished");
				game.setState(obj.getInt("state"));
				game.setLastAction(obj.getString("action"));
				game.setOpenCards(obj.getJSONArray("cards"));
				game.setFinished(1);
			}
			else {
				playersTurn = obj.getLong("playersTurn");

				if(obj.getInt("openCard") < 0){
					Log.d("sets state", "opponents turn");
					game.setState(GameActivity.OPPONENTS_TURN);
					game.setLastAction(getResources().getString(R.string.action_waiting));
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
				game.setYourCards(obj.getJSONArray("yourCards"));
				game.setType(obj.getInt("type"));
				game.setOpenCards(obj.getJSONArray("openCards"));
			}
			game.setTimeSinceLastMove(obj.getString("last_updated"));

			addToList(game, playersTurn, userId, gameId, obj.getInt("finished"), game.getState());
		
			if(setNewViews)
				setNewView();

			resetSections = false;
		}
		catch (JSONException e) { e.printStackTrace(); }
	}

	private void setNewView(){
		adapter = new SeparatedListAdapter(this);
		
		adapter.removeSection(yourTurnListSection);
		adapter.removeSection(opponentTurnListSection);
		adapter.removeSection(finishedTurnListSection);

		if(!mineTurnList.isEmpty())
			adapter.addSection(yourTurnListSection, new GameAdapter (this, android.R.layout.activity_list_item, mineTurnList));
		if(!opponentTurnList.isEmpty())
			adapter.addSection(opponentTurnListSection, new GameAdapter (this, android.R.layout.activity_list_item, opponentTurnList));  
		if(!finishedGamesList.isEmpty())
			adapter.addSection(finishedTurnListSection, new GameAdapter (this, android.R.layout.activity_list_item, finishedGamesList));

		setListAdapter(adapter);
		adapter.notifyDataSetChanged();
		
		if(adapter.getSectionsCount() < 1)
			setNewgameButtonVisible();
		else
			setNewgameButtonUnVisible();
	}

	private void addToList(Game game, long playersTurn, int userId, int gameId, int finish, int state){
		int pos;

		if(finish > 0){
			Log.i("inside finish", Integer.toString(finish));
			if((pos = findPosInListFromGameId(opponentTurnList, gameId)) >= 0)
				removeGameObj(opponentTurnList, pos, opponentTurnListSection);

			if((pos = findPosInListFromGameId(mineTurnList, gameId)) >= 0)
				removeGameObj(mineTurnList, pos, yourTurnListSection);

			if((pos = findPosInListFromGameId(finishedGamesList, gameId)) >= 0){
				finishedGamesList.set(pos, game);
				Log.i("inside finish", "overwrites pos");
			}
			else{
				Log.i("inside finish", "adds pos");
				finishedGamesList.add(game);	
			}
			return;
		}

		if(state == GameActivity.INIT){
			if((pos = findPosInListFromGameId(mineTurnList, gameId)) >= 0){
				Log.i("state init", "should overwrite!");

				mineTurnList.set(pos, game);
			}
			else{
				Log.i("state init", "should add!");
				mineTurnList.add(game);
			}
			return;
		}

		if(playersTurn == userId && state != GameActivity.OPPONENTS_TURN){
			if((pos = findPosInListFromGameId(opponentTurnList, gameId)) >= 0)
				removeGameObj(opponentTurnList, pos, opponentTurnListSection);

			if((pos = findPosInListFromGameId(mineTurnList, gameId)) >= 0)
				mineTurnList.set(pos, game);
			else
				mineTurnList.add(game);

			return;
		}

		// If it is not the users turn for this game. Update opponentsTurnList
		// and remove from mineTurnList if exists
		if((pos = findPosInListFromGameId(mineTurnList, gameId)) >= 0)
			removeGameObj(mineTurnList, pos, yourTurnListSection);

		if((pos = findPosInListFromGameId(opponentTurnList, gameId)) >= 0)
			opponentTurnList.set(pos, game);

		else
			opponentTurnList.add(game);
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
		if(list.isEmpty()){
			resetSections = true;
		}
	}

	//	// Adds the gameObj sorted by last updated
	//	private void addGameObjToList(ArrayList<Game> list, Game game){
	//		for(int i=0; i < list.size(); i++){
	//			if(list.get(i).getTimeSinceLastMove() >
	//		}
	//	}

	private void setNewgameButtonVisible(){
		Button button = (Button) findViewById(R.id.startNewGameButton);
		button.setVisibility(View.VISIBLE);
	}

	private void setNewgameButtonUnVisible(){
		Button button = (Button) findViewById(R.id.startNewGameButton);
		button.setVisibility(View.GONE);
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
	
	//	allCardsStrings = ["empty", "Norway", "Sweden", "Denmark", "Finland", "Iceland", "Russia", "Ukrania", 
	//	       						"Belgium", "Nederland", "United-Kingdom", "Germany", "Poland", "Estland", "Spain", 
	//	       						"France", "Portugal", "Italy", "Greece", "Turkey", "Romania", "Latvia", "Moldova",
	//	       						"Serbia", "Austria", "Belarus", "Estonia", "Hungary", "Ireland", "Lithuania", "Macedonia",
	//	       						"Malta", "Slovakia"]

	//	private void updateDatabase(){
	//		mDbHelper = new DbAdapter(this);
	//		mDbHelper.open();
	//		mDbHelper.wipeAndCreateDatabase();
	//		mDbHelper.insertCardIdToImageTable(R.drawable.cards1, 1);
	//		mDbHelper.insertCardIdToImageTable(R.drawable.cards2, 2);
	//		mDbHelper.insertCardIdToImageTable(R.drawable.cards3, 3);
	//		mDbHelper.insertCardIdToImageTable(R.drawable.cards4, 4);
	//		mDbHelper.insertCardIdToImageTable(R.drawable.cards5, 5);
	//		mDbHelper.insertCardIdToImageTable(R.drawable.cards6, 6);	// russia
	//		mDbHelper.insertCardIdToImageTable(R.drawable.cards7, 7);	// Ukrania
	//		mDbHelper.insertCardIdToImageTable(R.drawable.cards8, 8);
	//		mDbHelper.insertCardIdToImageTable(R.drawable.cards9, 9);
	//		mDbHelper.insertCardIdToImageTable(R.drawable.cards10, 10);
	//		mDbHelper.insertCardIdToImageTable(R.drawable.cards11, 11);
	//		mDbHelper.insertCardIdToImageTable(R.drawable.cards12, 12);
	//		mDbHelper.insertCardIdToImageTable(R.drawable.cards13, 13);
	//		mDbHelper.insertCardIdToImageTable(R.drawable.cards14, 14);
	//		mDbHelper.insertCardIdToImageTable(R.drawable.cards15, 15);
	//		mDbHelper.insertCardIdToImageTable(R.drawable.cards16, 16);
	//		mDbHelper.insertCardIdToImageTable(R.drawable.cards17, 17);
	//		mDbHelper.insertCardIdToImageTable(R.drawable.cards18, 18);
	//		mDbHelper.insertCardIdToImageTable(R.drawable.cards19, 19);
	//		mDbHelper.insertCardIdToImageTable(R.drawable.cards20, 20);
	//		mDbHelper.insertCardIdToImageTable(R.drawable.cards21, 21);
	//		mDbHelper.insertCardIdToImageTable(R.drawable.cards22, 22);
	//		mDbHelper.insertCardIdToImageTable(R.drawable.cards23, 23);
	//		mDbHelper.insertCardIdToImageTable(R.drawable.cards24, 24);
	//		mDbHelper.insertCardIdToImageTable(R.drawable.cards25, 25);
	//		mDbHelper.insertCardIdToImageTable(R.drawable.cards26, 26);
	//		mDbHelper.insertCardIdToImageTable(R.drawable.cards27, 27);
	//		mDbHelper.insertCardIdToImageTable(R.drawable.cards28, 28);
	//		mDbHelper.insertCardIdToImageTable(R.drawable.cards29, 29);
	//		mDbHelper.insertCardIdToImageTable(R.drawable.cards30, 30);
	//		mDbHelper.insertCardIdToImageTable(R.drawable.cards31, 31);
	//		mDbHelper.insertCardIdToImageTable(R.drawable.cards32, 32);
	//		mDbHelper.insertCardIdToImageTable(R.drawable.cards33, 33);
	//		mDbHelper.insertCardIdToImageTable(R.drawable.cards34, 34);
	//		mDbHelper.insertCardIdToImageTable(R.drawable.cards35, 35);
	//		mDbHelper.insertCardIdToImageTable(R.drawable.cards36, 36);
	//		mDbHelper.insertCardIdToImageTable(R.drawable.cards37, 37);
	//		mDbHelper.insertCardIdToImageTable(R.drawable.cards38, 38);
	//		mDbHelper.insertCardIdToImageTable(R.drawable.cards39, 39);
	//		mDbHelper.insertCardIdToImageTable(R.drawable.cards40, 40);
	//		mDbHelper.close();
	//	}
}