package com.lingtuan.firefly.setting.contract;

import com.lingtuan.firefly.base.BasePresenter;
import com.lingtuan.firefly.base.BaseView;

public interface BindMobileContract {

    interface Presenter extends BasePresenter {

        /**
         * send sms
         * @param type   0 binding mobile, 1 binding email, 2 phone number retrieve password, 3 email retrieve password
         * @param phoneNumber    phone number
         * */
        void sendSms(int type,String phoneNumber);
    }

    interface View extends BaseView<Presenter> {

        /**
         * send sms start
         * */
        void sendSmsStart();

        /**
         * send sms success
         * @param message   success message
         * @param type      0 binding mobile, 1 binding email, 2 phone number retrieve password, 3 email retrieve password
         * @param phoneNumber   phone number
         * */
        void sendSmsSuccess(String message,int type,String phoneNumber);

        /**
         * send sms error
         * @param errorCode  error code
         * @param errorMsg   error message
         * */
        void sendSmsError(int errorCode, String errorMsg);
    }
}
