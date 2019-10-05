package pansong291.xposed.quickenergy.util;

import org.json.JSONObject;
import java.util.ArrayList;
import org.json.JSONArray;

public class Statistics
{
 public enum TimeType
 { YEAR, MONTH, DAY }

 public enum DataType
 { TIME, COLLECTED, HELPED, WATERED }

 private class TimeStatistics
 {
  int time;
  int collected, helped, watered;

  TimeStatistics(int i)
  {
   reset(i);
  }

  public void reset(int i)
  {
   time = i;
   collected = 0;
   helped = 0;
   watered = 0;
  }
 }

 private class FeedFriendLog
 {
  String userId;
  int today = 0;
  int feedCount = 0;
  public FeedFriendLog(String id)
  {
   userId = id;
  }
 }

 private static final String TAG = Statistics.class.getCanonicalName();
 private static final String
 jn_year = "year", jn_month = "month", jn_day = "day",
 jn_collected = "collected", jn_helped = "helped", jn_watered = "watered",
 jn_questionHint = "questionHint", jn_memberSignin = "memberSignin";

 private TimeStatistics year;
 private TimeStatistics month;
 private TimeStatistics day;

 // forest

 // farm
 private int answerQuestion = 0;
 private String questionHint;
 private ArrayList<FeedFriendLog> feedFriendLogList;

 // member
 private int memberSignin = 0;

 private static Statistics statistics;

 public static void addData(DataType dt, int i)
 {
  Statistics stat = getStatistics();
  resetToday();
  switch(dt)
  {
   case COLLECTED:
    stat.day.collected += i;
    stat.month.collected += i;
    stat.year.collected += i;
    break;
   case HELPED:
    stat.day.helped += i;
    stat.month.helped += i;
    stat.year.helped += i;
    break;
   case WATERED:
    stat.day.watered += i;
    stat.month.watered += i;
    stat.year.watered += i;
    break;
  }
  save();
 }

 public static int getData(TimeType tt, DataType dt)
 {
  Statistics stat = getStatistics();
  int data = 0;
  TimeStatistics ts = null;
  switch(tt)
  {
   case YEAR:
    ts = stat.year;
    break;
   case MONTH:
    ts = stat.month;
    break;
   case DAY:
    ts = stat.day;
    break;
  }
  if(ts != null)
   switch(dt)
   {
    case TIME:
     data = ts.time;
     break;
    case COLLECTED:
     data = ts.collected;
     break;
    case HELPED:
     data = ts.helped;
     break;
    case WATERED:
     data = ts.watered;
     break;
   }
  return data;
 }

 public static String getText()
 {
  statistics = null;
  Statistics stat = getStatistics();
  StringBuilder sb = new StringBuilder(getData(TimeType.YEAR, DataType.TIME) + "年：收");
  sb.append(getData(TimeType.YEAR, DataType.COLLECTED));
  sb.append("，  帮" + getData(TimeType.YEAR, DataType.HELPED));
  sb.append("，  浇" + getData(TimeType.YEAR, DataType.WATERED));
  sb.append("\n" + getData(TimeType.MONTH, DataType.TIME) + "月：收");
  sb.append(getData(TimeType.MONTH, DataType.COLLECTED));
  sb.append("，  帮" + getData(TimeType.MONTH, DataType.HELPED));
  sb.append("，  浇" + getData(TimeType.MONTH, DataType.WATERED));
  sb.append("\n" + getData(TimeType.DAY, DataType.TIME) + "日：收");
  sb.append(getData(TimeType.DAY, DataType.COLLECTED));
  sb.append("，  帮" + getData(TimeType.DAY, DataType.HELPED));
  sb.append("，  浇" + getData(TimeType.DAY, DataType.WATERED));
  if(stat.questionHint != null && !stat.questionHint.isEmpty())
  {
   sb.append("\n今日答题提示：" + stat.questionHint);
  }
  return sb.toString();
 }

 public static boolean canAnswerQuestionToday()
 {
  Statistics stat = getStatistics();
  return stat.answerQuestion < stat.day.time;
 }

 public static void answerQuestionToday()
 {
  Statistics stat = getStatistics();
  stat.answerQuestion = stat.day.time;
  save();
 }

 public static void setQuestionHint(String s)
 {
  getStatistics().questionHint = s;
  save();
 }

 public static boolean canFeedFriendToday(String id, int count)
 {
  Statistics stat = getStatistics();
  int index = -1;
  for(int i = 0; i < stat.feedFriendLogList.size(); i++)
   if(stat.feedFriendLogList.get(i).userId.equals(id))
   {
    index = i;
    break;
   }
  if(index < 0) return true;
  FeedFriendLog ffl = stat.feedFriendLogList.get(index);
  if(ffl.today < stat.day.time)
  {
   return true;
  }
  return ffl.feedCount < count;
 }

