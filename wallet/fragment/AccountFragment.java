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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseFragment;
import com.lingtuan.firefly.custom.CustomListView;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.login.LoginUtil;
import com.lingtuan.firefly.quickmark.CaptureActivity;
import com.lingtuan.firefly.quickmark.QuickMarkShowUI;
import com.lingtuan.firefly.setting.CreateGestureActivity;
import com.lingtuan.firefly.setting.GestureLoginActivity;
import com.lingtuan.firefly.ui.AlertActivity;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.wallet.AccountAdapter;
import com.lingtuan.firefly.wallet.AccountTokenAdapter;
import com.lingtuan.firefly.wallet.ManagerWalletActivity;
import com.lingtuan.firefly.wallet.TokenListUI;
import com.lingtuan.firefly.wallet.WalletCopyActivity;
import com.lingtuan.firefly.wallet.WalletCreateActivity;
import com.lingtuan.firefly.wallet.WalletSendDetailUI;
import com.lingtuan.firefly.wallet.contract.AccountContract;
import com.lingtuan.firefly.wallet.presenter.AccountPresenterImpl;
import com.lingtuan.firefly.wallet.util.WalletStorage;
import com.lingtuan.firefly.wallet.vo.StorableWallet;
import com.lingtuan.firefly.wallet.vo.TokenVo;
import com.lingtuan.firefly.walletold.OldAccountUI;
import com.lingtuan.firefly.xmpp.XmppAction;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.Unbinder;

/**
 * Created  on 2017/8/23.
 * account
 */

public class AccountFragment extends BaseFragment implements View.OnClickListener, AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener, AccountTokenAdapter.TokenOnItemClick,AccountContract.View {

    @BindView(R.id.windowBg)
    View windowBg;
    @BindView(R.id.walletName)
    TextView walletName;//Name of the wallet
    @BindView(R.id.walletBackup)
    TextView walletBackup;//backup of the wallet
    @BindView(R.id.walletAddress)
    TextView walletAddress;//The wallet address
    @BindView(R.id.walletImg)
    ImageView walletImg;//The wallet picture
    @BindView(R.id.cnyUsdChangeGif)
    ImageView cnyUsdChangeGif;
    @BindView(R.id.currencyBg)
    LinearLayout currencyBg;
    @BindView(R.id.changeTokenUnit)
    ImageView changeTokenUnit;
    @BindView(R.id.walletBalanceNum)
    TextView walletBalanceNum;
    @BindView(R.id.walletBalanceAdd)
    ImageView walletBalanceAdd;
    @BindView(R.id.accountTokenList)
    CustomListView accountTokenList;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipe_refresh;
    @BindView(R.id.ethWarningImg)
    ImageView ethWarningImg;
    @BindView(R.id.oldWallet)
    TextView oldWallet;
    @BindView(R.id.oldWalletImg)
    ImageView oldWalletImg;
    @BindView(R.id.walletList)
    ListView walletListView;
    @BindView(R.id.walletGesture)
    TextView walletGesture;
    @BindView(R.id.walletManager)
    TextView walletManager;
    @BindView(R.id.createWallet)
    TextView createWallet;
    @BindView(R.id.showQuicMark)
    TextView showQuicMark;
    @BindView(R.id.account_drawerlayout)
    DrawerLayout mDrawerLayout;

    private Unbinder unbinder;

    /**
     * root view
     */
    private View view = null;
    private boolean isDataFirstLoaded;
    private ActionBarDrawerToggle mDrawerToggle;
    private AccountAdapter mAdapter;
    private ArrayList<TokenVo> tokenVos;
    private AccountTokenAdapter mTokenAdapter;
    private StorableWallet storableWallet;

    private boolean isChecked;

    private PopupWindow homePop;
    private PopupWindow ethPop;
    private PopupWindow windowPop;

    private LinearLayout ethWindow;
    private RelativeLayout homePopBody;

    private String total;
    private String usdTotal;

    private AccountContract.Presenter mPresenter;

