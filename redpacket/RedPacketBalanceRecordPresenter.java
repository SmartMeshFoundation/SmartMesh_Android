package com.lingtuan.firefly.redpacket;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.custom.LoadMoreListView;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.redpacket.bean.RedPacketRecordBean;
import com.lingtuan.firefly.util.MyToast;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class RedPacketBalanceRecordPresenter implements SwipeRefreshLayout.OnRefreshListener, LoadMoreListView.RefreshListener {

    private Context context;
    private SwipeRefreshLayout refreshLayout;
    private LoadMoreListView recordListView;
    private TextView emptyView;
    private ArrayList<RedPacketRecordBean> redPacketRecords;
    private RedPacketBalanceRecordAdapter redPacketBalanceRecordAdapter;

    private boolean isLoadingData = false;
    private int currentPage = 1 ;
    private int oldPage=1;

    public RedPacketBalanceRecordPresenter(Context context){
        this.context = context;
    }

    public void init(final SwipeRefreshLayout refreshLayout,LoadMoreListView recordListView,TextView emptyView){
        this.refreshLayout = refreshLayout;
        this.recordListView = recordListView;
        this.emptyView = emptyView;
        redPacketRecords = new ArrayList<>();
        redPacketBalanceRecordAdapter = new RedPacketBalanceRecordAdapter(context,redPacketRecords);
        recordListView.setAdapter(redPacketBalanceRecordAdapter);
        setListener();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(true);
                getRedPacketBalanceRecordMethod(0);
            }
        }, 500);

    }

    /**
     * set refresh listener
     * */
    private void setListener(){
        refreshLayout.setOnRefreshListener(this);
        recordListView.setOnRefreshListener(this);
    }

    /**
     * red packet recode
     * */
    public void rechargeMethod(){
        context.startActivity(new Intent(context,RedPacketRecharge.class));
        Utils.openNewActivityAnim((Activity) context,false);
    }

    /**
     * red packet withdraw method
     * */
    public void withDrawMethod(){
        context.startActivity(new Intent(context,RedPacketWithdraw.class));
        Utils.openNewActivityAnim((Activity) context,false);
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
        isLoadingData=true;
        oldPage= page;
        NetRequestImpl.getInstance().getRedPacketBalanceRecord("", new RequestListener() {
            @Override
            public void start() {

            }

            @Override
            public void success(JSONObject response) {
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
                    redPacketBalanceRecordAdapter.resetSource(redPacketRecords);
                } else {
                    MyToast.showToast(context,context.getString(R.string.red_balance_record_empty));
                }
                isLoadingData=false;
                refreshLayout.setRefreshing(false);
                if (jsonArray!=null&&jsonArray.length()>=10) {
                    recordListView.resetFooterState(true);
                } else {
                    recordListView.resetFooterState(false);
                }
                checkListEmpty();
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                isLoadingData=false;
                refreshLayout.setRefreshing(false);
                MyToast.showToast(context,errorMsg);
                checkListEmpty();
            }
        });
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
