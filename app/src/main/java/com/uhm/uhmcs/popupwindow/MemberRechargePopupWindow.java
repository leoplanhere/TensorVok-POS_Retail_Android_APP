package com.uhm.uhmcs.popupwindow;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
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

import com.dou361.dialogui.DialogUIUtils;
import com.dou361.dialogui.bean.BuildBean;
import com.google.gson.Gson;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.activity.LoginActivity;
import com.uhm.uhmcs.activity.MainActivity;
import com.uhm.uhmcs.bean.CheckoutBean;
import com.uhm.uhmcs.bean.ClubCardBean;
import com.uhm.uhmcs.bean.CustomRechargeBean;
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

public class MemberRechargePopupWindow {
    private PopupWindow popupWindow;
    private Activity context;
    private PopupWindowOnClickListener.DeleteShopOnClickListener deleteShopOnClickListener;

    private TextView weixin_btn, xianjin_btn, zhifubao_btn;
    private DeleteShopPopupWindow deleteShopPopupWindow;
    TextView huiyuankahao_tv ;
    TextView huiyuanyue_tv ;

    public MemberRechargePopupWindow(Activity context, PopupWindowOnClickListener.DeleteShopOnClickListener deleteShopOnClickListener) {
        this.context = context;
        this.deleteShopOnClickListener = deleteShopOnClickListener;
        initPopup();
    }

    ClubCardBean.DataBean clubCardData;
    CustomInputTextView chongzhijine_tv;
    View popupView;
    String pay_type = "wechat";

    private void initPopup() {
        popupView = LayoutInflater.from(context).inflate(R.layout.popupwindow_member_recharge, null);
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
        TextView hint_tv = popupView.findViewById(R.id.hint_tv);
         huiyuankahao_tv = popupView.findViewById(R.id.huiyuankahao_tv);
         huiyuanyue_tv = popupView.findViewById(R.id.huiyuanyue_tv);
        chongzhijine_tv = popupView.findViewById(R.id.chongzhijine_tv);

        popupView.findViewById(R.id.guanbi_btn).setOnClickListener(v -> {
            popupWindow.dismiss();
        });
        popupView.findViewById(R.id.huiyuanchaxun_btn).setOnClickListener(v -> {
            new ClubCardPopupWindow(context, clubCardBean -> {
                // 自动获取焦点
                chongzhijine_tv.postDelayed(() -> chongzhijine_tv.requestFocus(), 100);
                clubCardData = clubCardBean.getData().get(0);
                huiyuankahao_tv.setText(clubCardData.getNumber());
                huiyuanyue_tv.setText(clubCardData.getAmount());
            }).show();
        });
        weixin_btn = popupView.findViewById(R.id.weixin_btn);
        zhifubao_btn = popupView.findViewById(R.id.zhifubao_btn);

        xianjin_btn = popupView.findViewById(R.id.xianjin_btn);


        weixin_btn.setOnClickListener(v -> {
//            if (!NetworkUtils.getInstance().isNetworkConnected(context)){
//                new DeleteShopPopupWindow(context,"没有网络可用只能现金充值",true).show();
//                return;
//            }

            weixin_btn.setBackgroundResource(R.drawable.blue_bg3);
            xianjin_btn.setBackgroundResource(R.drawable.blue_bg2);
            zhifubao_btn.setBackgroundResource(R.drawable.blue_bg2);
            pay_type = "wechat";


        });
        xianjin_btn.setOnClickListener(v -> {
            xianjin_btn.setBackgroundResource(R.drawable.blue_bg3);
            weixin_btn.setBackgroundResource(R.drawable.blue_bg2);
            zhifubao_btn.setBackgroundResource(R.drawable.blue_bg2);
            pay_type = "cash";

        });
        zhifubao_btn.setOnClickListener(v -> {
//            if (!NetworkUtils.getInstance().isNetworkConnected(context)){
//                new DeleteShopPopupWindow(context,context.getString(R.string.Cash_only),true).show();
//                return;
//            }

            xianjin_btn.setBackgroundResource(R.drawable.blue_bg2);
            weixin_btn.setBackgroundResource(R.drawable.blue_bg2);
            zhifubao_btn.setBackgroundResource(R.drawable.blue_bg3);
            pay_type = "alipay";

        });
        chongzhijine_tv.setOnInputCompleteListener(text -> {


            submitChongzhi();
        });
        initKey();
        deleteShopPopupWindow = new DeleteShopPopupWindow(context, context.getString(R.string.Scan_to_pay), false, new PopupWindowOnClickListener.DeleteShopOnClickListener() {
            @Override
            public void onClick(String text) {
                chongzhijine_tv.postDelayed(() -> chongzhijine_tv.requestFocus(), 100);
                Log.i("ttt", ">>>1112>>>>支付码>" + text);
                if (TextUtils.isEmpty(text)) {
                    return;
                }
                String type = detectPaymentType(text);
                if (type.equals("unknown") || !pay_type.equals(type)) {
                    new DeleteShopPopupWindow(context, context.getString(R.string.Scan_payment_QR_code), true).show();
                    return;
                }
                authCode=text;
                customRecharge();
            }
        });
        buildBean=DialogUIUtils.showLoading(context,context.getString(R.string.paying),true,false,false,false);
        time = new TimeCount(30000, 5000);//一共执行60000毫秒，每5000执行一次。
        customRechargeBean=new CustomRechargeBean();
    }
    String authCode="";

