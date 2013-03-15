/**
 * 
 */
package com.saimenstravelapp.activitys;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ViewFlipper;
import com.saimenstravelapp.*;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.saimenstravelapp.activitys.domain.*;
import com.saimenstravelapp.helper.CommonFunctions;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;


public class IntroductionActivity extends FragmentActivity {
	private MyAdapter mAdapter;
	private ViewPager mPager;

	static final int NUM_ITEMS = 2;
	public SharedPreferences loginSettings;

	IntentFilter gcmFilter;
	ViewFlipper viewFlipper;
	static Context context;

	private BroadcastReceiver gcmReceiver = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.introduction);
		mAdapter = new MyAdapter(getSupportFragmentManager());
		context = this;

		mPager = (ViewPager) findViewById(R.id.pager);
		mPager.setOffscreenPageLimit(0);	// TODO check this out if it works when setting higher screen image
		mPager.setAdapter(mAdapter);

		loginSettings = getSharedPreferences(Login.PREFS_NAME, 0);

		gcmReceiver = CommonFunctions.createBroadCastReceiver(this, loginSettings, CommonFunctions.FROM_STANDARD_ACTIVITY);
		gcmFilter = new IntentFilter();
		gcmFilter.addAction("GCM_RECEIVED_ACTION");

		final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("Introduction");

		actionBar.addAction(new Action() {
			@Override
			public void performAction(View view) {
				finish();
			}
			@Override
			public int getDrawable() {
				return R.drawable.done;
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

	public static class MyAdapter extends FragmentPagerAdapter {
		public MyAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			return 8;
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
			case 0:
				return new ImageFragment(0, context);
			case 1:
				return new ImageFragment(1, context);
			case 2:
				return new ImageFragment(2, context);
			case 3:
				return new ImageFragment(3, context);
			case 4:
				return new ImageFragment(4, context);
			case 5:
				return new ImageFragment(5, context);
			case 6:
				return new ImageFragment(6, context);
			case 7:
				return new ImageFragment(7, context);
//			case 8:
//				return new ImageFragment(8);
//			case 9:
//				return new ImageFragment(9);
			default:
				return null;
			}
		}
	}
}




