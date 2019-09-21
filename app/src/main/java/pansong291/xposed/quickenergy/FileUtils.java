package pansong291.xposed.quickenergy;

import android.os.Environment;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class FileUtils
{
 private static final String TAG = FileUtils.class.getCanonicalName();
 private static File directory;
 private static File configFile;
 private static File friendIdMapFile;
 private static File statisticsFile;
 private static File logFile;
 private static File runtimeLogFile;

 public static File getDirectoryPath()
 {
  if(directory == null)
  {
   directory = new File(Environment.getExternalStorageDirectory(), "Android/data/pansong291.xposed.quickenergy");
   if(directory.exists())
   {
    if(directory.isFile())
    {
     directory.delete();
     directory.mkdirs();
    }
   }else
   {
    directory.mkdirs();
   }
  }
  return directory;
 }

 public static File getConfigFile()
 {
  if(configFile == null)
  {
   configFile = new File(getDirectoryPath(), "config.json");
   if(configFile.exists() && configFile.isDirectory())
    configFile.delete();
  }
  return configFile;
 }

 public static File getFriendIdMapFile()
 {
  if(friendIdMapFile == null)
  {
   friendIdMapFile = new File(getDirectoryPath(), "friendId.list");
   if(friendIdMapFile.exists() && friendIdMapFile.isDirectory())
    friendIdMapFile.delete();
  }
  return friendIdMapFile;
 }

 public static File getStatisticsFile()
 {
  if(statisticsFile == null)
  {
   statisticsFile = new File(getDirectoryPath(), "statistics.json");
   if(statisticsFile.exists() && statisticsFile.isDirectory())
    statisticsFile.delete();
  }
  return statisticsFile;
 }

 public static File getLogFile()
 {
  if(logFile == null)
  {
   logFile = new File(getDirectoryPath(), "energy.log");
   if(logFile.exists() && logFile.isDirectory())
    logFile.delete();
  }
  return logFile;
 }

 public static File getRuntimeLogFile()
 {
  if(runtimeLogFile == null)
  {
   runtimeLogFile = new File(getDirectoryPath(), "runtime.log");
   if(runtimeLogFile.exists() && runtimeLogFile.isDirectory())
    runtimeLogFile.delete();
  }
  return runtimeLogFile;
 }

 public static File getBackupFile(File f)
 {
  return new File(f.getAbsolutePath() + ".bak");
 }

 public static String readFromFile(File f)
 {
  StringBuilder result = new StringBuilder();
  FileReader fr = null;
  try
  {
   fr = new FileReader(f);
   char[] chs = new char[1024];
   int len = 0;
   while((len = fr.read(chs)) >= 0)
   {
    result .append(chs, 0, len);
   }
  }catch(Throwable t)
  {
   Log.printStackTrace(TAG, t);
  }
  close(fr);
  return result.toString();
 }

 public static boolean append2LogFile(String s)
 {
  return append2File(Log.getFormatDateTime() + "  " + s + "\n", getLogFile());
 }

 public static boolean append2RuntimeLogFile(String s)
 {
  if(getRuntimeLogFile().length() > 31_457_280) // 30MB
   getRuntimeLogFile().delete();
  return append2File(Log.getFormatDateTime() + "  " + s + "\n", getRuntimeLogFile());
 }

 public static boolean write2File(String s, File f)
 {
  boolean success = false;
  FileWriter fw = null;
  try
  {
   fw = new FileWriter(f);
   fw.write(s);
   fw.flush();
   success = true;
  }catch(Throwable t)
  {
   Log.printStackTrace(TAG, t);
  }
  close(fw);
  return success;
 }

 public static boolean append2File(String s, File f)
 {
  boolean success = false;
  FileWriter fw = null;
  try
  {
   fw = new FileWriter(f, true);
   fw.append(s);
   fw.flush();
   success = true;
  }catch(Throwable t)
  {
   Log.printStackTrace(TAG, t);
  }
  close(fw);
  return success;
 }

 public static void close(Closeable c)
 {
  try
  {
   if(c != null) c.close();
  }catch(Throwable t)
  {
   Log.printStackTrace(TAG, t);
  }
 }

}
