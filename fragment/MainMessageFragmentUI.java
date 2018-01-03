package com.lingtuan.firefly.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.adapter.MessageEventAdapter;
import com.lingtuan.firefly.base.BaseFragment;
import com.lingtuan.firefly.contact.ContactSearchNickUI;
import com.lingtuan.firefly.contact.SelectContactUI;
import com.lingtuan.firefly.custom.ChatMsgComparable;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.login.LoginUtil;
import com.lingtuan.firefly.message.MsgTransListUI;
import com.lingtuan.firefly.quickmark.CaptureActivity;
import com.lingtuan.firefly.message.MsgAddContactListUI;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MyDialogFragment;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.vo.ChatMsg;
import com.lingtuan.firefly.vo.UserBaseVo;
import com.lingtuan.firefly.xmpp.XmppAction;
import com.lingtuan.firefly.xmpp.XmppMessageUtil;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * News page
 */
public class MainMessageFragmentUI extends BaseFragment implements OnItemClickListener, OnItemLongClickListener, OnClickListener {

    private View view;
    private boolean isDataFirstLoaded;

    //The message list related
    private ListView mListView;
    private MessageEventAdapter mAdapter;
    private List<ChatMsg> mList;

    //The message list is empty
    private TextView emptyTextView;
    private ImageView emptyIcon;
    private RelativeLayout emptyRela;

    //Return key hidden
    private ImageView mBack;

    //title
    private TextView mTitle;

    //News broadcast
    private MsgReceiverListener msgReceiverListener;

    //Double click on the title the message list rolled back
    private long positionScrollToTopTime = 0;

    //Function of the upper right corner of the frame
    private PopupWindow homePop;
    private ImageView mRightBtn;

    //Wait for the tooltip
    private Dialog mDialog;

    private TextView uploadRegisterInfo;//Synchronous registration information


