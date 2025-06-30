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

        payment_rv=popupView.findViewById(R.id.payment_rv);
        payment_rv.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL,false));
//        printDeviceAdapter=new PrintDeviceAdapter(type);
//        payment_rv.setAdapter(printDeviceAdapter);

        getPaymentList();


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


    public void show() {
        View rootView = ((Activity) context).getWindow().getDecorView();
        popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, 0, 0);
    }
}
