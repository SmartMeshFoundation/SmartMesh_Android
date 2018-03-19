package com.lingtuan.firefly.wallet;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.quickmark.QuickMarkShowUI;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.wallet.vo.StorableWallet;

/**
 * Created on 2018/3/16.
 * Transfers or receipts
 */

public class WalletSendDetailUI extends BaseActivity{

    private TextView walletTransfer;
    private TextView walletReceipt;
    private int type;//0 eth transfer 、2 smt transfer 、3 mesh transfer
    private StorableWallet storableWallet;

    @Override
    protected void setContentView() {
        setContentView(R.layout.wallet_send_detail_layout);
        getPassData();
    }

    private void getPassData() {
        type = getIntent().getIntExtra("sendtype",-1);
        storableWallet = (StorableWallet) getIntent().getSerializableExtra("storableWallet");
    }

    @Override
    protected void findViewById() {
        walletTransfer = (TextView) findViewById(R.id.walletTransfer);
        walletReceipt = (TextView) findViewById(R.id.walletReceipt);
    }

    @Override
    protected void setListener() {
        walletTransfer.setOnClickListener(this);
        walletReceipt.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        if (type == 0){
            setTitle(getString(R.string.eth));
        }else if (type == 1){
            setTitle(getString(R.string.smt));
        }else if (type == 2){
            setTitle(getString(R.string.mesh));
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.walletTransfer:
                Intent ethIntent = new Intent(WalletSendDetailUI.this,WalletSendActivity.class);
                ethIntent.putExtra("sendtype", type);
                startActivity(ethIntent);
                Utils.openNewActivityAnim(WalletSendDetailUI.this,false);
                break;
            case R.id.walletReceipt:
                if (storableWallet == null){
                    return;
                }
                Intent qrEthIntent = new Intent(WalletSendDetailUI.this,QuickMarkShowUI.class);
                qrEthIntent.putExtra("type", type + 1);
                qrEthIntent.putExtra("address", storableWallet.getPublicKey());
                startActivity(qrEthIntent);
                Utils.openNewActivityAnim(WalletSendDetailUI.this,false);
                break;
        }
    }
}
