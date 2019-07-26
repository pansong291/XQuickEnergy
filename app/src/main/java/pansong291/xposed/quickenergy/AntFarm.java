package pansong291.xposed.quickenergy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;

public class AntFarm
{
 private static final String TAG = AntFarm.class.getCanonicalName();
 private static String cityAdCode;
 private static String districtAdCode;
 private static String version;
 private static String ownerFarmId;
 private static String ownerAnimalId;
 private static int foodStock;
 private static int foodStockLimit;
 public enum AnimalBuff
 { ACCELERATING, INJURED, NONE };
 private static String animalBuff;
 public enum AnimalFeedStatus
 { HUNGRY, EATING };
 private static String animalFeedStatus;
 public enum AnimalInteractStatus
 { HOME, GOTOSTEAL, STEALING };
 private static String animalInteractStatus;
 public enum TaskStatus
 { TODO, FINISHED, RECEIVED };
 public enum ToolType
 { STEALTOOL, ACCELERATETOOL, SHARETOOL, FENCETOOL, NEWEGGTOOL };
 private static int pageStartSum = 0;
 private static List<String> firendIdList = new ArrayList<>();
 
 public static void start(ClassLoader loader, String args0, String args1, String response)
 {
  if(isEnterFarmAndHasNull(args0))
   try
   {
    JSONArray ja = new JSONArray(args1);
    JSONObject jo = ja.getJSONObject(0);
    cityAdCode = jo.optString("cityAdCode");
    districtAdCode = jo.optString("districtAdCode");
    version = jo.optString("version");
   }catch(JSONException e)
   {
    Log.i(TAG, "start err: "+e.getMessage()+", args1="+args1);
   }
   
  if(isEnterOwnerFarm(response))
  {
   try
   {
    JSONObject jo = new JSONObject(response);
    JSONObject joFarmVO = jo.optJSONObject("farmVO");
    foodStock = joFarmVO.optInt("foodStock");
    foodStockLimit = joFarmVO.optInt("foodStockLimit");
    joFarmVO.optDouble("harvestBenevolenceScore");
    JSONObject joSubFarmVO = joFarmVO.optJSONObject("subFarmVO");
    ownerFarmId = joSubFarmVO.optString("farmId");
    JSONArray jaAnimals = joSubFarmVO.optJSONArray("animals");
    String goToStealFarmId = "";
    for(int i = 0; i < jaAnimals.length(); i++)
    {
     jo = jaAnimals.optJSONObject(i);
     String animalId = jo.optString("animalId");
     String currentFarmId = jo.optString("currentFarmId");
     String masterFarmId = jo.optString("masterFarmId");
     JSONObject joAnimalStatusVO = jo.optJSONObject("animalStatusVO");
     if(joAnimalStatusVO
      .optString("animalInteractStatus").equals(AnimalInteractStatus.STEALING.name()))
     {
      // 赶鸡
      sendBackAnimal(loader, animalId, currentFarmId, masterFarmId);
     }
     
     if(masterFarmId.equals(ownerFarmId))
     {
      // 自己的鸡
      animalBuff = jo.optString("animalBuff");
      animalFeedStatus = joAnimalStatusVO
       .optString("animalFeedStatus");
      animalInteractStatus = joAnimalStatusVO
       .optString("animalInteractStatus");
      ownerAnimalId = animalId;
      goToStealFarmId = currentFarmId;
     }
    }
    if(animalInteractStatus.equals(AnimalInteractStatus.GOTOSTEAL.name()))
    {
     //if(animalFeedStatus.equals(AnimalFeedStatus.HUNGRY.name()))
     recallAnimal(loader,ownerAnimalId,goToStealFarmId,ownerFarmId);
    }

    if(animalFeedStatus.equals(AnimalFeedStatus.HUNGRY.name()))
    {
     if(foodStock < 180)
     {
      // 答题或领取饲料
      answerQuestion(loader);
      receiveFarmTaskAward(loader);
     }
     // 喂鸡
     feedAnimal(loader, ownerFarmId);
    }
    
    if(!animalBuff.equals(AnimalBuff.ACCELERATING.name()))
    {
     // 加速卡
     useAccelerateTool(loader);
    }
   }catch(Exception e)
   {
    Log.i(TAG, "start err: "+e.getMessage());
   }
   
   
  }
  
 }
 
