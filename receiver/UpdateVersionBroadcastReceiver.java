package com.lingtuan.firefly.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lingtuan.firefly.ui.AlertActivity;
import com.lingtuan.firefly.util.Constants;


public class UpdateVersionBroadcastReceiver extends BroadcastReceiver {
  
    @Override
    public void onReceive(Context context, Intent i) {
		try {
			if (i.getAction().equals(Constants.ACTION_UPDATE_VERSION)) {
				int type = i.getExtras().getInt("type", 0);
				String version = i.getExtras().getString("version");
				String describe = i.getExtras().getString("describe");
				String url = i.getExtras().getString("url");

				Intent intent = new Intent(context, AlertActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra("type", type);
				intent.putExtra("version", version);
				intent.putExtra("describe", describe);
				intent.putExtra("url", url);
				context.startActivity(intent);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
    }  
}
