package com.uhm.uhmcs.popupwindow;

import static android.widget.Toast.LENGTH_SHORT;

import android.app.Activity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.dou361.dialogui.DialogUIUtils;
import com.dou361.dialogui.bean.BuildBean;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.activity.LoginActivity;
import com.uhm.uhmcs.adapter.HistoryOrderAdapter;
import com.uhm.uhmcs.bean.LastOrderBean;
import com.uhm.uhmcs.http.OkHttpUtil;
import com.uhm.uhmcs.http.POSApiSerview;
import com.uhm.uhmcs.utils.MyPrinterHelper;
import com.uhm.uhmcs.utils.UserUtils;
import com.uhm.uhmcs.view.CustomInputTextView;

import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistoryOrderPopupWindow {
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

    // --- 核心优化：防重复请求与状态恢复变量 ---
    private boolean isRequesting = false;
    private List<LastOrderBean> lastFullList = new ArrayList<>();
    private int lastFullPage = 1;
    private int lastFullTotalPage = 1;
    private boolean isSearchResult = false;

    public HistoryOrderPopupWindow(Activity context) {
        this.context = context;
        initPopup();
    }

    private void initPopup() {
        View popupView = LayoutInflater.from(context).inflate(R.layout.popupwindow_history_order, null);
        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        popupView.setBackgroundColor(context.getColor(R.color.black60));

        popupWindow.setOutsideTouchable(false);
        popupWindow.setFocusable(true);
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        initViews(popupView);
        setupListeners(popupView);

        getOrderList();
    }

    private void initViews(View popupView) {
        order_rv = popupView.findViewById(R.id.order_rv);
        order_rv.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        historyOrderAdapter = new HistoryOrderAdapter();
        order_rv.setAdapter(historyOrderAdapter);

        View emptyView = LayoutInflater.from(context).inflate(R.layout.layout_empty_view, null);
        emptyView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        historyOrderAdapter.setEmptyView(emptyView);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        String today = sdf.format(System.currentTimeMillis());
        starttime = today;
        endtime = today;

        starttime_tv = popupView.findViewById(R.id.starttime_tv);
        endtime_tv = popupView.findViewById(R.id.endtime_tv);
        starttime_tv.setText(starttime);
        endtime_tv.setText(endtime);

        order_sn_et = popupView.findViewById(R.id.order_sn_et);
        order_sn_et.postDelayed(() -> order_sn_et.requestFocus(), 100);
    }

    private void setupListeners(View popupView) {
        popupView.findViewById(R.id.fanhui_btn).setOnClickListener(v -> handleBackAction());

        order_sn_et.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                prepareSearch();
                getOrderList();
                return true;
            }
            return false;
        });

        order_sn_et.setOnInputCompleteListener(text -> {
            prepareSearch();
            getOrderList();
        });

        popupView.findViewById(R.id.time_btn).setOnClickListener(v -> {
            new TimePopupWindow(context, (sTime, eTime) -> {
                starttime = sTime;
                endtime = eTime;
                starttime_tv.setText(starttime);
                endtime_tv.setText(endtime);
                isSearchResult = false;
                resetAndReload();
            }).show();
        });

        order_rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && !isRequesting && page < totalpage) {
                    LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (lm != null && lm.findLastVisibleItemPosition() >= historyOrderAdapter.getItemCount() - 3) {
                        page++;
                        getOrderList();
                    }
                }
            }
        });

        historyOrderAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            LastOrderBean item = historyOrderAdapter.getData().get(position);
            int id = view.getId();
            if (id == R.id.gouwuxinxi_tv) {
                new OrderShopPopupWindow(context, item.getOrder_item()).show();
            } else if (id == R.id.zhifuxinxi_btn) {
                // ★ 核心同步逻辑：传入退款成功的回调
                new PaymentPopupWindow(context, item.getXf_type(), item.getNumber(), item.getPaymentlog(), new PaymentPopupWindow.OnRefundSuccessListener() {
                    @Override
                    public void onRefundSuccess() {
                        // 1. 更新当前列表中的状态
                        item.setRefund_type(2);
                        historyOrderAdapter.notifyItemChanged(position);

                        // 2. ★ 同步更新备份列表（防止返回后状态回滚）
                        if (isSearchResult && lastFullList != null) {
                            for (LastOrderBean fullItem : lastFullList) {
                                if (fullItem.getOrder_sn().equals(item.getOrder_sn())) {
                                    fullItem.setRefund_type(2);
                                    break;
                                }
                            }
                        }
                    }
                }).show();
            } else if (id == R.id.daying_tv) {
                MyPrinterHelper.getInstance().asyncPrintLastOrder(context, item, null);
            }
        });
    }

    private void prepareSearch() {
        if (!isSearchResult) {
            lastFullList = new ArrayList<>(historyOrderAdapter.getData());
            lastFullPage = page;
            lastFullTotalPage = totalpage;
        }
        isSearchResult = true;
        page = 1;
        totalpage = 1;
        historyOrderAdapter.setNewData(null);
    }

    private void handleBackAction() {
        if (isSearchResult) {
            isSearchResult = false;
            order_sn_et.setText("");
            historyOrderAdapter.setNewData(lastFullList);
            page = lastFullPage;
            totalpage = lastFullTotalPage;
            Toast.makeText(context, "已恢复完整列表", Toast.LENGTH_SHORT).show();
        } else {
            popupWindow.dismiss();
        }
    }

    private void resetAndReload() {
        page = 1;
        totalpage = 1;
        historyOrderAdapter.setNewData(null);
        getOrderList();
    }

    private void getOrderList() {
        if (isRequesting) return;
        isRequesting = true;

        String tip = (page == 1) ? "正在获取订单..." : "加载第 " + page + " / " + totalpage + " 页";
        buildBean = DialogUIUtils.showLoading(context, tip, true, true, false, false);
        buildBean.show();

        Map<String, String> params = new HashMap<>();
        String sn = order_sn_et.getText().toString().trim();

        if (!TextUtils.isEmpty(sn)) {
            params.put("order_sn", sn);
        } else {
            params.put("starttime", starttime);
            params.put("endtime", endtime);
        }

        params.put("page", String.valueOf(page));
        params.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid());
        params.put("strip", "10");

        String url = POSApiSerview.POS_URL + POSApiSerview.orderList;
        OkHttpUtil.postFormAsync(url, params, context, new OkHttpUtil.OkHttpCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.optInt("code") == 1 && !jsonObject.isNull("data")) {
                        JSONObject dataObj = new JSONObject(jsonObject.getString("data"));
                        ArrayList<LastOrderBean> list = new Gson().fromJson(
                                dataObj.getString("data"),
                                new TypeToken<ArrayList<LastOrderBean>>() {}.getType()
                        );

                        if (dataObj.has("pagination")) {
                            totalpage = new JSONObject(dataObj.getString("pagination")).optInt("totalpage");
                        }

                        context.runOnUiThread(() -> {
                            if (page > 1) {
                                historyOrderAdapter.addData(list);
                            } else {
                                historyOrderAdapter.setNewData(list);
                            }
                            finalizeRequest(false);
                        });
                    } else {
                        context.runOnUiThread(() -> {
                            if (page == 1) historyOrderAdapter.setNewData(null);
                            finalizeRequest(true);
                        });
                    }
                } catch (Exception e) {
                    context.runOnUiThread(() -> finalizeRequest(false));
                }
            }

            @Override
            public void onFailure(IOException e) {
                context.runOnUiThread(() -> {
                    finalizeRequest(false);
                    Toast.makeText(context, "网络异常，请重试", LENGTH_SHORT).show();
                });
            }
        });
    }

    private void finalizeRequest(boolean showToast) {
        isRequesting = false;
        if (buildBean != null) DialogUIUtils.dismiss(buildBean);
        if (showToast && !TextUtils.isEmpty(order_sn_et.getText().toString())) {
            Toast.makeText(context, "未找到该单号", LENGTH_SHORT).show();
        }
        order_sn_et.setText("");
    }

    public void show() {
        View rootView = context.getWindow().getDecorView();
        popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, 0, 0);
    }
}