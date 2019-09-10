package pansong291.xposed.quickenergy.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import java.util.List;
import pansong291.xposed.quickenergy.Config;
import pansong291.xposed.quickenergy.R;
import android.widget.Toast;

public class ListDialog
{
 static AlertDialog listDialog;
 static Button btn_add_id, btn_find, btn_find_next;
 static EditText edt_find;
 static ListView lv_list;
 static List<String> selectedList;

 static AlertDialog edtDialog;
 static EditText edt_id, edt_name;

 public static void show(Context c, CharSequence title, List<String> l)
 {
  selectedList = l;
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
  btn_add_id = v.findViewById(R.id.btn_add_id);
  btn_find = v.findViewById(R.id.btn_find);
  btn_find_next = v.findViewById(R.id.btn_find_next);
  btn_add_id.setOnClickListener(onBtnClickListener);
  btn_find.setOnClickListener(onBtnClickListener);
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
     ListAdapter.ViewHolder vh = (ListAdapter.ViewHolder)p2.getTag();
     AlipayUser au = (AlipayUser)p1.getAdapter().getItem(p3);
     if(vh.cb.isChecked())
     {
      if(selectedList.contains(au.id))
       selectedList.remove(au.id);
      vh.cb.setChecked(false);
     }else
     {
      if(!selectedList.contains(au.id))
       selectedList.add(au.id);
      vh.cb.setChecked(true);
     }
     Config.hasConfigChanged = true;
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
       add2AlipayUserList(false);
       break;

      case DialogInterface.BUTTON_NEUTRAL:
       add2AlipayUserList(true);
       break;
     }
     ListAdapter.get(c).notifyDataSetChanged();
    }
   }.setContext(c);
   edtDialog = new AlertDialog.Builder(c)
    .setView(getEdtView(c))
    .setPositiveButton("添加", listener)
    .setNegativeButton("取消", null)
    .setNeutralButton("添加并选中", listener)
    .create();
  }
  return edtDialog;
 }

 private static void add2AlipayUserList(boolean selected)
 {
  String id = edt_id.getText().toString();
  String name = edt_name.getText().toString();
  if(id.length() > 0)
  {
   if(!Config.getIdMap().containsKey(id))
   {
    if(name == null || name.isEmpty())
     name = "*(*)";
    if(!name.endsWith("(*)"))
     name += "(*)";
    Config.putIdMap(id, name);
    AlipayUser.getAlipayUserList().add(new AlipayUser(id, name));
   }
   if(selected)
   {
    selectedList.add(id);
    Config.hasConfigChanged = true;
   }
  }
 }

 private static View getEdtView(Context c)
 {
  View v = LayoutInflater.from(c).inflate(R.layout.dialog_edt, null);
  edt_id = v.findViewById(R.id.edt_id);
  edt_name = v.findViewById(R.id.edt_name);
  return v;
 }

 static class OnBtnClickListener implements View.OnClickListener
 {
  @Override
  public void onClick(View p1)
  {
   switch(p1.getId())
   {
    case R.id.btn_add_id:
     try
     {
      getEdtDialog(p1.getContext()).show();
     }catch(Throwable t)
     {
      edtDialog = null;
      getEdtDialog(p1.getContext()).show();
     }
     break;

    case R.id.btn_find:
     int visible = edt_find.getVisibility();
     if(visible == View.VISIBLE)
     {
      btn_find.setText("查找");
      ListAdapter la = ListAdapter.get(p1.getContext());
      la.exitFind();
     }else
     {
      btn_find.setText("关闭");
     }
     edt_find.setVisibility(View.GONE - visible);
     btn_find_next.setVisibility(View.GONE - visible);
     btn_add_id.setVisibility(visible);
     break;

    case R.id.btn_find_next:
     if(edt_find.length() <= 0) break;
     ListAdapter la = ListAdapter.get(p1.getContext());
     // 下面Text一定要转String，不然判断equals会出问题
     int index = la.find(edt_find.getText().toString());
     if(index < 0)
     {
      Toast.makeText(p1.getContext(), "未找到", Toast.LENGTH_SHORT).show();
     }else
     {
      lv_list.setSelection(index);
     }
     break;
   }
  }
 }

}
