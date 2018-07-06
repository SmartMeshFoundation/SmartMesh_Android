package com.lingtuan.firefly.ui;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
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
import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.functions.Consumer;

/**
 * Created on 2017/8/23.
 */

public class SplashActivity extends BaseActivity implements Animation.AnimationListener, View.OnClickListener {

    private RelativeLayout splash_bg;

    private TextView regsterBtn;
    private TextView loginBtn;
    private TextView walletPattern;

    private LinearLayout bottom_bg_login;

    private static int REQUEST_CODE_WRITE_SETTINGS = 0x01;

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
    protected void onResume() {
        super.onResume();
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final RxPermissions rxPermissions = new RxPermissions(this);
            rxPermissions
                    .request(Manifest.permission.CAMERA
                            ,Manifest.permission.READ_PHONE_STATE
                            ,Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ,Manifest.permission.READ_EXTERNAL_STORAGE
//                        ,Manifest.permission.CHANGE_CONFIGURATION
                            ,Manifest.permission.RECORD_AUDIO
                            ,Manifest.permission.READ_CONTACTS)
                    .subscribe(new Consumer<Boolean>() {
                        @Override
                        public void accept(Boolean aBoolean) throws Exception {
                            if (aBoolean){
                                //暂时不需要本permission
//                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(SplashActivity.this)) {
//                                    requestWriteSettings();
//                                }else{
//                                    intoNextMethod();
//                                }
                                intoNextMethod();
                            }else{
//                                showToast(getString(R.string.open_permission));
                                finish();
                            }
                        }
                    });
        }else{
            intoNextMethod();
        }

    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

//    private void requestWriteSettings() {
//        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
//        intent.setData(Uri.parse("package:" + getPackageName()));
//        startActivityForResult(intent, REQUEST_CODE_WRITE_SETTINGS );
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_CODE_WRITE_SETTINGS){
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.System.canWrite(SplashActivity.this)){
//                intoNextMethod();
//            }
//        }
//    }

    private void intoNextMethod(){
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
