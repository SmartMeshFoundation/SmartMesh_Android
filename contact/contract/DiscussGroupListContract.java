package com.lingtuan.firefly.contact.contract;

import com.lingtuan.firefly.base.BasePresenter;
import com.lingtuan.firefly.base.BaseView;
import com.lingtuan.firefly.vo.UserBaseVo;

import org.json.JSONObject;

import java.util.List;

public interface DiscussGroupListContract {

    interface Presenter extends BasePresenter {

        void createDiscussionGroup(String touids, final List<UserBaseVo> member);

        void loadGroupList();

    }

    interface View extends BaseView<Presenter> {
        void createDiscussionGroupStart();
        void createDiscussionGroupSuccess(String message,String cid,List<UserBaseVo> member);
        void createDiscussionGroupError(int errorCode, String errorMsg);

        void loadGroupListSuccess(JSONObject response);
        void loadGroupListError(int errorCode, String errorMsg);
    }
}
