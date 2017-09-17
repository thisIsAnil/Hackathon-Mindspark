package com.hackathon;

import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.Charset;


/**
 * Created by INFIi on 3/16/2017.
 */

public class Log {

    static boolean DEBUG=false;
    public static void write(String msg) {
            if(!DEBUG)return;
            try {
                File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/smartxlog.txt");
                if (!f.exists()) f.createNewFile();
                FileInputStream fis = new FileInputStream(f);
                String old;
                byte[] bytes = new byte[(int) f.length()];
                if (bytes.length > 0) fis.read(bytes, 0, bytes.length);
                old = new String(bytes, Charset.defaultCharset());
                msg = old + "\n" + msg;
                fis.close();
                FileOutputStream fos = new FileOutputStream(f);
                fos.write(msg.getBytes(), 0, msg.length());
                fos.close();


            } catch (Exception ffe) {
            }
        }

    public static void d(String msg){
        Log.write(msg);
    }

    public static void e(Exception e){
        Log.write(e.getMessage());
       // throwCrashyticsException(e);
    }
    public static void e(String tag,Exception e){
        Log.write(e.getMessage());
        //throwCrashyticsException(e);
    }
    public static void e(Throwable e){
        Log.write(e.getMessage());
        //throwCrashyticsException(e);
    }
    public static void e(String tag,Throwable e){
        Log.write(e.getMessage());
        //throwCrashyticsException(e);
    }

    public static void w(String tag,String msg){
        Log.write(tag+msg);

    }
    public static void i(String tag,String msg){
        Log.write(tag+msg);
    }
    public static void v(String tag,String msg){
        Log.write(tag+msg);
    }
    public static void d(String tag,String msg){
        Log.write(tag+msg);
    }
    public static void e(String tag,String msg,Exception e){
        Log.write(tag+msg+e.getMessage());
    }


}
