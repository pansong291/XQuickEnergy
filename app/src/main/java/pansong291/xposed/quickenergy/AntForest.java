package pansong291.xposed.quickenergy;

import org.json.JSONArray;
import org.json.JSONObject;
import pansong291.xposed.quickenergy.AntFarm.TaskStatus;
import pansong291.xposed.quickenergy.AntForestNotification;
import pansong291.xposed.quickenergy.hook.AntForestRpcCall;
import pansong291.xposed.quickenergy.util.Config;
import pansong291.xposed.quickenergy.util.FriendIdMap;
import pansong291.xposed.quickenergy.util.Log;
import pansong291.xposed.quickenergy.util.RandomUtils;
import pansong291.xposed.quickenergy.util.Statistics;

public class AntForest
{
 private static final String TAG = AntForest.class.getCanonicalName();
 private static String selfId;
 private static int collectedEnergy = 0;
 private static int helpCollectedEnergy = 0;
 private static int totalCollected = 0;
 private static int totalHelpCollected = 0;
 private static int collectTaskCount = 0;
 public enum CollectStatus
 { AVAILABLE, WAITING, INSUFFICIENT, ROBBED }
 public enum TaskAwardType
 {
  BUBBLE_BOOST, DRESS;
  public static final CharSequence[] nickNames =
  {"时光加速器","装扮"};
  public CharSequence nickName()
  {
   return nickNames[ordinal()];
  }
 }

 private static long serverTime = -1;
 private static long offsetTime = -1;
 private static long laterTime = -1;

 private static void queryEnergyRanking(ClassLoader loader, String startPoint)
 {
  boolean hasMore = false;
  try
  {
   String s;
   JSONObject jo;
   do
   {
    s = AntForestRpcCall.rpcCall_queryEnergyRanking(loader, startPoint);
    jo = new JSONObject(s);
    if(jo.getString("resultCode").equals("SUCCESS"))
    {
     hasMore = jo.getBoolean("hasMore");
     startPoint = jo.getString("nextStartPoint");
     JSONArray jaFriendRanking = jo.getJSONArray("friendRanking");
     for(int i = 0; i < jaFriendRanking.length(); i++)
     {
      jo = jaFriendRanking.getJSONObject(i);
      boolean optBoolean = jo.getBoolean("canCollectEnergy")
       || jo.getBoolean("canHelpCollect") || jo.getLong("canCollectLaterTime") > 0;
      String userId = jo.getString("userId");
      if(optBoolean && !userId.equals(selfId))
      {
       canCollectEnergy(loader, userId, true);
      }else
      {
       FriendIdMap.getNameById(userId);
      }
     }
    }else
    {
     Log.recordLog(jo.getString("resultDesc"), s);
    }
   }while(hasMore);
  }catch(Throwable t)
  {
   Log.i(TAG, "queryEnergyRanking err:");
   Log.printStackTrace(TAG, t);
  }
  onForestEnd(loader);
 }

 private static void canCollectSelfEnergy(ClassLoader loader, int times)
 {
  try
  {
   long start = System.currentTimeMillis();
   String s = AntForestRpcCall.rpcCall_queryNextAction(loader,FriendIdMap.getSelfId());
   long end = System.currentTimeMillis();
   if(s == null)
   {
    Thread.sleep(RandomUtils.delay());
    start = System.currentTimeMillis();
    s = AntForestRpcCall.rpcCall_queryNextAction(loader, FriendIdMap.getSelfId());
    end = System.currentTimeMillis();
   }
   JSONObject jo = new JSONObject(s);
   if(jo.getString("resultCode").equals("SUCCESS"))
   {
    serverTime = jo.getLong("now");
    offsetTime = (start + end) / 2 - serverTime;
    Log.i(TAG, "服务器时间：" + serverTime + "，本地减服务器时间差：" + offsetTime);
    JSONArray jaBubbles = jo.getJSONArray("bubbles");
    jo = jo.getJSONObject("userEnergy");
    selfId = jo.getString("userId");
    FriendIdMap.currentUid = selfId;
    String selfName = jo.getString("displayName");
    if(selfName == null || selfName.isEmpty())
     selfName = "我";
    FriendIdMap.putIdMap(selfId, selfName);
    Log.recordLog("进入【" + selfName + "】的蚂蚁森林", "");
    FriendIdMap.saveIdMap();
    if(Config.collectEnergy())
     for(int i = 0; i < jaBubbles.length(); i++)
     {
      jo = jaBubbles.getJSONObject(i);
      long bubbleId = jo.getLong("id");
      switch(CollectStatus.valueOf(jo.getString("collectStatus")))
      {
       case AVAILABLE:
        if(Config.getDontCollectList().contains(selfId))
         Log.recordLog("不偷取【" + selfName + "】", ", userId=" + selfId);
        else
         collectedEnergy += collectEnergy(loader, selfId, bubbleId, selfName, null);
        break;

       case WAITING:
        if(Config.getDontCollectList().contains(selfId))
         break;
        long produceTime = jo.getLong("produceTime");
        if(produceTime - serverTime < Config.checkInterval())
         execute(loader, selfName, selfId, null, bubbleId, produceTime);
        else
         setLaterTime(produceTime);
        break;
      }
     }
   }else
   {
    Log.recordLog(jo.getString("resultDesc"), s);
   }
   if(times == 0)
   {
    receiveTaskAward(loader);
    for(int i = 0; i < Config.getWaterFriendList().size(); i++)
    {
     String uid = Config.getWaterFriendList().get(i);
     if(selfId.equals(uid)) continue;
     int waterCount = Config.getWaterCountList().get(i);
     if(waterCount <= 0) continue;
     if(waterCount > 3) waterCount = 3;
     waterFriendEnergy(loader, uid, waterCount);
    }
    checkUnknownId(loader);
   }
  }catch(Throwable t)
  {
   Log.i(TAG, "canCollectSelfEnergy err:");
   Log.printStackTrace(TAG, t);
  }
 }

