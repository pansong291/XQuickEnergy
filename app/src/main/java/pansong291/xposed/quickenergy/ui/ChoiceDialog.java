package pansong291.xposed.quickenergy.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import pansong291.xposed.quickenergy.AntFarm.SendType;
import pansong291.xposed.quickenergy.Config;
import pansong291.xposed.quickenergy.Config.RecallAnimalType;
import pansong291.xposed.quickenergy.Config.ShowMode;

public class ChoiceDialog
{
 private static AlertDialog showModeDialog,
 sendTypeDialog, recallAnimalTypeDialog;

 public static void showShowMode(Context c, CharSequence title)
 {
  try
  {
   getShowModeDialog(c, title).show();
  }catch(Throwable t)
  {
   showModeDialog = null;
   getShowModeDialog(c, title).show();
  }
 }

 private static AlertDialog getShowModeDialog(Context c, CharSequence title)
 {
  if(showModeDialog == null)
   showModeDialog = new AlertDialog.Builder(c)
    .setTitle(title)
    .setSingleChoiceItems(ShowMode.nickNames, Config.showMode().ordinal(),
    new OnClickListener()
    {
     @Override
     public void onClick(DialogInterface p1, int p2)
     {
      Config.setShowMode(p2);
     }
    })
    .setPositiveButton("确定", null)
    .create();
  return showModeDialog;
 }

 public static void showSendType(Context c, CharSequence title)
 {
  try
  {
   getSendTypeDialog(c, title).show();
  }catch(Throwable t)
  {
   sendTypeDialog = null;
   getSendTypeDialog(c, title).show();
  }
 }

 private static AlertDialog getSendTypeDialog(Context c, CharSequence title)
 {
  if(sendTypeDialog == null)
   sendTypeDialog = new AlertDialog.Builder(c)
    .setTitle(title)
    .setSingleChoiceItems(SendType.nickNames, Config.sendType(null).ordinal(),
    new OnClickListener()
    {
     @Override
     public void onClick(DialogInterface p1, int p2)
     {
      Config.setSendType(p2);
     }
    })
    .setPositiveButton("确定", null)
    .create();
  return sendTypeDialog;
 }

 public static void showRecallAnimalType(Context c, CharSequence title)
 {
  try
  {
   getRecallAnimalTypeDialog(c, title).show();
  }catch(Throwable t)
  {
   recallAnimalTypeDialog = null;
   getRecallAnimalTypeDialog(c, title).show();
  }
 }

 private static AlertDialog getRecallAnimalTypeDialog(Context c, CharSequence title)
 {
  if(recallAnimalTypeDialog == null)
   recallAnimalTypeDialog = new AlertDialog.Builder(c)
    .setTitle(title)
    .setSingleChoiceItems(RecallAnimalType.nickNames, Config.recallAnimalType().ordinal(),
    new OnClickListener()
    {
     @Override
     public void onClick(DialogInterface p1, int p2)
     {
      Config.setRecallAnimalType(p2);
     }
    })
    .setPositiveButton("确定", null)
    .create();
  return recallAnimalTypeDialog;
 }

}
