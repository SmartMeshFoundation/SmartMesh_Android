package com.lingtuan.firefly.wallet.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseFragment;
import com.lingtuan.firefly.ui.MainFragmentUI;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MyToast;
import com.lingtuan.firefly.wallet.WalletHandler;
import com.lingtuan.firefly.wallet.WalletThread;

/**
 * Created  on 2017/8/21.
 * 官方钱包导入 KeyStore
 */

public class WalletOfficalFragment extends BaseFragment implements View.OnClickListener {

    /**
     * 根view
     */
    private View view = null;


    /*keyStore 密码  文本内容*/
    private EditText keyStorePwd;
    /*keyStore 文本内容*/
    private EditText keyStoreInfo;

    private TextView importWallet;

    private WalletHandler mHandler;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.wallet_offical_layout,container,false);
        findViewById();
        setListener();
        initData();
        return view;
    }

    private void findViewById() {
        keyStorePwd = (EditText) view.findViewById(R.id.keyStorePwd);
        keyStoreInfo = (EditText) view.findViewById(R.id.keyStoreInfo);
        importWallet = (TextView) view.findViewById(R.id.importWallet);
    }
    private void setListener() {
        importWallet.setOnClickListener(this);
    }

    private void initData() {
        mHandler = new WalletHandler(getActivity());
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.WALLET_SUCCESS);
        filter.addAction(Constants.WALLET_ERROR);
        filter.addAction(Constants.NO_MEMORY);
        filter.addAction(Constants.WALLET_PWD_ERROR);
        filter.addAction(Constants.WALLET_REPEAT_ERROR);
        getActivity().registerReceiver(mBroadcastReceiver, filter);
    }


    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.importWallet:
                String password = keyStorePwd.getText().toString().trim();
                String source = keyStoreInfo.getText().toString().trim();
                if (TextUtils.isEmpty(source)){
                    MyToast.showToast(getActivity(),getString(R.string.wallet_keystore_empty));
                    return;
                }
                if (TextUtils.isEmpty(password)){
                    MyToast.showToast(getActivity(),getString(R.string.wallet_pwd_empty));
                    return;
                }
                LoadingDialog.show(getActivity(),getString(R.string.wallet_import_ing));
                new WalletThread(mHandler,getActivity().getApplicationContext(),null,password,null,source,2).start();
                break;
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && (Constants.WALLET_SUCCESS.equals(intent.getAction()))) {
                LoadingDialog.close();
                MyToast.showToast(getActivity(),getString(R.string.notification_wallimp_finished));
                startActivity(new Intent(getActivity(),MainFragmentUI.class));
                getActivity().setResult(Activity.RESULT_OK);
                getActivity().finish();
            }else if (intent != null && (Constants.WALLET_ERROR.equals(intent.getAction()))) {
                LoadingDialog.close();
                MyToast.showToast(getActivity(),getString(R.string.notification_wallimp_failure));
            }else if (intent != null && (Constants.WALLET_PWD_ERROR.equals(intent.getAction()))) {
                LoadingDialog.close();
                MyToast.showToast(getActivity(),getString(R.string.wallet_copy_pwd_error));
            }else if (intent != null && (Constants.WALLET_REPEAT_ERROR.equals(intent.getAction()))) {
                LoadingDialog.close();
                MyToast.showToast(getActivity(),getString(R.string.notification_wallimp_repeat));
            }
            else if (intent != null && (Constants.NO_MEMORY.equals(intent.getAction()))) {
                LoadingDialog.close();
                MyToast.showToast(getActivity(),getString(R.string.notification_wallgen_no_memory));
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mBroadcastReceiver);
    }

}
