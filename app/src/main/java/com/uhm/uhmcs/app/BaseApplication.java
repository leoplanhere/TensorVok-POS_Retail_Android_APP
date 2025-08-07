package com.uhm.uhmcs.app;

import android.app.Application;
import android.os.Bundle;
import android.text.TextUtils;

import com.dou361.dialogui.DialogUIUtils;
import com.uhm.uhmcs.http.OkHttpUtil;
import com.uhm.uhmcs.utils.MyPrinterHelper;
import com.uhm.uhmcs.utils.MyUsbDeviceHelper;
import com.uhm.uhmcs.utils.UserUtils;


import java.util.HashMap;
import java.util.Map;

public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        UserUtils.getInstance().inti(getApplicationContext());
        if ( UserUtils.getInstance().getLoginBase()!=null&& UserUtils.getInstance().getLoginBase().getData()!=null&& UserUtils.getInstance().getLoginBase().getData().getUserinfo()!=null){
            Map<String, String> params = new HashMap<>();
            if (!TextUtils.isEmpty(UserUtils.getInstance().getLoginBase().getData().getUserinfo().getToken())){
                params.put("token", UserUtils.getInstance().getLoginBase().getData().getUserinfo().getToken());
                OkHttpUtil.setGlobalHeaders(params);
            }

        }

        CrashHandler.init(getApplicationContext());

    }
}
