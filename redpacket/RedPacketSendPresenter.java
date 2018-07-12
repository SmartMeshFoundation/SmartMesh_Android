package com.lingtuan.firefly.redpacket;

import android.content.Context;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

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
     * @param type                red packet type  true is  hand red packet  false is an average red packet
     * @param type                红包类型  true 是拼手气红包   false 是均分红包
     * */
    public void sendRedPacketMethod(String singleAmount,String redNumber,String redLeaveMessage,boolean type){
        String redPacketType;
        if (type){
            redPacketType = "拼手气红包";
        }else{
            redPacketType = "均分红包";
        }
        MyToast.showToast(context,"发出了" + redNumber +"个红包\n单个金额是:" + singleAmount + "\n留言是：" + redLeaveMessage + "\n红包类型是" + redPacketType);
    }
}
