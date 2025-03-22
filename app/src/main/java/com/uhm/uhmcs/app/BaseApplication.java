package com.uhm.uhmcs.app;

import android.app.Application;

import com.uhm.uhmcs.http.OkHttpUtil;
import com.uhm.uhmcs.utils.MyPrinterHelper;
import com.uhm.uhmcs.utils.UserUtils;

public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        UserUtils.getInstance().inti(getApplicationContext());
        MyPrinterHelper.getInstance().inti(this);
    }
}
