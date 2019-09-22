package pansong291.xposed.quickenergy;

import java.util.LinkedList;

public class BubbleTask
{
 public long bubbleId;
 public String userId;
 public long produceTime;
 private static LinkedList<BubbleTask> task;

 public static synchronized void addTask(long bi, String ui, long pt)
 {
  BubbleTask bt = new BubbleTask(bi, ui, pt);
  if(task == null) task = new LinkedList<>();
  int containsIndex = contains(bt);
  if(containsIndex >= 0)
  {
   if(task.get(containsIndex).produceTime == pt)
    return;
   task.remove(containsIndex);
  }
  int index = -1;
  for(int i = 0; i < task.size(); i++)
  {
   if(task.get(i).produceTime > bt.produceTime)
   {
    index = i;
    break;
   }
  }
  if(index < 0) task.add(bt);
  else task.add(index, bt);
 }

 public static synchronized BubbleTask outputTopTask()
 {
  BubbleTask bt = null;
  if(task != null) bt = task.remove(0);
  return bt;
 }

 public static long getDelayTime(int i, long offsetTime)
 {
  long l = 0;
  if(task != null)
   l = task.get(i).getDelayTime(offsetTime);
  return l;
 }

 public static int contains(BubbleTask bt)
 {
  if(task != null)
  {
   for(int i = 0; i < task.size(); i++)
   {
    if(task.get(i).equals(bt)) return i;
   }
  }
  return -1;
 }

 public static synchronized int size()
 {
  return task == null ? 0: task.size();
 }

 public long getDelayTime(long offsetTime)
 {
  return produceTime + offsetTime - System.currentTimeMillis() - 1000;
 }

 @Override
 public boolean equals(Object obj)
 {
  if(obj == null) return false;
  if(!(obj instanceof BubbleTask)) return false;
  BubbleTask bt = (BubbleTask) obj;
  return userId.equals(bt.userId) && bubbleId == bt.bubbleId;
 }

 private BubbleTask(long bi, String ui, long pt)
 {
  bubbleId = bi;
  userId = ui;
  produceTime = pt;
 }
}
