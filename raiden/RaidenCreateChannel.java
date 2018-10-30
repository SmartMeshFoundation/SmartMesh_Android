package com.lingtuan.firefly.raiden;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.quickmark.CaptureActivity;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MyToast;
import com.lingtuan.firefly.spectrum.vo.StorableWallet;
import com.lingtuan.firefly.spectrum.vo.TokenVo;

import java.math.BigDecimal;

/**
 * Created on 2018/1/24.
 * cheate raiden channel ui
 */

public class RaidenCreateChannel extends BaseActivity {

    private TextView partner;//partner
    private EditText deposit;//deposit
    private TextView balance;//balance
    private TextView token;//token
    private TextView mTokenType;
    private TextView channelEnter;//token
    private ImageView channelQrImg;

    private StorableWallet storableWallet;
    private TokenVo mTokenVo;

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
        partner = (TextView) findViewById(R.id.partner);
        deposit = (EditText) findViewById(R.id.deposit);
        balance = (TextView) findViewById(R.id.balance);
        token = (TextView) findViewById(R.id.token);
        mTokenType = (TextView) findViewById(R.id.tokenType);
        channelEnter = (TextView) findViewById(R.id.channelEnter);
        channelQrImg = (ImageView) findViewById(R.id.channelQrImg);
    }

    @Override
    protected void setListener() {
        channelEnter.setOnClickListener(this);
        channelQrImg.setOnClickListener(this);
        deposit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    String temp = s.toString();
                    int tempInt = Integer.parseInt(temp);
                    int ban = (int) mTokenVo.getTokenBalance();
                    if (tempInt >= ban) {
                        deposit.setText(ban);
                        return;
                    }
                    int posDot = temp.indexOf(".");
                    if (posDot <= 0) {
                        if (temp.length() <= 8) {
                            return;
                        } else {
                            s.delete(8, 9);
                            return;
                        }
                    }
                    if (temp.length() - posDot - 1 > 2) {
                        s.delete(posDot + 3, posDot + 4);
                    }
                } catch (Exception e) {
                }
            }
        });

        deposit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
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
        });
    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.raiden_channel_create));
        token.setText(getString(R.string.smt));
        mTokenType.setText(mTokenVo.getTokenSymbol());
        if (storableWallet != null) {
            if (storableWallet.getFftBalance() > 0) {
//                BigDecimal smtDecimal = new BigDecimal(storableWallet.getFftBalance()).setScale(5, BigDecimal.ROUND_DOWN);
                balance.setText(getString(R.string.smt_er, String.valueOf(storableWallet.getFftBalance())));
            } else {
                balance.setText(getString(R.string.smt_er, String.valueOf(storableWallet.getFftBalance())));
            }
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.channelEnter:
                createChannelMethod();
                break;
            case R.id.channelQrImg:
                Intent i = new Intent(RaidenCreateChannel.this, CaptureActivity.class);
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

    private void createChannelMethod() {
        final String partnerAddress = partner.getText().toString();
        final String depositBalance = deposit.getText().toString();
        if (TextUtils.isEmpty(depositBalance) || TextUtils.isEmpty(partnerAddress)) {
            return;
        }
        NextApplication.mRaidenThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (NextApplication.api != null) {
                        mHandler.sendEmptyMessage(0);
                        BigDecimal ONE_ETHER = new BigDecimal("1000000000000000000");
                        String str = NextApplication.api.openChannel(partnerAddress, NextApplication.myInfo.getTokenAddress(), RaidenUrl.SETTLE_TIMEOUT, new BigDecimal(depositBalance).multiply(ONE_ETHER).toBigInteger().toString());
                        Log.i("xxxxxxxxx创建通道==", str);
                        checkChannelState(str);
                    } else {
                        mHandler.sendEmptyMessage(2);
                    }
                } catch (Exception e) {
                    Log.i("xxxxxxxxx异常", e.toString());
                    mHandler.sendEmptyMessage(2);
                }
            }
        });
    }

    private void checkChannelState(String callId){
        try {
            Thread.sleep(2000);
            NextApplication.api.getCallResult(callId);
            mHandler.sendEmptyMessage(1);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("xxxxxxxxx",e.getMessage());
            checkChannelState(callId);
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    LoadingDialog.show(RaidenCreateChannel.this, "");
                    break;
                case 1:
                    LoadingDialog.close();
                    MyToast.showToast(RaidenCreateChannel.this, getResources().getString(R.string.raiden_open_channel_success));
                    finish();
                    break;
                case 2:
                    LoadingDialog.close();
                    MyToast.showToast(RaidenCreateChannel.this, getResources().getString(R.string.raiden_open_channel_error));
                    break;
            }
        }
    };
}
