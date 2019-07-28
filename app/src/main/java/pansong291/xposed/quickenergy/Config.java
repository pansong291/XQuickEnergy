package pansong291.xposed.quickenergy;

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
 public enum ShowMode
 {
  DIALOG, TOAST
 }
 
 private static final String TAG = Config.class.getCanonicalName();
 private static final String 
 jn_immediateEffect = "immediateEffect", jn_showMode = "showMode", jn_recordLog = "recordLog",
 jn_enableForest = "enableForest", jn_enableFarm = "enableFarm",
 jn_helpFriend = "helpFriend", jn_dontCollectList = "dontCollectList", jn_dontHelpList = "dontHelpList",
 jn_sendType = "sendType", jn_sendTypeExcludeList = "sendTypeExcludeList", jn_feedFriendAnimalList = "feedFriendAnimalList";
 
 public static boolean shouldReload;
 
 /* application */
 private boolean immediateEffect;
 private ShowMode showMode;
 private boolean recordLog;
 private boolean enableForest;
 private boolean enableFarm;
 
 /* forest */
 private boolean helpFriend;
 private List<String> dontCollectList;
 private List<String> dontHelpList;
 
 /* farm */
 private SendType sendType;
 private List<String> sendTypeExcludeList;
 private List<String> feedFriendAnimalList;
 
 /* other */
 private boolean defInit;
 private static Config config;
 private static Map idMap;
 private static boolean hasIdMapChanged = false;
 private static String selfId;

 /* application */
 public static ShowMode showMode()
 {
  return getConfig().showMode;
 }

 public static boolean recordLog()
 {
  return getConfig().recordLog;
 }
 
 public static boolean enableForest()
 {
  return getConfig().enableForest;
 }
 
 public static boolean enableFarm()
 {
  return getConfig().enableFarm;
 }
 
 /* forest */
 public static boolean helpFriend()
 {
  return getConfig().helpFriend;
 }

 public static boolean dontCollect(String id)
 {
  return getConfig().dontCollectList.contains(id);
 }
 
 public static boolean dontHelp(String id)
 {
  return getConfig().dontHelpList.contains(id);
 }
 
 /* farm */ 
 public static SendType sendType(String id)
 {
  if(getConfig().sendTypeExcludeList.contains(id))
  {
   return getConfig().sendType.another();
  }
  return getConfig().sendType;
 }
 
 public static boolean feedFriendAnimal(String id)
 {
  return getConfig().feedFriendAnimalList.contains(id);
 }
 
 /* other */
 private static Config getConfig()
 {
  if(config == null || config.immediateEffect && shouldReload)
  {
   shouldReload = false;
   if(FileUtils.getConfigFile().exists())
    config = json2Config(FileUtils.readFromFile(FileUtils.getConfigFile()));
   else
    config = json2Config(null);
   if(config.defInit)
    FileUtils.write2File(Config.config2Json(config), FileUtils.getConfigFile());
  }
  return config;
 }
  
 public static void putIdMap(String key, String value)
 {
  if(getIdMap().containsKey(key))
  {
   if(!getIdMap().get(key).equals(value))
   {
    getIdMap().replace(key, value);
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
  if(getIdMap().containsKey(id))
  {
   String n = getIdMap().get(id).toString();
   int ind = n.indexOf('(');
   if(ind > 0) n = n.substring(0, ind);
   return n;
  }
  return id;
 }
 
 private static Map getIdMap()
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
    }catch(Exception e)
    {
     Log.printStackTrace(TAG, e);
     idMap.clear();
    }
   }
  }
  return idMap;
 }
 
 public static Config defInit()
 {
  Config c = new Config();
  c.defInit = true;
  
  c.immediateEffect = true;
  c.showMode = ShowMode.DIALOG;
  c.recordLog = true;
  c.enableForest = true;
  c.enableFarm = true;
  
  c.helpFriend = true;
  c.dontCollectList = new ArrayList<>();
  c.dontHelpList = new ArrayList<>();
  
  c.sendType = SendType.HIT;
  c.sendTypeExcludeList = new ArrayList<>();
  c.feedFriendAnimalList = new ArrayList<>();
  return c;
 }
 
 public static Config json2Config(String json)
 {
  Config config = null;
  try
  {
   JSONObject jo = new JSONObject(json);
   config = new Config();

   config.immediateEffect = jo.getBoolean(jn_immediateEffect);
   Log.i(TAG, jn_immediateEffect + ":" + config.immediateEffect);
   
   config.showMode = ShowMode.valueOf(jo.getString(jn_showMode));
   Log.i(TAG, jn_showMode + ":" + config.showMode.name());

   config.recordLog = jo.getBoolean(jn_recordLog);
   Log.i(TAG, jn_recordLog + ":" + config.recordLog);
   
   config.enableForest = jo.getBoolean(jn_enableForest);
   Log.i(TAG, jn_enableForest + ":" + config.enableForest);
   
   config.enableFarm = jo.getBoolean(jn_enableFarm);
   Log.i(TAG, jn_enableFarm + ":" + config.enableFarm);
   
   config.helpFriend = jo.getBoolean(jn_helpFriend);
   Log.i(TAG, jn_helpFriend + ":" + config.helpFriend);

   JSONArray ja = jo.getJSONArray(jn_dontCollectList);
   config.dontCollectList = new ArrayList<>();
   Log.i(TAG, jn_dontCollectList + ":[");
   for(int i = 0; i < ja.length(); i++)
   {
    config.dontCollectList.add(ja.getString(i));
    Log.i(TAG, config.dontCollectList.get(i)+",");
   }

   ja = jo.getJSONArray(jn_dontHelpList);
   config.dontHelpList = new ArrayList<>();
   Log.i(TAG, jn_dontHelpList + ":[");
   for(int i = 0; i < ja.length(); i++)
   {
    config.dontHelpList.add(ja.getString(i));
    Log.i(TAG, config.dontHelpList.get(i)+",");
   }
   
   config.sendType = SendType.valueOf(jo.getString(jn_sendType));
   Log.i(TAG, jn_sendType + ":" + config.sendType.name());
   
   ja = jo.getJSONArray(jn_sendTypeExcludeList);
   config.sendTypeExcludeList = new ArrayList<>();
   Log.i(TAG, jn_sendTypeExcludeList + ":[");
   for(int i = 0; i < ja.length(); i++)
   {
    config.sendTypeExcludeList.add(ja.getString(i));
    Log.i(TAG, config.sendTypeExcludeList.get(i)+",");
   }
   
   ja = jo.getJSONArray(jn_feedFriendAnimalList);
   config.feedFriendAnimalList = new ArrayList<>();
   Log.i(TAG, jn_feedFriendAnimalList + ":[");
   for(int i = 0; i < ja.length(); i++)
   {
    config.feedFriendAnimalList.add(ja.getString(i));
    Log.i(TAG, config.feedFriendAnimalList.get(i)+",");
   }
   
   String formated = replaceIndentSpaces2Tables(jo.toString(1));
   if(!formated.equals(json))
    FileUtils.write2File(formated, FileUtils.getConfigFile());
  }catch(Exception e)
  {
   Log.printStackTrace(TAG, e);
   config = Config.defInit();
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

   jo.put(jn_showMode, config.showMode.name());

   jo.put(jn_recordLog, config.recordLog);
   
   jo.put(jn_enableForest, config.enableForest);
   
   jo.put(jn_enableFarm, config.enableFarm);
   
   jo.put(jn_helpFriend, config.helpFriend);

   JSONArray ja = new JSONArray();
   for(String s: config.dontCollectList)
   {
    ja.put(s);
   }
   jo.put(jn_dontCollectList, ja);

   ja = new JSONArray();
   for(String s: config.dontHelpList)
   {
    ja.put(s);
   }
   jo.put(jn_dontHelpList, ja);
   
   jo.put(jn_sendType, config.sendType.name());
   
   ja = new JSONArray();
   for(String s: config.sendTypeExcludeList)
   {
    ja.put(s);
   }
   jo.put(jn_sendTypeExcludeList, ja);
   
   ja = new JSONArray();
   for(String s: config.feedFriendAnimalList)
   {
    ja.put(s);
   }
   jo.put(jn_feedFriendAnimalList, ja);
   
   return replaceIndentSpaces2Tables(jo.toString(1));
  }catch(Exception e)
  {
   Log.printStackTrace(TAG, e);
  }
  return jo.toString();
 }
 
 public static String replaceIndentSpaces2Tables(String s)
 {
  StringBuilder sb = new StringBuilder(s);
  char currentChar, lastNonSpaceChar = 0;
  for(int i = 0; i < sb.length(); i++)
  {
   currentChar = sb.charAt(i);
   
   if(currentChar == ' ')
   {
    if(lastNonSpaceChar == '\n')
     sb.setCharAt(i, '\t');
   }else
   {
    lastNonSpaceChar = currentChar;
   }
  }
  return sb.toString();
 }
 
}
