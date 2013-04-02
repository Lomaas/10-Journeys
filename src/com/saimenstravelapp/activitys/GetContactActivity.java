/**
 * 
 */
package com.saimenstravelapp.activitys;

import java.util.ArrayList;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONException;
import org.json.JSONObject;

import com.saimenstravelapp.*;
import com.saimenstravelapp.activitys.domain.Contact;
import com.saimenstravelapp.activitys.domain.Login;
import com.saimenstravelapp.helper.*;
import com.saimenstravelapp.service.*;

import android.app.Activity;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import async.httprequest.AsynchronousHttpClient;
import async.httprequest.ResponseListener;


public class GetContactActivity extends ListActivity implements AsyncTaskDelegate<Contact> {
	public ArrayList<Contact> listItems = new ArrayList<Contact>();
	private ArrayAdapter<String> adapter;
	
	private ProgressDialogClass progDialog;
	private ResponseListener responseListener;
	Context ctx = this;
	IntentFilter gcmFilter;
	private BroadcastReceiver gcmReceiver = null;
	
	private String getNewGameUrl = "http://restfulserver.herokuapp.com/game/new_email";
	public SharedPreferences loginSettings;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.invite_contact);

		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
		setListAdapter(adapter);
		loginSettings = getSharedPreferences(Login.PREFS_NAME, 0);
		gcmReceiver = CommonFunctions.createBroadCastReceiver(ctx, loginSettings, CommonFunctions.FROM_STANDARD_ACTIVITY);

		gcmFilter = new IntentFilter();
		gcmFilter.addAction("GCM_RECEIVED_ACTION");

		responseListener = new ResponseListener() {
			@Override
			public void onResponseReceived(HttpResponse response, String message) {
				Log.i("Response", response.toString());
				progDialog.dissMissProgressDialog();
				evaluateResponse(message);
			}
		};
		
		GetContactsTask getContactsTask = new GetContactsTask(this, this);
		getContactsTask.execute();
	}
	
	protected void onResume(){
		super.onResume();
		registerReceiver(gcmReceiver, gcmFilter);

		loginSettings = getSharedPreferences(Login.PREFS_NAME, 0);
		if(Login.isSessionExpired(loginSettings)){
			Intent loginIntent = new Intent(this, LoginActivity.class);
			startActivity(loginIntent);
			finish();
		}
	}
	
	protected void onPause(){
		super.onPause();
		unregisterReceiver(gcmReceiver);
	}
	
	@Override
	protected void onListItemClick(ListView list, View view, int position, long id) {
		super.onListItemClick(list, view, position, id);
		Log.d("onclick", "click");
		sendEmail(listItems.get(position).getEmail());
	}

	/* (non-Javadoc)
	 * @see com.saimenstravelapp.service.AsyncTaskDelegate#publishItem(java.lang.Object)
	 */
	@Override
	public void publishItem(Contact object) {
		listItems.add(object);
		adapter.add(object.getName());
		adapter.notifyDataSetChanged();
	}

	/* (non-Javadoc)
	 * @see com.saimenstravelapp.service.AsyncTaskDelegate#didFailWithError(java.lang.String)
	 */
	@Override
	public void didFailWithError(String errorMessage) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.saimenstravelapp.service.AsyncTaskDelegate#didFinishProsess(java.lang.String)
	 */
	@Override
	public void didFinishProsess(String message) {
		// TODO Auto-generated method stub
		
	}
	
	private void sendEmail(String eMail){
		JSONObject postBody = new JSONObject();

		try {
			postBody.put("email", eMail);
			postBody.put("type", InviteFriendTabActivity.type);
		}

		catch (JSONException e) { e.printStackTrace(); }
		
		HttpPost httpPost = BuildHttpRequest.setEntity(postBody, getNewGameUrl);

		AsynchronousHttpClient a = new AsynchronousHttpClient();
		a.sendRequest(httpPost, responseListener, loginSettings);
		progDialog = new 
				ProgressDialogClass(this, 
						"Finding contact", 
						"Searching for contact, please wait...",
						15000);

		progDialog.run();
	}
	
	private void evaluateResponse(String message){
		Log.i("evaluateResponse", message);

		try {
			JSONObject response = new JSONObject(message);

			if(response.has("error")){
				new Alert("Not found", response.getString("error"), this);
			}
			else{
				Intent data = new Intent();
				
				if (getParent() == null) {
				    setResult(Activity.RESULT_OK, data);
				} else {
				    getParent().setResult(Activity.RESULT_OK, data);
				}
				Log.i("evaluateResponse", message);
				finish();
			}
		}
		catch (JSONException e) { e.printStackTrace(); }
	}
}
