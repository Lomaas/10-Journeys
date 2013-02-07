/**
 * 
 */
package com.main.activitys;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uit.nfc.AsynchronousHttpClient;
import uit.nfc.ResponseListener;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import com.main.R;
import com.main.activitys.domain.Login;
import com.main.helper.Alert;
import com.main.helper.CommonFunctions;
import com.main.helper.Constants;
import com.main.helper.ProgressDialogClass;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

/**
 * @author Simen
 *
 */
public class SelectProfileActivity extends Activity {
	public ResponseListener responseListener;

	SharedPreferences loginSettings;
	Context context = this;
	IntentFilter gcmFilter;
	private BroadcastReceiver gcmReceiver =null;
	public ProgressDialogClass progDialog;

	public static String ADD_PROFILE_PICTURE = "http://restfulserver.herokuapp.com/user/update_profile_picture";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.grid_profilepicture);

		loginSettings = getSharedPreferences(Login.PREFS_NAME, 0);
		gcmReceiver = CommonFunctions.createBroadCastReceiver(context, loginSettings, CommonFunctions.FROM_STANDARD_ACTIVITY);
		gcmFilter = new IntentFilter();
		gcmFilter.addAction("GCM_RECEIVED_ACTION");

		GridView myGrid = (GridView)findViewById(R.id.MyGrid);


		final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("Select profile picture");
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

		myGrid.setAdapter(new ImgAdapter(this));
		myGrid.setOnItemClickListener(

				new OnItemClickListener(){
					public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
						Log.i("maybe pos: ?", Integer.toString(position));
						postProfileIdUpdate(position+1);
					}
				});
		responseListener = new ResponseListener() {
			@Override
			public void onResponseReceived(HttpResponse response, String message) {
				Log.i("onResponseReceived", message);
				progDialog.dissMissProgressDialog();
				evaulateResponse(message);
			}
		};
	}

	protected void onResume(){
		super.onResume();
		registerReceiver(gcmReceiver, gcmFilter);
	}

	protected void onPause(){
		super.onPause();
		unregisterReceiver(gcmReceiver);
	}


	private void postProfileIdUpdate(int UID) {
		HttpPost httpPost = null;
		StringEntity se = null;
		JSONObject jsonObj = new JSONObject();

		try {
			httpPost = new HttpPost(new URI(ADD_PROFILE_PICTURE));
			jsonObj.put("imageId", UID);

			se = new StringEntity(jsonObj.toString());
		}
		catch (URISyntaxException e1) { e1.printStackTrace(); }
		catch (UnsupportedEncodingException e) { e.printStackTrace(); }
		catch (JSONException e) { e.printStackTrace(); }
		
		se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
		httpPost.setEntity(se);

		AsynchronousHttpClient a = new AsynchronousHttpClient();
		a.sendRequest(httpPost, responseListener, loginSettings);

		progDialog = new 
				ProgressDialogClass(this, 
						"Updating", 
						"Pushing to server..",
						15000);
		progDialog.run();
	}

	public void evaulateResponse(String message){
		try {
			JSONObject obj = new JSONObject(message);
			if(obj.has("imageId")){
				Login.storeProfileId(loginSettings, obj.getInt("imageId"));
				finish();
			}
			else{
				new Alert("Ups", obj.getString("error"), this);
			}
		}
		catch(JSONException e){ e.printStackTrace(); }
	}

	public class ImgAdapter extends BaseAdapter {
		Context MyContext;

		public ImgAdapter(Context _MyContext){
			MyContext = _MyContext;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View MyView = convertView;

			if ( convertView == null ) {
				//Inflate the layout
				LayoutInflater li = getLayoutInflater();
				MyView = li.inflate(R.layout.grid_item_profile, null);

				// Add The Image!!!           
				Log.i("pos", Integer.toString(position+1));
				ImageView imageView = (ImageView)MyView.findViewById(R.id.grid_item_image);
				if(Constants.profileArray.length > position +1)
					imageView.setImageResource(Constants.profileArray[position+1]);
			}
			return MyView;
		}

		public int getCount() {
			Log.i("len", Integer.toString(Constants.profileArray.length));

			return Constants.profileArray.length;
		}

		public Object getItem(int position) {
			return null;
		}

		public long getItemId(int position) {
			return 0;
		}
	}
}

