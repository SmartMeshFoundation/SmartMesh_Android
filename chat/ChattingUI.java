package com.lingtuan.firefly.chat;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewStub;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.chat.adapter.ChatAdapter;
import com.lingtuan.firefly.contact.ContactSelectedUI;
import com.lingtuan.firefly.contact.DiscussGroupSettingUI;
import com.lingtuan.firefly.contact.SelectGroupMemberListUI;
import com.lingtuan.firefly.custom.LoadMoreListView;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.listener.ChattingItemCallBack;
import com.lingtuan.firefly.offline.AppNetService;
import com.lingtuan.firefly.offline.vo.WifiPeopleVO;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.MyToast;
import com.lingtuan.firefly.util.MyViewDialogFragment;
import com.lingtuan.firefly.util.SDCardCtrl;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.vo.ChatMsg;
import com.lingtuan.firefly.xmpp.XmppAction;
import com.lingtuan.firefly.xmpp.XmppMessageUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The chat page
 */
public class ChattingUI extends BaseActivity implements TextWatcher, ChatAdapter.SceneListener, OnRefreshListener, OnGlobalLayoutListener,ChattingItemCallBack {


    private EditText mInputContent;//A chat
    private ImageView mAudioBtn;//Voice button
    private ImageView mFaceBtn;//Expression button
    private TextView mSendBtn;//Send button
    private ImageView mPhoto;//Select the album button
    private ImageView mCamera;//Taking pictures
    private ImageView mCard;//Business card button
    private ImageView mFile;//File button
    private View mFaceView;   //Expression button
    private ViewStub mFaceStub;
    private View mAudioView;//Voice button
    private ViewStub mAudioStub;
    private View stubBottomBg;//At the bottom of the view control expression and voice switch not flashing

    private ImageView chattingSet = null;  //Stranger Settings button
    private ImageView deleteTV;//Forward the delete button
    private TextView mLeftSelected;

    /**
     * Delete or forwarding     true is delete
     */
    private boolean isSceneDelete;

    //Chat list related
    private ListView listView;
    private ChatAdapter mAdapter;
    private int listViewHeight = 0;

    /**
     * Refresh the controls
     */
    private SwipeRefreshLayout swipeLayout;

    //Radio chat page
    private MsgReceiverListener msgReceiverListener;

    //The user name
    private String userName;
    //Head portrait
    private String avatarUrl;
    //gender
    private String gender;
    //uid
    private String uid;

    //The chat box at the bottom of the layout
    private LinearLayout chattingBottomRela;
    private RelativeLayout chattingBottomLayout;

    //Chat management class
    private ChattingManager chattingManager;

    private boolean isGroup = false;//Whether the group chat
    private boolean isDismissGroup = false;//Whether has disbanded group chat
    private boolean isKickGroup = false;//Whether be T in addition to the group chat

    //Chat unread messages
    private TextView unreadNum;
    private int unread;
    private int unreadStartIndex;
    private int unreadOffset;
    private boolean hasHideunreadNum;

    private AppNetService appNetService;
    @Override
    protected void setContentView() {
        setContentView(R.layout.chatting);
        userName = getIntent().getExtras().getString("username");
        avatarUrl = getIntent().getExtras().getString("avatarurl");
        gender = getIntent().getExtras().getString("gender");
        uid = getIntent().getExtras().getString("uid");
        isGroup = getIntent().getExtras().getBoolean("isgroup");
        isDismissGroup = getIntent().getExtras().getBoolean("dismissgroup", false);
        isKickGroup = getIntent().getExtras().getBoolean("kickgroup", false);
        unread = getIntent().getExtras().getInt("unreadNum");
        if (TextUtils.isEmpty(uid)) {
            Utils.exitActivityAndBackAnim(this, true);
            return;
        }
        listView = (LoadMoreListView) findViewById(R.id.refreshListView);
    }

    @Override
    protected void findViewById() {

        unreadNum = (TextView) findViewById(R.id.unreadNum);
        mInputContent = (EditText) findViewById(R.id.chatting_bottom_input);
        mAudioBtn = (ImageView) findViewById(R.id.chatting_bottom_audio);
        mPhoto = (ImageView) findViewById(R.id.chatting_bottom_photo);
        mCamera = (ImageView) findViewById(R.id.chatting_bottom_camera);
        mCard = (ImageView) findViewById(R.id.chatting_bottom_card);
        mFile = (ImageView) findViewById(R.id.chatting_bottom_file);

        mFaceBtn = (ImageView) findViewById(R.id.chatting_bottom_face);
        mSendBtn = (TextView) findViewById(R.id.chatting_bottom_send);
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);

        mFaceStub = (ViewStub) findViewById(R.id.include_chatting_face_stub);
        mAudioStub = (ViewStub) findViewById(R.id.include_chatting_audio_stub);
        stubBottomBg = findViewById(R.id.stubBottomBg);


