/**
 * 
 */
package com.main.activitys;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uit.nfc.AsynchronousHttpClient;
import uit.nfc.ResponseListener;

import com.main.activitys.domain.*;
import com.main.activitys.utils.Utility;
import com.main.helper.AwesomeAdapter;
import com.main.helper.BuildHttpRequest;
import com.main.helper.CommonFunctions;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.main.*;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

/**
 * MessageActivity is a main Activity to show a ListView containing Message items
 * 
 * @author Adil Soomro
 *
 */
public class ChatActivity extends ListActivity {
	/** Called when the activity is first created. */

	ArrayList<Message> messages;
	AwesomeAdapter adapter;
	EditText text;
	
	private ResponseListener responseForChatMsg;
	public SharedPreferences loginSettings;
	public String GET_CHAT_MSG = "http://restfulserver.herokuapp.com/chat/";
	public String POST_CHAT_MSG = "http://restfulserver.herokuapp.com/chat/";
	
	private ProgressBar mProgress;
	static Random rand = new Random();	
	static String sender;
	static String opponentId;
	private String gameId;
	
	IntentFilter gcmFilter;
	private BroadcastReceiver gcmReceiver = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.chat_main);
		Bundle extras = getIntent().getExtras();

		text = (EditText) this.findViewById(R.id.text);
		
		sender = extras.getString("opponentUsername");
		
		setGameId(extras.getString("gameId"));
		opponentId = extras.getString("opponentId");
		
		Extrainfo.setNewChatMsg(getSharedPreferences(Extrainfo.PREFS_NAME, 0), this.getGameId(), false);

		Log.i("gameIchatD", getGameId());

		GET_CHAT_MSG += getGameId();
		POST_CHAT_MSG += getGameId();
		
		loginSettings = getSharedPreferences(Login.PREFS_NAME, 0);
		mProgress = (ProgressBar) findViewById(R.id.progressBar);
		messages = new ArrayList<Message>();

		gcmReceiver = CommonFunctions.createBroadCastReceiver(this, loginSettings, CommonFunctions.FROM_CHAT_ACTIVITY);
		gcmFilter = new IntentFilter();
		gcmFilter.addAction("GCM_RECEIVED_ACTION");
		
		final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("Chat with " + sender);
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
		
		responseForChatMsg = new ResponseListener() {
			@Override
			public void onResponseReceived(HttpResponse response, String message) {
				Log.d("Response", message.toString());
				mProgress.setVisibility(View.GONE);
				evaluateResponse(message);
			}
		};

		getChatMessages();
		
		// Create message array after getting chat text. then set list adapter
		adapter = new AwesomeAdapter(this, messages);
		setListAdapter(adapter);
	}
	
	protected void onResume(){
		super.onResume();
		registerReceiver(gcmReceiver, gcmFilter);
	}

	protected void onPause(){
		super.onPause();
		unregisterReceiver(gcmReceiver);
	}
	

	
	
	public void getChatMessages(){
		mProgress.setVisibility(View.VISIBLE);
		HttpGet httpGet = null;

		try {
			httpGet = new HttpGet(new URI(GET_CHAT_MSG));
			AsynchronousHttpClient a = new AsynchronousHttpClient();
			a.sendRequest(httpGet, responseForChatMsg, loginSettings);
		}
		catch (URISyntaxException e) { e.printStackTrace(); }
	}
	
	public void postMessageToServer(String msg){
		Extrainfo.setNewChatMsg(getSharedPreferences(Extrainfo.PREFS_NAME, 0), this.getGameId(), false);

		ResponseListener postMsgListener= new ResponseListener() {
			@Override
			public void onResponseReceived(HttpResponse response, String message) {
				Log.i("Response", message.toString());
				evaluatePostMsg(message);
			}
		};

		JSONObject postBody = new JSONObject();

		try {
			postBody.put("msg", msg);
			postBody.put("gameId", getGameId());
			postBody.put("opponentId", opponentId);
		}
		catch (JSONException e) { e.printStackTrace(); }

		HttpPost httpPost = BuildHttpRequest.setEntity(postBody, POST_CHAT_MSG);
		AsynchronousHttpClient a = new AsynchronousHttpClient();
		a.sendRequest(httpPost, postMsgListener, loginSettings);
	}
	
	public void evaluateResponse(String message){
		try{
			JSONArray jObj = new JSONArray(message);
			JSONObject obj = jObj.getJSONObject(0);
			int index = 0;
			
			Log.i("opponentId", opponentId);
			
			while(obj != null){
				boolean isMine = true;
				
				if(Integer.parseInt(opponentId) == obj.getInt("uid")){
					isMine = false;
				}
					
				addNewMessage(new Message(obj.getString("msg"), isMine));
				index++;
				
				if(jObj.isNull(index))
					break;
				obj = jObj.getJSONObject(index);
			}
			
		}
		catch(JSONException e){ e.printStackTrace(); }
	}
	
	public void evaluatePostMsg(String message){
		try{
			JSONObject jObj = new JSONObject(message);
			
			if(!jObj.has("success")){
				Toast.makeText(this, "Ups... The server may be down", Toast.LENGTH_LONG).show();
			}
			
		}
		catch(JSONException e){ e.printStackTrace(); }
	}
	
	public void sendMessage(View v)
	{
		String newMessage = text.getText().toString().trim();
		if(newMessage.length() > 164){
			Toast.makeText(this, "Message to long", Toast.LENGTH_LONG).show();
		}
		else if(newMessage.length() > 0)
		{
			text.setText("");
			addNewMessage(new Message(newMessage, true));
			postMessageToServer(newMessage);
//			new SendMessage().execute();
		}
	}
	private class SendMessage extends AsyncTask<Void, String, String>
	{
		@Override
		protected String doInBackground(Void... params) {
			try {
				Thread.sleep(2000); //simulate a network call
			}catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			this.publishProgress(String.format("%s started writing", sender));
			try {
				Thread.sleep(2000); //simulate a network call
			}catch (InterruptedException e) {
				e.printStackTrace();
			}
			this.publishProgress(String.format("%s has entered text", sender));
			try {
				Thread.sleep(3000);//simulate a network call
			}catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			
			return Utility.messages[rand.nextInt(Utility.messages.length-1)];
			
			
		}
		@Override
		public void onProgressUpdate(String... v) {
			
			if(messages.get(messages.size()-1).isStatusMessage)//check wether we have already added a status message
			{
				messages.get(messages.size()-1).setMessage(v[0]); //update the status for that
				adapter.notifyDataSetChanged(); 
				getListView().setSelection(messages.size()-1);
			}
			else{
				addNewMessage(new Message(true,v[0])); //add new message, if there is no existing status message
			}
		}
		@Override
		protected void onPostExecute(String text) {
			if(messages.get(messages.size()-1).isStatusMessage)//check if there is any status message, now remove it.
			{
				messages.remove(messages.size()-1);
			}
			
			addNewMessage(new Message(text, false)); // add the orignal message from server.
		}
		

	}
	public void addNewMessage(Message m)
	{
		messages.add(m);
		adapter.notifyDataSetChanged();
		getListView().setSelection(messages.size()-1);
	}

	/**
	 * @return the gameId
	 */
	public String getGameId() {
		return gameId;
	}

	/**
	 * @param gameId the gameId to set
	 */
	public void setGameId(String gameId) {
		this.gameId = gameId;
	}
}
