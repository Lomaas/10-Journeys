package uit.nfc;

import org.apache.http.HttpRequest;

import com.main.activitys.domain.Login;

import android.content.SharedPreferences;
import android.os.Handler;

public class AsynchronousHttpClient {
	
	public void sendRequest(final HttpRequest request, ResponseListener callback, SharedPreferences loginSettings) {
		(new AsynchronousSender(request, new Handler(), new CallbackWrapper(callback), loginSettings)).start();
	}
	
}
