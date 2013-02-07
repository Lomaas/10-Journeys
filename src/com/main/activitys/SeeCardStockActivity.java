/**
 * 
 */
package com.main.activitys;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uit.nfc.AsynchronousHttpClient;
import uit.nfc.ResponseListener;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.main.R;
import com.main.activitys.GameFinishActivity.ImgAdapter;
import com.main.activitys.domain.Login;
import com.main.helper.CommonFunctions;
import com.main.helper.ImageAdapter;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

/**
 * @author Simen
 *
 */
public class SeeCardStockActivity 	extends Activity{
	public ResponseListener getCardStockListener;

	SharedPreferences loginSettings;
	Context context = this;
	IntentFilter gcmFilter;
	GridView myGrid;
	private BroadcastReceiver gcmReceiver = null;

	Bundle extras = null;
	private ProgressBar mProgress;
	public static String GET_CARD_STOCK= "http://restfulserver.herokuapp.com/cards/stock/";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.see_card_stock);

		loginSettings = getSharedPreferences(Login.PREFS_NAME, 0);
		gcmReceiver = CommonFunctions.createBroadCastReceiver(context, loginSettings, CommonFunctions.FROM_STANDARD_ACTIVITY);
		gcmFilter = new IntentFilter();
		gcmFilter.addAction("GCM_RECEIVED_ACTION");
		
		extras = getIntent().getExtras();
		String type = extras.getString("type");
		
		ActionBar actionBar = (ActionBar)findViewById(R.id.actionbar);
		actionBar.setTitle(CommonFunctions.getMapFromType(Integer.parseInt(type)));

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


		myGrid = (GridView) findViewById(R.id.MyGrid);
		mProgress = (ProgressBar) findViewById(R.id.progressBar);
		
		getCardStockListener = new ResponseListener() {
			@Override
			public void onResponseReceived(HttpResponse response, String message) {
				Log.i("onResponseReceived", message);
				evaulateResponse(message);
				mProgress.setVisibility(View.GONE);				
			}
		};
		getCardStock(type);
	}
	
	protected void onResume(){
		super.onResume();
		registerReceiver(gcmReceiver, gcmFilter);
	}
	
	protected void onPause(){
		super.onPause();
		unregisterReceiver(gcmReceiver);
	}
	
	private void getCardStock(String type) {
		HttpGet httpGet = new HttpGet(GET_CARD_STOCK + type);

		AsynchronousHttpClient a = new AsynchronousHttpClient();
		a.sendRequest(httpGet, getCardStockListener, loginSettings);
	}
	
	public void evaulateResponse(String message){
		Log.i("message", message);
		
		try {
			JSONArray cards = new JSONArray(message);
			myGrid.setAdapter(new ImageAdapter(this, cards));
		}
		catch(JSONException e){ e.printStackTrace(); }
	}
}