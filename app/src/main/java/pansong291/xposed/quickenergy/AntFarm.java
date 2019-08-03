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
 { ACCELERATING, INJURED, NONE }
 public enum AnimalFeedStatus
 { HUNGRY, EATING }
 public enum AnimalInteractStatus
 { HOME, GOTOSTEAL, STEALING }
 public enum SubAnimalType
 { NORMAL, GUEST, PIRATE }
 public enum TaskStatus
 { TODO, FINISHED, RECEIVED }
 public enum ToolType
 {
  STEALTOOL, ACCELERATETOOL, SHARETOOL, FENCETOOL, NEWEGGTOOL;
  public String nickName()
  {
   switch(this)
   {
    case STEALTOOL:
     return "蹭饭卡";
    case ACCELERATETOOL:
     return "加速卡";
    case SHARETOOL:
     return "救济卡";
    case FENCETOOL:
     return "篱笆卡";
    case NEWEGGTOOL:
     return "新蛋卡";
    default:
     return name();
   }
  }
 }

 private static class Animal
 {
  public String animalId, currentFarmId, masterFarmId,
  animalBuff, subAnimalType, animalFeedStatus, animalInteractStatus;
 }

 private static class RewardFriend
 {
  public String consistencyKey, friendId, time;
 }

 /**private static class FarmTool
 {
  public ToolType toolType;
  public String toolId;
  public int toolCount, toolHoldLimit;
 }/**/
 
 private static String cityAdCode;
 private static String districtAdCode;
 private static String version;
 private static int pageStartSum = 0;

 private static String ownerFarmId;
 private static Animal[] animals;
 private static Animal ownerAnimal;
 private static int foodStock;
 private static int foodStockLimit;
 private static double rewardProductNum;
 private static RewardFriend[] rewardList;
 private static double benevolenceScore;
 private static double harvestBenevolenceScore;
 private static int unreceiveTaskAward = 0;
 //private static FarmTool[] farmTools;

 public static void start(ClassLoader loader, String args0, String args1, String response)
 {
  if(isEnterFarmAndHasNull(args0))
  {
   try
   {
    JSONArray ja = new JSONArray(args1);
    JSONObject jo = ja.getJSONObject(0);
    cityAdCode = jo.getString("cityAdCode");
    districtAdCode = jo.getString("districtAdCode");
    version = jo.getString("version");
   }catch(JSONException e)
   {
    Log.i(TAG, "start err: args1= "+args1);
    Log.printStackTrace(TAG, e);
   }
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

       if(Config.rewardFriend())rewardFriend(loader);

       // 赶鸡
       if(Config.sendBackAnimal())sendBackAnimal(loader);

       if(!AnimalInteractStatus.HOME.name().equals(ownerAnimal.animalInteractStatus))
       {
        switch(SubAnimalType.valueOf(ownerAnimal.subAnimalType))
        {
         case GUEST:
          Log.showDialogAndRecordLog("小鸡到好友家去做客了","");
          break;
         case NORMAL:
         case PIRATE:
          Log.showDialogAndRecordLog("小鸡太饿，离家出走了","");
          break;
         default:
          Log.showDialogAndRecordLog("小鸡不在庄园",ownerAnimal.subAnimalType);
        }

        boolean recall = false;
        switch(Config.recallAnimalType())
        {
         case ALWAYS:
          recall = true;
          break;
         case WHEN_THIEF:
          recall = !SubAnimalType.GUEST.name().equals(ownerAnimal.subAnimalType);
          break;
         case WHEN_HUNGRY:
          recall = AnimalFeedStatus.HUNGRY.name().equals(ownerAnimal.animalFeedStatus);
          break;
        }
        if(recall)
        {
         recallAnimal(loader,ownerAnimal.animalId,ownerAnimal.currentFarmId,ownerFarmId);
         syncAnimalStatus(loader, ownerFarmId);
        }
       }
       
       if(Config.receiveFarmToolReward())receiveToolTaskReward(loader);
       
       if(Config.useNewEggTool())
       {
        useFarmTool(loader, ownerFarmId, ToolType.NEWEGGTOOL);
        syncAnimalStatus(loader, ownerFarmId);
       }

       if(Config.harvestProduce() && benevolenceScore >= 1)
       {
        Log.showDialogAndRecordLog("有可收取的爱心鸡蛋","");
        harvestProduce(loader, ownerFarmId);
       }

       if(Config.donation() && harvestBenevolenceScore >= 5)
       {
        Log.showDialogAndRecordLog("爱心鸡蛋已达到可捐赠个数","");
        donation(loader);
       }

       if(Config.answerQuestion())answerQuestion(loader);

       if(Config.receiveFarmTaskAward())receiveFarmTaskAward(loader);

       if(AnimalInteractStatus.HOME.name().equals(ownerAnimal.animalInteractStatus))
       {
        if(Config.feedAnimal() && AnimalFeedStatus.HUNGRY.name().equals(ownerAnimal.animalFeedStatus))
        {
         Log.showDialogAndRecordLog("你的小鸡在挨饿","");
         // 喂鸡
         feedAnimal(loader, ownerFarmId);
         //syncAnimalStatus(loader,ownerFarmId);
        }

        if(AnimalBuff.ACCELERATING.name().equals(ownerAnimal.animalBuff))
        {
         Log.showDialogAndRecordLog("小鸡正双手并用着加速吃饲料","");
        }else if(Config.useAccelerateTool())
        {
         // 加速卡
         useFarmTool(loader, ownerFarmId, ToolType.ACCELERATETOOL);
        }

        if(unreceiveTaskAward > 0)
        {
         Log.showDialogAndRecordLog("还有待领取的饲料","");
         receiveFarmTaskAward(loader);
        }

       }

       // 通知好友赶鸡并帮好友喂鸡
       notifyFriendAndFeedAnimal(loader);

      }catch(Exception e)
      {
       Log.i(TAG, "run err:");
       Log.printStackTrace(TAG, e);
      }
     }
    }.setData(response, loader)).start();
  }

 }

 private static boolean isEnterFarmAndHasNull(String args0)
 {
  if(args0.equals("com.alipay.antfarm.enterFarm"))
   return cityAdCode == null || districtAdCode == null || version == null;
  return false;
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
   Log.i(TAG, "syncAnimalStatus err:");
   Log.printStackTrace(TAG, e);
  }
 }

 private static void rewardFriend(ClassLoader loader)
 {
  try
  {
   if(rewardList != null)
   {
    Log.showDialogAndRecordLog("开始打赏好友…","");
    for(int i = 0; i < rewardList.length; i++)
    {
     Object o = rpcCall_rewardFriend(loader, rewardList[i].consistencyKey, rewardList[i].friendId, rewardProductNum, rewardList[i].time);
     String s = RpcCall.getResponse(o);
     JSONObject jo = new JSONObject(s);
     String memo = jo.getString("memo");
     if(memo.equals("SUCCESS"))
     {
      double rewardCount = benevolenceScore - jo.getDouble("farmProduct");
      benevolenceScore -= rewardCount;
      Log.showDialogAndRecordLog("打赏了〔"+Config.getNameById(rewardList[i].friendId)+"〕〔"+rewardCount+"颗〕爱心鸡蛋","");
     }else
     {
      Log.showDialogAndRecordLog("失败，"+memo,s);
     }
    }
    rewardList = null;
   }
  }catch(Exception e)
  {
   Log.i(TAG, "rewardFriend err:");
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
   String memo = jo.getString("memo");
   if(memo.equals("SUCCESS"))
   {
    double foodHaveStolen = jo.getDouble("foodHaveStolen");
    Log.showDialogAndRecordLog("已召回小鸡，偷吃了〔"+Config.getNameById(farmId2UserId(currentFarmId))+"〕的〔"+foodHaveStolen+"克〕饲料","");
    // 这里不需要加
    // add2FoodStock((int)foodHaveStolen);
   }else
   {
    Log.showDialogAndRecordLog("失败，"+memo,s);
   }
  }catch(Exception e)
  {
   Log.i(TAG, "recallAnimal err:");
   Log.printStackTrace(TAG, e);
  }
 }

 private static void sendBackAnimal(ClassLoader loader)
 {
  try
  {
   Log.showDialogAndRecordLog("开始赶鸡…","");
   boolean hasStealingAnimal = false;
   for(int i = 0; i < animals.length; i++)
   {
    if(AnimalInteractStatus.STEALING.name().equals(animals[i].animalInteractStatus)
     && !SubAnimalType.GUEST.name().equals(animals[i].subAnimalType))
    {
     hasStealingAnimal = true;
     // 赶鸡
     String user = farmId2UserId(animals[i].masterFarmId);
     SendType sendType = Config.sendType(user);
     user = Config.getNameById(user);
     Object o = rpcCall_sendBackAnimal(loader, sendType.name(), animals[i].animalId,
      animals[i].currentFarmId, animals[i].masterFarmId);
     String s = RpcCall.getResponse(o);
     JSONObject jo = new JSONObject(s);
     String memo = jo.getString("memo");
     if(memo.equals("SUCCESS"))
     {
      if(sendType == SendType.HIT)
      {
       s = "你对〔"+user+"〕的小鸡发起攻击\n";
       if(jo.has("hitLossFood"))
       {
        s += "胖揍了〔"+user+"〕的小鸡，掉落了〔"+jo.getInt("hitLossFood")+"克〕饲料";
        foodStock = jo.getInt("finalFoodStorage");
        s += "\n剩余〔"+foodStock+"克〕饲料";
       }else
        s += "〔"+user+"〕的小鸡躲开了你的攻击";
      }else
      {
       s = "已赶走〔"+user+"〕的小鸡";
      }
     }else
     {
      Log.showDialogAndRecordLog("失败，"+memo,s);
     }
     Log.showDialogAndRecordLog(s,"");
    }
   }
   if(!hasStealingAnimal)
    Log.showDialogAndRecordLog("没有来偷吃的小鸡","");
  }catch(Exception e)
  {
   Log.i(TAG, "sendBackAnimal err:");
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
   String memo = jo.getString("memo");
   if(memo.equals("SUCCESS"))
   {
    double harvest = jo.getDouble("harvestBenevolenceScore");
    harvestBenevolenceScore = jo.getDouble("finalBenevolenceScore");
    Log.showDialogAndRecordLog("收取了〔"+harvest+"颗〕爱心鸡蛋，剩余〔"+harvestBenevolenceScore+"颗〕","");
   }else
   {
    Log.showDialogAndRecordLog("失败，"+memo,s);
   }
  }catch(Exception e)
  {
   Log.i(TAG, "harvestProduce err:");
   Log.printStackTrace(TAG, e);
  }
 }

 private static void donation(ClassLoader loader)
 {
  try
  {
   Log.showDialogAndRecordLog("开始捐赠爱心鸡蛋","");
   Object o = rpcCall_listActivityInfo(loader);
   String s = RpcCall.getResponse(o);
   JSONObject jo = new JSONObject(s);
   String memo = jo.getString("memo");
   if(memo.equals("SUCCESS"))
   {
    JSONArray jaActivityInfos = jo.getJSONArray("activityInfos");
    String activityId = null, activityName = null;
    for(int i = 0; i < jaActivityInfos.length(); i++)
    {
     jo = jaActivityInfos.getJSONObject(i);
     if(!jo.get("donationTotal").equals(jo.get("donationLimit")))
     {
      activityId = jo.getString("activityId");
      activityName = jo.getString("activityName");
      break;
     }
    }
    if(activityId == null)
    {
     Log.showDialogAndRecordLog("今日已无可捐赠的活动","");
    }else
    {
     o = rpcCall_donation(loader, activityId);
     s = RpcCall.getResponse(o);
     jo = new JSONObject(s);
     memo = jo.getString("memo");
     if(memo.equals("SUCCESS"))
     {
      jo = jo.getJSONObject("donation");
      harvestBenevolenceScore = jo.getDouble("harvestBenevolenceScore");
      Log.showDialogAndRecordLog("捐赠活动〔"+activityName+"〕成功，累计捐赠"+jo.getInt("donationTimesStat")+"次","");
     }else
     {
      Log.showDialogAndRecordLog("失败，"+memo,s);
     }
    }
   }else
   {
    Log.showDialogAndRecordLog("失败，"+memo,s);
   }
  }catch(Exception e)
  {
   Log.i(TAG, "donation err:");
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
   String memo = jo.getString("memo");
   if(memo.equals("SUCCESS"))
   {
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
        memo = jo.getString("memo");
        if(memo.equals("SUCCESS"))
        {
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
          memo = jo.getString("memo");
          if(memo.equals("SUCCESS"))
          {
           s = jo.getBoolean("rightAnswer")? "正确":"错误";
           Log.showDialogAndRecordLog("答题"+s+"，可领取［"+jo.getInt("awardCount")+"克］饲料", "");
          }else
          {
           Log.showDialogAndRecordLog("失败，"+memo,s);
          }
         }else
         {
          Log.showDialogAndRecordLog("未找到正确答案，放弃作答","");
         }
        }else
        {
         Log.showDialogAndRecordLog("失败，"+memo,s);
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
   }else
   {
    Log.showDialogAndRecordLog("失败，"+memo,s);
   }
  }catch(Exception e)
  {
   Log.i(TAG, "answerQuestion err:");
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
   String memo = jo.getString("memo");
   if(memo.equals("SUCCESS"))
   {
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
       Log.showDialogAndRecordLog("领取"+awardCount+"克饲料后将超过〔"+foodStockLimit+"克〕上限，已终止领取","");
       break;
      }
      String title = jo.getString("title");
      o = rpcCall_receiveFarmTaskAward(loader, jo.getString("taskId"));
      s = RpcCall.getResponse(o);
      jo = new JSONObject(s);
      memo = jo.getString("memo");
      if(memo.equals("SUCCESS"))
      {
       foodStock = jo.getInt("foodStock");
       Log.showDialogAndRecordLog("已领取〔"+jo.getInt("haveAddFoodStock")+"克〕饲料，来源："+title,"");
       if(unreceiveTaskAward > 0)unreceiveTaskAward--;
      }else
      {
       Log.showDialogAndRecordLog("失败，"+memo,s);
      }
     }
    }
    if(!hasAwardFood)
     Log.showDialogAndRecordLog("暂时没有可领取的饲料","");
    Log.showDialogAndRecordLog("剩余〔"+foodStock+"克〕饲料","");
   }else
   {
    Log.showDialogAndRecordLog("失败，"+memo,s);
   }
  }catch(Exception e)
  {
   Log.i(TAG, "receiveFarmTaskAward err:");
   Log.printStackTrace(TAG, e);
  }
 }

 private static void receiveToolTaskReward(ClassLoader loader)
 {
  try
  {
   Log.showDialogAndRecordLog("开始领取道具卡…","");
   Object o = rpcCall_listToolTaskDetails(loader);
   String s = RpcCall.getResponse(o);
   JSONObject jo = new JSONObject(s);
   String memo = jo.getString("memo");
   if(memo.equals("SUCCESS"))
   {
    JSONArray jaList = jo.getJSONArray("list");
    boolean hasRewardTool = false;
    for(int i = 0; i < jaList.length(); i++)
    {
     jo = jaList.getJSONObject(i);
     if(jo.has("taskStatus")
      && TaskStatus.FINISHED.name().equals(jo.getString("taskStatus")))
     {
      hasRewardTool = true;
      int awardCount = jo.getInt("awardCount");
      String awardType = jo.getString("awardType");
      ToolType toolType = ToolType.valueOf(awardType);
      String taskType = jo.getString("taskType");
      /**if(awardCount + foodStock > foodStockLimit)
      {
       Log.showDialogAndRecordLog("领取"+awardCount+"克饲料后将超过〔"+foodStockLimit+"克〕上限，已终止领取","");
       break;
      }/**/
      jo = new JSONObject(jo.getString("bizInfo"));
      String taskTitle = jo.getString("taskTitle");
      o = rpcCall_receiveToolTaskReward(loader, awardType, awardCount, taskType);
      s = RpcCall.getResponse(o);
      jo = new JSONObject(s);
      memo = jo.getString("memo");
      if(memo.equals("SUCCESS"))
      {
       //int toolStockNum = jo.getInt("toolStockNum");
       Log.showDialogAndRecordLog("已领取〔"+awardCount+"张〕〔"+toolType.nickName()+"〕，来源："+taskTitle,"");
      }else
      {
       Log.showDialogAndRecordLog("失败，"+memo,s);
      }
     }
    }
    if(!hasRewardTool)
     Log.showDialogAndRecordLog("暂时没有可领取的道具卡","");
   }else
   {
    Log.showDialogAndRecordLog("失败，"+memo,s);
   }
  }catch(Exception e)
  {
   Log.i(TAG, "receiveToolTaskReward err:");
   Log.printStackTrace(TAG, e);
  }
 }

 private static void feedAnimal(ClassLoader loader, String farmId)
 {
  try
  {
   Log.showDialogAndRecordLog("开始喂鸡…","");
   if(foodStock < 180)
   {
    Log.showDialogAndRecordLog("饲料不足","");
   }else
   {
    Object o = rpcCall_feedAnimal(loader, farmId);
    String s = RpcCall.getResponse(o);
    JSONObject jo = new JSONObject(s);
    String memo = jo.getString("memo");
    if(memo.equals("SUCCESS"))
    {
     int feedFood = foodStock - jo.getInt("foodStock");
     add2FoodStock(-feedFood);
     Log.showDialogAndRecordLog("喂了小鸡［"+feedFood+"克］饲料，剩余〔"+foodStock+"克〕","");
    }else
    {
     Log.showDialogAndRecordLog("失败，"+memo,s);
    }
   }
  }catch(Exception e)
  {
   Log.i(TAG, "feedAnimal err:");
   Log.printStackTrace(TAG, e);
  }
 }

 private static void useFarmTool(ClassLoader loader, String targetFarmId, ToolType toolType)
 {
  try
  {
   Object o = rpcCall_listFarmTool(loader);
   String s = RpcCall.getResponse(o);
   JSONObject jo = new JSONObject(s);
   String memo = jo.getString("memo");
   if(memo.equals("SUCCESS"))
   {
    JSONArray jaToolList = jo.getJSONArray("toolList");
    for(int i = 0; i < jaToolList.length(); i++)
    {
     jo = jaToolList.getJSONObject(i);
     if(toolType.name().equals(jo.getString("toolType")))
     {
      Log.showDialogAndRecordLog("开始使用"+toolType.nickName()+"…","");
      int toolCount = jo.getInt("toolCount");
      if(toolCount > 0)
      {
       o = rpcCall_useFarmTool(loader, targetFarmId, jo.getString("toolId"), toolType.name());
       s = RpcCall.getResponse(o);
       jo = new JSONObject(s);
       memo = jo.getString("memo");
       if(memo.equals("SUCCESS"))
        Log.showDialogAndRecordLog("使用成功，剩余〔"+(toolCount-1)+"张〕","");
       else Log.showDialogAndRecordLog("失败，"+memo,s);
      }else Log.showDialogAndRecordLog("没有"+toolType.nickName()+"可用","");
      break;
     }
    }
   }else
   {
    Log.showDialogAndRecordLog("失败，"+memo,s);
   }
  }catch(Exception e)
  {
   Log.i(TAG, "useFarmTool err:");
   Log.printStackTrace(TAG, e);
  }
 }

 private static void notifyFriendAndFeedAnimal(ClassLoader loader)
 {
  try
  {
   Log.showDialogAndRecordLog("开始通知好友来赶鸡…","");
   boolean hasNext = false;
   Object o;
   String s;
   JSONObject jo;
   do
   {
    o = rpcCall_rankingList(loader);
    s = RpcCall.getResponse(o);
    jo = new JSONObject(s);
    String memo = jo.getString("memo");
    if(memo.equals("SUCCESS"))
    {
     hasNext = jo.getBoolean("hasNext");
     JSONArray jaRankingList = jo.getJSONArray("rankingList");
     for(int i = 0; i < jaRankingList.length(); i++)
     {
      jo = jaRankingList.getJSONObject(i);
      String userId = jo.getString("userId");
      String user = Config.getNameById(userId);
      boolean starve = jo.has("actionType") && 
       jo.getString("actionType").equals("starve_action");
      boolean feedFriendAnimal = starve && Config.feedFriendAnimal(userId);
      if(jo.getBoolean("stealingAnimal") && !starve || feedFriendAnimal)
      {
       o = rpcCall_enterFarm(loader, "", userId);
       s = RpcCall.getResponse(o);
       jo = new JSONObject(s);
       memo = jo.getString("memo");
       if(memo.equals("SUCCESS"))
       {
        jo = jo.getJSONObject("farmVO").getJSONObject("subFarmVO");
        String friendFarmId = jo.getString("farmId");
        JSONArray jaAnimals = jo.getJSONArray("animals");
        boolean notified = !Config.notifyFriend();
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
          notified = notifyFriend(loader, jo, friendFarmId, animalId);
         }
        }
       }else
       {
        Log.showDialogAndRecordLog("失败，"+memo,s);
       }
      }
     }
    }else
    {
     Log.showDialogAndRecordLog("失败，"+memo,s);
    }
   }while(hasNext);
   pageStartSum = 0;
   Log.showDialogAndRecordLog("通知好友结束，饲料剩余〔"+foodStock+"克〕","");
  }catch(Exception e)
  {
   Log.i(TAG, "notifyFriendAndFeed err:");
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
    String memo = joAnimalStatusVO.getString("memo");
    if(memo.equals("SUCCESS"))
    {
     double rewardCount = joAnimalStatusVO.getDouble("rewardCount");
     foodStock = (int)joAnimalStatusVO.getDouble("finalFoodStock");
     Log.showDialogAndRecordLog("已通知〔"+user+"〕，小鸡拿出〔"+rewardCount+"克〕饲料奖励你","");
     return true;
    }else
    {
     Log.showDialogAndRecordLog("失败，"+memo,s);
    }
   }
  }catch(Exception e)
  {
   Log.i(TAG, "notifyFriend err:");
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
    JSONObject jo = new JSONObject(s);
    String memo = jo.getString("memo");
    if(memo.equals("SUCCESS"))
    {
     int feedFood = foodStock - jo.getInt("foodStock");
     if(feedFood > 0)
     {
      add2FoodStock(-feedFood);
      Log.showDialogAndRecordLog("喂了〔"+user+"〕的小鸡〔"+feedFood+"克〕饲料，剩余〔"+foodStock+"克〕","");
      for(int i = 0; i < jaAnimals.length(); i++)
      {
       jo = jaAnimals.getJSONObject(i);
       JSONObject joo = jo.getJSONObject("animalStatusVO");
       joo.put("animalFeedStatus", AnimalFeedStatus.EATING.name());
       jo.put("animalStatusVO", joo);
       jaAnimals.put(i, jo);
      }
     }
    }else
    {
     Log.showDialogAndRecordLog("失败，"+memo,s);
    }
   }
  }catch(Exception e)
  {
   Log.i(TAG, "feedFriendAnimal err:");
   Log.printStackTrace(TAG, e);
  }
 }

 private static Object rpcCall_syncAnimalStatus(ClassLoader loader, String farmId)
 {
  try
  {
   String args1 = "[{\"farmId\":\""+farmId+
    "\",\"operType\":\"FEEDSYNC\",\"queryFoodStockInfo\":false,\"recall\":false,\"requestType\":\"NORMAL\",\"sceneCode\":\"ANTFARM\",\"source\":\"H5\",\"userId\":\""
    +farmId2UserId(farmId)+"\",\"version\":\""+version+"\"}]";
   return RpcCall.invoke(loader, "com.alipay.antfarm.syncAnimalStatus", args1);
  }catch(Exception e)
  {
   Log.i(TAG, "rpcCall_syncAnimalStatus err:");
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
      rewardList[i].time = joRewardList.getString("time");
     }
    }
   }
   JSONArray jaAnimals = jo.getJSONArray("animals");
   animals = new Animal[jaAnimals.length()];
   for(int i = 0; i < animals.length; i++)
   {
    if(animals[i] == null)animals[i] = new Animal();
    jo = jaAnimals.getJSONObject(i);
    animals[i].animalId = jo.getString("animalId");
    animals[i].currentFarmId = jo.getString("currentFarmId");
    animals[i].masterFarmId = jo.getString("masterFarmId");
    animals[i].animalBuff = jo.getString("animalBuff");
    animals[i].subAnimalType = jo.getString("subAnimalType");
    jo = jo.getJSONObject("animalStatusVO");
    animals[i].animalFeedStatus = jo.getString("animalFeedStatus");
    animals[i].animalInteractStatus = jo.getString("animalInteractStatus");
    if(animals[i].masterFarmId.equals(ownerFarmId))
     ownerAnimal = animals[i];
    Log.i("owner","ownerFarmId="+ownerFarmId);
    Log.i(i+" animal","animalId="+animals[i].animalId);
    Log.i(i+" animal","currentFarmId="+animals[i].currentFarmId);
    Log.i(i+" animal","masterFarmId="+animals[i].masterFarmId);
    Log.i(i+" animal","animalBuff="+animals[i].animalBuff);
    Log.i(i+" animal","animalFeedStatus="+animals[i].animalFeedStatus);
    Log.i(i+" animal","animalInteractStatus="+animals[i].animalInteractStatus);
   }
  }catch(Exception e)
  {
   Log.i(TAG, "parseSyncAnimalStatusResponse err:");
   Log.printStackTrace(TAG, e);
  }
 }

 private static Object rpcCall_rewardFriend(ClassLoader loader, String consistencyKey, String friendId, double productNum, String time)
 {
  try
  {
   String args1 = "[{\"canMock\":true,\"consistencyKey\":\""+consistencyKey
    +"\",\"friendId\":\""+friendId+"\",\"operType\":\"1\",\"productNum\":"+productNum+
    ",\"requestType\":\"NORMAL\",\"sceneCode\":\"ANTFARM\",\"source\":\"H5\",\"time\":"
    +time+",\"version\":\""+version+"\"}]";
   return RpcCall.invoke(loader, "com.alipay.antfarm.rewardFriend", args1);
  }catch(Exception e)
  {
   Log.i(TAG, "rpcCall_rewardFriend err:");
   Log.printStackTrace(TAG, e);
  }
  return null;
 }

 private static Object rpcCall_recallAnimal(ClassLoader loader, String animalId, String currentFarmId, String masterFarmId)
 {
  try
  {
   String args1 = "[{\"animalId\":\""+animalId+"\",\"currentFarmId\":\""
    +currentFarmId+"\",\"masterFarmId\":\""+masterFarmId+
    "\",\"requestType\":\"NORMAL\",\"sceneCode\":\"ANTFARM\",\"source\":\"H5\",\"version\":\""
    +version+"\"}]";
   return RpcCall.invoke(loader, "com.alipay.antfarm.recallAnimal", args1);
  }catch(Exception e)
  {
   Log.i(TAG, "rpcCall_recallAnimal err:");
   Log.printStackTrace(TAG, e);
  }
  return null;
 }

 private static Object rpcCall_sendBackAnimal(ClassLoader loader, String sendType, String animalId, String currentFarmId, String masterFarmId)
 {
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
   Log.i(TAG, "rpcCall_sendBackAnimal err:");
   Log.printStackTrace(TAG, e);
  }
  return null;
 }

 private static Object rpcCall_harvestProduce(ClassLoader loader, String farmId)
 {
  try
  {
   String args1 = "[{\"canMock\":true,\"farmId\":\""+farmId+
    "\",\"giftType\":\"\",\"requestType\":\"NORMAL\",\"sceneCode\":\"ANTFARM\",\"source\":\"H5\",\"version\":\""
    +version+"\"}]";
   return RpcCall.invoke(loader, "com.alipay.antfarm.harvestProduce", args1);
  }catch(Exception e)
  {
   Log.i(TAG, "rpcCall_harvestProduce err:");
   Log.printStackTrace(TAG, e);
  }
  return null;
 }

 private static Object rpcCall_listActivityInfo(ClassLoader loader)
 {
  try
  {
   String args1 = "[{\"requestType\":\"NORMAL\",\"sceneCode\":\"ANTFARM\",\"source\":\"H5\",\"version\":\""
    +version+"\"}]";
   return RpcCall.invoke(loader, "com.alipay.antfarm.listActivityInfo", args1);
  }catch(Exception e)
  {
   Log.i(TAG, "rpcCall_listActivityInfo err:");
   Log.printStackTrace(TAG, e);
  }
  return null;
 }

 private static Object rpcCall_donation(ClassLoader loader, String activityId)
 {
  try
  {
   String args1 = "[{\"activityId\":\""+activityId+
    "\",\"donationAmount\":5,\"requestType\":\"NORMAL\",\"sceneCode\":\"ANTFARM\",\"source\":\"H5\",\"version\":\""
    +version+"\"}]";
   return RpcCall.invoke(loader, "com.alipay.antfarm.donation", args1);
  }catch(Exception e)
  {
   Log.i(TAG, "rpcCall_donation err:");
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
   Log.i(TAG, "rpcCall_listFarmTask err:");
   Log.printStackTrace(TAG, e);
  }
  return null;
 }

 private static Object rpcCall_getAnswerInfo(ClassLoader loader)
 {
  try
  {
   String args1 = "[{\"answerSource\":\"foodTask\",\"requestType\":\"NORMAL\",\"sceneCode\":\"ANTFARM\",\"source\":\"H5\",\"version\":\""
    +version+"\"}]";
   return RpcCall.invoke(loader, "com.alipay.antfarm.getAnswerInfo", args1);
  }catch(Exception e)
  {
   Log.i(TAG, "rpcCall_getAnswerInfo err:");
   Log.printStackTrace(TAG, e);
  }
  return null;
 }

 private static Object rpcCall_answerQuestion(ClassLoader loader, String quesId, int answer)
 {
  try
  {
   String args1 = "[{\"answers\":\"[{\\\"questionId\\\":\\\""+quesId+"\\\",\\\"answers\\\":["+answer+
    "]}]\",\"bizkey\":\"ANSWER\",\"requestType\":\"NORMAL\",\"sceneCode\":\"ANTFARM\",\"source\":\"H5\",\"version\":\""
    +version+"\"}]";
   return RpcCall.invoke(loader, "com.alipay.antfarm.doFarmTask", args1);
  }catch(Exception e)
  {
   Log.i(TAG, "rpcCall_answerQuestion err:");
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
   Log.i(TAG, "rpcCall_receiveFarmTaskAward err:");
   Log.printStackTrace(TAG, e);
  }
  return null;
 }

 private static Object rpcCall_listToolTaskDetails(ClassLoader loader)
 {
  try
  {
   String args1 = "[{\"requestType\":\"NORMAL\",\"sceneCode\":\"ANTFARM\",\"source\":\"H5\",\"version\":\""
    +version+"\"}]";
   return RpcCall.invoke(loader, "com.alipay.antfarm.listToolTaskDetails", args1);
  }catch(Exception e)
  {
   Log.i(TAG, "rpcCall_listToolTaskDetails err:");
   Log.printStackTrace(TAG, e);
  }
  return null;
 }

 private static Object rpcCall_receiveToolTaskReward(ClassLoader loader, String awardType, int rewardCount, String taskType)
 {
  try
  {
   String args1 = "[{\"awardType\":\""+awardType+
   "\",\"ignoreLimit\":false,\"requestType\":\"NORMAL\",\"rewardCount\":"
   +rewardCount+",\"rewardType\":\""+awardType+
   "\",\"sceneCode\":\"ANTFARM\",\"source\":\"H5\",\"taskType\":\""
   +taskType+"\",\"version\":\""+version+"\"}]";
   return RpcCall.invoke(loader, "com.alipay.antfarm.receiveToolTaskReward", args1);
  }catch(Exception e)
  {
   Log.i(TAG, "rpcCall_receiveToolTaskReward err:");
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
   Log.i(TAG, "rpcCall_feedAnimal err:");
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
   Log.i(TAG, "rpcCall_listFarmTool err:");
   Log.printStackTrace(TAG, e);
  }
  return null;
 }

 private static Object rpcCall_useFarmTool(ClassLoader loader, String targetFarmId, String toolId, String toolType)
 {
  try
  {
   String args1 = "[{\"requestType\":\"NORMAL\",\"sceneCode\":\"ANTFARM\",\"source\":\"H5\",\"targetFarmId\":\""
    +targetFarmId+"\",\"toolId\":\""+toolId+"\",\"toolType\":\""+toolType+"\",\"version\":\""+version+"\"}]";
   return RpcCall.invoke(loader, "com.alipay.antfarm.useFarmTool", args1);
  }catch(Exception e)
  {
   Log.i(TAG, "rpcCall_useFarmTool err:");
   Log.printStackTrace(TAG, e);
  }
  return null;
 }

 private static Object rpcCall_rankingList(ClassLoader loader)
 {
  try
  {
   String args1 = "[{\"pageSize\":20,\"requestType\":\"NORMAL\",\"sceneCode\":\"ANTFARM\",\"source\":\"H5\",\"startNum\":"
    +pageStartSum+",\"version\":\""+version+"\"}]";
   pageStartSum += 20;
   return RpcCall.invoke(loader, "com.alipay.antfarm.rankingList", args1);
  }catch(Exception e)
  {
   Log.i(TAG, "rpcCall_rankingList err:");
   Log.printStackTrace(TAG, e);
  }
  return null;
 }

 private static Object rpcCall_enterFarm(ClassLoader loader, String farmId, String userId)
 {
  try
  {   
   String args1 = "[{\"animalId\":\"\",\"cityAdCode\":\""+cityAdCode+"\",\"districtAdCode\":\""+districtAdCode+"\",\"farmId\":\""+farmId+
    "\",\"masterFarmId\":\"\",\"recall\":false,\"requestType\":\"NORMAL\",\"sceneCode\":\"ANTFARM\",\"source\":\"H5\",\"touchRecordId\":\"\",\"userId\":\""
    +userId+"\",\"version\":\""+version+"\"}]";
   return RpcCall.invoke(loader, "com.alipay.antfarm.enterFarm", args1);
  }catch(Exception e)
  {
   Log.i(TAG, "rpcCall_enterFarm err:");
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
   Log.i(TAG, "rpcCall_notifyFriend err:");
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
   Log.i(TAG, "rpcCall_feedFriendAnimal err:");
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
