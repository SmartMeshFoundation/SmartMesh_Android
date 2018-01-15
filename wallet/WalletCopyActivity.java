package com.lingtuan.firefly.wallet;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MyViewDialogFragment;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;
import com.lingtuan.firefly.wallet.util.WalletStorage;
import com.lingtuan.firefly.wallet.vo.StorableWallet;

import org.json.JSONException;
import org.json.JSONObject;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;

/**
 * Created on 2017/8/22.
 * Backup account
 */

public class WalletCopyActivity extends BaseActivity {

    TextView walletCopyName;//Account name
    TextView walletCopyPwdInfo;//Password prompt
    TextView walletCopyKey;//Export the private key
    TextView walletCopyKeyStore;//Export the KeyStore
    TextView walletDelete;//To delete the wallet
    TextView success,address;
    ImageView icon;
    private StorableWallet storableWallet;
    private int iconId;
    private int type;//0 the newly created, 1 backup wallet
    @Override
    protected void setContentView() {
        setContentView(R.layout.wallet_copy_layout);
        getPassData();
    }

    @Override
    protected void findViewById() {
        walletCopyName = (TextView) findViewById(R.id.walletCopyName);
        walletCopyPwdInfo = (TextView) findViewById(R.id.walletCopyPwdInfo);
        walletCopyKey = (TextView) findViewById(R.id.walletCopyKey);
        walletCopyKeyStore = (TextView) findViewById(R.id.walletCopyKeyStore);
        walletDelete = (TextView) findViewById(R.id.walletDelete);
        success = (TextView) findViewById(R.id.success);
        address = (TextView) findViewById(R.id.address);
        icon = (ImageView) findViewById(R.id.icon);
    }

    @Override
    protected void setListener() {
        walletCopyKey.setOnClickListener(this);
        walletCopyKeyStore.setOnClickListener(this);
        walletDelete.setOnClickListener(this);
    }

    private void getPassData() {
        storableWallet = (StorableWallet) getIntent().getSerializableExtra(Constants.WALLET_INFO);
        iconId = getIntent().getIntExtra(Constants.WALLET_ICON,0);
        type = getIntent().getIntExtra(Constants.WALLET_TYPE,0);
    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.wallet_copy));
        if (storableWallet != null){
            walletCopyName.setText(getString(R.string.wallet_copy_name,storableWallet.getWalletName()));
            if (TextUtils.isEmpty(storableWallet.getPwdInfo())){
                walletCopyPwdInfo.setVisibility(View.GONE);
            }else{
                walletCopyPwdInfo.setText(getString(R.string.wallet_copy_pwd_info,storableWallet.getPwdInfo()));
            }
        }
        icon.setImageResource(iconId);
        if(type==1)
        {
            success.setVisibility(View.GONE);
            address.setVisibility(View.VISIBLE);
            String key =storableWallet.getPublicKey();
            if(!key.startsWith("0x"))
            {
                key = "0x"+key;
            }
            address.setText(key);
        }
        else{
            success.setVisibility(View.VISIBLE);
            address.setVisibility(View.GONE);
        }

        //Observe the purse
        if (storableWallet.getWalletType() == 1){
            walletCopyKey.setEnabled(false);
            walletCopyKeyStore.setEnabled(false);
        }

