package com.uhm.uhmcs.popupwindow;

import android.app.Activity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.dou361.dialogui.bean.BuildBean;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.adapter.HistoryOrderAdapter;
import com.uhm.uhmcs.bean.LastOrderBean;
import com.uhm.uhmcs.bean.PrintDataBean;
import com.uhm.uhmcs.http.OkHttpUtil;
import com.uhm.uhmcs.http.POSApiSerview;
import com.uhm.uhmcs.utils.MyPrinterHelper;
import com.uhm.uhmcs.utils.UserUtils;
import com.uhm.uhmcs.view.CustomInputTextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryOrderPopupWindow {
    private static final String TAG = "HistoryOrderPopup";
    private PopupWindow popupWindow;
    private Activity context;
    private HistoryOrderAdapter historyOrderAdapter;
    private RecyclerView order_rv;
    private String starttime, endtime;
    private TextView starttime_tv, endtime_tv;
    private int page = 1;
    private int totalpage = 1;
    private BuildBean buildBean;
    private CustomInputTextView order_sn_et;

    public HistoryOrderPopupWindow(Activity context) {
        this.context = context;
        initPopup();
    }

    private void initPopup() {
        View popupView = LayoutInflater.from(context).inflate(R.layout.popupwindow_history_order, null);
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

        initViews(popupView);
        setupListeners(popupView);

        // 初始加载
        getOrderList();
    }

    private void initViews(View popupView) {
        order_rv = popupView.findViewById(R.id.order_rv);
        order_rv.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        historyOrderAdapter = new HistoryOrderAdapter();
        order_rv.setAdapter(historyOrderAdapter);

        // 初始化时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String formattedTime = sdf.format(System.currentTimeMillis());
        starttime = formattedTime;
        endtime = formattedTime;

        starttime_tv = popupView.findViewById(R.id.starttime_tv);
        endtime_tv = popupView.findViewById(R.id.endtime_tv);
        starttime_tv.setText(starttime);
        endtime_tv.setText(endtime);

        order_sn_et = popupView.findViewById(R.id.order_sn_et);
        // 自动获取焦点
        order_sn_et.postDelayed(() -> order_sn_et.requestFocus(), 100);
    }

    private void setupListeners(View popupView) {
        // 返回按钮
        popupView.findViewById(R.id.fanhui_btn).setOnClickListener(v -> popupWindow.dismiss());

        // 搜索框输入监听
        order_sn_et.setOnInputCompleteListener(text -> {
            page = 1;
            getOrderList();
        });

        // 时间选择
        popupView.findViewById(R.id.time_btn).setOnClickListener(v -> {
            new TimePopupWindow(context, (startTime, endTime) -> {
                starttime = startTime;
                endtime = endTime;
                starttime_tv.setText(startTime);
                endtime_tv.setText(endTime);
                page = 1;
                getOrderList();
            }).show();
        });

        // 加载更多
        order_rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int pastVisiblesItems = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        if (page < totalpage) {
                            page++;
                            getOrderList();
                        }
                    }
                }
            }
        });

        // 列表点击事件
        historyOrderAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            LastOrderBean item = historyOrderAdapter.getData().get(position);
            int id = view.getId();

            if (id == R.id.gouwuxinxi_tv) {
                // 查看购物信息
                new OrderShopPopupWindow(context, item.getOrder_item()).show();

            } else if (id == R.id.zhifuxinxi_btn) {
                // 查看支付信息 (修复：解决空白问题，并为退款准备主订单号)
                showPaymentInfo(item);

            } else if (id == R.id.daying_tv) {
                // 补打小票 (获取详情后打印)
                operateDetails(item);
            }
        });
    }

    /**
     * 显示支付信息弹窗 (修复微信/支付宝为空的问题，并传递主订单号)
     */
    private void showPaymentInfo(LastOrderBean item) {
        // 必须使用 ArrayList，因为 PaymentPopupWindow 构造函数只接受 ArrayList
        ArrayList<LastOrderBean.PaymentlogBean> displayLogs = new ArrayList<>();

        // 1. 尝试添加 paymentlog (微信/支付宝交易号和商户单号的来源)
        if (item.getPaymentlog() != null && !item.getPaymentlog().isEmpty()) {
            displayLogs.addAll(item.getPaymentlog());
        }

        // 2. 尝试添加 payment (另一个可能的支付记录字段)
        if (item.getPayment() != null && !item.getPayment().isEmpty()) {
            displayLogs.addAll(item.getPayment());
        }

        // 获取主订单号，用于退款兜底 (out_trade_no)
        String mainOrderSn = item.getOrder_sn() != null ? item.getOrder_sn() : "";

        // 3. 如果支付记录列表仍然为空，尝试从主订单信息中构造一个临时的记录用于显示
        if (displayLogs.isEmpty()) {
            String mainPayType = item.getPay_type();
            String mainAmount = item.getTotal_amount();

            if (!TextUtils.isEmpty(mainAmount)) {
                LastOrderBean.PaymentlogBean manualLog = new LastOrderBean.PaymentlogBean();
                manualLog.setReceivedmoney(mainAmount);
                manualLog.setPay_type(mainPayType != null ? mainPayType : "unknown");
                // 确保手动构造的记录也有主订单号，方便 PaymentPopupWindow 读取
                manualLog.setOrder_sn(mainOrderSn);
                displayLogs.add(manualLog);
            }
        }

        if (displayLogs.isEmpty()) {
            Toast.makeText(context, "暂无详细支付记录", Toast.LENGTH_SHORT).show();
        } else {
            // 传递主订单号 mainOrderSn 作为第四个参数
            new PaymentPopupWindow(context, displayLogs, mainOrderSn).show();
        }
    }

    /**
     * 获取详情并打印
     */
    public void operateDetails(LastOrderBean lastOrderBean) {
        Map<String, String> params = new HashMap<>();
        params.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid());
        String url = POSApiSerview.POS_URL + POSApiSerview.operateDetails;

        OkHttpUtil.postFormAsync(url, params, context, new OkHttpUtil.OkHttpCallback() {
            @Override
            public void onSuccess(String response) {
                context.runOnUiThread(() -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        int code = jsonObject.getInt("code");
                        PrintDataBean printData = null;

                        if (code == 1) {
                            ArrayList<PrintDataBean> list = new Gson().fromJson(
                                    jsonObject.getString("data"),
                                    new TypeToken<ArrayList<PrintDataBean>>() {}.getType()
                            );
                            if (list != null && !list.isEmpty()) {
                                printData = list.get(0);
                            }
                        }
                        // 3个参数的打印调用
                        MyPrinterHelper.getInstance().asyncPrintLastOrder(context, lastOrderBean, printData);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        // 异常时也尝试打印基础版
                        MyPrinterHelper.getInstance().asyncPrintLastOrder(context, lastOrderBean, null);
                    }
                });
            }

            @Override
            public void onFailure(IOException e) {
                // 网络失败也尝试打印基础版
                context.runOnUiThread(() ->
                        MyPrinterHelper.getInstance().asyncPrintLastOrder(context, lastOrderBean, null)
                );
            }
        });
    }

    public void show() {
        View rootView = context.getWindow().getDecorView();
        popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, 0, 0);
    }

    private void getOrderList() {
        Map<String, String> params = new HashMap<>();
        String sn = order_sn_et.getText().toString();
        if (!TextUtils.isEmpty(sn)) {
            params.put("order_sn", sn);
        }
        params.put("starttime", starttime);
        params.put("endtime", endtime);
        params.put("page", String.valueOf(page));
        params.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid());
        params.put("strip", "10");

        String url = POSApiSerview.POS_URL + POSApiSerview.orderList;
        OkHttpUtil.postFormAsync(url, params, context, new OkHttpUtil.OkHttpCallback() {
            @Override
            public void onSuccess(String response) {
                Log.i(TAG, response);
                context.runOnUiThread(() -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        int code = jsonObject.getInt("code");

                        if (code == 1 && !jsonObject.isNull("data") && !TextUtils.isEmpty(jsonObject.getString("data"))) {
                            JSONObject dataObj = new JSONObject(jsonObject.getString("data"));
                            ArrayList<LastOrderBean> list = new Gson().fromJson(
                                    dataObj.getString("data"),
                                    new TypeToken<ArrayList<LastOrderBean>>() {}.getType()
                            );

                            if (dataObj.has("pagination")) {
                                totalpage = new JSONObject(dataObj.getString("pagination")).getInt("totalpage");
                            }

                            if (page > 1) {
                                historyOrderAdapter.addData(list);
                            } else {
                                historyOrderAdapter.setNewData(list);
                            }
                        } else {
                            if (!TextUtils.isEmpty(sn)) {
                                new DeleteShopPopupWindow(context, context.getString(R.string.No_matching_bill_found), true).show();
                            } else {
                                if (page == 1) {
                                    historyOrderAdapter.setNewData(new ArrayList<>());
                                }
                            }
                        }

                        // 如果是搜索触发的，清空输入框
                        if (!TextUtils.isEmpty(sn)) {
                            order_sn_et.setText("");
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void onFailure(IOException e) {
                Log.e(TAG, "请求失败: " + e.getMessage());
            }
        });
    }
}