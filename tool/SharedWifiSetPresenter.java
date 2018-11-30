package com.lingtuan.firefly.tool;

import android.annotation.SuppressLint;

import com.lingtuan.firefly.base.BasePresenterImpl;

import java.util.Map;

public class SharedWifiSetPresenter extends BasePresenterImpl<SharedWifiSetContract.View> implements SharedWifiSetContract.Presenter {
    public SharedWifiSetPresenter(SharedWifiSetContract.View view) {
        super(view);
    }


    @SuppressLint("CheckResult")
    @Override
    public void loadData(String url, Map<String, Object> map, String message) {
//        Api.getInstance().loadProjectList(url, map)
//                .subscribeOn(Schedulers.io())
//                .doOnSubscribe(disposable -> {
//                })
//                .map(bean -> bean)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(bean -> {
//                    view.onResult(bean, message);
//                }, throwable -> view.onError(throwable, message));
    }

    @SuppressLint("CheckResult")
    @Override
    public void loadPeopleTypeData(String url, String message) {
//        Api.getInstance().loadReopleType(url)
//                .subscribeOn(Schedulers.io())
//                .doOnSubscribe(disposable -> {
//                })
//                .map(bean -> bean)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(bean -> {
//                    view.onResult(bean, message);
//                }, throwable -> view.onError(throwable, message));
    }
}
