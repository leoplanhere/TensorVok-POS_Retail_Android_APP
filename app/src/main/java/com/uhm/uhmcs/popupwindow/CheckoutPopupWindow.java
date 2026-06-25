package com.uhm.uhmcs.popupwindow;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
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
import android.view.KeyEvent;
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
import com.uhm.uhmcs.bean.LastOrderBean;
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
    private static final String TAG = "CheckoutPopup";
    private static final long POLL_INTERVAL_MS = 5000L;
    private static final long POLL_TIMEOUT_DOMESTIC_MS = 60000L;
    private static final long POLL_TIMEOUT_FOREIGN_MS = 120000L;

    private PopupWindow popupWindow;
    private Activity context;
    private CheckoutBean checkoutBean;
    private TextView yingshou_tv,youhui_tv,shijishou_tv,weixin_btn,xianjin_btn,zhaolin_tv,zhifubao_btn,yishou_tv,tvForeignPaymentHint;
    private CustomInputTextView shoukuan_tv;
    private DeleteShopPopupWindow deleteShopPopupWindow;
    private String yinshou="0.00";

    private String pay_type="";
    private int order_status=0;
    private boolean isForeignPayment = false;
    private boolean isPollingActive = false;
    private final Handler pollingHandler = new Handler(Looper.getMainLooper());

    private boolean weixin_type=false;
    private boolean zhifubao_type=false;


    // ★★★ 新增变量：记录最后一次按数字键的时间
    private long lastNumericKeyTime = 0;

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
        yishou_tv.setText("￥0.00");
        yingshou_tv.setText("￥"+checkoutBean.getTotal_amount());
        shijishou_tv.setText("￥"+checkoutBean.getTotal_fee());
        youhui_tv.setText("￥"+checkoutBean.getDiscount_fee());
        shoukuan_tv=popupView.findViewById(R.id.shoukuan_tv);


        // ▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼ 新增拦截逻辑开始 ▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼
        shoukuan_tv.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {

                    // 1. 如果是数字键，记录当前时间
                    // 扫码枪输入数字的速度极快（通常几毫秒一个）
                    if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
                        lastNumericKeyTime = System.currentTimeMillis();
                        return false; // 不拦截数字，让它正常处理（虽然你说金额没变，但逻辑上不能断）
                    }

                    // 2. 拦截回车键
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        // 计算距离上一次按数字键过了多久
                        long interval = System.currentTimeMillis() - lastNumericKeyTime;

                        // 判断条件：
                        // A. 当前是现金模式
                        // B. 距离上一次数字输入非常近（< 100ms），说明是扫码枪连着发过来的回车
                        //    (人类手动输入通常慢于 200ms，或者按完数字会停顿一下再按回车)
                        if ("cash".equals(pay_type) && interval < 200) {

                            // 弹窗提示
                            new DeleteShopPopupWindow(context, "现金支付无法扫码", true).show();

                            // ★★★ 关键：返回 true，表示“这个回车键我消费掉了”，不再传给后面的 InputCompleteListener
                            return true;
                        }
                    }
                }
                return false;
            }
        });
// ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲ 新增拦截逻辑结束 ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲



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
        tvForeignPaymentHint = popupView.findViewById(R.id.tv_foreign_payment_hint);

        xianjin_btn=popupView.findViewById(R.id.xianjin_btn);
        weixin_btn.setOnClickListener(v -> {
            if (!NetworkUtils.getInstance().isNetworkConnected(context)){
                new DeleteShopPopupWindow(context,context.getString(R.string.Cash_only),true).show();
                return;
            }

            if (weixin_type){
                new DeleteShopPopupWindow(context,context.getString(R.string.WeChat_paid_already),true).show();
                return;
            }
            weixin_btn.setBackgroundResource(R.drawable.blue_bg3);
            xianjin_btn.setBackgroundResource(R.drawable.blue_bg2);
            zhifubao_btn.setBackgroundResource(R.drawable.blue_bg2);
            pay_type="wechat";
            shoukuan_tv.setText(new BigDecimal(checkoutBean.getTotal_fee()).subtract(new BigDecimal(yinshou)).toString());
            zhaolin_tv.setText("0.00");

        });
        xianjin_btn.setOnClickListener(v -> {
            xianjin_btn.setBackgroundResource(R.drawable.blue_bg3);
            weixin_btn.setBackgroundResource(R.drawable.blue_bg2);
            zhifubao_btn.setBackgroundResource(R.drawable.blue_bg2);
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
            xianjin_btn.setBackgroundResource(R.drawable.blue_bg2);
            weixin_btn.setBackgroundResource(R.drawable.blue_bg2);
            zhifubao_btn.setBackgroundResource(R.drawable.blue_bg3);
            pay_type="alipay";
            shoukuan_tv.setText(new BigDecimal(checkoutBean.getTotal_fee()).subtract(new BigDecimal(yinshou)).toString());
            zhaolin_tv.setText("0.00");
        });
        popupView.findViewById(R.id.guanbi_btn).setOnClickListener(v -> {
            popupWindow.dismiss();
        });
        initKey();
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
            pay_type="cash";
            shoukuan_tv.setText(checkoutBean.getTotal_fee()+"");
        }else {
            weixin_btn.setBackgroundResource(R.drawable.blue_bg3);
            xianjin_btn.setBackgroundResource(R.drawable.blue_bg2);
            zhifubao_btn.setBackgroundResource(R.drawable.blue_bg2);
            pay_type="wechat";
            shoukuan_tv.setText(checkoutBean.getTotal_fee()+"");

        }
    }


    public boolean isShowing() {
        if (popupWindow != null) {
            return popupWindow.isShowing();
        }
        return false;
    }

    public void dismiss() {
        stopPaymentPolling();
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
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
    public String xinjin_pice="",weixin_pice="",zhifubao_pice="";
    public void SubmitCheckout(){
        if (Utilis.isFastClick()){
            return;
        }

        // ★ 新增日志打印：查看整个订单对象的 JSON
        Log.e("SUBMIT_DEBUG", "提交给后端的全量数据: " + new Gson().toJson(checkoutBean));


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
                    .connectTimeout(100, TimeUnit.SECONDS) // 连接超时
                    .readTimeout(100, TimeUnit.SECONDS)    // 读取超时
                    .writeTimeout(100, TimeUnit.SECONDS)   // 写入超时
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


                                        if (isPaymentImmediateSuccess(jsonObject.optString("msg"))){
                                            DialogUIUtils.dismiss(buildBean);

                                            if (pay_type.equals("cash")){
                                                if (TextUtils.isEmpty(xinjin_pice)){
                                                    xinjin_pice=shoukuan_tv.getText().toString();
                                                }else {
                                                    xinjin_pice=new BigDecimal(xinjin_pice).add(new BigDecimal(shoukuan_tv.getText().toString())).toString();
                                                }

                                                order_sn=jsonObject.getString("data");

                                            }else if (pay_type.equals("wechat")){

                                                weixin_pice=shoukuan_tv.getText().toString();
                                                weixin_type=true;
                                                order_sn=new JSONObject(jsonObject.getString("code")).getString("order_sn");
                                            }else if (pay_type.equals("alipay")){
                                                zhifubao_pice=shoukuan_tv.getText().toString();
                                                zhifubao_type=true;
                                                order_sn=jsonObject.getString("order_sn");
                                                out_trade_no=jsonObject.getString("out_trade_no");
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
                                                MyPrinterHelper.getInstance().asyncPrintCheckout(context,checkoutBean,null,xinjin_pice,weixin_pice,zhifubao_pice,order_sn);
                                                order_sn="";
                                                out_trade_no="";
                                            }else {
                                                new DeleteShopPopupWindow(context,context.getString(R.string.Payment_succeeded),true).show();
                                                deleteShopPopupWindow.dismiss();
                                                xianjin_btn.setBackgroundResource(R.drawable.blue_bg3);
                                                weixin_btn.setBackgroundResource(R.drawable.blue_bg2);
                                                zhifubao_btn.setBackgroundResource(R.drawable.blue_bg2);
                                                pay_type="cash";
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
                                        if (isPaymentInProcess(jsonObject)) {
                                            isForeignPayment = detectForeignPayment(jsonObject);
                                            if (isForeignPayment) {
                                                DialogUIUtils.dismiss(buildBean);
                                                showForeignPaymentHint();
                                            } else {
                                                hideForeignPaymentHint();
                                            }
                                            if (pay_type.equals("wechat")) {
                                                if (!extractWechatTradeInfo(jsonObject)) {
                                                    DialogUIUtils.dismiss(buildBean);
                                                    new DeleteShopPopupWindow(context, context.getString(R.string.No_transaction_ID_recorded), true).show();
                                                    return;
                                                }
                                                startPaymentPolling(false);
                                                scheduleWechatPoll(1000);
                                            } else if (pay_type.equals("alipay")) {
                                                if (!extractAlipayTradeInfo(jsonObject)) {
                                                    DialogUIUtils.dismiss(buildBean);
                                                    new DeleteShopPopupWindow(context, context.getString(R.string.No_transaction_ID_recorded), true).show();
                                                    return;
                                                }
                                                startPaymentPolling(isForeignPayment);
                                            }
                                            return;
                                        }
                                        DialogUIUtils.dismiss(buildBean);
                                        new DeleteShopPopupWindow(context,context.getString(R.string.Payment_failed)+jsonObject.getString("msg"),true).show();

                                    } catch (JSONException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            });

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    } else {

                    }
                }
            });
        }




    }
    public String out_trade_no="";
    public void fwsgetOrderInformation() {
        if (!isPollingActive) return;

        Map<String, String> params = new HashMap<>();
        params.put("outTradeNo", out_trade_no);
        params.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid() + "");
        FormBody.Builder formBuilder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            formBuilder.add(entry.getKey(), entry.getValue());
        }
        RequestBody formBody = formBuilder.build();
        String url = POSApiSerview.POS_URL + POSApiSerview.fwsgetOrderInformation;
        Request.Builder builder = new Request.Builder().url(url);
        builder.addHeader("token", UserUtils.getInstance().getLoginBase().getData().getUserinfo().getToken());
        builder.post(formBody);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(new NetworkErrorInterceptor())
                .build();
        client.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.w(TAG, "微信轮询网络失败，3秒后重试: " + e.getMessage());
                scheduleWechatPoll(3000);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    scheduleWechatPoll(3000);
                    return;
                }
                try {
                    String success = response.body().string();
                    JSONObject jsonObject = new JSONObject(success);
                    context.runOnUiThread(() -> handleWechatPollResponse(jsonObject));
                } catch (Exception e) {
                    Log.e(TAG, "微信轮询解析失败", e);
                    scheduleWechatPoll(3000);
                }
            }
        });
    }

    private void handleWechatPollResponse(JSONObject jsonObject) {
        if (!isPollingActive) return;
        try {
            JSONObject codeObj = jsonObject.optJSONObject("code");
            String tradeState = codeObj != null ? codeObj.optString("trade_state", "") : "";
            String tradeStateDesc = codeObj != null ? codeObj.optString("trade_state_desc", "") : "";
            String stateText = (tradeState + " " + tradeStateDesc).toUpperCase();

            if (stateText.contains("USERPAYING") || tradeStateDesc.contains("密码") || tradeStateDesc.contains("支付密码")) {
                scheduleWechatPoll(1000);
            } else if (stateText.contains("SUCCESS") || tradeStateDesc.contains("支付成功")) {
                stopPaymentPolling();
                if (codeObj != null) {
                    checkoutBean.setTransaction_id(codeObj.optString("transaction_id"));
                }
                checkoutBean.setOrder_sn(order_sn);
                pushorders(checkoutBean);
            } else if (stateText.contains("PAYERROR") || tradeStateDesc.contains("支付失败")) {
                stopPaymentPolling();
                fwscancelanOrder();
            } else if (stateText.contains("REVOKED") || tradeStateDesc.contains("订单已撤销")) {
                stopPaymentPolling();
                order_sn = "";
                out_trade_no = "";
                DialogUIUtils.dismiss(buildBean);
                hideForeignPaymentHint();
                new DeleteShopPopupWindow(context, context.getString(R.string.order_canceled), true).show();
            } else {
                scheduleWechatPoll(3000);
            }
        } catch (Exception e) {
            Log.e(TAG, "处理微信轮询结果异常", e);
            scheduleWechatPoll(3000);
        }
    }

    public void queryOrder() {
        queryOrder(false);
    }

    private void queryOrder(boolean finalAttempt) {
        if (!isPollingActive && !finalAttempt) return;

        Map<String, String> params = new HashMap<>();
        params.put("out_trade_no", out_trade_no);
        params.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid() + "");
        String url = POSApiSerview.POS_URL + POSApiSerview.queryOrder;

        OkHttpUtil.postFormAsync(url, params, context, new OkHttpUtil.OkHttpCallback() {
            @Override
            public void onSuccess(String response) {
                context.runOnUiThread(() -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (isAlipayTradeSuccess(jsonObject)) {
                            stopPaymentPolling();
                            is_chaoshi = false;
                            checkoutBean.setTransaction_id(jsonObject.optString("trade_no"));
                            checkoutBean.setOrder_sn(order_sn);
                            pushorders(checkoutBean);
                            return;
                        }

                        String tradeStatus = jsonObject.optString("trade_status", "").toUpperCase();
                        if (tradeStatus.contains("TRADE_CLOSED")) {
                            stopPaymentPolling();
                            order_sn = "";
                            out_trade_no = "";
                            DialogUIUtils.dismiss(buildBean);
                            hideForeignPaymentHint();
                            new DeleteShopPopupWindow(context, context.getString(R.string.order_canceled), true).show();
                            return;
                        }

                        if (finalAttempt || is_chaoshi) {
                            is_chaoshi = false;
                            verifyPaymentResult();
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "支付宝轮询解析失败", e);
                        if (finalAttempt || is_chaoshi) {
                            verifyPaymentResult();
                        }
                    }
                });
            }

            @Override
            public void onFailure(IOException e) {
                Log.w(TAG, "支付宝轮询网络失败: " + e.getMessage());
                if (finalAttempt || is_chaoshi) {
                    context.runOnUiThread(() -> verifyPaymentResult());
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
                .connectTimeout(100, TimeUnit.SECONDS) // 连接超时
                .readTimeout(100, TimeUnit.SECONDS)    // 读取超时
                .writeTimeout(100, TimeUnit.SECONDS)   // 写入超时
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
                        throw new RuntimeException(e);
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
                .connectTimeout(100, TimeUnit.SECONDS) // 连接超时
                .readTimeout(100, TimeUnit.SECONDS)    // 读取超时
                .writeTimeout(100, TimeUnit.SECONDS)   // 写入超时
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
                                queryOrder();
                            }
                        });

                    } catch (Exception e) {
                        throw new RuntimeException(e);
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
                .connectTimeout(100, TimeUnit.SECONDS) // 连接超时
                .readTimeout(100, TimeUnit.SECONDS)    // 读取超时
                .writeTimeout(100, TimeUnit.SECONDS)   // 写入超时
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
                                        stopPaymentPolling();
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

                                            MyPrinterHelper.getInstance().asyncPrintCheckout(context,checkoutBean,null,xinjin_pice,weixin_pice,zhifubao_pice,order_sn);
                                            order_sn="";
                                            out_trade_no="";
                                        }else {
                                            new DeleteShopPopupWindow(context,context.getString(R.string.Payment_succeeded),true).show();
                                            deleteShopPopupWindow.dismiss();
                                            xianjin_btn.setBackgroundResource(R.drawable.blue_bg3);
                                            weixin_btn.setBackgroundResource(R.drawable.blue_bg2);
                                            zhifubao_btn.setBackgroundResource(R.drawable.blue_bg2);
                                            pay_type="cash";
                                            MyPresentation.setDaizhifu_tv(new BigDecimal(checkoutBean.getTotal_fee()).subtract(new BigDecimal(yinshou)).toString());
                                            shoukuan_tv.setText(new BigDecimal(checkoutBean.getTotal_fee()).subtract(new BigDecimal(yinshou)).toString());
                                        }



                                    }

                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
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
    private TimeCount time;
    private boolean is_chaoshi = false;
    private final Runnable wechatPollRunnable = this::fwsgetOrderInformation;

    private void startPaymentPolling(boolean foreign) {
        isForeignPayment = foreign;
        isPollingActive = true;
        is_chaoshi = false;
        if (time != null) {
            time.cancel();
        }
        long timeout = foreign ? POLL_TIMEOUT_FOREIGN_MS : POLL_TIMEOUT_DOMESTIC_MS;
        time = new TimeCount(timeout, POLL_INTERVAL_MS);
        if ("alipay".equals(pay_type)) {
            queryOrder(false);
        }
        time.start();
    }

    private void stopPaymentPolling() {
        isPollingActive = false;
        pollingHandler.removeCallbacks(wechatPollRunnable);
        if (time != null) {
            time.cancel();
        }
        hideForeignPaymentHint();
    }

    private void scheduleWechatPoll(long delayMs) {
        if (!isPollingActive) return;
        pollingHandler.removeCallbacks(wechatPollRunnable);
        pollingHandler.postDelayed(wechatPollRunnable, delayMs);
    }

    private void showForeignPaymentHint() {
        if (tvForeignPaymentHint != null) {
            tvForeignPaymentHint.setText(context.getString(R.string.foreign_payment_hint));
            tvForeignPaymentHint.setVisibility(View.VISIBLE);
        }
    }

    private void hideForeignPaymentHint() {
        if (tvForeignPaymentHint != null) {
            tvForeignPaymentHint.setVisibility(View.GONE);
        }
    }

    /** 供 MainActivity 快捷扫码支付时显示境外支付提示 */
    public void showForeignPaymentHintUi() {
        showForeignPaymentHint();
    }

    public void showPaymentVerifyHintUi() {
        if (tvForeignPaymentHint != null) {
            tvForeignPaymentHint.setText(context.getString(R.string.payment_verify_hint));
            tvForeignPaymentHint.setVisibility(View.VISIBLE);
        }
    }

    public void hidePaymentHintUi() {
        hideForeignPaymentHint();
    }

    private boolean isPaymentInProcess(JSONObject jsonObject) {
        String msg = jsonObject.optString("msg", "");
        if (TextUtils.isEmpty(msg)) return false;
        String lower = msg.toLowerCase();
        return msg.contains("输入密码")
                || lower.contains("inprocess")
                || lower.contains("userpaying")
                || "10003".equals(jsonObject.optString("code"));
    }

    private boolean isPaymentImmediateSuccess(String msg) {
        if (TextUtils.isEmpty(msg)) return false;
        String lower = msg.toLowerCase();
        if (msg.contains("输入密码") || lower.contains("inprocess") || lower.contains("userpaying")) {
            return false;
        }
        return msg.contains("成功") || lower.contains("success");
    }

    private boolean detectForeignPayment(JSONObject jsonObject) {
        String buyerLogonId = jsonObject.optString("buyer_logon_id", "");
        if (buyerLogonId.contains("@")) {
            return true;
        }
        JSONObject codeObj = jsonObject.optJSONObject("code");
        if (codeObj != null) {
            buyerLogonId = codeObj.optString("buyer_logon_id", "");
            if (buyerLogonId.contains("@")) {
                return true;
            }
        }
        return false;
    }

    private boolean extractAlipayTradeInfo(JSONObject jsonObject) {
        out_trade_no = jsonObject.optString("out_trade_no");
        order_sn = jsonObject.optString("order_sn");
        if (TextUtils.isEmpty(out_trade_no)) {
            JSONObject codeObj = jsonObject.optJSONObject("code");
            if (codeObj != null) {
                out_trade_no = codeObj.optString("out_trade_no");
                if (TextUtils.isEmpty(order_sn)) {
                    order_sn = codeObj.optString("order_sn");
                }
            }
        }
        return !TextUtils.isEmpty(out_trade_no) && !TextUtils.isEmpty(order_sn);
    }

    private boolean extractWechatTradeInfo(JSONObject jsonObject) {
        try {
            JSONObject codeObj = jsonObject.getJSONObject("code");
            order_sn = codeObj.optString("order_sn");
            if (codeObj.has("out_trade_no")) {
                out_trade_no = codeObj.getString("out_trade_no");
            }
            return !TextUtils.isEmpty(order_sn) && !TextUtils.isEmpty(out_trade_no);
        } catch (JSONException e) {
            return false;
        }
    }

    private boolean isAlipayTradeSuccess(JSONObject jsonObject) {
        String tradeStatus = jsonObject.optString("trade_status", "").toUpperCase();
        if (tradeStatus.contains("TRADE_SUCCESS") || tradeStatus.contains("TRADE_FINISHED")) {
            return true;
        }
        try {
            String payAmount = jsonObject.optString("buyer_pay_amount", "0");
            if (!TextUtils.isEmpty(payAmount) && new BigDecimal(payAmount).compareTo(BigDecimal.ZERO) > 0) {
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private void verifyPaymentResult() {
        if (tvForeignPaymentHint != null) {
            tvForeignPaymentHint.setText(context.getString(R.string.payment_verify_hint));
            tvForeignPaymentHint.setVisibility(View.VISIBLE);
        }

        Map<String, String> params = new HashMap<>();
        params.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid());
        String url = POSApiSerview.POS_URL + POSApiSerview.getLastOder;

        OkHttpUtil.postFormAsync(url, params, context, new OkHttpUtil.OkHttpCallback() {
            @Override
            public void onSuccess(String response) {
                context.runOnUiThread(() -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.optInt("code") != 1) {
                            showPaymentVerifyFailedDialog();
                            return;
                        }
                        String dataStr = jsonObject.optString("data");
                        if (TextUtils.isEmpty(dataStr) || "[]".equals(dataStr)) {
                            showPaymentVerifyFailedDialog();
                            return;
                        }
                        ArrayList<LastOrderBean> list = new Gson().fromJson(dataStr, new TypeToken<ArrayList<LastOrderBean>>() {}.getType());
                        if (list == null || list.isEmpty()) {
                            showPaymentVerifyFailedDialog();
                            return;
                        }
                        LastOrderBean lastOrder = list.get(0);
                        String serverPriceStr = TextUtils.isEmpty(lastOrder.getPay_fee()) ? lastOrder.getTotal_fee() : lastOrder.getPay_fee();
                        String localPriceStr = TextUtils.isEmpty(checkoutBean.getPay_fee()) ? checkoutBean.getTotal_fee() : checkoutBean.getPay_fee();
                        BigDecimal serverPrice = new BigDecimal(TextUtils.isEmpty(serverPriceStr) ? "0" : serverPriceStr);
                        BigDecimal localPrice = new BigDecimal(TextUtils.isEmpty(localPriceStr) ? "0" : localPriceStr);
                        boolean amountMatch = serverPrice.compareTo(localPrice) == 0;
                        long serverTime = lastOrder.getCreatetime() * 1000L;
                        long windowMs = isForeignPayment ? 180000L : 90000L;
                        boolean timeRecent = Math.abs(System.currentTimeMillis() - serverTime) < windowMs;
                        boolean payTypeMatch = TextUtils.isEmpty(lastOrder.getPay_type()) || pay_type.equals(lastOrder.getPay_type());

                        if (amountMatch && timeRecent && payTypeMatch) {
                            stopPaymentPolling();
                            checkoutBean.setOrder_sn(lastOrder.getOrder_sn());
                            checkoutBean.setTransaction_id(lastOrder.getTransaction_id());
                            order_sn = lastOrder.getOrder_sn();
                            pushorders(checkoutBean);
                        } else {
                            showPaymentVerifyFailedDialog();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "反查支付结果失败", e);
                        showPaymentVerifyFailedDialog();
                    }
                });
            }

            @Override
            public void onFailure(IOException e) {
                context.runOnUiThread(() -> showPaymentVerifyFailedDialog());
            }
        });
    }

    private void showPaymentVerifyFailedDialog() {
        stopPaymentPolling();
        DialogUIUtils.dismiss(buildBean);
        new DeleteShopPopupWindow(context, true, context.getString(R.string.payment_verify_manual), text -> {
        }).show();
    }

    class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            if ("alipay".equals(pay_type)) {
                queryOrder(false);
            }
        }

        @Override
        public void onFinish() {
            is_chaoshi = true;
            if ("alipay".equals(pay_type)) {
                queryOrder(true);
            } else {
                verifyPaymentResult();
            }
        }
    }

}
