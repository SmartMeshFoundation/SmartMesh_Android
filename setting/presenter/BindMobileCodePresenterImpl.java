package com.lingtuan.firefly.setting.presenter;

import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.setting.contract.BindMobileCodeContract;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;

import org.json.JSONObject;

public class BindMobileCodePresenterImpl implements BindMobileCodeContract.Presenter{

    private BindMobileCodeContract.View mView;

    public BindMobileCodePresenterImpl(BindMobileCodeContract.View view){
        this.mView = view;
        mView.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void verifySms(String code, String phoneNumber) {
        NetRequestImpl.getInstance().verifySmsc(code, phoneNumber, new RequestListener() {
            @Override
            public void start() {
                mView.start();
            }

            @Override
            public void success(JSONObject response) {
                mView.verifySmsSuccess();
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                mView.error(errorCode,errorMsg);
            }
        });
    }

    @Override
    public void verifyEmail(String code, String email) {
        NetRequestImpl.getInstance().verifyMail(code, email, new RequestListener() {
            @Override
            public void start() {
                mView.start();
            }

            @Override
            public void success(JSONObject response) {
                mView.verifyEmailSuccess();
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                mView.error(errorCode,errorMsg);
            }
        });
    }

    @Override
    public void bindMobile(String phoneNumber) {
        NetRequestImpl.getInstance().bindMobile(phoneNumber, new RequestListener() {
            @Override
            public void start() {

            }

            @Override
            public void success(JSONObject response) {
                mView.bindSuccess(response.optString("msg"),false);
            }

            @Override
            public void error(int errorCode, String errorMsg) {
               mView.error(errorCode,errorMsg);
            }
        });
    }

    @Override
    public void bindEmail(String email) {
        NetRequestImpl.getInstance().bindEmail(email, new RequestListener() {
            @Override
            public void start() {

            }

            @Override
            public void success(JSONObject response) {
                mView.bindSuccess(response.optString("msg"),true);
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                mView.error(errorCode,errorMsg);
            }
        });
    }
}
