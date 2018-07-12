package com.lingtuan.firefly.redpacket;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;

/**
 * 红包提现结果页面  包含成功、失败、正在进行中
 * red packet withdraw finish ui  contains success, failure, ongoing
 * @see RedPacketWithdrawUI
 * */
public class RedPacketWithdrawFinishUI extends BaseActivity{
    @Override
    protected void setContentView() {
        setContentView(R.layout.red_packet_withdraw_finish);
    }

    @Override
    protected void findViewById() {

    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.red_withdraw_success));
    }
}
