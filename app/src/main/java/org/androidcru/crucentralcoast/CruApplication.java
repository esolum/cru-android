package org.androidcru.crucentralcoast;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.orhanobut.logger.LogLevel;
import com.orhanobut.logger.Logger;

import org.aaronhe.threetengson.ThreeTenGsonAdapter;

public class CruApplication extends Application
{


    public static Gson gson;

    private static Context context;

    private static String PACKAGE_NAME;

    public static Context getContext()
    {
        return context;
    }

    public static String retrievePackageName()
    {
        return PACKAGE_NAME;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        Logger.init().setLogLevel(LogLevel.values()[BuildConfig.LOG_LEVEL]);
        context = this;
        PACKAGE_NAME = context.getPackageName();
        GsonBuilder builder = new GsonBuilder();
        builder = ThreeTenGsonAdapter.registerAll(builder);
        gson = builder.create();
    }


}
