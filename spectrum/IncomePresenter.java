package com.lingtuan.firefly.spectrum;

import android.annotation.SuppressLint;

import com.lingtuan.meshbox.App;
import com.lingtuan.meshbox.base.BasePresenterImpl;
import com.lingtuan.meshbox.network.BaseModelImpl;
import com.lingtuan.meshbox.network.URLHeaderBuilder;

import java.util.HashMap;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class IncomePresenter extends BasePresenterImpl<IncomeContract.View> implements IncomeContract.Presenter {

    public IncomePresenter(IncomeContract.View view) {
        super(view);
    }


    @SuppressLint("CheckResult")
    @Override
    public void loadData(String type) {
        try {
            if (App.mScanData == null){
                return;
            }
            HashMap<String, Object> map = new HashMap<>();
            map.put("mac",  App.mScanData.getDeviceMac());
            map.put("sn", App.mScanData.getSn());
            map.put("type", type);
            BaseModelImpl.getInstance().miningRevenue(URLHeaderBuilder.getInatance().parseHeader(map.toString()), map)
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe(() -> {
                    })
                    .map(bean -> bean)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(bean -> {
                        view.onResult(bean, null);
                    }, throwable -> view.onError(throwable, null));
        } catch (Exception e) {
            view.onError(e, null);
        }
    }
}
