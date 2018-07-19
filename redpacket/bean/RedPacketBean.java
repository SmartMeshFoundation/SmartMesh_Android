package com.lingtuan.firefly.redpacket.bean;

import org.json.JSONObject;

public class RedPacketBean {

    public RedPacketBean parse(JSONObject object){
        if (object == null){
            return null;
        }
        return this;
    }
}
