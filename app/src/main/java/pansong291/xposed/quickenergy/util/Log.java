package pansong291.xposed.quickenergy.util;

import android.app.Activity;
import android.app.AlertDialog;
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

 public static boolean forest(String s)
 {
  recordLog(s, "");
  return FileUtils.append2File(getFormatTime() + " " + s + "\n", FileUtils.getForestLogFile());
 }

 public static boolean farm(String s)
 {
  recordLog(s, "");
  return FileUtils.append2File(getFormatTime() + " " + s + "\n", FileUtils.getFarmLogFile());
 }

 public static boolean other(String s)
 {
  recordLog(s, "");
  String day = getFormatDateTime().split("-")[2];
  return FileUtils.append2File(day + " " + s + "\n", FileUtils.getOtherLogFile());
 }

 public static boolean recordLog(String str, String str2)
 {
  Log.i(TAG, str + str2);
  if(!Config.recordLog()) return false;
  return FileUtils.append2SimpleLogFile(str);
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
