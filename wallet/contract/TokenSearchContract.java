package com.lingtuan.firefly.wallet.contract;

import com.lingtuan.firefly.base.BasePresenter;
import com.lingtuan.firefly.base.BaseView;
import com.lingtuan.firefly.wallet.vo.TokenVo;

import java.util.ArrayList;

public interface TokenSearchContract {

    interface Presenter extends BasePresenter {

        /**
         * search token
         * @param keyword          search key
         * @param address          wallet address
         * @param localSource     local source
         * */
        void searchToken(String keyword,String address,ArrayList<TokenVo> localSource);

        /**
         * bind token
         * @param address           wallet address
         * @param token_address    token address
         * @param position          token position
         * */
        void bindTokenToList(String address,String token_address,int position);
    }

    interface View extends BaseView<Presenter> {

        /**
         * search token success
         * @param tempSource     token list
         * */
        void searchTokenSuccess(ArrayList<TokenVo> tempSource);

        /**
         * search token error
         * @param errorCode      error code
         * @param errorMsg       error message
         * */
        void searchTokenError(int errorCode, String errorMsg);

        /**
         * bind token start
         * */
        void bindTokenToListStart();

        /**
         * bind token success
         * @param message   bind message
         * @param position  token position
         * */
        void bindTokenToListSuccess(String message ,int position);

        /**
         * bind token error
         * @param errorCode      error code
         * @param errorMsg       error message
         * */
        void bindTokenToListError(int errorCode, String errorMsg);

    }
}
