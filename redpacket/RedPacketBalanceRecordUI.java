package com.lingtuan.firefly.redpacket;

import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.custom.LoadMoreListView;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 红包余额记录
 * red packet balance record
 * @see RedPacketBalanceUI
 * */
public class RedPacketBalanceRecordUI extends BaseActivity{

    private RedPacketBalanceRecordPresenter redPacketBalanceRecordPresenter;

    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout refreshLayout;
    @BindView(R.id.recordListView)
    LoadMoreListView recordListView;
    @BindView(R.id.emptyView)
    TextView emptyView;

    @Override
    protected void setContentView() {
        setContentView(R.layout.red_packet_balance_record_layout);
    }

    @Override
    protected void findViewById() {
        redPacketBalanceRecordPresenter = new RedPacketBalanceRecordPresenter(RedPacketBalanceRecordUI.this);
        redPacketBalanceRecordPresenter.init(refreshLayout,recordListView,emptyView);
    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.red_balance_record));
    }

    @OnClick({R.id.redBalanceRecharge,R.id.redBalanceWithdraw})
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.redBalanceRecharge:
                redPacketBalanceRecordPresenter.rechargeMethod();
                break;
            case R.id.redBalanceWithdraw:
                redPacketBalanceRecordPresenter.withDrawMethod();
                break;
        }
    }
}
