package com.lingtuan.firefly.wallet;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.quickmark.QuickMarkShowUI;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.wallet.vo.StorableWallet;
import com.lingtuan.firefly.wallet.vo.TokenVo;
import com.lingtuan.firefly.wallet.vo.TransVo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2018/3/16.
 * Transfers or receipts
 */

public class WalletSendDetailUI extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener, AdapterView.OnItemClickListener {

    private TextView walletTransfer;
    private TextView walletReceipt;
    private StorableWallet storableWallet;
    private TokenVo tokenVo;
    private double smtBalance;

    private SwipeRefreshLayout refreshLayout;
    private ListView transListView;
    private TransAdapter mAdapter;
    private ArrayList<TransVo> transVos;
    private TextView emptyView;

    @Override
    protected void setContentView() {
        setContentView(R.layout.wallet_send_detail_layout);
        getPassData();
    }

    private void getPassData() {
        tokenVo = (TokenVo) getIntent().getSerializableExtra("tokenVo");
        smtBalance =getIntent().getDoubleExtra("smtBalance",0);
        storableWallet = (StorableWallet) getIntent().getSerializableExtra("storableWallet");
    }

    @Override
    protected void findViewById() {
        walletTransfer = (TextView) findViewById(R.id.walletTransfer);
        walletReceipt = (TextView) findViewById(R.id.walletReceipt);
        emptyView = (TextView) findViewById(R.id.emptyView);
        transListView = (ListView) findViewById(R.id.transListView);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
    }

    @Override
    protected void setListener() {
        walletTransfer.setOnClickListener(this);
        walletReceipt.setOnClickListener(this);
        refreshLayout.setOnRefreshListener(this);
        transListView.setOnItemClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Handler().postDelayed(new Runnable(){
            public void run() {
                getTransMethod(storableWallet.getPublicKey());
                refreshLayout.setRefreshing(true);
            }
        }, 200);
    }

    @Override
    protected void initData() {
        if (tokenVo != null){
            setTitle(tokenVo.getTokenSymbol());
        }
        transVos = new ArrayList<>();
        mAdapter = new TransAdapter(WalletSendDetailUI.this,transVos,storableWallet.getPublicKey());
        transListView.setAdapter(mAdapter);
        new Handler().postDelayed(new Runnable(){
            public void run() {
                getTransMethod(storableWallet.getPublicKey());
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
                ethIntent.putExtra("tokenVo", tokenVo);
                ethIntent.putExtra("smtBalance", smtBalance);
                startActivity(ethIntent);
                Utils.openNewActivityAnim(WalletSendDetailUI.this,false);
                break;
            case R.id.walletReceipt:
                if (storableWallet == null){
                    return;
                }
                Intent qrEthIntent = new Intent(WalletSendDetailUI.this,QuickMarkShowUI.class);
                qrEthIntent.putExtra("tokenVo", tokenVo);
                qrEthIntent.putExtra("address", storableWallet.getPublicKey());
                startActivity(qrEthIntent);
                Utils.openNewActivityAnim(WalletSendDetailUI.this,false);
                break;
        }
    }

    /**
     * To obtain transfer record interface
     * */
    private void getTransMethod(final String address) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<TransVo> mlist =  FinalUserDataBase.getInstance().getTransTempList(tokenVo.getContactAddress(),address);
                transVos.clear();
                transVos.addAll(mlist);
                mHandler.sendEmptyMessage(0);
            }
        }).start();
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        public void handleMessage(Message msg) {
            refreshLayout.setRefreshing(false);
            mAdapter.resetSource(transVos,storableWallet.getPublicKey());
            checkEmpty();
        }
    };

    private void checkEmpty(){
        if (transVos.size() <= 0){
            emptyView.setVisibility(View.VISIBLE);
        }else{
            emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRefresh() {
        getTransMethod(storableWallet.getPublicKey());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(WalletSendDetailUI.this,TransactionDetailActivity.class);
        intent.putExtra("transVo",transVos.get(position));
        startActivity(intent);
        Utils.openNewActivityAnim(WalletSendDetailUI.this, false);
    }
}
