package com.saimenstravelapp.activitys;
//package com.saimenstravelapp.activitys;
//
//import org.apache.http.HttpResponse;
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//import async.httprequest.ResponseListener;
//import com.main.R;
//import com.saimenstravelapp.helper.CommonFunctions;
//import android.app.Activity;
//import android.app.ProgressDialog;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.content.SharedPreferences;
//import android.content.res.Resources;
//import android.os.Bundle;
//import android.os.Handler;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.Window;
//import android.widget.AdapterView;
//import android.widget.BaseAdapter;
//import android.widget.ImageView;
//import android.widget.ListView;
//import android.widget.TextView;
//import android.widget.AdapterView.OnItemClickListener;
//import android.widget.Toast;
//
//import com.facebook.android.SessionEvents.AuthListener;
//import com.facebook.android.SessionEvents.LogoutListener;
//
//public class GetRecentPlayersActivity extends Activity implements OnItemClickListener {
//
//	private LoginButton mLoginButton;
//	private TextView mText;
//	private ImageView mUserPic;
//	private Handler mHandler;
//	Context ctx = this;
//	ProgressDialog dialog;
//
//	protected ListView friendsList;
//	protected static JSONArray jsonArray = new JSONArray();
//	protected String graph_or_fql;
//
//	IntentFilter gcmFilter;
//	private BroadcastReceiver gcmReceiver = CommonFunctions.createBroadCastReceiver(ctx, getSharedPreferences("loginInfo", 0));
//
//
//	final static int AUTHORIZE_ACTIVITY_RESULT_CODE = 0;
//	final static int PICK_EXISTING_PHOTO_RESULT_CODE = 1;
//
//	String [] permissions = { "offline_access", "publish_stream", "user_photos"};
//
//	public void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
//
//		setContentView(R.layout.friends_list);
//		mHandler = new Handler();
//
//		mLoginButton = (LoginButton) findViewById(R.id.login);
//		mText = (TextView) findViewById(R.id.txt);
//		mUserPic = (ImageView) findViewById(R.id.user_pic);
//
//		gcmFilter = new IntentFilter();
//		gcmFilter.addAction("GCM_RECEIVED_ACTION");
//
//		Resources res = getResources();
//		Utility.mFacebook = new Facebook(res.getString(R.string.APP_ID));
//		Utility.mAsyncRunner = new AsyncFacebookRunner(Utility.mFacebook);
//		graph_or_fql = "fql";
//
//		friendsList = (ListView) findViewById(R.id.facebook_friends_list);
//		friendsList.setOnItemClickListener(this);
//		friendsList.setAdapter(new FriendListAdapter(this));
//
//		mLoginButton = (LoginButton) findViewById(R.id.login);
//
//		SessionStore.restore(Utility.mFacebook, this);
//		SessionEvents.addAuthListener(new FbAPIsAuthListener());
//		SessionEvents.addLogoutListener(new FbAPIsLogoutListener());
//
//		mLoginButton.init(this, AUTHORIZE_ACTIVITY_RESULT_CODE, Utility.mFacebook, permissions);
//
//		if (Utility.mFacebook.isSessionValid()) {
//			requestUserData();
//		}
//	}
//
//	public void requestFriends(){
//		String query = "select name, current_location, uid, pic_square from user where uid in (select uid2 from friend where uid1=me()) order by name";
//		Bundle params = new Bundle();
//		params.putString("method", "fql.query");
//		params.putString("query", query);
//
//		Utility.mAsyncRunner.request(null, params,
//				new FriendsRequestListener(this));
//	}
//
//	/* (non-Javadoc)
//	 * @see android.app.Activity#onPause()
//	 */
//	@Override
//	public void onResume() {
//		super.onResume();
//		registerReceiver(gcmReceiver, gcmFilter);
//
//		if(Utility.mFacebook != null) {
//			if (!Utility.mFacebook.isSessionValid()) {
//				mText.setText("You are logged out! ");
//				mUserPic.setImageBitmap(null);
//			} else {
//				Utility.mFacebook.extendAccessTokenIfNeeded(this, null);
//			}
//		}
//	}
//
//	protected void onPause(){
//		super.onPause();
//		unregisterReceiver(gcmReceiver);
//	}
//
//	@Override
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		switch (requestCode) {
//		/*
//		 * if this is the activity result from authorization flow, do a call
//		 * back to authorizeCallback Source Tag: login_tag
//		 */
//		case AUTHORIZE_ACTIVITY_RESULT_CODE: {
//			Utility.mFacebook.authorizeCallback(requestCode, resultCode, data);
//			break;
//		}      
//		}
//	}
//
//
//	/*
//	 * callback after friends are fetched via me/friends or fql query.
//	 */
//	public class FriendsRequestListener extends BaseRequestListener {
//		GetRecentPlayersActivity ctx;
//
//		public FriendsRequestListener(GetRecentPlayersActivity ctx){
//			this.ctx = ctx;
//		}
//
//		@Override
//		public void onComplete(final String response, final Object state) {
//			//          dialog.dismiss();
//			Log.i("onComplete - response", response);
//			final GetRecentPlayersActivity context = ctx;
//			try {
//				jsonArray = new JSONArray(response);
//				mHandler.post(new Runnable() {
//					@Override
//					public void run() {
//						friendsList = (ListView) findViewById(R.id.facebook_friends_list);
//						friendsList.setOnItemClickListener(context);
//						friendsList.setAdapter(new FriendListAdapter(context));
//					}
//				});
//			} catch (JSONException e) {
//				//				Toast.makeText(ctx, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
//				e.printStackTrace();
//				return;
//			}
//		}
//
//		public void onFacebookError(FacebookError error) {
//			//          dialog.dismiss();
//			Toast.makeText(getApplicationContext(), "Facebook Error: " + error.getMessage(),
//					Toast.LENGTH_SHORT).show();
//		}
//	}
//
//	/*
//	 * The Callback for notifying the application when authorization succeeds or
//	 * fails.
//	 */
//
//	public class FbAPIsAuthListener implements AuthListener {
//
//		@Override
//		public void onAuthSucceed() {
//			requestUserData();
//			requestFriends();
//		}
//
//		@Override
//		public void onAuthFail(String error) {
//			mText.setText("Login Failed: " + error);
//		}
//	}
//
//	/*
//	 * The Callback for notifying the application when log out starts and
//	 * finishes.
//	 */
//	public class FbAPIsLogoutListener implements LogoutListener {
//		@Override
//		public void onLogoutBegin() {
//			mText.setText("Logging out...");
//		}
//
//		@Override
//		public void onLogoutFinish() {
//			mText.setText("You have logged out! ");
//			mUserPic.setImageBitmap(null);
//			if(friendsList != null){
//				FriendListAdapter adapter = (FriendListAdapter) friendsList.getAdapter();
//				adapter.removeAllItems();
//			}
//		}
//	}
//
//	/*
//	 * Request user name, and picture to show on the main screen.
//	 */
//	public void requestUserData() {
//		mText.setText("Fetching user name, profile pic...");
//		Bundle params = new Bundle();
//		params.putString("fields", "name, picture");
//		Utility.mAsyncRunner.request("me", params, new UserRequestListener());
//	}
//
//	/*
//	 * Callback for fetching current user's name, picture, uid.
//	 */
//	public class UserRequestListener extends BaseRequestListener {
//
//		@Override
//		public void onComplete(final String response, final Object state) {
//			JSONObject jsonObject;
//			try {
//				jsonObject = new JSONObject(response);
//
//				final String picURL = jsonObject.getString("picture");
//				final String name = jsonObject.getString("name");
//				Utility.userUID = jsonObject.getString("id");
//
//				mHandler.post(new Runnable() {
//					@Override
//					public void run() {
//						mText.setText("Logged in as " + name);
//						mUserPic.setImageBitmap(Utility.getBitmap(picURL));
//					}
//				});
//
//			} catch (JSONException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//
//	}
//
//	/* (non-Javadoc)
//	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
//	 */
//	@Override
//	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
//		// TODO Auto-generated method stub
//		final long friendId;
//		try{
//			friendId = jsonArray.getJSONObject(position).getLong("uid");
//
//			String name = jsonArray.getJSONObject(position).getString("name");
//			Bundle params = new Bundle();
//			params.putString("to", String.valueOf(friendId));
//			params.putString("message", getString(R.string.sendAppRequest));
//			Utility.mFacebook.dialog(GetRecentPlayersActivity.this, "apprequests", params,
//					new AppRequestsListener());
//
//		} 
//		catch (JSONException e) {
//			showToast("Error: " + e.getMessage());
//		}
//	}
//
//	public void showToast(final String msg) {
//		mHandler.post(new Runnable() {
//			@Override
//			public void run() {
//				Toast toast = Toast.makeText(GetRecentPlayersActivity.this, msg, Toast.LENGTH_LONG);
//				toast.show();
//			}
//		});
//	}
//
//	/**
//	 * Definition of the list adapter
//	 */
//	public class FriendListAdapter extends BaseAdapter {
//		private LayoutInflater mInflater;
//		GetRecentPlayersActivity friendsList;
//
//		public FriendListAdapter(GetRecentPlayersActivity friendsList) {
//			this.friendsList = friendsList;
//			if (Utility.model == null) {
//				Utility.model = new FriendsGetProfilePics();
//			}
//			Utility.model.setListener(this);
//			mInflater = LayoutInflater.from(friendsList.getBaseContext());
//		}
//
//		@Override
//		public int getCount() {
//			return jsonArray.length();
//		}
//
//		@Override
//		public Object getItem(int position) {
//			return null;
//		}
//
//		public void removeAllItems(){
//			jsonArray = new JSONArray();
//			this.notifyDataSetChanged();
//		}
//
//		@Override
//		public long getItemId(int position) {
//			return 0;
//		}
//
//		@Override
//		public View getView(int position, View convertView, ViewGroup parent) {
//			JSONObject jsonObject = null;
//			try {
//				jsonObject = jsonArray.getJSONObject(position);
//			} catch (JSONException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//			View hView = convertView;
//			if (convertView == null) {
//				hView = mInflater.inflate(R.layout.friend_item, null);
//				ViewHolder holder = new ViewHolder();
//				holder.profile_pic = (ImageView) hView.findViewById(R.id.profile_pic);
//				holder.name = (TextView) hView.findViewById(R.id.name);
//				holder.info = (TextView) hView.findViewById(R.id.info);
//				hView.setTag(holder);
//			}
//
//			ViewHolder holder = (ViewHolder) hView.getTag();
//			try {
//				if (graph_or_fql.equals("graph")) {
//					holder.profile_pic.setImageBitmap(Utility.model.getImage(
//							jsonObject.getString("id"), jsonObject.getString("picture")));
//				} else {
//					holder.profile_pic.setImageBitmap(Utility.model.getImage(
//							jsonObject.getString("uid"), jsonObject.getString("pic_square")));
//				}
//			} catch (JSONException e) {
//				holder.name.setText("");
//			}
//			try {
//				holder.name.setText(jsonObject.getString("name"));
//			} catch (JSONException e) {
//				holder.name.setText("");
//			}
//			try {
//				if (graph_or_fql.equals("graph")) {
//					holder.info.setText(jsonObject.getJSONObject("location").getString("name"));
//				} else {
//					JSONObject location = jsonObject.getJSONObject("current_location");
//					holder.info.setText(location.getString("city") + ", "
//							+ location.getString("state"));
//				}
//
//			} catch (JSONException e) {
//				holder.info.setText("");
//			}
//			return hView;
//		}
//
//	}
//
//	/*
//	 * callback for the apprequests dialog which sends an app request to user's
//	 * friends.
//	 */
//	public class AppRequestsListener extends BaseDialogListener {
//		@Override
//		public void onComplete(Bundle values) {
//			Toast toast = Toast.makeText(getApplicationContext(), "App request sent",
//					Toast.LENGTH_SHORT);
//			toast.show();
//		}
//
//		@Override
//		public void onFacebookError(FacebookError error) {
//			Toast.makeText(getApplicationContext(), "Facebook Error: " + error.getMessage(),
//					Toast.LENGTH_SHORT).show();
//		}
//
//		@Override
//		public void onCancel() {
//			//          Toast toast = Toast.makeText(getApplicationContext(), "App request cancelled",
//			//                  Toast.LENGTH_SHORT);
//			//          toast.show();
//		}
//	}
//
//	class ViewHolder {
//		ImageView profile_pic;
//		TextView name;
//		TextView info;
//	}
//}