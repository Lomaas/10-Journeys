package com.saimenstravelapp.drag2;

import java.util.ArrayList;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;
import com.saimenstravelapp.R;
import com.saimenstravelapp.activitys.TryOutGame;
import com.saimenstravelapp.activitys.domain.GameGUI;
import com.saimenstravelapp.helper.Alert;
import com.saimenstravelapp.helper.NoZoomControllWebView;

/**
 * This subclass of ImageView is used to display an image on an GridView.
 * An ImageCell knows which cell on the grid it is showing and which grid it is attached to
 * Cell numbers are from 0 to NumCells-1.
 * It also knows if it is empty.
 *
 * <p> Image cells are places where images can be dragged from and dropped onto.
 * Therefore, this class implements both the DragSource and DropTarget interfaces.
 * 
 */

public class ImageCell extends ImageView 
implements DragSource, DropTarget
{
	public boolean mEmpty = true;
	public int mCellNumber = -1;
	public GridView mGrid;
	public int drawableId;
	public int cardId;
	public int fromGridId;
	public boolean draggable = true;
	public boolean shouldBeOpenCard = false;
	public int counterPressed = 0;

	/**
	 * Constructors
	 */

	public ImageCell (Context context, int id) {
		super (context);
		this.drawableId = id;
	}
	public ImageCell (Context context, AttributeSet attrs) {
		super (context, attrs);
	}
	public ImageCell (Context context, AttributeSet attrs, int style) {
		super (context, attrs, style);
	}

	/**
	 * This method is called to determine if the DragSource has something to drag.
	 * 
	 * @return True if there is something to drag
	 */

	public boolean allowDrag () {
		TryOutGame TryOutGame = (TryOutGame) this.getContext();

		if(TryOutGame.getState() == TryOutGame.INIT && this.mGrid != null && this.mEmpty == false && this.draggable == false){		
			return false;
		}

		if(TryOutGame.STATE == TryOutGame.YOUR_TURN){ 
			if(isYourCardsGrid()){
				Log.d("allowDrag", "test1");
				return false;
			}
		}
		else if(TryOutGame.STATE == TryOutGame.YOUR_TURN_FROM_DECK){
			Log.d("allowDrag", "state is your turn from deck");
			if(this.getFromGridId() == 0)
				return true;
			if(isYourCardsGrid())
				return false;
			if(isFromOpenCardsGrid())
				return false;

		}
		else if(TryOutGame.STATE == TryOutGame.OPPONENTS_TURN){
			Log.d("allowDrag", "state is opponentsturn");

			return false;
		}
		else if(TryOutGame.getState() == TryOutGame.YOUR_TURN_INSERT_TO_OPEN_CARDS){
			if(isYourCardsGrid())
				return false;
			if(!this.draggable)
				return false;

//			Log.d("mCellNumber", Integer.toString(TryOutGame.cellTakenFrom));
			
			// TODO if test er lagt til for Œ ikke fŒ lov til Œ drage kort nŒr det bare er ett
			if(mCellNumber == TryOutGame.cellTakenFrom && !TryOutGame.hasOpenCardParent(mCellNumber) && TryOutGame.cellTakenFrom != -1){
				return false;
			}
			Log.d("allowDrag", "is draggable");
		}
		return !mEmpty;
	}

	public boolean isYourCardsGrid(){
		if(this.mGrid != null && this.mGrid.getId() == R.id.image_grid_view){
			Log.d("isFromYourCardsGrid", "true");
			return true;
		}
		return false;
	}

	public boolean isFromOpenCardsGrid(){
		if(this.mGrid != null && this.mGrid.getId() == R.id.gridForCardsOnTable){
			Log.d("isFromYourOpenCards", "true");
			return true;
		}
		return false;
	}

	/**
	 * setDragController
	 *
	 */

	public void setDragController (DragController dragger)
	{
		// Do nothing. We do not need to know the controller object.
	}

	/**
	 * onDropCompleted
	 *
	 */

	public void onDropCompleted (View target, boolean success)
	{
		if(success){
			mEmpty = true;
			setBackgroundResource (R.color.cell_filled);	// Always et cell filled
			TryOutGame TryOutGame = (TryOutGame) target.getContext();

			if (mCellNumber >= 0) {
				if(TryOutGame.STATE == TryOutGame.YOUR_TURN && this.mGrid.getId() == 0){
					TryOutGame.setPlayButtonVisible();
				}
				ImageCellAdapter cardAdapter= (ImageCellAdapter) this.mGrid.getAdapter();

				if(TryOutGame.STATE == TryOutGame.YOUR_TURN_SWAP){
					this.mEmpty = false;
					this.draggable = true;
					TryOutGame.setPlayButtonVisible();
					TryOutGame.setState(TryOutGame.YOUR_TURN_INSERT_TO_OPEN_CARDS);
					Log.i("are you here", "onDropCompleted");
					if(this.allowDrag())
						setBackgroundResource(R.color.cell_empty_hover);
					return;
				}

				int deletedCardId = cardAdapter.getGameGUIS().get(this.mCellNumber).getDeletedCardId();
				int currentCardId = cardAdapter.getGameGUIS().get(this.mCellNumber).getCurrentCardId();

				Log.d("0 -currentcardid", Integer.toString(currentCardId));
				Log.d("0- deletedcardid", Integer.toString(deletedCardId));

				ArrayList<GameGUI> gameGui = cardAdapter.getGameGUIS();
				GameGUI currentGameGui = gameGui.get(this.mCellNumber);

				if(TryOutGame.cellTakenFrom == this.mCellNumber){
					// TODO kommentert kode var opprinnelige
					
					Log.d("onDropCompleted", "cellTakenFrom is this cell");
//					
					cardAdapter.getGameGUIS().set(this.mCellNumber, new GameGUI(TryOutGame.getCardIdFromOpenCardParent(this.mCellNumber), 
							this.mCellNumber, TryOutGame.getCardIdFromOpenCardParent(this.mCellNumber), true, deletedCardId, false));
					this.setImageResource(TryOutGame.getDrawableIdFromCardId(TryOutGame.getCardIdFromOpenCardParent(this.mCellNumber)));

//					cardAdapter.getGameGUIS().set(this.mCellNumber, new GameGUI(0, this.mCellNumber, 0, true, deletedCardId, false));
//					ImageCellAdapter.setCellDrawableForOpenCards(this.mCellNumber, this);
				}
				else if(TryOutGame.getState() == TryOutGame.YOUR_TURN_INSERT_TO_OPEN_CARDS && !isYourCardsGrid() && deletedCardId != 0){
					mEmpty = false;		// always not empty when coming so far,
					Resources res = getResources();
					Drawable d = res.getDrawable (TryOutGame.getDrawableIdFromCardId(deletedCardId));	// copy over the drawable

					this.setDrawableId(this.getDrawableId());
					this.setFromGridId(this.mGrid.getId());
					this.setCardId(deletedCardId);
					this.draggable = false;

					if (d != null) {
						Log.i("onDropCompleted", "not null drawable");
						this.setImageDrawable (d);
					}
					if(TryOutGame.cellTakenFrom == this.mCellNumber)
						cardAdapter.getGameGUIS().set(this.mCellNumber, new GameGUI(this.getDrawableId(), this.mCellNumber, deletedCardId, true, 0, this.draggable));
					else
						cardAdapter.getGameGUIS().set(this.mCellNumber, new GameGUI(this.getDrawableId(), this.mCellNumber, deletedCardId, false, 0, this.draggable));
				}
				else {
					Log.i("onDropCompleted", "Was previous a empty cell");
					setImageDrawable (null);
					ImageCellAdapter adapter = (ImageCellAdapter) this.mGrid.getAdapter();
					cardAdapter.getGameGUIS().set(this.mCellNumber, new GameGUI(0, this.mCellNumber, 0, false, 0, false));

					if(!isYourCardsGrid())
						ImageCellAdapter.setCellDrawableForOpenCards(mCellNumber, this);
					else 
						ImageCellAdapter.setCellDrawable(mCellNumber, this);

					this.setCardId(0);	
				}
			}
			else {
				Log.i("onDropCompleted", "DROP FROM OUTSIDE OF A GRID. DO NOTHING");
				if(TryOutGame.getState() == TryOutGame.YOUR_TURN){
					TryOutGame.setPlayButtonVisible();
				}
				else if(TryOutGame.getState() == TryOutGame.INIT)
					TryOutGame.setCardButtonEnabled();

				if(!isYourCardsGrid())
					ImageCellAdapter.setCellDrawableForOpenCards(mCellNumber, this);
				else
					ImageCellAdapter.setCellDrawable(mCellNumber, this);

				setImageResource (0);
			}
		}
	}

	/**
	 * Handle an object being dropped on the DropTarget.
	 * This is the where the drawable of the dragged view gets copied into the ImageCell.
	 * 
	 * @param source DragSource where the drag started
	 * @param x X coordinate of the drop location
	 * @param y Y coordinate of the drop location
	 * @param xOffset Horizontal offset with the object being dragged where the original
	 *          touch happened
	 * @param yOffset Vertical offset with the object being dragged where the original
	 *          touch happened
	 * @param dragView The DragView that's being dragged around on screen.
	 * @param dragInfo Data associated with the object being dragged
	 * 
	 */
	public void onDrop(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo)
	{	
		ImageCell cell = (ImageCell) dragInfo;

		ImageCellAdapter cardAdapter= (ImageCellAdapter) this.mGrid.getAdapter();
		TryOutGame TryOutGame = (TryOutGame) dragView.getContext();
		
		setBackgroundResource (R.color.cell_filled);
		
		if(TryOutGame.getState() == TryOutGame.YOUR_TURN_INSERT_TO_OPEN_CARDS){
			TryOutGame.setPlayButtonVisible();
			this.draggable = true;
			setBackgroundResource(R.color.cell_empty_hover);
		}
		
		if(TryOutGame.getState() == TryOutGame.INIT){
			TryOutGame.setImageSourceFrameGone();
			
			if(TryOutGame.isLastCardInInitPhase(cardAdapter.getGameGUIS()))
				TryOutGame.changeCardDeckButtonForNextPhase();
		}
		
		if(mEmpty == false){			
			if(TryOutGame.getState() == TryOutGame.INIT){
				toast("In init you can only update days that are still available");
				return;
			}
			else if(TryOutGame.getState() == TryOutGame.YOUR_TURN_INSERT_TO_OPEN_CARDS){
				// Do not add the card dropped on to the screen. Save it as "deleted card" in gameGuis
				// this is done below
				
			}
			else {
				TryOutGame.setState(TryOutGame.YOUR_TURN_SWAP);
				insertToOpenCardGrid(this.getCardId(), this.getDrawableId(), cell.mCellNumber, cell.mGrid, cell.getFromGridId());
			}
		}

		Log.d("onDrop5", Integer.toString(this.getDrawableId()));
		Log.d("onDrop6", Integer.toString(this.mGrid.getId()));

		mEmpty = false;		// always not empty when coming so far,

		// The view being dragged does not actually change its parent and switch over to the ImageCell.
		// What we do is copy the drawable from the source view.
		ImageView sourceView = (ImageView) source;
		Drawable d = sourceView.getDrawable ();

		this.setDrawableId(cell.getDrawableId());
		this.setFromGridId(this.mGrid.getId());
		this.setCardId(cell.getCardId());

		ArrayList<GameGUI> gameGui = cardAdapter.getGameGUIS();
		GameGUI currentGameGui = gameGui.get(this.mCellNumber);

		if(TryOutGame.cellTakenFrom == this.mCellNumber){
			GameGUI oldGui = cardAdapter.getGameGUIS().get(this.mCellNumber);
			cardAdapter.getGameGUIS().set(this.mCellNumber, new GameGUI(cell.getDrawableId(), this.mCellNumber, cell.getCardId(), true, oldGui.getDeletedCardId(), this.draggable));
		}
		else
			cardAdapter.getGameGUIS().set(this.mCellNumber, new GameGUI(cell.getDrawableId(), this.mCellNumber, cell.getCardId(), true, currentGameGui.getCurrentCardId(), this.draggable));

		if (d != null) {		
			this.setImageDrawable (d);
		}
	}

	/**
	 * React to a dragged object entering the area of this DropSpot.
	 * Provide the user with some visual feedback.
	 */    
	public void onDragEnter(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo)
	{
		int bg = mEmpty ? R.color.cell_empty_hover : R.color.cell_filled_hover;
		setBackgroundResource (bg);
	}

	/**
	 * React to something being dragged over the drop target.
	 */    
	public void onDragOver(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo)
	{
	}

	/**
	 * React to a drag 
	 */    
	public void onDragExit(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo)
	{
		setBackgroundResource (R.color.cell_filled);
	}

	/**
	 * Check if a drop action can occur at, or near, the requested location.
	 * This may be called repeatedly during a drag, so any calls should return
	 * quickly.
	 * 
	 * @param source DragSource where the drag started
	 * @param x X coordinate of the drop location
	 * @param y Y coordinate of the drop location
	 * @param xOffset Horizontal offset with the object being dragged where the
	 *            original touch happened
	 * @param yOffset Vertical offset with the object being dragged where the
	 *            original touch happened
	 * @param dragView The DragView that's being dragged around on screen.
	 * @param dragInfo Data associated with the object being dragged
	 * @return True if the drop will be accepted, false otherwise.
	 */
	public boolean acceptDrop(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo){
		ImageCell cell = (ImageCell) dragInfo;

		if(this.mGrid != null){		
			TryOutGame TryOutGame = (TryOutGame) dragView.getContext();

			Log.d("aceeptDrop", Integer.toString(this.mGrid.getId()));
			Log.d("acceptDrop", Integer.toString(R.id.gridForCardsOnTable));
			Log.d("acceptDrop", Integer.toString(cell.getFromGridId()));

			if(TryOutGame.STATE == TryOutGame.INIT){
				if(this.mGrid.getId() == R.id.gridForCardsOnTable)
					return false;

				if(mEmpty == false && this.mGrid.getId() == R.id.image_grid_view)
					return false;
			}
			else if(TryOutGame.STATE == TryOutGame.YOUR_TURN_INSERT_TO_OPEN_CARDS){
				if(isYourCardsGrid())
					return false;
				if(this == cell)
					return false;
			}

			else if(TryOutGame.STATE == TryOutGame.YOUR_TURN_FROM_DECK || TryOutGame.STATE == TryOutGame.YOUR_TURN){
				if(this.mGrid.getId() == cell.getFromGridId())
					return false;

				if(cell.getFromGridId() == 0 && this.mGrid.getId() == R.id.gridForCardsOnTable)
					return false;

				TryOutGame.setAllButtonUnvisible();
			}

			// From gridsforcardsOnTable set celltakenFrom
			if(TryOutGame.STATE == TryOutGame.YOUR_TURN && cell.getFromGridId() == R.id.gridForCardsOnTable){
				TryOutGame.cellTakenFrom = cell.mCellNumber;
			}
		}
		return mCellNumber >= 0;
	}

	private void insertToOpenCardGrid(int cardId, int resourceId, int index, GridView gridView, int fromGridId){
		ImageCellAdapter adapter = null;
		GameGUI newGui = null;

		if(fromGridId == 0){
			// If zero - means that it is a drop from the cardDeck
			Log.i("gridview", "is zero");

			adapter = (ImageCellAdapter) gridView.getAdapter();
			GameGUI oldGui = adapter.getGameGUIS().get(0);
			newGui = new GameGUI(resourceId, 0, cardId, true, oldGui.getCurrentCardId(), true);
			adapter.getGameGUIS().set(0, newGui);
			adapter.notifyDataSetChanged();
			
			ImageCell cell = (ImageCell) adapter.getView(0, null, gridView);
			cell.onDropCompleted(cell, true);

		}
		else{
			adapter = (ImageCellAdapter) gridView.getAdapter();
			GameGUI oldGui = adapter.getGameGUIS().get(index);
			newGui = new GameGUI(resourceId, index, cardId, true, oldGui.getCurrentCardId(), true);
			adapter.getGameGUIS().set(index, newGui);
			adapter.notifyDataSetChanged();
		}
		Log.i("inserts ToOpenCardGrid", "seems ok");
	}

	/**
	 * Estimate the surface area where this object would land if dropped at the
	 * given location.
	 * 
	 * @param source DragSource where the drag started
	 * @param x X coordinate of the drop location
	 * @param y Y coordinate of the drop location
	 * @param xOffset Horizontal offset with the object being dragged where the
	 *            original touch happened
	 * @param yOffset Vertical offset with the object being dragged where the
	 *            original touch happened
	 * @param dragView The DragView that's being dragged around on screen.
	 * @param dragInfo Data associated with the object being dragged
	 * @param recycle {@link Rect} object to be possibly recycled.
	 * @return Estimated area that would be occupied if object was dropped at
	 *         the given location. Should return null if no estimate is found,
	 *         or if this target doesn't provide estimations.
	 */
	public Rect estimateDropLocation(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo, Rect recycle)
	{
		return null;
	}


	/**
	 * Return true if this cell is empty.
	 * If it is, it means that it will accept dropped views.
	 * It also means that there is nothing to drag.
	 * 
	 * @return boolean
	 */

	public boolean isEmpty ()
	{
		return mEmpty;
	}

	public void setBackGroundDrawable(){
		if(isEmpty()){
			if(this.mGrid.getId() == R.id.gridForCardsOnTable){
				ImageCellAdapter.setCellDrawable(mCellNumber, this);
			}
			else {
				ImageCellAdapter.setCellDrawableForOpenCards(this.mCellNumber, this);
			}
		}
	}

	/**
	 * Call this view's onClick listener. Return true if it was called.
	 * Clicks are ignored if the cell is empty.
	 * 
	 * @return boolean
	 */

	public boolean performClick ()
	{
		if (!mEmpty) return super.performClick ();
		return false;
	}

	/**
	 * Call this view's onLongClick listener. Return true if it was called.
	 * Clicks are ignored if the cell is empty.
	 * 
	 * @return boolean
	 */

	public boolean performLongClick ()
	{
		if (!mEmpty) return super.performLongClick ();
		return false;
	}

	/**
	 * Show a string on the screen via Toast if DragActivity.Debugging is true.
	 * 
	 * @param msg String
	 * @return void
	 */

	public void toast (String msg)
	{
		Toast.makeText (getContext (), msg, Toast.LENGTH_SHORT).show ();
	}

	public int getDrawableId() {
		return drawableId;
	}

	public void setDrawableId(int drawableId) {
		this.drawableId = drawableId;
	}

	public int getFromGridId() {
		return fromGridId;
	}

	public void setFromGridId(int gridId) {
		this.fromGridId = gridId;
	}

	public int getCardId() {
		return cardId;
	}

	public void setCardId(int cardId) {
		this.cardId = cardId;
	}

	public int getmCellNumber() {
		return mCellNumber;
	}
	public void setmCellNumber(int mCellNumber) {
		this.mCellNumber = mCellNumber;
	}
}
