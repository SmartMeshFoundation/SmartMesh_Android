package com.lingtuan.firefly.redpacket.fragment;

import android.content.Context;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.custom.LoadMoreListView;
import com.lingtuan.firefly.redpacket.bean.RedPacketRecordBean;
import com.lingtuan.firefly.util.MyToast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class RedPacketRecordPresenter implements SwipeRefreshLayout.OnRefreshListener, LoadMoreListView.RefreshListener {

    private Context context;

    private TextView emptyView;
    private LoadMoreListView refreshListView;
    private SwipeRefreshLayout swipeContainer;

    private ArrayList<RedPacketRecordBean> redPacketRecords;
    private RedPacketRecordAdapter redPacketRecordAdapter;

    private boolean isLoadingData = false;
    private int currentPage = 1 ;
    private int oldPage=1;

    public RedPacketRecordPresenter(Context context){
        this.context = context;
    }

    public void init(final SwipeRefreshLayout swipeContainer,LoadMoreListView refreshListView,TextView emptyView){
        this.swipeContainer = swipeContainer;
        this.refreshListView = refreshListView;
        this.emptyView = emptyView;
        redPacketRecords = new ArrayList<>();
        redPacketRecordAdapter = new RedPacketRecordAdapter(context,redPacketRecords);
        refreshListView.setAdapter(redPacketRecordAdapter);
        setListener();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeContainer.setRefreshing(true);
                getRedPacketBalanceRecordMethod(0);
            }
        }, 500);
    }

    /**
     * set refresh listener
     * */
    private void setListener(){
        swipeContainer.setOnRefreshListener(this);
        refreshListView.setOnRefreshListener(this);
    }


    @Override
    public void onRefresh() {
        getRedPacketBalanceRecordMethod(0);
    }

    @Override
    public void loadMore() {
        getRedPacketBalanceRecordMethod(currentPage + 1);
    }

    /**
     * get red packet balance record
     * */
    private void getRedPacketBalanceRecordMethod(int page){
        if(isLoadingData){
            return;
        }

        isLoadingData=false;
        swipeContainer.setRefreshing(false);
        MyToast.showToast(context,"no api");
        checkListEmpty();

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
//                parseData(response);
//            }
//
//            @Override
//            public void error(int errorCode, String errorMsg) {
//                isLoadingData=false;
//                swipeContainer.setRefreshing(false);
//                MyToast.showToast(context,errorMsg);
//                checkListEmpty();
//            }
//        });
    }

    /**
     * Analytical data
     * */
    private void parseData(JSONObject response){
        currentPage=oldPage;
        if (currentPage == 1) {
            redPacketRecords.clear();
        }

        JSONArray jsonArray = response.optJSONArray("data");
        if (jsonArray != null) {
            int count = jsonArray.length();
            for (int i = 0; i < count; i++) {
                RedPacketRecordBean recordBean = new RedPacketRecordBean().parse(jsonArray.optJSONObject(i));
                redPacketRecords.add(recordBean);
            }
            redPacketRecordAdapter.resetSource(redPacketRecords);
        } else {
            MyToast.showToast(context,context.getString(R.string.red_balance_record_empty));
        }
        isLoadingData=false;
        swipeContainer.setRefreshing(false);
        if (jsonArray != null && jsonArray.length() >= 10) {
            refreshListView.resetFooterState(true);
        } else {
            refreshListView.resetFooterState(false);
        }
        checkListEmpty();
    }

    /**
     * check record is empty
     * */
    private void checkListEmpty(){
        if(redPacketRecords == null || redPacketRecords.size() == 0){
            emptyView.setVisibility(View.VISIBLE);
        }else{
            emptyView.setVisibility(View.GONE);
        }
    }
}
