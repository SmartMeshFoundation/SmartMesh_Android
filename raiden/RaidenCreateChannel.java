package com.lingtuan.firefly.raiden;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.quickmark.CaptureActivity;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.wallet.vo.StorableWallet;

import java.math.BigDecimal;

/**
 * Created on 2018/1/24.
 * cheate raiden channel ui
 */

public class RaidenCreateChannel extends BaseActivity{

    private TextView partner;//partner
    private EditText deposit;//deposit
    private TextView balance;//balance
    private TextView token;//token
    private TextView channelEnter;//token
    private ImageView channelQrImg;

    private StorableWallet storableWallet;

    @Override
    protected void setContentView() {
        setContentView(R.layout.raiden_channel_create);
        getPassData();
    }

    private void getPassData() {
        storableWallet = (StorableWallet) getIntent().getSerializableExtra("storableWallet");
    }

    @Override
    protected void findViewById() {
        partner = (TextView) findViewById(R.id.partner);
        deposit = (EditText) findViewById(R.id.deposit);
        balance = (TextView) findViewById(R.id.balance);
        token = (TextView) findViewById(R.id.token);
        channelEnter = (TextView) findViewById(R.id.channelEnter);
        channelQrImg = (ImageView) findViewById(R.id.channelQrImg);
    }

    @Override
    protected void setListener() {
        channelEnter.setOnClickListener(this);
        channelQrImg.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.raiden_channel_create));
        token.setText(getString(R.string.smt));
        if (storableWallet != null){
            if (storableWallet.getFftBalance() > 0){
                BigDecimal smtDecimal = new BigDecimal(storableWallet.getFftBalance()).setScale(10,BigDecimal.ROUND_DOWN);
                balance.setText(getString(R.string.smt_er,smtDecimal.toPlainString()));
            }else{
                balance.setText(getString(R.string.smt_er,storableWallet.getFftBalance() +""));
            }
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.channelEnter:
                createChannelMethod();
                break;
            case R.id.channelQrImg:
                Intent i = new Intent(RaidenCreateChannel.this,CaptureActivity.class);
                i.putExtra("type",2);
                startActivityForResult(i,100);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK){
            String address = data.getStringExtra("address");
            partner.setText(address);
        }
    }

    private void createChannelMethod() {
        final String partnerAddress = partner.getText().toString();
        final String depositBalance = deposit.getText().toString();
        if (TextUtils.isEmpty(depositBalance)){
            return;
        }
        LoadingDialog.show(this,"");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String jsonString = RaidenNetUtils.getInstance().openChannel(partnerAddress, Constants.CONTACT_ADDRESS, Double.parseDouble(depositBalance), 100);
                    if (TextUtils.isEmpty(jsonString)){
                        mHandler.sendEmptyMessage(0);
                    }else {
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
                    //Send to refresh the page
                    Intent i = new Intent();
                    setResult(RESULT_OK,i);
                    finish();
                    break;
            }
        }
    };

}
