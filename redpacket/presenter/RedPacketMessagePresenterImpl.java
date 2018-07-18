package com.lingtuan.firefly.redpacket.presenter;

import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.redpacket.bean.RedPacketMessageBean;
import com.lingtuan.firefly.redpacket.contract.RedPacketMessageContract;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class RedPacketMessagePresenterImpl implements RedPacketMessageContract.Presenter {


    private boolean isLoadingData = false;
    private int currentPage = 1 ;
    private int oldPage=1;

    private ArrayList<RedPacketMessageBean> redPacketMessages;
    private RedPacketMessageContract.View mView;

    public RedPacketMessagePresenterImpl(RedPacketMessageContract.View view){
        this.mView = view;
        mView.setPresenter(this);
    }


    @Override
    public void loadData(int page) {

        mView.error(0,null);

//        if(isLoadingData){
//            return;
//        }
//        isLoadingData=true;
//        oldPage= page;
//        NetRequestImpl.getInstance().getRedPacketMessages("", new RequestListener() {
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
//                isLoadingData=false;
//                mView.error(errorCode,errorMsg);
//            }
//        });
    }

    @Override
    public void start() {

    }

    /**
     * Analytical data
     * */
    private void parseData(JSONObject response){
        currentPage=oldPage;
        if (currentPage == 1) {
            redPacketMessages.clear();
        }

        JSONArray jsonArray = response.optJSONArray("data");
        if (jsonArray != null) {
            int count = jsonArray.length();
            for (int i = 0; i < count; i++) {
                RedPacketMessageBean messageBean = new RedPacketMessageBean().parse(jsonArray.optJSONObject(i));
                redPacketMessages.add(messageBean);
            }
            isLoadingData=false;
            if (jsonArray != null && jsonArray.length() >= 10) {
                mView.success(redPacketMessages,currentPage,true);
            } else {
                mView.success(redPacketMessages,currentPage,false);
            }
        } else {
            mView.error(0,null);
        }
    }
}
