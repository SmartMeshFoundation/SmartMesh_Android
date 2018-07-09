package com.lingtuan.firefly.redpacket;

import android.view.View;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;

public class RedPacketBalance extends BaseActivity{

    private TextView redBalanceSymbol;
    private TextView redBalanceBalance;
    private TextView redBalanceMoney;
    private TextView redBalanceRecharge;
    private TextView redBalanceWithdraw;
    private TextView appBtnRight;

    private RedPacketBalancePresenter redPacketBalancePresenter;

    @Override
    protected void setContentView() {
        setContentView(R.layout.red_packet_balance_layout);
    }

    @Override
    protected void findViewById() {
        redBalanceSymbol = findViewById(R.id.redBalanceSymbol);
        redBalanceBalance = findViewById(R.id.redBalanceBalance);
        redBalanceMoney = findViewById(R.id.redBalanceMoney);
        redBalanceRecharge = findViewById(R.id.redBalanceRecharge);
        redBalanceWithdraw = findViewById(R.id.redBalanceWithdraw);
        appBtnRight = findViewById(R.id.app_btn_right);
        redPacketBalancePresenter = new RedPacketBalancePresenter(RedPacketBalance.this);
        redPacketBalancePresenter.init(redBalanceSymbol,redBalanceBalance,redBalanceMoney);
    }

    @Override
    protected void setListener() {
        redBalanceRecharge.setOnClickListener(this);
        redBalanceWithdraw.setOnClickListener(this);
        appBtnRight.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.red_balance));
        appBtnRight.setVisibility(View.VISIBLE);
        appBtnRight.setText(getString(R.string.red_balance_record));
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.redBalanceRecharge:
                redPacketBalancePresenter.rechargeMethod();
                break;
            case R.id.redBalanceWithdraw:
                redPacketBalancePresenter.withDrawMethod();
                break;
            case R.id.app_btn_right:
                redPacketBalancePresenter.redPacketRecode();
                break;
        }
    }
}
