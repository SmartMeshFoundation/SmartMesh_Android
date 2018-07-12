package com.lingtuan.firefly.redpacket;

import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.custom.LoadMoreListView;

import butterknife.BindView;

/**
 * message notice
 * 支付消息通知页面
 * */
public class RedPacketMessageUI extends BaseActivity {

    private RedPacketMessagePresenter redPacketMessagePresenter;

    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout refreshLayout;
    @BindView(R.id.messageListView)
    LoadMoreListView messageListView;
    @BindView(R.id.emptyView)
    TextView emptyView;

    @Override
    protected void setContentView() {
        setContentView(R.layout.red_packet_message_layout);
    }

    @Override
    protected void findViewById() {
        redPacketMessagePresenter = new RedPacketMessagePresenter(RedPacketMessageUI.this);
        redPacketMessagePresenter.init(refreshLayout,messageListView,emptyView);
    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.red_packet_pay_message));
    }
}
