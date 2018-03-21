package com.lingtuan.firefly.wallet;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.quickmark.QuickMarkShowUI;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.wallet.vo.StorableWallet;
import com.lingtuan.firefly.wallet.vo.TransVo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2018/3/16.
 * Transfers or receipts
 */

public class WalletSendDetailUI extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    private TextView walletTransfer;
    private TextView walletReceipt;
    private int type;//0 eth transfer 、2 smt transfer 、3 mesh transfer
    private StorableWallet storableWallet;

    private SwipeRefreshLayout refreshLayout;
    private ListView transListView;
    private TransAdapter mAdapter;
    private ArrayList<TransVo> transVos;

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
        transListView = (ListView) findViewById(R.id.transListView);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
    }

    @Override
    protected void setListener() {
        walletTransfer.setOnClickListener(this);
        walletReceipt.setOnClickListener(this);
        refreshLayout.setOnRefreshListener(this);
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
        transVos = new ArrayList<>();
        mAdapter = new TransAdapter(WalletSendDetailUI.this,transVos,storableWallet.getPublicKey());
        transListView.setAdapter(mAdapter);
        new Handler().postDelayed(new Runnable(){
            public void run() {
                getTransMethod(0,storableWallet.getPublicKey());
                refreshLayout.setRefreshing(true);
            }
        }, 200);
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

    /**
     * To obtain transfer record interface
     * @param type 0 eth 1 smt
     * */
    private void getTransMethod(final int type,final String address) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<TransVo> mlist =   FinalUserDataBase.getInstance().getTransList(type,address);
                transVos.clear();
                transVos.addAll(mlist);
                mHandler.sendEmptyMessage(type);
            }
        }).start();
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    refreshLayout.setRefreshing(false);
                    mAdapter.resetSource(transVos,storableWallet.getPublicKey());
                    break;
            }
        }
    };

    @Override
    public void onRefresh() {
        getTransMethod(0,storableWallet.getPublicKey());
    }
}
