package com.saimenstravelapp.drag2;

import java.util.ArrayList;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.saimenstravelapp.*;
import com.saimenstravelapp.activitys.TryOutGame;
import com.saimenstravelapp.activitys.domain.GameGUI;


/**
 * This class is used with a GridView object. It provides a set of ImageCell objects 
 * that support dragging and dropping.
 * 
 */

public class ImageCellAdapter extends BaseAdapter 
{
	public int NUM_IMAGES;
	public int width;
	public int height;

	public ViewGroup mParentView = null;
	private Context mContext;
	private ArrayList<GameGUI> gameGUIS = null;

	public ImageCellAdapter(Context c, int numImages, int height, int width, ArrayList<GameGUI> gameGUIS) 
	{
		mContext = c;
		this.NUM_IMAGES = numImages;
		this.height = height;
		this.width = width;
		this.gameGUIS = gameGUIS;
	}

	public int getCount() 
	{
		//Resources res = mContext.getResources();
		//int numImages = res.getInteger (R.integer.num_images);
		return this.NUM_IMAGES;
	}

	public Object getItem(int position)
	{
		return null;
	}

	public long getItemId(int position) {
		return position;
	}

	/**
	 * getView
	 * Return a view object for the grid.
	 * 
	 * @return ImageCell
	 */
	public View getView (int position, View convertView, ViewGroup parent)
	{
		mParentView = parent;

		ImageCell v = null;
		if (convertView == null) {
			v = new ImageCell (mContext, 0);

			int w = LinearLayout.LayoutParams.FILL_PARENT/NUM_IMAGES;		
			TryOutGame game = (TryOutGame) v.getContext();

			Resources r = game.getResources();
			float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, r.getDisplayMetrics());
			GridView gridView = (GridView) mParentView;
			LinearLayout.LayoutParams params = null;
			
			if(gridView.getId() == R.id.gridForCardsOnTable){
				float py = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 62, r.getDisplayMetrics());
				params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, (int)py);
				v.setPadding(0, 0, 0, 4);
			}
			else {
				float py = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 65, r.getDisplayMetrics());
				params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, (int) py);
				v.setPadding(3, 3, 3, 3);
			}
			v.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			v.setLayoutParams(new GridView.LayoutParams(params));
//			Log.d("convertView", "convertView null");
		} 
		else {
			v = (ImageCell) convertView;
//			Log.d("convertView", Integer.toString(v.getDrawableId()));
		}
		v.mCellNumber = position; 
		v.mGrid = (GridView) mParentView;
		v.mEmpty = true;

		if(gameGUIS.size() > position && gameGUIS.get(position).getResourceId() != -1){
			TryOutGame TryOutGame = (TryOutGame) v.getContext();

			if(TryOutGame.getState() == TryOutGame.INIT && !gameGUIS.get(position).isDraggable()){
//				Log.d("convertview", "sets not draggable");
				v.draggable = false;
			}

			else if(TryOutGame.getState() == TryOutGame.YOUR_TURN){
//				Log.d("convertview", "test2");
				v.draggable = false;
			}
			else if(TryOutGame.getState() == TryOutGame.YOUR_TURN_FROM_DECK){		// newly added 01-09
				v.draggable = false;
//				Log.d("convertview", "test3");
			}
			else if(TryOutGame.getState() == TryOutGame.YOUR_TURN_INSERT_TO_OPEN_CARDS){
				v.draggable = false;
			}
			v.mEmpty = false;
//			Log.d("state", Integer.toString(TryOutGame.getState()));
			
			if(gameGUIS.get(position).isUpdated()){
				v.draggable = true;
//				Log.i("are you here", "getView adapter");
				if(v.allowDrag())
					v.setBackgroundResource (R.color.cell_empty_hover);
			}
			else
				v.setBackgroundResource (R.color.cell_filled);

			v.setImageResource(gameGUIS.get(position).getResourceId());
			v.setCardId(gameGUIS.get(position).getCurrentCardId());
			v.setDrawableId(gameGUIS.get(position).getResourceId());
			v.setFromGridId(v.mGrid.getId());
		}
		else {			
			if(v.mGrid.getId() == R.id.gridForCardsOnTable){
				setCellDrawableForOpenCards(position, v);
//				Log.d("convertview", "gridForCardsOnTable");
			}
			else
				setCellDrawable(position, v);
		}

		v.setOnTouchListener ((View.OnTouchListener) mContext);
		v.setOnClickListener ((View.OnClickListener) mContext);
		v.setOnLongClickListener ((View.OnLongClickListener) mContext);

		return v;
	}

	public ArrayList<GameGUI> getGameGUIS() {
		return gameGUIS;
	}

	public void setGameGUIS(ArrayList<GameGUI> gameGUIS) {
		this.gameGUIS = gameGUIS;
	}

	public static void setCellDrawable(int position, ImageCell cell){
		position += 1;

		if(position == 1)
			cell.setImageResource(R.drawable.cards_cell1);
		else if(position == 2)
			cell.setImageResource ( R.drawable.cards_cell2);
		else if(position == 3)
			cell.setImageResource ( R.drawable.cards_cell3);
		else if(position == 4)
			cell.setImageResource ( R.drawable.cards_cell4);
		else if(position == 5)
			cell.setImageResource ( R.drawable.cards_cell5);
		else if(position == 6)
			cell.setImageResource ( R.drawable.cards_cell6);
		else if(position == 7)
			cell.setImageResource ( R.drawable.cards_cell7);
		else if(position == 8)
			cell.setImageResource ( R.drawable.cards_cell8);
		else if(position == 9)
			cell.setImageResource ( R.drawable.cards_cell9);
		else if(position == 10)
			cell.setImageResource ( R.drawable.cards_cell10);
	}

	public static void setCellDrawableForOpenCards(int position, ImageCell cell){
		position += 1;

		if(position == 1)
			cell.setImageResource ( R.drawable.cards_opencard1);
		else if(position == 2)
			cell.setImageResource ( R.drawable.cards_opencard2);
		else if(position == 3)
			cell.setImageResource ( R.drawable.cards_opencard3);
	}	
}
