package com.lingtuan.firefly.wallet.vo;

import org.json.JSONObject;

/**
 * Created on 2017/8/25.
 */

public class TransVo {
    private String address;
    private String value;
    private long time;
    private int type;// 0 eth 1 smt;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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
        setAddress(object.optString("address"));
        setTime(object.optLong("dateline"));
        setValue(object.optString("value"));
        return this;
    }
}
