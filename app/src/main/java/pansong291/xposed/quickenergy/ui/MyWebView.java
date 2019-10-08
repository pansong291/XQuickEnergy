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
     post(
      new Runnable()
      {
       @Override
       public void run()
       {
        if(Thread.interrupted()) return;
        scrollToBottom();
       }
      });
    }
   });
 }

 public void scrollToBottom()
 {
  scrollTo(0, computeVerticalScrollRange());
 }
 
}
