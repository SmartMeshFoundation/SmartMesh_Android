package com.lingtuan.firefly.redpacket;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.redpacket.fragment.RedPacketRechargeRecordFragment;

public class RedPacketRechargeRecordUI extends BaseActivity {
    @Override
    protected void setContentView() {
        setContentView(R.layout.red_packet_fragment_layout);
    }

    @Override
    protected void findViewById() {

    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void initData() {

        setTitle(getString(R.string.red_packet_recharge_record));

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        if(fm.findFragmentById(R.id.redPacketRecord) == null ){
            RedPacketRechargeRecordFragment recordFragment = RedPacketRechargeRecordFragment.newInstance(getIntent().getExtras()) ;
            ft.add(R.id.redPacketRecord, recordFragment);
            ft.commit();
        }
    }
}
