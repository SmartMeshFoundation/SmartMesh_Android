package com.lingtuan.firefly.wallet;

import android.view.View;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.Utils;

/**
 * Created on 2017/8/22.
 * 显示私钥明文
 * {@link WalletCopyActivity}
 */

public class WalletPrivateKeyActivity extends BaseActivity{

    private TextView walletPrivateKey;
    private TextView walletCopyPrivateKey;

    private String privateKey;

    private boolean hasCopy;//是否已copy

    @Override
    protected void setContentView() {
        setContentView(R.layout.wallet_show_privatekey_layout);
        getPassData();
    }

    @Override
    protected void findViewById() {
        walletPrivateKey = (TextView) findViewById(R.id.walletPrivateKey);
        walletCopyPrivateKey = (TextView) findViewById(R.id.walletCopyPrivateKey);
    }

    @Override
    protected void setListener() {
        walletCopyPrivateKey.setOnClickListener(this);
    }

    private void getPassData() {
        privateKey = getIntent().getStringExtra(Constants.PRIVATE_KEY);
    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.wallet_show_private_key_2));
        walletPrivateKey.setText(privateKey);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.walletCopyPrivateKey:
                Utils.copyText(WalletPrivateKeyActivity.this,privateKey);
                walletCopyPrivateKey.setText(getString(R.string.wallet_show_private_key_3));
                break;
            default:
                super.onClick(v);
        }
    }
}
