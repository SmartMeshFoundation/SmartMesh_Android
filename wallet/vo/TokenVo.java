package com.lingtuan.firefly.wallet.vo;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created on 2018/3/19.
 * token info
 */

public class TokenVo implements Serializable{

    //token img
    private String tokenLogo;
    //token name
    private String tokenName;
    //token symbol
    private String tokenSymbol;
    //token number
    private double tokenBalance;

    private String tokenStringBalance;

    //token contact address
    private String contactAddress;
    //token is checked
    private boolean isChecked;
    //token is final open
    private boolean fixed;
    //wallet address
    private String walletAddress;
    //Operation type 0 Delete the default display token  1 Add the default display token   2 Modify the token information
    private int state;

    //Token unit price (RMB)
    private String unitPrice;
    //Token estimated market value (US$)
    private String usdPrice;
    //Token price (US$)
    private String usdUnitPrice;
    //token price
    private String tokenPrice;

    public String getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(String unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getUsdPrice() {
        return usdPrice;
    }

    public void setUsdPrice(String usdPrice) {
        this.usdPrice = usdPrice;
    }

    public String getUsdUnitPrice() {
        return usdUnitPrice;
    }

    public void setUsdUnitPrice(String usdUnitPrice) {
        this.usdUnitPrice = usdUnitPrice;
    }

    public String getTokenPrice() {
        return tokenPrice;
    }

    public void setTokenPrice(String tokenPrice) {
        this.tokenPrice = tokenPrice;
    }

    public boolean isFixed() {
        return fixed;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    public String getWalletAddress() {
        return walletAddress;
    }

    public void setWalletAddress(String walletAddress) {
        this.walletAddress = walletAddress;
    }

    public String getTokenLogo() {
        return tokenLogo;
    }

    public void setTokenLogo(String tokenLogo) {
        this.tokenLogo = tokenLogo;
    }

    public String getTokenName() {
        return tokenName;
    }

    public void setTokenName(String tokenName) {
        this.tokenName = tokenName;
    }

    public String getTokenSymbol() {
        return tokenSymbol;
    }

    public void setTokenSymbol(String tokenSymbol) {
        this.tokenSymbol = tokenSymbol;
    }

    public double getTokenBalance() {
        return tokenBalance;
    }

    public void setTokenBalance(double tokenBalance) {
        this.tokenBalance = tokenBalance;
    }


    public String getContactAddress() {
        return contactAddress;
    }

    public void setContactAddress(String contactAddress) {
        this.contactAddress = contactAddress;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getTokenStringBalance() {
        return tokenStringBalance;
    }

    public void setTokenStringBalance(String tokenStringBalance) {
        this.tokenStringBalance = tokenStringBalance;
    }

    public TokenVo parse(JSONObject object){
        if (object == null){
            return null;
        }
        setContactAddress(object.optString("token_address"));
        setTokenName(object.optString("name"));
        setTokenSymbol(object.optString("symbol"));
        setTokenLogo(object.optString("logo"));
        setTokenBalance(object.optDouble("balance",0.000000));
        setTokenStringBalance(object.optString("balance"));
        setTokenPrice(object.optString("price"));
        setChecked(object.optInt("is_open",0) == 1);
        setFixed(object.optInt("fixed",0) == 1);
        setState(object.optInt("state"));
        setUnitPrice(object.optString("unit_price"));
        setUsdUnitPrice(object.optString("usd_unit_price"));
        setUsdPrice(object.optString("usd_price"));
        return this;
    }

}
