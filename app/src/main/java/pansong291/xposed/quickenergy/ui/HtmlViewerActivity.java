package pansong291.xposed.quickenergy.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class HtmlViewerActivity extends Activity
{
 private MyWebView mWebView;
 private Uri uri;

 @Override
 protected void onCreate(Bundle savedInstanceState)
 {
  super.onCreate(savedInstanceState);
  mWebView = new MyWebView(this);
  setContentView(mWebView);
  uri = getIntent().getData();
  mWebView.loadUrl(uri.toString());
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
  menu.add(0, 2, 0, "滚动到顶部");
  menu.add(0, 3, 0, "滚动到底部");
  return super.onCreateOptionsMenu(menu);
 }

 @Override
 public boolean onOptionsItemSelected(MenuItem item)
 {
  switch(item.getItemId())
  {
   case 2:
    mWebView.scrollTo(0, 0);
    break;

   case 3:
    mWebView.scrollToBottom();
    break;
  }
  return true;
 }

}
