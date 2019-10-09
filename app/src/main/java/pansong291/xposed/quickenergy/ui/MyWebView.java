package pansong291.xposed.quickenergy.ui;

import android.content.Context;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MyWebView extends WebView
{
 public MyWebView(Context c)
 {
  super(c);
  getSettings().setSupportZoom(true);
  getSettings().setBuiltInZoomControls(true);
  getSettings().setDisplayZoomControls(false);
  getSettings().setUseWideViewPort(false);
  getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
  getSettings().setAllowFileAccess(true);
  setWebViewClient(
   new WebViewClient()
   {
    public void onPageFinished(WebView view, String url)
    {
     if(url.endsWith(".log"))
     postDelayed(
      new Runnable()
      {
       @Override
       public void run()
       {
        if(Thread.interrupted()) return;
        if(getContentHeight() == 0)
         postDelayed(this, 100);
        else
         scrollToBottom();
       }
      }, 500);
    }
   });
 }

 public void scrollToBottom()
 {
  scrollTo(0, computeVerticalScrollRange());
 }

}
