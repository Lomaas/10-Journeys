package com.saimenstravelapp.helper;


import java.util.ArrayList;
import com.saimenstravelapp.*;
import com.saimenstravelapp.activitys.domain.*;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author Simen
 *
 */
public class HighScoreAdapter extends BaseAdapter {
	Context context = null;
	private ArrayList<Player> players;


	public HighScoreAdapter(Context context, ArrayList<Player> players) {
		this.context = context;
		this.players = players;
		
	}

	@Override
	public int getCount() {
		return players.size();
	}
	@Override
	public Object getItem(int position) {		
		return players.get(position);
	}
	
	@Override
	public long getItemId(int position) {
		return 0;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		
		if (v == null) {
			LayoutInflater vi = 
					(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.highscore_row, null);
		}
		Player player = (Player) getItem(position);

		Log.d("usernameAdapter", Integer.toString(position));
		Log.d("usernameAdapter", player.getUsername());

		
		if(player != null){
			TextView nameAndRank = (TextView) v.findViewById(R.id.playerNameRank);
			TextView winsLossText = (TextView) v.findViewById(R.id.winsLossText);
			TextView score = (TextView) v.findViewById(R.id.score);
			ImageView imageView = (ImageView)v.findViewById(R.id.avatar);


			if (nameAndRank != null) {
				String tmp = Integer.toString(player.getRank()) + ". " + player.getUsername();
				nameAndRank.setText(tmp);
			}

			if(winsLossText != null) {
				String tmp = "Wins: " + Integer.toString(player.getWins()) + ", LOSS: " + Integer.toString(player.getLoss());
				winsLossText.setText(tmp);
			}

			if(score != null){
				score.setText(Integer.toString(player.getScore()));
			}

			if(imageView != null){
				imageView.setImageResource(Constants.profileArray[player.getImageId()]);
			}     
		}
		return v;
	}

}
