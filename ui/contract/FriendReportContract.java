package com.lingtuan.firefly.ui.contract;

import com.lingtuan.firefly.base.BasePresenter;
import com.lingtuan.firefly.base.BaseView;

public interface FriendReportContract {

    interface Presenter extends BasePresenter {

        /**
         * add black
         * @param state           0 add black
         * @param blocalid        friend id
         * */
        void updateBlackState(int state,String blocalid);

        /**
         * report friend
         * @param type            report type
         * @param blocalid       friend id
         * */
        void reportFriendMethod(int type,String blocalid);
    }

    interface View extends BaseView<Presenter> {

        /**
         * add black start
         * */
        void addBlackStart();

        /**
         * add black success
         * @param message    success message
         * */
        void addBlackSuccess(String message);

        /**
         * add black error
         * @param errorCode     error code
         * @param errorMsg      error message
         * */
        void addBlackError(int errorCode, String errorMsg);

        /**
         * report friend start
         * */
        void reportFriendStart();

        /**
         * report friend success
         * */
        void reportFriendSuccess();

        /**
         * report friend error
         * @param errorCode     error code
         * @param errorMsg      error message
         * */
        void reportFriendError(int errorCode, String errorMsg);
    }
}
