package com.lingtuan.firefly.redpacket;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.util.MyToast;
import com.lingtuan.firefly.util.Utils;

public class RedPacketSendPresenter {

    private Context context;

    public RedPacketSendPresenter(Context context){
        this.context = context;
        Utils.setStatusBar(context,3);
    }

    /**
     * send red packet
     * @param singleAmount        red packet single amount    红包单个金额
     * @param redNumber           red packet number           红包个数
     * @param redLeaveMessage    red packet leave message    红包留言
     * @param type                red packet type  true is an average red packet  false is hand red packet
     * @param type                红包类型  true 是均分红包   false 是拼手气红包
     * */
    public void sendRedPacketMethod(String singleAmount,String redNumber,String redLeaveMessage,boolean type){
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
        context.startActivity(new Intent(context,RedPacketDetailUI.class));
        Utils.openNewActivityAnim((Activity) context,false);
    }
}
