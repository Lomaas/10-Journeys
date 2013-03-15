/**
 * 
 */
package com.saimenstravelapp.helper;

import org.json.JSONArray;
import org.json.JSONException;

import com.saimenstravelapp.*;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;

/**
 * @author Simen
 *
 */
public class ImageAdapter extends BaseAdapter {
	private Context mContext;
	private JSONArray cards = null;

	public ImageAdapter(Context c, JSONArray cards) {
		mContext = c;
		this.cards = cards;
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

	// create a new ImageView for each item referenced by the Adapter
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView;
		if (convertView == null) {  // if it's not recycled, initialize some attributes
			imageView = new ImageView(mContext);
			imageView.setLayoutParams(new GridView.LayoutParams(LayoutParams.FILL_PARENT, 165));
			imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);	
			imageView.setPadding(1, 1, 1, 1);
		
		} else {
			imageView = (ImageView) convertView;
		}
		
		int cardId = 0;
		try {
			cardId = cards.getInt(position);
		
		}
		catch (JSONException e) {  e.printStackTrace(); }
		
		imageView.setImageResource(Constants.imageArray[cardId]);
		return imageView;
	}
}
