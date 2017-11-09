package com.lingtuan.firefly.wallet;

import android.content.Context;
import android.os.Message;
import android.text.TextUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingtuan.firefly.util.SDCardCtrl;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.wallet.util.OwnWalletUtils;
import com.lingtuan.firefly.wallet.util.WalletStorage;
import com.lingtuan.firefly.wallet.vo.StorableWallet;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.protocol.ObjectMapperFactory;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

/**
 * 创建或者导入钱包
 * */
public class WalletThread extends Thread {

	private WalletHandler mHandler;//钱包Handler
	private String walletName;//钱包名称
	private String password;//钱包密码
	private String pwdInfo;//密码提示信息
	private String source;//type 1是privatekey 2是keyStore
	private int type;//type 0 创建钱包  1 私钥导入钱包  2 keyStore导入钱包
	private Context context;

	public WalletThread(WalletHandler mHandler, Context context,String walletName, String password,String pwdInfo,String source,int type){
		this.mHandler = mHandler;
		this.context = context;
		this.walletName = walletName;
		this.password = password;
		this.pwdInfo = pwdInfo;
		this.source = source;
		this.type = type;
	}
	
	public void startThread(WalletHandler mHandler, Context context, String walletName, String password,String source,int type){
		this.mHandler = mHandler;
		this.context = context;
		this.walletName = walletName;
		this.password = password;
		this.pwdInfo = pwdInfo;
		this.source = source;
		this.type = type;
		this.start();
	}
	@Override
	public void run() {
		try {

			String walletAddress ;
			if(type == 0) { // 生成新的地址 0x.........
				WalletFile walletFile = OwnWalletUtils.generateNewWalletFile(password, false);
				walletAddress = OwnWalletUtils.getWalletFileName(walletFile);
				File destination = new File( new File(context.getFilesDir(), SDCardCtrl.WALLERPATH), walletAddress);
				ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
				objectMapper.writeValue(destination, walletFile);

			} else if (type == 1){ // 通过私钥导入新地址
				ECKeyPair keys = ECKeyPair.create(new BigInteger(source));
				WalletFile  walletFile = OwnWalletUtils.generateWalletFile(password, keys, false);
				walletAddress = OwnWalletUtils.getWalletFileName(walletFile);
				boolean exists = WalletStorage.getInstance(context).checkExists(walletAddress);
				if (exists){
					Message message = Message.obtain();
					message.what = WalletHandler.WALLET_REPEAT_ERROR;
					mHandler.sendMessage(message);
					return;
				}
				File destination = new File( new File(context.getFilesDir(), SDCardCtrl.WALLERPATH), walletAddress);
				ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
				objectMapper.writeValue(destination, walletFile);

			}else{//通过keyStore导入新地址
				ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
				WalletFile walletFile = objectMapper.readValue(source, WalletFile.class);
				Credentials credentials = Credentials.create(Wallet.decrypt(password, walletFile));
				credentials.getEcKeyPair().getPublicKey();
				walletFile = OwnWalletUtils.importNewWalletFile(password,credentials.getEcKeyPair(), false);
				walletAddress = OwnWalletUtils.getWalletFileName(walletFile);
				boolean exists = WalletStorage.getInstance(context).checkExists(walletAddress);
				if (exists){
					Message message = Message.obtain();
					message.what = WalletHandler.WALLET_REPEAT_ERROR;
					mHandler.sendMessage(message);
					return;
				}


				File destination = new File( new File(context.getFilesDir(), SDCardCtrl.WALLERPATH), walletAddress);
				objectMapper = ObjectMapperFactory.getObjectMapper();
				objectMapper.writeValue(destination, walletFile);
	    	}
			if (TextUtils.isEmpty(walletAddress)){
				mHandler.sendEmptyMessage(WalletHandler.
						WALLET_ERROR);
			}else{
				if(TextUtils.isEmpty(walletName))//导入时做操作
				{
					walletName = Utils.getWalletName(context);
				}

				StorableWallet storableWallet = new StorableWallet();
				storableWallet.setPublicKey(walletAddress);
				storableWallet.setWalletName(walletName);
				storableWallet.setPwdInfo(pwdInfo);
				if (type == 0){
					storableWallet.setCanExportPrivateKey(1);
				}
				if (WalletStorage.getInstance(context).get().size() <= 0){
					storableWallet.setSelect(true);
				}
				WalletStorage.getInstance(context).add(storableWallet,context);
				mHandler.sendEmptyMessage(WalletHandler.WALLET_SUCCESS);
			}
			return;
		} catch (CipherException e) {
			e.printStackTrace();
			mHandler.sendEmptyMessage(WalletHandler.WALLET_PWD_ERROR);
			return;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		}catch (NumberFormatException e){
			e.printStackTrace();
		}catch (RuntimeException e){
			e.printStackTrace();
			mHandler.sendEmptyMessage(WalletHandler.NO_MEMORY);
			return;
		}catch (Exception e)
		{
			e.printStackTrace();
		}
		mHandler.sendEmptyMessage(WalletHandler.WALLET_ERROR);
	}
}
