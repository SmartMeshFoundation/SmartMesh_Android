package com.lingtuan.firefly.network.download;

public interface ProgressListener {
    /**
     * progress  File size currently downloaded
     * total     File size
     * speed     Download speed
     * done      Whether the download is complete
     */
    void onProgress(long progress, long total, long speed, boolean done);

    void onStartDownload();

    void onFinishDownload();

    void onFail(String errorInfo);
}
