package pansong291.xposed.quickenergy;

import android.app.Activity;
import android.app.AlertDialog;
import android.icu.text.SimpleDateFormat;
import android.widget.Toast;
import de.robv.android.xposed.XposedBridge;
import java.util.Date;

public class Log
{
 private static final String TAG = Log.class.getCanonicalName();
 private static AlertDialog dlg;
 private static StringBuffer sb;
 /*
  Intent it = new Intent();
  it.setAction(it.ACTION_VIEW);
  it.addFlags(it.FLAG_INCLUDE_STOPPED_PACKAGES);
  it.addFlags(it.FLAG_RECEIVER_FOREGROUND);
  it.setData(Uri.parse("alipays://platformapi/startapp?appId=60000002"));
 */
 public static void i(String tag, String s)
 {
  StringBuilder sb = new StringBuilder(tag + ", " + s);
  for(int i = 0; i < sb.length(); i += 2000)
  {
   if(sb.length() < i + 2000)
    XposedBridge.log(sb.substring(i, sb.length()));
   else
    XposedBridge.log(sb.substring(i, i + 2000));
  }
 }
 
 public static void printStackTrace(String tag, Throwable t)
 {
  Log.i(tag, android.util.Log.getStackTraceString(t));
 }
 
 public static void showDialogOrToast(String str, String str2)
 {
  showDialog(str, str2);
  showToast(str, str2);
 }
 
 public static void showDialogAndRecordLog(String str, String str2)
 {
  showDialog(str, str2);
  recordLog(str, str2);
 }
 
 public static void showToast(final String str, String str2)
 {
  if(Config.showMode() != Config.ShowMode.TOAST)
   return;
  Log.i(TAG, str + str2);
  final Activity activity = RpcCall.h5Activity;
  if(activity != null)
  {
   try
   {
    activity.runOnUiThread(new Runnable()
     {
      public void run()
      {
       Toast.makeText(activity, str, 1).show();
      }
     });
   }catch(Exception e)
   {
    Log.i(TAG, "showToast err:");
    Log.printStackTrace(TAG, e);
   }
  }
 }
 
 public static boolean recordLog(String str, String str2)
 {
  if(!Config.recordLog())
   return false;
  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss  ");
  return FileUtils.append2LogFile(sdf.format(new Date()) + str + str2);
 }
 
 public static void showDialog(final String str, String str2)
 {
  if(Config.showMode() != Config.ShowMode.DIALOG)
   return;
  Log.i(TAG, str + str2);
  Activity activity = RpcCall.h5Activity;
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
         Log.i(TAG, "Dialog show error:");
         Log.printStackTrace(TAG, e);
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
    Log.i(TAG, "showDialog err:");
    Log.printStackTrace(TAG, e);
   }
  }
 }

 private static AlertDialog createNewDialog()
 {
  Activity activity = RpcCall.h5Activity;
  return new AlertDialog.Builder(activity)
   .setTitle("XQuickEnergy")
   .setMessage("")
   .setPositiveButton("OK", null)
   .create();
 }
 
}
