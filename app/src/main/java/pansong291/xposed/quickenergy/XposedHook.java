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

public class XposedHook implements IXposedHookLoadPackage
{

 private static boolean first = false;
 private static String TAG = XposedHook.class.getCanonicalName();

 @Override
 public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable
 {
  if("com.eg.android.AlipayGphone".equals(lpparam.packageName))
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
   Log.i(TAG, "hookSecurity err:" + e.getMessage());
  }
 }

 private void hookRpcCall(XC_LoadPackage.LoadPackageParam lpparam)
 {
  try
  {
   XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new ApplicationAttachMethodHook());
  }catch(Exception e2)
  {
   Log.i(TAG, "hookRpcCall err:" + e2.getMessage());
   //Log.showDialog("hook出错：\n" + e2.getMessage(), "");
  }
 }

 private class ApplicationAttachMethodHook extends XC_MethodHook
 {
  @Override
  protected void afterHookedMethod(MethodHookParam param) throws Throwable
  {
   if(first) return;
   final ClassLoader loader = ((Context) param.args[0]).getClassLoader();
   Class<?> clazz = loader.loadClass("com.alipay.mobile.nebulacore.ui.H5FragmentManager");
   if(clazz != null)
   {
    Class<?> h5FragmentClazz = loader.loadClass("com.alipay.mobile.nebulacore.ui.H5Fragment");
    if(h5FragmentClazz != null)
    {
     XposedHelpers.findAndHookMethod(clazz, "pushFragment", h5FragmentClazz,
      boolean.class, Bundle.class, boolean.class, boolean.class, new XC_MethodHook()
      {
       @Override
       protected void afterHookedMethod(MethodHookParam param) throws Throwable
       {
        Log.i("fragment", "cur fragment: " + param.args[0]);
        AliMobileAutoCollectEnergyUtils.curH5Fragment = param.args[0];
       }
      });
    }else
    {
     Log.i(TAG, "hook出错：\ncouldn't find class com.alipay.mobile.nebulacore.ui.H5Fragment");
    }
   }else
   {
    Log.i(TAG, "hook出错：\ncouldn't find class com.alipay.mobile.nebulacore.ui.H5FragmentManager");
   }

   clazz = loader.loadClass("com.alipay.mobile.nebulacore.ui.H5Activity");
   if(clazz != null)
   {
    XposedHelpers.findAndHookMethod(clazz, "onResume", new XC_MethodHook()
     {
      @Override
      protected void afterHookedMethod(MethodHookParam param) throws Throwable
      {
       AliMobileAutoCollectEnergyUtils.h5Activity = (Activity) param.thisObject;
      }
     });
   }else
   {
    Log.i(TAG, "hook出错：\ncouldn't find class com.alipay.mobile.nebulacore.ui.H5Activity");
   }

   clazz = loader.loadClass("com.alipay.mobile.nebulaappproxy.api.rpc.H5RpcUtil");
   if(clazz != null)
   {
    first = true;
    Log.i(TAG, "first");
    Class<?> h5PageClazz = loader.loadClass("com.alipay.mobile.h5container.api.H5Page");
    Class<?> jsonClazz = loader.loadClass("com.alibaba.fastjson.JSONObject");
    if(h5PageClazz != null && jsonClazz != null)
    {
     try
     {
      XposedHelpers.findAndHookMethod(clazz, "rpcCall", String.class, String.class, String.class,
       boolean.class, jsonClazz, String.class, boolean.class, h5PageClazz,
       int.class, String.class, boolean.class, int.class, new XC_MethodHook()
       {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable
        {
         // Log.i(TAG, "param" + param.args);
        }

        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable
        {
         Object resp = param.getResult();
         if(resp != null)
         {
          Method method = resp.getClass().getMethod("getResponse");
          String response = (String) method.invoke(resp);
          Log.i(TAG, "response: " + response);

          if(AliMobileAutoCollectEnergyUtils.isRankList(response))
          {
           Log.i(TAG, "autoGetCanCollectUserIdList");
           AliMobileAutoCollectEnergyUtils.autoGetCanCollectUserIdList(loader, response);
          }

          // 第一次是自己的能量，比上面的获取用户信息还要早，所有这里需要记录当前自己的userid值
          if(AliMobileAutoCollectEnergyUtils.isUserDetail(response))
          {
           Log.i(TAG, "autoGetCanCollectBubbleIdList");
           AliMobileAutoCollectEnergyUtils.autoGetCanCollectBubbleIdList(loader, response);
          }
         }
        }
       });
     }catch(Exception e)
     {
      Log.i(TAG, "hook rpcCall err:" + e.getMessage());
      Log.showDialog("hook出错：\n" + e.getMessage(), "");
     }
    }else
    {
     Log.showDialog("hook出错：\ncouldn't find class com.alipay.mobile.h5container.api.H5Page or com.alibaba.fastjson.JSONObject", "");
    }
   }else
   {
    Log.showDialog("hook出错：\ncouldn't find class com.alipay.mobile.nebulaappproxy.api.rpc.H5RpcUtil", "");
   }
  }
 }
}
