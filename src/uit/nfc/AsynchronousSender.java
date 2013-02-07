package uit.nfc;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import com.main.activitys.domain.Login;

import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
 
public class AsynchronousSender extends Thread {
 
	private static final DefaultHttpClient httpClient =
		new DefaultHttpClient();
 
	private HttpRequest request;
	private Handler handler;
	private CallbackWrapper wrapper;
	
	public SharedPreferences loginSettings;
	public HttpContext localContext;
	public CookieStore cookieStore;
	
	
	protected AsynchronousSender(HttpRequest request,
			Handler handler, CallbackWrapper wrapper, SharedPreferences loginSettings) {
		this.request = request;
		this.handler = handler;
		this.wrapper = wrapper;
		this.loginSettings = loginSettings;
		
    cookieStore = new BasicCookieStore();
    localContext = new BasicHttpContext();
    localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
	}
 
	public void run() {
		try {
			Log.d("HTTPCLIENT", "requesting!");

			String sessionId = Login.getSessionId(loginSettings);
			Log.d("current sessionId", sessionId);

			if(!sessionId.equals("error")){
				request.setHeader("Cookie", sessionId);
			}
			
			HttpResponse response = null;
			response = getClient().execute((HttpUriRequest) request, localContext);

			String message = readResponse(response);
	    
			if(message.equals("Session expired")){
				Log.d("SESSION EXPIRED", "NEED TO LOG IN AGAIN!!!");
				
				// Re-login or redirect to login screen if no password match!
				
				
			}
			List<Cookie> cookies = cookieStore.getCookies();
	    String tmp;
	    
      for (int i = 0; i < cookies.size(); i++) {
          System.out.println("Local cookiee: " + cookies.get(i));
          tmp = cookies.get(i).getName() + "=" + cookies.get(i).getValue();
          Log.i("TMP:", tmp);
          Log.i("SessionId", sessionId);

          /* Sets new session id if it changes for any reasons */
          if(!tmp.equals(sessionId)){
    				Log.d("NEW sessionId", tmp);
          	Login.storeSessionId(loginSettings, tmp);
          }
      }
			
			wrapper.setResponse(response, message);
			handler.post(wrapper);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
 
	private HttpClient getClient() {
		return httpClient;
	}
	
	public static String readResponse(HttpResponse response) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			
			String returnValue = "";
			String line = "";
			while ((line = br.readLine()) != null) {
				returnValue += line;
			}
			br.close();
			
			return returnValue;
		} catch (IllegalStateException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
 
}