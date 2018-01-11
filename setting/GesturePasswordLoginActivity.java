package com.lingtuan.firefly.setting;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.custom.gesturelock.ACache;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.wallet.util.WalletStorage;
import com.lingtuan.firefly.wallet.vo.StorableWallet;

import org.json.JSONException;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;

/**
 * Created on 2018/1/11.
 */

public class GesturePasswordLoginActivity extends BaseActivity{

    private ImageView walletImg;
    private TextView walletName;
    private TextView walletAddress;
    private TextView pwdConfirm;
    private TextView gestureLogin;
    private EditText keyStorePwd;
    private int index = -1;//Which one is selected

    private int type ; //0 login  1 open gesture 2 close gesture

    private StorableWallet storableWallet;

    @Override
    protected void setContentView() {
        setContentView(R.layout.gesture_pwd_login_layout);
        getPassData();
    }

    private void getPassData() {
        type = getIntent().getIntExtra("type",0);
    }

    @Override
    protected void findViewById() {
        walletImg = (ImageView) findViewById(R.id.walletImg);
        walletName = (TextView) findViewById(R.id.walletName);
        walletAddress = (TextView) findViewById(R.id.walletAddress);
        pwdConfirm = (TextView) findViewById(R.id.pwdConfirm);
        gestureLogin = (TextView) findViewById(R.id.gestureLogin);
        keyStorePwd = (EditText) findViewById(R.id.keyStorePwd);
    }

    @Override
    protected void setListener() {
        pwdConfirm.setOnClickListener(this);
        gestureLogin.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        if (type == 2){
            gestureLogin.setText(getString(R.string.gesture_close));
        }else{
            gestureLogin.setText(getString(R.string.gesture_login));
        }
        initWalletInfo();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.pwdConfirm:
                checkPwd();
                break;
            case R.id.gestureLogin:
                Utils.exitActivityAndBackAnim(GesturePasswordLoginActivity.this,true);
                break;
        }
    }

    /**
     * Load or refresh the wallet information
     * */
    private void initWalletInfo(){
        ArrayList<StorableWallet> storableWallets = WalletStorage.getInstance(NextApplication.mContext).get();
        for (int i = 0 ; i < storableWallets.size(); i++){
            if (storableWallets.get(i).isSelect() ){
                index = i;
                int imgId = Utils.getWalletImg(GesturePasswordLoginActivity.this,i);
                walletImg.setImageResource(imgId);
                storableWallet = storableWallets.get(i);
                storableWallet.setImgId(imgId);
                walletName.setText(storableWallet.getWalletName());
                String address = storableWallet.getPublicKey();
                if(!address.startsWith("0x")){
                    address = "0x"+address;
                }
                walletAddress.setText(address);
                break;
            }
        }
        if (index == -1 && storableWallets.size() > 0){
            int imgId = Utils.getWalletImg(GesturePasswordLoginActivity.this,0);
            walletImg.setImageResource(imgId);
            storableWallet = storableWallets.get(0);
            storableWallet.setImgId(imgId);
            walletName.setText(storableWallet.getWalletName());
            String address = storableWallet.getPublicKey();
            if(!address.startsWith("0x")){
                address = "0x"+address;
            }
            walletAddress.setText(address);
        }
    }


    /**
     * access to the private key
     * Password @ param walletPwd purse
     * */
    private void checkPwd(){
        LoadingDialog.show(GesturePasswordLoginActivity.this,"");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Credentials keys = WalletStorage.getInstance(getApplicationContext()).getFullWallet(GesturePasswordLoginActivity.this,keyStorePwd.getText().toString().trim(),storableWallet.getPublicKey());
                    BigInteger privateKey = keys.getEcKeyPair().getPrivateKey();
                    Message message = Message.obtain();
                    message.obj = privateKey;
                    message.what = 1;
                    mHandler.sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                    mHandler.sendEmptyMessage(4);
                } catch (JSONException e) {
                    e.printStackTrace();
                    mHandler.sendEmptyMessage(4);
                } catch (CipherException e) {
                    e.printStackTrace();
                    mHandler.sendEmptyMessage(3);
                }catch (RuntimeException e) {
                    e.printStackTrace();
                    mHandler.sendEmptyMessage(5);
                }
            }
        }).start();
    }


    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1://success
                    MySharedPrefs.writeInt(GesturePasswordLoginActivity.this,MySharedPrefs.FILE_USER,MySharedPrefs.GESTIRE_ERROR + NextApplication.myInfo.getLocalId(),0);
                    if (type == 2){
                        ACache.get(NextApplication.mContext).put(Constants.GESTURE_PASSWORD + NextApplication.myInfo.getLocalId(),"");
                    }else{
                        Intent intent = new Intent(Constants.ACTION_GESTURE_LOGIN);
                        Utils.sendBroadcastReceiver(GesturePasswordLoginActivity.this,intent,false);
                    }
                    setResult(RESULT_OK);
                    finish();
                    break;
                case 3://Password mistake
                    LoadingDialog.close();
                    showToast(getString(R.string.wallet_copy_pwd_error));
                    break;
                case 4://The operation failure
                    LoadingDialog.close();
                    showToast(getString(R.string.error));
                    break;
                case 5://Out of memory
                    LoadingDialog.close();
                    showToast(getString(R.string.notification_wallgen_no_memory));
                    break;
            }
        }
    };
}
