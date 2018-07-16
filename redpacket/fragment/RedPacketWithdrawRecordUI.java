package com.lingtuan.firefly.redpacket.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseFragment;
import com.lingtuan.firefly.custom.LoadMoreListView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class RedPacketWithdrawRecordUI extends BaseFragment {
    private View view = null;
    private boolean isDataFirstLoaded;
    private Unbinder unbinder;

    @BindView(R.id.emptyView)
    TextView emptyView;
    @BindView(R.id.refreshListView)
    LoadMoreListView refreshListView;
    @BindView(R.id.swipe_container)
    SwipeRefreshLayout swipeContainer;

    private RedPacketRecordPresenter redPacketRecordPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        isDataFirstLoaded = true;
        super.onCreate(savedInstanceState);
        setRetainInstance(false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (!isDataFirstLoaded && view != null) {
            ((ViewGroup) view.getParent()).removeView(view);
            return view;
        }
        view = inflater.inflate(R.layout.loadmore_list_notitle_layout, container, false);
        unbinder = ButterKnife.bind(this, view);
        initData();
        return view;
    }

    @SuppressLint("HandlerLeak")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!isDataFirstLoaded) {
            return;
        }
        isDataFirstLoaded = false;
    }

    private void initData() {
        redPacketRecordPresenter = new RedPacketRecordPresenter(getActivity());
        redPacketRecordPresenter.init(swipeContainer,refreshListView,emptyView);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
