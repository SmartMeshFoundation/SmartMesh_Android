package com.lingtuan.firefly.ui;

import android.app.DownloadManager;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.view.Window;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.custom.SubmitDialog;
import com.lingtuan.firefly.login.LoginUI;
import com.lingtuan.firefly.service.UpdateVersionService;
import com.lingtuan.firefly.util.Utils;

import java.io.File;


public class AlertActivity extends BaseActivity{

	@Override
	protected void setContentView() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
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
		int type=getIntent().getIntExtra("type", 0);
		if(type==0){
			showVersionDialog1(getIntent().getStringExtra("version"),getIntent().getStringExtra("describe"),getIntent().getStringExtra("url"));
		}else if(type==1){
			showVersionDialog2(getIntent().getStringExtra("version"),getIntent().getStringExtra("describe"),getIntent().getStringExtra("url"));
		}else if(type==2){
			showOfflineDialog(getIntent().getStringExtra("msg"));
		}
	}

	private void showVersionDialog1(String version, String describe, final String url){

		final SubmitDialog.Builder builder = new SubmitDialog.Builder(this);
		builder.setTitle(getString(R.string.new_version,version))
				.setMessage(describe.replace("\\n", "\n"))
				.setPositiveButton(getString(R.string.updatelater), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent versionService = new Intent(AlertActivity.this, UpdateVersionService.class);
						stopService(versionService);
						dialog.dismiss();
						finish();
					}
				})
				.setNegativeButton(getString(R.string.updatenow),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								showToast(getString(R.string.chatting_start_download));
								int index = url.lastIndexOf("/")+1;
								String apkName = url.substring(index,url.length());
								DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
								DownloadManager.Request request = new DownloadManager.Request(
										Uri.parse(url));
								request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
								request.setMimeType("application/vnd.android.package-archive");
								// Set to can be found by the media scanner
								request.allowScanningByMediaScanner();
								// Set to visible and manageable
								request.setVisibleInDownloadsUi(true);

								Utils.deleteFiles(new File(Environment.getExternalStorageDirectory() + "/download/"+apkName));

								request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, apkName);
								long refernece = dm.enqueue(request);
								SharedPreferences sPreferences = getSharedPreferences("downloadplato", 0);
								sPreferences.edit().putLong("plato", refernece).commit();
								finish();


							}
						});
		builder.show();
		builder.setCancelable(false);
	}


	private void showVersionDialog2(String version, String describe, final String url)
	{

		SubmitDialog.Builder builder = new
				SubmitDialog.Builder(this);
		builder.setTitle(getString(R.string.new_version,version))
				.setMessage(describe.replace("\\n", "\n"))
				.setPositiveButton(getString(R.string.updatenow),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								showToast(getString(R.string.chatting_start_download));
								int index = url.lastIndexOf("/")+1;
								String apkName = url.substring(index,url.length());
								DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
								DownloadManager.Request request = new DownloadManager.Request(
										Uri.parse(url));
								request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
								request.setMimeType("application/vnd.android.package-archive");
								// Set to can be found by the media scanner
								request.allowScanningByMediaScanner();
								// Set to visible and manageable
								request.setVisibleInDownloadsUi(true);

								Utils.deleteFiles(new File(Environment.getExternalStorageDirectory() + "/download/"+apkName));

								request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, apkName);
								long refernece = dm.enqueue(request);
								SharedPreferences sPreferences = getSharedPreferences("downloadplato", 0);
								sPreferences.edit().putLong("plato", refernece).commit();
								finish();
							}
						});
		builder.show();
		builder.setCancelable(false);
	}
	private void showOfflineDialog(String msg)
	{
		SubmitDialog.Builder builder = new
				SubmitDialog.Builder(this);
		builder.setTitle(getString(R.string.notif))
				.setMessage(msg)
				.setPositiveButton(getString(R.string.submit),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								//Clear notice
								NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
								notificationManager.cancelAll();
								startActivity(new Intent(AlertActivity.this, LoginUI.class));
								Utils.openNewActivityAnim(AlertActivity.this, true);
								finish();
								BaseActivity.exit();
								NextApplication.myInfo=null;

							}
						});
		builder.show();
		builder.setCancelable(false);
	}


	@Override
	public void onBackPressed() {

	}

	public interface AlertListener{
		void clickPositiveButton();
	}
}
