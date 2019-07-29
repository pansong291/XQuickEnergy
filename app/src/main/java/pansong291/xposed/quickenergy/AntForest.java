package pansong291.xposed.quickenergy;

import android.app.Activity;
import android.text.TextUtils;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

public class AntForest
{
 private static final String TAG = AntForest.class.getCanonicalName();
 private static ArrayList<String> friendsRankUseridList = new ArrayList<String>();
 private static Integer collectedEnergy = 0;
 private static Integer helpCollectedEnergy = 0;
 private static Integer pageCount = 0;


 /**
  * 自动获取有能量的好友信息
  *
  * @param loader
  * @param response
  */
 public static void autoGetCanCollectUserIdList(final ClassLoader loader, String response)
 {
  // 开始解析好友信息，循环把所有有能量的好友信息都解析完
  boolean hasMore = parseFrienRankPageDataResponse(response);
  if (hasMore)
  {
   Log.showDialog("开始获取可以收取能量的好友信息...", "");
   new Thread(new Runnable() {
     public void run()
     {
      // 发送获取下一页好友信息接口
      rpcCall_friendRankList(loader);
     }
    }).start();
  } else
  {
   pageCount = 0;
   Log.i(TAG, "friendsRankUseridList " + friendsRankUseridList);
   //如果发现已经解析完成了，如果有好友能量收取，就开始收取
   if (friendsRankUseridList.size() > 0)
   {
    Log.showDialogOrToast("开始获取每个好友能够收取的能量信息...", "");
    for (String userId : friendsRankUseridList)
    {
     // 开始收取每个用户的能量
     rpcCall_canCollectEnergy(loader, userId);
    }
    Log.showDialogOrToast("共偷取能量【" + collectedEnergy + "克】，共帮收能量【" + helpCollectedEnergy + "克】\n", "");
    Log.i(TAG, "能量收取结束");
    friendsRankUseridList.clear();
    collectedEnergy = 0;
    if(helpCollectedEnergy != 0)
    {
     helpCollectedEnergy = 0;
     autoGetCanCollectUserIdList(loader, null);
    }
    Config.saveIdMap();
   }else
   {
    Log.showDialogOrToast("暂时没有可收取的能量\n", "");
   }
   // 执行完了调用刷新页面，看看总能量效果
  }
 }

 /**
  * 自动获取能收取的能量ID
  *
  * @param loader
  * @param response
  */
 public static void autoGetCanCollectBubbleIdList(final ClassLoader loader, String response)
 {
  if (!TextUtils.isEmpty(response) && response.contains("collectStatus"))
  {
   try
   {
    JSONObject jsonObject = new JSONObject(response);
    JSONArray jsonArray = jsonObject.getJSONArray("bubbles");
    jsonObject = jsonObject.getJSONObject("userEnergy");
    String userName = jsonObject.getString("displayName");
    String loginId = userName;
    if(jsonObject.has("loginId"))
     loginId += "(" + jsonObject.getString("loginId") + ")";
    if (jsonArray != null && jsonArray.length() > 0)
    {
     for (int i = 0; i < jsonArray.length(); i++)
     {
      JSONObject jsonObject1 = jsonArray.getJSONObject(i);
      String userId = jsonObject1.getString("userId");
      long bubbleId = jsonObject1.getLong("id");
      Config.putIdMap(userId, loginId);
      if ("AVAILABLE".equals(jsonObject1.getString("collectStatus")))
      {
       if(Config.dontCollect(userId))
        Log.showDialog("不偷取【" + userName + "】", ", userId=" + userId);
       else
        rpcCall_collectEnergy(loader, userId, bubbleId, userName);
      }
      if (jsonObject1.getBoolean("canHelpCollect"))
      {
       if(Config.helpFriend())
       {
         if(Config.dontHelp(userId))
          Log.showDialog("不帮收【" + userName + "】", ", userId=" + userId);
         else
          rpcCall_forFriendCollectEnergy(loader, userId, bubbleId, userName);
       }else
        Log.showDialog("不帮收【" + userName + "】", ", userId=" + userId);
      }
     }
    }

   }catch(Exception e)
   {
    Log.printStackTrace(TAG, e);
   }
  }
 }

 public static boolean isRankList(String response)
 {
  return !TextUtils.isEmpty(response) && response.contains("friendRanking");
 }

 public static boolean isUserDetail(String response)
 {
  return !TextUtils.isEmpty(response) && response.contains("userEnergy");
 }

 /**
  * 解析好友信息
  *
  * @param response
  * @return
  */
 private static boolean parseFrienRankPageDataResponse(String response)
 {
  try
  {
   JSONObject jo = new JSONObject(response);
   JSONArray optJSONArray = jo.getJSONArray("friendRanking");
   if (optJSONArray != null)
   {
    for (int i = 0; i < optJSONArray.length(); i++)
    {
     JSONObject jsonObject = optJSONArray.getJSONObject(i);
     boolean optBoolean = jsonObject.getBoolean("canCollectEnergy")
      || jsonObject.getBoolean("canHelpCollect");
     String userId = jsonObject.getString("userId");
     if (optBoolean && !friendsRankUseridList.contains(userId))
     {
      friendsRankUseridList.add(userId);
     }
    }
    if(optJSONArray.length() == 0)
     return false;
    return jo.getBoolean("hasMore");
   }
  }catch(Exception e)
  {
   Log.i(TAG, "parseFrienRankPageDataResponse err:");
   Log.printStackTrace(TAG, e);
  }
  return true;
 }

