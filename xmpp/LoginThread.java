package com.lingtuan.firefly.xmpp;

import android.os.Message;

import org.jivesoftware.smack.XMPPException;

/**
 * Log in XMPP server
 */
public class LoginThread extends Thread {

	private XmppHandler mHandler;
	private String username;
	private String password;
	
	public LoginThread(XmppHandler mHandler, String username, String password){
		this.mHandler = mHandler;
		this.username = username;
		this.password = password;
	}
	
	public void startThread(XmppHandler mHandler, String username, String password){
		this.mHandler = mHandler;
		this.username = username;
		this.password = password;
		this.start();
	}
	@Override
	public void run() {
		int logincode = 200;
		String errorMessage = "";
		try {
			if(XmppUtils.getInstance().isLogin())
				return;
			XmppUtils.isLogining = true;
			XmppUtils.getInstance().createConnection();
			XmppUtils.getInstance().login(username, password);
			mHandler.sendEmptyMessage(XmppHandler.LOGIN_SUCCESS);
			XmppUtils.isLogining = false;
			return;
		} catch (XMPPException e) {
			if(e.getXMPPError() != null){
				logincode = e.getXMPPError().getCode();
			}else{
				logincode = XmppUtils.LOGIN_ERROR_PWD;
			}
			errorMessage = e.getMessage();
			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
			errorMessage = e.getMessage();
			logincode = 404;
		}
		XmppUtils.isLogining = false;
		Message msg = new Message();
		msg.what = XmppHandler.LOGIN_ERROR;
		msg.arg1 = logincode;
		msg.obj = errorMessage;
		mHandler.sendMessage(msg);
	}
}
