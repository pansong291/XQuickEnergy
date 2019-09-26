package pansong291.xposed.quickenergy.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.json.JSONArray;
import org.json.JSONObject;
import pansong291.xposed.quickenergy.AntFarm.SendType;

public class Config
{
 public enum RecallAnimalType
 {
  ALWAYS, WHEN_THIEF, WHEN_HUNGRY, NEVER;
  public static final CharSequence[] nickNames =
  {"始终召回", "作贼时召回", "饥饿时召回", "不召回"};
  public CharSequence nickName()
  {
   return nickNames[ordinal()];
  }
 }

 private static final String TAG = Config.class.getCanonicalName();
 public static final String
 /* application */
 jn_immediateEffect = "immediateEffect", jn_recordLog = "recordLog",
 /* forest */
 jn_collectEnergy = "collectEnergy", jn_ReturnWater30 = "returnWater30", jn_ReturnWater20 = "returnWater20",
 jn_ReturnWater10 = "returnWater10", jn_helpFriendCollect = "helpFriendCollect", jn_dontCollectList = "dontCollectList",
 jn_dontHelpCollectList = "dontHelpCollectList", jn_onTimeCollect = "onTimeCollect", jn_timeInterval = "timeInterval",
 jn_advanceTime = "advanceTime", jn_collectInterval = "collectInterval", jn_collectTimeout = "collectTimeout",
 jn_receiveForestTaskAward = "receiveForestTaskAward", jn_waterFriendList = "waterFriendList",
 /* farm */
 jn_enableFarm = "enableFarm",
 jn_rewardFriend = "rewardFriend", jn_sendBackAnimal = "sendBackAnimal", jn_sendType = "sendType",
 jn_dontSendFriendList = "dontSendFriendList", jn_recallAnimalType = "recallAnimalType", jn_receiveFarmToolReward = "receiveFarmToolReward",
 jn_useNewEggTool = "useNewEggTool", jn_harvestProduce = "harvestProduce", jn_donation = "donation",
 jn_answerQuestion = "answerQuestion", jn_receiveFarmTaskAward = "receiveFarmTaskAward", jn_feedAnimal = "feedAnimal",
 jn_useAccelerateTool = "useAccelerateTool", jn_feedFriendAnimalList = "feedFriendAnimalList", jn_notifyFriend = "notifyFriend",
 jn_dontNotifyFriendList = "dontNotifyFriendList",
 /* member */
 jn_receivePoint = "receivePoint";

 public static boolean shouldReloadConfig;
 public static boolean hasConfigChanged;

 /* application */
 private boolean immediateEffect;
 private boolean recordLog;
 private boolean enableForest;
 private boolean enableFarm;

 /* forest */
 private boolean collectEnergy;
 private int timeInterval;
 private int advanceTime;
 private int collectInterval;
 private int collectTimeout;
 private int returnWater30;
 private int returnWater20;
 private int returnWater10;
 private boolean helpFriendCollect;
 private List<String> dontCollectList;
 private List<String> dontHelpCollectList;
 private boolean receiveForestTaskAward;
 private List<String> waterFriendList;

 /* farm */
 private boolean rewardFriend;
 private boolean sendBackAnimal;
 private SendType sendType;
 private List<String> dontSendFriendList;
 private RecallAnimalType recallAnimalType;
 private boolean receiveFarmToolReward;
 private boolean useNewEggTool;
 private boolean harvestProduce;
 private boolean donation;
 private boolean answerQuestion;
 private boolean receiveFarmTaskAward;
 private boolean feedAnimal;
 private boolean useAccelerateTool;
 private List<String> feedFriendAnimalList;
 private boolean notifyFriend;
 private List<String> dontNotifyFriendList;

 /* member */
 private boolean receivePoint;

 /* other */
 private boolean reInit;
 private static Config config;
 private static Map idMap;
 private static boolean hasIdMapChanged = false;
 private static String selfId;

