package com.ummah.app;

import android.app.Application;
import android.content.Context;

/**
 * UmmahApp — Application class (Singleton context)
 */
public class UmmahApp extends Application {

    private static Context ctx;

    @Override
    public void onCreate() {
        super.onCreate();
        ctx = getApplicationContext();
    }

    public static Context getContext() {
        return ctx;
    }
}