//        //Export the private key
//        if (storableWallet.getCanExportPrivateKey() != 1){
//            walletCopyKey.setEnabled(false);
//        }
    }

    /**
     * to export the private key
     * export KeyStore
     * to delete the wallet
     * */
    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.walletCopyKey:
                showPwdDialog(0);
                break;
            case R.id.walletCopyKeyStore:
                showPwdDialog(1);
                break;
            case R.id.walletDelete:
                if (storableWallet.getWalletType() == 1){
                    showDelSacnWalletDialog();
                }else{
                    showPwdDialog(2);
                }

                break;
            default:
                super.onClick(v);
        }
    }

    /**
     * password authentication
     * @ param type 0 for the private key, 1 for keyStore, 2 to delete the wallet
     * */
    private void showPwdDialog(final int type){
        MyViewDialogFragment mdf = new MyViewDialogFragment(MyViewDialogFragment.DIALOG_INPUT_PWD, new MyViewDialogFragment.EditCallback() {
            @Override
            public void getEditText(String editText) {
                switch (type){
                    case 0:
                        getWalletPrivateKey(editText,0);
                        break;
                    case 1:
                        getWalletKeyStore(editText);
                        break;
                    case 2:
                        getWalletPrivateKey(editText,2);
                        break;
                }

            }
        });
        mdf.show(this.getSupportFragmentManager(), "mdf");
    }

    /**
     * Observe purse to delete
     * */
    private void showDelSacnWalletDialog(){
        MyViewDialogFragment mdf = new MyViewDialogFragment();
        mdf.setTitleAndContentText(getString(R.string.wallet_delete), getString(R.string.wallet_scan_del));
        mdf.setOkCallback(new MyViewDialogFragment.OkCallback() {
            @Override
            public void okBtn() {
                delWallet();
            }
        });
        mdf.show(getSupportFragmentManager(), "mdf");
    }

    /**
     * access to the private key
     * Password @ param walletPwd purse
     * */
    private void getWalletPrivateKey(final String walletPwd,final int type){
        LoadingDialog.show(WalletCopyActivity.this,"");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Credentials keys = WalletStorage.getInstance(getApplicationContext()).getFullWallet(WalletCopyActivity.this,walletPwd,storableWallet.getPublicKey());
                    BigInteger privateKey = keys.getEcKeyPair().getPrivateKey();
                    Message message = Message.obtain();
                    message.what = type;
                    message.obj = privateKey;
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


    /**
     * get the keyStore
     * Password @ param walletPwd purse
     * */
    private void getWalletKeyStore(final String walletPwd){
        LoadingDialog.show(WalletCopyActivity.this,"");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String keyStore =  WalletStorage.getInstance(getApplicationContext()).getWalletKeyStore(WalletCopyActivity.this,walletPwd,storableWallet.getPublicKey());
                    Message message = Message.obtain();
                    message.what = 1;
                    message.obj = keyStore;
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
                case 0://The private key
                    LoadingDialog.close();
                    BigInteger privateKey = (BigInteger) msg.obj;
                    Intent showPrivateKey = new Intent(WalletCopyActivity.this,WalletPrivateKeyActivity.class);
                    showPrivateKey.putExtra(Constants.PRIVATE_KEY,privateKey.toString());
                    startActivity(showPrivateKey);
                    storableWallet.setCanExportPrivateKey(0);
//                    walletCopyKey.setEnabled(false);
                    ArrayList<StorableWallet> list = WalletStorage.getInstance(getApplicationContext()).get();
                    for (int i = 0 ; i < list.size() ; i++){
                        if (list.get(i).getPublicKey().equals(storableWallet.getPublicKey())){
                            list.get(i).setCanExportPrivateKey(0);
                            break;
                        }
                    }
                    WalletStorage.getInstance(getApplicationContext()).updateWalletToList(WalletCopyActivity.this,storableWallet.getPublicKey());
                    break;
                case 1://keystore
                    LoadingDialog.close();
                    String keyStore = (String)msg.obj;
                    Intent showKeyStore = new Intent(WalletCopyActivity.this,WalletKeyStoreActivity.class);
                    showKeyStore.putExtra(Constants.KEYSTORE,keyStore);
                    startActivity(showKeyStore);
                    break;
                case 2://To delete the wallet
                    delWallet();
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

    /**
     * Delete the wallet data
     * */
    private void delWallet(){
        WalletStorage.getInstance(getApplicationContext()).removeWallet(storableWallet.getPublicKey(),storableWallet.getWalletType(),WalletCopyActivity.this);
        delAddressMethod(storableWallet.getPublicKey());
        if(storableWallet.isSelect()){
            WalletStorage.getInstance(getApplicationContext()).get().remove(storableWallet);
            if(WalletStorage.getInstance(getApplicationContext()).get().size()>0)
            {
                WalletStorage.getInstance(getApplicationContext()).get().get(0).setSelect(true);
            }
            else{
                WalletStorage.getInstance(getApplicationContext()).destroy();
            }
            //Send to refresh the page
            Utils.sendBroadcastReceiver(WalletCopyActivity.this, new Intent(Constants.WALLET_REFRESH_DEL), false);
            finish();
        }else{
            WalletStorage.getInstance(getApplicationContext()).get().remove(storableWallet);
            if(WalletStorage.getInstance(getApplicationContext()).get().size()<=0)
            {
                WalletStorage.getInstance(getApplicationContext()).destroy();
            }
            //Send to refresh the page
            Utils.sendBroadcastReceiver(WalletCopyActivity.this, new Intent(Constants.WALLET_REFRESH_DEL), false);
            finish();
        }
    }

    /**
     * delete wallet address
     * @param address address
     * */
    private void delAddressMethod(String address){

        if (!address.startsWith("0x")){
            address = "0x" + address;
        }

        NetRequestImpl.getInstance().delAddress(address, new RequestListener() {
            @Override
            public void start() {

            }

            @Override
            public void success(JSONObject response) {

            }

            @Override
            public void error(int errorCode, String errorMsg) {

            }
        });
    }
}
