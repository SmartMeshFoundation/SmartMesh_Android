package com.lingtuan.firefly.network.download;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

public class DownloadInterceptor implements Interceptor {
    private ProgressListener downloadListener;

    public DownloadInterceptor(ProgressListener downloadListener) {
        this.downloadListener = downloadListener;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        return response.newBuilder().body(
                new ProgressResponseBody(response.body(), downloadListener)).build();
    }
}