 public static void feedFriendToday(String id)
 {
  Statistics stat = getStatistics();
  FeedFriendLog ffl;
  int index = -1;
  for(int i = 0; i < stat.feedFriendLogList.size(); i++)
   if(stat.feedFriendLogList.get(i).userId.equals(id))
   {
    index = i;
    break;
   }
  if(index < 0)
  {
   ffl = stat.new FeedFriendLog(id);
   stat.feedFriendLogList.add(ffl);
  }else
  {
   ffl = stat.feedFriendLogList.get(index);
  }
  if(ffl.today < stat.day.time)
  {
   ffl.today = stat.day.time;
   ffl.feedCount = 1;
  }else
  {
   ffl.feedCount++;
  }
  save();
 }

 public static boolean canMemberSigninToday()
 {
  Statistics stat = getStatistics();
  return stat.memberSignin < stat.day.time;
 }

 public static void memberSigninToday()
 {
  Statistics stat = getStatistics();
  stat.memberSignin = stat.day.time;
  save();
 }

 private static Statistics getStatistics()
 {
  if(statistics == null)
  {
   String statJson = null;
   if(FileUtils.getStatisticsFile().exists())
    statJson = FileUtils.readFromFile(FileUtils.getStatisticsFile());
   statistics = json2Statistics(statJson);
  }
  return statistics;
 }

 public static void resetToday()
 {
  Statistics stat = getStatistics();
  String[] dateStr = Log.getFormatDate().split("-");
  int ye = Integer.parseInt(dateStr[0]);
  int mo = Integer.parseInt(dateStr[1]);
  int da = Integer.parseInt(dateStr[2]);

  if(ye > stat.year.time)
  {
   stat.year.reset(ye);
   stat.month.reset(mo);
   stat.day.reset(da);
   monthClear();
   dayClear();
  }else if(mo > stat.month.time)
  {
   stat.month.reset(mo);
   stat.day.reset(da);
   monthClear();
   dayClear();
  }else if(da > stat.day.time)
  {
   stat.day.reset(da);
   dayClear();
  }
 }

 private static void monthClear()
 {
  Statistics stat = getStatistics();
  stat.answerQuestion = 0;
  stat.feedFriendLogList.clear();
  stat.memberSignin = 0;
  save();
  FileUtils.getOtherLogFile().delete();
 }

 private static void dayClear()
 {
  FileUtils.getForestLogFile().delete();
  FileUtils.getFarmLogFile().delete();
 }

 private static Statistics defInit()
 {
  Statistics stat = new Statistics();
  String[] date = Log.getFormatDate().split("-");
  if(stat.year == null)
   stat.year = stat.new TimeStatistics(Integer.parseInt(date[0]));
  if(stat.month == null)
   stat.month = stat.new TimeStatistics(Integer.parseInt(date[1]));
  if(stat.day == null)
   stat.day = stat.new TimeStatistics(Integer.parseInt(date[2]));
  if(stat.feedFriendLogList == null)
   stat.feedFriendLogList = new ArrayList<>();
  return stat;
 }

