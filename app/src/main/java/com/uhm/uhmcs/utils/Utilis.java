package com.uhm.uhmcs.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static void parseProductInfo(String input) {
        if (!input.matches("^s\\d+w[\\d.]+d[\\d.]+$")) {
            throw new IllegalArgumentException("格式错误");
        }
        // 正则匹配模式：s开头id + w开头重量 + d开头折扣
        Pattern pattern = Pattern.compile("s(\\d+)w(\\d+\\.?\\d*)d(\\d+\\.?\\d*)");
        Matcher matcher = pattern.matcher(input);

        if(matcher.find()) {
            String productId = matcher.group(1);
            String weight = matcher.group(2);
            String discount = matcher.group(3);

            System.out.println("商品ID: " + productId);
            System.out.println("重量: " + weight);
            System.out.println("折扣: " + discount);
        } else {
            throw new IllegalArgumentException("输入格式错误");
        }
    }

}
