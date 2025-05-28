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
import com.uhm.uhmcs.bean.GrouponGoodsBean;
import com.uhm.uhmcs.bean.MemberBean;
import com.uhm.uhmcs.http.OkHttpUtil;
import com.uhm.uhmcs.http.POSApiSerview;
import com.uhm.uhmcs.utils.UserUtils;
import com.uhm.uhmcs.view.CustomInputTextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddNoCodePopupWindow {
    private PopupWindow popupWindow;
    private Activity context;

    private PopupWindowOnClickListener.AddNoCodeOnClickListener addNoCodeOnClickListener;

    private View popupView;
    private CustomInputTextView shop_pice_et;

    public AddNoCodePopupWindow(Activity context, PopupWindowOnClickListener.AddNoCodeOnClickListener addNoCodeOnClickListener) {
        this.context = context;
        this.addNoCodeOnClickListener=addNoCodeOnClickListener;

        initPopup();
    }

    private void initPopup() {
        popupView = LayoutInflater.from(context).inflate(R.layout.popupwindow_add_no_code, null);
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
        shop_pice_et=popupView.findViewById(R.id.shop_pice_et);
        shop_pice_et.setOnInputCompleteListener(text -> {

        });
        shop_pice_et.postDelayed(() -> shop_pice_et.requestFocus(), 100);
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
                                shop_pice_et.setText(shop_pice_et.getText().toString()+v.getTag().toString());
                            });
                        }


                    }
                }


                if(i ==1 ){
                    LinearLayout d_c_submit_view = (LinearLayout)llkeyArea.getChildAt(1);
                    for(int j = 0;j<d_c_submit_view.getChildCount();j++){
                        d_c_submit_view.getChildAt(j).setOnClickListener(v -> {
                            if(v.getTag().toString().equals("c")){
                                shop_pice_et.setText("");
                            }else if(v.getTag().toString().equals("d")){
                                if (!shop_pice_et.getText().toString().isEmpty()){
                                    shop_pice_et.setText(shop_pice_et.getText().toString().substring(0,shop_pice_et.getText().toString().length()-1));
                                }

                            }else if(v.getTag().toString().equals("submit")){
                                addNoCode();
                            }
                        });
                    }
                }
            }

        }catch (Exception ex){
            Log.i("错误返回",ex.getMessage()+"");
        }
    }

    public boolean isValidNumber(String input) {
        if (TextUtils.isEmpty(input)) return false;

        String regex = "^\\d+(\\.\\d*)?$";
        if (!input.matches(regex)) return false;

        // 检查小数点数量
        int dotCount = input.length() - input.replace(".", "").length();
        return dotCount <= 1;
    }

    private void addNoCode(){
        if (TextUtils.isEmpty(shop_pice_et.getText().toString())){
            new DeleteShopPopupWindow(context,"请输入商品价格").show();
            return;
        }
        if (!isValidNumber(shop_pice_et.getText().toString())){
            new DeleteShopPopupWindow(context,"请输入正确格式的价格").show();
            return;
        }
        getMember();

    }

    public void getMember(){
        Map<String, String> params = new HashMap<>();
        params.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid()+"");
        params.put("price",shop_pice_et.getText().toString());
        params.put("cost_price","15");
        params.put("online_type","normal");
        params.put("title",context.getString(R.string.no_barcode));
        String url = POSApiSerview.POS_URL + POSApiSerview.addNoCode;
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
                                Gson gson = new Gson();
                                GrouponGoodsBean grouponGoodsBean = gson.fromJson(response, GrouponGoodsBean.class);
                                addNoCodeOnClickListener.onClick(grouponGoodsBean.getData().get(0));
                                popupWindow.dismiss();
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
