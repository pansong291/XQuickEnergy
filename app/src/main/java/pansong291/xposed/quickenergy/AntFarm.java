package pansong291.xposed.quickenergy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;

public class AntFarm
{
 private static final String TAG = AntFarm.class.getCanonicalName();
 
 public enum SendType
 {
  HIT, NORMAL;
  public SendType another()
  {
   return this == HIT ? NORMAL : HIT;
  }
 }
 public enum AnimalBuff
 { ACCELERATING, INJURED, NONE };
 public enum AnimalFeedStatus
 { HUNGRY, EATING };
 public enum AnimalInteractStatus
 { HOME, GOTOSTEAL, STEALING };
 public enum TaskStatus
 { TODO, FINISHED, RECEIVED };
 public enum ToolType
 { STEALTOOL, ACCELERATETOOL, SHARETOOL, FENCETOOL, NEWEGGTOOL }
 private static class StealingAnimal
 {
  public boolean isNull;
  public String animalId, currentFarmId, masterFarmId,
  animalBuff, animalFeedStatus, animalInteractStatus;
 }
 
 private static class RewardFriend
 {
  public String consistencyKey, friendId;
 }
 
 private static String cityAdCode;
 private static String districtAdCode;
 private static String version;
 private static int pageStartSum = 0;
 private static List<String> friendIdList = new ArrayList<>();
 
 private static String ownerFarmId;
 private static final StealingAnimal[] stealingAnimals = new StealingAnimal[3];
 private static StealingAnimal ownerAnimal;
 private static int foodStock;
 private static int foodStockLimit;
 private static double rewardProductNum;
 private static RewardFriend[] rewardList;
 private static double benevolenceScore;
 private static double harvestBenevolenceScore;
 private static int unreceiveTaskAward = 0;
 
 public static void start(ClassLoader loader, String args0, String args1, String response)
 {
  if(isEnterFarmAndHasNull(args0))
   try
   {
    JSONArray ja = new JSONArray(args1);
    JSONObject jo = ja.getJSONObject(0);
    cityAdCode = jo.getString("cityAdCode");
    districtAdCode = jo.getString("districtAdCode");
    version = jo.getString("version");
   }catch(JSONException e)
   {
    Log.i(TAG, "start err: "+e.getMessage()+", args1="+args1);
    Log.printStackTrace(TAG, e);
   }
   
  if(isEnterOwnerFarm(response))
  {
   new Thread(new Runnable()
    {
     private String resp;
     private ClassLoader loader;
     
     public Runnable setData(String s, ClassLoader cl)
     {
      resp = s;
      loader = cl;
      return this;
     }
    
     @Override
     public void run()
     {
      try
      {
       JSONObject jo = new JSONObject(resp);
       rewardProductNum = jo.getJSONObject("dynamicGlobalConfig").getDouble("rewardProductNum");
       JSONObject joFarmVO = jo.getJSONObject("farmVO");
       foodStock = joFarmVO.getInt("foodStock");
       foodStockLimit = joFarmVO.getInt("foodStockLimit");
       harvestBenevolenceScore = joFarmVO.getDouble("harvestBenevolenceScore");
       parseSyncAnimalStatusResponse(joFarmVO.toString());
       
       if(rewardList != null)
       {
        for(int i = 0; i < rewardList.length; i++)
        {
         
        }
        rewardList = null;
       }
       
       // 赶鸡
       sendBackAnimal(loader);
       
       Log.i("run....","ownerAnimal"+ownerAnimal);
       if(!AnimalInteractStatus.HOME.name().equals(ownerAnimal.animalInteractStatus))
       {
        Log.showDialogAndRecordLog("小鸡不在庄园","");
        // 小鸡到好友家去做客了
        // 小鸡太饿，离家出走了
        if(AnimalFeedStatus.HUNGRY.name().equals(ownerAnimal.animalFeedStatus))
        {
         recallAnimal(loader,ownerAnimal.animalId,ownerAnimal.currentFarmId,ownerFarmId);
         syncAnimalStatus(loader,ownerFarmId);
        }
       }
       
       if(benevolenceScore >= 1)
       {
        Log.showDialogAndRecordLog("有可收取的爱心鸡蛋","");
        harvestProduce(loader, ownerFarmId);
       }
       
       if(harvestBenevolenceScore >= 5)
       {
        Log.showDialogAndRecordLog("爱心鸡蛋已达到可捐赠个数","");
        //donation(loader,"");
       }
       
       answerQuestion(loader);
       
       receiveFarmTaskAward(loader);
       
       if(AnimalInteractStatus.HOME.name().equals(ownerAnimal.animalInteractStatus))
       {
        if(AnimalFeedStatus.HUNGRY.name().equals(ownerAnimal.animalFeedStatus))
        {
         Log.showDialogAndRecordLog("小鸡在挨饿","");
         // 喂鸡
         feedAnimal(loader, ownerFarmId);
         //syncAnimalStatus(loader,ownerFarmId);
        }

        if(AnimalBuff.ACCELERATING.name().equals(ownerAnimal.animalBuff))
        {
         Log.showDialogAndRecordLog("小鸡已经双手并用着加速吃饲料了","");
        }else
        {
         // 加速卡
         useAccelerateTool(loader);
        }

        if(unreceiveTaskAward > 0)
        {
         Log.showDialogAndRecordLog("还有待领取的饲料","");
         receiveFarmTaskAward(loader);
        }
        
       }

       // 通知好友赶鸡并帮好友喂鸡
       notifyFriendAndFeed(loader);
       
      }catch(Exception e)
      {
       Log.i(TAG, "run err: "+e.getMessage());
       Log.printStackTrace(TAG, e);
      }
     }
    }.setData(response, loader)).start();
  }
  
 }
 
