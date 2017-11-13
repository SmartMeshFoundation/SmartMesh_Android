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
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MyViewDialogFragment;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;
import com.lingtuan.firefly.vo.UserInfoVo;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2017/10/11.
 * The blacklist
 */

public class BlackListUI extends BaseActivity implements LoadMoreListView.RefreshListener, SwipeRefreshLayout.OnRefreshListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private TextView emptyTextView;
    private RelativeLayout emptyRela;

    /** blacklisting */
    private LoadMoreListView blackLv = null;

    /** Refresh the controls */
    private SwipeRefreshLayout swipeLayout;

    private boolean isLoadingData = false;

    private BlackListAdapter blackAdapter = null;

    private int currentPage = 1 ;
    private int oldPage=1;
    private List<UserInfoVo> source = null ;


    @Override
    protected void setContentView() {
        setContentView(R.layout.loadmore_list_layout);
    }

    @Override
    protected void findViewById() {
        blackLv = (LoadMoreListView) findViewById(R.id.refreshListView);
        blackLv.setOnScrollListener(new PauseOnScrollListener(NextApplication.mImageLoader, true, true));
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);

        emptyRela = (RelativeLayout) findViewById(R.id.empty_like_rela);
        emptyTextView = (TextView) findViewById(R.id.empty_text);
    }

    @Override
    protected void setListener() {
        blackLv.setOnRefreshListener(this);
        swipeLayout.setOnRefreshListener(this);
        blackLv.setOnItemClickListener(this);
        blackLv.setOnItemLongClickListener(this);
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
                loadBlackList(1);
            }
        }, 500);
        blackAdapter = new BlackListAdapter(this, source);
        blackLv.setAdapter(blackAdapter);
    }

    /**
     * Load the data
     */
    private void loadBlackList(int page) {
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
                        UserInfoVo uInfo = new UserInfoVo().parse(jsonArray.optJSONObject(i));
                        source.add(uInfo);
                    }
                    blackAdapter.resetSource(source);
                } else {
                    showToast(getString(R.string.black_list_empty));
                }
                isLoadingData=false;
                swipeLayout.setRefreshing(false);
                if (jsonArray!=null&&jsonArray.length()>=10) {
                    blackLv.resetFooterState(true);
                } else {
                    blackLv.resetFooterState(false);
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


    @Override
    public void loadMore() {
        loadBlackList(currentPage + 1);
    }

    @Override
    public void onRefresh() {
        loadBlackList(1);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Utils.intentFriendUserInfo(this, source.get(position), false);
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
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        MyViewDialogFragment mdf = new MyViewDialogFragment();
        mdf.setTitleAndContentText(getString(R.string.black_list_remove_title), getString(R.string.black_list_remove));
        mdf.setOkCallback(new MyViewDialogFragment.OkCallback() {
            @Override
            public void okBtn() {
                removeBlackListMethod(position);
            }
        });
        mdf.show(getSupportFragmentManager(), "mdf");
        return true;
    }

    /**
     * remove blacklist users
     * @ param position want to remove the user
     * */
    private void removeBlackListMethod(final int position) {
        NetRequestImpl.getInstance().updateBlackState(1,source.get(position).getLocalId(), new RequestListener() {
            @Override
            public void start() {
                LoadingDialog.show(BlackListUI.this,"");
            }

            @Override
            public void success(JSONObject response) {
                LoadingDialog.close();
                showToast(response.optString("msg"));
                source.remove(position);
                blackAdapter.resetSource(source);
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                LoadingDialog.close();
                showToast(errorMsg);
            }
        });
    }
}
