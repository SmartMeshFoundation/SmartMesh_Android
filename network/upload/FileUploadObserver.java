package com.lingtuan.firefly.network.upload;

import rx.Subscriber;

public abstract class FileUploadObserver<T> extends Subscriber<T> {
    @Override
    public void onNext(T t) {
        onUpLoadSuccess(t);
    }

    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {
        onUpLoadFail(e);
    }

    //监听进度的改变
    public void onProgressChange(long bytesWritten, long contentLength) {
        onProgress((int) (bytesWritten * 100 / contentLength));
    }

    //上传成功的回调
    public abstract void onUpLoadSuccess(T t);

    //上传失败回调
    public abstract void onUpLoadFail(Throwable e);

    //上传进度回调
    public abstract void onProgress(int progress);

}
