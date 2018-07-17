package com.lingtuan.firefly.redpacket;

import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;

import butterknife.BindView;

/**
 * red packet message detail
 * 红包消息详情页面
 * @see RedPacketMessageUI
 * */
public class RedPacketMessageDetailUI extends BaseActivity {
    @BindView(R.id.messageAmount)
    TextView messageAmount;
    @BindView(R.id.messageType)
    TextView messageType;
    @BindView(R.id.messageTime)
    TextView messageTime;
    @BindView(R.id.messageNumber)
    TextView messageNumber;

    @Override
    protected void setContentView() {
        setContentView(R.layout.red_packet_message_detail);
    }

    @Override
    protected void findViewById() {

    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.red_packet_pay_message_details));
    }

}
