package com.uhm.uhmcs.utils;

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

}
