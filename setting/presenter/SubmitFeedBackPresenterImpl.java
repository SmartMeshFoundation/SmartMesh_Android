package com.lingtuan.firefly.setting.presenter;

import android.text.TextUtils;

import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.network.NetRequestImpl;
import com.lingtuan.firefly.setting.contract.SubmitFeedBackContract;

import org.json.JSONObject;

public class SubmitFeedBackPresenterImpl implements SubmitFeedBackContract.Presenter{

    private SubmitFeedBackContract.View mView;

    public SubmitFeedBackPresenterImpl(SubmitFeedBackContract.View view){
        this.mView = view;
        mView.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public boolean isEmail(String strEmail) {
        String strPattern = "^[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]@[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]$";
        return !TextUtils.isEmpty(strPattern) && strEmail.matches(strPattern);
    }

    @Override
    public void feedBack(String content, String email, int type) {
        NetRequestImpl.getInstance().feedBack(content, email, type, new RequestListener() {
            @Override
            public void start() {
                mView.feedBackStart();
            }

            @Override
            public void success(JSONObject response) {
               mView.feedBackSuccess();
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                mView.feedBackError();
            }
        });
    }
}
