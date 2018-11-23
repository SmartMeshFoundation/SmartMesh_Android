package com.lingtuan.firefly.spectrum;


import android.text.TextUtils;

import com.lingtuan.meshbox.App;
import com.lingtuan.meshbox.base.BasePresenterImpl;
import com.lingtuan.meshbox.network.BaseModelImpl;
import com.lingtuan.meshbox.network.URLHeaderBuilder;
import com.lingtuan.meshbox.network.UrlConstansApi;
import com.lingtuan.meshbox.utils.LoadingDialog;
import com.lingtuan.meshbox.wallet.WalletThread;

import java.util.HashMap;
import java.util.Map;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class BindingWalletPresenter extends BasePresenterImpl<BindingWalletContract.View> implements BindingWalletContract.Presenter {

    public BindingWalletPresenter(BindingWalletContract.View view) {
        super(view);
    }


    @Override
    public void getPrivateKey(int type, String inputString, String keyStorePwd) {
        if (TextUtils.isEmpty(inputString)){
            LoadingDialog.close();
            return;
        }
        if (type == 0){
            WalletThread walletThread = new WalletThread(0);
            walletThread.setPrivateKey(inputString);
            walletThread.start();
        } else if (type == 1) {
            if (TextUtils.isEmpty(keyStorePwd)){
                LoadingDialog.close();
                return;
            }
            WalletThread walletThread = new WalletThread(1);
            walletThread.setPassword(keyStorePwd);
            walletThread.setKeyStore(inputString);
            walletThread.start();
        } else {
            WalletThread walletThread = new WalletThread(2);
            walletThread.setMnemonic(inputString);
            walletThread.start();
        }
    }


    @Override
    public void getKey(String message) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("method", "photon_get_key");
            BaseModelImpl.getInstance().getSysMountsWdctl(UrlConstansApi.BASE_URL_WDCTL + App.sUserVo.getMeshBoxToken(), map)
                    .subscribeOn(rx.schedulers.Schedulers.io())
                    .doOnSubscribe(() -> {
                    })
                    .map(bean -> bean)
                    .observeOn(rx.android.schedulers.AndroidSchedulers.mainThread())
                    .subscribe(bean -> {
                        view.onResult(bean, message);
                    }, throwable -> view.onError(throwable, message));
        } catch (Exception e) {
            view.onError(null, null);
        }
    }

    @Override
    public void storePrivateKey(String message,String desPrivateKey,String md5PrivateKey) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("method", "photon_store_privatekey");
            map.put("params", new Object[]{desPrivateKey, md5PrivateKey});
            BaseModelImpl.getInstance().getSysMountsWdctl(UrlConstansApi.BASE_URL_WDCTL + App.sUserVo.getMeshBoxToken(), map)
                    .subscribeOn(rx.schedulers.Schedulers.io())
                    .doOnSubscribe(() -> {
                    })
                    .map(bean -> bean)
                    .observeOn(rx.android.schedulers.AndroidSchedulers.mainThread())
                    .subscribe(bean -> {
                        view.onResult(bean, message);
                    }, throwable -> view.onError(throwable, message));
        } catch (Exception e) {
            view.onError(null, null);
        }
    }

    @Override
    public void bindWallet(String message,String walletAddress) {
        try {
            HashMap<String, Object> map = new HashMap<>();
            map.put("smt", walletAddress);
            map.put("mac", App.mScanData.getDeviceMac());
            map.put("sn", App.mScanData.getSn());
            BaseModelImpl.getInstance().bindWallet(URLHeaderBuilder.getInatance().parseHeader(map.toString()), map)
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe(() -> {
                    })
                    .map(bean -> bean)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(bean -> {
                        view.onResult(bean, message);
                    }, throwable -> view.onError(throwable, message));
        } catch (Exception e) {
            view.onError(null, null);
        }
    }

    @Override
    public void exec(String message) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("method", "exec");
            map.put("params", new Object[]{"/etc/init.d/photon restart"});
            BaseModelImpl.getInstance().getSysMountsWdctl(UrlConstansApi.BASE_URL_WDCTL + App.sUserVo.getMeshBoxToken(), map)
                    .subscribeOn(rx.schedulers.Schedulers.io())
                    .doOnSubscribe(() -> {
                    })
                    .map(bean -> bean)
                    .observeOn(rx.android.schedulers.AndroidSchedulers.mainThread())
                    .subscribe(bean -> {
                        view.onResult(bean, message);
                    }, throwable -> view.onError(throwable, message));
        } catch (Exception e) {
            view.onError(null, null);
        }
    }
}
