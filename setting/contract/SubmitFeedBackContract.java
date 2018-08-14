package com.lingtuan.firefly.setting.contract;

import com.lingtuan.firefly.base.BasePresenter;
import com.lingtuan.firefly.base.BaseView;

public interface SubmitFeedBackContract {

    interface Presenter extends BasePresenter {

        /**
         * check is email
         * @param strEmail  string
         * */
        boolean isEmail(String strEmail);

        /**
         * feed back method
         * @param content    feed back message
         * @param email      user email
         * @param type       feed back type    0 problem  1 suggest
         * */
        void feedBack(String content,String email,int type);

    }

    interface View extends BaseView<Presenter> {

        void feedBackStart();

        void feedBackSuccess();

        void feedBackError();

    }
}
