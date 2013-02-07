/**
 * 
 */
package com.main.activitys;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import com.main.activitys.domain.*;
import com.main.helper.CommonFunctions;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ViewFlipper;
import com.main.*;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

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

	private BroadcastReceiver gcmReceiver = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.introduction);
		mAdapter = new MyAdapter(getSupportFragmentManager());
		
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
      Log.d("Test", "hell1o");
		}

		@Override
		public int getCount() {
			return 10;
		}

		@Override
		public Fragment getItem(int position) {
      Log.d("Test", "he2llo");

			switch (position) {
			case 0:
				return new ImageFragment(0);
			case 1:
				return new ImageFragment(1);
			case 2:
				return new ImageFragment(2);
			case 3:
				return new ImageFragment(3);
			case 4:
				return new ImageFragment(4);
			case 5:
				return new ImageFragment(5);
			case 6:
				return new ImageFragment(6);
			case 7:
				return new ImageFragment(7);
			case 8:
				return new ImageFragment(8);
			case 9:
				return new ImageFragment(9);
			default:
				return null;
			}
		}
	}
}




