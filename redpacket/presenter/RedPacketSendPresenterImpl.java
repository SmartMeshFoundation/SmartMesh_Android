package com.lingtuan.firefly.redpacket.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.redpacket.RedPacketDetailUI;
import com.lingtuan.firefly.redpacket.contract.RedPacketSendContract;
import com.lingtuan.firefly.util.MyToast;
import com.lingtuan.firefly.util.Utils;

import java.math.BigDecimal;

public class RedPacketSendPresenterImpl implements RedPacketSendContract.Presenter{

    private RedPacketSendContract.View mView;

    public RedPacketSendPresenterImpl(RedPacketSendContract.View view){
        this.mView = view;
        mView.setPresenter(this);
    }

    /**
     * send red packet
     * @param singleAmount        red packet single amount    红包单个金额
     * @param redNumber           red packet number           红包个数
     * @param isGroup            is group red packet    是否是群组红包
     * */
    @Override
    public void checkRedPacket(String singleAmount,String redNumber,boolean isGroup){
        if (isGroup && (TextUtils.isEmpty(redNumber) || Integer.parseInt(redNumber) < 1)){
            mView.error(0,NextApplication.mContext.getString(R.string.red_packet_send_number_hint));
            return;
        }

        if (TextUtils.isEmpty(singleAmount)){
            mView.error(0,NextApplication.mContext.getString(R.string.red_packet_send_value_hint));
            return;
        }else{
            BigDecimal singleAmount1 = new BigDecimal(singleAmount);
            BigDecimal singleAmount2 = new BigDecimal("0.01");
            if (singleAmount1.compareTo(singleAmount2) < 0){
                mView.error(0,NextApplication.mContext.getString(R.string.red_packet_send_value_hint));
                return;
            }
        }
        mView.checkSuccess();
    }

    /**
     * send red packet
     * @param singleAmount        red packet single amount    红包单个金额
     * @param redNumber           red packet number           红包个数
     * @param redLeaveMessage    red packet leave message    红包留言
     * @param type                red packet type  true is an average red packet  false is hand red packet
     *                             红包类型  true 是均分红包   false 是拼手气红包
     * @param isGroup            is group red packet    是否是群组红包
     * */
    @Override
    public void sendRedPacket(String singleAmount, String redNumber, String redLeaveMessage, boolean type, boolean isGroup) {
        if (TextUtils.isEmpty(redLeaveMessage.trim())){
            redLeaveMessage = NextApplication.mContext.getString(R.string.red_packet_send_leave_message);
        }
        mView.success();
    }


    @Override
    public void start() {

    }
}
