package com.lingtuan.firefly.wallet.contract;

import com.lingtuan.firefly.base.BasePresenter;
import com.lingtuan.firefly.base.BaseView;
import com.lingtuan.firefly.wallet.vo.StorableWallet;
import com.lingtuan.firefly.wallet.vo.TokenVo;

import java.util.ArrayList;

public interface AccountContract {

    interface Presenter extends BasePresenter{

        /**
         * set wallet gesture
         * */
        boolean setWalletGesture();

        /**
         * get all transaction list
         * @param address wallet address
         * */
        void getAllTransactionList(String address);

        /**
         * get token list
         * @param tokenVos token list
         * @param address wallet address
         * @param isShowToast is show toast
         * */
        void getTokenList(ArrayList<TokenVo> tokenVos,String address,boolean isShowToast);

        /**
         * get token list
         * @param tokens token list
         * @param address wallet address
         * @param isShowToast is show toast
         * */
        void getBalance(ArrayList<TokenVo> tokens,String address,boolean isShowToast);

        /**
         * show pow timer
         * */
        void showPowTimer();

        /**
         * show cny timer
         * */
        void showCnyTimer();

        /**
         * cancel timer
         * */
        void cancelTimer();

        /**
         * cancel cny timer
         * */
        void cancelCnyTimer();

        /**
         * check language
         * @return is chinese
         * */
        boolean checkLanguage();

        /**
         * get wallet bean
         * @return wallet bean
         * */
        StorableWallet getStorableWallet();
    }

    interface View extends BaseView<Presenter>{
        /**
         * get token list success
         * @param tokens wallet send detail list
         * */
        void getTokenListSuccess(ArrayList<TokenVo> tokens,boolean isShowToast);

        /**
         * get token list error
         * */
        void getTokenListError(boolean isShowToast);

        /**
         * get token list success
         * @param tokens wallet send detail list
         * */
        void getBalanceSuccess(ArrayList<TokenVo> tokens,String total,String usdTotal);

        /**
         * get token list error
         * */
        void getBalanceError(int errorCode, String errorMsg,boolean isShowToast,ArrayList<TokenVo> tokens);

        /**
         * cancel home pop
         * */
        void cancelHomePop();

        /**
         * cancel cny view
         * */
        void cancelCnyView();
    }
}
