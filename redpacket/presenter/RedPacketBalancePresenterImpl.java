package com.lingtuan.firefly.redpacket.presenter;

import com.lingtuan.firefly.redpacket.RedPacketBalanceUI;
import com.lingtuan.firefly.redpacket.contract.RedPacketBalanceContract;

/**
 * @see RedPacketBalanceUI
 * */
public class RedPacketBalancePresenterImpl implements RedPacketBalanceContract.Presenter{

    private RedPacketBalanceContract.View mView;

    public RedPacketBalancePresenterImpl(RedPacketBalanceContract.View view){
        this.mView = view;
        mView.setPresenter(this);
    }

    @Override
    public void start() {
        mView.refreshUI();
    }
}
