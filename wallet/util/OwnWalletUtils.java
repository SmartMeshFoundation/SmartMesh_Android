package com.lingtuan.firefly.wallet.util;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.crypto.WalletUtils;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class OwnWalletUtils extends WalletUtils {

    // OVERRIDING THOSE METHODS BECAUSE OF CUSTOM WALLET NAMING (CUTING ALL THE TIMESTAMPTS FOR INTERNAL STORAGE)

    /**
     * 创建一个完整的钱包
     * @param password 密码
     * @param destinationDirectory  文件内容
     * */
    public static String generateFullNewWalletFile(String password, File destinationDirectory)
            throws NoSuchAlgorithmException, NoSuchProviderException,
            InvalidAlgorithmParameterException, CipherException, IOException {

        return generateNewWalletFile(password, destinationDirectory, true);
    }

    /**
     * 创建一个轻型的钱包 （比完整钱包快）
     * @param password 密码
     * @param destinationDirectory  文件内容
     * */
    public static String generateLightNewWalletFile(String password, File destinationDirectory)
            throws NoSuchAlgorithmException, NoSuchProviderException,
            InvalidAlgorithmParameterException, CipherException, IOException {

        return generateNewWalletFile(password, destinationDirectory, false);
    }

    /**
     * 创建钱包
     * @param password 密码
     * @param useFullScrypt 是否生成完整的钱包
     * */
    public static WalletFile generateNewWalletFile(String password, boolean useFullScrypt)
            throws CipherException, IOException, InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException {

        ECKeyPair ecKeyPair = Keys.createEcKeyPair();
        return generateWalletFile(password, ecKeyPair, useFullScrypt);
    }
    /**
     * 导入钱包
     * @param password 密码
     * @param keys 秘钥对
     * @param useFullScrypt 是否生成完整的钱包
     * */
    public static WalletFile importNewWalletFile(String password,ECKeyPair keys, boolean useFullScrypt)
            throws CipherException, IOException, InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException {

        return generateWalletFile(password, keys, useFullScrypt);
    }

    /**
     * 导入钱包
     * @param password 密码
     * @param ecKeyPair 秘钥对
     * @param useFullScrypt 是否生成完整的钱包
     * @return 钱包地址
     * */
    public static WalletFile generateWalletFile(String password, ECKeyPair ecKeyPair, boolean useFullScrypt)
            throws CipherException, IOException {

        WalletFile walletFile;
        if (useFullScrypt) {
            walletFile = Wallet.createStandard(password, ecKeyPair);
        } else {
            walletFile = Wallet.createLight(password, ecKeyPair);
        }
        return walletFile;
    }

    public static String getWalletFileName(WalletFile walletFile) {
        return walletFile.getAddress();
    }

}