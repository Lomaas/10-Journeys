package com.saimenstravelapp.helper;

import java.util.ArrayList;
import com.saimenstravelapp.*;
import com.saimenstravelapp.activitys.domain.*;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author Simen
 *
 */
public class FriendsStatisticAdapter extends BaseAdapter {
	Context context = null;
	private ArrayList<Player> players;


	public FriendsStatisticAdapter(Context context, ArrayList<Player> players) {
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
			v = vi.inflate(R.layout.friends_stat_list_row, null);
		}
		Player player = (Player) getItem(position);
		
		if(player != null){
			TextView nameAndRank = (TextView) v.findViewById(R.id.playerNameRank);
			TextView winsLossText = (TextView) v.findViewById(R.id.winsLossText);
			TextView score = (TextView) v.findViewById(R.id.score);
			ImageView imageView = (ImageView)v.findViewById(R.id.avatar);

			if (nameAndRank != null) {
				nameAndRank.setText(player.getUsername());
			}

			if(winsLossText != null) {
				String tmp = "Wins: " + Integer.toString(player.getWins()) + " Loss: " + Integer.toString(player.getLoss());
				winsLossText.setText(tmp);
			}

			if(score != null){
				score.setText("Win-%: " + CommonFunctions.getWinPercent(player.getWins() + player.getLoss(), player.getWins()));
			}

			if(imageView != null){
				imageView.setImageResource(Constants.profileArray[player.getImageId()]);
			}     
		}
		return v;
	}

}
