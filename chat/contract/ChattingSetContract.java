package com.lingtuan.firefly.chat.contract;

import com.lingtuan.firefly.base.BasePresenter;
import com.lingtuan.firefly.base.BaseView;

public interface ChattingSetContract {

    interface Presenter extends BasePresenter {

        void getMaskUser(String uid);

        void maskUser(String uid ,boolean isChecked);
    }

    interface View extends BaseView<Presenter> {

        void getMaskUserSuccess(String statusString);
        void getMaskUserError(int errorCode, String errorMsg);

        void maskUserSuccess(boolean isChecked);
        void maskUserError(int errorCode, String errorMsg,boolean isChecked);
    }
}
