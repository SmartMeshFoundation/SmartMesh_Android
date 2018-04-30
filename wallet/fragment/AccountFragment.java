package com.lingtuan.firefly.wallet.fragment;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
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
import android.widget.PopupWindow;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseFragment;
import com.lingtuan.firefly.custom.CustomListView;
import com.lingtuan.firefly.custom.gesturelock.ACache;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.login.LoginUtil;
import com.lingtuan.firefly.quickmark.CaptureActivity;
import com.lingtuan.firefly.quickmark.QuickMarkShowUI;
import com.lingtuan.firefly.setting.CreateGestureActivity;
import com.lingtuan.firefly.setting.GestureLoginActivity;
import com.lingtuan.firefly.ui.AlertActivity;
import com.lingtuan.firefly.ui.WalletModeLoginUI;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;
import com.lingtuan.firefly.wallet.AccountAdapter;
import com.lingtuan.firefly.wallet.AccountTokenAdapter;
import com.lingtuan.firefly.wallet.ManagerWalletActivity;
import com.lingtuan.firefly.wallet.TokenListUI;
import com.lingtuan.firefly.wallet.TransactionRecordsActivity;
import com.lingtuan.firefly.wallet.WalletCopyActivity;
import com.lingtuan.firefly.wallet.WalletCreateActivity;
import com.lingtuan.firefly.wallet.WalletSendDetailUI;
import com.lingtuan.firefly.wallet.util.WalletStorage;
import com.lingtuan.firefly.wallet.vo.StorableWallet;
import com.lingtuan.firefly.wallet.vo.TokenVo;
import com.lingtuan.firefly.wallet.vo.TransVo;
import com.lingtuan.firefly.walletold.OldAccountUI;
import com.lingtuan.firefly.xmpp.XmppAction;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created  on 2017/8/23.
 * account
 */

public class AccountFragment extends BaseFragment implements View.OnClickListener, AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener, AccountTokenAdapter.TokenOnItemClick {

    /**
     * root view
     */
    private View view = null;
    private boolean isDataFirstLoaded;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private TextView walletGesture;//wallet gesture
    private TextView walletManager;//Account management
    private TextView createWallet;//Create a wallet
    private TextView showQuicMark;//Flicking a
    private ImageView changeTokenUnit;//changeTokenUnit

    private ListView walletListView;//The wallet list
    private AccountAdapter mAdapter;
    private ArrayList<TokenVo> tokenVos;

    private CustomListView accountTokenList;//token list
    private AccountTokenAdapter mTokenAdapter;
    private ImageView walletImg;//The wallet picture
    private TextView walletName;//Name of the wallet
    private TextView walletBackup;//backup of the wallet
    private TextView walletAddress;//The wallet address
    private TextView qrCode;//Qr code
    private TextView transRecord;//Transaction records
    private TextView copyAddress;//Copy the address

    private LinearLayout walletNameBody;

    private SwipeRefreshLayout swipe_refresh;

    private StorableWallet storableWallet;

    private TextView walletBalanceNum;

    private ImageView cnyUsdChangeGif;
    private LinearLayout currencyBg;

//    private LinearLayout raidenTransfer;//raiden

    private int index = -1;//Which one is selected

    private ImageView walletBalanceAdd;

    private boolean isChecked;

    private Timer timer;
    private TimerTask timerTask;
    private int timerLine = 10;

    private PopupWindow homePop;
    private PopupWindow ethPop;
    private PopupWindow windowPop;

    private LinearLayout ethWindow;

    private String total;
    private String usdTotal;

    private TextView oldWallet;
    private ImageView ethWarningImg;
    private ImageView oldWalletImg;

    private View windowBg;

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