 private static boolean isEnterFarmAndHasNull(String args0)
 {
  return args0.equals("com.alipay.antfarm.enterFarm") &&
  cityAdCode == null || districtAdCode == null || version == null;
 }
 
 private static boolean isEnterOwnerFarm(String resp)
 {
  return resp.contains("\"relation\":\"OWNER\"");
 }
 
 public static boolean isEnterFriendFarm(String resp)
 {
  return resp.contains("\"relation\":\"FRIEND\"");
 }
 
 private static void syncAnimalStatus(ClassLoader loader, String farmId)
 {
  try
  {
   Object o = rpcCall_syncAnimalStatus(loader, farmId);
   String s = RpcCall.getResponse(o);
   parseSyncAnimalStatusResponse(s);
  }catch(Exception e)
  {
   Log.i(TAG, "syncAnimalStatus err: "+e.getMessage());
   Log.printStackTrace(TAG, e);
  }
 }
 
 private static void recallAnimal(ClassLoader loader, String animalId, String currentFarmId, String masterFarmId)
 {
  try
  {
   Log.showDialogAndRecordLog("开始召回小鸡…","");
   Object o = rpcCall_recallAnimal(loader, animalId, currentFarmId, masterFarmId);
   String s = RpcCall.getResponse(o);
   JSONObject jo = new JSONObject(s);
   double foodHaveStolen = jo.getDouble("foodHaveStolen");
   Log.showDialogAndRecordLog("已召回小鸡，偷吃了〔"+Config.getNameById(farmId2UserId(currentFarmId))+"〕的［"+foodHaveStolen+"克］饲料","");
  }catch(Exception e)
  {
   Log.i(TAG, "recallAnimal err: "+e.getMessage());
   Log.printStackTrace(TAG, e);
  }
 }
 
 private static void sendBackAnimal(ClassLoader loader)
 {
  try
  {
   Log.showDialogAndRecordLog("开始赶鸡…","");
   boolean hasStealingAnimal = false;
   for(int i = 0; i < stealingAnimals.length; i++)
   {
    if(stealingAnimals[i].isNull) continue;
    if(AnimalInteractStatus.STEALING.name().equals(stealingAnimals[i].animalInteractStatus))
    {
     hasStealingAnimal = true;
     // 赶鸡
     String user = farmId2UserId(stealingAnimals[i].masterFarmId);
     SendType sendType = Config.sendType(user);
     user = Config.getNameById(user);
     Object o = rpcCall_sendBackAnimal(loader, sendType.name(), stealingAnimals[i].animalId,
      stealingAnimals[i].currentFarmId, stealingAnimals[i].masterFarmId);
     String s = RpcCall.getResponse(o);
     JSONObject jo = new JSONObject(s);
     if(sendType == SendType.HIT)
     {
      s = "你对〔"+user+"〕的小鸡发起攻击\n";
      if(jo.has("hitLossFood"))
      {
       s += "胖揍了〔"+user+"〕的小鸡，掉落了［"+jo.getInt("hitLossFood")+"克］饲料";
       add2FoodStock(jo.getInt("saveFoodNum"));
       s += "\n剩余〔"+foodStock+"克〕饲料";
      }else
       s += "〔"+user+"〕的小鸡躲开了你的攻击";
     }else
     {
      s = "已赶走〔"+user+"〕的小鸡";
     }
     Log.showDialogAndRecordLog(s,"");
    }
   }
   if(!hasStealingAnimal)
    Log.showDialogAndRecordLog("没有来偷吃的小鸡","");
  }catch(Exception e)
  {
   Log.i(TAG, "sendBackAnimal err: "+e.getMessage());
   Log.printStackTrace(TAG, e);
  }
 }
 
 private static void harvestProduce(ClassLoader loader, String farmId)
 {
  try
  {
   Log.showDialogAndRecordLog("开始收取爱心鸡蛋…","");
   Object o = rpcCall_harvestProduce(loader, farmId);
   String s = RpcCall.getResponse(o);
   JSONObject jo = new JSONObject(s);
   if(s.contains("\"memo\":\"SUCCESS\""))
   {
    double harvest = jo.getDouble("harvestBenevolenceScore") - harvestBenevolenceScore;
    Log.showDialogAndRecordLog("收取了〔"+harvest+"颗〕爱心鸡蛋","");
    harvestBenevolenceScore += harvest;
   }else
   {
    Log.showDialogAndRecordLog("失败",s);
   }
  }catch(Exception e)
  {
   Log.i(TAG, "harvestProduce err: "+e.getMessage());
   Log.printStackTrace(TAG, e);
  }
 }
 
