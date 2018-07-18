package com.lingtuan.firefly.redpacket.contract;

import android.content.Context;

import com.lingtuan.firefly.base.BasePresenter;
import com.lingtuan.firefly.base.BaseView;

public interface RedPacketSendContract {

    interface Presenter extends BasePresenter{

        /**
         * send red packet
         * @param singleAmount        red packet single amount    红包单个金额
         * @param redNumber           red packet number           红包个数
         * @param redLeaveMessage    red packet leave message    红包留言
         * @param type                red packet type  true is an average red packet  false is hand red packet
         * @param type                红包类型  true 是均分红包   false 是拼手气红包
         * */
        void sendRedPacketMethod(Context context,String singleAmount, String redNumber, String redLeaveMessage, boolean type);

    }

    interface View extends BaseView<Presenter>{

        void sendSuccess();

        void showToastMessage(String message);
    }
}
