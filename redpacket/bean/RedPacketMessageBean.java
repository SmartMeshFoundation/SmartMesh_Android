package com.lingtuan.firefly.redpacket.bean;

import org.json.JSONObject;

/**
 * pay message
 * 支付通知
 * */
public class RedPacketMessageBean {

    /**
     * message type  withdraw success or failure
     * 消息类型 提现成功或者失败
     */
    private int messageType;

    /**
     * withdraw amount
     * 提现金额
     * */
    private String withdrawAmount;

    /**
     * Beneficiary
     * 收款方
     * */
    private String messageToAddress;

    /**
     * message time
     * 到账时间
     * */
    private long messageTime;

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public String getWithdrawAmount() {
        return withdrawAmount;
    }

    public void setWithdrawAmount(String withdrawAmount) {
        this.withdrawAmount = withdrawAmount;
    }

    public String getMessageToAddress() {
        return messageToAddress;
    }

    public void setMessageToAddress(String messageToAddress) {
        this.messageToAddress = messageToAddress;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }

    public RedPacketMessageBean parse(JSONObject object){
        if (object == null){
            return null;
        }
        return this;
    }
}
