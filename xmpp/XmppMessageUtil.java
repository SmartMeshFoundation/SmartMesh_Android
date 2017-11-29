package com.lingtuan.firefly.xmpp;

import android.os.Bundle;
import android.text.TextUtils;


import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.chat.ChattingManager;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.service.LoadDataService;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.vo.ChatMsg;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.MsgType;
import org.jivesoftware.smack.packet.Message.Type;

import java.util.UUID;

public class XmppMessageUtil extends XmppUtils {

    private static XmppMessageUtil instance;

    private boolean isGroupDismiss = false;
    private boolean isGroupKick = false;
    private int mFriendLog = 0;

    public static XmppMessageUtil getInstance() {

        if (instance == null) {
            instance = new XmppMessageUtil();
        }

        return instance;
    }

    public void setGroupDismiss(boolean isGroupDismiss) {
        this.isGroupDismiss = isGroupDismiss;
    }

    public void setGroupKick(boolean isGroupKick) {
        this.isGroupKick = isGroupKick;
    }

    public void setFriendLog(int mFriendLog) {
        this.mFriendLog = mFriendLog;
    }

    /**
     * Send a text message
     */
    @Deprecated
    public ChatMsg sendText(String uid, String content, String uName, String avatarUrl, boolean isGroup, boolean isSend) {
        ChatMsg chatMsg = new ChatMsg();
        chatMsg.setType(0);
        chatMsg.setContent(content);
        return send(uid, uName, avatarUrl,  chatMsg, true, isGroup, isSend);
    }

    /**
     * Send a text message
     */
    public ChatMsg sendText(String uid, String content, boolean isAtGroupAll, String at, String uName, String avatarUrl, boolean isGroup, boolean isSend) {
        ChatMsg chatMsg = new ChatMsg();
        chatMsg.setType(0);
        chatMsg.setContent(content);
        chatMsg.setAtGroupIds(at);
        chatMsg.setIsAtGroupAll(isAtGroupAll);
        return send(uid, uName, avatarUrl,  chatMsg, true, isGroup, isSend);
    }

    /**
     * Send business card information
     */
    public ChatMsg sendCard(String uid, String uName, String avatarUrl, String cardName, String cardSign, String cardImage, String cardId, boolean isGroup, boolean isSend) {
        ChatMsg chatMsg = new ChatMsg();
        chatMsg.setType(6);
        chatMsg.setThirdId(cardId);
        chatMsg.setThirdImage(cardImage);
        chatMsg.setThirdName(cardName);
        chatMsg.setCardSign(cardSign);
        return send(uid, uName, avatarUrl, chatMsg, true, isGroup, isSend);
    }




    /**
     * heavy post this information
     * @ param isSend is sent
     */
    public boolean reSend(ChatMsg chatMsg, boolean isGroup, boolean isSend) {
        boolean result = false;
        chatMsg.parseUserBaseVo(NextApplication.myInfo.getUserBaseVo());
        String uid = chatMsg.getChatId();
        String jid = uid + "@" + XmppUtils.SERVER_NAME;
        Message msg;
        if(uid.equals("everyone"))
        {
            jid = uid + "@conference." + XmppUtils.SERVER_NAME;
            msg = new Message(jid, Type.groupchat);
        }
        else if (uid.startsWith("group-")) {
            jid = uid.replace("group-", "") + "@group." + XmppUtils.SERVER_NAME;
            msg = new Message(jid, Type.chat);
        }
        else{
            msg = new Message(jid, Type.chat);
        }
        if(!TextUtils.isEmpty(chatMsg.getMessageId()))
        {
            msg.setPacketID(chatMsg.getMessageId());
        }

        if (chatMsg.getType() == 103) {//Need to transform to share information
            chatMsg.setType(13);
        } else if (chatMsg.getType() == 104) {//Information need to transform is a red envelope
            chatMsg.setType(14);
        } else if (chatMsg.getType() == 105) {//Is the video need to transform
            chatMsg.setType(15);
        } else if (chatMsg.getType() == 106) {//Is live need to transform
            chatMsg.setType(16);
        } else if (chatMsg.getType() == 107) {//Is the gift bag need to transform
            chatMsg.setType(17);
        } else if (chatMsg.getType() == 1008) {//The props need to transform
            chatMsg.setType(18);
        } else if (chatMsg.getType() == 1009) {//Is the file need to transform
            chatMsg.setType(19);
        } else if (chatMsg.getType() == 1010) {//Were the fortunella venosa need to transform
            chatMsg.setType(20);
        }
        if (uid.startsWith("group-")) {//
            msg.setMsgtype(MsgType.groupchat);
            chatMsg.setChatId(uid);
            msg.setBody(chatMsg.toGroupChatJsonObject());
        }
        else {
            chatMsg.setSource(NextApplication.mContext.getString(R.string.app_name));
            msg.setBody(chatMsg.toChatJsonObject());
        }

        if (chatMsg.getType() == 13) {//Need to transform to share information
            chatMsg.setType(103);
        } else if (chatMsg.getType() == 14) {//Information need to transform is a red envelope
            chatMsg.setType(104);
        } else if (chatMsg.getType() == 15) {//Need to transform the micro video information
            chatMsg.setType(105);
        } else if (chatMsg.getType() == 16) {//The need to transform broadcast information
            chatMsg.setType(106);
        } else if (chatMsg.getType() == 17) {//Need to transform gift bag information
            chatMsg.setType(107);
        } else if (chatMsg.getType() == 18) {//The props need to transform information
            chatMsg.setType(1008);
        } else if (chatMsg.getType() == 19) {//Need to transform file information
            chatMsg.setType(1009);
        } else if (chatMsg.getType() == 20) {//Need to transform and fortunella venosa information
            chatMsg.setType(1010);
        }
        try {
            getConnection().sendPacket(msg);
            chatMsg.setSend(1);
            FinalUserDataBase.getInstance().updateChatMsg(chatMsg, chatMsg.getChatId());
            result = true;
        } catch (Exception e) {
            XmppUtils.loginXmppForNextApp(NextApplication.mContext);
        }
        return result;
    }


