package com.lingtuan.firefly.setting.contract;


import com.lingtuan.firefly.base.BasePresenter;
import com.lingtuan.firefly.base.BaseView;
import com.lingtuan.firefly.vo.UserInfoVo;

import java.util.List;

public interface BlackListContract {

    interface Presenter extends BasePresenter {
        void getBlackList(int page,List<UserInfoVo> source);
        void updateBlackState(int state,int position,String blocalid);
    }

    interface View extends BaseView<Presenter> {
        void getBlackSuccess(List<UserInfoVo> source, int currentPage,boolean showLoadMore);
        void getBlackError(int errorCode, String errorMsg);

        void updateBlackStart();
        void updateBlackSuccess(String message,int position);
        void updateBlackError(int errorCode, String errorMsg);
    }
}
