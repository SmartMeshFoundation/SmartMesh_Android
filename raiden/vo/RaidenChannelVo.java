package com.lingtuan.firefly.raiden.vo;

import org.json.JSONObject;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by Administrator on 2018/1/24.
 */

public class RaidenChannelVo implements Serializable {

    //raiden channel address
    private String channelAddress;
    //raiden partner address
    private String partnerAddress;
    //raiden token address
    private String tokenAddress;
    //raiden balance
    private String balance;
    //raiden channel address
    private String state;

    public String getChannelAddress() {
        return channelAddress;
    }

    public void setChannelAddress(String channelAddress) {
        this.channelAddress = channelAddress;
    }

    public String getPartnerAddress() {
        return partnerAddress;
    }

    public void setPartnerAddress(String partnerAddress) {
        this.partnerAddress = partnerAddress;
    }

    public String getTokenAddress() {
        return tokenAddress;
    }

    public void setTokenAddress(String tokenAddress) {
        this.tokenAddress = tokenAddress;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public RaidenChannelVo parse(JSONObject object){
        if (object == null){
            return null;
        }
        setChannelAddress(object.optString("channel_address"));
        setPartnerAddress(object.optString("partner_address"));
        setTokenAddress(object.optString("token_address"));
        BigDecimal ONE_ETHER = new BigDecimal("1000000000000000000");
        String balance = new BigDecimal(object.optString("balance")).divide(ONE_ETHER,5, BigDecimal.ROUND_HALF_UP).toPlainString();
        setBalance(balance);
        setState(object.optString("state"));
        return this;
    }
}
