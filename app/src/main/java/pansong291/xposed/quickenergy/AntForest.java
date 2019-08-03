package pansong291.xposed.quickenergy;

import android.os.Handler;
import android.os.Message;
import org.json.JSONArray;
import org.json.JSONObject;
import pansong291.xposed.quickenergy.AntFarm.TaskStatus;
import android.os.Looper;
import java.util.List;

public class AntForest
{
 private static final String TAG = AntForest.class.getCanonicalName();
 private static String selfId;
 private static int collectedEnergy = 0;
 private static int helpCollectedEnergy = 0;
 private static int onceHelpCollected = 0;
 public enum TaskAwardType
 {
  BUBBLE_BOOST;
  public String nickName()
  {
   switch(this)
   {
    case BUBBLE_BOOST:
     return "时光加速器";
    default:
    return name();
   }
  }
 }
 
 public enum ThreadStatus
 { START, END }
 private static int threadCount = 0;
 private static boolean hasMore = true;

 public static void start(ClassLoader loader, String args0, String args1, String resp)
 {
  if(!args0.equals("alipay.antmember.forest.h5.queryNextAction")
   || args1.contains("\"userId\"")) return;

  new Thread(new Runnable()
   {
    ClassLoader loader;
    String resp;

    public Runnable setData(ClassLoader cl, String s)
    {
     loader = cl;resp = s;
     return this;
    }

    @Override
    public void run()
    {
     try
     {
      if(Config.collectEnergy())
      {
       Log.showDialogOrToast("开始收取能量…","");
       queryEnergyRanking(loader, 1);
       JSONObject jo = new JSONObject(resp);
       if(jo.getString("resultCode").equals("SUCCESS"))
       {
        jo = jo.getJSONObject("userEnergy");
        selfId = jo.getString("userId");
        canCollectSelfEnergy(loader, resp);
       }else
       {
        Log.showDialogAndRecordLog(jo.getString("resultDesc"),"");
       }
      }

      for(String userId: Config.waterFriendList())
      {
       waterFriendEnergy(loader,userId);
      }
      
      if(Config.receiveForestTaskAward())receiveTaskAward(loader);
     }catch(Exception e)
     {
      Log.i(TAG, "start err:");
      Log.printStackTrace(TAG, e);
     }
    }
   }.setData(loader, resp)).start();


 }

 private static void queryEnergyRanking(ClassLoader loader, int startPoint)
 {
  new Thread(new Runnable()
   {
    ClassLoader loader;
    int startPoint;

    public Runnable setData(ClassLoader cl, int sp)
    {
     loader = cl;startPoint = sp;
     return this;
    }

    @Override
    public void run()
    {
     try
     {
      updateThreadCount(ThreadStatus.START,loader);
      String s = rpcCall_queryEnergyRanking(loader, startPoint);
      JSONObject jo = new JSONObject(s);
      if(jo.getString("resultCode").equals("SUCCESS"))
      {
       hasMore = jo.getBoolean("hasMore");
       if(hasMore)
        queryEnergyRanking(loader, startPoint + 20);
       JSONArray jaFriendRanking = jo.getJSONArray("friendRanking");
       for(int i = 0; i < jaFriendRanking.length(); i++)
       {
        jo = jaFriendRanking.getJSONObject(i);
        boolean optBoolean = jo.getBoolean("canCollectEnergy")
         || jo.getBoolean("canHelpCollect");
        String userId = jo.getString("userId");
        if(optBoolean && !userId.equals(selfId))
        {
         canCollectEnergy(loader, userId);
        }
       }
      }else
      {
       Log.showDialogAndRecordLog(jo.getString("resultDesc"),"");
      }
     }catch(Exception e)
     {
      Log.i(TAG, "queryEnergyRanking err:");
      Log.printStackTrace(TAG, e);
      hasMore = false;
     }
     updateThreadCount(ThreadStatus.END,loader);
    }
   }.setData(loader, startPoint)).start();
 }

