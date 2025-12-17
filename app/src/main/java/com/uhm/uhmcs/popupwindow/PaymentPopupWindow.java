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
import com.uhm.uhmcs.activity.MainActivity;
import com.uhm.uhmcs.adapter.PaymentAdapter;
import com.uhm.uhmcs.bean.LastOrderBean;
import com.uhm.uhmcs.http.POSApiSerview;
import com.uhm.uhmcs.utils.UserUtils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal; // ★★★ 补回了这行，解决报错 ★★★
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
    private static final String TAG = "PaymentPopupWindow";
    private PopupWindow popupWindow;
    private Activity context;
    private RecyclerView payment_rv;
    private PaymentAdapter paymentAdapter;
    private ArrayList<LastOrderBean.PaymentlogBean> paymentlogBeanArrayList;
    private BuildBean buildBean;
    private String mainOrderSn;

    public PaymentPopupWindow(Activity context, ArrayList<LastOrderBean.PaymentlogBean> paymentlogBeanArrayList, String mainOrderSn) {
        this.context = context;
        this.paymentlogBeanArrayList = paymentlogBeanArrayList;
        this.mainOrderSn = mainOrderSn != null ? mainOrderSn : "";
        initPopup();

        if (context instanceof MainActivity) {
            ((MainActivity) context).setPaymentStatusListener((success, position) -> {
                DialogUIUtils.dismiss(buildBean);
                if (success) {
                    new DeleteShopPopupWindow(context, context.getString(R.string.Refund_successful), true).show();
                    if (position >= 0 && position < paymentAdapter.getData().size()) {
                        paymentAdapter.getData().get(position).setOrder_status(4);
                        paymentAdapter.notifyItemChanged(position);
                    }
                    popupWindow.dismiss();
                } else {
                    new DeleteShopPopupWindow(context, context.getString(R.string.Refund_failed) + ": 查询超时", true).show();
                }
            });
        }
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

        popupView.post(() -> {
            DisplayMetrics metrics = new DisplayMetrics();
            context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            popupWindow.update((metrics.widthPixels - popupView.getWidth()) / 2, (metrics.heightPixels - popupView.getHeight()) / 2, -1, -1);
        });

        popupView.findViewById(R.id.guanbi_btn).setOnClickListener(v -> popupWindow.dismiss());

        buildBean = DialogUIUtils.showLoading(context, context.getString(R.string.Refunding), true, false, false, false);
        DialogUIUtils.dismiss(buildBean);

        payment_rv = popupView.findViewById(R.id.payment_rv);
        payment_rv.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        paymentAdapter = new PaymentAdapter(context);
        payment_rv.setAdapter(paymentAdapter);
        paymentAdapter.setNewData(paymentlogBeanArrayList);

        paymentAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            if (view.getId() == R.id.tuikuan_btn) {
                new RefundPassWordPopupWindow(context, discount -> {
                    if (!discount.equals("1234")) {
                        Toast.makeText(context, "密码错误", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    new DeleteShopPopupWindow(context, context.getString(R.string.Confirm_refund), text -> {
                        buildBean.show();
                        cash_refund(paymentAdapter.getData().get(position), position);
                    }).show();
                }).show();
            }
        });
    }

    public void cash_refund(LastOrderBean.PaymentlogBean paymentlogBean, int position) {
        Map<String, String> params = new HashMap<>();
        String url = "";

        // 1. 基础数据准备
        String orderSn = TextUtils.isEmpty(paymentlogBean.getOrder_sn()) ? this.mainOrderSn : paymentlogBean.getOrder_sn();
        String transactionId = paymentlogBean.getTransaction_id();
        String receivedMoney = paymentlogBean.getReceivedmoney() != null ? paymentlogBean.getReceivedmoney() : "0.00";

        // 定义变量名为 shopUid
        String shopUid = UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid();
        String refundNo = "RF" + System.currentTimeMillis() + (int)(Math.random() * 900 + 100);

        // 2. 根据支付类型构建参数
        if ("cash".equals(paymentlogBean.getPay_type())) {
            url = POSApiSerview.POS_URL + POSApiSerview.cash_refund;
            params.put("order_sn", orderSn);
            params.put("refund_fee", receivedMoney);

        } else if ("alipay".equals(paymentlogBean.getPay_type())) {
            url = POSApiSerview.POS_URL + POSApiSerview.order_refund;
            params.put("shop_id", shopUid); // 使用 shopUid
            params.put("refund_amount", receivedMoney);
            params.put("refund_request_no", refundNo);
            if (!TextUtils.isEmpty(transactionId)) {
                params.put("trade_no", transactionId);
            } else {
                params.put("out_trade_no", orderSn);
            }

        } else if ("wechat".equals(paymentlogBean.getPay_type())) {
            url = POSApiSerview.POS_URL + POSApiSerview.wx_refund;
            params.put("shop_id", shopUid); // ★★★ 修复点：这里改成 shopUid ★★★

            // --- 核心修复：只传 transaction_id，强制丢弃可能错误的 out_trade_no ---
            if (!TextUtils.isEmpty(transactionId)) {
                // 有官方单号，只传官方单号，这是最稳的！
                params.put("transaction_id", transactionId);
                Log.e(TAG, "WeChat Refund: 仅使用 transaction_id 退款: " + transactionId);
                // 注意：这里故意【不传】out_trade_no，防止后端拿错误的号去请求微信报错
            } else {
                // 实在没有官方单号，才死马当活马医，传本地单号
                params.put("out_trade_no", orderSn);
                Log.e(TAG, "WeChat Refund: 无 transaction_id，只能使用 out_trade_no: " + orderSn);
            }

            params.put("refund_no", refundNo);

            // 金额处理 (转为分)
            BigDecimal amountYuan = new BigDecimal(receivedMoney);
            BigDecimal amountFen = amountYuan.multiply(new BigDecimal("100"));
            String feeInFen = String.valueOf(amountFen.intValue());

            params.put("refund_fee", feeInFen);
            params.put("total_fee", feeInFen);

            // 打印日志方便确认
            Log.e(TAG, "--------------------------------------------------------");
            Log.e(TAG, "【微信退款参数】");
            Log.e(TAG, "transaction_id= " + params.get("transaction_id"));
            Log.e(TAG, "out_trade_no  = " + params.get("out_trade_no"));
            Log.e(TAG, "--------------------------------------------------------");
        }

        // 3. 发送网络请求 (保持不变)
        FormBody.Builder formBuilder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String value = entry.getValue() != null ? entry.getValue() : "";
            formBuilder.add(entry.getKey(), value);
        }

        Request request = new Request.Builder()
                .url(url)
                .addHeader("token", UserUtils.getInstance().getLoginBase().getData().getUserinfo().getToken())
                .post(formBuilder.build())
                .build();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                context.runOnUiThread(() -> {
                    DialogUIUtils.dismiss(buildBean);
                    Log.e(TAG, "网络请求失败: " + e.getMessage());
                    new DeleteShopPopupWindow(context, context.getString(R.string.Refund_failed) + ": 网络错误", true).show();
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "{}";
                Log.e(TAG, "【后端返回数据】: " + responseBody);

                context.runOnUiThread(() -> {
                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);
                        String msg = jsonObject.optString("msg", "未知错误");

                        if (msg.contains("成功") || msg.contains("Success")) {
                            DialogUIUtils.dismiss(buildBean);
                            new DeleteShopPopupWindow(context, context.getString(R.string.Refund_successful), true).show();
                            paymentAdapter.getData().get(position).setOrder_status(4);
                            paymentAdapter.notifyItemChanged(position);
                            popupWindow.dismiss();
                        } else if (msg.contains("处理中")) {
                            if (context instanceof MainActivity) {
                                ((MainActivity) context).startRefundQuery(
                                        paymentlogBean.getPay_type(),
                                        transactionId,
                                        orderSn,
                                        receivedMoney,
                                        position
                                );
                            }
                        } else {
                            DialogUIUtils.dismiss(buildBean);
                            if (jsonObject.has("code") && jsonObject.get("code") instanceof JSONObject) {
                                JSONObject codeObj = jsonObject.getJSONObject("code");
                                String errDes = codeObj.optString("err_code_des");
                                if (!TextUtils.isEmpty(errDes)) msg = errDes;
                            }
                            new DeleteShopPopupWindow(context, context.getString(R.string.Refund_failed) + ": " + msg, true).show();
                        }
                    } catch (JSONException e) {
                        DialogUIUtils.dismiss(buildBean);
                        new DeleteShopPopupWindow(context, "解析异常", true).show();
                    }
                });
            }
        });
    }

    public void show() {
        View rootView = context.getWindow().getDecorView();
        popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, 0, 0);
    }
}