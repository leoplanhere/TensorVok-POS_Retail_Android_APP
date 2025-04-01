package com.uhm.uhmcs.popupwindow;

import android.app.Activity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.applikeysolutions.cosmocalendar.utils.SelectionType;
import com.applikeysolutions.cosmocalendar.view.CalendarView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.savvi.rangedatepicker.CalendarPickerView;
import com.savvi.rangedatepicker.SubTitle;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.adapter.HistoryOrderAdapter;
import com.uhm.uhmcs.bean.LastOrderBean;
import com.uhm.uhmcs.http.OkHttpUtil;
import com.uhm.uhmcs.http.POSApiSerview;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TimePopupWindow {
    private PopupWindow popupWindow;
    private Activity context;
    private CalendarPickerView calendar_view;
    private PopupWindowOnClickListener.TimeOnClickListener timeOnClickListener;


    public TimePopupWindow(Activity context, PopupWindowOnClickListener.TimeOnClickListener timeOnClickListener) {

        this.context = context;
        this.timeOnClickListener = timeOnClickListener;
        initPopup();
    }

    private void initPopup() {
        View popupView = LayoutInflater.from(context).inflate(R.layout.popupwindow_time, null);
        popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                true
        );
//        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupView.setBackgroundColor(context.getColor(R.color.black60));
        popupWindow.setOutsideTouchable(true);
        // 计算居中位置
        popupView.post(() -> {
            DisplayMetrics metrics = new DisplayMetrics();
            context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int x = (metrics.widthPixels - popupView.getWidth()) / 2;
            int y = (metrics.heightPixels - popupView.getHeight()) / 2;
            popupWindow.update(x, y, -1, -1); // 更新位置
        });
        final Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.DATE, 1);

        final Calendar lastYear = Calendar.getInstance();
        lastYear.add(Calendar.YEAR, - 10);
        calendar_view = popupView.findViewById(R.id.calendar_view);
//        calendar_view.init(lastYear.getTime(),nextYear.getTime()).inMode(CalendarPickerView.SelectionMode.RANGE);
//        ArrayList<Integer> list = new ArrayList<>();
//        list.add(2);
//
//        calendar_view.deactivateDates(list);
        ArrayList<Date> arrayList = new ArrayList<>();
        try {
            SimpleDateFormat dateformat = new SimpleDateFormat("dd-MM-yyyy");

            String strdate = "22-4-2019";
            String strdate2 = "26-4-2019";

            Date newdate = dateformat.parse(strdate);
            Date newdate2 = dateformat.parse(strdate2);
            arrayList.add(newdate);
            arrayList.add(newdate2);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        calendar_view.init(lastYear.getTime(), nextYear.getTime(), new SimpleDateFormat("MMMM, YYYY", Locale.getDefault())) //
                .inMode(CalendarPickerView.SelectionMode.RANGE); //
//                .withDeactivateDates(list)
//                .withSubTitles(getSubTitles())
//                .withHighlightedDates(arrayList);

        calendar_view.scrollToDate(new Date());

        popupView.findViewById(R.id.btn_queren).setOnClickListener(v -> {
            Log.i("ttt","list " + calendar_view.getSelectedDates().toString());
            popupWindow.dismiss();
            List<Date> dateList=calendar_view.getSelectedDates();
            if (dateList.isEmpty()){
                return;
            }
            // 定义日期格式（示例：2025-03-26）
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String startTime = sdf.format(dateList.get(0));
            String endTime = sdf.format(dateList.get(dateList.size()-1));
            timeOnClickListener.onClick(startTime,endTime);

        });
        popupView.findViewById(R.id.btn_quxiao).setOnClickListener(v -> {
            popupWindow.dismiss();
        });


    }
    private ArrayList<SubTitle> getSubTitles() {
        final ArrayList<SubTitle> subTitles = new ArrayList<>();
        final Calendar tmrw = Calendar.getInstance();
        tmrw.add(Calendar.DAY_OF_MONTH, 1);
        subTitles.add(new SubTitle(tmrw.getTime(), "₹1000"));
        return subTitles;
    }

    public void show() {
        View rootView = ((Activity) context).getWindow().getDecorView();
        popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, 0, 0);
    }

}
