package com.lingtuan.firefly.wallet.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseFragment;
import com.lingtuan.firefly.login.LoginUtil;
import com.lingtuan.firefly.setting.GesturePasswordLoginActivity;
import com.lingtuan.firefly.ui.MainFragmentUI;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.wallet.WalletCreateActivity;
import com.lingtuan.firefly.wallet.WalletImportActivity;
import com.lingtuan.firefly.wallet.util.WalletStorage;


/**
 * There is no wallet
 * */
public class NewWalletFragment extends BaseFragment implements View.OnClickListener {

    /**
     * root view
     */
    private View view = null;
    private boolean isDataFirstLoaded;

    private TextView createWallet,importWallet,walletModeLogin;//Create a wallet, import wallet
    private TextView title;
    private RelativeLayout appTitleBg;

    private boolean  showAnimation;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        isDataFirstLoaded = true;
        super.onCreate(savedInstanceState);
        setRetainInstance(false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        showAnimation = ((MainFragmentUI)context).getShowAnimation();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (!isDataFirstLoaded && view != null) {
            ((ViewGroup) view.getParent()).removeView(view);
            return view;
        }
        view = inflater.inflate(R.layout.wallet_new_activity,container,false);
        findViewById();
        setListener();
        initData();
        return view;
    }


    protected void findViewById() {
        appTitleBg = (RelativeLayout) view.findViewById(R.id.app_title_rela);
        createWallet = (TextView) view.findViewById(R.id.createWallet);
        importWallet = (TextView) view.findViewById(R.id.importWallet);
        walletModeLogin = (TextView) view.findViewById(R.id.wallet_mode_login);
        title = (TextView) view.findViewById(R.id.app_title);

        if (showAnimation){
            appTitleBg.setVisibility(View.GONE);
            Animation transanim = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    appTitleBg.setVisibility(View.VISIBLE);
                    FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) appTitleBg.getLayoutParams();
                    lp.setMargins(0, - (int) (Utils.dip2px(getActivity(), 48) * (1 - interpolatedTime) ), 0, 0 );
                    appTitleBg.requestLayout();
                }
                @Override
                public boolean willChangeBounds() {
                    return true;
                }
            };
            transanim.setDuration(1000);
            appTitleBg.startAnimation(transanim);
        }
    }

    protected void setListener() {
        createWallet.setOnClickListener(this);
        importWallet.setOnClickListener(this);
        walletModeLogin.setOnClickListener(this);
    }


    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.createWallet://Create a wallet
                startActivityForResult(new Intent(getActivity(),WalletCreateActivity.class),100);
                Utils.openNewActivityAnim(getActivity(),false);
                break;
            case R.id.importWallet://Import the wallet
                startActivityForResult(new Intent(getActivity(),WalletImportActivity.class),100);
                Utils.openNewActivityAnim(getActivity(),false);
                break;
            case R.id.wallet_mode_login://wallet login
                Intent intent = new Intent(getActivity(),GesturePasswordLoginActivity.class);
                intent.putExtra("type",3);
                startActivity(intent);
                Utils.openNewActivityAnim(getActivity(),false);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected void initData() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.CHANGE_LANGUAGE);//Update language refresh the page
        getActivity().registerReceiver(mBroadcastReceiver, filter);
        view.findViewById(R.id.app_back).setVisibility(View.GONE);
        title.setText(getString(R.string.app_name));
        int walletMode = MySharedPrefs.readInt(getActivity(), MySharedPrefs.FILE_USER, MySharedPrefs.KEY_IS_WALLET_PATTERN);
        if (walletMode != 0 && NextApplication.myInfo == null && WalletStorage.getInstance(getActivity().getApplicationContext()).get().size()>0){
            walletModeLogin.setVisibility(View.VISIBLE);
        }else{
            walletModeLogin.setVisibility(View.GONE);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden && walletModeLogin != null){
            int walletMode = MySharedPrefs.readInt(getActivity(), MySharedPrefs.FILE_USER, MySharedPrefs.KEY_IS_WALLET_PATTERN);
            if (walletMode != 0 && NextApplication.myInfo == null && WalletStorage.getInstance(getActivity().getApplicationContext()).get().size()>0){
                walletModeLogin.setVisibility(View.VISIBLE);
            }else{
                walletModeLogin.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mBroadcastReceiver);
        LoginUtil.getInstance().destory();
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && (Constants.CHANGE_LANGUAGE.equals(intent.getAction()))) {
                Utils.updateViewLanguage(view.findViewById(android.R.id.content));
            }
        }
    };
}
