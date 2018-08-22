package com.lingtuan.firefly.base;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.chat.ChattingUI;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.language.MultiLanguageUtil;
import com.lingtuan.firefly.login.LoginUtil;
import com.lingtuan.firefly.mesh.MeshService;
import com.lingtuan.firefly.offline.AppNetService;
import com.lingtuan.firefly.raiden.RaidenNet;
import com.lingtuan.firefly.service.LoadDataService;
import com.lingtuan.firefly.service.XmppService;
import com.lingtuan.firefly.ui.MainFragmentUI;
import com.lingtuan.firefly.ui.SplashActivity;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.MyToast;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.network.NetRequestImpl;
import com.lingtuan.firefly.wallet.util.WalletStorage;
import com.lingtuan.firefly.xmpp.XmppUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.Unbinder;


/**
 * Created on 2017/8/18.
 */

public abstract class BaseActivity extends AppCompatActivity implements View.OnClickListener {
    public static List<Activity> activitys = new ArrayList<>();
    public static List<Activity> tempActivitys = new ArrayList<>();
    protected ImageView mBack;
    protected TextView mTitle;
    private Unbinder mUnBinder;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView();
        mUnBinder = ButterKnife.bind(this);
        findViewById();
        setListener();
        initPublicView();
        finishChattingUIHistory();
        activitys.add(this);
        addTempActivity();
        initData();
//        try {
//            String locationTime = MySharedPrefs.readString(this, MySharedPrefs.FILE_USER, MySharedPrefs.LOCATION_TIME);
//            if(!TextUtils.isEmpty(locationTime) && (System.currentTimeMillis() - Long.parseLong(locationTime) > 1000 * 60 * 15)){
//                ComponentName mName = new ComponentName(this, LoadDataService.class);
//                Intent locationService = new Intent(LoadDataService.ACTION_LOAD_LOCATION);
//                locationService.setComponent(mName);
//                startService(locationService);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    private void initPublicView() {
        mBack = (ImageView) findViewById(R.id.app_back);
        mTitle = (TextView) findViewById(R.id.app_title);
        if(mBack != null){
            mBack.setOnClickListener(this);
        }
    }

    protected abstract void setContentView();

    protected abstract void findViewById();

    protected abstract void setListener();

    protected abstract void initData();

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.app_back:
                Utils.hiddenKeyBoard(this);
                Utils.exitActivityAndBackAnim(this,true);
                break;
        }
    }

    protected void hindenBack() {
        if(mBack != null){
            mBack.setVisibility(View.GONE);
        }
    }

    protected void setTitle(String title) {
        if(mTitle != null){
            mTitle.setText(title);
        }
    }

    protected void showToast(String msg){
        MyToast.showToast(this, msg);
    }

    /**
     * Quit the application
     */
    public void exitApp() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    MySharedPrefs.clearUserInfo(BaseActivity.this);
                    //Clear notice
                    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.cancelAll();

                    FinalUserDataBase.getInstance().close();
                    NetRequestImpl.getInstance().destory();
                    LoginUtil.getInstance().destory();
                    NextApplication.myInfo = null ;

                    //Exit the XMPP service
                    XmppUtils.getInstance().destroy();
                    stopService(new Intent(BaseActivity.this, XmppService.class));
                    stopService(new Intent(BaseActivity.this, MeshService.class));
                    //Exit without social network service
                    int version =android.os.Build.VERSION.SDK_INT;
                    if(version >= 16){
                        Intent offlineservice = new Intent(NextApplication.mContext, AppNetService.class);
                        stopService(offlineservice);
                    }
                    stopService(new Intent(BaseActivity.this,LoadDataService.class));
                    RaidenNet.getInatance().stopRaiden();
                    WalletStorage.getInstance(NextApplication.mContext).destroy();
                    exit();
                } catch (Exception e) {
                    e.printStackTrace();
                }


                ActivityManager am = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
                am.killBackgroundProcesses(getPackageName());
                startActivity(new Intent(BaseActivity.this, SplashActivity.class));
                Utils.openNewActivityAnim(BaseActivity.this, true);

            }
        }).start();
    }

    public static void exit() {
        if (activitys != null && !activitys.isEmpty()) {
            for (Activity act : activitys) {
                if (!act.isFinishing()) {
                    act.finish();
                }
            }
            activitys.clear();
        }
    }

    private void finishChattingUIHistory(){
        if (this instanceof ChattingUI && activitys != null && !activitys.isEmpty()) {
            for (Activity act : activitys) {
                if (act instanceof ChattingUI) {
                    act.finish();
                }
            }
        }
    }

    private void addTempActivity(){
        if(!(this instanceof ChattingUI)){
            tempActivitys.add(this);
        }
    }

    public void removeTempActivity(){
        tempActivitys.remove(this);
    }

    public void finishTempActivity(){
        if (tempActivitys != null && !tempActivitys.isEmpty()) {
            for (int i=0;i<tempActivitys.size();i++) {
                Activity act = tempActivitys.get(i);
                if (!act.isFinishing() && ! (act instanceof MainFragmentUI)) {
                    act.finish();
                    tempActivitys.remove(i);
                    i--;
                }
            }
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(MultiLanguageUtil.attachBaseContext(newBase));
    }

    @Override
    protected void onDestroy() {
        mUnBinder.unbind();
        activitys.remove(this);
        removeTempActivity();
        super.onDestroy();
    }
}