 private static void canCollectEnergy(ClassLoader loader, String userId, boolean laterCollect)
 {
  try
  {
   long start = System.currentTimeMillis();
   String s = AntForestRpcCall.rpcCall_queryNextAction(loader, userId);
   long end = System.currentTimeMillis();
   JSONObject jo = new JSONObject(s);
   if(jo.getString("resultCode").equals("SUCCESS"))
   {
    serverTime = jo.getLong("now");
    offsetTime = (start + end) / 2 - serverTime;
    Log.i(TAG, "服务器时间：" + serverTime + "，本地减服务器时间差：" + offsetTime);
    String bizNo = jo.getString("bizNo");
    JSONArray jaProps = jo.getJSONArray("usingUserProps");
    JSONArray jaBubbles = jo.getJSONArray("bubbles");
    jo = jo.getJSONObject("userEnergy");
    String userName = jo.getString("displayName");
    if(userName == null || userName.isEmpty())
     userName = "*null*";
    String loginId = userName;
    if(jo.has("loginId"))
     loginId += "(" + jo.getString("loginId") + ")";
    FriendIdMap.putIdMap(userId, loginId);
    Log.recordLog("进入【" + loginId + "】的蚂蚁森林", "");
    FriendIdMap.saveIdMap();
    for(int i = 0; i < jaProps.length(); i++)
    {
     JSONObject joProps = jaProps.getJSONObject(i);
     if("energyShield".equals(joProps.getString("type")))
     {
      if(joProps.getLong("endTime") > serverTime)
      {
       Log.recordLog("【" + userName + "】被能量罩保护着哟", "");
       return;
      }
     }
    }
    int collected = 0;
    int helped = 0;
    for(int i = 0; i < jaBubbles.length(); i++)
    {
     jo = jaBubbles.getJSONObject(i);
     long bubbleId = jo.getLong("id");
     switch(CollectStatus.valueOf(jo.getString("collectStatus")))
     {
      case AVAILABLE:
       if(Config.getDontCollectList().contains(userId))
        Log.recordLog("不偷取【" + userName + "】", ", userId=" + userId);
       else
        collected += collectEnergy(loader, userId, bubbleId, userName, bizNo);
       break;

      case WAITING:
       if(!laterCollect || Config.getDontCollectList().contains(userId))
        break;
       long produceTime = jo.getLong("produceTime");
       if(produceTime - serverTime < Config.checkInterval())
        execute(loader, userName, userId, bizNo, bubbleId, produceTime);
       else
        setLaterTime(produceTime);
       break;
     }
     if(jo.getBoolean("canHelpCollect"))
     {
      if(Config.helpFriendCollect())
      {
       if(Config.getDontHelpCollectList().contains(userId))
        Log.recordLog("不帮收【" + userName + "】", ", userId=" + userId);
       else
        helped += forFriendCollectEnergy(loader, userId, bubbleId, userName);
      }else
       Log.recordLog("不帮收【" + userName + "】", ", userId=" + userId);
     }
    }
    if(helped > 0)
    {
     canCollectEnergy(loader, userId, false);
    }
    collectedEnergy += collected;
   }else
   {
    Log.recordLog(jo.getString("resultDesc"), s);
   }
  }catch(Throwable t)
  {
   Log.i(TAG, "canCollectEnergy err:");
   Log.printStackTrace(TAG, t);
  }
 }

