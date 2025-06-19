package com.uhm.uhmcs.popupwindow;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.uhm.uhmcs.utils.UserUtils;
import com.uhm.uhmcs.view.CustomInputTextView;

public class ClubCardPopupWindow {
    private PopupWindow popupWindow;
    private Context context;
    private PopupWindowOnClickListener.DeleteShopOnClickListener deleteShopOnClickListener;






    public ClubCardPopupWindow(Context context, PopupWindowOnClickListener.DeleteShopOnClickListener deleteShopOnClickListener) {
        this.context = context;
        this.deleteShopOnClickListener=deleteShopOnClickListener;
        initPopup();
    }

    private void initPopup() {
        View popupView = LayoutInflater.from(context).inflate(R.layout.popupwindow_club_card, null);
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
        TextView hint_tv=popupView.findViewById(R.id.hint_tv);


        popupView.findViewById(R.id.guanbi_btn).setOnClickListener(v -> {
            popupWindow.dismiss();
        });
        pay_code=popupView.findViewById(R.id.pay_code);

        pay_code.setOnInputCompleteListener(text -> {
            pay_code.setText("");
            Log.i("ttt",">>>>>>>支付码>"+text);

        });


    }
    CustomInputTextView pay_code;

    public void show() {
        if (popupWindow.isShowing()){
            return;
        }

        View rootView = ((Activity) context).getWindow().getDecorView();
        popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, 0, 0);
        // 自动获取焦点
        pay_code.postDelayed(() -> pay_code.requestFocus(), 100);

    }
    public void dismiss(){
        popupWindow.dismiss();
    }
}
