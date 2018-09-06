package com.lingtuan.firefly.setting.presenter;

import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.network.NetRequestImpl;
import com.lingtuan.firefly.setting.contract.BindMobileContract;
import com.lingtuan.firefly.util.Utils;

import org.json.JSONObject;

public class BindMobilePresenterImpl implements BindMobileContract.Presenter{

    private BindMobileContract.View mView;

    public BindMobilePresenterImpl(BindMobileContract.View view){
        this.mView = view;
        mView.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void sendSms(final int type,final String phoneNumber) {
        NetRequestImpl.getInstance().sendSmsc(Utils.makeRandomKey(16), type == 0 ? 0 : 1,phoneNumber,new RequestListener() {
            @Override
            public void start() {
                mView.sendSmsStart();
            }

            @Override
            public void success(JSONObject response) {
                mView.sendSmsSuccess(response.optString("msg"),type,phoneNumber);
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                mView.sendSmsError(errorCode,errorMsg);
            }
        });
    }
}
