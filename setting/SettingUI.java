
package com.lingtuan.firefly.setting;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.custom.gesturelock.ACache;
import com.lingtuan.firefly.custom.switchbutton.SwitchButton;
import com.lingtuan.firefly.fragment.MySelfFragment;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.offline.AppNetService;
import com.lingtuan.firefly.ui.WebViewUI;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.MyViewDialogFragment;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;

import org.json.JSONObject;

import java.util.Locale;

/**
 * Created on 2017/9/13.
 * Settings page
 * {@link MySelfFragment}
 */

public class SettingUI extends BaseActivity implements CompoundButton.OnCheckedChangeListener {

    //语言选择
    private RelativeLayout languageSelectionBody;//Language selection
    private RelativeLayout useAgree;//use agree
    private RelativeLayout blackListBody;//The blacklist version detection
    private TextView exit;//exit
    private SwitchButton startSmartMeshWorkButton,gestureButton;//stealth  、no net work.  gesture
    private RelativeLayout startSmartMeshWorkBody;

    private TextView versionCheck;//The version number

    @Override
    protected void setContentView() {
        setContentView(R.layout.setting_layout);
    }

    @Override
    protected void findViewById() {
        languageSelectionBody = (RelativeLayout) findViewById(R.id.languageSelectionBody);
        useAgree = (RelativeLayout) findViewById(R.id.useAgree);
        blackListBody = (RelativeLayout) findViewById(R.id.blackListBody);
        versionCheck = (TextView) findViewById(R.id.versionCheck);
        exit = (TextView) findViewById(R.id.exit);
        startSmartMeshWorkButton = (SwitchButton) findViewById(R.id.startSmartMeshWorkButton);
        gestureButton = (SwitchButton) findViewById(R.id.gestureButton);
        startSmartMeshWorkBody = (RelativeLayout) findViewById(R.id.startSmartMeshWorkBody);
    }

    @Override
    protected void setListener() {
        languageSelectionBody.setOnClickListener(this);
        useAgree.setOnClickListener(this);
        blackListBody.setOnClickListener(this);
        exit.setOnClickListener(this);
        // -1 default , 0 close , 1 open
        int noNetWork = MySharedPrefs.readInt1(NextApplication.mContext,MySharedPrefs.FILE_USER,MySharedPrefs.KEY_NO_NETWORK_COMMUNICATION + NextApplication.myInfo.getLocalId());
        startSmartMeshWorkButton.setOnCheckedChangeListener(null);
        if (noNetWork == 1){//is open
            startSmartMeshWorkButton.setChecked(true);
            startSmartMeshWorkButton.setBackColor(getResources().getColorStateList(R.color.switch_button_green));
        }else{
            startSmartMeshWorkButton.setChecked(false);
            startSmartMeshWorkButton.setBackColor(getResources().getColorStateList(R.color.switch_button_gray));
        }
        startSmartMeshWorkButton.setOnCheckedChangeListener(this);
    }

