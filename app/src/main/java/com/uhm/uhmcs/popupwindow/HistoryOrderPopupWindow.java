package com.uhm.uhmcs.popupwindow;

import static android.widget.Toast.LENGTH_SHORT;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.dou361.dialogui.DialogUIUtils;
import com.dou361.dialogui.bean.BuildBean;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.activity.LoginActivity;
import com.uhm.uhmcs.activity.MainActivity;
import com.uhm.uhmcs.adapter.HistoryOrderAdapter;
import com.uhm.uhmcs.adapter.ShopAdapter;
import com.uhm.uhmcs.bean.GrouponGoodsBean;
import com.uhm.uhmcs.bean.LastOrderBean;
import com.uhm.uhmcs.bean.PrintDataBean;
import com.uhm.uhmcs.http.OkHttpUtil;
import com.uhm.uhmcs.http.POSApiSerview;
import com.uhm.uhmcs.utils.MyLabeksPrinterHelper;
import com.uhm.uhmcs.utils.MyPrinterHelper;
import com.uhm.uhmcs.utils.UserUtils;
import com.uhm.uhmcs.view.CustomInputTextView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class HistoryOrderPopupWindow {
    private PopupWindow popupWindow;
    private Activity context;
    private HistoryOrderAdapter historyOrderAdapter;
    private RecyclerView order_rv;
    private String starttime,endtime;
    private TextView starttime_tv,endtime_tv;
    private int page=1;
    private int totalpage=1;
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
//        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupView.setBackgroundColor(context.getColor(R.color.black60));
        popupWindow.setOutsideTouchable(true);
        // 计算居中位置
        popupView.post(() -> {
            DisplayMetrics metrics = new DisplayMetrics();
            context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int x = (metrics.widthPixels - popupView.getWidth()) / 2;
            int y = (metrics.heightPixels - popupView.getHeight()) / 2;
            popupWindow.update(x, y, -1, -1); // 更新位置
        });

        order_rv=popupView.findViewById(R.id.order_rv);
        order_rv.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL,false));
        historyOrderAdapter=new HistoryOrderAdapter();
        order_rv.setAdapter(historyOrderAdapter);
        // 定义日期格式模板
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        // 获取当前时间（基于系统时区）
        String formattedTime = sdf.format(System.currentTimeMillis());
        starttime=formattedTime;
        endtime=formattedTime;
        starttime_tv=popupView.findViewById(R.id.starttime_tv);
        endtime_tv=popupView.findViewById(R.id.endtime_tv);
        starttime_tv.setText(starttime);
        endtime_tv.setText(endtime);
        popupView.findViewById(R.id.fanhui_btn).setOnClickListener(v -> {
            popupWindow.dismiss();});
