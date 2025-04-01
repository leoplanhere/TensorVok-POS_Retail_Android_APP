package com.uhm.uhmcs.view;

import static android.view.KeyEvent.KEYCODE_NUMPAD_ENTER;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import androidx.appcompat.widget.AppCompatTextView;

public class CustomInputTextView extends AppCompatTextView {
    private Paint mCursorPaint;
    private boolean mCursorVisible;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private static final int CURSOR_BLINK_INTERVAL = 500; // 光标闪烁间隔(ms)
    private int mCursorWidth = 2; // 光标宽度(px)

    public CustomInputTextView(Context context) {
        super(context);
        init();
    }

    public CustomInputTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // 初始化光标画笔
        mCursorPaint = new Paint();
        mCursorPaint.setColor(getCurrentTextColor());
        mCursorPaint.setStrokeWidth(mCursorWidth);
        mCursorPaint.setTextSize(100);

        // 启用焦点和触摸模式
        setFocusable(true);
        setFocusableInTouchMode(true);

        // 启动光标闪烁
        startCursorBlinking();
    }

    // 开始光标闪烁动画
    private void startCursorBlinking() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mCursorVisible = !mCursorVisible;
                invalidate(); // 重绘View
                mHandler.postDelayed(this, CURSOR_BLINK_INTERVAL);
            }
        }, CURSOR_BLINK_INTERVAL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mCursorVisible && hasFocus()) {
            // 计算光标位置（文本右侧）
            float x = getPaint().measureText(getText().toString()) + getPaddingLeft()+1;
            float baseline = getBaseline();
            canvas.drawLine(x, baseline - getTextSize(), x, baseline, mCursorPaint);
        }
    }

    // 捕获外接键盘输入
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            // 处理字母/数字输入
            if (event.getUnicodeChar() >= 32) { // 可打印字符
                setText(getText().toString() + (char) event.getUnicodeChar());
                return true;
            }
            // 处理回车键
            if (keyCode == KeyEvent.KEYCODE_ENTER||keyCode ==KEYCODE_NUMPAD_ENTER) {
                performInputComplete(); // 输入完成回调
                return true;
            }
            // 处理删除键
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                if (!TextUtils.isEmpty(getText().toString())){
                    setText(getText().toString().substring(0,getText().toString().length()-1));
                }
                return true;
            }
            Log.i("ttt",">>>>KEYCODE_DEL>>"+keyCode+">>?>"+(char)event.getUnicodeChar());
        }
        return super.onKeyDown(keyCode, event);
    }

    // 输入完成回调接口
    public interface OnInputCompleteListener {
        void onInputComplete(String text);
    }

    private OnInputCompleteListener mListener;
    public void setOnInputCompleteListener(OnInputCompleteListener listener) {
        mListener = listener;
    }

    private void performInputComplete() {
        if (mListener != null) {
            mListener.onInputComplete(getText().toString());
        }
//        setText(""); // 清空输入
    }
}

