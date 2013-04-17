package com.saimenstravelapp.activitys;

import org.apache.http.HttpResponse;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import async.httprequest.ResponseListener;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.saimenstravelapp.R;
import com.saimenstravelapp.activitys.domain.Login;
import com.saimenstravelapp.helper.CommonFunctions;

public class StatsActivity extends Activity {
	Context context = this;
	SharedPreferences loginSettings;
	IntentFilter gcmFilter;
	private BroadcastReceiver gcmReceiver = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.stats_activity);

		loginSettings = getSharedPreferences(Login.PREFS_NAME, 0);
		gcmReceiver = CommonFunctions.createBroadCastReceiver(context, loginSettings, CommonFunctions.FROM_STANDARD_ACTIVITY);

		gcmFilter = new IntentFilter();
		gcmFilter.addAction("GCM_RECEIVED_ACTION");

		ActionBar actionBar = (ActionBar)findViewById(R.id.actionbar);
		actionBar.setTitle("Statistics");

		actionBar.setHomeAction(new Action() {
			@Override
			public void performAction(View view) {
				finish();
			}
			@Override
			public int getDrawable() {
				return R.drawable.arrow_left;
			}
		});
	}

	protected void onPause(){
		super.onPause();
		unregisterReceiver(gcmReceiver);
	}

	protected void onResume(){
		super.onResume();
		registerReceiver(gcmReceiver, gcmFilter);
	}
	
	public void goToHighScoreActivity(View view){
		Intent intent = new Intent();
		intent.setClass(this, HighScoreTmp.class);
		startActivity(intent);
	}
	
	public void goToOpponentStatsActivity(View view){
		Intent intent = new Intent();
		intent.setClass(this, OpponentStatsActivity.class);
		startActivity(intent);
	}
	
	public void goToHighestScoreActivity(View view){
		Intent intent = new Intent();
		intent.setClass(this, HighestScoreActivity.class);
		startActivity(intent);
	}
}