 private static int collectEnergy(ClassLoader loader, String userId, long bubbleId, String userName, String bizNo)
 {
  int collected = 0;
  try
  {
   String s = AntForestRpcCall.rpcCall_collectEnergy(loader, userId, bubbleId);
   JSONObject jo = new JSONObject(s);
   if(jo.getString("resultCode").equals("SUCCESS"))
   {
    JSONArray jaBubbles = jo.getJSONArray("bubbles");
    for(int i = 0; i < jaBubbles.length(); i++)
    {
     jo = jaBubbles.getJSONObject(i);
     collected += jo.getInt("collectedEnergy");
    }
    if(collected > 0)
    {
     totalCollected += collected;
     Statistics.addData(Statistics.DataType.COLLECTED, collected);
     String str = "偷取【" + userName + "】的能量【" + collected + "克】";
     Log.forest(str);
     AntForestToast.show(str);
    }else
    {
     Log.recordLog("偷取【" + userName + "】的能量失败", "，UserID：" + userId + "，BubbleId：" + bubbleId);
    }
    if(bizNo == null || bizNo.isEmpty())
     return collected;
    int returnCount = 0;
    if(Config.returnWater30() > 0 && collected >= Config.returnWater30())
     returnCount = 3;
    else if(Config.returnWater20() > 0 && collected >= Config.returnWater20())
     returnCount = 2;
    else if(Config.returnWater10() > 0 && collected >= Config.returnWater10())
     returnCount = 1;
    if(returnCount > 0)
     returnFriendWater(loader, userId, userName, bizNo, returnCount);
   }else
   {
    Log.recordLog("【" + userName + "】" + jo.getString("resultDesc"), s);
   }
  }catch(Throwable t)
  {
   Log.i(TAG, "collectEnergy err:");
   Log.printStackTrace(TAG, t);
  }
  return collected;
 }

 private static int forFriendCollectEnergy(ClassLoader loader, String targetUserId, long bubbleId, String userName)
 {
  int helped = 0;
  try
  {
   String s = AntForestRpcCall.rpcCall_forFriendCollectEnergy(loader, targetUserId, bubbleId);
   JSONObject jo = new JSONObject(s);
   if(jo.getString("resultCode").equals("SUCCESS"))
   {
    JSONArray jaBubbles = jo.getJSONArray("bubbles");
    for(int i = 0; i < jaBubbles.length(); i++)
    {
     jo = jaBubbles.getJSONObject(i);
     helped += jo.getInt("collectedEnergy");
    }
    if(helped > 0)
    {
     Log.forest("帮【" + userName + "】收取【" + helped + "克】");
     helpCollectedEnergy += helped;
     totalHelpCollected += helped;
     Statistics.addData(Statistics.DataType.HELPED, helped);
    }else
    {
     Log.recordLog("帮【" + userName + "】收取失败", "，UserID：" + targetUserId + "，BubbleId" + bubbleId);
    }
   }else
   {
    Log.recordLog("【" + userName + "】" + jo.getString("resultDesc"), s);
   }
  }catch(Throwable t)
  {
   Log.i(TAG, "forFriendCollectEnergy err:");
   Log.printStackTrace(TAG, t);
  }
  return helped;
 }

 private static void waterFriendEnergy(ClassLoader loader, String userId, int count)
 {
  try
  {
   String s = AntForestRpcCall.rpcCall_queryNextAction(loader, userId);
   JSONObject jo = new JSONObject(s);
   if(jo.getString("resultCode").equals("SUCCESS"))
   {
    String bizNo = jo.getString("bizNo");
    jo = jo.getJSONObject("userEnergy");
    String userName = jo.getString("displayName");
    returnFriendWater(loader, userId, userName, bizNo, count);
   }else
   {
    Log.recordLog(jo.getString("resultDesc"), s);
   }
  }catch(Throwable t)
  {
   Log.i(TAG, "waterFriendEnergy err:");
   Log.printStackTrace(TAG, t);
  }
 }

