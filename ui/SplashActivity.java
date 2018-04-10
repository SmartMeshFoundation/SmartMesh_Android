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
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.login.LoginUI;
import com.lingtuan.firefly.login.RegistUI;
import com.lingtuan.firefly.service.UpdateVersionService;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.wallet.util.WalletStorage;
import com.lingtuan.firefly.xmpp.XmppUtils;

/**
 * Created on 2017/8/23.
 */

public class SplashActivity extends BaseActivity implements Animation.AnimationListener, View.OnClickListener {

    private RelativeLayout splash_bg;

    private TextView regsterBtn;
    private TextView loginBtn;
    private TextView walletPattern;

    private LinearLayout bottom_bg_login;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void setContentView() {
        setContentView(R.layout.splash_layout);
    }

    @Override
    protected void findViewById() {
        splash_bg = (RelativeLayout) findViewById(R.id.splash_bg);
        regsterBtn = (TextView) findViewById(R.id.guide_register);
        loginBtn = (TextView) findViewById(R.id.guide_login);
        walletPattern = (TextView) findViewById(R.id.guide_wallet_mode);
        bottom_bg_login = (LinearLayout) findViewById(R.id.bottom_bg_login);
    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void initData() {

        IntentFilter filter = new IntentFilter(Constants.ACTION_CLOSE_GUID);
        LocalBroadcastManager.getInstance(SplashActivity.this).registerReceiver(mBroadcastReceiver, filter);

        /**Set the language*/
        Utils.settingLanguage(SplashActivity.this);
        Utils.updateViewLanguage(findViewById(android.R.id.content));

        regsterBtn.setOnClickListener(this);
        loginBtn.setOnClickListener(this);
        walletPattern.setOnClickListener(this);
        AlphaAnimation aa = new AlphaAnimation(0.3f, 1.0f);
        aa.setDuration(1000);
        splash_bg.startAnimation(aa);
        aa.setAnimationListener(this);

//        ComponentName mName = new ComponentName(this, LoadDataService.class);
//        Intent locationService = new Intent(LoadDataService.ACTION_LOAD_LOCATION);
//        locationService.setComponent(mName);
//        startService(locationService);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(SplashActivity.this).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {

        Intent versionService = new Intent(SplashActivity.this, UpdateVersionService.class);
        stopService(versionService);
        startService(versionService);

        int walletMode = MySharedPrefs.readInt(SplashActivity.this, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_IS_WALLET_PATTERN);
        String jsonToken = MySharedPrefs.readString(SplashActivity.this, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_LOGIN_USERINFO);
        String isFirst = MySharedPrefs.readString(SplashActivity.this, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_IS_FIRST_WALLET_USE);
        if (TextUtils.isEmpty(isFirst) && NextApplication.myInfo != null){
            WalletStorage.getInstance(NextApplication.mContext).firstLoadAllWallet(NextApplication.mContext);
        }
        FinalUserDataBase.getInstance().close();
        if (walletMode != 0){
            startActivity(new Intent(SplashActivity.this, MainFragmentUI.class));
            Utils.openNewActivityAnim(SplashActivity.this,true);
        }else{
            if (TextUtils.isEmpty(jsonToken)) {
                bottom_bg_login.setVisibility(View.VISIBLE);
            } else {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        XmppUtils.loginXmppForNextApp(SplashActivity.this);
                    }
                },5000);
                startActivity(new Intent(SplashActivity.this, MainFragmentUI.class));
                Utils.openNewActivityAnim(SplashActivity.this, true);
            }
        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

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
            case R.id.guide_register:
                MySharedPrefs.writeInt(SplashActivity.this, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_IS_WALLET_PATTERN,0);
                startActivity(new Intent(SplashActivity.this, RegistUI.class));
                Utils.openNewActivityAnim(SplashActivity.this, false);
                break;
            case R.id.guide_login:
                MySharedPrefs.writeInt(SplashActivity.this, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_IS_WALLET_PATTERN,0);
                if (NextApplication.myInfo == null || TextUtils.isEmpty(NextApplication.myInfo.getLocalId())) {
                    startActivity(new Intent(SplashActivity.this, LoginUI.class));
                    Utils.openNewActivityAnim(SplashActivity.this, false);
                } else {
                    FinalUserDataBase.getInstance().close();
                    XmppUtils.loginXmppForNextApp(SplashActivity.this);
                    startActivity(new Intent(SplashActivity.this, MainFragmentUI.class));
                    Utils.openNewActivityAnim(SplashActivity.this, true);
                }
                break;
            case R.id.guide_wallet_mode:
                MySharedPrefs.writeInt(SplashActivity.this, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_IS_WALLET_PATTERN,1);
                Intent intent =  new Intent(SplashActivity.this, MainFragmentUI.class);
                intent.putExtra("showAnimation",true);
                startActivity(intent);
                Utils.openNewActivityFullScreenAnim(SplashActivity.this,true);
                break;

        }
    }

}
