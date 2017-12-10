package com.lingtuan.firefly.service;

import android.app.DownloadManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.text.TextUtils;

import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;

import org.json.JSONObject;

import java.io.File;

/**
 * Version update service
 */
public class UpdateVersionService extends Service {

	private BroadcastReceiver receiver;
	private String url;
	@Override
	public void onCreate() {
		super.onCreate();
		requestVersion();
		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				long myDwonloadID = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
				SharedPreferences sPreferences = context.getSharedPreferences("downloadplato", 0);
				long refernece = sPreferences.getLong("plato", 0);
				if (refernece == myDwonloadID && !TextUtils.isEmpty(url)) {
					intent = new Intent(Intent.ACTION_VIEW);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					int index = url.lastIndexOf("/") + 1;
					String apkName = url.substring(index, url.length());
					intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/download/" + apkName)),
							"application/vnd.android.package-archive");
					startActivity(intent);
					stopSelf();
				}
			}
		};
		registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
	}

	/**
	 * Version update
	 */
	private void requestVersion() {

		NetRequestImpl.getInstance().versionUpdate(new RequestListener() {
			@Override
			public void start() {

			}

			@Override
			public void success(JSONObject response) {
				String version = response.optString("version");
				String describe = response.optString("describe");
				url = response.optString("url");
				int coerce = 0;
				if(!TextUtils.isEmpty(response.optString("coerce"))){
					coerce = Integer.parseInt(response.optString("coerce"));
				}
				
				Bundle data = new Bundle();
				if (coerce == 0){// Don't force
					data.putInt("type", 0);
				}else{
					data.putInt("type", 1);
				}
				data.putString("version", version);
				data.putString("describe", describe);
				data.putString("url", url);
				Intent intent = new Intent();
				intent.setAction(Constants.ACTION_UPDATE_VERSION);
				intent.putExtras(data);
				sendBroadcast(intent);
			}

			@Override
			public void error(int errorCode, String errorMsg) {
				stopSelf();
			}
		});
	}
}
