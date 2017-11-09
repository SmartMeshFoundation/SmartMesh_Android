package com.lingtuan.firefly.wallet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.wallet.util.WalletStorage;
import com.lingtuan.firefly.wallet.vo.StorableWallet;

/**
 * Created  on 2017/8/18.
 * 创建钱包
 */

public class WalletCreateActivity extends BaseActivity {

    EditText walletName;//钱包名称
    EditText walletPwd;//钱包密吗
    EditText walletAgainPwd;//再次输入钱包密吗
    EditText walletPwdInfo;//密码提示信息

    private TextView createWallet;//创建钱包

    //清除钱包名称  显示密码
    private ImageView clearWalletName,isShowPass;

    private WalletHandler mHandler;

    /**
     * 显示密码
     */
    boolean isShowPassWorld = false;

    @Override
    protected void setContentView() {
        setContentView(R.layout.wallet_create_layout);
    }

    @Override
    protected void findViewById() {
        walletName = (EditText) findViewById(R.id.walletName);
        walletPwd = (EditText) findViewById(R.id.walletPwd);
        walletAgainPwd = (EditText) findViewById(R.id.walletAgainPwd);
        walletPwdInfo = (EditText) findViewById(R.id.walletPwdInfo);

        createWallet = (TextView) findViewById(R.id.createWallet);
        clearWalletName = (ImageView) findViewById(R.id.clearWalletName);
        isShowPass = (ImageView) findViewById(R.id.isShowPass);
    }

    @Override
    protected void setListener() {
        createWallet.setOnClickListener(this);
        clearWalletName.setOnClickListener(this);
        isShowPass.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.wallet_create));
        mHandler = new WalletHandler(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.WALLET_SUCCESS);
        filter.addAction(Constants.WALLET_ERROR);
        filter.addAction(Constants.NO_MEMORY);
        registerReceiver(mBroadcastReceiver, filter);
    }

    //创建钱包  导入钱包
    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.clearWalletName://清除名称
                walletName.setText("");
                break;
            case R.id.isShowPass://显示或隐藏密码
                isShowPassWorld = !isShowPassWorld;
                if (isShowPassWorld) { /* 设定EditText的内容为可见的 */
                    walletPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    isShowPass.setImageResource(R.drawable.eye_open);
                } else {/* 设定EditText的内容为隐藏*/
                    walletPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    isShowPass.setImageResource(R.drawable.eye_close);
                }
                break;
            case R.id.createWallet:
                if (walletName.length() > 12 || walletName.length() <= 0){
                    showToast(getString(R.string.wallet_name_warning));
                    return;
                }
                if (walletPwd.length() > 16 || walletPwd.length() < 6){
                    showToast(getString(R.string.wallet_pwd_warning));
                    return;
                }
                String password = walletPwd.getText().toString().trim();
                String name = walletName.getText().toString().trim();
                String pwdInfo = walletPwdInfo.getText().toString().trim();
                if (TextUtils.equals(password,walletAgainPwd.getText().toString().trim())){
                    for(StorableWallet storableWallet : WalletStorage.getInstance(getApplicationContext()).get()){
                        if(name.equals(storableWallet.getWalletName())){
                            showToast(getString(R.string.account_name_exist));
                            return;
                        }
                    }
                    LoadingDialog.show(WalletCreateActivity.this,getString(R.string.wallet_create_ing));
                    new WalletThread(mHandler,getApplicationContext(),name,password,pwdInfo,null,0).start();
                }else{
                    showToast(getString(R.string.account_pwd_again_warning));
                }
                break;
            default:
                super.onClick(v);
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && (Constants.WALLET_SUCCESS.equals(intent.getAction()))) {
                createSuccess();
            }else if (intent != null && (Constants.WALLET_ERROR.equals(intent.getAction()))) {
                LoadingDialog.close();
                showToast(getString(R.string.notification_wallgen_failure));
            }
            else if (intent != null && (Constants.NO_MEMORY.equals(intent.getAction()))) {
                LoadingDialog.close();
                showToast(getString(R.string.notification_wallgen_no_memory));
            }
        }
    };

    private void createSuccess(){
        LoadingDialog.close();
        startActivity(new Intent(WalletCreateActivity.this,WalletCreateSuccessActivity.class));
        showToast(getString(R.string.notification_wallgen_finished));
        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注销广播
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }
    }

}
