package com.lingtuan.wallet;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.lingtuan.meshbox.base.BaseActivity;
import com.lingtuan.meshbox.R;

import javax.xml.transform.Result;

import butterknife.BindView;
import butterknife.OnClick;

public class ShowWalletAddressActivity extends BaseActivity<ShowWalletAddressContract.Presenter> implements ShowWalletAddressContract.View {

    @BindView(R.id.smtContent)
    TextView smtContent;

    private String bindWalletAddress;

    @Override
    public int getLayoutId() {
        bindWalletAddress = getIntent().getStringExtra("address");
        return R.layout.activity_show_wallet_address;
    }

    @Override
    public ShowWalletAddressPresenter createPresenter() {
        return new ShowWalletAddressPresenter(this);
    }

    @Override
    protected void initData() {
        mTitle.setText(getResources().getString(R.string.binding_wallet_title_show));
        if (!TextUtils.isEmpty(bindWalletAddress)){
            smtContent.setText(bindWalletAddress);
        }
    }
//
//    @OnClick(R.id.next)
//    public void OnClick(View v){
//        switch (v.getId()){
//            case R.id.next:
//                startActivityForResult(new Intent(this, BindingWalletActivity.class),100);
//                finish();
//                break;
//            default:
//                super.onClick(v);
//                break;
//        }
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK){
            if (data != null){
                String address = data.getStringExtra("address");
                if (!TextUtils.isEmpty(address)){
                    smtContent.setText(address);
                }else{
                    finish();
                }
            }
        }
    }

    @Override
    public void onResult(Object result, String message) {

    }

    @Override
    public void onError(Throwable throwable, String message) {

    }
}
