package com.lingtuan.firefly.tool;

import android.view.View;

import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.R;

public class SharedWifiSetActivity extends BaseActivity<SharedWifiSetContract.Presenter> implements SharedWifiSetContract.View {
    @Override
    public int getLayoutId() {
        return R.layout.activity_shared_wifi_set;
    }

    @Override
    public SharedWifiSetPresenter createPresenter() {
        return new SharedWifiSetPresenter(this);
    }

    @Override
    protected void initData() {
        mTitle.setText(getResources().getString(R.string.shared_wifi_set_title));
        mTitleRight.setVisibility(View.VISIBLE);
        mTitleRight.setText(getResources().getString(R.string.visitor_wifi_set_title_right));

    }

    @Override
    public void onResult(Object result, String message) {

    }

    @Override
    public void onError(Throwable throwable, String message) {

    }
}
