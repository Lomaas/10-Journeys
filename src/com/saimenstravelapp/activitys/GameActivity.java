/**
 * 
 */
package com.saimenstravelapp.activitys;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.saimenstravelapp.activitys.domain.Extrainfo;
import com.saimenstravelapp.activitys.domain.Game;
import com.saimenstravelapp.activitys.domain.GameGUI;
import com.saimenstravelapp.activitys.domain.Login;
import com.saimenstravelapp.drag.DragController;
import com.saimenstravelapp.drag.DragLayer;
import com.saimenstravelapp.drag.DragSource;
import com.saimenstravelapp.drag.ImageCell;
import com.saimenstravelapp.drag.ImageCellAdapter;
import com.saimenstravelapp.helper.Alert;
import com.saimenstravelapp.helper.CommonFunctions;
import com.saimenstravelapp.helper.Constants;
import com.saimenstravelapp.helper.ProgressDialogClass;
import com.saimenstravelapp.service.AsyncTaskDelegate;
import com.saimenstravelapp.service.TimerService;
import com.markupartist.android.widget.ActionBar;
import com.saimenstravelapp.*;
import com.markupartist.android.widget.ActionBar.Action;
import com.revmob.RevMob;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import async.httprequest.AsynchronousHttpClient;
import async.httprequest.ResponseListener;

// e9f0c2

public class GameActivity extends Activity implements View.OnLongClickListener, View.OnClickListener, View.OnTouchListener, AsyncTaskDelegate<GameGUI> //  , AdapterView.OnItemClickListener
{
	public static String SELECTED_GAME_USERNAME_OPPONENT = "selectedGameUsername";
	public static String SELECTED_GAME_ID = "selectedGameId";

	private Game gameObject = new Game();
	public static String gameUrl;
	private Context context;
	public String last_updated;
	public int gameId;
	public int cellTakenFrom = -1;		// used to see which card the player took from OpenCards

	private ResponseListener responseListener;
	private ResponseListener responseForNextCard;
	public SharedPreferences loginSettings;
	public SharedPreferences extraInfo;

	private Intent serviceintent;
	private ProgressDialogClass progDialog;

	IntentFilter gcmFilter;
	private BroadcastReceiver gcmReceiver = null;

	//	private DbAdapter mDbHelper;
	private Bundle extras;
	private DragController mDragController;   // Object that handles a drag-drop sequence. It intersacts with DragSource and DropTarget objects.
	private DragLayer mDragLayer;             // The ViewGroup within which an object can be dragged.

	public int STATE = 1;							// default INIT
	private int previousState = 1;
	private boolean finished = false;
	public static final int INIT = 1;
	public static final int YOUR_TURN = 2;
	public static final int YOUR_TURN_FROM_DECK = 3;
	public static final int YOUR_TURN_INSERT_TO_OPEN_CARDS = 4;
	public static final int YOUR_TURN_SWAP = 5;
	public static final int OPPONENTS_TURN = 6;
	public static final int GAME_OVER = 7;

	private boolean mLongClickStartsDrag = false;   // If true, it takes a long click to start the drag operation.
	public static final boolean Debugging = true;   // Use this to see extra toast messages.
	private boolean polling = false;

	// Just replace the ID below with your appID.
	private static String APPLICATION_ID = "51110aa6077d687403000071";
	private RevMob revmob;

	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		extras = getIntent().getExtras();
		gameId = extras.getInt(SELECTED_GAME_ID);

		if(extras.containsKey("finished"))
			finished = extras.getBoolean("finished");


		if(finished == false){
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
			setContentView(R.layout.game);

		}
		else {
			setContentView(R.layout.game);
			removeSettingsAndChatIcon();
			addActionBar();
		}

		context = this;
		serviceintent = new Intent("com.saimenstravelapp.service.TimerService");

		WebView webView = (WebView)findViewById(R.id.mainMap);
		webView.setBackgroundColor(getResources().getColor(R.color.grid_background));
		webView.setVerticalScrollBarEnabled(false);
		webView.setHorizontalScrollBarEnabled(false);
		webView.getSettings().setLoadWithOverviewMode(true);
		webView.getSettings().setUseWideViewPort(true);

		webView.loadUrl("file:///android_asset/test.html");

		gameUrl = "http://restfulserver.herokuapp.com/game/" + Integer.toString(gameId);
		loginSettings = getSharedPreferences(Login.PREFS_NAME, 0);
		extraInfo = getSharedPreferences(Extrainfo.PREFS_NAME, 0);

		gcmReceiver = CommonFunctions.createBroadCastReceiver(context, loginSettings, CommonFunctions.FROM_GAME_ACTIVITY);
		gcmFilter = new IntentFilter();
		gcmFilter.addAction("GCM_RECEIVED_ACTION");

		setState(extras.getInt("STATE"));

		responseListener = new ResponseListener() {
			@Override
			public void onResponseReceived(HttpResponse response, String message) {
				progDialog.dissMissProgressDialog();
				confirmPlay(message);
			}
		};

		responseForNextCard = new ResponseListener() {
			@Override
			public void onResponseReceived(HttpResponse response, String message) {
				progDialog.dissMissProgressDialog();
				evaluateResponseForNextCard(message);
			}
		};
//		// Starting RevMob session
		revmob = RevMob.start(this, APPLICATION_ID);

