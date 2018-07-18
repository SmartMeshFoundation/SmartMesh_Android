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
     * @param redLeaveMessage    red packet leave message    红包留言
     * @param type                red packet type  true is an average red packet  false is hand red packet
     * @param type                红包类型  true 是均分红包   false 是拼手气红包
     * */
    @Override
    public void sendRedPacketMethod(Context context,String singleAmount,String redNumber,String redLeaveMessage,boolean type){
        if (TextUtils.isEmpty(singleAmount)){
            mView.showToastMessage(context.getString(R.string.red_packet_send_value_hint));
            return;
        }else{
            BigDecimal singleAmount1 = new BigDecimal(singleAmount);
            BigDecimal singleAmount2 = new BigDecimal("0.01");
            if (singleAmount1.compareTo(singleAmount2) < 0){
                mView.showToastMessage(context.getString(R.string.red_packet_send_value_hint));
                return;
            }
        }
        if (TextUtils.isEmpty(redNumber) || Integer.parseInt(redNumber) < 1){
            mView.showToastMessage(context.getString(R.string.red_packet_send_number_hint));
            return;
        }
        String redPacketType;
        if (type){
            redPacketType = "均分红包";
        }else{
            redPacketType = "拼手气红包";
        }
        if (TextUtils.isEmpty(redLeaveMessage.trim())){
            redLeaveMessage = context.getString(R.string.red_packet_send_leave_message);
        }
        MyToast.showToast(context,"发出了" + redNumber +"个红包\n单个金额是:" + singleAmount + "\n留言是：" + redLeaveMessage + "\n红包类型是" + redPacketType);
        mView.sendSuccess();
    }


    @Override
    public void start() {

    }
}
