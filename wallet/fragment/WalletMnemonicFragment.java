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

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.ui.MainFragmentUI;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MyToast;
import com.lingtuan.firefly.wallet.WalletHandler;

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
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.WALLET_SUCCESS);
        filter.addAction(Constants.WALLET_ERROR);
        filter.addAction(Constants.NO_MEMORY);
        filter.addAction(Constants.WALLET_REPEAT_ERROR);
        getActivity().registerReceiver(mBroadcastReceiver, filter);
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
                    MyToast.showToast(getActivity(),getString(R.string.wallet_private_key_empty));
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

    public void getPrivateKey(final String seedCode,final String password,final String pwdInfo){
//        new Thread(new Runnable() {
//           @Override
//           public void run() {
//                try {
//                    long creationTimeSeconds = System.currentTimeMillis() / 1000;
//                    DeterministicSeed seed = new DeterministicSeed(Arrays.asList(seedCode.split(" ")), null, "", creationTimeSeconds);
//                    DeterministicKeyChain chain = DeterministicKeyChain.builder().seed(seed).build();
//                    List<ChildNumber> keyPath = HDUtils.parsePath("M/44H/60H/0H/0/0");
//                    DeterministicKey key = chain.getKeyByPath(keyPath, true);
//                    BigInteger tempPrivateKey = key.getPrivKey();
//                    String privateKey = tempPrivateKey.toString(16);
//                    new WalletThread(mHandler,getActivity().getApplicationContext(),null,password,pwdInfo,privateKey,1,false).start();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//           }
//       }).start();
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
