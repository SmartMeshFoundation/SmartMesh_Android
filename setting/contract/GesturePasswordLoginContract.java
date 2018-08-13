package com.lingtuan.firefly.setting.contract;

import com.lingtuan.firefly.base.BasePresenter;
import com.lingtuan.firefly.base.BaseView;
import com.lingtuan.firefly.wallet.vo.StorableWallet;

public interface GesturePasswordLoginContract {

    interface Presenter extends BasePresenter {

        /**
         * get wallet info
         * @param type gesture type
         * */
        StorableWallet initWalletInfo(int type);

        /**
         * set wallet selected
         * @param type  gesture type
         * @param index wallet selected
         * */
        void setWalletSelected(int type,int index);

        void gesturePasswordSuccess(int type,StorableWallet storableWallet);
    }

    interface View extends BaseView<Presenter> {

    }
}