 private static void donation(ClassLoader loader, String activityId)
 {
  try
  {
   Log.showDialogAndRecordLog("开始捐赠爱心鸡蛋","");
   Object o = rpcCall_donation(loader, activityId);
   String s = RpcCall.getResponse(o);
   JSONObject jo = new JSONObject(s).getJSONObject("donation");
   if(s.contains("\"memo\":\"SUCCESS\""))
   {
    harvestBenevolenceScore = jo.getDouble("harvestBenevolenceScore");
    Log.showDialogAndRecordLog("捐赠成功，累计捐赠"+jo.getInt("donationTimesStat")+"次","");
   }else
   {
    Log.showDialogAndRecordLog("失败",s);
   }
  }catch(Exception e)
  {
   Log.i(TAG, "harvestProduce err: "+e.getMessage());
   Log.printStackTrace(TAG, e);
  }
 }
 
 private static void answerQuestion(ClassLoader loader)
 {
  try
  {
   Log.showDialogAndRecordLog("开始答题…","");
   Object o = rpcCall_listFarmTask(loader);
   String s = RpcCall.getResponse(o);
   JSONObject jo = new JSONObject(s);
   JSONArray jaFarmTaskList = jo.getJSONArray("farmTaskList");
   for(int i = 0; i < jaFarmTaskList.length(); i++)
   {
    jo = jaFarmTaskList.getJSONObject(i);
    if(jo.getString("title").equals("庄园小课堂"))
    {
     switch(TaskStatus.valueOf((jo.getString("taskStatus"))))
     {
      case TODO:
       o = rpcCall_getAnswerInfo(loader);
       s = RpcCall.getResponse(o);
       jo = new JSONObject(s);
       jo = jo.getJSONArray("answerInfoVOs").getJSONObject(0);
       JSONArray jaOptionContents = jo.getJSONArray("optionContents");
       String rightReply = jo.getString("rightReply");
       Log.showDialogAndRecordLog(jo.getString("questionContent"),"");
       Log.showDialogAndRecordLog(jaOptionContents.toString(),"");
       String questionId = jo.getString("questionId");
       int answer = -1;
       for(int j = 0; j < jaOptionContents.length(); j++)
       {
        if(rightReply.contains(jaOptionContents.getString(j)))
        {
         answer = j + 1;
         break;
        }
       }
       if(answer > 0)
       {
        o = rpcCall_answerQuestion(loader, questionId, answer);
        s = RpcCall.getResponse(o);
        jo = new JSONObject(s);
        s = jo.getBoolean("rightAnswer")? "正确":"错误";
        Log.showDialogAndRecordLog("答题"+s+"，可领取［"+jo.getInt("awardCount")+"克］饲料", "");
       }else
       {
        Log.showDialogAndRecordLog("未找到正确答案，放弃作答","");
       }
       break;
       
      case RECEIVED:
       Log.showDialogAndRecordLog("今日答题已完成","");
       break;
       
      case FINISHED:
       Log.showDialogAndRecordLog("已经答过题了，饲料待领取","");
       break;
     }
     break;
    }
   }
  }catch(Exception e)
  {
   Log.i(TAG, "answerQuestion err: "+e.getMessage());
   Log.printStackTrace(TAG, e);
  }
 }
 
 private static void receiveFarmTaskAward(ClassLoader loader)
 {
  try
  {
   Log.showDialogAndRecordLog("开始领取饲料…","");
   Object o = rpcCall_listFarmTask(loader);
   String s = RpcCall.getResponse(o);
   JSONObject jo = new JSONObject(s);
   JSONArray jaFarmTaskList = jo.getJSONArray("farmTaskList");
   boolean hasAwardFood = false;
   for(int i = 0; i < jaFarmTaskList.length(); i++)
   {
    jo = jaFarmTaskList.getJSONObject(i);
    if(TaskStatus.FINISHED.name().equals(jo.getString("taskStatus")))
    {
     hasAwardFood = true;
     int awardCount = jo.getInt("awardCount");
     if(awardCount + foodStock > foodStockLimit)
     {
      unreceiveTaskAward++;
      Log.showDialogAndRecordLog("领取"+awardCount+"克饲料后将超过"+foodStockLimit+"克上限，已终止领取","");
      break;
     }
     String title = jo.getString("title");
     o = rpcCall_receiveFarmTaskAward(loader, jo.getString("taskId"));
     s = RpcCall.getResponse(o);
     jo = new JSONObject(s);
     foodStock = jo.getInt("foodStock");
     Log.showDialogAndRecordLog("已领取［"+jo.getInt("haveAddFoodStock")+"克］饲料，来源："+title,"");
     if(unreceiveTaskAward > 0)unreceiveTaskAward--;
    }
   }
   if(!hasAwardFood)
    Log.showDialogAndRecordLog("暂时没有可领取的饲料","");
   Log.showDialogAndRecordLog("剩余〔"+foodStock+"克〕饲料","");
  }catch(Exception e)
  {
   Log.i(TAG, "receiveFarmTaskAward err: "+e.getMessage());
   Log.printStackTrace(TAG, e);
  }
 }
 
