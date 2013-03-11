
/**
 * 
 */
package com.main.activitys;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.main.R;
import com.main.drag2.DragController;
import com.main.drag2.DragLayer;
import com.main.drag2.DragSource;
import com.main.drag2.ImageCell;
import com.main.drag2.ImageCellAdapter;
import com.main.helper.Alert;
import com.main.helper.CommonFunctions;
import com.main.helper.Constants;
import com.main.activitys.domain.Extrainfo;
import com.main.activitys.domain.Game;
import com.main.activitys.domain.GameGUI;
import com.main.activitys.domain.Login;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

// e9f0c2

public class TryOutGame extends Activity implements View.OnLongClickListener, View.OnClickListener, View.OnTouchListener {
	private Game gameObject = new Game();
	private Context context;
	public int gameId = 1;
	private int userId;
	public int cellTakenFrom = -1;		// used to see which card the player took from OpenCards
	public JSONArray cardDeck = new JSONArray();
	public int cardDeckIndex = 1;
	public SharedPreferences loginSettings;
	public SharedPreferences extraInfo;
	
	TextView extraInfoText = null;

	private Intent serviceintent;

	IntentFilter gcmFilter;
	private BroadcastReceiver gcmReceiver = null;

	private DragController mDragController;   // Object that handles a drag-drop sequence. It intersacts with DragSource and DropTarget objects.
	private DragLayer mDragLayer;             // The ViewGroup within which an object can be dragged.

	public int STATE = 1;							// default INIT
	private int previousState = 1;
	public static final int INIT = 1;
	public static final int YOUR_TURN = 2;
	public static final int YOUR_TURN_FROM_DECK = 3;
	public static final int YOUR_TURN_INSERT_TO_OPEN_CARDS = 4;
	public static final int YOUR_TURN_SWAP = 5;
	public static final int OPPONENTS_TURN = 6;
	public static final int GAME_OVER = 7;
	
	private int timePlayed = 0;

	private boolean mLongClickStartsDrag = false;   // If true, it takes a long click to start the drag operation.
	public static final boolean Debugging = true;   // Use this to see extra toast messages.
	private Handler handlerOpponentRunning = new Handler();

	private Runnable runnable2 = new Runnable() {
		@Override
		public void run() {

			ImageCellAdapter yourCardAdapter= (ImageCellAdapter) ((GridView) findViewById(R.id.image_grid_view)).getAdapter();
			ImageCellAdapter cardsOnTableAdapter= (ImageCellAdapter) ((GridView) findViewById(R.id.gridForCardsOnTable)).getAdapter();

			Log.i("card", Integer.toString(yourCardAdapter.getGameGUIS().get(0).getCurrentCardId()));

			ArrayList<GameGUI> gameGUIS = yourCardAdapter.getGameGUIS();
			JSONArray yourCardArray = fixArrays(gameGUIS);

			gameGUIS = cardsOnTableAdapter.getGameGUIS();
			JSONArray openCardArray = fixArrays(gameGUIS);
			
			timePlayed ++;
			String action = null;
			try {
				
				if(timePlayed == 1){
					openCardArray.put(0, 40);
					action ="Computer took a card from deck. Placed Moldova in pile 1, Your turn!";
				}
				if(timePlayed == 2){
					openCardArray.put(2, 41);
					action = "Computer took a card from deck. Placed blue plane in pile 3. Your turn!";
				}
				if(timePlayed == 3){
					openCardArray.put(1, 47);
					action = "Computer took a card from deck. Placed a Mediterranean Sea boat in pile 2. Your turn!";
				}
				if(timePlayed == 4){
					openCardArray.put(0, 45);
					action = "Computer took a card from deck. Placed a Yellow plane in pile 1. Your turn!";
				}
				if(timePlayed == 5){
					specialAlert("Tutorial finish", "The game goes on until a player find a valid travel route", context);
					return;
				}
				
			}
			catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			confirmPlay(yourCardArray,
					openCardArray, 
					fixOpenCardParent(),
					0, 
					2, 
					action,
					userId);
			
	}
	};
	
	private Handler handler = new Handler();

	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
			try {
				Thread.sleep(500);
			}
			catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if(STATE == INIT)
				setNormalViewFirstTime();

