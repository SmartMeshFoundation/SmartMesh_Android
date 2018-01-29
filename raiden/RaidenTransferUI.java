package com.lingtuan.firefly.raiden;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.raiden.vo.RaidenChannelVo;
import com.lingtuan.firefly.util.LoadingDialog;

/**
 * Created on 2018/1/26.
 * raiden transfer ui
 */

public class RaidenTransferUI extends BaseActivity {

    private TextView toAddress;
    private TextView channelType;
    private TextView channelPay;
    private EditText toValue;

    private RaidenChannelVo channelVo;

    @Override
    protected void setContentView() {
        setContentView(R.layout.raiden_transfer_layout);
        getPassData();
    }

    private void getPassData() {
        channelVo = (RaidenChannelVo) getIntent().getSerializableExtra("raidenChannelVo");
    }

    @Override
    protected void findViewById() {
        toAddress = (TextView) findViewById(R.id.toAddress);
        channelType = (TextView) findViewById(R.id.channelType);
        channelPay = (TextView) findViewById(R.id.channelPay);
        toValue = (EditText) findViewById(R.id.toValue);
    }

    @Override
    protected void setListener() {
        channelPay.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.send_blance));
        channelType.setText(getString(R.string.smt));
        if (channelVo != null){
            toAddress.setText(channelVo.getPartnerAddress());
        }

    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.channelPay:
                channelPayMethod();
                 break;
        }
    }

    private void channelPayMethod() {
        final String ammount = toValue.getText().toString().trim();
        if (TextUtils.isEmpty(ammount)){
            return;
        }
        LoadingDialog.show(this,"");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String jsonString =  RaidenNetUtils.getInstance().sendAmount(Double.parseDouble(ammount), channelVo.getPartnerAddress(),channelVo.getTokenAddress());
                    if (TextUtils.isEmpty(jsonString)){
                        mHandler.sendEmptyMessage(0);
                    }else{
                        mHandler.sendEmptyMessage(1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    mHandler.sendEmptyMessage(0);
                }
            }
        }).start();

    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    LoadingDialog.close();
                    finish();
                    break;
                case 1:
                    LoadingDialog.close();
                    Intent intent = new Intent();
                    setResult(RESULT_OK,intent);
                    finish();
                    break;
            }
        }
    };
}
