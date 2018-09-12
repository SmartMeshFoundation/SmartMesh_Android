package com.lingtuan.firefly.wallet;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.ui.MainFragmentUI;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.wallet.util.WalletStorage;
import com.lingtuan.firefly.wallet.vo.StorableWallet;

import java.util.ArrayList;

import butterknife.OnClick;

/**
 * Created on 2017/8/22.
 * The purse to create success page
 */

public class WalletCopyMnemonicUI extends BaseActivity {


    private String mnemonic;

    @Override
    protected void setContentView() {
        setContentView(R.layout.wallet_copy_mnemonic_layout);
        getPassData();
    }

    private void getPassData(){
        mnemonic = getIntent().getStringExtra(Constants.MNEMONIC);
    }

    @Override
    protected void findViewById() {

    }

    @Override
    protected void setListener() {

    }

    @OnClick({R.id.walletCopy,R.id.app_back})
    public void onClick(View v){
        switch (v.getId()){
            case R.id.walletCopy:
                copyMethod();
                break;
            case R.id.app_back:
                startActivity(new Intent(WalletCopyMnemonicUI.this,MainFragmentUI.class));
                finish();
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
            Intent intent = new Intent(WalletCopyMnemonicUI.this,WalletCopyMnemonicNextUI.class);
            intent.putExtra(Constants.MNEMONIC,mnemonic);
            startActivity(intent);
            Utils.openNewActivityAnim(WalletCopyMnemonicUI.this,true);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(WalletCopyMnemonicUI.this,MainFragmentUI.class));
    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.notification_wallgen_success));
    }
}
