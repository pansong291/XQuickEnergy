package pansong291.xposed.quickenergy;

import org.json.JSONObject;

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

 private static final String TAG = Statistics.class.getCanonicalName();
 private static final String
 jn_year = "year", jn_month = "month", jn_day = "day",
 jn_collected = "collected", jn_helped = "helped", jn_watered = "watered";

 private TimeStatistics year;
 private TimeStatistics month;
 private TimeStatistics day;
 private static Statistics statistics;

 public static void addData(DataType dt, int i)
 {
  String[] dateStr = Log.getFormatDate().split("-");
  int ye = Integer.parseInt(dateStr[0]);
  int mo = Integer.parseInt(dateStr[1]);
  int da = Integer.parseInt(dateStr[2]);

  if(ye > getStatistics().year.time)
  {
   getStatistics().year.reset(ye);
   getStatistics().month.reset(mo);
   getStatistics().day.reset(da);
  }else if(mo > getStatistics().month.time)
  {
   getStatistics().month.reset(mo);
   getStatistics().day.reset(da);
  }else if(da > getStatistics().day.time)
  {
   getStatistics().day.reset(da);
  }
  switch(dt)
  {
   case COLLECTED:
    getStatistics().day.collected += i;
    getStatistics().month.collected += i;
    getStatistics().year.collected += i;
    break;
   case HELPED:
    getStatistics().day.helped += i;
    getStatistics().month.helped += i;
    getStatistics().year.helped += i;
    break;
   case WATERED:
    getStatistics().day.watered += i;
    getStatistics().month.watered += i;
    getStatistics().year.watered += i;
    break;
  }
  FileUtils.write2File(statistics2Json(getStatistics()), FileUtils.getStatisticsFile());
 }

 public static int getData(TimeType tt, DataType dt)
 {
  int data = 0;
  TimeStatistics ts = null;
  switch(tt)
  {
   case YEAR:
    ts = getStatistics().year;
    break;
   case MONTH:
    ts = getStatistics().month;
    break;
   case DAY:
    ts = getStatistics().day;
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
  return sb.toString();
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

 private static Statistics defInit()
 {
  Statistics statis = new Statistics();
  String[] date = Log.getFormatDate().split("-");
  if(statis.year == null)
   statis.year = statis.new TimeStatistics(Integer.parseInt(date[0]));
  if(statis.month == null)
   statis.month = statis.new TimeStatistics(Integer.parseInt(date[1]));
  if(statis.day == null)
   statis.day = statis.new TimeStatistics(Integer.parseInt(date[2]));
  return statis;
 }

 private static Statistics json2Statistics(String json)
 {
  Statistics statis = null;
  try
  {
   JSONObject jo = new JSONObject(json);
   JSONObject joo = null;
   statis = new Statistics();

   joo = jo.getJSONObject(jn_year);
   statis.year = statis.new TimeStatistics(joo.getInt(jn_year));
   Log.i(TAG, jn_year + ":" + statis.year.time);
   statis.year.collected = joo.getInt(jn_collected);
   Log.i(TAG, "  " + jn_collected + ":" + statis.year.collected);
   statis.year.helped = joo.getInt(jn_helped);
   Log.i(TAG, "  " + jn_helped + ":" + statis.year.helped);
   statis.year.watered = joo.getInt(jn_watered);
   Log.i(TAG, "  " + jn_watered + ":" + statis.year.watered);

   joo = jo.getJSONObject(jn_month);
   statis.month = statis.new TimeStatistics(joo.getInt(jn_month));
   Log.i(TAG, jn_month + ":" + statis.month.time);
   statis.month.collected = joo.getInt(jn_collected);
   Log.i(TAG, "  " + jn_collected + ":" + statis.month.collected);
   statis.month.helped = joo.getInt(jn_helped);
   Log.i(TAG, "  " + jn_helped + ":" + statis.month.helped);
   statis.month.watered = joo.getInt(jn_watered);
   Log.i(TAG, "  " + jn_watered + ":" + statis.month.watered);

   joo = jo.getJSONObject(jn_day);
   statis.day = statis.new TimeStatistics(joo.getInt(jn_day));
   Log.i(TAG, jn_day + ":" + statis.day.time);
   statis.day.collected = joo.getInt(jn_collected);
   Log.i(TAG, "  " + jn_collected + ":" + statis.day.collected);
   statis.day.helped = joo.getInt(jn_helped);
   Log.i(TAG, "  " + jn_helped + ":" + statis.day.helped);
   statis.day.watered = joo.getInt(jn_watered);
   Log.i(TAG, "  " + jn_watered + ":" + statis.day.watered);

  }catch(Throwable t)
  {
   Log.printStackTrace(TAG, t);
   if(json != null)
   {
    Log.showToastIgnoreConfig("统计文件格式有误，已重置统计文件并备份原文件", "");
    FileUtils.write2File(json, FileUtils.getBackupFile(FileUtils.getStatisticsFile()));
   }
   statis = defInit();
  }
  String formated = statistics2Json(statis);
  if(!formated.equals(json))
  {
   Log.i(TAG, "重新格式化 statistics.json");
   FileUtils.write2File(formated, FileUtils.getStatisticsFile());
  }
  return statis;
 }

 public static String statistics2Json(Statistics statis)
 {
  JSONObject jo = new JSONObject();
  try
  {
   if(statis == null) statis = Statistics.defInit();
   JSONObject joo = new JSONObject();
   joo.put(jn_year, statis.year.time);
   joo.put(jn_collected, statis.year.collected);
   joo.put(jn_helped, statis.year.helped);
   joo.put(jn_watered, statis.year.watered);
   jo.put(jn_year, joo);

   joo = new JSONObject();
   joo.put(jn_month, statis.month.time);
   joo.put(jn_collected, statis.month.collected);
   joo.put(jn_helped, statis.month.helped);
   joo.put(jn_watered, statis.month.watered);
   jo.put(jn_month, joo);

   joo = new JSONObject();
   joo.put(jn_day, statis.day.time);
   joo.put(jn_collected, statis.day.collected);
   joo.put(jn_helped, statis.day.helped);
   joo.put(jn_watered, statis.day.watered);
   jo.put(jn_day, joo);

  }catch(Throwable t)
  {
   Log.printStackTrace(TAG, t);
  }
  return Config.formatJson(jo);
 }

}
