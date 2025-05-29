package com.uhm.uhmcs.popupwindow;

import android.app.Activity;
import android.content.Intent;
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
import com.uhm.uhmcs.adapter.PaymentAdapter;
import com.uhm.uhmcs.adapter.PrintDeviceAdapter;
import com.uhm.uhmcs.bean.LastOrderBean;
import com.uhm.uhmcs.http.POSApiSerview;
import com.uhm.uhmcs.utils.MyUsbDeviceHelper;
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

public class PrintDevicePopupWindow {
    private PopupWindow popupWindow;
    private Activity context;



    private RecyclerView print_device_rv;
    private PrintDeviceAdapter printDeviceAdapter;
    private PopupWindowOnClickListener.PrintDeviceOnClickListener printDeviceOnClickListener;




    public PrintDevicePopupWindow(Activity context,PopupWindowOnClickListener.PrintDeviceOnClickListener printDeviceOnClickListener){

        this.context = context;
        this.printDeviceOnClickListener = printDeviceOnClickListener;


        initPopup();
    }

    private void initPopup() {
        View popupView = LayoutInflater.from(context).inflate(R.layout.popupwindow_print_device, null);
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

        print_device_rv=popupView.findViewById(R.id.print_device_rv);
        print_device_rv.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL,false));
        printDeviceAdapter=new PrintDeviceAdapter();
        print_device_rv.setAdapter(printDeviceAdapter);
        printDeviceAdapter.setNewData(MyUsbDeviceHelper.getInstance().getDeviceList());
        printDeviceAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            if (view.getId()==R.id.xuanze_btn){
                new DeleteShopPopupWindow(context, context.getString(R.string.Confirm_this_device), new PopupWindowOnClickListener.DeleteShopOnClickListener() {
                    @Override
                    public void onClick(String text) {
                        printDeviceOnClickListener.onClick(printDeviceAdapter.getItem(position));
                        popupWindow.dismiss();
                    }
                }).show();

            }
        });

    }



    public void show() {
        View rootView = ((Activity) context).getWindow().getDecorView();
        popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, 0, 0);
    }
}