//        starttime_tv.setOnClickListener(v -> {
//
//            showStartDatePicker();
//        });
//        endtime_tv.setOnClickListener(v -> {
//            showEndDatePicker();
//        });
        order_sn_et=popupView.findViewById(R.id.order_sn_et);
        // 自动获取焦点
        order_sn_et.postDelayed(() -> order_sn_et.requestFocus(), 100);
        order_sn_et.setOnInputCompleteListener(text -> {
            getOrderList();
        });
        popupView.findViewById(R.id.time_btn).setOnClickListener(v -> {
            new TimePopupWindow(context, new PopupWindowOnClickListener.TimeOnClickListener() {
                @Override
                public void onClick(String startTime, String endTime) {
                    starttime=startTime;
                    endtime=endTime;
                    starttime_tv.setText(startTime);
                    endtime_tv.setText(endTime);
                    page=1;
                    getOrderList();
                }
            }).show();
        });
        order_rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int pastVisiblesItems = layoutManager.findFirstVisibleItemPosition();


                if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) { // 当滚动到列表底部时触发加载更多事件
                    page++;
                    if (page>totalpage){
                        return;
                    }
                    getOrderList();

                }


            }
        });
        historyOrderAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                int id=view.getId();
                if (id==R.id.gouwuxinxi_tv){
                    new OrderShopPopupWindow(context,historyOrderAdapter.getData().get(position).getOrder_item()).show();
                }
                if (id==R.id.zhifuxinxi_btn){

                    new PaymentPopupWindow(context,historyOrderAdapter.getData().get(position).getPaymentlog()).show();
                }
                if (id==R.id.daying_tv){
                    MyPrinterHelper.getInstance().asyncPrintLastOrder(context,true,historyOrderAdapter.getData().get(position),null);
                }
            }
        });

        getOrderList();

    }


    public void operateDetails(LastOrderBean lastOrderBean) {
        Map<String, String> params = new HashMap<>();
        params.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid() + "");
        String url = POSApiSerview.POS_URL + POSApiSerview.operateDetails;
        OkHttpUtil.postFormAsync(url, params,context, new OkHttpUtil.OkHttpCallback() {
            @Override
            public void onSuccess(String response) {
                Log.i("ttt", ">>>>>>>>>>>>>");
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int code = jsonObject.getInt("code");
                            if (code == 1) {
                                ArrayList<PrintDataBean> printDataBeanArrayList = new Gson().fromJson(jsonObject.getString("data"), new TypeToken<ArrayList<PrintDataBean>>() {
                                }.getType());
                                MyPrinterHelper.getInstance().asyncPrintLastOrder(context, lastOrderBean, printDataBeanArrayList.get(0));
                            }else {
                                MyPrinterHelper.getInstance().asyncPrintLastOrder(context,lastOrderBean,null);
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


    // 定义日期变量
    private Calendar startDate = Calendar.getInstance();
    private Calendar endDate = Calendar.getInstance();


    private String formatDate(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(calendar.getTime());
    }

    public void show() {
        View rootView = ((Activity) context).getWindow().getDecorView();
        popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, 0, 0);
    }
    private void getOrderList() {
        Map<String, String> params = new HashMap<>();
        if (!TextUtils.isEmpty(order_sn_et.getText().toString())){
            params.put("order_sn", order_sn_et.getText().toString());
        }
        params.put("starttime",starttime);
        params.put("endtime", endtime);
        params.put("page", page+"");
        params.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid());
        params.put("strip", "10");
//        params.put("page", grouponGoods_page + "");
//        params.put("strip", "20");
        String url = POSApiSerview.POS_URL + POSApiSerview.orderList;
        OkHttpUtil.postFormAsync(url, params,context, new OkHttpUtil.OkHttpCallback() {
            @Override
            public void onSuccess(String response) {
                Log.i("ttt", response);
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int code = jsonObject.getInt("code");
                            if (code == 1 && !TextUtils.isEmpty(jsonObject.getString("data"))) {
                                JSONObject jsonObject1=new JSONObject(jsonObject.getString("data"));
                                ArrayList<LastOrderBean> lastOrderBeanArrayList = new Gson().fromJson(jsonObject1.getString("data"), new TypeToken<ArrayList<LastOrderBean>>() {
                                }.getType());
                                totalpage=new JSONObject(jsonObject1.getString("pagination")).getInt("totalpage");
//                                operateDetails(lastOrderBeanArrayList.get(0));
                                if (page>1){
                                    historyOrderAdapter.addData(lastOrderBeanArrayList);
                                }else {
                                    historyOrderAdapter.setNewData(lastOrderBeanArrayList);
                                }

                            }else {
                                if (!TextUtils.isEmpty(order_sn_et.getText().toString())){
                                    new DeleteShopPopupWindow(context,context.getString(R.string.No_matching_bill_found),true).show();
                                }else {
                                    historyOrderAdapter.setNewData(new ArrayList<>());
                                }

                            }

                            order_sn_et.setText("");
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                    }
                });

            }

            @Override
            public void onFailure(IOException e) {
                System.err.println("请求失败: " + e.getMessage());
            }
        });
    }

}
