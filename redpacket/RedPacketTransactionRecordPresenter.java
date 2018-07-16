package com.lingtuan.firefly.redpacket;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.lingtuan.firefly.redpacket.fragment.ContentFragmentAdapter;
import com.lingtuan.firefly.redpacket.fragment.RedPacketRechargeRecordUI;
import com.lingtuan.firefly.redpacket.fragment.RedPacketRecordUI;
import com.lingtuan.firefly.redpacket.fragment.RedPacketWithdrawRecordUI;

public class RedPacketTransactionRecordPresenter {

    private Context context;
    private ViewPager mViewPager;
    private TextView redPacketRecord;
    private TextView rechargeRecord;
    private TextView withdrawRecord;

    private RedPacketRecordUI mRedPacketRecord;
    private RedPacketRechargeRecordUI mRedPacketRechargeRecord;
    private RedPacketWithdrawRecordUI mRedPacketWithdrawRecord;

    public RedPacketTransactionRecordPresenter(Context context){
        this.context = context;
    }

    public void init(ViewPager viewPager,TextView redPacketRecord, TextView rechargeRecord, TextView withdrawRecord){
        this.mViewPager = viewPager;
        this.redPacketRecord = redPacketRecord;
        this.rechargeRecord = rechargeRecord;
        this.withdrawRecord = withdrawRecord;
        mRedPacketRecord = new RedPacketRecordUI();
        mRedPacketRechargeRecord = new RedPacketRechargeRecordUI();
        mRedPacketWithdrawRecord = new RedPacketWithdrawRecordUI();
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(new ContentFragmentAdapter.Holder(((AppCompatActivity)context).getSupportFragmentManager())
                .add(mRedPacketRecord)
                .add(mRedPacketRechargeRecord)
                .add(mRedPacketWithdrawRecord)
                .set());
        onPageChange(0);
    }

    public void onPageChange(int position){
        redPacketRecord.setSelected(false);
        rechargeRecord.setSelected(false);
        withdrawRecord.setSelected(false);
        redPacketRecord.setEnabled(true);
        rechargeRecord.setEnabled(true);
        withdrawRecord.setEnabled(true);
        switch (position){
            case 0:
                redPacketRecord.setSelected(true);
                redPacketRecord.setEnabled(false);
                break;
            case 1:
                rechargeRecord.setSelected(true);
                rechargeRecord.setEnabled(false);
                break;
            case 2:
                withdrawRecord.setSelected(true);
                withdrawRecord.setEnabled(false);
                break;
        }
    }

    /**
     * get red packet record page
     * 获取红包记录页面
     * */
    public void  getRedPacketRecordMethod(){
        onPageChange(0);
        mViewPager.setCurrentItem(0);
    }

    /**
     * get red packet recharge page
     * 获取充值记录页面
     * */
    public void  getRedPacketRechargeRecord(){
        onPageChange(1);
        mViewPager.setCurrentItem(1);
    }

    /**
     * get red packet withdraw page
     * 获取提现记录页面
     * */
    public void  getRedPacketWithdraw(){
        onPageChange(2);
        mViewPager.setCurrentItem(2);
    }

}
