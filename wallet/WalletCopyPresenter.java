package com.lingtuan.firefly.wallet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.ui.AlertActivity;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.MyToast;
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

public class WalletCopyPresenter {

    private Context context;
    private StorableWallet storableWallet;
    private String iconId;
    private int type;//0 the newly created, 1 backup wallet

    private EditText walletCopyName;
    private TextView address;

    public WalletCopyPresenter(Context context){
        this.context = context;
    }

    public void initView(EditText walletCopyName,TextView address){
        this.walletCopyName = walletCopyName;
        this.address = address;
    }


    /**
     * get pass data
     * 获取传递的数据
     * */
    public void getPassData(StorableWallet storableWallet,String iconId,int type) {
        this.storableWallet = storableWallet;
        this.iconId = iconId;
        this.type = type;
    }

    /**
     * init data
     * 初始化数据
     * */
    public void initData(TextView appBtnRight, TextView walletCopyPwdInfo,View walletCopyPwdInfoLine,ImageView icon,TextView success,TextView walletCopyKey,TextView walletCopyKeyStore) {
        if (NextApplication.myInfo != null && !TextUtils.isEmpty(NextApplication.myInfo.getLocalId())) {
            appBtnRight.setVisibility(View.VISIBLE);
            appBtnRight.setText(context.getString(R.string.save));
            walletCopyName.setEnabled(true);
        } else {
            walletCopyName.setEnabled(false);
        }

        if (storableWallet != null) {
            walletCopyName.setHint(context.getString(R.string.wallet_copy_name, storableWallet.getWalletName()));
            if (TextUtils.isEmpty(storableWallet.getPwdInfo())) {
                walletCopyPwdInfo.setVisibility(View.GONE);
                walletCopyPwdInfoLine.setVisibility(View.GONE);
            } else {
                walletCopyPwdInfo.setText(context.getString(R.string.wallet_copy_pwd_info, storableWallet.getPwdInfo()));
                walletCopyPwdInfoLine.setVisibility(View.VISIBLE);
            }
        }
        if (TextUtils.isEmpty(storableWallet.getWalletImageId()) || !storableWallet.getWalletImageId().startsWith("icon_static_")) {
            storableWallet.setWalletImageId(iconId);
        }
        icon.setImageResource(Utils.getWalletImageId(context, storableWallet.getWalletImageId()));
        if (type == 1) {
            success.setVisibility(View.GONE);
            address.setVisibility(View.VISIBLE);
            String key = storableWallet.getPublicKey();
            if (!key.startsWith("0x")) {
                key = "0x" + key;
            }
            address.setText(key);
        } else {
            success.setVisibility(View.VISIBLE);
            address.setVisibility(View.GONE);
        }

        //Observe the purse
        if (storableWallet.getWalletType() == 1) {
            walletCopyKey.setEnabled(false);
            walletCopyKeyStore.setEnabled(false);
        }
    }

    /**
     * is show wallet dialog
     * 是否显示操作提示
     * */
    public void onResume(){
        boolean isShowWalletDialog = MySharedPrefs.readBooleanNormal(context, MySharedPrefs.FILE_USER, MySharedPrefs.IS_SHOW_WALLET_DIALOG);
        if (isShowWalletDialog) {
            Intent intent = new Intent(context, AlertActivity.class);
            intent.putExtra("type", 4);
            context.startActivity(intent);
            ((Activity)context).overridePendingTransition(0, 0);
        } else {
            MySharedPrefs.writeBoolean(context, MySharedPrefs.FILE_USER, MySharedPrefs.IS_SHOW_WALLET_DIALOG, true);
        }
    }

