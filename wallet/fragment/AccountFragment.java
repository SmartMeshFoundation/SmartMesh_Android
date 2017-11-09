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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseFragment;
import com.lingtuan.firefly.login.LoginUtil;
import com.lingtuan.firefly.quickmark.CaptureActivity;
import com.lingtuan.firefly.quickmark.QuickMarkShowUI;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.wallet.AccountAdapter;
import com.lingtuan.firefly.wallet.ManagerWalletActivity;
import com.lingtuan.firefly.wallet.TransactionRecordsActivity;
import com.lingtuan.firefly.wallet.WalletCopyActivity;
import com.lingtuan.firefly.wallet.WalletCreateActivity;
import com.lingtuan.firefly.wallet.WalletSendActivity;
import com.lingtuan.firefly.wallet.util.WalletStorage;
import com.lingtuan.firefly.wallet.vo.StorableWallet;

import java.math.BigDecimal;
import java.util.ArrayList;

import geth.Address;
import geth.BigInt;
import geth.BoundContract;
import geth.CallOpts;
import geth.Geth;
import geth.Header;
import geth.Interface;
import geth.Interfaces;
import geth.NewHeadHandler;
import geth.SyncProgress;


/**
 * Created  on 2017/8/23.
 * 账户
 */

public class AccountFragment extends BaseFragment implements View.OnClickListener, AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    /**
     * 根view
     */
    private View view = null;
    private boolean isDataFirstLoaded;

    private static final String CONTACT_ADDRESS = "0x4042698c5f4c7eb64035870feea5c316b913927f";
    private static final String CONTACT_ABI = "[{\"constant\":true,\"inputs\":[],\"name\":\"name\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_spender\",\"type\":\"address\"},{\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"approve\",\"outputs\":[{\"name\":\"success\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"totalSupply\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_from\",\"type\":\"address\"},{\"name\":\"_to\",\"type\":\"address\"},{\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"transferFrom\",\"outputs\":[{\"name\":\"success\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"_addr\",\"type\":\"address\"}],\"name\":\"getNonce\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"decimals\",\"outputs\":[{\"name\":\"\",\"type\":\"uint8\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"founder\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"version\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"allocateEndBlock\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"_owner\",\"type\":\"address\"}],\"name\":\"balanceOf\",\"outputs\":[{\"name\":\"balance\",\"type\":\"uint256\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_from\",\"type\":\"address\"},{\"name\":\"_spender\",\"type\":\"address\"},{\"name\":\"_value\",\"type\":\"uint256\"},{\"name\":\"_v\",\"type\":\"uint8\"},{\"name\":\"_r\",\"type\":\"bytes32\"},{\"name\":\"_s\",\"type\":\"bytes32\"}],\"name\":\"approveProxy\",\"outputs\":[{\"name\":\"success\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"symbol\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_owners\",\"type\":\"address[]\"},{\"name\":\"_values\",\"type\":\"uint256[]\"}],\"name\":\"allocateTokens\",\"outputs\":[],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_to\",\"type\":\"address\"},{\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"transfer\",\"outputs\":[{\"name\":\"success\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_spender\",\"type\":\"address\"},{\"name\":\"_value\",\"type\":\"uint256\"},{\"name\":\"_extraData\",\"type\":\"bytes\"}],\"name\":\"approveAndCallcode\",\"outputs\":[{\"name\":\"success\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_spender\",\"type\":\"address\"},{\"name\":\"_value\",\"type\":\"uint256\"},{\"name\":\"_extraData\",\"type\":\"bytes\"}],\"name\":\"approveAndCall\",\"outputs\":[{\"name\":\"success\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"_owner\",\"type\":\"address\"},{\"name\":\"_spender\",\"type\":\"address\"}],\"name\":\"allowance\",\"outputs\":[{\"name\":\"remaining\",\"type\":\"uint256\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"allocateStartBlock\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_from\",\"type\":\"address\"},{\"name\":\"_to\",\"type\":\"address\"},{\"name\":\"_value\",\"type\":\"uint256\"},{\"name\":\"_feeFft\",\"type\":\"uint256\"},{\"name\":\"_v\",\"type\":\"uint8\"},{\"name\":\"_r\",\"type\":\"bytes32\"},{\"name\":\"_s\",\"type\":\"bytes32\"}],\"name\":\"transferProxy\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\"},{\"inputs\":[],\"payable\":false,\"type\":\"constructor\"},{\"payable\":false,\"type\":\"fallback\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"_from\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"_to\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"Transfer\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"_owner\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"_spender\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"Approval\",\"type\":\"event\"}]";
    private BigDecimal ONE_ETHER = new BigDecimal("1000000000000000000");//1个eth

    private TextView accountTitle;
    private ImageView accountInfo;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private TextView walletManager;//账户管理
    private TextView createWallet;//创建钱包
    private TextView showQuicMark;//扫一扫
    private ListView walletListView;//钱包列表
    private AccountAdapter mAdapter;

    private ImageView walletImg;//钱包头像
    private TextView walletName;//钱包名称
    private TextView walletAddress;//钱包地址
    private TextView qrCode;//二维码
    private TextView transRecord;//交易记录
    private TextView copyAddress;//复制地址

    private SwipeRefreshLayout swipe_refresh;

    private StorableWallet storableWallet;

    //eth smt部分
    private TextView ethBalance,fftBalance;//eth、smt余额
    private LinearLayout ethTransfer,fftTransfer;//eth、smt转账
    private LinearLayout ethQrCode,fftQrCode;//eth、smt二维码收款

    private LinearLayout syncblockBg;
    private TextView syncblockText;
    private TextView syncblockBtn;

    private int index = -1;//第几个被选中



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

        syncblockBg = (LinearLayout) view.findViewById(R.id.syncblockBg);
        syncblockText = (TextView) view.findViewById(R.id.syncblockText);
        syncblockBtn = (TextView) view.findViewById(R.id.syncblockBtn);

        //侧边栏相关
        mDrawerLayout = (DrawerLayout) view.findViewById(R.id.account_drawerlayout);
        walletListView = (ListView) view.findViewById(R.id.walletList);
        createWallet = (TextView) view.findViewById(R.id.createWallet);
        showQuicMark = (TextView) view.findViewById(R.id.showQuicMark);
        walletManager = (TextView) view.findViewById(R.id.walletManager);

        //主体相关
        walletImg = (ImageView) view.findViewById(R.id.walletImg);
        walletName = (TextView) view.findViewById(R.id.walletName);
        walletAddress = (TextView) view.findViewById(R.id.walletAddress);
        qrCode = (TextView) view.findViewById(R.id.qrCode);
        transRecord = (TextView) view.findViewById(R.id.transRecord);
        copyAddress = (TextView) view.findViewById(R.id.copyAddress);

        //eth smt相关
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

        syncblockBg.setOnClickListener(this);
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
        initSyncBlockState();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.WALLET_REFRESH_DEL);//刷新页面
        filter.addAction(Constants.CHANGE_LANGUAGE);//语言更新刷新页面
        filter.addAction(Constants.SYNC_PROGRESS);//语言更新刷新页面
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
            }else if (intent != null && (Constants.SYNC_PROGRESS.equals(intent.getAction()))) {
                long number = intent.getLongExtra("currentNumber",0);
                long totalNumber = intent.getLongExtra("totalNumber",0);
                syncblockText.setText(getString(R.string.wallet_sync_ing,number,totalNumber));
                if(number == 0 ||  number >= totalNumber){
                    syncblockBg.setVisibility(View.GONE);
                    loadData();
                }
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
            case R.id.app_right://打开侧边栏
                mDrawerLayout.openDrawer(GravityCompat.END);
                break;
            case R.id.walletManager://账户管理
                mDrawerLayout.closeDrawer(GravityCompat.END);
                startActivity(new Intent(getActivity(), ManagerWalletActivity.class));
                Utils.openNewActivityAnim(getActivity(),false);
                break;
            case R.id.createWallet://创建钱包
                mDrawerLayout.closeDrawer(GravityCompat.END);
                startActivity(new Intent(getActivity(), WalletCreateActivity.class));
                Utils.openNewActivityAnim(getActivity(),false);
                break;
            case R.id.showQuicMark://扫一扫
                mDrawerLayout.closeDrawer(GravityCompat.END);
                startActivity(new Intent(getActivity(), CaptureActivity.class));
                Utils.openNewActivityAnim(getActivity(),false);
                break;
            case R.id.walletImg://备份钱包
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
            case R.id.qrCode://二维码
                if (storableWallet == null){
                    return;
                }
                Intent qrCodeIntent = new Intent(getActivity(),QuickMarkShowUI.class);
                qrCodeIntent.putExtra("type", 0);
                qrCodeIntent.putExtra("address", storableWallet.getPublicKey());
                startActivity(qrCodeIntent);
                Utils.openNewActivityAnim(getActivity(),false);
                break;
            case R.id.transRecord://交易记录
                Intent transIntent = new Intent(getActivity(), TransactionRecordsActivity.class);
                transIntent.putExtra("address",walletAddress.getText().toString());
                transIntent.putExtra("name",storableWallet.getWalletName());
                startActivity(transIntent);
                Utils.openNewActivityAnim(getActivity(),false);
                break;
            case R.id.copyAddress://复制地址
                Utils.copyText(getActivity(),walletAddress.getText().toString());
                break;
            case R.id.ethTransfer://eth转账
                Intent ethIntent = new Intent(getActivity(),WalletSendActivity.class);
                ethIntent.putExtra("sendtype", 0);
                startActivity(ethIntent);
                Utils.openNewActivityAnim(getActivity(),false);
                break;
            case R.id.fftTransfer://smt转账
                Intent fftIntent = new Intent(getActivity(),WalletSendActivity.class);
                fftIntent.putExtra("sendtype", 1);
                startActivity(fftIntent);
                Utils.openNewActivityAnim(getActivity(),false);
                break;
            case R.id.ethQrCode://eth二维码收款
                if (storableWallet == null){
                    return;
                }
                Intent qrEthIntent = new Intent(getActivity(),QuickMarkShowUI.class);
                qrEthIntent.putExtra("type", 1);
                qrEthIntent.putExtra("address", storableWallet.getPublicKey());
                startActivity(qrEthIntent);
                Utils.openNewActivityAnim(getActivity(),false);
                break;
            case R.id.fftQrCode://fft二维码收款
                if (storableWallet == null){
                    return;
                }
                Intent fftEthIntent = new Intent(getActivity(),QuickMarkShowUI.class);
                fftEthIntent.putExtra("type", 2);
                fftEthIntent.putExtra("address", storableWallet.getPublicKey());
                startActivity(fftEthIntent);
                Utils.openNewActivityAnim(getActivity(),false);
                break;
            case R.id.syncblockBg:
                int state = MySharedPrefs.readInt(getActivity(),MySharedPrefs.FILE_USER,MySharedPrefs.AGREE_SYNC_BLOCK);
                if(state == 0){
                    state = 1;
                    MySharedPrefs.writeInt(getActivity(),MySharedPrefs.FILE_USER,MySharedPrefs.AGREE_SYNC_BLOCK,state);
                    syncblockText.setText(getString(R.string.wallet_sync_ing,0,0));
                    syncblockBtn.setVisibility(View.GONE);
                    NextApplication application = (NextApplication) getActivity().getApplication();
                    application.startSync(state);
                }
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
     * 加载或者刷新钱包信息
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
     * 获取账户余额
     * */
    private void loadData(){
        int state = MySharedPrefs.readInt(getActivity(),MySharedPrefs.FILE_USER,MySharedPrefs.AGREE_SYNC_BLOCK);
        if (state != 2){
            swipe_refresh.setRefreshing(false);
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Address address = new Address(walletAddress.getText().toString());
                    BigInt ethBalanceB = NextApplication.ec.getBalanceAt(new geth.Context(),address,-1);
                    BigDecimal ethbalanceD = new BigDecimal(ethBalanceB.toString());
                    double ethBalanceValue = ethbalanceD.divide(ONE_ETHER).setScale(10,BigDecimal.ROUND_DOWN).doubleValue();

                    BigInt fftBalanceB = getTokenBalance();
                    BigDecimal fftbalanceD = new BigDecimal(fftBalanceB.toString());
                    double fftBalanceValue = fftbalanceD.divide(ONE_ETHER).setScale(10,BigDecimal.ROUND_DOWN).doubleValue();

                    if (ethBalanceValue > 0){
                        storableWallet.setEthBalance(ethBalanceValue);
                    }

                    if (fftBalanceValue > 0){
                        storableWallet.setFftBalance(fftBalanceValue);
                    }
                    mHandler.sendEmptyMessage(1);
                } catch (Exception e) {
                    e.printStackTrace();
                    mHandler.sendEmptyMessage(0);
                }
            }
        }).start();

    }

    private BigInt getTokenBalance(){
        geth.Context context = new geth.Context();
        Address address = new Address(CONTACT_ADDRESS);
         try {
            CallOpts opts = Geth.newCallOpts();
            opts.setContext(context);
            BoundContract contract = Geth.bindContract(address,CONTACT_ABI,NextApplication.ec);

            Interfaces results = Geth.newInterfaces(1);
            Interface result = Geth.newInterface();
            result.setDefaultBigInt();
            results.set(0, result);


            Interfaces params = Geth.newInterfaces(1);
            Interface anInterface = Geth.newInterface();
            anInterface.setAddress(new Address(walletAddress.getText().toString()));
            params.set(0,anInterface);

            contract.call(opts,results,"balanceOf",params);
            Interface fft = results.get(0);
            return fft.getBigInt();
        } catch (Exception e) {
            e.printStackTrace();
            return new BigInt(0);
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
                    ethBalance.setText(storableWallet.getEthBalance() + "");
                    fftBalance.setText(storableWallet.getFftBalance() + "");
                    break;
                case 2:
                    showToast(getString(R.string.syning_later));
                    swipe_refresh.setRefreshing(false);
                    break;
            }
        }
    };

    private void  initSyncBlockState(){
        int state = MySharedPrefs.readInt(getActivity(),MySharedPrefs.FILE_USER,MySharedPrefs.AGREE_SYNC_BLOCK);
        if(state == 0){
            syncblockBtn.setVisibility(View.VISIBLE);
            syncblockBg.setVisibility(View.VISIBLE);
            syncblockText.setText(getString(R.string.wallet_sync_start));
        }else if (state == 1){
            syncblockBtn.setVisibility(View.GONE);
            syncblockBg.setVisibility(View.VISIBLE);
            syncblockText.setText(getString(R.string.wallet_sync_ing,0,0));
        }else{
            syncblockBg.setVisibility(View.GONE);
        }
    }
}
