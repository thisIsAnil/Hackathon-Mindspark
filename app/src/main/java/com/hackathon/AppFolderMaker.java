package com.hackathon;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created by INFIi on 3/17/2017.
 */

public class AppFolderMaker {
    private static final String ROOT= Environment.getExternalStorageDirectory().getPath();
    private static final String SLASH="/";
    public static final String DATA_FOLDER=ROOT+"/Android/data/com.hackathon";
    public static final String DOWNLOAD="Download";
    public static File getRootDirectory(Context context){
        File f=context.getFilesDir();
        if(!f.exists())f.mkdirs();
        return f;
    }
    public static File createCacheDir(Context context) {
        File cache=getCacheDirectory(context);
        cache.mkdirs();
        return cache;
    }
    public static File getCacheDirectory(Context context){
        File cache=new File(getRootDirectory(context).getAbsoluteFile()+"/cache");
        cache.mkdirs();
        return cache;
    }
    public static File createCacheFolderFor(Context context,String name) throws Exception{
        return new File(getCacheDirectory(context),name);
    }
    public static File getDownloadFile(){
        File f=new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Hackathon/Download");
        if(!f.exists())f.mkdirs();
        return f;
    }
    public static void createFolders(){
        File f=new File(DATA_FOLDER);
        if(!f.exists())f.mkdirs();
        f=new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Hackathon/Download");
        if(!f.exists())f.mkdirs();
        createCacheDir(AndroidUtilities.context);

    }
}