    /**
     * Sending pictures information
     */
    public ChatMsg sendImage(String uid, String uName, String avatarUrl, ChatMsg chatMsg, boolean isGroup, boolean isSend) {
        return send(uid, uName, avatarUrl, chatMsg, false, isGroup, isSend);
    }

    /**
     * Send a voice message
     */
    public ChatMsg sendAudio(String uid, String content, String uName, String avatarUrl, String second, boolean isGroup, boolean isSend) {
        ChatMsg chatMsg = new ChatMsg();
        chatMsg.setType(2);
        final String body = chatMsg.getContent();
        chatMsg.setContent(content);
        chatMsg.setSecond(second);
        chatMsg = send(uid, uName, avatarUrl, chatMsg, false, isGroup, isSend);
        chatMsg.setContent(body);//Using a local url
        return chatMsg;
    }




    /**
     * Send to share
     */
    public ChatMsg sendShare(String uid, String uName, String avatarUrl, String shareUrl, String shareTitle, String shareContent, String shareThumb,boolean isGroup, boolean isSend) {
        ChatMsg chatMsg = new ChatMsg();
        chatMsg.setType(103);
        chatMsg.setContent(shareContent);
        chatMsg.setShareUrl(shareUrl);
        chatMsg.setShareTitle(shareTitle);
        chatMsg.setShareThumb(shareThumb);
        return send(uid, uName, avatarUrl, chatMsg, true, isGroup, isSend);
    }


    /**
     * Send a file
     */
    public ChatMsg sendFile(String uid, String uName, String avatarUrl, ChatMsg chatMsg, boolean isGroup, boolean isSend) {
        return send(uid, uName, avatarUrl, chatMsg, false, isGroup, isSend);
    }

    /**
     * Send a small video
     */
    public void sendEnterLeaveEveryOne(long time,boolean isLeave) {
        String jid =  "everyone@conference." + XmppUtils.SERVER_NAME;
        Message msg = new Message(jid, Type.normal);
        if(isLeave)
        {
            msg = new Message(jid, Type.unavailable);
        }
        else{
            msg.setMsgTime(time);
        }
        msg.setMsgtype(MsgType.everyone);
        try {
            getConnection().sendPacket(msg);
        } catch (Exception e) {
            XmppUtils.loginXmppForNextApp(NextApplication.mContext);
        }
    }

