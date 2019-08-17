package pansong291.xposed.quickenergy;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import java.lang.reflect.Method;
import org.json.JSONObject;
import org.json.JSONArray;
import pansong291.xposed.quickenergy.ui.MainActivity;

public class XposedHook implements IXposedHookLoadPackage
{

 private static final String TAG = XposedHook.class.getCanonicalName();

 @Override
 public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable
 {
  if(getClass().getPackage().getName().equals(lpparam.packageName))
  {
   XposedHelpers.findAndHookMethod(MainActivity.class.getName(), lpparam.classLoader, "setModuleActive", boolean.class, new XC_MethodHook()
    {
     @Override
     protected void beforeHookedMethod(MethodHookParam param) throws Throwable
     {
      param.args[0] = true;
      RpcCall.h5Activity = (Activity)param.thisObject;
     }
    });
  }

  if(ClassMember.com_eg_android_AlipayGphone.equals(lpparam.packageName))
  {
   Log.i(TAG, lpparam.packageName);
   //hookSecurity(lpparam);
   hookRpcCall(lpparam);
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
  }catch(Throwable e)
  {
   Log.i(TAG, "hookSecurity err:");
   Log.printStackTrace(TAG, e);
  }
 }

 private void hookRpcCall(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable
 {
  final ClassLoader loader = lpparam.classLoader;
  Class<?> clazz = null;
  try
  {
   clazz = loader.loadClass(ClassMember.com_alipay_mobile_nebulacore_ui_H5FragmentManager);
   Class<?> h5FragmentClazz = loader.loadClass(ClassMember.com_alipay_mobile_nebulacore_ui_H5Fragment);
   XposedHelpers.findAndHookMethod(clazz, ClassMember.pushFragment, h5FragmentClazz,
    boolean.class, Bundle.class, boolean.class, boolean.class, new XC_MethodHook()
    {
     @Override
     protected void afterHookedMethod(MethodHookParam param) throws Throwable
     {
      Log.i(TAG, "cur fragment: " + param.args[0]);
      RpcCall.curH5Fragment = param.args[0];
     }
    });
   Log.i(TAG, "hook " + ClassMember.pushFragment + " successfully");
  }catch(Exception e)
  {
   Log.i(TAG, "hook " + ClassMember.pushFragment + " err:");
   Log.printStackTrace(TAG, e);
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
  }catch(Exception e)
  {
   Log.i(TAG, "hook " + ClassMember.onResume + " err:");
   Log.printStackTrace(TAG, e);
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
   Log.i(TAG, "hook " + ClassMember.rpcCall + " successfully");
  }catch(Exception e)
  {
   Log.i(TAG, "hook " + ClassMember.rpcCall + " err:");
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
   }catch(Exception e)
   {
    Log.i(TAG, "hook " + ClassMember.rpcCall + " err:");
    Log.printStackTrace(TAG, e);
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

   AntForest.saveUserIdAndName(args0, response);

   if(Config.enableForest())
    AntForest.start(loader, args0, args1, response);

   if(Config.enableFarm())
    AntFarm.start(loader, args0, args1, response);

   if(AntFarm.isEnterFriendFarm(response))
   {
    JSONObject jo = new JSONArray(args1).getJSONObject(0);
    String userId = jo.getString("userId");
    if(userId == null || userId.isEmpty())
     userId = AntFarm.farmId2UserId(jo.getString("farmId"));
    Log.recordLog("进入〔" + Config.getNameById(userId) + "〕的蚂蚁庄园", "");
   }

   if(Config.receivePoint())
    AntMember.receivePoint(loader, args0, args1, response);
  }
 }

}
