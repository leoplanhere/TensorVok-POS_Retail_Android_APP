package com.uhm.uhmcs.popupwindow;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.uhm.uhmcs.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TimePopupWindow {
    private PopupWindow popupWindow;
    private Activity context;
    private PopupWindowOnClickListener.TimeOnClickListener timeOnClickListener;

    private DatePicker datePicker;
    private TextView tvTitle, btnConfirm;

    private String startTime = "";
    private String endTime = "";
    private boolean isPickingStart = true; // 状态位：正在选开始还是结束

    public TimePopupWindow(Activity context, PopupWindowOnClickListener.TimeOnClickListener timeOnClickListener) {
        this.context = context;
        this.timeOnClickListener = timeOnClickListener;
        initPopup();
    }

    private void initPopup() {
        View popupView = LayoutInflater.from(context).inflate(R.layout.popupwindow_time, null);
        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        popupWindow.setOutsideTouchable(true);

        datePicker = popupView.findViewById(R.id.date_picker_view);
        tvTitle = popupView.findViewById(R.id.time_title_tv);
        btnConfirm = popupView.findViewById(R.id.btn_queren);

        // 确认按钮逻辑：两步走
        btnConfirm.setOnClickListener(v -> {
            int year = datePicker.getYear();
            int month = datePicker.getMonth();
            int day = datePicker.getDayOfMonth();

            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);

            if (isPickingStart) {
                // 第一步：存下开始时间，切换到选结束时间
                startTime = sdf.format(calendar.getTime());
                isPickingStart = false;
                tvTitle.setText("选择结束日期");
                btnConfirm.setText("完成查询");
            } else {
                // 第二步：存下结束时间，回调并关闭
                endTime = sdf.format(calendar.getTime());
                if (timeOnClickListener != null) {
                    timeOnClickListener.onClick(startTime, endTime);
                }
                popupWindow.dismiss();
            }
        });

        popupView.findViewById(R.id.btn_quxiao).setOnClickListener(v -> popupWindow.dismiss());
    }

    public void show() {
        popupWindow.showAtLocation(context.getWindow().getDecorView(), Gravity.NO_GRAVITY, 0, 0);
    }
}