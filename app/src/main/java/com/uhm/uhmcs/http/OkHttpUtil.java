package com.uhm.uhmcs.http;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


import com.uhm.uhmcs.R;
import com.uhm.uhmcs.activity.LoginActivity;
import com.uhm.uhmcs.popupwindow.DeleteShopPopupWindow;
import com.uhm.uhmcs.popupwindow.PopupWindowOnClickListener;
import com.uhm.uhmcs.utils.UserUtils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * OKhttp
 */
public class OkHttpUtil {

    private static OkHttpClient client;
    private static final Map<String, String> globalHeaders = new HashMap<>(); // 全局请求头
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static Activity context;

    static {
        initClient(); // 初始化 OkHttpClient
    }

    /**
     * 初始化 OkHttpClient
     */
    private static void initClient() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // 设置日志级别

        client = new OkHttpClient.Builder()
                .connectTimeout(10000, TimeUnit.SECONDS) // 连接超时
                .readTimeout(10000, TimeUnit.SECONDS)    // 读取超时
                .writeTimeout(10000, TimeUnit.SECONDS)   // 写入超时
                .addInterceptor(new NetworkErrorInterceptor()) // 先添加异常拦截器
                .addInterceptor(loggingInterceptor)   // 添加日志拦截器
                .build();
    }

    /**
     * 设置全局请求头
     *
     * @param headers 全局请求头
     */
    public static void setGlobalHeaders(Map<String, String> headers) {
        globalHeaders.clear();
        globalHeaders.putAll(headers);
    }

    /**
     * 同步 GET 请求
     *
     * @param url 请求地址
     * @return 响应结果
     * @throws IOException 网络异常
     */
    public static String get(String url) throws IOException {
        return get(url, new HashMap<>());
    }

    /**
     * 同步 GET 请求（带请求头）
     *
     * @param url     请求地址
     * @param headers 请求头
     * @return 响应结果
     * @throws IOException 网络异常
     */
    public static String get(String url, Map<String, String> headers) throws IOException {
        Request request = buildRequest(url, headers, null, null);
        return executeRequest(request);
    }

    /**
     * 异步 GET 请求
     *
     * @param url      请求地址
     * @param callback 回调接口
     */
    public static void getAsync(String url, OkHttpCallback callback) {
        getAsync(url, new HashMap<>(), callback);
    }

    /**
     * 异步 GET 请求（带请求头）
     *
     * @param url      请求地址
     * @param headers  请求头
     * @param callback 回调接口
     */
    public static void getAsync(String url, Map<String, String> headers, OkHttpCallback callback) {
        Request request = buildRequest(url, headers, null, null);
        enqueueRequest(request, callback);
    }

    /**
     * 同步 POST 请求（表单）
     *
     * @param url    请求地址
     * @param params 请求参数
     * @return 响应结果
     * @throws IOException 网络异常
     */
    public static String postForm(String url, Map<String, String> params) throws IOException {
        return postForm(url, new HashMap<>(), params);
    }

    /**
     * 同步 POST 请求（表单，带请求头）
     *
     * @param url     请求地址
     * @param headers 请求头
     * @param params  请求参数
     * @return 响应结果
     * @throws IOException 网络异常
     */
    public static String postForm(String url, Map<String, String> headers, Map<String, String> params) throws IOException {
        FormBody.Builder formBuilder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            formBuilder.add(entry.getKey(), entry.getValue());
        }
        RequestBody formBody = formBuilder.build();
        Request request = buildRequest(url, headers, formBody, null);
        return executeRequest(request);
    }

    /**
     * 异步 POST 请求（表单）
     *
     * @param url      请求地址
     * @param params   请求参数
     * @param callback 回调接口
     */
    public static void postFormAsync(String url, Map<String, String> params,Activity context1, OkHttpCallback callback) {
        context=context1;
        postFormAsync(url, new HashMap<>(), params, callback);
    }

    /**
     * 异步 POST 请求（表单，带请求头）
     *
     * @param url      请求地址
     * @param headers  请求头
     * @param params   请求参数
     * @param callback 回调接口
     */
    public static void postFormAsync(String url, Map<String, String> headers, Map<String, String> params, OkHttpCallback callback) {
        FormBody.Builder formBuilder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            Log.i("ttt","??>>>"+entry.getKey()+">>>>"+entry.getValue());
            formBuilder.add(entry.getKey(), entry.getValue());
        }
        RequestBody formBody = formBuilder.build();
        Request request = buildRequest(url, headers, formBody, null);
        enqueueRequest(request, callback);
    }

    /**
     * 同步 POST 请求（JSON）
     *
     * @param url  请求地址
     * @param json JSON 数据
     * @return 响应结果
     * @throws IOException 网络异常
     */
    public static String postJson(String url, String json) throws IOException {
        return postJson(url, new HashMap<>(), json);
    }

    /**
     * 同步 POST 请求（JSON，带请求头）
     *
     * @param url     请求地址
     * @param headers 请求头
     * @param json    JSON 数据
     * @return 响应结果
     * @throws IOException 网络异常
     */
    public static String postJson(String url, Map<String, String> headers, String json) throws IOException {
        RequestBody body = RequestBody.create(json, JSON);
        Request request = buildRequest(url, headers, body, null);
        return executeRequest(request);
    }

    /**
     * 异步 POST 请求（JSON）
     *
     * @param url      请求地址
     * @param json     JSON 数据
     * @param callback 回调接口
     */
    public static void postJsonAsync(String url, String json, Activity context1, OkHttpCallback callback) {
        context=context1;
        postJsonAsync(url, new HashMap<>(), json, callback);
    }

    /**
     * 异步 POST 请求（JSON，带请求头）
     *
     * @param url      请求地址
     * @param headers  请求头
     * @param json     JSON 数据
     * @param callback 回调接口
     */
    public static void postJsonAsync(String url, Map<String, String> headers, String json, OkHttpCallback callback) {
        RequestBody body = RequestBody.create(json, JSON);
        Request request = buildRequest(url, headers, body, null);
        enqueueRequest(request, callback);
    }

    /**
     * 文件上传
     *
     * @param url      请求地址
     * @param file     文件
     * @param callback 回调接口
     */
    public static void uploadFile(String url, File file, OkHttpCallback callback) {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), RequestBody.create(file, MediaType.parse("application/octet-stream")))
                .build();
        Request request = buildRequest(url, new HashMap<>(), requestBody, null);
        enqueueRequest(request, callback);
    }

    /**
     * 构建请求
     */
    private static Request buildRequest(String url, Map<String, String> headers, RequestBody body, String method) {
        Request.Builder builder = new Request.Builder()
                .url(url);

        // 添加全局请求头
        for (Map.Entry<String, String> entry : globalHeaders.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }

        // 添加自定义请求头
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }

        // 设置请求方法
        if (body != null) {
            builder.post(body);
        } else if ("DELETE".equalsIgnoreCase(method)) {
            builder.delete();
        } else {
            builder.get();
        }

        return builder.build();
    }

    /**
     * 执行同步请求
     */
    private static String executeRequest(Request request) throws IOException {
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("请求失败，状态码: " + response.code());
            }
            return response.body().string();
        }
    }

    /**
     * 执行异步请求
     */
    private static void enqueueRequest(Request request, OkHttpCallback callback) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onFailure(e);
                        new DeleteShopPopupWindow(context, context.getString(R.string.no_network_detected),true).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String success=response.body().string();
                        JSONObject jsonObject=new JSONObject(success);
                        if (jsonObject.getString("msg").contains("失效")||jsonObject.getString("msg").contains("无效")){
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        if (context.isFinishing() || context.isDestroyed()){
                                            return;
                                        }
                                        if (UserUtils.getInstance().getLoginBase()==null){
                                            return;
                                        }
                                        new DeleteShopPopupWindow(context, true, jsonObject.getString("msg"), new PopupWindowOnClickListener.DeleteShopOnClickListener() {
                                            @Override
                                            public void onClick(String text) {

                                                UserUtils.getInstance().setLoginBase(context,null);

                                                Intent intent=new Intent(context, LoginActivity.class);
                                                context.startActivity(intent);
                                            }
                                        }).show();

                                    } catch (JSONException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            });

                        }else {
                            callback.onSuccess(success);
                        }

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    callback.onFailure(new IOException("请求失败，状态码: " + response.code()));
                }
            }
        });
    }

    /**
     * 自定义回调接口
     */
    public interface OkHttpCallback {
        void onSuccess(String response);

        void onFailure(IOException e);
    }
}