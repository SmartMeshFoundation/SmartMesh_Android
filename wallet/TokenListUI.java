package com.lingtuan.firefly.wallet;

import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.custom.LoadMoreListView;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;
import com.lingtuan.firefly.wallet.vo.TokenVo;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created on 2018/3/19.
 *
 */

public class TokenListUI extends BaseActivity implements AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener, LoadMoreListView.RefreshListener {

    private TextView emptyTextView;
    private RelativeLayout emptyRela;

    private LoadMoreListView tokenListView = null;

    /** Refresh the controls */
    private SwipeRefreshLayout swipeLayout;

    private boolean isLoadingData = false;

    private TokenListAdapter tokenListAdapter = null;

    private int currentPage = 1 ;
    private int oldPage=1;
    private ArrayList<TokenVo> source = null ;

    @Override
    protected void setContentView() {
        setContentView(R.layout.token_list_layout);
    }

    @Override
    protected void findViewById() {
        tokenListView = (LoadMoreListView) findViewById(R.id.refreshListView);
        tokenListView.setOnScrollListener(new PauseOnScrollListener(NextApplication.mImageLoader, true, true));
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);

        emptyRela = (RelativeLayout) findViewById(R.id.empty_like_rela);
        emptyTextView = (TextView) findViewById(R.id.empty_text);
    }

    @Override
    protected void setListener() {
        tokenListView.setOnRefreshListener(this);
        swipeLayout.setOnRefreshListener(this);
        tokenListView.setOnItemClickListener(this);
    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.black_list));
        swipeLayout.setColorSchemeResources(R.color.black);
        source = new ArrayList<>();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(true);
                loadTokenList(1);
            }
        }, 500);
        tokenListAdapter = new TokenListAdapter(this, source);
        tokenListView.setAdapter(tokenListAdapter);
    }

    private void loadTokenList(int page) {
        if(isLoadingData){
            return;
        }
        isLoadingData=true;
        oldPage= page;

        NetRequestImpl.getInstance().getBlackList(page, new RequestListener() {
            @Override
            public void start() {

            }

            @Override
            public void success(JSONObject response) {
                currentPage=oldPage;
                if (currentPage == 1) {
                    source.clear();
                }

                JSONArray jsonArray = response.optJSONArray("data");
                if (jsonArray != null) {
                    int count = jsonArray.length();
                    for (int i = 0; i < count; i++) {
                        TokenVo tokenVo = new TokenVo().parse(jsonArray.optJSONObject(i));
                        source.add(tokenVo);
                    }
                    tokenListAdapter.resetSource(source);
                } else {
                    showToast(getString(R.string.black_list_empty));
                }
                isLoadingData=false;
                swipeLayout.setRefreshing(false);
                if (jsonArray!=null&&jsonArray.length()>=10) {
                    tokenListView.resetFooterState(true);
                } else {
                    tokenListView.resetFooterState(false);
                }
                checkListEmpty();
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                isLoadingData=false;
                swipeLayout.setRefreshing(false);
                showToast(errorMsg);
                checkListEmpty();
            }
        });
    }

    /**
     * To test whether the current list is empty
     */
    private void checkListEmpty() {
        if(source == null || source.size() == 0){
            emptyRela.setVisibility(View.VISIBLE);
            emptyTextView.setText(R.string.black_list_empty);
        }else{
            emptyRela.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onRefresh() {
        loadTokenList(1);
    }

    @Override
    public void loadMore() {
        loadTokenList(currentPage + 1);
    }
}