    @Override
    protected void initData() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.CHANGE_LANGUAGE);//Refresh the page
        registerReceiver(mBroadcastReceiver, filter);
        setTitle(getString(R.string.setting));
        versionCheck.setText(Utils.getVersionName(SettingUI.this));
        int version =android.os.Build.VERSION.SDK_INT;
        if(version < 16){
            startSmartMeshWorkBody.setVisibility(View.GONE);
        }else{
            startSmartMeshWorkBody.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        gestureButton.setOnCheckedChangeListener(null);
        byte[] gestureByte  = ACache.get(NextApplication.mContext).getAsBinary(Constants.GESTURE_PASSWORD + NextApplication.myInfo.getLocalId());
        if (gestureByte != null && gestureByte.length > 0){
            gestureButton.setChecked(true);
            gestureButton.setBackColor(getResources().getColorStateList(R.color.switch_button_green));
        }else{
            gestureButton.setChecked(false);
            gestureButton.setBackColor(getResources().getColorStateList(R.color.switch_button_gray));
        }
        gestureButton.setOnCheckedChangeListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.languageSelectionBody:
                startActivity(new Intent(SettingUI.this, LanguageSelectionUI.class));
                Utils.openNewActivityAnim(SettingUI.this,false);
                break;
            case R.id.useAgree:
                String result = "";
                String language = MySharedPrefs.readString(SettingUI.this,MySharedPrefs.FILE_APPLICATION,MySharedPrefs.KEY_LANGUAFE);
                if (TextUtils.isEmpty(language)){
                    Locale locale = new Locale(Locale.getDefault().getLanguage());
                    if (TextUtils.equals(locale.getLanguage(),"zh")){
                        result = Constants.USE_AGREE_ZH;
                    }else{
                        result = Constants.USE_AGREE_EN;
                    }
                }else{
                    if (TextUtils.equals(language,"zh")){
                        result = Constants.USE_AGREE_ZH;
                    }else{
                        result = Constants.USE_AGREE_EN;
                    }
                }
                Intent intent = new Intent(SettingUI.this, WebViewUI.class);
                intent.putExtra("loadUrl", result);
                intent.putExtra("title", getString(R.string.use_agreement));
                startActivity(intent);
                Utils.openNewActivityAnim(SettingUI.this,false);
                break;
            case R.id.blackListBody:
                startActivity(new Intent(SettingUI.this, BlackListUI.class));
                Utils.openNewActivityAnim(SettingUI.this,false);
                break;
            case R.id.exit:
                checkLogOut();
                break;
            default:
                super.onClick(v);
                break;

        }
    }

    /**
     * check logout
     * */
    private void checkLogOut(){
        if (NextApplication.myInfo != null && TextUtils.isEmpty(NextApplication.myInfo.getMid())&& TextUtils.isEmpty(NextApplication.myInfo.getMobile())&& TextUtils.isEmpty(NextApplication.myInfo.getEmail())) {
            MyViewDialogFragment mdf = new MyViewDialogFragment(MyViewDialogFragment.DIALOG_CHECK_LOGOUT);
            mdf.setTitleAndContentText(getString(R.string.account_logout_mid_warn), getString(R.string.account_logout_mid_hint));
            mdf.setOkCallback(new MyViewDialogFragment.OkCallback() {
                @Override
                public void okBtn() {
                    checkLogOutAgain();
                }
            });
            mdf.setCancelCallback(new MyViewDialogFragment.CancelCallback() {
                @Override
                public void cancelBtn() {
                    startActivity(new Intent(SettingUI.this, SecurityUI.class));
                    Utils.openNewActivityAnim(SettingUI.this,false);
                }
            });
            mdf.show(getSupportFragmentManager(), "mdf");
        }else{
            MyViewDialogFragment mdf = new MyViewDialogFragment();
            mdf.setTitleAndContentText(getString(R.string.account_logout_warn), getString(R.string.account_logout_hint));
            mdf.setOkCallback(new MyViewDialogFragment.OkCallback() {
                @Override
                public void okBtn() {
                    if (TextUtils.isEmpty(NextApplication.myInfo.getToken())){
                        exitApp();
                    }else{
                        logOutMethod();
                    }

                }
            });
            mdf.show(getSupportFragmentManager(), "mdf");
        }

    }

    /**
     * check logout again
     * */
    private void checkLogOutAgain() {
        MyViewDialogFragment mdf = new MyViewDialogFragment(MyViewDialogFragment.DIALOG_CHECK_LOGOUT_AGAIBN);
        mdf.setTitleAndContentText(getString(R.string.account_logout_mid_warn), getString(R.string.account_logout_mid_hint_again));
        mdf.setOkCallback(new MyViewDialogFragment.OkCallback() {
            @Override
            public void okBtn() {
                if (TextUtils.isEmpty(NextApplication.myInfo.getToken())){
                    exitApp();
                }else{
                    logOutMethod();
                }

            }
        });
        mdf.setCancelCallback(new MyViewDialogFragment.CancelCallback() {
            @Override
            public void cancelBtn() {
                finish();
            }
        });
        mdf.show(getSupportFragmentManager(), "mdf");
    }

    /**
     * 退出方法
     * */
    private void logOutMethod() {
        NetRequestImpl.getInstance().logout(new RequestListener() {
            @Override
            public void start() {
                LoadingDialog.show(SettingUI.this,"");
            }

            @Override
            public void success(JSONObject response) {
                LoadingDialog.close();
                exitApp();
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                LoadingDialog.close();
                exitApp();
            }
        });

    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && (Constants.CHANGE_LANGUAGE.equals(intent.getAction()))) {
                setTitle(getString(R.string.setting));
                Utils.updateViewLanguage(findViewById(android.R.id.content));
            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()){
            case R.id.startSmartMeshWorkButton:
                MySharedPrefs.writeInt(NextApplication.mContext,MySharedPrefs.FILE_USER,MySharedPrefs.KEY_NO_NETWORK_COMMUNICATION + NextApplication.myInfo.getLocalId(),isChecked ? 1 : 0);
                if (isChecked){
                    startSmartMeshWorkButton.setBackColor(getResources().getColorStateList(R.color.switch_button_green));
                    //Start without social network service
                    startService(new Intent(this, AppNetService.class));
                    Utils.sendBroadcastReceiver(SettingUI.this,new Intent(Constants.OPEN_SMARTMESH_NETWORE), false);
                }else{
                    startSmartMeshWorkButton.setBackColor(getResources().getColorStateList(R.color.switch_button_gray));
                    int version =android.os.Build.VERSION.SDK_INT;
                    if(version >= 16){
                        Utils.sendBroadcastReceiver(SettingUI.this,new Intent(Constants.CLOSE_SMARTMESH_NETWORE), false);
                        Intent offlineservice = new Intent(NextApplication.mContext, AppNetService.class);
                        stopService(offlineservice);
                    }
                }
                break;
            case R.id.gestureButton:
                if (isChecked){
                    gestureButton.setBackColor(getResources().getColorStateList(R.color.switch_button_green));
                }else{
                    gestureButton.setBackColor(getResources().getColorStateList(R.color.switch_button_gray));
                }
                byte[] gestureByte  = ACache.get(NextApplication.mContext).getAsBinary(Constants.GESTURE_PASSWORD + NextApplication.myInfo.getLocalId());
                if (gestureByte != null && gestureByte.length > 0){
                    Intent intent = new Intent(SettingUI.this,GestureLoginActivity.class);
                    intent.putExtra("type",isChecked ? 1 : 2);
                    startActivity(intent);
                }else{
                    Intent intent = new Intent(SettingUI.this,CreateGestureActivity.class);
                    startActivity(intent);
                }
                break;
        }

    }
}
