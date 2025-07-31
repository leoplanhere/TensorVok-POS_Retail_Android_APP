package com.uhm.uhmcs.popupwindow;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
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

import com.dou361.dialogui.DialogUIUtils;
import com.dou361.dialogui.bean.BuildBean;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.activity.LoginActivity;
import com.uhm.uhmcs.activity.MainActivity;
import com.uhm.uhmcs.bean.CategoryListBean;
import com.uhm.uhmcs.bean.CheckoutBean;
import com.uhm.uhmcs.bean.ClubCardBean;
import com.uhm.uhmcs.bean.PrintDataBean;
import com.uhm.uhmcs.http.NetworkErrorInterceptor;
import com.uhm.uhmcs.http.OkHttpUtil;
import com.uhm.uhmcs.http.POSApiSerview;
import com.uhm.uhmcs.utils.GsonSandL;
import com.uhm.uhmcs.utils.MyPrinterHelper;
import com.uhm.uhmcs.utils.NetworkUtils;
import com.uhm.uhmcs.utils.UserUtils;
import com.uhm.uhmcs.utils.Utilis;
import com.uhm.uhmcs.view.CustomInputTextView;
import com.uhm.uhmcs.view.MyPresentation;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class CheckoutPopupWindow {
    private PopupWindow popupWindow;
    private Activity context;
    private CheckoutBean checkoutBean;
    private TextView yingshou_tv,youhui_tv,shijishou_tv,weixin_btn,xianjin_btn,zhaolin_tv,zhifubao_btn,yishou_tv,huiyuanka_btn,huiyuanchaxun_btn;
    private CustomInputTextView shoukuan_tv;
    ClubCardBean.DataBean clubCardData;
    TextView huiyuankahao_tv ;
    TextView huiyuanyue_tv ;
    private LinearLayout huiyuan_view;
    private DeleteShopPopupWindow deleteShopPopupWindow;
    private String yinshou="0.00";

    private String pay_type="";
    private int order_status=0;

    private boolean weixin_type=false;
    private boolean zhifubao_type=false;

    private boolean isyouhuijuan=true;

    private Runnable shoukuan_tvRunnable = new Runnable() {
        @Override
        public void run() {
            shoukuan_tv.requestFocus();
        }
    };

    private PopupWindowOnClickListener.CheckoutOnClickListener checkoutOnClickListener;

    public boolean isShow(){
        return popupWindow.isShowing();
    }

    public CheckoutPopupWindow(Activity context, CheckoutBean checkoutBean, PopupWindowOnClickListener.CheckoutOnClickListener checkoutOnClickListener) {
        this.context = context;
        this.checkoutBean=checkoutBean;
        this.checkoutOnClickListener=checkoutOnClickListener;
        initPopup();
    }
    View popupView;
    BuildBean buildBean;
    private LinearLayout youhuijuan_view;
    private TextView youhuijuan_jine,shiyong_btn,bushiyong_btn;
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
        yishou_tv=popupView.findViewById(R.id.yishou_tv);

        youhuijuan_view=popupView.findViewById(R.id.youhuijuan_view);
        youhuijuan_jine=popupView.findViewById(R.id.youhuijuan_jine);
        shiyong_btn=popupView.findViewById(R.id.shiyong_btn);
        bushiyong_btn=popupView.findViewById(R.id.bushiyong_btn);


        shoukuan_tv=popupView.findViewById(R.id.shoukuan_tv);

        if (!TextUtils.isEmpty(checkoutBean.getCoupon_fee())){
            youhuijuan_view.setVisibility(VISIBLE);
            youhuijuan_jine.setText(checkoutBean.getCoupon_fee());
            checkoutBean.setTotal_fee(new BigDecimal(checkoutBean.getTotal_fee()).subtract(new BigDecimal(checkoutBean.getCoupon_fee())).toString());
            youhui_tv.setText("￥"+new BigDecimal(checkoutBean.getDiscount_fee()).add(new BigDecimal(checkoutBean.getCoupon_fee())).toString());
        }else {
            youhui_tv.setText("￥"+checkoutBean.getDiscount_fee());
        }
        yishou_tv.setText("￥0.00");
        yingshou_tv.setText("￥"+checkoutBean.getTotal_amount());
        shijishou_tv.setText("￥"+checkoutBean.getTotal_fee());


        shiyong_btn.setOnClickListener(v -> {
            if (isyouhuijuan){
                return;
            }
            isyouhuijuan=true;
            shiyong_btn.setBackgroundResource(R.drawable.blue_bg3);
            bushiyong_btn.setBackgroundResource(R.drawable.blue_bg2);
            youhui_tv.setText("￥"+new BigDecimal(checkoutBean.getDiscount_fee()).add(new BigDecimal(checkoutBean.getCoupon_fee())).toString());
            checkoutBean.setTotal_fee(new BigDecimal(checkoutBean.getTotal_fee()).subtract(new BigDecimal(checkoutBean.getCoupon_fee())).toString());
            shijishou_tv.setText("￥"+checkoutBean.getTotal_fee());
            shoukuan_tv.setText(checkoutBean.getTotal_fee()+"");
        });




        bushiyong_btn.setOnClickListener(v -> {
            if (!isyouhuijuan){
                return;
            }
            bushiyong_btn.setBackgroundResource(R.drawable.blue_bg3);
            shiyong_btn.setBackgroundResource(R.drawable.blue_bg2);
            isyouhuijuan=false;
            youhui_tv.setText("￥"+checkoutBean.getDiscount_fee());
            checkoutBean.setTotal_fee(new BigDecimal(checkoutBean.getTotal_fee()).add(new BigDecimal(checkoutBean.getCoupon_fee())).toString());
            shijishou_tv.setText("￥"+checkoutBean.getTotal_fee());
            shoukuan_tv.setText(checkoutBean.getTotal_fee()+"");
        });


        shoukuan_tv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 文本变化前的状态，如需要撤销操作可在此处理‌:ml-citation{ref="7,8" data="citationList"}
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 文本变化中实时触发，适合实时搜索‌:ml-citation{ref="7,8" data="citationList"}
                // 实时获取当前输入的字符
                if (TextUtils.isEmpty(s.toString())){
                    zhaolin_tv.setText("0.00");
                    return;
                }
                if (count > before) {
                    CharSequence newChar = s.subSequence(start, start + count);
//                    Log.d("ttt", "新增字符: " + newChar+">>>>"+s.toString());
                    if (newChar.equals(".")||newChar.equals("-")){
                        return;
                    }
                }
                if (!isValidNumber(s.toString())){
                    return;
                }

                BigDecimal zhaolin;
                zhaolin=new BigDecimal(TextUtils.isEmpty(s.toString())?"0.00":s.toString()).subtract(new BigDecimal(checkoutBean.getTotal_fee()).subtract(new BigDecimal(yinshou)));
                if (zhaolin.compareTo(BigDecimal.ZERO)>0){
                    zhaolin_tv.setText(zhaolin.toString());
                }else {
                    zhaolin_tv.setText("0.00");
                }

            }

            @Override
            public void afterTextChanged(Editable s) {
                // 文本变化完成后触发，如手机号输入满11位时触发验证‌:ml-citation{ref="6,8" data="citationList"}


            }
        });
        // 自动获取焦点
        shoukuan_tv.postDelayed(shoukuan_tvRunnable, 100);
        shoukuan_tv.setText(checkoutBean.getTotal_fee()+"");
        shoukuan_tv.setOnInputCompleteListener(text -> {

            if (TextUtils.isEmpty(shoukuan_tv.getText().toString())){
                new DeleteShopPopupWindow(context,context.getString(R.string.enter_payment_amount),true).show();
                return;
            }
            if (!isValidNumber(shoukuan_tv.getText().toString())){
                new DeleteShopPopupWindow(context,context.getString(R.string.Please_input_in_correct_price_format),true).show();
                return;
            }
            if (new BigDecimal(shoukuan_tv.getText().toString()).compareTo(BigDecimal.ZERO)<=0){
                new DeleteShopPopupWindow(context,context.getString(R.string.Amount_must_0),true).show();
                return;
            }

            if (pay_type.equals("cash")){
                checkoutBean.setPay_type(pay_type);
                checkoutBean.setPay_fee(shoukuan_tv.getText().toString());
                checkoutBean.setCash_price(shoukuan_tv.getText().toString());
                checkoutBean.setCash_change(zhaolin_tv.getText().toString());
                if (!NetworkUtils.getInstance().isNetworkConnected(context)){
                    if (new BigDecimal(shoukuan_tv.getText().toString()).subtract(new BigDecimal(checkoutBean.getTotal_fee())).compareTo(BigDecimal.ZERO)<0){
                        new DeleteShopPopupWindow(context,context.getString(R.string.Cannot_underpay),true).show();
                        return;
                    }
                }

                SubmitCheckout();
            }else if (pay_type.equals("wallet")){

                if (clubCardData == null) {
                    new DeleteShopPopupWindow(context, "请先查询会员信息", true).show();
                    return;
                }
                if (new BigDecimal(shoukuan_tv.getText().toString()).subtract(new BigDecimal(checkoutBean.getTotal_fee())).add(new BigDecimal(yinshou)).compareTo(BigDecimal.ZERO)>0){
                    new DeleteShopPopupWindow(context,context.getString(R.string.Cannot_overpay),true).show();
                    return;
                }
                if (new BigDecimal(clubCardData.getAmount()).subtract(new BigDecimal(shoukuan_tv.getText().toString())).compareTo(BigDecimal.ZERO)<0){
                    new DeleteShopPopupWindow(context,"会员卡余额不足",true).show();
                    return;
                }
                checkoutBean.setPay_type(pay_type);
                checkoutBean.setPay_fee(shoukuan_tv.getText().toString());
                checkoutBean.setCash_price(shoukuan_tv.getText().toString());
                checkoutBean.setCash_change(zhaolin_tv.getText().toString());
                checkoutBean.setCardnumber(clubCardData.getNumber());
                SubmitCheckout();
            }else {
                if (new BigDecimal(shoukuan_tv.getText().toString()).subtract(new BigDecimal(checkoutBean.getTotal_fee())).add(new BigDecimal(yinshou)).compareTo(BigDecimal.ZERO)>0){
                    new DeleteShopPopupWindow(context,context.getString(R.string.Cannot_overpay),true).show();
                    return;
                }
                shoukuan_tv.removeCallbacks(shoukuan_tvRunnable);
                shoukuan_tv.clearFocus();
                deleteShopPopupWindow.show();

            }
        });
        shoukuan_tv.setOnFnListener(()->popupWindow.dismiss());
        buildBean=DialogUIUtils.showLoading(context,context.getString(R.string.paying),true,false,false,false);
        deleteShopPopupWindow=new DeleteShopPopupWindow(context, context.getString(R.string.Scan_to_pay),false, new PopupWindowOnClickListener.DeleteShopOnClickListener() {
            @Override
            public void onClick(String text) {
                shoukuan_tv.postDelayed(shoukuan_tvRunnable, 100);
                Log.i("ttt",">>>1112>>>>支付码>"+text);
                if (TextUtils.isEmpty(text)){
                    return;
                }
                String type=detectPaymentType(text);
                if (type.equals("unknown")||!pay_type.equals(type)){
                    new DeleteShopPopupWindow(context,context.getString(R.string.Scan_payment_QR_code),true).show();
                    return;
                }

                pay_type=type;
                checkoutBean.setPay_type(pay_type);
                checkoutBean.setShop_id(UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid());
                checkoutBean.setAuthCode(text);
                checkoutBean.setPay_fee(shoukuan_tv.getText().toString());
                checkoutBean.setCash_price(shoukuan_tv.getText().toString());
                checkoutBean.setCash_change(zhaolin_tv.getText().toString());
//                checkoutBean.setPay_fee((int) 0.01);
//                checkoutBean.setCash_price("0.01");
//                checkoutBean.setCash_change("0.00");


                SubmitCheckout();
            }
        });

        weixin_btn=popupView.findViewById(R.id.weixin_btn);
        zhifubao_btn=popupView.findViewById(R.id.zhifubao_btn);
        huiyuanka_btn=popupView.findViewById(R.id.huiyuanka_btn);
        xianjin_btn=popupView.findViewById(R.id.xianjin_btn);
        huiyuan_view=popupView.findViewById(R.id.huiyuan_view);
        huiyuankahao_tv = popupView.findViewById(R.id.huiyuankahao_tv);
        huiyuanyue_tv = popupView.findViewById(R.id.huiyuanyue_tv);
        huiyuanchaxun_btn = popupView.findViewById(R.id.huiyuanchaxun_btn);

        weixin_btn.setOnClickListener(v -> {
            if (!NetworkUtils.getInstance().isNetworkConnected(context)){
                new DeleteShopPopupWindow(context,context.getString(R.string.Cash_only),true).show();
                return;
            }

            if (weixin_type){
                new DeleteShopPopupWindow(context,context.getString(R.string.WeChat_paid_already),true).show();
                return;
            }
            huiyuan_view.setVisibility(GONE);
            weixin_btn.setBackgroundResource(R.drawable.blue_bg3);
            xianjin_btn.setBackgroundResource(R.drawable.blue_bg2);
            zhifubao_btn.setBackgroundResource(R.drawable.blue_bg2);
            huiyuanka_btn.setBackgroundResource(R.drawable.blue_bg2);
            pay_type="wechat";
            shoukuan_tv.setText(new BigDecimal(checkoutBean.getTotal_fee()).subtract(new BigDecimal(yinshou)).toString());
            zhaolin_tv.setText("0.00");

        });
        xianjin_btn.setOnClickListener(v -> {
            huiyuan_view.setVisibility(GONE);
            xianjin_btn.setBackgroundResource(R.drawable.blue_bg3);
            weixin_btn.setBackgroundResource(R.drawable.blue_bg2);
            zhifubao_btn.setBackgroundResource(R.drawable.blue_bg2);
            huiyuanka_btn.setBackgroundResource(R.drawable.blue_bg2);
            pay_type="cash";
            shoukuan_tv.setText(new BigDecimal(checkoutBean.getTotal_fee()).subtract(new BigDecimal(yinshou)).toString());
            zhaolin_tv.setText("0.00");
        });
        zhifubao_btn.setOnClickListener(v -> {
            if (!NetworkUtils.getInstance().isNetworkConnected(context)){
                new DeleteShopPopupWindow(context,context.getString(R.string.Cash_only),true).show();
                return;
            }
            if (zhifubao_type){
                new DeleteShopPopupWindow(context,context.getString(R.string.Alipay_paid_already),true).show();
                return;
            }
            huiyuan_view.setVisibility(GONE);
            xianjin_btn.setBackgroundResource(R.drawable.blue_bg2);
            weixin_btn.setBackgroundResource(R.drawable.blue_bg2);
            zhifubao_btn.setBackgroundResource(R.drawable.blue_bg3);
            huiyuanka_btn.setBackgroundResource(R.drawable.blue_bg2);
            pay_type="alipay";
            shoukuan_tv.setText(new BigDecimal(checkoutBean.getTotal_fee()).subtract(new BigDecimal(yinshou)).toString());
            zhaolin_tv.setText("0.00");
        });
        huiyuanka_btn.setOnClickListener(v -> {
            if (!NetworkUtils.getInstance().isNetworkConnected(context)){
                new DeleteShopPopupWindow(context,context.getString(R.string.Cash_only),true).show();
                return;
            }
            huiyuan_view.setVisibility(VISIBLE);
            huiyuankahao_tv.setText("");
            huiyuanyue_tv.setText("");
            clubCardData=null;
            xianjin_btn.setBackgroundResource(R.drawable.blue_bg2);
            weixin_btn.setBackgroundResource(R.drawable.blue_bg2);
            zhifubao_btn.setBackgroundResource(R.drawable.blue_bg2);
            huiyuanka_btn.setBackgroundResource(R.drawable.blue_bg3);
            pay_type="wallet";
            shoukuan_tv.setText(new BigDecimal(checkoutBean.getTotal_fee()).subtract(new BigDecimal(yinshou)).toString());
            zhaolin_tv.setText("0.00");
            new ClubCardPopupWindow(context, clubCardBean -> {
                clubCardData = clubCardBean.getData().get(0);
                huiyuankahao_tv.setText(clubCardData.getNumber());
                huiyuanyue_tv.setText(clubCardData.getAmount());
            }).show();
        });
        popupView.findViewById(R.id.guanbi_btn).setOnClickListener(v -> {
            popupWindow.dismiss();
        });
        initKey();
        time = new TimeCount(30000, 5000);//一共执行60000毫秒，每5000执行一次。
        huiyuanchaxun_btn.setOnClickListener(v -> {
            new ClubCardPopupWindow(context, clubCardBean -> {
                clubCardData = clubCardBean.getData().get(0);
                huiyuankahao_tv.setText(clubCardData.getNumber());
                huiyuanyue_tv.setText(clubCardData.getAmount());
            }).show();
        });


    }
    public boolean isValidNumber(String input) {
        if (TextUtils.isEmpty(input)) return false;

        String regex = "^\\d+(\\.\\d*)?$";
        if (!input.matches(regex)) return false;

        // 检查小数点数量
        int dotCount = input.length() - input.replace(".", "").length();
        return dotCount <= 1;
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
        if (popupWindow.isShowing()){
            return;
        }
        View rootView = ((Activity) context).getWindow().getDecorView();
        popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, 0, 0);
        pay_type=checkoutBean.getPay_type();
        if (pay_type.equals("cash")){
            xianjin_btn.setBackgroundResource(R.drawable.blue_bg3);
            weixin_btn.setBackgroundResource(R.drawable.blue_bg2);
            zhifubao_btn.setBackgroundResource(R.drawable.blue_bg2);
            huiyuanka_btn.setBackgroundResource(R.drawable.blue_bg2);
            pay_type="cash";
            shoukuan_tv.setText(checkoutBean.getTotal_fee()+"");
        }else {
            weixin_btn.setBackgroundResource(R.drawable.blue_bg3);
            xianjin_btn.setBackgroundResource(R.drawable.blue_bg2);
            zhifubao_btn.setBackgroundResource(R.drawable.blue_bg2);
            huiyuanka_btn.setBackgroundResource(R.drawable.blue_bg2);
            pay_type="wechat";
            shoukuan_tv.setText(checkoutBean.getTotal_fee()+"");


        }
    }
    public void dismiss(){
        popupWindow.dismiss();
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
                                if (v.getTag().toString().equals("-")){
                                    if (!TextUtils.isEmpty(shoukuan_tv.getText().toString())){
                                        return;
                                    }
                                }
                                if (v.getTag().toString().equals(".")){
                                    if (shoukuan_tv.getText().toString().contains(".")){
                                        return;
                                    }
                                }
                                if (!TextUtils.isEmpty(shoukuan_tv.getText().toString())&&isValidDecimal(shoukuan_tv.getText().toString(),2)){
                                    return;
                                }

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
                                    new DeleteShopPopupWindow(context,context.getString(R.string.enter_payment_amount),true).show();
                                    return;
                                }
                                if (!isValidNumber(shoukuan_tv.getText().toString())){
                                    new DeleteShopPopupWindow(context,context.getString(R.string.Please_input_in_correct_price_format),true).show();
                                    return;
                                }


                                if (pay_type.equals("cash")){

                                    checkoutBean.setPay_type(pay_type);
                                    checkoutBean.setPay_fee(shoukuan_tv.getText().toString());
                                    checkoutBean.setCash_price(shoukuan_tv.getText().toString());
                                    checkoutBean.setCash_change(zhaolin_tv.getText().toString());
                                    if (!NetworkUtils.getInstance().isNetworkConnected(context)){
                                        if (new BigDecimal(shoukuan_tv.getText().toString()).subtract(new BigDecimal(checkoutBean.getTotal_fee())).compareTo(BigDecimal.ZERO)<0){
                                            new DeleteShopPopupWindow(context,context.getString(R.string.Cannot_underpay),true).show();
                                            return;
                                        }
                                    }

                                    SubmitCheckout();
                                }else if (pay_type.equals("wallet")){

                                    if (clubCardData == null) {
                                        new DeleteShopPopupWindow(context, "请先查询会员信息", true).show();
                                        return;
                                    }
                                    if (new BigDecimal(shoukuan_tv.getText().toString()).subtract(new BigDecimal(checkoutBean.getTotal_fee())).add(new BigDecimal(yinshou)).compareTo(BigDecimal.ZERO)>0){
                                        new DeleteShopPopupWindow(context,context.getString(R.string.Cannot_overpay),true).show();
                                        return;
                                    }
                                    if (new BigDecimal(clubCardData.getAmount()).subtract(new BigDecimal(shoukuan_tv.getText().toString())).compareTo(BigDecimal.ZERO)<0){
                                        new DeleteShopPopupWindow(context,"会员卡余额不足",true).show();
                                        return;
                                    }
                                    checkoutBean.setPay_type(pay_type);
                                    checkoutBean.setPay_fee(shoukuan_tv.getText().toString());
                                    checkoutBean.setCash_price(shoukuan_tv.getText().toString());
                                    checkoutBean.setCash_change(zhaolin_tv.getText().toString());
                                    checkoutBean.setCardnumber(clubCardData.getNumber());
                                    SubmitCheckout();
                                }else {
                                    if (new BigDecimal(shoukuan_tv.getText().toString()).subtract(new BigDecimal(checkoutBean.getTotal_fee())).add(new BigDecimal(yinshou)).compareTo(BigDecimal.ZERO)>0){
                                        new DeleteShopPopupWindow(context,context.getString(R.string.Cannot_overpay),true).show();
                                        return;
                                    }
                                    shoukuan_tv.removeCallbacks(shoukuan_tvRunnable);
                                    shoukuan_tv.clearFocus();
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
    public static boolean isValidDecimal(String input, int maxDecimalDigits) {
        if (input == null || input.isEmpty()) return false;

        // 校验整体格式（含小数点）
        if (!input.contains(".")) return false;

        // 校验小数位数
        int dotIndex = input.indexOf('.');
        if (dotIndex != -1 && input.substring(dotIndex + 1).length() < maxDecimalDigits) {
            return false;
        }

        return true;
    }
    public String order_sn="";
    public String xinjin_pice="",weixin_pice="",zhifubao_pice="",huiyuanka_pice="";
    public void SubmitCheckout(){
        if (Utilis.isFastClick()){
            return;
        }
        buildBean.show();
        checkoutBean.setOrder_sn(order_sn);
        if (new BigDecimal(shoukuan_tv.getText().toString()).subtract(new BigDecimal(checkoutBean.getTotal_fee())).add(new BigDecimal(yinshou)).compareTo(BigDecimal.ZERO)<0){
            order_status=1;
            checkoutBean.setType(2);
        }else {

            if (order_status==0){
                checkoutBean.setType(1);
            }else {
                checkoutBean.setType(2);
            }
            order_status=2;
        }
        if (!isyouhuijuan){
//            checkoutBean.setMember_name("");
//            checkoutBean.setMember_phone("");
//            checkoutBean.setCardnumber("");
            checkoutBean.setCoupon_fee("");
        }
        checkoutBean.setXf_type("1");
        checkoutBean.setOrder_status(order_status);
        String url = POSApiSerview.POS_URL + POSApiSerview.addOrder;
        Gson gson=new Gson();
        if (!NetworkUtils.getInstance().isNetworkConnected(context)){
            // 定义日期格式模板
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            // 获取当前时间（基于系统时区）
            String formattedTime = sdf.format(System.currentTimeMillis());
            checkoutBean.setPay_time(formattedTime);
            ArrayList<CheckoutBean> checkoutBeans=new ArrayList<>();
            Log.i("ttt","??????1313123??????"+UserUtils.getInstance().getOrderListJson());
            if (!TextUtils.isEmpty(UserUtils.getInstance().getOrderListJson())){
                checkoutBeans = (ArrayList<CheckoutBean>) GsonSandL.getInstance().GsonStoL(UserUtils.getInstance().getOrderListJson(), CheckoutBean.class);
            }
            checkoutBeans.add(checkoutBean);
            UserUtils.getInstance().setOrderListJson(context,gson.toJson(checkoutBeans));
            xinjin_pice=shoukuan_tv.getText().toString();

            deleteShopPopupWindow.dismiss();
            popupWindow.dismiss();
            checkoutOnClickListener.onClick();
            if (!TextUtils.isEmpty(xinjin_pice)){
                MyPrinterHelper.getInstance().asyncOpenMoneyBox(context);
            }
            MyPrinterHelper.getInstance().asyncPrintCheckout(context,checkoutBean,null,xinjin_pice,weixin_pice,zhifubao_pice,order_sn);
            order_sn="";
            out_trade_no="";
            DialogUIUtils.dismiss(buildBean);
        }else {
            RequestBody body = RequestBody.create(gson.toJson(checkoutBean), MediaType.parse("application/json; charset=utf-8"));
            Request.Builder builder = new Request.Builder()
                    .url(url);

            builder.addHeader("token", UserUtils.getInstance().getLoginBase().getData().getUserinfo().getToken());


            builder.post(body);

            Request request = builder.build();
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // 设置日志级别
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(10000, TimeUnit.SECONDS) // 连接超时
                    .readTimeout(10000, TimeUnit.SECONDS)    // 读取超时
                    .writeTimeout(10000, TimeUnit.SECONDS)   // 写入超时
                    .addInterceptor(new NetworkErrorInterceptor()) // 先添加异常拦截器
                    .addInterceptor(loggingInterceptor)   // 添加日志拦截器
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            DialogUIUtils.dismiss(buildBean);
                            new DeleteShopPopupWindow(context, context.getString(R.string.no_network_detected),true).show();
                        }
                    });
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            String success=response.body().string();
                            JSONObject jsonObject=new JSONObject(success);
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {


                                        if (jsonObject.getString("msg").contains("成功")||jsonObject.getString("msg").contains("Success")){
                                            DialogUIUtils.dismiss(buildBean);

                                            if (pay_type.equals("cash")){
                                                if (TextUtils.isEmpty(xinjin_pice)){
                                                    xinjin_pice=shoukuan_tv.getText().toString();
                                                }else {
                                                    xinjin_pice=new BigDecimal(xinjin_pice).add(new BigDecimal(shoukuan_tv.getText().toString())).toString();
                                                }

                                                order_sn=jsonObject.getString("code");

                                            }else if (pay_type.equals("wechat")){

                                                weixin_pice=shoukuan_tv.getText().toString();
                                                weixin_type=true;
                                                order_sn=new JSONObject(jsonObject.getString("code")).getString("order_sn");
                                            }else if (pay_type.equals("alipay")){
                                                zhifubao_pice=shoukuan_tv.getText().toString();
                                                zhifubao_type=true;
                                                order_sn=jsonObject.getString("order_sn");
                                                out_trade_no=jsonObject.getString("out_trade_no");
                                            }else if (pay_type.equals("wallet")){
                                                if (TextUtils.isEmpty(huiyuanka_pice)){
                                                    huiyuanka_pice=shoukuan_tv.getText().toString();
                                                }else {
                                                    huiyuanka_pice=new BigDecimal(huiyuanka_pice).add(new BigDecimal(shoukuan_tv.getText().toString())).toString();
                                                }
                                                order_sn=jsonObject.getString("code");
                                            }
                                            yinshou=new BigDecimal(yinshou).add(new BigDecimal(shoukuan_tv.getText().toString())).toString();
                                            yishou_tv.setText(yinshou);
                                            if (order_status==2){

                                                deleteShopPopupWindow.dismiss();
                                                popupWindow.dismiss();
                                                checkoutOnClickListener.onClick();
                                                if (!TextUtils.isEmpty(xinjin_pice)){
                                                    MyPrinterHelper.getInstance().asyncOpenMoneyBox(context);
                                                }
                                                MyPrinterHelper.getInstance().asyncPrintCheckout(context,checkoutBean,null,xinjin_pice,weixin_pice,zhifubao_pice,huiyuanka_pice,order_sn);
                                                order_sn="";
                                                out_trade_no="";
                                            }else {
                                                new DeleteShopPopupWindow(context,context.getString(R.string.Payment_succeeded),true).show();
                                                deleteShopPopupWindow.dismiss();
                                                xianjin_btn.setBackgroundResource(R.drawable.blue_bg3);
                                                weixin_btn.setBackgroundResource(R.drawable.blue_bg2);
                                                zhifubao_btn.setBackgroundResource(R.drawable.blue_bg2);
                                                huiyuanka_btn.setBackgroundResource(R.drawable.blue_bg2);
                                                pay_type="cash";
                                                huiyuan_view.setVisibility(GONE);
                                                MyPresentation.setDaizhifu_tv(new BigDecimal(checkoutBean.getTotal_fee()).subtract(new BigDecimal(yinshou)).toString());
                                                shoukuan_tv.setText(new BigDecimal(checkoutBean.getTotal_fee()).subtract(new BigDecimal(yinshou)).toString());
                                            }
                                            return;

                                        }
                                        if (jsonObject.getString("msg").contains("失效")){
                                            DialogUIUtils.dismiss(buildBean);
                                            new DeleteShopPopupWindow(context, true, jsonObject.getString("msg"), new PopupWindowOnClickListener.DeleteShopOnClickListener() {
                                                @Override
                                                public void onClick(String text) {
                                                    Intent intent=new Intent(context, LoginActivity.class);
                                                    context.startActivity(intent);
                                                }
                                            }).show();
                                            return;
                                        }
                                        if (jsonObject.getString("msg").contains("输入密码中")||jsonObject.getString("msg").contains("order success pay inprocess")){
                                            if (pay_type.equals("wechat")){
                                                order_sn=new JSONObject(jsonObject.getString("code")).getString("order_sn");
                                                if (new JSONObject(jsonObject.getString("code")).has("out_trade_no")){
                                                    out_trade_no=new JSONObject(jsonObject.getString("code")).getString("out_trade_no");
                                                }else {
                                                    DialogUIUtils.dismiss(buildBean);
                                                    new DeleteShopPopupWindow(context,context.getString(R.string.No_transaction_ID_recorded),true).show();
                                                    return;
                                                }
                                                fwsgetOrderInformation();
                                            }else if (pay_type.equals("alipay")){
                                                out_trade_no=jsonObject.getString("out_trade_no");
                                                order_sn=jsonObject.getString("order_sn");
                                                queryOrder();
                                                time.start();

                                            }
                                            return;
                                        }
                                        DialogUIUtils.dismiss(buildBean);
                                        new DeleteShopPopupWindow(context,context.getString(R.string.Payment_failed)+jsonObject.getString("msg"),true).show();

                                    } catch (JSONException e) {
                                        Log.e("ttt", "Error occurred", e);
                                    }
                                }
                            });

                        } catch (JSONException e) {
                            Log.e("ttt", "Error occurred", e);
                        }
                    } else {

                    }
                }
            });
        }




    }
    public String out_trade_no="";
    public void fwsgetOrderInformation() {
        Map<String, String> params = new HashMap<>();
        params.put("outTradeNo", out_trade_no);
        params.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid()+"");
        FormBody.Builder formBuilder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            formBuilder.add(entry.getKey(), entry.getValue());
        }
        RequestBody formBody = formBuilder.build();
        String url = POSApiSerview.POS_URL + POSApiSerview.fwsgetOrderInformation;
        Request.Builder builder = new Request.Builder()
                .url(url);

        builder.addHeader("token", UserUtils.getInstance().getLoginBase().getData().getUserinfo().getToken());


        builder.post(formBody);

        Request request = builder.build();
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // 设置日志级别
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10000, TimeUnit.SECONDS) // 连接超时
                .readTimeout(10000, TimeUnit.SECONDS)    // 读取超时
                .writeTimeout(10000, TimeUnit.SECONDS)   // 写入超时
                .addInterceptor(new NetworkErrorInterceptor()) // 先添加异常拦截器
                .addInterceptor(loggingInterceptor)   // 添加日志拦截器
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DialogUIUtils.dismiss(buildBean);
                        new DeleteShopPopupWindow(context, context.getString(R.string.no_network_detected),true).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String success=response.body().string();
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    JSONObject jsonObject=new JSONObject(success);

                                    String msg=jsonObject.getString("msg");

                                    String trade_state_desc=new JSONObject(jsonObject.getString("code")).getString("trade_state_desc");
                                    if (trade_state_desc.contains("输入支付密码")){
                                        fwsgetOrderInformation();
                                    }else if (trade_state_desc.contains("支付成功")){
                                        checkoutBean.setTransaction_id(new JSONObject(jsonObject.getString("code")).getString("transaction_id"));
                                        checkoutBean.setOrder_sn(order_sn);
                                        pushorders(checkoutBean);
                                    }else if (trade_state_desc.contains("支付失败")){
                                        fwscancelanOrder();
                                    }else if (trade_state_desc.contains("订单已撤销")){
                                        order_sn="";
                                        out_trade_no="";
                                        DialogUIUtils.dismiss(buildBean);
                                        new DeleteShopPopupWindow(context, context.getString(R.string.order_canceled), true).show();
                                    }


                                } catch (JSONException e) {
                                    Log.e("ttt", "Error occurred", e);
                                }
                            }
                        });

                    } catch (Exception e) {
                        Log.e("ttt", "Error occurred", e);
                    }
                } else {

                }
            }
        });
    }

    public void queryOrder() {
        Map<String, String> params = new HashMap<>();
        params.put("out_trade_no", out_trade_no);
        params.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid()+"");
        FormBody.Builder formBuilder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            formBuilder.add(entry.getKey(), entry.getValue());
        }
        RequestBody formBody = formBuilder.build();
        String url = POSApiSerview.POS_URL + POSApiSerview.queryOrder;
        Request.Builder builder = new Request.Builder()
                .url(url);

        builder.addHeader("token", UserUtils.getInstance().getLoginBase().getData().getUserinfo().getToken());


        builder.post(formBody);

        Request request = builder.build();
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // 设置日志级别
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10000, TimeUnit.SECONDS) // 连接超时
                .readTimeout(10000, TimeUnit.SECONDS)    // 读取超时
                .writeTimeout(10000, TimeUnit.SECONDS)   // 写入超时
                .addInterceptor(new NetworkErrorInterceptor()) // 先添加异常拦截器
                .addInterceptor(loggingInterceptor)   // 添加日志拦截器
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DialogUIUtils.dismiss(buildBean);
                        new DeleteShopPopupWindow(context, context.getString(R.string.no_network_detected),true).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String success=response.body().string();
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    JSONObject jsonObject=new JSONObject(success);

                                    String trade_status=jsonObject.getString("trade_status");
                                    if (is_chaoshi&&!trade_status.contains("TRADE_FINISHED")&&!trade_status.contains("TRADE_SUCCESS")){
                                        is_chaoshi=false;
                                        revokeOrder();
                                        return;
                                    }
                                    if (trade_status.contains("TRADE_FINISHED")||trade_status.contains("TRADE_SUCCESS")){
                                        time.cancel();
                                        checkoutBean.setTransaction_id(jsonObject.getString("trade_no"));
                                        checkoutBean.setOrder_sn(order_sn);
                                        pushorders(checkoutBean);

                                    }
                                    if (trade_status.contains("TRADE_CLOSED")){
                                        order_sn="";
                                        out_trade_no="";
                                        DialogUIUtils.dismiss(buildBean);
                                        new DeleteShopPopupWindow(context, context.getString(R.string.order_canceled), true).show();
                                    }


                                } catch (JSONException e) {
                                    Log.e("ttt", "Error occurred", e);
                                }
                            }
                        });

                    } catch (Exception e) {
                        Log.e("ttt", "Error occurred", e);
                    }
                } else {

                }
            }
        });
    }
    public void fwscancelanOrder() {
        Map<String, String> params = new HashMap<>();
        params.put("outTradeNo", out_trade_no);
        params.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid()+"");
        FormBody.Builder formBuilder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            formBuilder.add(entry.getKey(), entry.getValue());
        }
        RequestBody formBody = formBuilder.build();
        String url = POSApiSerview.POS_URL + POSApiSerview.fwscancelanOrder;
        Request.Builder builder = new Request.Builder()
                .url(url);

        builder.addHeader("token", UserUtils.getInstance().getLoginBase().getData().getUserinfo().getToken());


        builder.post(formBody);

        Request request = builder.build();
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // 设置日志级别
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10000, TimeUnit.SECONDS) // 连接超时
                .readTimeout(10000, TimeUnit.SECONDS)    // 读取超时
                .writeTimeout(10000, TimeUnit.SECONDS)   // 写入超时
                .addInterceptor(new NetworkErrorInterceptor()) // 先添加异常拦截器
                .addInterceptor(loggingInterceptor)   // 添加日志拦截器
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DialogUIUtils.dismiss(buildBean);
                        new DeleteShopPopupWindow(context, context.getString(R.string.no_network_detected),true).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String success=response.body().string();
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                fwsgetOrderInformation();
                            }
                        });

                    } catch (Exception e) {
                        Log.e("ttt", "Error occurred", e);
                    }
                } else {

                }
            }
        });
    }
    public void revokeOrder() {
        Map<String, String> params = new HashMap<>();
        params.put("out_trade_no", out_trade_no);
        params.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid()+"");
        FormBody.Builder formBuilder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            formBuilder.add(entry.getKey(), entry.getValue());
        }
        RequestBody formBody = formBuilder.build();
        String url = POSApiSerview.POS_URL + POSApiSerview.revokeOrder;
        Request.Builder builder = new Request.Builder()
                .url(url);

        builder.addHeader("token", UserUtils.getInstance().getLoginBase().getData().getUserinfo().getToken());


        builder.post(formBody);

        Request request = builder.build();
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // 设置日志级别
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10000, TimeUnit.SECONDS) // 连接超时
                .readTimeout(10000, TimeUnit.SECONDS)    // 读取超时
                .writeTimeout(10000, TimeUnit.SECONDS)   // 写入超时
                .addInterceptor(new NetworkErrorInterceptor()) // 先添加异常拦截器
                .addInterceptor(loggingInterceptor)   // 添加日志拦截器
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DialogUIUtils.dismiss(buildBean);
                        new DeleteShopPopupWindow(context, context.getString(R.string.no_network_detected),true).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String success=response.body().string();
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                order_sn="";
                                out_trade_no="";
                                DialogUIUtils.dismiss(buildBean);
                                new DeleteShopPopupWindow(context, context.getString(R.string.order_canceled), true).show();
                            }
                        });

                    } catch (Exception e) {
                        Log.e("ttt", "Error occurred", e);
                    }
                } else {

                }
            }
        });
    }
    public void pushorders(CheckoutBean checkoutBean) {
        String url = POSApiSerview.POS_URL + POSApiSerview.pushorders;
        Gson gson = new Gson();
        RequestBody body = RequestBody.create(gson.toJson(checkoutBean), MediaType.parse("application/json; charset=utf-8"));
        Request.Builder builder = new Request.Builder()
                .url(url);

        builder.addHeader("token", UserUtils.getInstance().getLoginBase().getData().getUserinfo().getToken());


        builder.post(body);

        Request request = builder.build();
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // 设置日志级别
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10000, TimeUnit.SECONDS) // 连接超时
                .readTimeout(10000, TimeUnit.SECONDS)    // 读取超时
                .writeTimeout(10000, TimeUnit.SECONDS)   // 写入超时
                .addInterceptor(new NetworkErrorInterceptor()) // 先添加异常拦截器
                .addInterceptor(loggingInterceptor)   // 添加日志拦截器
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DialogUIUtils.dismiss(buildBean);
                        new DeleteShopPopupWindow(context, context.getString(R.string.no_network_detected),true).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String success = response.body().string();
                        JSONObject jsonObject = new JSONObject(success);
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (jsonObject.getString("msg").contains("成功") || jsonObject.getString("msg").contains("Success")) {
                                        DialogUIUtils.dismiss(buildBean);

                                        if (pay_type.equals("wechat")){
                                            weixin_pice=shoukuan_tv.getText().toString();
                                            weixin_type=true;
                                        }
                                        if (pay_type.equals("alipay")){
                                            zhifubao_pice=shoukuan_tv.getText().toString();
                                            zhifubao_type=true;
                                        }
                                        yinshou=new BigDecimal(yinshou).add(new BigDecimal(shoukuan_tv.getText().toString())).toString();
                                        yishou_tv.setText(yinshou);

                                        if (order_status==2){

                                            deleteShopPopupWindow.dismiss();
                                            popupWindow.dismiss();
                                            checkoutOnClickListener.onClick();

                                            MyPrinterHelper.getInstance().asyncPrintCheckout(context,checkoutBean,null,xinjin_pice,weixin_pice,zhifubao_pice,huiyuanka_pice,order_sn);
                                            order_sn="";
                                            out_trade_no="";
                                        }else {
                                            new DeleteShopPopupWindow(context,context.getString(R.string.Payment_succeeded),true).show();
                                            deleteShopPopupWindow.dismiss();
                                            xianjin_btn.setBackgroundResource(R.drawable.blue_bg3);
                                            weixin_btn.setBackgroundResource(R.drawable.blue_bg2);
                                            zhifubao_btn.setBackgroundResource(R.drawable.blue_bg2);
                                            huiyuanka_btn.setBackgroundResource(R.drawable.blue_bg2);
                                            pay_type="cash";
                                            huiyuan_view.setVisibility(GONE);
                                            MyPresentation.setDaizhifu_tv(new BigDecimal(checkoutBean.getTotal_fee()).subtract(new BigDecimal(yinshou)).toString());
                                            shoukuan_tv.setText(new BigDecimal(checkoutBean.getTotal_fee()).subtract(new BigDecimal(yinshou)).toString());
                                        }



                                    }

                                } catch (JSONException e) {
                                    Log.e("ttt", "Error occurred", e);
                                }
                            }
                        });

                    } catch (JSONException e) {
                        Log.e("ttt", "Error occurred", e);
                    }
                } else {

                }
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
                                MyPrinterHelper.getInstance().asyncPrintCheckout(context,checkoutBean,printDataBeanArrayList.get(0),xinjin_pice,weixin_pice,zhifubao_pice,order_sn);
                            }else {
                                MyPrinterHelper.getInstance().asyncPrintCheckout(context,checkoutBean,null,xinjin_pice,weixin_pice,zhifubao_pice,order_sn);
                            }
                        } catch (JSONException e) {
                            Log.e("ttt", "Error occurred", e);
                        }
                    }
                });
            }

            @Override
            public void onFailure(IOException e) {

            }
        });
    }
    private TimeCount time;
    private boolean is_chaoshi=false;
    class TimeCount extends CountDownTimer {


        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        //时间定时器运行过程调用此方法。millisUntilFinished   为剩余时间
        @Override
        public void onTick(long millisUntilFinished) {

            queryOrder();

        }

        //时间定时器结束调用此方法
        @Override
        public void onFinish() {
            is_chaoshi=true;
            queryOrder();
        }
    }

}
