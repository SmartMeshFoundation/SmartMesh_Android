package com.lingtuan.firefly.fragment;

import com.lingtuan.firefly.contact.vo.NewContactVO;
import com.lingtuan.firefly.custom.contact.PinYin;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.network.NetRequestImpl;
import com.lingtuan.firefly.vo.UserBaseVo;
import com.lingtuan.firefly.vo.UserInfoVo;

import org.json.JSONObject;

import java.util.List;

public class MainContactPresenter implements MainContactContract.Presenter{

    private MainContactContract.View mView;

    public MainContactPresenter(MainContactContract.View view){
        this.mView = view;
        mView.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void addOfflineFriend(String flocalids) {
        NetRequestImpl.getInstance().addOfflineFriend(flocalids, new RequestListener() {
            @Override
            public void start() {

            }

            @Override
            public void success(JSONObject response) {
                loadFriends();
            }

            @Override
            public void error(int errorCode, String errorMsg) {

            }
        });
    }

    @Override
    public void loadFriends() {
        NetRequestImpl.getInstance().loadFriends(new RequestListener() {
            @Override
            public void start() {

            }

            @Override
            public void success(JSONObject response) {
                mView.loadFriendsSuccess(response);
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                mView.loadFriendsError(errorCode,errorMsg);
            }
        });
    }

    @Override
    public void deleteFriends(final String uid) {
        NetRequestImpl.getInstance().deleteFriends(uid, new RequestListener() {
            @Override
            public void start() {

            }

            @Override
            public void success(JSONObject response) {
                mView.deleteFriendsSuccess(response.optString("msg"),uid);
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                mView.deleteFriendsError(errorCode,errorMsg);
            }
        });
    }

    @Override
    public void createDiscussionGroup(String tolocalids, final List<UserBaseVo> member) {
        NetRequestImpl.getInstance().createDiscussionGroups(tolocalids, new RequestListener() {
            @Override
            public void start() {
                mView.createDiscussionGroupStart();
            }
            @Override
            public void success(JSONObject response) {
                mView.createDiscussionGroupSuccess(response.optString("msg"),response.optString("cid"), member);
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                mView.createDiscussionGroupError(errorCode,errorMsg);
            }
        });
    }

    @Override
    public NewContactVO getNewContact(UserBaseVo voT) {
        NewContactVO info = new NewContactVO();
        info.setAge(voT.getAge());
        info.setDistance(voT.getDistance());
        info.setFriendLog(voT.getFriendLog());
        info.setGender(voT.getGender());
        info.setSightml(voT.getSightml());
        info.setThumb(voT.getThumb());
        info.setLocalId(voT.getLocalId());
        info.setUsername(voT.getUserName());
        info.setNote(voT.getNote());
        info.setLogintime(voT.getLogintime());
        info.setAddress(voT.getAddress());
        info.setFullName(PinYin.getPinYin(voT.getShowName()));
        info.setMid(voT.getMid());
        info.setOffLine(voT.isOffLine());
        return info;
    }
}
