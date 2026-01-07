package com.uhm.uhmcs.utils;

import android.view.MotionEvent;
import android.view.View;

/**
 * 带有“点击分发”与“网格吸附感”的拖拽监听器
 */
public class DragTouchListener implements View.OnTouchListener {
    private float dX, dY;
    private float startX, startY; // 用于记录手指按下的初始位置

    // 点击判定阈值：手指移动距离小于 10 像素则判定为点击
    private static final int CLICK_ACTION_THRESHOLD = 10;

    // 吸附步长：1mm = 8dp，你可以根据需要调整为 4 或 8
    private static final int SNAP_STEP_DP = 8;

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        float density = view.getContext().getResources().getDisplayMetrics().density;
        float snapStepPx = SNAP_STEP_DP * density; // 换算成像素步长

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                // 记录手指初始坐标
                startX = event.getRawX();
                startY = event.getRawY();

                // 记录 View 初始偏移量
                dX = view.getX() - startX;
                dY = view.getY() - startY;

                view.bringToFront();
                break;

            case MotionEvent.ACTION_MOVE:
                float currentX = event.getRawX();
                float currentY = event.getRawY();

                // 计算目标坐标（带上偏移）
                float targetX = currentX + dX;
                float targetY = currentY + dY;

                // --- 增加：网格吸附逻辑 ---
                // 计算最接近步长整数倍的位置
                float snappedX = Math.round(targetX / snapStepPx) * snapStepPx;
                float snappedY = Math.round(targetY / snapStepPx) * snapStepPx;

                // 边界限制
                View parent = (View) view.getParent();
                if (parent != null) {
                    if (snappedX < 0) snappedX = 0;
                    if (snappedX + view.getWidth() > parent.getWidth()) {
                        snappedX = parent.getWidth() - view.getWidth();
                    }
                    if (snappedY < 0) snappedY = 0;
                    if (snappedY + view.getHeight() > parent.getHeight()) {
                        snappedY = parent.getHeight() - view.getHeight();
                    }
                }

                // 执行移动
                view.setX(snappedX);
                view.setY(snappedY);
                break;

            case MotionEvent.ACTION_UP:
                float endX = event.getRawX();
                float endY = event.getRawY();

                // --- 核心修复：判断是点击还是拖拽 ---
                if (isAClick(startX, endX, startY, endY)) {
                    // 如果移动距离极小，手动触发 View 的 Click 事件
                    view.performClick();
                }
                break;

            default:
                return false;
        }
        return true;
    }

    /**
     * 判断手指位移是否在点击范围内
     */
    private boolean isAClick(float startX, float endX, float startY, float endY) {
        float differenceX = Math.abs(startX - endX);
        float differenceY = Math.abs(startY - endY);
        return !(differenceX > CLICK_ACTION_THRESHOLD || differenceY > CLICK_ACTION_THRESHOLD);
    }
}