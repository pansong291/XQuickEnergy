package pansong291.xposed.quickenergy.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class CooperationIdMap
{
 private static final String TAG = CooperationIdMap.class.getCanonicalName();

 public static boolean shouldReload = false;

 private static Map idMap;
 private static boolean hasChanged = false;

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

 public static void clearIdMap()
 {
  getIdMap().clear();
  hasChanged = true;
 }

 public static boolean saveIdMap()
 {
  if(hasChanged)
  {
   StringBuilder sb = new StringBuilder();
   Set idSet = getIdMap().entrySet();
   for(Object entry: idSet)
   {
    sb.append(((Map.Entry) entry).getKey());
    sb.append(':');
    sb.append(((Map.Entry) entry).getValue());
    sb.append('\n');
   }
   hasChanged = !FileUtils.write2File(sb.toString(), FileUtils.getCooperationIdMapFile());
  }
  return hasChanged;
 }

 public static String getNameById(String id)
 {
  if(id == null || id.isEmpty()) return id;
  if(getIdMap().containsKey(id))
  {
   id = getIdMap().get(id).toString();
  }
  return id;
 }

 public static Map getIdMap()
 {
  if(idMap == null || shouldReload)
  {
   shouldReload = false;
   idMap = new TreeMap<>();
   String str = FileUtils.readFromFile(FileUtils.getCooperationIdMapFile());
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
