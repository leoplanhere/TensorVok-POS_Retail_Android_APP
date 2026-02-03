package com.uhm.uhmcs.utils;

import android.view.MotionEvent;
import android.view.View;

/**
 * 拖拽监听器 - 用于 DIY 标签时移动文本和条码
 */
public class DragTouchListener implements View.OnTouchListener {
    private float lastX, lastY;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = event.getRawX();
                lastY = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = event.getRawX() - lastX;
                float dy = event.getRawY() - lastY;

                v.setX(v.getX() + dx);
                v.setY(v.getY() + dy);

                lastX = event.getRawX();
                lastY = event.getRawY();
                break;
            case MotionEvent.ACTION_UP:
                v.performClick(); // 触发点击事件（用于切换字号）
                break;
        }
        return true;
    }
}