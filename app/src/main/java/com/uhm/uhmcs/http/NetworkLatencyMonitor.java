package com.uhm.uhmcs.http;

import android.os.Handler;
import android.os.Looper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkLatencyMonitor {
    private static final String PING_CMD = "/system/bin/ping -c 1 8.8.8.8";
    private static final int INTERVAL = 2000; // 2秒更新间隔
    private final Handler handler = new Handler(Looper.getMainLooper());
    private LatencyCallback callback;

    public interface LatencyCallback {
        void onLatencyUpdate(int pingMs, int httpMs);
    }

    public void startMonitoring(LatencyCallback callback) {
        this.callback = callback;
        handler.post(monitorRunnable);
    }

    private final Runnable monitorRunnable = new Runnable() {
        @Override
        public void run() {
            new Thread(() -> {
                int ping = measurePingLatency();
                int http = measureHttpLatency("https://posvox.com");
                handler.post(() -> {
                    if (callback != null) {
                        callback.onLatencyUpdate(ping, http);
                    }
                });
                handler.postDelayed(this, INTERVAL);
            }).start();
        }
    };

    private int measurePingLatency() {
        try {
            Process process = Runtime.getRuntime().exec(PING_CMD);
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("time=")) {
                    String time = line.split("time=")[1].split(" ")[0];
                    return (int) Float.parseFloat(time);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private int measureHttpLatency(String url) {
        long startTime = System.currentTimeMillis();
        try {
            HttpURLConnection connection = (HttpURLConnection) 
                new URL(url).openConnection();
            connection.setConnectTimeout(3000);
            connection.setRequestMethod("HEAD");
            connection.connect();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return (int) (System.currentTimeMillis() - startTime);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void stopMonitoring() {
        handler.removeCallbacks(monitorRunnable);
    }
}