 private static void canCollectSelfEnergy(ClassLoader loader, String resp)
 {
  try
  {
   JSONObject jo = new JSONObject(resp);
   if(jo.getString("resultCode").equals("SUCCESS"))
   {
    JSONArray jaBubbles = jo.getJSONArray("bubbles");
    jo = jo.getJSONObject("userEnergy");
    String userId = selfId;
    String userName = jo.getString("displayName");
    Config.putIdMap(userId, userName);
    for(int i = 0; i < jaBubbles.length(); i++)
    {
     jo = jaBubbles.getJSONObject(i);
     long bubbleId = jo.getLong("id");
     if("AVAILABLE".equals(jo.getString("collectStatus")))
     {
      if(Config.dontCollect(userId))
       Log.showDialog("不偷取【"+userName+"】",", userId="+userId);
      else
       collectEnergy(loader, userId, bubbleId, userName);
     }
    }
   }else
   {
    Log.showDialogAndRecordLog(jo.getString("resultDesc"),"");
   }
  }catch(Exception e)
  {
   Log.i(TAG, "canCollectSelfEnergy err:");
   Log.printStackTrace(TAG, e);
  }
 }

 private static void canCollectEnergy(ClassLoader loader, String userId)
 {
  try
  {
   String s = rpcCall_queryNextAction(loader, userId);
   JSONObject jo = new JSONObject(s);
   if(jo.getString("resultCode").equals("SUCCESS"))
   {
    JSONArray jaBubbles = jo.getJSONArray("bubbles");
    jo = jo.getJSONObject("userEnergy");
    String userName = jo.getString("displayName");
    for(int i = 0; i < jaBubbles.length(); i++)
    {
     jo = jaBubbles.getJSONObject(i);
     long bubbleId = jo.getLong("id");
     if("AVAILABLE".equals(jo.getString("collectStatus")))
     {
      if(Config.dontCollect(userId))
       Log.showDialog("不偷取【"+userName+"】",", userId="+userId);
      else
       collectEnergy(loader, userId, bubbleId, userName);
     }
     if(jo.getBoolean("canHelpCollect"))
     {
      if(Config.helpFriend())
      {
       if(Config.dontHelp(userId))
        Log.showDialog("不帮收【"+userName+"】",", userId="+userId);
       else
        forFriendCollectEnergy(loader, userId, bubbleId, userName);
      }else
       Log.showDialog("不帮收【"+userName+"】",", userId="+userId);
     }
    }
   }else
   {
    Log.showDialogAndRecordLog(jo.getString("resultDesc"),"");
   }
  }catch(Exception e)
  {
   Log.i(TAG, "canCollectEnergy err:");
   Log.printStackTrace(TAG, e);
  }
 }

 private static void collectEnergy(ClassLoader loader, String userId, Long bubbleId, String userName)
 {
  try
  {
   String s = rpcCall_collectEnergy(loader,userId,bubbleId);
   JSONObject jo = new JSONObject(s);
   if(jo.getString("resultCode").equals("SUCCESS"))
   {
    JSONArray jaBubbles = jo.getJSONArray("bubbles");
    int collected = 0;
    for(int i = 0; i < jaBubbles.length(); i++)
    {
     jo = jaBubbles.getJSONObject(i);
     collected += jo.getInt("collectedEnergy");
    }
    if(collected > 0)
    {
     Log.showDialogAndRecordLog("偷取【"+userName+"】的能量【"+collected+"克】","，UserID："+userId+"，BubbleId："+bubbleId);
     collectedEnergy += collected;
    }else
    {
     Log.showDialogAndRecordLog("偷取【"+userName+"】的能量失败","，UserID："+userId+"，BubbleId："+bubbleId);
    }
   }else
   {
    s = jo.getString("resultDesc");
    if(s.contains("TA"))
     s = s.replace("TA","【"+userName+"】");
    Log.showDialogAndRecordLog(s,"");
   }
  }catch(Exception e)
  {
   Log.i(TAG, "collectEnergy err:");
   Log.printStackTrace(TAG, e);
  }
 }

