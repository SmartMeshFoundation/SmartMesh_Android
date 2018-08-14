package com.lingtuan.firefly.ui;

import android.app.DownloadManager;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
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
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.wallet.WalletCopyActivity;
import com.lingtuan.firefly.wallet.vo.StorableWallet;

import java.io.File;


public class AlertActivity extends BaseActivity implements AlertContract.View{

	private AlertContract.Presenter mPresenter;

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
		}
	}

	private void updateVersionDialog(String version, String describe, final String url){
		mPresenter.updateVersionDialog(this,getString(R.string.new_version,version),describe,url,getString(R.string.updatelater),getString(R.string.updatenow));
	}

	private void updateVersionNowDialog(String version, String describe, final String url){
		mPresenter.updateVersionNowDialog(this,getString(R.string.new_version,version),describe,url,getString(R.string.updatenow));
	}

	private void showOfflineDialog(String msg){
		msg = getString(R.string.show_offline_hint);
		mPresenter.showOfflineDialog(this,getString(R.string.notif),msg,getString(R.string.submit));
	}

	private void showSmartMeshDialog(){
		String title = getString(R.string.smartmesh_communication_open);
		String message = getString(R.string.smartmesh_communication_hint);
		String positiveMsg = getString(R.string.smartmesh_later);
		String negativeMsg = getString(R.string.smartmesh_open);
		mPresenter.showSmartMeshDialog(this,title,message,positiveMsg,negativeMsg);
	}


	private void showWalletDialog(){
		mPresenter.showWalletDialog(this,getString(R.string.wallet_copy_disclaimer),getString(R.string.wallet_copy_disclaimer_info),getString(R.string.ok));
	}

	private void showBackupDialog(final StorableWallet storableWallet){
		mPresenter.showBackupDialog(this,getString(R.string.wallet_backup_title),getString(R.string.wallet_copy_disclaimer_info),getString(R.string.wallet_backup_now),storableWallet);
	}

	@Override
	public void onBackPressed() {

	}

	@Override
	public void setPresenter(AlertContract.Presenter presenter) {
		this.mPresenter = presenter;
	}

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

}
