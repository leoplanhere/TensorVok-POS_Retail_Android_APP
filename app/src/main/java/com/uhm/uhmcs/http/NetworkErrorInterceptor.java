package com.uhm.uhmcs.http;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLHandshakeException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class NetworkErrorInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        try {
            Response response = chain.proceed(request);

            // 处理非200响应码
            if (!response.isSuccessful()) {
                throw new IOException("HTTP Error: " + response.code() + " - " + response.message());
            }
            return response;

        } catch (Exception e) {
            // 统一转换异常类型
            String errorMsg = "网络请求失败: ";
            if (e instanceof UnknownHostException) {
                errorMsg += "域名解析失败";
            } else if (e instanceof SocketTimeoutException) {
                errorMsg += "请求超时";
            } else if (e instanceof SSLHandshakeException) {
                errorMsg += "SSL证书错误";
            } else {
                errorMsg += e.getMessage();
            }

            // 抛出统一异常（可自定义业务异常类）
            throw new IOException(errorMsg, e);
        }
    }
}

