package com.lingtuan.firefly.wallet;

import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.text.TextUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.setting.GesturePasswordLoginActivity;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.SDCardCtrl;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;
import com.lingtuan.firefly.wallet.util.OwnWalletUtils;
import com.lingtuan.firefly.wallet.util.WalletStorage;
import com.lingtuan.firefly.wallet.vo.StorableWallet;

import org.json.JSONObject;
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
 * Create or import the wallet
 * */
public class WalletThread extends Thread {

	private WalletHandler mHandler;//The wallet Handler
	private String walletName;//Name of the wallet
	private String password;//The wallet password
	private String pwdInfo;//Password prompt information
	private String source;//Type 1 is a privatekey 2 is a keyStore
	private int type;//Type 0 create wallets, 1 private key import wallet, 2 keyStore into the purse
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

	@Override
	public void run() {
		try {

			String walletAddress ;
			if(type == 0) { // Generate a new address 0 x...
				WalletFile walletFile = OwnWalletUtils.generateNewWalletFile(password, false);
				walletAddress = OwnWalletUtils.getWalletFileName(walletFile);
				File destination = new File( new File(context.getFilesDir(), SDCardCtrl.WALLERPATH), walletAddress);
				ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
				objectMapper.writeValue(destination, walletFile);

			} else if (type == 1){ // By private key into the new address
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

			}else{//Through the keyStore import new address
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
				mHandler.sendEmptyMessage(WalletHandler.WALLET_ERROR);
			}else{
				if(TextUtils.isEmpty(walletName)){//When import operation
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
				int walletMode = MySharedPrefs.readInt(NextApplication.mContext, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_IS_WALLET_PATTERN);
				if (walletMode == 1){
					MySharedPrefs.writeInt(NextApplication.mContext, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_IS_WALLET_PATTERN, 2);
					WalletStorage.getInstance(context).addWalletList(storableWallet,context);
				}else{
					WalletStorage.getInstance(context).add(storableWallet,context);
				}
				if (WalletStorage.getInstance(context).get().size() > 0){
					WalletStorage.getInstance(NextApplication.mContext).updateMapDb(storableWallet.getPublicKey());
					WalletStorage.getInstance(NextApplication.mContext).updateWalletToList(NextApplication.mContext,storableWallet.getPublicKey(),false);
				}
				mHandler.sendEmptyMessage(WalletHandler.WALLET_SUCCESS);
				addAddressMethod(walletAddress);
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

	/**
	 * add wallet address
	 * @param address address
	 * */
	private void addAddressMethod(String address){

		if (!address.startsWith("0x")){
			address = "0x" + address;
		}

		NetRequestImpl.getInstance().addAddress(address, new RequestListener() {
			@Override
			public void start() {

			}

			@Override
			public void success(JSONObject response) {

			}

			@Override
			public void error(int errorCode, String errorMsg) {

			}
		});
	}
}