 private static boolean isEnterFarmAndHasNull(String args0)
 {
  return cityAdCode == null || districtAdCode == null
   || version == null && args0.equals("com.alipay.antfarm.enterFarm");
 }
 
 private static boolean isEnterOwnerFarm(String resp)
 {
  return resp.contains("\"relation\":\"OWNER\"");
 }
 
 private static void recallAnimal(ClassLoader loader, String animalId, String currentFarmId, String masterFarmId)
 {
  try
  {
   Object o = rpcCall_RecallAnimal(loader, animalId, currentFarmId, masterFarmId);
   String s = RpcCall.getResponse(o);
   JSONObject jo = new JSONObject(s);
   double foodHaveStolen = jo.optDouble("foodHaveStolen");
   Log.showDialogAndRecordLog("唤回小鸡，并偷吃了"+Config.getNameById(farmId2UserId(currentFarmId))+"［"+foodHaveStolen+"克］饲料","");
  }catch(Exception e)
  {
   Log.i(TAG, "recallAnimal err: "+e.getMessage());
  }
 }
 
 private static void sendBackAnimal(ClassLoader loader, String animalId, String currentFarmId, String masterFarmId)
 {
  try
  {
   Object o = rpcCall_SendBackAnimal(loader, animalId, currentFarmId, masterFarmId);
   String s = RpcCall.getResponse(o);
   JSONObject jo = new JSONObject(s);
   if(jo.has("hitLossFood"))
   {
    s = "胖揍了"+Config.getNameById(farmId2UserId(masterFarmId))+"的小鸡，掉落［"+jo.optInt("hitLossFood")+"克］饲料";
    foodStock += jo.optInt("saveFoodNum");
    if(foodStock > foodStockLimit) foodStock = foodStockLimit;
   }else
    s = Config.getNameById(farmId2UserId(masterFarmId))+"的小鸡在手底下溜走了";
   Log.showDialogAndRecordLog(s,"");
  }catch(Exception e)
  {
   Log.i(TAG, "sendBackAnimal err: "+e.getMessage());
  }
 }
 
 private static void answerQuestion(ClassLoader loader)
 {
  try
  {
   Object o = rpcCall_ListFarmTask(loader);
   String s = RpcCall.getResponse(o);
   JSONObject jo = new JSONObject(s);
   JSONArray jaFarmTaskList = jo.optJSONArray("farmTaskList");
   for(int i = 0; i < jaFarmTaskList.length(); i++)
   {
    jo = jaFarmTaskList.optJSONObject(i);
    if(jo.optString("title").equals("庄园小课堂")&&
       jo.optString("taskStatus").equals(TaskStatus.TODO.name()))
    {
     o = rpcCall_getAnswerInfo(loader);
     s = RpcCall.getResponse(o);
     jo = new JSONObject(s);
     jo = jo.optJSONArray("answerInfoVOs").optJSONObject(0);
     Log.showDialogAndRecordLog(jo.optString("questionContent"),"");
     Log.showDialogAndRecordLog(jo.optString("rightReply"),"");
     int answer = jo.optInt("answerNum");
     String questionId = jo.optString("questionId");
     o = rpcCall_AnswerQuestion(loader, questionId, answer);
     s = RpcCall.getResponse(o);
     jo = new JSONObject(s);
     s = jo.optBoolean("rightAnswer")? "正确":"错误";
     Log.showDialogAndRecordLog("答题"+s+"，可领取［"+jo.optInt("awardCount",0)+"克］饲料", "");
     break;
    }
   }
  }catch(Exception e)
  {
   Log.i(TAG, "answerQuestion err: "+e.getMessage());
  }
 }
 
 private static void receiveFarmTaskAward(ClassLoader loader)
 {
  try
  {
   Object o = rpcCall_ListFarmTask(loader);
   String s = RpcCall.getResponse(o);
   JSONObject jo = new JSONObject(s);
   JSONArray jaFarmTaskList = jo.optJSONArray("farmTaskList");
   for(int i = 0; i < jaFarmTaskList.length(); i++)
   {
    jo = jaFarmTaskList.optJSONObject(i);
    if(jo.optString("taskStatus").equals(TaskStatus.FINISHED.name()))
    {
     if(jo.optInt("awardCount") + foodStock > foodStockLimit)
     {
      Log.showDialogAndRecordLog("领取饲料后将超过上限，已终止领取饲料","");
      break;
     }
     o = rpcCall_ReceiveFarmTaskAward(loader, jo.optString("taskId"));
     s = RpcCall.getResponse(o);
     jo = new JSONObject(s);
     foodStock = jo.optInt("foodStock");
     Log.showDialogAndRecordLog("已领取［"+jo.optInt("haveAddFoodStock")+"克］饲料","");
    }
   }
  }catch(Exception e)
  {
   Log.i(TAG, "receiveFarmTaskAward err: "+e.getMessage());
  }
 }
 
