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
 private static File logFile;
 
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
  }catch(Exception e)
  {
   Log.printStackTrace(TAG, e);
  }
  close(fr);
  return result.toString();
 }
 
 public static boolean append2LogFile(String s)
 {
  return append2File(s + "\n", getLogFile());
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
  }catch(Exception e)
  {
   Log.printStackTrace(TAG, e);
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
  }catch(Exception e)
  {
   Log.printStackTrace(TAG, e);
  }
  close(fw);
  return success;
 }
 
 public static void close(Closeable c)
 {
  try
  {
   if(c != null) c.close();
  }catch(Exception e)
  {
   Log.printStackTrace(TAG, e);
  }
 }
 
}
