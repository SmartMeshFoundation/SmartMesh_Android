package com.lingtuan.firefly.contact.contract;

import com.lingtuan.firefly.base.BasePresenter;
import com.lingtuan.firefly.base.BaseView;
import com.lingtuan.firefly.contact.vo.DiscussionGroupsVo;
import com.lingtuan.firefly.vo.UserBaseVo;

import java.util.ArrayList;

public interface DiscussGroupJoinContract {

    interface Presenter extends BasePresenter {

        /**
         * get group members
         * @param cid     group id
         * @param data    members
         * */
        void getDiscussMembers(String cid,ArrayList<UserBaseVo> data);

        /**
         * join group
         * @param cid     group id
         * @param data    members
         * */
        void joinDiscussGroup(String cid,ArrayList<UserBaseVo> data);
    }

    interface View extends BaseView<Presenter> {

        /**
         * get group members success
         * @param vo        group info
         * @param data      group data
         * */
        void getDiscussMembersSuccess(DiscussionGroupsVo vo,ArrayList<UserBaseVo> data);

        /**
         * get group members error
         * @param errorCode     error code
         * @param errorMsg      error message
         * */
        void getDiscussMembersError(int errorCode, String errorMsg);

        /**
         * join group start
         * */
        void joinDiscussGroupStart();

        /**
         * join group success
         * */
        void joinDiscussGroupSuccess();

        /**
         * join group error
         * @param errorCode error code
         * @param errorMsg  error message
         * */
        void joinDiscussGroupError(int errorCode, String errorMsg);
    }
}
