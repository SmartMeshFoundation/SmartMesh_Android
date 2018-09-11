package com.lingtuan.firefly.wallet;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.custom.flowtag.FlowTagLayout;
import com.lingtuan.firefly.custom.flowtag.OnTagSelectListener;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created on 2017/8/22.
 * According to the private key plaintext
 * {@link WalletCopyActivity}
 */

public class WalletMnemonicActivity extends BaseActivity{

    @BindView(R.id.walletPrivateKey)
    TextView walletPrivateKey;
    @BindView(R.id.walletCopyPrivateKey)
    TextView walletCopyPrivateKey;
    @BindView(R.id.mnemonicFlowTag)
    FlowTagLayout mnemonicFlowTag;

    private MnemonicAdapter  mMnemonicAdapter;

    private ArrayList<String> mnemonicList;

    private String mnemonic;

    @Override
    protected void setContentView() {
        setContentView(R.layout.wallet_show_mnemonic_layout);
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
//        walletPrivateKey.setText(mnemonic);
        if (!TextUtils.isEmpty(mnemonic)){
            mnemonicList = new ArrayList<>();
            String[] mnemonics = mnemonic.split(" ");
            mnemonicList.addAll(Arrays.asList(mnemonics));
        }
        mMnemonicAdapter = new MnemonicAdapter(this);
        mnemonicFlowTag.setTagCheckedMode(FlowTagLayout.FLOW_TAG_CHECKED_MULTI);
        mnemonicFlowTag.setAdapter(mMnemonicAdapter);
        mMnemonicAdapter.onlyAddAll(mnemonicList);
        mnemonicFlowTag.setOnTagSelectListener(new OnTagSelectListener() {
            @Override
            public void onItemSelect(FlowTagLayout parent, List<Integer> selectedList) {
                if (selectedList != null && selectedList.size() > 0) {
                    StringBuilder sb = new StringBuilder();
                    for (int i : selectedList) {
                        sb.append(parent.getAdapter().getItem(i));
                        sb.append(" ");
                    }
                    walletPrivateKey.setText(sb.toString());
                }
            }
        });
    }

    @OnClick(R.id.walletCopyPrivateKey)
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.walletCopyPrivateKey:
                String tempMnemonic = walletPrivateKey.getText().toString().trim();
                if (TextUtils.equals(tempMnemonic,mnemonic.trim())){
                    Utils.copyText(WalletMnemonicActivity.this,mnemonic);
                    walletCopyPrivateKey.setText(getString(R.string.wallet_show_mnemonic_3));
                }else{
                    showToast("助记词顺序不正确");
                }
                break;
            default:
                super.onClick(v);
        }
    }
}
