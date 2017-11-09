package com.lingtuan.firefly.wallet;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.wallet.vo.StorableWallet;

/**
 * 
 */
public class WalletHandler extends Handler {
	
	/** 正在创建 */
	public static final int WALLET_ING = 10000;
	/** 创建（导入）成功 */
	public static final int WALLET_SUCCESS = 10001;
	/** 创建(导入)失败 */
	public static final int WALLET_ERROR = 10002;

	/** 导入密码错误 */
	public static final int WALLET_PWD_ERROR = 10003;

	/** 导入重复错误 */
	public static final int WALLET_REPEAT_ERROR = 10004;

	/** 内存不足错误 */
	public static final int NO_MEMORY = 10005;
	private Context mContext;
	
	
	public WalletHandler(Context mContext) {
		this.mContext = mContext;
	}

	@Override
	public void handleMessage(Message msg) {
		
		switch (msg.what) {
		case WALLET_ING://正在导入
			
			break;
		case WALLET_SUCCESS://创建（导入）成功
			Intent intent = new Intent(Constants.WALLET_SUCCESS);
			StorableWallet storableWallet = (StorableWallet) msg.obj;
			intent.putExtra(Constants.WALLET_INFO,storableWallet);
			Utils.sendBroadcastReceiver(mContext, intent, false);
			break;
		case WALLET_ERROR://创建（导入）失败
			Utils.sendBroadcastReceiver(mContext, new Intent(Constants.WALLET_ERROR), false);
			break;
		case NO_MEMORY:
			Utils.sendBroadcastReceiver(mContext, new Intent(Constants.NO_MEMORY), false);
			break;
		case WALLET_PWD_ERROR://导入密码错误
			Utils.sendBroadcastReceiver(mContext, new Intent(Constants.WALLET_PWD_ERROR), false);
			break;
		case WALLET_REPEAT_ERROR://导入重复错误
			Utils.sendBroadcastReceiver(mContext, new Intent(Constants.WALLET_REPEAT_ERROR), false);
			break;

		}
	}
	
	
}
