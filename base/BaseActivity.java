package com.lingtuan.firefly.base;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.chat.ChattingUI;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.login.LoginUI;
import com.lingtuan.firefly.login.LoginUtil;
import com.lingtuan.firefly.offline.AppNetService;
import com.lingtuan.firefly.service.LoadDataService;
import com.lingtuan.firefly.service.XmppService;
import com.lingtuan.firefly.ui.SplashActivity;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.MyToast;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;
import com.lingtuan.firefly.xmpp.XmppUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * Created on 2017/8/18.
 */

public abstract class BaseActivity extends AppCompatActivity implements View.OnClickListener {
    public static List<Activity> activitys = new ArrayList<>();
    public static List<Activity> tempActivitys = new ArrayList<>();
    protected ImageView mBack;
    protected TextView mTitle;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView();
        findViewById();
        setListener();
        initPublicView();
        finishChattingUIHistory();
        activitys.add(this);
        addTempActivity();
        initData();
        try {
            String locationTime = MySharedPrefs.readString(this, MySharedPrefs.FILE_USER, MySharedPrefs.LOCATION_TIME);
            if(!TextUtils.isEmpty(locationTime) && (System.currentTimeMillis() - Long.parseLong(locationTime) > 1000 * 60 * 15)){
                ComponentName mName = new ComponentName(this, LoadDataService.class);
                Intent locationService = new Intent(LoadDataService.ACTION_LOAD_LOCATION);
                locationService.setComponent(mName);
                startService(locationService);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    /**
    *show toast
    */
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
                    stopService(new Intent(BaseActivity.this, AppNetService.class));
                    stopService(new Intent(BaseActivity.this,LoadDataService.class));
                    exit();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                ActivityManager am = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
                am.killBackgroundProcesses(getPackageName());

                startActivity(new Intent(BaseActivity.this, SplashActivity.class));
                Utils.openNewActivityAnim(BaseActivity.this, true);

                Utils.sendBroadcastReceiver(BaseActivity.this,new Intent(Constants.ACTION_CLOSE_MAIN),false);
            }
        }).start();
    }

    /**
    * exit
    */
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

    /**
    *finish chatting history
    */
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
                if (!act.isFinishing()) {
                    act.finish();
                    tempActivitys.remove(i);
                    i--;
                }
            }

        }
    }

    @Override
    protected void onDestroy() {
        activitys.remove(this);
        removeTempActivity();
        super.onDestroy();
    }
}