 private static void feedAnimal(ClassLoader loader, String farmId)
 {
  try
  {
   Object o = rpcCall_FeedAnimal(loader, farmId);
   String s = RpcCall.getResponse(o);
   JSONObject jo = new JSONObject(s);
   int feedFood = foodStock - jo.optInt("foodStock");
   foodStock -= feedFood;
   Log.showDialogAndRecordLog("喂了小鸡［"+feedFood+"克］饲料","");
  }catch(Exception e)
  {
   Log.i(TAG, "feedAnimal err: "+e.getMessage());
  }
 }
 
 private static void useAccelerateTool(ClassLoader loader)
 {
  try
  {
   Object o = rpcCall_ListFarmTool(loader);
   String s = RpcCall.getResponse(o);
   JSONObject jo = new JSONObject(s);
   JSONArray jaToolList = jo.optJSONArray("toolList");
   for(int i = 0; i < jaToolList.length(); i++)
   {
    jo = jaToolList.optJSONObject(i);
    if(jo.optString("toolType").equals(ToolType.ACCELERATETOOL.name()))
    {
     if(jo.optInt("toolCount") > 0)
     {
      o = rpcCall_UseFarmTool(loader, ownerFarmId, jo.optString("toolId"), ToolType.ACCELERATETOOL.name());
      s = RpcCall.getResponse(o);
      if(s.contains("\"memo\":\"SUCCESS\""))
       Log.showDialogAndRecordLog("加速卡使用成功","");
     }else
     {
      Log.showDialogAndRecordLog("没有加速卡可用","");
     }
     break;
    }
   }
  }catch(Exception e)
  {
   Log.i(TAG, "useAccelerateCard err: "+e.getMessage());
  }
 }

 private static void helpFriend()
 {

 }

 private static void notifyFriend()
 {

 }

 private static void feedFriendAnimal()
 {

 }
 
