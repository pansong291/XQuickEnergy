package pansong291.xposed.quickenergy.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import pansong291.xposed.quickenergy.R;
import pansong291.xposed.quickenergy.util.FileUtils;
import pansong291.xposed.quickenergy.util.Statistics;

public class MainActivity extends Activity
{
 TextView tv_statistics;

 @Override
 protected void onCreate(Bundle savedInstanceState)
 {
  super.onCreate(savedInstanceState);
  setContentView(R.layout.activity_main);
  setModuleActive(false);

  tv_statistics = (TextView) findViewById(R.id.tv_statistics);
 }

 @Override
 protected void onResume()
 {
  super.onResume();
  tv_statistics.setText(Statistics.getText());
 }

 public void onClick(View v)
 {
  String data = "file://";
  switch(v.getId())
  {
   case R.id.btn_forest_log:
    data += FileUtils.getForestLogFile().getAbsolutePath();
    break;

   case R.id.btn_farm_log:
    data += FileUtils.getFarmLogFile().getAbsolutePath();
    break;

   case R.id.btn_other_log:
    data += FileUtils.getOtherLogFile().getAbsolutePath();
    break;

   case R.id.btn_help:
    data = "https://github.com/pansong291/XQuickEnergy/wiki";
    break;
  }
  Intent it = new Intent(this, HtmlViewerActivity.class);
  it.setData(Uri.parse(data));
  startActivity(it);
 }

 @Override
 public boolean onCreateOptionsMenu(Menu menu)
 {
  int state = getPackageManager()
   .getComponentEnabledSetting(new ComponentName(this, getClass().getCanonicalName() + "Alias"));
  menu.add(0, 1, 0, "Hide the application icon")
   .setCheckable(true)
   .setChecked(state > PackageManager.COMPONENT_ENABLED_STATE_ENABLED);
  menu.add(0, 2, 0, "Export the statistic file");
  menu.add(0, 3, 0, "Import the statistic file");
  menu.add(0, 4, 0, "Settings");
  return super.onCreateOptionsMenu(menu);
 }

 @Override
 public boolean onOptionsItemSelected(MenuItem item)
 {
  switch(item.getItemId())
  {
   case 1:
    int state = item.isChecked() ? PackageManager.COMPONENT_ENABLED_STATE_DEFAULT: PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
    getPackageManager()
     .setComponentEnabledSetting(new ComponentName(this, getClass().getCanonicalName() + "Alias"), state, PackageManager.DONT_KILL_APP);
    item.setChecked(!item.isChecked());
    break;

   case 2:
    if(FileUtils.copyTo(FileUtils.getStatisticsFile(), FileUtils.getExportedStatisticsFile()))
     Toast.makeText(this, "Export success", 0).show();
    break;

   case 3:
    if(FileUtils.copyTo(FileUtils.getExportedStatisticsFile(), FileUtils.getStatisticsFile()))
    {
     tv_statistics.setText(Statistics.getText());
     Toast.makeText(this, "Import success", 0).show();
    }
    break;

   case 4:
    startActivity(new Intent(this, SettingsActivity.class));
    break;
  }
  return super.onOptionsItemSelected(item);
 }

 private void setModuleActive(boolean b)
 {
  b = b || isExpModuleActive(this);
  TextView tv_unactive = (TextView) findViewById(R.id.tv_unactive);
  tv_unactive.setVisibility(b ? View.GONE : View.VISIBLE);
 }

 private static boolean isExpModuleActive(Context context)
 {
  boolean isExp = false;
  if(context == null)
   throw new IllegalArgumentException("context must not be null!!");

  try
  {
   ContentResolver contentResolver = context.getContentResolver();
   Uri uri = Uri.parse("content://me.weishu.exposed.CP/");
   Bundle result = null;
   try
   {
    result = contentResolver.call(uri, "active", null, null);
   }catch(RuntimeException e)
   {
    // TaiChi is killed, try invoke
    try
    {
     Intent intent = new Intent("me.weishu.exp.ACTION_ACTIVE");
     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
     context.startActivity(intent);
    }catch(Throwable e1)
    {
     return false;
    }
   }
   if(result == null)
    result = contentResolver.call(uri, "active", null, null);

   if(result == null)
    return false;
   isExp = result.getBoolean("active", false);
  }catch(Throwable ignored)
  {
  }
  return isExp;
 }

}
