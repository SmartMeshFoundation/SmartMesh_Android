package com.lingtuan.firefly.redpacket;

import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.custom.LoadMoreListView;
import com.lingtuan.firefly.wallet.TransAdapter;
import com.lingtuan.firefly.wallet.vo.TransVo;

import java.util.ArrayList;

public class RedPacketBalanceRecord extends BaseActivity{

    private RedPacketBalanceRecordPresenter redPacketBalanceRecordPresenter;

    private SwipeRefreshLayout refreshLayout;
    private LoadMoreListView recordListView;
    private TextView emptyView;
    private TextView redBalanceRecharge;
    private TextView redBalanceWithdraw;

    @Override
    protected void setContentView() {
        setContentView(R.layout.red_packet_balance_record_layout);
    }

    @Override
    protected void findViewById() {
        emptyView = findViewById(R.id.emptyView);
        recordListView = findViewById(R.id.recordListView);
        refreshLayout = findViewById(R.id.swipe_refresh);
        redBalanceRecharge = findViewById(R.id.redBalanceRecharge);
        redBalanceWithdraw = findViewById(R.id.redBalanceWithdraw);
        redPacketBalanceRecordPresenter = new RedPacketBalanceRecordPresenter(RedPacketBalanceRecord.this);
        redPacketBalanceRecordPresenter.init(refreshLayout,recordListView,emptyView);
    }

    @Override
    protected void setListener() {
        redBalanceRecharge.setOnClickListener(this);
        redBalanceWithdraw.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.red_balance_record));
    }

    @Override
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
