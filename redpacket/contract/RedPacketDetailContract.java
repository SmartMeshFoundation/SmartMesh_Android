package com.lingtuan.firefly.redpacket.contract;

import com.lingtuan.firefly.base.BasePresenter;
import com.lingtuan.firefly.base.BaseView;
import com.lingtuan.firefly.redpacket.bean.RedPacketBean;

import java.util.ArrayList;

public interface RedPacketDetailContract {


    interface Presenter extends BasePresenter {

    }

    interface View extends BaseView<Presenter> {

        void success(ArrayList<RedPacketBean> redPacketRecords);

        void error(int errorCode, String errorMsg);
    }
}
