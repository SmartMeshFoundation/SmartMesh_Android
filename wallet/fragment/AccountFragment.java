package com.lingtuan.firefly.wallet.fragment;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseFragment;
import com.lingtuan.firefly.login.LoginUtil;
import com.lingtuan.firefly.quickmark.CaptureActivity;
import com.lingtuan.firefly.quickmark.QuickMarkShowUI;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.MyToast;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.util.netutil.NetRequestUtils;
import com.lingtuan.firefly.wallet.AccountAdapter;
import com.lingtuan.firefly.wallet.ManagerWalletActivity;
import com.lingtuan.firefly.wallet.TransactionRecordsActivity;
import com.lingtuan.firefly.wallet.WalletCopyActivity;
import com.lingtuan.firefly.wallet.WalletCreateActivity;
import com.lingtuan.firefly.wallet.WalletSendActivity;
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

public class AccountFragment extends BaseFragment implements View.OnClickListener, AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    /**
     * root view
     */
    private View view = null;
    private boolean isDataFirstLoaded;
    private TextView accountTitle;
    private ImageView accountInfo;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private TextView walletManager;//Account management
    private TextView createWallet;//Create a wallet
    private TextView showQuicMark;//Flicking a
    private ListView walletListView;//The wallet list
    private AccountAdapter mAdapter;

    private ImageView walletImg;//The wallet picture
    private TextView walletName;//Name of the wallet
    private TextView walletAddress;//The wallet address
    private TextView qrCode;//Qr code
    private TextView transRecord;//Transaction records
    private TextView copyAddress;//Copy the address

    private SwipeRefreshLayout swipe_refresh;

    private StorableWallet storableWallet;

    //ETH SMT parts
    private TextView ethBalance,fftBalance;//eth、smt balance
    private LinearLayout ethTransfer,fftTransfer;//eth、smt transfer
    private LinearLayout ethQrCode,fftQrCode;//eth、smt Qr code collection

    private int index = -1;//Which one is selected



    public AccountFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        isDataFirstLoaded = true;
        super.onCreate(savedInstanceState);
        setRetainInstance(false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (!isDataFirstLoaded && view != null) {
            ((ViewGroup) view.getParent()).removeView(view);
            return view;
        }
        view = inflater.inflate(R.layout.wallet_account_fragment,container,false);
        findViewById();
        setListener();
        initData();
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!isDataFirstLoaded) {
            return;
        }
        isDataFirstLoaded = false;
    }


    private void findViewById() {
        accountTitle = (TextView) view.findViewById(R.id.app_title);
        accountInfo = (ImageView) view.findViewById(R.id.app_right);

        swipe_refresh = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);

        //The sidebar related
        mDrawerLayout = (DrawerLayout) view.findViewById(R.id.account_drawerlayout);
        walletListView = (ListView) view.findViewById(R.id.walletList);
        createWallet = (TextView) view.findViewById(R.id.createWallet);
        showQuicMark = (TextView) view.findViewById(R.id.showQuicMark);
        walletManager = (TextView) view.findViewById(R.id.walletManager);

        //The main related
        walletImg = (ImageView) view.findViewById(R.id.walletImg);
        walletName = (TextView) view.findViewById(R.id.walletName);
        walletAddress = (TextView) view.findViewById(R.id.walletAddress);
        qrCode = (TextView) view.findViewById(R.id.qrCode);
        transRecord = (TextView) view.findViewById(R.id.transRecord);
        copyAddress = (TextView) view.findViewById(R.id.copyAddress);

        //eth smt related
        ethBalance = (TextView) view.findViewById(R.id.ethBalance);
        fftBalance = (TextView) view.findViewById(R.id.fftBalance);
        ethTransfer = (LinearLayout) view.findViewById(R.id.ethTransfer);
        fftTransfer = (LinearLayout) view.findViewById(R.id.fftTransfer);
        ethQrCode = (LinearLayout) view.findViewById(R.id.ethQrCode);
        fftQrCode = (LinearLayout) view.findViewById(R.id.fftQrCode);
    }

    private void setListener(){
        accountInfo.setOnClickListener(this);
        walletManager.setOnClickListener(this);
        createWallet.setOnClickListener(this);
        showQuicMark.setOnClickListener(this);
        walletListView.setOnItemClickListener(this);

        walletImg.setOnClickListener(this);
        walletName.setOnClickListener(this);
        qrCode.setOnClickListener(this);
        transRecord.setOnClickListener(this);
        copyAddress.setOnClickListener(this);

        ethTransfer.setOnClickListener(this);
        fftTransfer.setOnClickListener(this);
        ethQrCode.setOnClickListener(this);
        fftQrCode.setOnClickListener(this);

        swipe_refresh.setOnRefreshListener(this);
    }

    private void initData() {

        swipe_refresh.setColorSchemeResources(R.color.black);
        accountInfo.setVisibility(View.VISIBLE);
        accountInfo.setImageResource(R.drawable.icon_menu);
        view.findViewById(R.id.app_back).setVisibility(View.GONE);
        mAdapter = new AccountAdapter(getActivity());
        walletListView.setAdapter(mAdapter);
        accountTitle.setText(getString(R.string.app_name));
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), mDrawerLayout, R.string.drawer_open,R.string.drawer_close);
        mDrawerToggle.syncState();
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        initWalletInfo();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.WALLET_REFRESH_DEL);//Refresh the page
        filter.addAction(Constants.CHANGE_LANGUAGE);//Update language refresh the page
        getActivity().registerReceiver(mBroadcastReceiver, filter);


    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && (Constants.WALLET_REFRESH_DEL.equals(intent.getAction()))) {
                mAdapter.notifyDataSetChanged();
                initWalletInfo();
            }else if (intent != null && (Constants.CHANGE_LANGUAGE.equals(intent.getAction()))) {
                accountTitle.setText(getString(R.string.app_name));
                Utils.updateViewLanguage(view.findViewById(R.id.account_drawerlayout));
            }
        }
    };


    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mBroadcastReceiver);
        LoginUtil.getInstance().destory();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.app_right://Open the sidebar
                mDrawerLayout.openDrawer(GravityCompat.END);
                break;
            case R.id.walletManager://Account management
                mDrawerLayout.closeDrawer(GravityCompat.END);
                startActivity(new Intent(getActivity(), ManagerWalletActivity.class));
                Utils.openNewActivityAnim(getActivity(),false);
                break;
            case R.id.createWallet://Create a wallet
                mDrawerLayout.closeDrawer(GravityCompat.END);
                startActivity(new Intent(getActivity(), WalletCreateActivity.class));
                Utils.openNewActivityAnim(getActivity(),false);
                break;
            case R.id.showQuicMark://Flicking a
                mDrawerLayout.closeDrawer(GravityCompat.END);
                startActivity(new Intent(getActivity(), CaptureActivity.class));
                Utils.openNewActivityAnim(getActivity(),false);
                break;
            case R.id.walletImg://Backup the purse
            case R.id.walletName:
                if (storableWallet == null){
                    return;
                }
                Intent copyIntent = new Intent(getActivity(),WalletCopyActivity.class);
                copyIntent.putExtra(Constants.WALLET_INFO, storableWallet);
                copyIntent.putExtra(Constants.WALLET_ICON, storableWallet.getImgId());
                copyIntent.putExtra(Constants.WALLET_TYPE, 1);
                startActivity(copyIntent);
                Utils.openNewActivityAnim(getActivity(),false);
                break;
            case R.id.qrCode://Qr code
                if (storableWallet == null){
                    return;
                }
                Intent qrCodeIntent = new Intent(getActivity(),QuickMarkShowUI.class);
                qrCodeIntent.putExtra("type", 0);
                qrCodeIntent.putExtra("address", storableWallet.getPublicKey());
                startActivity(qrCodeIntent);
                Utils.openNewActivityAnim(getActivity(),false);
                break;
            case R.id.transRecord://Transaction records
                Intent transIntent = new Intent(getActivity(), TransactionRecordsActivity.class);
                transIntent.putExtra("address",walletAddress.getText().toString());
                transIntent.putExtra("name",storableWallet.getWalletName());
                startActivity(transIntent);
                Utils.openNewActivityAnim(getActivity(),false);
                break;
            case R.id.copyAddress://Copy the address
                Utils.copyText(getActivity(),walletAddress.getText().toString());
                break;
            case R.id.ethTransfer://The eth transfer
                Intent ethIntent = new Intent(getActivity(),WalletSendActivity.class);
                ethIntent.putExtra("sendtype", 0);
                startActivity(ethIntent);
                Utils.openNewActivityAnim(getActivity(),false);
                break;
            case R.id.fftTransfer://SMT transfer
                Intent fftIntent = new Intent(getActivity(),WalletSendActivity.class);
                fftIntent.putExtra("sendtype", 1);
                startActivity(fftIntent);
                Utils.openNewActivityAnim(getActivity(),false);
                break;
            case R.id.ethQrCode://The eth qr code collection
                if (storableWallet == null){
                    return;
                }
                Intent qrEthIntent = new Intent(getActivity(),QuickMarkShowUI.class);
                qrEthIntent.putExtra("type", 1);
                qrEthIntent.putExtra("address", storableWallet.getPublicKey());
                startActivity(qrEthIntent);
                Utils.openNewActivityAnim(getActivity(),false);
                break;
            case R.id.fftQrCode://SMT qr code collection
                if (storableWallet == null){
                    return;
                }
                Intent fftEthIntent = new Intent(getActivity(),QuickMarkShowUI.class);
                fftEthIntent.putExtra("type", 2);
                fftEthIntent.putExtra("address", storableWallet.getPublicKey());
                startActivity(fftEthIntent);
                Utils.openNewActivityAnim(getActivity(),false);
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (!WalletStorage.getInstance(getActivity().getApplicationContext()).get().get(position).isSelect()){
            for (int i = 0 ; i < WalletStorage.getInstance(getActivity().getApplicationContext()).get().size(); i++){
                if (i != position){
                    WalletStorage.getInstance(getActivity().getApplicationContext()).get().get(i).setSelect(false);
                }else{
                    WalletStorage.getInstance(getActivity().getApplicationContext()).get().get(i).setSelect(true);
                }
            }
            mAdapter.notifyDataSetChanged();
            initWalletInfo();
            mDrawerLayout.closeDrawer(GravityCompat.END);
        }
    }

    /**
     * Load or refresh the wallet information
     * */
    private void initWalletInfo(){
        ArrayList<StorableWallet> storableWallets = WalletStorage.getInstance(getActivity().getApplicationContext()).get();
        for (int i = 0 ; i < storableWallets.size(); i++){
            if (storableWallets.get(i).isSelect() ){
                index = i;
                int imgId = Utils.getWalletImg(getActivity(),i);
                walletImg.setImageResource(imgId);
                storableWallet = storableWallets.get(i);
                storableWallet.setImgId(imgId);
                walletName.setText(storableWallet.getWalletName());
                String address = storableWallet.getPublicKey();
                if(!address.startsWith("0x")){
                    address = "0x"+address;
                }
                walletAddress.setText(address);
                ethBalance.setText(storableWallet.getEthBalance() +"");
                fftBalance.setText(storableWallet.getFftBalance() +"");
            }
        }
        if (index == -1 && storableWallets.size() > 0){
            int imgId = Utils.getWalletImg(getActivity(),0);
            walletImg.setImageResource(imgId);
            storableWallet = storableWallets.get(0);
            storableWallet.setImgId(imgId);
            walletName.setText(storableWallet.getWalletName());
            String address = storableWallet.getPublicKey();

            if(!address.startsWith("0x")){
                address = "0x"+address;
            }
            walletAddress.setText(address);
            ethBalance.setText(storableWallet.getEthBalance() +"");
            fftBalance.setText(storableWallet.getFftBalance() +"");
        }

        new Handler().postDelayed(new Runnable(){
            public void run() {
                swipe_refresh.setRefreshing(true);
                loadData();
            }
        }, 500);
    }
    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable(){
            public void run() {
                loadData();
            }
        }, 500);
    }

    /**
     * Access to the account balance
     * */
    private void loadData(){
        try {
            NetRequestUtils.getInstance().getBalance(getActivity(),walletAddress.getText().toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    mHandler.sendEmptyMessage(0);
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

    @SuppressLint("HandlerLeak")
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
                if (ethBalance1 > 0){
                    BigDecimal ethDecimal = new BigDecimal(ethBalance1).setScale(10,BigDecimal.ROUND_DOWN);
                    ethBalance.setText(ethDecimal.toString());
                }else{
                    ethBalance.setText(ethBalance1 +"");
                }
                if (fftBalance1 > 0){
                    BigDecimal fftDecimal = new BigDecimal(fftBalance1).setScale(5,BigDecimal.ROUND_DOWN);
                    fftBalance.setText(fftDecimal.toString());
                }else{
                    fftBalance.setText(fftBalance1 + "");
                }
                storableWallet.setEthBalance(ethBalance1);
                storableWallet.setFftBalance(fftBalance1);
            }else{
                if(errcod == -2){
                    long difftime = object.optJSONObject("data").optLong("difftime");
                    long tempTime =  MySharedPrefs.readLong(getActivity(),MySharedPrefs.FILE_APPLICATION,MySharedPrefs.KEY_REQTIME);
                    MySharedPrefs.writeLong(getActivity(),MySharedPrefs.FILE_APPLICATION,MySharedPrefs.KEY_REQTIME,difftime + tempTime);
                }
                MyToast.showToast(getActivity(),object.optString("msg"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
