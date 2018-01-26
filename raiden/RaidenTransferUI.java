package com.lingtuan.firefly.raiden;

import android.annotation.SuppressLint;
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

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

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
        String ammount = toValue.getText().toString().trim();
        if (TextUtils.isEmpty(ammount)){
            return;
        }
        LoadingDialog.show(this,"");
        RaidenNetUtils.getInstance().sendAmount(Double.parseDouble(ammount), channelVo.getPartnerAddress(),channelVo.getTokenAddress(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mHandler.sendEmptyMessage(0);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                mHandler.sendEmptyMessage(1);
            }
        });
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
                    finish();
                    break;
            }
        }
    };
}
