package com.lingtuan.firefly.wallet;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.ui.MainFragmentUI;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MyToast;
import com.lingtuan.firefly.util.MyViewDialogFragment;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.wallet.util.WalletStorage;
import com.lingtuan.firefly.wallet.vo.StorableWallet;

import org.json.JSONException;
import org.web3j.crypto.CipherException;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.OnClick;

/**
 * Created on 2017/8/22.
 * The purse to create success page
 */

public class WalletCopyMnemonicUI extends BaseActivity {


    private String mnemonic;
    private String walletAddress;

    @Override
    protected void setContentView() {
        setContentView(R.layout.wallet_copy_mnemonic_layout);
        getPassData();
    }

    private void getPassData(){
        mnemonic = getIntent().getStringExtra(Constants.MNEMONIC);
        walletAddress = getIntent().getStringExtra(Constants.WALLET_ADDRESS);
    }

    @Override
    protected void findViewById() {

    }

    @Override
    protected void setListener() {

    }


    @Override
    protected void initData() {
        setTitle(getString(R.string.wallet_backup_now));
    }

    @OnClick({R.id.walletCopy,R.id.app_back})
    public void onClick(View v){
        switch (v.getId()){
            case R.id.walletCopy:
                copyMethod();
                break;
            case R.id.app_back:
                clearMnemonic();
                break;
            default:
                super.onClick(v);
        }
    }

    private void copyMethod(){
        if (TextUtils.isEmpty(mnemonic)){
            String imgId = Utils.getWalletImg(WalletCopyMnemonicUI.this,(WalletStorage.getInstance(getApplicationContext()).get().size()-1));
            Intent copyIntent = new Intent(WalletCopyMnemonicUI.this,WalletCopyActivity.class);
            ArrayList<StorableWallet> list = WalletStorage.getInstance(getApplicationContext()).get();
            copyIntent.putExtra(Constants.WALLET_INFO,list.get(list.size()-1));
            copyIntent.putExtra(Constants.WALLET_IMAGE, imgId);
            startActivity(copyIntent);
        }else{
            showPwdDialog();
        }

    }

    @Override
    public void onBackPressed() {
        clearMnemonic();
    }

    private void clearMnemonic(){
        MyViewDialogFragment mdf = new MyViewDialogFragment();
        mdf.setTitleAndContentText(getString(R.string.wallet_export_prompt),getString(R.string.wallet_mnemonic_5));
        mdf.setOkCallback(new MyViewDialogFragment.OkCallback() {
            @Override
            public void okBtn() {
                startActivity(new Intent(WalletCopyMnemonicUI.this,MainFragmentUI.class));
                finish();
            }
        });
        mdf.show(getSupportFragmentManager(), "mdf");
    }

    /**
     * password authentication
     * @ param type 0 for the private key, 1 for keyStore, 2 to delete the wallet
     */
    public void showPwdDialog() {
        MyViewDialogFragment mdf = new MyViewDialogFragment(MyViewDialogFragment.DIALOG_INPUT_PWD, new MyViewDialogFragment.EditCallback() {
            @Override
            public void getEditText(String editText) {
                getWalletPrivateKey(editText);
            }
        });
        mdf.show(getSupportFragmentManager(), "mdf");
    }


    /**
     * access to the private key
     * Password @ param walletPwd purse
     */
    private void getWalletPrivateKey(final String walletPwd) {
        LoadingDialog.show(this, "");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    WalletStorage.getInstance(getApplicationContext()).getFullWallet(WalletCopyMnemonicUI.this, walletPwd, walletAddress);
                    mHandler.sendEmptyMessage(0);
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
                }catch (Exception e){
                    e.printStackTrace();
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
                    Intent intent = new Intent(WalletCopyMnemonicUI.this,WalletCopyMnemonicNextUI.class);
                    intent.putExtra(Constants.MNEMONIC,mnemonic);
                    intent.putExtra(Constants.WALLET_ADDRESS,walletAddress);
                    startActivity(intent);
                    Utils.openNewActivityAnim(WalletCopyMnemonicUI.this,true);
                    break;
                case 3://Password mistake
                    LoadingDialog.close();
                    MyToast.showToast(WalletCopyMnemonicUI.this,getString(R.string.wallet_copy_pwd_error));
                    break;
                case 4://The operation failure
                    LoadingDialog.close();
                    MyToast.showToast(WalletCopyMnemonicUI.this,getString(R.string.error));
                    break;
                case 5://Out of memory
                    LoadingDialog.close();
                    MyToast.showToast(WalletCopyMnemonicUI.this,getString(R.string.notification_wallgen_no_memory));
                    break;
            }
        }
    };
}