    public void show() {
        if (popupWindow.isShowing()) {
            return;
        }

        View rootView = ((Activity) context).getWindow().getDecorView();
        popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, 0, 0);
//        // 自动获取焦点
        chongzhijine_tv.postDelayed(() -> chongzhijine_tv.requestFocus(), 100);

    }

    public void dismiss() {
        popupWindow.dismiss();
    }

    @SuppressLint("SetTextI18n")
    private void initKey() {
        try {
            LinearLayout llkeyArea = (LinearLayout) popupView.findViewById(R.id.llkeyArea);
            for (int i = 0; i < llkeyArea.getChildCount(); i++) {
                if (i == 0) {
                    LinearLayout numview = (LinearLayout) llkeyArea.getChildAt(0);
                    for (int j = 0; j < numview.getChildCount(); j++) {
                        LinearLayout llNum01_09 = (LinearLayout) numview.getChildAt(j);

                        for (int u = 0; u < llNum01_09.getChildCount(); u++) {
                            llNum01_09.getChildAt(u).setOnClickListener(v -> {
                                Log.i("ttt", v.getTag().toString());
                                if (v.getTag().toString().equals("-")) {
                                    if (!TextUtils.isEmpty(chongzhijine_tv.getText().toString())) {
                                        return;
                                    }
                                }
                                if (v.getTag().toString().equals(".")) {
                                    if (chongzhijine_tv.getText().toString().contains(".")) {
                                        return;
                                    }
                                }
                                if (!TextUtils.isEmpty(chongzhijine_tv.getText().toString()) && isValidDecimal(chongzhijine_tv.getText().toString(), 2)) {
                                    return;
                                }

                                chongzhijine_tv.append(v.getTag().toString());
                            });
                        }


                    }
                }


                if (i == 1) {
                    LinearLayout d_c_submit_view = (LinearLayout) llkeyArea.getChildAt(1);
                    for (int j = 0; j < d_c_submit_view.getChildCount(); j++) {
                        d_c_submit_view.getChildAt(j).setOnClickListener(v -> {
                            if (v.getTag().toString().equals("c")) {
                                chongzhijine_tv.setText("");
                            } else if (v.getTag().toString().equals("d")) {
                                if (!chongzhijine_tv.getText().toString().isEmpty()) {
                                    chongzhijine_tv.setText(chongzhijine_tv.getText().toString().substring(0, chongzhijine_tv.getText().toString().length() - 1));
                                }

                            } else if (v.getTag().toString().equals("submit")) {
                                submitChongzhi();
                            }
                        });
                    }
                }
            }

        } catch (Exception ex) {
            Log.i("错误返回", ex.getMessage() + "");
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
     *
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

    private void submitChongzhi() {



        if (clubCardData == null) {
            new DeleteShopPopupWindow(context, "请先查询会员信息", true).show();
            return;
        }
        if (TextUtils.isEmpty(chongzhijine_tv.getText().toString())) {
            new DeleteShopPopupWindow(context, "请输入充值金额", true).show();
            return;
        }
        if (new BigDecimal(chongzhijine_tv.getText().toString()).compareTo(BigDecimal.ZERO) <= 0) {
            new DeleteShopPopupWindow(context, "充值金额必须大于0", true).show();
            return;
        }

        new DeleteShopPopupWindow(true, context, "输入密码", new PopupWindowOnClickListener.DeleteShopOnClickListener() {
            @Override
            public void onClick(String text) {
                if (pay_type.equals("cash")){
                    customRecharge();
                }else {
                    deleteShopPopupWindow.show();
                }

            }
        }).show();



    }
    BuildBean buildBean;
    CustomRechargeBean customRechargeBean;
    public void customRecharge() {
        if (Utilis.isFastClick()) {
            return;
        }
        buildBean.show();

        customRechargeBean.setCardnumber(clubCardData.getNumber());
        customRechargeBean.setAuthCode(authCode);
        customRechargeBean.setMachineNumber("001");
        customRechargeBean.setShop_id(UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid());
        customRechargeBean.setPay_type(pay_type);
        customRechargeBean.setUser_id(UserUtils.getInstance().getLoginBase().getData().getUserinfo().getId()+"");
        customRechargeBean.setPay_fee(chongzhijine_tv.getText().toString());
        customRechargeBean.setType("1");
        customRechargeBean.setXf_type("2");
        customRechargeBean.setOrder_status("2");
        String url = POSApiSerview.POS_URL + POSApiSerview.customRecharge;
        Gson gson = new Gson();
        RequestBody body = RequestBody.create(gson.toJson(customRechargeBean), MediaType.parse("application/json; charset=utf-8"));
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

                                        new DeleteShopPopupWindow(context,"充值成功",true).show();
                                        if (pay_type.equals("cash")){
                                            MyPrinterHelper.getInstance().asyncOpenMoneyBox(context);
                                        }else {
                                            deleteShopPopupWindow.dismiss();
                                        }
                                        MyPrinterHelper.getInstance().asyncPrintCheckout(context,customRechargeBean.getPay_fee(),pay_type);

                                        chongzhijine_tv.setText("");
                                        getCustomBlock();
//                                        popupWindow.dismiss();
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
                                    new DeleteShopPopupWindow(context,"充值失败"+jsonObject.getString("msg"),true).show();

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
    public void getCustomBlock(){
        Map<String, String> params = new HashMap<>();
        params.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid());
        params.put("cardnumber",clubCardData.getNumber());
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
                                clubCardData=clubCardBean.getData().get(0);
                                huiyuankahao_tv.setText(clubCardData.getNumber());
                                huiyuanyue_tv.setText(clubCardData.getAmount());
                            }else {
                                new DeleteShopPopupWindow(context,jsonObject.getString("msg"),true).show();
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
    public String out_trade_no="";
    public String order_sn="";
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
                                        customRechargeBean.setTransaction_id(new JSONObject(jsonObject.getString("code")).getString("transaction_id"));
                                        customRechargeBean.setOrder_sn(order_sn);
                                        pushOrderszh();
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
                                        customRechargeBean.setTransaction_id(jsonObject.getString("trade_no"));
                                        customRechargeBean.setOrder_sn(order_sn);
                                        pushOrderszh();

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
    public void pushOrderszh() {
        String url = POSApiSerview.POS_URL + POSApiSerview.pushOrderszh;
        Gson gson = new Gson();
        RequestBody body = RequestBody.create(gson.toJson(customRechargeBean), MediaType.parse("application/json; charset=utf-8"));
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

                                        new DeleteShopPopupWindow(context,"充值成功",true).show();
                                        deleteShopPopupWindow.dismiss();
                                        MyPrinterHelper.getInstance().asyncPrintCheckout(context,customRechargeBean.getPay_fee(),pay_type);

                                        chongzhijine_tv.setText("");
                                        getCustomBlock();


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
