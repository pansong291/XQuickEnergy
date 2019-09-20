package pansong291.xposed.quickenergy;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

public class AntForestNotification
{
 public static final int ANTFOREST_NOTIFICATION_ID = 46;
 private static NotificationManager mNotifyManager;
 public static final String CHANNEL_ID = "CHANNELID0";
 private static Notification mNotification;
 private static Notification.Builder builder;
 private static boolean isStart = false;

 private AntForestNotification()
 {}

 public static boolean isAntForestNotificationStart()
 {
  return isStart;
 }

 public static void start(Context context)
 {
  if(mNotification == null)
  {
   Intent it = new Intent(Intent.ACTION_VIEW);
   it.setData(Uri.parse("alipays://platformapi/startapp?appId=60000002"));
   PendingIntent pi = PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_UPDATE_CURRENT);

   if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
   {
    NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "ChannelName", NotificationManager.IMPORTANCE_DEFAULT);
    mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    if(mNotifyManager != null)
    {
     mNotifyManager.createNotificationChannel(notificationChannel);
    }
    builder = new Notification.Builder(context, CHANNEL_ID);
   }else
   {
    builder = new Notification.Builder(context);
   }
   mNotification = builder
    .setSmallIcon(android.R.drawable.sym_def_app_icon)
    .setContentTitle("XQuickEnergy")
    .setContentText("点此启动蚂蚁森林")
    .setAutoCancel(true)
    .setContentIntent(pi)
    .build();
  }
  if(!isStart)
  {
   if(context instanceof Service)
    ((Service)context).startForeground(ANTFOREST_NOTIFICATION_ID, mNotification);
   else
    mNotifyManager.notify(ANTFOREST_NOTIFICATION_ID, mNotification);
   isStart = true;
  }
 }

 public static void setContentText(CharSequence cs)
 {
  if(isStart)
  {
   mNotification = builder.setContentText(cs).build();
   mNotifyManager.notify(ANTFOREST_NOTIFICATION_ID, mNotification);
  }
 }

 public static void stop(Context context, boolean remove)
 {
  if(isStart)
  {
   if(context instanceof Service)
    ((Service)context).stopForeground(remove);
   else
    mNotifyManager.cancel(ANTFOREST_NOTIFICATION_ID);
   isStart = false;
  }
 }

}
