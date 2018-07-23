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

public class WalletKeyStoreActivity extends BaseActivity {

    @BindView(R.id.walletKeyStore)
    TextView walletKeyStore;
    @BindView(R.id.walletCopyKeyStore)
    TextView walletCopyKeyStore;

    private String keystore;

    private boolean hasCopy;//Is the copy

    @Override
    protected void setContentView() {
        setContentView(R.layout.wallet_show_keystore_layout);
        getPassData();
    }

    @Override
    protected void findViewById() {

    }

    @Override
    protected void setListener() {

    }

    private void getPassData() {
        keystore = getIntent().getStringExtra(Constants.KEYSTORE);
    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.wallet_show_keystore_2));
        walletKeyStore.setText(keystore);
    }

    @OnClick(R.id.walletCopyKeyStore)
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.walletCopyKeyStore:
                Utils.copyText(WalletKeyStoreActivity.this,keystore);
                break;
            default:
                super.onClick(v);
        }
    }
}
