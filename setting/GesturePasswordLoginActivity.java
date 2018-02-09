package com.lingtuan.firefly.setting;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.GravityCompat;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.custom.gesturelock.ACache;
import com.lingtuan.firefly.custom.picker.OptionPicker;
import com.lingtuan.firefly.custom.picker.WheelView;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.wallet.WalletCopyActivity;
import com.lingtuan.firefly.wallet.util.WalletStorage;
import com.lingtuan.firefly.wallet.vo.StorableWallet;

import org.json.JSONException;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Locale;

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
    //0 login、  1 open gesture、 2 close gesture、 3 wallet mode login 、4 login again
    private int type ;
    //wallet mode
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
        walletAddress.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.gesture_forget_gesture));
        if (type == 2){
            gestureLogin.setVisibility(View.VISIBLE);
            gestureLogin.setText(getString(R.string.gesture_close));
            setTitle(getString(R.string.gesture_pwd_close));
        }else if (type == 3){
            setTitle(getString(R.string.login));
            gestureLogin.setVisibility(View.GONE);
        }else if (type == 4){
            setTitle(getString(R.string.login));
            gestureLogin.setVisibility(View.GONE);
            WalletStorage.getInstance(NextApplication.mContext).loadAll(NextApplication.mContext);
        }else{
            gestureLogin.setVisibility(View.VISIBLE);
            gestureLogin.setText(getString(R.string.gesture_login));
            setTitle(getString(R.string.gesture_forget_gesture));
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
            case R.id.walletAddress:
                onConstellationPicker();
                break;
        }
    }

    /**
     * Load or refresh the wallet information
     * */
    private void initWalletInfo(){
        ArrayList<StorableWallet> storableWallets;
        if (type == 4){
            storableWallets = WalletStorage.getInstance(NextApplication.mContext).getAll();
        }else{
            storableWallets = WalletStorage.getInstance(NextApplication.mContext).get();
        }
        if (storableWallets == null || storableWallets.size() <= 0){
            return;
        }
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
                    if (type == 3 || type == 4){
                        MySharedPrefs.writeInt(GesturePasswordLoginActivity.this, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_IS_WALLET_PATTERN, 2);
                        Intent intent = new Intent(Constants.ACTION_GESTURE_LOGIN);
                        WalletStorage.getInstance(NextApplication.mContext).addWalletList(storableWallet,NextApplication.mContext);
                        Utils.sendBroadcastReceiver(GesturePasswordLoginActivity.this,intent,false);
                    }else{
                        if (NextApplication.myInfo != null){
                            MySharedPrefs.writeInt(GesturePasswordLoginActivity.this,MySharedPrefs.FILE_USER,MySharedPrefs.GESTIRE_ERROR + NextApplication.myInfo.getLocalId(),0);
                            if (type == 2){
                                ACache.get(NextApplication.mContext).put(Constants.GESTURE_PASSWORD + NextApplication.myInfo.getLocalId(),"");
                            }else{
                                Intent intent = new Intent(Constants.ACTION_GESTURE_LOGIN);
                                Utils.sendBroadcastReceiver(GesturePasswordLoginActivity.this,intent,false);
                            }
                        }
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


    public void onConstellationPicker() {
        ArrayList<StorableWallet> storableWallets;
        if (type == 4){
            storableWallets = WalletStorage.getInstance(NextApplication.mContext).getAll();
        }else{
            storableWallets = WalletStorage.getInstance(NextApplication.mContext).get();
        }
        if (storableWallets == null || storableWallets.size() <= 0){
            return;
        }
        int index = -1;
        String nameArray[] = new String[storableWallets.size()];
        for (int i = 0 ; i < storableWallets.size(); i++){
            String address = storableWallets.get(i).getPublicKey();
            if (storableWallets.get(i).isSelect()){
                index = i;
            }
            if(!address.startsWith("0x")){
                address = "0x"+address;
            }
            nameArray[i] =  address.replace(address.substring(12,30),"...");
        }
        OptionPicker picker = new OptionPicker(this,nameArray);
        picker.setCycleDisable(true);//Do not disable loops
        picker.setTopBackgroundColor(0xFFEEEEEE);
        picker.setTopHeight(40);
        picker.setTextSize(14);
        picker.setOffset(3);
        picker.setTopLineColor(getResources().getColor(R.color.textColorCard));
        picker.setTopLineHeight(1);
        picker.setCancelTextColor(getResources().getColor(R.color.textColorCard));
        picker.setCancelTextSize(13);
        picker.setSubmitTextColor(getResources().getColor(R.color.gesture_lock_select));
        picker.setSubmitTextSize(13);
        picker.setTextColor(getResources().getColor(R.color.black), getResources().getColor(R.color.textColorCard));
        WheelView.DividerConfig config = new WheelView.DividerConfig();
        config.setColor(getResources().getColor(R.color.tab_sep_line));//Line color
        config.setAlpha(140);//Line transparency
        config.setRatio(1);//Line ratio
        picker.setDividerConfig(config);
        picker.setBackgroundColor(0xFFE1E1E1);
        picker.setSelectedIndex(index);
        picker.setCanceledOnTouchOutside(true);
        picker.setOnOptionPickListener(new OptionPicker.OnOptionPickListener() {
            @Override
            public void onOptionPicked(int index, String item) {
                ArrayList<StorableWallet> storableWallets;
                if (type == 4){
                    storableWallets = WalletStorage.getInstance(NextApplication.mContext).getAll();
                }else{
                    storableWallets = WalletStorage.getInstance(NextApplication.mContext).get();
                }
                for (int i = 0 ; i < storableWallets.size(); i++){
                    if (i != index){
                        if (type == 4){
                            WalletStorage.getInstance(NextApplication.mContext).getAll().get(i).setSelect(false);
                        }else{
                            WalletStorage.getInstance(getApplicationContext()).get().get(i).setSelect(false);
                        }
                    }else{
                        if (type == 4){
                            WalletStorage.getInstance(NextApplication.mContext).getAll().get(i).setSelect(true);
                        }else{
                            WalletStorage.getInstance(getApplicationContext()).get().get(i).setSelect(true);
                        }
                    }
                }
                initWalletInfo();
                if (type != 4){
                    Utils.sendBroadcastReceiver(GesturePasswordLoginActivity.this, new Intent(Constants.WALLET_REFRESH_GESTURE), false);
                }
            }
        });
        picker.show();
    }
}
