package com.lingtuan.firefly.ui.presenter;

import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.network.NetRequestImpl;
import com.lingtuan.firefly.ui.contract.FriendReportContract;

import org.json.JSONObject;

public class FriendReportPresenterImpl implements FriendReportContract.Presenter {

    private FriendReportContract.View mView;

    public FriendReportPresenterImpl(FriendReportContract.View view){
        this.mView = view;
        mView.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void updateBlackState(int state, final String blocalid) {
        NetRequestImpl.getInstance().updateBlackState(0, blocalid, new RequestListener() {
            @Override
            public void start() {
                mView.addBlackStart();
            }

            @Override
            public void success(JSONObject response) {
                mView.addBlackSuccess(response.optString("msg"));
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                mView.addBlackError(errorCode,errorMsg);
            }
        });
    }

    @Override
    public void reportFriendMethod(int type, String blocalid) {
        NetRequestImpl.getInstance().reportFriend(type, blocalid, new RequestListener() {
            @Override
            public void start() {
                mView.reportFriendStart();
            }

            @Override
            public void success(JSONObject response) {
                mView.reportFriendSuccess();
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                mView.reportFriendError(errorCode,errorMsg);
            }
        });
    }
}