 private static void returnFriendWater(ClassLoader loader, String userId, String userName, String bizNo, int count)
 {
  if(bizNo == null || bizNo.isEmpty()) return;
  try
  {
   String s;
   JSONObject jo;
   for(int waterCount = 1; waterCount <= count; waterCount++)
   {
    s = AntForestRpcCall.rpcCall_transferEnergy(loader, userId, bizNo, waterCount);
    jo = new JSONObject(s);
    s = jo.getString("resultCode");
    if(s.equals("SUCCESS"))
    {
     s = jo.getJSONObject("treeEnergy").getString("currentEnergy");
     Log.forest("给【" + userName + "】浇水成功，剩余能量【" + s + "克】");
     Statistics.addData(Statistics.DataType.WATERED, 10);
    }else if(s.equals("WATERING_TIMES_LIMIT"))
    {
     Log.recordLog("今日给【" + userName + "】浇水已达上限", "");
     break;
    }else
    {
     Log.recordLog(jo.getString("resultDesc"), jo.toString());
    }
    Thread.sleep(2000);
   }
  }catch(Throwable t)
  {
   Log.i(TAG, "returnFriendWater err:");
   Log.printStackTrace(TAG, t);
  }
 }

 private static void receiveTaskAward(ClassLoader loader)
 {
  try
  {
   String s = AntForestRpcCall.rpcCall_queryTaskList(loader);
   JSONObject jo = new JSONObject(s);
   if(jo.getString("resultCode").equals("SUCCESS"))
   {
    JSONArray jaForestTaskVOList = jo.getJSONArray("forestTaskVOList");
    for(int i = 0; i < jaForestTaskVOList.length(); i++)
    {
     jo = jaForestTaskVOList.getJSONObject(i);
     if(TaskStatus.FINISHED.name().equals(jo.getString("taskStatus")))
     {
      String taskAwardTypeStr = jo.getString("awardType");
      String awardName = null;
      if(taskAwardTypeStr.endsWith(TaskAwardType.DRESS.name()))
      {
       awardName = TaskAwardType.DRESS.nickName().toString();
      }else if(TaskAwardType.BUBBLE_BOOST.name().equals(taskAwardTypeStr))
      {
       awardName = TaskAwardType.BUBBLE_BOOST.nickName().toString();
      }
      int awardCount = jo.getInt("awardCount");
      s = AntForestRpcCall.rpcCall_receiveTaskAward(loader, jo.getString("taskType"));
      jo = new JSONObject(s);
      s = jo.getString("desc");
      if(s.equals("SUCCESS"))
       Log.forest("已领取【" + awardCount + "个】【" + awardName + "】");
      else
       Log.recordLog("领取失败，" + s, jo.toString());
     }
    }
   }else
   {
    Log.recordLog(jo.getString("resultDesc"), s);
   }
  }catch(Throwable t)
  {
   Log.i(TAG, "receiveTaskAward err:");
   Log.printStackTrace(TAG, t);
  }
 }

