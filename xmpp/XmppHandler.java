package com.lingtuan.firefly.xmpp;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.service.XmppService;
import com.lingtuan.firefly.util.Utils;

/**
 * Xmpp asynchronous unified handling
 */
public class XmppHandler extends Handler {
	/** Login successful */
	public static final int LOGIN_SUCCESS = 10001;
	/** Login failed */
	public static final int LOGIN_ERROR = 10002;
	
	private Context mContext;
	
	
	public XmppHandler(Context mContext) {
		this.mContext = mContext;
	}

	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
			case LOGIN_SUCCESS://Login successful
				Utils.intentAction(NextApplication.mContext, XmppAction.ACTION_LOGIN_SUCCESS,null);
				sendPackageListener();
				break;
			case LOGIN_ERROR://Login failed
				Bundle bundle = new Bundle();//The error message
				bundle.putStringArray(XmppAction.ACTION_LOGIN_ERROR,new String[]{msg.arg1+"",(String) msg.obj});
				Utils.intentAction(NextApplication.mContext, XmppAction.ACTION_LOGIN_ERROR,bundle);
				break;
		}
	}

	private void sendPackageListener(){
		Utils.intentService(mContext, XmppService.class, XmppAction.ACTION_LOGIN_MESSAGE_LISTENER,null,null);
	}
	
}
