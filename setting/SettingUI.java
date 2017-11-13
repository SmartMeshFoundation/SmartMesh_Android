
package com.lingtuan.firefly.setting;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.custom.SwitchButton;
import com.lingtuan.firefly.fragment.MySelfFragment;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MyViewDialogFragment;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created on 2017/9/13.
 * Settings page
 * {@link MySelfFragment}
 */

public class SettingUI extends BaseActivity implements CompoundButton.OnCheckedChangeListener {

    //语言选择
    private RelativeLayout languageSelectionBody;//Language selection
    private RelativeLayout blackListBody,versionBody;//The blacklist version detection
    private TextView exit;//exit
    private SwitchButton stealthButton;//stealth

    private TextView versionCheck;//The version number

    @Override
    protected void setContentView() {
        setContentView(R.layout.setting_layout);
    }

    @Override
    protected void findViewById() {
        languageSelectionBody = (RelativeLayout) findViewById(R.id.languageSelectionBody);
        blackListBody = (RelativeLayout) findViewById(R.id.blackListBody);
        versionBody = (RelativeLayout) findViewById(R.id.versionBody);
        versionCheck = (TextView) findViewById(R.id.versionCheck);
        exit = (TextView) findViewById(R.id.exit);
        stealthButton = (SwitchButton) findViewById(R.id.stealthButton);
    }

    @Override
    protected void setListener() {
        languageSelectionBody.setOnClickListener(this);
        blackListBody.setOnClickListener(this);
//        versionBody.setOnClickListener(this);
        exit.setOnClickListener(this);
        stealthButton.setOnCheckedChangeListener(this);
    }

    @Override
    protected void initData() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.CHANGE_LANGUAGE);//Refresh the page
        registerReceiver(mBroadcastReceiver, filter);
        setTitle(getString(R.string.setting));
        versionCheck.setText(Utils.getVersionName(SettingUI.this));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.languageSelectionBody:
                startActivity(new Intent(SettingUI.this, LanguageSelectionUI.class));
                Utils.openNewActivityAnim(SettingUI.this,false);
                break;
            case R.id.blackListBody:
                startActivity(new Intent(SettingUI.this, BlackListUI.class));
                Utils.openNewActivityAnim(SettingUI.this,false);
                break;
            case R.id.exit:
                MyViewDialogFragment mdf = new MyViewDialogFragment();
                mdf.setTitleAndContentText(getString(R.string.account_logout_warn), getString(R.string.account_logout_hint));
                mdf.setOkCallback(new MyViewDialogFragment.OkCallback() {
                    @Override
                    public void okBtn() {
                        logOutMethod();
                    }
                });
                mdf.show(getSupportFragmentManager(), "mdf");

                break;
            default:
                super.onClick(v);
                break;

        }
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

    }
}
