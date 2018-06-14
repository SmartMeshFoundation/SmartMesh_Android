package com.lingtuan.firefly.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.offline.AppNetService;
import com.lingtuan.firefly.ui.MainFragmentUI;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.vo.ChatMsg;
import com.lingtuan.firefly.contact.vo.FriendRecommentVo;
import com.lingtuan.firefly.contact.vo.GroupMemberAvatarVo;
import com.lingtuan.firefly.contact.vo.PhoneContactVo;
import com.lingtuan.firefly.wallet.util.WalletStorage;
import com.lingtuan.firefly.wallet.vo.StorableWallet;
import com.lingtuan.firefly.wallet.vo.TokenVo;
import com.lingtuan.firefly.wallet.vo.TransVo;
import com.lingtuan.firefly.xmpp.LoginThread;
import com.lingtuan.firefly.xmpp.XmppAction;
import com.lingtuan.firefly.xmpp.XmppHandler;
import com.lingtuan.firefly.xmpp.XmppUtils;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.MsgType;
import org.jivesoftware.smack.packet.Message.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

/**
 * Chat background services
 */
public class XmppService extends Service {

    private XmppHandler mHandler;

    private XmppUtils mXmppUtils;

    private NextPacketListener mPacketListener;

    private NotificationManager notificationManager;

    private WakeLock wakeLock;

    private boolean isContected = true;

    /*To prevent continuous offline push message, voice repeated problems*/
    private Timer mTimer;
    private TimerTask mTimerTask;
    private boolean cantSend = false;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private int offLinetype = 0;

    private int livingtype = 0;

//    private int totalUnreadCount = 0;

