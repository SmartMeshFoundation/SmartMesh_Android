package com.lingtuan.firefly.wallet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.adapter.DropTextViewAdapter;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.custom.DropTextView;
import com.lingtuan.firefly.custom.LoadMoreListView;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;
import com.lingtuan.firefly.wallet.util.WalletStorage;
import com.lingtuan.firefly.wallet.vo.TransVo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created on 2017/8/23.
 * Transaction records
 */

public class TransactionRecordsActivity extends BaseActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener, LoadMoreListView.RefreshListener {

    //Select the account
    private DropTextView walletAccount;
    private DropTextViewAdapter adapter;

    private TextView appTitle;

    private TextView walletAddress;

    private TextView transEth,transFft;//eth  smt record
    private View transLine, transLine2;
    private LoadMoreListView transListView;//Record list
    private ArrayList<TransVo> transEthVos;
    private ArrayList<TransVo> transFftVos;
    private TransAdapter mAdapter;

    private boolean isLoadingData = false;
    private int currentPage = 1 ;
    private int oldPage=1;

    private TextView emptyView;//Trans is empty
    private SwipeRefreshLayout swipe_refresh;

    private String mAddress;
    private String mWalletName;


    @Override
    protected void setContentView() {
        setContentView(R.layout.wallet_trans_record_fragment);
        getPassData();
    }

    private void getPassData() {
        mAddress = getIntent().getStringExtra("address");
        mWalletName = getIntent().getStringExtra("name");
    }

    @Override
    protected void findViewById() {
        walletAccount = (DropTextView) findViewById(R.id.walletAccount);
        walletAddress = (TextView) findViewById(R.id.walletAddress);
        appTitle = (TextView) findViewById(R.id.app_title);

        swipe_refresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        emptyView = (TextView) findViewById(R.id.emptyView);
        transEth = (TextView) findViewById(R.id.transEth);
        transFft = (TextView) findViewById(R.id.transFft);
        transLine = findViewById(R.id.transLine);
        transLine2 = findViewById(R.id.transLine2);
        transListView = (LoadMoreListView) findViewById(R.id.transListView);
    }

