package com.lingtuan.firefly.chat.presenter;

import com.lingtuan.firefly.chat.contract.ChattingSetContract;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.network.NetRequestImpl;

import org.json.JSONObject;

public class ChattingSetPresenterImpl implements ChattingSetContract.Presenter {

    private ChattingSetContract.View mView;

    public ChattingSetPresenterImpl(ChattingSetContract.View view){
        this.mView = view;
        mView.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void getMaskUser(String uid) {
        NetRequestImpl.getInstance().getMaskUser(uid, new RequestListener() {
            @Override
            public void start() {

            }
            @Override
            public void success(JSONObject response) {
                JSONObject result = response.optJSONObject("data");
                if (result != null) {
                    mView.getMaskUserSuccess(result.optString("status"));
                }
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                mView.getMaskUserError(errorCode,errorMsg);
            }
        });
    }

    @Override
    public void maskUser(String uid ,final boolean isChecked) {
        NetRequestImpl.getInstance().maskUser(uid, isChecked, new RequestListener() {
            @Override
            public void start() {

            }

            @Override
            public void success(JSONObject response) {
                mView.maskUserSuccess(isChecked);
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                mView.maskUserError(errorCode,errorMsg,isChecked);
            }
        });
    }
}
