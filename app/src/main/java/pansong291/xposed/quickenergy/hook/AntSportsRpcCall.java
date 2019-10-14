package pansong291.xposed.quickenergy.hook;

import pansong291.xposed.quickenergy.util.Log;

public class AntSportsRpcCall
{
 private static final String TAG = AntSportsRpcCall.class.getCanonicalName();
 private static final String chInfo = "antsports-account", timeZone = "Asia\\/Shanghai";

 public static String rpcCall_queryMyHomePage(ClassLoader loader)
 {
  try
  {
   String args1 = "[{\"chInfo\":\"" + chInfo
    + "\",\"pathListUsePage\":true,\"timeZone\":\"" + timeZone + "\"}]";
   return RpcCall.invoke(loader, "alipay.antsports.walk.map.queryMyHomePage", args1);
  }catch(Throwable t)
  {
   Log.i(TAG, "rpcCall_queryMyHomePage err:");
   Log.printStackTrace(TAG, t);
  }
  return null;
 }

 public static String rpcCall_join(ClassLoader loader, String pathId)
 {
  try
  {
   String args1 = "[{\"chInfo\":\"" + chInfo
    + "\",\"pathId\":\"" + pathId + "}]";
   return RpcCall.invoke(loader, "alipay.antsports.walk.map.join", args1);
  }catch(Throwable t)
  {
   Log.i(TAG, "rpcCall_join err:");
   Log.printStackTrace(TAG, t);
  }
  return null;
 }

 public static String rpcCall_go(ClassLoader loader, String day, String rankCacheKey, int stepCount)
 {
  try
  {
   String args1 = "[{\"chInfo\":\"" + chInfo + "\",\"day\":\"" + day
    + "\",\"needAllBox\":true,\"rankCacheKey\":\"" + rankCacheKey
    + "\",\"timeZone\":\"" + timeZone + "\",\"useStepCount\":" + stepCount + "}]";
   return RpcCall.invoke(loader, "alipay.antsports.walk.map.go", args1);
  }catch(Throwable t)
  {
   Log.i(TAG, "rpcCall_go err:");
   Log.printStackTrace(TAG, t);
  }
  return null;
 }

 public static String rpcCall_openTreasureBox(ClassLoader loader, String boxNo, String userId)
 {
  try
  {
   String args1 = "[{\"boxNo\":\"" + boxNo + "\",\"chInfo\":\""
    + chInfo + "\",\"userId\":\"" + userId + "\"}]";
   return RpcCall.invoke(loader, "alipay.antsports.walk.treasureBox.openTreasureBox", args1);
  }catch(Throwable t)
  {
   Log.i(TAG, "rpcCall_openTreasureBox err:");
   Log.printStackTrace(TAG, t);
  }
  return null;
 }

 public static String rpcCall_queryProjectList(ClassLoader loader, int index)
 {
  try
  {
   String args1 = "[{\"chInfo\":\"" + chInfo + "\",\"index\":"
    + index + ",\"projectListUseVertical\":true}]";
   return RpcCall.invoke(loader, "alipay.antsports.walk.charity.queryProjectList", args1);
  }catch(Throwable t)
  {
   Log.i(TAG, "rpcCall_queryProjectList err:");
   Log.printStackTrace(TAG, t);
  }
  return null;
 }

 public static String rpcCall_donate(ClassLoader loader, int donateCharityCoin, String projectId)
 {
  try
  {
   String args1 = "[{\"chInfo\":\"" + chInfo + "\",\"donateCharityCoin\":"
    + donateCharityCoin + ",\"projectId\":\"" + projectId + "\"}]";
   return RpcCall.invoke(loader, "alipay.antsports.walk.charity.donate", args1);
  }catch(Throwable t)
  {
   Log.i(TAG, "rpcCall_queryProjectList err:");
   Log.printStackTrace(TAG, t);
  }
  return null;
 }

}
