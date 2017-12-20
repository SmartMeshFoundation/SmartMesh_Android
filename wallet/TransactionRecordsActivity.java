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
import android.widget.ListView;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.adapter.DropTextViewAdapter;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.custom.DropTextView;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.util.netutil.NetRequestUtils;
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
 * Transaction records
 */

public class TransactionRecordsActivity extends BaseActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener, AdapterView.OnItemClickListener {

    //Select the account
    private DropTextView walletAccount;
    private DropTextViewAdapter adapter;

    private TextView appTitle;

    private TextView walletAddress;

    private TextView transEth,transFft;//eth  smt record
    private View transLine, transLine2;
    private ListView transListView;//Record list
    private ArrayList<TransVo> transEthVos;
    private ArrayList<TransVo> transFftVos;
    private TransAdapter mAdapter;

    private TextView emptyView;//Trans is empty
    private SwipeRefreshLayout swipe_refresh;

    private String mAddress;
    private String mWalletName;

    private int recordType;//smt or eth


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
        transListView.setOnItemClickListener(this);
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
                emptyView.setVisibility(View.GONE);
                getTransMethod(0,walletAddress.getText().toString());
                swipe_refresh.setRefreshing(true);
                break;
            case R.id.transFft:
                transEth.setSelected(false);
                transFft.setSelected(true);
                transLine.setVisibility(View.INVISIBLE);
                transLine2.setVisibility(View.VISIBLE);
                mAdapter.resetSource(transFftVos);
                emptyView.setVisibility(View.GONE);
                getTransMethod(1,walletAddress.getText().toString());
                swipe_refresh.setRefreshing(true);
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    /**
     * To obtain transfer record interface
     * @param type 0 eth 1 smt
     * */
    private void getTransMethod(final int type,final String address) {
        recordType = type;
        try {
            NetRequestUtils.getInstance().getTxlist(TransactionRecordsActivity.this,type,address, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Message message = Message.obtain();
                    message.what = 2;
                    message.arg1 = type;
                    mHandler.sendMessage(message);
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
            Message message = Message.obtain();
            message.what = 2;
            message.arg1 = type;
            mHandler.sendMessage(message);
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
                    checkEmpty(msg.arg1);
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(TransactionRecordsActivity.this,TransactionDetailActivity.class);
        if (recordType == 0 && transEthVos.size() > 0){//eth record
            intent.putExtra("transVo",transEthVos.get(position));
        }else if (recordType == 1 && transFftVos.size() > 0){
            intent.putExtra("transVo",transFftVos.get(position));
        }
        intent.putExtra("fromAddress",walletAddress.getText().toString());
        intent.putExtra("recordType",recordType);
        startActivity(intent);
    }
}
