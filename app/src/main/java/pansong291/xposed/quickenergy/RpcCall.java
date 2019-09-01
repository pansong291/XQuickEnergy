package pansong291.xposed.quickenergy;

import android.app.Activity;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class RpcCall
{
 private static final String TAG = RpcCall.class.getCanonicalName();
 private static Method rpcCallMethod;
 private static Method getResponseMethod;
 public static Object curH5Fragment;
 private static Object curH5PageImpl;
 public static Activity h5Activity;

 public static Object invoke(ClassLoader loader, String args0, String args1) throws Exception
 {
  if(rpcCallMethod == null)
  {
   try
   {
    Class<?> rpcClazz = loader.loadClass(ClassMember.com_alipay_mobile_nebulabiz_rpc_H5RpcUtil);
    Field aF = curH5Fragment.getClass().getDeclaredField(ClassMember.a);
    aF.setAccessible(true);
    Object viewHolder = aF.get(curH5Fragment);
    Field hF = viewHolder.getClass().getDeclaredField(ClassMember.h);
    hF.setAccessible(true);
    curH5PageImpl = hF.get(viewHolder);
    Class<?> h5PageClazz = loader.loadClass(ClassMember.com_alipay_mobile_h5container_api_H5Page);
    Class<?> jsonClazz = loader.loadClass(ClassMember.com_alibaba_fastjson_JSONObject);
    if(curH5PageImpl != null)
    {
     rpcCallMethod = rpcClazz.getMethod(
      ClassMember.rpcCall, String.class, String.class, String.class,
      boolean.class, jsonClazz, String.class, boolean.class, h5PageClazz,
      int.class, String.class, boolean.class, int.class);
     Log.i(TAG, "get Old RpcCallMethod successfully");
    }
   }catch(Throwable t)
   {
    Log.i(TAG, "get Old RpcCallMethod err:");
    //Log.printStackTrace(TAG, t);
   }

   if(rpcCallMethod == null)
    try
    {
     Field aF = curH5Fragment.getClass().getDeclaredField(ClassMember.a);
     aF.setAccessible(true);
     Object viewHolder = aF.get(curH5Fragment);
     Field hF = viewHolder.getClass().getDeclaredField(ClassMember.h);
     hF.setAccessible(true);
     curH5PageImpl = hF.get(viewHolder);
     Class<?> h5PageClazz = loader.loadClass(ClassMember.com_alipay_mobile_h5container_api_H5Page);
     Class<?> jsonClazz = loader.loadClass(ClassMember.com_alibaba_fastjson_JSONObject);
     Class<?> rpcClazz = loader.loadClass(ClassMember.com_alipay_mobile_nebulaappproxy_api_rpc_H5RpcUtil);
     if(curH5PageImpl != null)
     {
      rpcCallMethod = rpcClazz.getMethod(
       ClassMember.rpcCall, String.class, String.class, String.class,
       boolean.class, jsonClazz, String.class, boolean.class, h5PageClazz,
       int.class, String.class, boolean.class, int.class, String.class);
      Log.i(TAG, "get RpcCallMethod successfully");
     }
    }catch(Throwable t)
    {
     Log.i(TAG, "get RpcCallMethod err:");
     Log.printStackTrace(TAG, t);
    }
  }

  Log.i(TAG, "rpcCall params count: "+rpcCallMethod.getParameterTypes().length);
  switch(rpcCallMethod.getParameterTypes().length)
  {
   case 13:
    return rpcCallMethod.invoke(
     null, args0, args1, "", true, null, null, false, curH5PageImpl, 0, "", false, -1, "");
  }
  return rpcCallMethod.invoke(
   null, args0, args1, "", true, null, null, false, curH5PageImpl, 0, "", false, -1);
 }

 public static String getResponse(Object resp) throws Exception
 {
  if(getResponseMethod == null)
   getResponseMethod = resp.getClass().getMethod(ClassMember.getResponse);
  return (String) getResponseMethod.invoke(resp);
 }

}
