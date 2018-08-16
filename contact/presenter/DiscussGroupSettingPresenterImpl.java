package com.lingtuan.firefly.contact.presenter;

import android.os.Bundle;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.contact.contract.DiscussGroupSettingContract;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.network.NetRequestImpl;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.vo.ChatMsg;
import com.lingtuan.firefly.vo.UserBaseVo;
import com.lingtuan.firefly.xmpp.XmppAction;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DiscussGroupSettingPresenterImpl implements DiscussGroupSettingContract.Presenter {

    private DiscussGroupSettingContract.View mView;

    public DiscussGroupSettingPresenterImpl(DiscussGroupSettingContract.View view){
        this.mView = view;
        mView.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void getDiscussMembers(int cid) {
        NetRequestImpl.getInstance().getDiscussMumbers(cid+"", new RequestListener() {
            @Override
            public void start() {

            }

            @Override
            public void success(JSONObject response) {
                mView.getDiscussMembersSuccess(response);
            }

            @Override
            public void error(int errorCode, String errorMsg) {

            }
        });
    }

    @Override
    public void discussGroupRename(final String groupName , int cid) {
        NetRequestImpl.getInstance().renameDicsuss(groupName,cid, new RequestListener() {
            @Override
            public void start() {
                mView.discussGroupRenameStart();
            }

            @Override
            public void success(JSONObject response) {
                mView.discussGroupRenameSuccess(groupName,response.optString("msg"));
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                mView.discussGroupRenameError(errorCode,errorMsg);
            }
        });
    }

    @Override
    public void discussGroupRenameMessage(String groupName, List<UserBaseVo> data, int cid, String content) {
        StringBuilder sb = new StringBuilder();
        int index=1;
        for (UserBaseVo vo : data) {
            if(index>4){//Most need four pictures
                break;
            }
            sb.append(vo.getThumb()).append("___").append(vo.getGender()).append("#");
            index++;
        }
        sb.deleteCharAt(sb.lastIndexOf("#"));
        String url = sb.toString();
        ChatMsg chatmsg = new ChatMsg();
        chatmsg.setChatId("group-" + cid);
        chatmsg.setGroupName(groupName);
        chatmsg.setGroup(true);
        chatmsg.setType(17);
        chatmsg.setSend(1);
        chatmsg.setContent(content);
        chatmsg.setMsgTime(System.currentTimeMillis() / 1000);
        chatmsg.setMessageId(UUID.randomUUID().toString());
        chatmsg.parseUserBaseVo(NextApplication.myInfo.getUserBaseVo());
        Bundle bundle = new Bundle();
        bundle.putSerializable(XmppAction.ACTION_MESSAGE_LISTENER, chatmsg);
        Utils.intentAction(NextApplication.mContext,XmppAction.ACTION_MESSAGE_LISTENER, bundle);
        FinalUserDataBase.getInstance().saveChatMsg(chatmsg, chatmsg.getChatId(), groupName, url, false);
    }

    @Override
    public void addMembersRequest(final ArrayList<UserBaseVo> continueList, int cid) {
        if (continueList.size() <= 0) {
            return;
        }
        StringBuilder touids = new StringBuilder();
        final StringBuilder tonames = new StringBuilder();
        for (int i = 0; i < continueList.size(); i++) {
            if (i == continueList.size() - 1) {
                touids.append(continueList.get(i).getLocalId());
                tonames.append(continueList.get(i).getShowName());
            } else {
                touids.append(continueList.get(i).getLocalId()).append(",");
                tonames.append(continueList.get(i).getShowName()).append(",");
            }
        }
        NetRequestImpl.getInstance().addDiscussMembers(touids.toString(), cid, new RequestListener() {
            @Override
            public void start() {
                mView.addDiscussMembersStart();
            }

            @Override
            public void success(JSONObject response) {
                mView.addDiscussMembersSuccess(response.optString("msg"),continueList,tonames.toString());
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                mView.addDiscussMembersError(errorCode,errorMsg);
            }
        });
    }

    @Override
    public void inviteOthersChatMsg(String groupName,List<UserBaseVo> data, int cid, String content) {
        StringBuilder sb = new StringBuilder();
        int index=1;
        for (UserBaseVo vo : data) {
            if(index>4){//Most need four pictures
                break;
            }
            sb.append(vo.getThumb()).append("___").append(vo.getGender()).append("#");
            index++;
        }
        sb.deleteCharAt(sb.lastIndexOf("#"));
        String url = sb.toString();
        ChatMsg chatmsg = new ChatMsg();
        chatmsg.setChatId("group-" + cid);
        chatmsg.setGroupName(groupName);
        chatmsg.setGroup(true);
        chatmsg.setType(13);
        chatmsg.setSend(1);
        chatmsg.setContent(content);
        chatmsg.setMsgTime(System.currentTimeMillis() / 1000);
        chatmsg.setMessageId(UUID.randomUUID().toString());
        chatmsg.parseUserBaseVo(NextApplication.myInfo.getUserBaseVo());
        Bundle bundle = new Bundle();
        bundle.putSerializable(XmppAction.ACTION_MESSAGE_LISTENER, chatmsg);
        Utils.intentAction(NextApplication.mContext,XmppAction.ACTION_MESSAGE_LISTENER, bundle);
        FinalUserDataBase.getInstance().saveChatMsg(chatmsg, chatmsg.getChatId(), groupName, url, false);
    }

    @Override
    public void removeMemberChatMsg(String groupName, List<UserBaseVo> data, int cid, String content) {
        StringBuilder sb = new StringBuilder();
        int index=1;
        for (UserBaseVo vo : data) {
            if(index>4){//Most need four pictures
                break;
            }
            sb.append(vo.getThumb()).append("___").append(vo.getGender()).append("#");
            index++;
        }
        sb.deleteCharAt(sb.lastIndexOf("#"));
        String url = sb.toString();
        ChatMsg chatmsg = new ChatMsg();
        chatmsg.setChatId("group-" + cid);
        chatmsg.setGroupName(groupName);
        chatmsg.setGroup(true);
        chatmsg.setType(14);
        chatmsg.setSend(1);
        chatmsg.setContent(content);
        chatmsg.setMsgTime(System.currentTimeMillis() / 1000);
        chatmsg.setMessageId(UUID.randomUUID().toString());
        chatmsg.parseUserBaseVo(NextApplication.myInfo.getUserBaseVo());
        Bundle bundle = new Bundle();
        bundle.putSerializable(XmppAction.ACTION_MESSAGE_LISTENER, chatmsg);
        Utils.intentAction(NextApplication.mContext,XmppAction.ACTION_MESSAGE_LISTENER, bundle);
        FinalUserDataBase.getInstance().saveChatMsg(chatmsg, chatmsg.getChatId(), groupName, url, false);
    }

    @Override
    public void removeMember(final int position, String aimid, int cid) {
        NetRequestImpl.getInstance().removeDiscussMember(aimid, cid, new RequestListener() {
            @Override
            public void start() {
                mView.removeMemberStart();
            }

            @Override
            public void success(JSONObject response) {
                mView.removeMemberSuccess(position,response.optString("msg"));
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                mView.removeMemberError(errorCode,errorMsg);
            }
        });
    }

    @Override
    public void switchNotify(boolean isChecked,int cid) {
        NetRequestImpl.getInstance().switchNotify(isChecked, cid, new RequestListener() {
            @Override
            public void start() {

            }

            @Override
            public void success(JSONObject response) {
                mView.switchNotifySuccess(response.optString("msg"));
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                mView.switchNotifyError(errorCode,errorMsg);
            }
        });
    }

    @Override
    public void quitGroup(int cid) {
        NetRequestImpl.getInstance().removeDiscussMember(NextApplication.myInfo.getLocalId(), cid, new RequestListener() {
            @Override
            public void start() {
                mView.quitGroupStart();
            }

            @Override
            public void success(JSONObject response) {
                mView.quitGroupSuccess(response.optString("msg"));
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                mView.quitGroupError(errorCode,errorMsg);
            }
        });
    }
}