    public AccountFragment() {

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
        view = inflater.inflate(R.layout.wallet_account_fragment, container, false);
        new AccountPresenterImpl(this);
        unbinder = ButterKnife.bind(this, view);
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


    private void initData() {
        tokenVos = new ArrayList<>();
        swipe_refresh.setOnRefreshListener(this);
        Utils.setStatusBar(getActivity(), 1);
        accountTokenList.setFocusable(false);
        swipe_refresh.setColorSchemeResources(R.color.black);
        mAdapter = new AccountAdapter(getActivity());
        walletListView.setAdapter(mAdapter);

        mTokenAdapter = new AccountTokenAdapter(getActivity(), tokenVos, this);
        accountTokenList.setAdapter(mTokenAdapter);

        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), mDrawerLayout, R.string.drawer_open, R.string.drawer_close);
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
        filter.addAction(Constants.WALLET_REFRESH_MAPPING);//trans
        filter.addAction(Constants.ACTION_GESTURE_LOGIN);//trans
        filter.addAction(XmppAction.ACTION_TRANS);//trans
        filter.addAction(Constants.WALLET_BIND_TOKEN);
        filter.addAction(Constants.WALLET_UPDATE_NAME);
        if (getActivity() != null){
            getActivity().registerReceiver(mBroadcastReceiver, filter);
        }

        int priceUnit = MySharedPrefs.readIntDefaultUsd(getActivity(), MySharedPrefs.FILE_USER, MySharedPrefs.KEY_TOKEN_PRICE_UNIT);//0 default  1 usd
        if (priceUnit == 0) {
            changeTokenUnit.setImageResource(R.drawable.icon_unit_cny);
        } else {
            changeTokenUnit.setImageResource(R.drawable.icon_unit_usd);
        }

