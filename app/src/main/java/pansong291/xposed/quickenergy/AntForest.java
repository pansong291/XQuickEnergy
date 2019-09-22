package pansong291.xposed.quickenergy;

import android.content.Intent;
import java.util.Timer;
import java.util.TimerTask;
import org.json.JSONArray;
import org.json.JSONObject;
import pansong291.xposed.quickenergy.AntFarm.TaskStatus;
import pansong291.xposed.quickenergy.AntForestNotification;

public class AntForest
{
 private static final String TAG = AntForest.class.getCanonicalName();
 private static String selfId;
 private static int collectedEnergy = 0;
 private static int helpCollectedEnergy = 0;
 private static int onceHelpCollected = 0;
 private static int totalCollected = 0;
 private static int totalHelpCollected = 0;
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

 public enum ThreadStatus
 { START, END }
 private static int threadCount = 0;
 private static boolean hasMore = true;
 private static boolean checkingIds = false;
 private static long serverTime = -1;
 private static long offsetTime = -1;
 private static long laterTime = -1;
 private static final Timer timer = new Timer(true);
// private static boolean hasNextTask = false;
// private static long nextTime = -1;

 public static void start(ClassLoader loader, String args0, String args1, String resp)
 {
  if(!args0.equals("alipay.antmember.forest.h5.queryNextAction") ||
     !args1.contains("\"userId\":\"\"") && args1.contains("\"userId\""))
   return;

  new Thread(
   new Runnable()
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
     updateThreadCount(ThreadStatus.START, loader);
     Log.showToastAndRecordLog("森林功能开始…", "");
     Log.resetDialog();
     try
     {
      checkUnknownId(loader);
      if(Config.enableForest() && Config.collectEnergy())
      {
       Log.showDialogAndRecordLog("开始收取能量…", "");
       queryEnergyRanking(loader, "1");
       canCollectSelfEnergy(loader, resp);
      }

      for(String userId: Config.getWaterFriendList())
      {
       waterFriendEnergy(loader, userId, 3);
      }

      if(Config.receiveForestTaskAward())receiveTaskAward(loader);
     }catch(Throwable t)
     {
      Log.i(TAG, "start err:");
      Log.printStackTrace(TAG, t);
     }
     updateThreadCount(ThreadStatus.END, loader);
    }
   }.setData(loader, resp)).start();

 }

 private static void queryEnergyRanking(ClassLoader loader, String startPoint)
 {
  new Thread(
   new Runnable()
   {
    ClassLoader loader;
    String startPoint;

    public Runnable setData(ClassLoader cl, String sp)
    {
     loader = cl;startPoint = sp;
     return this;
    }

    @Override
    public void run()
    {
     updateThreadCount(ThreadStatus.START, loader);
     try
     {
      String s = rpcCall_queryEnergyRanking(loader, startPoint);
      JSONObject jo = new JSONObject(s);
      if(jo.getString("resultCode").equals("SUCCESS"))
      {
       hasMore = jo.getBoolean("hasMore");
       if(hasMore)
        queryEnergyRanking(loader, jo.getString("nextStartPoint"));
       JSONArray jaFriendRanking = jo.getJSONArray("friendRanking");
       for(int i = 0; i < jaFriendRanking.length(); i++)
       {
        jo = jaFriendRanking.getJSONObject(i);
        boolean optBoolean = jo.getBoolean("canCollectEnergy")
         || jo.getBoolean("canHelpCollect") || jo.getLong("canCollectLaterTime") > 0;
        String userId = jo.getString("userId");
        if(optBoolean && !userId.equals(selfId))
        {
         canCollectEnergy(loader, userId);
        }else
        {
         Config.getNameById(userId);
        }
       }
      }else
      {
       Log.showDialogAndRecordLog(jo.getString("resultDesc"), s);
      }
     }catch(Throwable t)
     {
      Log.i(TAG, "queryEnergyRanking err:");
      Log.printStackTrace(TAG, t);
      hasMore = false;
     }
     updateThreadCount(ThreadStatus.END, loader);
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
    selfId = jo.getString("userId");
    String userName = jo.getString("displayName");
    Config.putIdMap(selfId, userName);
    for(int i = 0; i < jaBubbles.length(); i++)
    {
     jo = jaBubbles.getJSONObject(i);
     long bubbleId = jo.getLong("id");
     switch(CollectStatus.valueOf(jo.getString("collectStatus")))
     {
      case AVAILABLE:
       if(Config.dontCollect(selfId))
        Log.showDialogAndRecordLog("不偷取【" + userName + "】", ", userId=" + selfId);
       else
        collectEnergy(loader, selfId, bubbleId, userName);
       break;

      case WAITING:
       if(Config.dontCollect(selfId))
        break;
       long produceTime = jo.getLong("produceTime");
       setLaterTime(produceTime);
       BubbleTask.addTask(bubbleId, selfId, produceTime);
       break;
     }
    }
   }else
   {
    Log.showDialogAndRecordLog(jo.getString("resultDesc"), resp);
   }
  }catch(Throwable t)
  {
   Log.i(TAG, "canCollectSelfEnergy err:");
   Log.printStackTrace(TAG, t);
  }
 }

 private static void canCollectEnergy(ClassLoader loader, String userId)
 {
  try
  {
   String s = rpcCall_queryNextAction(loader, userId);
   JSONObject jo = new JSONObject(s);
   String bizNo;
   if(jo.getString("resultCode").equals("SUCCESS"))
   {
    bizNo = jo.getString("bizNo");
    JSONArray jaBubbles = jo.getJSONArray("bubbles");
    jo = jo.getJSONObject("userEnergy");
    String userName = jo.getString("displayName");
    int collected = 0;
    for(int i = 0; i < jaBubbles.length(); i++)
    {
     jo = jaBubbles.getJSONObject(i);
     long bubbleId = jo.getLong("id");
     switch(CollectStatus.valueOf(jo.getString("collectStatus")))
     {
      case AVAILABLE:
       if(Config.dontCollect(userId))
        Log.showDialogAndRecordLog("不偷取【" + userName + "】", ", userId=" + userId);
       else
        collectEnergy(loader, userId, bubbleId, userName);
       break;

      case WAITING:
       if(Config.dontCollect(userId))
        break;
       long produceTime = jo.getLong("produceTime");
       setLaterTime(produceTime);
       BubbleTask.addTask(bubbleId, userId, produceTime);
       break;
     }
     if(jo.getBoolean("canHelpCollect"))
     {
      if(Config.helpFriendCollect())
      {
       if(Config.dontHelpCollect(userId))
        Log.showDialogAndRecordLog("不帮收【" + userName + "】", ", userId=" + userId);
       else
        forFriendCollectEnergy(loader, userId, bubbleId, userName);
      }else
       Log.showDialogAndRecordLog("不帮收【" + userName + "】", ", userId=" + userId);
     }
    }
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
    Log.showDialogAndRecordLog(jo.getString("resultDesc"), s);
   }
  }catch(Throwable t)
  {
   Log.i(TAG, "canCollectEnergy err:");
   Log.printStackTrace(TAG, t);
  }
 }

 private static int collectEnergy(ClassLoader loader, String userId, long bubbleId, String userName)
 {
  try
  {
   String s = rpcCall_collectEnergy(loader, userId, bubbleId);
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
     Log.showDialogAndRecordLog("偷取【" + userName + "】的能量【" + collected + "克】", "，UserID：" + userId + "，BubbleId：" + bubbleId);
     collectedEnergy += collected;
     totalCollected += collected;
     Statistics.addData(Statistics.DataType.COLLECTED, collected);
    }else
    {
     Log.showDialogAndRecordLog("偷取【" + userName + "】的能量失败", "，UserID：" + userId + "，BubbleId：" + bubbleId);
    }
    return collected;
   }else
   {
    s = jo.getString("resultDesc");
    if(s.contains("TA"))
     s = s.replace("TA", "【" + userName + "】");
    Log.showDialogAndRecordLog(s, jo.toString());
   }
  }catch(Throwable t)
  {
   Log.i(TAG, "collectEnergy err:");
   Log.printStackTrace(TAG, t);
  }
  return 0;
 }

 private static void forFriendCollectEnergy(ClassLoader loader, String targetUserId, long bubbleId, String userName)
 {
  try
  {
   String s = rpcCall_forFriendCollectEnergy(loader, targetUserId, bubbleId);
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
     Log.showDialogAndRecordLog("帮【" + userName + "】收取【" + helped + "克】", "，UserID：" + targetUserId + "，BubbleId：" + bubbleId);
     onceHelpCollected += helped;
     Statistics.addData(Statistics.DataType.HELPED, helped);
    }else
    {
     Log.showDialogAndRecordLog("帮【" + userName + "】收取失败", "，UserID：" + targetUserId + "，BubbleId" + bubbleId);
    }
   }else
   {
    s = jo.getString("resultDesc");
    if(s.contains("TA"))
     s = s.replace("TA", "【" + userName + "】");
    Log.showDialogAndRecordLog(s, jo.toString());
   }
  }catch(Throwable t)
  {
   Log.i(TAG, "forFriendCollectEnergy err:");
   Log.printStackTrace(TAG, t);
  }
 }

 private static void waterFriendEnergy(ClassLoader loader, String userId, int count)
 {
  try
  {
   String s = rpcCall_queryNextAction(loader, userId);
   JSONObject jo = new JSONObject(s);
   if(jo.getString("resultCode").equals("SUCCESS"))
   {
    String bizNo = jo.getString("bizNo");
    jo = jo.getJSONObject("userEnergy");
    String userName = jo.getString("displayName");
    returnFriendWater(loader, userId, userName, bizNo, count);
   }else
   {
    Log.showDialogAndRecordLog(jo.getString("resultDesc"), s);
   }
  }catch(Throwable t)
  {
   Log.i(TAG, "waterFriendEnergy err:");
   Log.printStackTrace(TAG, t);
  }
 }

 private static void returnFriendWater(ClassLoader loader, String userId, String userName, String bizNo, int count)
 {
  try
  {
   String s;
   JSONObject jo;
   for(int waterCount = 1; waterCount <= count; waterCount++)
   {
    s = rpcCall_transferEnergy(loader, userId, bizNo, waterCount);
    jo = new JSONObject(s);
    s = jo.getString("resultCode");
    if(s.equals("SUCCESS"))
    {
     s = jo.getJSONObject("treeEnergy").getString("currentEnergy");
     Log.showDialogAndRecordLog("给【" + userName + "】浇水成功，剩余能量【" + s + "克】", "");
     Statistics.addData(Statistics.DataType.WATERED, 10);
     Thread.sleep(2000);
    }else if(s.equals("WATERING_TIMES_LIMIT"))
    {
     Log.showDialogAndRecordLog("今日给【" + userName + "】浇水已达上限", "");
     break;
    }else if(s.equals("LOW_VERSION"))
    {
     Log.showDialogAndRecordLog("给【" + userName + "】浇水失败，" + s, "");
     break;
    }else
    {
     Log.showDialogAndRecordLog(jo.getString("resultDesc"), jo.toString());
    }
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
   Log.showDialogAndRecordLog("开始领取任务奖励…", "");
   String s = rpcCall_queryTaskList(loader);
   JSONObject jo = new JSONObject(s);
   if(jo.getString("resultCode").equals("SUCCESS"))
   {
    boolean hasCanReceive = false;
    JSONArray jaForestTaskVOList = jo.getJSONArray("forestTaskVOList");
    for(int i = 0; i < jaForestTaskVOList.length(); i++)
    {
     jo = jaForestTaskVOList.getJSONObject(i);
     if(TaskStatus.FINISHED.name().equals(jo.getString("taskStatus")))
     {
      hasCanReceive = true;
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
      s = rpcCall_receiveTaskAward(loader, jo.getString("taskType"));
      jo = new JSONObject(s);
      s = jo.getString("desc");
      if(s.equals("SUCCESS"))
       Log.showDialogAndRecordLog("已领取【" + awardCount + "个】【" + awardName + "】", "");
      else
       Log.showDialogAndRecordLog("领取失败，" + s, jo.toString());
     }
    }
    if(!hasCanReceive)
     Log.showDialogAndRecordLog("暂时没有可领取的任务奖励", "");
   }else
   {
    Log.showDialogAndRecordLog(jo.getString("resultDesc"), s);
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
    Log.showDialogAndRecordLog(jo.getString("resultDesc"), s);
   }
  }catch(Throwable t)
  {
   Log.i(TAG, "queryTaskList err:");
   Log.printStackTrace(TAG, t);
  }
 }

 private static String rpcCall_queryEnergyRanking(ClassLoader loader, String startPoint)
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

 private static String rpcCall_queryNextAction(ClassLoader loader, String userId)
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

 private static String rpcCall_collectEnergy(ClassLoader loader, String userId, long bubbleId)
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

 private static String rpcCall_transferEnergy(ClassLoader loader, String targetUser, String bizNo, int ordinal)
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

 private static String rpcCall_forFriendCollectEnergy(ClassLoader loader, String targetUserId, long bubbleId)
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

 private static String rpcCall_queryTaskList(ClassLoader loader)
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

 private static String rpcCall_receiveTaskAward(ClassLoader loader, String taskType)
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

 private static void setLaterTime(long time)
 {
  if(time > serverTime && serverTime > 0
     && (laterTime < 0 || time < laterTime))
  {
   laterTime = time;
   Log.i(TAG, laterTime - serverTime + "ms 后能量成熟");
  }
 }

 private static synchronized void updateThreadCount(ThreadStatus ts, ClassLoader loader)
 {
  switch(ts)
  {
   case START:
    threadCount++;
    Log.i(TAG, "新线程开始" + threadCount);
    break;

   case END:
    threadCount--;
    Log.i(TAG, "线程结束" + threadCount);
    if(!hasMore && threadCount == 0)
    {
     hasMore = true;
     if(collectedEnergy == 0 &&
        helpCollectedEnergy == 0 && onceHelpCollected == 0)
     {
      Log.showDialogOrToastAndRecordLog("暂时没有可收取的能量", "");
      onForestEnd(loader);
     }else if(onceHelpCollected != 0)
     {
      helpCollectedEnergy += onceHelpCollected;
      onceHelpCollected = 0;
      Log.showDialogAndRecordLog("再次收取能量…", "");
      queryEnergyRanking(loader, "1");
     }else
     {
      Log.showDialogOrToastAndRecordLog("共收取【" + collectedEnergy + "克】，帮收【" + helpCollectedEnergy + "克】", "");
      totalHelpCollected += helpCollectedEnergy;
      Config.saveIdMap();
      collectedEnergy = 0;
      helpCollectedEnergy = 0;
      onForestEnd(loader);
     }
    }
    break;
  }
 }

 private static void onForestEnd(ClassLoader loader)
 {
  Log.showToastAndRecordLog("森林功能结束", "");
  if(Config.enableForest() && Config.collectEnergy() && Config.onTimeCollect())
  {
   if(RpcCall.loginActivity != null)
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

   while(BubbleTask.size() > 0)
   {
    if(BubbleTask.getDelayTime(0, offsetTime) < Config.timeInterval())
    {
     BubbleTask bt = BubbleTask.outputTopTask();
     long delay = bt.getDelayTime(offsetTime);
     timer.schedule(new BubbleTimerTask(loader, bt), delay);
     Log.showDialogOrToastAndRecordLog(delay / 1000 + "秒后尝试收取能量", "");
    }else break;
   }
   laterTime = -1;
  }
//  long delay = laterTime + offsetTime - System.currentTimeMillis();
//  if(Config.enableForest() && Config.collectEnergy()
//     && Config.onTimeCollect() && delay > 0
//     && delay < Config.timeInterval())
//  {
//   laterTime = -1;
//   if(nextTime < System.currentTimeMillis())
//    hasNextTask = false;
//   if(!hasNextTask)
//   {
//    hasNextTask = true;
//    delay += 1000;
//    nextTime = System.currentTimeMillis() + delay;
//    Log.showDialogOrToastAndRecordLog(delay / 1000 + "秒后尝试检测能量", "");
//    timer.schedule(
//     new TimerTask()
//     {
//      ClassLoader loader;
//
//      public TimerTask setData(ClassLoader cl)
//      {
//       loader = cl;
//       return this;
//      }
//
//      @Override
//      public void run()
//      {
//       checkEnergyRanking(loader);
//       hasNextTask = false;
//      }
//     }.setData(loader), delay);
//   }else
//   {
//    Log.showDialogOrToastAndRecordLog(
//     (nextTime - System.currentTimeMillis()) / 1000
//     + "秒后已有检测任务", "");
//   }
//  }
 }

 public static void checkEnergyRanking(ClassLoader loader)
 {
  if(!Config.enableForest() || !Config.collectEnergy() || !Config.onTimeCollect())
   return;
  Log.recordLog("定时执行开始", "");
  new Thread(
   new Runnable()
   {
    ClassLoader loader;

    public Runnable setData(ClassLoader cl)
    {
     loader = cl;
     return this;
    }

    @Override
    public void run()
    {
     String s = rpcCall_queryNextAction(loader, Config.getSelfId());
     queryEnergyRanking(loader, "1");
     canCollectSelfEnergy(loader, s);
    }
   }.setData(loader)).start();
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
    serverTime = jo.getLong("now");
    offsetTime = System.currentTimeMillis() - serverTime;
    jo = jo.getJSONObject("userEnergy");
    String userName = jo.getString("displayName");
    String loginId = userName;
    if(jo.has("loginId"))
     loginId += "(" + jo.getString("loginId") + ")";
    if(loginId == null || loginId.isEmpty())
     loginId = "*null*";
    Config.putIdMap(jo.getString("userId"), loginId);
    Log.recordLog("进入【" + loginId + "】的蚂蚁森林", "");
    Config.saveIdMap();
   }
  }catch(Throwable t)
  {
   Log.i(TAG, "saveUserIdAndName err:");
   Log.printStackTrace(TAG, t);
  }
 }

 public static void checkUnknownId(ClassLoader loader)
 {
  if(checkingIds) return;
  String[] unknownIds = Config.getUnknownIds();
  if(unknownIds != null)
  {
   checkingIds = true;
   new Thread(
    new Runnable()
    {
     ClassLoader loader;
     String[] unknownIds;
     public Runnable setData(ClassLoader cl, String[] ss)
     {
      loader = cl;unknownIds = ss;
      return this;
     }

     @Override
     public void run()
     {
      Log.i(TAG, "checking " + unknownIds.length + " unknown ids");
      for(int i = 0; i < unknownIds.length; i++)
      {
       rpcCall_queryNextAction(loader, unknownIds[i]);
      }
      checkingIds = false;
     }
    }.setData(loader, unknownIds)).start();
  }
 }

 static class BubbleTimerTask extends TimerTask
 {
  ClassLoader loader;
  BubbleTask bTask;

  BubbleTimerTask(ClassLoader cl, BubbleTask bt)
  {
   loader = cl;
   bTask = bt;
  }

  @Override
  public void run()
  {
   long time = System.currentTimeMillis();
   String userName = Config.getNameById(bTask.userId);
   int collected = 0;
   while(System.currentTimeMillis() - time < 5000)
   {
    collected = collectEnergy(loader, bTask.userId, bTask.bubbleId, userName);
    if(collected > 0) break;
   }
   int returnCount = 0;
   if(Config.returnWater30() > 0 && collected >= Config.returnWater30())
   {
    returnCount = 3;
   }else if(Config.returnWater20() > 0 && collected >= Config.returnWater20())
   {
    returnCount = 2;
   }else if(Config.returnWater10() > 0 && collected >= Config.returnWater10())
   {
    returnCount = 1;
   }
   if(returnCount > 0)
    try
    {
     JSONObject jo = new JSONObject(rpcCall_queryNextAction(loader, bTask.userId));
     if(jo.getString("resultCode").equals("SUCCESS"))
     {
      returnFriendWater(loader, bTask.userId, userName, jo.getString("bizNo"), returnCount);
     }
    }catch(Throwable t)
    {
     Log.i(TAG, "BubbleTimerTask.run err:");
     Log.printStackTrace(TAG, t);
    }
   AntForestNotification.setContentText(Log.getFormatTime() + "  收：" + totalCollected + "，帮：" + totalHelpCollected);
  }

 }

}