 private static void feedAnimal(ClassLoader loader, String farmId)
 {
  try
  {
   Log.showDialogAndRecordLog("开始喂鸡…","");
   Object o = rpcCall_feedAnimal(loader, farmId);
   String s = RpcCall.getResponse(o);
   JSONObject jo = new JSONObject(s);
   int feedFood = foodStock - jo.getInt("foodStock");
   add2FoodStock(-feedFood);
   Log.showDialogAndRecordLog("喂了小鸡［"+feedFood+"克］饲料，剩余〔"+foodStock+"克〕","");
  }catch(Exception e)
  {
   Log.i(TAG, "feedAnimal err: "+e.getMessage());
   Log.printStackTrace(TAG, e);
  }
 }
 
 private static void useAccelerateTool(ClassLoader loader)
 {
  try
  {
   Log.showDialogAndRecordLog("开始使用加速卡…","");
   Object o = rpcCall_listFarmTool(loader);
   String s = RpcCall.getResponse(o);
   JSONObject jo = new JSONObject(s);
   JSONArray jaToolList = jo.getJSONArray("toolList");
   for(int i = 0; i < jaToolList.length(); i++)
   {
    jo = jaToolList.getJSONObject(i);
    if(ToolType.ACCELERATETOOL.name().equals(jo.getString("toolType")))
    {
     int toolCount = jo.getInt("toolCount");
     if(toolCount > 0)
     {
      o = rpcCall_useFarmTool(loader, ownerFarmId, jo.getString("toolId"), ToolType.ACCELERATETOOL.name());
      s = RpcCall.getResponse(o);
      if(s.contains("\"memo\":\"SUCCESS\""))
       Log.showDialogAndRecordLog("加速卡使用成功，剩余"+(toolCount-1)+"张","");
      else
       Log.showDialogAndRecordLog("失败","");
     }else
     {
      Log.showDialogAndRecordLog("没有加速卡可用","");
     }
     break;
    }
   }
  }catch(Exception e)
  {
   Log.i(TAG, "useAccelerateTool err: "+e.getMessage());
   Log.printStackTrace(TAG, e);
  }
 }

 private static void notifyFriendAndFeed(ClassLoader loader)
 {
  try
  {
   Log.showDialogAndRecordLog("开始通知好友来赶鸡…","");
   boolean hasNext;
   Object o;
   String s;
   JSONObject jo;
   do
   {
    o = rpcCall_rankingList(loader);
    s = RpcCall.getResponse(o);
    jo = new JSONObject(s);
    hasNext = jo.getBoolean("hasNext");
    JSONArray jaRankingList = jo.getJSONArray("rankingList");
    for(int i = 0; i < jaRankingList.length(); i++)
    {
     jo = jaRankingList.getJSONObject(i);
     String userId = jo.getString("userId");
     String user = Config.getNameById(userId);
     boolean feedFriendAnimal = jo.has("actionType") && 
      jo.getString("actionType").equals("starve_action") && 
      Config.feedFriendAnimal(userId);
     if(jo.getBoolean("stealingAnimal") || feedFriendAnimal)
     {
      o = rpcCall_enterFarm(loader, "", userId);
      s = RpcCall.getResponse(o);
      jo = new JSONObject(s).getJSONObject("farmVO").getJSONObject("subFarmVO");
      String friendFarmId = jo.getString("farmId");
      JSONArray jaAnimals = jo.getJSONArray("animals");
      boolean notified = false;
      for(int j = 0; j < jaAnimals.length(); j++)
      {
       jo = jaAnimals.getJSONObject(j);
       String animalId = jo.getString("animalId");
       if(jo.getString("masterFarmId").equals(friendFarmId))
       {
        jo = jo.getJSONObject("animalStatusVO");
        feedFriendAnimal &= AnimalInteractStatus.HOME.name().equals(jo.getString("animalInteractStatus"));
        if(feedFriendAnimal)
         feedFriendAnimal(loader, jaAnimals, friendFarmId, user);
       }else
       {
        if(notified) continue;
        jo = jo.getJSONObject("animalStatusVO");
        notifyFriend(loader, jo, friendFarmId, animalId);
       }
      }
      
     }
    }
   }while(hasNext);
   pageStartSum = 0;
   Log.showDialogAndRecordLog("通知好友结束","");
  }catch(Exception e)
  {
   Log.i(TAG, "notifyFriend err: "+e.getMessage());
   Log.printStackTrace(TAG, e);
  }
  
 }

 private static boolean notifyFriend(ClassLoader loader, JSONObject joAnimalStatusVO, String friendFarmId, String animalId)
 {
  try
  {
   if(AnimalInteractStatus.STEALING.name().equals(joAnimalStatusVO.getString("animalInteractStatus"))
    && AnimalFeedStatus.EATING.name().equals(joAnimalStatusVO.getString("animalFeedStatus")))
   {
    String user = Config.getNameById(farmId2UserId(friendFarmId));
    Log.showDialogAndRecordLog("有小鸡正在偷吃〔"+user+"〕的饲料","");
    Object o = rpcCall_notifyFriend(loader, animalId, friendFarmId);
    String s = RpcCall.getResponse(o);
    joAnimalStatusVO = new JSONObject(s);
    double rewardCount = joAnimalStatusVO.getDouble("rewardCount");
    add2FoodStock((int)rewardCount);

    Log.showDialogAndRecordLog("已通知〔"+user+"〕，小鸡拿出〔"+rewardCount+"克〕饲料奖励你","");
    return true;
   }
  }catch(Exception e)
  {
   Log.i(TAG, "feedFriendAnimal err: "+e.getMessage());
   Log.printStackTrace(TAG, e);
  }
  return false;
 }
 
