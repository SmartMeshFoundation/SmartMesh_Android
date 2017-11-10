package com.lingtuan.firefly.wallet;

import android.view.View;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.Utils;


/**
 * Created on 2017/8/22.
 *
 * {@link WalletCopyActivity}
 */

public class WalletKeyStoreActivity extends BaseActivity {

    private TextView walletKeyStore;
    private TextView walletCopyKeyStore;

    private String keystore;

    private boolean hasCopy;//

    @Override
    protected void setContentView() {
        setContentView(R.layout.wallet_show_keystore_layout);
        getPassData();
    }

    @Override
    protected void findViewById() {
        walletKeyStore = (TextView) findViewById(R.id.walletKeyStore);
        walletCopyKeyStore = (TextView) findViewById(R.id.walletCopyKeyStore);
    }

    @Override
    protected void setListener() {
        walletCopyKeyStore.setOnClickListener(this);
    }

    private void getPassData() {
        keystore = getIntent().getStringExtra(Constants.KEYSTORE);
    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.wallet_show_keystore_2));
        walletKeyStore.setText(keystore);
    }

    @Override
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
