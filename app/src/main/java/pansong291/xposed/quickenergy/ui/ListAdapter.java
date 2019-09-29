package pansong291.xposed.quickenergy.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import java.util.List;
import pansong291.xposed.quickenergy.R;

public class ListAdapter extends BaseAdapter
{
 private static ListAdapter adapter;

 public static ListAdapter get(Context c)
 {
  if(adapter == null)
   adapter = new ListAdapter(c);
  return adapter;
 }

 Context context;
 List<AlipayUser>list;
 List<String> selects;
 int findIndex = -1;
 CharSequence findWord = null;

 private ListAdapter(Context c)
 {
  context = c;
 }

 public void setAlipayUserList(List<AlipayUser> l)
 {
  list = l;
 }

 public void setSelectedList(List<String> l)
 {
  selects = l;
 }

 public int findLast(CharSequence cs)
 {
  if(!cs.equals(findWord))
  {
   findIndex = -1;
   findWord = cs;
  }
  int i = findIndex;
  if(i < 0) i = list.size(); 
  for(;;)
  {
   i = (i + list.size() - 1) % list.size();
   if(list.get(i).name.contains(cs))
   {
    findIndex = i;
    break;
   }
   if(findIndex < 0 && i == 0)
    break;
  }
  notifyDataSetChanged();
  return findIndex;
 }

 public int findNext(CharSequence cs)
 {
  if(!cs.equals(findWord))
  {
   findIndex = -1;
   findWord = cs;
  }
  for(int i = findIndex;;)
  {
   i = (i + 1) % list.size();
   if(list.get(i).name.contains(cs))
   {
    findIndex = i;
    break;
   }
   if(findIndex < 0 && i == list.size() - 1)
    break;
  }
  notifyDataSetChanged();
  return findIndex;
 }

 public void exitFind()
 {
  findIndex = -1;
  notifyDataSetChanged();
 }

 @Override
 public int getCount()
 {
  return list.size();
 }

 @Override
 public Object getItem(int p1)
 {
  return list.get(p1);
 }

 @Override
 public long getItemId(int p1)
 {
  return p1;
 }

 @Override
 public View getView(int p1, View p2, ViewGroup p3)
 {
  ViewHolder vh;
  if(p2 == null)
  {
   vh = new ViewHolder();
   p2 = LayoutInflater.from(context).inflate(R.layout.list_item, null);
   vh.tv = p2.findViewById(R.id.tv_idn);
   vh.cb = p2.findViewById(R.id.cb_list);
   p2.setTag(vh);
  }else
  {
   vh = (ViewHolder)p2.getTag();
  }

  AlipayUser au = list.get(p1);
  vh.tv.setText(au.name);
  vh.tv.setTextColor(findIndex == p1 ? Color.RED: Color.BLACK);
  vh.cb.setChecked(selects == null ? false: selects.contains(au.id));
  return p2;
 }

 class ViewHolder
 {
  TextView tv;
  CheckBox cb;
 }

}