 /**
  * 获取分页好友信息命令
  *
  * @param loader
  */
 private static void rpcCall_friendRankList(final ClassLoader loader)
 {
  try
  {
   JSONArray jsonArray = new JSONArray();
   JSONObject json = new JSONObject();
   json.put("av", "5");
   json.put("ct", "android");
   json.put("pageSize", 20); // pageCount * 20);
   json.put("startPoint", String.valueOf(pageCount * 20 + 1));
   pageCount++;
   jsonArray.put(json);
   Log.i(TAG, "call friendranklist params:" + jsonArray);

   RpcCall.invoke(loader, "alipay.antmember.forest.h5.queryEnergyRanking", jsonArray.toString());

  }catch(Exception e)
  {
   Log.i(TAG, "rpcCall_friendRankList err:");
   Log.printStackTrace(TAG, e);
  }
 }

 /**
  * 获取指定用户可以收取的能量信息
  *
  * @param loader
  * @param userId
  */
 private static void rpcCall_canCollectEnergy(final ClassLoader loader, String userId)
 {
  try
  {
   JSONArray jsonArray = new JSONArray();
   JSONObject json = new JSONObject();
   json.put("av", "5");
   json.put("ct", "android");
   json.put("pageSize", 3);
   json.put("startIndex", 0);
   json.put("userId", userId);
   jsonArray.put(json);
   Log.i(TAG, "call cancollect energy params:" + jsonArray);

   RpcCall.invoke(loader, "alipay.antmember.forest.h5.queryNextAction", jsonArray.toString());

   RpcCall.invoke(loader, "alipay.antmember.forest.h5.pageQueryDynamics", jsonArray.toString());

  }catch(Exception e)
  {
   Log.i(TAG, "rpcCall_canCollectEnergy err:");
   Log.printStackTrace(TAG, e);
  }
 }

 /**
  * 收取能量命令
  *
  * @param loader
  * @param userId
  * @param bubbleId
  */
 private static void rpcCall_collectEnergy(final ClassLoader loader, String userId, Long bubbleId, String userName)
 {
  try
  {
   JSONArray jsonArray = new JSONArray();
   JSONArray bubbleAry = new JSONArray();
   bubbleAry.put(bubbleId);
   JSONObject json = new JSONObject();
   //json.put("av", "5");
   //json.put("ct", "android");
   json.put("userId", userId);
   json.put("bubbleIds", bubbleAry);
   jsonArray.put(json);
   Log.i(TAG, "call collect energy params:" + jsonArray);

   Object resp = RpcCall.invoke(loader, "alipay.antmember.forest.h5.collectEnergy", jsonArray.toString());
   String response = RpcCall.getResponse(resp);
   int collect = parseCollectEnergyResponse(response, false);
   if(collect > 0)
   {
    Log.showDialogAndRecordLog("偷取【" + userName + "】的能量【" + collect + "克】", "，UserID：" + userId + "，BubbleId：" + bubbleId);
   }else
   {
    Log.showDialogAndRecordLog("偷取【" + userName + "】的能量失败", "，UserID：" + userId + "，BubbleId：" + bubbleId);
   }
  }catch(Exception e)
  {
   Log.i(TAG, "rpcCall_collectEnergy err:");
   Log.printStackTrace(TAG, e);
  }
 }
 
 /**
  * 帮好友收取能量命令
  *
  * @param loader
  * @param userId
  * @param bubbleId
  */
 private static void rpcCall_forFriendCollectEnergy(ClassLoader loader, String targetUserId, Long bubbleId, String userName)
 {
  try
  {
   JSONArray jsonArray = new JSONArray();
   JSONArray bubbleAry = new JSONArray();
   bubbleAry.put(bubbleId);
   JSONObject json = new JSONObject();
   json.put("bubbleIds", bubbleAry);
   json.put("targetUserId", targetUserId);
   jsonArray.put(json);
   Log.i(TAG, "call help collect energy params:" + jsonArray);
   Object resp = RpcCall.invoke(loader, "alipay.antmember.forest.h5.forFriendCollectEnergy", jsonArray.toString());
   String response = RpcCall.getResponse(resp);
   int helped = parseCollectEnergyResponse(response, true);
   if (helped > 0)
   {
    Log.showDialogAndRecordLog("帮【" + userName + "】收取【" + helped + "克】", "，UserID：" + targetUserId + "，BubbleId：" + bubbleId);
   }else
   {
    Log.showDialogAndRecordLog("帮【" + userName + "】收取失败", "，UserID：" + targetUserId + "，BubbleId" + bubbleId);
   }
  }catch(Exception e)
  {
   Log.i(TAG, "rpcCall_forFriendCollectEnergy err:");
   Log.printStackTrace(TAG, e);
  }

 }
 
 private static int parseCollectEnergyResponse(String response, boolean isForFriend)
 {
  if(!TextUtils.isEmpty(response) && response.contains("failedBubbleIds"))
  {
   try
   {
    int count = 0;
    JSONObject jsonObject = new JSONObject(response);
    JSONArray jsonArray = jsonObject.getJSONArray("bubbles");
    for(int i = 0; i < jsonArray.length(); i++)
     count += jsonArray.getJSONObject(i).getInt("collectedEnergy");
    if(isForFriend)
    {
     helpCollectedEnergy += count;
    }else
    {
     collectedEnergy += count;
    }
    if("SUCCESS".equals(jsonObject.getString("resultCode")))
    {
     return count;
    }
   }catch(Exception e)
   {
    Log.i(TAG, "parseCollectEnergyResponse err:");
    Log.printStackTrace(TAG, e);
   }
  }
  return -1;
 }

 
 
}