 private static void forFriendCollectEnergy(ClassLoader loader, String targetUserId, Long bubbleId, String userName)
 {
  try
  {
   String s = rpcCall_forFriendCollectEnergy(loader,targetUserId,bubbleId);
   JSONObject jo = new JSONObject(s);
   if(jo.getString("resultCode").equals("SUCCESS"))
   {
    JSONArray jaBubbles = jo.getJSONArray("bubbles");
    int helped = 0;
    for(int i = 0; i < jaBubbles.length(); i++)
    {
     jo = jaBubbles.getJSONObject(i);
     helped += jo.getInt("collectedEnergy");
    }
    if(helped > 0)
    {
     Log.showDialogAndRecordLog("帮【"+userName+"】收取【"+helped+"克】","，UserID："+targetUserId+"，BubbleId："+bubbleId);
     onceHelpCollected += helped;
    }else
    {
     Log.showDialogAndRecordLog("帮【"+userName+"】收取失败","，UserID："+targetUserId+"，BubbleId"+bubbleId);
    }
   }else
   {
    s = jo.getString("resultDesc");
    if(s.contains("TA"))
     s = s.replace("TA","【"+userName+"】");
    Log.showDialogAndRecordLog(s,"");
   }
  }catch(Exception e)
  {
   Log.i(TAG, "forFriendCollectEnergy err:");
   Log.printStackTrace(TAG, e);
  }
 }

 private static void waterFriendEnergy(ClassLoader loader, String userId)
 {
  try
  {
   String s = rpcCall_queryNextAction(loader,userId);
   JSONObject jo = new JSONObject(s);
   s = jo.getString("resultCode");
   if(s.equals("SUCCESS"))
   {
    String bizNo = jo.getString("bizNo");
    jo = jo.getJSONObject("userEnergy");
    String userName = jo.getString("displayName");
    for(int waterCount = 1; waterCount <= 3;)
    {
     s = rpcCall_transferEnergy(loader,userId,bizNo,waterCount);
     jo = new JSONObject(s);
     s = jo.getString("resultCode");
     if(s.equals("SUCCESS"))
     {
      s = jo.getJSONObject("treeEnergy").getString("currentEnergy");
      Log.showDialogAndRecordLog("给【"+userName+"】浇水成功，剩余能量【"+s+"克】","");
      waterCount++;
      Thread.sleep(2000);
     }else if(s.equals("WATERING_TIMES_LIMIT"))
     {
      Log.showDialogAndRecordLog("今日给【"+userName+"】浇水已达上限","");
      break;
     }else if(s.equals("LOW_VERSION"))
     {
      Log.showDialogAndRecordLog("给【"+userName+"】浇水失败，"+s,"");
      break;
     }else
     {
      Log.showDialogAndRecordLog(jo.getString("resultDesc"),"");
     }
    }
   }else
   {
    Log.showDialogAndRecordLog(jo.getString("resultDesc"),"");
   }
  }catch(Exception e)
  {
   Log.i(TAG, "waterFriendEnergy err:");
   Log.printStackTrace(TAG, e);
  }
 }

 private static void receiveTaskAward(ClassLoader loader)
 {
  try
  {
   String s = rpcCall_queryTaskList(loader);
   JSONObject jo = new JSONObject(s);
   if(jo.getString("resultCode").equals("SUCCESS"))
   {
    JSONArray jaForestTaskVOList = jo.getJSONArray("forestTaskVOList");
    for(int i = 0; i < jaForestTaskVOList.length(); i++)
    {
     jo = jaForestTaskVOList.getJSONObject(i);
     if(TaskStatus.FINISHED.name().equals(jo.getString("taskStatus")))
     {
      TaskAwardType taskAwardType = TaskAwardType.valueOf(jo.getString("awardType"));
      int awardCount = jo.getInt("awardCount");
      String taskType = jo.getString("taskType");
      s = rpcCall_receiveTaskAward(loader,taskType);
      jo = new JSONObject(s);
      s = jo.getString("desc");
      if(s.equals("SUCCESS"))
       Log.showDialogAndRecordLog("已领取【"+awardCount+"个】【"+taskAwardType.nickName()+"】","");
      else
       Log.showDialogAndRecordLog("领取失败，"+s,"");
     }
    }
   }else
   {
    Log.showDialogAndRecordLog(jo.getString("resultDesc"),"");
   }
  }catch(Exception e)
  {
   Log.i(TAG, "receiveTaskAward err:");
   Log.printStackTrace(TAG, e);
  }
 }

