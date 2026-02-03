package com.uhm.uhmcs.popupwindow;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.uhm.uhmcs.R;

public class PrintProgressPopupWindow {
    private PopupWindow popupWindow;
    private ProgressBar progressBar;
    private TextView statusText;
    private Activity context;

    public PrintProgressPopupWindow(Activity context) {
        this.context = context;
        View view = LayoutInflater.from(context).inflate(R.layout.popupwindow_print_progress, null);
        popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);

        // 【关键】锁定屏幕：不接受外部点击，不接受焦点（防止返回键关闭）
        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(false);

        progressBar = view.findViewById(R.id.print_progress_bar);
        statusText = view.findViewById(R.id.print_status_text);
    }

    public void update(int current, int total) {
        int progress = (int) (((float) current / total) * 100);
        if (progressBar != null) progressBar.setProgress(progress);
        if (statusText != null) {
            statusText.setText("正在打印: " + current + " / " + total + " (" + progress + "%)");
        }
    }

    public void show() {
        popupWindow.showAtLocation(context.getWindow().getDecorView(), Gravity.CENTER, 0, 0);
    }

    public void dismiss() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }
}