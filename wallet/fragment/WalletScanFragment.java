package com.lingtuan.firefly.wallet.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.quickmark.CaptureActivity;
import com.lingtuan.firefly.ui.MainFragmentUI;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.MyToast;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.wallet.util.WalletStorage;
import com.lingtuan.firefly.wallet.vo.StorableWallet;

/**
 * Created on 2017/8/21.
 */

public class WalletScanFragment extends Fragment implements View.OnClickListener {

   
    private View view = null;
    private EditText walletAddress;
    private TextView importWallet;
    private ImageView walletQrImg;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.wallet_scan_layout,container,false);
        findViewById();
        setListener();
        initData();
        return view;
    }

    private void findViewById() {
        walletAddress = (EditText) view.findViewById(R.id.walletAddress);
        importWallet = (TextView) view.findViewById(R.id.importWallet);
        walletQrImg = (ImageView) view.findViewById(R.id.walletQrImg);
    }
    private void setListener() {
        importWallet.setOnClickListener(this);
        walletQrImg.setOnClickListener(this);
    }

    private void initData() {
    }

    
    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.importWallet:
                String address = walletAddress.getText().toString();
                if (TextUtils.isEmpty(address) || address.length() != 42 || !address.startsWith("0x")){
                    MyToast.showToast(getActivity(),getString(R.string.wallet_scan_warning));
                    return;
                }
                address = address.substring(2);
                boolean exists = WalletStorage.getInstance(getActivity().getApplicationContext()).checkExists(address);
                if (!exists){

                    StorableWallet storableWallet = new StorableWallet();
                    storableWallet.setPublicKey(address);
                    storableWallet.setWalletType(1);
                    storableWallet.setWalletName(Utils.getWalletName(getActivity()));
                    if (WalletStorage.getInstance(getActivity().getApplicationContext()).get().size() <= 0){
                        storableWallet.setSelect(true);
                    }
                    WalletStorage.getInstance(getActivity().getApplicationContext()).add(storableWallet,getActivity());
                    MyToast.showToast(getActivity(),getString(R.string.notification_wallimp_finished));

                    Utils.sendBroadcastReceiver(getActivity(), new Intent(Constants.WALLET_SUCCESS), false);
                    startActivity(new Intent(getActivity(),MainFragmentUI.class));
                    getActivity().setResult(Activity.RESULT_OK);
                    getActivity().finish();
                }else{
                    MyToast.showToast(getActivity(),getString(R.string.notification_wallimp_repeat));
                }
                break;
            case R.id.walletQrImg:
                Intent i = new Intent(getActivity(),CaptureActivity.class);
                i.putExtra("type",2);
                startActivityForResult(i,100);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK){
            String address = data.getStringExtra("address");
            walletAddress.setText(address);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
