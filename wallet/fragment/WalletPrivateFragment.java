package com.lingtuan.firefly.wallet.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.ui.MainFragmentUI;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MyToast;
import com.lingtuan.firefly.wallet.WalletHandler;
import com.lingtuan.firefly.wallet.WalletThread;

/**
 * Created on 2017/8/21.
 * 私钥导入钱包
 */

public class WalletPrivateFragment extends Fragment implements View.OnClickListener {

    /**
     * 根view
     */
    private View view = null;


    private EditText walletPwd;//钱包密吗
    private EditText walletAgainPwd;//再次输入钱包密吗
    private EditText walletPwdInfo;//密码提示信息

    private EditText keyStoreInfo;

    private WalletHandler mHandler;

    private TextView importWallet;//导入钱包


    //清除钱包名称  显示密码
    private ImageView isShowPass;

    /**
     * 显示密码
     */
    boolean isShowPassWorld = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.wallet_private_layout,container,false);
        findViewById();
        setListener();
        initData();
        return view;
    }

    private void findViewById() {
        walletPwd = (EditText) view.findViewById(R.id.walletPwd);
        walletAgainPwd = (EditText) view.findViewById(R.id.walletAgainPwd);
        walletPwdInfo = (EditText) view.findViewById(R.id.walletPwdInfo);
        keyStoreInfo = (EditText) view.findViewById(R.id.keyStoreInfo);
        importWallet = (TextView) view.findViewById(R.id.importWallet);
        isShowPass = (ImageView) view.findViewById(R.id.isShowPass);
    }
    private void setListener() {
        importWallet.setOnClickListener(this);
        isShowPass.setOnClickListener(this);
    }

    private void initData() {
        mHandler = new WalletHandler(getActivity());
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.WALLET_SUCCESS);
        filter.addAction(Constants.WALLET_ERROR);
        filter.addAction(Constants.NO_MEMORY);
        filter.addAction(Constants.WALLET_REPEAT_ERROR);
        getActivity().registerReceiver(mBroadcastReceiver, filter);
    }

    //导入钱包 如何导入钱包
    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.isShowPass:
                isShowPassWorld = !isShowPassWorld;
                if (isShowPassWorld) { /* 设定EditText的内容为可见的 */
                    walletPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    isShowPass.setImageResource(R.drawable.eye_open);
                } else {/* 设定EditText的内容为隐藏*/
                    walletPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    isShowPass.setImageResource(R.drawable.eye_close);
                }
                break;
            case R.id.importWallet:
                if (walletPwd.length() > 16 || walletPwd.length() < 6){
                    MyToast.showToast(getActivity(),getString(R.string.wallet_pwd_warning));
                    return;
                }
                String password = walletPwd.getText().toString().trim();
                String pwdInfo = walletPwdInfo.getText().toString().trim();
                String source = keyStoreInfo.getText().toString().trim();
                if (TextUtils.isEmpty(source)){
                    MyToast.showToast(getActivity(),getString(R.string.wallet_private_key_empty));
                    return;
                }
                if (TextUtils.equals(password,walletAgainPwd.getText().toString().trim())){
                    LoadingDialog.show(getActivity(),getString(R.string.wallet_import_ing));
                    new WalletThread(mHandler,getActivity().getApplicationContext(),null,password,pwdInfo,source,1).start();
                }else{
                    MyToast.showToast(getActivity(),getString(R.string.account_pwd_again_warning));
                }
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
