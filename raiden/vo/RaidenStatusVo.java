package com.lingtuan.firefly.raiden.vo;

import org.json.JSONObject;

public class RaidenStatusVo {

    /**
     * XMPPStatus : 0
     * EthStatus : 3
     * LastBlockTime : 09-05|07:49:11.878
     */

    private int XMPPStatus;
    private int EthStatus;
    private String LastBlockTime;

    public int getXMPPStatus() {
        return XMPPStatus;
    }

    public void setXMPPStatus(int XMPPStatus) {
        this.XMPPStatus = XMPPStatus;
    }

    public int getEthStatus() {
        return EthStatus;
    }

    public void setEthStatus(int EthStatus) {
        this.EthStatus = EthStatus;
    }

    public String getLastBlockTime() {
        return LastBlockTime;
    }

    public void setLastBlockTime(String LastBlockTime) {
        this.LastBlockTime = LastBlockTime;
    }

    public RaidenStatusVo parse(JSONObject obj) {
        setEthStatus(obj.optInt("EthStatus"));
        setXMPPStatus(obj.optInt("XMPPStatus"));
        setLastBlockTime(obj.optString("LastBlockTime"));
        return this;
    }
}
