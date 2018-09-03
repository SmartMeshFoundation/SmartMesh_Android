package com.lingtuan.firefly.login.presenter;

import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.login.contract.RegisterContract;
import com.lingtuan.firefly.network.NetRequestImpl;

import org.json.JSONObject;

public class RegisterPresenterImpl implements RegisterContract.Presenter {

    private RegisterContract.View mView;

    public RegisterPresenterImpl(RegisterContract.View view){
        this.mView = view;
        mView.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void smsForget(String phoneNumber, String password) {
        NetRequestImpl.getInstance().smscForget(phoneNumber, password, new RequestListener() {
            @Override
            public void start() {
                mView.retrievePasswordStart();
            }

            @Override
            public void success(JSONObject response) {
                mView.retrievePasswordSuccess(response.optString("msg"));
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                mView.retrievePasswordError(errorCode,errorMsg);
            }
        });
    }

    @Override
    public void emailForget(String email, String password) {
        NetRequestImpl.getInstance().emailForget(email, password, new RequestListener() {
            @Override
            public void start() {
                mView.retrievePasswordStart();
            }

            @Override
            public void success(JSONObject response) {
                mView.retrievePasswordSuccess(response.optString("msg"));
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                mView.retrievePasswordError(errorCode,errorMsg);
            }
        });
    }
}
