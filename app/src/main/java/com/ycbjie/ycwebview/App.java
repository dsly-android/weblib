package com.ycbjie.ycwebview;

import android.app.Application;

import com.dsly.weblib.utils.X5LogUtils;
import com.dsly.weblib.utils.X5WebUtils;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        X5LogUtils.setIsDebug(BuildConfig.DEBUG);
        X5WebUtils.init(this,true,false);
        X5WebUtils.initCache(this,null);
    }
}