package pansong291.xposed.quickenergy.hook;

import android.app.Activity;
import android.app.Service;
import android.os.Handler;
import android.os.PowerManager;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import java.util.Map;
import pansong291.xposed.quickenergy.AntFarm;
import pansong291.xposed.quickenergy.AntForest;
import pansong291.xposed.quickenergy.AntForestNotification;
import pansong291.xposed.quickenergy.AntMember;
import pansong291.xposed.quickenergy.hook.ClassMember;
import pansong291.xposed.quickenergy.ui.MainActivity;
import pansong291.xposed.quickenergy.util.Config;
import pansong291.xposed.quickenergy.util.Log;
import pansong291.xposed.quickenergy.util.Statistics;

public class XposedHook implements IXposedHookLoadPackage
{
 private static final String TAG = XposedHook.class.getCanonicalName();
 private static PowerManager.WakeLock wakeLock;
 private static Handler handler;
 private static Runnable runnable;

 @Override
 public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable
 {
  if("pansong291.xposed.quickenergy".equals(lpparam.packageName))
  {
   XposedHelpers.findAndHookMethod(MainActivity.class.getName(), lpparam.classLoader, "setModuleActive", boolean.class, new XC_MethodHook()
    {
     @Override
     protected void beforeHookedMethod(MethodHookParam param) throws Throwable
     {
      param.args[0] = true;
     }
    });
  }

  if(ClassMember.com_eg_android_AlipayGphone.equals(lpparam.packageName))
  {
   Log.i(TAG, lpparam.packageName);
   //hookSecurity(lpparam);
   hookLauncherService(lpparam.classLoader);
   hookRpcCall(lpparam.classLoader);
  }
 }

 private void hookSecurity(XC_LoadPackage.LoadPackageParam lpparam)
 {
  try
  {
   Class<?> loadClass = lpparam.classLoader.loadClass("com.alipay.mobile.base.security.CI");
   if(loadClass != null)
   {
    XposedHelpers.findAndHookMethod(loadClass, "a", loadClass, Activity.class, new XC_MethodReplacement()
     {
      @Override
      protected Object replaceHookedMethod(MethodHookParam param) throws Throwable
      {
       return null;
      }
     });
    XposedHelpers.findAndHookMethod(loadClass, "a", String.class, String.class, String.class, new XC_MethodHook()
     {
      @Override
      protected void afterHookedMethod(MethodHookParam param) throws Throwable
      {
       param.setResult(null);
      }
     });
   }
  }catch(Throwable t)
  {
   Log.i(TAG, "hookSecurity err:");
   Log.printStackTrace(TAG, t);
  }
 }

 private void hookLauncherService(final ClassLoader loader)
 {
  try
  {
   XposedHelpers.findAndHookMethod(ClassMember.com_alipay_android_launcher_service_LauncherService, loader, ClassMember.onCreate, new XC_MethodHook()
    {
     @Override
     protected void afterHookedMethod(MethodHookParam param) throws Throwable
     {
      Service service = (Service) param.thisObject;
      PowerManager pm = (PowerManager) service.getSystemService(service.POWER_SERVICE);
      wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, service.getClass().getName());
      wakeLock.acquire();
      if(handler == null) handler = new Handler();
      if(runnable == null) runnable = new Runnable()
       {
        Service service;

        public Runnable setData(Service s)
        {
         service = s;
         return this;
        }

        @Override
        public void run()
        {
         Statistics.resetToday();
         AntForest.checkEnergyRanking(loader);
         AntFarm.start(loader);
         AntMember.receivePoint(loader);
         if(Config.collectEnergy() || Config.enableFarm())
          handler.postDelayed(this, Config.timeInterval());
         else AntForestNotification.stop(service, false);
        }
       }.setData(service);
      if(Config.collectEnergy() || Config.enableFarm())
      {
       AntForestNotification.start(service);
       handler.post(runnable);
       Log.i(TAG, "task start. interval=" + Config.timeInterval());
      }
     }
    });
   Log.i(TAG, "hook " + ClassMember.onCreate + " successfully");
  }catch(Throwable t)
  {
   Log.i(TAG, "hook " + ClassMember.onCreate + " err:");
   Log.printStackTrace(TAG, t);
  }

