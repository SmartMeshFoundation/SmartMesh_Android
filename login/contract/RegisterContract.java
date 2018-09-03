package com.lingtuan.firefly.login.contract;

import com.lingtuan.firefly.base.BasePresenter;
import com.lingtuan.firefly.base.BaseView;

public interface RegisterContract {

    interface Presenter extends BasePresenter {
        void smsForget(String phoneNumber,String password);
        void emailForget(String email,String password);
    }

    interface View extends BaseView<Presenter> {

        void retrievePasswordStart();
        void retrievePasswordSuccess(String message);
        void retrievePasswordError(int errorCode, String errorMsg);
    }
}
