package com.lingtuan.firefly.setting.contract;

import com.lingtuan.firefly.base.BasePresenter;
import com.lingtuan.firefly.base.BaseView;

public interface BindMobileCodeContract {

    interface Presenter extends BasePresenter{

        /**
         * verify sms
         * @param code message code
         * @param phoneNumber phone number
         * */
        void verifySms(String code,String phoneNumber);

        /**
         * verify email
         * @param code message code
         * @param email email address
         * */
        void verifyEmail(String code,String email);

        /**
         * bind mobile
         * @param phoneNumber mobile number
         * */
        void bindMobile(String phoneNumber);

        /**
         * bind email
         * @param email email address
         * */
        void bindEmail(String email);
    }

    interface View extends BaseView<Presenter>{

        /**
         * loading start
         * */
        void start();

        /**
         * verify sms success
         * */
        void verifySmsSuccess();

        /**
         * verify email success
         * */
        void verifyEmailSuccess();

        /**
         * bind success
         * @param message success message
         * @param isEmail is bind email  /true is bind email  /false is bind mobile
         * */
        void bindSuccess(String message,boolean isEmail);

        /**
         * loading error
         * @param errorCode error code
         * @param errorMsg error message
         * */
        void error(int errorCode, String errorMsg );
    }

}
