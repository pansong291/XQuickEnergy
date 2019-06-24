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
  XposedBridge.log(s + ", " + s2);
 }
 
 public static void showDialog(final String str, String str2)
 {
  Log.i(TAG, str + str2);
  if(AliMobileAutoCollectEnergyUtils.h5Activity != null)
  {
   try
   {
    AliMobileAutoCollectEnergyUtils.h5Activity.runOnUiThread(new Runnable()
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
  return new AlertDialog.Builder(AliMobileAutoCollectEnergyUtils.h5Activity)
   .setTitle("XQuickEnergy")
   .setMessage("")
   .setCancelable(false)
   .setPositiveButton("OK", null)
   .create();
 }
 
}
