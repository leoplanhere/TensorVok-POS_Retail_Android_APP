package com.uhm.uhmcs.utils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.widget.Toast;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NetworkUtils {
    private static NetworkUtils instance;
    /**
     * 获取单件实例
     *
     */
    public static NetworkUtils getInstance() {
        if (null == instance)
            instance = new NetworkUtils();
        return instance;
    }

    // 检查设备是否连接到网络（WiFi/移动数据）
    public  boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        Network network = cm.getActiveNetwork();
        if (network == null) return false;
        NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    // 验证互联网实际可达性
    public  boolean isInternetAvailable() {
        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(3, TimeUnit.SECONDS)
                    .readTimeout(3, TimeUnit.SECONDS)
                    .build();

            Request request = new Request.Builder()
                    .url("https://www.google.com")
                    .head()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                return response.isSuccessful();
            }
        } catch (IOException e) {
            return false;
        }
    }
    // 综合检测网络状态
    private void checkNetworkStatus(Activity activity) {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            boolean isConnected = isNetworkConnected(activity);
            boolean isInternetAvailable = isConnected && isInternetAvailable();

            activity.runOnUiThread(() -> {
                if (isInternetAvailable) {
                    Toast.makeText(activity, "网络可用", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(activity, "网络不可用", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
    // 网络变化广播接收器
    private class NetworkChangeReceiver extends BroadcastReceiver {
        private final Activity activity;

        public NetworkChangeReceiver(Activity activity) {
            this.activity = activity;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            checkNetworkStatus(activity);
        }
    }

    // 注册网络状态监听
    private NetworkChangeReceiver registerNetworkReceiver(Activity context) {
        NetworkChangeReceiver receiver;
        receiver = new NetworkChangeReceiver(context);
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(receiver, filter);
        return receiver;
    }

}

