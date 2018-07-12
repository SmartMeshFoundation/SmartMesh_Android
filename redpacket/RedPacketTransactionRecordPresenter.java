package com.lingtuan.firefly.redpacket;

import android.content.Context;
import android.support.v4.view.ViewPager;

public class RedPacketTransactionRecordPresenter implements ViewPager.OnPageChangeListener {

    private Context context;
    private ViewPager viewPager;

    public RedPacketTransactionRecordPresenter(Context context){
        this.context = context;
    }

    public void init(ViewPager viewPager){
        this.viewPager = viewPager;
        setPageChangeListener();
    }

    /**
     * get red packet record page
     * 获取红包记录页面
     * */
    public void  getRedPacketRecordMethod(){

    }

    /**
     * get red packet recharge page
     * 获取充值记录页面
     * */
    public void  getRedPacketRechargeRecord(){

    }

    /**
     * get red packet withdraw page
     * 获取提现记录页面
     * */
    public void  getRedPacketWithdraw(){

    }

    /**
     * add on page change listener
     * 监听页面改变状态
     * */
    private void setPageChangeListener(){
        viewPager.addOnPageChangeListener(this);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