    /**
     * send
     * @ param uid uid
     * @ param uName each other's nickname
     * @ param avatarUrlThe head of each other address
     * @ param chatMsg chat messages
     * @ param save save chat messages that don't need to save because saved when sending
     * @ param isGroup is a discussion group
     * @ param isSend
     */
    private ChatMsg send(String uid, String uName, String avatarUrl,
                         ChatMsg chatMsg, boolean save, boolean isGroup, boolean isSend) {
        chatMsg.parseUserBaseVo(NextApplication.myInfo.getUserBaseVo());
        String jid = uid + "@" + XmppUtils.SERVER_NAME;
        Message msg;
        if(uid.equals("everyone"))//
        {
            jid = uid + "@conference." + XmppUtils.SERVER_NAME;
            msg = new Message(jid, Type.groupchat);
        }
        else if (isGroup) {
            jid = uid.replace("group-", "") + "@group." + XmppUtils.SERVER_NAME;
            msg = new Message(jid, Type.chat);
        }
        else{
            msg = new Message(jid, Type.chat);
        }

        if (chatMsg.getType() == 103) {//Need to transform to share information
            chatMsg.setType(13);
        }  else if (chatMsg.getType() == 105) {//Is the need to convert video information
            chatMsg.setType(15);
        } else if (chatMsg.getType() == 1009) {//Need to convert file information
            chatMsg.setType(19);
        }
        if (isGroup) {//
            msg.setMsgtype(MsgType.groupchat);
            chatMsg.setChatId(uid);
            chatMsg.setSource(NextApplication.mContext.getString(R.string.app_name));
            chatMsg.setUserfrom(NextApplication.mContext.getString(R.string.app_name));
            chatMsg.setUsersource(XmppUtils.SERVER_NAME);
            msg.setBody(chatMsg.toGroupChatJsonObject());
        }
        else {
            chatMsg.setSource(NextApplication.mContext.getString(R.string.app_name));
            msg.setBody(chatMsg.toChatJsonObject());
        }

        if (chatMsg.getType() == 13) {//Need to transform to share information
            chatMsg.setType(103);
        } else if (chatMsg.getType() == 15) {//Is the need to convert video information
            chatMsg.setType(105);
        }else if (chatMsg.getType() == 19) {//Need to convert file information
            chatMsg.setType(1009);
        }

        if(TextUtils.isEmpty(chatMsg.getMessageId()))        //Only the messageid is empty resetting forwarded message Not here set
        {
            chatMsg.setMessageId(msg.getPacketID());
        }
        else{
            msg.setPacketID(chatMsg.getMessageId());
        }

        if (chatMsg.getType() == 1 || chatMsg.getType() == 2)//Due to the messageID changes need to be modified
        {
            Bundle bundle = new Bundle();
            bundle.putSerializable(XmppAction.ACTION_MESSAGE_UPDATE_LISTENER, chatMsg);
            Utils.intentAction(NextApplication.mContext, XmppAction.ACTION_MESSAGE_UPDATE_LISTENER, bundle);
        }
        chatMsg.setDismissGroup(isGroupDismiss);
        chatMsg.setKickGroup(isGroupKick);
        chatMsg.setFriendLog(mFriendLog);
        if (chatMsg.getMsgTime() == 0) {
            chatMsg.setMsgTime(System.currentTimeMillis() / 1000);
        }
        try {
            if (isSend) {
                getConnection().sendPacket(msg);
                chatMsg.setSend(1);
            }
        } catch (Exception e) {
            XmppUtils.loginXmppForNextApp(NextApplication.mContext);
        }

        try {
            boolean mask = MySharedPrefs.readBooleanNormal(NextApplication.mContext, MySharedPrefs.FILE_USER, MySharedPrefs.IS_MASK_MSG + NextApplication.myInfo.getLocalId() + "_" + uid);
            chatMsg.setGroupMask(mask);
        } catch (Exception e) {
            e.printStackTrace();
        }
        chatMsg.setShowTime(FinalUserDataBase.getInstance().isShowTime(uid, chatMsg.getMsgTime()));
        if (save) {
            FinalUserDataBase.getInstance().saveChatMsg(chatMsg, uid, uName, avatarUrl);
        }
        chatMsg.parseUserBaseVo(NextApplication.myInfo.getUserBaseVo());
        try {
            Utils.intentAction(NextApplication.mContext, XmppAction.ACTION_MESSAGE_EVENT_LISTENER, null);
        } catch (Exception e) {
        }
        return chatMsg;
    }

    /**
     * resend
     * @ param type message type
     */
    public boolean reSend(int type, ChatMsg chatMsg) {

        switch (type) {//This type is the inside of the adapter type Chat is different from the type
            case 1://text
            case 15://share
                return reSend(chatMsg, chatMsg.isGroup(), true);
            case 19://small video
                if (chatMsg.getContent().startsWith("http")) {
                    return reSend(chatMsg, chatMsg.isGroup(), true);
                }else {
                    ChattingManager.getInstance(NextApplication.mContext).reSendVideo(chatMsg);
                }
            case 29://file
                if (chatMsg.getContent().startsWith("http")) {
                    return reSend(chatMsg, chatMsg.isGroup(), true);
                }else {
                    ChattingManager.getInstance(NextApplication.mContext).reSendFile(chatMsg);
                }
            case 3://picture
            case 11://voice
                if (chatMsg.getContent().startsWith("http")) {
                    return reSend(chatMsg, chatMsg.isGroup(), true);
                } else {
                    ChattingManager.getInstance(NextApplication.mContext).reSendAudioOrPic(chatMsg);
                }
        }
        return false;
    }

