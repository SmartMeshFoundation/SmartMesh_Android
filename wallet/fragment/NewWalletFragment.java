package com.lingtuan.firefly.wallet.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.base.BaseFragment;
import com.lingtuan.firefly.login.LoginUtil;
import com.lingtuan.firefly.setting.SecurityUI;
import com.lingtuan.firefly.ui.MainFragmentUI;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.wallet.WalletCreateActivity;
import com.lingtuan.firefly.wallet.WalletImportActivity;


/**
 * There is no wallet
 * */
public class NewWalletFragment extends BaseFragment implements View.OnClickListener {

    /**
     * root view
     */
    private View view = null;
    private boolean isDataFirstLoaded;

    private TextView createWallet,importWallet;//Create a wallet, import wallet
    private TextView title;


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
        view = inflater.inflate(R.layout.wallet_new_activity,container,false);
        findViewById();
        setListener();
        initData();
        return view;
    }



    protected void findViewById() {
        createWallet = (TextView) view.findViewById(R.id.createWallet);
        importWallet = (TextView) view.findViewById(R.id.importWallet);
        title = (TextView) view.findViewById(R.id.app_title);

    }

    protected void setListener() {
        createWallet.setOnClickListener(this);
        importWallet.setOnClickListener(this);
    }


    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.createWallet://Create a wallet
                if (NextApplication.myInfo != null && TextUtils.isEmpty(NextApplication.myInfo.getMid())&& TextUtils.isEmpty(NextApplication.myInfo.getMobile())&& TextUtils.isEmpty(NextApplication.myInfo.getEmail())) {
                    startActivity(new Intent(getActivity(), SecurityUI.class));
                    Utils.openNewActivityAnim(getActivity(),false);
                }else{
                    startActivityForResult(new Intent(getActivity(),WalletCreateActivity.class),100);
                }
                break;
            case R.id.importWallet://Import the wallet
                if (NextApplication.myInfo != null && TextUtils.isEmpty(NextApplication.myInfo.getMid())&& TextUtils.isEmpty(NextApplication.myInfo.getMobile())&& TextUtils.isEmpty(NextApplication.myInfo.getEmail())) {
                    startActivity(new Intent(getActivity(), SecurityUI.class));
                    Utils.openNewActivityAnim(getActivity(),false);
                }else{
                    startActivityForResult(new Intent(getActivity(),WalletImportActivity.class),100);
                }
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