        boolean isFirstShowWindow = MySharedPrefs.readBoolean(getActivity(), MySharedPrefs.FILE_USER, MySharedPrefs.KEY_FIRST_WALLET_SHOW_WINDOW);
        if (isFirstShowWindow) {
            MySharedPrefs.writeBoolean(getActivity(), MySharedPrefs.FILE_USER, MySharedPrefs.KEY_FIRST_WALLET_SHOW_WINDOW, false);
            initWindowPop();
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                switch (intent.getAction()){
                    case Constants.WALLET_REFRESH_DEL:
                    case Constants.WALLET_REFRESH_BACKUP:
                    case Constants.WALLET_REFRESH_GESTURE:
                    case Constants.WALLET_SUCCESS:
                    case Constants.ACTION_GESTURE_LOGIN:
                    case Constants.WALLET_UPDATE_NAME:
                        initWalletInfo();
                        break;
                    case XmppAction.ACTION_TRANS:
                        loadData(false);
                        break;
                    case Constants.WALLET_REFRESH_SHOW_HINT:
                        if (mDrawerLayout != null) {
                            mDrawerLayout.closeDrawer(GravityCompat.END);
                        }
                        initHomePop();
                        mPresenter.showPowTimer();
                        break;
                    case Constants.WALLET_BIND_TOKEN:
                        tokenVos = FinalUserDataBase.getInstance().getOpenTokenList(walletAddress.getText().toString());
                        mTokenAdapter.resetSource(tokenVos,"1");
                        break;
                    case Constants.WALLET_REFRESH_MAPPING:
                        mTokenAdapter.resetSource(tokenVos,"0");
                        break;
                }
            }
        }
    };


    /**
     * Initialize the Pop layo ut
     */
    private void initHomePop() {
        if (getActivity() == null){
            return;
        }
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.account_more_popup_layout, null);
        homePop = new PopupWindow(view, Utils.dip2px(getActivity(), 160), LinearLayout.LayoutParams.WRAP_CONTENT);
        homePop.setBackgroundDrawable(new BitmapDrawable());
        homePop.setOutsideTouchable(true);
        homePop.setFocusable(true);
        homePopBody = view.findViewById(R.id.txt_home_pop_1);
        homePopBody.setOnClickListener(this);
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
        if (getActivity() == null){
            return;
        }
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.eth_more_popup_layout, null);
        ethPop = new PopupWindow(view, Utils.dip2px(getActivity(), 320), LinearLayout.LayoutParams.WRAP_CONTENT);
        ethPop.setBackgroundDrawable(new BitmapDrawable());
        ethPop.setOutsideTouchable(true);
        ethPop.setFocusable(true);
        if (ethPop.isShowing()) {
            ethPop.dismiss();
        } else {
            boolean isChinese = mPresenter.checkLanguage();
            if (isChinese) {
                ethPop.showAsDropDown(ethWarningImg, Utils.dip2px(getActivity(), -25), Utils.dip2px(getActivity(), -265));
            } else {
                ethPop.showAsDropDown(ethWarningImg, Utils.dip2px(getActivity(), -25), Utils.dip2px(getActivity(), -326));
            }

        }
    }

    /**
     * Initialize the Pop layout
     */
    private void initWindowPop() {
        if (getActivity() == null){
            return;
        }
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.eth_window_popup_layout, null);
        windowPop = new PopupWindow(view, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        windowPop.setBackgroundDrawable(new BitmapDrawable());
        windowPop.setOutsideTouchable(true);
        windowPop.setFocusable(true);
        ethWindow = view.findViewById(R.id.ethWindow);
        ethWindow.setOnClickListener(this);
        boolean isChinese = mPresenter.checkLanguage();
        if (isChinese) {
            ethWindow.setBackgroundResource(R.drawable.icon_eth_window_zh);
        } else {
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
        if (getActivity() != null){
            getActivity().unregisterReceiver(mBroadcastReceiver);
        }
        LoginUtil.getInstance().destory();
        mPresenter.cancelTimer();
        mPresenter.cancelCnyTimer();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onResume() {
        super.onResume();
        setWalletGesture();
    }

    /**
     * set wallet gesture  open or close
     */
    private void setWalletGesture() {
        if (mPresenter.setWalletGesture()){
            isChecked = true;
            walletGesture.setText(getString(R.string.gesture_wallet_open));
        }else{
            isChecked = false;
            walletGesture.setText(getString(R.string.gesture_wallet_close));
        }
    }

    @Nullable
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.txt_home_pop_1://Open the sidebar
                dismissHomePop();
                mPresenter.cancelTimer();
                break;
            case R.id.ethWindow:
                if (windowPop != null && windowPop.isShowing()) {
                    windowPop.dismiss();
                    windowPop = null;
                }
                boolean isFirstShowGif = MySharedPrefs.readBoolean(getActivity(), MySharedPrefs.FILE_USER, MySharedPrefs.KEY_FIRST_WALLET_SHOW_GIF);
                if (isFirstShowGif) {
                    mPresenter.showCnyTimer();
                    currencyBg.setVisibility(View.VISIBLE);
                    MySharedPrefs.writeBoolean(getActivity(), MySharedPrefs.FILE_USER, MySharedPrefs.KEY_FIRST_WALLET_SHOW_GIF, false);
                    cnyUsdChangeGif.setImageResource(R.drawable.cny_usd_change);
                    if (cnyUsdChangeGif.getDrawable() instanceof AnimationDrawable) {
                        ((AnimationDrawable) cnyUsdChangeGif.getDrawable()).start();
                        ((AnimationDrawable) cnyUsdChangeGif.getDrawable()).setOneShot(false);
                    }
                } else {
                    currencyBg.setVisibility(View.GONE);
                }
                break;
        }
    }

    @OnClick({R.id.walletImg,R.id.walletManager,R.id.walletGesture,R.id.createWallet,R.id.showQuicMark,
            R.id.changeTokenUnit,R.id.walletNameBody,R.id.walletAddress,R.id.walletBalanceAdd,R.id.oldWallet,R.id.oldWalletImg,
            R.id.ethWarningImg})
    public void onViewClick(View v) {
        switch (v.getId()) {
            case R.id.walletImg://Open the sidebar
                mDrawerLayout.openDrawer(GravityCompat.END);
                dismissHomePop();
                mPresenter.cancelTimer();
                break;
            case R.id.walletManager://Account management
                mDrawerLayout.closeDrawer(GravityCompat.END);
                dismissHomePop();
                mPresenter.cancelTimer();
                startActivity(new Intent(getActivity(), ManagerWalletActivity.class));
                Utils.openNewActivityAnim(getActivity(), false);
                break;
            case R.id.walletGesture://Account management
                if (mPresenter.setWalletGesture()) {
                    Intent intent = new Intent(getActivity(), GestureLoginActivity.class);
                    intent.putExtra("type", isChecked ? 2 : 1);
                    startActivity(intent);
                    isChecked = !isChecked;
                } else {
                    Intent intent = new Intent(getActivity(), CreateGestureActivity.class);
                    startActivity(intent);
                }
                break;
            case R.id.createWallet://Create a wallet
                mDrawerLayout.closeDrawer(GravityCompat.END);
                startActivity(new Intent(getActivity(), WalletCreateActivity.class));
                Utils.openNewActivityAnim(getActivity(), false);
                break;
            case R.id.showQuicMark://Flicking a
                mDrawerLayout.closeDrawer(GravityCompat.END);
                startActivity(new Intent(getActivity(), CaptureActivity.class));
                Utils.openNewActivityAnim(getActivity(), false);
                break;
            case R.id.changeTokenUnit://Flicking a
                mPresenter.cancelCnyTimer();
                MySharedPrefs.writeBoolean(getActivity(), MySharedPrefs.FILE_USER, MySharedPrefs.KEY_FIRST_WALLET_SHOW_GIF, false);
                currencyBg.setVisibility(View.GONE);

                int priceUnit = MySharedPrefs.readIntDefaultUsd(getActivity(), MySharedPrefs.FILE_USER, MySharedPrefs.KEY_TOKEN_PRICE_UNIT);//0 default  1 usd
                if (priceUnit == 0) {
                    if (getActivity() != null){
                        MySharedPrefs.writeInt(getActivity(), MySharedPrefs.FILE_USER, MySharedPrefs.KEY_TOKEN_PRICE_UNIT, 1);
                    }
                    if (TextUtils.isEmpty(usdTotal)) {
                        walletBalanceNum.setText("━");
                    } else {
                        walletBalanceNum.setText(getString(R.string.token_total_usd_price, usdTotal));
                    }

                    changeTokenUnit.setImageResource(R.drawable.icon_unit_usd);
                } else {
                    if (getActivity() != null){
                        MySharedPrefs.writeInt(getActivity(), MySharedPrefs.FILE_USER, MySharedPrefs.KEY_TOKEN_PRICE_UNIT, 0);
                    }
                    if (TextUtils.isEmpty(total)) {
                        walletBalanceNum.setText("━");
                    } else {
                        walletBalanceNum.setText(getString(R.string.token_total_price, total));
                    }
                    changeTokenUnit.setImageResource(R.drawable.icon_unit_cny);
                }
                mTokenAdapter.notifyDataSetChanged();
                break;
            case R.id.walletNameBody://Backup the purse
                if (storableWallet == null) {
                    return;
                }
                Intent copyIntent = new Intent(getActivity(), WalletCopyActivity.class);
                copyIntent.putExtra(Constants.WALLET_INFO, storableWallet);
                copyIntent.putExtra(Constants.WALLET_IMAGE, storableWallet.getWalletImageId());
                copyIntent.putExtra(Constants.WALLET_TYPE, 1);
                startActivity(copyIntent);
                Utils.openNewActivityAnim(getActivity(), false);
                break;
            case R.id.walletAddress://Qr code
                if (storableWallet == null || tokenVos == null) {
                    return;
                }
                if (!storableWallet.isBackup()) {
                    Intent intent = new Intent(getActivity(), AlertActivity.class);
                    intent.putExtra("type", 5);
                    intent.putExtra("strablewallet", storableWallet);
                    startActivity(intent);
                    if (getActivity() != null){
                        getActivity().overridePendingTransition(0, 0);
                    }
                    return;
                }
                Intent qrCodeIntent = new Intent(getActivity(), QuickMarkShowUI.class);
                qrCodeIntent.putExtra("tokenVo", tokenVos.get(0));
                qrCodeIntent.putExtra("address", storableWallet.getPublicKey());
                startActivity(qrCodeIntent);
                Utils.openNewActivityAnim(getActivity(), false);
                break;
            case R.id.walletBalanceAdd:
                Intent tokenListIntent = new Intent(getActivity(), TokenListUI.class);
                tokenListIntent.putExtra("address", walletAddress.getText().toString());
                startActivity(tokenListIntent);
                Utils.openNewActivityAnim(getActivity(), false);
                break;
            case R.id.oldWallet://Copy the address
            case R.id.oldWalletImg://Copy the address
                Intent intent = new Intent(getActivity(), OldAccountUI.class);
                intent.putExtra("strablewallet", storableWallet);
                startActivity(intent);
                Utils.openNewActivityAnim(getActivity(), false);
                break;
            case R.id.ethWarningImg://Copy the address
                initEthereumPop();
                break;
        }
    }

    @OnItemClick(R.id.walletList)
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (getActivity() != null && !WalletStorage.getInstance(getActivity().getApplicationContext()).get().get(position).isSelect()) {
            String address = "";
            for (int i = 0; i < WalletStorage.getInstance(getActivity().getApplicationContext()).get().size(); i++) {
                if (i != position) {
                    WalletStorage.getInstance(getActivity().getApplicationContext()).get().get(i).setSelect(false);
                } else {
                    WalletStorage.getInstance(getActivity().getApplicationContext()).get().get(i).setSelect(true);
                    address = WalletStorage.getInstance(getActivity().getApplicationContext()).get().get(i).getPublicKey();
                }
            }
            initWalletInfo();
            mDrawerLayout.closeDrawer(GravityCompat.END);
            if (!TextUtils.isEmpty(address)) {
                if (!address.startsWith("0x")) {
                    address = "0x" + address;
                }
                boolean hasGetTrans = MySharedPrefs.readBooleanNormal(getActivity(), MySharedPrefs.FILE_USER, MySharedPrefs.KEY_WALLET_ALL_TRANS + address);
                if (!hasGetTrans) {
                    mPresenter.getAllTransactionList(address);
                }
            }
        }
    }

    @Override
    public void setTokenOnItemClick(int position) {
        if (storableWallet == null || tokenVos == null || tokenVos.size() <= 0) {
            return;
        }
        Intent ethIntent = new Intent(getActivity(), WalletSendDetailUI.class);
        ethIntent.putExtra("tokenVo", tokenVos.get(position));
        double smtBalance = 0;
        if (TextUtils.equals(tokenVos.get(0).getTokenSymbol(), getString(R.string.smt))) {
            smtBalance = tokenVos.get(0).getTokenBalance();
        } else {
            for (int i = 0; i < tokenVos.size(); i++) {
                TokenVo tokenVo = tokenVos.get(i);
                if (TextUtils.equals(tokenVo.getTokenSymbol(), getString(R.string.smt))) {
                    smtBalance = tokenVo.getTokenBalance();
                    break;
                }
            }
        }
        ethIntent.putExtra("smtBalance", smtBalance);
        ethIntent.putExtra("storableWallet", storableWallet);
        startActivity(ethIntent);
        Utils.openNewActivityAnim(getActivity(), false);
    }

    @Override
    public void tokenMapping() {
        mPresenter.getMappingInfo(walletAddress.getText().toString());
    }

    /**
     * Load or refresh the wallet information
     */
    private void initWalletInfo() {
        storableWallet = mPresenter.getStorableWallet();
        if (storableWallet != null) {
            if (getActivity() != null){
                walletImg.setImageResource(Utils.getWalletImageId(getActivity(), storableWallet.getWalletImageId()));
            }
            walletName.setText(storableWallet.getWalletName());
            if (storableWallet.isBackup()) {
                walletBackup.setVisibility(View.GONE);
            } else {
                walletBackup.setVisibility(View.VISIBLE);
            }

            String address = storableWallet.getPublicKey();
            if (!address.startsWith("0x")) {
                address = "0x" + address;
            }
            walletAddress.setText(address);
        }

        mAdapter.resetSource(WalletStorage.getInstance(NextApplication.mContext).get());
        new Handler().postDelayed(new Runnable() {
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
     */
    private void loadData(final boolean isShowToast) {
        boolean isFirstGet = MySharedPrefs.readBoolean(getActivity(), MySharedPrefs.FILE_USER, MySharedPrefs.KEY_FIRST_GET_TOKEN_LIST + walletAddress.getText().toString());
        tokenVos = FinalUserDataBase.getInstance().getOpenTokenList(walletAddress.getText().toString());
        if (!isFirstGet && tokenVos != null && tokenVos.size() > 0) {
            mTokenAdapter.resetSource(tokenVos,"1");
            mPresenter.getBalance(tokenVos,walletAddress.getText().toString(),isShowToast);
        } else {
            if (tokenVos == null){
                tokenVos = new ArrayList<>();
            }
            mPresenter.getTokenList(tokenVos,walletAddress.getText().toString(),isShowToast);
        }
    }


    @Override
    public void setPresenter(AccountContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void getTokenListSuccess(ArrayList<TokenVo> tokens,boolean isShowToast) {
        this.tokenVos = tokens;
        mTokenAdapter.resetSource(tokenVos,"1");
        mPresenter.getBalance(tokenVos,walletAddress.getText().toString(),isShowToast);
    }

    @Override
    public void getTokenListError(boolean isShowToast) {
        mPresenter.getBalance(tokenVos,walletAddress.getText().toString(),isShowToast);
    }

    @Override
    public void getBalanceSuccess(ArrayList<TokenVo> tokens,String total,String usdTotal,String isMapping) {
        this.tokenVos = tokens;
        this.total = total;
        this.usdTotal = usdTotal;
        mTokenAdapter.resetSource(tokenVos,isMapping);
        swipe_refresh.setRefreshing(false);
        int priceUnit = MySharedPrefs.readIntDefaultUsd(getActivity(), MySharedPrefs.FILE_USER, MySharedPrefs.KEY_TOKEN_PRICE_UNIT);//0 default  1 usd
        if (priceUnit == 0) {
            if (TextUtils.isEmpty(total)) {
                walletBalanceNum.setText("━");
            } else {
                walletBalanceNum.setText(getString(R.string.token_total_price, total));
            }
            changeTokenUnit.setImageResource(R.drawable.icon_unit_cny);
        } else {
            if (TextUtils.isEmpty(usdTotal)) {
                walletBalanceNum.setText("━");
            } else {
                walletBalanceNum.setText(getString(R.string.token_total_usd_price, usdTotal));
            }
            changeTokenUnit.setImageResource(R.drawable.icon_unit_usd);
        }
    }

    @Override
    public void getBalanceError(int errorCode, String errorMsg,boolean isShowToast,ArrayList<TokenVo> tokens) {
        if (isShowToast) {
            showToast(errorMsg);
        }
        mTokenAdapter.resetSource(tokenVos,"1");
        swipe_refresh.setRefreshing(false);
    }

    @Override
    public void cancelHomePop() {
        mHandler.sendEmptyMessage(2);
    }

    @Override
    public void cancelCnyView() {
        mHandler.sendEmptyMessage(3);
    }


    @Override
    public void getMappingInfoStart() {
        LoadingDialog.show(getActivity(),"");
    }

    @Override
    public void getMappingInfoSuccess(String balance ,String url) {
        LoadingDialog.close();
        Intent intent = new Intent(getActivity(), AlertActivity.class);
        intent.putExtra("type", 6);
        intent.putExtra("smtBalance", balance);
        intent.putExtra("title", getString(R.string.mapping_instructions));
        intent.putExtra("url", url);
        intent.putExtra("address", walletAddress.getText().toString());
        startActivity(intent);
        if (getActivity() != null){
            getActivity().overridePendingTransition(0, 0);
        }
    }

    @Override
    public void getMappingInfoError(int errorCode, String errorMsg) {
        LoadingDialog.close();
        showToast(errorMsg);
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2:
                    dismissHomePop();
                    break;
                case 3:
                    MySharedPrefs.writeBoolean(getActivity(), MySharedPrefs.FILE_USER, MySharedPrefs.KEY_FIRST_WALLET_SHOW_GIF, false);
                    currencyBg.setVisibility(View.GONE);
                    break;
            }
        }
    };

}
