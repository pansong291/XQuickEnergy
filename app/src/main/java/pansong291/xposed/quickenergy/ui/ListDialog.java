package pansong291.xposed.quickenergy.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import java.util.List;
import pansong291.xposed.quickenergy.R;
import pansong291.xposed.quickenergy.util.Config;

public class ListDialog
{
 static AlertDialog listDialog;
 static Button btn_find_last, btn_find_next;
 static EditText edt_find;
 static ListView lv_list;
 static List<String> selectedList;
 static List<Integer> countList;
 static ListAdapter.ViewHolder curViewHolder;
 static AlipayUser curAlipayUser;

 static AlertDialog edtDialog;
 static EditText edt_count;

 static AlertDialog deleteDialog;

 public static void show(Context c, CharSequence title, List<String> l, List<Integer> lc)
 {
  selectedList = l;
  countList = lc;
  try
  {
   getListDialog(c).show();
  }catch(Throwable t)
  {
   listDialog = null;
   getListDialog(c).show();
  }
  listDialog.setTitle(title);
  ListAdapter la = ListAdapter.get(c);
  la.setSelectedList(selectedList);
  la.notifyDataSetChanged();
 }

 private static AlertDialog getListDialog(Context c)
 {
  if(listDialog == null)
   listDialog = new AlertDialog.Builder(c)
    .setTitle("title")
    .setView(getListView(c))
    .setPositiveButton("确定", null)
    .create();
  return listDialog;
 }

 private static View getListView(Context c)
 {
  View v = LayoutInflater.from(c).inflate(R.layout.dialog_list, null);
  OnBtnClickListener onBtnClickListener = new OnBtnClickListener();
  btn_find_last = v.findViewById(R.id.btn_find_last);
  btn_find_next = v.findViewById(R.id.btn_find_next);
  btn_find_last.setOnClickListener(onBtnClickListener);
  btn_find_next.setOnClickListener(onBtnClickListener);
  edt_find = v.findViewById(R.id.edt_find);
  lv_list = v.findViewById(R.id.lv_list);
  ListAdapter la = ListAdapter.get(c);
  la.setAlipayUserList(AlipayUser.getAlipayUserList());
  lv_list.setAdapter(la);
  lv_list.setOnItemClickListener(
   new OnItemClickListener()
   {
    @Override
    public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4)
    {
     curViewHolder = (ListAdapter.ViewHolder) p2.getTag();
     curAlipayUser = (AlipayUser) p1.getAdapter().getItem(p3);
     if(countList == null)
     {
      if(curViewHolder.cb.isChecked())
      {
       if(selectedList.contains(curAlipayUser.id))
        selectedList.remove(curAlipayUser.id);
       curViewHolder.cb.setChecked(false);
      }else
      {
       if(!selectedList.contains(curAlipayUser.id))
        selectedList.add(curAlipayUser.id);
       curViewHolder.cb.setChecked(true);
      }
      Config.hasConfigChanged = true;
     }else
     {
      try
      {
       getEdtDialog(p1.getContext()).show();
      }catch(Throwable t)
      {
       edtDialog = null;
       getEdtDialog(p1.getContext()).show();
      }
      edtDialog.setTitle(curAlipayUser.name);
      int i = selectedList.indexOf(curAlipayUser.id);
      if(i >= 0)
       edt_count.setText(String.valueOf(countList.get(i)));
      else
       edt_count.getText().clear();
     }
    }
   });
  lv_list.setOnItemLongClickListener(
   new OnItemLongClickListener()
   {
    @Override
    public boolean onItemLongClick(AdapterView<?> p1, View p2, int p3, long p4)
    {
     curAlipayUser = (AlipayUser) p1.getAdapter().getItem(p3);
     try
     {
      getDeleteDialog(p1.getContext()).show();
     }catch(Throwable t)
     {
      deleteDialog = null;
      getDeleteDialog(p1.getContext()).show();
     }
     deleteDialog.setMessage("删除用户 " + curAlipayUser.name);
     return true;
    }
   });
  return v;
 }

 private static AlertDialog getEdtDialog(Context c)
 {
  if(edtDialog == null)
  {
   OnClickListener listener = new OnClickListener()
   {
    Context c;

    public OnClickListener setContext(Context c)
    {
     this.c = c;
     return this;
    }

    @Override
    public void onClick(DialogInterface p1, int p2)
    {
     switch(p2)
     {
      case DialogInterface.BUTTON_POSITIVE:
       int count = 0;
       if(edt_count.length() > 0)
        try
        {
         count = Integer.parseInt(edt_count.getText().toString());
        }catch(Throwable t)
        {
         return;
        }
       int index = selectedList.indexOf(curAlipayUser.id);
       if(count > 0)
       {
        if(index < 0)
        {
         selectedList.add(curAlipayUser.id);
         countList.add(count);
        }else
        {
         countList.set(index, count);
        }
        curViewHolder.cb.setChecked(true);
       }else
       {
        if(index >= 0)
        {
         selectedList.remove(index);
         countList.remove(index);
        }
        curViewHolder.cb.setChecked(false);
       }
       Config.hasConfigChanged = true;
       break;
     }
     ListAdapter.get(c).notifyDataSetChanged();
    }
   }.setContext(c);
   edt_count = new EditText(c);
   edt_count.setHint("次数");
   edtDialog = new AlertDialog.Builder(c)
    .setTitle("title")
    .setView(edt_count)
    .setPositiveButton("确定", listener)
    .setNegativeButton("取消", null)
    .create();
  }
  return edtDialog;
 }

 private static AlertDialog getDeleteDialog(Context c)
 {
  if(deleteDialog == null)
  {
   OnClickListener listener = new OnClickListener()
   {
    Context c;

    public OnClickListener setContext(Context c)
    {
     this.c = c;
     return this;
    }

    @Override
    public void onClick(DialogInterface p1, int p2)
    {
     switch(p2)
     {
      case DialogInterface.BUTTON_POSITIVE:
       Config.removeIdMap(curAlipayUser.id);
       AlipayUser.remove(curAlipayUser.id);
       break;
     }
     ListAdapter.get(c).notifyDataSetChanged();
    }
   }.setContext(c);
   deleteDialog = new AlertDialog.Builder(c)
   .setMessage("msg")
   .setPositiveButton("确定", listener)
   .setNegativeButton("取消", null)
   .create();
  }
  return deleteDialog;
 }

 static class OnBtnClickListener implements View.OnClickListener
 {
  @Override
  public void onClick(View p1)
  {
   if(edt_find.length() <= 0) return;
   ListAdapter la = ListAdapter.get(p1.getContext());
   int index = -1;
   switch(p1.getId())
   {
    case R.id.btn_find_last:
     // 下面Text要转String，不然判断equals会出问题
     index = la.findLast(edt_find.getText().toString());
     break;

    case R.id.btn_find_next:
     // 同上
     index = la.findNext(edt_find.getText().toString());
     break;
   }
   if(index < 0)
   {
    Toast.makeText(p1.getContext(), "未找到", Toast.LENGTH_SHORT).show();
   }else
   {
    lv_list.setSelection(index);
   }
  }
 }

}
