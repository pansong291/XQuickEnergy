package pansong291.xposed.quickenergy;

import android.app.Activity;
import android.app.AlertDialog;
import android.text.TextUtils;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

public class AliMobileAutoCollectEnergyUtils
{

 private static String TAG = "AliMobileAutoCollectEnergyUtils";
 private static ArrayList<String> friendsRankUseridList = new ArrayList<String>();
 private static boolean isWebViewRefresh;
 private static Integer collectedEnergy = 0;
 private static Integer helpCollectedEnergy = 0;
 private static Integer pageCount = 0;
 private static Object curH5PageImpl;
 public static Object curH5Fragment;
 public static Activity h5Activity;
 private static AlertDialog dlg;
 private static StringBuffer sb;


 /**
  * 自动获取有能量的好友信息
  *
  * @param loader
  * @param response
  */
 public static void autoGetCanCollectUserIdList(final ClassLoader loader, String response)
 {
  /**
  if (isWebViewRefresh)
  {
   // 如果已经刷新了，这里又回来response了，就表示这里是我们刷新webview结束来到的逻辑
   finishWork();
   return;
  }/**/
  // 开始解析好友信息，循环把所有有能量的好友信息都解析完
  boolean hasMore = parseFrienRankPageDataResponse(response);
  if (hasMore)
  {
   showDialog("开始获取可以收取能量的好友信息...", "");
   new Thread(new Runnable() {
     public void run()
     {
      // 发送获取下一页好友信息接口
      rpcCall_FriendRankList(loader);
     }
    }).start();
  } else
  {
   pageCount = 0;
   Log.i(TAG, "friendsRankUseridList " + friendsRankUseridList);
   //如果发现已经解析完成了，如果有好友能量收取，就开始收取
   if (friendsRankUseridList.size() > 0)
   {
    showDialog("开始获取每个好友能够收取的能量信息...", "");
    for (String userId : friendsRankUseridList)
    {
     // 开始收取每个用户的能量
     rpcCall_CanCollectEnergy(loader, userId);
    }
    showDialog("共偷取能量【" + collectedEnergy + "克】，共帮收能量【" + helpCollectedEnergy + "克】\n", "");
    Log.i(TAG, "能量收取结束");
    friendsRankUseridList.clear();
    collectedEnergy = 0;
    if(helpCollectedEnergy != 0)
    {
     helpCollectedEnergy = 0;
     autoGetCanCollectUserIdList(loader, null);
    }
   }else
   {
    showDialog("暂时没有可收取的能量\n", "");
   }
   // 执行完了调用刷新页面，看看总能量效果
   // refreshWebView();
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
    JSONArray jsonArray = jsonObject.optJSONArray("bubbles");
    String userName = jsonObject.getJSONObject("userEnergy").getString("displayName");
    if (jsonArray != null && jsonArray.length() > 0)
    {
     for (int i = 0; i < jsonArray.length(); i++)
     {
      JSONObject jsonObject1 = jsonArray.getJSONObject(i);
      if ("AVAILABLE".equals(jsonObject1.optString("collectStatus")))
      {
       rpcCall_CollectEnergy(loader, jsonObject1.optString("userId"), jsonObject1.optLong("id"), userName);
      }
      if (jsonObject1.optBoolean("canHelpCollect"))
      {
       rpcCall_ForFriendCollectEnergy(loader, jsonObject1.optString("userId"), jsonObject1.optLong("id"), userName);
      }
     }
    }

   } catch (Exception e)
   {
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
  * 刷新页面
  */
 private static void refreshWebView()
 {
  showDialog("一共收取了" + collectedEnergy + "g能量", "");
  isWebViewRefresh = true;
 }

 /**
  * 结束工作
  */
 private static void finishWork()
 {
  isWebViewRefresh = false;
  // 打印收取了多少能量
  Log.i(TAG, "一共收取了" + collectedEnergy + "g能量");
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
   JSONArray optJSONArray = jo.optJSONArray("friendRanking");
   if (optJSONArray != null)
   {
    for (int i = 0; i < optJSONArray.length(); i++)
    {
     JSONObject jsonObject = optJSONArray.getJSONObject(i);
     boolean optBoolean = jsonObject.optBoolean("canCollectEnergy")
      || jsonObject.optBoolean("canHelpCollect");
     String userId = jsonObject.optString("userId");
     if (optBoolean && !friendsRankUseridList.contains(userId))
     {
      friendsRankUseridList.add(userId);
     }
    }
    if(optJSONArray.length() == 0)
     return false;
    return jo.optBoolean("hasMore");
   }
  } catch (Exception e)
  {
   Log.i(TAG, "parseFrienRankPageDataResponse err: " + e.getMessage());
  }
  return true;
 }

 /**
  * 获取分页好友信息命令
  *
  * @param loader
  */
 private static void rpcCall_FriendRankList(final ClassLoader loader)
 {
  try
  {
   Method rpcCallMethod = getRpcCallMethod(loader);
   JSONArray jsonArray = new JSONArray();
   JSONObject json = new JSONObject();
   json.put("av", "5");
   json.put("ct", "android");
   json.put("pageSize", 20); // pageCount * 20);
   json.put("startPoint", String.valueOf(pageCount * 20 + 1));
   pageCount++;
   jsonArray.put(json);
   Log.i(TAG, "call friendranklist params:" + jsonArray);

   rpcCallMethod.invoke(null, "alipay.antmember.forest.h5.queryEnergyRanking", jsonArray.toString(),
    "", true, null, null, false, curH5PageImpl, 0, "", false, -1);

  } catch (Exception e)
  {
   Log.i(TAG, "rpcCall_FriendRankList err: " + e.getMessage());
  }
 }

 /**
  * 获取指定用户可以收取的能量信息
  *
  * @param loader
  * @param userId
  */
 private static void rpcCall_CanCollectEnergy(final ClassLoader loader, String userId)
 {
  try
  {
   Method rpcCallMethod = getRpcCallMethod(loader);
   JSONArray jsonArray = new JSONArray();
   JSONObject json = new JSONObject();
   json.put("av", "5");
   json.put("ct", "android");
   json.put("pageSize", 3);
   json.put("startIndex", 0);
   json.put("userId", userId);
   jsonArray.put(json);
   Log.i(TAG, "call cancollect energy params:" + jsonArray);

   rpcCallMethod.invoke(null, "alipay.antmember.forest.h5.queryNextAction", jsonArray.toString(),
    "", true, null, null, false, curH5PageImpl, 0, "", false, -1);

   rpcCallMethod.invoke(null, "alipay.antmember.forest.h5.pageQueryDynamics", jsonArray.toString(),
    "", true, null, null, false, curH5PageImpl, 0, "", false, -1);

  } catch (Exception e)
  {
   Log.i(TAG, "rpcCall_CanCollectEnergy err: " + e.getMessage());
  }
 }

 /**
  * 收取能量命令
  *
  * @param loader
  * @param userId
  * @param bubbleId
  */
 private static void rpcCall_CollectEnergy(final ClassLoader loader, String userId, Long bubbleId, String userName)
 {
  try
  {
   Method rpcCallMethod = getRpcCallMethod(loader);
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

   Object resp = rpcCallMethod.invoke(null, "alipay.antmember.forest.h5.collectEnergy", jsonArray.toString(),
    "", true, null, null, false, curH5PageImpl, 0, "", false, -1);
   String response = (String)resp.getClass().getMethod("getResponse").invoke(resp);
   int collect = parseCollectEnergyResponse(response, false);
   if(collect > 0)
   {
    showDialog("偷取【" + userName + "】的能量【" + collect + "克】", "，UserID：" + userId + "，BubbleId：" + bubbleId);
   }else
   {
    Log.i(TAG, "偷取【" + userName + "】的能量失败，UserID：" + userId + "，BubbleId：" + bubbleId);
   }
  }catch(Exception e)
  {
   Log.i(TAG, "rpcCall_CollectEnergy err: " + e.getMessage());
  }
 }
 
 /**
  * 帮好友收取能量命令
  *
  * @param loader
  * @param userId
  * @param bubbleId
  */
 private static void rpcCall_ForFriendCollectEnergy(ClassLoader loader, String targetUserId, Long bubbleId, String userName)
 {
  try
  {
   Method rpcCallMethod = getRpcCallMethod(loader);
   JSONArray jsonArray = new JSONArray();
   JSONArray bubbleAry = new JSONArray();
   bubbleAry.put(bubbleId);
   JSONObject json = new JSONObject();
   json.put("bubbleIds", bubbleAry);
   json.put("targetUserId", targetUserId);
   jsonArray.put(json);
   Log.i(TAG, "call help collect energy params:" + jsonArray);
   Object resp = rpcCallMethod.invoke(null, "alipay.antmember.forest.h5.forFriendCollectEnergy", jsonArray.toString(),
    "", true, null, null, false, curH5PageImpl, 0, "", false, -1);
   String response = (String)resp.getClass().getMethod("getResponse").invoke(resp);
   int helped = parseCollectEnergyResponse(response, true);
   if (helped > 0)
   {
    showDialog("帮【" + userName + "】收取【" + helped + "克】", "，UserID：" + targetUserId + "，BubbleId：" + bubbleId);
   }else
   {
    Log.i(TAG, "帮【" + userName + "】收取失败，UserID：" + targetUserId + "，BubbleId" + bubbleId);
   }
  }catch(Exception e)
  {
   Log.i(TAG, "rpcCall_ForFriendCollectEnergy err: " + e.getMessage());
  }

 }
 
 private static Method getRpcCallMethod(ClassLoader loader)
 {
  try
  {
   Field aF = curH5Fragment.getClass().getDeclaredField("a");
   aF.setAccessible(true);
   Object viewHolder = aF.get(curH5Fragment);
   Field hF = viewHolder.getClass().getDeclaredField("h");
   hF.setAccessible(true);
   curH5PageImpl = hF.get(viewHolder);
   Class<?> h5PageClazz = loader.loadClass("com.alipay.mobile.h5container.api.H5Page");
   Class<?> jsonClazz = loader.loadClass("com.alibaba.fastjson.JSONObject");
   Class<?> rpcClazz = loader.loadClass("com.alipay.mobile.nebulabiz.rpc.H5RpcUtil");
   if (curH5PageImpl != null)
   {
    Method callM = rpcClazz.getMethod("rpcCall", String.class, String.class, String.class,
     boolean.class, jsonClazz, String.class, boolean.class, h5PageClazz,
     int.class, String.class, boolean.class, int.class);
    return callM;

   }
  } catch (Exception e)
  {
   Log.i(TAG, "getRpcCallMethod err: " + e.getMessage());
  }
  return null;
 }

 private static int parseCollectEnergyResponse(String response, boolean isForFriend)
 {
  if(!TextUtils.isEmpty(response) && response.contains("failedBubbleIds"))
  {
   try
   {
    int count = 0;
    JSONObject jsonObject = new JSONObject(response);
    JSONArray jsonArray = jsonObject.optJSONArray("bubbles");
    for(int i = 0; i < jsonArray.length(); i++)
     count += jsonArray.getJSONObject(i).optInt("collectedEnergy");
    if(isForFriend)
    {
     helpCollectedEnergy += count;
    }else
    {
     collectedEnergy += count;
    }
    if("SUCCESS".equals(jsonObject.optString("resultCode")))
    {
     return count;
    }
   }catch(Exception e)
   {
    Log.i(TAG, "parseCollectEnergyResponse err: " + e.getMessage());
   }
  }
  return -1;
 }

 private static void showDialog(final String str, String str2)
 {
  Log.i(TAG, str + str2);
  if(h5Activity != null)
  {
   try
   {
    h5Activity.runOnUiThread(new Runnable()
    {
      public void run()
      {
       if(sb == null)
        sb = new StringBuffer();
       if(dlg == null)
        dlg = createNewDialog();
       if(!dlg.isShowing())
        try{
         dlg.show();
        }catch(Exception e)
        {
         Log.i(TAG, "Dialog show error: "+e.getMessage());
         dlg = createNewDialog();
         dlg.show();
         sb.delete(0, sb.length());
        }
       sb.append(str).append('\n');
       dlg.setMessage(sb.toString());
      }
     });
   }catch(Exception e)
   {
    Log.i(TAG, "showDialog err: " + e.getMessage());
   }
  }
 }
 
 private static AlertDialog createNewDialog()
 {
  return new AlertDialog.Builder(h5Activity)
   .setTitle("XQuickEnergy")
   .setMessage("")
   .setCancelable(false)
   .setPositiveButton("OK", null)
   .create();
 }
 
}
