package com.uhm.uhmcs.popupwindow;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
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

import com.google.gson.Gson;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.bean.ClubCardBean;
import com.uhm.uhmcs.bean.MemberBean;
import com.uhm.uhmcs.http.OkHttpUtil;
import com.uhm.uhmcs.http.POSApiSerview;
import com.uhm.uhmcs.utils.UserUtils;
import com.uhm.uhmcs.view.CustomInputTextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ClubCardPopupWindow {
    private PopupWindow popupWindow;
    private Activity context;
    private PopupWindowOnClickListener.ClubCardOnClickListener clubCardOnClickListener;






    public ClubCardPopupWindow(Activity context, PopupWindowOnClickListener.ClubCardOnClickListener clubCardOnClickListener) {
        this.context = context;
        this.clubCardOnClickListener=clubCardOnClickListener;
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
            Log.i("ttt",">>>>>>>支付码>"+text);
            if (TextUtils.isEmpty(pay_code.getText().toString())){
                new DeleteShopPopupWindow(context,"请输入会员卡号",true).show();
                return;
            }
            getCustomBlock();

        });
        popupView.findViewById(R.id.chaxunhuiyuan_btn).setOnClickListener(v -> {
            if (TextUtils.isEmpty(pay_code.getText().toString())){
                new DeleteShopPopupWindow(context,"请输入会员卡号",true).show();
                return;
            }
            getCustomBlock();
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

    public void getCustomBlock(){
        Map<String, String> params = new HashMap<>();
        params.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid());
        params.put("cardnumber",pay_code.getText().toString());
        String url = POSApiSerview.POS_URL + POSApiSerview.getCustomBlock;
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
                                ClubCardBean clubCardBean=new Gson().fromJson(response,ClubCardBean.class);
                                clubCardOnClickListener.onClick(clubCardBean);
                                dismiss();
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