        swipe_refresh = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);

        //The sidebar related
        mDrawerLayout = (DrawerLayout) view.findViewById(R.id.account_drawerlayout);
        walletListView = (ListView) view.findViewById(R.id.walletList);
        accountTokenList = (CustomListView) view.findViewById(R.id.accountTokenList);
        createWallet = (TextView) view.findViewById(R.id.createWallet);
        showQuicMark = (TextView) view.findViewById(R.id.showQuicMark);
        changeTokenUnit = (ImageView) view.findViewById(R.id.changeTokenUnit);
        walletManager = (TextView) view.findViewById(R.id.walletManager);
        walletGesture = (TextView) view.findViewById(R.id.walletGesture);

        //The main related
        walletImg = (ImageView) view.findViewById(R.id.walletImg);
        walletName = (TextView) view.findViewById(R.id.walletName);
        walletNameBody = (LinearLayout) view.findViewById(R.id.walletNameBody);
        walletBackup = (TextView) view.findViewById(R.id.walletBackup);
        walletAddress = (TextView) view.findViewById(R.id.walletAddress);
        walletBalanceAdd = (ImageView) view.findViewById(R.id.walletBalanceAdd);
        qrCode = (TextView) view.findViewById(R.id.qrCode);
        transRecord = (TextView) view.findViewById(R.id.transRecord);
        copyAddress = (TextView) view.findViewById(R.id.copyAddress);
        walletBalanceNum = (TextView) view.findViewById(R.id.walletBalanceNum);

        oldWallet = (TextView) view.findViewById(R.id.oldWallet);
        ethWarningImg = (ImageView) view.findViewById(R.id.ethWarningImg);
        oldWalletImg = (ImageView) view.findViewById(R.id.oldWalletImg);
        cnyUsdChangeGif = (ImageView) view.findViewById(R.id.cnyUsdChangeGif);
        currencyBg = (LinearLayout) view.findViewById(R.id.currencyBg);
        windowBg = view.findViewById(R.id.windowBg);
//        raidenTransfer = (LinearLayout) view.findViewById(R.id.raidenTransfer);
    }

    private void setListener(){
        walletManager.setOnClickListener(this);
        walletGesture.setOnClickListener(this);
        walletAddress.setOnClickListener(this);
        createWallet.setOnClickListener(this);
        showQuicMark.setOnClickListener(this);
        changeTokenUnit.setOnClickListener(this);
        walletListView.setOnItemClickListener(this);

        walletImg.setOnClickListener(this);
        walletNameBody.setOnClickListener(this);
        walletBalanceAdd.setOnClickListener(this);
        qrCode.setOnClickListener(this);
        transRecord.setOnClickListener(this);
        copyAddress.setOnClickListener(this);
        oldWallet.setOnClickListener(this);
        oldWalletImg.setOnClickListener(this);
        ethWarningImg.setOnClickListener(this);

//        raidenTransfer.setOnClickListener(this);

        swipe_refresh.setOnRefreshListener(this);
    }

    private void initData() {

        accountTokenList.setFocusable(false);

        swipe_refresh.setColorSchemeResources(R.color.black);
        mAdapter = new AccountAdapter(getActivity());
        walletListView.setAdapter(mAdapter);

        tokenVos = new ArrayList<>();
        mTokenAdapter = new AccountTokenAdapter(getActivity(),tokenVos,this);
        accountTokenList.setAdapter(mTokenAdapter);

        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), mDrawerLayout, R.string.drawer_open,R.string.drawer_close);
        mDrawerToggle.syncState();
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        setWalletGesture();
        initWalletInfo();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.WALLET_REFRESH_DEL);//Refresh the page
        filter.addAction(Constants.WALLET_SUCCESS);//Refresh the page
        filter.addAction(Constants.WALLET_REFRESH_BACKUP);//Refresh the page
        filter.addAction(Constants.WALLET_REFRESH_GESTURE);//Refresh the page
        filter.addAction(Constants.WALLET_REFRESH_SHOW_HINT);//trans
        filter.addAction(Constants.ACTION_GESTURE_LOGIN);//trans
        filter.addAction(Constants.CHANGE_LANGUAGE);//Update language refresh the page
        filter.addAction(XmppAction.ACTION_TRANS);//trans
        filter.addAction(Constants.WALLET_BIND_TOKEN);
        getActivity().registerReceiver(mBroadcastReceiver, filter);

        int priceUnit = MySharedPrefs.readInt(getActivity(),MySharedPrefs.FILE_USER,MySharedPrefs.KEY_TOKEN_PRICE_UNIT);//0 default  1 usd
        if (priceUnit == 0){
            changeTokenUnit.setImageResource(R.drawable.icon_unit_cny);
        }else{
            changeTokenUnit.setImageResource(R.drawable.icon_unit_usd);
        }

        boolean isFirstShowWindow = MySharedPrefs.readBoolean(getActivity(),MySharedPrefs.FILE_USER,MySharedPrefs.KEY_FIRST_WALLET_SHOW_WINDOW);
        if (isFirstShowWindow){
            MySharedPrefs.writeBoolean(getActivity(),MySharedPrefs.FILE_USER,MySharedPrefs.KEY_FIRST_WALLET_SHOW_WINDOW,false);
            initWindowPop();
        }

