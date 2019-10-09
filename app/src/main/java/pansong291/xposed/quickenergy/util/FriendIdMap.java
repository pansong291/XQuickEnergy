package pansong291.xposed.quickenergy.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class FriendIdMap
{
 private static final String TAG = FriendIdMap.class.getCanonicalName();

 public static boolean shouldReload = false;

 public static String currentUid;

 private static Map idMap;
 private static boolean hasChanged = false;
 private static String selfId;

 public static void putIdMap(String key, String value)
 {
  if(key == null || key.isEmpty()) return;
  if(getIdMap().containsKey(key))
  {
   if(!getIdMap().get(key).equals(value))
   {
    getIdMap().remove(key);
    getIdMap().put(key, value);
    hasChanged = true;
   }
  }else
  {
   getIdMap().put(key, value);
   hasChanged = true;
  }
 }

 public static void removeIdMap(String key)
 {
  if(key == null || key.isEmpty()) return;
  if(getIdMap().containsKey(key))
  {
   getIdMap().remove(key);
   hasChanged = true;
  }
 }

 public static boolean saveIdMap()
 {
  if(hasChanged)
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
   hasChanged = !FileUtils.write2File(sb.toString(), FileUtils.getFriendIdMapFile());
  }
  return hasChanged;
 }

 private static String getSelfId()
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
   int ind = n.lastIndexOf('(');
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
  if(idMap == null || shouldReload)
  {
   shouldReload = false;
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

}
