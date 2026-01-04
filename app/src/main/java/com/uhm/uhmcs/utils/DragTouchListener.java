package com.uhm.uhmcs.utils;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * 通用拖拽监听器，限制只能在父容器范围内移动
 */
public class DragTouchListener implements View.OnTouchListener {
    private float dX, dY;
    private int lastAction;

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                dX = view.getX() - event.getRawX();
                dY = view.getY() - event.getRawY();
                lastAction = MotionEvent.ACTION_DOWN;
                // 选中时给点视觉反馈（可选）
                view.bringToFront();
                break;

            case MotionEvent.ACTION_MOVE:
                float newX = event.getRawX() + dX;
                float newY = event.getRawY() + dY;

                // 边界限制 (防止拖出画布)
                View parent = (View) view.getParent();
                if (parent != null) {
                    if (newX < 0) newX = 0;
                    if (newX + view.getWidth() > parent.getWidth()) {
                        newX = parent.getWidth() - view.getWidth();
                    }
                    if (newY < 0) newY = 0;
                    if (newY + view.getHeight() > parent.getHeight()) {
                        newY = parent.getHeight() - view.getHeight();
                    }
                }

                view.setX(newX);
                view.setY(newY);
                lastAction = MotionEvent.ACTION_MOVE;
                break;

            case MotionEvent.ACTION_UP:
                // 可以在这里保存坐标，但为了性能，我们放在“打印/保存”按钮里统一读取 View.getX()/getY()
                break;
        }
        return true;
    }
}