package com.lingtuan.firefly.contact;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.adapter.MessageEventAdapter;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.custom.ChatMsgComparable;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.offline.AppNetService;
import com.lingtuan.firefly.offline.vo.WifiPeopleVO;
import com.lingtuan.firefly.service.LoadDataService;
import com.lingtuan.firefly.util.BitmapUtils;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.MyViewDialogFragment;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.vo.ChatMsg;
import com.lingtuan.firefly.vo.UserBaseVo;
import com.lingtuan.firefly.xmpp.XmppMessageUtil;
import com.lingtuan.firefly.xmpp.XmppUtils;

import org.jivesoftware.smack.packet.Message.MsgType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Forward messages page
 */
public class ContactSelectedUI extends BaseActivity implements OnItemClickListener {
    private LinearLayout discussLinear;
    private LinearLayout contactLinear;

    private ListView mListView;

    private MessageEventAdapter mAdapter;

    private ArrayList<ChatMsg> msgList;
    private View headerView;
    private String linkTitle = null;

    private AppNetService appNetService;

    @Override
    protected void setContentView() {
        setContentView(R.layout.contact_selected);
    }

    @Override
    protected void findViewById() {
        mListView = (ListView) findViewById(R.id.chatting_list);
        headerView = LayoutInflater.from(this).inflate(R.layout.contact_selected_header, null, false);
        discussLinear = (LinearLayout) headerView.findViewById(R.id.contact_discuss_linear);
        contactLinear = (LinearLayout) headerView.findViewById(R.id.contact_friends_linear);
    }

    @Override
    protected void setListener() {
        mListView.setOnItemClickListener(this);
        discussLinear.setOnClickListener(this);
        contactLinear.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.chatting_selected_title));

        // -1 default  0 close  1 open
        int openSmartMesh = MySharedPrefs.readInt1(NextApplication.mContext,MySharedPrefs.FILE_USER,MySharedPrefs.KEY_NO_NETWORK_COMMUNICATION + NextApplication.myInfo.getLocalId());
        if (openSmartMesh == -1){
            bindService(new Intent(this, AppNetService.class), serviceConn, Activity.BIND_AUTO_CREATE);
        }

        if (getIntent() != null) {
            msgList = (ArrayList<ChatMsg>) getIntent().getSerializableExtra("msglist");
            if (msgList != null && msgList.size() >= 0){
                if (!TextUtils.isEmpty(msgList.get(0).getForwardLeaveMsg())){
                    linkTitle = msgList.get(0).getForwardLeaveMsg();
                }
            }
        }
        mListView.addHeaderView(headerView);

        mAdapter = new MessageEventAdapter(null, this);
        mListView.setAdapter(mAdapter);
        new Thread(new UpdateMessage()).start();
    }

    // The Activity and netService2 connection
    private ServiceConnection serviceConn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            // Binding service success
            AppNetService.NetServiceBinder binder = (AppNetService.NetServiceBinder) service;
            appNetService = binder.getService();
        }
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
        if (position == 0) {
            return;
        }
        final ChatMsg msg = mAdapter.getItem(position - 1);
