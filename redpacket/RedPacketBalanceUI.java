package com.lingtuan.firefly.redpacket;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.redpacket.contract.RedPacketBalanceContract;
import com.lingtuan.firefly.redpacket.presenter.RedPacketBalancePresenterImpl;
import com.lingtuan.firefly.util.Utils;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 红包余额页面
 * red packet balance
 * @see com.lingtuan.firefly.fragment.MySelfFragment
 * */
public class RedPacketBalanceUI extends BaseActivity implements RedPacketBalanceContract.View{

    @BindView(R.id.redBalanceSymbol)
    TextView redBalanceSymbol;
    @BindView(R.id.redBalanceBalance)
    TextView redBalanceBalance;
    @BindView(R.id.redBalanceMoney)
    TextView redBalanceMoney;
    @BindView(R.id.app_btn_right)
    TextView appBtnRight;

    private RedPacketBalanceContract.Presenter mPresenter;

    @Override
    protected void setContentView() {
        setContentView(R.layout.red_packet_balance_layout);
    }

    @Override
    protected void findViewById() {
        new RedPacketBalancePresenterImpl(this);
        mPresenter.start();
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
                startActivity(new Intent(this,RedPacketRechargeUI.class));
                Utils.openNewActivityAnim(this,false);
                break;
            case R.id.redBalanceWithdraw:
                startActivity(new Intent(this,RedPacketWithdrawUI.class));
                Utils.openNewActivityAnim(this,false);
                break;
            case R.id.app_btn_right:
                startActivity(new Intent(this,RedPacketBalanceRecordUI.class));
                Utils.openNewActivityAnim(this,false);
                break;
        }
    }

    @Override
    public void setPresenter(RedPacketBalanceContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public void refreshUI() {

    }
}
