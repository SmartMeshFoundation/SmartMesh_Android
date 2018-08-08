package com.lingtuan.firefly.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
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
import com.lingtuan.firefly.setting.SecurityUI;
import com.lingtuan.firefly.setting.SettingUI;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.MyViewDialogFragment;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.wallet.util.WalletStorage;
import com.lingtuan.firefly.xmpp.XmppUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.functions.Consumer;

/**
 * Created on 2017/8/23.
 */

public class SplashActivity extends BaseActivity implements Animation.AnimationListener, View.OnClickListener {

    @BindView(R.id.splash_bg)
    RelativeLayout splash_bg;
    @BindView(R.id.bottom_bg_login)
    LinearLayout bottom_bg_login;

    @Override
    protected void setContentView() {
        setContentView(R.layout.splash_layout);
    }

    @Override
    protected void findViewById() {

    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void initData() {

        IntentFilter filter = new IntentFilter(Constants.ACTION_CLOSE_GUID);
        LocalBroadcastManager.getInstance(SplashActivity.this).registerReceiver(mBroadcastReceiver, filter);

        /**Set the language*/
        Utils.settingLanguage();
        AlphaAnimation aa = new AlphaAnimation(0.3f, 1.0f);
        aa.setDuration(1000);
        splash_bg.startAnimation(aa);
        aa.setAnimationListener(this);

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

    @SuppressLint("CheckResult")
    @Override
    public void onAnimationEnd(Animation animation) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final RxPermissions rxPermissions = new RxPermissions(this);
            rxPermissions
                    .request(Manifest.permission.CAMERA
                            ,Manifest.permission.READ_PHONE_STATE
                            ,Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ,Manifest.permission.READ_EXTERNAL_STORAGE
                            ,Manifest.permission.RECORD_AUDIO
                            ,Manifest.permission.READ_CONTACTS)
                    .subscribe(new Consumer<Boolean>() {
                        @Override
                        public void accept(Boolean aBoolean){
                            if (aBoolean){
                                intoNextMethod();
                            }else{
                                openPermission();
                            }
                        }
                    });
        }else{
            intoNextMethod();
        }
    }

    private void openPermission(){
        MyViewDialogFragment mdf = new MyViewDialogFragment();
        mdf.setTitleAndContentText(getString(R.string.open_permission_about_smartmesh), getString(R.string.open_permission));
        mdf.setOkCallback(new MyViewDialogFragment.OkCallback() {
            @Override
            public void okBtn() {
                Intent localIntent = new Intent();
                localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                localIntent.setData(Uri.fromParts("package", getPackageName(), null));
                startActivity(localIntent);
                finish();
            }
        });

        mdf.setCancelCallback(new MyViewDialogFragment.CancelCallback() {
            @Override
            public void cancelBtn() {
                finish();
            }
        });
        mdf.setCancelable(false);
        mdf.show(getSupportFragmentManager(), "mdf");
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

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


    @OnClick({R.id.guide_register,R.id.guide_login,R.id.guide_wallet_mode})
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
