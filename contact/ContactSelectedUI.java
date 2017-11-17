package com.lingtuan.firefly.contact;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
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
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MyViewDialogFragment;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.vo.ChatMsg;
import com.lingtuan.firefly.vo.UserBaseVo;
import com.lingtuan.firefly.xmpp.XmppMessageUtil;

import org.jivesoftware.smack.packet.Message.MsgType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Forward messages page
 */
public class ContactSelectedUI extends BaseActivity implements OnItemClickListener {
    private LinearLayout discussLinear;

    private ListView mListView;

    private MessageEventAdapter mAdapter;

    private ArrayList<ChatMsg> msgList;
    private View headerView;
    private boolean needClose;
    private String linkTitle = null;

    @Override
    protected void setContentView() {
        setContentView(R.layout.contact_selected);
    }

    @Override
    protected void findViewById() {
        mListView = (ListView) findViewById(R.id.chatting_list);
        headerView = LayoutInflater.from(this).inflate(R.layout.contact_selected_header, null, false);
        discussLinear = (LinearLayout) headerView.findViewById(R.id.contact_discuss_linear);
    }

    @Override
    protected void setListener() {
        mListView.setOnItemClickListener(this);
        discussLinear.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.chatting_selected_title));

        if (getIntent() != null) {
            msgList = (ArrayList<ChatMsg>) getIntent().getSerializableExtra("msglist");
            needClose = getIntent().getBooleanExtra("needClose", false);
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
        if (position == 0) {
            return;
        }
        final ChatMsg msg = mAdapter.getItem(position - 1);
        if(cantTrans(msg.getChatId())){
            return;
        }
        if (msg.isSystem() || TextUtils.equals("system-2", msg.getChatId())) {//friends of friends, or friends
            List<UserBaseVo> members = msg.getMemberAvatarUserBaseList();
            if (members == null || members.size() == 0) {
                members = new ArrayList<>();
                UserBaseVo vo = new UserBaseVo();
                vo.setGender(msg.getGender() + "");
                vo.setThumb(msg.getUserImage());
                members.add(vo);
            }
            showDialogFormat(msg.getChatId(),msg.getUsername(),msg.getUserImage(),msg.getGender() + "", MsgType.super_groupchat.equals(msg.getMsgType()),members);
        }
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
        String remoteSource;
        Handler handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (dialog != null) {
                    dialog.dismiss();
                    dialog = null;
                }
                if (needClose) {
                    showToast(getResources().getString(R.string.send_ok));
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Utils.intentChattingUI(ContactSelectedUI.this, chatId, avatarUrl,uName, "1",0, chatId.startsWith("group-"), isDismiss, isKick, 0, true);
                }
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
                        if (!chatmsg.getUserId().equals(NextApplication.myInfo.getLocalId())) {
                            chatmsg.setSend(1);
                        }

                        XmppMessageUtil.getInstance().forwarding(chatmsg, chatmsg.getChatId(), uName, avatarUrl,!(isDismiss || isKick), isNewGroup);
                        SystemClock.sleep(1100);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            handler.sendEmptyMessage(0);
        }

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
            final String uid = data.getStringExtra("uid");
            final String avatarUrl = data.getStringExtra("avatarurl");
            final String username = data.getStringExtra("username");
            final String gender = data.getStringExtra("gender");
            final boolean isNewGroup = data.getBooleanExtra("isNewGroup", false);
            final String remoteSource = data.getStringExtra("remoteSource");
            if(cantTrans(uid)){
                return;
            }
            List<UserBaseVo> members = new ArrayList<>();
            UserBaseVo vo = new UserBaseVo();
            vo.setGender(gender);
            vo.setThumb(avatarUrl);
            members.add(vo);
            showDialogFormat(uid,username,avatarUrl,gender,isNewGroup,members);
        } else if (requestCode == 10 && resultCode == RESULT_OK) {

            final String uid = data.getStringExtra("uid");
            final String avatarUrl = data.getStringExtra("avatarurl");
            final String username = data.getStringExtra("username");
            final String gender = data.getStringExtra("gender");
            final boolean isNewGroup = data.getBooleanExtra("isNewGroup", false);
            final String remoteSource = data.getStringExtra("remoteSource");
            if(cantTrans(uid)){
                return;
            }
            ArrayList<UserBaseVo> members = new ArrayList<>();
            members = (ArrayList<UserBaseVo>) data.getSerializableExtra("member");
            showDialogFormat(uid,username,avatarUrl,gender,isNewGroup,members);
        } else if (requestCode == 100 && resultCode == RESULT_OK) {

            final String gid = data.getStringExtra("gid");
            final String avatarUrl = data.getStringExtra("avatarurl");
            final String username = data.getStringExtra("groupName");
            final String gender = data.getStringExtra("gender");
            final boolean isNewGroup = data.getBooleanExtra("isNewGroup", false);
            final String remoteSource = data.getStringExtra("remoteSource");
            if(cantTrans(gid)){
                return;
            }
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
        MyViewDialogFragment mdf = new MyViewDialogFragment(MyViewDialogFragment.DIALOG_FORWOED, getString(R.string.chat_send_to),username,linkTitle,members);
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
}
