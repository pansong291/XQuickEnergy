package pansong291.xposed.quickenergy.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import pansong291.xposed.quickenergy.util.CooperationIdMap;

public class AlipayCooperate extends AlipayId
{
 private static List<AlipayCooperate> list;

 public AlipayCooperate(String i, String n)
 {
  id = i;
  name = n;
 }

 public static List<AlipayCooperate> getList()
 {
  if(list == null || CooperationIdMap.shouldReload)
  {
   list = new ArrayList<AlipayCooperate>();
   Set idSet = CooperationIdMap.getIdMap().entrySet();
   for(Object entry: idSet)
   {
    list.add(new AlipayCooperate(((Map.Entry) entry).getKey().toString(), ((Map.Entry) entry).getValue().toString()));
   }
  }
  return list;
 }

 public static void remove(String id)
 {
  getList();
  for(int i = 0; i < list.size(); i++)
  {
   if(list.get(i).id.equals(id))
   {
    list.remove(i);
    break;
   }
  }
 }

}
