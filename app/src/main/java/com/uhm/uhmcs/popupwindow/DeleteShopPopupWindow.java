package com.uhm.uhmcs.popupwindow;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

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
    private CustomInputTextView pay_password;
    private boolean is_edit=false;


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
    public DeleteShopPopupWindow(boolean is_edit,Context context,String hint_content,PopupWindowOnClickListener.DeleteShopOnClickListener deleteShopOnClickListener) {
        this.context = context;
        this.is_edit=is_edit;
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
    public DeleteShopPopupWindow(Context context,String hint_content,PopupWindowOnClickListener.DeleteShopOnClickListener deleteShopOnClickListener) {
        this.context = context;
        this.hint_content=hint_content;
        this.deleteShopOnClickListener=deleteShopOnClickListener;
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
        if (!TextUtils.isEmpty(hint_content)){
            hint_tv.setText(hint_content);
        }
        // 绑定子 View 事件
        popupView.findViewById(R.id.btn_quxiao).setOnClickListener(v -> {
            popupWindow.dismiss();
        });
        popupView.findViewById(R.id.btn_queren).setOnClickListener(v -> {

            if (deleteShopOnClickListener!=null){
                if (is_edit){
                    if (pay_password.getText().toString().equals("1234")){
                        deleteShopOnClickListener.onClick(pay_password.getText().toString());
                    }else {
                        hint_tv.setText("密码错误");
                        hint_tv.setTextColor(Color.RED);
                        return;
                    }
                }else {
                    deleteShopOnClickListener.onClick("");
                }

            }
            popupWindow.dismiss();

        });
        btn_view=popupView.findViewById(R.id.btn_view);
        if (is_dismiss){
            btn_view.setVisibility(GONE);
        }
        if (!is_shouBtn){
            btn_view.setVisibility(GONE);
        }
        popupView.findViewById(R.id.guanbi_btn).setOnClickListener(v -> {
            if (deleteShopOnClickListener!=null&&!is_edit){
                deleteShopOnClickListener.onClick("");
            }
            popupWindow.dismiss();
        });
        pay_code=popupView.findViewById(R.id.pay_code);

        pay_code.setOnInputCompleteListener(text -> {
            pay_code.setText("");
            Log.i("ttt",">>>>>>>支付码>"+text);
//            popupWindow.dismiss();
            if (deleteShopOnClickListener!=null){
                if (is_shouBtn){
                    deleteShopOnClickListener.onClick("");
                    popupWindow.dismiss();
                }else {
                    deleteShopOnClickListener.onClick(text);
                }

            }

        });
        pay_password=popupView.findViewById(R.id.pay_password);

        pay_password.setOnInputCompleteListener(text -> {
            Log.i("ttt",">>>>>>>支付码>"+text);
//            popupWindow.dismiss();
            if (deleteShopOnClickListener!=null){
                if (text.equals("1234")){
                    popupWindow.dismiss();
                    deleteShopOnClickListener.onClick(text);
                }else {
                    hint_tv.setText("密码错误");
                    hint_tv.setTextColor(Color.RED);
                }

            }

        });


    }
    CustomInputTextView pay_code;
    LinearLayout btn_view;
    public void show() {
        if (popupWindow.isShowing()){
            return;
        }

        View rootView = ((Activity) context).getWindow().getDecorView();
        popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, 0, 0);
        if (is_edit){
            // 自动获取焦点
            pay_password.setVisibility(VISIBLE);
            pay_password.postDelayed(() -> pay_password.requestFocus(), 100);

        }else {
            // 自动获取焦点
            pay_code.postDelayed(() -> pay_code.requestFocus(), 100);
        }

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
    public void dismiss(){
        popupWindow.dismiss();
    }
}
