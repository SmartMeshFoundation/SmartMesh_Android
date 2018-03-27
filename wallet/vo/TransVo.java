package com.lingtuan.firefly.wallet.vo;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created on 2017/8/25.
 * Transfer record
 */

public class TransVo implements Serializable{
    // from address
    private String fromAddress;
    // to address
    private String toAddress;

    //The amount of
    private String value;
    //time
    private long time;
    // 0 eth 1 smt,2 mesh;   now update  0 SMT  1 ERC20合约币
    private int type;
    //Transaction details web page address
    private String txurl;
    //gas fee
    private String fee;
    //Exchange block number
    private int txBlockNumber;
    //Transaction hash
    private String tx;
    //The latest block number
    private int blockNumber;
    //-1 Unpackaged 、0 Waits for 12 blocks to be confirmed 、1 Transaction Completed 、2 Transaction failed
    private int state;

    private int noticeType;//0 sender 1 receiver

    public int getNoticeType() {
        return noticeType;
    }

    public void setNoticeType(int noticeType) {
        this.noticeType = noticeType;
    }

    public String getTxurl() {
        return txurl;
    }

    public void setTxurl(String txurl) {
        this.txurl = txurl;
    }

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public int getTxBlockNumber() {
        return txBlockNumber;
    }

    public void setTxBlockNumber(int txBlockNumber) {
        this.txBlockNumber = txBlockNumber;
    }

    public String getTx() {
        return tx;
    }

    public void setTx(String tx) {
        this.tx = tx;
    }

    public int getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(int blockNumber) {
        this.blockNumber = blockNumber;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public TransVo parse(JSONObject object){
        if (object == null){
            return null;
        }
        setToAddress(object.optString("address"));
        setTime(object.optLong("dateline"));
        setValue(object.optString("value"));
        setTxurl(object.optString("txurl"));
        setFee(object.optString("fee"));
        setTxBlockNumber(object.optInt("txBlockNumber",0));
        setTx(object.optString("tx"));
        setBlockNumber(object.optInt("blockNumber",0));
        setState(object.optInt("state",-1));
        return this;
    }
}