 private static void queryPropList(ClassLoader loader)
 {
  try
  {
   String s = AntForestRpcCall.rpcCall_queryTaskList(loader);
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
    Log.recordLog(jo.getString("resultDesc"), s);
   }
  }catch(Throwable t)
  {
   Log.i(TAG, "queryTaskList err:");
   Log.printStackTrace(TAG, t);
  }
 }

 private static void setLaterTime(long time)
 {
  Log.i(TAG, "能量成熟时间：" + time);
  if(time > serverTime && serverTime > 0
     && (laterTime < 0 || time < laterTime))
  {
   laterTime = time;
   Log.i(TAG, laterTime - serverTime + "ms 后能量成熟");
  }
 }

 private static void onForestEnd(ClassLoader loader)
 {
  Log.recordLog(
   "收【" + collectedEnergy + "克】，帮【"
   + helpCollectedEnergy + "克】，"
   + collectTaskCount + "个蹲点任务", "");
  FriendIdMap.saveIdMap();
  collectedEnergy = 0;
  helpCollectedEnergy = 0;
  if(Config.collectEnergy())
  {
   StringBuilder sb = new StringBuilder();
   sb.append("  收：" + totalCollected + "，帮：" + totalHelpCollected);
   if(laterTime > 0)
   {
    sb.append("，下个：");
    long second = (laterTime - serverTime) / 1000;
    long minute = second / 60;
    second %= 60;
    long hour = minute / 60;
    minute %= 60;
    if(hour > 0) sb.append(hour + "时");
    if(minute > 0) sb.append(minute + "分");
    sb.append(second + "秒");
   }
   Log.recordLog(sb.toString(), "");
   AntForestNotification.setContentText(Log.getFormatTime() + sb.toString());
  }
  laterTime = -1;
 }

 public static void checkEnergyRanking(ClassLoader loader, int times)
 {
  Log.recordLog("定时检测开始", "");
  new Thread()
  {
   ClassLoader loader;
   int times;

   public Thread setData(ClassLoader cl, int i)
   {
    loader = cl;
    times = i;
    return this;
   }

   @Override
   public void run()
   {
    canCollectSelfEnergy(loader, times);
    if(Config.collectEnergy())
     queryEnergyRanking(loader, "1");
   }
  }.setData(loader, times).start();
 }

 public static void checkUnknownId(ClassLoader loader)
 {
  String[] unknownIds = FriendIdMap.getUnknownIds();
  if(unknownIds != null)
  {
   new Thread()
   {
    ClassLoader loader;
    String[] unknownIds;
    public Thread setData(ClassLoader cl, String[] ss)
    {
     loader = cl;unknownIds = ss;
     return this;
    }

    @Override
    public void run()
    {
     try
     {
      Log.i(TAG, "开始检查" + unknownIds.length + "个未知id");
      for(int i = 0; i < unknownIds.length; i++)
      {
       long start = System.currentTimeMillis();
       String s = AntForestRpcCall.rpcCall_queryNextAction(loader, unknownIds[i]);
       long end = System.currentTimeMillis();
       JSONObject jo = new JSONObject(s);
       if(jo.getString("resultCode").equals("SUCCESS"))
       {
        serverTime = jo.getLong("now");
        offsetTime = (start + end) / 2 - serverTime;
        Log.i(TAG, "服务器时间：" + serverTime + "，本地减服务器时间差：" + offsetTime);
        jo = jo.getJSONObject("userEnergy");
        String userName = jo.getString("displayName");
        if(userName == null || userName.isEmpty())
         userName = "*null*";
        String loginId = userName;
        if(jo.has("loginId"))
         loginId += "(" + jo.getString("loginId") + ")";
        FriendIdMap.putIdMap(unknownIds[i], loginId);
        Log.recordLog("进入【" + loginId + "】的蚂蚁森林", "");
        FriendIdMap.saveIdMap();
       }
      }
     }catch(Throwable t)
     {
      Log.i(TAG, "checkUnknownId.run err:");
      Log.printStackTrace(TAG, t);
     }
    }
   }.setData(loader, unknownIds).start();
  }
 }

 public static void execute(ClassLoader loader, String userName, String userId, String bizNo, long bubbleId, long produceTime)
 {
  for(int i = Config.threadCount(); i > 0; i--)
  {
   BubbleTimerTask btt = new BubbleTimerTask(loader, userName, userId, bizNo, bubbleId, produceTime);
   long delay = btt.getDelayTime();
   btt.start();
   collectTaskCount++;
   Log.recordLog(delay / 1000 + "秒后尝试收取能量", "");
  }
 }

 public static class BubbleTimerTask extends Thread
 {
  ClassLoader loader;
  String userName;
  String userId;
  String bizNo;
  long bubbleId;
  long produceTime;
  long sleep = 0;

  BubbleTimerTask(ClassLoader cl, String un, String ui, String bn, long bi, long pt)
  {
   loader = cl;
   userName = un;
   bizNo = bn;
   userId = ui;
   bubbleId = bi;
   produceTime = pt;
  }

  public long getDelayTime()
  {
   sleep = produceTime + offsetTime - System.currentTimeMillis() - Config.advanceTime();
   return  sleep;
  }

  @Override
  public void run()
  {
   try
   {
    if(sleep > 0) sleep(sleep);
    Log.recordLog("【" + userName + "】蹲点收取开始" + collectTaskCount, "");
    collectTaskCount--;
    long time = System.currentTimeMillis();
    int collected = 0;
    while(System.currentTimeMillis() - time < Config.collectTimeout())
    {
     collected = collectEnergy(loader, userId, bubbleId, userName, bizNo);
     if(collected > 0) break;
     if(Config.collectInterval() > 0)
      sleep(Config.collectInterval());
    }
   }catch(Throwable t)
   {
    Log.i(TAG, "BubbleTimerTask.run err:");
    Log.printStackTrace(TAG, t);
   }
   String s = "  收：" + totalCollected + "，帮：" + totalHelpCollected;
   Log.recordLog(s, "");
   AntForestNotification.setContentText(Log.getFormatTime() + s);
  }

 }

}
