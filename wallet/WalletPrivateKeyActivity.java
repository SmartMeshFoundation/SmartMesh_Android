package com.lingtuan.firefly.wallet;

import android.view.View;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.Utils;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created on 2017/8/22.
 * According to the private key plaintext
 * {@link WalletCopyActivity}
 */

public class WalletPrivateKeyActivity extends BaseActivity{

    @BindView(R.id.walletPrivateKey)
    TextView walletPrivateKey;
    @BindView(R.id.walletCopyPrivateKey)
    TextView walletCopyPrivateKey;

    private String privateKey;
    private boolean hasCopy;//Is the copy

    @Override
    protected void setContentView() {
        setContentView(R.layout.wallet_show_privatekey_layout);
        getPassData();
    }

    @Override
    protected void findViewById() {

    }

    @Override
    protected void setListener() {

    }

    private void getPassData() {
        privateKey = getIntent().getStringExtra(Constants.PRIVATE_KEY);
    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.wallet_show_private_key_2));
        walletPrivateKey.setText(privateKey);
    }

    @OnClick(R.id.walletCopyPrivateKey)
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
