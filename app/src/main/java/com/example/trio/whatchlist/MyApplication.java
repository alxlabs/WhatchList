package com.example.trio.whatchlist;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * Created by ASUS on 28/08/2017.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }
}
