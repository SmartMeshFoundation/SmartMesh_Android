package com.lingtuan.firefly.wallet.contract;

import com.lingtuan.firefly.base.BasePresenter;
import com.lingtuan.firefly.base.BaseView;
import com.lingtuan.firefly.wallet.vo.TransVo;

import java.util.ArrayList;

public interface WalletSendDetailContract {

    interface Presenter extends BasePresenter{

        /**
         * get wallet send list
         * */
        void loadData(String contactAddress,String walletAddress);

        void onDestroy();
    }

    interface View extends BaseView<Presenter>{
        /**
         * get wallet send list success
         * @param transVos wallet send detail list
         * */
        void success(ArrayList<TransVo> transVos);

        /**
         * get wallet send list error
         * @param errorCode error code
         * @param errorMsg  error message
         * */
        void error(int errorCode, String errorMsg);
    }
}