        chattingSet = (ImageView) findViewById(R.id.app_right);
        deleteTV = (ImageView) findViewById(R.id.detail_set);
        mLeftSelected = (TextView) findViewById(R.id.app_title_left);
        chattingBottomRela = (LinearLayout) findViewById(R.id.include_chatting_bottomlinear);
        chattingBottomLayout = (RelativeLayout) findViewById(R.id.chatting_bottom_rela);
        chattingBottomLayout.setVisibility(View.VISIBLE);
    }

    @Override
    protected void setListener() {
        chattingSet.setOnClickListener(this);
        deleteTV.setOnClickListener(this);
        mAudioBtn.setOnClickListener(this);
        mPhoto.setOnClickListener(this);
        mCamera.setOnClickListener(this);
        mCard.setOnClickListener(this);
        mFile.setOnClickListener(this);
        mFaceBtn.setOnClickListener(this);
        mSendBtn.setOnClickListener(this);
        unreadNum.setOnClickListener(this);
        swipeLayout.setOnRefreshListener(this);
        listView.getViewTreeObserver().addOnGlobalLayoutListener(this);
        mInputContent.addTextChangedListener(this);

        mInputContent.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEvent.ACTION_DOWN == event.getAction()) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setViewGone();
                        }
                    }, 100);
                }
                return false;
            }
        });
        listView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getX() > Utils.dip2px(ChattingUI.this, 50)) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setViewGone();
                            Utils.hiddenKeyBoard(ChattingUI.this);
                        }
                    }, 100);
                }
                return false;
            }
        });



    }


    // The Activity and netService2 connection
    private ServiceConnection serviceConn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            // Binding service success
            AppNetService.NetServiceBinder binder = (AppNetService.NetServiceBinder) service;
            appNetService = binder.getService();
            chattingManager.setAppNetService(appNetService);
        }
        public void onServiceDisconnected(ComponentName name) {
        }
    };
    @Override
    protected void initData() {

        if (uid.equals("everyone")){
            setTitle(getString(R.string.everyone));
            chattingSet.setVisibility(View.GONE);
        }else{
            setTitle(userName);
            chattingSet.setImageResource(R.drawable.icon_friend_info);
            chattingSet.setVisibility(View.VISIBLE);
        }

        if (uid.equals("everyone") || isGroup){
            mFile.setVisibility(View.GONE);
        }else{
            mFile.setVisibility(View.VISIBLE);
        }

        swipeLayout.setColorSchemeResources(R.color.black);
        //Remove notification bar
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        ChattingManager.getInstance(this).destory();
        chattingManager = ChattingManager.getInstance(this);

        bindService(new Intent(this, AppNetService.class), serviceConn,BIND_AUTO_CREATE);
        IntentFilter filter = new IntentFilter(XmppAction.ACTION_MESSAGE_LISTENER);//message broadcast distribution
        filter.addAction(XmppAction.ACTION_OFFLINE_MESSAGE_LIST_LISTENER);//message list broadcast distribution
        filter.addAction(XmppAction.ACTION_MESSAGE_UPDATE_LISTENER);//message update broadcast distribution
        filter.addAction(XmppAction.ACTION_MESSAGE_GROUP_KICK_LISTENER);//message update kick
        filter.addAction(XmppAction.ACTION_MESSAGE_IMAGE_PERCENT);//message sending pictures percentage
        filter.addAction(Constants.ACTION_CHATTING_FRIEND_NOTE);//More pictures to send
        filter.addAction(XmppAction.ACTION_ENTER_EVERYONE_LISTENER);//More pictures to send

        //chat
        filter.addAction(Constants.MSG_REPORT_SEND_MSG_RESULT);
        filter.addAction(Constants.MSG_REPORT_SEND_MSG_PROGRESS);
        filter.addAction(Constants.MSG_REPORT_START_RECV_FILE);
        filter.addAction(Constants.MSG_REPORT_CANCEL_SEND_FILE);
        filter.addAction(Constants.MSG_REPORT_CANCEL_RECV_FILE);
        msgReceiverListener = new MsgReceiverListener();
        registerReceiver(msgReceiverListener, filter);

        //Choose picture sends the message
        IntentFilter filterVideo = new IntentFilter(Constants.ACTION_CHATTING_PHOTO_LIST);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filterVideo);
        XmppMessageUtil.getInstance().setGroupDismiss(isDismissGroup);
        XmppMessageUtil.getInstance().setGroupKick(isKickGroup);

        chattingManager.setGroup(isGroup);
        chattingManager.setSend(!(isDismissGroup || isKickGroup));

        List<ChatMsg> mList = FinalUserDataBase.getInstance().getChatMsgListByChatId(uid, 0, 20);
        for (int i = 0; i < mList.size(); i++) {
            ChatMsg vo = mList.get(i);
            if (vo.getType() == 1010 && vo.getUnread() == 1) {
                vo.setUnread(0);
                FinalUserDataBase.getInstance().updateChatMsgUrneadBymessageId(vo.getMessageId());
            }
        }
        unreadStartIndex = (mList.size() - unread) > 0 ? (mList.size() - unread) : 0;
        if (unread > mList.size()) {
            unreadOffset = unread - mList.size();
        }
        if (unread > 0) {
            showunreadNum();
        }

        for (ChatMsg msg : mList) {
            if (msg.getSend() == 2)//Is sending images, voice, video because of the need to upload, is if it is sent every time into the state, send again
            {
                if (msg.getType() == 1)//The picture
                {
                    XmppMessageUtil.getInstance().reSend(3, msg);
                } else if (msg.getType() == 2)//voice
                {
                    XmppMessageUtil.getInstance().reSend(11, msg);
                }
            }
        }
        mAdapter = new ChatAdapter(mList, this, this, listView);
        int source = 1;
        String sourcid = uid;
        if (isGroup) {
            source = 2;
            sourcid = uid.replace("group-", "");
        }
        mAdapter.setChatType(source, sourcid);
        mAdapter.setInputEditText(mInputContent, chattingManager);
        mAdapter.setGroup(isGroup);
        mAdapter.setKickDismiss(isKickGroup, isDismissGroup);
        chattingManager.setUserInfo(userName, avatarUrl, uid, mAdapter, listView);
        listView.setAdapter(mAdapter);
        listView.setSelection(mList.size());

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finishTempActivity();
            }
        }, 1000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isDismissGroup || isKickGroup) {
                    showToast(isDismissGroup ? getString(R.string.discuss_group_dismiss) : getString(R.string.discuss_group_kick));
                }
            }
        }, 500);

    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences = getSharedPreferences("chatuid", Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putString("chatuid", uid);
        editor.commit();

        if (ChattingManager.getInstance(this).isFinish()) {
            ChattingManager.getInstance(this).setFinish(false);
            Utils.exitActivityAndBackAnim(this, true);
        }
    }


    private boolean setViewGone() {
        boolean result = false;
        if (mFaceView != null) {
            if (mFaceView.getVisibility() == View.VISIBLE){
                result = true;
            }
            mFaceView.setVisibility(View.GONE);
        }
        if (mAudioView != null) {
            if (mAudioView.getVisibility() == View.VISIBLE){
                result = true;
            }
            mAudioView.setVisibility(View.GONE);
        }
        if (stubBottomBg != null) {
            if (stubBottomBg.getVisibility() == View.VISIBLE){
                result = true;
            }
            stubBottomBg.setVisibility(View.GONE);
        }
        return result;
    }

    private void showContentInput() {
        mInputContent.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        setViewGone();
        SharedPreferences preferences = getSharedPreferences("chatuid", Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.remove("chatuid");
        editor.commit();
        FaceUtils.getInstance(this).destory();
        FinalUserDataBase.getInstance().updateChatEventMsgGender(uid, gender);
        FinalUserDataBase.getInstance().updateUnreadEventChat(uid);
        FinalUserDataBase.getInstance().updateAtGroupMe(uid);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConn);
        if(uid.equals("everyone"))
        {
            XmppMessageUtil.getInstance().sendEnterLeaveEveryOne(0,true);
        }
        try {
            unregisterReceiver(msgReceiverListener);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
            mAdapter.destory();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            listView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
        } else {
            listView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }


    }

    @Override
    public void onBackPressed() {
        if (setViewGone()) {
            return;
        }
        if (!chattingBottomRela.isShown()) {
            rollbackScene();
            return;
        }
        super.onBackPressed();
    }

    private void showunreadNum() {
        unreadNum.setText(getString(R.string.chat_unread_num,unread));
        if (unread > 0) {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    RelativeLayout.LayoutParams lp = (android.widget.RelativeLayout.LayoutParams) unreadNum.getLayoutParams();
                    final int width = unreadNum.getWidth();
                    lp.setMargins(0, Utils.dip2px(ChattingUI.this, 50), -width, 0);
                    unreadNum.requestLayout();
                    if (unread > 0 && listView.getFirstVisiblePosition() > unreadStartIndex + 1) {
                        unreadNum.setVisibility(View.VISIBLE);
                        Animation transanimIn = new Animation() {
                            @Override
                            protected void applyTransformation(float interpolatedTime,Transformation t) {
                                {
                                    RelativeLayout.LayoutParams lp = (android.widget.RelativeLayout.LayoutParams) unreadNum.getLayoutParams();
                                    lp.setMargins(0, Utils.dip2px(ChattingUI.this, 50), -width + (int) (width * interpolatedTime), 0);
                                    unreadNum.requestLayout();
                                }
                            }

                            @Override
                            public boolean willChangeBounds() {
                                return true;
                            }

                        };
                        transanimIn.setDuration(300);
                        unreadNum.startAnimation(transanimIn);
                        listView.setOnScrollListener(new AbsListView.OnScrollListener() {

                            @Override
                            public void onScrollStateChanged(AbsListView view, int scrollState) {

                            }

                            @Override
                            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                                if (!hasHideunreadNum && listView.getFirstVisiblePosition() <= unreadStartIndex + 1) {
                                    hideunreadNum(false);
                                }
                            }
                        });
                    } else {
                        hasHideunreadNum = true;
                    }
                }
            }, 500);
        }
    }

    private void hideunreadNum(final boolean gotoTop) {
        hasHideunreadNum = true;
        if (gotoTop) {
            if (unreadOffset > 0) {
                List<ChatMsg> mList = FinalUserDataBase.getInstance().getChatMsgListByChatId(uid, 0, unreadOffset + mAdapter.getCount());
                for (int i = 0; i < mList.size(); i++) {
                    ChatMsg vo = mList.get(i);
                    if (vo.getType() == 1010 && vo.getUnread() == 1) {
                        vo.setUnread(0);
                        FinalUserDataBase.getInstance().updateChatMsgUrneadBymessageId(vo.getMessageId());
                    }
                }
                mAdapter.resetSource(mList);
            }

            ChatMsg msg = new ChatMsg();
            msg.setType(12);
            msg.setContent(getString(R.string.chatting_new_message));
            mAdapter.insertSystemChatMsg(unreadStartIndex, msg);
            listView.setSelection(unreadStartIndex + 1);
        }

        final int width = unreadNum.getWidth();
        Animation transanimOut = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                {
                    RelativeLayout.LayoutParams lp = (android.widget.RelativeLayout.LayoutParams) unreadNum.getLayoutParams();
                    lp.setMargins(0, Utils.dip2px(ChattingUI.this, 50), -(int) (width * interpolatedTime), 0);
                    unreadNum.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }

        };
        transanimOut.setDuration(300);
        transanimOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                unreadNum.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        unreadNum.startAnimation(transanimOut);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.unreadNum:
                hideunreadNum(true);
                break;
            case R.id.app_right:
                settingMethod();
                break;
            case R.id.detail_set:
                deleteMethod();
                break;
            case R.id.chatting_bottom_send:
                sentTextMethod();
                break;
            case R.id.chatting_bottom_audio:
                sendAudioMethod();
                break;
            case R.id.chatting_bottom_photo:
                sendPhotoMethod();
                break;
            case R.id.chatting_bottom_camera:
                sendCameraMethod();
                break;
            case R.id.chatting_bottom_card:
                sendCardMethod();
                break;
            case R.id.chatting_bottom_file:
                sendFileMethod();
                break;
            case R.id.chatting_bottom_face:
                sendFaceMethod();
                break;
        }
    }

    /**
     * Send the file method
     * */
    private void sendFileMethod() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                chattingManager.showFileView();
            }
        }, 100);
        setViewGone();
        Utils.hiddenKeyBoard(this);
    }

    /**
     * Sending card method
     * */
    private void sendCardMethod() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                chattingManager.showCardView();
            }
        }, 100);
        setViewGone();
        Utils.hiddenKeyBoard(this);
    }

    /**
     * Camera photo sending pictures method
     * */
    private void sendCameraMethod() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                chattingManager.showCameraView();
            }
        }, 100);
        setViewGone();
        Utils.hiddenKeyBoard(this);
    }

    /**
     * Sending pictures method
     * */
    private void sendPhotoMethod() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                chattingManager.showPhotoView();
            }
        }, 100);
        setViewGone();
        Utils.hiddenKeyBoard(this);
    }

    /**
     * Send expression method
     * */
    private void sendFaceMethod() {
        showContentInput();
        Utils.hiddenKeyBoard(this);
        if (mAudioView != null) {
            mAudioView.setVisibility(View.GONE);
        }
        if (mFaceView == null) {
            mFaceStub.setLayoutResource(R.layout.include_face_layout);
            mFaceView = mFaceStub.inflate();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                chattingManager.showFaceView(mFaceView, mInputContent,stubBottomBg);
            }
        }, 100);
    }

    /**
     * Send voice method
     * */
    private void sendAudioMethod() {
        Utils.hiddenKeyBoard(this);
        if (mFaceView != null) {
            mFaceView.setVisibility(View.GONE);
        }
        if (mAudioView == null) {
            mAudioStub.setLayoutResource(R.layout.include_audio_layout);
            mAudioView = mAudioStub.inflate();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                chattingManager.showAudioView(mAudioView,stubBottomBg);
            }
        }, 100);
    }

    /**
     * Forward delete methods
     * */
    private void deleteMethod() {
        if (mAdapter.getSelectedList() == null || mAdapter.getSelectedList().size() == 0) {
            showToast(getString(R.string.chat_file_empty));
            return;
        }
        int titleIds = isSceneDelete ? R.string.chatting_selected_delete : R.string.chatting_selected_relay;
        if (isSceneDelete) {//delete
            MyViewDialogFragment mdf = new MyViewDialogFragment();
            mdf.setTitleAndContentText(getString(titleIds), null);
            mdf.setOkCallback(new MyViewDialogFragment.OkCallback() {

                @Override
                public void okBtn() {
                    if (isSceneDelete) {//Delete
                        Map<String, ChatMsg> selectedList = mAdapter.getSelectedList();
                        ChatMsg msg = null;
                        if (!selectedList.isEmpty()) {
                            for (Map.Entry<String, ChatMsg> s : selectedList.entrySet()) {
                                FinalUserDataBase.getInstance().deleteChatMsgBymessageId(s.getValue().getMessageId());
                                if (msg != null && s.getValue().getMsgTime() > msg.getMsgTime()) {
                                    continue;
                                }
                                msg = s.getValue();
                            }
                        }
                        mAdapter.removeSelectList();
                        if (msg == null) {
                            return;
                        }
                        if (msg != null) {
                            msg.setUsername(userName);
                            msg.setUserImage(avatarUrl);
                        }
                        FinalUserDataBase.getInstance().updateChatEventContent(uid, msg);
                        MyToast.showToast(ChattingUI.this, getString(R.string.delete_success));
                    } else {//Forwarding

                    }
                    rollbackScene();
                }
            });
            mdf.show(getSupportFragmentManager(), "mdf");

        } else {//Forwarding
            ArrayList<ChatMsg> list = new ArrayList<>();
            for (Map.Entry<String, ChatMsg> s : mAdapter.getSelectedList().entrySet()) {
                list.add(s.getValue());
            }
            Intent intent = new Intent(this, ContactSelectedUI.class);
            intent.putExtra("msglist", list);
            startActivity(intent);
            Utils.openNewActivityAnim(this, false);
        }
    }

    /**
     * Set method
     * */
    private void settingMethod() {
        if (isGroup) {
            if (isDismissGroup || isKickGroup) {
                showToast(isDismissGroup ? getString(R.string.discuss_group_dismiss) : getString(R.string.discuss_group_kick));
                return;
            }
            Intent i = new Intent(this, DiscussGroupSettingUI.class);
            i.putExtra("cid", Integer.parseInt(uid.replace("group-", "")));
            i.putExtra("nickname", userName);
            i.putExtra("avatarurl", avatarUrl);
            startActivity(i);
            Utils.openNewActivityAnim(this, false);
        } else {
            Intent intent = new Intent(this, ChattingSetUI.class);
            intent.putExtra("avatarurl", avatarUrl);
            intent.putExtra("username", userName);
            intent.putExtra("gender", gender);
            intent.putExtra("isgroup", isGroup);
            intent.putExtra("dismissgroup", isDismissGroup);
            intent.putExtra("kickgroup", isKickGroup);
            intent.putExtra("uid", uid);
            startActivity(intent);
            Utils.openNewActivityAnim(this, false);
        }
    }

    /**
     * Send text
     * */
    private void sentTextMethod() {
        final String content = mInputContent.getText().toString().trim();
        String atIds = chattingManager.getSbAtGroupSelectIds();

        Editable mEditable = mInputContent.getText();
        boolean isAtAll = chattingManager.isAtAll(mEditable.toString());
        boolean successed = true;
        if(uid.equals("everyone")){
            ChatMsg msg = XmppMessageUtil.getInstance().sendText(uid, content, isAtAll, atIds, userName, avatarUrl, isGroup, !(isDismissGroup || isKickGroup));
            successed = appNetService.handleSendString(content, true, "", msg.getMessageId());
            if(msg.getSend() ==0 && !successed)
            {
                msg.setSend(0);
            }
            mAdapter.addChatMsg(msg, true);
        }else if(isGroup){
            ChatMsg msg = XmppMessageUtil.getInstance().sendText(uid, content, isAtAll, atIds, userName, avatarUrl, isGroup, !(isDismissGroup || isKickGroup));
            mAdapter.addChatMsg(msg, true);
        }
        else{
            boolean foundPeople  = false;
            if(appNetService!=null && appNetService.getwifiPeopleList()!=null)
            {
                for (WifiPeopleVO vo : appNetService.getwifiPeopleList())//All users need to traverse, find out the corresponding touid users
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
                msg.parseUserBaseVo(NextApplication.myInfo.getUserBaseVo());
                msg.setChatId(uid);
                msg.setType(0);
                msg.setSend(1);
                msg.setMsgTime(System.currentTimeMillis() / 1000);
                msg.setShowTime(FinalUserDataBase.getInstance().isOffLineShowTime(uid, msg.getMsgTime()));
                msg.setContent(content);
                msg.setOffLineMsg(true);
                msg.setMessageId(UUID.randomUUID().toString());
                successed = appNetService.handleSendString(content, false, uid, msg.getMessageId());
                if(!successed)
                {
                    msg.setSend(0);
                }
                mAdapter.addChatMsg(msg, true);
                File recvFile = new File(SDCardCtrl.getOfflinePath()+File.separator + uid + ".jpg");
                String imageAvatar = "file://"+recvFile.getAbsolutePath();
                FinalUserDataBase.getInstance().saveChatMsg(msg,uid, isGroup ? "offline" : userName,imageAvatar);
            }
            else{
                ChatMsg msg = XmppMessageUtil.getInstance().sendText(uid, content, isAtAll, atIds, userName, avatarUrl, isGroup, !(isDismissGroup || isKickGroup));
                mAdapter.addChatMsg(msg, true);
            }
        }
        mInputContent.setText("");
        listView.setSelection(mAdapter.getCount() - 1);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
       if (isGroup && count == 1 && s != null) {//Is chatting to enter
            try {
                if (s.toString().substring(start, start + 1).equals("@")) {//输入@
                    if (AtGroupParser.getInstance() != null) {
                        String ids = AtGroupParser.getInstance().parser(s);
                        chattingManager.updateAtGroupIds(ids);
                    }
                    Intent intent = new Intent(this, SelectGroupMemberListUI.class);
                    intent.putExtra("cid", Integer.parseInt(uid.replace("group-", "")));
                    startActivityForResult(intent, Constants.REQUEST_SELECT_GROUP_MEMBER);
                    Utils.openNewActivityAnim(this, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (before != 0 && AtGroupParser.getInstance() != null) {
            //Delete the word Data have been generated and @ function

            Editable mEditable = mInputContent.getText();
            if (before == 1) {//Delete one character at a time
                int selectIndex = mInputContent.getSelectionStart();
                if (selectIndex == 0) {
                    return;
                }
                String strSub = mEditable.toString().substring(0, selectIndex);
                for (int i = 0; i < AtGroupParser.getInstance().nicknames.length; i++) {
                    if (strSub.endsWith(AtGroupParser.getInstance().nicknames[i].trim())) {
                        mEditable.delete(selectIndex - AtGroupParser.getInstance().nicknames[i].trim().length() - 1, selectIndex);
                        break;
                    }
                }
            }
            String ids = AtGroupParser.getInstance().parser(mEditable.toString());
            chattingManager.updateAtGroupIds(ids);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (TextUtils.isEmpty(s) || TextUtils.isEmpty(s.toString().trim())) {
            mSendBtn.setEnabled(false);
        } else {
            mSendBtn.setEnabled(true);
        }
    }


    @Override
    public void onRefresh() {

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                List<ChatMsg> mTempList = FinalUserDataBase.getInstance().getChatMsgListByChatId(uid, mAdapter.getCount(), 20);
                for (int i = 0; i < mTempList.size(); i++) {
                    ChatMsg vo = mTempList.get(i);
                    if (vo.getType() == 1010 && vo.getUnread() == 1) {
                        vo.setUnread(0);
                        FinalUserDataBase.getInstance().updateChatMsgUrneadBymessageId(vo.getMessageId());
                    }
                }
                for (ChatMsg msg : mTempList) {
                    if (msg.getSend() == 2)//Is sending images, voice, video because of the need to upload, is if it is sent every time into the state, send again
                    {
                        if (msg.getType() == 1)//picture
                        {
                            XmppMessageUtil.getInstance().reSend(3, msg);
                        } else if (msg.getType() == 2)//voice
                        {
                            XmppMessageUtil.getInstance().reSend(11, msg);
                        }
                    }
                }
                final int position = mTempList.size();
                mTempList.addAll(mAdapter.getList());
                mAdapter.updateList(mTempList);
                swipeLayout.setRefreshing(false);
                listView.setSelection(position);
            }
        }, 1000);
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && Constants.ACTION_CHATTING_PHOTO_LIST.equals(intent.getAction())) {//Choose image correction
                chattingManager.onActivityResult(ChattingManager.ACTION_PHOTO_MORE_RESULT, Activity.RESULT_OK, intent);
            }
        }
    };

    @Override
    public void clickItem() {
    }

    class MsgReceiverListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && XmppAction.ACTION_MESSAGE_LISTENER.equals(intent.getAction())) {
                Bundle bundle = intent.getBundleExtra(XmppAction.ACTION_MESSAGE_LISTENER);

                ChatMsg chatMsg = (ChatMsg) bundle.getSerializable(XmppAction.ACTION_MESSAGE_LISTENER);


                if (chatMsg != null && uid.equals(chatMsg.getChatId())) {//I chat with the friends
                    if (chatMsg.getType() == 1010 && chatMsg.getUnread() == 1) {
                        chatMsg.setUnread(0);
                        FinalUserDataBase.getInstance().updateChatMsgUrneadBymessageId(chatMsg.getMessageId());
                    }

                    if (chatMsg.getChatId().startsWith("group-") && chatMsg.getType() == 17) {
                        setTitle(chatMsg.getGroupName());
                    }

                    //With a nickname
                    if (!TextUtils.isEmpty(chatMsg.getUserId())) {
                        chatMsg.setRealname(chatMsg.getUsername());
                        String note = MySharedPrefs.readString(ChattingUI.this, MySharedPrefs.KEY_FRIEND_NOTE + NextApplication.myInfo.getLocalId(), chatMsg.getUserId());
                        if (!TextUtils.isEmpty(note)) {
                            chatMsg.setUsername(note);
                        }
                    }


                    mAdapter.addChatMsg(chatMsg, true);
                    if (listView.getLastVisiblePosition() >= mAdapter.getCount() - 2)//Only in the bottom, to the new message to scroll to the bottom
                    {
                        listView.setSelection(mAdapter.getCount());
                    }
                }
            }
            else if (intent != null && XmppAction.ACTION_OFFLINE_MESSAGE_LIST_LISTENER.equals(intent.getAction())) {
                List<ChatMsg> list = (List<ChatMsg>) intent.getSerializableExtra("array");
                if (list != null) {
                    for (int i = 0; i < list.size(); i++) {
                        ChatMsg chatMsg = list.get(i);

                        if (chatMsg != null && uid.equals(chatMsg.getChatId())) {   //I chat with the friends
                            if (chatMsg.getType() == 1010 && chatMsg.getUnread() == 1) {
                                chatMsg.setUnread(0);
                                FinalUserDataBase.getInstance().updateChatMsgUrneadBymessageId(chatMsg.getMessageId());
                            }
                            //With a nickname
                            if (!TextUtils.isEmpty(chatMsg.getUserId())) {
                                chatMsg.setRealname(chatMsg.getUsername());
                                String note = MySharedPrefs.readString(ChattingUI.this, MySharedPrefs.KEY_FRIEND_NOTE + NextApplication.myInfo.getLocalId(), chatMsg.getUserId());
                                if (!TextUtils.isEmpty(note)) {
                                    chatMsg.setUsername(note);
                                }
                            }

                            mAdapter.addChatMsg(chatMsg, i == list.size() - 1);

                            if (i == list.size() - 1) {
                                if (listView.getLastVisiblePosition() >= mAdapter.getCount() - 1 - list.size())//Only in the bottom, to the new message to scroll to the bottom
                                {
                                    listView.setSelection(mAdapter.getCount());
                                }
                            }
                        }

                    }
                }
            } else if (intent != null && XmppAction.ACTION_MESSAGE_UPDATE_LISTENER.equals(intent.getAction())) {
                Bundle bundle = intent.getBundleExtra(XmppAction.ACTION_MESSAGE_UPDATE_LISTENER);
                ChatMsg chatMsg = (ChatMsg) bundle.getSerializable(XmppAction.ACTION_MESSAGE_UPDATE_LISTENER);
                if (chatMsg != null && uid.equals(chatMsg.getChatId())) {//I chat with the friends
                    mAdapter.updateChatMsg(chatMsg);
                }
            } else if (intent != null && XmppAction.ACTION_MESSAGE_GROUP_KICK_LISTENER.equals(intent.getAction())) {
                //T or dissolution of update group chat Someone quit the group chat
                Bundle bundle = intent.getBundleExtra(XmppAction.ACTION_MESSAGE_GROUP_KICK_LISTENER);
                if (uid.equals(bundle.getString("uid"))) {//Whether the current chat object
                    isDismissGroup = bundle.getBoolean("dismissgroup");
                    isKickGroup = bundle.getBoolean("kickgroup");
                    mAdapter.setKickDismiss(isKickGroup, isDismissGroup);
                    chattingManager.setSend(!(isDismissGroup || isKickGroup));
                    XmppMessageUtil.getInstance().setGroupDismiss(isDismissGroup);
                    XmppMessageUtil.getInstance().setGroupKick(isKickGroup);
                    if (isDismissGroup || isKickGroup) {
                        showToast(isDismissGroup ? getString(R.string.discuss_group_dismiss) : getString(R.string.discuss_group_kick));
                        return;
                    }
                }
            } else if (intent != null && XmppAction.ACTION_MESSAGE_IMAGE_PERCENT.equals(intent.getAction())) {
                // Image upload percentage
                String messageId = intent.getExtras().getString("time");
                int percent = intent.getExtras().getInt("percent");
                if (percent > 100) {
                    percent = 100;
                }
                View linear = listView.findViewWithTag(messageId);
                if (linear == null) {
                    return;
                }
                TextView percentTv = (TextView) linear.findViewById(R.id.item_chatting_image_upload_percent);
                percentTv.setText(getString(R.string.chatting_image_update_percent, percent) + "%");
                if (percent == 100) {
                } else {
                    linear.setVisibility(View.VISIBLE);
                }
            }else if (intent != null && Constants.ACTION_CHATTING_PHOTO_LIST.equals(intent.getAction())) {//Choose image correction

                chattingManager.onActivityResult(ChattingManager.ACTION_PHOTO_MORE_RESULT, Activity.RESULT_OK, intent);

            }else if (intent != null && Constants.ACTION_CHATTING_FRIEND_NOTE.equals(intent.getAction())) {    //Choose image correction
                String showname = intent.getExtras().getString("showname");
                String showuid = intent.getExtras().getString("showuid");
                if (!isGroup) {
                    //With a nickname
                    if (showuid.equals(uid)) {
                        setTitle(showname);
                    }
                } else {
                    for (int i = 0; i < mAdapter.getList().size(); i++) {
                        if (mAdapter.getList().get(i).getUserId().equals(showuid)) {
                            mAdapter.getList().get(i).setUsername(showname);
                        }
                    }
                    mAdapter.modifyNickName(showuid, showname);
                    mAdapter.notifyDataSetChanged();
                }
            }
            else if(intent != null && XmppAction.ACTION_ENTER_EVERYONE_LISTENER.equals(intent.getAction()))//进入evertone
            {
                ChatMsg chatMsg = new ChatMsg();
                if(uid.equals("everyone"))
                {
                    chatMsg.setType(12);
                    chatMsg.setContent(getString(R.string.chat_welcome_to_everyone));
                    mAdapter.addChatMsg(chatMsg, true);
                    if (listView.getLastVisiblePosition() >= mAdapter.getCount() - 2)//Only in the bottom, to the new message to scroll to the bottom
                    {
                        listView.setSelection(mAdapter.getCount());
                    }
                }
            }

            /*The following five without a net*/
            else if (intent != null && intent.getAction().equals(Constants.MSG_REPORT_SEND_MSG_RESULT)) {
                boolean result = intent.getExtras().getBoolean("result");
                int  state = intent.getExtras().getInt("state");
                String msgId = intent.getExtras().getString("msgid");
                mAdapter.updateOfflineSendStatus(result, state,msgId);
            }else if(intent != null && intent.getAction().equals(Constants.MSG_REPORT_SEND_MSG_PROGRESS)){
                long currentLength = intent.getExtras().getLong("currentLength");
                String msgId = intent.getExtras().getString("msgid");
                for (int m = mAdapter.getCount() - 1; m > -1; m--) {
                    if (mAdapter.getList().get(m).getMessageId().equals(msgId)) {
                        mAdapter.getList().get(m).setNewprogress(currentLength);
                        break;
                    }
                }
                int first = listView.getFirstVisiblePosition();
                int last = listView.getLastVisiblePosition();

                for (int m = first; m <= last; m++) {
                    View convertView = listView.getChildAt(m - first);
                    if (convertView == null || m  >= mAdapter.getCount()) {
                        continue;
                    }
                    if (mAdapter.getItem(m).getType() == 1009 &&  mAdapter.getItem(m).getMessageId().equals(msgId)) {
                        ProgressBar progressBar = (ProgressBar) convertView.findViewById(R.id.file_send_progress);
                        int percent = (int) ((mAdapter.getItem(m).getNewprogress() * 100) / (Long.parseLong(mAdapter.getItem(m).getNumber())) * 1.0f);
                        progressBar.setProgress(percent);
                        break;
                    }
                }
            }else if(intent != null && intent.getAction().equals(Constants.MSG_REPORT_START_RECV_FILE))//Begin to receive files
            {
                int  commend = intent.getExtras().getInt("commend");
                String msgId = intent.getExtras().getString("msgid");
                appNetService.handleSendFileCommend(uid,msgId,commend);
                mAdapter.updateOfflineSendStatus(true, 5,msgId);
                FinalUserDataBase.getInstance().updateOfflineChatMsgState(msgId,5,1);
            }
            else if(intent.getAction().equals(Constants.MSG_REPORT_CANCEL_SEND_FILE))//Cancel sending files
            {
                int  commend = intent.getExtras().getInt("commend");
                String msgId = intent.getExtras().getString("msgid");
                appNetService.handleSendFileCommend(uid,msgId,commend);
                mAdapter.updateOfflineSendStatus(true, 3,msgId);
                FinalUserDataBase.getInstance().updateOfflineChatMsgState(msgId, 3, 1);

            }
            else if(intent != null && intent.getAction().equals(Constants.MSG_REPORT_CANCEL_RECV_FILE))//Cancel the receiving
            {
                int  commend = intent.getExtras().getInt("commend");
                String msgId = intent.getExtras().getString("msgid");
                appNetService.handleSendFileCommend(uid, msgId, commend);
                mAdapter.updateOfflineSendStatus(true, 8,msgId);
                FinalUserDataBase.getInstance().updateOfflineChatMsgState(msgId, 8, 1);
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        chattingManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Delete or forwarding
     * */
    @Override
    public void onSceneListener(boolean isDelete) {
        setViewGone();
        isSceneDelete = isDelete;
        chattingSet.setVisibility(View.GONE);
        deleteTV.setVisibility(View.VISIBLE);
        if (isDelete) {
            deleteTV.setImageResource(R.drawable.delete_noraml);
        } else {
            deleteTV.setImageResource(R.drawable.chatting_relay);
        }
        mLeftSelected.setText(getString(R.string.chatting_selected, 1));
        mLeftSelected.setVisibility(View.VISIBLE);
        mTitle.setVisibility(View.GONE);
        mBack.setVisibility(View.GONE);
        Utils.hiddenKeyBoard(this);
        mAdapter.setSelectedTextView(mLeftSelected);
        chattingBottomRela.setVisibility(View.GONE);
    }

    /**
     * According to the receiver model suggests
     * */
    @Override
    public void onShow_mode_in_call_tip() {
        final LinearLayout layout = (LinearLayout) findViewById(R.id.mode_in_call_warning);
        layout.setVisibility(View.VISIBLE);
        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(getResources().getString(R.string.tip_receiver_mode));
        text.setTextSize(16);
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                layout.setVisibility(View.GONE);
            }
        }, 1500);
    }


    /**
     * Reducing state
     * */
    private void rollbackScene() {
        if (uid.equals("everyone")){
            setTitle(getString(R.string.everyone));
        }else{
            setTitle(userName);
            chattingSet.setVisibility(View.VISIBLE);
        }
        mLeftSelected.setText("");
        mLeftSelected.setVisibility(View.GONE);
        deleteTV.setVisibility(View.GONE);
        mTitle.setVisibility(View.VISIBLE);
        mBack.setVisibility(View.VISIBLE);
        chattingBottomRela.setVisibility(View.VISIBLE);
        mAdapter.rollbackSelected();
    }


    @Override
    public void onGlobalLayout() {
        if (listViewHeight != listView.getHeight()) {
            listViewHeight = listView.getHeight();
            listView.setSelection(mAdapter.getCount());
        }
    }
}