 private static void feedFriendAnimal(ClassLoader loader, JSONArray jaAnimals, String friendFarmId, String user)
 {
  try
  {
   Log.showDialogAndRecordLog("〔"+user+"〕的小鸡在挨饿","");
   if(foodStock < 180)
   {
    Log.showDialogAndRecordLog("饲料不足","");
    if(unreceiveTaskAward > 0)
    {
     Log.showDialogAndRecordLog("还有待领取的饲料","");
     receiveFarmTaskAward(loader);
    }
   }
   if(foodStock >= 180)
   {
    Object o = rpcCall_feedFriendAnimal(loader, friendFarmId);
    String s = RpcCall.getResponse(o);
    int feedFood = foodStock - new JSONObject(s).getInt("foodStock");
    if(feedFood > 0)
    {
     add2FoodStock(-feedFood);
     Log.showDialogAndRecordLog("喂了〔"+user+"〕的小鸡〔"+feedFood+"克〕饲料，剩余〔"+foodStock+"克〕","");
     for(int i = 0; i < jaAnimals.length(); i++)
     {
      JSONObject jo = jaAnimals.getJSONObject(i);
      JSONObject joo = jo.getJSONObject("animalStatusVO");
      joo.put("animalFeedStatus", AnimalFeedStatus.EATING.name());
      jo.put("animalStatusVO", joo);
      jaAnimals.put(i, jo);
     }
    }
   }
  }catch(Exception e)
  {
   Log.i(TAG, "feedFriendAnimal err: "+e.getMessage());
   Log.printStackTrace(TAG, e);
  }
 }
 
 private static Object rpcCall_syncAnimalStatus(ClassLoader loader, String farmId)
 {
  /*
   [{"farmId":"10181101172642012088022401920420","operType":"FEEDSYNC","queryFoodStockInfo":false,"recall":false,"requestType":"NORMAL","sceneCode":"ANTFARM","source":"H5","userId":"2088022401920420","version":"1.0.1907111732.11"}]
   */
  try
  {
   String args1 = "[{\"farmId\":\""+farmId+
    "\",\"operType\":\"FEEDSYNC\",\"queryFoodStockInfo\":false,\"recall\":false,\"requestType\":\"NORMAL\",\"sceneCode\":\"ANTFARM\",\"source\":\"H5\",\"userId\":\""
    +farmId2UserId(farmId)+"\",\"version\":\""+version+"\"}]";
   return RpcCall.invoke(loader, "com.alipay.antfarm.syncAnimalStatus", args1);
  }catch(Exception e)
  {
   Log.i(TAG, "rpcCall_syncAnimalStatus err: "+e.getMessage());
   Log.printStackTrace(TAG, e);
  }
  return null;
 }
 
 private static void parseSyncAnimalStatusResponse(String resp)
 {
  try
  {
   JSONObject jo = new JSONObject(resp);
   jo = jo.getJSONObject("subFarmVO");
   ownerFarmId = jo.getString("farmId");
   benevolenceScore = jo.getJSONObject("farmProduce").getDouble("benevolenceScore");
   if(jo.has("rewardList"))
   {
    JSONArray jaRewardList = jo.getJSONArray("rewardList");
    if(jaRewardList.length() > 0)
    {
     rewardList = new RewardFriend[jaRewardList.length()];
     for(int i = 0; i < rewardList.length; i++)
     {
      JSONObject joRewardList = jaRewardList.getJSONObject(i);
      if(rewardList[i] == null)rewardList[i] = new RewardFriend();
      rewardList[i].consistencyKey = joRewardList.getString("consistencyKey");
      rewardList[i].friendId = joRewardList.getString("friendId");
     }
    }
   }
   JSONArray jaAnimals = jo.getJSONArray("animals");
   for(int i = 0; i < stealingAnimals.length; i++)
   {
    if(stealingAnimals[i] == null)stealingAnimals[i] = new StealingAnimal();
    if(i >= jaAnimals.length())
    {
     stealingAnimals[i].isNull = true;
     continue;
    }
    jo = jaAnimals.getJSONObject(i);
    stealingAnimals[i].isNull = false;
    stealingAnimals[i].animalId = jo.getString("animalId");
    stealingAnimals[i].currentFarmId = jo.getString("currentFarmId");
    stealingAnimals[i].masterFarmId = jo.getString("masterFarmId");
    stealingAnimals[i].animalBuff = jo.getString("animalBuff");
    jo = jo.getJSONObject("animalStatusVO");
    stealingAnimals[i].animalFeedStatus = jo.getString("animalFeedStatus");
    stealingAnimals[i].animalInteractStatus = jo.getString("animalInteractStatus");
    if(stealingAnimals[i].masterFarmId.equals(ownerFarmId))
     ownerAnimal = stealingAnimals[i];
    Log.i("parse....","ownerAnimal="+ownerAnimal);
    Log.i("owner","ownerFarmId="+ownerFarmId);
    Log.i(i+" Animal","isNull="+stealingAnimals[i].isNull);
    Log.i(i+" Animal","animalId="+stealingAnimals[i].animalId);
    Log.i(i+" Animal","currentFarmId="+stealingAnimals[i].currentFarmId);
    Log.i(i+" Animal","masterFarmId="+stealingAnimals[i].masterFarmId);
    Log.i(i+" Animal","animalBuff="+stealingAnimals[i].animalBuff);
    Log.i(i+" Animal","animalFeedStatus="+stealingAnimals[i].animalFeedStatus);
    Log.i(i+" Animal","animalInteractStatus="+stealingAnimals[i].animalInteractStatus);
   }
  }catch(Exception e)
  {
   Log.i(TAG, "parseSyncAnimalStatusResponse err: "+e.getMessage());
   Log.printStackTrace(TAG, e);
  }
 }
 
