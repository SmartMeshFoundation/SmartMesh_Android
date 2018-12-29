package com.lingtuan.firefly.raiden;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.quickmark.CaptureActivity;
import com.lingtuan.firefly.raiden.contract.PhotonCreateContract;
import com.lingtuan.firefly.raiden.presenter.PhotonCreatePresenterImpl;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MyToast;
import com.lingtuan.firefly.spectrum.vo.StorableWallet;
import com.lingtuan.firefly.spectrum.vo.TokenVo;

import java.math.BigDecimal;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;

/**
 * Created on 2018/1/24.
 * cheate raiden channel ui
 */

public class PhotonCreateChannel extends BaseActivity implements PhotonCreateContract.View {

    @BindView(R.id.partner)
    TextView partner;//partner
    @BindView(R.id.deposit)
    EditText deposit;//deposit
    @BindView(R.id.balance)
    TextView balance;//balance
    @BindView(R.id.tokenType)
    TextView mTokenType;
    @BindView(R.id.channelNickName)
    EditText channelNickName;

    private StorableWallet storableWallet;
    private TokenVo mTokenVo;

    private PhotonCreateContract.Presenter mPresenter;

    @Override
    protected void setContentView() {
        setContentView(R.layout.raiden_channel_create);
        getPassData();
    }

    private void getPassData() {
        storableWallet = (StorableWallet) getIntent().getSerializableExtra("storableWallet");
        mTokenVo = (TokenVo) getIntent().getSerializableExtra("tokenVo");
    }

    @Override
    protected void findViewById() {

    }

    @Override
    protected void setListener() {

    }

    @OnFocusChange({R.id.deposit})
    public void onFocusChange(View v, boolean hasFocus) {
        final String depositBalance = deposit.getText().toString();
        if (hasFocus) {
            if (TextUtils.isEmpty(depositBalance)) {
                deposit.setHint(getString(R.string.raiden_create_balance, String.valueOf(mTokenVo.getTokenBalance())));
            }
        } else {
            if (TextUtils.isEmpty(depositBalance)) {
                deposit.setHint(getString(R.string.set_amount));
            }
        }
    }

    @OnTextChanged(value = R.id.deposit,callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void afterTextChanged(Editable s){
        try {
            mPresenter.checkDepositValue(s, (int) mTokenVo.getTokenBalance(),deposit);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initData() {
        new PhotonCreatePresenterImpl(this);
        setTitle(getString(R.string.raiden_channel_create));
        mTokenType.setText(mTokenVo.getTokenSymbol());
        if (storableWallet != null) {
            if (storableWallet.getFftBalance() > 0) {
                balance.setText(getString(R.string.smt_er, String.valueOf(storableWallet.getFftBalance())));
            } else {
                balance.setText(getString(R.string.smt_er, String.valueOf(storableWallet.getFftBalance())));
            }
        }
    }

    @OnClick({R.id.channelEnter,R.id.channelQrImg})
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.channelEnter:
                String partnerAddress = partner.getText().toString();
                String depositBalance = deposit.getText().toString();
                mPresenter.createChannelMethod(partnerAddress,depositBalance);
                break;
            case R.id.channelQrImg:
                Intent i = new Intent(PhotonCreateChannel.this, CaptureActivity.class);
                i.putExtra("type", 1);
                startActivityForResult(i, 100);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            String address = data.getStringExtra("address");
            partner.setText(address);
        }
    }

    private int timeOut = 0;
    private void checkChannelState(String callId){
        try {
            Thread.sleep(2000);
            NextApplication.api.getCallResult(callId);
            mHandler.sendEmptyMessage(1);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            timeOut++;
            if (TextUtils.equals(e.getMessage(),"dealing")){
                if (timeOut > 90){
                    timeOut = 0;
                    mHandler.sendEmptyMessage(2);
                }else{
                    checkChannelState(callId);
                }
            }else{
                mHandler.sendEmptyMessage(2);
            }
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    LoadingDialog.show(PhotonCreateChannel.this, "");
                    break;
                case 1:
                    LoadingDialog.close();
                    MyToast.showToast(PhotonCreateChannel.this, getResources().getString(R.string.raiden_open_channel_success));
                    finish();
                    break;
                case 2:
                    LoadingDialog.close();
                    MyToast.showToast(PhotonCreateChannel.this, getResources().getString(R.string.raiden_open_channel_error));
                    break;
            }
        }
    };

    @Override
    public void setPresenter(PhotonCreateContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public void createChannelStart() {
        mHandler.sendEmptyMessage(0);
    }

    @Override
    public void createChannelSuccess(String response) {
        checkChannelState(response);
    }

    @Override
    public void createChannelError() {
        mHandler.sendEmptyMessage(2);
    }
}