 /* application */
 public static void setImmediateEffect(boolean b)
 {
  getConfig().immediateEffect = b;
  hasConfigChanged = true;
 }

 public static boolean immediateEffect()
 {
  return getConfig().immediateEffect;
 }

 public static void setRecordLog(boolean b)
 {
  getConfig().recordLog = b;
  hasConfigChanged = true;
 }

 public static boolean recordLog()
 {
  return getConfig().recordLog;
 }

 public static void setEnableForest(boolean b)
 {
  getConfig().enableForest = b;
  hasConfigChanged = true;
 }

 public static boolean enableForest()
 {
  return getConfig().enableForest;
 }

 public static void setEnableFarm(boolean b)
 {
  getConfig().enableFarm = b;
  hasConfigChanged = true;
 }

 public static boolean enableFarm()
 {
  return getConfig().enableFarm;
 }

 /* forest */
 public static void setCollectEnergy(boolean b)
 {
  getConfig().collectEnergy = b;
  hasConfigChanged = true;
 }

 public static boolean collectEnergy()
 {
  return getConfig().collectEnergy;
 }

 public static void setTimeInterval(int i)
 {
  getConfig().timeInterval = i;
  hasConfigChanged = true;
 }

 public static int timeInterval()
 {
  return getConfig().timeInterval;
 }

 public static void setAdvanceTime(int i)
 {
  getConfig().advanceTime = i;
  hasConfigChanged = true;
 }

 public static int advanceTime()
 {
  return getConfig().advanceTime;
 }

 public static void setCollectInterval(int i)
 {
  getConfig().collectInterval = i;
  hasConfigChanged = true;
 }

 public static int collectInterval()
 {
  return getConfig().collectInterval;
 }

 public static void setCollectTimeout(int i)
 {
  getConfig().collectTimeout = i;
  hasConfigChanged = true;
 }

 public static int collectTimeout()
 {
  return getConfig().collectTimeout;
 }

 public static void setReturnWater30(int i)
 {
  getConfig().returnWater30 = i;
  hasConfigChanged = true;
 }

 public static int returnWater30()
 {
  return getConfig().returnWater30;
 }

 public static void setReturnWater20(int i)
 {
  getConfig().returnWater20 = i;
  hasConfigChanged = true;
 }

 public static int returnWater20()
 {
  return getConfig().returnWater20;
 }

 public static void setReturnWater10(int i)
 {
  getConfig().returnWater10 = i;
  hasConfigChanged = true;
 }

 public static int returnWater10()
 {
  return getConfig().returnWater10;
 }

 public static void setHelpFriendCollect(boolean b)
 {
  getConfig().helpFriendCollect = b;
  hasConfigChanged = true;
 }

 public static boolean helpFriendCollect()
 {
  return getConfig().helpFriendCollect;
 }

 public static List<String> getDontCollectList()
 {
  return getConfig().dontCollectList;
 }

 public static List<String> getDontHelpCollectList()
 {
  return getConfig().dontHelpCollectList;
 }

 public static void setReceiveForestTaskAward(boolean b)
 {
  getConfig().receiveForestTaskAward = b;
  hasConfigChanged = true;
 }

 public static boolean receiveForestTaskAward()
 {
  return getConfig().receiveForestTaskAward;
 }

 public static List<String> getWaterFriendList()
 {
  return getConfig().waterFriendList;
 }

 /* farm */
 public static void setRewardFriend(boolean b)
 {
  getConfig().rewardFriend = b;
  hasConfigChanged = true;
 }

 public static boolean rewardFriend()
 {
  return getConfig().rewardFriend;
 }

 public static void setSendBackAnimal(boolean b)
 {
  getConfig().sendBackAnimal = b;
  hasConfigChanged = true;
 }

 public static boolean sendBackAnimal()
 {
  return getConfig().sendBackAnimal;
 }

