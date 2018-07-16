package com.lingtuan.firefly.redpacket;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnPageChange;

/**
 * red packet transaction record
 * 红包交易记录
 *
 * @see RedPacketDetailUI
 */
public class RedPacketTransactionRecordUI extends BaseActivity {

    @BindView(R.id.redPacketRecord)
    TextView redPacketRecord;
    @BindView(R.id.rechargeRecord)
    TextView rechargeRecord;
    @BindView(R.id.withdrawRecord)
    TextView withdrawRecord;
    @BindView(R.id.viewPager)
    ViewPager viewPager;

    private RedPacketTransactionRecordPresenter redPacketTransactionRecordPresenter;


    @Override
    protected void setContentView() {
        setContentView(R.layout.red_packet_transaction_record_layout);
    }

    @Override
    protected void findViewById() {
        redPacketTransactionRecordPresenter = new RedPacketTransactionRecordPresenter(RedPacketTransactionRecordUI.this);
        redPacketTransactionRecordPresenter.init(viewPager,redPacketRecord,rechargeRecord,withdrawRecord);
    }

    @Override
    protected void setListener() {

    }

    @OnPageChange(R.id.viewPager)
    public void onPageSelected(int position) {
        redPacketTransactionRecordPresenter.onPageChange(position);
    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.red_packet_transaction_record));
    }

    @OnClick({R.id.redPacketRecord, R.id.rechargeRecord, R.id.withdrawRecord})
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.redPacketRecord:
                redPacketTransactionRecordPresenter.getRedPacketRecordMethod();
                break;
            case R.id.rechargeRecord:
                redPacketTransactionRecordPresenter.getRedPacketRechargeRecord();
                break;
            case R.id.withdrawRecord:
                redPacketTransactionRecordPresenter.getRedPacketWithdraw();
                break;
        }
    }

}
