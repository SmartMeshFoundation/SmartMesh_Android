package com.lingtuan.firefly.redpacket.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.lingtuan.firefly.custom.CustomListView;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.redpacket.RedPacketDetailAdapter;
import com.lingtuan.firefly.redpacket.RedPacketTransactionRecordUI;
import com.lingtuan.firefly.redpacket.bean.RedPacketBean;
import com.lingtuan.firefly.redpacket.bean.RedPacketRecordBean;
import com.lingtuan.firefly.redpacket.contract.RedPacketDetailContract;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class RedPacketDetailPresenterImpl implements RedPacketDetailContract.Presenter{

    private ArrayList<RedPacketBean> redPacketBeans;

    private RedPacketDetailContract.View mView;

    public RedPacketDetailPresenterImpl(RedPacketDetailContract.View view){
        this.mView = view;
        mView.setPresenter(this);
    }

    @Override
    public void start() {
        mView.error(0,null);
//        NetRequestImpl.getInstance().getRedPacketBalanceRecord("", new RequestListener() {
//            @Override
//            public void start() {
//
//            }
//
//            @Override
//            public void success(JSONObject response) {
//                parseData(response);
//            }
//
//            @Override
//            public void error(int errorCode, String errorMsg) {
//                mView.error(errorCode,errorMsg);
//            }
//        });
    }

    /**
     * Analytical data
     * */
    private void parseData(JSONObject response){
        JSONArray jsonArray = response.optJSONArray("data");
        if (jsonArray != null) {
            int count = jsonArray.length();
            for (int i = 0; i < count; i++) {
                RedPacketBean redPacketBean = new RedPacketBean().parse(jsonArray.optJSONObject(i));
                redPacketBeans.add(redPacketBean);
            }
            mView.success(redPacketBeans);
        }else{
            mView.error(0,null);
        }
    }
}
