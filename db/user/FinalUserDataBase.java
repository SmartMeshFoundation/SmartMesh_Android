package com.lingtuan.firefly.db.user;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.contact.vo.FriendRecommentVo;
import com.lingtuan.firefly.contact.vo.GroupMemberAvatarVo;
import com.lingtuan.firefly.contact.vo.PhoneContactGroupVo;
import com.lingtuan.firefly.contact.vo.PhoneContactVo;
import com.lingtuan.firefly.db.TableField;
import com.lingtuan.firefly.offline.vo.WifiPeopleVO;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.vo.ChatMsg;
import com.lingtuan.firefly.vo.UserBaseVo;
import com.lingtuan.firefly.vo.UserInfoVo;
import com.lingtuan.firefly.wallet.vo.TokenVo;
import com.lingtuan.firefly.wallet.vo.TransVo;

import org.jivesoftware.smack.packet.Message.MsgType;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FinalUserDataBase {

    private static FinalUserDataBase instance;
    private FinalUserDbHelper helper;
    private SQLiteDatabase db;

    private static Map<String, Integer> map;

    private FinalUserDataBase() {
        if (NextApplication.myInfo == null && NextApplication.mContext != null) {
            NextApplication.myInfo = new UserInfoVo().readMyUserInfo(NextApplication.mContext);
        }

        if (NextApplication.myInfo != null) {
            helper = new FinalUserDbHelper(NextApplication.mContext, NextApplication.myInfo.getLocalId());
            db = helper.getWritableDatabase();
        }
    }

    public static synchronized FinalUserDataBase getInstance() {
        if (instance == null) {
            instance = new FinalUserDataBase();
            map = new HashMap<>();
        }
        return instance;
    }




    /**
     * Modify the recent chat messages
     *
     * @param chatId
     * @param isTop
     * @param topTime
     */
    public void updateChatEventTop(String chatId, boolean isTop, long topTime) {
        ContentValues values = new ContentValues();
        values.put(TableField.FIELD_RESERVED_DATA15, isTop ? 1 : 0);
        values.put(TableField.FIELD_RESERVED_DATA16, topTime);
        db.update(TableField.TABLE_CHAT_EVENT, values,
                TableField.FIELD_CHAT_ID + "=? and "
                        + TableField.FIELD_CHAT_HIDDEN + "=0"
                , new String[]{chatId});
    }

    /**
     * Message to heavy (ah, the server is not to force The client is trouble Really don't want to write the code)
     *
     * @param msgId
     * @param msgType
     * @param type
     * @return
     */
    public boolean checkMsgExist(String msgId, MsgType msgType, int type,String number,String noticetype) {
        if (TextUtils.isEmpty(msgId) || msgType == null) {
            return false;
        }
        String table = "";
        String field = "";
        if (MsgType.normalchat.equals(msgType) || MsgType.groupchat.equals(msgType) || MsgType.super_groupchat.equals(msgType)) {//General chat
            table = TableField.TABLE_CHAT;
            field = TableField.FIELD_RESERVED_DATA1;
        } else if (MsgType.system.equals(msgType)) {//System information (including the group members to add or remove exit information)
            if (type > 11 && type < 19) {//Group chat message
                field = TableField.FIELD_RESERVED_DATA1;
                table = TableField.TABLE_CHAT;
            } else if (type == 19) {//Dynamic new review notification
                field = TableField._MSGID;
                table = TableField.TABLE_DYNAMIC_NOTIF;
            } else if (type == 108 || type == 109 || type == 1000) {//Group information
                field = TableField.FIELD_RESERVED_DATA1;
                table = TableField.TABLE_CHAT;
            } else {//Offer information such as the information such as social circle
                table = TableField.TABLE_CHAT_EVENT;
                field = TableField.FIELD_RESERVED_DATA1;
            }
        } else {//Message receipt Don't go to tube
            return false;
        }
        String sql = "select " + TableField._ID + " from " + table + " where " + field + "=?";
        Cursor cursor = db.rawQuery(sql, new String[]{msgId});
        boolean result = false;
        result = cursor.moveToNext();
        cursor.close();
        if (!result && MsgType.system.equals(msgType) && type == 300)//Trans message
        {
            String fieldNumber = TableField.FIELD_RESERVED_DATA4;
            String fieldNoticetype = TableField.FIELD_RESERVED_DATA9;
            table = TableField.TABLE_TRANS;
            sql = "select " + TableField._ID + " from " + table + " where " + fieldNumber + "=? and " + fieldNoticetype + "=?";
            cursor = db.rawQuery(sql, new String[]{number,noticetype});
            result = cursor.moveToNext();
            cursor.close();
            return result;
        }
        else{
          return result;
        }
    }

    /**
     * Save chat messages
     *
     * @param vo
     * @param chatId
     * @param uname     For my information page Ensure that nickname unchanged
     * @param avatarUrl For group chat head remains the same
     */
    public void saveChatMsg(ChatMsg vo, String chatId, String uname, String avatarUrl) {
        vo.setChatId(chatId);
        ContentValues values = new ContentValues();
        values.put(TableField.FIELD_FRIEND_UNAME, vo.getUsername());
        values.put(TableField.FIELD_FRIEND_UID, vo.getUserId());
        values.put(TableField.FIELD_FRIEND_PIC, vo.getUserImage());
        values.put(TableField.FIELD_FRIEND_GENDER, vo.getGender());
        values.put(TableField.FIELD_CHAT_TYPE, vo.getType());
        values.put(TableField.FIELD_CHAT_BODY, vo.getContent());
        values.put(TableField.FIELD_CHAT_MSGTIME, vo.getMsgTime());
        values.put(TableField.FIELD_CHAT_ID, chatId);
        values.put(TableField.FIELD_CHAT_UNREAD, vo.getUnread());
        values.put(TableField.FIELD_CHAT_ISSEND, vo.getSend());

        values.put(TableField.FIELD_CHAT_GROUP_IMAGE, vo.getGroupImage());
        values.put(TableField.FIELD_CHAT_OBJECT, vo.getMsgTypeInt());

        values.put(TableField.FIELD_CHAT_COVER, vo.getCover());
        values.put(TableField.FIELD_CHAT_SECOND, vo.getSecond());
        values.put(TableField.FIELD_CHAT_LON, vo.getLon());
        values.put(TableField.FIELD_CHAT_LAT, vo.getLat());

        values.put(TableField.FIELD_CHAT_THIRDNAME, vo.getThirdName());
        values.put(TableField.FIELD_CHAT_THIRDIMAGE, vo.getThirdImage());
        values.put(TableField.FIELD_CHAT_THIRDID, vo.getThirdId());
        values.put(TableField.FIELD_CHAT_SHOPADDRESS, vo.getShopAddress());
        values.put(TableField.FIELD_CHAT_CARDSIGN, vo.getCardSign());

        values.put(TableField.FIELD_CHAT_LOCALURL, vo.getLocalUrl());
        values.put(TableField.FIELD_RESERVED_DATA1, vo.getMessageId());
        values.put(TableField.FIELD_RESERVED_DATA2, vo.getThirdGender());
        values.put(TableField.FIELD_RESERVED_DATA3, vo.getDatingSOSId());
        values.put(TableField.FIELD_RESERVED_DATA4, vo.getFriendLog());
        values.put(TableField.FIELD_RESERVED_DATA5, vo.getInviteType());
        values.put(TableField.FIELD_RESERVED_DATA6, vo.getNumber());
        values.put(TableField.FIELD_RESERVED_DATA7, vo.isOffLineMsg() ? 1 : 0);

        values.put(TableField.FIELD_RESERVED_DATA8, vo.getShareUrl());
        values.put(TableField.FIELD_RESERVED_DATA9, vo.getShareTitle());
        values.put(TableField.FIELD_RESERVED_DATA10, vo.getShareThumb());
        values.put(TableField.FIELD_RESERVED_DATA11, vo.getRedpacketId());

        values.put(TableField.FIELD_RESERVED_DATA12, vo.getCreateName());
        values.put(TableField.FIELD_RESERVED_DATA13, vo.getSource());
        values.put(TableField.FIELD_RESERVED_DATA14, vo.getRemoteSource());
        values.put(TableField.FIELD_RESERVED_DATA15, vo.getCreateTime());
        values.put(TableField.FIELD_RESERVED_DATA16, vo.getUsersource());
        values.put(TableField.FIELD_RESERVED_DATA17, vo.getUserfrom());
        boolean showTime = isShowTime(chatId, vo.getMsgTime());
        values.put(TableField.FIELD_CHAT_SHOWTIME, showTime ? 1 : 0);
        vo.setShowTime(showTime);

        db.insert(TableField.TABLE_CHAT, TableField._ID, values);
        vo.setUsername(uname);
        vo.setUserImage(avatarUrl);
        saveChatEvent(vo);
    }

    /**
     * Save chat messages
     *
     * @param vo
     * @param chatId
     * @param uname     For my information page Ensure that nickname unchanged
     * @param avatarUrl For group chat head remains the same
     */
    public void saveChatMsgNew(ChatMsg vo, String chatId, String uname, String avatarUrl, boolean saveChatEvent) {
        vo.setChatId(chatId);
        ContentValues values = new ContentValues();
        values.put(TableField.FIELD_FRIEND_UNAME, vo.getUsername());
        values.put(TableField.FIELD_FRIEND_UID, vo.getUserId());
        values.put(TableField.FIELD_FRIEND_PIC, vo.getUserImage());
        values.put(TableField.FIELD_FRIEND_GENDER, vo.getGender());
        values.put(TableField.FIELD_CHAT_TYPE, vo.getType());
        values.put(TableField.FIELD_CHAT_BODY, vo.getContent());
        values.put(TableField.FIELD_CHAT_MSGTIME, vo.getMsgTime());
        values.put(TableField.FIELD_CHAT_ID, chatId);
        values.put(TableField.FIELD_CHAT_UNREAD, vo.getUnread());
        values.put(TableField.FIELD_CHAT_ISSEND, vo.getSend());

        values.put(TableField.FIELD_CHAT_GROUP_IMAGE, vo.getGroupImage());
        values.put(TableField.FIELD_CHAT_OBJECT, vo.getMsgTypeInt());

        values.put(TableField.FIELD_CHAT_COVER, vo.getCover());
        values.put(TableField.FIELD_CHAT_SECOND, vo.getSecond());
        values.put(TableField.FIELD_CHAT_LON, vo.getLon());
        values.put(TableField.FIELD_CHAT_LAT, vo.getLat());

        values.put(TableField.FIELD_CHAT_THIRDNAME, vo.getThirdName());
        values.put(TableField.FIELD_CHAT_THIRDIMAGE, vo.getThirdImage());
        values.put(TableField.FIELD_CHAT_THIRDID, vo.getThirdId());
        values.put(TableField.FIELD_CHAT_SHOPADDRESS, vo.getShopAddress());
        values.put(TableField.FIELD_CHAT_CARDSIGN, vo.getCardSign());

        values.put(TableField.FIELD_CHAT_LOCALURL, vo.getLocalUrl());
        values.put(TableField.FIELD_RESERVED_DATA1, vo.getMessageId());
        values.put(TableField.FIELD_RESERVED_DATA2, vo.getThirdGender());
        values.put(TableField.FIELD_RESERVED_DATA3, vo.getDatingSOSId());
        values.put(TableField.FIELD_RESERVED_DATA4, vo.getFriendLog());
        values.put(TableField.FIELD_RESERVED_DATA5, vo.getInviteType());
        values.put(TableField.FIELD_RESERVED_DATA6, vo.getNumber());
        values.put(TableField.FIELD_RESERVED_DATA7, vo.isOffLineMsg() ? 1 : 0);

        values.put(TableField.FIELD_RESERVED_DATA8, vo.getShareUrl());
        values.put(TableField.FIELD_RESERVED_DATA9, vo.getShareTitle());
        values.put(TableField.FIELD_RESERVED_DATA10, vo.getShareThumb());
        values.put(TableField.FIELD_RESERVED_DATA11, vo.getRedpacketId());
        values.put(TableField.FIELD_RESERVED_DATA12, vo.getCreateName());
        values.put(TableField.FIELD_RESERVED_DATA13, vo.getSource());
        values.put(TableField.FIELD_RESERVED_DATA14, vo.getRemoteSource());
        values.put(TableField.FIELD_RESERVED_DATA15, vo.getCreateTime());
        values.put(TableField.FIELD_RESERVED_DATA16, vo.getUsersource());
        values.put(TableField.FIELD_RESERVED_DATA17, vo.getUserfrom());
        boolean showTime = isShowTime(chatId, vo.getMsgTime());
        values.put(TableField.FIELD_CHAT_SHOWTIME, showTime ? 1 : 0);
        vo.setShowTime(showTime);
        db.insert(TableField.TABLE_CHAT, TableField._ID, values);

        if (saveChatEvent) {
            vo.setUsername(uname);
            vo.setUserImage(avatarUrl);
            saveChatEvent(vo);
        }
    }


    /**
     * Save chat messages
     *
     * @param vo
     * @param chatId    The chat object uid
     * @param uname     The message list shows that nickname
     * @param avatarUrl The head of the message list shows
     * @param showTime  Whether to show time
     */
    public void saveChatMsg(ChatMsg vo, String chatId, String uname, String avatarUrl, boolean showTime) {
        vo.setChatId(chatId);
        ContentValues values = new ContentValues();
        values.put(TableField.FIELD_FRIEND_UNAME, vo.getUsername());
        values.put(TableField.FIELD_FRIEND_UID, vo.getUserId());
        values.put(TableField.FIELD_FRIEND_PIC, vo.getUserImage());
        values.put(TableField.FIELD_FRIEND_GENDER, vo.getGender());
        values.put(TableField.FIELD_CHAT_TYPE, vo.getType());
        values.put(TableField.FIELD_CHAT_BODY, vo.getContent());
        values.put(TableField.FIELD_CHAT_MSGTIME, vo.getMsgTime());
        values.put(TableField.FIELD_CHAT_ID, chatId);
        values.put(TableField.FIELD_CHAT_UNREAD, vo.getUnread());
        values.put(TableField.FIELD_CHAT_ISSEND, vo.getSend());

        values.put(TableField.FIELD_CHAT_GROUP_IMAGE, vo.getGroupImage());
        values.put(TableField.FIELD_CHAT_OBJECT, vo.getMsgTypeInt());

        values.put(TableField.FIELD_CHAT_COVER, vo.getCover());
        values.put(TableField.FIELD_CHAT_SECOND, vo.getSecond());
        values.put(TableField.FIELD_CHAT_LON, vo.getLon());
        values.put(TableField.FIELD_CHAT_LAT, vo.getLat());

        values.put(TableField.FIELD_CHAT_THIRDNAME, vo.getThirdName());
        values.put(TableField.FIELD_CHAT_THIRDIMAGE, vo.getThirdImage());
        values.put(TableField.FIELD_CHAT_THIRDID, vo.getThirdId());
        values.put(TableField.FIELD_CHAT_SHOPADDRESS, vo.getShopAddress());
        values.put(TableField.FIELD_CHAT_CARDSIGN, vo.getCardSign());

        values.put(TableField.FIELD_CHAT_LOCALURL, vo.getLocalUrl());
        values.put(TableField.FIELD_CHAT_SHOWTIME, showTime ? 1 : 0);
        values.put(TableField.FIELD_RESERVED_DATA1, vo.getMessageId());
        values.put(TableField.FIELD_RESERVED_DATA2, vo.getThirdGender());
        values.put(TableField.FIELD_RESERVED_DATA3, vo.getDatingSOSId());
        values.put(TableField.FIELD_RESERVED_DATA4, vo.getFriendLog());
        values.put(TableField.FIELD_RESERVED_DATA5, vo.getInviteType());
        values.put(TableField.FIELD_RESERVED_DATA6, vo.getNumber());
        values.put(TableField.FIELD_RESERVED_DATA7, vo.isOffLineMsg() ? 1 : 0);

        values.put(TableField.FIELD_RESERVED_DATA8, vo.getShareUrl());
        values.put(TableField.FIELD_RESERVED_DATA9, vo.getShareTitle());
        values.put(TableField.FIELD_RESERVED_DATA10, vo.getShareThumb());
        values.put(TableField.FIELD_RESERVED_DATA11, vo.getRedpacketId());
        values.put(TableField.FIELD_RESERVED_DATA12, vo.getCreateName());
        values.put(TableField.FIELD_RESERVED_DATA13, vo.getSource());
        values.put(TableField.FIELD_RESERVED_DATA14, vo.getRemoteSource());
        values.put(TableField.FIELD_RESERVED_DATA15, vo.getCreateTime());
        values.put(TableField.FIELD_RESERVED_DATA16, vo.getUsersource());
        values.put(TableField.FIELD_RESERVED_DATA17, vo.getUserfrom());
        vo.setShowTime(showTime);

        db.insert(TableField.TABLE_CHAT, TableField._ID, values);


        vo.setUsername(uname);
        vo.setUserImage(avatarUrl);
        saveChatEvent(vo);
    }

    /**
     * Whether to show chat time
     *
     * @return
     */
    public boolean isShowTime(String chatId, long currentTime) {
        if (TextUtils.isEmpty(chatId)) {
            return true;
        }
        String sql = "select max(" + TableField.FIELD_CHAT_MSGTIME + ") from " + TableField.TABLE_CHAT +
                " where " + TableField.FIELD_CHAT_ID + "=? and " + TableField.FIELD_CHAT_SHOWTIME + "=1";
        boolean result = false;
        Cursor cursor = db.rawQuery(sql, new String[]{chatId});
        if (cursor.moveToNext()) {
            long time = cursor.getLong(0);
            if (currentTime - time > 60 * 3) {
                result = true;
            }
        } else {
            result = true;
        }
        cursor.close();
        return result;
    }

    /**
     * If there is no net chat show time
     *
     * @return
     */
    public boolean isOffLineShowTime(String chatId, long currentTime) {
        if (TextUtils.isEmpty(chatId)) {
            return true;
        }
        String sql = "select max(" + TableField.FIELD_CHAT_MSGTIME + ") from "
                + TableField.TABLE_CHAT +
                " where " + TableField.FIELD_CHAT_ID + "=? and "
                + TableField.FIELD_CHAT_SHOWTIME + "=1 and "
                + TableField.FIELD_RESERVED_DATA7 + "=1";
        boolean result = false;
        Cursor cursor = db.rawQuery(sql, new String[]{chatId});
        if (cursor.moveToNext()) {
            long time = cursor.getLong(0);
            if (currentTime - time > 60 * 3) {
                result = true;
            }
        } else {
            result = true;
        }
        cursor.close();
        return result;
    }

    /**
     * Modify the chat network document delivery status
     */
    public void updateOfflineChatMsgState(String messageId, int state, int send) {
        ContentValues values = new ContentValues();
        if (state == 6)//Accept failure
        {
            String sql = "select " + TableField.FIELD_RESERVED_DATA5 + " from " + TableField.TABLE_CHAT
                    + " where " + TableField.FIELD_RESERVED_DATA1 + "=?";
            Cursor cursor = db.rawQuery(sql, new String[]{messageId});
            int oldstate = 0;
            if (cursor.moveToNext()) {
                oldstate = cursor.getInt(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA5));
            }
            cursor.close();
            if (oldstate != 8)//Not cancel the receiving
            {
                values.put(TableField.FIELD_RESERVED_DATA5, state);
            }

        } else {
            values.put(TableField.FIELD_RESERVED_DATA5, state);
        }


        values.put(TableField.FIELD_CHAT_ISSEND, send);
        db.update(
                TableField.TABLE_CHAT,
                values,
                TableField.FIELD_RESERVED_DATA1 + "=? ",
                new String[]{messageId});
    }

    /**
     * Modify the chat information in the file transfer
     *
     * @param messageId
     * @param state
     */
    public void updateChatMsgState(int send, String messageId, int state, String loaclUrl) {
        ContentValues values = new ContentValues();
        values.put(TableField.FIELD_RESERVED_DATA5, state);
        if (send != -1) {
            values.put(TableField.FIELD_CHAT_ISSEND, send);
        }
        if (!TextUtils.isEmpty(loaclUrl)) {
            values.put(TableField.FIELD_CHAT_LOCALURL, loaclUrl);
        }
        db.update(
                TableField.TABLE_CHAT,
                values,
                TableField.FIELD_RESERVED_DATA1 + "=? ",
                new String[]{messageId});
    }

    /**
     * Modify the chat message file collection status
     *
     * @param messageId
     * @param state     0: no collection 1: already collected
     */
    public void updateChatMsgCollectState(String messageId, int state) {
        ContentValues values = new ContentValues();
        values.put(TableField.FIELD_RESERVED_DATA3, state);
        db.update(
                TableField.TABLE_CHAT,
                values,
                TableField.FIELD_RESERVED_DATA1 + "=? ",
                new String[]{messageId});
    }

    /**
     * Modify the chat messages
     *
     * @param vo
     * @param chatId
     */
    public void updateChatMsg(ChatMsg vo, String chatId) {
        ContentValues values = new ContentValues();
        values.put(TableField.FIELD_FRIEND_UNAME, vo.getUsername());
        values.put(TableField.FIELD_FRIEND_UID, vo.getUserId());
        values.put(TableField.FIELD_FRIEND_PIC, vo.getUserImage());
        values.put(TableField.FIELD_FRIEND_GENDER, vo.getGender());
        values.put(TableField.FIELD_CHAT_TYPE, vo.getType());
        values.put(TableField.FIELD_CHAT_BODY, vo.getContent());
        values.put(TableField.FIELD_CHAT_MSGTIME, vo.getMsgTime());
        values.put(TableField.FIELD_CHAT_ISSEND, vo.getSend());
        values.put(TableField.FIELD_CHAT_LOCALURL, vo.getLocalUrl());
        values.put(TableField.FIELD_RESERVED_DATA2, vo.getThirdGender());
        values.put(TableField.FIELD_CHAT_THIRDID, vo.getThirdId());
        values.put(TableField.FIELD_RESERVED_DATA5, vo.getInviteType());
        if(vo.getCreateTime()>0)
        {
            values.put(TableField.FIELD_RESERVED_DATA15, vo.getCreateTime());
        }
        if (vo.getType() == 7)
            values.put(TableField.FIELD_CHAT_COVER, vo.getCover());
        db.update(
                TableField.TABLE_CHAT,
                values,
                TableField.FIELD_CHAT_ID + "=? and " +
                        TableField.FIELD_CHAT_TYPE + "=? and " +
                        TableField.FIELD_RESERVED_DATA1 + "=? ",
                new String[]{chatId, String.valueOf(vo.getType()), String.valueOf(vo.getMessageId())});
    }

    /**
     * Modify the chat has read the information
     *
     * @param chatId
     */
    public void updateUnreadEventChat(String chatId) {
        String sql = "update " + TableField.TABLE_CHAT_EVENT
                + " set " + TableField.FIELD_CHAT_UNREAD + "=0"
                + " where " + TableField.FIELD_CHAT_ID + "=? ";
        db.execSQL(sql, new String[]{chatId});
    }


    /**
     * Modify the group chat Someone @ I have read
     *
     * @param chatId
     */
    public void updateAtGroupMe(String chatId) {
        String sql = "update " + TableField.TABLE_CHAT_EVENT
                + " set " + TableField.FIELD_RESERVED_DATA8 + "=0"
                + " where " + TableField.FIELD_CHAT_ID + "=?  and "
                + TableField.FIELD_RESERVED_DATA8 + "!=0";
        db.execSQL(sql, new String[]{chatId});
    }

    /**
     * Access to chat
     *
     * @param chatId Chat with who
     * @param limit  From which to take
     * @param count  Article take how much
     * @return
     */
    public List<ChatMsg> getChatMsgListByChatId(String chatId, int limit, int count) {

        String sql = "select * from " + TableField.TABLE_CHAT
                + " where " + TableField.FIELD_CHAT_ID + "=? "/*and " + TableField.FIELD_RESERVED_DATA7 + "!=1*/+" order by " + TableField._ID + " desc "
                + " limit " + limit + "," + count;
        String sql1 = "select * from (" + sql + ") order by " + TableField._ID;
        Cursor cursor = db.rawQuery(sql1, new String[]{chatId});
        List<ChatMsg> list = new ArrayList<ChatMsg>();
        ChatMsg msg;

        while (cursor.moveToNext()) {

            msg = new ChatMsg();
            msg.setId(cursor.getInt(cursor.getColumnIndex(TableField._ID)));
            msg.setContent(cursor.getString(cursor.getColumnIndex(TableField.FIELD_CHAT_BODY)));
            msg.setGender(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_FRIEND_GENDER)));
            msg.setMsgTime(cursor.getLong(cursor.getColumnIndex(TableField.FIELD_CHAT_MSGTIME)));
            msg.setType(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_CHAT_TYPE)));
            msg.setUserId(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_UID)));
            msg.setUserImage(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_PIC)));
            msg.setRealname(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_UNAME)));
            msg.setUnread(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_CHAT_UNREAD)));
            msg.setUsername(msg.getRealname());

            msg.setChatId(cursor.getString(cursor.getColumnIndex(TableField.FIELD_CHAT_ID)));
            msg.setSend(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_CHAT_ISSEND)));

            msg.setGroupImage(cursor.getString(cursor.getColumnIndex(TableField.FIELD_CHAT_GROUP_IMAGE)));
            msg.setMsgTypeInt(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_CHAT_OBJECT)));

            msg.setCover(cursor.getString(cursor.getColumnIndex(TableField.FIELD_CHAT_COVER)));
            msg.setSecond(cursor.getString(cursor.getColumnIndex(TableField.FIELD_CHAT_SECOND)));
            msg.setLon(cursor.getString(cursor.getColumnIndex(TableField.FIELD_CHAT_LON)));
            msg.setLat(cursor.getString(cursor.getColumnIndex(TableField.FIELD_CHAT_LAT)));
            msg.setThirdName(cursor.getString(cursor.getColumnIndex(TableField.FIELD_CHAT_THIRDNAME)));
            msg.setThirdImage(cursor.getString(cursor.getColumnIndex(TableField.FIELD_CHAT_THIRDIMAGE)));
            msg.setThirdId(cursor.getString(cursor.getColumnIndex(TableField.FIELD_CHAT_THIRDID)));
            msg.setShopAddress(cursor.getString(cursor.getColumnIndex(TableField.FIELD_CHAT_SHOPADDRESS)));
            msg.setCardSign(cursor.getString(cursor.getColumnIndex(TableField.FIELD_CHAT_CARDSIGN)));

            msg.setLocalUrl(cursor.getString(cursor.getColumnIndex(TableField.FIELD_CHAT_LOCALURL)));
            msg.setShowTime(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_CHAT_SHOWTIME)) == 1 ? true : false);

            msg.setMessageId(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA1)));
            msg.setThirdGender(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA2)));
            msg.setDatingSOSId(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA3)));
            msg.setFriendLog(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA4)));
            msg.setInviteType(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA5)));
            msg.setOffLineMsg(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA7))==1?true:false);
            msg.setNumber(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA6)));
            msg.setShareUrl(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA8)));
            msg.setShareTitle(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA9)));
            msg.setShareThumb(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA10)));
            msg.setRedpacketId(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA11)));
            msg.setCreateName(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA12)));
            msg.setSource(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA13)));
            msg.setRemoteSource(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA14)));
            msg.setCreateTime(cursor.getLong(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA15)));
            msg.setUsersource(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA16)));
            msg.setUserfrom(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA17)));
            if (!TextUtils.isEmpty(msg.getUserId())){
                UserBaseVo baseVo = FinalUserDataBase.getInstance().getUserBaseVoByUid(msg.getUserId());
                if(baseVo!=null&&!TextUtils.isEmpty(baseVo.getShowName())){
                    msg.setUsername(baseVo.getShowName());
                }
            }
            list.add(msg);
        }
        cursor.close();
        return list;
    }

    /**
     *My chat history record contains information system information
     *
     * @return
     */
    public List<ChatMsg> getChatMsgEventList() {
        List<ChatMsg> list = new ArrayList<ChatMsg>();
        if (db != null) {
            String sql = "select * from " + TableField.TABLE_CHAT_EVENT +  /*" where "  +TableField.FIELD_RESERVED_DATA7 + " is not 1*/ " order by " + TableField.FIELD_CHAT_MSGTIME + " desc ";
            Cursor cursor = db.rawQuery(sql, null);
            ChatMsg msg;
            int total = 0;
            int unFriendTotle = 0;
            while (cursor.moveToNext()) {

                msg = new ChatMsg();
                msg.setId(cursor.getInt(cursor.getColumnIndex(TableField._ID)));
                msg.setContent(cursor.getString(cursor.getColumnIndex(TableField.FIELD_CHAT_BODY)));
                msg.setGender(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_FRIEND_GENDER)));
                msg.setMsgTime(cursor.getLong(cursor.getColumnIndex(TableField.FIELD_CHAT_MSGTIME)));
                msg.setType(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_CHAT_TYPE)));
                msg.setUserId(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_UID)));
                msg.setUserImage(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_PIC)));
                msg.setUsername(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_UNAME)));
                msg.setChatId(cursor.getString(cursor.getColumnIndex(TableField.FIELD_CHAT_ID)));

                msg.setHidden(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_CHAT_HIDDEN)) == 1 ? true : false);
                msg.setUnread(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_CHAT_UNREAD)));
                msg.setSystem(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_CHAT_SYSTEM)) == 1 ? true : false);
                msg.setDismissGroup(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_CHAT_DISMISSGROUP)) == 1 ? true : false);
                msg.setKickGroup(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_CHAT_KICKGROUP)) == 1 ? true : false);
                msg.setMessageId(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA1)));
                msg.setShareFriendName(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA2)));
                msg.setModifyType(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA3)));
                msg.setFriendLog(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA5)));
                msg.setGroupMask(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA6)) == 1 ? true : false);
                msg.setAtGroupMe(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA8)));

                msg.setGroupImage(cursor.getString(cursor.getColumnIndex(TableField.FIELD_CHAT_GROUP_IMAGE)));
                msg.setMsgTypeInt(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_CHAT_OBJECT)));
                msg.setCreateName(cursor.getString(cursor.getColumnIndex(TableField.FIELD_CHAT_CREATENAME)));
                msg.setVip_type(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA12)));
                msg.setVip_level(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA13)));
                msg.setIs_vip(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA14)) == 1);
                msg.setTop(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA15)) == 1);
                msg.setRemoteSource(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA18)));
                msg.setInviteType(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA19)));
                msg.setInviteSource(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA20)));
                msg.setMoney(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA9)));
                msg.setMode(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA11)));
                msg.setNoticeType(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA26)));

                if (msg.isTop()) {
                    msg.setTopTime(cursor.getLong(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA16)));
                }
                if (msg.getChatId().startsWith("group")) {
                    msg.setGroup(true);
                    try {
                        List<GroupMemberAvatarVo> lists = new ArrayList<>();
                        GroupMemberAvatarVo vo;
                        String[] split = msg.getUserImage().split("#");
                        for (int i = 0; i < split.length; i++) {
                            vo = new GroupMemberAvatarVo();
                            String[] splitVo = split[i].split("___");
                            int gender = 2;
                            try {
                                gender = Integer.parseInt(splitVo[1]);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            String image = "";
                            try {
                                image = splitVo[0];
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            vo.setGender(gender);
                            vo.setImage(image);
                            lists.add(vo);
                        }
                        msg.setMemberAvatarList(lists);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (!msg.isHidden()) {
                    list.add(msg);
                    if (msg.getChatId().equals("system-0") || msg.getChatId().equals("system-1") || msg.getChatId().equals("system-3") || msg.getChatId().equals("system-4") || msg.getChatId().equals("system-5")) {
                        total += msg.getUnread();
                    } else if (!msg.getGroupMask()) {
                        total += msg.getUnread();
                    }
                } else {
                    if (!msg.isSystem() && !msg.getGroupMask()) {
                        unFriendTotle += msg.getUnread();
                    }
                }
               if (!msg.isGroup() && msg.getMsgTypeInt() != 3 && !TextUtils.isEmpty(msg.getChatId()) && !TextUtils.equals("everyone",msg.getChatId()) && !msg.getChatId().startsWith("system")) {
                    UserBaseVo baseVo = FinalUserDataBase.getInstance().getUserBaseVoByUid(msg.getChatId());
                    if(baseVo!=null&&!TextUtils.isEmpty(baseVo.getShowName())){
                        msg.setUsername(baseVo.getShowName());
                    }
                }
            }
            try {
                map.put("totalunread", total);
            } catch (Exception e) {
                e.printStackTrace();
            }
            cursor.close();
        }
        return list;
    }


    /**
     * Get my message history chat records only chat records (forward) used in the
     */
    public List<ChatMsg> getChatMsgEventListChat() {

        String sql = "select * from " + TableField.TABLE_CHAT_EVENT + " where "
                /*+ TableField.FIELD_CHAT_HIDDEN + "=0 and "*/
                + TableField.FIELD_CHAT_SYSTEM + "=0 "/*and "
                + TableField.FIELD_RESERVED_DATA7 + "!=1*/+" order by "
                + TableField.FIELD_CHAT_MSGTIME + " desc ";
        Cursor cursor = db.rawQuery(sql, null);
        List<ChatMsg> list = new ArrayList<ChatMsg>();
        ChatMsg msg;
        int unFriendTotle = 0;
        while (cursor.moveToNext()) {

            msg = new ChatMsg();
            msg.setId(cursor.getInt(cursor.getColumnIndex(TableField._ID)));
            msg.setContent(cursor.getString(cursor.getColumnIndex(TableField.FIELD_CHAT_BODY)));
            msg.setGender(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_FRIEND_GENDER)));
            msg.setMsgTime(cursor.getLong(cursor.getColumnIndex(TableField.FIELD_CHAT_MSGTIME)));
            msg.setType(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_CHAT_TYPE)));
            msg.setUserId(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_UID)));
            msg.setUserImage(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_PIC)));
            msg.setUsername(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_UNAME)));
            msg.setChatId(cursor.getString(cursor.getColumnIndex(TableField.FIELD_CHAT_ID)));
            msg.setUnread(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_CHAT_UNREAD)));
            msg.setSystem(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_CHAT_SYSTEM)) == 1);
            msg.setDismissGroup(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_CHAT_DISMISSGROUP)) == 1);
            msg.setKickGroup(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_CHAT_KICKGROUP)) == 1);
            msg.setMessageId(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA1)));
            msg.setFriendLog(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA5)));
            msg.setGroupMask(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA6)) == 1);
            msg.setAtGroupMe(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA8)));

            msg.setGroupImage(cursor.getString(cursor.getColumnIndex(TableField.FIELD_CHAT_GROUP_IMAGE)));
            msg.setMsgTypeInt(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_CHAT_OBJECT)));
            msg.setTop(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA15)) == 1);

            msg.setRemoteSource(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA18)));
            msg.setInviteType(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA19)));
            msg.setInviteSource(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA20)));
            msg.setMoney(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA9)));
            msg.setMode(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA11)));
            msg.setNoticeType(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA26)));
            if (msg.isTop()) {
                msg.setTopTime(cursor.getLong(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA16)));
            }
            if (msg.getChatId().startsWith("group")) {
                msg.setGroup(true);
                try {
                    List<GroupMemberAvatarVo> lists = new ArrayList<>();
                    GroupMemberAvatarVo vo;
                    String[] split = msg.getUserImage().split("#");
                    for (int i = 0; i < split.length; i++) {
                        vo = new GroupMemberAvatarVo();
                        String[] splitVo = split[i].split("___");
                        int gender = 2;
                        try {
                            gender = Integer.parseInt(splitVo[1]);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        String image = "";
                        try {
                            image = splitVo[0];
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        vo.setGender(gender);
                        vo.setImage(image);
                        lists.add(vo);
                    }
                    msg.setMemberAvatarList(lists);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (!msg.isHidden()) {
                list.add(msg);
            } else {
                if (!msg.getGroupMask()) {
                    unFriendTotle++;
                }

            }

        }
        try {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getChatId().equals("system-2"))
                {
                    list.get(i).setUnread(unFriendTotle);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        cursor.close();
        return list;
    }

    /**
     * Get unread item number
     *
     * @return
     */
    public Map<String, Integer> getUnreadMap() {
        if (!map.containsKey("totalunread")) {
            map.put("totalunread", 0);

        }
        return map;
    }

    /**
     * Add buddy information list
     */
    public List<ChatMsg> getChatMsgAddContactList() {

        String sql = "select * from " + TableField.TABLE_CHAT_EVENT + " where "
                + TableField.FIELD_CHAT_SYSTEM + "=1 and "
                + TableField.FIELD_CHAT_TYPE + "=0 and "
                + TableField.FIELD_CHAT_HIDDEN + "=1 "
                + " order by " + TableField.FIELD_CHAT_MSGTIME + " desc ";
        Cursor cursor = db.rawQuery(sql, null);
        List<ChatMsg> list = new ArrayList<ChatMsg>();
        ChatMsg msg;

        while (cursor.moveToNext()) {

            msg = new ChatMsg();
            msg.setId(cursor.getInt(cursor.getColumnIndex(TableField._ID)));
            msg.setContent(cursor.getString(cursor.getColumnIndex(TableField.FIELD_CHAT_BODY)));
            msg.setGender(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_FRIEND_GENDER)));
            msg.setMsgTime(cursor.getLong(cursor.getColumnIndex(TableField.FIELD_CHAT_MSGTIME)));
            msg.setType(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_CHAT_TYPE)));
            msg.setUserId(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_UID)));
            msg.setUserImage(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_PIC)));
            msg.setUsername(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_UNAME)));
            msg.setChatId(cursor.getString(cursor.getColumnIndex(TableField.FIELD_CHAT_ID)));
            msg.setUnread(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_CHAT_UNREAD)));
            msg.setSystem(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_CHAT_SYSTEM)) == 1 );
            msg.setAgree(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_CHAT_AGREE)) == 1);
            msg.setMessageId(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA1)));
            msg.setShareFriendName(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA2)));
            msg.setOffLineMsg(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA7))==1?true:false);
            msg.setRemoteSource(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA18)));
            msg.setGroupImage(cursor.getString(cursor.getColumnIndex(TableField.FIELD_CHAT_GROUP_IMAGE)));
            msg.setMsgTypeInt(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_CHAT_OBJECT)));
            list.add(msg);

        }

        cursor.close();
        return list;
    }

    /**
     * Add trans list
     */
    public List<TransVo> getTransList(int type,String address) {
        if (!address.startsWith("0x")){
            address = "0x" + address;
        }
        String sql = "select * from " + TableField.TABLE_TRANS + " where "
                + TableField.FIELD_RESERVED_DATA1 + "=? and ("
                + TableField.FIELD_RESERVED_DATA5 + "=? or "
                + TableField.FIELD_RESERVED_DATA6 + "=? )"
                + " order by " + TableField.FIELD_CHAT_MSGTIME + " desc ";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(type),address,address});
        List<TransVo> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            TransVo vo = new TransVo();
            vo.setTime(cursor.getLong(cursor.getColumnIndex(TableField.FIELD_CHAT_MSGTIME)));
            vo.setType(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA1)));
            vo.setValue(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA2)));
            vo.setFee(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA3)));
            vo.setTx(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA4)));
            vo.setFromAddress(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA5)));
            vo.setToAddress(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA6)));
            vo.setTxBlockNumber(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA7)));
            vo.setTxurl(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA8)));
            vo.setNoticeType(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA9)));
            vo.setState(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA10)) + 1 );
            if(vo.getNoticeType() == 0)//send
            {
                if(TextUtils.equals(address,vo.getToAddress()) && !TextUtils.equals(address,vo.getFromAddress()))
                {
                    continue;
                }
            }else{  //rec
                if(TextUtils.equals(address,vo.getFromAddress()) && !TextUtils.equals(address,vo.getToAddress()))
                {
                    continue;
                }
            }
            list.add(vo);
        }
        cursor.close();
        getTransTempList(list,type,address);
        return list;
    }

    /**
     * Add trans list
     */
    public void getTransTempList(List<TransVo> transVoList,int type,String address) {

        String sql = "select * from " + TableField.TABLE_TRANS_TEMP + " where "
                + TableField.FIELD_RESERVED_DATA1 + "=? and ("
                + TableField.FIELD_RESERVED_DATA5 + "=? or "
                + TableField.FIELD_RESERVED_DATA6 + "=? )"
                + " order by " + TableField.FIELD_CHAT_MSGTIME + " asc ";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(type),address,address});
        while (cursor.moveToNext()) {
            TransVo vo = new TransVo();
            vo.setTime(cursor.getLong(cursor.getColumnIndex(TableField.FIELD_CHAT_MSGTIME)));
            vo.setType(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA1)));
            vo.setValue(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA2)));
            vo.setFee(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA3)));
            vo.setTx(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA4)));
            vo.setFromAddress(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA5)));
            vo.setToAddress(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA6)));
            vo.setTxBlockNumber(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA7)));
            vo.setTxurl(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA8)));
            vo.setNoticeType(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA9)));
            vo.setState(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA10)) + 1 );
            if(vo.getNoticeType() == 0)//send
            {
                if(TextUtils.equals(address,vo.getToAddress()) && !TextUtils.equals(address,vo.getFromAddress()))
                {
                    continue;
                }
            }else{  //rec
                if(TextUtils.equals(address,vo.getFromAddress()) && !TextUtils.equals(address,vo.getToAddress()))
                {
                    continue;
                }
            }
            boolean isFound = false;
            for (int i = 0 ; i < transVoList.size() ; i++){
                String tx = transVoList.get(i).getTx();
                if (TextUtils.equals(tx,vo.getTx())){
                    FinalUserDataBase.getInstance().deleteTransTemp(vo.getTx());
                    isFound = true;
                    break;
                }
            }

            if (!isFound){
                transVoList.add(0,vo);
            }
        }
        cursor.close();
    }

    /**
     * Add trans message list
     */
    public List<ChatMsg> getChatMsgTransList() {
        String sql = "select * from " + TableField.TABLE_CHAT_EVENT + " where "
                + TableField.FIELD_CHAT_SYSTEM + "=1 and "
                + TableField.FIELD_CHAT_TYPE + "=300 and "
                + TableField.FIELD_CHAT_HIDDEN + "=1 "
                + " order by " + TableField.FIELD_CHAT_MSGTIME + " desc ";
        Cursor cursor = db.rawQuery(sql, null);
        List<ChatMsg> list = new ArrayList<>();
        ChatMsg msg;
        while (cursor.moveToNext()) {
            msg = new ChatMsg();
            msg.setId(cursor.getInt(cursor.getColumnIndex(TableField._ID)));
            msg.setMsgTime(cursor.getLong(cursor.getColumnIndex(TableField.FIELD_CHAT_MSGTIME)));
            msg.setType(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_CHAT_TYPE)));
            msg.setMessageId(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA1)));
            msg.setCreateTime(cursor.getLong(cursor.getColumnIndex(TableField.FIELD_CHAT_CREATETIME)));
            msg.setMoney(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA9)));
            msg.setNumber(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA10)));
            msg.setMode(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA11)));
            msg.setInviteType(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA19)));
            msg.setFee(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA21)));
            msg.setFromAddress(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA22)));
            msg.setToAddress(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA23)));
            msg.setTxBlockNumber(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA24)));
            msg.setShareUrl(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA25)));
            msg.setNoticeType(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA26)));
            list.add(msg);
        }
        cursor.close();
        return list;
    }

    /**
     * Add buddy request information after the new one
     */
    public ChatMsg getChatMsgAddContact() {


        String sql = "select * from " + TableField.TABLE_CHAT_EVENT + " where "
                + TableField.FIELD_CHAT_SYSTEM + "=1 and "
                + TableField.FIELD_CHAT_TYPE + "=0 and "
                + TableField.FIELD_CHAT_HIDDEN + "=1 "
                + " order by " + TableField.FIELD_CHAT_MSGTIME + " desc "
                + " limit " + 0 + "," + 1;
        Cursor cursor = db.rawQuery(sql, null);
        ChatMsg msg = null;

        if (cursor.moveToNext()) {

            msg = new ChatMsg();
            msg.setId(cursor.getInt(cursor.getColumnIndex(TableField._ID)));
            msg.setContent(cursor.getString(cursor.getColumnIndex(TableField.FIELD_CHAT_BODY)));
            msg.setGender(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_FRIEND_GENDER)));
            msg.setMsgTime(cursor.getLong(cursor.getColumnIndex(TableField.FIELD_CHAT_MSGTIME)));
            msg.setType(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_CHAT_TYPE)));
            msg.setUserId(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_UID)));
            msg.setUserImage(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_PIC)));
            msg.setUsername(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_UNAME)));
            msg.setChatId(cursor.getString(cursor.getColumnIndex(TableField.FIELD_CHAT_ID)));
            msg.setUnread(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_CHAT_UNREAD)));
            msg.setSystem(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_CHAT_SYSTEM)) == 1);
            msg.setAgree(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_CHAT_AGREE)) == 1);
            msg.setMessageId(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA1)));
            msg.setShareFriendName(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA2)));

            msg.setGroupImage(cursor.getString(cursor.getColumnIndex(TableField.FIELD_CHAT_GROUP_IMAGE)));
            msg.setMsgTypeInt(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_CHAT_OBJECT)));
        }

        cursor.close();
        return msg;
    }
    /**
     * Add Trans after the new one
     */
    public ChatMsg getChatMsgTrans() {


        String sql = "select * from " + TableField.TABLE_CHAT_EVENT + " where "
                + TableField.FIELD_CHAT_SYSTEM + "=1 and "
                + TableField.FIELD_CHAT_TYPE + "=300 and "
                + TableField.FIELD_CHAT_HIDDEN + "=1 "
                + " order by " + TableField.FIELD_CHAT_MSGTIME + " desc "
                + " limit " + 0 + "," + 1;
        Cursor cursor = db.rawQuery(sql, null);
        ChatMsg msg = null;

        if (cursor.moveToNext()) {

            msg = new ChatMsg();
            msg.setId(cursor.getInt(cursor.getColumnIndex(TableField._ID)));
            msg.setMsgTime(cursor.getLong(cursor.getColumnIndex(TableField.FIELD_CHAT_MSGTIME)));
            msg.setType(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_CHAT_TYPE)));
            msg.setUsername(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_UNAME)));
            msg.setChatId(cursor.getString(cursor.getColumnIndex(TableField.FIELD_CHAT_ID)));
            msg.setUnread(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_CHAT_UNREAD)));
            msg.setSystem(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_CHAT_SYSTEM)) == 1);
            msg.setMessageId(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA1)));
            msg.setMsgTypeInt(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_CHAT_OBJECT)));
            msg.setMoney(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA9)));
            msg.setNoticeType(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA26)));

        }

        cursor.close();
        return msg;
    }

    /**
     * Save my information (conversation) recently
     *
     * @param vo
     */
    public void saveChatEvent(ChatMsg vo) {

        boolean result = hasChatEvent(vo, vo.getChatId(), vo.isSystem(), vo.getType(), vo.isHidden());
        if (vo.getType() == 103) {//Share information content into shareTitle
            vo.setContent(vo.getShareTitle());
        }
        if (result) {
            updateChatEventMsg(vo, vo.isSystem(), true);
        } else {
            insertChatEvent(vo);
        }

    }
    /**
     * Save the reference information Didn't do heavy work (if the need to rewrite)
     */
    public boolean saveFriendsReComment(FriendRecommentVo vo) {
        boolean result = hasFriendsRecomment(vo.getFriendId());
        if (result) {
            return false;
        } else {
            insertFriendsRecomment(vo);
            return true;
        }
    }



    /**
     * Save the token list all
     * @param vo token bean
     */
    public void insertToken(TokenVo vo) {
        ContentValues values = new ContentValues();
        values.put(TableField.FIELD_RESERVED_DATA1, vo.getTokenName());
        values.put(TableField.FIELD_RESERVED_DATA2, vo.getTokenPic());
        values.put(TableField.FIELD_RESERVED_DATA3, vo.getTokenNumber());
        values.put(TableField.FIELD_RESERVED_DATA4, vo.getTokenPrice());
        values.put(TableField.FIELD_RESERVED_DATA5, vo.getTokenTotalPrice());
        db.insert(TableField.TABLE_TOKEN_LIST, TableField._ID, values);
    }

    /**
     * update the token list all
     */
    public void updateTokenList(TokenVo vo) {
        boolean result = hasToken(vo);
        if(result){
            ContentValues values = new ContentValues();
            values.put(TableField.FIELD_RESERVED_DATA2, vo.getTokenPic());
            values.put(TableField.FIELD_RESERVED_DATA3, vo.getTokenNumber());
            values.put(TableField.FIELD_RESERVED_DATA4, vo.getTokenPrice());
            values.put(TableField.FIELD_RESERVED_DATA5, vo.getTokenTotalPrice());
            db.update(TableField.TABLE_TOKEN_LIST,values,TableField.FIELD_RESERVED_DATA1 + "=? ",new String[]{vo.getTokenName()});
        }
    }

    /**
     * check has token
     * */
    private boolean hasToken(TokenVo vo){
        String sql = "select * from " + TableField.TABLE_TOKEN_LIST + " where " + TableField.FIELD_RESERVED_DATA1 + "=?";
        Cursor cursor = db.rawQuery(sql, new String[]{vo.getTokenName()});
        if (cursor.moveToNext()) {
            cursor.close();
            return true;
        }else{
            cursor.close();
            return false;
        }
    }

    /**
     * Save the trans list all
     */
    public void insertTrans(TransVo vo,boolean isTemp) {
        ContentValues values = new ContentValues();
        values.put(TableField.FIELD_CHAT_MSGTIME, vo.getTime());
        values.put(TableField.FIELD_RESERVED_DATA1, vo.getType());
        values.put(TableField.FIELD_RESERVED_DATA2, vo.getValue());
        values.put(TableField.FIELD_RESERVED_DATA3, vo.getFee());
        values.put(TableField.FIELD_RESERVED_DATA4, vo.getTx());
        values.put(TableField.FIELD_RESERVED_DATA5, vo.getFromAddress());
        values.put(TableField.FIELD_RESERVED_DATA6, vo.getToAddress());
        values.put(TableField.FIELD_RESERVED_DATA7, vo.getTxBlockNumber());
        values.put(TableField.FIELD_RESERVED_DATA8, vo.getTxurl() );
        values.put(TableField.FIELD_RESERVED_DATA9, vo.getNoticeType());
        if (isTemp){
            values.put(TableField.FIELD_RESERVED_DATA10, vo.getState() - 1);
            db.insert(TableField.TABLE_TRANS_TEMP, TableField._ID, values);
        }else{
            values.put(TableField.FIELD_RESERVED_DATA10, vo.getState());
            db.insert(TableField.TABLE_TRANS, TableField._ID, values);
        }
    }

    /**
     * Save the trans list all
     */
    public void updateTransTemp(TransVo vo) {
        boolean result = hasTransTemp(vo);
        if(result)
        {
            ContentValues values = new ContentValues();
            values.put(TableField.FIELD_RESERVED_DATA7, vo.getTxBlockNumber());
            values.put(TableField.FIELD_RESERVED_DATA10, vo.getState()-1);
            db.update(
                    TableField.TABLE_TRANS_TEMP,
                    values,
                    TableField.FIELD_RESERVED_DATA4 + "=? ",
                    new String[]{vo.getTx()});


        }
    }
    private  boolean  hasTransTemp(TransVo vo)
    {
        String sql = "select * from " + TableField.TABLE_TRANS_TEMP + " where "
                + TableField.FIELD_RESERVED_DATA4 + "=?";
        Cursor cursor = db.rawQuery(sql, new String[]{vo.getTx()});
        if (cursor.moveToNext()) {
            cursor.close();
            return true;
        }
        else{
            cursor.close();
            return false;
        }
    }

    /**
     * delete the transtemp list
     */
    public void deleteTransTemp(String tx) {
        db.delete(TableField.TABLE_TRANS_TEMP, TableField.FIELD_RESERVED_DATA4 + "=?", new String[]{tx});
    }

    private boolean hasFriendsRecomment(String friendId) {
        if (TextUtils.isEmpty(friendId)) return false;
        boolean result = false;

        String sql = "select * from " + TableField.TABLE_FRIENDS_RECOMMENT
                + " where " + TableField.FIELD_CHAT_THIRDID + "=?";
        Cursor cursor = db.rawQuery(sql, new String[]{friendId});
        result = cursor.moveToNext();
        cursor.close();
        return result;
    }

    private void insertFriendsRecomment(FriendRecommentVo vo) {
        ContentValues values = new ContentValues();
        values.put(TableField._MSGID, vo.getMsgId());
        values.put(TableField.FIELD_CHAT_THIRDID, vo.getFriendId());
        values.put(TableField.FIELD_CHAT_THIRDNAME, vo.getThirdName());
        values.put(TableField.FIELD_FRIEND_UID, vo.getUid());
        values.put(TableField.FIELD_FRIEND_UNAME, vo.getUsername());
        values.put(TableField.FIELD_FRIEND_PIC, vo.getPic());
        values.put(TableField.FIELD_FRIEND_THUMB, vo.getThumb());
        values.put(TableField.FIELD_CHAT_MSGTIME, vo.getTime());
        values.put(TableField.FIELD_CHAT_TYPE, vo.getType());
        values.put(TableField.FIELD_CHAT_UNREAD, vo.getUnread());
        values.put(TableField.FIELD_CHAT_AGREE, vo.isAgree() ? 1 : 0);
        db.insert(TableField.TABLE_FRIENDS_RECOMMENT, TableField._ID, values);
    }

    public void deleteFriendsRecommentByFriendId(String friendId) {
        if (TextUtils.isEmpty(friendId)) return;
        db.delete(TableField.TABLE_FRIENDS_RECOMMENT, TableField.FIELD_CHAT_THIRDID + "=?", new String[]{friendId});
    }

    public void deleteFriendsRecommentByUid(String uid) {
        if (TextUtils.isEmpty(uid)) return;
        db.delete(TableField.TABLE_FRIENDS_RECOMMENT, TableField.FIELD_FRIEND_UID + "=?", new String[]{uid});
    }

    /**
     * For reference information
     *
     * @return
     */
    public List<FriendRecommentVo> getFriendsRecomment() {

        List<FriendRecommentVo> mList = new ArrayList<FriendRecommentVo>();
        if (null == db) {
            return mList;
        }
        String sql = "select * from " + TableField.TABLE_FRIENDS_RECOMMENT + " order by " + TableField._ID + " desc ";
        Cursor cursor = db.rawQuery(sql, null);
        FriendRecommentVo msg;
        while (cursor.moveToNext()) {
            msg = new FriendRecommentVo();
            msg.setMsgId(cursor.getString(cursor.getColumnIndex(TableField._MSGID)));
            msg.setFriendId(cursor.getString(cursor.getColumnIndex(TableField.FIELD_CHAT_THIRDID)));
            msg.setUid(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_UID)));
            msg.setUsername(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_UNAME)));
            msg.setPic(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_PIC)));
            msg.setThumb(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_THUMB)));
            msg.setType(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_CHAT_TYPE)));
            msg.setTime(cursor.getLong(cursor.getColumnIndex(TableField.FIELD_CHAT_MSGTIME)));
            msg.setUnread(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_CHAT_UNREAD)));
            msg.setThirdName(cursor.getString(cursor.getColumnIndex(TableField.FIELD_CHAT_THIRDNAME)));

            UserBaseVo baseVo = FinalUserDataBase.getInstance().getUserBaseVoByUid(msg.getUid());
            if (baseVo == null) {//不是好友
                msg.setAgree(false);
            } else {
                msg.setAgree(true);
            }
            try {
                if (!TextUtils.isEmpty(msg.getThirdName())) {
                    if (msg.getThirdName().endsWith(")")
                            && msg.getThirdName().contains("(")) {
                        msg.setThirdName(msg.getThirdName().substring(0, msg.getThirdName().lastIndexOf("(")));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            mList.add(msg);
        }
        cursor.close();

        return mList;

    }


    /**
     * Friend recommended number of unread item was read
     */
    public void updateFriendsRecommentUnread() {
        String sql = "update " + TableField.TABLE_FRIENDS_RECOMMENT
                + " set " + TableField.FIELD_CHAT_UNREAD + "=0";
        db.execSQL(sql);
    }

    /**
     * Insert the recent chat messages
     *
     * @param vo
     */
    public void insertChatEvent(ChatMsg vo) {
        ContentValues values = new ContentValues();
        values.put(TableField.FIELD_FRIEND_UNAME, vo.getUsername());
        values.put(TableField.FIELD_FRIEND_UID, vo.getUserId());
        values.put(TableField.FIELD_FRIEND_PIC, vo.getUserImage());
        values.put(TableField.FIELD_FRIEND_GENDER, vo.getGender());
        values.put(TableField.FIELD_CHAT_TYPE, vo.getType());
        values.put(TableField.FIELD_CHAT_BODY, vo.getContent());
        values.put(TableField.FIELD_CHAT_MSGTIME, vo.getMsgTime());
        values.put(TableField.FIELD_CHAT_ID, vo.getChatId());
        values.put(TableField.FIELD_CHAT_UNREAD, vo.getUnread());
        values.put(TableField.FIELD_CHAT_SYSTEM, vo.isSystem() ? 1 : 0);
        values.put(TableField.FIELD_CHAT_HIDDEN, vo.isHidden() ? 1 : 0);

        values.put(TableField.FIELD_CHAT_GROUP_IMAGE, vo.getGroupImage());
        values.put(TableField.FIELD_CHAT_OBJECT, vo.getMsgTypeInt());

        values.put(TableField.FIELD_CHAT_CREATEID, vo.getCreateId());
        values.put(TableField.FIELD_CHAT_CREATENAME, vo.getCreateName());
        values.put(TableField.FIELD_CHAT_CREATEIMAGE, vo.getCreateImage());
        values.put(TableField.FIELD_CHAT_CREATEAGE, vo.getCreateAge());
        values.put(TableField.FIELD_CHAT_CREATEGENDER, vo.getCreateGender());
        values.put(TableField.FIELD_CHAT_CREATEGSIGN, vo.getCreategSign());
        values.put(TableField.FIELD_CHAT_SHOPADDRESS, vo.getShopAddress());
        values.put(TableField.FIELD_CHAT_SORT, vo.getSort());
        values.put(TableField.FIELD_CHAT_GUEST, vo.getGuest());
        values.put(TableField.FIELD_CHAT_INVITEMSG, vo.getInviteMsg());
        values.put(TableField.FIELD_CHAT_CREATETIME, vo.getCreateTime());
        values.put(TableField.FIELD_CHAT_INVITEID, vo.getInviteId());
        values.put(TableField.FIELD_RESERVED_DATA1, vo.getMessageId());
        values.put(TableField.FIELD_RESERVED_DATA2, vo.getShareFriendName());
        values.put(TableField.FIELD_RESERVED_DATA3, vo.getModifyType());
        values.put(TableField.FIELD_RESERVED_DATA4, vo.getSceneType());
        values.put(TableField.FIELD_RESERVED_DATA5, vo.getFriendLog());
        values.put(TableField.FIELD_RESERVED_DATA6, vo.getGroupMask() ? 1 : 0);
        values.put(TableField.FIELD_RESERVED_DATA7, vo.isOffLineMsg() ? 1 : 0);
        if (vo.isAtGroupMe() > 0) {
            values.put(TableField.FIELD_RESERVED_DATA8, vo.isAtGroupMe());
        }
        values.put(TableField.FIELD_CHAT_DISMISSGROUP, vo.isDismissGroup() ? 1 : 0);
        values.put(TableField.FIELD_CHAT_KICKGROUP, vo.isKickGroup() ? 1 : 0);

        values.put(TableField.FIELD_RESERVED_DATA9, vo.getMoney());
        values.put(TableField.FIELD_RESERVED_DATA10, vo.getNumber());
        values.put(TableField.FIELD_RESERVED_DATA11, vo.getMode());

        values.put(TableField.FIELD_RESERVED_DATA12, vo.getVip_type());
        values.put(TableField.FIELD_RESERVED_DATA13, vo.getVip_level());
        values.put(TableField.FIELD_RESERVED_DATA14, vo.isIs_vip() ? 1 : 0);


        values.put(TableField.FIELD_RESERVED_DATA17, vo.getInviteImage());

        values.put(TableField.FIELD_RESERVED_DATA18, vo.getRemoteSource());

        values.put(TableField.FIELD_RESERVED_DATA19, vo.getInviteType());

        values.put(TableField.FIELD_RESERVED_DATA20, vo.getInviteSource());
        values.put(TableField.FIELD_RESERVED_DATA21, vo.getFee());
        values.put(TableField.FIELD_RESERVED_DATA22, vo.getFromAddress());
        values.put(TableField.FIELD_RESERVED_DATA23, vo.getToAddress());
        values.put(TableField.FIELD_RESERVED_DATA24, vo.getTxBlockNumber());
        values.put(TableField.FIELD_RESERVED_DATA25, vo.getShareUrl());
        values.put(TableField.FIELD_RESERVED_DATA26, vo.getNoticeType());
        db.insert(TableField.TABLE_CHAT_EVENT, TableField._ID, values);

    }

    public boolean hasChatEvent(ChatMsg vo, String chatId, boolean isSystem, int type, boolean isHidden) {
        if (isSystem && isHidden) return false;
        String sql = "select " + TableField.FIELD_CHAT_ID
                + " from " + TableField.TABLE_CHAT_EVENT
                + " where "
                + TableField.FIELD_CHAT_ID + "=? and "
                + TableField.FIELD_CHAT_HIDDEN + "=0 and "
                + TableField.FIELD_CHAT_SYSTEM + "=1";

        String[] selectionArgs = null;

        if (!isSystem && !isHidden) {//normal chat
            sql = "select " + TableField.FIELD_CHAT_ID
                    + " from " + TableField.TABLE_CHAT_EVENT
                    + " where "
                    + TableField.FIELD_CHAT_ID + "=? and "
                    + TableField.FIELD_CHAT_SYSTEM + "=0 "/*and "
                    + TableField.FIELD_RESERVED_DATA7 + "=" + (vo.isOffLineMsg() ? 1 : 0)*/;
        } else if (!isSystem && isHidden) {
            sql = "select " + TableField.FIELD_CHAT_ID
                    + " from " + TableField.TABLE_CHAT_EVENT
                    + " where "
                    + TableField.FIELD_CHAT_ID + "=? and "
                    + TableField.FIELD_CHAT_SYSTEM + "=0"
            ;

        }

        selectionArgs = new String[]{
                String.valueOf(chatId),
        };
        Cursor cursor = db.rawQuery(sql, selectionArgs);
        boolean result = cursor.moveToNext();

        cursor.close();

        return result;
    }

    /**
     * Modify the recent chat messages
     *
     * @param vo
     * @param isSystem Whether the system messages
     * @param isNewMsg Whether the new news
     */
    public void updateChatEventMsg(ChatMsg vo, boolean isSystem, boolean isNewMsg) {
        ContentValues values = new ContentValues();
        if (!TextUtils.isEmpty(vo.getUsername())) {
            values.put(TableField.FIELD_FRIEND_UNAME, vo.getUsername());
        }
        if (!TextUtils.isEmpty(vo.getUserImage())) {
            values.put(TableField.FIELD_FRIEND_PIC, vo.getUserImage());
        }
        values.put(TableField.FIELD_FRIEND_GENDER, vo.getGender());
        values.put(TableField.FIELD_CHAT_TYPE, vo.getType());
        values.put(TableField.FIELD_CHAT_BODY, vo.getContent());
        values.put(TableField.FIELD_CHAT_CREATENAME, vo.getCreateName());
        values.put(TableField.FIELD_CHAT_MSGTIME, vo.getMsgTime());
        if(isSystem)// system msg clear inside outside should be delete
        {
            values.put(TableField.FIELD_RESERVED_DATA1, vo.getMessageId());
        }
        if (vo.isAtGroupMe() > 0) {//Only when the @ I go to modify Otherwise do not change state
            values.put(TableField.FIELD_RESERVED_DATA8, vo.isAtGroupMe());
        }
        if (isNewMsg) {
            String sql = "select " + TableField.FIELD_CHAT_UNREAD + " from " + TableField.TABLE_CHAT_EVENT
                    + " where " + TableField.FIELD_CHAT_ID + "=? and " + TableField.FIELD_CHAT_HIDDEN + " = 0";
            Cursor cursor = db.rawQuery(sql, new String[]{vo.getChatId()});

            int unread = 0;
            if (cursor.moveToNext()) {
                unread = cursor.getInt(cursor.getColumnIndex(TableField.FIELD_CHAT_UNREAD));
            }
            cursor.close();

            values.put(TableField.FIELD_CHAT_UNREAD, unread + vo.getUnread());

            values.put(TableField.FIELD_CHAT_HIDDEN, vo.isHidden() ? 1 : 0);
            values.put(TableField.FIELD_RESERVED_DATA2, vo.getShareFriendName());
            values.put(TableField.FIELD_RESERVED_DATA3, vo.getModifyType());
            values.put(TableField.FIELD_RESERVED_DATA5, vo.getFriendLog());
            values.put(TableField.FIELD_RESERVED_DATA6, vo.getGroupMask() ? 1 : 0);
            values.put(TableField.FIELD_RESERVED_DATA7, vo.isOffLineMsg() ? 1 : 0);

            values.put(TableField.FIELD_RESERVED_DATA9, vo.getMoney());
            values.put(TableField.FIELD_RESERVED_DATA10, vo.getNumber());
            values.put(TableField.FIELD_RESERVED_DATA11, vo.getMode());

            values.put(TableField.FIELD_RESERVED_DATA12, vo.getVip_type());
            values.put(TableField.FIELD_RESERVED_DATA13, vo.getVip_level());
            values.put(TableField.FIELD_RESERVED_DATA14, vo.isIs_vip() ? 1 : 0);

            values.put(TableField.FIELD_RESERVED_DATA17, vo.getInviteImage());

            values.put(TableField.FIELD_RESERVED_DATA18, vo.getRemoteSource());
            values.put(TableField.FIELD_RESERVED_DATA19, vo.getInviteType());
            values.put(TableField.FIELD_RESERVED_DATA20, vo.getInviteSource());
            values.put(TableField.FIELD_RESERVED_DATA21, vo.getFee());
            values.put(TableField.FIELD_RESERVED_DATA22, vo.getFromAddress());
            values.put(TableField.FIELD_RESERVED_DATA23, vo.getToAddress());
            values.put(TableField.FIELD_RESERVED_DATA24, vo.getTxBlockNumber());
            values.put(TableField.FIELD_RESERVED_DATA25, vo.getShareUrl());
            values.put(TableField.FIELD_RESERVED_DATA26, vo.getNoticeType());


            values.put(TableField.FIELD_CHAT_DISMISSGROUP, vo.isDismissGroup() ? 1 : 0);
            values.put(TableField.FIELD_CHAT_KICKGROUP, vo.isKickGroup() ? 1 : 0);

            values.put(TableField.FIELD_CHAT_GROUP_IMAGE, vo.getGroupImage());
            values.put(TableField.FIELD_CHAT_OBJECT, vo.getMsgTypeInt());


        }else if(isSystem && vo.getType()==300)//trans message
        {
            values.put(TableField.FIELD_RESERVED_DATA9, vo.getMoney());
            values.put(TableField.FIELD_RESERVED_DATA19, vo.getInviteType());
            values.put(TableField.FIELD_RESERVED_DATA26, vo.getNoticeType());
        }
        String[] whereArgs = null;
        whereArgs = new String[]{String.valueOf(vo.getChatId())};
        String whereClause = TableField.FIELD_CHAT_ID + "=? and "
                + TableField.FIELD_CHAT_SYSTEM + "=1 and "
                + TableField.FIELD_CHAT_HIDDEN + "=0";

        if (!isSystem) {
            whereClause = TableField.FIELD_CHAT_ID + "=? and "
                    + TableField.FIELD_CHAT_SYSTEM + "=0 "/*and " + TableField.FIELD_RESERVED_DATA7
                    + (vo.isOffLineMsg() ? "=1" : "=0")*/;

        }
        db.update(TableField.TABLE_CHAT_EVENT,
                values,
                whereClause,
                whereArgs);
    }

    /**
     * Modify my information the sex of the friends has been useless
     *
     * @param uid
     * @param gender
     */
    @Deprecated
    public void updateChatEventMsgGender(String uid, String gender) {
        if (!TextUtils.isEmpty(gender) && !TextUtils.isEmpty(uid)) {
            ContentValues values = new ContentValues();
            values.put(TableField.FIELD_FRIEND_GENDER, gender);
            String whereClause = TableField.FIELD_CHAT_ID + "=? and "
                    + TableField.FIELD_CHAT_SYSTEM + "=0 ";

            db.update(TableField.TABLE_CHAT_EVENT,
                    values,
                    whereClause,
                    new String[]{uid});
        }

    }

    /**
     * Modify the recent chat last one
     *
     * @param msg2
     */
    public void updateChatEventContent(String chatId, ChatMsg msg2) {
        String sql = "select * from " + TableField.TABLE_CHAT
                + " where " + TableField.FIELD_CHAT_ID + "=? "/*and " + TableField.FIELD_RESERVED_DATA7 +
                (msg2.isOffLineMsg() ? "=1 " : "=0 ")*/
                + " order by " + TableField._ID + " desc "
                + " limit " + 0 + "," + 1;
        String sql1 = "select * from (" + sql + ") order by " + TableField._ID;
        Cursor cursor = db.rawQuery(sql1, new String[]{chatId});
        ChatMsg msg = null;

        if (cursor.moveToNext()) {
            msg = new ChatMsg();
            msg.setContent(cursor.getString(cursor.getColumnIndex(TableField.FIELD_CHAT_BODY)));
            msg.setMsgTime(cursor.getLong(cursor.getColumnIndex(TableField.FIELD_CHAT_MSGTIME)));
            msg.setType(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_CHAT_TYPE)));
            msg.setUserId(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_UID)));
            msg.setChatId(cursor.getString(cursor.getColumnIndex(TableField.FIELD_CHAT_ID)));

            msg.setLocalUrl(cursor.getString(cursor.getColumnIndex(TableField.FIELD_CHAT_LOCALURL)));
            msg.setShowTime(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_CHAT_SHOWTIME)) == 1 ? true : false);
            msg.setOffLineMsg(msg2.isOffLineMsg());
        }
        if (msg == null) {
            msg = msg2;
            msg.setContent("");
        }


        updateChatEventMsg(msg, false, false);


        cursor.close();
    }

    /**
     * Modify the recent chat messages
     *
     * @param type
     */
    public void updateChatEventAgree(String uid, boolean agree, int type) {
        ContentValues values = new ContentValues();
        values.put(TableField.FIELD_CHAT_AGREE, agree ? 1 : 0);
        db.update(TableField.TABLE_CHAT_EVENT, values,
                TableField.FIELD_FRIEND_UID + "=? and "
                        + TableField.FIELD_CHAT_SYSTEM + "=1 and "
                        + TableField.FIELD_CHAT_TYPE + "=?"
                , new String[]{uid, String.valueOf(type)});
    }

    /**
     * Modify the recent chat messages
     *
     * @param uid
     * @param mask
     */
    public void updateChatEventMask(String uid, boolean mask) {
        ContentValues values = new ContentValues();
        values.put(TableField.FIELD_RESERVED_DATA6, mask ? 1 : 0);
        db.update(TableField.TABLE_CHAT_EVENT, values,
                TableField.FIELD_CHAT_ID + "=?"
                , new String[]{uid});
        try {
            if (NextApplication.myInfo != null){
                MySharedPrefs.writeBoolean(NextApplication.mContext, MySharedPrefs.FILE_USER, MySharedPrefs.IS_MASK_MSG + NextApplication.myInfo.getLocalId() + "_" + uid, mask);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *Friends add information whether already exists (annotation error call please ask me or watch)
     * @param chatId
     * @return
     */
    public String hasFriendAddMsg(String chatId) {
        String sql = " select " + TableField.FIELD_RESERVED_DATA1 + " from " + TableField.TABLE_CHAT_EVENT +
                " where "
                + TableField.FIELD_CHAT_ID + "='system-0' and "
                + TableField.FIELD_CHAT_HIDDEN + "=1 and "
                + TableField.FIELD_CHAT_SYSTEM + "=1 and "
//                + TableField.FIELD_CHAT_AGREE + " is null and "
                + TableField.FIELD_FRIEND_UID + "=?";
        Cursor cursor = db.rawQuery(sql, new String[]{chatId});


        if (cursor.moveToNext()) {
            String messageid = cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA1));
            cursor.close();
            return messageid;
        } else {
            cursor.close();
            return null;
        }
    }


    /**
     * Delete my information
     * @param chatId
     * @param isAddFriend If you delete the add buddy to go
     */
    public void deleteChatEventByChatId(String chatId, boolean isAddFriend) {
        if (isAddFriend) {
            db.delete(TableField.TABLE_CHAT_EVENT,
                    TableField.FIELD_CHAT_ID + "='system-0' and "
                            + TableField.FIELD_CHAT_HIDDEN + "=1 and "
                            + TableField.FIELD_CHAT_SYSTEM + "=1 and "
                            + TableField.FIELD_FRIEND_UID + "=?", new String[]{chatId});
        } else {
            db.delete(TableField.TABLE_CHAT_EVENT, TableField.FIELD_CHAT_ID + "=?", new String[]{chatId});
        }
    }

    /**
     * Delete my information add buddy
     *
     * @param messageid
     */
    public void deleteChatEventAddContactByMessageId(String messageid) {

        db.delete(TableField.TABLE_CHAT_EVENT,
                TableField.FIELD_CHAT_ID + "='system-0' and "
                        + TableField.FIELD_CHAT_HIDDEN + "=1 and "
                        + TableField.FIELD_CHAT_SYSTEM + "=1 and "
                        + TableField.FIELD_RESERVED_DATA1 + "=?", new String[]{messageid});
        ChatMsg msg = getChatMsgAddContact();
        if (msg != null) {
            updateChatEventMsg(msg, true, false);
        } else {
            db.delete(TableField.TABLE_CHAT_EVENT,
                    TableField.FIELD_CHAT_ID + "='system-0' and "
                            + TableField.FIELD_CHAT_HIDDEN + "=0 and "
                            + TableField.FIELD_CHAT_SYSTEM + "=1 and "
                            + TableField.FIELD_RESERVED_DATA1 + "=?", new String[]{messageid});
        }
    }

    /**
     * Delete my trans list item
     *
     * @param messageid
     */
    public void deleteChatEventTransByMessageId(String messageid) {

        db.delete(TableField.TABLE_CHAT_EVENT,
                TableField.FIELD_CHAT_ID + "='system-5' and "
                        + TableField.FIELD_CHAT_HIDDEN + "=1 and "
                        + TableField.FIELD_CHAT_SYSTEM + "=1 and "
                        + TableField.FIELD_RESERVED_DATA1 + "=?", new String[]{messageid});
        ChatMsg msg = getChatMsgTrans();
        if (msg != null) {
            updateChatEventMsg(msg, true, false);
        } else {
            db.delete(TableField.TABLE_CHAT_EVENT,
                    TableField.FIELD_CHAT_ID + "='system-5' and "
                            + TableField.FIELD_CHAT_HIDDEN + "=0 and "
                            + TableField.FIELD_CHAT_SYSTEM + "=1 and "
                            + TableField.FIELD_RESERVED_DATA1 + "=?", new String[]{messageid});
        }
    }

    /**
     * Delete the chat messages
     *
     * @param chatId
     */
    public void deleteChatMsgByChatId(String chatId) {
        db.delete(TableField.TABLE_CHAT, TableField.FIELD_CHAT_ID + "=?", new String[]{chatId});
        deleteChatEventByChatId(chatId, false);
    }

    /**
     * Empty chat messages
     *
     * @param chatId
     */
    public void clearChatMsgByChatId(String chatId,ChatMsg msg) {
        db.delete(TableField.TABLE_CHAT, TableField.FIELD_CHAT_ID + "=?", new String[]{chatId});
        updateChatEventContent(chatId, msg);
        updateUnreadEventChat(chatId);
    }


    /**
     * Delete the chat messages with messageId delete information
     *
     * @param messageId
     */
    public void deleteChatMsgBymessageId(String messageId) {
        if (TextUtils.isEmpty(messageId)) {
            return;
        }
        db.delete(TableField.TABLE_CHAT, TableField.FIELD_RESERVED_DATA1 + "=?", new String[]{messageId});
    }

    /**
     * Update for a chat messages unread status with messageId updates
     *
     * @param messageId
     */
    public void updateChatMsgUrneadBymessageId(String messageId) {
        if (TextUtils.isEmpty(messageId)) {
            return;
        }
        String sql = "update " + TableField.TABLE_CHAT
                + " set " + TableField.FIELD_CHAT_UNREAD + "=0"
                + " where " + TableField.FIELD_RESERVED_DATA1 + "=? ";
        db.execSQL(sql, new String[]{messageId});
    }


    /**
     * Save the friend information
     *
     * @param vo
     */
    public void saveFriendUserBase(UserBaseVo vo) {
        if (vo == null || TextUtils.isEmpty(vo.getLocalId())) {
            return;
        }
        boolean result = hasFriend(vo.getLocalId());
        if (result) {
            updateFriendUserBase(vo);
        } else {
            insertFriendUserBase(vo);
        }
    }

    /**
     * Modify the chat information in the file transfer
     * @param loaclId user id
     */
    public void updateFriendNote(String loaclId,String note) {
        if (TextUtils.isEmpty(loaclId)) {
            return;
        }
        boolean result = hasFriend(loaclId);
        if (result) {
            ContentValues values = new ContentValues();
            values.put(TableField.FIELD_FRIEND_NOTE, note);
            db.update(TableField.TABLE_FRIEND,values,TableField.FIELD_FRIEND_UID + "=? ",new String[]{loaclId});
        }
    }



    /**
     * Save the friend information
     *
     * @param vo
     */
    private void insertFriendUserBase(UserBaseVo vo) {
        ContentValues values = new ContentValues();
        values.put(TableField.FIELD_FRIEND_AGE, vo.getAge());
        values.put(TableField.FIELD_FRIEND_DISTANCE, vo.getDistance());
        values.put(TableField.FIELD_FRIEND_GENDER, vo.getGender());
        values.put(TableField.FIELD_FRIEND_LOGINTIME, vo.getLogintime());
        values.put(TableField.FIELD_FRIEND_NOTE, vo.getNote());
        values.put(TableField.FIELD_FRIEND_PIC, vo.getPic());
        values.put(TableField.FIELD_FRIEND_SIGHTML, vo.getSightml());
        values.put(TableField.FIELD_FRIEND_THUMB, vo.getThumb());
        values.put(TableField.FIELD_FRIEND_UID, vo.getLocalId());
        values.put(TableField.FIELD_FRIEND_UNAME, vo.getUserName());
        values.put(TableField.FIELD_RESERVED_DATA3, vo.isOffLine()?1:0);
        values.put(TableField.FIELD_RESERVED_DATA7, vo.getAddress());
        db.insert(TableField.TABLE_FRIEND, TableField._ID, values);
    }
    /**
     * Update the friend information
     *
     * @param vo
     */
    public void updateFriendUserBase(UserBaseVo vo) {
        if (vo == null || TextUtils.isEmpty(vo.getLocalId())) {
            return;
        }
        ContentValues values = new ContentValues();
        values.put(TableField.FIELD_FRIEND_AGE, vo.getAge());
        values.put(TableField.FIELD_FRIEND_DISTANCE, vo.getDistance());
        values.put(TableField.FIELD_FRIEND_GENDER, vo.getGender());
        values.put(TableField.FIELD_FRIEND_LOGINTIME, vo.getLogintime());
        values.put(TableField.FIELD_FRIEND_NOTE, vo.getNote());
        values.put(TableField.FIELD_FRIEND_PIC, vo.getPic());
        values.put(TableField.FIELD_FRIEND_SIGHTML, vo.getSightml());
        values.put(TableField.FIELD_FRIEND_THUMB, vo.getThumb());
        values.put(TableField.FIELD_FRIEND_UID, vo.getLocalId());
        values.put(TableField.FIELD_FRIEND_UNAME, vo.getUserName());
        values.put(TableField.FIELD_RESERVED_DATA3, vo.isOffLine()?1:0);
        values.put(TableField.FIELD_RESERVED_DATA7, vo.getAddress());
        db.update(TableField.TABLE_FRIEND, values, TableField.FIELD_FRIEND_UID + "=? and " + TableField.FIELD_RESERVED_DATA3 + "=0", new String[]{vo.getLocalId()});
    }


    /**
     * Get all the friends
     *
     * @return
     */
    public List<UserBaseVo> getFriendUserBaseAll() {
        List<UserBaseVo> list = new ArrayList<UserBaseVo>();
        String sql = "select * from " + TableField.TABLE_FRIEND + " order by " + TableField.FIELD_FRIEND_UID;
        Cursor cursor = db.rawQuery(sql, null);
        UserBaseVo vo;
        while (cursor.moveToNext()) {
            vo = new UserBaseVo();
            vo.setAge(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_AGE)));
            vo.setDistance(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_DISTANCE)));
            vo.setGender(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_GENDER)));
            vo.setLogintime(cursor.getLong(cursor.getColumnIndex(TableField.FIELD_FRIEND_LOGINTIME)));
            vo.setNote(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_NOTE)));
            vo.setPic(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_PIC)));
            vo.setSightml(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_SIGHTML)));
            vo.setThumb(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_THUMB)));
            vo.setLocalId(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_UID)));
            vo.setUsername(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_UNAME)));
            vo.setOffLine(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA3))==1?true:false);
            vo.setAddress(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA7)));
            list.add(vo);
        }
        /*add your self*/
        UserBaseVo voMe = new UserBaseVo();
        voMe.setFriendLog(-1);
        voMe.setGender(NextApplication.myInfo.getGender());
        voMe.setPic(NextApplication.myInfo.getPic());
        voMe.setSightml(NextApplication.myInfo.getSightml());
        voMe.setThumb(NextApplication.myInfo.getThumb());
        voMe.setLocalId(NextApplication.myInfo.getLocalId());
        voMe.setUsername(NextApplication.myInfo.getShowName());
        list.add(voMe);
        cursor.close();
        return list;
    }

    /**
     * To obtain a single friend
     *
     * @return
     */
    public UserBaseVo getUserBaseVoByUid(String uid) {
        String sql = "select * from " + TableField.TABLE_FRIEND + " where " + TableField.FIELD_FRIEND_UID + "=? "
                + " order by " + TableField.FIELD_FRIEND_UID;
        Cursor cursor = db.rawQuery(sql, new String[]{uid});
        UserBaseVo vo = null;
        if (cursor.moveToNext()) {
            vo = new UserBaseVo();
            vo.setAge(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_AGE)));
            vo.setDistance(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_DISTANCE)));
            vo.setGender(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_GENDER)));
            vo.setLogintime(cursor.getLong(cursor.getColumnIndex(TableField.FIELD_FRIEND_LOGINTIME)));
            vo.setNote(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_NOTE)));
            vo.setPic(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_PIC)));
            vo.setSightml(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_SIGHTML)));
            vo.setThumb(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_THUMB)));
            vo.setLocalId(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_UID)));
            vo.setUsername(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_UNAME)));
            vo.setAddress(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA7)));
            vo.setOffLine(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA3))==1?true:false);;
        }
        cursor.close();
        return vo;
    }

    /**
     * Whether friends there
     *
     * @param uid
     */
    private boolean hasFriend(String uid) {
        String sql = "select " + TableField.FIELD_FRIEND_UID + "," + TableField.FIELD_RESERVED_DATA3
                + " from " + TableField.TABLE_FRIEND
                + " where "
                + TableField.FIELD_FRIEND_UID + "=?";
        Cursor cursor = db.rawQuery(sql, new String[]{uid});
        boolean result = cursor.moveToNext();

        cursor.close();

        return result;
    }

    /**
     * Get offline user information
     *
     * @param uid The user id
     */
    public WifiPeopleVO getOffLineInfoByUid(String uid) {
        if (TextUtils.isEmpty(uid)) {
            return null;
        }
        String sql = "select * from " + TableField.TABLE_FRIEND
                + " where " + TableField.FIELD_FRIEND_UID + "=? and "
                + TableField.FIELD_RESERVED_DATA3 + "=1";
        Cursor cursor = db.rawQuery(sql, new String[]{uid});
        WifiPeopleVO vo = null;
        if (cursor.moveToNext()) {
            vo = new WifiPeopleVO();
            vo.setAge(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_AGE)));
            vo.setDistance(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_DISTANCE)));
            vo.setGender(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_GENDER)));
            vo.setLogintime(cursor.getLong(cursor.getColumnIndex(TableField.FIELD_FRIEND_LOGINTIME)));
            vo.setNote(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_NOTE)));
            vo.setPic(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_PIC)));
            vo.setSightml(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_SIGHTML)));
            vo.setThumb(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_THUMB)));
            vo.setLocalId(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_UID)));
            vo.setUsername(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_UNAME)));
            vo.setOffLine(true);
            vo.setMeetNum(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA4)));
            vo.setAddress(cursor.getString(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA7)));
            UserBaseVo baseVo = getUserBaseVoByUid(uid);
            if (baseVo != null) {
                vo.setFriendLog(1);
            }
        }
        cursor.close();
        return vo;

    }


    /**
     * Remove buddy
     *
     * @param uid
     */
    public void deleteFriendByUid(String uid) {
        db.delete(TableField.TABLE_FRIEND, TableField.FIELD_FRIEND_UID + "=?", new String[]{uid});
    }

    public void clearFriends(List<UserBaseVo> mFriendInfoList) {

        String sql = "select * from " + TableField.TABLE_FRIEND + " order by " + TableField.FIELD_FRIEND_UID;
        Cursor cursor = db.rawQuery(sql, null);
        UserBaseVo vo;
        while (cursor.moveToNext()) {
            vo = new UserBaseVo();
            vo.setLocalId(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_UID)));
            vo.setOffLine(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_RESERVED_DATA3))==1?true:false);
            if(vo.isOffLine())
            {
                boolean hasSynced = false;
                for(int i=0;i<mFriendInfoList.size();i++)
                {
                    if(mFriendInfoList.get(i).getLocalId().equals(vo.getLocalId()))
                {
                    hasSynced = true;
                }
                }
                if(hasSynced)
                {
                    db.delete(TableField.TABLE_FRIEND, TableField.FIELD_FRIEND_UID + "=?", new String[]{vo.getLocalId()});
                }

            }
            else{
                db.delete(TableField.TABLE_FRIEND, TableField.FIELD_FRIEND_UID + "=?", new String[]{vo.getLocalId()});
            }
        }
        cursor.close();
