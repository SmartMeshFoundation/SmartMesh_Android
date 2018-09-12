package com.lingtuan.firefly.wallet;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.custom.flowtag.FlowTagLayout;
import com.lingtuan.firefly.custom.flowtag.OnTagSelectListener;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.MyViewDialogFragment;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.wallet.vo.MnemonicSelectVo;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created on 2017/8/22.
 * According to the private key plaintext
 * {@link WalletCopyActivity}
 */

public class WalletCopyMnemonicNextUI extends BaseActivity{

    @BindView(R.id.walletCopyPrivateKey)
    TextView walletCopyPrivateKey;
    @BindView(R.id.mnemonicAddFlowTag)
    FlowTagLayout mnemonicAddFlowTag;

    private MnemonicCopyNextAdapter  mMnemonicAdapter;

    private ArrayList<MnemonicSelectVo> mnemonicList;

    private String mnemonic;

    @Override
    protected void setContentView() {
        setContentView(R.layout.wallet_copy_mnemonic_next_layout);
        getPassData();
    }

    @Override
    protected void findViewById() {

    }

    @Override
    protected void setListener() {

    }

    private void getPassData() {
        mnemonic = getIntent().getStringExtra(Constants.MNEMONIC);
    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.wallet_show_mnemonic_2));
        if (!TextUtils.isEmpty(mnemonic)){
            mnemonicList = new ArrayList<>();
            String[] mnemonics = mnemonic.split(" ");
            MnemonicSelectVo mnemonicSelectVo;
            for (int i = 0 ; i < mnemonics.length ; i++){
                mnemonicSelectVo = new MnemonicSelectVo();
                mnemonicSelectVo.setHasSelected(false);
                mnemonicSelectVo.setMnemonic(mnemonics[i]);
                mnemonicList.add(mnemonicSelectVo);
            }
        }

        mMnemonicAdapter = new MnemonicCopyNextAdapter(this);
        mnemonicAddFlowTag.setAdapter(mMnemonicAdapter,false);


        mMnemonicAdapter.loadData(mnemonicList);

        mnemonicAddFlowTag.setOnTagSelectListener(new OnTagSelectListener() {
            @Override
            public void onItemSelect(FlowTagLayout parent, MnemonicSelectVo tempMnemonic) {
                for (int j = 0 ; j < mnemonicList.size() ; j++){
                    if (mnemonicList.get(j).getMnemonic().equals(tempMnemonic.getMnemonic())){
                        mnemonicList.get(j).setHasSelected(true);
                    }
                }
                mMnemonicAdapter.reloadAll(mnemonicList);
            }
        });

    }

    @OnClick(R.id.walletCopyPrivateKey)
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.walletCopyPrivateKey:
                Intent intent = new Intent(this,WalletMnemonicFinishUI.class);
                intent.putExtra(Constants.MNEMONIC,mnemonic);
                startActivity(intent);
                Utils.openNewActivityAnim(this,true);
                break;
            default:
                super.onClick(v);
        }
    }

    private void deleteMnemonic(){
        MyViewDialogFragment mdf = new MyViewDialogFragment();
        mdf.setTitleAndContentText(getString(R.string.wallet_export_prompt),getString(R.string.wallet_export_mnemonic_remove));
        mdf.setOkCallback(new MyViewDialogFragment.OkCallback() {
            @Override
            public void okBtn() {
                copyMnemonic();
            }
        });
        mdf.show(getSupportFragmentManager(), "mdf");
    }

    private void copyMnemonic(){
        Utils.copyText(WalletCopyMnemonicNextUI.this,mnemonic);
        walletCopyPrivateKey.setText(getString(R.string.wallet_show_mnemonic_3));
        Utils.sendBroadcastReceiver(WalletCopyMnemonicNextUI.this, new Intent(Constants.WALLET_REFRESH_MNEMONIC), false);
    }
}
