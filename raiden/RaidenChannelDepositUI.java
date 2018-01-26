package com.lingtuan.firefly.raiden;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.raiden.vo.RaidenChannelVo;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.wallet.vo.StorableWallet;

import java.io.IOException;
import java.math.BigDecimal;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created  on 2018/1/26.
 * raiden channel add ui
 */

public class RaidenChannelDepositUI extends BaseActivity{

    private TextView partner;//partner
    private TextView deposit;//deposit
    private TextView balance;//pay
    private TextView token;//token
    private TextView channelAddNumber;//add number
    private TextView channelAdd;//to add

    private RaidenChannelVo channelVo;
    private StorableWallet storableWallet;

    @Override
    protected void setContentView() {
        setContentView(R.layout.raiden_channel_add_layout);
        getPassData();
    }

    private void getPassData() {
        channelVo = (RaidenChannelVo) getIntent().getSerializableExtra("raidenChannelVo");
        storableWallet = (StorableWallet) getIntent().getSerializableExtra("storableWallet");
    }

    @Override
    protected void findViewById() {
        partner = (TextView) findViewById(R.id.partner);
        deposit = (TextView) findViewById(R.id.deposit);
        balance = (TextView) findViewById(R.id.balance);
        token = (TextView) findViewById(R.id.token);
        channelAddNumber = (TextView) findViewById(R.id.channelAddNumber);
        channelAdd = (TextView) findViewById(R.id.channelAdd);
    }

    @Override
    protected void setListener() {
        channelAdd.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.raiden_channel_add));

        if (storableWallet != null){
            if (storableWallet.getFftBalance() > 0){
                BigDecimal smtDecimal = new BigDecimal(storableWallet.getFftBalance()).setScale(10,BigDecimal.ROUND_DOWN);
                balance.setText(getString(R.string.smt_er,smtDecimal.toPlainString()));
            }else{
                balance.setText(getString(R.string.smt_er,storableWallet.getFftBalance() +""));
            }
        }

        if (channelVo != null){
            partner.setText(channelVo.getPartnerAddress());
            deposit.setText(getString(R.string.smt_er,channelVo.getBalance()));
            token.setText(getString(R.string.smt));
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.channelAdd:
                channelAddBalanceMethod();
                break;
        }
    }

    private void channelAddBalanceMethod() {
        String channelNumber = channelAddNumber.getText().toString().trim();
        if (TextUtils.isEmpty(channelNumber)){
            return;
        }
        LoadingDialog.show(this,"");
        RaidenNetUtils.getInstance().addBalance(Double.parseDouble(channelNumber), channelVo.getChannelAddress(), new Callback() {
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
