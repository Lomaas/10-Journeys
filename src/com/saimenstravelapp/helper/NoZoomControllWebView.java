/**
 * 
 */
package com.saimenstravelapp.helper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;
import android.widget.ZoomButtonsController;

/**
 * @author Simen
 *
 */
public class NoZoomControllWebView extends WebView {

  private ZoomButtonsController zoom_controll = null;

  public NoZoomControllWebView(Context context) {
      super(context);
      disableControls();
  }

  public NoZoomControllWebView(Context context, AttributeSet attrs, int defStyle) {
      super(context, attrs, defStyle);
      disableControls();
  }

  public NoZoomControllWebView(Context context, AttributeSet attrs) {
      super(context, attrs);
      disableControls();
  }

  /**
   * Disable the controls
   */

	private void disableControls(){
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
          // Use the API 11+ calls to disable the controls
          this.getSettings().setBuiltInZoomControls(true);
          this.getSettings().setDisplayZoomControls(false);
      } else {
          // Use the reflection magic to make it work on earlier APIs
          getControlls();
      }
  }

  /**
   * This is where the magic happens :D
   */
  private void getControlls() {
      try {
          Class webview = Class.forName("android.webkit.WebView");
          Method method = webview.getMethod("getZoomButtonsController");
          zoom_controll = (ZoomButtonsController) method.invoke(this, null);
      } catch (ClassNotFoundException e) {
          e.printStackTrace();
      } catch (SecurityException e) {
          e.printStackTrace();
      } catch (NoSuchMethodException e) {
          e.printStackTrace();
      } catch (IllegalArgumentException e) {
          e.printStackTrace();
      } catch (IllegalAccessException e) {
          e.printStackTrace();
      } catch (InvocationTargetException e) {
          e.printStackTrace();
      }
  }

  @Override
  public boolean onTouchEvent(MotionEvent ev) {
      super.onTouchEvent(ev);
      if (zoom_controll != null){
          // Hide the controlls AFTER they where made visible by the default implementation.
          zoom_controll.setVisible(false);
      }
      return true;
  }
}
