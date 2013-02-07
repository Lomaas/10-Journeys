/**
 * 
 */
package com.main.helper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import com.main.*;
import com.main.activitys.AllGamesActivity;
import com.main.activitys.domain.*;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * @author Simen
 *
 */
public class GameAdapter extends ArrayAdapter<Game> {
	Context context = null;
		
	public GameAdapter(Context context, int textViewResourceId, ArrayList<Game> gameList) {
		super(context, textViewResourceId, gameList);
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = 
					(LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.games_row, null);
		}

		final Game game = getItem(position);
		
		if(game != null){
      TextView username = (TextView) v.findViewById(R.id.gameRowUsername);
      TextView message = (TextView) v.findViewById(R.id.gameRowLastAction);
      TextView lastUpdate = (TextView) v.findViewById(R.id.timeSinceUpdate);
      TextView dateCreated = (TextView) v.findViewById(R.id.gameStatus);
      ImageView imageView = (ImageView)v.findViewById(R.id.avatar);
      
//      LinearLayout linearLayout = (LinearLayout)v.findViewById(R.id.thumbnail);
//      linearLayout.setOnClickListener(new OnClickListener() {
//				
//				@Override
//				public void onClick(View v) {
//					AllGamesActivity games = (AllGamesActivity) context;
//					games.startChatActivity(Integer.toString(game.getOpponentId()), Integer.toString(game.getGameId()), game.getOpponentsUsername().get(0));
//
//				}
//			});
      
      if (username != null) {
      	ArrayList<String> tmp = game.getOpponentsUsername();
      	String stringTmp = "";
      	for(int iter=0; iter< tmp.size(); iter++){
      		if(stringTmp.length() > 0)
      			stringTmp = stringTmp + ", " + tmp.get(iter);
      		else
      			stringTmp = tmp.get(iter);
      	}

      	
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
      		lastUpdate.setText(" " + tmp[0] + "h");
      	}
      	else if(Integer.parseInt(tmp[1]) != 0){
      		char test []= tmp[1].toCharArray();
      		if(test[0] == '0')
        		lastUpdate.setText(" " + test[1] + "m");
      		else
      			lastUpdate.setText(" " + tmp[1] + "m");
      	}
      	else{
      		lastUpdate.setText(" " + tmp[0] + "s");
      	}
      }
      
      if(dateCreated != null){
      	String tmpDate = game.getDateCreated();
      	TimeZone zone = SimpleTimeZone.getDefault();
//      	Log.d("timeZone", Integer.toString(zone.getRawOffset()));
//      	Log.d("timeZone", tmpDate);
      	
      	DateFormat myDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      	try {
					Date tempDate = myDateFormat.parse(tmpDate);
					long milliseconds = tempDate.getTime() + zone.getRawOffset();
					tempDate.setTime(milliseconds);
//					Log.d("tempDate", tempDate.toString());
					
					String typeOfGame = " ";
					if(game.getType() == 1){
						typeOfGame = "Europe, ";
					}
					else if(game.getType() == 2)
						typeOfGame = "America, ";
					
					dateCreated.setText(typeOfGame + DateFormat.getDateTimeInstance().format(tempDate));
				}
				catch (ParseException e) { e.printStackTrace(); }
      }
      if(imageView != null){
      	imageView.setImageResource(Constants.profileArray[game.getImageId()]);
				ImageView chatIcon = (ImageView)v.findViewById(R.id.chatIcon);

  			if(Extrainfo.isNewChatMsg(context.getSharedPreferences(Extrainfo.PREFS_NAME, 0), Integer.toString(game.getGameId()))){
  				chatIcon.setImageResource(R.drawable.chat_icon_main);
  			}
  			else {
  				chatIcon.setImageDrawable(null);
  			}
  				
      }     
		}
		return v;
	}
}
