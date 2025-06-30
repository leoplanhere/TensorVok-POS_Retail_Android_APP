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

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.adapter.PaymentListAdapter;
import com.uhm.uhmcs.adapter.PrintDeviceAdapter;
import com.uhm.uhmcs.bean.LastOrderBean;
import com.uhm.uhmcs.bean.PaymentBean;
import com.uhm.uhmcs.http.OkHttpUtil;
import com.uhm.uhmcs.http.POSApiSerview;
import com.uhm.uhmcs.utils.MyUsbDeviceHelper;
import com.uhm.uhmcs.utils.UserUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PaymentListPopupWindow {
    private PopupWindow popupWindow;
    private Activity context;



    private RecyclerView payment_rv;

    private PaymentListAdapter paymentListAdapter;

    private TextView add_payment_btn;




    public PaymentListPopupWindow(Activity context){

        this.context = context;

        initPopup();
    }

    private void initPopup() {

        View popupView = LayoutInflater.from(context).inflate(R.layout.popupwindow_payment_list, null);
        popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                true
        );
//        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupView.setBackgroundColor(context.getColor(R.color.black60));
        popupWindow.setOutsideTouchable(true);

        popupView.findViewById(R.id.guanbi_btn).setOnClickListener(v -> {
            popupWindow.dismiss();
        });

        payment_rv=popupView.findViewById(R.id.payment_rv);
        payment_rv.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL,false));
        paymentListAdapter=new PaymentListAdapter();
        payment_rv.setAdapter(paymentListAdapter);
        paymentListAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            if (view.getId()==R.id.edit_btn){
                new AddPaymentPopupWindow(context,false,paymentListAdapter.getItem(position),text -> getPaymentList()).show();
            }
            if (view.getId()==R.id.delete_btn){
                new DeleteShopPopupWindow(context, "是否确认删除", new PopupWindowOnClickListener.DeleteShopOnClickListener() {
                    @Override
                    public void onClick(String text) {
                        deletePaymentMethod(paymentListAdapter.getItem(position).getId()+"");
                    }
                }).show();
            }
        });
        getPaymentList();
        add_payment_btn=popupView.findViewById(R.id.add_payment_btn);
        add_payment_btn.setOnClickListener(v ->{
            new AddPaymentPopupWindow(context,true,new PaymentBean.DataBean(),text -> getPaymentList()).show();
        });

    }
    private void getPaymentList() {
        Map<String, String> params = new HashMap<>();
        params.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid());
//        params.put("page", grouponGoods_page + "");
//        params.put("strip", "20");
        String url = POSApiSerview.POS_URL + POSApiSerview.listPaymentMethod;
        OkHttpUtil.postFormAsync(url, params,context, new OkHttpUtil.OkHttpCallback() {
            @Override
            public void onSuccess(String response) {
                Log.i("ttt", response);
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        PaymentBean paymentBean=new Gson().fromJson(response,PaymentBean.class);
                        if (paymentBean.getCode() == 1 ) {

                            paymentListAdapter.setNewData(paymentBean.getData());
                        }else {


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

    private void deletePaymentMethod(String id) {
        Map<String, String> params = new HashMap<>();
        params.put("id", id);
        String url = POSApiSerview.POS_URL + POSApiSerview.deletePaymentMethod;
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
                            if (code == 1 ) {
                                new DeleteShopPopupWindow(context,"删除成功",true).show();
                                getPaymentList();
                            }else {


                            }

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


    public void show() {
        View rootView = ((Activity) context).getWindow().getDecorView();
        popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, 0, 0);
    }
}
