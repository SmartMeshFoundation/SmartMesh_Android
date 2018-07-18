package com.lingtuan.firefly.redpacket.contract;

import com.lingtuan.firefly.base.BasePresenter;
import com.lingtuan.firefly.base.BaseView;

public interface RedPacketBalanceContract {

    interface Presenter extends BasePresenter{

    }

    interface View extends BaseView<Presenter>{
        void refreshUI();
    }
}
