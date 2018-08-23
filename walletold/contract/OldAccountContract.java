package com.lingtuan.firefly.walletold.contract;

import android.content.Context;

import com.lingtuan.firefly.base.BasePresenter;
import com.lingtuan.firefly.base.BaseView;
import com.lingtuan.firefly.wallet.vo.StorableWallet;

public interface OldAccountContract {

    interface Presenter extends BasePresenter {

        /**
         * get wallet bean
         * @return wallet bean
         * */
        StorableWallet getStorableWallet();

        /**
         * get wallet balance
         * @param context context
         * @param address wallet address
         * @param isShowToast   show toast  trie or false
         * */
        void getBalance(Context context, String address,boolean isShowToast);

        /**
         * parse json
         * @param jsonString json string
         * */
        void parseJson(String jsonString);
    }

    interface View extends BaseView<Presenter> {

        /**
         * get balance failure
         * @param isShowToast show toast  trie or false
         * */
        void onFailure(boolean isShowToast);

        /**
         * ge balance success
         * @param string message
         * */
        void onResponse(String string);

        /**
         * reset data
         * @param ethBalance eth balance
         * @param smtBalance smt balance
         * @param meshBalance mesh balance
         * @param smtMapping  smt mapping state
         * */
        void resetData(double ethBalance,double smtBalance,double meshBalance,String smtMapping);

        /**
         * reset data error
         * @param message  message
         * */
        void resetDataError(String message);

    }
}
