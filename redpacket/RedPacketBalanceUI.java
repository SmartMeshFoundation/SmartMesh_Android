package com.lingtuan.firefly.redpacket;

import android.view.View;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 红包余额页面
 * red packet balance
 * @see com.lingtuan.firefly.fragment.MySelfFragment
 * */
public class RedPacketBalanceUI extends BaseActivity{

    @BindView(R.id.redBalanceSymbol)
    TextView redBalanceSymbol;
    @BindView(R.id.redBalanceBalance)
    TextView redBalanceBalance;
    @BindView(R.id.redBalanceMoney)
    TextView redBalanceMoney;
    @BindView(R.id.app_btn_right)
    TextView appBtnRight;

    private RedPacketBalancePresenter redPacketBalancePresenter;

    @Override
    protected void setContentView() {
        setContentView(R.layout.red_packet_balance_layout);
    }

    @Override
    protected void findViewById() {
        redPacketBalancePresenter = new RedPacketBalancePresenter(RedPacketBalanceUI.this);
        redPacketBalancePresenter.init(redBalanceSymbol,redBalanceBalance,redBalanceMoney);
    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.red_balance));
        appBtnRight.setVisibility(View.VISIBLE);
        appBtnRight.setText(getString(R.string.red_balance_record));
    }

    @OnClick({R.id.redBalanceRecharge,R.id.redBalanceWithdraw,R.id.app_btn_right})
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
