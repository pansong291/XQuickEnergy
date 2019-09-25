package pansong291.xposed.quickenergy.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.widget.Toast;
import de.robv.android.xposed.XposedBridge;
import java.text.SimpleDateFormat;
import java.util.Date;
import pansong291.xposed.quickenergy.hook.RpcCall;

public class Log
{
 private static final String TAG = Log.class.getCanonicalName();
 private static SimpleDateFormat sdf;

 public static void i(String tag, String s)
 {
  StringBuilder sb = new StringBuilder(tag + ", " + s);
  try
  {
   for(int i = 0; i < sb.length(); i += 2000)
   {
    if(sb.length() < i + 2000)
     XposedBridge.log(sb.substring(i, sb.length()));
    else
     XposedBridge.log(sb.substring(i, i + 2000));
   }
  }catch(Throwable t)
  {
   // when hooking self, this XposedBridge.class will
   // not be found, ignore it.
   android.util.Log.i(tag, s);
  }
  FileUtils.append2RuntimeLogFile(sb.toString());
 }

 public static void printStackTrace(String tag, Throwable t)
 {
  Log.i(tag, android.util.Log.getStackTraceString(t));
 }

 public static void showToastIgnoreConfig(final String str, String str2)
 {
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
       try
       {
        Toast.makeText(activity, str, 1).show();
       }catch(Throwable t)
       {
        Log.i(TAG, "showToast err:");
       }
      }
     });
   }catch(Throwable t)
   {
    Log.i(TAG, "showToast err:");
    Log.printStackTrace(TAG, t);
   }
  }
 }

 public static boolean recordLog(String str, String str2)
 {
  return recordLog(str, str2, true);
 }

 private static boolean recordLog(String str, String str2, boolean log)
 {
  if(log) Log.i(TAG, str + str2);
  if(!Config.recordLog()) return false;
  return FileUtils.append2LogFile(str);
 }

 public static String getFormatDateTime()
 {
  if(sdf == null) sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  return sdf.format(new Date());
 }

 public static String getFormatDate()
 {
  return getFormatDateTime().split(" ")[0];
 }

 public static String getFormatTime()
 {
  return getFormatDateTime().split(" ")[1];
 }

}
