package com.lingtuan.firefly.redpacket;

import android.content.Context;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.custom.LoadMoreListView;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.redpacket.bean.RedPacketMessageBean;
import com.lingtuan.firefly.redpacket.bean.RedPacketRecordBean;
import com.lingtuan.firefly.redpacket.listener.SetOnClickListener;
import com.lingtuan.firefly.util.MyToast;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class RedPacketMessagePresenter implements SwipeRefreshLayout.OnRefreshListener, LoadMoreListView.RefreshListener, SetOnClickListener {

    private Context context;
    private SwipeRefreshLayout refreshLayout;
    private LoadMoreListView messageListView;
    private TextView emptyView;
    private ArrayList<RedPacketMessageBean> redPacketMessages;
    private RedPacketMessageAdapter redPacketMessageAdapter;

    private boolean isLoadingData = false;
    private int currentPage = 1 ;
    private int oldPage=1;

    public RedPacketMessagePresenter(Context context){
        this.context = context;
    }

    public void init(final SwipeRefreshLayout refreshLayout,LoadMoreListView messageListView,TextView emptyView){
        this.refreshLayout = refreshLayout;
        this.messageListView = messageListView;
        this.emptyView = emptyView;
        redPacketMessages = new ArrayList<>();
        redPacketMessageAdapter = new RedPacketMessageAdapter(context,redPacketMessages,this);
        messageListView.setAdapter(redPacketMessageAdapter);
        setListener();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(true);
                getRedPacketMessageMethod(0);
            }
        }, 500);
    }

    /**
     * set refresh listener
     * */
    private void setListener(){
        refreshLayout.setOnRefreshListener(this);
        messageListView.setOnRefreshListener(this);
    }


    @Override
    public void onRefresh() {

    }

    @Override
    public void loadMore() {

    }

    /**
     * get red packet balance record
     * */
    private void getRedPacketMessageMethod(int page){
        if(isLoadingData){
            return;
        }
        isLoadingData=true;
        oldPage= page;
        NetRequestImpl.getInstance().getRedPacketMessages("", new RequestListener() {
            @Override
            public void start() {

            }

            @Override
            public void success(JSONObject response) {
                parseData(response);
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
            redPacketMessageAdapter.resetSource(redPacketMessages);
        } else {
            MyToast.showToast(context,context.getString(R.string.red_balance_record_empty));
        }
        isLoadingData=false;
        refreshLayout.setRefreshing(false);
        if (jsonArray != null && jsonArray.length() >= 10) {
            messageListView.resetFooterState(true);
        } else {
            messageListView.resetFooterState(false);
        }
        checkListEmpty();
    }

    /**
     * check messages is empty
     * */
    private void checkListEmpty(){
        if(redPacketMessages == null || redPacketMessages.size() == 0){
            emptyView.setVisibility(View.VISIBLE);
        }else{
            emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemClickListener(int position) {
        MyToast.showToast(context,"点击了第" + position + "几项");
    }
}
