/**
 * 
 */
package com.main.helper;

import org.json.JSONArray;
import org.json.JSONException;

import com.main.R;

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
		
		if(cardId > 2)
			cardId += 1;
		
		imageView.setImageResource(mThumbIds[cardId]);
		return imageView;
	}
}
