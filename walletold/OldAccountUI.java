package com.lingtuan.firefly.walletold;

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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.ui.AlertActivity;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.MyToast;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.network.NetRequestUtils;
import com.lingtuan.firefly.wallet.WalletCopyActivity;
import com.lingtuan.firefly.wallet.util.WalletStorage;
import com.lingtuan.firefly.wallet.vo.StorableWallet;
import com.lingtuan.firefly.walletold.contract.OldAccountContract;
import com.lingtuan.firefly.walletold.presenter.OldAccountPresenterImpl;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created  on 2017/8/23.
 * account
 */

public class OldAccountUI extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener,OldAccountContract.View{

    @BindView(R.id.walletImg)
    ImageView walletImg;//The wallet picture
    @BindView(R.id.walletName)
    TextView walletName;//Name of the wallet
    @BindView(R.id.walletBackup)
    TextView walletBackup;//backup of the wallet
    @BindView(R.id.walletAddress)
    TextView walletAddress;//The wallet address
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipe_refresh;
    @BindView(R.id.ethTokenBalance)
    TextView ethTokenBalance;
    @BindView(R.id.smtTokenBalance)
    TextView smtTokenBalance;
    @BindView(R.id.meshTokenBalance)
    TextView meshTokenBalance;
    @BindView(R.id.smtTokenBody)
    LinearLayout smtTokenBody;

    private OldAccountContract.Presenter mPresenter;

    private StorableWallet storableWallet;

    private boolean hasLoadData;

    @Override
    protected void setContentView() {
        setContentView(R.layout.old_wallet_account_fragment);
    }

    @Override
    protected void findViewById() {

    }
    @Override
    protected void setListener(){
        swipe_refresh.setOnRefreshListener(this);
    }

