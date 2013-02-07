package com.main.activitys;

 
import com.main.*;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

 
public class InviteFriendTabActivity extends TabActivity {
		private Bundle extras;
		public static int type = 0;
		
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.find_friend);
        
    		extras = getIntent().getExtras();
    		type = (Integer) extras.get("type");
    		
        TabHost tabHost = getTabHost();
 
        TabSpec friends = tabHost.newTabSpec("Friends");
        friends.setIndicator("Friends", getResources().getDrawable(R.drawable.contact_tab));
        Intent friendIntent = new Intent(this, GetAddedFriendsActivity.class);
        friends.setContent(friendIntent);
 
        TabSpec contact = tabHost.newTabSpec("Contacts");
        contact.setIndicator("Contacts", getResources().getDrawable(R.drawable.friends_tab));
        Intent contactIntent = new Intent(this, GetContactActivity.class);
        contact.setContent(contactIntent);
 
//        TabSpec recent = tabHost.newTabSpec("Facebook friends");
//        recent.setIndicator("Facebook friends", getResources().getDrawable(R.drawable.recent));
//        Intent recentIntent = new Intent(this, GetRecentPlayersActivity.class);
//        recent.setContent(recentIntent);
 
        tabHost.addTab(friends);
        tabHost.addTab(contact);
//        tabHost.addTab(recent);
    }
}