 public static void setSendType(int i)
 {
  getConfig().sendType = SendType.values()[i];
  hasConfigChanged = true;
 }

 public static SendType sendType()
 {
  return getConfig().sendType;
 }

 public static List<String> getDontSendFriendList()
 {
  return getConfig().dontSendFriendList;
 }

 public static void setRecallAnimalType(int i)
 {
  getConfig().recallAnimalType = RecallAnimalType.values()[i];
  hasConfigChanged = true;
 }

 public static RecallAnimalType recallAnimalType()
 {
  return getConfig().recallAnimalType;
 }

 public static void setReceiveFarmToolReward(boolean b)
 {
  getConfig().receiveFarmToolReward = b;
  hasConfigChanged = true;
 }

 public static boolean receiveFarmToolReward()
 {
  return getConfig().receiveFarmToolReward;
 }

 public static void setUseNewEggTool(boolean b)
 {
  getConfig().useNewEggTool = b;
  hasConfigChanged = true;
 }

 public static boolean useNewEggTool()
 {
  return getConfig().useNewEggTool;
 }

 public static void setHarvestProduce(boolean b)
 {
  getConfig().harvestProduce = b;
  hasConfigChanged = true;
 }

 public static boolean harvestProduce()
 {
  return getConfig().harvestProduce;
 }

 public static void setDonation(boolean b)
 {
  getConfig().donation = b;
  hasConfigChanged = true;
 }

 public static boolean donation()
 {
  return getConfig().donation;
 }

 public static void setAnswerQuestion(boolean b)
 {
  getConfig().answerQuestion = b;
  hasConfigChanged = true;
 }

 public static boolean answerQuestion()
 {
  return getConfig().answerQuestion;
 }

 public static void setReceiveFarmTaskAward(boolean b)
 {
  getConfig().receiveFarmTaskAward = b;
  hasConfigChanged = true;
 }

 public static boolean receiveFarmTaskAward()
 {
  return getConfig().receiveFarmTaskAward;
 }

 public static void setFeedAnimal(boolean b)
 {
  getConfig().feedAnimal = b;
  hasConfigChanged = true;
 }

 public static boolean feedAnimal()
 {
  return getConfig().feedAnimal;
 }

 public static void setUseAccelerateTool(boolean b)
 {
  getConfig().useAccelerateTool = b;
  hasConfigChanged = true;
 }

 public static boolean useAccelerateTool()
 {
  return getConfig().useAccelerateTool;
 }

 public static List<String> getFeedFriendAnimalList()
 {
  return getConfig().feedFriendAnimalList;
 }

 public static void setNotifyFriend(boolean b)
 {
  getConfig().notifyFriend = b;
  hasConfigChanged = true;
 }

 public static boolean notifyFriend()
 {
  return getConfig().notifyFriend;
 }

 public static List<String> getDontNotifyFriendList()
 {
  return getConfig().dontNotifyFriendList;
 }

 /* member */
 public static void setReceivePoint(boolean b)
 {
  getConfig().receivePoint = b;
  hasConfigChanged = true;
 }

 public static boolean receivePoint()
 {
  return getConfig().receivePoint;
 }

 /* other */
 private static Config getConfig()
 {
  if(config == null || shouldReloadConfig && config.immediateEffect)
  {
   shouldReloadConfig = false;
   String confJson = null;
   if(FileUtils.getConfigFile().exists())
    confJson = FileUtils.readFromFile(FileUtils.getConfigFile());
   config = json2Config(confJson);
  }
  return config;
 }

 public static void putIdMap(String key, String value)
 {
  if(key == null || key.isEmpty()) return;
  if(getIdMap().containsKey(key))
  {
   if(!getIdMap().get(key).equals(value))
   {
    getIdMap().remove(key);
    getIdMap().put(key, value);
    hasIdMapChanged = true;
   }
  }else
  {
   getIdMap().put(key, value);
   hasIdMapChanged = true;
  }
 }

