package com.lingtuan.firefly.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.contact.AddFriendsUI;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.service.LoadDataService;
import com.lingtuan.firefly.setting.SecurityUI;
import com.lingtuan.firefly.setting.SettingUI;
import com.lingtuan.firefly.ui.MainFragmentUI;
import com.lingtuan.firefly.ui.SplashActivity;
import com.lingtuan.firefly.ui.WalletModeLoginUI;
import com.lingtuan.firefly.util.Aes;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.MyToast;
import com.lingtuan.firefly.util.MyViewDialogFragment;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;
import com.lingtuan.firefly.vo.UserInfoVo;
import com.lingtuan.firefly.wallet.util.WalletStorage;
import com.lingtuan.firefly.xmpp.XmppUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Login interface tools
 */
public class LoginUtil{

	private static LoginUtil instance;
	private Activity mContext;
	private String aeskey = null ;

	public static LoginUtil getInstance() {
		if (instance == null) {
			instance = new LoginUtil();
		}
		return instance;
	}

	public void initContext(Activity mContext){
		if(this.mContext == null){
			this.mContext = mContext;
		}

	}

	/**
	 * registered
	 * @ param uName user name
	 * @ param uPwd password
	 */
	public void register(final String uName, final String uPwd, final String mid, final String localid, final TextView uploadRegisterInfo){
		this.aeskey = Utils.makeRandomKey(16);
		NetRequestImpl.getInstance().register(uName, uPwd,mid,localid, aeskey, new RequestListener() {
			@Override
			public void start() {
				if (!TextUtils.isEmpty(mid)){
					LoadingDialog.show(mContext,"");
				}
			}

			@Override
			public void success(JSONObject response) {
				String token = response.optString("token");
				if (TextUtils.isEmpty(token)){
					return;
				}

				String 	tokenTmp = Aes.decode(aeskey,token);
			    if(!TextUtils.isEmpty(tokenTmp)){
					token = tokenTmp;
				}
				try {//Local encrypted token decrypted storage

					if (!TextUtils.isEmpty(mid)){
						LoadingDialog.close();
					}

					JSONObject info = new JSONObject();
					info.put("username", uName);
					info.put("password", uPwd);
					if (!TextUtils.isEmpty(mid)){
						info.put("mid", mid);
					}
					info.put("localid", localid);
					info.put("token",token);
					MySharedPrefs.write(mContext, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_LOGIN_USERINFO, info.toString());

					UserInfoVo userInfoVo = new UserInfoVo();
					userInfoVo.setUsername(uName);
					userInfoVo.setPassword(uPwd);
					if (!TextUtils.isEmpty(mid)){
						userInfoVo.setMid(mid);
					}
					userInfoVo.setLocalId(localid);
					userInfoVo.setToken(token);
					JsonUtil.updateLocalInfo(mContext,userInfoVo);

					NextApplication.myInfo = new UserInfoVo().readMyUserInfo(mContext);
					if (uploadRegisterInfo != null){
						uploadRegisterInfo.setVisibility(View.GONE);
						if (NextApplication.myInfo != null && TextUtils.isEmpty(NextApplication.myInfo.getMobile())&& TextUtils.isEmpty(NextApplication.myInfo.getEmail())) {
							MyViewDialogFragment mdf = new MyViewDialogFragment();
							mdf.setTitleAndContentText(mContext.getString(R.string.account_logout_mid_warn), mContext.getString(R.string.account_logout_email_hint));
							mdf.setOkCallback(new MyViewDialogFragment.OkCallback() {
								@Override
								public void okBtn() {
									mContext.startActivity(new Intent(mContext, SecurityUI.class));
									Utils.openNewActivityAnim(mContext,false);
								}
							});
							mdf.show(((AppCompatActivity)mContext).getSupportFragmentManager(), "mdf");
						}
					}


					//Login XMPP
					XmppUtils.loginXmppForNextApp(mContext);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public void error(int errorCode, String errorMsg) {
				if (!TextUtils.isEmpty(mid)){
					LoadingDialog.close();
					MyToast.showToast(mContext, errorMsg);
				}
			}
		});
	}
	
	/**
	 * login authentication
	 * @ param uName user name
	 * @ param uPwd password
	 * @ param showDialog whether to display the dialog
	 */
	public void login(final String uName, final String uPwd,final boolean showDialog){
		this.aeskey = Utils.makeRandomKey(16);
		NetRequestImpl.getInstance().login(uName, uPwd, aeskey, new RequestListener() {
			@Override
			public void start() {
				if (showDialog){
					LoadingDialog.show(mContext,"");
				}
			}

			@Override
			public void success(JSONObject response) {
				try {
					JSONObject object = response.optJSONObject("data");
					//Local encrypted token decrypted storage
					String token = object.optString("token");
					String mid = object.optString("mid");
					if (TextUtils.isEmpty(token)){
						return;
					}
					token = Aes.decode(aeskey,token);
					object.put("token",token);
					object.put("mid",mid);
					object.put("password",uPwd);
					MySharedPrefs.write(mContext, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_LOGIN_USERINFO, object.toString());
					UserInfoVo userInfoVo = new UserInfoVo().parse(object);
					JsonUtil.updateLocalInfo(mContext,userInfoVo);
					NextApplication.myInfo = new UserInfoVo().readMyUserInfo(mContext);
					XmppUtils.loginXmppForNextApp(mContext);
					getUserInfo(showDialog,mid,uPwd,token);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public void error(int errorCode, String errorMsg) {
				if (showDialog){
					LoadingDialog.close();
					MyToast.showToast(mContext,errorMsg);
				}
			}
		});
	}


	/**
	 * Get the user information
	 * */
	private void getUserInfo(final boolean showDialog,final String mid,final String uPwd,final String token){
		NetRequestImpl.getInstance().requestUserInfo(NextApplication.myInfo.getLocalId(),new RequestListener() {
			@Override
			public void start() {

			}

			@Override
			public void success(JSONObject response) {

				MySharedPrefs.reLoadWalletList();
				JSONObject object = response.optJSONObject("data");
				try {
					object.put("token",token);
					object.put("mid",mid);
					object.put("password",uPwd);
					object.put("localid",NextApplication.myInfo.getLocalId());
				} catch (JSONException e) {
					e.printStackTrace();
				}
				MySharedPrefs.write(mContext, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_LOGIN_USERINFO, object.toString());

				UserInfoVo userInfoVo = new UserInfoVo().parse(object);
				JsonUtil.updateLocalInfo(mContext,userInfoVo);

				if (showDialog){
					LoadingDialog.close();
					FinalUserDataBase.getInstance().close();
					MySharedPrefs.write(mContext, MySharedPrefs.FILE_USER, MySharedPrefs.IS_FIRST_LOGIN, "yes");
					mContext.startActivity(new Intent(mContext, MainFragmentUI.class));
					Utils.openNewActivityAnim(mContext, true);

					//Close the guide page
					Intent intent = new Intent(Constants.ACTION_CLOSE_GUID);
					Utils.sendBroadcastReceiver(mContext, intent, true);
				}
			}

			@Override
			public void error(int errorCode, String errorMsg) {
				if (showDialog){
					LoadingDialog.close();
				}
			}
		});
	}

	/**
	 * pure digital
	 * @ param STR string
	 */
	public static boolean isNumeric(String str){
		for (int i = str.length();--i>=0;){
			if (!Character.isDigit(str.charAt(i))){
				return false;
			}
		}
		return true;
	}


	//According to the registered bounced
	public void showRegistDialog(final TextView uploadRegisterInfo) {
		MyViewDialogFragment mdf = new MyViewDialogFragment(MyViewDialogFragment.DIALOG_SINGLE_EDIT, mContext.getString(R.string.account_update_mid),mContext.getString(R.string.mid_length_warning),null);
		mdf.setEditOkCallback(new MyViewDialogFragment.EditOkCallback() {
			@Override
			public void okBtn(String edittext) {
				if (isNumeric(edittext)){
					MyToast.showToast(mContext,mContext.getString(R.string.mid_content_warning));
					return;
				}
				uploadRegister(edittext,uploadRegisterInfo);
			}
		});
		mdf.show(((AppCompatActivity)mContext).getSupportFragmentManager(), "mdf");
	}

	/**
	 * upload the registration information
	 *The only marked * @ param mid user input
	 * */
	private void uploadRegister(String mid,TextView uploadRegisterInfo) {
		String jsonString = MySharedPrefs.readString(mContext, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_LOGIN_USERINFO);
		try {
			JSONObject object = new JSONObject(jsonString);
			String mName = object.optString("username");
			String mPwd = object.optString("password");
			String locailid = object.optString("localid");
			register(mName, mPwd,mid,locailid,uploadRegisterInfo);
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}


	public boolean checkUser(String uName, String uPass){
		return !TextUtils.isEmpty(uName) && !TextUtils.isEmpty(uPass);
	}

	public boolean registCheck(String username,String password){
		if (username.length() > 20 || username.length() <= 0){
			MyToast.showToast(mContext,mContext.getString(R.string.account_name_warning));
			return false;
		}
		if (password.length() > 18 || password.length() < 6){
			MyToast.showToast(mContext,mContext.getString(R.string.account_pwd_warning));
			return false;
		}
		return true;
	}

	/**
	 * registration verification
	 * @ param mPwd password
	 * @ param mAgainPwd secondary password
	 * @ param mName user name
	 * */
	public void registMethod(String mPwd,String mAgainPwd,String mName,String locailid){
		//Authentication codes are consistent
		if (!TextUtils.equals(mPwd,mAgainPwd)){
			MyToast.showToast(mContext,mContext.getString(R.string.account_pwd_again_warning));
			return;
		}

		//Verify account password is in accordance with the specification
		if (!LoginUtil.getInstance().registCheck(mName,mPwd)){
			return;
		}

		LoadingDialog.show(mContext,"");

		//Store the registration information Convenient synchronization
		UserInfoVo userInfoVo = new UserInfoVo();
		userInfoVo.setUsername(mName);
		userInfoVo.setPassword(mPwd);
		userInfoVo.setLocalId(locailid);

		try {
			JSONObject data = new JSONObject();
			data.put(JsonConstans.Username,userInfoVo.getUserName());
			data.put(JsonConstans.Password,userInfoVo.getPassword());
			data.put(JsonConstans.Localid,userInfoVo.getLocalId());
			MySharedPrefs.write(mContext, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_LOGIN_USERINFO, data.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		NextApplication.myInfo = new UserInfoVo().readMyUserInfo(mContext);
		//Login XMPP
		XmppUtils.loginXmppForNextApp(mContext);
		MySharedPrefs.reLoadWalletList();
		FinalUserDataBase.getInstance().close();
		//Enter the app
		mContext.startActivity(new Intent(mContext, MainFragmentUI.class));
		Utils.openNewActivityAnim(mContext, true);


		LoadingDialog.close();
	}


	/**
	 * enter MainUI
	 * @ param mName user name
	 * @ param mPwd password
	 * */
	public boolean intoMainUI(Context context,String mName,String mPwd){
		ArrayList<UserInfoVo> infoList = JsonUtil.readLocalInfo(context);
		for (int i = 0 ; i < infoList.size() ; i++){
			String mid = infoList.get(i).getMid();
			String mobile = infoList.get(i).getMobile();
			String email = infoList.get(i).getEmail();
			String password = infoList.get(i).getPassword();
			if ((TextUtils.equals(mName,mid) || TextUtils.equals(mName,mobile) || TextUtils.equals(mName,email)) && TextUtils.equals(mPwd,password)){
				//Save the current user information
				String jsonString  = MySharedPrefs.readString(mContext, MySharedPrefs.FILE_USER, infoList.get(i).getLocalId());
				try {
					JSONObject object = new JSONObject(jsonString);
					MySharedPrefs.write(mContext, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_LOGIN_USERINFO, object.optString(infoList.get(i).getLocalId()));
				} catch (JSONException e) {
					e.printStackTrace();
				}

				NextApplication.myInfo = new UserInfoVo().readMyUserInfo(context);
				FinalUserDataBase.getInstance().close();
				XmppUtils.loginXmppForNextApp(context);
				MySharedPrefs.reLoadWalletList();
				MySharedPrefs.write(context, MySharedPrefs.FILE_USER, MySharedPrefs.IS_FIRST_LOGIN, "yes");
				context.startActivity(new Intent(context, MainFragmentUI.class));
				Utils.openNewActivityAnim((Activity) context, true);

				//Close the guide page
				Intent intent = new Intent(Constants.ACTION_CLOSE_GUID);
				Utils.sendBroadcastReceiver(mContext, intent, true);
				return true;
			}
		}

		MyToast.showToast(context,context.getString(R.string.login_pwd_error));
		return false;
	}
	
	public void destory(){
		instance = null;
	}

}
