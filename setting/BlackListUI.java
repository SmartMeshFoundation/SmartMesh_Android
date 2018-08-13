package com.lingtuan.firefly.setting;

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
import com.lingtuan.firefly.network.NetRequestImpl;
import com.lingtuan.firefly.setting.contract.BlackListContract;
import com.lingtuan.firefly.setting.contract.SettingContract;
import com.lingtuan.firefly.setting.presenter.BlackListPresenterImpl;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MyViewDialogFragment;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.vo.UserInfoVo;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnItemClick;
import butterknife.OnItemLongClick;

/**
 * Created on 2017/10/11.
 * The blacklist
 */

public class BlackListUI extends BaseActivity implements LoadMoreListView.RefreshListener, SwipeRefreshLayout.OnRefreshListener,BlackListContract.View{

    @BindView(R.id.empty_text)
    TextView emptyTextView;
    @BindView(R.id.empty_like_rela)
    RelativeLayout emptyRela;
    /** blacklisting */
    @BindView(R.id.refreshListView)
    LoadMoreListView blackLv;
    /** Refresh the controls */
    @BindView(R.id.swipe_container)
    SwipeRefreshLayout swipeLayout;

    private BlackListContract.Presenter mPresenter;

    private boolean isLoadingData = false;

    private BlackListAdapter blackAdapter = null;

    private int currentPage = 1 ;
    private List<UserInfoVo> source = null ;


    @Override
    protected void setContentView() {
        setContentView(R.layout.loadmore_list_layout);
    }

    @Override
    protected void findViewById() {

    }

    @Override
    protected void setListener() {
        blackLv.setOnRefreshListener(this);
        swipeLayout.setOnRefreshListener(this);
    }

    @Override
    protected void initData() {
        new BlackListPresenterImpl(this);
        source = new ArrayList<>();
        setTitle(getString(R.string.black_list));
        blackLv.setOnScrollListener(new PauseOnScrollListener(NextApplication.mImageLoader, true, true));
        swipeLayout.setColorSchemeResources(R.color.black);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(true);
                loadBlackList(1);
            }
        }, 500);
        blackAdapter = new BlackListAdapter(this, source);
        blackLv.setAdapter(blackAdapter);
    }

    @Override
    public void loadMore() {
        loadBlackList(currentPage + 1);
    }

    @Override
    public void onRefresh() {
        loadBlackList(1);
    }


    @OnItemClick(R.id.refreshListView)
    void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Utils.intentFriendUserInfo(this, source.get(position), false);
    }

    @OnItemLongClick(R.id.refreshListView)
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        MyViewDialogFragment mdf = new MyViewDialogFragment();
        mdf.setTitleAndContentText(getString(R.string.black_list_remove_title), getString(R.string.black_list_remove));
        mdf.setOkCallback(new MyViewDialogFragment.OkCallback() {
            @Override
            public void okBtn() {
                mPresenter.updateBlackState(1,position,source.get(position).getLocalId());
            }
        });
        mdf.show(getSupportFragmentManager(), "mdf");
        return true;
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

    /**
     * Load the data
     */
    private void loadBlackList(int page) {
        if(isLoadingData){
            return;
        }
        isLoadingData = true;
        mPresenter.getBlackList(page,source);
    }

    @Override
    public void setPresenter(BlackListContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public void getBlackSuccess(List<UserInfoVo> source, int currentPage,boolean showLoadMore) {
        this.source = source;
        this.currentPage = currentPage;
        blackAdapter.resetSource(source);
        isLoadingData=false;
        if (swipeLayout != null){
            swipeLayout.setRefreshing(false);
        }
        if (blackLv != null){
            if (showLoadMore) {
                blackLv.resetFooterState(true);
            } else {
                blackLv.resetFooterState(false);
            }
        }
        checkListEmpty();
    }

    @Override
    public void getBlackError(int errorCode, String errorMsg) {
        isLoadingData=false;
        if (swipeLayout != null){
            swipeLayout.setRefreshing(false);
        }
        showToast(errorMsg);
        checkListEmpty();
    }

    @Override
    public void updateBlackStart() {
        LoadingDialog.show(BlackListUI.this,"");
    }

    @Override
    public void updateBlackSuccess(String message, int position) {
        LoadingDialog.close();
        showToast(message);
        source.remove(position);
        blackAdapter.resetSource(source);
        checkListEmpty();
    }

    @Override
    public void updateBlackError(int errorCode, String errorMsg) {
        LoadingDialog.close();
        showToast(errorMsg);
    }
}