 private static Object rpcCall_recallAnimal(ClassLoader loader, String animalId, String currentFarmId, String masterFarmId)
 {
  /*
   com.alipay.antfarm.recallAnimal
   [{\"animalId\":\"20181101172642012088022401920420\",\"currentFarmId\":\"10170908084649012088422804385848\",\"masterFarmId\":\"10181101172642012088022401920420\",
   \"requestType\":\"NORMAL\",\"sceneCode\":\"ANTFARM\",\"source\":\"H5\",\"version\":\"1.0.1907111732.11\"}]
  */
  try
  {
   String args1 = "[{\"animalId\":\""+animalId+"\",\"currentFarmId\":\""
    +currentFarmId+"\",\"masterFarmId\":\""+masterFarmId+
    "\",\"requestType\":\"NORMAL\",\"sceneCode\":\"ANTFARM\",\"source\":\"H5\",\"version\":\""
    +version+"\"}]";
   return RpcCall.invoke(loader, "com.alipay.antfarm.recallAnimal", args1);
  }catch(Exception e)
  {
   Log.i(TAG, "rpcCall_recallAnimal err: "+e.getMessage());
   Log.printStackTrace(TAG, e);
  }
  return null;
 }
 
 private static Object rpcCall_sendBackAnimal(ClassLoader loader, String sendType, String animalId, String currentFarmId, String masterFarmId)
 {
  /*
   [{
   "animalId":"20171230000402012088922385376474",
   "currentFarmId":"10181101172642012088022401920420",
   "masterFarmId":"10171230000402012088922385376474",
   "requestType":"NORMAL",
   "sceneCode":"ANTFARM",
   "sendType":"HIT",
   "source":"H5",
   "version":"1.0.1907111732.11"
   }]
  */
  try
  {
   String args1 = "[{\"animalId\":\""+animalId+"\",\"currentFarmId\":\""
    +currentFarmId+"\",\"masterFarmId\":\""+masterFarmId+
    "\",\"requestType\":\"NORMAL\",\"sceneCode\":\"ANTFARM\",\"sendType\":\""
    +sendType+"\",\"source\":\"H5\",\"version\":\""
    +version+"\"}]";
   return RpcCall.invoke(loader, "com.alipay.antfarm.sendBackAnimal", args1);
  }catch(Exception e)
  {
   Log.i(TAG, "rpcCall_sendBackAnimal err: "+e.getMessage());
   Log.printStackTrace(TAG, e);
  }
  return null;
 }
 
 private static Object rpcCall_harvestProduce(ClassLoader loader, String farmId)
 {
  /*
   [{"canMock":true,"farmId":"10181101172642012088022401920420","giftType":"","requestType":"NORMAL","sceneCode":"ANTFARM","source":"H5","version":"1.0.1907111732.11"}]
  */
  try
  {
   String args1 = "[{\"canMock\":true,\"farmId\":\""+farmId+
    "\",\"giftType\":\"\",\"requestType\":\"NORMAL\",\"sceneCode\":\"ANTFARM\",\"source\":\"H5\",\"version\":\""
    +version+"\"}]";
   return RpcCall.invoke(loader, "com.alipay.antfarm.harvestProduce", args1);
  }catch(Exception e)
  {
   Log.i(TAG, "rpcCall_harvestProduce err: "+e.getMessage());
   Log.printStackTrace(TAG, e);
  }
  return null;
 }
 
 private static Object rpcCall_donation(ClassLoader loader, String activityId)
 {
  /*
   [{"activityId":"1906101203590000002563639261","donationAmount":5,
   "requestType":"NORMAL","sceneCode":"ANTFARM","source":"H5","version":"1.0.1907111732.11"}]
  */
  try
  {
   String args1 = "[{\"activityId\":\""+activityId+
    "\",\"donationAmount\":5,\"requestType\":\"NORMAL\",\"sceneCode\":\"ANTFARM\",\"source\":\"H5\",\"version\":\""
    +version+"\"}]";
   return RpcCall.invoke(loader, "com.alipay.antfarm.donation", args1);
  }catch(Exception e)
  {
   Log.i(TAG, "rpcCall_donation err: "+e.getMessage());
   Log.printStackTrace(TAG, e);
  }
  return null;
 }
 
