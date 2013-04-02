

/**
 * 
 */
package com.saimenstravelapp.activitys;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import com.saimenstravelapp.activitys.domain.*;
import com.saimenstravelapp.helper.CommonFunctions;
import com.saimenstravelapp.*;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

public class FullRuleset extends Activity {

	public SharedPreferences loginSettings;
	IntentFilter gcmFilter;
	private BroadcastReceiver gcmReceiver = null;
	Context context = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.fullruleset);

		WebView webView = (WebView) findViewById(R.id.webView1);
		webView.getSettings().setLoadWithOverviewMode(true);
		webView.loadUrl("file:///android_asset/fullruleset.html");
		loginSettings = getSharedPreferences(Login.PREFS_NAME, 0);

		gcmReceiver = CommonFunctions.createBroadCastReceiver(this, loginSettings, CommonFunctions.FROM_STANDARD_ACTIVITY);
		gcmFilter = new IntentFilter();
		gcmFilter.addAction("GCM_RECEIVED_ACTION");

		final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("Info and special rules");
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

	protected void onResume(){
		super.onResume();
		registerReceiver(gcmReceiver, gcmFilter);
	}

	protected void onPause(){
		super.onPause();
		unregisterReceiver(gcmReceiver);
	}
}