    @Override
    protected void initData() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.WALLET_REFRESH_DEL);//Refresh the page
        filter.addAction(Constants.CHANGE_LANGUAGE);//Modify the language refresh the page
        registerReceiver(mBroadcastReceiver, filter);

        appTitle.setText(getString(R.string.transcation_records));
        transEth.setSelected(true);
        transLine2.setVisibility(View.INVISIBLE);
        initWalletInfo(0);
        swipe_refresh.setColorSchemeResources(R.color.black);
        swipe_refresh.setOnRefreshListener(this);
        adapter = new DropTextViewAdapter(TransactionRecordsActivity.this,WalletStorage.getInstance(getApplicationContext()).get());
        walletAccount.setAdapter(adapter);

        transEthVos = new ArrayList<>();
        transFftVos = new ArrayList<>();
        mAdapter = new TransAdapter(TransactionRecordsActivity.this,transEthVos);
        transListView.setAdapter(mAdapter);

        if (!TextUtils.isEmpty(mAddress)){
            if (!mAddress.startsWith("0x")){
                mAddress = "0x" + mAddress;
            }
            walletAccount.setText(mWalletName);
            walletAddress.setText(mAddress);
        }

        emptyView.setVisibility(View.GONE);

        new Handler().postDelayed(new Runnable(){
            public void run() {
                swipe_refresh.setRefreshing(true);
                getTransMethod(1,1,walletAddress.getText().toString());
            }
        }, 200);

    }

    @Override
    protected void setListener() {
        transEth.setOnClickListener(this);
        transFft.setOnClickListener(this);
        transListView.setOnRefreshListener(this);
        walletAccount.setOnItemListener(new DropTextView.OnItemListener() {
            @Override
            public void onItemListener(int position) {
                initWalletInfo(position);
                transEth.setSelected(true);
                transFft.setSelected(false);
                transLine.setVisibility(View.VISIBLE);
                transLine2.setVisibility(View.INVISIBLE);
                transEthVos.clear();
                transFftVos.clear();
                mAdapter.resetSource(transEthVos);
                emptyView.setVisibility(View.GONE);
                transListView.resetFooterState(false);
                getTransMethod(1,1,walletAddress.getText().toString());
                swipe_refresh.setRefreshing(true);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.transEth:
                transEth.setSelected(true);
                transFft.setSelected(false);
                transLine.setVisibility(View.VISIBLE);
                transLine2.setVisibility(View.INVISIBLE);
                mAdapter.resetSource(transEthVos);
                emptyView.setVisibility(View.GONE);
                transListView.resetFooterState(false);
                getTransMethod(1,1,walletAddress.getText().toString());
                swipe_refresh.setRefreshing(true);
                break;
            case R.id.transFft:
                transEth.setSelected(false);
                transFft.setSelected(true);
                transLine.setVisibility(View.INVISIBLE);
                transLine2.setVisibility(View.VISIBLE);
                mAdapter.resetSource(transFftVos);
                emptyView.setVisibility(View.GONE);
                transListView.resetFooterState(false);
                getTransMethod(1,0,walletAddress.getText().toString());
                swipe_refresh.setRefreshing(true);
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    /**
     * To obtain transfer record interface
     * @param type 0 smt 1 eth
     * */
    private void getTransMethod(final int page,final int type,final String address) {
        if(isLoadingData){
            return;
        }
        isLoadingData=true;
        oldPage = page;
        NetRequestImpl.getInstance().getTxlist(page, type, address, new RequestListener() {
            @Override
            public void start() {

            }

            @Override
            public void success(JSONObject response) {
                currentPage = oldPage;
                parseJson(response,type,address);
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                checkEmpty(type);
                swipe_refresh.setRefreshing(false);
                if (!TextUtils.isEmpty(errorMsg)){
                    showToast(errorMsg);
                }
            }
        });
    }

    private void parseJson(JSONObject object,final int type,final String address) {

        swipe_refresh.setRefreshing(false);

        if (!TextUtils.equals(address,walletAddress.getText().toString())){
            return;
        }

        if (object == null){
            checkEmpty(type);
            return;
        }
        if (currentPage == 1){
            if (type == 0){
                transFftVos.clear();
            }else{
                transEthVos.clear();
            }
        }

        JSONArray array = object.optJSONArray("data");
        if (array == null){
            return;
        }
        isLoadingData=false;
        if (array!=null&&array.length()>=10) {
            transListView.resetFooterState(true);
        } else {
            transListView.resetFooterState(false);
        }

        for (int i = 0 ; i < array.length() ; i++){
            TransVo transVo = new TransVo().parse(array.optJSONObject(i));
            if (type == 0){
                transVo.setType(0);
                transFftVos.add(transVo);
            }else{
                transVo.setType(1);
                transEthVos.add(transVo);
            }
        }
        if (type == 0 && transFft.isSelected()){
            checkEmpty(0);
            mAdapter.resetSource(transFftVos);
        }else if (type == 1 && transEth.isSelected()){
            checkEmpty(1);
            mAdapter.resetSource(transEthVos);
        }
    }

    private void checkEmpty(int type){
        if (type == 0 && transFftVos.size() <= 0){
            emptyView.setVisibility(View.VISIBLE);
        }else if (type == 1 && transEthVos.size() <= 0){
            emptyView.setVisibility(View.VISIBLE);
        }else{
            emptyView.setVisibility(View.GONE);
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && (Constants.WALLET_REFRESH_DEL.equals(intent.getAction()))) {
                initWalletInfo(0);
            }else if (intent != null && (Constants.CHANGE_LANGUAGE.equals(intent.getAction()))) {
                appTitle.setText(getString(R.string.app_name));
                Utils.updateViewLanguage(findViewById(R.id.trans_recode_bg));
            }
        }
    };

    private void initWalletInfo(int position) {
        if (WalletStorage.getInstance(getApplicationContext()).get().size() <= 0){
            return;
        }
        walletAccount.setText(WalletStorage.getInstance(getApplicationContext()).get().get(position).getWalletName());
        String address = WalletStorage.getInstance(getApplicationContext()).get().get(position).getPublicKey();
        if (!TextUtils.isEmpty(address) && !address.startsWith("0x")){
            address = "0x" + address;
        }
        walletAddress.setText(address);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }


    @Override
    public void onRefresh() {
        if (transEth.isSelected()){
            mAdapter.resetSource(transEthVos);
            getTransMethod(1,1,walletAddress.getText().toString());
        }else{
            mAdapter.resetSource(transFftVos);
            getTransMethod(1,0,walletAddress.getText().toString());
        }
    }

    @Override
    public void loadMore() {
        getTransMethod(currentPage + 1,0,walletAddress.getText().toString());
    }
}
