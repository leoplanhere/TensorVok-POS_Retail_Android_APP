package com.uhm.uhmcs.popupwindow;

import static android.view.View.GONE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.uhm.uhmcs.R;
import com.uhm.uhmcs.activity.LoginActivity;
import com.uhm.uhmcs.activity.StartActivity;
import com.uhm.uhmcs.view.CustomInputTextView;

public class DeleteShopPopupWindow {
    private PopupWindow popupWindow;
    private Context context;
    private PopupWindowOnClickListener.DeleteShopOnClickListener deleteShopOnClickListener;
    private String hint_content;
    private boolean is_dismiss=false;
    private boolean is_shouBtn=true;


    public DeleteShopPopupWindow(Context context,PopupWindowOnClickListener.DeleteShopOnClickListener deleteShopOnClickListener) {
        this.context = context;
        this.deleteShopOnClickListener=deleteShopOnClickListener;
        initPopup();
    }
    public DeleteShopPopupWindow(Context context,String hint_content) {
        this.context = context;
        this.hint_content=hint_content;
        initPopup();
    }
    public DeleteShopPopupWindow(Context context,String hint_content,boolean is_shouBtn,PopupWindowOnClickListener.DeleteShopOnClickListener deleteShopOnClickListener) {
        this.context = context;
        this.is_shouBtn=is_shouBtn;
        this.hint_content=hint_content;
        this.deleteShopOnClickListener=deleteShopOnClickListener;
        initPopup();
    }
    public DeleteShopPopupWindow(Context context,String hint_content,boolean is_dismiss) {
        this.context = context;
        this.hint_content=hint_content;
        this.is_dismiss=is_dismiss;
        initPopup();
    }
    public DeleteShopPopupWindow(Context context,boolean is_dismiss,String hint_content,PopupWindowOnClickListener.DeleteShopOnClickListener deleteShopOnClickListener) {
        this.context = context;
        this.hint_content=hint_content;
        this.is_dismiss=is_dismiss;
        this.deleteShopOnClickListener=deleteShopOnClickListener;
        initPopup();
    }

    private void initPopup() {
        View popupView = LayoutInflater.from(context).inflate(R.layout.popupwindow_delete_shop, null);
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
        TextView hint_tv=popupView.findViewById(R.id.hint_tv);
        if (!TextUtils.isEmpty(hint_content)){
            hint_tv.setText(hint_content);
        }
        // 绑定子 View 事件
        popupView.findViewById(R.id.btn_quxiao).setOnClickListener(v -> {
            popupWindow.dismiss();
        });
        popupView.findViewById(R.id.btn_queren).setOnClickListener(v -> {
            popupWindow.dismiss();
            if (deleteShopOnClickListener!=null){
                deleteShopOnClickListener.onClick("");
            }

        });
        btn_view=popupView.findViewById(R.id.btn_view);
        if (is_dismiss){
            btn_view.setVisibility(GONE);
        }
        if (!is_shouBtn){
            btn_view.setVisibility(GONE);
        }
        popupView.findViewById(R.id.guanbi_btn).setOnClickListener(v -> {
            popupWindow.dismiss();
        });
        pay_code=popupView.findViewById(R.id.pay_code);
        // 自动获取焦点
        pay_code.postDelayed(() -> pay_code.requestFocus(), 100);
        pay_code.setOnInputCompleteListener(text -> {
            pay_code.setText("");
            Log.i("ttt",">>>>>>>支付码>"+text);
            if (deleteShopOnClickListener!=null){
                deleteShopOnClickListener.onClick(text);
            }

        });

    }
    CustomInputTextView pay_code;
    LinearLayout btn_view;
    public void show() {
        View rootView = ((Activity) context).getWindow().getDecorView();
        popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, 0, 0);
        if (is_dismiss){
            btn_view.setVisibility(GONE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    popupWindow.dismiss();
                    if (deleteShopOnClickListener!=null){
                        deleteShopOnClickListener.onClick("");
                    }
                }
            }, 2000);
        }
    }
}
