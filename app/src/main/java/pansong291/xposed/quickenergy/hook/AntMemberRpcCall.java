package pansong291.xposed.quickenergy.hook;

import pansong291.xposed.quickenergy.util.Log;

public class AntMemberRpcCall
{
 private static final String TAG = AntMemberRpcCall.class.getCanonicalName();

 public static String rpcCall_queryPointCert(ClassLoader loader, int page, int pageSize)
 {
  try
  {
   String args1 = "[{\"page\":" + page + ",\"pageSize\":" + pageSize + "}]";
   Object o = RpcCall.invoke(loader, "alipay.antmember.biz.rpc.member.h5.queryPointCert", args1);
   return RpcCall.getResponse(o);
  }catch(Throwable t)
  {
   Log.i(TAG, "rpcCall_queryPointCert err:");
   Log.printStackTrace(TAG, t);
  }
  return null;
 }

 public static String rpcCall_receivePointByUser(ClassLoader loader, String certId)
 {
  try
  {
   String args1 = "[{\"certId\":" + certId + "}]";
   Object o = RpcCall.invoke(loader, "alipay.antmember.biz.rpc.member.h5.receivePointByUser", args1);
   return RpcCall.getResponse(o);
  }catch(Throwable t)
  {
   Log.i(TAG, "rpcCall_receivePointByUser err:");
   Log.printStackTrace(TAG, t);
  }
  return null;
 }

 public static String rpcCall_queryPoint(ClassLoader loader)
 {
  try
  {
   String args1 = "[{}]";
   Object o = RpcCall.invoke(loader, "alipay.antmember.h5.queryPoint", args1);
   return RpcCall.getResponse(o);
  }catch(Throwable t)
  {
   Log.i(TAG, "rpcCall_queryPoint err:");
   Log.printStackTrace(TAG, t);
  }
  return null;
 }

 public static String rpcCall_memberSignin(ClassLoader loader)
 {
  try
  {
   String args1 = "[{}]";
   Object o = RpcCall.invoke(loader, "alipay.antmember.biz.rpc.member.h5.memberSignin", args1);
   return RpcCall.getResponse(o);
  }catch(Throwable t)
  {
   Log.i(TAG, "rpcCall_memberSignin err:");
   Log.printStackTrace(TAG, t);
  }
  return null;
 }
}
