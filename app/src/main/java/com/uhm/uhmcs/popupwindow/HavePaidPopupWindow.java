package com.uhm.uhmcs.popupwindow;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.view.CustomInputTextView;

public class HavePaidPopupWindow {
    private PopupWindow popupWindow;
    private Context context;



    public HavePaidPopupWindow(Context context) {
        this.context = context;
        initPopup();
    }


    private void initPopup() {
        View popupView = LayoutInflater.from(context).inflate(R.layout.popupwindow_have_paid, null);
        popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                true
        );
        popupWindow.setFocusable(true);
        popupWindow.setTouchable(true);
        popupWindow.setOutsideTouchable(false);
//        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupView.setBackgroundColor(context.getColor(R.color.black60));

        // 计算居中位置
        popupView.post(() -> {
            DisplayMetrics metrics = new DisplayMetrics();
            ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int x = (metrics.widthPixels - popupView.getWidth()) / 2;
            int y = (metrics.heightPixels - popupView.getHeight()) / 2;
            popupWindow.update(x, y, -1, -1); // 更新位置
        });
        ImageView imageView=popupView.findViewById(R.id.image);

// 加载本地资源
        Glide.with(context).load(R.drawable.have_paid_img).into(imageView);


    }
    public void show() {
        View rootView = ((Activity) context).getWindow().getDecorView();
        popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, 0, 0);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                popupWindow.dismiss();
            }
        }, 2000);
    }

}
