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

public class RefundPassWordPopupWindow {
    private PopupWindow popupWindow;
    private Context context;

    private PopupWindowOnClickListener.DiscountOnClickListener discountOnClickListener;


    private CustomInputTextView zhekou_tv;
    private View popupView;

    public RefundPassWordPopupWindow(Context context,  PopupWindowOnClickListener.DiscountOnClickListener discountOnClickListener) {
        this.context = context;
        this.discountOnClickListener=discountOnClickListener;

        initPopup();
    }

    private void initPopup() {
        popupView = LayoutInflater.from(context).inflate(R.layout.popupwindow_refund_password, null);
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


        zhekou_tv=popupView.findViewById(R.id.zhekou_tv);
        zhekou_tv.setOnInputCompleteListener(text -> {
            if (TextUtils.isEmpty(zhekou_tv.getText().toString())||!zhekou_tv.getText().toString().equals("1234")){
                new DeleteShopPopupWindow(context,"密码错误",true).show();
                return;
            }
            discountOnClickListener.onClick(zhekou_tv.getText().toString());
            popupWindow.dismiss();
        });
        zhekou_tv.postDelayed(() -> zhekou_tv.requestFocus(), 100);
        popupView.findViewById(R.id.guanbi_btn).setOnClickListener(v -> {
            popupWindow.dismiss();
        });
        initKey();

    }

    public void show() {
        View rootView = ((Activity) context).getWindow().getDecorView();
        popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, 0, 0);
    }
    @SuppressLint("SetTextI18n")
    private void initKey(){
        try{
            LinearLayout llkeyArea = (LinearLayout) popupView.findViewById(R.id.llkeyArea);
            for(int i = 0;i<llkeyArea.getChildCount();i++){
                if (i==0){
                    LinearLayout numview = (LinearLayout)llkeyArea.getChildAt(0);
                    for(int j = 0;j<numview.getChildCount();j++){
                        LinearLayout llNum01_09 = (LinearLayout)numview.getChildAt(j);

                        for (int u=0;u<llNum01_09.getChildCount();u++){
                            llNum01_09.getChildAt(u).setOnClickListener(v -> {
                                Log.i("ttt",v.getTag().toString());
                                zhekou_tv.setText(zhekou_tv.getText().toString()+v.getTag().toString());
                            });
                        }


                    }
                }


                if(i ==1 ){
                    LinearLayout d_c_submit_view = (LinearLayout)llkeyArea.getChildAt(1);
                    for(int j = 0;j<d_c_submit_view.getChildCount();j++){
                        d_c_submit_view.getChildAt(j).setOnClickListener(v -> {
                            if(v.getTag().toString().equals("c")){
                                zhekou_tv.setText("");
                            }else if(v.getTag().toString().equals("d")){
                                if (!zhekou_tv.getText().toString().isEmpty()){
                                    zhekou_tv.setText(zhekou_tv.getText().toString().substring(0,zhekou_tv.getText().toString().length()-1));
                                }

                            }else if(v.getTag().toString().equals("submit")){

                               if (TextUtils.isEmpty(zhekou_tv.getText().toString())||!zhekou_tv.getText().toString().equals("1234")){
                                   new DeleteShopPopupWindow(context,"密码错误",true).show();
                                   return;
                               }
                               discountOnClickListener.onClick(zhekou_tv.getText().toString());
                               popupWindow.dismiss();

                            }
                        });
                    }
                }
            }

        }catch (Exception ex){
            Log.i("错误返回",ex.getMessage()+"");
        }
    }

    public static boolean isNumber(String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        Pattern pattern = Pattern.compile("^[-+]?\\d+(\\.\\d+)?([eE][-+]?\\d+)?$");
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }

}
