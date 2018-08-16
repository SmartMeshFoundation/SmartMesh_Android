package com.lingtuan.firefly.contact.contract;

import com.lingtuan.firefly.base.BasePresenter;
import com.lingtuan.firefly.base.BaseView;
import com.lingtuan.firefly.vo.UserBaseVo;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public interface DiscussGroupSettingContract {

    interface Presenter extends BasePresenter {

        /**
         * get group members
         * @param cid   group id
         * */
        void getDiscussMembers(int cid);

        /**
         * rename group
         * @param groupName  group name
         * @param cid         group id
         * */
        void discussGroupRename(String groupName ,int cid);

        /**
         * rename group message
         * @param groupName       group name
         * @param data             group members
         * @param cid              group id
         * @param content          message content
         * */
        void discussGroupRenameMessage(String groupName, List<UserBaseVo> data, int cid, String content);

        /**
         * add group member
         * @param continueList  group members
         * @param cid            group id
         * */
        void addMembersRequest(ArrayList<UserBaseVo> continueList, int cid);

        /**
         * add group member message
         * @param groupName       group name
         * @param data             group members
         * @param cid              group id
         * @param content          message content
         * */
        void inviteOthersChatMsg(String groupName,List<UserBaseVo> data,int cid,String content);

        /**
         * remove group member message
         * @param groupName       group name
         * @param data             group members
         * @param cid              group id
         * @param content          message content
         * */
        void removeMemberChatMsg(String groupName,List<UserBaseVo> data,int cid,String content);

        /**
         * remover member
         * @param position   member
         * @param aimid      member id
         * @param cid        group id
         * */
        void removeMember(int position,String aimid,int cid);

        /**
         * message notify
         * @param isChecked   true not notify    false  notify
         * */
        void switchNotify(boolean isChecked,int cid);

        /**
         * quit group
         * @param cid  group id
         * */
        void quitGroup(int cid);
    }

    interface View extends BaseView<Presenter> {
        void getDiscussMembersSuccess(JSONObject object);

        void discussGroupRenameStart();
        void discussGroupRenameSuccess(String name,String message);
        void discussGroupRenameError(int errorCode, String errorMsg);

        void addDiscussMembersStart();
        void addDiscussMembersSuccess(String message,ArrayList<UserBaseVo> continueList,String name);
        void addDiscussMembersError(int errorCode, String errorMsg);

        void removeMemberStart();
        void removeMemberSuccess(int position,String message);
        void removeMemberError(int errorCode, String errorMsg);

        void switchNotifySuccess(String message);
        void switchNotifyError(int errorCode, String errorMsg);

        void quitGroupStart();
        void quitGroupSuccess(String message);
        void quitGroupError(int errorCode, String errorMsg);
    }
}
