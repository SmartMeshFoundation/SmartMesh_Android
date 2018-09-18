package com.lingtuan.firefly.setting.contract;

import com.lingtuan.firefly.base.BasePresenter;
import com.lingtuan.firefly.base.BaseView;

public interface SettingContract {

    interface Presenter extends BasePresenter{

        /**
         * get user agreement
         * @return  agreement
         * */
        String getUserAgreement();

        /**
         * log out method
         * */
        void logOutMethod();

        /**
         * check version
         * */
        void checkVersion();

    }

    interface View extends BaseView<Presenter>{

        /**
         * start
         * */
        void start();

        /**
         * success
         * */
        void success();

        /**
         * send broadcast
         * */
        void updateVersion(String version,String url);

        /**
         * error
         * @param errorCode error code
         * @param errorMsg error message
         * @param exitApp exit app
         * */
        void error(int errorCode, String errorMsg,boolean exitApp);
    }
}
