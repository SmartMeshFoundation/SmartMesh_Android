package com.lingtuan.firefly.redpacket.bean;

import org.json.JSONObject;

public class RedPacketRecordBean {

    /**
     * type
     * 交易类型
     * */
    private int redRecordType;

    /**
     * address
     * 交易地址
     * */
    private String redRecordAddress;

    /**
     * amount
     * 交易金额
     * */
    private String redRecordAmount;

    /**
     * time
     * 交易时间
     * */
    private long redRecordTime;

    public int getRedRecordType() {
        return redRecordType;
    }

    public void setRedRecordType(int redRecordType) {
        this.redRecordType = redRecordType;
    }

    public String getRedRecordAddress() {
        return redRecordAddress;
    }

    public void setRedRecordAddress(String redRecordAddress) {
        this.redRecordAddress = redRecordAddress;
    }

    public String getRedRecordAmount() {
        return redRecordAmount;
    }

    public void setRedRecordAmount(String redRecordAmount) {
        this.redRecordAmount = redRecordAmount;
    }

    public long getRedRecordTime() {
        return redRecordTime;
    }

    public void setRedRecordTime(long redRecordTime) {
        this.redRecordTime = redRecordTime;
    }

    public RedPacketRecordBean parse(JSONObject object){
        if (object == null){
            return null;
        }
        return this;
    }
}
