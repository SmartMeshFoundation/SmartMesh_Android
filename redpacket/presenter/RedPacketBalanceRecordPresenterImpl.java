package com.lingtuan.firefly.redpacket.presenter;

import com.lingtuan.firefly.redpacket.RedPacketBalanceRecordUI;
import com.lingtuan.firefly.redpacket.bean.RedPacketRecordBean;
import com.lingtuan.firefly.redpacket.contract.RedPacketBalanceRecordContract;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * @see RedPacketBalanceRecordUI
 * */
public class RedPacketBalanceRecordPresenterImpl implements RedPacketBalanceRecordContract.Presenter{

    private boolean isLoadingData = false;
    private int currentPage = 1 ;
    private int oldPage=1;
    private ArrayList<RedPacketRecordBean> redPacketRecords ;

    private RedPacketBalanceRecordContract.View mView;

    public RedPacketBalanceRecordPresenterImpl(RedPacketBalanceRecordContract.View view){
        this.mView = view;
        redPacketRecords = new ArrayList<>();
        mView.setPresenter(this);
    }

    @Override
    public void start() {

    }

    /**
     * get red packet balance record
     * */
    @Override
    public void loadData(int page) {

        isLoadingData=false;
        mView.error(0,null);

//        if(isLoadingData){
//            return;
//        }
//        isLoadingData=true;
//        oldPage= page;
//        NetRequestImpl.getInstance().getRedPacketBalanceRecord("", new RequestListener() {
//            @Override
//            public void start() {
//
//            }
//
//            @Override
//            public void success(JSONObject response) {
//                currentPage = oldPage;
//                if (currentPage == 1) {
//                    redPacketRecords.clear();
//                }
//                isLoadingData=false;
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

    /**
     * Analytical data
     * */
    private void parseData(JSONObject response){
        JSONArray jsonArray = response.optJSONArray("data");
        if (jsonArray != null) {
            if (currentPage == 1 || currentPage == 0){
                redPacketRecords.clear();
            }
            int count = jsonArray.length();
            for (int i = 0; i < count; i++) {
                RedPacketRecordBean recordBean = new RedPacketRecordBean().parse(jsonArray.optJSONObject(i));
                redPacketRecords.add(recordBean);
            }
            if (jsonArray.length() >= 10) {
                mView.success(redPacketRecords,currentPage,true);
            } else {
                mView.success(redPacketRecords,currentPage,false);
            }
        }else{
            mView.error(0,null);
        }
    }
}