    /**
     * update wallet name
     * 修改用户名称
     * */
    public void updateWalletName(){
        String walletName = walletCopyName.getText().toString();
        String newAddress = address.getText().toString().trim();
        boolean hasName = false;
        for (int i = 0; i < WalletStorage.getInstance(context.getApplicationContext()).get().size(); i++) {
            String tempWalletName = WalletStorage.getInstance(context.getApplicationContext()).get().get(i).getWalletName();
            if (TextUtils.equals(walletName,tempWalletName)){
                hasName = true;
                break;
            }
        }
        if (hasName){
            MyToast.showToast(context,context.getString(R.string.notification_wallimp_repeat_username));
            return;
        }
        for (int i = 0; i < WalletStorage.getInstance(context.getApplicationContext()).get().size(); i++) {
            String walletAddress = WalletStorage.getInstance(context.getApplicationContext()).get().get(i).getPublicKey();
            if (TextUtils.equals(walletAddress, storableWallet.getPublicKey())) {
                WalletStorage.getInstance(context.getApplicationContext()).get().get(i).setWalletName(walletName);
                break;
            }
        }
        WalletStorage.getInstance(context).updateWalletName(context, newAddress, walletName);
        MyToast.showToast(context,context.getString(R.string.notification_update_username_success));
        Utils.sendBroadcastReceiver(context, new Intent(Constants.WALLET_UPDATE_NAME), false);
    }

    /**
     * password authentication
     * @ param type 0 for the private key, 1 for keyStore, 2 to delete the wallet
     */
    public void showPwdDialog(final int type) {
        MyViewDialogFragment mdf = new MyViewDialogFragment(MyViewDialogFragment.DIALOG_INPUT_PWD, new MyViewDialogFragment.EditCallback() {
            @Override
            public void getEditText(String editText) {
                switch (type) {
                    case 0:
                        getWalletPrivateKey(editText, 0);
                        break;
                    case 1:
                        getWalletKeyStore(editText);
                        break;
                    case 2:
                        getWalletPrivateKey(editText, 2);
                        break;
                }

            }
        });
        if (type == 2) {
            mdf.setTitleAndContentText(null, context.getString(R.string.wallet_scan_del_hint));
        }
        mdf.show(((AppCompatActivity)context).getSupportFragmentManager(), "mdf");
    }

    /**
     * Observe purse to delete
     */
    public void showDelSacnWalletDialog() {
        MyViewDialogFragment mdf = new MyViewDialogFragment();
        mdf.setTitleAndContentText(context.getString(R.string.wallet_delete), context.getString(R.string.wallet_scan_del));
        mdf.setOkCallback(new MyViewDialogFragment.OkCallback() {
            @Override
            public void okBtn() {
                delWallet();
            }
        });
        mdf.show(((AppCompatActivity)context).getSupportFragmentManager(), "mdf");
    }