 private static Object rpcCall_listFarmTask(ClassLoader loader)
 {
  try
  {
   String args1 = "[{\"requestType\":\"NORMAL\",\"sceneCode\":\"ANTFARM\",\"source\":\"H5\",\"version\":\""
    +version+"\"}]";
   return RpcCall.invoke(loader, "com.alipay.antfarm.listFarmTask", args1);
  }catch(Exception e)
  {
   Log.i(TAG, "rpcCall_listFarmTask err: "+e.getMessage());
   Log.printStackTrace(TAG, e);
  }
  return null;
 }
 
 private static Object rpcCall_getAnswerInfo(ClassLoader loader)
 {
  /*
   [{"answerSource":"foodTask","requestType":"NORMAL","sceneCode":"ANTFARM","source":"H5","version":"1.0.1907111732.11"}]
   */
  try
  {
   String args1 = "[{\"answerSource\":\"foodTask\",\"requestType\":\"NORMAL\",\"sceneCode\":\"ANTFARM\",\"source\":\"H5\",\"version\":\""
    +version+"\"}]";

   return RpcCall.invoke(loader, "com.alipay.antfarm.getAnswerInfo", args1);
  }catch(Exception e)
  {
   Log.i(TAG, "rpcCall_getAnswerInfo err: "+e.getMessage());
   Log.printStackTrace(TAG, e);
  }
  return null;
 }
 
 private static Object rpcCall_answerQuestion(ClassLoader loader, String quesId, int answer)
 {
  /*
   [{"answers":"[{\"questionId\":\"farm0369\",\"answers\":[1]}]","bizkey":"ANSWER","requestType":"NORMAL","sceneCode":"ANTFARM","source":"H5","version":"1.0.1907111732.11"}]
   */
  try
  {
   String args1 = "[{\"answers\":\"[{\\\"questionId\\\":\\\""+quesId+"\\\",\\\"answers\\\":["+answer+
    "]}]\",\"bizkey\":\"ANSWER\",\"requestType\":\"NORMAL\",\"sceneCode\":\"ANTFARM\",\"source\":\"H5\",\"version\":\""
    +version+"\"}]";

   return RpcCall.invoke(loader, "com.alipay.antfarm.doFarmTask", args1);
  }catch(Exception e)
  {
   Log.i(TAG, "rpcCall_answerQuestion err: "+e.getMessage());
   Log.printStackTrace(TAG, e);
  }
  return null;
 }
 
 private static Object rpcCall_receiveFarmTaskAward(ClassLoader loader, String taskId)
 {
  try
  {
   String args1 = "[{\"requestType\":\"NORMAL\",\"sceneCode\":\"ANTFARM\",\"source\":\"H5\",\"taskId\":\""
    +taskId+"\",\"version\":\""+version+"\"}]";
   return RpcCall.invoke(loader, "com.alipay.antfarm.receiveFarmTaskAward", args1);
  }catch(Exception e)
  {
   Log.i(TAG, "rpcCall_receiveFarmTaskAward err: "+e.getMessage());
   Log.printStackTrace(TAG, e);
  }
  return null;
 }
 
 private static Object rpcCall_feedAnimal(ClassLoader loader, String farmId)
 {
  try
  {
   String args1 = "[{\"animalType\":\"CHICK\",\"canMock\":true,\"farmId\":\""+farmId+
   "\",\"requestType\":\"NORMAL\",\"sceneCode\":\"ANTFARM\",\"source\":\"H5\",\"version\":\""
    +version+"\"}]";
   return RpcCall.invoke(loader, "com.alipay.antfarm.feedAnimal", args1);
  }catch(Exception e)
  {
   Log.i(TAG, "rpcCall_feedAnimal err: "+e.getMessage());
   Log.printStackTrace(TAG, e);
  }
  return null;
 }
 
 private static Object rpcCall_listFarmTool(ClassLoader loader)
 {
  try
  {
   String args1 = "[{\"requestType\":\"NORMAL\",\"sceneCode\":\"ANTFARM\",\"source\":\"H5\",\"version\":\""
   +version+"\"}]";
   return RpcCall.invoke(loader, "com.alipay.antfarm.listFarmTool", args1);
  }catch(Exception e)
  {
   Log.i(TAG, "rpcCall_listFarmTool err: "+e.getMessage());
   Log.printStackTrace(TAG, e);
  }
  return null;
 }
 
 private static Object rpcCall_useFarmTool(ClassLoader loader, String targetFarmId, String toolId, String toolType)
 {
  /*
   [{"requestType":"NORMAL","sceneCode":"ANTFARM","source":"H5","targetFarmId":"10181101172642012088022401920420","toolId":"501562732868401ACCELERATETOOL2088022401920420","toolType":"ACCELERATETOOL","version":"1.0.1907111732.11"}]
  */
  try
  {
   String args1 = "[{\"requestType\":\"NORMAL\",\"sceneCode\":\"ANTFARM\",\"source\":\"H5\",\"targetFarmId\":\""
    +targetFarmId+"\",\"toolId\":\""+toolId+"\",\"toolType\":\""+toolType+"\",\"version\":\""+version+"\"}]";
   return RpcCall.invoke(loader, "com.alipay.antfarm.useFarmTool", args1);
  }catch(Exception e)
  {
   Log.i(TAG, "rpcCall_useFarmTool err: "+e.getMessage());
   Log.printStackTrace(TAG, e);
  }
  return null;
 }
  
