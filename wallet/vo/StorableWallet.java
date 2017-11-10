package com.lingtuan.firefly.wallet.vo;



import java.io.Serializable;

import geth.Account;

public class StorableWallet implements Serializable{

    private String publicKey;

    private String walletName;
    
    private String pwdInfo;

   
    private boolean isSelect;

    
    private int canExportPrivateKey;

    
    private int imgId;

   
    private double ethBalance;

   
    private double fftBalance;

    
    private int walletType;

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public int getCanExportPrivateKey() {
        return canExportPrivateKey;
    }

    public void setCanExportPrivateKey(int canExportPrivateKey) {
        this.canExportPrivateKey = canExportPrivateKey;
    }

    public int getWalletType() {
        return walletType;
    }

    public void setWalletType(int walletType) {
        this.walletType = walletType;
    }


    public double getEthBalance() {
        return ethBalance;
    }

    public void setEthBalance(double ethBalance) {
        this.ethBalance = ethBalance;
    }

    public double getFftBalance() {
        return fftBalance;
    }

    public void setFftBalance(double fftBalance) {
        this.fftBalance = fftBalance;
    }

    public int getImgId() {
        return imgId;
    }

    public void setImgId(int imgId) {
        this.imgId = imgId;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    public String getPwdInfo() {
        return pwdInfo;
    }

    public void setPwdInfo(String pwdInfo) {
        this.pwdInfo = pwdInfo;
    }


    public String getWalletName() {
        return walletName;
    }

    public void setWalletName(String walletName) {
        this.walletName = walletName;
    }

}
