package com.main.service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore; 
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import uit.nfc.AsynchronousSender;
import uit.nfc.ResponseListener;
import com.main.activitys.domain.Login;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;


public class TimerService extends Service {

	private static final String TAG = "MyService";
	public static final String BROADCAST_ACTION_GAMES = "com.main.activitys.displayUpdateGames";
	public static final String BROADCAST_ACTION_GAME = "com.main.activitys.displayUpdateGame";

	public static String URL = "URL";

	public String gamesUrl;
	private String sessionId;

	public static final int INTERVAL = 10*1000;		// 10 seconds
	private ScheduledExecutorService executor;
	private SharedPreferences loginSettings;
	
	public HttpContext localContext;
	public CookieStore cookieStore;

	public ResponseListener responseListener;
	Intent callBackIntent;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d("SERVICE", "onCreate");
		loginSettings = getSharedPreferences(Login.PREFS_NAME, 0);

		responseListener = new ResponseListener() {
			@Override
			public void onResponseReceived(HttpResponse response, String message) {
				Log.d("SERVICE - MESSAGE RESPONSE", message);
				callBackIntent.putExtra("data", message);
				sendBroadcast(callBackIntent);
			}
		};
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d("SERVICE", "onDestroy");
		executor.shutdownNow();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		callBackIntent = new Intent(intent.getExtras().getString("broadcast"));	
		gamesUrl = intent.getExtras().getString(URL);
		sessionId = Login.getSessionId(loginSettings);
		
    cookieStore = new BasicCookieStore();
    localContext = new BasicHttpContext();
    localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
	
		executor = Executors.newSingleThreadScheduledExecutor();

		executor.scheduleAtFixedRate(new TimerTask() {
			public void run() {				
				pollServer();
			}
		}, 0, INTERVAL, TimeUnit.MILLISECONDS);
		Log.d("SERVICE", "onStart");
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private void pollServer(){
		Log.d("SERVICE", "Polling the server - url: " + gamesUrl);
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpGet httpGet;
			httpGet = new HttpGet(new URI(gamesUrl));
			
			httpGet.setHeader("Cookie", sessionId);
			Log.d("sessionId", sessionId);
			
    	client.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.RFC_2109);
    	
			HttpResponse response = client.execute((HttpUriRequest)httpGet, localContext);
			String tmp;
	    List<Cookie> cookies = cookieStore.getCookies();
	    
      for (int i = 0; i < cookies.size(); i++) {
          System.out.println("Local cookiie: " + cookies.get(i));
          tmp = cookies.get(i).getName() + "=" + cookies.get(i).getValue();
          Log.i("TMP:", tmp);
          Log.i("SessionId", sessionId);
          
          /* Sets new session id if it changes for any reasons */
          if(!tmp.equals(sessionId))
          	Login.storeSessionId(loginSettings, sessionId);
      }
    	
			
			String message = AsynchronousSender.readResponse(response);
			Log.d("Response", message);
			
			callBackIntent.putExtra("data", message);
			sendBroadcast(callBackIntent);
		}

		catch (ClientProtocolException e1) { e1.printStackTrace(); }
		catch (IOException e1) { e1.printStackTrace(); }
		catch (URISyntaxException e) { e.printStackTrace(); }
	}
}