 private static Object rpcCall_rankingList(ClassLoader loader)
 {
  /*
   [{"pageSize":20,"requestType":"NORMAL","sceneCode":"ANTFARM","source":"H5","startNum":0,"version":"1.0.1907111732.11"}]
  */
  try
  {
   String args1 = "[{\"pageSize\":20,\"requestType\":\"NORMAL\",\"sceneCode\":\"ANTFARM\",\"source\":\"H5\",\"startNum\":"
   +pageStartSum+",\"version\":\""+version+"\"}]";
   pageStartSum += 20;
   
   return RpcCall.invoke(loader, "com.alipay.antfarm.rankingList", args1);
   
  }catch(Exception e)
  {
   Log.i(TAG, "rpcCall_rankingList err: "+e.getMessage());
   Log.printStackTrace(TAG, e);
  }
  return null;
 }
 
 private static boolean parseRankingListResponse(String resp)
 {
  boolean hasNext = false;
  try
  {
   JSONObject jo = new JSONObject(resp);
   hasNext = jo.getBoolean("hasNext");
   JSONArray jaRankingList = jo.getJSONArray("rankingList");
   if(jaRankingList == null)
    return true;
   for(int i = 0; i < jaRankingList.length(); i++)
   {
    jo = jaRankingList.getJSONObject(i);
    String userId = jo.getString("userId");
    boolean b = jo.getBoolean("stealingAnimal") || jo.has("actionType") &&
     jo.getString("actionType").equals("starve_action");
    if(b && !friendIdList.contains(userId))
    {
     friendIdList.add(userId);
    }
   }
   if(jaRankingList.length() == 0)
    return false;
  }catch(Exception e)
  {
   Log.i(TAG, "parseFrienRankPageDataResponse err: " + e.getMessage());
   Log.printStackTrace(TAG, e);
  }
  return hasNext;
 }
 
 private static Object rpcCall_enterFarm(ClassLoader loader, String farmId, String userId)
 {
  try
  {
   /*
    [{"animalId":"","cityAdCode":"440500","districtAdCode":"440514","farmId":"","masterFarmId":"","recall":false,"requestType":"NORMAL","sceneCode":"ANTFARM","source":"H5","touchRecordId":"","userId":"2088022401920420","version":"1.0.1907111732.11"}]
   */
   
   String args1 = "[{\"animalId\":\"\",\"cityAdCode\":\""+cityAdCode+"\",\"districtAdCode\":\""+districtAdCode+"\",\"farmId\":\""+farmId+
    "\",\"masterFarmId\":\"\",\"recall\":false,\"requestType\":\"NORMAL\",\"sceneCode\":\"ANTFARM\",\"source\":\"H5\",\"touchRecordId\":\"\",\"userId\":\""
    +userId+"\",\"version\":\""+version+"\"}]";

   return RpcCall.invoke(loader, "com.alipay.antfarm.enterFarm", args1);

  }catch(Exception e)
  {
   Log.i(TAG, "rpcCall_enterFarm err: "+e.getMessage());
   Log.printStackTrace(TAG, e);
  }
  return null;
 }
 
 private static Object rpcCall_notifyFriend(ClassLoader loader, String animalId, String notifiedFarmId)
 {
  try
  {
   String args1 = "[{\"animalId\":\""+animalId+
   "\",\"animalType\":\"CHICK\",\"canBeGuest\":true,\"notifiedFarmId\":\""+notifiedFarmId+
   "\",\"requestType\":\"NORMAL\",\"sceneCode\":\"ANTFARM\",\"source\":\"H5\",\"version\":\""
   +version+"\"}]";

   return RpcCall.invoke(loader, "com.alipay.antfarm.notifyFriend", args1);

  }catch(Exception e)
  {
   Log.i(TAG, "rpcCall_notifyFriend err: "+e.getMessage());
   Log.printStackTrace(TAG, e);
  }
  return null;
 }
 
 private static Object rpcCall_feedFriendAnimal(ClassLoader loader, String friendFarmId)
 {
  try
  {
   String args1 = "[{\"animalType\":\"CHICK\",\"canMock\":true,\"friendFarmId\":\""+friendFarmId+
    "\",\"requestType\":\"NORMAL\",\"sceneCode\":\"ANTFARM\",\"source\":\"H5\",\"version\":\""
    +version+"\"}]";

   return RpcCall.invoke(loader, "com.alipay.antfarm.feedFriendAnimal", args1);

  }catch(Exception e)
  {
   Log.i(TAG, "rpcCall_feedFriendAnimal err: "+e.getMessage());
   Log.printStackTrace(TAG, e);
  }
  return null;
 }
 
 public static String farmId2UserId(String farmId)
 {
  int l = farmId.length() / 2;
  return farmId.substring(l);
 }
 
 private static void add2FoodStock(int i)
 {
  foodStock += i;
  if(foodStock > foodStockLimit) foodStock = foodStockLimit;
 }
 
}
