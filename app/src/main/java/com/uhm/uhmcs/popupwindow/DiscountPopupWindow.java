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
import com.uhm.uhmcs.activity.MainActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiscountPopupWindow {
    private PopupWindow popupWindow;
    private Context context;

    private PopupWindowOnClickListener.DiscountOnClickListener discountOnClickListener;
    private String hint_content;

    private TextView zhekou_tv;
    private View popupView;

    public DiscountPopupWindow(Context context,String hint_content, PopupWindowOnClickListener.DiscountOnClickListener discountOnClickListener) {
        this.context = context;
        this.discountOnClickListener=discountOnClickListener;
        this.hint_content=hint_content;
        initPopup();
    }

    private void initPopup() {
        popupView = LayoutInflater.from(context).inflate(R.layout.popupwindow_discount, null);
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
        zhekou_tv=popupView.findViewById(R.id.zhekou_tv);
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
                                if (TextUtils.isEmpty(zhekou_tv.getText().toString())){
                                    return;
                                }
                                if (isNumber(zhekou_tv.getText().toString())){

                                        try {
                                            if (0<Double.parseDouble(zhekou_tv.getText().toString())&&Double.parseDouble(zhekou_tv.getText().toString())<=100){
                                                discountOnClickListener.onClick(zhekou_tv.getText().toString());
                                                popupWindow.dismiss();
                                            }else {
                                                new DeleteShopPopupWindow(context,"请输入正确格式的折扣").show();
                                            }
                                        } catch (NumberFormatException e) {

                                        }
                                }else {
                                    new DeleteShopPopupWindow(context,"请输入正确格式的折扣").show();
                                }

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
