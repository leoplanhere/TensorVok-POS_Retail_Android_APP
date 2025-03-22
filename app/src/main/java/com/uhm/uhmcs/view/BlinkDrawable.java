package com.uhm.uhmcs.view;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BlinkDrawable extends Drawable {
    private boolean mVisible = false;
    private final Paint mPaint = new Paint();
    private final int mWidth;

    public BlinkDrawable(int color, int width) {
        mPaint.setColor(color);
        mWidth = width;
        // 启动光标闪烁动画
        scheduleSelf(this::invalidateSelf, SystemClock.uptimeMillis() + 500);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (mVisible) {
            Rect bounds = getBounds();
            canvas.drawRect(bounds.left, bounds.top, bounds.left + mWidth, bounds.bottom, mPaint);
        }
        mVisible = !mVisible;
        scheduleSelf(this::invalidateSelf, SystemClock.uptimeMillis() + 500);
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.UNKNOWN;
    }

}

