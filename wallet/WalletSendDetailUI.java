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
import com.lingtuan.firefly.ui.AlertActivity;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;
import com.lingtuan.firefly.wallet.vo.StorableWallet;
import com.lingtuan.firefly.wallet.vo.TokenVo;
import com.lingtuan.firefly.wallet.vo.TransVo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created on 2018/3/16.
 * Transfers or receipts
 */

public class WalletSendDetailUI extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener, AdapterView.OnItemClickListener, LoadMoreListView.RefreshListener {
    
    private TextView walletTransfer;
    private TextView walletReceipt;
    private TextView walletRaiden;
    private StorableWallet storableWallet;
    private TokenVo tokenVo;
    private double smtBalance;
    
    private SwipeRefreshLayout refreshLayout;
    private LoadMoreListView transListView;
    private TransAdapter mAdapter;
    private ArrayList<TransVo> transVos;
    private TextView emptyView;
    
    private Timer timer;
    private TimerTask timerTask;
    
    private boolean isFirstLoad = true;
    
    
    @Override
    protected void setContentView() {
        setContentView(R.layout.wallet_send_detail_layout);
        getPassData();
    }
    
    private void getPassData() {
        tokenVo = (TokenVo) getIntent().getSerializableExtra("tokenVo");
        smtBalance = getIntent().getDoubleExtra("smtBalance", 0);
        storableWallet = (StorableWallet) getIntent().getSerializableExtra("storableWallet");
    }
    
    @Override
    protected void findViewById() {
        walletTransfer = (TextView) findViewById(R.id.walletTransfer);
        walletReceipt = (TextView) findViewById(R.id.walletReceipt);
        walletRaiden = (TextView) findViewById(R.id.walletRaiden);
        emptyView = (TextView) findViewById(R.id.emptyView);
        transListView = (LoadMoreListView) findViewById(R.id.transListView);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
    }
    
    @Override
    protected void setListener() {
        walletTransfer.setOnClickListener(this);
        walletReceipt.setOnClickListener(this);
        walletRaiden.setOnClickListener(this);
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
                    transDetailState();
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
        
        
        transVos = new ArrayList<>();
        mAdapter = new TransAdapter(WalletSendDetailUI.this, transVos, storableWallet.getPublicKey());
        transListView.setAdapter(mAdapter);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (isFirstLoad) {
                    isFirstLoad = false;
                    getTransMethod(storableWallet.getPublicKey());
                    refreshLayout.setRefreshing(true);
                    transDetailState();
                }
            }
        }, 200);
    }
    
    @Override
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
            mAdapter.resetSource(transVos, storableWallet.getPublicKey());
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
        transDetailState();
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
        mAdapter.resetSource(transVos, storableWallet.getPublicKey());
    }
    
    final StringBuilder builder = new StringBuilder();
    
    /**
     * Turn on the timer to call the interface every 12 seconds
     */
    private void transDetailState() {
        if (timer == null) {
            timer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    List<TransVo> mlist = FinalUserDataBase.getInstance().getTransTempList(tokenVo.getContactAddress(), storableWallet.getPublicKey(), true);
                    if (mlist == null || mlist.size() <= 0) {
                        if (timer != null) {
                            timer.cancel();
                            timer = null;
                        }
                        if (timerTask != null) {
                            timerTask.cancel();
                            timerTask = null;
                        }
                        return;
                    }
                    builder.delete(0, builder.length());
                    for (int i = 0; i < mlist.size(); i++) {
                        builder.append(mlist.get(i).getTx()).append(",");
                    }
                    if (builder.length() > 0) {
                        builder.deleteCharAt(builder.length() - 1);
                    }
                    getTranscationBlock(builder.toString());
                }
            };
            timer.schedule(timerTask, 0, 10000);
        }
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
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
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
    
    
}
