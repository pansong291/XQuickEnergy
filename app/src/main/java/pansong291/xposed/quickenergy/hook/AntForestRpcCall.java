package pansong291.xposed.quickenergy.hook;

import pansong291.xposed.quickenergy.util.Log;

public class AntForestRpcCall
{
 private static final String TAG = AntForestRpcCall.class.getCanonicalName();

 public static String rpcCall_queryEnergyRanking(ClassLoader loader, String startPoint)
 {
  try
  {
   String args1 = "[{\"av\":\"5\",\"ct\":\"android\",\"pageSize\":20,\"startPoint\":\""
    + startPoint + "\"}]";
   Object o = RpcCall.invoke(loader, "alipay.antmember.forest.h5.queryEnergyRanking", args1);
   return RpcCall.getResponse(o);
  }catch(Throwable t)
  {
   Log.i(TAG, "rpcCall_queryEnergyRanking err:");
   Log.printStackTrace(TAG, t);
  }
  return null;
 }

 public static String rpcCall_queryNextAction(ClassLoader loader, String userId)
 {
  try
  {
   String args1 = "[{\"userId\":\"" + userId + "\"}]";
   Object o = RpcCall.invoke(loader, "alipay.antmember.forest.h5.queryNextAction", args1);

//   args1 = "[{\"av\":\"5\",\"ct\":\"android\",\"pageSize\":3,\"startIndex\":0,\"userId\":\""
//    +userId+"\"}]";
//   RpcCall.invoke(loader, "alipay.antmember.forest.h5.pageQueryDynamics", args1);

   return RpcCall.getResponse(o);
  }catch(Throwable t)
  {
   Log.i(TAG, "rpcCall_queryNextAction err:");
   Log.printStackTrace(TAG, t);
  }
  return null;
 }

 public static String rpcCall_collectEnergy(ClassLoader loader, String userId, long bubbleId)
 {
  try
  {
   String args1 = "[{\"bubbleIds\":[" + bubbleId + "],\"userId\":\"" + userId + "\"}]";
   Object o = RpcCall.invoke(loader, "alipay.antmember.forest.h5.collectEnergy", args1);
   return RpcCall.getResponse(o);
  }catch(Throwable t)
  {
   Log.i(TAG, "rpcCall_collectEnergy err:");
   Log.printStackTrace(TAG, t);
  }
  return null;
 }

 public static String rpcCall_transferEnergy(ClassLoader loader, String targetUser, String bizNo, int ordinal)
 {
  try
  {
   String args1 = "[{\"bizNo\":\"" + bizNo + ordinal + "\",\"targetUser\":\""
    + targetUser + "\",\"transferType\":\"WATERING\",\"version\":\"20181217\"}]";// 
   Object o = RpcCall.invoke(loader, "alipay.antmember.forest.h5.transferEnergy", args1);
   return RpcCall.getResponse(o);
  }catch(Throwable t)
  {
   Log.i(TAG, "rpcCall_transferEnergy err:");
   Log.printStackTrace(TAG, t);
  }
  return null;
 }

 public static String rpcCall_forFriendCollectEnergy(ClassLoader loader, String targetUserId, long bubbleId)
 {
  try
  {
   String args1 = "[{\"bubbleIds\":[" + bubbleId + "],\"targetUserId\":\"" + targetUserId + "\"}]";
   Object o = RpcCall.invoke(loader, "alipay.antmember.forest.h5.forFriendCollectEnergy", args1);
   return RpcCall.getResponse(o);
  }catch(Throwable t)
  {
   Log.i(TAG, "rpcCall_forFriendCollectEnergy err:");
   Log.printStackTrace(TAG, t);
  }
  return null;
 }

 public static String rpcCall_queryTaskList(ClassLoader loader)
 {
  try
  {
   String args1 = "[{\"version\":\"20190321\"}]"; // 
   Object o = RpcCall.invoke(loader, "alipay.antforest.forest.h5.queryTaskList", args1);
   return RpcCall.getResponse(o);
  }catch(Throwable t)
  {
   Log.i(TAG, "rpcCall_queryTaskList err:");
   Log.printStackTrace(TAG, t);
  }
  return null;
 }

 public static String rpcCall_receiveTaskAward(ClassLoader loader, String taskType)
 {
  try
  {
   String args1 =
    "[{\"ignoreLimit\":false,\"requestType\":\"H5\",\"sceneCode\":\"ANTFOREST_TASK\",\"source\":\"ANTFOREST\",\"taskType\":\""
    + taskType + "\"}]";
   Object o = RpcCall.invoke(loader, "com.alipay.antiep.receiveTaskAward", args1);
   return RpcCall.getResponse(o);
  }catch(Throwable t)
  {
   Log.i(TAG, "rpcCall_receiveTaskAward err:");
   Log.printStackTrace(TAG, t);
  }
  return null;
 }

 private static String rpcCall_queryPropList(ClassLoader loader)
 {
  try
  {
   String args1 = "[{\"version\":\"\"}]"; //20181217
   Object o = RpcCall.invoke(loader, "alipay.antforest.forest.h5.queryPropList", args1);
   return RpcCall.getResponse(o);
  }catch(Throwable t)
  {
   Log.i(TAG, "rpcCall_queryPropList err:");
   Log.printStackTrace(TAG, t);
  }
  return null;
 }

}
