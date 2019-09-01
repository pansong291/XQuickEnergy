package pansong291.xposed.quickenergy;

import org.json.JSONObject;
import org.json.JSONArray;

public class AntMember
{
 public static final String TAG = AntMember.class.getCanonicalName();
  
 public static void receivePoint(ClassLoader loader, String args0, String args1, String response)
 {
  memberSignin(loader, args0, response);
  if(!"alipay.antmember.biz.rpc.member.h5.queryPointCert".equals(args0))
   return;
  
  try
  {
   AntForest.checkUnknownId(loader);
   JSONObject jo = new JSONArray(args1).getJSONObject(0);
   int page = jo.getInt("page");
   int pageSize = jo.getInt("pageSize");
   jo = new JSONObject(response);
   if(jo.getString("resultCode").equals("SUCCESS"))
   {
    Log.showDialogAndRecordLog("开始领取第"+page+"页的蚂蚁会员积分…","");
    String s;
    boolean hasNextPage = jo.getBoolean("hasNextPage");
    JSONArray jaCertList = jo.getJSONArray("certList");
    if(jaCertList.length() < 1)
     Log.showDialogAndRecordLog("暂时没有可领取的积分","");
    for(int i = 0; i < jaCertList.length(); i++)
    {
     jo = jaCertList.getJSONObject(i);
     String bizTitle = jo.getString("bizTitle");
     String id = jo.getString("id");
     int pointAmount = jo.getInt("pointAmount");
     s = rpcCall_receivePointByUser(loader,id);
     jo = new JSONObject(s);
     if(jo.getString("resultCode").equals("SUCCESS"))
     {
      Log.showDialogAndRecordLog("领取〔"+bizTitle+"〕〔"+pointAmount+"积分〕","");
     }else
     {
      Log.showDialogAndRecordLog(jo.getString("resultDesc"),s);
     }
    }
    if(hasNextPage)
    {
     rpcCall_queryPointCert(loader, page + 1, pageSize);
    }else
    {
     s = rpcCall_queryPoint(loader);
     jo = new JSONObject(s);
     if(jo.getString("resultCode").equals("SUCCESS"))
     {
      Log.showDialogAndRecordLog("剩余〔"+jo.getString("pointAvailableAmount")+"积分〕","");
     }else
     {
      Log.showDialogAndRecordLog(jo.getString("resultDesc"),s);
     }
    }
   }else
   {
    Log.showDialogAndRecordLog(jo.getString("resultDesc"),response);
   }
  }catch(Throwable t)
  {
   Log.i(TAG, "receivePoint err:");
   Log.printStackTrace(TAG, t);
  }
 }
 
 public static void memberSignin(ClassLoader loader, String args0, String response)
 {
  if(!"alipay.antmember.biz.rpc.member.h5.queryMemberSigninIndex".equals(args0))
   return;
  try
  {
   JSONObject jo = new JSONObject(response);
   if(jo.getString("resultCode").equals("SUCCESS"))
   {
    if(!jo.getBoolean("currentSigninStatus"))
    {
     String s = rpcCall_memberSignin(loader);
     jo = new JSONObject(s);
     if(jo.getString("resultCode").equals("SUCCESS"))
     {
      Log.showDialogAndRecordLog("领取〔每日签到〕〔"+
       jo.getString("signinPoint")+"积分〕，已签到〔"+
       jo.getString("signinSumDay")+"天〕","");
     }else
     {
      Log.showDialogAndRecordLog(jo.getString("resultDesc"),s);
     }
    }
   }else
   {
    Log.showDialogAndRecordLog(jo.getString("resultDesc"),response);
   }
  }catch(Throwable t)
  {
   Log.i(TAG, "memberSignin err:");
   Log.printStackTrace(TAG, t);
  }
 }
 
 private static String rpcCall_queryPointCert(ClassLoader loader, int page, int pageSize)
 {
  try
  {
   String args1 = "[{\"page\":"+page+",\"pageSize\":"+pageSize+"}]";
   Object o = RpcCall.invoke(loader, "alipay.antmember.biz.rpc.member.h5.queryPointCert", args1);
   return RpcCall.getResponse(o);
  }catch(Throwable t)
  {
   Log.i(TAG, "rpcCall_queryPointCert err:");
   Log.printStackTrace(TAG, t);
  }
  return null;
 }
 
 private static String rpcCall_receivePointByUser(ClassLoader loader, String certId)
 {
  try
  {
   String args1 = "[{\"certId\":"+certId+"}]";
   Object o = RpcCall.invoke(loader, "alipay.antmember.biz.rpc.member.h5.receivePointByUser", args1);
   return RpcCall.getResponse(o);
  }catch(Throwable t)
  {
   Log.i(TAG, "rpcCall_receivePointByUser err:");
   Log.printStackTrace(TAG, t);
  }
  return null;
 }
 
 private static String rpcCall_queryPoint(ClassLoader loader)
 {
  try
  {
   String args1 = "[{}]";
   Object o = RpcCall.invoke(loader, "alipay.antmember.h5.queryPoint", args1);
   return RpcCall.getResponse(o);
  }catch(Throwable t)
  {
   Log.i(TAG, "rpcCall_queryPoint err:");
   Log.printStackTrace(TAG, t);
  }
  return null;
 }
 
 private static String rpcCall_memberSignin(ClassLoader loader)
 {
  try
  {
   String args1 = "[{}]";
   Object o = RpcCall.invoke(loader, "alipay.antmember.biz.rpc.member.h5.memberSignin", args1);
   return RpcCall.getResponse(o);
  }catch(Throwable t)
  {
   Log.i(TAG, "rpcCall_memberSignin err:");
   Log.printStackTrace(TAG, t);
  }
  return null;
 }
 
}
