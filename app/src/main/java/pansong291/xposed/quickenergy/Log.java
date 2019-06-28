package pansong291.xposed.quickenergy;

import android.app.Activity;
import android.app.AlertDialog;
import de.robv.android.xposed.XposedBridge;

public class Log
{
 private static String TAG = Log.class.getCanonicalName();
 private static AlertDialog dlg;
 private static StringBuffer sb;
 
 public static void i(String s, String s2)
 {
  StringBuilder sb = new StringBuilder(s + ", " + s2);
  for(int i = 0; i < sb.length(); i += 2000)
  {
   if(sb.length() < i + 2000)
    XposedBridge.log(sb.substring(i, sb.length()));
   else
    XposedBridge.log(sb.substring(i, i + 2000));
  }
 }
 
 public static void showDialog(final String str, String str2)
 {
  Log.i(TAG, str + str2);
  Activity activity = AliMobileAutoCollectEnergyUtils.h5Activity;
  if(activity != null)
  {
   try
   {
    activity.runOnUiThread(new Runnable()
     {
      public void run()
      {
       if(sb == null)
        sb = new StringBuffer();
       if(dlg == null)
        dlg = createNewDialog();
       if(!dlg.isShowing())
        try{
         dlg.show();
        }catch(Exception e)
        {
         Log.i(TAG, "Dialog show error: "+e.getMessage());
         dlg = createNewDialog();
         dlg.show();
         sb.delete(0, sb.length());
        }
       sb.append(str).append('\n');
       dlg.setMessage(sb.toString());
      }
     });
   }catch(Exception e)
   {
    Log.i(TAG, "showDialog err: " + e.getMessage());
   }
  }
 }

 private static AlertDialog createNewDialog()
 {
  Activity activity = AliMobileAutoCollectEnergyUtils.h5Activity;
  return new AlertDialog.Builder(activity)
   .setTitle("XQuickEnergy")
   .setMessage("")
   .setPositiveButton("OK", null)
   .create();
 }
 
}
