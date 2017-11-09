package com.lingtuan.firefly.wallet.vo;



import java.io.Serializable;

import geth.Account;

public class StorableWallet implements Serializable{

//    //钱包地址  公钥
    private String publicKey;

    //钱包名称
    private String walletName;
    //密码提示信息
    private String pwdInfo;

    //当前选中钱包
    private boolean isSelect;

    // 0不能导出 1能导出
    private int canExportPrivateKey;

    //当前选中钱包头像
    private int imgId;

    //以太坊余额
    private double ethBalance;

    //smt余额
    private double fftBalance;

    // 0 ，默认钱包  1 观察钱包
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
