package com.uhm.uhmcs.app;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.uhm.uhmcs.http.NetworkErrorInterceptor;
import com.uhm.uhmcs.http.POSApiSerview;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "CrashHandler";
    private final Thread.UncaughtExceptionHandler mDefaultHandler;
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private final WeakReference<Context> mContextRef;
    private Context context;
    public static void init(Context context) {
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(context));
    }

    private CrashHandler(Context context) {
        mContextRef = new WeakReference<>(context.getApplicationContext());
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        this.context = context.getApplicationContext();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        mExecutor.execute(() -> {
            String crashLog = buildCrashReport(thread, ex);
//            saveToFile(crashLog);
            uploadToServer(crashLog);
            Log.i("ttt",">>>>>>"+crashLog);
        });

        if (isMainThread(thread)) {
            new Handler(Looper.getMainLooper()).postDelayed(() ->
                mDefaultHandler.uncaughtException(thread, ex), 300);
        } else {
            mDefaultHandler.uncaughtException(thread, ex);
        }
    }

    private String buildCrashReport(Thread thread, Throwable ex) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        @SuppressLint("DefaultLocale") String crashLog=String.format(
                "*** CRASH REPORT ***\n" +
                        "Time: %s\n" +
                        "Thread: %s\n" +
                        "Device: %s %s\n" +
                        "Android: %s (SDK %d)\n" +
                        "Stacktrace:\n%s",
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
                thread.getName(),
                Build.MANUFACTURER, Build.MODEL,
                Build.VERSION.RELEASE, Build.VERSION.SDK_INT,
                sw.toString()
        );


        return crashLog;
    }

    private boolean isMainThread(Thread thread) {
        return Looper.getMainLooper().getThread() == thread;
    }

    private void saveToFile(String log) {
        // 异步文件存储实现
        try {
            File dir = new File(context.getExternalFilesDir(null), "crash_logs");
            if (!dir.exists()) dir.mkdirs();

            File file = new File(dir,
                    "crash_" + new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(new Date()) + ".txt");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(log.getBytes(StandardCharsets.UTF_8));
            fos.close();
        } catch (Exception e) {
            Log.e(TAG, "Save crash log failed", e);
        }
    }

    private void uploadToServer(String log) {
        // 网络上传实现
        Map<String, String> params = new HashMap<>();
        params.put("content", log);
        params.put("order_sn","");
        String url = POSApiSerview.POS_URL+POSApiSerview.addHomeLog;


        FormBody.Builder formBuilder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            Log.i("ttt","??>>>"+entry.getKey()+">>>>"+entry.getValue());
            formBuilder.add(entry.getKey(), entry.getValue());
        }
        RequestBody formBody = formBuilder.build();

        Request.Builder builder = new Request.Builder()
                .url(url);
        builder.post(formBody);
        Request request = builder.build();

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // 设置日志级别
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS) // 连接超时
                .readTimeout(300, TimeUnit.SECONDS)    // 读取超时
                .writeTimeout(300, TimeUnit.SECONDS)   // 写入超时
                .addInterceptor(new NetworkErrorInterceptor()) // 先添加异常拦截器
                .addInterceptor(loggingInterceptor)   // 添加日志拦截器
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

            }
        });
    }
}
