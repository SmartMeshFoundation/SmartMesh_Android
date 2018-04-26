package com.lingtuan.firefly.walletold;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
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
import com.lingtuan.firefly.util.netutil.NetRequestUtils;
import com.lingtuan.firefly.wallet.WalletCopyActivity;
import com.lingtuan.firefly.wallet.util.WalletStorage;
import com.lingtuan.firefly.wallet.vo.StorableWallet;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created  on 2017/8/23.
 * account
 */

public class OldAccountUI extends BaseActivity implements View.OnClickListener,  SwipeRefreshLayout.OnRefreshListener{

    private ImageView walletImg;//The wallet picture
    private TextView walletName;//Name of the wallet
    private TextView walletBackup;//backup of the wallet
    private TextView walletAddress;//The wallet address
    private LinearLayout walletNameBody;
    private SwipeRefreshLayout swipe_refresh;
    private StorableWallet storableWallet;
    private int index = -1;//Which one is selected

    private TextView etnTokenBalance,smtTokenBalance,meshTokenBalance;

    private CardView ethTokenBody,smtTokenBody,meshTokenBody;



    @Override
    protected void setContentView() {
        setContentView(R.layout.old_wallet_account_fragment);
    }

    @Override
    protected void findViewById() {
        swipe_refresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        walletImg = (ImageView) findViewById(R.id.walletImg);
        walletName = (TextView) findViewById(R.id.walletName);
        walletNameBody = (LinearLayout) findViewById(R.id.walletNameBody);
        walletBackup = (TextView) findViewById(R.id.walletBackup);
        walletAddress = (TextView) findViewById(R.id.walletAddress);
        etnTokenBalance = (TextView) findViewById(R.id.etnTokenBalance);
        smtTokenBalance = (TextView) findViewById(R.id.smtTokenBalance);
        meshTokenBalance = (TextView) findViewById(R.id.meshTokenBalance);
        ethTokenBody = (CardView) findViewById(R.id.ethTokenBody);
        smtTokenBody = (CardView) findViewById(R.id.smtTokenBody);
        meshTokenBody = (CardView) findViewById(R.id.meshTokenBody);
    }
    @Override
    protected void setListener(){
        walletAddress.setOnClickListener(this);
        walletImg.setOnClickListener(this);
        walletNameBody.setOnClickListener(this);
        ethTokenBody.setOnClickListener(this);
        smtTokenBody.setOnClickListener(this);
        meshTokenBody.setOnClickListener(this);
        swipe_refresh.setOnRefreshListener(this);
    }

