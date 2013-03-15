package async.httprequest;

import org.apache.http.HttpResponse;
 
public interface ResponseListener {
 
	public void onResponseReceived(HttpResponse response, String message);

}