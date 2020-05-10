package edu.caltech.seva.activities;

import android.app.Application;
import android.content.Context;

import com.squareup.leakcanary.RefWatcher;


public class MainApplication extends Application {
    private RefWatcher refWatcher;
    private static MainApplication instance;

    public static MainApplication getInstance() {
        return instance;
    }

    public static Context getContext() {
        return instance;
    }

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();

//        if (LeakCanary.isInAnalyzerProcess(this)){
//            return;
//        }
//        refWatcher = LeakCanary.install(this);
    }

//    public static RefWatcher getRefWatcher(Context context){
//        MainApplication application = (MainApplication) context.getApplicationContext();
//        return application.refWatcher;
//    }
}
