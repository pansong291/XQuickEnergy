package pansong291.xposed.quickenergy.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;
import pansong291.xposed.quickenergy.Config;

public class EditDialog
{
 private static AlertDialog editDialog;
 private static EditText edt;
 public enum EditMode
 { TIME_INTERVAL, RETURN_WATER_30, RETURN_WATER_20, RETURN_WATER_10 }
 private static EditMode mode;

 public static void showReturnWaterDialog(Context c, CharSequence title, EditMode em)
 {
  mode = em;
  try
  {
   getEditDialog(c, title).show();
  }catch(Throwable t)
  {
   editDialog = null;
   getEditDialog(c, title).show();
  }
  editDialog.setTitle(title);
 }

 public static void showTimeIntervalDialog(Context c, CharSequence title)
 {
  mode = EditMode.TIME_INTERVAL;
  try
  {
   getEditDialog(c, title).show();
  }catch(Throwable t)
  {
   editDialog = null;
   getEditDialog(c, title).show();
  }
  editDialog.setTitle(title);
 }

 private static AlertDialog getEditDialog(Context c, CharSequence title)
 {
  if(editDialog == null)
  {
   edt = new EditText(c);
   editDialog = new AlertDialog.Builder(c)
    .setTitle(title)
    .setView(edt)
    .setPositiveButton(
    "确定",
    new OnClickListener()
    {
     Context context;

     public OnClickListener setData(Context c)
     {
      context = c;
      return this;
     }

     @Override
     public void onClick(DialogInterface p1, int p2)
     {
      switch(mode)
      {
       case TIME_INTERVAL:
        try
        {
         long l = Long.parseLong(edt.getText().toString());
         if(l >= 0)
         {
          if(l < 10_000)
          {
           Toast.makeText(context, "间隔时间太短", 0).show();
           break;
          }else if(l < 60_000)
           Toast.makeText(context, "该项不影响秒偷，请尽量以分钟起步", 0).show();
          else
           Toast.makeText(context, "需要重启支付宝生效", 0).show();
          Config.setTimeInterval(l);
         }
        }catch(Throwable t)
        {}
        break;

       case RETURN_WATER_30:
        try
        {
         int i = Integer.parseInt(edt.getText().toString());
         if(i >= 0)
         {
          Config.setReturnWater30(i);
         }
        }catch(Throwable t)
        {}
        break;

       case RETURN_WATER_20:
        try
        {
         int i = Integer.parseInt(edt.getText().toString());
         if(i >= 0)
         {
          Config.setReturnWater20(i);
         }
        }catch(Throwable t)
        {}
        break;

       case RETURN_WATER_10:
        try
        {
         int i = Integer.parseInt(edt.getText().toString());
         if(i >= 0)
         {
          Config.setReturnWater10(i);
         }
        }catch(Throwable t)
        {}
        break;
      }
     }
    }.setData(c))
    .create();
  }
  String str = "";
  switch(mode)
  {
   case TIME_INTERVAL:
    str = String.valueOf(Config.timeInterval());
    break;

   case RETURN_WATER_30:
    str = String.valueOf(Config.returnWater30());
    break;

   case RETURN_WATER_20:
    str = String.valueOf(Config.returnWater20());
    break;

   case RETURN_WATER_10:
    str = String.valueOf(Config.returnWater10());
    break;
  }
  edt.setText(str);
  return editDialog;
 }

}
