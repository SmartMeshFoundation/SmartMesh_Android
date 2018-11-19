package com.lingtuan.wallet;

import android.annotation.SuppressLint;

import com.lingtuan.meshbox.base.BasePresenterImpl;

import java.util.Map;

public class ShowWalletAddressPresenter extends BasePresenterImpl<ShowWalletAddressContract.View> implements ShowWalletAddressContract.Presenter {
    public ShowWalletAddressPresenter(ShowWalletAddressContract.View view) {
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
