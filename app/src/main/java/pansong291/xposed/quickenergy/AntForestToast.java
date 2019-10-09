package pansong291.xposed.quickenergy;

import android.content.Context;
import android.widget.Toast;
import pansong291.xposed.quickenergy.util.Config;
import pansong291.xposed.quickenergy.util.Log;
import pansong291.xposed.quickenergy.hook.XposedHook;

public class AntForestToast
{
 private static final String TAG = AntForestToast.class.getCanonicalName();
 public static Context context;
 private static Toast toast;

 public static void show(CharSequence cs)
 {
  Log.recordLog(cs.toString(), "");
  try
  {
   if(context != null && Config.showToast())
   {
    XposedHook.handler.post(
     new Runnable()
     {
      CharSequence cs;

      public Runnable setData(CharSequence c)
      {
       cs = c;
       return this;
      }

      @Override
      public void run()
      {
       try
       {
        if(toast == null)
         toast = Toast.makeText(context, "toast", 1);
        toast.setText(cs);
        toast.show();
       }catch(Throwable t)
       {
        Log.i(TAG, "show.run err:");
        Log.printStackTrace(TAG, t);
       }
      }
     }.setData(cs));
   }
  }catch(Throwable t)
  {
   Log.i(TAG, "show err:");
   Log.printStackTrace(TAG, t);
  }
 }
}
