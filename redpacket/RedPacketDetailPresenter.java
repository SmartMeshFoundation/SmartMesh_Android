package com.lingtuan.firefly.redpacket;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.lingtuan.firefly.custom.CustomListView;
import com.lingtuan.firefly.redpacket.bean.RedPacketBean;
import com.lingtuan.firefly.util.Utils;

import java.util.ArrayList;

public class RedPacketDetailPresenter {

    private Context context;
    private CustomListView listView;
    private RedPacketDetailAdapter redPacketDetailAdapter;
    private ArrayList<RedPacketBean> redPacketBeans;

    public RedPacketDetailPresenter(Context context){
        this.context = context;
    }

    /**
     * red packet record
     * 红包记录
     * */
    public void redPacketRecord(){
        context.startActivity(new Intent(context,RedPacketTransactionRecordUI.class));
        Utils.openNewActivityAnim((Activity) context,false);
    }

    public void init(CustomListView listView) {
        Utils.setStatusBar(context,3);
        this.listView = listView;
        redPacketBeans = new ArrayList<>();
        redPacketDetailAdapter = new RedPacketDetailAdapter(context,redPacketBeans);
        listView.setAdapter(redPacketDetailAdapter);
        requestData();
    }

    /**
     * request red packet details data
     * 请求红包详情页数据
     * */
    private void requestData(){
        redPacketDetailAdapter.resetSource(redPacketBeans);
    }

}
