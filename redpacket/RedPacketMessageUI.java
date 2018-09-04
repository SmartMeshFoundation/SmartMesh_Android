package com.lingtuan.firefly.redpacket;

import android.content.Intent;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.custom.LoadMoreListView;
import com.lingtuan.firefly.redpacket.bean.RedPacketMessageBean;
import com.lingtuan.firefly.redpacket.contract.RedPacketMessageContract;
import com.lingtuan.firefly.redpacket.listener.SetOnClickListener;
import com.lingtuan.firefly.redpacket.presenter.RedPacketMessagePresenterImpl;
import com.lingtuan.firefly.util.MyToast;
import com.lingtuan.firefly.util.Utils;

import java.util.ArrayList;

import butterknife.BindView;

/**
 * message notice
 * 支付消息通知页面
 * */
public class RedPacketMessageUI extends BaseActivity implements RedPacketMessageContract.View, SetOnClickListener, SwipeRefreshLayout.OnRefreshListener, LoadMoreListView.RefreshListener {

    private RedPacketMessageContract.Presenter mPresenter;

    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout refreshLayout;
    @BindView(R.id.messageListView)
    LoadMoreListView messageListView;
    @BindView(R.id.emptyView)
    TextView emptyView;
    private int currentPage = 1;
    private ArrayList<RedPacketMessageBean> redPacketMessages;
    private RedPacketMessageAdapter redPacketMessageAdapter;

    @Override
    protected void setContentView() {
        setContentView(R.layout.red_packet_message_layout);
    }

    @Override
    protected void findViewById() {

    }

    @Override
    protected void setListener() {
        refreshLayout.setOnRefreshListener(this);
        messageListView.setOnRefreshListener(this);
    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.red_packet_pay_message));
        new RedPacketMessagePresenterImpl(this);
        redPacketMessages = new ArrayList<>();
        redPacketMessageAdapter = new RedPacketMessageAdapter(RedPacketMessageUI.this,redPacketMessages,this);
        messageListView.setAdapter(redPacketMessageAdapter);
        setListener();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(true);
                mPresenter.loadData(0);
            }
        }, 500);
    }

    @Override
    public void success(ArrayList<RedPacketMessageBean> tempRedPacketMessages, int tempCurrentPage, boolean resetFooterState) {
        refreshLayout.setRefreshing(false);
        this.currentPage = tempCurrentPage;
        if (currentPage == 0 || currentPage == 1){
            redPacketMessages.clear();
        }
        redPacketMessages.addAll(redPacketMessages);
        redPacketMessageAdapter.resetSource(redPacketMessages);
        messageListView.resetFooterState(resetFooterState);
    }

    @Override
    public void error(int errorCode, String errorMsg) {
        refreshLayout.setRefreshing(false);
        if (errorCode == 0){
            showToast(getString(R.string.red_balance_record_empty));
        }else{
            showToast(errorMsg);
        }
    }

    @Override
    public void setPresenter(RedPacketMessageContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public void onItemClickListener(int position) {
        MyToast.showToast(this,"点击了第" + position + "几项");
        startActivity(new Intent(this,RedPacketMessageDetailUI.class));
        Utils.openNewActivityAnim(this,false);
    }

    @Override
    public void onRefresh() {
        mPresenter.loadData(0);
    }

    @Override
    public void loadMore() {
        mPresenter.loadData(currentPage + 1);
    }
}
