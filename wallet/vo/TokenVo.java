package com.lingtuan.firefly.wallet.vo;

import org.json.JSONObject;

/**
 * Created on 2018/3/19.
 * token info
 */

public class TokenVo {

    //token img
    private String tokenPic;
    //token name
    private String tokenName;
    //token info
    private String tokenInfo;
    //token number
    private String tokenNumber;
    //token price
    private String tokenPrice;
    //token total price
    private String tokenTotalPrice;
    //token is checked
    private boolean isChecked;

    public String getTokenPic() {
        return tokenPic;
    }

    public void setTokenPic(String tokenPic) {
        this.tokenPic = tokenPic;
    }

    public String getTokenName() {
        return tokenName;
    }

    public void setTokenName(String tokenName) {
        this.tokenName = tokenName;
    }

    public String getTokenInfo() {
        return tokenInfo;
    }

    public void setTokenInfo(String tokenInfo) {
        this.tokenInfo = tokenInfo;
    }

    public String getTokenNumber() {
        return tokenNumber;
    }

    public void setTokenNumber(String tokenNumber) {
        this.tokenNumber = tokenNumber;
    }

    public String getTokenPrice() {
        return tokenPrice;
    }

    public void setTokenPrice(String tokenPrice) {
        this.tokenPrice = tokenPrice;
    }

    public String getTokenTotalPrice() {
        return tokenTotalPrice;
    }

    public void setTokenTotalPrice(String tokenTotalPrice) {
        this.tokenTotalPrice = tokenTotalPrice;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public TokenVo parse(JSONObject object){
        return this;
    }

}
