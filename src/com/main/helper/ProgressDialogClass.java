package com.main.helper;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Looper;
import android.util.Log;

/**
 * Helper function class.
 */
public class ProgressDialogClass implements Runnable {
	public static ProgressDialog progDialog;
	private Context callingActivity;
	private String title;
	private String message;
	private boolean dismissed = false;

	/**
	 * Constructor
	 */
	public ProgressDialogClass(Context c, String title, String message){
		this.callingActivity = c;
		this.title = title;
		this.message = message;
	}

	/**
	 * Dismiss progress dialog.
	 * @param progressDialog the progress dialog
	 */

	public void dissMissProgressDialog(){
		if(notDismissed()){
			progDialog.dismiss();
			this.dismissed = true;
		}
	}

	/* 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		progDialog = ProgressDialog.show(callingActivity, 
				title, message,
				true);

		Log.d("progressDialog", "thread sleep");
		new Thread(new Runnable() {
			public void run() {
				Looper.prepare();

				try {
					Thread.sleep(15000);
				}
				catch (InterruptedException e) { e.printStackTrace(); }

				if(notDismissed()){
					dissMissProgressDialog();
					new Alert("Internet error", "Didn't get contact with the server. Please check that you have internett access", callingActivity);
				}
		    Looper.loop();
			}
		}).start();
		
		Log.d("progressDialog", "after sleep");
	}


	private boolean notDismissed(){
		return !this.dismissed;
	}
}
