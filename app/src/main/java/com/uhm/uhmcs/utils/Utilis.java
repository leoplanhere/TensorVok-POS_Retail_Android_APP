package com.uhm.uhmcs.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils; // ★ 必须导入，用于判空

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utilis {
    private static long lastClickTime = 0;
    private static final int MIN_CLICK_DELAY = 1000; // 1秒

    /**
     * 防止重复点击（默认1秒）
     */
    public static boolean isFastClick() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime < MIN_CLICK_DELAY) {
            return true; // 快速点击
        }
        lastClickTime = currentTime;
        return false;
    }

    /**
     * 防止重复点击（自定义时间）
     */
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

    /**
     * ★★★ 核心修复：手机号脱敏方法 ★★★
     * 作用：将 13812345678 转换为 138****5678
     * 用于 ReceiptBitmapGenerator 第116行调用
     */
    public static String maskPhone(String phone) {
        if (TextUtils.isEmpty(phone)) {
            return "";
        }
        // 如果不是11位手机号（例如座机或其他格式），直接返回原样，不进行遮掩
        if (phone.length() != 11) {
            return phone;
        }
        // 截取前3位 + 星号 + 后4位
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /**
     * 解析产品信息（保留原有逻辑）
     */
    public static void parseProductInfo(String input) {
        if (TextUtils.isEmpty(input)) return;

        if (!input.matches("^s\\d+w[\\d.]+d[\\d.]+$")) {
            return;
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
        }
    }
}