    private boolean cantAddUnreadCount;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                Timer timer = new Timer();
                timer.schedule(new QunXTask(getApplicationContext()),
                        new Date());
            }
        }
    };

    @Override
    public void onCreate() {

        super.onCreate();
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION); // Add receive network connection state changes of the Action
        registerReceiver(mReceiver, mFilter);

        mHandler = new XmppHandler(this);
        mPacketListener = new NextPacketListener();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mTimer = new Timer();
        acquireWakeLock();
    }


    class QunXTask extends TimerTask {
        private Context context;

        public QunXTask(Context context) {
            this.context = context;
        }

        @Override
        public void run() {
            try {
                isContected = isConnectNet();
                if (isContected) {
                    if (context != null) {
                        XmppUtils.loginXmppForNextApp(context);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    XmppUtils.loginXmppForNextApp(context);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    /**
     * Determine whether connected network
     */
    private boolean isConnectNet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo Mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo Wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return State.CONNECTED.equals(Mobile.getState()) || State.CONNECTED.equals(Wifi.getState());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver); // Delete the radio
        releaseWakeLock();
    }

    /**
     * Power lock
     */
    private void acquireWakeLock() {
        if (wakeLock == null) {
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getCanonicalName());
            wakeLock.acquire();
        }
    }

    /**
     * Release the power lock
     * */
    private void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            wakeLock = null;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (XmppAction.ACTION_LOGIN.equals(intent.getAction())) {//The login
                if (intent.getExtras() != null) {
                    Bundle mBundle = intent.getExtras().getBundle(XmppAction.ACTION_LOGIN);
                    String[] uNamePwd = mBundle.getStringArray(XmppAction.ACTION_LOGIN);
                    String username = uNamePwd[0];
                    String password = uNamePwd[1];
                    if (!XmppUtils.isLogining) {
                        new LoginThread(mHandler, username, password).start();
                    }
                }
            } else if (XmppAction.ACTION_LOGIN_MESSAGE_LISTENER.equals(intent.getAction())) {
                try {
                    mXmppUtils = XmppUtils.getInstance();
                    if (mPacketListener != null) {
                        mXmppUtils.getConnection().removePacketListener(mPacketListener);
                    }
                    mXmppUtils.sendOnLine();
                    mPacketListener = new NextPacketListener();
                    mXmppUtils.getConnection().addPacketListener(mPacketListener, new PacketFilter() {
                        @Override
                        public boolean accept(Packet packet) {
                            return true;
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    class NextPacketListener implements PacketListener {
        @Override
        public void processPacket(Packet packet) {
                if (packet instanceof Message) {
                    final Message msg = (Message) packet;
                    ChatMsg chatmsg = null;
                    try {
                        ackMsg(msg);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    if(msg.getFrom().contains(Constants.APP_EVERYONE))
                    {
                        msg.setMsgtype(MsgType.normalchat);
                    }

                    if (msg.getType() == Type.groupchat) {
                        //Everyone chat room
                        synchronized (NextApplication.lock) {
                            try {
                                boolean isMsgExit = FinalUserDataBase.getInstance()
                                        .checkMsgExist(msg.getPacketID(), msg.getMsgtype(), parseBody(msg).getType(), "", "");
                                if (isMsgExit) {
                                    return;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            chatmsg = parseRoomChat(msg);
                        }
                    }
                    else if (msg.getMsgtype() == Message.MsgType.normalchat) {
                        //Normal chat
                        try {
                            boolean isMsgExit  = FinalUserDataBase.getInstance()
                                    .checkMsgExist(msg.getPacketID(),msg.getMsgtype(),parseBody(msg).getType(),"","");
                            if (isMsgExit) {
                                return;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        chatmsg = parseNormalChat(msg, false);
                    } else if (msg.getMsgtype() == Message.MsgType.system) {
                        //System information
                        try {
                            ChatMsg chatmsgT = parseBody(msg);
                            String number = chatmsgT.getNumber();
                            String noticetype = String.valueOf(chatmsgT.getNoticeType());
                            if (chatmsgT.getType() == 300 && !checkAddressIsExist(chatmsgT,noticetype)){
                                return;
                            }
                            boolean isMsgExit = FinalUserDataBase.getInstance()
                                    .checkMsgExist(msg.getPacketID(),msg.getMsgtype(),parseBody(msg).getType(),number,noticetype);
                            if (isMsgExit) {
                                return;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        chatmsg = parseSystem(msg);
                        if (chatmsg != null && (chatmsg.getType() == 19 || chatmsg.getType() == 20)) {
                            return;
                        }

                    } else if (msg.getMsgtype() == Message.MsgType.groupchat) {
                        //Group chat
                        try {
                            boolean isMsgExit = FinalUserDataBase.getInstance()
                                    .checkMsgExist(msg.getPacketID(),msg.getMsgtype(),parseBody(msg).getType(),"","");
                            if (isMsgExit) {
                                return;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        chatmsg = parseGroupChat(msg, false);
                    }
                    else {//Unknown chat type to abandon the if (MSG) getMsgtype () = = Message. The MsgType. Unkonw)
                        return;
                    }
                    if (MsgType.msgStatus.equals(msg.getMsgtype())) {
                        return;
                    }
                    Bundle bundle = new Bundle();
                    if (chatmsg != null && chatmsg.getType() == 10000)//Offline message set
                    {
                        return;
                    } else if (chatmsg != null) {
                        bundle.putSerializable("chat", chatmsg);
                    } else {
                        if (offLinetype == -1) {
                            offLinetype = 0;
                            notifFunction(msg, chatmsg);
                        } else if (livingtype == 130) {
                            livingtype = 0;
                            notifFunction(msg, chatmsg);
                        }
                        return;
                    }
                    if(cantAddUnreadCount){//Add buddy messages and orders need to deal with the unique, if delete the duplicate messages, the total number of the unread did not increase
                        cantAddUnreadCount = false;
                        notifFunction(msg, chatmsg);
                        return;
                    }
                    Utils.intentAction(getApplicationContext(), XmppAction.ACTION_MESSAGE_EVENT_LISTENER, bundle);
                    notifFunction(msg, chatmsg);
                }
                else if (packet instanceof Presence) {
                    Presence presence = (Presence)packet;
                    if(presence.getType() == Presence.Type.available && presence.getFrom().contains(Constants.APP_EVERYONE))
                    {
                        Intent intent = new Intent(XmppAction.ACTION_ENTER_EVERYONE_LISTENER);
                        Utils.intentAction(getApplicationContext(), intent);
                    }
                }
            }

    }


    /**
     * check wallet address is exist
     * @param chatmsgT     chatmsg
     * @param noticetype   0 sender 1 receiver
     * @return  isAddressExist   true or false
     * */
    private boolean checkAddressIsExist(ChatMsg chatmsgT,String noticetype){
        int tempType = chatmsgT.getType();
        String fromAddress = chatmsgT.getFromAddress();
        String toAddress = chatmsgT.getToAddress();
        boolean isAddressExist = false;
        if (tempType == 300){
            int length = WalletStorage.getInstance(getApplicationContext()).get().size();
            for (int i = 0 ; i < length; i++){
                StorableWallet storableWallet = WalletStorage.getInstance(getApplicationContext()).get().get(i);
                String address = "";
                if(!TextUtils.isEmpty(storableWallet.getPublicKey())) {
                    address = storableWallet.getPublicKey();
                    if (!address.startsWith("0x")){
                        address= "0x"+ address;
                    }
                }
                if ((TextUtils.equals("0",noticetype) && TextUtils.equals(fromAddress,address)) || (TextUtils.equals("1",noticetype) && TextUtils.equals(toAddress,address))){
                    isAddressExist = true;
                    break;
                }
            }
        }
        return isAddressExist;
    }

    /**
     * Notification bar notification
     */
    private void notifFunction(final Message msg, final ChatMsg chatMsg) {
        boolean sendNotify = MySharedPrefs.readBoolean(XmppService.this, MySharedPrefs.FILE_USER, MySharedPrefs.RECEIVE_NEW_MSG + NextApplication.myInfo.getLocalId());
        if (sendNotify){//2000 milliseconds delay send push, if less than 200 milliseconds to receive news only push last message
            try {
                if (!cantSend) {
                    sendNotify(msg, chatMsg);
                    cantSend = true;
                }
                if (mTimer != null) {
                    if (mTimerTask != null) {
                        mTimerTask.cancel();
                        mTimerTask = null;
                    }
                } else {
                    mTimer = new Timer();
                }
                mTimerTask = new TimerTask() {
                    @Override
                    public void run() {
                        cantSend = false;
                    }
                };
                mTimer.schedule(mTimerTask, 2000);//No longer push message is received within 2000 ms
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Offline message set processing. Due to too much data offline message one-time, avoid blocking the main thread, so on the thread inside with the transaction
     */
    class HandleOfflineMsgList implements Runnable {
        ChatMsg msgs;
        MsgType type;
        HandleOfflineMsgList(ChatMsg msgs, MsgType type) {
            this.msgs = msgs;
            this.type = type;
        }
        @Override
        public void run() {
            parseOfflineMsgList(msgs, type);
        }
    }

    private synchronized void parseOfflineMsgList(ChatMsg msgs, MsgType type) {
        ArrayList<ChatMsg> offlineList = new ArrayList<>();
        if (msgs != null && msgs.getOfflinmsgList() != null) {
            JSONArray offlinemsgList = new JSONArray();//Offline message set
            for (int index = 0; index < msgs.getOfflinmsgList().length(); index++){//This step is to throw out temporarily unable to parse messages and repeat
                try {
                    JSONObject obj = (JSONObject) msgs.getOfflinmsgList().get(index);
                    int msgtype = obj.optInt("type");
                    boolean isMsgExit = FinalUserDataBase.getInstance().checkMsgExist(obj.optString("msgid"), type, msgtype,"","");
                    if (isMsgExit) {//To repeat
                        continue;
                    }
                    if (type == MsgType.super_groupchat) {
                        msgtype = msgtype - 1000;
                    }
                    if(msgtype == 21){//Group broadcast messages don't do offline message processing, direct abandoned
                        continue;
                    }
                    offlinemsgList.put(obj);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            FinalUserDataBase.getInstance().beginTransaction();        //Manually start the transaction
            int totalCount = 0;
            for (int index = 0; index < offlinemsgList.length(); index++) {
                JSONObject object = offlinemsgList.optJSONObject(index);
                if (type == MsgType.everyone){//normalChat
                    try {
                        String content = null;
                        ChatMsg chatmsg = parseOfflineBody(msgs, object, index == offlinemsgList.length() - 1 ? offlinemsgList.length() : 0, type);
                        chatmsg.setMsgType(msgs.getMsgType());
                        if (!checkChatType(chatmsg.getType())) {
                            chatmsg.setType(50000);
                        }
                        switch (chatmsg.getType()) {
                            case 13://Share the news
                                chatmsg.setType(103);
                                content = chatmsg.getContent();
                                break;
                            case 50000://Not compatible with high version of the news
                                chatmsg.setContent(chatmsg.getMsgName());
                                break;
                        }
                        FinalUserDataBase.getInstance().saveChatMsgNew(chatmsg, chatmsg.getUserId(), chatmsg.getUsername(), chatmsg.getUserImage(), index == offlinemsgList.length() - 1);

                        if (chatmsg.getType() == 103 && !TextUtils.isEmpty(content)) {
                            chatmsg.setContent(content);
                        }
                        offlineList.add(chatmsg);
                        if (index == offlinemsgList.length() - 1){//Collection of traverse after send to unity
                            Intent intent = new Intent(XmppAction.ACTION_OFFLINE_MESSAGE_LIST_LISTENER);
                            intent.putExtra("array", offlineList);
                            Utils.intentAction(getApplicationContext(), intent);
                        }
                        if (chatmsg.getType() == 2) {//Download the audio files

                            Bundle downloadBundle = new Bundle();
                            downloadBundle.putString("uid", chatmsg.getChatId());
                            downloadBundle.putString("username", chatmsg.getUsername());
                            downloadBundle.putString("avatarurl", chatmsg.getUserImage());
                            downloadBundle.putSerializable("chatmsg", chatmsg);
                            Utils.intentService(
                                    getApplicationContext(),
                                    LoadDataService.class,
                                    LoadDataService.ACTION_FILE_DOWNLOAD,
                                    LoadDataService.ACTION_FILE_DOWNLOAD,
                                    downloadBundle);

                        }
                        if (chatmsg.getType() == 103 && !TextUtils.isEmpty(content)) {
                            chatmsg.setContent(chatmsg.getShareTitle());
                        }
                        totalCount++;
                        if (index == offlinemsgList.length() - 1){//Collection of traverse after send to unity
                            Intent intent = new Intent(XmppAction.ACTION_OFFLINE_MESSAGE_LIST_EVENT_LISTENER);
                            intent.putExtra("chat", chatmsg);
                            intent.putExtra("totalCount", totalCount);
                            Utils.intentAction(getApplicationContext(), intent);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else if (type == MsgType.normalchat){//normalChat
                    try {
                        String content = null;
                        ChatMsg chatmsg = parseOfflineBody(msgs, object, index == offlinemsgList.length() - 1 ? offlinemsgList.length() : 0, type);
                        chatmsg.setMsgType(msgs.getMsgType());
                        if (!checkChatType(chatmsg.getType())) {
                            chatmsg.setType(50000);
                        }
                        switch (chatmsg.getType()) {
                            case 13://Share the news
                                chatmsg.setType(103);
                                content = chatmsg.getContent();
                                break;
                            case 19://file
                                chatmsg.setInviteType(4);
                                chatmsg.setType(1009);
                                break;
                            case 50000://Not compatible with high version of the news
                                chatmsg.setContent(chatmsg.getMsgName());
                                break;
                        }

                        FinalUserDataBase.getInstance().saveChatMsgNew(chatmsg, chatmsg.getUserId(), chatmsg.getUsername(), chatmsg.getUserImage(), index == offlinemsgList.length() - 1);


                        if (chatmsg.getType() == 103 && !TextUtils.isEmpty(content)) {
                            chatmsg.setContent(content);
                        }

                        offlineList.add(chatmsg);

                        if (index == offlinemsgList.length() - 1){//Collection of traverse after send to unity
                            Intent intent = new Intent(XmppAction.ACTION_OFFLINE_MESSAGE_LIST_LISTENER);
                            intent.putExtra("array", offlineList);
                            Utils.intentAction(getApplicationContext(), intent);
                        }
                        if (chatmsg.getType() == 2) {//Download the audio files

                            Bundle downloadBundle = new Bundle();
                            downloadBundle.putString("uid", chatmsg.getChatId());
                            downloadBundle.putString("username", chatmsg.getUsername());
                            downloadBundle.putString("avatarurl", chatmsg.getUserImage());
                            downloadBundle.putSerializable("chatmsg", chatmsg);
                            Utils.intentService(
                                    getApplicationContext(),
                                    LoadDataService.class,
                                    LoadDataService.ACTION_FILE_DOWNLOAD,
                                    LoadDataService.ACTION_FILE_DOWNLOAD,
                                    downloadBundle);

                        }
                        if (chatmsg.getType() == 103 && !TextUtils.isEmpty(content)) {
                            chatmsg.setContent(chatmsg.getShareTitle());
                        }

                        if (chatmsg.getGroupMask() && !TextUtils.equals("system-0", chatmsg.getChatId()) && !TextUtils.equals("system-1", chatmsg.getChatId()) && !TextUtils.equals("system-3", chatmsg.getChatId()) && !TextUtils.equals("system-4", chatmsg.getChatId())  && !TextUtils.equals("system-5", chatmsg.getChatId())) {//屏蔽的信息不与管理

                        } else {
                            totalCount++;
                        }
                        if (index == offlinemsgList.length() - 1){//Collection of traverse after send to unity
                            Intent intent = new Intent(XmppAction.ACTION_OFFLINE_MESSAGE_LIST_EVENT_LISTENER);
                            intent.putExtra("chat", chatmsg);
                            intent.putExtra("totalCount", totalCount);
                            Utils.intentAction(getApplicationContext(), intent);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (type == MsgType.groupchat){//groupchat
                    try {
                        String content = null;
                        ChatMsg chatmsg = parseOfflineBody(msgs, object, index == offlinemsgList.length() - 1 ? offlinemsgList.length() : 0, type);
                        chatmsg.setMsgType(msgs.getMsgType());
                        String[] uids = chatmsg.getChatId().split("@");
                        chatmsg.setChatId("group-" + chatmsg.getGroupId());
                        if (uids.length > 1)//Not from news about your users
                        {
                            chatmsg.setChatId(chatmsg.getChatId() + "@" + uids[1]);
                        }
                        chatmsg.setGroup(true);
                        if (!checkChatType(chatmsg.getType())) {
                            chatmsg.setType(50000);
                        }
                        switch (chatmsg.getType()) {
                            case 13://Share the news
                                chatmsg.setType(103);
                                content = chatmsg.getContent();
                                break;
                            case 19://file
                                chatmsg.setInviteType(4);
                                chatmsg.setType(1009);
                                break;
                            case 50000://Not compatible with high version of the news
                                chatmsg.setContent(chatmsg.getMsgName());
                                break;
                        }

                        StringBuilder url = new StringBuilder();
                        if (chatmsg.getMemberAvatarList() != null) {
                            for (GroupMemberAvatarVo vo : chatmsg.getMemberAvatarList()) {
                                url.append(vo.getImage()).append("___").append(vo.getGender()).append("___").append(vo.getUsername()).append("#");
                            }
                            url.deleteCharAt(url.lastIndexOf("#"));
                        }
                        final String avatarUrl = chatmsg.getUserImage();
                        String uName = chatmsg.getUsername();
                        FinalUserDataBase.getInstance().saveChatMsgNew(chatmsg, chatmsg.getChatId(), chatmsg.getGroupName(), url.toString(), index == offlinemsgList.length() - 1 ? true : false);
                        chatmsg.setUsername(uName);
                        chatmsg.setRealname(uName);
                        chatmsg.setUserImage(avatarUrl);
                        if (chatmsg.getType() == 103 && !TextUtils.isEmpty(content)) {
                            chatmsg.setContent(content);
                        }
                        offlineList.add(chatmsg);
                        if (index == offlinemsgList.length() - 1)//Collection of traverse after send to unity
                        {
                            Intent intent = new Intent(XmppAction.ACTION_OFFLINE_MESSAGE_LIST_LISTENER);
                            intent.putExtra("array", offlineList);
                            Utils.intentAction(getApplicationContext(), intent);
                        }
                        if (chatmsg.getType() == 2) {//Download the audio files
                            Bundle downloadBundle = new Bundle();
                            downloadBundle.putString("uid", chatmsg.getChatId());
                            downloadBundle.putString("username", chatmsg.getUsername());
                            downloadBundle.putString("avatarurl", chatmsg.getUserImage());
                            downloadBundle.putSerializable("chatmsg", chatmsg);
                            Utils.intentService(
                                    getApplicationContext(),
                                    LoadDataService.class,
                                    LoadDataService.ACTION_FILE_DOWNLOAD,
                                    LoadDataService.ACTION_FILE_DOWNLOAD,
                                    downloadBundle);
                        }
                        ChatMsg chatMsgT = new ChatMsg();
                        chatMsgT.parseChatMsgVo(chatmsg);
                        chatMsgT.setUserImage(url.toString());
                        if (chatmsg.getType() == 103 && !TextUtils.isEmpty(content)) {
                            chatmsg.setContent(chatmsg.getShareTitle());
                            chatMsgT.setContent(chatMsgT.getShareTitle());
                        }

                        if (chatmsg.getGroupMask() && !TextUtils.equals("system-0", chatmsg.getChatId()) && !TextUtils.equals("system-1", chatmsg.getChatId()) && !TextUtils.equals("system-3", chatmsg.getChatId()) && !TextUtils.equals("system-4", chatmsg.getChatId()) && !TextUtils.equals("system-5", chatmsg.getChatId())) {//屏蔽的信息不与管理

                        } else {
                            totalCount++;
                        }
                        if (index == offlinemsgList.length() - 1)//Collection of traverse after send to unity
                        {
                            Intent intent = new Intent(XmppAction.ACTION_OFFLINE_MESSAGE_LIST_EVENT_LISTENER);
                            intent.putExtra("chat", chatMsgT);
                            intent.putExtra("totalCount", totalCount);
                            Utils.intentAction(getApplicationContext(), intent);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (type == MsgType.super_groupchat){//supergroupvchat
                    try {
                        String content = null;
                        ChatMsg chatmsg = parseOfflineBody(msgs, object, index == offlinemsgList.length() - 1 ? offlinemsgList.length() : 0, type);
                        chatmsg.setMsgType(msgs.getMsgType());
                        chatmsg.setType(chatmsg.getType() - 1000);//Group on the basis of information type as the original+1000
                        String[] uids = chatmsg.getChatId().split("@");
                        chatmsg.setChatId("superGroup-" + chatmsg.getGroupId());
                        if (uids.length > 1)
                        {
                            chatmsg.setChatId(chatmsg.getChatId() + "@" + uids[1]);
                        }
                        chatmsg.setGroup(true);
                        if (!checkChatType(chatmsg.getType())) {
                            chatmsg.setType(50000);
                        }
                        switch (chatmsg.getType()) {
                            case 13://Share the news
                                if(!TextUtils.isEmpty(chatmsg.getUsersource()) && !chatmsg.getUsersource().equals(XmppUtils.SERVER_NAME) && !TextUtils.isEmpty(chatmsg.getShareUrl()) &&  (chatmsg.getShareUrl().contains("/vodInfo.html") || (chatmsg.getShareUrl().contains("/share_vod.html"))) && !chatmsg.getShareUrl().contains(Constants.APP_URL_FLAG))
                                {
                                    chatmsg.setType(50002);
                                }
                                else{
                                    chatmsg.setType(103);
                                    content = chatmsg.getContent();
                                }
                                break;
                            case 19://file
                                chatmsg.setInviteType(4);
                                chatmsg.setType(1009);
                                break;
                            case 50000://Not compatible with high version of the news
                                chatmsg.setContent(chatmsg.getMsgName());
                                break;
                        }

                        final String avatarUrl = chatmsg.getUserImage();
                        String uName = chatmsg.getUsername();

                        FinalUserDataBase.getInstance().saveChatMsgNew(chatmsg, chatmsg.getChatId(), chatmsg.getGroupName(), chatmsg.getGroupImage(), index == offlinemsgList.length() - 1 ? true : false);

                        chatmsg.setUsername(uName);
                        chatmsg.setUserImage(avatarUrl);
                        if (chatmsg.getType() == 103 && !TextUtils.isEmpty(content)) {
                            chatmsg.setContent(content);
                        }

                        offlineList.add(chatmsg);


                        if (index == offlinemsgList.length() - 1)//Collection of traverse after send to unity
                        {
                            Intent intent = new Intent(XmppAction.ACTION_OFFLINE_MESSAGE_LIST_LISTENER);
                            intent.putExtra("array", offlineList);
                            Utils.intentAction(getApplicationContext(), intent);
                        }
                        if (chatmsg.getType() == 2) {//Download the audio files

                            Bundle downloadBundle = new Bundle();
                            downloadBundle.putString("uid", chatmsg.getChatId());
                            downloadBundle.putString("username", chatmsg.getUsername());
                            downloadBundle.putString("avatarurl", chatmsg.getUserImage());
                            downloadBundle.putSerializable("chatmsg", chatmsg);
                            Utils.intentService(
                                    getApplicationContext(),
                                    LoadDataService.class,
                                    LoadDataService.ACTION_FILE_DOWNLOAD,
                                    LoadDataService.ACTION_FILE_DOWNLOAD,
                                    downloadBundle);
                        }
                        if (chatmsg.getType() == 103 && !TextUtils.isEmpty(content)) {
                            chatmsg.setContent(chatmsg.getShareTitle());
                        }

                        if (chatmsg.getGroupMask() && !TextUtils.equals("system-0", chatmsg.getChatId()) && !TextUtils.equals("system-1", chatmsg.getChatId()) && !TextUtils.equals("system-3", chatmsg.getChatId()) && !TextUtils.equals("system-4", chatmsg.getChatId()) && !TextUtils.equals("system-5", chatmsg.getChatId())) {//屏蔽的信息不与管理

                        } else {
                            totalCount++;
                        }
                        if (index == offlinemsgList.length() - 1)//Collection of traverse after send to unity
                        {
                            Intent intent = new Intent(XmppAction.ACTION_OFFLINE_MESSAGE_LIST_EVENT_LISTENER);
                            intent.putExtra("chat", chatmsg);
                            intent.putExtra("totalCount", totalCount);
                            Utils.intentAction(getApplicationContext(), intent);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
            FinalUserDataBase.getInstance().endTransactionSuccessful();        //Processing is complete
        }
    }


    /**
     * Normal chat processing
     */
    private ChatMsg parseNormalChat(Message msg, boolean isSystem) {
        if (!TextUtils.isEmpty(msg.getBody())) {
            try {
                String content = null;
                ChatMsg chatmsg = parseBody(msg);
                chatmsg.setMsgType(msg.getMsgtype());
                if (!isSystem && !checkChatType(chatmsg.getType())) {    //Is not the system news, if not compatible message directly to ignore
                    chatmsg.setType(50000);
                }

                try {
                    MySharedPrefs.writeBoolean(NextApplication.mContext, MySharedPrefs.FILE_USER, MySharedPrefs.IS_MASK_MSG + NextApplication.myInfo.getLocalId() + "_" + chatmsg.getChatId(), chatmsg.getGroupMask());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (chatmsg.getType() == 10000){//Offline message set
                    new Thread(new HandleOfflineMsgList(chatmsg, MsgType.normalchat)).start();
                    return chatmsg;
                }
                switch (chatmsg.getType()) {
                    case 13://Share the news
                        chatmsg.setType(103);
                        content = chatmsg.getContent();
                        break;
                    case 19://file
                        chatmsg.setInviteType(4);
                        chatmsg.setType(1009);
                        break;
                    case 500://System prompt message, such as greeting strangers than 5 tips will return system
                        chatmsg.setType(12);
                        break;
                    case 50000://Not compatible with high version of the news
                        chatmsg.setContent(chatmsg.getMsgName());
                        break;
                }
                if (isSystem){//The system prompt
                    FinalUserDataBase.getInstance().saveChatMsg(chatmsg, chatmsg.getUserId(), chatmsg.getUsername(), chatmsg.getUserImage(), false);
                } else {
                    FinalUserDataBase.getInstance().saveChatMsg(chatmsg, chatmsg.getUserId(), chatmsg.getUsername(), chatmsg.getUserImage());
                }

                if (chatmsg.getType() == 103 && !TextUtils.isEmpty(content)) {
                    chatmsg.setContent(content);
                }
                Bundle bundle = new Bundle();
                bundle.putSerializable(XmppAction.ACTION_MESSAGE_LISTENER, chatmsg);
                Utils.intentAction(getApplicationContext(), XmppAction.ACTION_MESSAGE_LISTENER, bundle);

                if (chatmsg.getType() == 2) {//Download the audio files

                    Bundle downloadBundle = new Bundle();
                    downloadBundle.putString("uid", chatmsg.getChatId());
                    downloadBundle.putString("username", chatmsg.getUsername());
                    downloadBundle.putString("avatarurl", chatmsg.getUserImage());
                    downloadBundle.putSerializable("chatmsg", chatmsg);
                    Utils.intentService(
                            getApplicationContext(),
                            LoadDataService.class,
                            LoadDataService.ACTION_FILE_DOWNLOAD,
                            LoadDataService.ACTION_FILE_DOWNLOAD,
                            downloadBundle);

                }
                if (chatmsg.getType() == 103 && !TextUtils.isEmpty(content)) {
                    chatmsg.setContent(chatmsg.getShareTitle());
                }

                return chatmsg;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Everyone chat room
     */
    private ChatMsg parseRoomChat(Message msg) {
            if (!TextUtils.isEmpty(msg.getBody())) {
                try {
                    String content = null;
                    ChatMsg chatmsg = parseBody(msg);
                    chatmsg.setSend(1);
                    chatmsg.setMsgType(msg.getMsgtype());
                    chatmsg.setChatId(Constants.APP_EVERYONE);
                    {
                        if (!checkChatType(chatmsg.getType())) {
                            chatmsg.setType(50000);
                        }
                        switch (chatmsg.getType()) {
                            case 13://Share the news
                                chatmsg.setType(103);
                                content = chatmsg.getContent();
                                break;
                            case 50000://Not compatible with high version of the news
                                chatmsg.setContent(chatmsg.getMsgName());
                                break;
                        }
                    }
                    final String avatarUrl = chatmsg.getUserImage();
                    String uName = chatmsg.getUsername();
                    chatmsg.setGroupName(getString(R.string.everyone));
                    FinalUserDataBase.getInstance().saveChatMsg(chatmsg, chatmsg.getChatId(), chatmsg.getGroupName(), null);
                    chatmsg.setUsername(uName);
                    chatmsg.setUserImage(avatarUrl);
                    if (chatmsg.getType() == 103 && !TextUtils.isEmpty(content)) {
                        chatmsg.setContent(content);
                    }
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(XmppAction.ACTION_MESSAGE_LISTENER, chatmsg);
                    Utils.intentAction(getApplicationContext(), XmppAction.ACTION_MESSAGE_LISTENER, bundle);
                    if (chatmsg.getType() == 2) {//Download the audio files
                        Bundle downloadBundle = new Bundle();
                        downloadBundle.putString("uid", chatmsg.getChatId());
                        downloadBundle.putString("username", chatmsg.getUsername());
                        downloadBundle.putString("avatarurl", chatmsg.getUserImage());
                        downloadBundle.putSerializable("chatmsg", chatmsg);
                        Utils.intentService(
                                getApplicationContext(),
                                LoadDataService.class,
                                LoadDataService.ACTION_FILE_DOWNLOAD,
                                LoadDataService.ACTION_FILE_DOWNLOAD,
                                downloadBundle);
                    }
                    if (chatmsg.getType() == 103 && !TextUtils.isEmpty(content)) {
                        chatmsg.setContent(chatmsg.getShareTitle());
                    }
                    return chatmsg;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
    }


    /**
     * Group chat with
     */
    private ChatMsg parseGroupChat(Message msg, boolean isGroupSystem) {
        if (!TextUtils.isEmpty(msg.getBody())) {
            try {
                String content = null;
                ChatMsg chatmsg = parseBody(msg);
                chatmsg.setMsgType(msg.getMsgtype());
                String[] uids = chatmsg.getChatId().split("@");
                chatmsg.setChatId("group-" + chatmsg.getGroupId());
                if (uids.length > 1){
                    chatmsg.setChatId(chatmsg.getChatId() + "@" + uids[1]);
                }
                chatmsg.setGroup(true);
                if (chatmsg.getType() == 10000){//Offline message
                    new Thread(new HandleOfflineMsgList(chatmsg, MsgType.groupchat)).start();
                    return chatmsg;
                } else if (!isGroupSystem) {//Is not a group of notice
                    if (!checkChatType(chatmsg.getType())) {
                        chatmsg.setType(50000);
                    }
                    switch (chatmsg.getType()) {
                        case 13://Share the news
                            chatmsg.setType(103);
                            content = chatmsg.getContent();
                            break;
                        case 19://file
                            chatmsg.setInviteType(4);
                            chatmsg.setType(1009);
                            break;
                        case 50000://Not compatible with high version of the news
                            chatmsg.setContent(chatmsg.getMsgName());
                            break;
                    }
                } else {//Is a group of notification
                    if (chatmsg.getType() == 12 || chatmsg.getType() == 13) {
                        //Invited to join the group chat message by default
                        if (!TextUtils.isEmpty(chatmsg.getInviteName())) {
                            chatmsg.setContent(getString(R.string.discuss_group_invite, chatmsg.getInviteName()));
                        } else {
                            chatmsg.setContent(getString(R.string.discuss_group_invite_default));
                        }
                    } else if (chatmsg.getType() == 14) {
                        chatmsg.setContent(getString(R.string.discuss_group_kick));

                    } else if (chatmsg.getType() == 15) {

                        chatmsg.setContent(getString(R.string.discuss_group_dismiss));
                    } else if (chatmsg.getType() == 17) {//Modify the multiplayer session name

                        chatmsg.setContent(getString(R.string.discuss_group_rename, chatmsg.getGroupName()));

                    } else if (chatmsg.getType() == 18) {//Someone quit the multiplayer session

                        chatmsg.setContent(getString(R.string.discuss_group_outside, chatmsg.getUsername()));

                    }
                    try {
                        MySharedPrefs.writeBoolean(NextApplication.mContext, MySharedPrefs.FILE_USER, MySharedPrefs.IS_MASK_MSG + NextApplication.myInfo.getLocalId() + "_" + chatmsg.getChatId(), chatmsg.getGroupMask());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    chatmsg.setDismissGroup(chatmsg.getType() == 15);
                    chatmsg.setKickGroup(chatmsg.getType() == 14);
                    Bundle bundle = new Bundle();
                    bundle.putString("uid", chatmsg.getChatId());
                    bundle.putBoolean("dismissgroup", chatmsg.isDismissGroup());
                    bundle.putBoolean("kickgroup", chatmsg.isKickGroup());
                    Utils.intentAction(getApplicationContext(), XmppAction.ACTION_MESSAGE_GROUP_KICK_LISTENER, bundle);
                }
                StringBuilder url = new StringBuilder();
                if (chatmsg.getMemberAvatarList() != null) {
                    for (GroupMemberAvatarVo vo : chatmsg.getMemberAvatarList()) {
                        url.append(vo.getImage()).append("___").append(vo.getGender()).append("___").append(vo.getUsername()).append("#");
                    }
                    url.deleteCharAt(url.lastIndexOf("#"));
                }
                final String avatarUrl = chatmsg.getUserImage();
                String uName = chatmsg.getUsername();
                if (isGroupSystem) {//Is the information system is not to judge whether time display
                    FinalUserDataBase.getInstance().saveChatMsg(chatmsg, chatmsg.getChatId(), chatmsg.getGroupName(), url.toString(), false);
                } else {//Is not the system information is normal
                    FinalUserDataBase.getInstance().saveChatMsg(chatmsg, chatmsg.getChatId(), chatmsg.getGroupName(), url.toString());
                }
                chatmsg.setUsername(uName);
                chatmsg.setRealname(uName);
                chatmsg.setUserImage(avatarUrl);
                if (chatmsg.getType() == 103 && !TextUtils.isEmpty(content)) {
                    chatmsg.setContent(content);
                }
                Bundle bundle = new Bundle();
                bundle.putSerializable(XmppAction.ACTION_MESSAGE_LISTENER, chatmsg);
                Utils.intentAction(getApplicationContext(), XmppAction.ACTION_MESSAGE_LISTENER, bundle);
                if (chatmsg.getType() == 2) {//Download the audio files
                    Bundle downloadBundle = new Bundle();
                    downloadBundle.putString("uid", chatmsg.getChatId());
                    downloadBundle.putString("username", chatmsg.getUsername());
                    downloadBundle.putString("avatarurl", chatmsg.getUserImage());
                    downloadBundle.putSerializable("chatmsg", chatmsg);
                    Utils.intentService(
                            getApplicationContext(),
                            LoadDataService.class,
                            LoadDataService.ACTION_FILE_DOWNLOAD,
                            LoadDataService.ACTION_FILE_DOWNLOAD,
                            downloadBundle);
                }
                ChatMsg chatMsgT =  new ChatMsg();
                chatMsgT.parseChatMsgVo(chatmsg);
                chatMsgT.setUserImage(url.toString());
                if (chatmsg.getType() == 103 && !TextUtils.isEmpty(content)) {
                    chatmsg.setContent(chatmsg.getShareTitle());
                    chatMsgT.setContent(chatMsgT.getShareTitle());
                }
                return chatMsgT;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Group chat with
     */
    private ChatMsg parseSuperGroupChat(Message msg, boolean isGroupSystem) {
        if (!TextUtils.isEmpty(msg.getBody())) {
            try {
                String content = null;
                ChatMsg chatmsg = parseBody(msg);
                chatmsg.setMsgType(msg.getMsgtype());
                if (chatmsg.getType() == 10000)//Offline message
                {
                    new Thread(new HandleOfflineMsgList(chatmsg, MsgType.super_groupchat)).start();
                    return chatmsg;
                } else if (!isGroupSystem) {//The system information
                    chatmsg.setType(chatmsg.getType() - 1000);//Group information for the type on the basis of original + 1000
                }
                String[] uids = chatmsg.getChatId().split("@");
                chatmsg.setChatId("superGroup-" + chatmsg.getGroupId());
                if (uids.length > 1)
                {
                    chatmsg.setChatId(chatmsg.getChatId() + "@" + uids[1]);
                }
                chatmsg.setGroup(true);
                if (!isGroupSystem) {//Is not a group of notice
                    if (!checkChatType(chatmsg.getType())) {
                        chatmsg.setType(50000);
                    }
                    switch (chatmsg.getType()) {
                        case 13://Share the news
                            if(!TextUtils.isEmpty(chatmsg.getUsersource()) && !chatmsg.getUsersource().equals(XmppUtils.SERVER_NAME) && !TextUtils.isEmpty(chatmsg.getShareUrl()) &&  (chatmsg.getShareUrl().contains("/vodInfo.html") || (chatmsg.getShareUrl().contains("/share_vod.html"))) && !chatmsg.getShareUrl().contains(Constants.APP_URL_FLAG))
                            {
                                chatmsg.setType(50002);
                            }
                            else{
                                chatmsg.setType(103);
                                content = chatmsg.getContent();
                            }
                            break;
                        case 19://file
                            chatmsg.setInviteType(4);
                            chatmsg.setType(1009);
                            break;
                        case 50000://Not compatible with high version of the news
                            chatmsg.setContent(chatmsg.getMsgName());
                            break;
                    }

                } else {//Is a group of notification
                    chatmsg.setDismissGroup(chatmsg.getType() == 109);
                    chatmsg.setKickGroup(chatmsg.getType() == 108);
                    if (chatmsg.getType() == 108) {
                        chatmsg.setType(12);
                    } else if (chatmsg.getType() == 109) {
                        chatmsg.setType(12);
                    } else if (chatmsg.getType() == 1000) {
                        chatmsg.setType(12);
                    }
                    try {
                        MySharedPrefs.writeBoolean(NextApplication.mContext, MySharedPrefs.FILE_USER, MySharedPrefs.IS_MASK_MSG + NextApplication.myInfo.getLocalId() + "_" + chatmsg.getChatId(), chatmsg.getGroupMask());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Bundle bundle = new Bundle();
                    bundle.putString("uid", chatmsg.getChatId());
                    bundle.putBoolean("dismissgroup", chatmsg.isDismissGroup());
                    bundle.putBoolean("kickgroup", chatmsg.isKickGroup());
                    Utils.intentAction(getApplicationContext(), XmppAction.ACTION_MESSAGE_GROUP_KICK_LISTENER, bundle);
                }

                final String avatarUrl = chatmsg.getUserImage();
                String uName = chatmsg.getUsername();

                if (isGroupSystem) {//Is the information system is not to judge whether time display
                    FinalUserDataBase.getInstance().saveChatMsg(chatmsg, chatmsg.getChatId(), chatmsg.getGroupName(), chatmsg.getGroupImage(), false);
                } else {//Is not the system information is normal
                    FinalUserDataBase.getInstance().saveChatMsg(chatmsg, chatmsg.getChatId(), chatmsg.getGroupName(), chatmsg.getGroupImage());
                }
                chatmsg.setUsername(uName);
                chatmsg.setUserImage(avatarUrl);
                if (chatmsg.getType() == 103 && !TextUtils.isEmpty(content)) {
                    chatmsg.setContent(content);
                }
                Bundle bundle = new Bundle();
                bundle.putSerializable(XmppAction.ACTION_MESSAGE_LISTENER, chatmsg);
                Utils.intentAction(getApplicationContext(), XmppAction.ACTION_MESSAGE_LISTENER, bundle);
                if (chatmsg.getType() == 2) {//Download the audio files
                    Bundle downloadBundle = new Bundle();
                    downloadBundle.putString("uid", chatmsg.getChatId());
                    downloadBundle.putString("username", chatmsg.getUsername());
                    downloadBundle.putString("avatarurl", chatmsg.getUserImage());
                    downloadBundle.putSerializable("chatmsg", chatmsg);
                    Utils.intentService(
                            getApplicationContext(),
                            LoadDataService.class,
                            LoadDataService.ACTION_FILE_DOWNLOAD,
                            LoadDataService.ACTION_FILE_DOWNLOAD,
                            downloadBundle);
                }
                if (chatmsg.getType() == 103 && !TextUtils.isEmpty(content)) {
                    chatmsg.setContent(chatmsg.getShareTitle());
                }
                return chatmsg;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Chat type testing
     */
    private boolean checkChatType(int type) {
        switch (type) {
            case 0://The text
            case 1://The picture
            case 2://voice
            case 3://gif
            case 4://positioning
            case 5://scenario
            case 6://Business card
            case 7://Asked about
            case 8://Agreed to ask about
            case 9://Refused to ask about
            case 10://Ask for information
            case 11://Ask for agree
            case 12://Help to ask refused to
            case 13://Share the news
            case 14://A red envelope message
            case 15://Micro video
            case 16://live
            case 17://Gift bag
            case 18://Props message
            case 19://File message
            case 20://And fortunella venosa
            case 21://Group broadcast news
            case 10000://Offline message

//				
//			case 100://group
//			case 101://group
//			case 102://group
//			case 103://group
//			case 104://group
//			case 105://group
//			case 106://group
//			case 107://group
//			case 108://group
//			case 109://group
//			case 111://group
//			case 112://group
//			case 113://group
//			case 114://group
//			case 115://group
//			case 116://group
//			case 117://group
//			case 118://group
//			case 119://group
//			case 120://group
//			case 121://group
//			case 122://group
//			case 123://group
//			case 124://group
//			case 125://group
//			case 126://group
//			case 127://group
                return true;
            default:
                return false;
        }
    }

    /**
     * System type testing
     */
    private boolean checkSystemType(int type) {
        if (type > -1 && type < 20 && type != 2) {//Resolve to 19
            return true;
        } else if (type == 21) {//21 Invitation activities modify warning
            return true;
        } else if (type == 22) {//22 Someone was invited to join people session or someone kicked out messages
            return true;
        } else if (type == 23) {//Friend recommended information
            return true;
        } else if (type > 100 && type < 118) {//Group information
            return true;
        } else if (type == 1000) {//Group information
            return true;
        } else if (type == 500) {//Single system messages
            return true;
        } else if (type == 300) {//transaction system messages
            return true;
        }else //The payment information
            return type > 199 && type < 211;
    }

    /**
     * Information processing system
     */
    private ChatMsg parseSystem(Message msg) {
        try {
            ChatMsg chatmsg = parseBody(msg);
            chatmsg.setMsgType(msg.getMsgtype());
            if (chatmsg.getType() == -1 && Utils.getIMEI(XmppService.this).equals(msg.getTo().split("/")[1])) {
                //Forced offline
                offLinetype = -1;
                Bundle bundle = new Bundle();
                bundle.putString("offlineContent", chatmsg.getContent());
                Utils.intentAction(getApplicationContext(), XmppAction.ACTION_MAIN_OFFLINE_LISTENER, bundle);
                return null;
            }

            if (!checkSystemType(chatmsg.getType())) {
                return null;
            }

            if (chatmsg.getType() == 23) {//Friend recommended information
                FriendRecommentVo vo = parseFriendRecommentVo(msg);
                vo.setUnread(1);
                boolean saved = FinalUserDataBase.getInstance().saveFriendsReComment(vo);
                if(saved){
                    Intent intent = new Intent(XmppAction.ACTION_FRIEND_RECOMMENT);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(XmppAction.ACTION_FRIEND_RECOMMENT, vo);
                    intent.putExtras(bundle);
                    Utils.sendBroadcastReceiver(getApplicationContext(), intent, true);
                }
                return null;
            }
            if (chatmsg.getType() == 1) {//Passive add buddy Refresh the list
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(Constants.ACTION_SELECT_CONTACT_REFRESH));
                FinalUserDataBase.getInstance().deleteFriendsRecommentByUid(chatmsg.getUserId());
                return null;
            }

            int type = 0;

            if ((chatmsg.getType() > 11 && chatmsg.getType() < 19) || chatmsg.getType() == 22) {//Group chat
                return parseGroupChat(msg, true);//Group chat chat system information with unified handling
            } else if ((chatmsg.getType() > 100 && chatmsg.getType() < 120) || chatmsg.getType() == 1000) {//Group system information
                switch (chatmsg.getType()) {//Group chat window notification
                    case 101:
                    case 104:
                        chatmsg.setCreateName(chatmsg.getInviteName());
                        chatmsg.setModifyType(chatmsg.getIs_manager() == 1 ? 1 : 0);////As is a group of the Lord or administrator to invite someone else
                        break;
                    case 102:
                    case 103:
                        chatmsg.setCreateName(chatmsg.getBeinviteName());
                        break;
                    case 105:
                        break;
                    case 115:
                    case 116:
                        chatmsg.setModifyType(chatmsg.getLefttimes());//As a group of frequency change
                        break;

                    case 108:
                    case 109:
                        return parseSuperGroupChat(msg, true);
                    case 1000:
                        return parseSuperGroupChat(msg, true);
                }
                type = 3;
                String[] uids = chatmsg.getChatId().split("@");
                if(uids.length>1)
                {
                    chatmsg.setUserId(chatmsg.getGroupId()+"@"+uids[1]);
                }
                else{
                    chatmsg.setUserId(chatmsg.getGroupId());
                }

                chatmsg.setUsername(chatmsg.getGroupName());
                chatmsg.setUserImage(chatmsg.getGroupImage());

                Bundle bundle = new Bundle();
                bundle.putSerializable(XmppAction.ACTION_MESSAGE_ADDGROUP, chatmsg);
                Utils.intentAction(getApplicationContext(), XmppAction.ACTION_MESSAGE_ADDGROUP, bundle);

            }else if (chatmsg.getType() == 500)//Single system messages
            {
                return parseNormalChat(msg, true);
            }else if (chatmsg.getType() == 300){//transaction message
                boolean found = false;
                for(int i=0;i<WalletStorage.getInstance(NextApplication.mContext).get().size();i++)
                {
                    StorableWallet wallet = WalletStorage.getInstance(NextApplication.mContext).get().get(i);
                    String walletAddress = wallet.getPublicKey();
                    if (!walletAddress.startsWith("0x")){
                        walletAddress = "0x" + walletAddress;
                    }
                    if(TextUtils.equals(walletAddress,chatmsg.getFromAddress()) && chatmsg.getNoticeType() == 0)
                    {
                        found = true;
                        break;
                    }
                    else if(TextUtils.equals(walletAddress,chatmsg.getToAddress()) && chatmsg.getNoticeType() == 1)
                    {
                        found = true;
                        break;
                    }
                }

                if(!found || TextUtils.isEmpty(chatmsg.getTokenAddress())){
                    return null;
                }

                type = 5;
                TransVo transVo = new TransVo();
                transVo.setTime(chatmsg.getCreateTime());
                transVo.setType(Integer.parseInt(chatmsg.getMode()));
                transVo.setTxBlockNumber(Integer.parseInt(chatmsg.getTxBlockNumber()));
                transVo.setFee(chatmsg.getFee());
                transVo.setValue(chatmsg.getMoney());
                transVo.setTxurl(chatmsg.getShareUrl());
                transVo.setTx(chatmsg.getNumber());
                transVo.setFromAddress(chatmsg.getFromAddress());
                transVo.setToAddress(chatmsg.getToAddress());
                transVo.setState(chatmsg.getInviteType());
                transVo.setNoticeType(chatmsg.getNoticeType());
                transVo.setName(chatmsg.getTokenName());
                transVo.setLogo(chatmsg.getTokenLogo());
                transVo.setSymbol(chatmsg.getTokenSymbol());
                transVo.setTokenAddress(chatmsg.getTokenAddress());
                if (transVo.getState() == 1){
                    transVo.setState(2);
                    FinalUserDataBase.getInstance().insertTrans(transVo,false);
                }else{
                    transVo.setState(1);
                    FinalUserDataBase.getInstance().insertTrans(transVo,true);
                }


                if (chatmsg.getNoticeType() != 0){// ==0 sender
                    boolean hasFound = FinalUserDataBase.getInstance().hasToken(chatmsg.getTokenAddress(),chatmsg.getToAddress());
                    if (!hasFound){
                        TokenVo tokenVo = new TokenVo();
                        tokenVo.setChecked(true);
                        tokenVo.setTokenLogo(transVo.getLogo());
                        tokenVo.setTokenSymbol(transVo.getSymbol());
                        tokenVo.setTokenName(transVo.getName());
                        tokenVo.setContactAddress(transVo.getTokenAddress());
                        FinalUserDataBase.getInstance().insertToken(tokenVo,transVo.getToAddress());
                    }
                }

                Bundle bundle = new Bundle();
                bundle.putSerializable(XmppAction.ACTION_TRANS, chatmsg);
                Utils.intentAction(getApplicationContext(), XmppAction.ACTION_TRANS, bundle);
            }

            if (chatmsg.getType() == 0) {//Add buddy information to heavy or not deleted!
                String messageid = FinalUserDataBase.getInstance().hasFriendAddMsg(chatmsg.getChatId());
                if (!TextUtils.isEmpty(messageid)) {
//                    cantAddUnreadCount = true;
                    chatmsg.setUnread(1);
                    FinalUserDataBase.getInstance().deleteChatEventAddContactByMessageId(messageid);
                }
                Bundle bundle = new Bundle();
                bundle.putSerializable(XmppAction.ACTION_MESSAGE_ADDCONTACT, chatmsg);
                Utils.intentAction(getApplicationContext(), XmppAction.ACTION_MESSAGE_ADDCONTACT, bundle);
            }
            try {
                MySharedPrefs.writeBoolean(NextApplication.mContext, MySharedPrefs.FILE_USER, MySharedPrefs.IS_MASK_MSG + NextApplication.myInfo.getLocalId() + "_" + chatmsg.getChatId(), chatmsg.getGroupMask());
            } catch (Exception e) {
                e.printStackTrace();
            }

            chatmsg.setChatId("system-" + type);
            chatmsg.setSystem(true);
            chatmsg.setHidden(false);

            FinalUserDataBase.getInstance().saveChatEvent(chatmsg);
            chatmsg.setHidden(true);
            FinalUserDataBase.getInstance().saveChatEvent(chatmsg);
            return chatmsg;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private ChatMsg parseBody(Message msg) throws Exception {
        JSONObject obj = new JSONObject(msg.getBody());
        ChatMsg chatmsg = new ChatMsg().parse(obj);
        chatmsg.setChatId(msg.getFrom().split("@")[0]);

        if (msg.getMsgtype() == MsgType.system && chatmsg.getType() == 500) {
            chatmsg.setChatId(chatmsg.getUserId());
        }

        String server_name = msg.getFrom().split("@")[1];
        if (server_name.contains("/")) {
            int end = server_name.indexOf("/");
            server_name = server_name.substring(0, end);
        }
        boolean isGroupNormalOrSystemMsg = false;//If your group chat live news userid domain not group system
        if(server_name.startsWith("super_group.") || server_name.startsWith("group."))
        {
            isGroupNormalOrSystemMsg = true;
        }
        if(server_name.startsWith("super_group."))
        {
            server_name = server_name.replace("super_group.","");//Group special processing, it is necessary to remove super_group.
        }
        if (!TextUtils.isEmpty(server_name) && !server_name.contains(XmppUtils.SERVER_NAME))
        {
            chatmsg.setChatId(chatmsg.getChatId() + "@" + server_name);
            if(!isGroupNormalOrSystemMsg && !TextUtils.isEmpty(chatmsg.getUserId()))//The group messages or group system, the userid and the from same domain
            {
                chatmsg.setUserId(chatmsg.getUserId() + "@" + server_name);
            }
            chatmsg.setFriendLog(1);
        }
        if(!TextUtils.isEmpty(chatmsg.getUsersource()) && !chatmsg.getUsersource().equals(XmppUtils.SERVER_NAME))//Groups, and discussion groups will use, labeled message domain users may source and groups
        {
            String[] uids = chatmsg.getUserId().split("@");//Due to the above code, for cross domain group or group chat may userid has spliced into xxx@juejian.net but the message does not necessarily and group users of the domain name
            if(uids.length>0)
            {
                chatmsg.setUserId(uids[0] + "@" + chatmsg.getUsersource());
            }
            else{
                chatmsg.setUserId(chatmsg.getUserId() + "@" + chatmsg.getUsersource());
            }
        }
        chatmsg.setMsgTime(msg.getMsgTime() / 1000);
        chatmsg.setMessageId(msg.getPacketID());
        chatmsg.setUnread(1);
        return chatmsg;
    }

    private ChatMsg parseOfflineBody(ChatMsg msg, JSONObject obj, int unreadcount, MsgType type) throws Exception {
        ChatMsg chatmsg = new ChatMsg().parse(obj, msg, type);
        chatmsg.setUnread(unreadcount);
        return chatmsg;
    }

    private void ackMsg(Message msg) throws Exception {
        if (MsgType.msgStatus.equals(msg.getMsgtype())) {
            return;
        }
        Message ackMsg = new Message();
        ackMsg.setPacketID(msg.getPacketID());
        ackMsg.setTo(msg.getFrom());
        ackMsg.setFrom(mXmppUtils.getConnection().getUser());
        ackMsg.setMsgtype(MsgType.msgStatus);
        ackMsg.setType(Type.normal);
        JSONObject obj = new JSONObject();
        obj.put("state", "received");
        ackMsg.setBody(obj.toString());
        mXmppUtils.getConnection().sendPacket(ackMsg);
    }

    @SuppressLint("NewApi")
    private void sendNotify(Message msg, ChatMsg chat) {
        //Figure to push open the corresponding chat only
        boolean isOfflineMsg = false;
        String notifyTicker = "";
        if (msg.getMsgtype() == Message.MsgType.normalchat) {
            //Normal chat
            try {
                ChatMsg chatmsg = parseBody(msg);
                chatmsg.setMsgType(msg.getMsgtype());
                //Determine whether is talking to the user
                SharedPreferences preferences = getSharedPreferences("chatuid", MODE_PRIVATE);
                String chatuid = preferences.getString("chatuid", null);
                if (chatuid != null && chatuid.equals(chatmsg.getChatId())) {
                    return;
                }
                if (chatmsg.getGroupMask())//Only digital tip shielding group chat, don't push messages, and there is no sound and vibration
                {
                    return;
                }
                switch (chatmsg.getType()) {
                    case 0://The text
                        notifyTicker = chatmsg.getUsername() + "：" + chatmsg.getContent();
                        break;
                    case 1://The picture
                        notifyTicker = getString(R.string.chat_notify_picture,chatmsg.getUsername());
                        break;
                    case 2://voice
                        notifyTicker = getString(R.string.chat_notify_audio,chatmsg.getUsername());
                        break;
                    case 3://gif
                        notifyTicker = getString(R.string.chat_notify_gif,chatmsg.getUsername());
                        break;
                    case 6://Business card
                        notifyTicker = getString(R.string.chat_notify_card,chatmsg.getUsername(),chatmsg.getThirdName());
                        break;
                    case 13://Share links
                        notifyTicker = getString(R.string.chat_notify_link,chatmsg.getUsername());
                        break;
                    case 15://Micro video
                        notifyTicker = getString(R.string.chat_notify_small_video,chatmsg.getUsername());
                        break;
                    case 19://file
                        notifyTicker = getString(R.string.chat_notify_file,chatmsg.getUsername());
                        break;
                    default:
                        return;
                }
            } catch (Exception e) {

            }
        } else if (msg.getMsgtype() == Message.MsgType.system) {
            //System information
            try {
                ChatMsg sysmsg = parseBody(msg);
                sysmsg.setMsgType(msg.getMsgtype());
                switch (sysmsg.getType()) {
                    case -1://Offline message
                        if (!Utils.getIMEI(XmppService.this).equals(msg.getTo().split("/")[1])) {
                            return;
                        }
                        notifyTicker = sysmsg.getContent();
                        isOfflineMsg = true;
                        break;
                    case 0://A friend request message
                        return;
                    case 1://no
                    case 2://Friend, delete the message
                        return;
                    case 12:
                    case 13: {
                        //Determine whether is the group chat
                        SharedPreferences preferences = getSharedPreferences("chatuid", MODE_PRIVATE);
                        String chatuid = preferences.getString("chatuid", null);
                        if (chatuid != null && chatuid.replace("group-", "").equals(sysmsg.getGroupId())) {
                            return;
                        }
                        if (!TextUtils.isEmpty(sysmsg.getInviteName())) {
                            notifyTicker = getString(R.string.chat_notify_invite,sysmsg.getInviteName(),sysmsg.getGroupName());
                        } else {
                            notifyTicker = getString(R.string.chat_notify_invited_join,sysmsg.getGroupName());
                        }

                        break;
                    }
                    case 14: {
                        SharedPreferences preferences = getSharedPreferences("chatuid", MODE_PRIVATE);
                        String chatuid = preferences.getString("chatuid", null);
                        if (chatuid != null && chatuid.replace("group-", "").equals(sysmsg.getGroupId())) {
                            return;
                        }
                        notifyTicker = getString(R.string.chat_notify_move_out,sysmsg.getGroupName());
                        break;
                    }
                    case 15: {
                        SharedPreferences preferences = getSharedPreferences("chatuid", MODE_PRIVATE);
                        String chatuid = preferences.getString("chatuid", null);
                        if (chatuid != null && chatuid.replace("group-", "").equals(sysmsg.getGroupId())) {
                            return;
                        }

                        if (TextUtils.isEmpty(sysmsg.getGroupName())) {
                            notifyTicker = getString(R.string.chat_notify_group_dissolve);
                        } else {
                            notifyTicker = getString(R.string.chat_notify_dissolution_of,sysmsg.getGroupName());
                        }
                        break;
                    }
                    case 105:
                        notifyTicker = getString(R.string.chat_notify_have_join,sysmsg.getGroupName());
                        break;
                    case 300:
                        notifyTicker = getString(R.string.chat_notify_have_new_trans);
                        break;
                    default:
                        return;
                }
            } catch (Exception e) {
                // TODO: handle exception
            }

        } else if (msg.getMsgtype() == Message.MsgType.groupchat) {
            //Group chat
            try {
                ChatMsg chatmsg = parseBody(msg);
                //Determine whether is the group chat
                SharedPreferences preferences = getSharedPreferences("chatuid", MODE_PRIVATE);
                String chatuid = preferences.getString("chatuid", null);
                if (chatuid != null && chatuid.replace("group-", "").equals(chatmsg.getGroupId())) {
                    return;
                }
                if (chatmsg.getGroupMask())//Only digital tip shielding group chat, don't push messages, and there is no sound and vibration
                {
                    return;
                }
                switch (chatmsg.getType()) {
                    case 0://The text
                        notifyTicker = chatmsg.getUsername() + "：" + chatmsg.getContent();
                        break;
                    case 1://The picture
                        notifyTicker = getString(R.string.chat_notify_picture,chatmsg.getUsername());
                        break;
                    case 2://voice
                        notifyTicker = getString(R.string.chat_notify_audio,chatmsg.getUsername());
                        break;
                    case 3://gif
                        notifyTicker = getString(R.string.chat_notify_gif,chatmsg.getUsername());
                        break;
                    case 6://Business card
                        notifyTicker = getString(R.string.chat_notify_card,chatmsg.getUsername(),chatmsg.getThirdName());
                        break;
                    case 13://Share links
                        notifyTicker = getString(R.string.chat_notify_link,chatmsg.getUsername());
                        break;
                    case 15://Micro video
                        notifyTicker = getString(R.string.chat_notify_small_video,chatmsg.getUsername());
                        break;
                    case 19://file
                        notifyTicker = getString(R.string.chat_notify_file,chatmsg.getUsername());
                        break;
                    default:
                        return;
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
        } else if (msg.getMsgtype() == Message.MsgType.super_groupchat) {
            //Group chat
            try {
                ChatMsg chatmsg = parseBody(msg);
                //Determine whether is the group chat
                SharedPreferences preferences = getSharedPreferences("chatuid", MODE_PRIVATE);
                String chatuid = preferences.getString("chatuid", null);
                if (chatuid != null && chatuid.replace("superGroup-", "").equals(chatmsg.getGroupId())) {
                    return;
                }
                if (chatmsg.getGroupMask())//Only digital tip shielding group chat, don't push messages, and there is no sound and vibration
                {
                    return;
                }
                switch (chatmsg.getType()) {
                    case 1000://text
                        notifyTicker = chatmsg.getUsername() + "：" + chatmsg.getContent();
                        break;
                    case 1001://picture
                        notifyTicker = getString(R.string.chat_notify_picture,chatmsg.getUsername());
                        break;
                    case 1002://voice
                        notifyTicker = getString(R.string.chat_notify_audio,chatmsg.getUsername());
                        break;
                    case 1003://gif
                        notifyTicker = getString(R.string.chat_notify_gif,chatmsg.getUsername());
                        break;
                    case 1006://card
                        notifyTicker = getString(R.string.chat_notify_card,chatmsg.getUsername(),chatmsg.getThirdName());
                        break;
                    case 1013://share link
                        notifyTicker = getString(R.string.chat_notify_link,chatmsg.getUsername());
                        break;
                    case 1015://video
                        notifyTicker = getString(R.string.chat_notify_small_video,chatmsg.getUsername());
                        break;
                    case 1019://file
                        notifyTicker = getString(R.string.chat_notify_file,chatmsg.getUsername());
                        break;
                    case 50001://Compatibility groups across the domain name to send message does not display, such as red envelopes, red packets +, live, replay scene
                    case 50002:
                        notifyTicker = getString(R.string.chat_notify_two_hint,chatmsg.getUsername(),chatmsg.getContent());
                        break;
                    default:
                        return;
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
        } else {
            return;
        }
        String notifyContent = "";
        String notifyTitle = getString(R.string.app_name);
        Intent i = new Intent(XmppService.this, MainFragmentUI.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra("isNewMsg", true);
        int notifyid = 0;
        if (isOfflineMsg) {
            notifyid = (int) (System.currentTimeMillis() / 1000);
            i.putExtra("isOfflineMsg", isOfflineMsg);
            i.putExtra("offlineContent", notifyTicker);
            XmppUtils.getInstance().destroy();
            stopSelf();
        }else {
            i.putExtra("msg", chat);
        }
        if (notifyid == 0)//General chat messages
        {
                notifyContent = notifyTicker;
        } else {
            notifyContent = notifyTicker;
        }

        PendingIntent contentIntent = PendingIntent.getActivity(XmppService.this, notifyid,
                i, PendingIntent.FLAG_UPDATE_CURRENT);

        Builder mBuilder = new Notification.Builder(XmppService.this)
                .setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.notifyicon)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.logo))
                .setWhen(System.currentTimeMillis())
                .setTicker(notifyTicker)
                .setContentTitle(notifyTitle)
                .setContentText(notifyContent)
                .setAutoCancel(true);
        boolean sound = MySharedPrefs.readBoolean(XmppService.this, MySharedPrefs.FILE_USER, MySharedPrefs.MSG_SOUND + NextApplication.myInfo.getLocalId());
        boolean vibration = MySharedPrefs.readBoolean(XmppService.this, MySharedPrefs.FILE_USER, MySharedPrefs.MSG_VIBRATION + NextApplication.myInfo.getLocalId());

        boolean isNoDisturb = MySharedPrefs.readBoolean(XmppService.this, MySharedPrefs.FILE_USER, MySharedPrefs.NO_DISURB_MODE);
        boolean isInTime = false;//Determine if the do not disturb mode period
        if (isNoDisturb) {
            int beginHour = MySharedPrefs.readInt1(XmppService.this, MySharedPrefs.FILE_USER, MySharedPrefs.NO_DISURB_MODE_BEGIN_TIME_HOUR);
            int beginMinute = MySharedPrefs.readInt1(XmppService.this, MySharedPrefs.FILE_USER, MySharedPrefs.NO_DISURB_MODE_BEGIN_TIME_MINUTE);
            int endHour = MySharedPrefs.readInt1(XmppService.this, MySharedPrefs.FILE_USER, MySharedPrefs.NO_DISURB_MODE_END_TIME_HOUR);
            int endMinute = MySharedPrefs.readInt1(XmppService.this, MySharedPrefs.FILE_USER, MySharedPrefs.NO_DISURB_MODE_END_TIME_MINUTE);
            if (beginHour == -1 || beginMinute == -1) {
                beginHour = 23;
                beginMinute = 0;
            }
            if (endHour == -1 || endMinute == -1) {
                endHour = 8;
                endMinute = 0;
            }
            if (beginHour > endHour || beginHour == endHour && beginMinute > endMinute) {
                endHour += 24;
            }
            Date date = new Date();
            int currentHour = date.getHours();
            int currentMin = date.getMinutes();
            if (currentHour > beginHour && currentHour < endHour) {
                isInTime = true;
            } else if (currentHour == beginHour && currentMin >= beginMinute) {
                if (beginHour == endHour) {
                    if (currentMin <= endMinute) {
                        isInTime = true;
                    }
                } else {
                    if (currentHour < endHour) {
                        isInTime = true;
                    }
                }
            } else if (currentHour == endHour && currentMin <= endMinute) {
                if (beginHour == endHour) {
                    if (currentMin >= beginMinute) {
                        isInTime = true;
                    }
                } else {
                    if (currentHour > beginHour) {
                        isInTime = true;
                    }
                }
            }
        }
        if (!isInTime) {
            if (sound && vibration) {
                mBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
            } else if (sound) {
                mBuilder.setDefaults(Notification.DEFAULT_SOUND);
            } else if (vibration) {
                mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
            }
        }
        if (notifyid == 0) {
            notificationManager.cancel(0);
        }
        notificationManager.notify(notifyid, mBuilder.getNotification());
    }

    /**
     * Parsing friend recommendation information
     */
    private FriendRecommentVo parseFriendRecommentVo(Message msg) {
        FriendRecommentVo vo = new FriendRecommentVo();
        vo.parseXmpp(msg.getBody());
        vo.setMsgId(msg.getPacketID());
        if (vo.getType() == 0) {//The address book friends
            PhoneContactVo cVo = FinalUserDataBase.getInstance().getPhoneContactById(vo.getFriendId(), 1);
            if (cVo != null) {
                vo.setThirdName(cVo.getName());
            }
        }
        return vo;
    }

}
