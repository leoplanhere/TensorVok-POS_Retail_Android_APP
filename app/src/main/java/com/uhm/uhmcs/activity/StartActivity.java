package com.uhm.uhmcs.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.dou361.dialogui.DialogUIUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.bean.PrintDataBean;
import com.uhm.uhmcs.bean.VersionBean;
import com.uhm.uhmcs.http.NetworkErrorInterceptor;
import com.uhm.uhmcs.http.OkHttpUtil;
import com.uhm.uhmcs.http.POSApiSerview;
import com.uhm.uhmcs.popupwindow.DeleteShopPopupWindow;
import com.uhm.uhmcs.popupwindow.PopupWindowOnClickListener;
import com.uhm.uhmcs.utils.MyPrinterHelper;
import com.uhm.uhmcs.utils.UserUtils;
import com.uhm.uhmcs.utils.Utilis;
import com.uhm.uhmcs.view.MyPresentation;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import constant.UiType;
import model.UiConfig;
import model.UpdateConfig;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import update.UpdateAppUtils;

public class StartActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);


        String url= POSApiSerview.POS_URL1+POSApiSerview.version;
        Request.Builder builder = new Request.Builder()
                .url(url);
        Request request = builder.build();
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // 设置日志级别
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS) // 连接超时
                .readTimeout(10, TimeUnit.SECONDS)    // 读取超时
                .writeTimeout(10, TimeUnit.SECONDS)   // 写入超时
                .addInterceptor(new NetworkErrorInterceptor()) // 先添加异常拦截器
                .addInterceptor(loggingInterceptor)   // 添加日志拦截器
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String success=response.body().string();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            VersionBean versionBean=new Gson().fromJson(success, VersionBean.class);
                            Log.i("ttt",">>VersionName>>"+Utilis.getVersionName(StartActivity.this));
                            if (versionBean.getVersionName().equals(Utilis.getVersionName(StartActivity.this))){
                                delaymillinon();
                            }else {
                                UpdateAppUtils.init(StartActivity.this);
                                UpdateConfig updateConfig=new UpdateConfig();
                                updateConfig.setAlwaysShow(true);//非强制更新时是否每次显示弹窗
                                updateConfig.setAlwaysShowDownLoadDialog(true);//飞强制更新时是否显示进度条
                                updateConfig.setForce(true);//是否强制更新
                                updateConfig.setApkSaveName("uhm");
                                updateConfig.setApkSavePath(Environment.getExternalStorageDirectory().getAbsolutePath() +"/UHM");
                                updateConfig.isShowNotification();
                                updateConfig.setShowDownloadingToast(true);
                                updateConfig.setServerVersionName(versionBean.getVersionName());
                                UiConfig uiConfig = new UiConfig();
                                uiConfig.setDownloadingBtnText("下载中");
                                uiConfig.setUiType(UiType.PLENTIFUL);
                                UpdateAppUtils
                                        .getInstance()
                                        .apkUrl(versionBean.getApkUrl())
                                        .updateConfig(updateConfig)
                                        .uiConfig(uiConfig)
                                        .updateTitle("发现新版本"+versionBean.getVersionName())
                                        .updateContent("版本更新优化")
                                        .update();
                            }
                        }
                    });
                } else {

                }
            }
        });
//        Map<String, String> params = new HashMap<>();
//        params.put("shop_id","DP2025021528377");
//        OkHttpUtil.postFormAsync("https://xlcc.uhimao.com/api/supermarket/getVersion", params, this, new OkHttpUtil.OkHttpCallback() {
//            @Override
//            public void onSuccess(String response) {
//
//            }
//
//            @Override
//            public void onFailure(IOException e) {
//
//            }
//        });

        ImageView ivLogo=findViewById(R.id.ivLogo);
        Glide.with(this)
                .load(R.drawable.qidongtu)
                .into(ivLogo);
    }

    public void delaymillinon(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i("ttt",">>>>>>>>>>>>>");

                if (UserUtils.getInstance().getLoginBase()!=null&&UserUtils.getInstance().getShopDataBean()!=null){
                    startActivity(new Intent(StartActivity.this, MainActivity.class));
                    finish();
                }else {
                    Intent intent=new Intent();
                    intent.setClass(StartActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }, 2000);
    }
}
