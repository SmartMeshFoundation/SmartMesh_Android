package com.lingtuan.firefly.contact.contract;

import com.lingtuan.firefly.base.BasePresenter;
import com.lingtuan.firefly.base.BaseView;
import com.lingtuan.firefly.vo.UserBaseVo;

import java.util.ArrayList;
import java.util.List;

public interface DiscussGroupMemberListContract {

    interface Presenter extends BasePresenter {

        void removeDiscussMember(int position,String aimid,int cid);

        void addMembersRequest(ArrayList<UserBaseVo> continueList, int cid);

        void inviteOthersChatMsg(String name,List<UserBaseVo> data,int cid,String content);

        void removeMemberChatMsg(String name,List<UserBaseVo> data,int cid,String content);
    }

    interface View extends BaseView<Presenter> {
        void removeDiscussMemberStart();
        void removeDiscussMemberSuccess(int position,String message);
        void removeDiscussMemberError(int errorCode, String errorMsg);

        void addDiscussMembersStart();
        void addDiscussMembersSuccess(String message,ArrayList<UserBaseVo> continueList,String name);
        void addDiscussMembersError(int errorCode, String errorMsg);
    }
}
