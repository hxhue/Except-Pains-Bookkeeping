package com.example.epledger.db;

import android.app.Application;
import android.content.Context;
//import android.support.wearable.activity.WearableActivity;


public class MainApplication extends Application {
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = MainApplication.this;
    }

    public static Context getCustomApplicationContext() {
        return mContext;
    }
}