  try
  {
   XposedHelpers.findAndHookMethod(ClassMember.com_alipay_android_launcher_service_LauncherService, loader, ClassMember.onDestroy, new XC_MethodHook()
    {
     @Override
     protected void afterHookedMethod(MethodHookParam param) throws Throwable
     {
      if(wakeLock != null)
      {
       wakeLock.release();
       wakeLock = null;
      }
      Service service = (Service) param.thisObject;
      AntForestNotification.setContentText("支付宝前台服务被销毁");
      Log.recordLog("支付宝前台服务被销毁", "");
      handler.removeCallbacks(runnable);
      AntForestNotification.stop(service, false);
     }
    });
   Log.i(TAG, "hook " + ClassMember.onDestroy + " successfully");
  }catch(Throwable t)
  {
   Log.i(TAG, "hook " + ClassMember.onDestroy + " err:");
   Log.printStackTrace(TAG, t);
  }
 }

 private void hookRpcCall(final ClassLoader loader)
 {
  Class<?> clazz = null;
//  try
//  {
//   clazz = loader.loadClass(ClassMember.com_alipay_mobile_nebulacore_ui_H5FragmentManager);
//   Class<?> h5FragmentClazz = loader.loadClass(ClassMember.com_alipay_mobile_nebulacore_ui_H5Fragment);
//   XposedHelpers.findAndHookMethod(clazz, ClassMember.pushFragment, h5FragmentClazz,
//    boolean.class, Bundle.class, boolean.class, boolean.class, new XC_MethodHook()
//    {
//     @Override
//     protected void afterHookedMethod(MethodHookParam param) throws Throwable
//     {
//      Log.i(TAG, "cur fragment: " + param.args[0]);
//      if(RpcCall.curH5Fragment == null && param.args[0] != null)
//       RpcCall.curH5Fragment = param.args[0];
//     }
//    });
//   Log.i(TAG, "hook " + ClassMember.pushFragment + " successfully");
//  }catch(Throwable t)
//  {
//   Log.i(TAG, "hook " + ClassMember.pushFragment + " err:");
//   Log.printStackTrace(TAG, t);
//  }

  try
  {
   clazz = loader.loadClass(ClassMember.com_alipay_mobile_nebulaappproxy_api_rpc_H5AppRpcUpdate);
   Class<?> H5PageClazz = loader.loadClass(ClassMember.com_alipay_mobile_h5container_api_H5Page);
   XposedHelpers.findAndHookMethod(clazz, ClassMember.matchVersion, H5PageClazz, Map.class, String.class, new XC_MethodHook()
    {
     @Override
     protected void afterHookedMethod(MethodHookParam param) throws Throwable
     {
      param.setResult(false);
     }
    });
   Log.i(TAG, "hook " + ClassMember.matchVersion + " successfully");
  }catch(Throwable t)
  {
   Log.i(TAG, "hook " + ClassMember.matchVersion + " err:");
   Log.printStackTrace(TAG, t);
  }

  try
  {
   clazz = loader.loadClass(ClassMember.com_alipay_mobile_nebulacore_ui_H5Activity);
   XposedHelpers.findAndHookMethod(clazz, ClassMember.onResume, new XC_MethodHook()
    {
     @Override
     protected void afterHookedMethod(MethodHookParam param) throws Throwable
     {
      Log.i(TAG, "cur activity: " + param.thisObject);
      Activity act = (Activity)param.thisObject;
      if(RpcCall.h5Activity != act)
      {
       Config.shouldReloadConfig = true;
      }
      RpcCall.h5Activity = act;
     }
    });
   Log.i(TAG, "hook " + ClassMember.onResume + " successfully");
  }catch(Throwable t)
  {
   Log.i(TAG, "hook " + ClassMember.onResume + " err:");
   Log.printStackTrace(TAG, t);
  }

  boolean hookRpcCallSuccess = false;
  try
  {
   clazz = loader.loadClass(ClassMember.com_alipay_mobile_nebulabiz_rpc_H5RpcUtil);
   Class<?> h5PageClazz = loader.loadClass(ClassMember.com_alipay_mobile_h5container_api_H5Page);
   Class<?> jsonClazz = loader.loadClass(ClassMember.com_alibaba_fastjson_JSONObject);
   XposedHelpers.findAndHookMethod(clazz, ClassMember.rpcCall, String.class, String.class, String.class,
    boolean.class, jsonClazz, String.class, boolean.class, h5PageClazz,
    int.class, String.class, boolean.class, int.class, new XC_MethodHook()
    {
     @Override
     protected void afterHookedMethod(MethodHookParam param) throws Throwable
     {
      afterHookRpcCall(param, loader);
     }
    });
   hookRpcCallSuccess = true;
   Log.i(TAG, "hook old " + ClassMember.rpcCall + " successfully");
  }catch(Throwable t)
  {
   Log.i(TAG, "hook old " + ClassMember.rpcCall + " err:");
   //Log.printStackTrace(TAG, e);
  }

  if(!hookRpcCallSuccess)
   try
   {
    clazz = loader.loadClass(ClassMember.com_alipay_mobile_nebulaappproxy_api_rpc_H5RpcUtil);
    Class<?> h5PageClazz = loader.loadClass(ClassMember.com_alipay_mobile_h5container_api_H5Page);
    Class<?> jsonClazz = loader.loadClass(ClassMember.com_alibaba_fastjson_JSONObject);
    XposedHelpers.findAndHookMethod(clazz, ClassMember.rpcCall, String.class, String.class, String.class,
     boolean.class, jsonClazz, String.class, boolean.class, h5PageClazz,
     int.class, String.class, boolean.class, int.class, String.class, new XC_MethodHook()
     {
      @Override
      protected void afterHookedMethod(MethodHookParam param) throws Throwable
      {
       afterHookRpcCall(param, loader);
      }
     });
    Log.i(TAG, "hook " + ClassMember.rpcCall + " successfully");
   }catch(Throwable t)
   {
    Log.i(TAG, "hook " + ClassMember.rpcCall + " err:");
    Log.printStackTrace(TAG, t);
   }
 }

 private void afterHookRpcCall(XC_MethodHook.MethodHookParam param, ClassLoader loader) throws Throwable
 {
  String args0 = (String)param.args[0], args1 = (String)param.args[1];
  if(args0 == null ||
     !args0.contains("forest") &&
     !args0.contains("antfarm") &&
     !args0.contains("antmember"))
   return;
  Log.i(TAG, args0 + ", " + args1);
  Object resp = param.getResult();
  if(resp != null)
  {
   String response = RpcCall.getResponse(resp);
   Log.i(TAG, "response: " + response);

//   AntForest.saveUserIdAndName(args0, response);

//   if(Config.enableForest())
//    AntForest.start(loader, args0, args1, response);

//   if(Config.enableFarm())
//    AntFarm.start(loader, args0, args1, response);

//   if(AntFarm.isEnterFriendFarm(response))
//   {
//    JSONObject jo = new JSONArray(args1).getJSONObject(0);
//    String userId = jo.getString("userId");
//    if(userId == null || userId.isEmpty())
//     userId = AntFarmRpcCall.farmId2UserId(jo.getString("farmId"));
//    Log.recordLog("进入〔" + Config.getNameById(userId) + "〕的蚂蚁庄园", "");
//   }

//   if(Config.receivePoint())
//    AntMember.receivePoint(loader, args0, args1, response);
  }
 }

}