    /**
     * access to the private key
     * Password @ param walletPwd purse
     */
    private void getWalletPrivateKey(final String walletPwd, final int type) {
        LoadingDialog.show(context, "");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Credentials keys = WalletStorage.getInstance(context.getApplicationContext()).getFullWallet(context, walletPwd, storableWallet.getPublicKey());
                    BigInteger privateKey = keys.getEcKeyPair().getPrivateKey();
                    Message message = Message.obtain();
                    message.what = type;
                    message.obj = privateKey.toString(16);
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
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    mHandler.sendEmptyMessage(5);
                }
            }
        }).start();
    }


    /**
     * get the keyStore
     * Password @ param walletPwd purse
     */
    private void getWalletKeyStore(final String walletPwd) {
        LoadingDialog.show(context, "");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String keyStore = WalletStorage.getInstance(context.getApplicationContext()).getWalletKeyStore(context, walletPwd, storableWallet.getPublicKey());
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
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    mHandler.sendEmptyMessage(5);
                }
            }
        }).start();
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0://The private key
                    LoadingDialog.close();
                    storableWallet.setCanExportPrivateKey(0);
                    storableWallet.setBackup(true);
                    ArrayList<StorableWallet> list = WalletStorage.getInstance(context.getApplicationContext()).get();
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i).getPublicKey().equals(storableWallet.getPublicKey())) {
                            list.get(i).setCanExportPrivateKey(0);
                            list.get(i).setBackup(true);
                            break;
                        }
                    }
                    String privateKey = (String) msg.obj;
                    WalletStorage.getInstance(context.getApplicationContext()).updateWalletToList(context, storableWallet.getPublicKey(), true);
                    Utils.sendBroadcastReceiver(context, new Intent(Constants.WALLET_REFRESH_BACKUP), false);
                    Intent showPrivateKey = new Intent(context, WalletPrivateKeyActivity.class);
                    showPrivateKey.putExtra(Constants.PRIVATE_KEY, privateKey);
                    context.startActivity(showPrivateKey);
                    Utils.openNewActivityAnim((Activity)context,false);
                    break;
                case 1://keystore
                    LoadingDialog.close();
                    storableWallet.setBackup(true);
                    ArrayList<StorableWallet> walletList = WalletStorage.getInstance(context.getApplicationContext()).get();
                    for (int i = 0; i < walletList.size(); i++) {
                        if (walletList.get(i).getPublicKey().equals(storableWallet.getPublicKey())) {
                            walletList.get(i).setBackup(true);
                            break;
                        }
                    }
                    WalletStorage.getInstance(context.getApplicationContext()).updateWalletToList(context, storableWallet.getPublicKey(), true);
                    Utils.sendBroadcastReceiver(context, new Intent(Constants.WALLET_REFRESH_BACKUP), false);
                    String keyStore = (String) msg.obj;
                    Intent showKeyStore = new Intent(context, WalletKeyStoreActivity.class);
                    showKeyStore.putExtra(Constants.KEYSTORE, keyStore);
                    context.startActivity(showKeyStore);
                    Utils.openNewActivityAnim((Activity)context,false);
                    break;
                case 2://To delete the wallet
                    delWallet();
                    break;
                case 3://Password mistake
                    LoadingDialog.close();
                    MyToast.showToast(context,context.getString(R.string.wallet_copy_pwd_error));
                    break;
                case 4://The operation failure
                    LoadingDialog.close();
                    MyToast.showToast(context,context.getString(R.string.error));
                    break;
                case 5://Out of memory
                    LoadingDialog.close();
                    MyToast.showToast(context,context.getString(R.string.notification_wallgen_no_memory));
                    break;
            }
        }
    };

    /**
     * Delete the wallet data
     */
    private void delWallet() {
        WalletStorage.getInstance(context.getApplicationContext()).removeWallet(storableWallet.getPublicKey(), storableWallet.getWalletType(), context);
        delAddressMethod(storableWallet.getPublicKey());
        if (storableWallet.isSelect()) {
            WalletStorage.getInstance(context.getApplicationContext()).get().remove(storableWallet);
            if (WalletStorage.getInstance(context.getApplicationContext()).get().size() > 0) {
                WalletStorage.getInstance(context.getApplicationContext()).get().get(0).setSelect(true);
            } else {
                WalletStorage.getInstance(context.getApplicationContext()).destroy();
            }
            //Send to refresh the page
            Utils.sendBroadcastReceiver(context, new Intent(Constants.WALLET_REFRESH_DEL), false);
            Utils.exitActivityAndBackAnim((Activity)context,true);
        } else {
            WalletStorage.getInstance(context.getApplicationContext()).get().remove(storableWallet);
            if (WalletStorage.getInstance(context.getApplicationContext()).get().size() <= 0) {
                WalletStorage.getInstance(context.getApplicationContext()).destroy();
            }
            //Send to refresh the page
            Utils.sendBroadcastReceiver(context, new Intent(Constants.WALLET_REFRESH_DEL), false);
            Utils.exitActivityAndBackAnim((Activity)context,true);
        }
    }

    /**
     * delete wallet address
     *
     * @param address address
     */
    private void delAddressMethod(String address) {

        if (!address.startsWith("0x")) {
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

    public void onDestroy(){
        if (mHandler != null){
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
    }

}
