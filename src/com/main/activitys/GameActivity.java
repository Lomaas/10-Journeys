/**
 * 
 */
package com.main.activitys;

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
import com.main.R;
import com.main.drag.DragController;
import com.main.drag.DragLayer;
import com.main.drag.DragSource;
import com.main.drag.ImageCell;
import com.main.drag.ImageCellAdapter;
import com.main.helper.Alert;
import com.main.helper.CommonFunctions;
import com.main.helper.ProgressDialogClass;
import com.main.service.AsyncTaskDelegate;
import com.main.service.TimerService;
import com.main.activitys.domain.Game;
import com.main.activitys.domain.GameGUI;
import com.main.activitys.domain.Login;
import uit.nfc.AsynchronousHttpClient;
import uit.nfc.ResponseListener;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
	private Intent serviceintent;
	private ProgressDialogClass progDialog;

	//	private DbAdapter mDbHelper;
	private Bundle extras;
	private DragController mDragController;   // Object that handles a drag-drop sequence. It intersacts with DragSource and DropTarget objects.
	private DragLayer mDragLayer;             // The ViewGroup within which an object can be dragged.

	public int STATE = 1;							// default INIT
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

	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.game);
		context = this;
		serviceintent = new Intent("com.main.service.TimerService");
		Log.i("GameActivity onCreate", "oncreate");

		WebView webView = (WebView)findViewById(R.id.mainMap);
		webView.setBackgroundColor(getResources().getColor(R.color.grid_background));
		webView.setVerticalScrollBarEnabled(false);
		webView.setHorizontalScrollBarEnabled(false);
		webView.getSettings().setLoadWithOverviewMode(true);
		webView.getSettings().setUseWideViewPort(true);

		webView.loadUrl("file:///android_asset/test.html");

		//		mDbHelper = new DbAdapter(this);
		//		mDbHelper.open();

		extras = getIntent().getExtras();
		gameId = extras.getInt(SELECTED_GAME_ID);

		gameUrl = "http://restfulserver.herokuapp.com/game/" + Integer.toString(gameId);
		loginSettings = getSharedPreferences(Login.PREFS_NAME, 0);
		setState(extras.getInt("STATE"));

		responseListener = new ResponseListener() {
			@Override
			public void onResponseReceived(HttpResponse response, String message) {
				Log.d("Response", response.toString());
				progDialog.dissMissProgressDialog();
				confirmPlay(message);
			}
		};

		responseForNextCard = new ResponseListener() {
			@Override
			public void onResponseReceived(HttpResponse response, String message) {
				Log.d("Response", response.toString());
				progDialog.dissMissProgressDialog();
				evaluateResponseForNextCard(message);
			}
		};

		try {
			STATE = extras.getInt("STATE");
			last_updated = extras.getString("last_updated");

			if(STATE == GameActivity.YOUR_TURN){
				dynamicAddCardsOnTable();
			}

			if(STATE == GameActivity.INIT && extras.getInt("openCard") >= 0){
				Log.d("onCreate", "its init and openCard is open");
				toast("its init turn state");

				if(extras.getInt("openCard") > 0)
					addOpenCardToScreen(extras.getInt("openCard"));

				// Set cards on table grid unVisible
				GridView openCardsGridView = (GridView) findViewById(R.id.gridForCardsOnTable);
				openCardsGridView.setVisibility(View.GONE);
				//				Button button = (Button) findViewById(R.id.buttonPlay);
				//				button.setVisibility(View.GONE);
			}
			else if(STATE == GameActivity.OPPONENTS_TURN){
				setViewForOpponentsTurn();
			}
			else if(extras.getInt("openCard") > 0 && STATE == GameActivity.YOUR_TURN){
				toast("its your turn state");
				setAllButtonUnvisible();
				this.setState(GameActivity.YOUR_TURN_FROM_DECK);
				addOpenCardToScreen(extras.getInt("openCard"));
			}
			else if(extras.getInt("openCard") < 0){
				setAllButtonUnvisible();
			}

			TextView lastAction = (TextView) findViewById(R.id.lastAction);
			lastAction.setText(extras.getString("action"));

			JSONArray tmpYourCards = new JSONArray(extras.getString("YOUR_CARDS"));	
			JSONArray tmpOpenCards = new JSONArray(extras.getString("openCards"));
			ArrayList<GameGUI> gameGUIS = getGameGUI(tmpYourCards);
			ArrayList<GameGUI> openCards = getGameGUI(tmpOpenCards);

			ArrayList<GameGUI> openCardGui = new ArrayList<GameGUI>();
			openCardGui.add(openCards.get(0));
			openCardGui.add(openCards.get(1));
			openCardGui.add(openCards.get(2));

			setGraphicalInterface(gameGUIS, openCardGui);
			Log.i("playersTurn ONCREATE", Integer.toString(extras.getInt("playersTurn")));

			updateGameObject(gameId, extras.getString("action"), extras.getInt("openCard"), tmpOpenCards, extras.getString("opponent"), 
					STATE, last_updated, tmpYourCards, extras.getInt("playersTurn"), extras.getInt("type"), extras.getInt("opponentId"));
		}
		catch(JSONException e){
			e.printStackTrace();
			toast("ups.. something went wrong. Try reselecting the game");
		}
	}

	public void onResume(){
		super.onResume();
		//		mDbHelper = new DbAdapter(this);
		//		mDbHelper.open();	
		doPolling();
	}

	public void onPause(){
		super.onPause();
		//		mDbHelper.close();
		stopPolling();
	}

	@Override
	public void onBackPressed() {
		Log.d("backpressed", "onBackPressed Called");

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
			Log.i("platersTurn", Integer.toString(gameObject.getPlayersTurn()));
			Log.i("state", Integer.toString(gameObject.getState()));
	
			resultCode = Activity.RESULT_OK;
		}
		catch(JSONException e){ e.printStackTrace(); }

		returnIntent.putExtra("gameInfo", obj.toString());
		Log.d("gameinfo", obj.toString());
		
		if (getParent() == null) {
			setResult(resultCode, returnIntent);
		} else {
			getParent().setResult(resultCode, returnIntent);
		}
		finish();
	}   

	public void doPolling(){
		Log.d("OnResume", gameUrl);

		if(this.getState() == OPPONENTS_TURN && polling == false){
			polling = true;
			serviceintent.putExtra(TimerService.URL, "http://restfulserver.herokuapp.com/game/" + extras.getInt(SELECTED_GAME_ID));
			serviceintent.putExtra("broadcast", TimerService.BROADCAST_ACTION_GAME);
			startService(serviceintent);
			registerReceiver(broadcastReceiver, new IntentFilter(TimerService.BROADCAST_ACTION_GAME));
		}
	}

	public void stopPolling(){
		if(polling){
			Log.d("stopPolling gameActivity", "stopping");
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

	public void showSettings(View v){
		final ResponseListener giveUpListener = new ResponseListener() {
			@Override
			public void onResponseReceived(HttpResponse response, String message) {
				Log.d("Response", response.toString());
				confirmPlay(message);
			}	
		};

		final ResponseListener startGameListener = new ResponseListener() {
			@Override
			public void onResponseReceived(HttpResponse httpResponse, String message) {
				Log.d("Response", httpResponse.toString());

				try {
					JSONObject response = new JSONObject(message);

					if(response.has("error"))
						new Alert("Uups", response.getString("error"), context);
					else{
						toast("New game created");
					}
				}
				catch (JSONException e) { 
					e.printStackTrace();
					new Alert("Uups", "An error occured", context);
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Options")
		.setItems(R.array.options_game, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Log.i("which", "WHICH::" + Integer.toString(which));
				switch (which) {
				case 0:
					break;
				case 1:
					CommonFunctions.sendFriendRequest("username", gameObject.getOpponentsUsername().get(0), loginSettings, context);
					break;
				case 2:
					CommonFunctions.startGameFromUsername(gameObject.getOpponentsUsername().get(0), gameObject.getType(), startGameListener, loginSettings);
					break;
				case 3:
					CommonFunctions.giveUp(gameObject.getGameId(), giveUpListener, loginSettings);
					break;
				}
			}
		});
		builder.create();
		builder.show();
	}

	protected Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Options")
		.setItems(R.array.options_game, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				toast("test");
			}
		});
		return builder.create();
	}


	public int getDrawableIdFromCardId(int cardId){
		//		Cursor cursor = mDbHelper.fetchImage(cardId);
		//		return cursor.getInt(0);
		return AllGamesActivity.imageArray[cardId];

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
		if (gridView == null) toast ("Unable to find GridView");
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
			Log.d("onDrop", "not null drawable");
			dropTarget.setImageDrawable (d);
		}
	}

	public void newCard(View v){
		Log.d("newCard", "adding new Card");
		if(this.getState() == GameActivity.INIT)
			playMove(null);
		else
			getNextCardInDeck();
	}

	public void getNextCardInDeck(){
		Log.d("getnextCardInDeck", "next card");
		HttpGet httpGet = null;

		try {
			Log.d("SERVICE", "done reque111st");
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
						"Getting next card in the card deck");
		progDialog.run();
	}

	public void evaluateResponseForNextCard(String response){
		if(this.getState() != GameActivity.INIT){
			this.setState(GameActivity.YOUR_TURN_FROM_DECK);		// sets new state!
			stopPolling();
		}
		try {
			JSONObject cards = new JSONObject(response);
			int cardId = cards.getInt("cardId");
			//			Cursor cursor = mDbHelper.fetchImage(cardId);
			Log.d("state", Integer.toString(this.getState()));
			setAllButtonUnvisible();
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
			Log.d("postUpdate", finalJsonResponse.toString());

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
						"Updating", 
						"Playing your move, please wait a moment...");
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
				//				Log.d("currentCardId", Integer.toString(gameGui.getCurrentCardId()));
				//				Log.d("deletedCardId", Integer.toString(gameGui.getDeletedCardId()));

				//				if(gameGui.isUpdated())
				//					Log.d("isUpdated", "is updated");
				//				else
				//					Log.d("Not updated!", "not updated");
				//				if(gameGui.isDraggable())
				//					Log.d("isDragable", "is draggable");
				//				else
				//					Log.d("Not dragable!", "not dragable");
				//				if(gameGui.isSpecialCase())
				//					Log.d("isSpecial", "is specialcase");
				//				else
				//					Log.d("Not special case!", "not special case");

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
							//							Log.i("setsCURRENT 0", "SETS CURRENT 0");
						}
						else if(gameGui.isDraggable() && cellTakenFrom != gameGui.getIndex()){
							tmpJobject.put("currentCardId", gameGui.getCurrentCardId());
							tmpJobject.put("deletedCardId", 0);
							//							Log.i("DRAGGABLE", "AND NOT INSERTED ON CELLTAKENFROM");
						}
						else {
							tmpJobject.put("currentCardId", gameGui.getCurrentCardId());
							tmpJobject.put("deletedCardId", gameGui.getDeletedCardId());
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
		Log.d("confirmPlay", msg);
		try {
			JSONObject jResponse= new JSONObject(msg);

			if(jResponse.getInt("finished") == 1){
				gameIsFinished(jResponse.getJSONArray("cards").toString(), jResponse.getString("action"), jResponse.getInt("type"), extras.getString("opponent"),
						jResponse.getInt("player1"), jResponse.getInt("player2"), jResponse.getInt("wins_player1"), jResponse.getInt("player2"));
				return;
			}

			JSONArray yourCards = jResponse.getJSONArray("yourCards");
			JSONArray openCardGui = jResponse.getJSONArray("openCards");

			int previousState = this.getState();
			this.setState(jResponse.getInt("state"));
			String action = null;

			if(this.getState() == GameActivity.YOUR_TURN && previousState == GameActivity.INIT){
				dynamicAddCardsOnTable();
			}

			if(jResponse.has("action"))
				action = jResponse.getString("action");

			/* Special case when we are waiting for opponent */
			if(jResponse.getInt("openCard") < 0){
				setAllButtonUnvisible();
				action = getResources().getString(R.string.action_waiting);
				this.setState(GameActivity.OPPONENTS_TURN);
			}
			else if(!isMyTurn(jResponse.getInt("playersTurn")) && this.getState() != GameActivity.INIT){
				setAllButtonUnvisible();
				Log.d("confirmPlay", "sets new Play");
				this.setState(GameActivity.OPPONENTS_TURN);
			}
			else if(jResponse.getInt("openCard") > 0 && STATE == GameActivity.YOUR_TURN){
				setAllButtonUnvisible();
				this.setState(GameActivity.YOUR_TURN_FROM_DECK);
				addOpenCardToScreen(jResponse.getInt("openCard"));
			}
			else if(jResponse.getInt("openCard") > 0){
				addOpenCardToScreen(jResponse.getInt("openCard"));
			}
			else {
				setGetNextCardButtonVisible();
			}

			TextView lastAction = (TextView) findViewById(R.id.lastAction);

			if(action != null)
				lastAction.setText(action);

			updateGameGuiListInAdapter((GridView) findViewById(R.id.image_grid_view), getGameGUI(yourCards));
			updateGameGuiListInAdapter((GridView) findViewById(R.id.gridForCardsOnTable), getGameGUI(openCardGui));

			updateGameObject(jResponse.getInt("GID"), jResponse.getString("action"), jResponse.getInt("openCard"), openCardGui, extras.getString("opponent"), 
					jResponse.getInt("state"), jResponse.get("last_updated").toString(), yourCards, jResponse.getInt("playersTurn"), jResponse.getInt("type"), jResponse.getInt("opponentId"));

			cellTakenFrom = -1;

			if(this.getState() != GameActivity.OPPONENTS_TURN)
				stopPolling();
			else
				doPolling();
		}
		catch(JSONException e){
			e.printStackTrace();
			toast("Something went wrong while getting game info from server");
		}
	}

	public void updateGameObject(int gid, String action, int openCard, JSONArray openCardGui, String username, int state, String last_updated,
			JSONArray yourCards, int playersTurn, int type, int opponentId){
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
	}

	public void addOpenCardToScreen(int openCard){
		//		Log.i("addOppenCardToScreen", Integer.toString(openCard));
		//		Cursor cursor = mDbHelper.fetchImage(openCard);
		addNewImageToScreen (getDrawableIdFromCardId(openCard), (FrameLayout) findViewById (R.id.image_source_frame_main), openCard, 0);
		setAllButtonUnvisible();
	}

	public void setState(int s){
		Log.d("STATE", "SETS NEW STATE-> " + Integer.toString(s));
		this.STATE = s;
	}

	public int getState(){
		return this.STATE;
	}

	public void addNewImageToScreen (int resourceId, FrameLayout imageHolder, int cardId, int fromGridId){
		Log.d("ADDNEWIMAGETO SCREEN", "START OF ADD NEW IMAGE TO SCREEN!!!!");

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

		Log.d("ADDNEWIMAGETO SCREEN", "kommer DU HIIIIIIIIT");
		Log.d("resourceId", Integer.toString(resourceId));	

		// Have this activity listen to touch and click events for the view.
		newView.setOnClickListener(this);
		newView.setOnLongClickListener(this);
		newView.setOnTouchListener (this);

		stopPolling();
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
		Log.d("finish!", "user thinks his finish");

		String finalJsonResponse = getStringResponse().toString();

		HttpPost httpPost = null;
		StringEntity se = null;

		try {
			int gameId = extras.getInt(SELECTED_GAME_ID);
			String finishUrl = "http://restfulserver.herokuapp.com/finish/" + Integer.toString(gameId);

			httpPost = new HttpPost(new URI(finishUrl));
			Log.d("postUpdate", finalJsonResponse.toString());

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
						"Please wait a moment..");
		progDialog.run();
	}

	private void gameIsFinished(String msg, String action, int type, String opponent, int player1, int player2, int wins_player1, int wins_player2){
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

		finishIntent.setClass(this, GameFinishActivity.class);
		startActivity(finishIntent);
		finish();
	}

	public void dynamicAddCardsOnTable(){
		GridView openCardsGridView = (GridView) findViewById(R.id.gridForCardsOnTable);
		openCardsGridView.setVisibility(View.VISIBLE);
		openCardsGridView.setNumColumns(3);

		FrameLayout frameLayout = (FrameLayout) findViewById(R.id.image_framelayout);
		frameLayout.removeView(findViewById(R.id.image_source_frame_main));
		frameLayout.removeView(findViewById(R.id.buttonNextCard));

		LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View viewButton = vi.inflate(R.layout.dynamic_newcard, null);
		vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View viewDeck = vi.inflate(R.layout.dynamic_card_deck, null);
		RelativeLayout rel = (RelativeLayout) findViewById(R.id.linearLayoutForDynamicAdding);

		viewDeck.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				newCard(null);						
			}
		});

		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				50, RelativeLayout.LayoutParams.MATCH_PARENT);
		RelativeLayout.LayoutParams lpDeck = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

		Resources r = this.getResources();
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 55, r.getDisplayMetrics());

		lpDeck.addRule(RelativeLayout.RIGHT_OF, R.id.gridForCardsOnTable);
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
					specialAlert("Notice", "You loose the game if your 10 journeys isn't correctly connected. Are you sure you want to continue?", context);
				}
			});

			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

			Resources r = this.getResources();

			float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 65, r.getDisplayMetrics());
			lp.width = (int)px;
			lp.addRule(RelativeLayout.RIGHT_OF, R.id.buttonNextCard);

			rel.addView(viewEndButton, lp);
		}
	}
	
	public void setViewForOpponentsTurn(){
		RelativeLayout rel = (RelativeLayout) findViewById(R.id.linearLayoutForDynamicAdding);
		if(findViewById(R.id.buttonPlay) != null){
			rel.removeView(findViewById(R.id.buttonPlay));
		}
		
		GridView openCardsGridView = (GridView) findViewById(R.id.gridForCardsOnTable);
		openCardsGridView.setVisibility(View.VISIBLE);
		openCardsGridView.setNumColumns(3);
		FrameLayout frameLayout = (FrameLayout) findViewById(R.id.image_framelayout);
	
		if(findViewById(R.id.image_source_frame_main) != null)
			frameLayout.removeView(findViewById(R.id.image_source_frame_main));
		
		if(findViewById(R.id.buttonNextCard) != null){
			frameLayout.removeView(findViewById(R.id.buttonNextCard));
		}
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
		Log.d ("onItemClick in view", Integer.toString(i.mCellNumber));
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
		Log.d ("DragActivity", msg);
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

}