    public MainMessageFragmentUI() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        isDataFirstLoaded = true;
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        if (!isDataFirstLoaded && view != null) {
            ((ViewGroup) view.getParent()).removeView(view);
            return view;
        }
        view = inflater.inflate(R.layout.main_msg_layout, container, false);
        initView();
        setListener();
        return view;
    }


    private void initView() {

        IntentFilter filter1 = new IntentFilter();
        filter1.addAction(Constants.CHANGE_LANGUAGE);//Update language refresh the page
        getActivity().registerReceiver(mBroadcastReceiver, filter1);

        mBack = (ImageView) view.findViewById(R.id.app_back);
        mTitle = (TextView) view.findViewById(R.id.app_title);

        emptyRela = (RelativeLayout) view.findViewById(R.id.empty_like_rela);
        emptyIcon = (ImageView) view.findViewById(R.id.empty_like_icon);
        emptyTextView = (TextView) view.findViewById(R.id.empty_text);

        mRightBtn = (ImageView) view.findViewById(R.id.detail_set);
        mRightBtn.setImageResource(R.drawable.icon_home_more);
        mRightBtn.setVisibility(View.VISIBLE);

        uploadRegisterInfo = (TextView) view.findViewById(R.id.uploadRegisterInfo);

        mListView = (ListView) view.findViewById(R.id.msg_event_list);
        mList = new ArrayList<>();
        mAdapter = new MessageEventAdapter(mList, getActivity());
        mListView.setAdapter(mAdapter);

        if (uploadRegisterInfo != null){
            if (NextApplication.myInfo == null || TextUtils.isEmpty(NextApplication.myInfo.getMid())){
                uploadRegisterInfo.setVisibility(View.VISIBLE);
            }else{
                uploadRegisterInfo.setVisibility(View.GONE);
            }
        }
    }

    private void setListener() {
        mTitle.setOnClickListener(this);
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);
        mRightBtn.setOnClickListener(this);
        uploadRegisterInfo.setOnClickListener(this);
    }

    @SuppressLint("HandlerLeak")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!isDataFirstLoaded) {
            return;
        }
        mTitle.setText(R.string.main_chats);
        mBack.setVisibility(View.GONE);
        isDataFirstLoaded = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(XmppAction.ACTION_MESSAGE_EVENT_LISTENER);//message Session record distribution
        filter.addAction(XmppAction.ACTION_OFFLINE_MESSAGE_LIST_EVENT_LISTENER);//message list Session record distribution
        filter.addAction(Constants.ACTION_SCROLL_TO_NEXT_UNREAD_MSG);//Unread messages scroll
        filter.addAction(Constants.ACTION_NETWORK_RECEIVER);//Network monitoring
        msgReceiverListener = new MsgReceiverListener();
        getActivity().registerReceiver(msgReceiverListener, filter);

        //Update the chat messages
        new Thread(new UpdateMessage()).start();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden){
            Utils.updateViewMethod(uploadRegisterInfo,getActivity());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (msgReceiverListener != null) {
            getActivity().unregisterReceiver(msgReceiverListener);
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && (Constants.CHANGE_LANGUAGE.equals(intent.getAction()))) {
                mTitle.setText(R.string.main_chats);
            }
        }
    };


    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mBroadcastReceiver);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
        if (mList != null && !mList.isEmpty()) {
            if (position >= mList.size()) {
                return;
            }
            ChatMsg msg = mList.get(position);
            String uid = msg.getChatId();
            String username = msg.getUsername();
            boolean isGroup = false;
            if (TextUtils.isEmpty(uid)) {
                return;
            }
            if (!msg.isSystem()) {//The system information
                //Normal chat messages
                if(uid.equals("everyone"))
                {
                    XmppMessageUtil.getInstance().sendEnterLeaveEveryOne(msg.getMsgTime(),false);
                }

                String avatarPath = msg.getUserImage();
                if (uid.startsWith("group-") || uid.startsWith("superGroup-")) {//Is group chat
                    uid = msg.getChatId();
                    isGroup = true;
                    if (uid.startsWith("superGroup-")) {
                        avatarPath = msg.getGroupImage();
                    }
                }


                Utils.intentChattingUI(getActivity(), uid, avatarPath, username, msg.getGender() + "",msg.getFriendLog(),isGroup, msg.isDismissGroup(), msg.isKickGroup(), msg.getUnread(), false);

                if (!msg.getGroupMask())//The shielding
                {
                    Bundle bundle = new Bundle();
                    bundle.putInt("unread", -msg.getUnread());
                    Utils.intentAction(getActivity(), XmppAction.ACTION_MESSAGE_EVENT_LISTENER, bundle);
                }
                msg.setUnread(0);
                msg.setAtGroupMe(0);
                int firstPosition = mListView.getFirstVisiblePosition();
                int lastPosition = mListView.getLastVisiblePosition();
                if (position >= firstPosition && position <= lastPosition) {
                    int currentPostion = position - firstPosition;
                    View childView = mListView.getChildAt(currentPostion);
                    TextView unread = (TextView) childView.findViewById(R.id.item_unread);
                    childView.findViewById(R.id.item_at).setVisibility(View.GONE);
                    unread.setVisibility(View.GONE);
                }
            } else {
                if (msg.getType() == 0) {//Friend request
                    Intent intent = new Intent(getActivity(), MsgAddContactListUI.class);
                    intent.putExtra("chatid", msg.getChatId());
                    startActivity(intent);
                    Utils.openNewActivityAnim(getActivity(), false);
                    Bundle bundle = new Bundle();
                    bundle.putInt("unread", -msg.getUnread());
                    Utils.intentAction(getActivity(), XmppAction.ACTION_MESSAGE_EVENT_LISTENER, bundle);
                    int firstPosition = mListView.getFirstVisiblePosition();
                    int lastPosition = mListView.getLastVisiblePosition();
                    if (position >= firstPosition && position <= lastPosition) {
                        int currentPostion = position - firstPosition;
                        View childView = mListView.getChildAt(currentPostion);
                        TextView unread = (TextView) childView.findViewById(R.id.item_unread);
                        unread.setVisibility(View.GONE);
                    }
                }else  if (msg.getType() == 300) {//Trans request
                    Intent intent = new Intent(getActivity(), MsgTransListUI.class);
                    intent.putExtra("chatid", msg.getChatId());
                    startActivity(intent);
                    Utils.openNewActivityAnim(getActivity(), false);
                    Bundle bundle = new Bundle();
                    bundle.putInt("unread", -msg.getUnread());
                    Utils.intentAction(getActivity(), XmppAction.ACTION_MESSAGE_EVENT_LISTENER, bundle);
                    int firstPosition = mListView.getFirstVisiblePosition();
                    int lastPosition = mListView.getLastVisiblePosition();
                    if (position >= firstPosition && position <= lastPosition) {
                        int currentPostion = position - firstPosition;
                        View childView = mListView.getChildAt(currentPostion);
                        TextView unread = (TextView) childView.findViewById(R.id.item_unread);
                        unread.setVisibility(View.GONE);
                    }
                }
            }

        }
    }

    class MsgReceiverListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && Constants.ACTION_SCROLL_TO_NEXT_UNREAD_MSG.equals(intent.getAction())) {
                boolean foundNextUnreadMsg = false;
                for (int i = mListView.getFirstVisiblePosition() + 1; i < mList.size(); i++) {
                    ChatMsg msg = mList.get(i);
                     if ("system-0".equals(msg.getChatId()) || "system-1".equals(msg.getChatId()) || "system-3".equals(msg.getChatId()) || "system-4".equals(msg.getChatId())  || "system-5".equals(msg.getChatId())) {
                        if (msg.getUnread() > 0 && mListView.getLastVisiblePosition() < mList.size() - 1) {
                            foundNextUnreadMsg = true;
                            mListView.setSelection(i);
                            break;
                        } else {
                            continue;
                        }

                    } else if (!msg.getGroupMask()) {
                        if (msg.getUnread() > 0 && mListView.getLastVisiblePosition() < mList.size() - 1) {
                            foundNextUnreadMsg = true;
                            mListView.setSelection(i);
                            break;
                        } else {
                            continue;
                        }
                    } else {
                        continue;
                    }
                }
                if (!foundNextUnreadMsg) {
                    for (int i = 0; i < mListView.getFirstVisiblePosition() - 1; i++) {
                        ChatMsg msg = mList.get(i);
                         if ("system-0".equals(msg.getChatId()) || "system-1".equals(msg.getChatId()) || "system-3".equals(msg.getChatId()) || "system-4".equals(msg.getChatId())  || "system-5".equals(msg.getChatId())) {
                            if (msg.getUnread() > 0) {
                                mListView.setSelection(i);
                                break;
                            } else {
                                continue;
                            }

                        } else if (!msg.getGroupMask()) {
                            if (msg.getUnread() > 0) {
                                mListView.setSelection(i);
                                break;
                            } else {
                                continue;
                            }
                        } else {
                            continue;
                        }
                    }
                }
            } else if (intent != null && XmppAction.ACTION_MESSAGE_EVENT_LISTENER.equals(intent.getAction())) {

                Bundle bundle = intent.getBundleExtra(XmppAction.ACTION_MESSAGE_EVENT_LISTENER);
                if (bundle != null && bundle.getSerializable("chat") != null) {

                    ChatMsg msg = (ChatMsg) bundle.getSerializable("chat");
                    if (msg.isGroup()) {
                        msg.setUsername(msg.getGroupName());
                    }

                    //With a nickname
                    if (!TextUtils.isEmpty(msg.getChatId())) {
                        String note = MySharedPrefs.readString(getActivity(), MySharedPrefs.KEY_FRIEND_NOTE + NextApplication.myInfo.getLocalId(), msg.getChatId());
                        if (!TextUtils.isEmpty(note)) {
                            msg.setUsername(note);
                        }
                    }
                    mAdapter.addChatMsg(msg);
                }
            } else if (intent != null && XmppAction.ACTION_OFFLINE_MESSAGE_LIST_EVENT_LISTENER.equals(intent.getAction())) {
                ChatMsg msg = (ChatMsg) intent.getSerializableExtra("chat");
                if (msg != null) {
                    if (msg.isGroup()) {
                        msg.setUsername(msg.getGroupName());
                    }
                    //With a nickname
                    if (!TextUtils.isEmpty(msg.getChatId())) {
                        String note = MySharedPrefs.readString(getActivity(), MySharedPrefs.KEY_FRIEND_NOTE + NextApplication.myInfo.getLocalId(), msg.getChatId());
                        if (!TextUtils.isEmpty(note)) {
                            msg.setUsername(note);
                        }
                    }
                    mAdapter.addChatMsg(msg);
                }
            }else if (intent != null && Constants.ACTION_NETWORK_RECEIVER.equals(intent.getAction())) {
                Utils.updateViewMethod(uploadRegisterInfo,getActivity());
            }

        }
    }

    /**
     * Update the message thread
     * */
    class UpdateMessage implements Runnable {

        private Handler handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                mList = (List<ChatMsg>) msg.obj;
                mAdapter.updateList(mList);
                Utils.intentAction(getActivity(), XmppAction.ACTION_MAIN_UNREADMSG_UPDATE_LISTENER, null);// message Number of unread updates
            }
        };

        @Override
        public void run() {
            List<ChatMsg> mList = FinalUserDataBase.getInstance().getChatMsgEventList();
            boolean foundEveryOne = false;
            for(int i=0;i<mList.size();i++)
            {
                if("everyone".equals(mList.get(i).getChatId()))
                {
                    foundEveryOne =  true;
                    break;
                }
            }

            if(!foundEveryOne)
            {
                ChatMsg chatMsg = new ChatMsg();
                chatMsg.setUsername(getString(R.string.everyone));
                chatMsg.setChatId("everyone");
                chatMsg.setType(0);
                chatMsg.setMsgTime(System.currentTimeMillis()/1000);
                chatMsg.setContent("");
                mList.add(chatMsg);
                FinalUserDataBase.getInstance().insertChatEvent(chatMsg);
            }
            Collections.sort(mList, new ChatMsgComparable());
            Message msg = new Message();
            msg.obj = mList;
            handler.sendMessage(msg);
        }

    }


    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view,final int position, long id) {
        int itemArrayId;
        final ChatMsg msg = mList.get(position);
        if(msg.getChatId().equals("everyone"))
        {
            itemArrayId = mList.get(position).isTop() ? R.array.delete_clear_top_chat_array : R.array.clear_to_top_chat_array;
        }
        else{
            itemArrayId = mList.get(position).isTop() ? R.array.delete_remove_top_chat_array : R.array.delete_to_top_chat_array;
        }
        MyDialogFragment mdf = new MyDialogFragment(MyDialogFragment.DIALOG_LIST, itemArrayId);
        mdf.setItemClickCallback(new MyDialogFragment.ItemClickCallback() {
            @Override
            public void itemClickCallback(int which) {
                switch (which) {
                    case 0://Placed at the top, cancel placed at the top
                    {
                        long topTime = System.currentTimeMillis() / 1000;
                        msg.setTop(!msg.isTop());
                        if (msg.isTop()) {
                            msg.setTopTime(topTime);
                        }
                        Collections.sort(mList, new ChatMsgComparable());
                        mAdapter.updateList(mList);
                        FinalUserDataBase.getInstance().updateChatEventTop(msg.getChatId(), msg.isTop(), topTime);

                        for (int i = 0; i < mList.size(); i++) {
                            if (msg.equals(mList.get(i))) {
                                mListView.setSelection(i);
                                break;
                            }

                        }
                    }
                    break;
                    case 1: {
                        Bundle bundle = new Bundle();
                        bundle.putInt("unread", -msg.getUnread());
                        Utils.intentAction(getActivity(), XmppAction.ACTION_MESSAGE_EVENT_LISTENER, bundle);
                        if(msg.getChatId().equals("everyone"))
                        {
                            FinalUserDataBase.getInstance().clearChatMsgByChatId(msg.getChatId(),msg);
                            msg.setContent("");
                            msg.setUnread(0);
                            mAdapter.updateList(mList);
                        }
                        else{
                            FinalUserDataBase.getInstance().deleteChatMsgByChatId(msg.getChatId());
                            mList.remove(position);
                            mAdapter.updateList(mList);
                        }

                    }
                    break;
                }


            }
        });
        mdf.show(getFragmentManager(), "mdf");
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.uploadRegisterInfo:
                LoginUtil.getInstance().initContext(getActivity());
                LoginUtil.getInstance().showRegistDialog(uploadRegisterInfo);
                break;
            case R.id.app_title://Double-click the message rolled back
                if (System.currentTimeMillis() - positionScrollToTopTime < 500) {
                    positionScrollToTopTime = 0;
                    if (Build.VERSION.SDK_INT < 11) {
                        mListView.setSelection(0);
                    } else {
                        mListView.smoothScrollToPositionFromTop(0, 20, 200);
                    }
                } else {
                    positionScrollToTopTime = System.currentTimeMillis();
                }
                break;
            case R.id.detail_set: //"+", pop-up Pop popup window
                initHomePop();
                break;
            case R.id.txt_home_pop_1:       //Add buddy
//                startActivity(new Intent(getActivity(), AddFriendsUI.class));
                startActivity(new Intent(getActivity(), ContactSearchNickUI.class));
                Utils.openNewActivityAnim(getActivity(), false);
                dismissHomePop();
                break;
            case R.id.txt_home_pop_2:       //Create a group chat
                Intent intent = new Intent(getActivity(), SelectContactUI.class);
                intent.putExtra("isMultipleChoice", true);
                startActivityForResult(intent, 0);
                Utils.openNewActivityAnim(getActivity(), false);
                dismissHomePop();
                break;
            case R.id.txt_home_pop_3:       //scan
                startActivity(new Intent(getActivity(), CaptureActivity.class));
                Utils.openNewActivityAnim(getActivity(), false);
                dismissHomePop();
                break;
            default:
                break;
        }
    }


    /**
     * Initialize the Pop layout
     */
    private void initHomePop() {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.contact_more_popup_layout, null);
        homePop = new PopupWindow(view, Utils.dip2px(getActivity(), 170), LinearLayout.LayoutParams.WRAP_CONTENT);
        homePop.setBackgroundDrawable(new BitmapDrawable());
        homePop.setOutsideTouchable(true);
        homePop.setFocusable(true);
        view.findViewById(R.id.txt_home_pop_1).setOnClickListener(this);
        view.findViewById(R.id.txt_home_pop_2).setOnClickListener(this);
        view.findViewById(R.id.txt_home_pop_3).setOnClickListener(this);
        if (homePop.isShowing()) {
            homePop.dismiss();
        } else {
            // On the coordinates of a specific display PopupWindow custom menu
            homePop.showAsDropDown(mRightBtn, Utils.dip2px(getActivity(), -110), Utils.dip2px(getActivity(), 0));
        }
    }

    private void dismissHomePop() {
        if (homePop != null && homePop.isShowing()) {
            homePop.dismiss();
            homePop = null;
        }
    }

    /**
     * To test whether the current list is empty
     */
