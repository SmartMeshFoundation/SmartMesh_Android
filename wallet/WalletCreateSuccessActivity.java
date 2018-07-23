package com.lingtuan.firefly.wallet;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.ui.MainFragmentUI;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.wallet.util.WalletStorage;
import com.lingtuan.firefly.wallet.vo.StorableWallet;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created on 2017/8/22.
 * The purse to create success page
 */

public class WalletCreateSuccessActivity extends BaseActivity {


    @BindView(R.id.walletImg)
    ImageView walletImg;

    @Override
    protected void setContentView() {
        setContentView(R.layout.wallet_create_success_layout);
    }

    @Override
    protected void findViewById() {

    }

    @Override
    protected void setListener() {

    }

    @OnClick({R.id.walletCopy,R.id.app_back})
    public void onClick(View v){
        switch (v.getId()){
            case R.id.walletCopy:
                String imgId = Utils.getWalletImg(WalletCreateSuccessActivity.this,(WalletStorage.getInstance(getApplicationContext()).get().size()-1));
                Intent copyIntent = new Intent(WalletCreateSuccessActivity.this,WalletCopyActivity.class);
                ArrayList<StorableWallet> list = WalletStorage.getInstance(getApplicationContext()).get();
                copyIntent.putExtra(Constants.WALLET_INFO,list.get(list.size()-1));
                copyIntent.putExtra(Constants.WALLET_IMAGE, imgId);
                startActivity(copyIntent);
                break;
            case R.id.app_back:
                startActivity(new Intent(WalletCreateSuccessActivity.this,MainFragmentUI.class));
                finish();
                break;
            default:
                super.onClick(v);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(WalletCreateSuccessActivity.this,MainFragmentUI.class));
    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.notification_wallgen_success));
        ArrayList<StorableWallet> list = WalletStorage.getInstance(getApplicationContext()).get();
        String imgId = Utils.getWalletImg(WalletCreateSuccessActivity.this,list.size() - 1);
        StorableWallet storableWallet = list.get(list.size() - 1);
        if (TextUtils.isEmpty(storableWallet.getWalletImageId()) || !storableWallet.getWalletImageId().startsWith("icon_static_")) {
            storableWallet.setWalletImageId(imgId);
        }
        walletImg.setImageResource(Utils.getWalletImageId(WalletCreateSuccessActivity.this,storableWallet.getWalletImageId()));
    }
}
