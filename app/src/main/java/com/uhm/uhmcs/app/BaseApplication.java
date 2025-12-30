package com.uhm.uhmcs.app;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.text.TextUtils;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.dou361.dialogui.DialogUIUtils;
import com.uhm.uhmcs.http.OkHttpUtil;
import com.uhm.uhmcs.utils.MyPrinterHelper;
import com.uhm.uhmcs.utils.MyUsbDeviceHelper;
import com.uhm.uhmcs.utils.UserUtils;


import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();


        org.litepal.LitePal.initialize(this);
        UserUtils.getInstance().inti(getApplicationContext());
        if ( UserUtils.getInstance().getLoginBase()!=null&& UserUtils.getInstance().getLoginBase().getData()!=null&& UserUtils.getInstance().getLoginBase().getData().getUserinfo()!=null){
            Map<String, String> params = new HashMap<>();
            if (!TextUtils.isEmpty(UserUtils.getInstance().getLoginBase().getData().getUserinfo().getToken())){
                params.put("token", UserUtils.getInstance().getLoginBase().getData().getUserinfo().getToken());
                OkHttpUtil.setGlobalHeaders(params);
            }

        }

        CrashHandler.init(getApplicationContext());

        if (TextUtils.isEmpty(UserUtils.getInstance().getLanguage())){
            // 获取语言代码（如 "en"/"zh"）
            String language = Locale.getDefault().getLanguage();
            Log.i("ttt",">>>>>>>>>yuyan>>>>"+language);
            UserUtils.getInstance().setLanguage(getApplicationContext(),language);
        }else {
            Log.i("ttt",">>>>>>123123213>>>yuyan>>>>"+UserUtils.getInstance().getLanguage());
//            // 切换语言（示例为英文）
//            AppCompatDelegate.setApplicationLocales(
//                    LocaleListCompat.forLanguageTags(UserUtils.getInstance().getLanguage())
//            );
            Locale locale=new Locale(UserUtils.getInstance().getLanguage());
            Resources res = getResources();
            Configuration config = res.getConfiguration();
            if (Build.VERSION.SDK_INT >= 24) {
                config.setLocale(locale);
                config.setLocales(new LocaleList(locale));
            } else {
                config.locale = locale;
            }
            res.updateConfiguration(config, res.getDisplayMetrics());
        }
        Log.i("ttt",">>>>>>123123213>>>yuyan>>>>"+UserUtils.getInstance().getLanguage());
        // 获取语言代码（如 "en"/"zh"）
        String language = Locale.getDefault().getLanguage();
        Log.i("ttt",">>>>>>>>>yuyan>>>>"+language);
    }

    @Override
    protected void attachBaseContext(Context base) {
        UserUtils.getInstance().inti(base);
        String lang = UserUtils.getInstance().getLanguage();
        super.attachBaseContext(updateBaseContext(base, lang));
    }

    private Context updateBaseContext(Context context, String language) {
        Configuration config = context.getResources().getConfiguration();
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        config.setLocale(locale);
        return context.createConfigurationContext(config);
    }
}