 public static boolean saveIdMap()
 {
  if(hasIdMapChanged)
  {
   StringBuilder sb = new StringBuilder();
   Set idSet = getIdMap().entrySet();
   for(Map.Entry entry: idSet)
   {
    sb.append(entry.getKey());
    sb.append(':');
    sb.append(entry.getValue());
    sb.append('\n');
   }
   hasIdMapChanged = !FileUtils.write2File(sb.toString(), FileUtils.getFriendIdMapFile());
  }
  return hasIdMapChanged;
 }

 public static String getSelfId()
 {
  if(selfId == null)
  {
   Set idSet = getIdMap().entrySet();
   for(Map.Entry entry: idSet)
    if(!entry.getValue().toString().contains("*"))
    {
     selfId = entry.getKey().toString();
     break;
    }
  }
  return selfId;
 }

 public static String getNameById(String id)
 {
  if(id == null || id.isEmpty()) return id;
  if(getIdMap().containsKey(id))
  {
   String n = getIdMap().get(id).toString();
   int ind = n.indexOf('(');
   if(ind > 0) n = n.substring(0, ind);
   if(!n.equals("*")) return n;
  }else
  {
   putIdMap(id, "*(*)");
  }
  return id;
 }

 public static String[] getUnknownIds()
 {
  List<String> idList = new ArrayList<String>();
  Set idSet = getIdMap().entrySet();
  for(Map.Entry entry: idSet)
   if(entry.getValue().toString().contains("(*)"))
    idList.add(entry.getKey().toString());
  if(idList.size() > 0)
  {
   String[] ids = new String[idList.size()];
   for(int i = 0; i < ids.length; i++)
   {
    ids[i] = idList.get(i);
    Log.i(TAG, "未知id: " + ids[i]);
   }
   return ids;
  }
  return null;
 }

 public static Map getIdMap()
 {
  if(idMap == null)
  {
   idMap = new TreeMap<>();
   String str = FileUtils.readFromFile(FileUtils.getFriendIdMapFile());
   if(str != null && str.length() > 0)
   {
    try
    {
     String[] idSet = str.split("\n");
     for(String s: idSet)
     {
      Log.i(TAG, s);
      String[] entry = s.split(":");
      idMap.put(entry[0], entry[1]);
     }
    }catch(Throwable t)
    {
     Log.printStackTrace(TAG, t);
     idMap.clear();
    }
   }
  }
  return idMap;
 }

 public static Config defInit()
 {
  Config c = new Config();
  c.reInit = true;

  c.immediateEffect = true;
  c.recordLog = true;
  c.enableForest = true;
  c.enableFarm = true;

  c.collectEnergy = true;
  c.timeInterval = 120_000;
  c.advanceTime = 500;
  c.collectInterval = 100;
  c.collectTimeout = 2_000;
  c.returnWater30 = 0;
  c.returnWater20 = 0;
  c.returnWater10 = 0;
  c.helpFriendCollect = true;
  if(c.dontCollectList == null) c.dontCollectList = new ArrayList<>();
  if(c.dontHelpCollectList == null) c.dontHelpCollectList = new ArrayList<>();
  c.receiveForestTaskAward = true;
  if(c.waterFriendList == null) c.waterFriendList = new ArrayList<>();

  c.rewardFriend = true;
  c.sendBackAnimal = true;
  c.sendType = SendType.HIT;
  if(c.dontSendFriendList == null) c.dontSendFriendList = new ArrayList<>();
  c.recallAnimalType = RecallAnimalType.ALWAYS;
  c.receiveFarmToolReward = true;
  c.useNewEggTool = true;
  c.harvestProduce = true;
  c.donation = true;
  c.answerQuestion = true;
  c.receiveFarmTaskAward = true;
  c.feedAnimal = true;
  c.useAccelerateTool = true;
  if(c.feedFriendAnimalList == null) c.feedFriendAnimalList = new ArrayList<>();
  c.notifyFriend = true;
  if(c.dontNotifyFriendList == null) c.dontNotifyFriendList = new ArrayList<>();

  c.receivePoint = true;
  return c;
 }

