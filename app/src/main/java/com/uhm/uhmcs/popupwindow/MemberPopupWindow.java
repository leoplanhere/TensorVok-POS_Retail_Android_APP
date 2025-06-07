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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.bean.MemberBean;
import com.uhm.uhmcs.bean.PrintDataBean;
import com.uhm.uhmcs.http.OkHttpUtil;
import com.uhm.uhmcs.http.POSApiSerview;
import com.uhm.uhmcs.utils.MyPrinterHelper;
import com.uhm.uhmcs.utils.UserUtils;
import com.uhm.uhmcs.view.CustomInputTextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MemberPopupWindow {
    private PopupWindow popupWindow;
    private Activity context;

    private View popupView;
    private CustomInputTextView phone_tv;
    private TextView mingzi_tv,shoujihao_tv;
    private PopupWindowOnClickListener.MemberOnClickListener memberOnClickListener;

    public MemberPopupWindow(Activity context, PopupWindowOnClickListener.MemberOnClickListener memberOnClickListener) {
        this.context = context;
        this.memberOnClickListener=memberOnClickListener;
        initPopup();
    }

    private void initPopup() {
        popupView = LayoutInflater.from(context).inflate(R.layout.popupwindow_member, null);
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
        phone_tv=popupView.findViewById(R.id.phone_tv);

        popupView.findViewById(R.id.guanbi_btn).setOnClickListener(v -> {
            popupWindow.dismiss();
        });
        popupView.findViewById(R.id.sousuo_btn).setOnClickListener(v -> {
            if (TextUtils.isEmpty(phone_tv.getText().toString())){
                new DeleteShopPopupWindow(context,context.getString(R.string.enter_phone_number),true).show();
                return;
            }
            if (!isPhoneValid(phone_tv.getText().toString())){
                new DeleteShopPopupWindow(context,context.getString(R.string.Valid_cellphone_number_required),true).show();
                return;
            }
            getMember();
        });

        // 设置输入完成监听
        phone_tv.setOnInputCompleteListener(text -> {
            if (memberBean==null){
                if (TextUtils.isEmpty(phone_tv.getText().toString())){
                    new DeleteShopPopupWindow(context,context.getString(R.string.enter_phone_number),true).show();
                    return;
                }
                if (!isPhoneValid(phone_tv.getText().toString())){
                    new DeleteShopPopupWindow(context,context.getString(R.string.Valid_cellphone_number_required),true).show();
                    return;
                }
                getMember();
            }else {
                popupWindow.dismiss();
                memberOnClickListener.onClick(memberBean);
            }



        });
        phone_tv.postDelayed(() -> phone_tv.requestFocus(), 100);
        phone_tv.setOnFnListener(()->popupWindow.dismiss());
        mingzi_tv=popupView.findViewById(R.id.mingzi_tv);
        shoujihao_tv=popupView.findViewById(R.id.shoujihao_tv);
        initKey();
        popupView.findViewById(R.id.all_view).setOnClickListener(v -> {

            popupWindow.dismiss();
        });
    }
    public static boolean isPhoneValid(String phone) {
        Pattern pattern = Pattern.compile("^1[3-9]\\d{9}$");
        Matcher matcher = pattern.matcher(phone);
        return matcher.matches();  // 精确匹配‌:ml-citation{ref="5,6" data="citationList"}
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
                                phone_tv.setText(phone_tv.getText().toString()+v.getTag().toString());
                            });
                        }


                    }
                }


                if(i ==1 ){
                    LinearLayout d_c_submit_view = (LinearLayout)llkeyArea.getChildAt(1);
                    for(int j = 0;j<d_c_submit_view.getChildCount();j++){
                        d_c_submit_view.getChildAt(j).setOnClickListener(v -> {
                            if(v.getTag().toString().equals("c")){
                                phone_tv.setText("");
                            }else if(v.getTag().toString().equals("d")){
                                if (!phone_tv.getText().toString().isEmpty()){
                                    phone_tv.setText(phone_tv.getText().toString().substring(0,phone_tv.getText().toString().length()-1));
                                }

                            }else if(v.getTag().toString().equals("submit")){
                                if (memberBean==null){
                                    return;
                                }
                                popupWindow.dismiss();
                                memberOnClickListener.onClick(memberBean);



                            }
                        });
                    }
                }
            }

        }catch (Exception ex){
            Log.i("错误返回",ex.getMessage()+"");
        }
    }
    private MemberBean memberBean;
    public void getMember(){
        Map<String, String> params = new HashMap<>();
        params.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid()+"");
        params.put("phone",phone_tv.getText().toString());
        String url = POSApiSerview.POS_URL + POSApiSerview.getMember;
        OkHttpUtil.postFormAsync(url, params, context,new OkHttpUtil.OkHttpCallback() {
            @Override
            public void onSuccess(String response) {
                Log.i("ttt",">>>>>>>>>>>>>");
                context.runOnUiThread(new Runnable() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonObject=new JSONObject(response);
                            int code=jsonObject.getInt("code");
                            if (code==1){
                                ArrayList<MemberBean> memberBeanArrayList=new Gson().fromJson(jsonObject.getString("data"),new TypeToken<ArrayList<MemberBean>>(){}.getType());
                                memberBean=memberBeanArrayList.get(0);
                                mingzi_tv.setText(context.getString(R.string.name)+memberBean.getNickname());
                                shoujihao_tv.setText(context.getString(R.string.phone_number)+memberBean.getMobile());
                            }else {
                                new DeleteShopPopupWindow(context,jsonObject.getString("msg"),true).show();
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }

            @Override
            public void onFailure(IOException e) {

            }
        });
    }

}
