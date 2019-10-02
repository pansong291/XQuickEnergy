package pansong291.xposed.quickenergy.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.webkit.WebView;

public class HtmlViewerActivity extends Activity
{
 private WebView webView;
 private Uri uri;

 @Override
 protected void onCreate(Bundle savedInstanceState)
 {
  super.onCreate(savedInstanceState);
  webView = new WebView(this);
  setContentView(webView);
  uri = getIntent().getData();
  webView.getSettings().setSupportZoom(true);
  webView.getSettings().setBuiltInZoomControls(true);
  webView.getSettings().setDisplayZoomControls(false);
  webView.getSettings().setUseWideViewPort(true);
  webView.getSettings().setAllowFileAccess(true);
  webView.loadUrl(uri.toString());
  setTitle(uri.getLastPathSegment());
 }

 @Override
 public boolean onCreateOptionsMenu(Menu menu)
 {
  Intent it = new Intent(Intent.ACTION_VIEW);
  it.addCategory(Intent.CATEGORY_DEFAULT);  
  it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
  it.setDataAndType(uri, "text/html");
  menu.add(0, 1, 0, "使用浏览器打开")
   .setIntent(Intent.createChooser(it, "选择浏览器"));
  return super.onCreateOptionsMenu(menu);
 }

}