 public static boolean saveConfigFile()
 {
  return FileUtils.write2File(config2Json(config), FileUtils.getConfigFile());
 }

 public static Config json2Config(String json)
 {
  Config config = null;
  try
  {
   JSONObject jo = new JSONObject(removeOutcomment(json));
   JSONArray ja = null;
   config = new Config();

   if(jo.has(jn_immediateEffect))
    config.immediateEffect = jo.getBoolean(jn_immediateEffect);
   else
    config.immediateEffect = true;
   Log.i(TAG, jn_immediateEffect + ":" + config.immediateEffect);

   if(jo.has(jn_recordLog))
    config.recordLog = jo.getBoolean(jn_recordLog);
   else
    config.recordLog = true;
   Log.i(TAG, jn_recordLog + ":" + config.recordLog);

   /* forest */
   if(jo.has(jn_collectEnergy))
    config.collectEnergy = jo.getBoolean(jn_collectEnergy);
   else
    config.collectEnergy = true;
   Log.i(TAG, jn_collectEnergy + ":" + config.collectEnergy);

   if(jo.has(jn_timeInterval))
    config.timeInterval = jo.getInt(jn_timeInterval);
   else
    config.timeInterval = 120_000;
   Log.i(TAG, jn_timeInterval + ":" + config.timeInterval);

   if(jo.has(jn_advanceTime))
    config.advanceTime = jo.getInt(jn_advanceTime);
   else
    config.advanceTime = 500;
   Log.i(TAG, jn_advanceTime + ":" + config.advanceTime);

   if(jo.has(jn_collectInterval))
    config.collectInterval = jo.getInt(jn_collectInterval);
   else
    config.collectInterval = 100;
   Log.i(TAG, jn_collectInterval + ":" + config.collectInterval);

   if(jo.has(jn_collectTimeout))
    config.collectTimeout = jo.getInt(jn_collectTimeout);
   else
    config.collectTimeout = 2_000;
   Log.i(TAG, jn_collectTimeout + ":" + config.collectTimeout);

   if(jo.has(jn_ReturnWater30))
    config.returnWater30 = jo.getInt(jn_ReturnWater30);
   else
    config.returnWater30 = 0;
   Log.i(TAG, jn_ReturnWater30 + ":" + config.returnWater30);

   if(jo.has(jn_ReturnWater20))
    config.returnWater20 = jo.getInt(jn_ReturnWater20);
   else
    config.returnWater20 = 0;
   Log.i(TAG, jn_ReturnWater20 + ":" + config.returnWater20);

   if(jo.has(jn_ReturnWater10))
    config.returnWater10 = jo.getInt(jn_ReturnWater10);
   else
    config.returnWater10 = 0;
   Log.i(TAG, jn_ReturnWater10 + ":" + config.returnWater10);

   if(jo.has(jn_helpFriendCollect))
    config.helpFriendCollect = jo.getBoolean(jn_helpFriendCollect);
   else
    config.helpFriendCollect = true;
   Log.i(TAG, jn_helpFriendCollect + ":" + config.helpFriendCollect);

   config.dontCollectList = new ArrayList<>();
   Log.i(TAG, jn_dontCollectList + ":[");
   if(jo.has(jn_dontCollectList))
   {
    ja = jo.getJSONArray(jn_dontCollectList);
    for(int i = 0; i < ja.length(); i++)
    {
     config.dontCollectList.add(ja.getString(i));
     Log.i(TAG, "  " + config.dontCollectList.get(i) + ",");
    }
   }

   config.dontHelpCollectList = new ArrayList<>();
   Log.i(TAG, jn_dontHelpCollectList + ":[");
   if(jo.has(jn_dontHelpCollectList))
   {
    ja = jo.getJSONArray(jn_dontHelpCollectList);
    for(int i = 0; i < ja.length(); i++)
    {
     config.dontHelpCollectList.add(ja.getString(i));
     Log.i(TAG, "  " + config.dontHelpCollectList.get(i) + ",");
    }
   }

   if(jo.has(jn_receiveForestTaskAward))
    config.receiveForestTaskAward = jo.getBoolean(jn_receiveForestTaskAward);
   else
    config.receiveForestTaskAward = true;
   Log.i(TAG, jn_receiveForestTaskAward + ":" + config.receiveForestTaskAward);

   config.waterFriendList = new ArrayList<>();
   Log.i(TAG, jn_waterFriendList + ":[");
   if(jo.has(jn_waterFriendList))
   {
    ja = jo.getJSONArray(jn_waterFriendList);
    for(int i = 0; i < ja.length(); i++)
    {
     config.waterFriendList.add(ja.getString(i));
     Log.i(TAG, "  " + config.waterFriendList.get(i) + ",");
    }
   }

   /* farm */
   if(jo.has(jn_enableFarm))
    config.enableFarm = jo.getBoolean(jn_enableFarm);
   else
    config.enableFarm = true;
   Log.i(TAG, jn_enableFarm + ":" + config.enableFarm);

   if(jo.has(jn_rewardFriend))
    config.rewardFriend = jo.getBoolean(jn_rewardFriend);
   else
    config.rewardFriend = true;
   Log.i(TAG, jn_rewardFriend + ":" + config.rewardFriend);

   if(jo.has(jn_sendBackAnimal))
    config.sendBackAnimal = jo.getBoolean(jn_sendBackAnimal);
   else
    config.sendBackAnimal = true;
   Log.i(TAG, jn_sendBackAnimal + ":" + config.sendBackAnimal);

   if(jo.has(jn_sendType))
    config.sendType = SendType.valueOf(jo.getString(jn_sendType));
   else
    config.sendType = SendType.HIT;
   Log.i(TAG, jn_sendType + ":" + config.sendType.name());

   config.dontSendFriendList = new ArrayList<>();
   Log.i(TAG, jn_dontSendFriendList + ":[");
   if(jo.has(jn_dontSendFriendList))
   {
    ja = jo.getJSONArray(jn_dontSendFriendList);
    for(int i = 0; i < ja.length(); i++)
    {
     config.dontSendFriendList.add(ja.getString(i));
     Log.i(TAG, "  " + config.dontSendFriendList.get(i) + ",");
    }
   }

   if(jo.has(jn_recallAnimalType))
    config.recallAnimalType = RecallAnimalType.valueOf(jo.getString(jn_recallAnimalType));
   else
    config.recallAnimalType = RecallAnimalType.ALWAYS;
   Log.i(TAG, jn_recallAnimalType + ":" + config.recallAnimalType.name());

   if(jo.has(jn_receiveFarmToolReward))
    config.receiveFarmToolReward = jo.getBoolean(jn_receiveFarmToolReward);
   else
    config.receiveFarmToolReward = true;
   Log.i(TAG, jn_receiveFarmToolReward + ":" + config.receiveFarmToolReward);

   if(jo.has(jn_useNewEggTool))
    config.useNewEggTool = jo.getBoolean(jn_useNewEggTool);
   else
    config.useNewEggTool = true;
   Log.i(TAG, jn_useNewEggTool + ":" + config.useNewEggTool);

   if(jo.has(jn_harvestProduce))
    config.harvestProduce = jo.getBoolean(jn_harvestProduce);
   else
    config.harvestProduce = true;
   Log.i(TAG, jn_harvestProduce + ":" + config.harvestProduce);

   if(jo.has(jn_donation))
    config.donation = jo.getBoolean(jn_donation);
   else
    config.donation = true;
   Log.i(TAG, jn_donation + ":" + config.donation);

   if(jo.has(jn_answerQuestion))
    config.answerQuestion = jo.getBoolean(jn_answerQuestion);
   else
    config.answerQuestion = true;
   Log.i(TAG, jn_answerQuestion + ":" + config.answerQuestion);

   if(jo.has(jn_receiveFarmTaskAward))
    config.receiveFarmTaskAward = jo.getBoolean(jn_receiveFarmTaskAward);
   else
    config.receiveFarmTaskAward = true;
   Log.i(TAG, jn_receiveFarmTaskAward + ":" + config.receiveFarmTaskAward);

   if(jo.has(jn_feedAnimal))
    config.feedAnimal = jo.getBoolean(jn_feedAnimal);
   else
    config.feedAnimal = true;
   Log.i(TAG, jn_feedAnimal + ":" + config.feedAnimal);

   if(jo.has(jn_useAccelerateTool))
    config.useAccelerateTool = jo.getBoolean(jn_useAccelerateTool);
   else
    config.useAccelerateTool = true;
   Log.i(TAG, jn_useAccelerateTool + ":" + config.useAccelerateTool);

   config.feedFriendAnimalList = new ArrayList<>();
   Log.i(TAG, jn_feedFriendAnimalList + ":[");
   if(jo.has(jn_feedFriendAnimalList))
   {
    ja = jo.getJSONArray(jn_feedFriendAnimalList);
    for(int i = 0; i < ja.length(); i++)
    {
     config.feedFriendAnimalList.add(ja.getString(i));
     Log.i(TAG, "  " + config.feedFriendAnimalList.get(i) + ",");
    }
   }

   if(jo.has(jn_notifyFriend))
    config.notifyFriend = jo.getBoolean(jn_notifyFriend);
   else
    config.notifyFriend = true;
   Log.i(TAG, jn_notifyFriend + ":" + config.notifyFriend);

   config.dontNotifyFriendList = new ArrayList<>();
   Log.i(TAG, jn_dontNotifyFriendList + ":[");
   if(jo.has(jn_dontNotifyFriendList))
   {
    ja = jo.getJSONArray(jn_dontNotifyFriendList);
    for(int i = 0; i < ja.length(); i++)
    {
     config.dontNotifyFriendList.add(ja.getString(i));
     Log.i(TAG, "  " + config.dontNotifyFriendList.get(i) + ",");
    }
   }

   /* member */
   if(jo.has(jn_receivePoint))
    config.receivePoint = jo.getBoolean(jn_receivePoint);
   else
    config.receivePoint = true;
   Log.i(TAG, jn_receivePoint + ":" + config.receivePoint);

  }catch(Throwable t)
  {
   Log.printStackTrace(TAG, t);
   if(json != null)
   {
    Log.showToastIgnoreConfig("配置文件格式有误，已重置配置文件并备份原文件", "");
    FileUtils.write2File(json, FileUtils.getBackupFile(FileUtils.getConfigFile()));
   }
   config = defInit();
  }
  String formated = config2Json(config);
  if(!formated.equals(json))
  {
   Log.i(TAG, "重新格式化 config.json");
   FileUtils.write2File(formated, FileUtils.getConfigFile());
  }
  return config;
 }