		try {
			STATE = extras.getInt("STATE");
			String action = extras.getString("action");
			last_updated = extras.getString("last_updated");

			if(STATE == GameActivity.YOUR_TURN){
				addCardsOnTableAndCardStockButton();

				if(action.equals("The game is on"))
					action += ". Take a card from one of the 3 piles or from the card deck";
			}

			if(STATE == GameActivity.INIT && extras.getInt("openCard") >= 0){
				if(extras.getInt("openCard") > 0){
					addOpenCardToScreen(extras.getInt("openCard"));
				}

				// Set cards on table grid unVisible
				GridView openCardsGridView = (GridView) findViewById(R.id.gridForCardsOnTable);
				openCardsGridView.setVisibility(View.GONE);
			}
			else if(STATE == GameActivity.OPPONENTS_TURN){
				setViewForOpponentsTurn(true);

				if(action.equals("The game is on"))
					action += ". Waiting for " + extras.getString("opponent") + " to make his/her move";
			}
			else if(extras.getInt("openCard") > 0 && STATE == GameActivity.YOUR_TURN){
				setAllButtonUnvisible();
				this.setState(GameActivity.YOUR_TURN_FROM_DECK);
				addOpenCardToScreen(extras.getInt("openCard"));
			}
			else if(extras.getInt("openCard") < 0){
				setAllButtonUnvisible();
				addCardsOnTable();
			}

			TextView lastAction = (TextView) findViewById(R.id.lastAction);
			lastAction.setText(action);

			JSONArray tmpYourCards = new JSONArray(extras.getString("YOUR_CARDS"));	
			JSONArray tmpOpenCards = new JSONArray(extras.getString("openCards"));
			JSONArray openCardParents = new JSONArray(extras.getString("openCardParents"));

			ArrayList<GameGUI> gameGUIS = getGameGUI(tmpYourCards);
			ArrayList<GameGUI> openCards = getGameGUI(tmpOpenCards);

			ArrayList<GameGUI> openCardGui = new ArrayList<GameGUI>();
			openCardGui.add(openCards.get(0));
			openCardGui.add(openCards.get(1));
			openCardGui.add(openCards.get(2));

			setGraphicalInterface(gameGUIS, openCardGui);

			updateGameObject(gameId, extras.getString("action"), extras.getInt("openCard"), tmpOpenCards, extras.getString("opponent"), 
					extras.getInt("STATE"), last_updated, tmpYourCards, extras.getInt("playersTurn"), extras.getInt("type"), extras.getInt("opponentId"), openCardParents,
					extras.getString("date_created"), extras.getInt("image"));

			if(Extrainfo.isNewChatMsg(extraInfo, Integer.toString(gameId)))
				setGotNewMessage();
		}
		catch(JSONException e){
			e.printStackTrace();
			toast("ups.. something went wrong. Try reselecting the game");
		}
	}

	public void onResume(){
		super.onResume();
		registerReceiver(gcmReceiver, gcmFilter);
		doPolling();
		
		CommonFunctions.getGameInfo(gameId, this);
	}

	public void onPause(){
		super.onPause();
		unregisterReceiver(gcmReceiver);
		stopPolling();
	}

	@Override
	public void onBackPressed() {
		Intent returnIntent = new Intent();
		JSONObject obj = new JSONObject();
		int resultCode = Activity.RESULT_CANCELED;

		try{
			obj.put("state", gameObject.getState());
			obj.put("action", gameObject.getLastAction());
			obj.put("openCard", gameObject.getOpenCard());
			obj.put("yourCards", gameObject.getYourCards());
			obj.put("openCards", gameObject.getOpenCards());
			obj.put("GID", extras.getInt(SELECTED_GAME_ID));
			obj.put("opponent", gameObject.getOpponentsUsername().get(0));
			obj.put("opponentId", gameObject.getOpponentId());
			obj.put("finished", gameObject.isFinished());
			obj.put("last_updated", gameObject.getTimeSinceLastMove());
			obj.put("playersTurn", gameObject.getPlayersTurn());
			obj.put("type", gameObject.getType());
			obj.put("openCardParents", gameObject.getOpenCardParents());
			obj.put("date_created", gameObject.getDateCreated());
			obj.put("image", gameObject.getImageId());

			resultCode = Activity.RESULT_OK;
		}
		catch(JSONException e){ e.printStackTrace(); }

		returnIntent.putExtra("gameInfo", obj.toString());

		if (getParent() == null) {
			setResult(resultCode, returnIntent);
		} else {
			getParent().setResult(resultCode, returnIntent);
		}
		finish();
	}

	public void doPolling(){
		if(this.getState() == OPPONENTS_TURN && polling == false && finished != true){
			polling = true;
			serviceintent.putExtra(TimerService.URL, "http://restfulserver.herokuapp.com/game/" + extras.getInt(SELECTED_GAME_ID));
			serviceintent.putExtra("broadcast", TimerService.BROADCAST_ACTION_GAME);
			startService(serviceintent);
			registerReceiver(broadcastReceiver, new IntentFilter(TimerService.BROADCAST_ACTION_GAME));
		}
	}

	public void stopPolling(){
		if(polling){
			polling = false;
			unregisterReceiver(broadcastReceiver);
			stopService(serviceintent);
		}
	}

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			confirmPlay(intent.getExtras().getString("data"));
		}
	};

	public void goToChat(View v){
		Intent intent = new Intent().setClass(this, ChatActivity.class);
		String id = Integer.toString(gameObject.getGameId());
		intent.putExtra("opponentUsername", gameObject.getOpponentsUsername().get(0));
		intent.putExtra("gameId", id);
		intent.putExtra("opponentId", Integer.toString(gameObject.getOpponentId()));

		ImageView chatIcon = (ImageView) findViewById(R.id.chat);
		chatIcon.setImageResource(R.drawable.chat_icon_main_white);

		startActivity(intent);
	}


	// Initiating Menu XML file (menu.xml)
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.layout.menugame, menu);
		return true;
	}

	/**
	 * Event Handling for Individual menu item selected
	 * Identify single menu item by it's id
	 * */
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		final ResponseListener giveUpListener = new ResponseListener() {
			@Override
			public void onResponseReceived(HttpResponse response, String message) {
				confirmPlay(message);
			}	
		};
		int itemId = item.getItemId(); 

		if(itemId == R.id.menu_addFriend){
			CommonFunctions.sendFriendRequest("username", gameObject.getOpponentsUsername().get(0), loginSettings, context);
			return true;
		}
		else if(itemId == R.id.menu_newGame){
			CommonFunctions.startGameFromUsername(context, gameObject.getOpponentsUsername().get(0), gameObject.getType(), null, loginSettings);
			return true;
		}
		else if(itemId == R.id.menu_giveUp){
			CommonFunctions.alertForGiveUp(context, gameObject.getGameId(), giveUpListener, loginSettings, CommonFunctions.FROM_GAME_ACTIVITY);
			return true;
		}
		else if(itemId == R.id.menu_rules){
			Intent intent = new Intent().setClass(context, FullRuleset.class);
			startActivity(intent);
			return true;
		}
		else{
			return super.onOptionsItemSelected(item);
		}
	}    

	protected Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Options")
		.setItems(R.array.options_game, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				//toast("test");
			}
		});
		return builder.create();
	}

	public int getCardIdFromOpenCardParent(int cellNumber){
		JSONArray array = gameObject.getOpenCardParents();
//		Log.d("getCardIdFromOpenCardParent", array.toString());
		try {
			return array.getInt(cellNumber);
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public boolean hasOpenCardParent(int cellNumber){
		JSONArray array = gameObject.getOpenCardParents();
//		Log.d("hasOpenCardParent", array.toString());
		try {
			if(array.getInt(cellNumber) == 0)
				return false;
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		return true;
	}

	public int getDrawableIdFromCardId(int cardId){
		return Constants.imageArray[cardId];
	}

	// Goes in the database and gets the image resource
	public ArrayList<GameGUI> getGameGUI(JSONArray imageCells) throws JSONException{
		int cardId = 0;
		ArrayList<GameGUI> gameGUIS = new ArrayList<GameGUI>();

		if(imageCells.length() == 0){
			GameGUI yourCard = new GameGUI(0,0,0, false, 0, false);
			yourCard.setIndex(0);
			yourCard.setResourceId(-1);
			yourCard.setDraggable(true);
			gameGUIS.add(yourCard);
			return gameGUIS;
		}

		for(int index=0; index < imageCells.length(); index++){
			cardId = (Integer) imageCells.get(index);
			GameGUI yourCard = new GameGUI(0,0,0, false, 0, false);

			if(cardId > 0){
				//				Log.d("cardID", Integer.toString(cardId));
				//				Cursor cursor = mDbHelper.fetchImage(cardId);
				//				drawableId = cursor.getInt(0);

				yourCard.setIndex(index);
				yourCard.setResourceId(getDrawableIdFromCardId(cardId));
				yourCard.setCurrentCardId(cardId);

				if(this.STATE == GameActivity.INIT)
					yourCard.setDraggable(false);

				gameGUIS.add(yourCard);
			}
			else {
				//				Log.d("cardID", Integer.toString(cardId));
				yourCard.setIndex(0);
				yourCard.setResourceId(-1);
				yourCard.setCurrentCardId(0);
				gameGUIS.add(yourCard);
			}
		}
		return gameGUIS;
	}

	public void setGraphicalInterface(ArrayList<GameGUI> gameGUIS, ArrayList<GameGUI> openCardGui ){
		GridView gridView = (GridView) findViewById(R.id.image_grid_view);
		GridView openCardsGridView = (GridView) findViewById(R.id.gridForCardsOnTable);

		mDragController = new DragController(this);
		mDragLayer = (DragLayer) findViewById(R.id.drag_layer);
		mDragLayer.setDragController (mDragController);

		// Two differnet gridviews!
		mDragLayer.setGridView (gridView);
		mDragLayer.setOpenCardGridViewGridView(openCardsGridView);
		mDragController.setDragListener (mDragLayer);		
		Resources res = getResources();
		setAdapterForGrid(gridView, res.getInteger(R.integer.num_images), 85, 85, gameGUIS);
		setAdapterForGrid(openCardsGridView, 3, 115, 115, openCardGui);
	}



	public void setAdapterForGrid(GridView gridView, int numImages, int height, int width, ArrayList<GameGUI> gameGuis){
		if (gridView == null)
			Log.d("gridView", "not found");
		else {
			ImageCellAdapter cellAdapter = new ImageCellAdapter(this, numImages, height, width, gameGuis);	
			gridView.setAdapter (cellAdapter);
		}
	}

	private void updateGameGuiListInAdapter(GridView gridView, ArrayList<GameGUI> gameGUIS){
		ImageCellAdapter adapter = (ImageCellAdapter) gridView.getAdapter();
		adapter.setGameGUIS(gameGUIS);
		adapter.notifyDataSetChanged();
	}

	/**
	 * @param drawableId
	 * @param index
	 */
	private void addNewImageToCell(int resourceId, int index, int cardId) {
		//		Log.d("addNewImageToCell1", Integer.toString(resourceId) + " " + Integer.toString(index));

		GridView gridView = (GridView) findViewById(R.id.image_grid_view);	
		ImageCellAdapter adapter = (ImageCellAdapter) gridView.getAdapter();

		int numVisibleChildren = gridView.getChildCount();
		//		Log.d("numVisChildren", Integer.toString(numVisibleChildren));

		View view = gridView.getChildAt (index);
		ImageCell dropTarget = (ImageCell) adapter.getView(index, view, gridView);
		ImageCell newView = new ImageCell (this, resourceId);
		newView.setImageResource (resourceId);

		//		DragView dragView =  new DragView(this, BitmapFactory.decodeResource(getResources(), resourceId), 0, 0,
		//        15, 15, 15, 15);
		//		
		//		dropTarget.onDrop((DragSource) newView, 0, 0, 0, 0, dragView, (Object) newView);

		ImageView sourceView = (ImageView) newView;
		Drawable d = sourceView.getDrawable ();
		dropTarget.setFromGridId(gridView.getId());
		dropTarget.setDrawableId(resourceId);
		dropTarget.setCardId(cardId);
		dropTarget.mEmpty = false;		// always not empty when coming so far,

		int bg = R.color.cell_filled;
		dropTarget.setBackgroundResource (bg);

		if (d != null) {
			dropTarget.setImageDrawable (d);
		}
	}

	public void newCard(View v){
		if(this.getState() == GameActivity.INIT)			
			playMove(null); // This is to update the sever with info.
		else
			getNextCardInDeck();
	}

	public void getNextCardInDeck(){
		HttpGet httpGet = null;

		try {
			int gameId = extras.getInt(SELECTED_GAME_ID);
			String cardUrl = "http://restfulserver.herokuapp.com/cards/" + Integer.toString(gameId);

			httpGet = new HttpGet(new URI(cardUrl));
		}
		catch (URISyntaxException e) { e.printStackTrace(); }

		AsynchronousHttpClient a = new AsynchronousHttpClient();
		a.sendRequest(httpGet, responseForNextCard, loginSettings);

		progDialog = new 
				ProgressDialogClass(this, 
						"Getting next card",
						"Getting next card in the card deck",
						15000);
		progDialog.run();
	}

	public void evaluateResponseForNextCard(String response){
		if(this.getState() != GameActivity.INIT){
			this.setState(GameActivity.YOUR_TURN_FROM_DECK);		// sets new state!
			stopPolling();
		}
		try {
			JSONObject cards = new JSONObject(response);
			
			if(cards.has("notYourTurn")){
				this.setState(GameActivity.OPPONENTS_TURN);
				setViewForOpponentsTurn(false);
				new Alert("Uups", "Something went wrong at the server. Not your turn", this);
				doPolling();
				return;
			}
			if(cards.has("cardDeckReset"))
				new Alert("Card deck reset", "The card deck has been reset", this);
			
			int cardId = cards.getInt("cardId");
			setAllButtonUnvisible();
			gameObject.setOpenCard(cardId);
			addNewImageToScreen(getDrawableIdFromCardId(cardId), (FrameLayout) findViewById(R.id.image_source_frame_main), cardId, 0);
		}
		catch(JSONException e){ e.printStackTrace(); }
	}

	public void playMove(View v){
		postUpdate();
	}

	public JSONObject getStringResponse(){
		ImageCellAdapter yourCardAdapter= (ImageCellAdapter) ((GridView) findViewById(R.id.image_grid_view)).getAdapter();
		ImageCellAdapter cardsOnTableAdapter= (ImageCellAdapter) ((GridView) findViewById(R.id.gridForCardsOnTable)).getAdapter();

		JSONObject finalJsonResponse = new JSONObject();

		try {
			finalJsonResponse.put("yourCards", createJsonObjectFromArray(yourCardAdapter.getGameGUIS()));
			finalJsonResponse.put("openCards", createJsonObjectFromArray(cardsOnTableAdapter.getGameGUIS()));
			finalJsonResponse.put("UID", Login.getUserId(loginSettings));
			finalJsonResponse.put("GID", extras.getInt(SELECTED_GAME_ID));

			return finalJsonResponse;
		}
		catch (JSONException e) { e.printStackTrace(); }
		return null;
	}

	public void postUpdate(){
		String finalJsonResponse = getStringResponse().toString();

		HttpPost httpPost = null;
		StringEntity se = null;

		try {
			httpPost = new HttpPost(new URI(gameUrl));

			se = new StringEntity(finalJsonResponse.toString());
		}
		catch (URISyntaxException e1) { e1.printStackTrace(); }
		catch (UnsupportedEncodingException e) { e.printStackTrace(); }

		se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
		httpPost.setEntity(se);

		AsynchronousHttpClient a = new AsynchronousHttpClient();
		a.sendRequest(httpPost, responseListener, loginSettings);
		
		String header = null;
		String text = null;
		
		if(this.getState() == GameActivity.INIT){
			header = "Picks next card";
			text = "Please wait a moment...";
		}
		else {
			header = "Playing your move";
			text = "Please wait a moment...";		
		}
		
		progDialog = new ProgressDialogClass(this, header, text, 15000);
		progDialog.run();
	}

	/**
	 * @param gameGUIS
	 */
	private JSONArray createJsonObjectFromArray(ArrayList<GameGUI> gameGUIS) {
		JSONArray jArray = new JSONArray();
		JSONObject tmpJobject = null;

		for(GameGUI gameGui : gameGUIS) {
			try {
				tmpJobject = new JSONObject();

				if(gameGUIS.size() != 3){
					tmpJobject.put("currentCardId", gameGui.getCurrentCardId());
					tmpJobject.put("deletedCardId", gameGui.getDeletedCardId());
				}
				else{
					if(gameGui.isUpdated()){
						if(cellTakenFrom == gameGui.getIndex() && gameGui.getDeletedCardId() == 0){
							tmpJobject.put("deletedCardId", gameGui.getCurrentCardId());
							tmpJobject.put("currentCardId", 0);
						}
						else if(gameGui.isDraggable() && cellTakenFrom != gameGui.getIndex()){
							tmpJobject.put("currentCardId", gameGui.getCurrentCardId());
							tmpJobject.put("deletedCardId", 0);
						}
						else {
							//TODO disse testene er lagt til for å få kortstokk effekt - det som er i siste else er det som opprinnelig var her
							if(cellTakenFrom == gameGui.getIndex() && gameGui.getCurrentCardId() == this.getCardIdFromOpenCardParent(gameGui.getIndex())){
								tmpJobject.put("currentCardId", 0);
								tmpJobject.put("deletedCardId", gameGui.getDeletedCardId());
							}
							else{
								tmpJobject.put("currentCardId", gameGui.getCurrentCardId());
								tmpJobject.put("deletedCardId", gameGui.getDeletedCardId());
							}
						}
					}
					else {
						tmpJobject.put("currentCardId", 0);
						tmpJobject.put("deletedCardId", 0);
					}
				}
				tmpJobject.put("isUpdated", gameGui.isUpdated());

			}
			catch (JSONException e) {
				e.printStackTrace();
			}
			jArray.put(tmpJobject);
		}
		return jArray;
	}

	public void confirmPlay(String msg){
		try {
			JSONObject jResponse= new JSONObject(msg);
			
			if(jResponse.has("notYourTurn")){
				this.setState(GameActivity.OPPONENTS_TURN);
				setViewForOpponentsTurn(false);
				new Alert("Uups", "Something went wrong. It was not your turn", this);
				doPolling();
				return;
			}
			
			if(jResponse.has("error")){
				new Alert("Unvalid travelroute", jResponse.getString("error"), this);
				return;
			}

			if(jResponse.getInt("finished") == 1){
				gameIsFinished(jResponse.getJSONArray("cards").toString(), jResponse.getString("action"), jResponse.getInt("type"), extras.getString("opponent"),
						jResponse.getInt("player1"), jResponse.getInt("player2"), jResponse.getInt("wins_player1"), jResponse.getInt("wins_player2"), jResponse.getInt("score"));
				return;
			}
			
			JSONArray yourCards = jResponse.getJSONArray("yourCards");
			JSONArray openCardGui = jResponse.getJSONArray("openCards");
			JSONArray openCardParents = new JSONArray();
			try{
				openCardParents = jResponse.getJSONArray("openCardParents");
			}
			catch(Exception e){
				e.printStackTrace();
			}

			this.setState(jResponse.getInt("state"));
			String action = null;

			if(jResponse.has("action"))
				action = jResponse.getString("action");

			/* Special case when we are waiting for opponent */
			if(jResponse.getInt("openCard") < 0){
				this.setState(GameActivity.OPPONENTS_TURN);
				setAllButtonUnvisible();
				setViewForOpponentsTurn(false);
				action = "Waiting for " + gameObject.getOpponentsUsername().get(0) + " to finish start up";
			}
			// Not my turn
			else if(!isMyTurn(jResponse.getInt("playersTurn")) && this.getState() != GameActivity.INIT){
				this.setState(GameActivity.OPPONENTS_TURN);
				setViewForOpponentsTurn(false);

				if(action.equals("The game is on"))
					action += ". Waiting for " + gameObject.getOpponentsUsername().get(0) + " to make his/her move";
			}
			else if(jResponse.getInt("openCard") > 0 && STATE == GameActivity.YOUR_TURN){
				this.setState(GameActivity.YOUR_TURN_FROM_DECK);
				setAllButtonUnvisible();
				addOpenCardToScreen(jResponse.getInt("openCard"));
			}
			else if(jResponse.getInt("openCard") > 0){
				// This is in init phase
				addOpenCardToScreen(jResponse.getInt("openCard"));
			}
			else {
				// Its my turn
				//setGetNextCardButtonVisible();
				addCardsOnTableAndCardStockButton();
				if(action.equals("The game is on"))
					action += ". Take a card from one of the 3 piles or from the card deck";
			}

			TextView lastAction = (TextView) findViewById(R.id.lastAction);

			if(action != null)
				lastAction.setText(action);

			updateGameGuiListInAdapter((GridView) findViewById(R.id.image_grid_view), getGameGUI(yourCards));
			updateGameGuiListInAdapter((GridView) findViewById(R.id.gridForCardsOnTable), getGameGUI(openCardGui));

			updateGameObject(jResponse.getInt("GID"), jResponse.getString("action"), jResponse.getInt("openCard"), openCardGui, extras.getString("opponent"), 
					jResponse.getInt("state"), jResponse.get("last_updated").toString(), yourCards, jResponse.getInt("playersTurn"), jResponse.getInt("type"), 
					jResponse.getInt("opponentId"), openCardParents, jResponse.getString("date_created"), -1);

			cellTakenFrom = -1;

			if(this.getState() != GameActivity.OPPONENTS_TURN)
				stopPolling();
			else
				doPolling();
		}
		catch(JSONException e){
			e.printStackTrace();
			toast("Ups, something went wrong");
		}
	}

	public void updateGameObject(int gid, String action, int openCard, JSONArray openCardGui, String username, int state, String last_updated,
			JSONArray yourCards, int playersTurn, int type, int opponentId, JSONArray openCardParents, String dateCreated, int image){
		gameObject.setGameId(gid);
		gameObject.setLastAction(action);
		gameObject.setOpenCard(openCard);
		gameObject.setOpenCards(openCardGui);
		gameObject.setOpponentsUsername(username);
		gameObject.setState(state);
		gameObject.setYourCards(yourCards);
		gameObject.setPlayersTurn(playersTurn);
		gameObject.setType(type);
		gameObject.setTimeSinceLastMove(last_updated);
		gameObject.setOpponentId(opponentId);
		gameObject.setOpenCardParents(openCardParents);
		gameObject.setDateCreated(dateCreated);

		if(image >= 0)
			gameObject.setImageId(image);
	}

	public void addOpenCardToScreen(int openCard){
		addNewImageToScreen (getDrawableIdFromCardId(openCard), (FrameLayout) findViewById (R.id.image_source_frame_main), openCard, 0);
		setAllButtonUnvisible();
	}

	public boolean isLastCardInInitPhase(ArrayList<GameGUI> gameGuiList){
		int numZeros = 0;
		for(int index=0; index < gameGuiList.size(); index++){
			if(gameGuiList.get(index).getCurrentCardId() == 0){
				numZeros++;
			}
		}

		if(numZeros == 1)
			return true;
		else
			return false;
	}

	public void setState(int s){
		this.previousState = this.STATE;
		this.STATE = s;
	}

	public int getState(){
		return this.STATE;
	}

	public void addNewImageToScreen (int resourceId, FrameLayout imageHolder, int cardId, int fromGridId){
		if(imageHolder == null)
			imageHolder = (FrameLayout) findViewById (R.id.image_source_frame_main);

		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams (LayoutParams.FILL_PARENT, 
				LayoutParams.FILL_PARENT, 
				Gravity.CENTER);

		ImageCell newView = new ImageCell (this, resourceId);
		newView.setImageResource (resourceId);

		imageHolder.setVisibility(View.VISIBLE);
		imageHolder.addView (newView, lp);
		imageHolder.setBackgroundResource(R.color.transparent);

		newView.mEmpty = false;
		newView.mCellNumber = -1;
		newView.setCardId(cardId);
		newView.setFromGridId(fromGridId);
		newView.setBackgroundResource(R.color.transparent);
		newView.mGrid = (GridView) findViewById(R.id.gridForCardsOnTable);

		// Have this activity listen to touch and click events for the view.
		newView.setOnClickListener(this);
		newView.setOnLongClickListener(this);
		newView.setOnTouchListener (this);

		stopPolling();
	}

	public void changeCardDeckButtonForNextPhase(){
		Button addNewCard = (Button) findViewById(R.id.buttonNextCard);		
		addNewCard.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.cards_finish_start_up));
	}

	public void setPlayButtonVisible(){
		Button addNewCard = (Button) findViewById(R.id.buttonNextCard);		
		addNewCard.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.cards_play_your_move));

		addNewCard.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//				specialAlert("Warning", "You loose the game if your journey's isn't correctly connected. Are you sure you want to continue?", context);
				playMove(null);
			}
		});

		addNewCard.setEnabled(true);
		dynamicAddEndButtonToView();
	}

	public void setCardButtonEnabled(){
		Button addNewCard = (Button) findViewById(R.id.buttonNextCard);
		addNewCard.setEnabled(true);
	}

	public void setGetNextCardButtonVisible(){
		Button addNewCard = (Button) findViewById(R.id.buttonNextCard);
		//		Button playButton = (Button) findViewById(R.id.buttonPlay);
		addNewCard.setEnabled(true);
		addNewCard.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				getNextCardInDeck();

			}
		});
		//		playButton.setEnabled(false);
	}

	public void setAllButtonUnvisible(){
		Button addNewCard = (Button) findViewById(R.id.buttonNextCard);
		if(addNewCard != null){
			//		Button playButton = (Button) findViewById(R.id.buttonPlay);
			addNewCard.setEnabled(false);
			//		playButton.setEnabled(false);
		}

	}

	public void setImageSourceFrameGone(){
		FrameLayout frame = (FrameLayout) findViewById(R.id.image_source_frame_main);
		frame.setVisibility(View.GONE);
	}

	public void setImageSourceFrameVisible(){
		FrameLayout frame = (FrameLayout) findViewById(R.id.image_source_frame_main);
		frame.setVisibility(View.VISIBLE);
	}

	public boolean isMyTurn(long playersTurn){
		int userId = Login.getUserId(loginSettings);

		if(userId == playersTurn)
			return true;
		else
			return false;
	}

	private void specialAlert(String title, String message, Context context){

		new AlertDialog.Builder(this)
		.setTitle(title)
		.setMessage(message)
		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				postFinishedGame();
			}
		}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Do nothing.
			}
		}).show();
	}

	private void postFinishedGame(){
		String finalJsonResponse = getStringResponse().toString();

		HttpPost httpPost = null;
		StringEntity se = null;

		try {
			int gameId = extras.getInt(SELECTED_GAME_ID);
			String finishUrl = "http://restfulserver.herokuapp.com/finish/" + Integer.toString(gameId);

			httpPost = new HttpPost(new URI(finishUrl));

			se = new StringEntity(finalJsonResponse.toString());
		}
		catch (URISyntaxException e1) { e1.printStackTrace(); }
		catch (UnsupportedEncodingException e) { e.printStackTrace(); }

		se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
		httpPost.setEntity(se);

		AsynchronousHttpClient a = new AsynchronousHttpClient();
		a.sendRequest(httpPost, responseListener, loginSettings);

		progDialog = new 
				ProgressDialogClass(this,
						"Evaluating your route", 
						"Please wait a moment..",
						15000);
		progDialog.run();
	}

	private void gameIsFinished(String msg, String action, int type, String opponent, int player1, int player2, int wins_player1, int wins_player2, int score){
		Intent finishIntent = new Intent();
		finishIntent.putExtra("cards", msg);
		finishIntent.putExtra("action", action);
		finishIntent.putExtra("type", type);
		finishIntent.putExtra("opponent", opponent);
		finishIntent.putExtra("opponentId", extras.getInt("opponentId"));
		finishIntent.putExtra("wins_player1", wins_player1);
		finishIntent.putExtra("wins_player2", wins_player2);
		finishIntent.putExtra("player1", player1);
		finishIntent.putExtra("player2", player2);
		finishIntent.putExtra("score", score);
		finishIntent.putExtra("YOUR_CARDS", gameObject.getYourCards().toString());
		finishIntent.putExtra("last_updated", "0");
		finishIntent.putExtra("openCards", gameObject.getOpenCards().toString());
		finishIntent.putExtra(GameActivity.SELECTED_GAME_ID, gameId);

		finishIntent.setClass(this, GameFinishActivity.class);
		startActivity(finishIntent);

		finish();
	}

	public void addCardsOnTable(){
		GridView openCardsGridView = (GridView) findViewById(R.id.gridForCardsOnTable);
		openCardsGridView.setVisibility(View.VISIBLE);
		openCardsGridView.setNumColumns(3);

		LinearLayout linearLayoutCardSlots = (LinearLayout) findViewById(R.id.linearLayoutCardSlots);
		linearLayoutCardSlots.setVisibility(View.VISIBLE);
	}

	public void addCardsOnTableAndCardStockButton(){
		addCardsOnTable();

		// Remove frame and card stock button that is overlayed over the webview
		FrameLayout frameLayout = (FrameLayout) findViewById(R.id.image_framelayout);

		if(findViewById(R.id.image_source_frame_main) != null)
			frameLayout.removeView(findViewById(R.id.image_source_frame_main));

		if(findViewById(R.id.buttonNextCard)!= null)
			frameLayout.removeView(findViewById(R.id.buttonNextCard));

		// Create newcard and card deck buttons
		LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View viewButton = vi.inflate(R.layout.dynamic_newcard, null);
		vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View viewDeck = vi.inflate(R.layout.dynamic_card_deck, null);

		// Change on click for press on deck image
		viewDeck.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				newCard(null);						
			}
		});

		RelativeLayout rel = (RelativeLayout) findViewById(R.id.linearLayoutForDynamicAdding);

		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				50, RelativeLayout.LayoutParams.MATCH_PARENT);
		RelativeLayout.LayoutParams lpDeck = new RelativeLayout.LayoutParams(
				50, RelativeLayout.LayoutParams.MATCH_PARENT);

		Resources r = this.getResources();
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 55, r.getDisplayMetrics());

		//		lpDeck.addRule(RelativeLayout.RIGHT_OF, R.id.gridForCardsOnTable);
		lpDeck.width = (int)px;

		px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 65, r.getDisplayMetrics());
		lp.width = (int)px;
		lp.addRule(RelativeLayout.RIGHT_OF, viewDeck.getId());

		rel.addView(viewButton, lp);		// may add layoutparams width, height
		rel.addView(viewDeck,  lpDeck);
	}

	public void dynamicAddEndButtonToView(){
		if(findViewById(R.id.buttonPlay) == null){
			RelativeLayout rel = (RelativeLayout) findViewById(R.id.linearLayoutForDynamicAdding);
			rel.removeView(findViewById(R.id.image_source_frame_main));

			LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View viewEndButton = vi.inflate(R.layout.dynamic_endgamebutton, null);

			viewEndButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					specialAlert("Notice", "Are you sure that all of your 10 journeys are correctly connected? Continue?", context);
				}
			});

			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

			Resources r = this.getResources();

			float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 55, r.getDisplayMetrics());
			lp.width = (int)px;
			lp.addRule(RelativeLayout.RIGHT_OF, R.id.buttonNextCard);

			rel.addView(viewEndButton, lp);
		}
	}

	public void setViewForOpponentsTurn(boolean fromAllGames){
		RelativeLayout rel = (RelativeLayout) findViewById(R.id.linearLayoutForDynamicAdding);

		if(findViewById(R.id.buttonPlay) != null)
			rel.removeView(findViewById(R.id.buttonPlay));

		if(findViewById(R.id.buttonNextCard) != null)
			rel.removeView(findViewById(R.id.buttonNextCard));

		if(findViewById(R.id.buttonPlay) != null)
			rel.removeView(findViewById(R.id.buttonPlay));

		if(findViewById(R.id.image_source_frame_main) != null)
			rel.removeView(findViewById(R.id.image_source_frame_main));

		addCardsOnTable();

		FrameLayout frameLayout = (FrameLayout) findViewById(R.id.image_framelayout);

		if(findViewById(R.id.image_source_frame_main) != null)
			frameLayout.removeView(findViewById(R.id.image_source_frame_main));

		if(findViewById(R.id.buttonNextCard) != null){
			frameLayout.removeView(findViewById(R.id.buttonNextCard));
		}

		if(this.getState() == GameActivity.OPPONENTS_TURN && fromAllGames == false && this.getState() != previousState && polling == false){
			int num = (int) (Math.random() * 5);
			if(num == 0)
				revmob.showFullscreen(this);
		}
	}


	public void setGotNewMessage(){
		ImageView chatIcon = (ImageView) findViewById(R.id.chat);
		chatIcon.setImageResource(R.drawable.chat_icon_main_red);
	}

	public void removeSettingsAndChatIcon(){
		FrameLayout frameLayout = (FrameLayout) findViewById(R.id.image_framelayout);
		//		frameLayout.removeView(findViewById(R.id.settingsImage));
		frameLayout.removeView(findViewById(R.id.chat));
	}

	public void addActionBar(){
		RelativeLayout rel = (RelativeLayout) findViewById(R.id.relativeMainMap);

		LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View viewActionbar = vi.inflate(R.layout.dynamic_actionbar, null);
		ActionBar actionBar = (ActionBar) viewActionbar;
		actionBar.setTitle("Cards before game ended");

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
		rel.addView(actionBar);
	}

	/**
	 * Handle a click on a view.
	 *
	 */    

	public void onClick(View v) 
	{
		if (mLongClickStartsDrag) {
			// Tell the user that it takes a long click to start dragging.
			toast ("Press and hold to drag an image.");
		}
	}

	/**
	 * Handle a click of an item in the grid of cells.
	 * 
	 */

	public void onItemClick(AdapterView<?> parent, View v, int position, long id) 
	{
		ImageCell i = (ImageCell) v;
	}


	/* (non-Javadoc)
	 * @see android.view.View.OnLongClickListener#onLongClick(android.view.View)
	 */

	@Override
	public boolean onLongClick(View v) {
		return false;
	}

	/**
	 * This is the starting point for a drag operation if mLongClickStartsDrag is false.
	 * It looks for the down event that gets generated when a user touches the screen.
	 * Only that initiates the drag-drop sequence.
	 *
	 */    

	public boolean onTouch (View v, MotionEvent ev) 
	{
		boolean handledHere = false;

		final int action = ev.getAction();

		// In the situation where a long click is not needed to initiate a drag, simply start on the down event.
		if (action == MotionEvent.ACTION_DOWN) {
			handledHere = startDrag (v);
		}

		return handledHere;
	}   

	/**
	 * Start dragging a view.
	 *
	 */    

	public boolean startDrag (View v)
	{
		DragSource dragSource = (DragSource) v;

		// We are starting a drag. Let the DragController handle it.
		mDragController.startDrag (v, dragSource, dragSource, DragController.DRAG_ACTION_MOVE);

		return true;
	}

	/**
	 * Show a string on the screen via Toast.
	 * 
	 * @param msg String
	 * @return void
	 */

	public void toast (String msg)
	{
		Toast.makeText (getApplicationContext(), msg, Toast.LENGTH_SHORT).show ();
	}

	/**
	 * Send a message to the debug log. Also display it using Toast if Debugging is true.
	 */

	public void trace (String msg) 
	{
		if (!Debugging) return;
		toast (msg);
	}


	@Override
	public void publishItem(GameGUI gameGui) {
		addNewImageToCell(gameGui.getResourceId(), gameGui.getIndex(), gameGui.getCurrentCardId());
	}


	@Override
	public void didFailWithError(String errorMessage) {
		// TODO Auto-generated method stub

	}

	@Override
	public void didFinishProsess(String message) {
		// TODO Auto-generated method stub

	}
	//public void showSettings(View v){
	//final ResponseListener giveUpListener = new ResponseListener() {
	//	@Override
	//	public void onResponseReceived(HttpResponse response, String message) {
	//		Log.d("Response", response.toString());
	//		confirmPlay(message);
	//	}	
	//};
	//
	//final ResponseListener startGameListener = new ResponseListener() {
	//	@Override
	//	public void onResponseReceived(HttpResponse httpResponse, String message) {
	//		Log.d("Response", httpResponse.toString());
	//
	//		try {
	//			JSONObject response = new JSONObject(message);
	//
	//			if(response.has("error"))
	//				new Alert("Uups", response.getString("error"), context);
	//			else{
	//				toast("New game created");
	//			}
	//		}
	//		catch (JSONException e) { 
	//			e.printStackTrace();
	//			new Alert("Uups", "An error occured", context);
	//		}
	//	}
	//};
	//
	//AlertDialog.Builder builder = new AlertDialog.Builder(context);
	//builder.setTitle("Options")
	//.setItems(R.array.options_game, new DialogInterface.OnClickListener() {
	//	public void onClick(DialogInterface dialog, int which) {
	//		Log.i("which", "WHICH::" + Integer.toString(which));
	//		switch (which) {
	//		case 0:
	//			CommonFunctions.sendFriendRequest("username", gameObject.getOpponentsUsername().get(0), loginSettings, context);
	//			break;
	//		case 1:
	//			CommonFunctions.startGameFromUsername(context, gameObject.getOpponentsUsername().get(0), gameObject.getType(), null, loginSettings);
	//			break;
	//		case 2:
	//			CommonFunctions.giveUp(gameObject.getGameId(), giveUpListener, loginSettings);
	//			break;
	//		case 3:
	//			Intent intent = new Intent().setClass(context, FullRuleset.class);
	//			startActivity(intent);
	//		}
	//	}
	//});
	//builder.create();
	//builder.show();
	//}
}
