package pansong291.xposed.quickenergy;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;

public class MainActivity extends Activity
{

 @Override
 protected void onCreate(Bundle savedInstanceState)
 {
  super.onCreate(savedInstanceState);
  Intent it = new Intent(Intent.ACTION_VIEW);
  it.setData(Uri.parse("https://github.com/pansong291/XQuickEnergy/wiki"));
  startActivity(it);
  finish();
 }
 
}
