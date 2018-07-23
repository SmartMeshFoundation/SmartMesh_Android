package com.lingtuan.firefly.wallet;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.custom.LoadMoreListView;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.quickmark.QuickMarkShowUI;
import com.lingtuan.firefly.raiden.RaidenChannelList;
import com.lingtuan.firefly.raiden.RaidenNet;
import com.lingtuan.firefly.ui.AlertActivity;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;
import com.lingtuan.firefly.wallet.contract.WalletSendDetailContract;
import com.lingtuan.firefly.wallet.presenter.WalletSendDetailPresenterImpl;
import com.lingtuan.firefly.wallet.vo.StorableWallet;
import com.lingtuan.firefly.wallet.vo.TokenVo;
import com.lingtuan.firefly.wallet.vo.TransVo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created on 2018/3/16.
 * Transfers or receipts
 */

public class WalletSendDetailUI extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener, AdapterView.OnItemClickListener, LoadMoreListView.RefreshListener,WalletSendDetailContract.View{

    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout refreshLayout;
    @BindView(R.id.transListView)
    LoadMoreListView transListView;
    @BindView(R.id.emptyView)
    TextView emptyView;

    private StorableWallet storableWallet;
    private TokenVo tokenVo;
    private double smtBalance;
    private TransAdapter mAdapter;
    private ArrayList<TransVo> transVos;
    private boolean isFirstLoad = true;

    private WalletSendDetailContract.Presenter mPresenter;

    @Override
    protected void setContentView() {
        setContentView(R.layout.wallet_send_detail_layout);
        getPassData();
    }
    
    private void getPassData() {
        tokenVo = (TokenVo) getIntent().getSerializableExtra("tokenVo");
        smtBalance = getIntent().getDoubleExtra("smtBalance", 0);
        storableWallet = (StorableWallet) getIntent().getSerializableExtra("storableWallet");
        new WalletSendDetailPresenterImpl(this);
    }
    
    @Override
    protected void findViewById() {

    }
    
    @Override
    protected void setListener() {
        refreshLayout.setOnRefreshListener(this);
        transListView.setOnItemClickListener(this);
        transListView.setOnRefreshListener(this);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (!isFirstLoad) {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    getTransMethod(storableWallet.getPublicKey());
                    refreshLayout.setRefreshing(true);
                    mPresenter.loadData(tokenVo.getContactAddress(), storableWallet.getPublicKey());
                }
            }, 200);
        }
    }
    
    @Override
    protected void initData() {
        if (tokenVo != null) {
            setTitle(tokenVo.getTokenSymbol());
        }
        
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.WALLET_REFRESH_BACKUP);//Refresh the page
        registerReceiver(mBroadcastReceiver, filter);
        
        //debug
