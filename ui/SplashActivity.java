package com.lingtuan.firefly.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.login.GuideUI;
import com.lingtuan.firefly.login.LoginUI;
import com.lingtuan.firefly.login.RegistUI;
import com.lingtuan.firefly.service.UpdateVersionService;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.MyToast;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.xmpp.XmppUtils;

/**
 * Created on 2017/8/23.
 */

public class SplashActivity extends AppCompatActivity implements Animation.AnimationListener, View.OnClickListener {

    private RelativeLayout splash_bg;

    private TextView regsterBtn;
    private TextView loginBtn;

    private LinearLayout bottom_bg_login;

    private long firstTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_layout);
        splash_bg = (RelativeLayout) findViewById(R.id.splash_bg);
        regsterBtn = (TextView) findViewById(R.id.guide_register);
        loginBtn = (TextView) findViewById(R.id.guide_login);
        bottom_bg_login = (LinearLayout) findViewById(R.id.bottom_bg_login);
        initData();
    }

    protected void initData() {

        IntentFilter filter = new IntentFilter(Constants.ACTION_CLOSE_GUID);
        LocalBroadcastManager.getInstance(SplashActivity.this).registerReceiver(mBroadcastReceiver, filter);

        /**Set the language*/
        Utils.settingLanguage(SplashActivity.this);
        Utils.updateViewLanguage(findViewById(android.R.id.content));

        regsterBtn.setOnClickListener(this);
        loginBtn.setOnClickListener(this);
        AlphaAnimation aa = new AlphaAnimation(0.3f, 1.0f);
        aa.setDuration(1000);
        splash_bg.startAnimation(aa);
        aa.setAnimationListener(this);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.recycleImageBg(splash_bg);
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

        String str = MySharedPrefs.readString(SplashActivity.this, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_IS_FIRST_USE);
        String jsonToken = MySharedPrefs.readString(SplashActivity.this, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_LOGIN_USERINFO);
        String versionCode = Utils.getVersionCode(SplashActivity.this) + "";
        if (!TextUtils.equals(versionCode, str) || TextUtils.isEmpty(jsonToken)) {
            bottom_bg_login.setVisibility(View.VISIBLE);
            MySharedPrefs.write(SplashActivity.this, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_IS_FIRST_USE, versionCode);
//            Intent intent = new Intent(SplashActivity.this, GuideUI.class);
//            if (NextApplication.myInfo == null || TextUtils.isEmpty(NextApplication.myInfo.getLocalId())) {
//                intent.putExtra("isLogin", false);
//            } else {
//                intent.putExtra("isLogin", true);
//            }
//
//            startActivity(intent);
//            Utils.openNewActivityAnim(SplashActivity.this, true);
        } else {
//            if (NextApplication.myInfo == null || TextUtils.isEmpty(NextApplication.myInfo.getUid())) {
//                startActivity(new Intent(SplashActivity.this, LoginUI.class));
//                Utils.openNewActivityAnim(SplashActivity.this, true);
//            } else {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        XmppUtils.loginXmppForNextApp(SplashActivity.this);
                    }
                },5000);

                startActivity(new Intent(SplashActivity.this, MainFragmentUI.class));
                Utils.openNewActivityAnim(SplashActivity.this, true);
//            }
        }

//        if(RootUtil.isDeviceRooted()){
//            MyViewDialogFragment mdf = new MyViewDialogFragment(MyViewDialogFragment.DIALOG_SINGLE_BUTTON, new MyViewDialogFragment.SingleCallback() {
//                @Override
//                public void getSingleCallBack() {
//                    System.exit(0);
//                }
//            });
//            mdf.setCancelable(false);
//            mdf.show(this.getSupportFragmentManager(), "mdf");
//        }else{
//            if(WalletStorage.getInstance(getApplicationContext()).get().size()>0)
//            {
//                startActivity(new Intent(SplashActivity.this,WalletMainActivity.class));
//                finish();
//            }
//            else{
//                startActivity(new Intent(SplashActivity.this,NewWalletActivity.class));
//                finish();
//            }
//        }
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
                startActivity(new Intent(SplashActivity.this, RegistUI.class));
                Utils.openNewActivityAnim(SplashActivity.this, false);
                break;
            case R.id.guide_login:
                if (NextApplication.myInfo == null || TextUtils.isEmpty(NextApplication.myInfo.getLocalId())) {
                    startActivity(new Intent(SplashActivity.this, LoginUI.class));
                    Utils.openNewActivityAnim(SplashActivity.this, false);
                } else {
                    XmppUtils.loginXmppForNextApp(SplashActivity.this);
                    startActivity(new Intent(SplashActivity.this, MainFragmentUI.class));
                    Utils.openNewActivityAnim(SplashActivity.this, true);
                }
                break;

        }
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - firstTime > 2000){
            MyToast.showToast(SplashActivity.this,getString(R.string.exit_app));
            firstTime = System.currentTimeMillis();
        }else{
            finish();
            System.exit(0);
        }
    }
}
