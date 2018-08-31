package com.lingtuan.firefly.ui;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.login.LoginUI;
import com.lingtuan.firefly.offline.AppNetService;
import com.lingtuan.firefly.service.UpdateVersionService;
import com.lingtuan.firefly.ui.contract.AlertContract;
import com.lingtuan.firefly.ui.presenter.AlertPresenterImpl;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.MyViewDialogFragment;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.wallet.WalletCopyActivity;
import com.lingtuan.firefly.wallet.util.Sign2;
import com.lingtuan.firefly.wallet.util.WalletStorage;
import com.lingtuan.firefly.wallet.vo.StorableWallet;

import org.json.JSONException;
import org.spongycastle.util.encoders.Hex;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Hash;
import org.web3j.utils.Numeric;

import java.io.File;
import java.io.IOException;


public class AlertActivity extends BaseActivity implements AlertContract.View{

	private AlertContract.Presenter mPresenter;

	private String address;

	private DialogInterface dialog = null;

	@Override
	protected void setContentView() {
		setContentView(R.layout.alert_null_layout);
	}

	@Override
	protected void findViewById() {

	}

	@Override
	protected void setListener() {

	}

	@Override
	protected void initData() {

		new AlertPresenterImpl(this);

		int type=getIntent().getIntExtra("type", 0);

		if(type==0){
			updateVersionDialog(getIntent().getStringExtra("version"),getIntent().getStringExtra("describe"),getIntent().getStringExtra("url"));
		}else if(type==1){
			updateVersionNowDialog(getIntent().getStringExtra("version"),getIntent().getStringExtra("describe"),getIntent().getStringExtra("url"));
		}else if(type==2){
			showOfflineDialog(getIntent().getStringExtra("msg"));
		}else if(type==3){
			showSmartMeshDialog();
		}else if(type==4){
			showWalletDialog();
		}else if(type==5){
			StorableWallet storableWallet = (StorableWallet) getIntent().getSerializableExtra("strablewallet");
			showBackupDialog(storableWallet);
		}else if(type==6){
			String smtBalance = getIntent().getStringExtra("smtBalance");
			String title = getIntent().getStringExtra("title");
			String url = getIntent().getStringExtra("url");
			address = getIntent().getStringExtra("address");
			showMappingDialog(smtBalance,title,url);
		}else if(type==7){
			String mappingId = getIntent().getStringExtra("mappingId");
			String content = getIntent().getStringExtra("content");
			showMappingSuccessDialog(mappingId,content);
		}
	}

	/**
	 * update version dialog
	 * 版本更新弹框
	 * */
	private void updateVersionDialog(String version, String describe, final String url){
		mPresenter.updateVersionDialog(this,getString(R.string.new_version,version),describe,url,getString(R.string.updatelater),getString(R.string.updatenow));
	}

	/**
	 * update version dialog now
	 * 版本立即更新弹框
	 * */
	private void updateVersionNowDialog(String version, String describe, final String url){
		mPresenter.updateVersionNowDialog(this,getString(R.string.new_version,version),describe,url,getString(R.string.updatenow));
	}

	/**
	 * offline dialog
	 * 强制下线弹框
	 * */
	private void showOfflineDialog(String msg){
		msg = getString(R.string.show_offline_hint);
		mPresenter.showOfflineDialog(this,getString(R.string.notif),msg,getString(R.string.submit));
	}

	/**
	 * smart mesh dialog
	 * 开启无网模式弹框
	 * */
	private void showSmartMeshDialog(){
		String title = getString(R.string.smartmesh_communication_open);
		String message = getString(R.string.smartmesh_communication_hint);
		String positiveMsg = getString(R.string.smartmesh_later);
		String negativeMsg = getString(R.string.smartmesh_open);
		mPresenter.showSmartMeshDialog(this,title,message,positiveMsg,negativeMsg);
	}

	/**
	 * wallet dialog
	 * 钱包提示弹框
	 * */
	private void showWalletDialog(){
		mPresenter.showWalletDialog(this,getString(R.string.wallet_copy_disclaimer),getString(R.string.wallet_copy_disclaimer_info),getString(R.string.ok));
	}

	/**
	 * backup dialog
	 * 钱包备份弹框
	 * */
	private void showBackupDialog(final StorableWallet storableWallet){
		mPresenter.showBackupDialog(this,getString(R.string.wallet_backup_title),getString(R.string.wallet_copy_disclaimer_info),getString(R.string.wallet_backup_now),storableWallet);
	}

	/**
	 * mapping dialog
	 * 映射弹框
	 * */
	private void showMappingDialog(String smtBalance,String title,String url) {
		mPresenter.mappingDialog(this,getString(R.string.mapping_start),smtBalance,title,url);
	}

	private void showMappingSuccessDialog(String mappingId, String content) {
		mPresenter.mappingSuccessDialog(this,getString(R.string.ok),mappingId,content);
	}

	@Override
	public void onBackPressed() {

	}

	@Override
	public void setPresenter(AlertContract.Presenter presenter) {
		this.mPresenter = presenter;
	}