 private static void queryPropList(ClassLoader loader)
 {
  try
  {
   String s = rpcCall_queryTaskList(loader);
   JSONObject jo = new JSONObject(s);
   if(jo.getString("resultCode").equals("SUCCESS"))
   {
    JSONArray jaForestPropVOList = jo.getJSONArray("forestPropVOList");
    for(int i = 0; i < jaForestPropVOList.length(); i++)
    {
     jo = jaForestPropVOList.getJSONObject(i);
     if(TaskStatus.FINISHED.name().equals(jo.getString("taskStatus")))
     {
      String taskType = jo.getString("taskType");

     }
    }
   }else
   {
    Log.showDialogAndRecordLog(jo.getString("resultDesc"),"");
   }
  }catch(Exception e)
  {
   Log.i(TAG, "queryTaskList err:");
   Log.printStackTrace(TAG, e);
  }
 }

 private static String rpcCall_queryEnergyRanking(ClassLoader loader, int startPoint)
 {
  try
  {
   String args1 = "[{\"av\":\"5\",\"ct\":\"android\",\"pageSize\":20,\"startPoint\":"
    +startPoint+"}]";
   Object o = RpcCall.invoke(loader, "alipay.antmember.forest.h5.queryEnergyRanking", args1);
   return RpcCall.getResponse(o);
  }catch(Exception e)
  {
   Log.i(TAG, "rpcCall_queryEnergyRanking err:");
   Log.printStackTrace(TAG, e);
  }
  return null;
 }

 private static String rpcCall_queryNextAction(ClassLoader loader, String userId)
 {
  try
  {
   String args1 = "[{\"userId\":\""+userId+"\"}]";
   Object o = RpcCall.invoke(loader, "alipay.antmember.forest.h5.queryNextAction", args1);

   args1 = "[{\"av\":\"5\",\"ct\":\"android\",\"pageSize\":3,\"startIndex\":0,\"userId\":\""
    +userId+"\"}]";
   RpcCall.invoke(loader, "alipay.antmember.forest.h5.pageQueryDynamics", args1);

   return RpcCall.getResponse(o);
  }catch(Exception e)
  {
   Log.i(TAG, "rpcCall_queryNextAction err:");
   Log.printStackTrace(TAG, e);
  }
  return null;
 }

 private static String rpcCall_collectEnergy(ClassLoader loader, String userId, Long bubbleId)
 {
  try
  {
   String args1 = "[{\"bubbleIds\":["+bubbleId+"],\"userId\":\""+userId+"\"}]";
   Object o = RpcCall.invoke(loader, "alipay.antmember.forest.h5.collectEnergy", args1);
   return RpcCall.getResponse(o);
  }catch(Exception e)
  {
   Log.i(TAG, "rpcCall_collectEnergy err:");
   Log.printStackTrace(TAG, e);
  }
  return null;
 }

 private static String rpcCall_transferEnergy(ClassLoader loader, String targetUser, String bizNo, int ordinal)
 {
  try
  {
   String args1 = "[{\"bizNo\":\""+bizNo+ordinal+"\",\"targetUser\":\""
    +targetUser+"\",\"transferType\":\"WATERING\",\"version\":\"\"}]";//20181217
   Object o = RpcCall.invoke(loader, "alipay.antmember.forest.h5.transferEnergy", args1);
   return RpcCall.getResponse(o);
  }catch(Exception e)
  {
   Log.i(TAG, "rpcCall_transferEnergy err:");
   Log.printStackTrace(TAG, e);
  }
  return null;
 }

