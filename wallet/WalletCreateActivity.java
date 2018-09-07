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

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.wallet.util.WalletStorage;
import com.lingtuan.firefly.wallet.vo.StorableWallet;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created  on 2017/8/18.
 * Create a wallet
 */

public class WalletCreateActivity extends BaseActivity {

    @BindView(R.id.walletName)
    EditText walletName;//Name of the wallet
    @BindView(R.id.walletPwd)
    EditText walletPwd;//The wallet password
    @BindView(R.id.walletAgainPwd)
    EditText walletAgainPwd;//Input the purse close again
    @BindView(R.id.walletPwdInfo)
    EditText walletPwdInfo;//Password prompt information
    @BindView(R.id.isShowPass)
    ImageView isShowPass;//Remove the wallet name Show the password

    private WalletHandler mHandler;

    /**
     * Show the password
     */
    boolean isShowPassWorld = false;

    @Override
    protected void setContentView() {
        setContentView(R.layout.wallet_create_layout);
    }

    @Override
    protected void findViewById() {

    }

    @Override
    protected void setListener() {

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

    //Create a wallet 、import the purse
    @OnClick({R.id.clearWalletName,R.id.isShowPass,R.id.createWallet})
    public void onClick(View v){
        switch (v.getId()){
            case R.id.clearWalletName://Remove the name
                walletName.setText("");
                break;
            case R.id.isShowPass://Show or hide the password
                isShowPassWorld = !isShowPassWorld;
                if (isShowPassWorld) { /* Set the EditText content is visible */
                    walletPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    isShowPass.setImageResource(R.drawable.eye_open);
                } else {/* The content of the EditText set as hidden*/
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
                    new WalletThread(mHandler,getApplicationContext(),name,password,pwdInfo,null,0,false).start();
                }else{
                    showToast(getString(R.string.account_pwd_again_warning));
                }
                break;
            default:
                super.onClick(v);
        }
    }

    /**
     * covert mnemonic string to List<String>
     */
    private static String convertMnemonicList(List<String> mnemonics) {
        StringBuilder sb = new StringBuilder();
        for (String mnemonic : mnemonics) {
            sb.append(mnemonic);
            sb.append(" ");
        }
        return sb.toString();
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
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }
    }

}