 public static String config2Json(Config config)
 {
  JSONObject jo = new JSONObject();
  try
  {
   if(config == null) config = Config.defInit();

   jo.put(jn_immediateEffect, config.immediateEffect);

   jo.put(jn_recordLog, config.recordLog);

   /* forest */
   jo.put(jn_collectEnergy, config.collectEnergy);

   jo.put(jn_timeInterval, config.timeInterval);

   jo.put(jn_advanceTime, config.advanceTime);

   jo.put(jn_collectInterval, config.collectInterval);

   jo.put(jn_collectTimeout, config.collectTimeout);

   jo.put(jn_ReturnWater30, config.returnWater30);

   jo.put(jn_ReturnWater20, config.returnWater20);

   jo.put(jn_ReturnWater10, config.returnWater10);

   jo.put(jn_helpFriendCollect, config.helpFriendCollect);

   JSONArray ja = new JSONArray();
   for(String s: config.dontCollectList)
   {
    ja.put(s);
   }
   jo.put(jn_dontCollectList, ja);

   ja = new JSONArray();
   for(String s: config.dontHelpCollectList)
   {
    ja.put(s);
   }
   jo.put(jn_dontHelpCollectList, ja);

   jo.put(jn_receiveForestTaskAward, config.receiveForestTaskAward);

   ja = new JSONArray();
   for(String s: config.waterFriendList)
   {
    ja.put(s);
   }
   jo.put(jn_waterFriendList, ja);

   /* farm */
   jo.put(jn_enableFarm, config.enableFarm);

   jo.put(jn_rewardFriend, config.rewardFriend);

   jo.put(jn_sendBackAnimal, config.sendBackAnimal);

   jo.put(jn_sendType, config.sendType.name());

   ja = new JSONArray();
   for(String s: config.dontSendFriendList)
   {
    ja.put(s);
   }
   jo.put(jn_dontSendFriendList, ja);

   jo.put(jn_recallAnimalType, config.recallAnimalType);

   jo.put(jn_receiveFarmToolReward, config.receiveFarmToolReward);

   jo.put(jn_useNewEggTool, config.useNewEggTool);

   jo.put(jn_harvestProduce, config.harvestProduce);

   jo.put(jn_donation, config.donation);

   jo.put(jn_answerQuestion, config.answerQuestion);

   jo.put(jn_receiveFarmTaskAward, config.receiveFarmTaskAward);

   jo.put(jn_feedAnimal, config.feedAnimal);

   jo.put(jn_useAccelerateTool, config.useAccelerateTool);

   ja = new JSONArray();
   for(String s: config.feedFriendAnimalList)
   {
    ja.put(s);
   }
   jo.put(jn_feedFriendAnimalList, ja);

   jo.put(jn_notifyFriend, config.notifyFriend);

   ja = new JSONArray();
   for(String s: config.dontNotifyFriendList)
   {
    ja.put(s);
   }
   jo.put(jn_dontNotifyFriendList, ja);

   /* member */
   jo.put(jn_receivePoint, config.receivePoint);

  }catch(Throwable t)
  {
   Log.printStackTrace(TAG, t);
  }
  return formatJson(jo);
 }

