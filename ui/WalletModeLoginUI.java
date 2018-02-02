package com.lingtuan.firefly.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.login.LoginUI;
import com.lingtuan.firefly.login.RegistUI;
import com.lingtuan.firefly.service.UpdateVersionService;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.wallet.util.WalletStorage;
import com.lingtuan.firefly.xmpp.XmppUtils;

import java.io.IOException;

/**
 * Created on 2017/8/23.
 */

public class WalletModeLoginUI extends BaseActivity implements View.OnClickListener {


    private TextView regsterBtn;
    private TextView loginBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void setContentView() {
        setContentView(R.layout.wallet_mode_login_layout);
    }

    @Override
    protected void findViewById() {
        regsterBtn = (TextView) findViewById(R.id.register);
        loginBtn = (TextView) findViewById(R.id.login);
    }

    @Override
    protected void setListener() {
        regsterBtn.setOnClickListener(this);
        loginBtn.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.login));
        IntentFilter filter = new IntentFilter(Constants.ACTION_CLOSE_GUID);
        LocalBroadcastManager.getInstance(WalletModeLoginUI.this).registerReceiver(mBroadcastReceiver, filter);
        /**Set the language*/
        Utils.settingLanguage(WalletModeLoginUI.this);
        Utils.updateViewLanguage(findViewById(android.R.id.content));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(WalletModeLoginUI.this).unregisterReceiver(mBroadcastReceiver);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && (Constants.ACTION_CLOSE_GUID.equals(intent.getAction()))) {
                finish();
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.register:
                startActivity(new Intent(WalletModeLoginUI.this, RegistUI.class));
                Utils.openNewActivityAnim(WalletModeLoginUI.this, false);
                break;
            case R.id.login:
                if (NextApplication.myInfo == null || TextUtils.isEmpty(NextApplication.myInfo.getLocalId())) {
                    startActivity(new Intent(WalletModeLoginUI.this, LoginUI.class));
                    Utils.openNewActivityAnim(WalletModeLoginUI.this, false);
                } else {
                    MySharedPrefs.reLoadWalletList();
                    XmppUtils.loginXmppForNextApp(WalletModeLoginUI.this);
                    startActivity(new Intent(WalletModeLoginUI.this, MainFragmentUI.class));
                    Utils.openNewActivityAnim(WalletModeLoginUI.this, true);
                }
                break;

        }
    }

}