 private static Object rpcCall_RecallAnimal(ClassLoader loader, String animalId, String currentFarmId, String masterFarmId)
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
   Log.i(TAG, "rpcCall_RecallAnimal err: "+e.getMessage());
  }
  return null;
 }
 
 private static Object rpcCall_SendBackAnimal(ClassLoader loader, String animalId, String currentFarmId, String masterFarmId)
 {
  /*
   [
   {
   "animalId":"20171230000402012088922385376474",
   "currentFarmId":"10181101172642012088022401920420",
   "masterFarmId":"10171230000402012088922385376474",
   "requestType":"NORMAL",
   "sceneCode":"ANTFARM",
   "sendType":"HIT",
   "source":"H5",
   "version":"1.0.1907111732.11"
   }
   ]
  */
  try
  {
   String args1 = "[{\"animalId\":\""+animalId+"\",\"currentFarmId\":\""
    +currentFarmId+"\",\"masterFarmId\":\""+masterFarmId+
    "\",\"requestType\":\"NORMAL\",\"sceneCode\":\"ANTFARM\",\"sendType\":\"HIT\",\"source\":\"H5\",\"version\":\""
    +version+"\"}]";
   return RpcCall.invoke(loader, "com.alipay.antfarm.sendBackAnimal", args1);
  }catch(Exception e)
  {
   Log.i(TAG, "rpcCall_SendBackAnimal err: "+e.getMessage());
  }
  return null;
 }
 
 private static Object rpcCall_ListFarmTask(ClassLoader loader)
 {
  try
  {
   String args1 = "[{\"requestType\":\"NORMAL\",\"sceneCode\":\"ANTFARM\",\"source\":\"H5\",\"version\":\""
    +version+"\"}]";
   return RpcCall.invoke(loader, "com.alipay.antfarm.listFarmTask", args1);
  }catch(Exception e)
  {
   Log.i(TAG, "rpcCall_ListFarmTask err: "+e.getMessage());
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
  }
  return null;
 }
 
 private static Object rpcCall_AnswerQuestion(ClassLoader loader, String quesId, int answer)
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
   Log.i(TAG, "rpcCall_AnswerQuestion err: "+e.getMessage());
  }
  return null;
 }
 
 private static Object rpcCall_ReceiveFarmTaskAward(ClassLoader loader, String taskId)
 {
  try
  {
   String args1 = "[{\"requestType\":\"NORMAL\",\"sceneCode\":\"ANTFARM\",\"source\":\"H5\",\"taskId\":\""
    +taskId+"\",\"version\":\""+version+"\"}]";
   return RpcCall.invoke(loader, "com.alipay.antfarm.listFarmTool", args1);
  }catch(Exception e)
  {
   Log.i(TAG, "rpcCall_ReceiveFarmTaskAward err: "+e.getMessage());
  }
  return null;
 }
 
 private static Object rpcCall_FeedAnimal(ClassLoader loader, String farmId)
 {
  try
  {
   String args1 = "[{\"animalType\":\"CHICK\",\"canMock\":true,\"farmId\":\""+farmId+
   "\",\"requestType\":\"NORMAL\",\"sceneCode\":\"ANTFARM\",\"source\":\"H5\",\"version\":\""
    +version+"\"}]";
   return RpcCall.invoke(loader, "com.alipay.antfarm.feedAnimal", args1);
  }catch(Exception e)
  {
   Log.i(TAG, "rpcCall_FeedAnimal err: "+e.getMessage());
  }
  return null;
 }
 
 private static Object rpcCall_ListFarmTool(ClassLoader loader)
 {
  try
  {
   String args1 = "[{\"requestType\":\"NORMAL\",\"sceneCode\":\"ANTFARM\",\"source\":\"H5\",\"version\":\""
   +version+"\"}]";
   return RpcCall.invoke(loader, "com.alipay.antfarm.listFarmTool", args1);
  }catch(Exception e)
  {
   Log.i(TAG, "rpcCall_ListFarmTool err: "+e.getMessage());
  }
  return null;
 }
 
 private static Object rpcCall_UseFarmTool(ClassLoader loader, String targetFarmId, String toolId, String toolType)
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
   Log.i(TAG, "rpcCall_UseFarmTool err: "+e.getMessage());
  }
  return null;
 }
  
 private static void rpcCall_RankingList(ClassLoader loader)
 {
  /*
   [{"pageSize":20,"requestType":"NORMAL","sceneCode":"ANTFARM","source":"H5","startNum":0,"version":"1.0.1907111732.11"}]
  */
  try
  {
   String args1 = "[{\"pageSize\":20,\"requestType\":\"NORMAL\",\"sceneCode\":\"ANTFARM\",\"source\":\"H5\",\"startNum\":"
   +pageStartSum+",\"version\":\""+version+"\"}]";
   pageStartSum += 20;
   
   RpcCall.invoke(loader, "com.alipay.antfarm.rankingList", args1);
   
  }catch(Exception e)
  {
   Log.i(TAG, "rpcCall_RankingList err: "+e.getMessage());
  }
 }
 
 private static boolean parseFrienRankPageDataResponse(String resp)
 {
  boolean hasNext = false;
  try
  {
   JSONObject jo = new JSONObject(resp);
   hasNext = jo.optBoolean("hasNext");
   JSONArray jaRankingList = jo.optJSONArray("rankingList");
   if(jaRankingList == null)
    return true;
   for(int i = 0; i < jaRankingList.length(); i++)
   {
    jo = jaRankingList.optJSONObject(i);
    String userId = jo.optString("userId");
    boolean b = jo.optBoolean("stealingAnimal") || jo.has("actionType") &&
     jo.optString("actionType").equals("starve_action");
    if(b && !firendIdList.contains(userId))
    {
     firendIdList.add(userId);
    }
   }
   if(jaRankingList.length() == 0)
    return false;
  }catch(Exception e)
  {
   Log.i(TAG, "parseFrienRankPageDataResponse err: " + e.getMessage());
  }
  return hasNext;
 }
 
 private static String farmId2UserId(String farmId)
 {
  int l = farmId.length() / 2;
  return farmId.substring(l);
 }
 
}