 public static String formatJson(JSONObject jo)
 {
  String formated = null;
  try
  {
   formated = jo.toString(4);
  }catch(Throwable t)
  {
   return jo.toString();
  }
  StringBuilder sb = new StringBuilder(formated);
  char currentChar, lastNonSpaceChar = 0;
  for(int i = 0; i < sb.length(); i++)
  {
   currentChar = sb.charAt(i);
   switch(currentChar)
   {
    case '"':
     switch(lastNonSpaceChar)
     {
      case ':':
      case '[':
       sb.deleteCharAt(i);
       i = sb.indexOf("\"", i);
       sb.deleteCharAt(i);
       if(lastNonSpaceChar != '[') lastNonSpaceChar = sb.charAt(--i);
     }
     break;

    case ' ':
     break;

    default:
     if(lastNonSpaceChar == '[' && currentChar != ']')
      break;
     lastNonSpaceChar = currentChar;
   }
  }
  formated = sb.toString();
  return formated;
 }

 private static String removeOutcomment(String s)
 {
  StringBuilder sb = new StringBuilder(s);
  sbi:for(int i = 0; i < sb.length(); i++)
  {
   if(sb.charAt(i) == '\n')
   {
    int nextNL = sb.indexOf("\n", i + 1),
     outcoI = sb.indexOf("//", i + 1);
    if(i < outcoI && outcoI < nextNL)
    {
     sb.delete(i--, nextNL);
    }
   }
  }
  return sb.toString();
 }

}
