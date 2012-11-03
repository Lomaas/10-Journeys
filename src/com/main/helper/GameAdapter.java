/**
 * 
 */
package com.main.helper;

import java.util.ArrayList;

import com.main.*;
import com.main.activitys.domain.*;
import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * @author Simen
 *
 */
public class GameAdapter extends ArrayAdapter<Game> {
		
	public GameAdapter(Context context, int textViewResourceId, ArrayList<Game> gameList) {
		super(context, textViewResourceId, gameList);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = 
					(LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.games_row, null);
		}

		Game game = getItem(position);
		
		if(game != null){
      TextView username = (TextView) v.findViewById(R.id.gameRowUsername);
      TextView message = (TextView) v.findViewById(R.id.gameRowLastAction);
      TextView lastUpdate = (TextView) v.findViewById(R.id.timeSinceUpdate);
      
      if (username != null) {
      	ArrayList<String> tmp = game.getOpponentsUsername();
      	String stringTmp = "";
      	for(int iter=0; iter< tmp.size(); iter++){
      		if(stringTmp.length() > 0)
      			stringTmp = stringTmp + ", " + tmp.get(iter);
      		else
      			stringTmp = tmp.get(iter);
      	}
//      	Log.d("username", stringTmp);
        username.setText("Playing with " + stringTmp);
      }

      if(message != null) {
//      	Log.d("lastaction", game.getLastAction());
        message.setText(game.getLastAction());
      }
      
      if(lastUpdate != null){
      	String tmp [] = game.getTimeSinceLastMove().split(":");
//      	Log.d("tmp", tmp[0]);
//      	Log.d("tmp", tmp[1]);
//      	Log.d("tmp", tmp[2]);

      	if(tmp[0].contains("day")){
    			lastUpdate.setText(tmp[0] + "h");
    		}
      	else if(Integer.parseInt(tmp[0]) != 0){
      		lastUpdate.setText(tmp[0] + " h");
      	}
      	else if(Integer.parseInt(tmp[1]) != 0){
      		char test []= tmp[1].toCharArray();
      		if(test[0] == '0')
        		lastUpdate.setText(test[1] + " m");
      		else
      			lastUpdate.setText(tmp[1] + " m");
      	}
      	else{
      		lastUpdate.setText(tmp[0] + " s");
      	}
      }
		}
		return v;
	}
}
