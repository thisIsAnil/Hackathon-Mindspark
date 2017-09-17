package com.hackathon;


import android.content.Context;
import android.os.Handler;

import com.facebook.soloader.SoLoader;

/**
 * Created by Aniket on 16-Sep-17.
 */

public class Application extends android.app.Application {
    public static volatile Handler applicationHandler;
    public static volatile Context applicationContext;
    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext =getApplicationContext();
        applicationHandler =new Handler(getApplicationContext().getMainLooper());
        SoLoader.init(applicationContext,false);
        AppFolderMaker.createFolders();
    }
}