//        if (!tokenVo.getTokenSymbol().equals("SMT")) {
//            walletRaiden.setVisibility(View.VISIBLE);
//        }
        transVos = new ArrayList<>();
        mAdapter = new TransAdapter(WalletSendDetailUI.this, transVos);
        transListView.setAdapter(mAdapter);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (isFirstLoad) {
                    isFirstLoad = false;
                    getTransMethod(storableWallet.getPublicKey());
                    refreshLayout.setRefreshing(true);
                    mPresenter.loadData(tokenVo.getContactAddress(), storableWallet.getPublicKey());
                }
            }
        }, 200);
        RaidenNet.getInatance().registerToken();
    }
    
    @OnClick({R.id.walletTransfer,R.id.walletReceipt,R.id.walletRaiden})
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.walletTransfer:
                Intent ethIntent = new Intent(WalletSendDetailUI.this, WalletSendActivity.class);
                ethIntent.putExtra("tokenVo", tokenVo);
                ethIntent.putExtra("smtBalance", smtBalance);
                startActivity(ethIntent);
                Utils.openNewActivityAnim(WalletSendDetailUI.this, false);
                break;
            case R.id.walletReceipt:
                if (storableWallet == null) {
                    return;
                }
                if (!storableWallet.isBackup()) {
                    Intent intent = new Intent(WalletSendDetailUI.this, AlertActivity.class);
                    intent.putExtra("type", 5);
                    intent.putExtra("strablewallet", storableWallet);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return;
                }
                Intent qrEthIntent = new Intent(WalletSendDetailUI.this, QuickMarkShowUI.class);
                qrEthIntent.putExtra("tokenVo", tokenVo);
                qrEthIntent.putExtra("address", storableWallet.getPublicKey());
                startActivity(qrEthIntent);
                Utils.openNewActivityAnim(WalletSendDetailUI.this, false);
                break;
            case R.id.walletRaiden:
                Intent raidenIntent = new Intent(this, RaidenChannelList.class);
                raidenIntent.putExtra("storableWallet", storableWallet);
                raidenIntent.putExtra("tokenVo", tokenVo);
                startActivity(raidenIntent);
                Utils.openNewActivityAnim(WalletSendDetailUI.this, false);
                break;
        }
    }
    
    /**
     * To obtain transfer record interface
     */
    private void getTransMethod(final String address) {
        List<TransVo> mlist = FinalUserDataBase.getInstance().getTransTempList(tokenVo.getContactAddress(), address, false);
        transVos.clear();
        transVos.addAll(mlist);
        Message message = Message.obtain();
        message.arg1 = mlist.size();
        message.what = 0;
        mHandler.sendMessage(message);
    }
    
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            int size = msg.arg1;
            refreshLayout.setRefreshing(false);
            mAdapter.resetSource(transVos);
            if (size >= 10) {
                transListView.resetFooterState(true);
            } else {
                transListView.resetFooterState(false);
            }
            checkEmpty();
        }
    };
    
    private void checkEmpty() {
        if (transVos.size() <= 0) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
        }
    }
    
    @Override
    public void onRefresh() {
        getTransMethod(storableWallet.getPublicKey());
        mPresenter.loadData(tokenVo.getContactAddress(), storableWallet.getPublicKey());
    }
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(WalletSendDetailUI.this, TransactionDetailActivity.class);
        intent.putExtra("transVo", transVos.get(position));
        startActivity(intent);
        Utils.openNewActivityAnim(WalletSendDetailUI.this, false);
    }
    
    @Override
    public void loadMore() {
        int limit;
        List<TransVo> tempList = FinalUserDataBase.getInstance().getTransTempList(tokenVo.getContactAddress(), storableWallet.getPublicKey(), true);
        if (tempList != null && tempList.size() > 0) {
            limit = mAdapter.getCount() - tempList.size();
        } else {
            limit = mAdapter.getCount();
        }
        ArrayList<TransVo> mlist = FinalUserDataBase.getInstance().getTransListLimit(tokenVo.getContactAddress(), storableWallet.getPublicKey(), limit, 10);
        if (mlist.size() >= 10) {
            transListView.resetFooterState(true);
        } else {
            transListView.resetFooterState(false);
        }
        transVos.addAll(mlist);
        mAdapter.resetSource(transVos);
    }

    /**
     * Get the block number of the transaction hash
     */
    private void getTranscationBlock(String txList) {
        NetRequestImpl.getInstance().getTxBlockNumber(txList, new RequestListener() {
            @Override
            public void start() {
                
            }
            
            @Override
            public void success(JSONObject response) {
                JSONArray array = response.optJSONArray("data");
                int lastBlockNumber = response.optInt("blockNumber", 0);
                if (array != null) {
                    FinalUserDataBase.getInstance().beginTransaction();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = array.optJSONObject(i);
                        int transBlockNumber = object.optInt("txBlockNumber", 0);
                        int state = object.optInt("state", 0);
                        String tx = object.optString("tx");
                        for (int j = 0; j < transVos.size(); j++) {
                            if (TextUtils.equals(tx, transVos.get(j).getTx())) {
                                transVos.get(j).setTxBlockNumber(transBlockNumber);
                                transVos.get(j).setBlockNumber(lastBlockNumber);
                                transVos.get(j).setState(state);
                                FinalUserDataBase.getInstance().updateTransTemp(transVos.get(j));
                            }
                        }
                    }
                    FinalUserDataBase.getInstance().endTransactionSuccessful();
                    mAdapter.notifyDataSetChanged();
                }
            }
            
            @Override
            public void error(int errorCode, String errorMsg) {
                showToast(errorMsg);
            }
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
        mPresenter.onDestroy();
    }
    
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if (Constants.WALLET_REFRESH_BACKUP.equals(intent.getAction())) {
                    if (storableWallet != null) {
                        storableWallet.setBackup(true);
                    }
                }
            }
        }
    };

    @Override
    public void setPresenter(WalletSendDetailContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public void success(ArrayList<TransVo> transVos) {
        this.transVos = transVos;
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void error(int errorCode, String errorMsg) {
        showToast(errorMsg);
    }
}