//    private void checkListEmpty() {
//        if (mList == null || mList.size() == 0) {
//            emptyRela.setVisibility(View.VISIBLE);
//            emptyIcon.setImageResource(R.drawable.empty_msg);
//            emptyTextView.setText(R.string.contact_empty_msg);
//            mListView.setVisibility(View.GONE);
//        } else {
//            emptyRela.setVisibility(View.GONE);
//            mListView.setVisibility(View.VISIBLE);
//        }
//    }

    private void showDialog() {
        dismissDialog();
        mDialog = LoadingDialog.showDialog(getActivity(), null, null);
        mDialog.setCancelable(false);

    }

    private void dismissDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == Activity.RESULT_OK){//选择联系人返回

            ArrayList<UserBaseVo> selectList = (ArrayList<UserBaseVo>) data.getSerializableExtra("selectList");

            if (selectList != null && selectList.size() == 1) {
                UserBaseVo vo = selectList.get(0);
                Utils.intentChattingUI(getActivity(), vo.getLocalId(), vo.getThumb(), vo.getShowName(), vo.getGender(),vo.getFriendLog(),false, false, false, 0, false);
            } else if (selectList != null && selectList.size() > 1) {
                StringBuilder touids = new StringBuilder();
                for (UserBaseVo vo : selectList) {
                    touids.append(vo.getLocalId()).append(",");
                }
                touids.deleteCharAt(touids.lastIndexOf(","));
                createDiscussionGroups(touids.toString(), selectList);
            }
        }
    }

    /**
     * create a group chat
     * @ param touids member id
     * @ param member group members
     */
    private void createDiscussionGroups(String touids, final List<UserBaseVo> member) {
        showDialog();
        NetRequestImpl.getInstance().createDiscussionGroups(touids, new RequestListener() {
            @Override
            public void start() {

            }

            @Override
            public void success(JSONObject response) {
                showToast(response.optString("msg"));
                dismissDialog();
                Utils.gotoGroupChat(getActivity(),false,null,response.optString("cid"), member);
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                dismissDialog();
                showToast(errorMsg);
            }
        });

    }

}


