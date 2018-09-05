package com.lingtuan.firefly.fragment;

import com.lingtuan.firefly.base.BasePresenter;
import com.lingtuan.firefly.base.BaseView;
import com.lingtuan.firefly.contact.vo.NewContactVO;
import com.lingtuan.firefly.vo.UserBaseVo;

import org.json.JSONObject;

import java.util.List;

public interface MainContactContract {

    interface Presenter extends BasePresenter {

        /**
         * add offline friend
         * @param flocalids  user id
         * */
        void addOfflineFriend(String flocalids);

        /**
         * load friend list
         * */
        void loadFriends();

        /**
         * delete friend
         * @param uid  user id
         * */
        void deleteFriends(String uid);

        /**
         * create group
         * @param tolocalids    user id
         * @param member        user list
         * */
        void createDiscussionGroup(String tolocalids,List<UserBaseVo>member);

        /**
         * get user info
         * @param userInfoVo   user info
         * */
        NewContactVO getNewContact(UserBaseVo userInfoVo);
    }

    interface View extends BaseView<Presenter> {

        /**
         * load friend success
         * @param response    response
         * */
        void loadFriendsSuccess(JSONObject response);

        /**
         * load friend error
         * @param errorCode    error code
         * @param errorMsg     error message
         * */
        void loadFriendsError(int errorCode, String errorMsg);

        /**
         * delete friend success
         * @param uid      user id
         * @param message  success message
         * */
        void deleteFriendsSuccess(String message,String uid);

        /**
         * delete friend error
         * @param errorCode  error code
         * @param errorMsg   error message
         * */
        void deleteFriendsError(int errorCode, String errorMsg);

        /**
         * create group start
         * */
        void createDiscussionGroupStart();

        /**
         * create group success
         * @param message  success message
         * @param cid       group id
         * @param member   user list
         * */
        void createDiscussionGroupSuccess(String message,String cid,List<UserBaseVo> member);

        /**
         * create group error
         * @param errorCode   error code
         * @param errorMsg    error message
         * */
        void createDiscussionGroupError(int errorCode, String errorMsg);
    }
}
