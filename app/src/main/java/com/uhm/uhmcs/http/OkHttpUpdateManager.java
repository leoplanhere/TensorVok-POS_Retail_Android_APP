package com.uhm.uhmcs.http;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Looper;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.util.logging.Handler;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;

public class OkHttpUpdateManager {
    private Context context;
    private OkHttpClient client;
    private String apkUrl;
    private ProgressListener progressListener;

    public interface ProgressListener {
        void onProgress(int progress);
        void onError(String error);
    }

    public OkHttpUpdateManager(Context context, ProgressListener listener) {
        this.context = context;
        this.progressListener = listener;
        this.client = new OkHttpClient.Builder()
                .addNetworkInterceptor(new ProgressInterceptor())
                .build();
    }



    private int getLocalVersionCode() {
        try {
            return context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return 0;
        }
    }

    private void showUpdateDialog(String updateInfo) {
        new AlertDialog.Builder(context)
                .setTitle("发现新版本")
                .setMessage(updateInfo)
                .setPositiveButton("更新", (dialog, which) -> startDownload())
                .setNegativeButton("取消", null)
                .show();
    }

    private void startDownload() {
        Request request = new Request.Builder().url(apkUrl).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                progressListener.onError("下载失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                saveApk(response);
            }
        });
    }

    private void saveApk(Response response) {
        try {
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File apkFile = new File(dir, "app_update.apk");

            BufferedSource source = response.body().source();
            BufferedSink sink = Okio.buffer(Okio.sink(apkFile));
            source.readAll(sink);
            sink.close();

            installApk(apkFile);
        } catch (IOException e) {
            progressListener.onError("文件保存失败");
        }
    }

    private void installApk(File apkFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", apkFile);
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(intent);
    }

    // 进度监听拦截器
    private class ProgressInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Response originalResponse = chain.proceed(chain.request());
            return originalResponse.newBuilder()
                    .body(new ProgressResponseBody(originalResponse.body()))
                    .build();
        }
    }

    // 自定义 ResponseBody 实现进度回调
    private class ProgressResponseBody extends ResponseBody {
        private final ResponseBody responseBody;
        private long totalBytesRead = 0;

        ProgressResponseBody(ResponseBody responseBody) {
            this.responseBody = responseBody;
        }

        @Override
        public MediaType contentType() {
            return responseBody.contentType();
        }

        @Override
        public long contentLength() {
            return responseBody.contentLength();
        }

        @Override
        public BufferedSource source() {
            return Okio.buffer(new ForwardingSource(responseBody.source()) {
                @Override
                public long read(Buffer sink, long byteCount) throws IOException {
                    long bytesRead = super.read(sink, byteCount);
                    totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                    int progress = (int) ((totalBytesRead * 100) / responseBody.contentLength());
                    progressListener.onProgress(progress);
                    return bytesRead;
                }
            });
        }
    }
}
