package com.lingtuan.firefly.spectrum;


import android.content.Context;

import com.lingtuan.meshbox.base.BasePresenter;
import com.lingtuan.meshbox.base.BaseView;

import java.util.Map;

public class BindingWalletContract {
    public interface View extends BaseView {

    }

    public interface Presenter extends BasePresenter {

        void getPrivateKey(int type, String inputString, String keyStorePwd);

        void getKey(String message);

        void storePrivateKey(String message,String desPrivateKey,String md5PrivateKey);

        void bindWallet(String message,String walletAddress);

        void exec(String message);
    }
}
