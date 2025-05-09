package com.uhm.uhmcs.popupwindow;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.uhm.uhmcs.R;
import com.uhm.uhmcs.view.CustomInputTextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrintNumPopupWindow {
    private PopupWindow popupWindow;
    private Context context;

    private PopupWindowOnClickListener.DiscountOnClickListener discountOnClickListener;

    private View popupView;
    private CustomInputTextView pint_num;

    public PrintNumPopupWindow(Context context,  PopupWindowOnClickListener.DiscountOnClickListener discountOnClickListener) {
        this.context = context;
        this.discountOnClickListener=discountOnClickListener;
        initPopup();
    }

    private void initPopup() {
        popupView = LayoutInflater.from(context).inflate(R.layout.popupwindow_print_num, null);
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
            ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int x = (metrics.widthPixels - popupView.getWidth()) / 2;
            int y = (metrics.heightPixels - popupView.getHeight()) / 2;
            popupWindow.update(x, y, -1, -1); // 更新位置
        });



        popupView.findViewById(R.id.guanbi_btn).setOnClickListener(v -> {
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.print_btn).setOnClickListener(v -> {
            if (!isInteger(pint_num.getText().toString())){
                new DeleteShopPopupWindow(context,"请输入正确的数字",true).show();
                return;
            }
            discountOnClickListener.onClick(pint_num.getText().toString());
            popupWindow.dismiss();
        });
        pint_num=popupView.findViewById(R.id.print_num);
        pint_num.setOnInputCompleteListener(text -> {
            if (!isInteger(text)){
                new DeleteShopPopupWindow(context,"请输入正确的数字",true).show();
                return;
            }
            discountOnClickListener.onClick(text);
            popupWindow.dismiss();
        });
        pint_num.postDelayed(() -> pint_num.requestFocus(), 100);

    }

    public void show() {
        View rootView = ((Activity) context).getWindow().getDecorView();
        popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, 0, 0);
    }

    public static boolean isInteger(String str) {
        if (str == null || str.isEmpty()) return false;
        if (!str.matches("^\\d+$")) return false;  // 格式校验
        try {
            int num = Integer.parseInt(str);  // 范围校验（避免溢出）
            if (num>0){
                return true;
            }else {
                return false;
            }

        } catch (NumberFormatException e) {
            return false;
        }
    }


}
