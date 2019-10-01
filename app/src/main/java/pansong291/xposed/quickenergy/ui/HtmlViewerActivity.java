package pansong291.xposed.quickenergy.ui;

import android.app.Activity;
import android.webkit.WebView;
import android.os.Bundle;
import android.content.Intent;

public class HtmlViewerActivity extends Activity
{
 private WebView webView;

 @Override
 protected void onCreate(Bundle savedInstanceState)
 {
  super.onCreate(savedInstanceState);
  webView = new WebView(this);
  setContentView(webView);
  webView.getSettings().setSupportZoom(true);
  webView.getSettings().setBuiltInZoomControls(true);
  webView.getSettings().setDisplayZoomControls(false);
  webView.getSettings().setUseWideViewPort(true);
  webView.getSettings().setAllowFileAccess(true);
  webView.loadUrl(getIntent().getData().toString());
 }

}
