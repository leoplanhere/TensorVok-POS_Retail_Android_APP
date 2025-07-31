package com.uhm.uhmcs.popupwindow;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dou361.dialogui.DialogUIUtils;
import com.dou361.dialogui.bean.BuildBean;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.activity.LoginActivity;
import com.uhm.uhmcs.adapter.OrderShopAdapter;
import com.uhm.uhmcs.adapter.PaymentAdapter;
import com.uhm.uhmcs.bean.LastOrderBean;
import com.uhm.uhmcs.http.POSApiSerview;
import com.uhm.uhmcs.utils.UserUtils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class PaymentPopupWindow {
    private PopupWindow popupWindow;
    private Activity context;



    private RecyclerView payment_rv;
    private PaymentAdapter paymentAdapter;

    private  ArrayList<LastOrderBean.PaymentlogBean> paymentlogBeanArrayList;
    private BuildBean buildBean;

    int xfType=1;
    String number="";
    public PaymentPopupWindow(Activity context, ArrayList<LastOrderBean.PaymentlogBean> paymentlogBeanArrayList){

        this.context = context;
        this.paymentlogBeanArrayList = paymentlogBeanArrayList;

        initPopup();
    }

    public PaymentPopupWindow(Activity context, int xfType, String number, ArrayList<LastOrderBean.PaymentlogBean> paymentlog) {
        this.context = context;
        this.paymentlogBeanArrayList = paymentlog;
        this.xfType = xfType;
        this.number = number;
        initPopup();
    }


    private void initPopup() {
        View popupView = LayoutInflater.from(context).inflate(R.layout.popupwindow_payment, null);
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
        buildBean= DialogUIUtils.showLoading(context,context.getString(R.string.Refunding),true,false,false,false);
        payment_rv=popupView.findViewById(R.id.payment_rv);
        payment_rv.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL,false));
        paymentAdapter=new PaymentAdapter(context);
        payment_rv.setAdapter(paymentAdapter);
        paymentAdapter.setNewData(paymentlogBeanArrayList);
        paymentAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            if (view.getId()==R.id.tuikuan_btn){
                new RefundPassWordPopupWindow(context, new PopupWindowOnClickListener.DiscountOnClickListener() {
                    @Override
                    public void onClick(String discount) {
                        if (!discount.equals("1234")){
                            return;
                        }
                        new DeleteShopPopupWindow(context, context.getString(R.string.Confirm_refund), new PopupWindowOnClickListener.DeleteShopOnClickListener() {
                            @Override
                            public void onClick(String text) {
                                buildBean.show();
                                cash_refund(paymentAdapter.getData().get(position), position);
                            }
                        }).show();
                    }
                }).show();

            }
        });

    }
    public void cash_refund(LastOrderBean.PaymentlogBean paymentlogBean, int position) {
        Map<String, String> params = new HashMap<>();
//        if (item.getPay_type().equals("cash")){
//            xianjin="现金";
//        }else if (item.getPay_type().equals("alipay")){
//            xianjin="支付宝";
//        }else if (item.getPay_type().equals("wechat")){
//            xianjin="微信";
//        }
        String url = "";
        if (paymentlogBean.getPay_type().equals("cash")){
            params.put("order_sn", paymentlogBean.getOrder_sn());
            params.put("refund_fee", paymentlogBean.getReceivedmoney());
            params.put("xf_type", xfType+"");
            params.put("pay_type",paymentlogBean.getPay_type());
            params.put("cardnumber", TextUtils.isEmpty(number)?"":number);
            url = POSApiSerview.POS_URL + POSApiSerview.cash_refund;
        }else if (paymentlogBean.getPay_type().equals("alipay")){
            params.put("refund_amount", paymentlogBean.getReceivedmoney());
            params.put("trade_no", paymentlogBean.getTransaction_id());
            params.put("xf_type", xfType+"");
            params.put("pay_type",paymentlogBean.getPay_type());
            params.put("cardnumber", TextUtils.isEmpty(number)?"":number);
            url = POSApiSerview.POS_URL + POSApiSerview.order_refund;
        }else if (paymentlogBean.getPay_type().equals("wechat")){
            params.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid());
            params.put("transaction_id", paymentlogBean.getTransaction_id());
            params.put("refund_fee",new BigDecimal(paymentlogBean.getReceivedmoney()).multiply(new BigDecimal("100"))+"");
            params.put("total_fee",new BigDecimal(paymentlogBean.getReceivedmoney()).multiply(new BigDecimal("100"))+"");
            params.put("xf_type", xfType+"");
            params.put("pay_type",paymentlogBean.getPay_type());
            params.put("cardnumber", TextUtils.isEmpty(number)?"":number);
            url = POSApiSerview.POS_URL + POSApiSerview.wx_refund;
        }else if (paymentlogBean.getPay_type().equals("wallet")){
            params.put("order_sn", paymentlogBean.getOrder_sn());
            params.put("refund_fee", paymentlogBean.getReceivedmoney());
            params.put("xf_type", xfType+"");
            params.put("pay_type",paymentlogBean.getPay_type());
            params.put("cardnumber", paymentlogBean.getCode());
            url = POSApiSerview.POS_URL + POSApiSerview.wallet_refund;
        }
        FormBody.Builder formBuilder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            Log.i("ttt","??>>>"+entry.getKey()+">>>>"+entry.getValue());
            formBuilder.add(entry.getKey(), entry.getValue());
        }
        RequestBody formBody = formBuilder.build();
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
                .addInterceptor(loggingInterceptor)   // 添加日志拦截器
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

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
                                    DialogUIUtils.dismiss(buildBean);
                                    String msg=jsonObject.getString("msg");
                                    if (msg.contains("成功")||msg.contains("Success")){
                                        new DeleteShopPopupWindow(context,context.getString(R.string.Refund_successful),true).show();
                                        paymentAdapter.getData().get(position).setOrder_status(4);
                                        paymentAdapter.notifyItemChanged(position);
                                        return;
                                    }
                                    if (msg.contains("失效")){
                                        new DeleteShopPopupWindow(context, true, msg, new PopupWindowOnClickListener.DeleteShopOnClickListener() {
                                            @Override
                                            public void onClick(String text) {
                                                Intent intent=new Intent(context, LoginActivity.class);
                                                context.startActivity(intent);
                                            }
                                        }).show();
                                        return;
                                    }
                                    new DeleteShopPopupWindow(context,context.getString(R.string.Refund_failed),true).show();


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


    public void show() {
        View rootView = ((Activity) context).getWindow().getDecorView();
        popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, 0, 0);
    }
}
