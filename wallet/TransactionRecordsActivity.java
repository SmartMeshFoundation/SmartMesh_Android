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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.adapter.DropTextViewAdapter;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.custom.DropTextView;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.util.netutil.WalletRequestUtils;
import com.lingtuan.firefly.wallet.util.WalletStorage;
import com.lingtuan.firefly.wallet.vo.TransVo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created on 2017/8/23.
 * 交易记录
 */

public class TransactionRecordsActivity extends BaseActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    //选择账户
    private DropTextView walletAccount;
    private DropTextViewAdapter adapter;

    private TextView appTitle;

    private TextView walletAddress;

    private TextView transEth,transFft;//eth  smt 记录
    private View transLine, transLine2;
    private ListView transListView;//记录列表
    private ArrayList<TransVo> transEthVos;
    private ArrayList<TransVo> transFftVos;
    private TransAdapter mAdapter;

    private TextView emptyView;//trans为空
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
        transListView = (ListView) findViewById(R.id.transListView);
    }

    @Override
    protected void initData() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.WALLET_REFRESH_DEL);//刷新页面
        filter.addAction(Constants.CHANGE_LANGUAGE);//修改语言刷新页面
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
        new Handler().postDelayed(new Runnable(){
            public void run() {
                getTransMethod(0,walletAddress.getText().toString());
                swipe_refresh.setRefreshing(true);
            }
        }, 200);

    }

    @Override
    protected void setListener() {
        transEth.setOnClickListener(this);
        transFft.setOnClickListener(this);
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
                getTransMethod(0,walletAddress.getText().toString());
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
                getTransMethod(0,walletAddress.getText().toString());
                swipe_refresh.setRefreshing(true);
                break;
            case R.id.transFft:
                transEth.setSelected(false);
                transFft.setSelected(true);
                transLine.setVisibility(View.INVISIBLE);
                transLine2.setVisibility(View.VISIBLE);
                mAdapter.resetSource(transFftVos);
                getTransMethod(1,walletAddress.getText().toString());
                swipe_refresh.setRefreshing(true);
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    /**
     * 获取转账记录接口
     * @param type 0 eth 1 smt
     * */
    private void getTransMethod(final int type,final String address) {
        checkEmpty(type);
        try {
            WalletRequestUtils.getInstance().getTxlist(TransactionRecordsActivity.this,type,address, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    mHandler.sendEmptyMessage(2);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String jsonString = response.body().string();
                    parseJson(jsonString,type,address);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseJson(String jsonString,final int type,final String address) {
        if (!TextUtils.equals(address,walletAddress.getText().toString())){
            return;
        }
        if (TextUtils.isEmpty(jsonString)){
            mHandler.sendEmptyMessage(2);
            return;
        }
        try {
            JSONObject object = new JSONObject(jsonString);
            int errcod = object.optInt("errcod");
            if (errcod == 0){
                if (type == 0){
                    transEthVos.clear();
                }else{
                    transFftVos.clear();
                }
                JSONArray array = object.optJSONArray("data");
                if (array == null){
                    mHandler.sendEmptyMessage(type);
                    return;
                }
                for (int i = 0 ; i < array.length() ; i++){
                    TransVo transVo = new TransVo().parse(array.optJSONObject(i));
                    if (type == 0){
                        transVo.setType(0);
                        transEthVos.add(transVo);
                    }else{
                        transVo.setType(1);
                        transFftVos.add(transVo);
                    }
                }
                mHandler.sendEmptyMessage(type);
            }else{
                if (errcod == -2){
                    long difftime = object.optJSONObject("data").optLong("difftime");
                    long tempTime =  MySharedPrefs.readLong(TransactionRecordsActivity.this,MySharedPrefs.FILE_APPLICATION,MySharedPrefs.KEY_REQTIME);
                    MySharedPrefs.writeLong(TransactionRecordsActivity.this,MySharedPrefs.FILE_APPLICATION,MySharedPrefs.KEY_REQTIME,difftime + tempTime);
                }
                Message message = Message.obtain();
                message.obj = object.optString("msg");
                message.what = 2;
                mHandler.sendMessage(message);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            mHandler.sendEmptyMessage(type);
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (transEth.isSelected()){
                        checkEmpty(0);
                        swipe_refresh.setRefreshing(false);
                        mAdapter.resetSource(transEthVos);
                    }
                    break;
                case 1:
                    if (transFft.isSelected()){
                        checkEmpty(1);
                        swipe_refresh.setRefreshing(false);
                        mAdapter.resetSource(transFftVos);
                    }
                    break;
                case 2:
                    swipe_refresh.setRefreshing(false);
                    String errorMsg = (String) msg.obj;
                    if (!TextUtils.isEmpty(errorMsg)){
                        showToast(errorMsg);
                    }
                    break;
            }
        }
    };

    private void checkEmpty(int type){
        if (type == 0 && transEthVos.size() <= 0){
            emptyView.setVisibility(View.VISIBLE);
        }else if (type == 1 && transFftVos.size() <= 0){
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
            getTransMethod(0,walletAddress.getText().toString());
        }else{
            mAdapter.resetSource(transFftVos);
            getTransMethod(1,walletAddress.getText().toString());
        }
    }
}
