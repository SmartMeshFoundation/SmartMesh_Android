package com.lingtuan.firefly.walletold;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.custom.LoadMoreListView;
import com.lingtuan.firefly.ui.AlertActivity;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.util.netutil.NetRequestUtils;
import com.lingtuan.firefly.wallet.vo.StorableWallet;
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
 * Created on 2018/3/16.
 * Transfers or receipts
 */

public class OldWalletSendDetailUI extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    private TextView walletTransfer;
    private TextView walletReceipt;
    private StorableWallet storableWallet;
    private int type;//0 eth  1 smt  2 mesh

    private SwipeRefreshLayout refreshLayout;
    private LoadMoreListView transListView;
    private OldTransAdapter mAdapter;
    private ArrayList<TransVo> transVos;
    private TextView emptyView;


    private boolean isFirstLoad = true;


    @Override
    protected void setContentView() {
        setContentView(R.layout.wallet_send_detail_layout);
        getPassData();
    }

    private void getPassData() {
        type =getIntent().getIntExtra("sendtype",0);
        storableWallet = (StorableWallet) getIntent().getSerializableExtra("storableWallet");
    }

    @Override
    protected void findViewById() {
        walletTransfer = (TextView) findViewById(R.id.walletTransfer);
        walletReceipt = (TextView) findViewById(R.id.walletReceipt);
        emptyView = (TextView) findViewById(R.id.emptyView);
        transListView = (LoadMoreListView) findViewById(R.id.transListView);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
    }

    @Override
    protected void setListener() {
        walletTransfer.setOnClickListener(this);
        walletReceipt.setOnClickListener(this);
        refreshLayout.setOnRefreshListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isFirstLoad){
            new Handler().postDelayed(new Runnable(){
                public void run() {
                    refreshLayout.setRefreshing(true);
                    getTransMethod();
                }
            }, 200);
        }
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
        mAdapter = new OldTransAdapter(OldWalletSendDetailUI.this,transVos,storableWallet.getPublicKey());
        transListView.setAdapter(mAdapter);
        new Handler().postDelayed(new Runnable(){
            public void run() {
                if (isFirstLoad){
                    isFirstLoad = false;
                    getTransMethod();
                    refreshLayout.setRefreshing(true);
                }
            }
        }, 200);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.walletTransfer:
                Intent ethIntent = new Intent(OldWalletSendDetailUI.this,OldWalletSendActivity.class);
                ethIntent.putExtra("sendtype", type);
                startActivity(ethIntent);
                Utils.openNewActivityAnim(OldWalletSendDetailUI.this,false);
                break;
            case R.id.walletReceipt:
                if (storableWallet == null){
                    return;
                }
                if (!storableWallet.isBackup()){
                    Intent intent = new Intent(OldWalletSendDetailUI.this, AlertActivity.class);
                    intent.putExtra("type", 5);
                    intent.putExtra("strablewallet", storableWallet);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return;
                }
                Intent qrEthIntent = new Intent(OldWalletSendDetailUI.this,OldQuickMarkShowUI.class);
                qrEthIntent.putExtra("type", type);
                qrEthIntent.putExtra("address", storableWallet.getPublicKey());
                startActivity(qrEthIntent);
                Utils.openNewActivityAnim(OldWalletSendDetailUI.this,false);
                break;
        }
    }

    /**
     * To obtain transfer record interface
     * */
    private void getTransMethod() {
        try {
            NetRequestUtils.getInstance().getTxlist(OldWalletSendDetailUI.this,type,storableWallet.getPublicKey(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    mHandler.sendEmptyMessage(0);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    parseJson(response.body().string());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    refreshLayout.setRefreshing(false);
                    break;
                case 1:
                    refreshLayout.setRefreshing(false);
                    mAdapter.resetSource(transVos,storableWallet.getPublicKey());
                    checkEmpty();
                    break;
                case 2:
                    refreshLayout.setRefreshing(false);
                    String errorMsg = (String) msg.obj;
                    if (!TextUtils.isEmpty(errorMsg)){
                       showToast(errorMsg);
                    }
                    break;
            }
        }
    };

    private void parseJson(String jsonString) {
        if (TextUtils.isEmpty(jsonString)){
            mHandler.sendEmptyMessage(0);
            return;
        }
        try {
            JSONObject object = new JSONObject(jsonString);
            int errcod = object.optInt("errcod");
            if (errcod == 0){
                transVos.clear();
                JSONArray array = object.optJSONArray("data");
                if (array == null){
                    mHandler.sendEmptyMessage(0);
                    return;
                }
                for (int i = 0 ; i < array.length() ; i++){
                    TransVo transVo = new TransVo().parse(array.optJSONObject(i));
                    transVos.add(transVo);
                }
                mHandler.sendEmptyMessage(1);
            }else{
                if (errcod == -2){
                    long difftime = object.optJSONObject("data").optLong("difftime");
                    long tempTime =  MySharedPrefs.readLong(OldWalletSendDetailUI.this,MySharedPrefs.FILE_APPLICATION, MySharedPrefs.KEY_REQTIME);
                    MySharedPrefs.writeLong(OldWalletSendDetailUI.this,MySharedPrefs.FILE_APPLICATION,MySharedPrefs.KEY_REQTIME,difftime + tempTime);
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

    private void checkEmpty(){
        if (transVos.size() <= 0){
            emptyView.setVisibility(View.VISIBLE);
        }else{
            emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRefresh() {
        getTransMethod();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