			else if(cardDeckIndex == 12){
				setNormalViewFirstTime();
			}
			else if(STATE == OPPONENTS_TURN){

				ImageCellAdapter yourCardAdapter= (ImageCellAdapter) ((GridView) findViewById(R.id.image_grid_view)).getAdapter();
				ImageCellAdapter cardsOnTableAdapter= (ImageCellAdapter) ((GridView) findViewById(R.id.gridForCardsOnTable)).getAdapter();

				Log.i("card", Integer.toString(yourCardAdapter.getGameGUIS().get(0).getCurrentCardId()));

				ArrayList<GameGUI> gameGUIS = yourCardAdapter.getGameGUIS();
				JSONArray yourCardArray = fixArrays(gameGUIS);

				gameGUIS = cardsOnTableAdapter.getGameGUIS();
				JSONArray openCardArray = fixArrays(gameGUIS);
				
				confirmPlay(yourCardArray, 
						openCardArray, 
						fixOpenCardParent(),
						0,
						2,
						"Computer: LALALALLAL",
						userId
						);
			}
		}
	};


	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.tryoutgame);
		removeSettingsAndChatIcon();

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

		loginSettings = getSharedPreferences(Login.PREFS_NAME, 0);
		extraInfo = getSharedPreferences(Extrainfo.PREFS_NAME, 0);
		
		extraInfoText = (TextView)findViewById(R.id.extraInformationText);

		userId = Login.getUserId(loginSettings);

		gcmReceiver = CommonFunctions.createBroadCastReceiver(context, loginSettings, CommonFunctions.FROM_STANDARD_ACTIVITY);
		gcmFilter = new IntentFilter();
		gcmFilter.addAction("GCM_RECEIVED_ACTION");

		gameObject.setOpenCardParents(fixOpenCardParent());

		setState(GameActivity.INIT);

		try {
			STATE = GameActivity.INIT;
			String action = "Start with placing your 10 start up cards. Press Next Card to get your first card";
			TextView lastAction = (TextView) findViewById(R.id.lastAction);
			lastAction.setText(action);

			// Set cards on table grid unVisible
			GridView openCardsGridView = (GridView) findViewById(R.id.gridForCardsOnTable);
			openCardsGridView.setVisibility(View.GONE);

			for(int i = 0; i < Constants.imageArray.length; i++){
				cardDeck.put(i);
			}

			JSONArray tmpYourCards = new JSONArray();

			for(int i = 0; i < 10; i++)
				tmpYourCards.put(i, 0);

			JSONArray tmpOpenCards = new JSONArray();

			for(int i = 0; i < 3; i++)
				tmpOpenCards.put(i, 0);

			JSONArray openCardParents = new JSONArray();

			for(int i = 0; i < 3; i++)
				openCardParents.put(i, 0);

			ArrayList<GameGUI> gameGUIS = getGameGUI(tmpYourCards);
			ArrayList<GameGUI> openCards = getGameGUI(tmpOpenCards);

			ArrayList<GameGUI> openCardGui = new ArrayList<GameGUI>();

			openCardGui.add(openCards.get(0));
			openCardGui.add(openCards.get(1));
			openCardGui.add(openCards.get(2));

			setGraphicalInterface(gameGUIS, openCardGui);
			new Alert("Notice", "Follow the instructions at the top of the screen for help", this);

		}
		catch(JSONException e){
			e.printStackTrace();
			toast("ups.. something went wrong. Try reselecting the game");
		}
	}

	public void onResume(){
		super.onResume();
		registerReceiver(gcmReceiver, gcmFilter);
	}

	public void onPause(){
		super.onPause();
		unregisterReceiver(gcmReceiver);
	}


	// Initiating Menu XML file (menu.xml)
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.layout.testgamemenu, menu);
		return true;
	}

	/**
	 * Event Handling for Individual menu item selected
	 * Identify single menu item by it's id
	 * */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.menu_rules:
			Intent intent = new Intent().setClass(context, FullRuleset.class);
			startActivity(intent);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}    


	public int getCardIdFromOpenCardParent(int cellNumber){
		JSONArray array = gameObject.getOpenCardParents();
		Log.d("getCardIdFromOpenCardParent", array.toString());
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
		Log.d("hasOpenCardParent", array.toString());
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
				yourCard.setIndex(index);
				yourCard.setResourceId(getDrawableIdFromCardId(cardId));
				yourCard.setCurrentCardId(cardId);

				if(this.STATE == GameActivity.INIT)
					yourCard.setDraggable(false);

				gameGUIS.add(yourCard);
			}
			else {
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

		GridView gridView = (GridView) findViewById(R.id.image_grid_view);	
		ImageCellAdapter adapter = (ImageCellAdapter) gridView.getAdapter();

		int numVisibleChildren = gridView.getChildCount();

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
		if(STATE == INIT)
			getNextCardInDeck();

		if(STATE == YOUR_TURN)
			getNextCardInDeck();

	}

	public void setNormalViewFirstTime(){
		ImageCellAdapter yourCardAdapter= (ImageCellAdapter) ((GridView) findViewById(R.id.image_grid_view)).getAdapter();

		ArrayList<GameGUI> gameGUIS = yourCardAdapter.getGameGUIS();
		JSONArray yourCardArray = fixArrays(gameGUIS);

		JSONArray openCardArray = new JSONArray();
		try {
			openCardArray.put(cardDeck.getInt(cardDeckIndex));
			openCardArray.put(cardDeck.getInt(cardDeckIndex+1));
			openCardArray.put(cardDeck.getInt(cardDeckIndex+2));
		}
		catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		cardDeckIndex += 3;

		confirmPlay(yourCardArray,
				openCardArray, 
				fixOpenCardParent(),
				0, 
				YOUR_TURN, 
				"The game is now on. Pick a card from one of the three piles or take one from the top of the deck",
				userId
				);

	}

	public JSONArray fixOpenCardParent(){
		JSONArray openCardParents = new JSONArray();

		openCardParents.put(0);
		openCardParents.put(0);
		openCardParents.put(0);

		return openCardParents;
	}

	public void getNextCardInDeck(){
		int cardId = 0;
		JSONArray openCardParents = new JSONArray();

		try {
			openCardParents.put(0);
			openCardParents.put(0);
			openCardParents.put(0);

			cardId = cardDeck.getInt(cardDeckIndex);

		}
		catch (JSONException e) { e.printStackTrace(); }
		cardDeckIndex ++;

		if(cardDeckIndex == 2){
			addOpenCardToScreen(cardId);
			TextView view = (TextView)findViewById(R.id.lastAction);
			view.setText("Drag the card to a position in your route you find convenient. Then press next card");
			view.setBackgroundResource(R.drawable.test);
			view.setTextColor(getResources().getColor(R.color.black));
			//extraInfoText.setVisibility(View.VISIBLE);
			return;
		}
		if(cardDeckIndex == 3){
			addOpenCardToScreen(cardId);
			TextView view = (TextView)findViewById(R.id.lastAction);
			view.setText("Continue until all 10 cards have been placed. A card that is already placed can't be replaced until the next phase of the game");
			view.setBackgroundColor(getResources().getColor(R.color.darktrans));
			view.setTextColor(getResources().getColor(R.color.white));

			return;
		}
		if(cardDeckIndex == 4){
			addOpenCardToScreen(cardId);
			TextView view = (TextView)findViewById(R.id.lastAction);
			view.setText("Tip: A good start is crucial for being competitive in the next phase. Be tactical in your placement of your cards");
			view.setBackgroundResource(R.drawable.test);
			view.setTextColor(getResources().getColor(R.color.black));

			return;
		}
		if(cardDeckIndex == 5){
			addOpenCardToScreen(cardId);
			TextView view = (TextView)findViewById(R.id.lastAction);
			view.setText("Notice there is two railroads on the map; one in northern Africa and one in central Europe");
			view.setBackgroundColor(getResources().getColor(R.color.darktrans));
			view.setTextColor(getResources().getColor(R.color.white));

			return;
		}
		else{
			TextView view = (TextView)findViewById(R.id.lastAction);
			view.setBackgroundColor(getResources().getColor(R.color.darktrans));
			view.setTextColor(getResources().getColor(R.color.white));
		}

		ImageCellAdapter yourCardAdapter= (ImageCellAdapter) ((GridView) findViewById(R.id.image_grid_view)).getAdapter();
		ImageCellAdapter cardsOnTableAdapter= (ImageCellAdapter) ((GridView) findViewById(R.id.gridForCardsOnTable)).getAdapter();

		Log.i("card", Integer.toString(yourCardAdapter.getGameGUIS().get(0).getCurrentCardId()));

		ArrayList<GameGUI> gameGUIS = yourCardAdapter.getGameGUIS();
		JSONArray yourCardArray = fixArrays(gameGUIS);

		gameGUIS = cardsOnTableAdapter.getGameGUIS();
		JSONArray openCardArray = fixArrays(gameGUIS);

		if(cardDeckIndex == 12){
			// last card in start up
			confirmPlay(yourCardArray,
					openCardArray, 
					openCardParents,
					-1, 
					INIT, 
					"Test",
					userId
					);
			new Alert("Next phase", "Turn by turn, exchange one card in your route. The player who first links a valid route together, wins the game", this);
		}
		else if(cardDeckIndex > 12){
			// next phase
			evaluateResponseForNextCard(cardId);

		}
		else {
			// start up
			confirmPlay(yourCardArray,
					openCardArray, 
					openCardParents,
					cardId, 
					INIT, 
					"Continue until all 10 cards have been placed.",
					userId
					);
		}
	}

	public JSONArray fixArrays(ArrayList<GameGUI> gameGUIS){
		JSONArray yourCardArray = new JSONArray();

		for(GameGUI gameGui : gameGUIS) {
			yourCardArray.put(gameGui.getCurrentCardId());
		}
		return yourCardArray;
	}

	public void evaluateResponseForNextCard(int cardId){
		if(this.getState() != GameActivity.INIT){
			this.setState(GameActivity.YOUR_TURN_FROM_DECK);		// sets new state!
		}
		setAllButtonUnvisible();
		Log.i("evaluateResponseForNextCard", "are you here");
		addNewImageToScreen(getDrawableIdFromCardId(cardId), (FrameLayout) findViewById(R.id.image_source_frame_main), cardId, 0);
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
			finalJsonResponse.put("GID", 1);

			return finalJsonResponse;
		}
		catch (JSONException e) { e.printStackTrace(); }
		return null;
	}

	public void postUpdate(){		
		ImageCellAdapter yourCardAdapter= (ImageCellAdapter) ((GridView) findViewById(R.id.image_grid_view)).getAdapter();
		ImageCellAdapter cardsOnTableAdapter= (ImageCellAdapter) ((GridView) findViewById(R.id.gridForCardsOnTable)).getAdapter();

		Log.i("card", Integer.toString(yourCardAdapter.getGameGUIS().get(0).getCurrentCardId()));

		ArrayList<GameGUI> gameGUIS = yourCardAdapter.getGameGUIS();
		JSONArray yourCardArray = fixArrays(gameGUIS);

		gameGUIS = cardsOnTableAdapter.getGameGUIS();
		JSONArray openCardArray = fixArrays(gameGUIS);

		if(STATE == YOUR_TURN_INSERT_TO_OPEN_CARDS){
			setViewForOpponentsTurn(false);
			
			if(timePlayed == 5){
				Log.i("timePlayed", " is 5");
				specialAlert("Tutorial finish", "The game goes on like this until a player finds a valid route", context);
				return;
			}

			ProgressDialogClass progDialogClass = new ProgressDialogClass(this, 
					"Opponents turn", "Your opponent will now play his turn", 3000);
			progDialogClass.run();
			handlerOpponentRunning.postDelayed(runnable2, 2900);
		}
		else{
			confirmPlay(yourCardArray,
					openCardArray, 
					fixOpenCardParent(),
					0, 
					2, 
					"Place your 10 start up cards. Press 'Next Card' to get your next card",
					userId
					);
		}
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

	public void confirmPlay(JSONArray yourCards, JSONArray openCards, JSONArray openCardParent, int openCard, int state,
			String action, int playersTurn){
		Log.d("confirmPlay", "test");
		try {

			this.setState(state);

			/* Special case when we are waiting for opponent */
			if(openCard < 0){
				this.setState(GameActivity.OPPONENTS_TURN);
				setAllButtonUnvisible();
				setViewForOpponentsTurn(false);
				action = "Waiting for computer to finish start up";
			}
			// Not my turn
			else if(!isMyTurn(playersTurn) && this.getState() != GameActivity.INIT){
				this.setState(GameActivity.OPPONENTS_TURN);
				setViewForOpponentsTurn(false);
				Log.d("confirmPlay", "sets new Play");

				if(action.equals("The game is on"))
					action += ". Waiting for computer to make his/her move";
			}
			else if(openCard > 0 && STATE == GameActivity.YOUR_TURN){
				this.setState(GameActivity.YOUR_TURN_FROM_DECK);
				setAllButtonUnvisible();
				addOpenCardToScreen(openCard);
			}
			else if(openCard > 0){
				// This is in init phase
				addOpenCardToScreen(openCard);
			}
			else {
				// Its my turn
				addCardsOnTableAndCardStockButton();
			}

			TextView lastAction = (TextView) findViewById(R.id.lastAction);

			if(action != null)
				lastAction.setText(action);

			updateGameGuiListInAdapter((GridView) findViewById(R.id.image_grid_view), getGameGUI(yourCards));
			updateGameGuiListInAdapter((GridView) findViewById(R.id.gridForCardsOnTable), getGameGUI(openCards));

			cellTakenFrom = -1;

			if(this.getState() == GameActivity.OPPONENTS_TURN)
				setComputerPlaying();
		}
		catch(JSONException e){
			e.printStackTrace();
			toast("Ups, something went wrong - error code: 1");
		}
	}

	public void setComputerPlaying(){
		handler.postDelayed(runnable, 100);
	}

	public void addOpenCardToScreen(int openCard){
		addNewImageToScreen (getDrawableIdFromCardId(openCard), (FrameLayout) findViewById (R.id.image_source_frame_main), openCard, 0);
		setAllButtonUnvisible();
	}

	public boolean isLastCardInInitPhase(ArrayList<GameGUI> gameGuiList){
		int numZeros = 0;
		for(int index=0; index < gameGuiList.size(); index++){
			Log.d("currentCardId", Integer.toString((gameGuiList.get(index).getCurrentCardId())));

			if(gameGuiList.get(index).getCurrentCardId() == 0){
				numZeros++;
				Log.d("numZEROS", Integer.toString(numZeros));
			}
		}

		if(numZeros == 1){
			TextView view = (TextView)findViewById(R.id.lastAction);
			view.setText("Press 'Finish start up' button to go to the next phase");
			view.setBackgroundResource(R.drawable.test);
			view.setTextColor(getResources().getColor(R.color.black));
			return true;
		}
		else
			return false;
	}

	public void setState(int s){
		Log.d("STATE", "SETS NEW STATE-> " + Integer.toString(s));
		this.previousState = this.STATE;
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
				Log.i("ON CLICK", "ARE YOU HERE?");
				TextView view = (TextView)findViewById(R.id.lastAction);
				view.setBackgroundColor(getResources().getColor(R.color.darktrans));
				view.setTextColor(getResources().getColor(R.color.white));
				playMove(null);
			}
		});
		TextView view = (TextView)findViewById(R.id.lastAction);
		view.setText("Press 'Play Move' to play your turn. When you have a correct route you can press 'End journey'");
		view.setBackgroundResource(R.drawable.test);
		view.setTextColor(getResources().getColor(R.color.black));

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
					new AlertDialog.Builder(context)
					.setTitle("Notice")
					.setMessage("In a real game this will end your game if your route is correct, or tell you which part that failed. End tutorial?")
					.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							finish();
						}
					}).setNegativeButton("No", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							
						}
					}).show();
				
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
	}

	public void removeSettingsAndChatIcon(){
		FrameLayout frameLayout = (FrameLayout) findViewById(R.id.image_framelayout);
		//		frameLayout.removeView(findViewById(R.id.settingsImage));
		frameLayout.removeView(findViewById(R.id.chat));
	}

	
	public void specialAlert(String title,String message, final Context context){
		new AlertDialog.Builder(context)
		.setTitle(title)
		.setMessage(message)
		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				finish();
			}
		}).show();
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
	
	public class ProgressDialogClass implements Runnable {
		private Context callingActivity;
		private String title;
		private String message;
		private int timeout = 3000;

		/**
		 * Constructor
		 */
		public ProgressDialogClass(Context c, String title, String message, int timeout){
			this.callingActivity = c;
			this.title = title;
			this.message = message;
			this.timeout = timeout;

		}

		@Override
		public void run() {
			final ProgressDialog progDialog = ProgressDialog.show(callingActivity, 
					title, message,
					true);

			Log.d("progressDialog", "thread sleep");
			new Thread(new Runnable() {
				public void run() {
					Looper.prepare();

					try {
						Thread.sleep(timeout);
						progDialog.dismiss();

					}
					catch (InterruptedException e) { e.printStackTrace(); }

			    Looper.loop();
				}
			}).start();
		}
	}

}