 private static Statistics json2Statistics(String json)
 {
  Statistics stat = null;
  try
  {
   JSONObject jo = new JSONObject(json);
   JSONObject joo = null;
   stat = new Statistics();

   joo = jo.getJSONObject(jn_year);
   stat.year = stat.new TimeStatistics(joo.getInt(jn_year));
   Log.i(TAG, jn_year + ":" + stat.year.time);
   stat.year.collected = joo.getInt(jn_collected);
   Log.i(TAG, "  " + jn_collected + ":" + stat.year.collected);
   stat.year.helped = joo.getInt(jn_helped);
   Log.i(TAG, "  " + jn_helped + ":" + stat.year.helped);
   stat.year.watered = joo.getInt(jn_watered);
   Log.i(TAG, "  " + jn_watered + ":" + stat.year.watered);

   joo = jo.getJSONObject(jn_month);
   stat.month = stat.new TimeStatistics(joo.getInt(jn_month));
   Log.i(TAG, jn_month + ":" + stat.month.time);
   stat.month.collected = joo.getInt(jn_collected);
   Log.i(TAG, "  " + jn_collected + ":" + stat.month.collected);
   stat.month.helped = joo.getInt(jn_helped);
   Log.i(TAG, "  " + jn_helped + ":" + stat.month.helped);
   stat.month.watered = joo.getInt(jn_watered);
   Log.i(TAG, "  " + jn_watered + ":" + stat.month.watered);

   joo = jo.getJSONObject(jn_day);
   stat.day = stat.new TimeStatistics(joo.getInt(jn_day));
   Log.i(TAG, jn_day + ":" + stat.day.time);
   stat.day.collected = joo.getInt(jn_collected);
   Log.i(TAG, "  " + jn_collected + ":" + stat.day.collected);
   stat.day.helped = joo.getInt(jn_helped);
   Log.i(TAG, "  " + jn_helped + ":" + stat.day.helped);
   stat.day.watered = joo.getInt(jn_watered);
   Log.i(TAG, "  " + jn_watered + ":" + stat.day.watered);

   if(jo.has(Config.jn_answerQuestion))
    stat.answerQuestion = jo.getInt(Config.jn_answerQuestion);
   Log.i(TAG, Config.jn_answerQuestion + ":" + stat.answerQuestion);

   if(jo.has(jn_questionHint))
    stat.questionHint = jo.getString(jn_questionHint);
   Log.i(TAG, jn_questionHint + ":" + stat.questionHint);

   stat.feedFriendLogList = new ArrayList<>();
   Log.i(TAG, Config.jn_feedFriendAnimalList + ":[");
   if(jo.has(Config.jn_feedFriendAnimalList))
   {
    JSONArray ja = jo.getJSONArray(Config.jn_feedFriendAnimalList);
    for(int i = 0; i < ja.length(); i++)
    {
     JSONArray jaa = ja.getJSONArray(i);
     FeedFriendLog ffl = stat.new FeedFriendLog(jaa.getString(0));
     ffl.today = jaa.getInt(1);
     ffl.feedCount = jaa.getInt(2);
     stat.feedFriendLogList.add(ffl);
     Log.i(TAG, "  " + ffl.userId + "," + ffl.today + "," + ffl.feedCount + ",");
    }
   }

   if(jo.has(jn_memberSignin))
    stat.memberSignin = jo.getInt(jn_memberSignin);
   Log.i(TAG, jn_memberSignin + ":" + stat.memberSignin);

  }catch(Throwable t)
  {
   Log.printStackTrace(TAG, t);
   if(json != null)
   {
    Log.i(TAG, "统计文件格式有误，已重置统计文件并备份原文件");
    FileUtils.write2File(json, FileUtils.getBackupFile(FileUtils.getStatisticsFile()));
   }
   stat = defInit();
  }
  String formated = statistics2Json(stat);
  if(!formated.equals(json))
  {
   Log.i(TAG, "重新格式化 statistics.json");
   FileUtils.write2File(formated, FileUtils.getStatisticsFile());
  }
  return stat;
 }

 private static String statistics2Json(Statistics stat)
 {
  JSONObject jo = new JSONObject();
  try
  {
   if(stat == null) stat = Statistics.defInit();
   JSONObject joo = new JSONObject();
   joo.put(jn_year, stat.year.time);
   joo.put(jn_collected, stat.year.collected);
   joo.put(jn_helped, stat.year.helped);
   joo.put(jn_watered, stat.year.watered);
   jo.put(jn_year, joo);

   joo = new JSONObject();
   joo.put(jn_month, stat.month.time);
   joo.put(jn_collected, stat.month.collected);
   joo.put(jn_helped, stat.month.helped);
   joo.put(jn_watered, stat.month.watered);
   jo.put(jn_month, joo);

   joo = new JSONObject();
   joo.put(jn_day, stat.day.time);
   joo.put(jn_collected, stat.day.collected);
   joo.put(jn_helped, stat.day.helped);
   joo.put(jn_watered, stat.day.watered);
   jo.put(jn_day, joo);

   jo.put(Config.jn_answerQuestion, stat.answerQuestion);
   if(stat.questionHint != null)
    jo.put(jn_questionHint, stat.questionHint);

   JSONArray ja = new JSONArray();
   for(int i = 0; i < stat.feedFriendLogList.size(); i++)
   {
    FeedFriendLog ffl = stat.feedFriendLogList.get(i);
    JSONArray jaa = new JSONArray();
    jaa.put(ffl.userId);
    jaa.put(ffl.today);
    jaa.put(ffl.feedCount);
    ja.put(jaa);
   }
   jo.put(Config.jn_feedFriendAnimalList, ja);

   jo.put(jn_memberSignin, stat.memberSignin);
  }catch(Throwable t)
  {
   Log.printStackTrace(TAG, t);
  }
  return Config.formatJson(jo);
 }

 private static boolean save()
 {
  return FileUtils.write2File(statistics2Json(getStatistics()), FileUtils.getStatisticsFile());
 }

}