//        if(cantTrans(msg.getChatId())){
//            return;
//        }
        List<UserBaseVo> members = msg.getMemberAvatarUserBaseList();
        if (members == null || members.size() == 0) {
            members = new ArrayList<>();
            UserBaseVo vo = new UserBaseVo();
            vo.setGender(msg.getGender() + "");
            vo.setThumb(msg.getUserImage());
            vo.setUsername(msg.getUsername());
            members.add(vo);
        }
        showDialogFormat(msg.getChatId(),msg.getUsername(),msg.getUserImage(),msg.getGender()+"",msg.isGroup(),members);
    }

    class UpdateMessage implements Runnable {

        private Handler handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                List<ChatMsg> mList = (List<ChatMsg>) msg.obj;
                mAdapter.updateList(mList);
            }
        };

        @Override
        public void run() {
            List<ChatMsg> mList = FinalUserDataBase.getInstance().getChatMsgEventListChat();
            Collections.sort(mList, new ChatMsgComparable());
            Message msg = new Message();
            msg.obj = mList;
            handler.sendMessage(msg);
        }

    }


    /**
     * Forward threads
     */
    class SendMsgThread extends Thread {

        ArrayList<ChatMsg> list;
        Dialog dialog;

        String chatId;
        String uName;
        String avatarUrl;
        String gender;
        boolean isDismiss;
        boolean isKick;
        boolean isNewGroup;
        Handler handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (dialog != null) {
                    dialog.dismiss();
                    dialog = null;
                }
                showToast(getResources().getString(R.string.send_ok));
                setResult(RESULT_OK);
                finish();
            }
        };

        public SendMsgThread(Context context, ArrayList<ChatMsg> list, String chatId, String uName, String avatarUrl, String gender, boolean isDismiss, boolean isKick, boolean isNewGroup) {
            this.isDismiss = isDismiss;
            this.isKick = isKick;
            this.avatarUrl = avatarUrl;
            this.list = list;
            this.chatId = chatId;
            this.uName = uName;
            this.gender = gender;
            this.isNewGroup = isNewGroup;
            dialog = LoadingDialog.showDialog(context, null, null);
            start();
        }


        @Override
        public void run() {

            try {
                if (list != null && list.size() != 0) {
                    for (ChatMsg chatmsg : list) {
                        chatmsg.setChatId(chatId);
                        if(chatmsg.getType() ==1)//image
                        {
                            forwardImgMethod( chatmsg.getChatId(),chatmsg.getContent(),uName,avatarUrl,isNewGroup,true);
                        }

//                       XmppMessageUtil.getInstance().forwarding(chatmsg, chatmsg.getChatId(), uName, avatarUrl,!(isDismiss || isKick), isNewGroup);
                        SystemClock.sleep(1100);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            handler.sendEmptyMessage(0);
        }

    }

    /**
     * Sending pictures method
     * @param picturePath Image path
     * */
    private void forwardImgMethod(String uid,String picturePath,String userName,String avatarUrl,boolean isGroup,boolean isSend){
        float density = getResources().getDisplayMetrics().density;
        int screenWidth = Constants.MAX_IMAGE_WIDTH;//mContext.getResources().getDisplayMetrics().widthPixels;
        int screenHeight = Constants.MAX_IMAGE_HEIGHT;//mContext.getResources().getDisplayMetrics().heightPixels;
        int width = (int) (120 * density);
        Bitmap bmp = BitmapUtils.getimage(picturePath, width, width, 10);
        Bitmap bmpUpload = BitmapUtils.getimage(picturePath, screenWidth, screenHeight, Constants.MAX_KB);
        BitmapUtils.saveBitmap2SD(bmp, 10, false);
        String uploadPath = BitmapUtils.saveBitmap2SD(bmpUpload, screenWidth, true).getPath();
        String url = uploadPath;
        boolean successed = true;
        if(uid.equals("everyone")){
            XmppMessageUtil.getInstance().sendEnterLeaveEveryOne(0,false);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ChatMsg msg = createImageChatMsg(uid, url, userName, avatarUrl, BitmapUtils.BitmapToBase64String(bmp), isGroup, isSend);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            XmppMessageUtil.getInstance().sendEnterLeaveEveryOne(0,true);
            if (appNetService != null) {
                successed = appNetService.handleSendPicutre(url, true, uid, msg.getMessageId());
            }
            if(msg.getSend() ==0 && !successed)
            {
                msg.setSend(0);
            }
        }else if(isGroup){
            createImageChatMsg(uid, url, userName, avatarUrl, BitmapUtils.BitmapToBase64String(bmp), isGroup, isSend);
        }
        else{
            boolean foundPeople  = false;
            if(appNetService!=null && appNetService.getwifiPeopleList()!=null)
            {
                for (WifiPeopleVO vo : appNetService.getwifiPeopleList())// All users need to traverse, find out the corresponding touid users
                {
                    if (uid.equals(vo.getLocalId())) {
                        foundPeople=true;
                        break;
                    }
                }
            }
            if(foundPeople)//With no net with no net send messages
            {
                ChatMsg msg = new ChatMsg();
                msg.setType(1);
                msg.setContent(url);
                msg.setLocalUrl(url);
                msg.setCover(BitmapUtils.BitmapToBase64String(bmp));
                msg.parseUserBaseVo(NextApplication.myInfo.getUserBaseVo());
                msg.setChatId(uid);
                msg.setOffLineMsg(true);
                msg.setSend(1);
                msg.setMessageId(UUID.randomUUID().toString());
                msg.setMsgTime(System.currentTimeMillis() / 1000);
                msg.setShowTime(FinalUserDataBase.getInstance().isOffLineShowTime(uid, msg.getMsgTime()));
                successed = appNetService.handleSendPicutre(url, false, uid, msg.getMessageId());
                if(!successed)
                {
                    msg.setSend(0);
                }
                FinalUserDataBase.getInstance().saveChatMsg(msg, uid, userName, avatarUrl);
            }
            else{
                createImageChatMsg(uid, url, userName, avatarUrl, BitmapUtils.BitmapToBase64String(bmp), isGroup, isSend);
            }
        }
        if (bmp != null && !bmp.isRecycled()) {
            bmp.recycle();
        }
        if (bmpUpload != null && !bmpUpload.isRecycled()) {
            bmpUpload.recycle();
        }
    }
    /**
     * Create photo chat entity class
     */
    private ChatMsg createImageChatMsg(String uid, String content, String uName, String avatarUrl, String cover, boolean isGroup, boolean isSend) {

        ChatMsg chatMsg = new ChatMsg();
        chatMsg.setType(1);
        chatMsg.setContent(content);//
        chatMsg.setLocalUrl(content);
        chatMsg.setCover(cover);
        chatMsg.parseUserBaseVo(NextApplication.myInfo.getUserBaseVo());
        String jid = uid + "@" + XmppUtils.SERVER_NAME;
        if (isGroup) {
            jid = uid.replace("group-", "") + "@group." + XmppUtils.SERVER_NAME;
        }


        org.jivesoftware.smack.packet.Message msg = new org.jivesoftware.smack.packet.Message(jid, org.jivesoftware.smack.packet.Message.Type.chat);
        chatMsg.setMessageId(msg.getPacketID());
        if (isGroup) {//
            msg.setMsgtype(MsgType.groupchat);
            chatMsg.setChatId(uid);
            chatMsg.setGroup(isGroup);
            chatMsg.setSource(NextApplication.mContext.getString(R.string.app_name));
            chatMsg.setUserfrom(getString(R.string.app_name));
            chatMsg.setUsersource(XmppUtils.SERVER_NAME);
            msg.setBody(chatMsg.toGroupChatJsonObject());
        } else {
            chatMsg.setSource(getString(R.string.app_name));
            msg.setBody(chatMsg.toChatJsonObject());

        }
        chatMsg.setMsgTime(System.currentTimeMillis() / 1000);
        if (isSend) {
            chatMsg.setSend(2);
        }
        FinalUserDataBase.getInstance().saveChatMsg(chatMsg, uid, uName, avatarUrl);
        chatMsg.parseUserBaseVo(NextApplication.myInfo.getUserBaseVo());
        if (isSend) {
            Bundle bundle = new Bundle();
            bundle.putInt("type", 2);
            bundle.putString("uid", uid);
            bundle.putString("username", uName);
            bundle.putString("avatarurl", avatarUrl);
            bundle.putSerializable("chatmsg", chatMsg);
            Utils.intentService(
                    this,
                    LoadDataService.class,
                    LoadDataService.ACTION_FILE_UPLOAD_CHAT,
                    LoadDataService.ACTION_FILE_UPLOAD_CHAT,
                    bundle);
        }
        return chatMsg;
    }
    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.contact_discuss_linear:// Group chat
                Intent intentDiscuss = new Intent(this, DiscussGroupListUI.class);
                intentDiscuss.putExtra("single", true);
                startActivityForResult(intentDiscuss, 10);
                Utils.openNewActivityAnim(this, false);
                break;
            case R.id.contact_friends_linear:// contact
                Intent intent = new Intent(this, SelectContactUI.class);
                startActivityForResult(intent, 0);
                Utils.openNewActivityAnim(this, false);
                break;
        }
    }
    private boolean cantTrans(String uid) {
       String[] uids = uid.split("@");
       boolean found = false;
       if (uids.length > 1) {
           for (int i = 0; i < msgList.size(); i++) {

               int type = msgList.get(i).getType();
               String url = msgList.get(i).getShareUrl();
               if (type == 5 || type == 7 || type == 8 || type == 9 || type == 100 || type == 101 || type == 102 || type == 104 || type == 106 || type == 107 || type == 1008 || type == 1010 || (type == 103 && !TextUtils.isEmpty(url) && (url.contains("/vodInfo.html") || (url.contains("/share_vod.html")))))//场景，帮问，红包，直播消息，礼包，道具，洒金豆不支持跨域名转发
               {
                   found = true;
                   break;
               }
           }
           if (found) {
               showToast(getString(R.string.transpond_warning));
           }
       }
        else if(uid.equals("everyone")){
           showToast(getString(R.string.transpond_warning_everyone));
       }
       return  found;
  }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == 0 || requestCode == 1000) && resultCode == RESULT_OK) {
            ArrayList<UserBaseVo> selectList = (ArrayList<UserBaseVo>) data.getSerializableExtra("selectList");
            if (selectList == null || selectList.isEmpty()) {
               return;
            }
            UserBaseVo userBaseVo = selectList.get(0);
            showDialogFormat(userBaseVo.getLocalId(),userBaseVo.getUsername(),userBaseVo.getThumb(),userBaseVo.getGender(),false,selectList);
        } else if (requestCode == 10 && resultCode == RESULT_OK) {

            final String uid = data.getStringExtra("uid");
            final String avatarUrl = data.getStringExtra("avatarurl");
            final String username = data.getStringExtra("username");
            final String gender = data.getStringExtra("gender");
            final boolean isNewGroup = data.getBooleanExtra("isNewGroup", false);
//            if(cantTrans(uid)){
//                return;
//            }
            ArrayList<UserBaseVo> members = new ArrayList<>();
            members = (ArrayList<UserBaseVo>) data.getSerializableExtra("member");
            showDialogFormat(uid,username,avatarUrl,gender,isNewGroup,members);
        } else if (requestCode == 100 && resultCode == RESULT_OK) {

            final String gid = data.getStringExtra("gid");
            final String avatarUrl = data.getStringExtra("avatarurl");
            final String username = data.getStringExtra("groupName");
            final String gender = data.getStringExtra("gender");
            final boolean isNewGroup = data.getBooleanExtra("isNewGroup", false);
//            if(cantTrans(gid)){
//                return;
//            }
            List<UserBaseVo> members = new ArrayList<>();
            UserBaseVo vo = new UserBaseVo();
            vo.setThumb(avatarUrl);
            members.add(vo);
            showDialogFormat(gid,username,avatarUrl,gender,isNewGroup,members);

        }
    }

    /**
     * Forwarding operation box
     * */
    private void showDialogFormat(final String chatId, final String username, final String avatarUrl, final String gender, final boolean isNewGroup, List<UserBaseVo> members){
        MyViewDialogFragment mdf = new MyViewDialogFragment(MyViewDialogFragment.DIALOG_FORWOED, chatId,getString(R.string.chat_send_to),username,linkTitle,members);
        FragmentManager manager = getSupportFragmentManager();
        mdf.setLeaveMsgCallBack(new MyViewDialogFragment.LeaveMsgCallBack() {
            @Override
            public void leaveMsgMethod(String content) {
                if (msgList != null && msgList.size() > 0) {
                    if (!TextUtils.isEmpty(content)) {
                        ChatMsg chatmsg = new ChatMsg();
                        chatmsg.setType(0);
                        chatmsg.setContent(content);
                        chatmsg.setCreateTime(System.currentTimeMillis() / 1000);
                        chatmsg.parseUserBaseVo(NextApplication.myInfo);
                        msgList.add(chatmsg);
                    }
                    new SendMsgThread(ContactSelectedUI.this, msgList, chatId, username, avatarUrl, gender, false, false, isNewGroup);
                }
            }
        });
        if (manager != null && !isFinishing()) {
            try {
                mdf.show(manager, "mdf");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        int openSmartMesh = MySharedPrefs.readInt1(NextApplication.mContext,MySharedPrefs.FILE_USER,MySharedPrefs.KEY_NO_NETWORK_COMMUNICATION + NextApplication.myInfo.getLocalId());
        if (openSmartMesh == 1 && serviceConn != null){
            unbindService(serviceConn);
        }

    }
}
