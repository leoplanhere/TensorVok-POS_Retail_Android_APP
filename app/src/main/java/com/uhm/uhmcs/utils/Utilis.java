package com.uhm.uhmcs.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;

public class Utilis {
    private static long lastClickTime = 0;
    private static final int MIN_CLICK_DELAY = 1000; // 1秒

    public static boolean isFastClick() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime < MIN_CLICK_DELAY) {
            return true; // 快速点击
        }
        lastClickTime = currentTime;
        return false;
    }
    public static boolean isFastClick(int time) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime < time) {
            return true; // 快速点击
        }
        lastClickTime = currentTime;
        return false;
    }
    /**
     * 获取应用版本名称
     */
    public static String getVersionName(Context context) {
        try {
            return context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0)
                    .versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "unknown";
        }
    }

    public static String maskPhone(String phone) {
        if (TextUtils.isEmpty(phone) || phone.length() != 11) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

}
