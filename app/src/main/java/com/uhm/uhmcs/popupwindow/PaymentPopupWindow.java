package com.uhm.uhmcs.popupwindow;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dou361.dialogui.DialogUIUtils;
import com.dou361.dialogui.bean.BuildBean;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.activity.LoginActivity;
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
    private ArrayList<LastOrderBean.PaymentlogBean> paymentlogBeanArrayList;
    private BuildBean buildBean;

    private int xfType = 1;
    private String number = "";

    // ★ 核心优化：定义退款成功回调接口
    public interface OnRefundSuccessListener {
        void onRefundSuccess();
    }

    private OnRefundSuccessListener refundSuccessListener;

    // 构造函数 1
    public PaymentPopupWindow(Activity context, ArrayList<LastOrderBean.PaymentlogBean> paymentlogBeanArrayList) {
        this.context = context;
        this.paymentlogBeanArrayList = paymentlogBeanArrayList;
        initPopup();
    }

    // 构造函数 2（带业务参数和回调）
    public PaymentPopupWindow(Activity context, int xfType, String number,
                              ArrayList<LastOrderBean.PaymentlogBean> paymentlog,
                              OnRefundSuccessListener listener) {
        this.context = context;
        this.paymentlogBeanArrayList = paymentlog;
        this.xfType = xfType;
        this.number = number;
        this.refundSuccessListener = listener; // 注入监听器
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

        popupView.setBackgroundColor(context.getColor(R.color.black60));
        popupWindow.setOutsideTouchable(true);

        // 计算居中位置
        popupView.post(() -> {
            DisplayMetrics metrics = new DisplayMetrics();
            context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int x = (metrics.widthPixels - popupView.getWidth()) / 2;
            int y = (metrics.heightPixels - popupView.getHeight()) / 2;
            popupWindow.update(x, y, -1, -1);
        });

        popupView.findViewById(R.id.guanbi_btn).setOnClickListener(v -> popupWindow.dismiss());

        // 初始化退款等待框
        buildBean = DialogUIUtils.showLoading(context, context.getString(R.string.Refunding), true, true, false, false);

        payment_rv = popupView.findViewById(R.id.payment_rv);
        payment_rv.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        paymentAdapter = new PaymentAdapter(context);
        payment_rv.setAdapter(paymentAdapter);
        paymentAdapter.setNewData(paymentlogBeanArrayList);

        paymentAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            if (view.getId() == R.id.tuikuan_btn) {
                // 退款权限校验
                new RefundPassWordPopupWindow(context, new PopupWindowOnClickListener.DiscountOnClickListener() {
                    @Override
                    public void onClick(String discount) {
                        if (!"1234".equals(discount)) {
                            Toast.makeText(context, "密码错误", Toast.LENGTH_SHORT).show();
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

    /**
     * 核心退款逻辑：支持现金、支付宝、微信、会员余额
     */
    public void cash_refund(LastOrderBean.PaymentlogBean paymentlogBean, int position) {
        Map<String, String> params = new HashMap<>();
        String url = "";

        // 根据支付类型组装参数
        if (paymentlogBean.getPay_type().equals("cash")) {
            params.put("order_sn", paymentlogBean.getOrder_sn());
            params.put("refund_fee", paymentlogBean.getReceivedmoney());
            params.put("xf_type", xfType + "");
            params.put("pay_type", paymentlogBean.getPay_type());
            params.put("cardnumber", TextUtils.isEmpty(number) ? "" : number);
            url = POSApiSerview.POS_URL + POSApiSerview.cash_refund;
        } else if (paymentlogBean.getPay_type().equals("alipay")) {
            params.put("refund_amount", paymentlogBean.getReceivedmoney());
            params.put("trade_no", paymentlogBean.getTransaction_id());
            params.put("xf_type", xfType + "");
            params.put("pay_type", paymentlogBean.getPay_type());
            params.put("cardnumber", TextUtils.isEmpty(number) ? "" : number);
            url = POSApiSerview.POS_URL + POSApiSerview.order_refund;
        } else if (paymentlogBean.getPay_type().equals("wechat")) {
            params.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid());
            params.put("transaction_id", paymentlogBean.getTransaction_id());
            // 微信退款通常以分为单位
            params.put("refund_fee", new BigDecimal(paymentlogBean.getReceivedmoney()).multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString());
            params.put("total_fee", new BigDecimal(paymentlogBean.getReceivedmoney()).multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString());
            params.put("xf_type", xfType + "");
            params.put("pay_type", paymentlogBean.getPay_type());
            params.put("cardnumber", TextUtils.isEmpty(number) ? "" : number);
            url = POSApiSerview.POS_URL + POSApiSerview.wx_refund;
        } else if (paymentlogBean.getPay_type().equals("wallet")) {
            params.put("order_sn", paymentlogBean.getOrder_sn());
            params.put("refund_fee", paymentlogBean.getReceivedmoney());
            params.put("xf_type", xfType + "");
            params.put("pay_type", paymentlogBean.getPay_type());
            params.put("cardnumber", paymentlogBean.getCode()); // 会员卡号
            url = POSApiSerview.POS_URL + POSApiSerview.wallet_refund;
        }

        FormBody.Builder formBuilder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            formBuilder.add(entry.getKey(), entry.getValue());
        }

        Request request = new Request.Builder()
                .url(url)
                .addHeader("token", UserUtils.getInstance().getLoginBase().getData().getUserinfo().getToken())
                .post(formBuilder.build())
                .build();

        // 配置 OkHttp 客户端
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                context.runOnUiThread(() -> {
                    DialogUIUtils.dismiss(buildBean);
                    Toast.makeText(context, "网络错误，退款请求失败", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String result = response.body().string();
                        JSONObject jsonObject = new JSONObject(result);
                        context.runOnUiThread(() -> {
                            DialogUIUtils.dismiss(buildBean);
                            try {
                                String msg = jsonObject.getString("msg");
                                if (msg.contains("成功") || msg.contains("Success")) {
                                    // 1. 弹出提示
                                    new DeleteShopPopupWindow(context, context.getString(R.string.Refund_successful), true).show();

                                    // 2. 更新当前详情列表状态
                                    paymentAdapter.getData().get(position).setOrder_status(4);
                                    paymentAdapter.notifyItemChanged(position);

                                    // 3. ★ 核心点：通知父窗口（历史订单页）刷新
                                    if (refundSuccessListener != null) {
                                        refundSuccessListener.onRefundSuccess();
                                    }
                                    return;
                                }

                                if (msg.contains("失效")) {
                                    new DeleteShopPopupWindow(context, true, msg, text -> {
                                        context.startActivity(new Intent(context, LoginActivity.class));
                                    }).show();
                                    return;
                                }

                                new DeleteShopPopupWindow(context, context.getString(R.string.Refund_failed) + ": " + msg, true).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void show() {
        View rootView = context.getWindow().getDecorView();
        popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, 0, 0);
    }
}