//        writePassToSdcard();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                String keystorePath = new File(getActivity().getFilesDir(), SDCardCtrl.WALLERPATH).getPath();
//                String raidenDataPath = new File(getActivity().getFilesDir(), SDCardCtrl.RAIDEN_DATA).getPath();
//                String raidenPassPath = new File(getActivity().getFilesDir(), SDCardCtrl.RAIDEN_PASS).getPath() + "/pass";
//                Mobile.mobileStartUp("0x70aefe8d97ef5984b91b5169418f3db283f65a29", keystorePath,"ws://192.168.0.131:8546",raidenDataPath,raidenPassPath);
//            }
//        }).start();
    }
//
//    private void writePassToSdcard() {
//        try {
//            File dir = new File(getActivity().getFilesDir() + SDCardCtrl.RAIDEN_PASS);
//            if (!dir.exists()) {
//                dir.mkdirs();
//            }
//            File f = new File(dir, "pass");
//            if (!f.exists()) {
//                f.createNewFile();
//            }
//            copyString("123456", f);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void copyString(String fileContents, File outputFile) throws FileNotFoundException {
//        OutputStream output = new FileOutputStream(outputFile);
//        PrintWriter p = new PrintWriter(output);
//        p.println(fileContents);
//        p.flush();
//        p.close();
//    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null){
                if (Constants.WALLET_REFRESH_DEL.equals(intent.getAction()) || Constants.WALLET_REFRESH_BACKUP.equals(intent.getAction()) ) {
                    initWalletInfo();
                }else if (Constants.WALLET_REFRESH_GESTURE.equals(intent.getAction()) || Constants.WALLET_SUCCESS.equals(intent.getAction())) {
                    initWalletInfo();
                }else if (Constants.ACTION_GESTURE_LOGIN.equals(intent.getAction())) {
                    initWalletInfo();
                }else if (Constants.CHANGE_LANGUAGE.equals(intent.getAction())) {
                    Utils.updateViewLanguage(view.findViewById(R.id.account_drawerlayout));
                }else if (XmppAction.ACTION_TRANS.equals(intent.getAction())) {
                    loadData(false);
                }else if (Constants.WALLET_REFRESH_SHOW_HINT.equals(intent.getAction())){
                    if (mDrawerLayout != null){
                        mDrawerLayout.closeDrawer(GravityCompat.END);
                    }
                    initHomePop();
                    showPowTimer();
                }else if (Constants.WALLET_BIND_TOKEN.equals(intent.getAction())){
                    tokenVos = FinalUserDataBase.getInstance().getOpenTokenList(walletAddress.getText().toString());
                    mTokenAdapter.resetSource(tokenVos);
                }
            }
        }
    };


    /**
     * Initialize the Pop layout
     */
    private void initHomePop() {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.account_more_popup_layout, null);
        homePop = new PopupWindow(view, Utils.dip2px(getActivity(), 160), LinearLayout.LayoutParams.WRAP_CONTENT);
        homePop.setBackgroundDrawable(new BitmapDrawable());
        homePop.setOutsideTouchable(true);
        homePop.setFocusable(true);
        view.findViewById(R.id.txt_home_pop_1).setOnClickListener(this);
        if (homePop.isShowing()) {
            homePop.dismiss();
        } else {
            // On the coordinates of a specific display PopupWindow custom menu
            homePop.showAsDropDown(walletImg, Utils.dip2px(getActivity(), -118), Utils.dip2px(getActivity(), 0));
        }
    }

    private void dismissHomePop() {
        if (homePop != null && homePop.isShowing()) {
            homePop.dismiss();
            homePop = null;
        }
    }

    /**
     * Initialize the Pop layout
     */
    private void initEthereumPop() {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.eth_more_popup_layout, null);
        ethPop = new PopupWindow(view, Utils.dip2px(getActivity(), 320), LinearLayout.LayoutParams.WRAP_CONTENT);
        ethPop.setBackgroundDrawable(new BitmapDrawable());
        ethPop.setOutsideTouchable(true);
        ethPop.setFocusable(true);
        if (ethPop.isShowing()) {
            ethPop.dismiss();
        } else {
            boolean isChinese;
            String language = MySharedPrefs.readString(getActivity(),MySharedPrefs.FILE_APPLICATION,MySharedPrefs.KEY_LANGUAFE);
            if (TextUtils.isEmpty(language)){
                Locale locale = new Locale(Locale.getDefault().getLanguage());
                if (TextUtils.equals(locale.getLanguage(),"zh")){
                    isChinese = true;
                }else{
                    isChinese = false;
                }
            }else{
                if (TextUtils.equals(language,"zh")){
                    isChinese = true;
                }else{
                    isChinese = false;
                }
            }
            if (isChinese){
                ethPop.showAsDropDown(ethWarningImg, Utils.dip2px(getActivity(), -25), Utils.dip2px(getActivity(), -265));
            }else{
                ethPop.showAsDropDown(ethWarningImg, Utils.dip2px(getActivity(), -25), Utils.dip2px(getActivity(), -326));
            }

        }
    }

    /**
     * Initialize the Pop layout
     */
    private void initWindowPop() {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.eth_window_popup_layout, null);
        windowPop = new PopupWindow(view, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        windowPop.setBackgroundDrawable(new BitmapDrawable());
        windowPop.setOutsideTouchable(true);
        windowPop.setFocusable(true);
        ethWindow = (LinearLayout) view.findViewById(R.id.ethWindow);
        ethWindow.setOnClickListener(this);
        boolean isChinese;
        String language = MySharedPrefs.readString(getActivity(),MySharedPrefs.FILE_APPLICATION,MySharedPrefs.KEY_LANGUAFE);
        if (TextUtils.isEmpty(language)){
            Locale locale = new Locale(Locale.getDefault().getLanguage());
            if (TextUtils.equals(locale.getLanguage(),"zh")){
                isChinese = true;
            }else{
                isChinese = false;
            }
        }else{
            if (TextUtils.equals(language,"zh")){
                isChinese = true;
            }else{
                isChinese = false;
            }
        }
        if (isChinese){
            ethWindow.setBackgroundResource(R.drawable.icon_eth_window_zh);
        }else{
            ethWindow.setBackgroundResource(R.drawable.icon_eth_window_en);
        }
        if (windowPop.isShowing()) {
            windowPop.dismiss();
        } else {
            windowPop.showAsDropDown(windowBg, Utils.dip2px(getActivity(), 0), Utils.dip2px(getActivity(), 0));
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mBroadcastReceiver);
        LoginUtil.getInstance().destory();
        cancelTimer();
    }

    @Override
    public void onResume() {
        super.onResume();
        setWalletGesture();
    }

    /**
     * set wallet gesture  open or close
     * */
    private void setWalletGesture(){
        int walletMode = MySharedPrefs.readInt(getActivity(), MySharedPrefs.FILE_USER, MySharedPrefs.KEY_IS_WALLET_PATTERN);
        byte[] gestureByte;
        if (walletMode == 0 && NextApplication.myInfo != null){
            gestureByte = ACache.get(NextApplication.mContext).getAsBinary(Constants.GESTURE_PASSWORD + NextApplication.myInfo.getLocalId());
        }else{
            gestureByte = ACache.get(NextApplication.mContext).getAsBinary(Constants.GESTURE_PASSWORD);
        }
        if (gestureByte != null && gestureByte.length > 0){
            isChecked = true;
            walletGesture.setText(getString(R.string.gesture_wallet_open));
        }else{
            isChecked = false;
            walletGesture.setText(getString(R.string.gesture_wallet_close));
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.walletImg://Open the sidebar
                mDrawerLayout.openDrawer(GravityCompat.END);
                dismissHomePop();
                cancelTimer();
                break;
            case R.id.txt_home_pop_1://Open the sidebar
                dismissHomePop();
                cancelTimer();
                break;
            case R.id.walletManager://Account management
                mDrawerLayout.closeDrawer(GravityCompat.END);
                dismissHomePop();
                cancelTimer();
                startActivity(new Intent(getActivity(), ManagerWalletActivity.class));
                Utils.openNewActivityAnim(getActivity(),false);
                break;
            case R.id.walletGesture://Account management
                int walletMode = MySharedPrefs.readInt(getActivity(), MySharedPrefs.FILE_USER, MySharedPrefs.KEY_IS_WALLET_PATTERN);
                byte[] gestureByte;
                if (walletMode == 0 && NextApplication.myInfo != null){
                    gestureByte = ACache.get(NextApplication.mContext).getAsBinary(Constants.GESTURE_PASSWORD + NextApplication.myInfo.getLocalId());
                }else{
                    gestureByte = ACache.get(NextApplication.mContext).getAsBinary(Constants.GESTURE_PASSWORD);
                }
                if (gestureByte != null && gestureByte.length > 0){
                    Intent intent = new Intent(getActivity(),GestureLoginActivity.class);
                    intent.putExtra("type",isChecked ? 2 : 1);
                    startActivity(intent);
                    isChecked = !isChecked;
                }else{
                    Intent intent = new Intent(getActivity(),CreateGestureActivity.class);
                    startActivity(intent);
                }
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
            case R.id.changeTokenUnit://Flicking a

                MySharedPrefs.writeBoolean(getActivity(),MySharedPrefs.FILE_USER,MySharedPrefs.KEY_FIRST_WALLET_SHOW_GIF,false);
                currencyBg.setVisibility(View.GONE);

                int priceUnit = MySharedPrefs.readInt(getActivity(),MySharedPrefs.FILE_USER,MySharedPrefs.KEY_TOKEN_PRICE_UNIT);//0 default  1 usd
                if (priceUnit == 0){
                    MySharedPrefs.writeInt(getActivity(),MySharedPrefs.FILE_USER,MySharedPrefs.KEY_TOKEN_PRICE_UNIT,1);
                    walletBalanceNum.setText(getString(R.string.token_total_usd_price,usdTotal));
                    changeTokenUnit.setImageResource(R.drawable.icon_unit_usd);
                }else{
                    MySharedPrefs.writeInt(getActivity(),MySharedPrefs.FILE_USER,MySharedPrefs.KEY_TOKEN_PRICE_UNIT,0);
                    walletBalanceNum.setText(getString(R.string.token_total_price,total));
                    changeTokenUnit.setImageResource(R.drawable.icon_unit_cny);
                }
                mTokenAdapter.notifyDataSetChanged();
                break;
            case R.id.walletNameBody://Backup the purse
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
            case R.id.walletAddress://Qr code
                if (storableWallet == null || tokenVos == null){
                    return;
                }
                if (!storableWallet.isBackup()){
                    Intent intent = new Intent(getActivity(), AlertActivity.class);
                    intent.putExtra("type", 5);
                    intent.putExtra("strablewallet", storableWallet);
                    startActivity(intent);
                    getActivity().overridePendingTransition(0, 0);
                    return;
                }
                Intent qrCodeIntent = new Intent(getActivity(),QuickMarkShowUI.class);
                qrCodeIntent.putExtra("tokenVo", tokenVos.get(0));
                qrCodeIntent.putExtra("address", storableWallet.getPublicKey());
                startActivity(qrCodeIntent);
                Utils.openNewActivityAnim(getActivity(),false);
                break;
            case R.id.walletBalanceAdd:
                Intent tokenListIntent = new Intent(getActivity(),TokenListUI.class);
                tokenListIntent.putExtra("address",walletAddress.getText().toString());
                startActivity(tokenListIntent);
                Utils.openNewActivityAnim(getActivity(),false);
                break;
            case R.id.transRecord://Transaction records
                if (NextApplication.myInfo == null){
                    startActivity(new Intent(getActivity(),WalletModeLoginUI.class));
                    Utils.openNewActivityAnim(getActivity(),false);
                }else{
                    Intent transIntent = new Intent(getActivity(), TransactionRecordsActivity.class);
                    transIntent.putExtra("address",walletAddress.getText().toString());
                    transIntent.putExtra("name",storableWallet.getWalletName());
                    startActivity(transIntent);
                    Utils.openNewActivityAnim(getActivity(),false);
                }
                break;
            case R.id.copyAddress://Copy the address
                if (storableWallet != null && !storableWallet.isBackup()){
                    Intent intent = new Intent(getActivity(), AlertActivity.class);
                    intent.putExtra("type", 5);
                    intent.putExtra("strablewallet", storableWallet);
                    startActivity(intent);
                    getActivity().overridePendingTransition(0, 0);
                    return;
                }
                Utils.copyText(getActivity(),walletAddress.getText().toString());
                break;
            case R.id.oldWallet://Copy the address
            case R.id.oldWalletImg://Copy the address
                Intent intent = new Intent(getActivity(), OldAccountUI.class);
                intent.putExtra("strablewallet", storableWallet);
                startActivity(intent);
                Utils.openNewActivityAnim(getActivity(),false);
                break;
            case R.id.ethWarningImg://Copy the address
                initEthereumPop();
                break;
            case R.id.ethWindow://Copy the address
                if (windowPop != null && windowPop.isShowing()) {
                    windowPop.dismiss();
                    windowPop = null;
                }
                boolean isFirstShowGif = MySharedPrefs.readBoolean(getActivity(),MySharedPrefs.FILE_USER,MySharedPrefs.KEY_FIRST_WALLET_SHOW_GIF);
                if (isFirstShowGif){
                        currencyBg.setVisibility(View.VISIBLE);
                        MySharedPrefs.writeBoolean(getActivity(),MySharedPrefs.FILE_USER,MySharedPrefs.KEY_FIRST_WALLET_SHOW_GIF,false);
                        cnyUsdChangeGif.setImageResource(R.drawable.cny_usd_change);
                        if ( cnyUsdChangeGif.getDrawable() instanceof AnimationDrawable) {
                            ((AnimationDrawable) cnyUsdChangeGif.getDrawable()).start();
                            ((AnimationDrawable) cnyUsdChangeGif.getDrawable()).setOneShot(false);
                        }
                }else{
                    currencyBg.setVisibility(View.GONE);
                }
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (!WalletStorage.getInstance(getActivity().getApplicationContext()).get().get(position).isSelect()){
            String address = "";
            for (int i = 0 ; i < WalletStorage.getInstance(getActivity().getApplicationContext()).get().size(); i++){
                if (i != position){
                    WalletStorage.getInstance(getActivity().getApplicationContext()).get().get(i).setSelect(false);
                }else{
                    WalletStorage.getInstance(getActivity().getApplicationContext()).get().get(i).setSelect(true);
                    address = WalletStorage.getInstance(getActivity().getApplicationContext()).get().get(i).getPublicKey();
                }
            }
            initWalletInfo();
            mDrawerLayout.closeDrawer(GravityCompat.END);
            if (!TextUtils.isEmpty(address)){
                if (!address.startsWith("0x")){
                    address = "0x" + address;
                }
                boolean hasGetTrans =  MySharedPrefs.readBooleanNormal(getActivity(),MySharedPrefs.FILE_USER,MySharedPrefs.KEY_WALLET_ALL_TRANS + address);
                if (!hasGetTrans){
                    getAllTransactionList(address);
                }
            }
        }
    }


    /**
     * Get all transaction records for the specified address
     * @param address   wallet address
     * */
    private void getAllTransactionList(String address) {

        if (!address.startsWith("0x")){
            address = "0x" + address;
        }
        final String finalAddress = address;
        NetRequestImpl.getInstance().getAllTransactionList(address, new RequestListener() {
            @Override
            public void start() {

            }

            @Override
            public void success(JSONObject response) {
                MySharedPrefs.writeBoolean(getActivity(),MySharedPrefs.FILE_USER,MySharedPrefs.KEY_WALLET_ALL_TRANS + finalAddress,true);
                JSONArray array = response.optJSONArray("data");
                int blockNumber = response.optInt("blockNumber");
                if (array != null){
                    FinalUserDataBase.getInstance().beginTransaction();
                    for (int i = 0 ; i < array.length() ; i++){
                        JSONObject obiect = array.optJSONObject(i);
                        TransVo transVo = new TransVo().parse(obiect);
                        transVo.setBlockNumber(blockNumber);
                        transVo.setState(1);
                        FinalUserDataBase.getInstance().updateTrans(transVo);
                    }
                    FinalUserDataBase.getInstance().endTransactionSuccessful();
                }
            }

            @Override
            public void error(int errorCode, String errorMsg) {

            }
        });
    }

    @Override
    public void setTokenOnItemClick(int position) {
        if (storableWallet == null || tokenVos == null || tokenVos.size() <= 0){
            return;
        }
        Intent ethIntent = new Intent(getActivity(),WalletSendDetailUI.class);
        ethIntent.putExtra("tokenVo", tokenVos.get(position));
        double smtBalance = 0;
        if (TextUtils.equals(tokenVos.get(0).getTokenSymbol(),getString(R.string.smt))){
            smtBalance = tokenVos.get(0).getTokenBalance();
        }else{
            for (int i = 0 ; i < tokenVos.size() ; i++){
                TokenVo tokenVo = tokenVos.get(i);
                if (TextUtils.equals(tokenVo.getTokenSymbol(),getString(R.string.smt))){
                    smtBalance = tokenVo.getTokenBalance();
                    break;
                }
            }
        }
        ethIntent.putExtra("smtBalance", smtBalance);
        ethIntent.putExtra("storableWallet", storableWallet);
        startActivity(ethIntent);
        Utils.openNewActivityAnim(getActivity(),false);
    }

    /**
     * Load or refresh the wallet information
     * */
    private void initWalletInfo(){
        ArrayList<StorableWallet> storableWallets = WalletStorage.getInstance(getActivity().getApplicationContext()).get();
        for (int i = 0 ; i < storableWallets.size(); i++){
            if (storableWallets.get(i).isSelect() ){
                WalletStorage.getInstance(NextApplication.mContext).updateWalletToList(NextApplication.mContext,storableWallets.get(i).getPublicKey(),false);
                index = i;
                int imgId = Utils.getWalletImg(getActivity(),i);
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
            int imgId = Utils.getWalletImg(getActivity(),0);
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

        mAdapter.resetSource(WalletStorage.getInstance(NextApplication.mContext).get());
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
        boolean isFirstGet = MySharedPrefs.readBoolean(getActivity(),MySharedPrefs.FILE_USER,MySharedPrefs.KEY_FIRST_GET_TOKEN_LIST + walletAddress.getText().toString());
        tokenVos = FinalUserDataBase.getInstance().getOpenTokenList(walletAddress.getText().toString());
        if (!isFirstGet && tokenVos != null &&  tokenVos.size() > 0){
            mTokenAdapter.resetSource(tokenVos);
            getBalance(isShowToast);
        }else{
            NetRequestImpl.getInstance().getTokenList(walletAddress.getText().toString(), new RequestListener() {
                @Override
                public void start() {

                }

                @Override
                public void success(JSONObject response) {
                    tokenVos.clear();
                    JSONArray array = response.optJSONArray("data");
                    if (array != null){
                        for (int i = 0 ; i < array.length() ; i++){
                            TokenVo tokenVo = new TokenVo().parse(array.optJSONObject(i));
                            if (tokenVo.isFixed()){
                                tokenVo.setChecked(true);
                            }
                            tokenVos.add(tokenVo);
                        }
                    }
                    FinalUserDataBase.getInstance().beginTransaction();
                    for (int i = 0 ; i < tokenVos.size() ; i++){
                        FinalUserDataBase.getInstance().updateTokenList(tokenVos.get(i),walletAddress.getText().toString(),true);
                    }
                    FinalUserDataBase.getInstance().endTransactionSuccessful();
                    MySharedPrefs.writeBoolean(getActivity(),MySharedPrefs.FILE_USER,MySharedPrefs.KEY_FIRST_GET_TOKEN_LIST + walletAddress.getText().toString(),false);
                    mTokenAdapter.resetSource(tokenVos);
                    getBalance(isShowToast);
                }

                @Override
                public void error(int errorCode, String errorMsg) {
                    getBalance(isShowToast);
                }
            });
        }
    }

    /**
     * get token balance
     * */
    private void getBalance(final boolean isShowToast){

        if (tokenVos == null || tokenVos.size() <= 0){
            return;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0 ; i < tokenVos.size() ; i++){
            builder.append(tokenVos.get(i).getContactAddress()).append(",");
        }
        if (builder.length() > 0){
            builder.deleteCharAt(builder.length() - 1);
        }
        NetRequestImpl.getInstance().getBalance(walletAddress.getText().toString(), builder.toString(), new RequestListener() {
            @Override
            public void start() {

            }

            @Override
            public void success(JSONObject response) {
                tokenVos.clear();
                swipe_refresh.setRefreshing(false);
                BigDecimal totalDecimal = new BigDecimal(response.optString("total")).setScale(2,BigDecimal.ROUND_DOWN);
                BigDecimal usdTotalDecimal = new BigDecimal(response.optString("usd_total")).setScale(2,BigDecimal.ROUND_DOWN);
                total = totalDecimal.toPlainString();
                usdTotal = usdTotalDecimal.toPlainString();
                int priceUnit = MySharedPrefs.readInt(getActivity(),MySharedPrefs.FILE_USER,MySharedPrefs.KEY_TOKEN_PRICE_UNIT);//0 default  1 usd
                if (priceUnit == 0){
                    walletBalanceNum.setText(getString(R.string.token_total_price,total));
                    changeTokenUnit.setImageResource(R.drawable.icon_unit_cny);
                }else{
                    walletBalanceNum.setText(getString(R.string.token_total_usd_price,usdTotal));
                    changeTokenUnit.setImageResource(R.drawable.icon_unit_usd);
                }
                JSONArray array = response.optJSONArray("data");
                if (array != null){
                    for (int i = 0 ; i < array.length() ; i++){
                        TokenVo tokenVo = new TokenVo().parse(array.optJSONObject(i));
                        tokenVo.setChecked(true);
                        tokenVos.add(tokenVo);
                    }
                }

                FinalUserDataBase.getInstance().beginTransaction();
                for (int i = 0 ; i < tokenVos.size() ; i++){
                    FinalUserDataBase.getInstance().updateTokenList(tokenVos.get(i),walletAddress.getText().toString(),false);
                }
                FinalUserDataBase.getInstance().endTransactionSuccessful();
                mTokenAdapter.resetSource(tokenVos);
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                if (isShowToast){
                    showToast(errorMsg);
                }
                mTokenAdapter.resetSource(tokenVos);
                swipe_refresh.setRefreshing(false);
            }
        });
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2:
                    dismissHomePop();
                    break;
            }
        }
    };


    /**
     * Control the popup countdown
     * */
    private void showPowTimer(){
        if(timer == null){
            timer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    timerLine--;
                    if(timerLine < 0){
                        mHandler.sendEmptyMessage(2);
                        if (timer != null){
                            timer.cancel();
                            timer = null;
                        }
                        if (timerTask != null){
                            timerTask.cancel();
                            timerTask = null;
                        }
                    }
                }
            };
            timer.schedule(timerTask,1000,1000);
        }
    }

    private void cancelTimer(){
        if (timer != null){
            timer.cancel();
            timer = null;
        }
        if (timerTask != null){
            timerTask.cancel();
            timerTask = null;
        }
    }

}
