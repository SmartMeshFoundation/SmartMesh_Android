package com.lingtuan.firefly.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import com.android.volley.VolleyError;
import com.lingtuan.firefly.service.LoadDataService;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.Utils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 *Used to monitor the network
 */
public class OffLineNetworkReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Constants.isConnectNet = Utils.isConnectNet(context);
        try {
            if (context != null) {
                Utils.sendBroadcastReceiver(context, new Intent(Constants.ACTION_NETWORK_RECEIVER), false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
