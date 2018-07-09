package com.lingtuan.firefly.redpacket;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.TextView;

import com.lingtuan.firefly.util.Utils;

public class RedPacketBalancePresenter {

    private Context context;
    private TextView redBalanceSymbol;
    private TextView redBalanceBalance;
    private TextView redBalanceMoney;

    public RedPacketBalancePresenter(Context context){
        this.context = context;
    }

    public void init(TextView redBalanceSymbol,TextView redBalanceBalance,TextView redBalanceMoney){
        this.redBalanceSymbol = redBalanceSymbol;
        this.redBalanceBalance = redBalanceBalance;
        this.redBalanceMoney = redBalanceMoney;
    }

    /**
     * red packet recharge method
     * */
    public void rechargeMethod(){
        context.startActivity(new Intent(context,RedPacketRecharge.class));
        Utils.openNewActivityAnim((Activity) context,false);
    }

    /**
     * red packet withdraw method
     * */
    public void withDrawMethod(){
        context.startActivity(new Intent(context,RedPacketWithdraw.class));
        Utils.openNewActivityAnim((Activity) context,false);
    }

    /**
     * red packet recode
     * */
    public void redPacketRecode(){
        context.startActivity(new Intent(context,RedPacketBalanceRecord.class));
        Utils.openNewActivityAnim((Activity) context,false);
    }
}