 private static String rpcCall_forFriendCollectEnergy(ClassLoader loader, String targetUserId, Long bubbleId)
 {
  try
  {
   String args1 = "[{\"bubbleIds\":["+bubbleId+"],\"targetUserId\":\""+targetUserId+"\"}]";
   Object o = RpcCall.invoke(loader, "alipay.antmember.forest.h5.forFriendCollectEnergy", args1);
   return RpcCall.getResponse(o);
  }catch(Exception e)
  {
   Log.i(TAG, "rpcCall_forFriendCollectEnergy err:");
   Log.printStackTrace(TAG, e);
  }
  return null;
 }

 private static String rpcCall_queryTaskList(ClassLoader loader)
 {
  try
  {
   String args1 = "[{\"version\":\"\"}]"; //20190321
   Object o = RpcCall.invoke(loader, "alipay.antforest.forest.h5.queryTaskList", args1);
   return RpcCall.getResponse(o);
  }catch(Exception e)
  {
   Log.i(TAG, "rpcCall_queryTaskList err:");
   Log.printStackTrace(TAG, e);
  }
  return null;
 }

 private static String rpcCall_receiveTaskAward(ClassLoader loader, String taskType)
 {
  try
  {
   String args1 =
    "[{\"ignoreLimit\":false,\"requestType\":\"H5\",\"sceneCode\":\"ANTFOREST_TASK\",\"source\":\"ANTFOREST\",\"taskType\":\""
    +taskType+"\"}]";
   Object o = RpcCall.invoke(loader, "com.alipay.antiep.receiveTaskAward", args1);
   return RpcCall.getResponse(o);
  }catch(Exception e)
  {
   Log.i(TAG, "rpcCall_receiveTaskAward err:");
   Log.printStackTrace(TAG, e);
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
  }catch(Exception e)
  {
   Log.i(TAG, "rpcCall_queryPropList err:");
   Log.printStackTrace(TAG, e);
  }
  return null;
 }

 private static void updateThreadCount(ThreadStatus ts, ClassLoader loader)
 {
  switch(ts)
  {
   case START:
    threadCount++;
    Log.showDialog("新线程开始"+threadCount,"");
    break;

   case END:
    threadCount--;
    Log.showDialog("线程结束"+threadCount,"");
    if(!hasMore && threadCount == 0)
    {
     hasMore = true;
     if(collectedEnergy == 0 &&
      helpCollectedEnergy == 0 && onceHelpCollected == 0)
     {
      Log.showDialogOrToast("暂时没有可收取的能量","");
     }else if(onceHelpCollected != 0)
     {
      helpCollectedEnergy += onceHelpCollected;
      onceHelpCollected = 0;
      Log.showDialog("再次收取能量…","");
      queryEnergyRanking(loader, 1);
     }else
     {
      Log.showDialogOrToast("共收取【"+collectedEnergy+"克】，帮收【"+helpCollectedEnergy+"克】","");
      collectedEnergy = 0;
      helpCollectedEnergy = 0;
      Config.saveIdMap();
     }
    }
    break;
  }
 }

 public static void saveUserIdAndName(String args0, String resp)
 {
  if(!args0.equals("alipay.antmember.forest.h5.queryNextAction"))
   return;
  try
  {
   JSONObject jo = new JSONObject(resp);
   if(jo.has("userEnergy"))
   {
    jo = jo.getJSONObject("userEnergy");
    String userName = jo.getString("displayName");
    String loginId = userName;
    if(jo.has("loginId"))
     loginId += "(" + jo.getString("loginId") + ")";
    Config.putIdMap(jo.getString("userId"), loginId);
    Config.saveIdMap();
   }
  }catch(Exception e)
  {
   Log.i(TAG, "saveUserIdAndName err:");
   Log.printStackTrace(TAG, e);
  }
 }

}