    /**
     * forward
     * @ param send when the discussion group is not send T or dissolve
     */
    public boolean forwarding(ChatMsg chatMsg, String uid, String uName, String avatarUrl, boolean isGroup, boolean send) {

        if (isGroup) {
            chatMsg.setMsgType(MsgType.groupchat);
        }  else {
            chatMsg.setMsgType(MsgType.normalchat);
        }
        chatMsg.setMessageId(UUID.randomUUID().toString());
        chatMsg.setMsgTime(System.currentTimeMillis() / 1000);   //Forwarding messages to reset id and time
        switch (chatMsg.getType()) {
            case 0://text
            case 6://card
            case 103://share
                chatMsg.setSend(0);
                send(chatMsg.getChatId(), uName, avatarUrl,chatMsg, true, isGroup, send);
                break;
            case 1009:  //file
                //Upload 0 on the cross, 1 failed, 2 uploaded successfully, 3 cancel upload 、 4 download not, 5, 6 in the download failed, 7 download success, 8 cancel the download
                if(chatMsg.getInviteType() ==1 || chatMsg.getInviteType() ==3){// 1 the upload failure after 3 cancel upload forward into a cross
                    chatMsg.setInviteType(1);
                }else if(chatMsg.getInviteType() == 5 || chatMsg.getInviteType() == 6 ||  chatMsg.getInviteType() == 8)// 5 in the download 6 failed download into not download after 8 forward to cancel the download
                {
                    chatMsg.setInviteType(4);
                }
                if (chatMsg.getSend() == 1) {
                    chatMsg.setSend(0);
                    send(uid, uName, avatarUrl, chatMsg, true, chatMsg.getChatId().startsWith("group-"), send);
                } else {
                    chatMsg.setSend(2);
                    if (chatMsg.getContent().startsWith("http")) {
                        send(uid, uName, avatarUrl,chatMsg, true, chatMsg.getChatId().startsWith("group-"), send);
                    } else {
                        chatMsg.parseUserBaseVo(NextApplication.myInfo.getUserBaseVo());
                        FinalUserDataBase.getInstance().saveChatMsg(chatMsg, uid, uName, avatarUrl);
                        Bundle bundle = new Bundle();
                        bundle.putString("uid", uid);
                        bundle.putString("username", uName);
                        bundle.putString("avatarurl", chatMsg.getUserImage());
                        bundle.putSerializable("chatmsg", chatMsg);
                    }
                }

                break;

            case 105://small video
                if (chatMsg.getSend() == 1) {
                    chatMsg.setSend(0);
                    send(uid, uName, avatarUrl,chatMsg, true, chatMsg.getChatId().startsWith("group-"), send);
                } else {
                    chatMsg.setSend(2);
                    if (chatMsg.getContent().startsWith("http")) {
                        send(uid, uName, avatarUrl,  chatMsg, true, chatMsg.getChatId().startsWith("group-"), send);
                    } else {
                        chatMsg.parseUserBaseVo(NextApplication.myInfo.getUserBaseVo());
                        FinalUserDataBase.getInstance().saveChatMsg(chatMsg, uid, uName, avatarUrl);
                        Bundle bundle = new Bundle();
                        bundle.putString("uid", uid);
                        bundle.putString("username", uName);
                        bundle.putString("avatarurl", chatMsg.getUserImage());
                        bundle.putSerializable("chatmsg", chatMsg);
                    }
                }
                break;
            case 1://picture
                if (chatMsg.getSend() == 1) {
                    chatMsg.setSend(0);
                    send(uid, uName, avatarUrl, chatMsg, true, chatMsg.getChatId().startsWith("group-"), send);
                } else {
                    chatMsg.setSend(2);
                    if (chatMsg.getContent().startsWith("http")) {
                        send(uid, uName, avatarUrl, chatMsg, true, chatMsg.getChatId().startsWith("group-"), send);
                    } else {
                        chatMsg.parseUserBaseVo(NextApplication.myInfo.getUserBaseVo());
                        FinalUserDataBase.getInstance().saveChatMsg(chatMsg, uid, uName, avatarUrl);
                        Bundle bundle = new Bundle();
                        bundle.putInt("type", 2);
                        bundle.putString("uid", uid);
                        bundle.putString("username", uName);
                        bundle.putString("avatarurl", chatMsg.getUserImage());
                        bundle.putSerializable("chatmsg", chatMsg);
                        Utils.intentService(
                                NextApplication.mContext,
                                LoadDataService.class,
                                LoadDataService.ACTION_FILE_UPLOAD_CHAT,
                                LoadDataService.ACTION_FILE_UPLOAD_CHAT,
                                bundle);
                    }
                }
                break;
        }
        return false;
    }

}
