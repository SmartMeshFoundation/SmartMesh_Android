package com.lingtuan.firefly.wallet;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.custom.flowtag.FlowTagLayout;
import com.lingtuan.firefly.ui.AlertActivity;
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

    private static final int MNEMONIC_RESULT = 0x01;

    private MnemonicCopyNextAdapter  mMnemonicAdapter;

    private ArrayList<MnemonicSelectVo> mnemonicList;

    private String mnemonic;
    private String walletAddress;

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
        walletAddress = getIntent().getStringExtra(Constants.WALLET_ADDRESS);
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
                mnemonicSelectVo.setHasSelected(true);
                mnemonicSelectVo.setMnemonic(mnemonics[i]);
                mnemonicList.add(mnemonicSelectVo);
            }
        }
        mMnemonicAdapter = new MnemonicCopyNextAdapter(this);
        mnemonicAddFlowTag.setAdapter(mMnemonicAdapter,false);
        mMnemonicAdapter.loadData(mnemonicList);
        showMnemonicDialog();
    }

    private void showMnemonicDialog(){
        Intent intent = new Intent(WalletCopyMnemonicNextUI.this, AlertActivity.class);
        intent.putExtra("type", 8);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    @OnClick(R.id.walletCopyPrivateKey)
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.walletCopyPrivateKey:
                Intent intent = new Intent(this,WalletMnemonicFinishUI.class);
                intent.putExtra(Constants.MNEMONIC,mnemonic);
                intent.putExtra(Constants.WALLET_ADDRESS,walletAddress);
                startActivityForResult(intent,MNEMONIC_RESULT);
                Utils.openNewActivityAnim(this,false);
                break;
            case R.id.app_back:
                clearMnemonic();
                break;
            default:
                super.onClick(v);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MNEMONIC_RESULT && resultCode == RESULT_OK){
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        clearMnemonic();
    }

    private void clearMnemonic(){
        MyViewDialogFragment mdf = new MyViewDialogFragment();
        mdf.setTitleAndContentText(getString(R.string.wallet_export_prompt),getString(R.string.wallet_mnemonic_5));
        mdf.setOkCallback(new MyViewDialogFragment.OkCallback() {
            @Override
            public void okBtn() {
                finish();
            }
        });
        mdf.show(getSupportFragmentManager(), "mdf");
    }
}