	/**
	 * update version dialog submit
	 * 版本更新确认回调
	 * */
	@Override
	public void updateVersionDialogSubmit(String url) {
		try {
			if (TextUtils.isEmpty(url)){
				finish();
				return;
			}
			showToast(getString(R.string.chatting_start_download));
			int index = url.lastIndexOf("/")+1;
			String apkName = url.substring(index,url.length());
			DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
			DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
			request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
			request.setMimeType("application/vnd.android.package-archive");
			request.allowScanningByMediaScanner();
			request.setVisibleInDownloadsUi(true);
			Utils.deleteFiles(new File(Environment.getExternalStorageDirectory() + "/download/"+apkName));
			request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, apkName);
			long refernece = dm.enqueue(request);
			SharedPreferences sPreferences = getSharedPreferences("downloadplato", 0);
			sPreferences.edit().putLong("plato", refernece).commit();
			finish();
		}catch (Exception e){
			e.printStackTrace();
			finish();
		}
	}

	@Override
	public void updateNowVersionDialogSubmit() {
		finish();
	}

	/**
	 * cancel update version
	 * 取消版本更新
	 * */
	@Override
	public void updateVersionDialogCancel() {
		try {
			Intent versionService = new Intent(NextApplication.mContext, UpdateVersionService.class);
			stopService(versionService);
			finish();
		}catch (Exception e){
			e.printStackTrace();
			finish();
		}
	}

	/**
	 * offline submit
	 * 强制下线回调
	 * */
	@Override
	public void offlineDialogSubmit() {
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.cancelAll();
		startActivity(new Intent(AlertActivity.this, LoginUI.class));
		Utils.openNewActivityAnim(AlertActivity.this, true);
		finish();
		BaseActivity.exit();
		NextApplication.myInfo=null;
	}

	@Override
	public void smartMeshDialogSubmit(boolean isNegative) {
		if (isNegative){
			MySharedPrefs.writeInt(NextApplication.mContext,MySharedPrefs.FILE_USER,MySharedPrefs.KEY_NO_NETWORK_COMMUNICATION + NextApplication.myInfo.getLocalId(),1);
			startService(new Intent(AlertActivity.this, AppNetService.class));
		}else{
			MySharedPrefs.writeInt(NextApplication.mContext,MySharedPrefs.FILE_USER,MySharedPrefs.KEY_NO_NETWORK_COMMUNICATION + NextApplication.myInfo.getLocalId(),0);
		}
		finish();
	}

	@Override
	public void walletDialogSubmit() {
		MySharedPrefs.writeBoolean(NextApplication.mContext,MySharedPrefs.FILE_USER,MySharedPrefs.IS_SHOW_WALLET_DIALOG,false);
		finish();
	}

	@Override
	public void backUpDialogSubmit(StorableWallet storableWallet) {
		Intent copyIntent = new Intent(NextApplication.mContext,WalletCopyActivity.class);
		copyIntent.putExtra(Constants.WALLET_INFO, storableWallet);
		copyIntent.putExtra(Constants.WALLET_IMAGE, storableWallet.getWalletImageId());
		copyIntent.putExtra(Constants.WALLET_TYPE, 1);
		startActivity(copyIntent);
		finish();
	}

	@Override
	public void walletMappingSubmit(final DialogInterface dialog) {
		MyViewDialogFragment mdf = new MyViewDialogFragment(MyViewDialogFragment.DIALOG_INPUT_PWD, new MyViewDialogFragment.EditCallback() {
            @Override
            public void getEditText(String editText) {
                getCredentials(editText,dialog);
            }
        });
        mdf.show(getSupportFragmentManager(), "mdf");
	}

	@Override
	public void walletMappingClose() {
		finish();
	}

	@Override
	public void walletMappingSuccessSubmit() {
		finish();
	}

	@Override
	public void mappingSuccess(String mappingId,String content) {
		LoadingDialog.close();
		showMappingSuccessDialog(mappingId,content);
		if (dialog != null){
			dialog.dismiss();
		}
		Utils.sendBroadcastReceiver(this, new Intent(Constants.WALLET_REFRESH_MAPPING), false);
	}

	@Override
	public void mappingError(int errorCode, String errorMsg) {
		LoadingDialog.close();
		if (errorCode == 1801220){
			Utils.sendBroadcastReceiver(this, new Intent(Constants.WALLET_REFRESH_MAPPING), false);
			finish();
		}else{
			showToast(errorMsg);
			finish();
		}
	}
	/**
	 * get credentials
	 * */
	private void getCredentials(final String password,DialogInterface dialog){
		LoadingDialog.show(this,"");
		this.dialog = dialog;
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Credentials keys = WalletStorage.getInstance(getApplicationContext()).getFullWallet(NextApplication.mContext,password,address);
					Message message = Message.obtain();
					message.what = 0;
					message.obj = keys;
					mHandler.sendMessage(message);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (CipherException e) {
					e.printStackTrace();
					mHandler.sendEmptyMessage(1);
				}
			}
		}).start();
	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 0:
					Credentials keys = (Credentials)msg.obj;
					String message = Constants.WALLET_MAPPING_SIGN;
					byte[] strByte = Hash.sha3(Numeric.hexStringToByteArray(message));
					Sign2.SignatureData data = Sign2.signMessage(strByte,keys.getEcKeyPair());
					String signR = "0x" + Hex.toHexString(data.getR());
					String S = "0x" + Hex.toHexString(data.getS());
					byte V = data.getV();
					mPresenter.startMappingMethod(address,signR,S,V);
					break;
				case 1:
					LoadingDialog.close();
					showToast(getString(R.string.wallet_copy_pwd_error));
					break;
			}
		}
	};

}
