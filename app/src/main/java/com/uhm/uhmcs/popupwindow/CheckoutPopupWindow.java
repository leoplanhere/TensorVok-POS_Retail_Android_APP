package com.uhm.uhmcs.popupwindow;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
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
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.bean.CheckoutBean;
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

public class CheckoutPopupWindow {
    private PopupWindow popupWindow;
    private Activity context;
    private CheckoutBean checkoutBean;
    private TextView yingshou_tv,youhui_tv,shijishou_tv,yidong_btn,xianjin_btn,zhaolin_tv;
    private CustomInputTextView shoukuan_tv;
    private DeleteShopPopupWindow deleteShopPopupWindow;

    private String pay_type="";

    private PopupWindowOnClickListener.CheckoutOnClickListener checkoutOnClickListener;



    public CheckoutPopupWindow(Activity context, CheckoutBean checkoutBean, PopupWindowOnClickListener.CheckoutOnClickListener checkoutOnClickListener) {
        this.context = context;
        this.checkoutBean=checkoutBean;
        this.checkoutOnClickListener=checkoutOnClickListener;
        initPopup();
    }
    View popupView;
    @SuppressLint("SetTextI18n")
    private void initPopup() {
        popupView = LayoutInflater.from(context).inflate(R.layout.popupwindow_checkout, null);
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
        yingshou_tv=popupView.findViewById(R.id.yingshou_tv);
        youhui_tv=popupView.findViewById(R.id.youhui_tv);
        shijishou_tv=popupView.findViewById(R.id.shijishou_tv);
        zhaolin_tv=popupView.findViewById(R.id.zhaolin_tv);
        yingshou_tv.setText("￥"+checkoutBean.getTotal_amount());
        shijishou_tv.setText("￥"+checkoutBean.getTotal_fee());
        youhui_tv.setText("￥"+checkoutBean.getDiscount_fee());
        shoukuan_tv=popupView.findViewById(R.id.shoukuan_tv);
        // 设置输入完成监听
        shoukuan_tv.setOnInputCompleteListener(text -> {
            shoukuan_tv.setText(text);
            if (TextUtils.isEmpty(shoukuan_tv.getText().toString())){
                return;
            }
            if (pay_type.equals("cash")){
                checkoutBean.setPay_type(pay_type);
                checkoutBean.setPay_fee(new BigDecimal(shoukuan_tv.getText().toString()).intValue());
                checkoutBean.setCash_price(shoukuan_tv.getText().toString());
                checkoutBean.setCash_change(zhaolin_tv.getText().toString());
                SubmitCheckout();
            }else {
                deleteShopPopupWindow.show();
            }
        });
        shoukuan_tv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 文本变化前的状态，如需要撤销操作可在此处理‌:ml-citation{ref="7,8" data="citationList"}
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 文本变化中实时触发，适合实时搜索‌:ml-citation{ref="7,8" data="citationList"}
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 文本变化完成后触发，如手机号输入满11位时触发验证‌:ml-citation{ref="6,8" data="citationList"}
                BigDecimal zhaolin;
                zhaolin=new BigDecimal(TextUtils.isEmpty(s.toString())?"0.00":s.toString()).subtract(new BigDecimal(checkoutBean.getTotal_fee()));
                zhaolin_tv.setText(zhaolin.toString());


            }
        });
        // 自动获取焦点
        shoukuan_tv.postDelayed(() -> shoukuan_tv.requestFocus(), 100);
        shoukuan_tv.setText(checkoutBean.getTotal_fee()+"");
        shoukuan_tv.setOnInputCompleteListener(text -> {
            if (TextUtils.isEmpty(shoukuan_tv.getText().toString())){
                return;
            }
            if (pay_type.equals("cash")){
                checkoutBean.setPay_type(pay_type);
                checkoutBean.setPay_fee(new BigDecimal(shoukuan_tv.getText().toString()).intValue());
                checkoutBean.setCash_price(shoukuan_tv.getText().toString());
                checkoutBean.setCash_change(zhaolin_tv.getText().toString());
                SubmitCheckout();
            }else {
                deleteShopPopupWindow.show();
            }
        });
        deleteShopPopupWindow=new DeleteShopPopupWindow(context, "请使用扫码枪完成支付",false, new PopupWindowOnClickListener.DeleteShopOnClickListener() {
            @Override
            public void onClick(String text) {
                String type=detectPaymentType(text);
                if (type.equals("unknown")){
                    return;
                }
                pay_type=type;
                checkoutBean.setPay_type(pay_type);
                checkoutBean.setShop_id(UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid());
                checkoutBean.setAuthCode(text);
                checkoutBean.setPay_fee(new BigDecimal(shoukuan_tv.getText().toString()).intValue());
                checkoutBean.setCash_price(shoukuan_tv.getText().toString());
                checkoutBean.setCash_change(zhaolin_tv.getText().toString());
                SubmitCheckout();
            }
        });

        yidong_btn=popupView.findViewById(R.id.yidong_btn);
        xianjin_btn=popupView.findViewById(R.id.xianjin_btn);
        yidong_btn.setOnClickListener(v -> {
            yidong_btn.setBackgroundResource(R.drawable.blue_bg3);
            xianjin_btn.setBackgroundResource(R.drawable.blue_bg2);
            pay_type="";
            shoukuan_tv.setText(checkoutBean.getTotal_fee()+"");
            deleteShopPopupWindow.show();
        });
        xianjin_btn.setOnClickListener(v -> {
            xianjin_btn.setBackgroundResource(R.drawable.blue_bg3);
            yidong_btn.setBackgroundResource(R.drawable.blue_bg2);
            pay_type="cash";
            shoukuan_tv.setText(checkoutBean.getTotal_fee()+"");
        });
        popupView.findViewById(R.id.guanbi_btn).setOnClickListener(v -> {
            popupWindow.dismiss();
        });
        initKey();

    }
    /**
     * 判断支付类型
     * @param code 扫码获取的字符串
     * @return "alipay"（支付宝）、"wechat"（微信）、"unknown"（未知）
     */
    public static String detectPaymentType(String code) {
        if (TextUtils.isEmpty(code)) return "unknown";

        // 检查是否为纯数字
        if (!code.matches("\\d+")) return "unknown";

        // 微信规则验证
        if (code.length() == 18 && code.matches("^(10|11|12|13|14|15)\\d{16}$")) {
            return "wechat";
        }

        // 支付宝规则验证
        if (code.length() >= 16 && code.length() <= 24
                && code.matches("^(25|26|27|28|29|30)\\d+")) {
            return "alipay";
        }

        return "unknown";
    }
    public void show() {
        View rootView = ((Activity) context).getWindow().getDecorView();
        popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, 0, 0);
        pay_type=checkoutBean.getPay_type();
        if (pay_type.equals("cash")){
            xianjin_btn.setBackgroundResource(R.drawable.blue_bg3);
            yidong_btn.setBackgroundResource(R.drawable.blue_bg2);
            pay_type="cash";
            shoukuan_tv.setText(checkoutBean.getTotal_fee()+"");
        }else {
            yidong_btn.setBackgroundResource(R.drawable.blue_bg3);
            xianjin_btn.setBackgroundResource(R.drawable.blue_bg2);
            pay_type="";
            shoukuan_tv.setText(checkoutBean.getTotal_fee()+"");
            deleteShopPopupWindow.show();
        }
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
                                shoukuan_tv.append(v.getTag().toString());
                            });
                        }


                    }
                }


                if(i ==1 ){
                    LinearLayout d_c_submit_view = (LinearLayout)llkeyArea.getChildAt(1);
                    for(int j = 0;j<d_c_submit_view.getChildCount();j++){
                        d_c_submit_view.getChildAt(j).setOnClickListener(v -> {
                            if(v.getTag().toString().equals("c")){
                                shoukuan_tv.setText("");
                            }else if(v.getTag().toString().equals("d")){
                                if (!shoukuan_tv.getText().toString().isEmpty()){
                                    shoukuan_tv.setText(shoukuan_tv.getText().toString().substring(0,shoukuan_tv.getText().toString().length()-1));
                                }

                            }else if(v.getTag().toString().equals("submit")){
                                if (TextUtils.isEmpty(shoukuan_tv.getText().toString())){
                                    return;
                                }
                                if (pay_type.equals("cash")){
                                    checkoutBean.setPay_type(pay_type);
                                    checkoutBean.setPay_fee(new BigDecimal(shoukuan_tv.getText().toString()).intValue());
                                    checkoutBean.setCash_price(shoukuan_tv.getText().toString());
                                    checkoutBean.setCash_change(zhaolin_tv.getText().toString());
                                    SubmitCheckout();
                                }else {
                                    deleteShopPopupWindow.show();
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

    public void SubmitCheckout(){
        String url = POSApiSerview.POS_URL + POSApiSerview.addOrder;
        Gson gson=new Gson();
        OkHttpUtil.postJsonAsync(url, gson.toJson(checkoutBean),context, new OkHttpUtil.OkHttpCallback() {
            @Override
            public void onSuccess(String response) {
                Log.i("ttt",">>>>>>>>>>>>>");
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonObject=new JSONObject(response);
                            int code=jsonObject.getInt("code");
                            if (code==1){
                                popupWindow.dismiss();
                                checkoutOnClickListener.onClick();
                                new DeleteShopPopupWindow(context,"支付成功",true).show();
                                operateDetails();
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

    public void operateDetails(){
        Map<String, String> params = new HashMap<>();
        params.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid()+"");
        String url = POSApiSerview.POS_URL + POSApiSerview.operateDetails;
        OkHttpUtil.postFormAsync(url, params,context, new OkHttpUtil.OkHttpCallback() {
            @Override
            public void onSuccess(String response) {
                Log.i("ttt",">>>>>>>>>>>>>");
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonObject=new JSONObject(response);
                            int code=jsonObject.getInt("code");
                            if (code==1){
                                ArrayList<PrintDataBean> printDataBeanArrayList = new Gson().fromJson(jsonObject.getString("data"),new TypeToken<ArrayList<PrintDataBean>>(){}.getType());
                                MyPrinterHelper.getInstance().asyncPrintCheckout(context,checkoutBean,printDataBeanArrayList.get(0));
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
