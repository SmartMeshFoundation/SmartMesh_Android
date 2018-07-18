package com.lingtuan.firefly.redpacket;

import android.content.Intent;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.custom.LoadMoreListView;
import com.lingtuan.firefly.redpacket.bean.RedPacketRecordBean;
import com.lingtuan.firefly.redpacket.contract.RedPacketBalanceRecordContract;
import com.lingtuan.firefly.redpacket.presenter.RedPacketBalanceRecordPresenterImpl;
import com.lingtuan.firefly.util.Utils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 红包余额记录
 * red packet balance record
 * @see RedPacketBalanceUI
 * */
public class RedPacketBalanceRecordUI extends BaseActivity implements RedPacketBalanceRecordContract.View, LoadMoreListView.RefreshListener, SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout refreshLayout;
    @BindView(R.id.recordListView)
    LoadMoreListView recordListView;
    @BindView(R.id.emptyView)
    TextView emptyView;

    private int currentPage = 1 ;
    private ArrayList<RedPacketRecordBean> redPacketRecords;
    private RedPacketBalanceRecordAdapter redPacketBalanceRecordAdapter;
    private RedPacketBalanceRecordContract.Presenter mPresenter;

    @Override
    protected void setContentView() {
        setContentView(R.layout.red_packet_balance_record_layout);
    }

    @Override
    protected void findViewById() {
        new RedPacketBalanceRecordPresenterImpl(this);
    }

    @Override
    protected void setListener() {
        refreshLayout.setOnRefreshListener(this);
        recordListView.setOnRefreshListener(this);
    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.red_balance_record));
        redPacketRecords = new ArrayList<>();
        redPacketBalanceRecordAdapter = new RedPacketBalanceRecordAdapter(this,redPacketRecords);
        recordListView.setAdapter(redPacketBalanceRecordAdapter);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mPresenter.loadData(0);
            }
        }, 500);
    }

    @OnClick({R.id.redBalanceRecharge,R.id.redBalanceWithdraw})
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
        }
    }

    @Override
    public void setPresenter(RedPacketBalanceRecordContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public void loadMore() {
        mPresenter.loadData(currentPage + 1);
    }

    @Override
    public void onRefresh() {
        mPresenter.loadData(0);
    }

    @Override
    public void success(ArrayList<RedPacketRecordBean> redPacketRecords,int currentPage,boolean resetFooterState) {
        this.currentPage = currentPage;
        refreshLayout.setRefreshing(false);
        redPacketBalanceRecordAdapter.resetSource(redPacketRecords);
        recordListView.resetFooterState(resetFooterState);
        checkListEmpty();
    }

    @Override
    public void error(int errorCode, String errorMsg) {
        refreshLayout.setRefreshing(false);
        if (errorCode == 0){
            showToast(getString(R.string.red_balance_record_empty));
        }else{
            showToast(errorMsg);
        }
        checkListEmpty();
    }

    /**
     * check record is empty
     * */
    private void checkListEmpty(){
        if(redPacketRecords == null || redPacketRecords.size() == 0){
            emptyView.setVisibility(View.VISIBLE);
        }else{
            emptyView.setVisibility(View.GONE);
        }
    }
}
