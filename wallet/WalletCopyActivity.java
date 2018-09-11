package com.lingtuan.firefly.wallet;

import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.wallet.vo.MnemonicVo;
import com.lingtuan.firefly.wallet.vo.StorableWallet;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created on 2017/8/22.
 * Backup account
 */

public class WalletCopyActivity extends BaseActivity {

    @BindView(R.id.app_btn_right)
    TextView appBtnRight;
    @BindView(R.id.success)
    TextView success;
    @BindView(R.id.address)
    TextView address;
    @BindView(R.id.walletCopyName)
    EditText walletCopyName;
    @BindView(R.id.walletCopyPwdInfo)
    TextView walletCopyPwdInfo;
    @BindView(R.id.walletCopyPwdInfoLine)
    View walletCopyPwdInfoLine;
    @BindView(R.id.walletCopyKey)
    TextView walletCopyKey;
    @BindView(R.id.walletCopyKeyStore)
    TextView walletCopyKeyStore;
    @BindView(R.id.walletMnemonic)
    TextView walletMnemonic;
    @BindView(R.id.icon)
    ImageView icon;

    private WalletCopyPresenter walletCopyPresenter;

    private StorableWallet storableWallet;
    private String iconId;
    private int type;//0 the newly created, 1 backup wallet

    @Override
    protected void setContentView() {
        setContentView(R.layout.wallet_copy_layout);
    }

    @Override
    protected void findViewById() {
        Utils.setStatusBar(WalletCopyActivity.this, 1);
        storableWallet = (StorableWallet) getIntent().getSerializableExtra(Constants.WALLET_INFO);
        iconId = getIntent().getStringExtra(Constants.WALLET_IMAGE);
        type = getIntent().getIntExtra(Constants.WALLET_TYPE, 0);
        walletCopyPresenter = new WalletCopyPresenter(WalletCopyActivity.this);
        walletCopyPresenter.getPassData(storableWallet,iconId,type);
        walletCopyPresenter.initView(walletCopyName,address);
    }

    @Override
    protected void setListener() {

    }


    @Override
    protected void initData() {
        setTitle(getString(R.string.wallet_copy));
        walletCopyPresenter.initData(appBtnRight,walletCopyPwdInfo,walletCopyPwdInfoLine,icon,success,walletCopyKey,walletCopyKeyStore);
    }

    @Override
    protected void onResume() {
        super.onResume();
        walletCopyPresenter.onResume();
        MnemonicVo mnemonicVo = FinalUserDataBase.getInstance().getMnemonic(address.getText().toString().trim());
        if (mnemonicVo != null && !TextUtils.isEmpty(mnemonicVo.getMnemonic())){
            walletMnemonic.setVisibility(View.VISIBLE);
        }else{
            walletMnemonic.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MySharedPrefs.writeBoolean(WalletCopyActivity.this, MySharedPrefs.FILE_USER, MySharedPrefs.IS_SHOW_WALLET_DIALOG, false);
        walletCopyPresenter.onDestroy();
    }

    /**
     * to export the private key
     * export KeyStore
     * to delete the wallet
     */
    @OnClick({R.id.walletCopyKey,R.id.walletMnemonic,R.id.walletCopyKeyStore,R.id.walletDelete,R.id.app_btn_right})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.walletCopyKey:
                walletCopyPresenter.showPwdDialog(0);
                break;
            case R.id.walletMnemonic:
                walletCopyPresenter.showPwdDialog(3);
                break;
            case R.id.walletCopyKeyStore:
                walletCopyPresenter.showPwdDialog(1);
                break;
            case R.id.walletDelete:
                if (storableWallet.getWalletType() == 1) {
                    walletCopyPresenter.showDelSacnWalletDialog();
                } else {
                    walletCopyPresenter.showPwdDialog(2);
                }
                break;
            case R.id.app_btn_right:
                walletCopyPresenter.updateWalletName();
                break;
            default:
                super.onClick(v);
        }
    }
}
