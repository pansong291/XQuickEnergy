package pansong291.xposed.quickenergy;

import de.robv.android.xposed.XposedBridge;

public class Log
{
 
 public static void i(String s, String s2)
 {
  XposedBridge.log(s + ", " + s2);
 }
 
}
