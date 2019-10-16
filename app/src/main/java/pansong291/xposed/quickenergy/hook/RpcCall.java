package pansong291.xposed.quickenergy.hook;

import java.lang.reflect.Method;
import pansong291.xposed.quickenergy.util.Log;

public class RpcCall
{
 private static final String TAG = RpcCall.class.getCanonicalName();
 private static Method rpcCallMethod;
 private static Method getResponseMethod;
 private static Object curH5PageImpl;

 public static String invoke(ClassLoader loader, String args0, String args1) throws Throwable
 {
  if(rpcCallMethod == null)
  {
   try
   {
    Class<?> rpcClazz = loader.loadClass(ClassMember.com_alipay_mobile_nebulabiz_rpc_H5RpcUtil);
    Class<?> h5PageClazz = loader.loadClass(ClassMember.com_alipay_mobile_h5container_api_H5Page);
    Class<?> jsonClazz = loader.loadClass(ClassMember.com_alibaba_fastjson_JSONObject);
    rpcCallMethod = rpcClazz.getMethod(
     ClassMember.rpcCall, String.class, String.class, String.class,
     boolean.class, jsonClazz, String.class, boolean.class, h5PageClazz,
     int.class, String.class, boolean.class, int.class);
    Log.i(TAG, "get Old RpcCallMethod successfully");
   }catch(Throwable t)
   {
    Log.i(TAG, "get Old RpcCallMethod err:");
    //Log.printStackTrace(TAG, t);
   }

   if(rpcCallMethod == null)
    try
    {
     Class<?> h5PageClazz = loader.loadClass(ClassMember.com_alipay_mobile_h5container_api_H5Page);
     Class<?> jsonClazz = loader.loadClass(ClassMember.com_alibaba_fastjson_JSONObject);
     Class<?> rpcClazz = loader.loadClass(ClassMember.com_alipay_mobile_nebulaappproxy_api_rpc_H5RpcUtil);
     rpcCallMethod = rpcClazz.getMethod(
      ClassMember.rpcCall, String.class, String.class, String.class,
      boolean.class, jsonClazz, String.class, boolean.class, h5PageClazz,
      int.class, String.class, boolean.class, int.class, String.class);
     Log.i(TAG, "get RpcCallMethod successfully");
    }catch(Throwable t)
    {
     Log.i(TAG, "get RpcCallMethod err:");
     //Log.printStackTrace(TAG, t);
    }
  }

  Object o = null;
  switch(rpcCallMethod.getParameterTypes().length)
  {
   case 12:
    o = rpcCallMethod.invoke(
     null, args0, args1, "", true, null, null, false, curH5PageImpl, 0, "", false, -1);
    break;
   default:
    o = rpcCallMethod.invoke(
     null, args0, args1, "", true, null, null, false, curH5PageImpl, 0, "", false, -1, "");
  }
  String str = getResponse(o);
  Log.i(TAG, "argument: " + args0 + ", " + args1);
  Log.i(TAG, "response: " + str);
  return str;
 }

 public static String getResponse(Object resp) throws Throwable
 {
  if(getResponseMethod == null)
   getResponseMethod = resp.getClass().getMethod(ClassMember.getResponse);

  return (String) getResponseMethod.invoke(resp);
 }

}
