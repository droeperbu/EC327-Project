package com.beep_boop.Beep;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application
{
    private static Context context;
    
    public static final String FONT = "fonts/Krungthep.ttf";

    public void onCreate(){
        super.onCreate();
        MyApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }
}