//        db.delete(TableField.TABLE_FRIEND, null, null);
    }


    /**
     * save
     *
     * @ param vo
     * @ param updateAll whether to update all the data
     * @ return return value greater than 0 is the new data
     */
    public long savePhoneContact(PhoneContactVo vo, boolean updateAll) {
        long insert = 0;
        if (db != null) {
            if (hasPhoneContact(vo.getId())) {
                updatePhoneContact(vo, updateAll);
            } else {
                insert = insertPhoneContact(vo);
            }
        }
        return insert;
    }


    private boolean hasPhoneContact(String id) {

        String sql = "select * from " + TableField.TABLE_CONTACT
                + " where " + TableField.FIELD_CHAT_THIRDID + "=?";

        Cursor cursor = db.rawQuery(sql, new String[]{id});

        boolean result = cursor.moveToNext();

        cursor.close();

        return result;
    }

    public void updatePhoneContact(PhoneContactVo vo, boolean updateAll) {
        ContentValues values = new ContentValues();
        values.put(TableField.FIELD_FRIEND_UNAME, vo.getName());
        if (!TextUtils.isEmpty(vo.getNote())) {
            values.put(TableField.FIELD_FRIEND_NOTE, vo.getNote());
        }

        if (updateAll) {
            values.put(TableField.FIELD_FRIEND_UID, vo.getUid());
            values.put(TableField.FIELD_FRIEND_RELATION, vo.getRelation());
        }
        db.update(TableField.TABLE_CONTACT, values, TableField.FIELD_CHAT_THIRDID + "=?", new String[]{vo.getId()});
    }

    public void updatePhoneContact(PhoneContactVo vo) {
        ContentValues values = new ContentValues();
        values.put(TableField.FIELD_FRIEND_UID, vo.getUid());
        values.put(TableField.FIELD_FRIEND_RELATION, vo.getRelation());
        db.update(TableField.TABLE_CONTACT, values, TableField.FIELD_CHAT_THIRDID + "=?", new String[]{vo.getId()});
    }

    public void deletePhoneContact(String friendId) {
        db.delete(TableField.TABLE_CONTACT, TableField.FIELD_CHAT_THIRDID + "=?", new String[]{friendId});
    }

    public long insertPhoneContact(PhoneContactVo vo) {
        ContentValues values = new ContentValues();
        values.put(TableField.FIELD_CHAT_THIRDID, vo.getId());
        values.put(TableField.FIELD_FRIEND_UNAME, vo.getName());
        values.put(TableField.FIELD_FRIEND_NOTE, vo.getNote());
        values.put(TableField.FIELD_CHAT_TYPE, vo.getType());
        values.put(TableField.FIELD_FRIEND_RELATION, vo.getRelation());
        values.put(TableField.FIELD_FRIEND_UID, vo.getUid());
        return db.insert(TableField.TABLE_CONTACT, TableField._ID, values);
    }

    /**
     * Get the address book information
     *
     * @param type 1. Mobile phone address book, 2. Sina weibo friends, 3. Tencent weibo friends
     * @return
     */
    public List<PhoneContactVo> getPhoneContactList(int type) {

        List<PhoneContactVo> mList = new ArrayList<PhoneContactVo>();
        if (db == null) {
            return mList;
        }
        String sql = "select * from " + TableField.TABLE_CONTACT
                + " where " + TableField.FIELD_CHAT_TYPE + "=? " + " order by " + TableField.FIELD_FRIEND_UNAME;

        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(type)});
        PhoneContactVo vo;
        while (cursor.moveToNext()) {
            vo = new PhoneContactVo();
            vo.setId(cursor.getString(cursor.getColumnIndex(TableField.FIELD_CHAT_THIRDID)));
            vo.setName(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_UNAME)));
            vo.setNote(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_NOTE)));
            vo.setRelation(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_FRIEND_RELATION)));
            vo.setUid(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_FRIEND_UID)));
            vo.setType(type);
            mList.add(vo);
        }

        cursor.close();

        return mList;
    }

    /**
     * get local save three friends information
     *
     * @ param type 1. The mobile phone address book, 2. Sina weibo friends, 3. Tencent weibo friends
     * @ return
     */
    public List<PhoneContactGroupVo> getPhoneContactGroup(int type) {
        String sql = "select * from " + TableField.TABLE_CONTACT
                + " where " + TableField.FIELD_CHAT_TYPE + "=? "
                + " order by " + TableField.FIELD_FRIEND_RELATION + "," + TableField.FIELD_FRIEND_UNAME;
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(type)});
        List<PhoneContactGroupVo> mGList = new ArrayList<PhoneContactGroupVo>();
        PhoneContactGroupVo gVo = null;
        List<PhoneContactVo> mCList = new ArrayList<PhoneContactVo>();
        PhoneContactVo vo = null;
        int relation = 0;

        while (cursor.moveToNext()) {
            vo = new PhoneContactVo();
            vo.setId(cursor.getString(cursor.getColumnIndex(TableField.FIELD_CHAT_THIRDID)));
            vo.setName(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_UNAME)));
            vo.setNote(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_NOTE)));
            vo.setRelation(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_FRIEND_RELATION)));
            vo.setUid(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_FRIEND_UID)));
            vo.setType(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_CHAT_TYPE)));
            if (vo.getRelation() != relation) {//新的组
                gVo = new PhoneContactGroupVo();
                gVo.setContactList(mCList);
                gVo.setType(relation);
                mGList.add(gVo);
                relation = vo.getRelation();
                mCList = new ArrayList<>();
                mCList.add(vo);
            } else {
                mCList.add(vo);
            }
        }
        cursor.close();
        gVo = new PhoneContactGroupVo();
        gVo.setContactList(mCList);
        if (vo != null) {
            gVo.setType(vo.getRelation());
        }
        mGList.add(gVo);
        return mGList;
    }

    /**
     * for a single recommendation
     *Unique identifier * @ param id friends phone number, openI
     * @param type
     * @return
     */
    public PhoneContactVo getPhoneContactById(String id, int type) {

        String sql = "select * from " + TableField.TABLE_CONTACT
                + " where "
                + TableField.FIELD_CHAT_THIRDID + "=? and "
                + TableField.FIELD_CHAT_TYPE + "=?";

        Cursor cursor = db.rawQuery(sql, new String[]{id, String.valueOf(type)});
        PhoneContactVo vo = null;
        if (cursor.moveToNext()) {
            vo = new PhoneContactVo();
            vo.setId(cursor.getString(cursor.getColumnIndex(TableField.FIELD_CHAT_THIRDID)));
            vo.setName(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_UNAME)));
            vo.setNote(cursor.getString(cursor.getColumnIndex(TableField.FIELD_FRIEND_NOTE)));
            vo.setRelation(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_FRIEND_RELATION)));
            vo.setUid(cursor.getInt(cursor.getColumnIndex(TableField.FIELD_FRIEND_UID)));
            vo.setType(type);
        }
        cursor.close();
        return vo;
    }



    /**
     * Database transaction processing
     */

    public void beginTransaction() {
        if (db != null) { // add by : KNothing
            db.beginTransaction();
        }
    }

    public void endTransactionSuccessful() {
        if (db != null) { // add by : KNothing 
            db.setTransactionSuccessful();
            db.endTransaction();
        }
    }

    public void setTransactionSuccessful() {
        if (db != null) { // add by : KNothing
            db.setTransactionSuccessful();
        }
    }

    public void endTransaction() {
        if (db != null) { // add by : KNothing
            db.endTransaction();
        }
    }


    /**
     * Close the database
     */
    public void close() {
        try {
            if (db != null)
                db.close();
            if (helper != null) {
                helper.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            instance = null;
        }
    }
}
