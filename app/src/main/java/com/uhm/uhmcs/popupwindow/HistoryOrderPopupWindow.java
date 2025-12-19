package com.uhm.uhmcs.popupwindow;

import android.app.Activity;
import android.app.ProgressDialog;
import android.text.TextUtils;
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
import java.util.Locale;
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

    private ProgressDialog progressDialog;
    private CustomInputTextView order_sn_et;
    private boolean isRequesting = false;

    // ★ 状态备份变量
    private List<LastOrderBean> lastFullList = new ArrayList<>();
    private int lastFullPage = 1;      // 备份离开时的页码
    private int lastFullTotalPage = 1; // 备份离开时的总页数
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
                    if (lm != null && lm.findLastVisibleItemPosition() >= historyOrderAdapter.getItemCount() - 5) {
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
                showPaymentInfo(item);
            } else if (id == R.id.daying_tv) {
                operateDetails(item);
            }
        });
    }

    // ★ 进入搜索前备份当前的分页状态
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
            // ★ 恢复列表及分页状态
            historyOrderAdapter.setNewData(lastFullList);
            page = lastFullPage;
            totalpage = lastFullTotalPage;
            Toast.makeText(context, "已恢复，当前共 " + totalpage + " 页", Toast.LENGTH_SHORT).show();
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

    private void showProgress(String msg) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(context);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
        }
        progressDialog.setMessage(msg);
        progressDialog.show();
    }

    private void hideProgress() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void getOrderList() {
        if (isRequesting) return;
        isRequesting = true;

        String tip = (page == 1) ? "正在初始化订单..." : "正在获取第 " + page + " 页 / 共 " + totalpage + " 页";
        showProgress(tip);

        Map<String, String> params = new HashMap<>();
        String sn = order_sn_et.getText().toString().trim();
        if (!TextUtils.isEmpty(sn)) params.put("order_sn", sn);

        params.put("starttime", starttime);
        params.put("endtime", endtime);
        params.put("page", String.valueOf(page));
        params.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid());
        params.put("strip", "10");

        String url = POSApiSerview.POS_URL + POSApiSerview.orderList;
        OkHttpUtil.postFormAsync(url, params, context, new OkHttpUtil.OkHttpCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getInt("code") == 1 && !jsonObject.isNull("data")) {
                        JSONObject dataObj = new JSONObject(jsonObject.getString("data"));
                        ArrayList<LastOrderBean> list = new Gson().fromJson(dataObj.getString("data"), new TypeToken<ArrayList<LastOrderBean>>() {}.getType());

                        if (dataObj.has("pagination")) {
                            totalpage = new JSONObject(dataObj.getString("pagination")).getInt("totalpage");
                        }

                        context.runOnUiThread(() -> {
                            if (page > 1) historyOrderAdapter.addData(list);
                            else historyOrderAdapter.setNewData(list);

                            isRequesting = false;
                            hideProgress();
                        });
                    } else {
                        context.runOnUiThread(() -> {
                            isRequesting = false;
                            hideProgress();
                            if (!TextUtils.isEmpty(sn)) Toast.makeText(context, "单号不存在", Toast.LENGTH_SHORT).show();
                        });
                    }
                } catch (Exception e) {
                    context.runOnUiThread(() -> { isRequesting = false; hideProgress(); });
                }
            }
            @Override
            public void onFailure(IOException e) {
                context.runOnUiThread(() -> { isRequesting = false; hideProgress(); Toast.makeText(context, "网络异常", Toast.LENGTH_SHORT).show(); });
            }
        });
    }

    private void showPaymentInfo(LastOrderBean item) {
        ArrayList<LastOrderBean.PaymentlogBean> displayLogs = new ArrayList<>();
        if (item.getPaymentlog() != null) displayLogs.addAll(item.getPaymentlog());
        if (item.getPayment() != null) displayLogs.addAll(item.getPayment());
        String mainOrderSn = item.getOrder_sn() != null ? item.getOrder_sn() : "";
        if (displayLogs.isEmpty()) {
            LastOrderBean.PaymentlogBean manualLog = new LastOrderBean.PaymentlogBean();
            manualLog.setReceivedmoney(item.getTotal_amount());
            manualLog.setPay_type(item.getPay_type());
            manualLog.setOrder_sn(mainOrderSn);
            displayLogs.add(manualLog);
        }
        new PaymentPopupWindow(context, displayLogs, mainOrderSn).show();
    }

    public void operateDetails(LastOrderBean lastOrderBean) {
        showProgress("正在获取打印详情...");
        Map<String, String> params = new HashMap<>();
        params.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid());
        params.put("order_sn", lastOrderBean.getOrder_sn());
        String url = POSApiSerview.POS_URL + POSApiSerview.operateDetails;

        OkHttpUtil.postFormAsync(url, params, context, new OkHttpUtil.OkHttpCallback() {
            @Override
            public void onSuccess(String response) {
                context.runOnUiThread(() -> {
                    hideProgress();
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        PrintDataBean printData = null;
                        if (jsonObject.getInt("code") == 1) {
                            ArrayList<PrintDataBean> list = new Gson().fromJson(jsonObject.getString("data"), new TypeToken<ArrayList<PrintDataBean>>() {}.getType());
                            if (list != null && !list.isEmpty()) printData = list.get(0);
                        }
                        MyPrinterHelper.getInstance().asyncPrintLastOrder(context, lastOrderBean, printData);
                    } catch (Exception e) {
                        MyPrinterHelper.getInstance().asyncPrintLastOrder(context, lastOrderBean, null);
                    }
                });
            }
            @Override
            public void onFailure(IOException e) {
                context.runOnUiThread(() -> { hideProgress(); MyPrinterHelper.getInstance().asyncPrintLastOrder(context, lastOrderBean, null); });
            }
        });
    }

    public void show() {
        popupWindow.showAtLocation(context.getWindow().getDecorView(), Gravity.NO_GRAVITY, 0, 0);
    }
}