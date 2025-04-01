package com.uhm.uhmcs.popupwindow;

import android.annotation.SuppressLint;
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

import com.uhm.uhmcs.R;
import com.uhm.uhmcs.activity.MainActivity;
import com.uhm.uhmcs.bean.GrouponGoodsBean;
import com.uhm.uhmcs.http.OkHttpUtil;
import com.uhm.uhmcs.http.POSApiSerview;
import com.uhm.uhmcs.utils.MyPrinterHelper;
import com.uhm.uhmcs.view.CustomInputTextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class MoneyBoxPopupWindow {
    private PopupWindow popupWindow;
    private Activity context;




    public MoneyBoxPopupWindow(Activity context) {

        this.context = context;

        initPopup();
    }




    private void initPopup() {
        View popupView = LayoutInflater.from(context).inflate(R.layout.popupwindow_money_box, null);
        popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                true
        );
        popupView.setBackgroundColor(context.getColor(R.color.black60));
        popupWindow.setOutsideTouchable(true);

        popupView.findViewById(R.id.guanbi_btn).setOnClickListener(v -> {
            popupWindow.dismiss();
        });
        popupView.findViewById(R.id.open_money_box_btn).setOnClickListener(v -> {
            MyPrinterHelper.getInstance().asyncOpenMoneyBox(context);
        });



    }


    public void show() {
        View rootView = context.getWindow().getDecorView();
        popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, 0, 0);

    }
}
