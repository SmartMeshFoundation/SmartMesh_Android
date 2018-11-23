package com.lingtuan.firefly.spectrum.fragment;

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
import com.lingtuan.firefly.spectrum.WalletHandler;
import com.lingtuan.firefly.spectrum.WalletThread;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MyToast;

import org.web3j.crypto.MnemonicUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created on 2017/8/21.
 * The private key import the purse
 */

public class WalletMnemonicFragment extends Fragment implements View.OnClickListener {

    /**
     * root view
     */
    private View view = null;
    private Unbinder unbinder;
    @BindView(R.id.walletPwd)
    EditText walletPwd;//The wallet password
    @BindView(R.id.walletAgainPwd)
    EditText walletAgainPwd;//The wallet password again
    @BindView(R.id.walletPwdInfo)
    EditText walletPwdInfo;//Password prompt information
    @BindView(R.id.keyStoreInfo)
    EditText keyStoreInfo;
    //Remove the wallet name Show the password
    @BindView(R.id.isShowPass)
    ImageView isShowPass;

    private WalletHandler mHandler;

    /**
     * Show the password
     */
    boolean isShowPassWorld = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.wallet_mnemonic_layout,container,false);
        unbinder = ButterKnife.bind(this,view);
        initData();
        return view;
    }

    private void initData() {
        mHandler = new WalletHandler(getActivity());
    }

    //Import the purse, how to import the wallet
    @OnClick({R.id.isShowPass,R.id.importWallet})
    public void onClick(View v){
        switch (v.getId()){
            case R.id.isShowPass:
                isShowPassWorld = !isShowPassWorld;
                if (isShowPassWorld) { /*Set the EditText content is visible */
                    walletPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    isShowPass.setImageResource(R.drawable.eye_open);
                } else {/* The content of the EditText set as hidden*/
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
                    MyToast.showToast(getActivity(),getString(R.string.wallet_mnemonic_empty));
                    return;
                }
                if (TextUtils.equals(password,walletAgainPwd.getText().toString().trim())){
                    LoadingDialog.show(getActivity(),getString(R.string.wallet_import_ing));
                    getPrivateKey(source,password,pwdInfo);
                }else{
                    MyToast.showToast(getActivity(),getString(R.string.account_pwd_again_warning));
                }
                break;
        }
    }

    public void getPrivateKey(final String mnemonic,final String password,final String pwdInfo){
        try {
            if (TextUtils.isEmpty(mnemonic) || !MnemonicUtils.validateMnemonic(mnemonic)){
                MyToast.showToast(getActivity(),getString(R.string.wallet_mnemonic_13));
                LoadingDialog.close();
                return;
            }
            if (getActivity() != null){
                WalletThread walletThread =  new WalletThread(mHandler,getActivity().getApplicationContext(),null,password,pwdInfo,null,3,false);
                walletThread.setMnemonic(mnemonic);
                walletThread.start();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