    @Override
    protected void initData() {
        new OldAccountPresenterImpl(this);
        Utils.setStatusBar(OldAccountUI.this,1);
        swipe_refresh.setColorSchemeResources(R.color.black);
        initWalletInfo();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.WALLET_REFRESH_BACKUP);//Refresh the page
        filter.addAction(Constants.WALLET_REFRESH_DEL);//Refresh the page
        registerReceiver(mBroadcastReceiver, filter);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null){
                if (Constants.WALLET_REFRESH_BACKUP.equals(intent.getAction()) ) {
                    initWalletInfo();
                }else if (Constants.WALLET_REFRESH_DEL.equals(intent.getAction())){
                    finish();
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }

    @OnClick({R.id.walletNameBody,R.id.walletAddress,R.id.ethTokenBody,R.id.smtTokenBody,R.id.meshTokenBody})
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.walletNameBody://Backup the purse
                if (storableWallet == null || !hasLoadData){
                    return;
                }
                Intent copyIntent = new Intent(OldAccountUI.this,WalletCopyActivity.class);
                copyIntent.putExtra(Constants.WALLET_INFO, storableWallet);
                copyIntent.putExtra(Constants.WALLET_IMAGE, storableWallet.getWalletImageId());
                copyIntent.putExtra(Constants.WALLET_TYPE, 1);
                startActivity(copyIntent);
                Utils.openNewActivityAnim(OldAccountUI.this,false);
                break;
            case R.id.walletAddress://Qr code
                if (storableWallet == null || !hasLoadData){
                    return;
                }
                if (!storableWallet.isBackup()){
                    Intent intent = new Intent(OldAccountUI.this, AlertActivity.class);
                    intent.putExtra("type", 5);
                    intent.putExtra("strablewallet", storableWallet);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return;
                }
                Intent qrCodeIntent = new Intent(OldAccountUI.this,OldQuickMarkShowUI.class);
                qrCodeIntent.putExtra("type", 0);
                qrCodeIntent.putExtra("address", storableWallet.getPublicKey());
                startActivity(qrCodeIntent);
                Utils.openNewActivityAnim(OldAccountUI.this,false);
                break;
            case R.id.ethTokenBody://Copy the address
                if (storableWallet == null || !hasLoadData){
                    return;
                }
                Intent ethIntent = new Intent(OldAccountUI.this,OldWalletSendDetailUI.class);
                ethIntent.putExtra("sendtype", 0);
                ethIntent.putExtra("storableWallet", storableWallet);
                startActivity(ethIntent);
                Utils.openNewActivityAnim(OldAccountUI.this,false);
                break;
            case R.id.smtTokenBody://Copy the address
                if (storableWallet == null || !hasLoadData){
                    return;
                }
                Intent smtIntent = new Intent(OldAccountUI.this,OldWalletSendDetailUI.class);
                smtIntent.putExtra("sendtype", 1);
                smtIntent.putExtra("storableWallet", storableWallet);
                startActivity(smtIntent);
                Utils.openNewActivityAnim(OldAccountUI.this,false);
                break;
            case R.id.meshTokenBody://Copy the address
                Intent meshIntent = new Intent(OldAccountUI.this,OldWalletSendDetailUI.class);
                meshIntent.putExtra("sendtype", 2);
                meshIntent.putExtra("storableWallet", storableWallet);
                startActivity(meshIntent);
                Utils.openNewActivityAnim(OldAccountUI.this,false);
                break;
        }
    }


    /**
     * Load or refresh the wallet information
     * */
    private void initWalletInfo(){
        storableWallet = mPresenter.getStorableWallet();
        if (storableWallet != null){
            walletImg.setImageResource(Utils.getWalletImageId(OldAccountUI.this,storableWallet.getWalletImageId()));
            walletName.setText(storableWallet.getWalletName());

            if (storableWallet.isBackup()){
                walletBackup.setVisibility(View.GONE);
            }else{
                walletBackup.setVisibility(View.VISIBLE);
            }

            String address = storableWallet.getPublicKey();
            if(!address.startsWith("0x")){
                address = "0x"+address;
            }
            walletAddress.setText(address);
        }
        new Handler().postDelayed(new Runnable(){
            public void run() {
                swipe_refresh.setRefreshing(true);
                mPresenter.getBalance(OldAccountUI.this,walletAddress.getText().toString(),true);
            }
        }, 10);
    }

    @Override
    public void onRefresh() {
        mPresenter.getBalance(this,walletAddress.getText().toString(),true);
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    showToast(getString(R.string.error_get_balance));
                    if (swipe_refresh != null){
                        swipe_refresh.setRefreshing(false);
                    }
                    break;
                case 1:
                    if (swipe_refresh != null){
                        swipe_refresh.setRefreshing(false);
                    }
                    mPresenter.parseJson((String) msg.obj);
                    break;
            }
        }
    };

    @Override
    public void setPresenter(OldAccountContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public void onFailure(boolean isShowToast) {
        if (isShowToast){
            mHandler.sendEmptyMessage(0);
        }
    }

    @Override
    public void onResponse(String string) {
        Message message = Message.obtain();
        message.what = 1;
        message.obj = string;
        mHandler.sendMessage(message);
    }

    @Override
    public void resetData(double ethBalance, double smtBalance, double meshBalance, String smtMapping) {
        if (ethTokenBalance != null){
            BigDecimal ethDecimal = new BigDecimal(ethBalance).setScale(10,BigDecimal.ROUND_DOWN);
            ethTokenBalance.setText(ethDecimal.toPlainString());
        }
        if (smtTokenBalance != null){
            BigDecimal fftDecimal = new BigDecimal(smtBalance).setScale(5,BigDecimal.ROUND_DOWN);
            smtTokenBalance.setText(fftDecimal.toPlainString());
        }
        if (meshTokenBalance != null){
            BigDecimal meshDecimal = new BigDecimal(meshBalance).setScale(5,BigDecimal.ROUND_DOWN);
            meshTokenBalance.setText(meshDecimal.toPlainString());
        }
        storableWallet.setEthBalance(ethBalance);
        storableWallet.setFftBalance(smtBalance);
        storableWallet.setMeshBalance(meshBalance);

        if (smtTokenBody != null){
            if (TextUtils.equals(smtMapping,"2")){
                smtTokenBody.setVisibility(View.GONE);
            }else if (TextUtils.equals(smtMapping,"1")){
                smtTokenBody.setVisibility(View.VISIBLE);
                smtTokenBody.setEnabled(false);
            }else{
                smtTokenBody.setVisibility(View.VISIBLE);
                smtTokenBody.setEnabled(true);
            }
        }

        hasLoadData = true;
    }

    @Override
    public void resetDataError(String message) {
        showToast(message);
    }
}