    @Override
    protected void initData() {
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

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.walletNameBody://Backup the purse
                if (storableWallet == null){
                    return;
                }
                Intent copyIntent = new Intent(OldAccountUI.this,WalletCopyActivity.class);
                copyIntent.putExtra(Constants.WALLET_INFO, storableWallet);
                copyIntent.putExtra(Constants.WALLET_ICON, storableWallet.getImgId());
                copyIntent.putExtra(Constants.WALLET_TYPE, 1);
                startActivity(copyIntent);
                Utils.openNewActivityAnim(OldAccountUI.this,false);
                break;
            case R.id.walletAddress://Qr code
                if (storableWallet == null){
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
                Intent ethIntent = new Intent(OldAccountUI.this,OldWalletSendDetailUI.class);
                ethIntent.putExtra("sendtype", 0);
                ethIntent.putExtra("storableWallet", storableWallet);
                startActivity(ethIntent);
                Utils.openNewActivityAnim(OldAccountUI.this,false);
                break;
            case R.id.smtTokenBody://Copy the address
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
        ArrayList<StorableWallet> storableWallets = WalletStorage.getInstance(getApplicationContext()).get();
        for (int i = 0 ; i < storableWallets.size(); i++){
            if (storableWallets.get(i).isSelect() ){
                WalletStorage.getInstance(NextApplication.mContext).updateWalletToList(NextApplication.mContext,storableWallets.get(i).getPublicKey(),false);
                index = i;
                int imgId = Utils.getWalletImg(OldAccountUI.this,i);
                storableWallet = storableWallets.get(i);
                if (storableWallet.getImgId() == 0){
                    storableWallet.setImgId(imgId);
                    walletImg.setImageResource(imgId);
                }else{
                    walletImg.setImageResource(storableWallet.getImgId());
                }
                break;
            }
        }
        if (index == -1 && storableWallets.size() > 0){
            int imgId = Utils.getWalletImg(OldAccountUI.this,0);
            storableWallet = storableWallets.get(0);
            if (storableWallet.getImgId() == 0){
                storableWallet.setImgId(imgId);
                walletImg.setImageResource(imgId);
            }else{
                walletImg.setImageResource(storableWallet.getImgId());
            }
        }

        if (storableWallet != null){
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
                loadData(true);
            }
        }, 10);
    }

    @Override
    public void onRefresh() {
        loadData(true);
    }

    /**
     * get token list
     * */
    private void loadData(final boolean isShowToast){
        try {
            NetRequestUtils.getInstance().getBalance(OldAccountUI.this,walletAddress.getText().toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (isShowToast){
                        mHandler.sendEmptyMessage(0);
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Message message = Message.obtain();
                    message.what = 1;
                    message.obj = response.body().string();
                    mHandler.sendMessage(message);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Handler mHandler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    showToast(getString(R.string.error_get_balance));
                    swipe_refresh.setRefreshing(false);
                    break;
                case 1:
                    swipe_refresh.setRefreshing(false);
                    parseJson((String)msg.obj);
                    break;
            }
        }
    };

    /**
     * parse json
     * */
    private void parseJson(String jsonString){
        if (TextUtils.isEmpty(jsonString)){
            return;
        }
        try {
            JSONObject object = new JSONObject(jsonString);
            int errcod = object.optInt("errcod");
            if (errcod == 0){
                double ethBalance1 = object.optJSONObject("data").optDouble("eth");
                double fftBalance1 = object.optJSONObject("data").optDouble("smt");
                double meshBalance1 = object.optJSONObject("data").optDouble("mesh");
                if (ethBalance1 > 0){
                    BigDecimal ethDecimal = new BigDecimal(ethBalance1).setScale(10,BigDecimal.ROUND_DOWN);
                    etnTokenBalance.setText(ethDecimal.toPlainString());
                }else{
                    etnTokenBalance.setText(ethBalance1 +"");
                }
                if (fftBalance1 > 0){
                    BigDecimal fftDecimal = new BigDecimal(fftBalance1).setScale(5,BigDecimal.ROUND_DOWN);
                    smtTokenBalance.setText(fftDecimal.toPlainString());
                }else{
                    smtTokenBalance.setText(fftBalance1 + "");
                }

                if (meshBalance1 > 0){
                    BigDecimal meshDecimal = new BigDecimal(meshBalance1).setScale(5,BigDecimal.ROUND_DOWN);
                    meshTokenBalance.setText(meshDecimal.toPlainString());
                }else{
                    meshTokenBalance.setText(meshBalance1 + "");
                }

                storableWallet.setEthBalance(ethBalance1);
                storableWallet.setFftBalance(fftBalance1);
                storableWallet.setMeshBalance(meshBalance1);
            }else{
                if(errcod == -2){
                    long difftime = object.optJSONObject("data").optLong("difftime");
                    long tempTime =  MySharedPrefs.readLong(OldAccountUI.this,MySharedPrefs.FILE_APPLICATION,MySharedPrefs.KEY_REQTIME);
                    MySharedPrefs.writeLong(OldAccountUI.this,MySharedPrefs.FILE_APPLICATION,MySharedPrefs.KEY_REQTIME,difftime + tempTime);
                }
                MyToast.showToast(OldAccountUI.this,object.optString("msg"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
