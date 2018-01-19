package com.lingtuan.firefly.chat.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.chat.AtGroupParser;
import com.lingtuan.firefly.chat.ChatFileInfoUI;
import com.lingtuan.firefly.chat.ChattingManager;
import com.lingtuan.firefly.custom.BitmapFillet;
import com.lingtuan.firefly.custom.CharAvatarView;
import com.lingtuan.firefly.custom.CustomLinkMovementMethod;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.imagescan.ScanLargePic;
import com.lingtuan.firefly.listener.DialogItemClickListener;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.ui.ShowTextUI;
import com.lingtuan.firefly.util.BitmapUtils;
import com.lingtuan.firefly.util.FileSizeUtils;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MyDialogFragment;
import com.lingtuan.firefly.util.MyPopupWindow;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.MyToast;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;
import com.lingtuan.firefly.vo.ChatMsg;
import com.lingtuan.firefly.vo.UserBaseVo;
import com.lingtuan.firefly.xmpp.XmppMessageUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class ChatAdapter extends BaseAdapter {

    private List<ChatMsg> mList;
    private Context mContext;

    private int TYPE_SIZE = 15;

    private final int LEFT_TEXT = 0;
    private final int RIGHT_TEXT = 1;
    private final int LEFT_IMAGE = 2;
    private final int RIGHT_IMAGE = 3;
    private final int LEFT_CARD = 4;
    private final int RIGHT_CARD = 5;
    private final int LEFT_AUDIO = 6;
    private final int RIGHT_AUDIO = 7;
    private final int NOTIF = 8;
    private final int LEFT_DATIONG_SOS = 9;
    private final int LEFT_SHARE = 10;
    private final int RIGHT_SHARE = 11;
    /**
     * Send a file
     */
    private final int LEFT_SEND_FILE = 12;
    private final int RIGHT_SEND_FILE = 13;

    private MediaPlayer mPlayer;

    private AnimationDrawable animationDrawable;

    private ArrayList<String> imagePathList;

    private long currentMsgTime;//Used for displaying text double-click
    private long clickTimes;//Used for displaying text double-click

    private boolean kick;
    private boolean dismiss;

    private boolean forwarding = false;//forwarding
    private boolean delete = false;//delete
    private boolean isUpload = false;//Upload the chat record

    private boolean isGroup;
    private TextView selectedView;

    private Map<String, ChatMsg> selectedList;

    private Map<String, String> tempAvatar = new HashMap<>();
    private Map<String, String> tempName = new HashMap<>();
    private SceneListener listener;

    private EditText mInputContent;

    private ChattingManager mChattingManager;

    private AudioManager audioManager;

    private int source;
    private String sourceid;
    private Dialog mProgressDialog;
    private ListView listview;

    public ChatAdapter(List<ChatMsg> mList, Context mContext, SceneListener listener, ListView listview) {
        if (mList == null) {
            mList = new ArrayList<>();
        }
        audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        this.mList = mList;
        this.mContext = mContext;
        this.listener = listener;
        this.listview = listview;

        mPlayer = new MediaPlayer();
        imagePathList = new ArrayList<>();
        if (mList != null) {
            for (int i = 0; i < mList.size(); i++) {
                addImageUrl(mList.get(i));
                try {
                    addTempAvatar(mList.get(i).getUserId(), mList.get(i).getUserImage());
                    addTempName(mList.get(i).getUserId(), mList.get(i).getUsername());
                } catch (Exception e) {
                }
            }
        }
        selectedList = new HashMap<>();
    }

    public void resetSource(List<ChatMsg> mList) {
        this.mList = mList;
    }

    public void setChatType(int source, String sourceid) {
        this.source = source;
        this.sourceid = sourceid;
    }

    public void setGroup(boolean isGroup) {
        this.isGroup = isGroup;
    }

    public void setInputEditText(EditText mInputContent, ChattingManager mChattingManager) {
        this.mInputContent = mInputContent;
        this.mChattingManager = mChattingManager;
    }

    /**
     *  remove
     */
    public void addSelectedItem(ChatMsg msg) {
        if (forwarding || delete || isUpload) {
            selectedList.put(msg.getMessageId(), msg);
            notifyDataSetChanged();
        }
    }

    /**
     * Return to selected collection
     */
    public Map<String, ChatMsg> getSelectedList() {
        return selectedList;
    }

    public void updateAgree(boolean agree, String messageId) {
        if (TextUtils.isEmpty(messageId)) {
            return;
        }
        int count = mList.size();
        for (int i = 0; i < count; i++) {
            ChatMsg msg = mList.get(i);
            if (TextUtils.equals(msg.getMessageId(), messageId)) {
                msg.setCover(agree ? "1" : "2");
                notifyDataSetChanged();
                break;
            }
        }
    }

    /**
     * Upload the chat record A separate interface If not please do not call
     * The result set, please call the interface{@link ChatAdapter#getSelectedList()}
     * @param mList    If there are data into not null
     * @param isUpload Whether to upload the chats
     */
    public void addSelectedList(List<ChatMsg> mList, boolean isUpload) {
        this.isUpload = isUpload;
        if (mList != null && !mList.isEmpty()) {
            for (ChatMsg msg : mList) {
                selectedList.put(msg.getMessageId(), msg);
            }
            notifyDataSetChanged();
        }
    }

    public synchronized void selectedItem(ChatMsg msg) {
        if (selectedList.containsKey(msg.getMessageId())) {
            removeSelectedItem(msg);
        } else {
            addSelectedItem(msg);
        }
        if (selectedView != null){
            selectedView.setText(mContext.getString(R.string.chatting_selected, selectedList.size()));
        }
    }

    public synchronized void rollbackSelected() {
        delete = false;
        forwarding = false;
        isUpload = false;
        selectedList.clear();
        notifyDataSetChanged();
    }

    public void setSelectedTextView(TextView selectedView) {
        this.selectedView = selectedView;
    }

    /**
     * Delete the selected item
     */
    public synchronized void removeSelectList() {
        if (!selectedList.isEmpty()) {
            for (Entry<String, ChatMsg> s : selectedList.entrySet()) {

                if (s.getValue().getType() == 1)//图片
                {
                    String url = s.getValue().getLocalUrl();
                    if (TextUtils.isEmpty(url)) {
                        url = s.getValue().getContent();
                    } else {
                        File file = new File(url);
                        if (file.exists()) {
                            url = "file://" + s.getValue().getLocalUrl();
                        } else {
                            url = s.getValue().getContent();
                        }
                    }

                    for (int i = 0; i < imagePathList.size(); i++) {
                        if (imagePathList.get(i).equals(url)) {
                            imagePathList.remove(i);
                            break;
                        }
                    }
                }

                mList.remove(s.getValue());
            }
        }
        rollbackSelected();
        notifyDataSetChanged();
    }

    /**
     * remove
     */
    public void removeSelectedItem(ChatMsg msg) {
        if (forwarding || delete || isUpload) {
            selectedList.remove(msg.getMessageId());
            notifyDataSetChanged();
        }
    }

    /**
     * @param kick
     * @param dismiss
     */
    public void setKickDismiss(boolean kick, boolean dismiss) {
        this.kick = kick;
        this.dismiss = dismiss;
    }

    public void insertSystemChatMsg(int index, ChatMsg msg) {
        mList.add(index, msg);
        notifyDataSetChanged();
        imagePathList.clear();
        if (mList != null) {
            for (int i = 0; i < mList.size(); i++) {
                addImageUrl(mList.get(i));
            }
        }
    }

    public void addChatMsg(ChatMsg msg, boolean notify) {
        mList.add(msg);
        if (notify)
            notifyDataSetChanged();
        addImageUrl(msg);
        addTempAvatar(msg.getUserId(), msg.getUserImage());
        addTempName(msg.getUserId(), msg.getUsername());
    }

    private void addTempAvatar(String uid, String avaratUrl) {
        tempAvatar.put(uid, avaratUrl);
    }

    private void addTempName(String uid, String userName) {
        tempName.put(uid, userName);
    }

    public void updateChatMsg(ChatMsg msg) {
        int count = mList.size();
        for (int i = count - 1; i > -1; i--) {
            if (mList.get(i).getType() == msg.getType() && mList.get(i).getMessageId() != null && mList.get(i).getMessageId().equals(msg.getMessageId())) {
                mList.get(i).setContent(msg.getContent());
                mList.get(i).setLocalUrl(msg.getLocalUrl());
                mList.get(i).setSend(msg.getSend());
                mList.get(i).setInviteType(msg.getInviteType());
                mList.get(i).setThirdId(msg.getThirdId());
                if (msg.getCreateTime() > 0)//The expiry time
                {
                    mList.get(i).setCreateTime(msg.getCreateTime());
                }
                break;
            }
        }
        notifyDataSetChanged();
    }

    /*No network chat to update the delivery status*/
    public void updateOfflineSendStatus(boolean send, int state, String msgId) {
        if (mList == null) {
            return;
        }
        int count = mList.size();
        for (int i = count - 1; i > -1; i--) {
            if (mList.get(i).getMessageId() != null && mList.get(i).getMessageId().equals(msgId)) {
                mList.get(i).setSend(send ? 1 : 0);
                if (mList.get(i).getInviteType() != 8)//Cancel to accept later do not change the state
                {
                    mList.get(i).setInviteType(state);
                }
                notifyDataSetChanged();
                break;
            }
        }
    }

    /*A web chat to update the delivery status*/
    public ChatMsg updateSendStatus(int send, int state, String msgId, String localUrl, int collectState) {
        if (mList == null) {
            return null;
        }
        ChatMsg chatMsg = null;
        int count = mList.size();
        for (int i = count - 1; i > -1; i--) {
            if (mList.get(i).getMessageId() != null && mList.get(i).getMessageId().equals(msgId)) {
                chatMsg = mList.get(i);
                if (send != -1) {
                    chatMsg.setSend(send);
                }
                if (!TextUtils.isEmpty(localUrl)) {
                    chatMsg.setLocalUrl(localUrl);
                }
                if (collectState != -1) {
                    chatMsg.setDatingSOSId(collectState);
                }
                if (state != -2) {     //To get their state not only to update the state was 2
                    chatMsg.setInviteType(state);
                }
                notifyDataSetChanged();
                break;
            }
        }
        return chatMsg;
    }

    public void updateList(List<ChatMsg> mList) {
        this.mList = mList;
        notifyDataSetChanged();
        imagePathList.clear();
        if (mList != null) {
            for (int i = 0; i < mList.size(); i++) {
                addImageUrl(mList.get(i));
            }
        }
    }

    private void addImageUrl(ChatMsg msg) {
        if (msg.getType() == 1) {
            String url = msg.getLocalUrl();
            if (TextUtils.isEmpty(url)) {
                url = msg.getContent();
            } else {
                File file = new File(url);
                if (file.exists()) {
                    url = "file://" + msg.getLocalUrl();
                } else {
                    url = msg.getContent();
                }
            }
            imagePathList.add(url);
        }
    }

    public List<ChatMsg> getList() {
        return mList;
    }

    public void modifyNickName(String uid, String nickName) {
        tempName.put(uid, nickName);
    }

    @Override
    public int getCount() {
        if (mList == null) {
            return 0;
        }
        return mList.size();
    }

    @Override
    public ChatMsg getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @Override
    public int getItemViewType(int position) {
        int type = 0;
        if (mList.get(position).isMe()) {//Their information
            switch (mList.get(position).getType()) {
                case 0://text
                    type = RIGHT_TEXT;
                    break;
                case 1://picture
                    type = RIGHT_IMAGE;
                    break;
                case 2://voice
                    type = RIGHT_AUDIO;
                    break;
                case 6://Business card
                    type = RIGHT_CARD;
                    break;
                case 103://Share information
                    type = RIGHT_SHARE;
                    break;
                case 1009://file
                    type = RIGHT_SEND_FILE;
                    break;
                case 12:
                case 13://Notice the information
                case 14:
                case 15:
                case 17:
                case 18:
                case 50000://Not compatible with high version of the news
                case 50001://Compatibility across the app can't receive the message
                case 50002://Compatibility across the app can't see the message
                    type = NOTIF;
                    break;
            }
        } else {
            switch (mList.get(position).getType()) {//Other people's information
                case 0://text
                    type = LEFT_TEXT;
                    break;
                case 1://picture
                    type = LEFT_IMAGE;
                    break;
                case 2://audio
                    type = LEFT_AUDIO;
                    break;
                case 6://Business card
                    type = LEFT_CARD;
                    break;
                case 103://share
                    type = LEFT_SHARE;
                    break;
                case 1009://file
                    type = LEFT_SEND_FILE;
                    break;
                case 12:
                case 13://Notice the information
                case 14:
                case 15:
                case 17:
                case 18:
                case 22:
                case 50000://Not compatible with high version of the news
                case 50001://Compatibility across the app can't receive the message
                case 50002://Compatibility across the app can't see the message
                    type = NOTIF;
                    break;
            }
        }
        return type;
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_SIZE;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Holder h;
        final ChatMsg msg = mList.get(position);
        final int type = getItemViewType(position);
        if (convertView == null) {
            h = new Holder();
            switch (type) {
                case LEFT_TEXT://The other side of the text information
                    convertView = View.inflate(mContext, R.layout.item_chatting_left_text, null);
                    break;
                case LEFT_IMAGE://The other side of the picture information
                    convertView = View.inflate(mContext, R.layout.item_chatting_left_image, null);

                    break;
                case LEFT_AUDIO://Each other's voice messages
                    convertView = View.inflate(mContext, R.layout.item_chatting_left_audio, null);
                    break;

                case LEFT_CARD://The other side of the business card information
                    convertView = View.inflate(mContext, R.layout.item_chatting_left_card, null);
                    break;

                case LEFT_SHARE://Share information
                    convertView = View.inflate(mContext, R.layout.item_chatting_left_share, null);
                    break;
                /**----------------------------------------------------*/
                case RIGHT_TEXT://His text message
                    convertView = View.inflate(mContext, R.layout.item_chatting_right_text, null);
                    break;

                case RIGHT_IMAGE://His picture information
                    convertView = View.inflate(mContext, R.layout.item_chatting_right_image, null);
                    break;

                case RIGHT_AUDIO://His voice messages
                    convertView = View.inflate(mContext, R.layout.item_chatting_right_audio, null);
                    break;

                case RIGHT_CARD://Your business card information
                    convertView = View.inflate(mContext, R.layout.item_chatting_right_card, null);
                    break;

                case RIGHT_SHARE://Share information
                    convertView = View.inflate(mContext, R.layout.item_chatting_right_share, null);
                    break;

                case NOTIF://The system informs the information
                    convertView = View.inflate(mContext, R.layout.item_chatting_notif, null);
                    break;

                case LEFT_SEND_FILE:    //Received the documents
                    convertView = View.inflate(mContext, R.layout.item_chatting_left_file, null);
                    break;

                case RIGHT_SEND_FILE:   //From the file
                    convertView = View.inflate(mContext, R.layout.item_chatting_right_file, null);
                    break;
            }

            h.leftLinear = (LinearLayout) convertView.findViewById(R.id.item_chatting_body_linear);
            h.imageUploadLinear = (LinearLayout) convertView.findViewById(R.id.item_chatting_image_upload_linear);
            h.avatar = (CharAvatarView) convertView.findViewById(R.id.item_chatting_avatar);
            h.mNickname = (TextView) convertView.findViewById(R.id.item_chatting_nickname);
            h.content = (TextView) convertView.findViewById(R.id.item_chatting_text);
            h.time = (TextView) convertView.findViewById(R.id.item_chatting_time);
            h.icon = (ImageView) convertView.findViewById(R.id.item_chatting_icon);
            h.msgWarnning = (ImageView) convertView.findViewById(R.id.item_chatting_warnning);
            h.msgImage = (ImageView) convertView.findViewById(R.id.item_chatting_image);
            h.mNotifBar = (ProgressBar) convertView.findViewById(R.id.item_chatting_notif);

            h.shopAddress = (TextView) convertView.findViewById(R.id.item_chatting_third_address);
            h.shopName = (TextView) convertView.findViewById(R.id.item_chatting_third_name);
            h.shopImage = (CharAvatarView) convertView.findViewById(R.id.item_chatting_third_avatar);
            h.audioTimes = (TextView) convertView.findViewById(R.id.item_chatting_audio_times);
            h.audioIcon = (ImageView) convertView.findViewById(R.id.item_chatting_audio_icon);

            h.selectBox = (ImageView) convertView.findViewById(R.id.item_chatting_select_iv);
            h.relativeLayout = (RelativeLayout) convertView.findViewById(R.id.relativelayout);


            /**************************Documents related to*****************************************/
            h.imgFileLogo = (ImageView) convertView.findViewById(R.id.img_file_logo);
            h.txtFileName = (TextView) convertView.findViewById(R.id.txt_file_name);
            h.txtFileSize = (TextView) convertView.findViewById(R.id.txt_file_size);
            h.txtFileSendState = (TextView) convertView.findViewById(R.id.txt_file_sent_state);
            h.progressbarFile = (ProgressBar) convertView.findViewById(R.id.file_send_progress);
            convertView.setTag(h);
        } else {
            h = (Holder) convertView.getTag();
        }

        if (h.selectBox != null) {
            if (delete || forwarding || isUpload) {
                h.selectBox.setVisibility(View.VISIBLE);
                int resId = selectedList.get(msg.getMessageId()) == null ? R.drawable.checkbox_unselected : R.drawable.checkbox_selected;
                h.selectBox.setImageResource(resId);
                if (h.relativeLayout != null) {
                    h.relativeLayout.setVisibility(View.VISIBLE);
                    h.relativeLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (forwarding && (type == LEFT_SEND_FILE || type == RIGHT_SEND_FILE)) {
                                if (msg.getCreateTime() > 0) {     //One to one message file
                                    long time = msg.getCreateTime() + 7 * 24 * 3600 - System.currentTimeMillis() / 1000;
                                    if (time <= 0) {      //Has the failure
                                        return;
                                    }
                                }
                            }
                            //Voice is selected //Except upload chat logs
                            if (isUpload || !(forwarding && (type == LEFT_AUDIO || type == RIGHT_AUDIO || type == LEFT_DATIONG_SOS))){
                                selectedItem(msg);
                            }
                        }

                    });
                }
            } else {
                if (h.relativeLayout != null) {
                    h.relativeLayout.setVisibility(View.GONE);
                }
                h.selectBox.setVisibility(View.GONE);
            }

        }
        switch (type) {
            case LEFT_TEXT://The other side of the text information
            case RIGHT_TEXT://His text message
                showTextMsg(h,msg,type);
                break;
            case LEFT_IMAGE://The other side of the picture information
            case RIGHT_IMAGE://His picture information
                showImgMsg(h,msg);
                break;
            case LEFT_AUDIO://Each other's voice messages
            case RIGHT_AUDIO://His voice messages
                setAudioContent(h, position, type);
                break;

            case LEFT_CARD://The other side of the business card information
            case RIGHT_CARD://Your business card information
                showCardMsg(h,msg);
                break;

            case RIGHT_SEND_FILE:   //Send the file
                showFileInfo(h, msg, true);
                break;

            case LEFT_SEND_FILE:   //Received the documents
                showFileInfo(h, msg, true);
                break;

            case LEFT_SHARE://Share information
            case RIGHT_SHARE:
                showShareMsg(h, msg);
                break;

            case NOTIF://The system informs the information
                showSystemMsg(h, msg);

                return convertView;
        }

        h.avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    UserBaseVo vo = new UserBaseVo();
                    vo.setUsername(msg.getUsername());
                    vo.setLocalId(msg.getUserId());
                    vo.setThumb(msg.getUserImage());
                    vo.setFriendLog(msg.getFriendLog());
                    intentPeopleDetailUI(vo);
                }
        });
        h.avatar.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if ((isGroup || TextUtils.equals("everyone",msg.getChatId() ))&& !msg.isMe() && mInputContent != null && mChattingManager != null) {//Long press head @ function
                    int selectIndex = mInputContent.getSelectionStart();
                    Editable mEditable = mInputContent.getEditableText();
                    mEditable.insert(selectIndex, "@" + msg.getRealname() + " ");
                    mChattingManager.sbAtGroupSelectIds.add(msg.getUserId());
                    Utils.showKeyBoard(mInputContent);
                    if (AtGroupParser.getInstance() == null) {
                        AtGroupParser.init(new String[]{msg.getRealname() + " "}, new String[]{msg.getUserId()});
                    } else {
                        AtGroupParser.getInstance().addParse(msg.getRealname() + " ", msg.getUserId());
                    }
                }
                return true;
            }
        });
        if (h.leftLinear != null) {
            h.leftLinear.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    //Long chat content according to the event
                    return onLongClickLogic(msg);
                }
            });
        }
        String url = "";
        if (msg.isMe()) {
            url = NextApplication.myInfo.getThumb();
        } else {
            url = msg.getUserImage();
            if (!TextUtils.isEmpty(tempAvatar.get(msg.getUserId()))) {
                url = tempAvatar.get(msg.getUserId());
            }
        }
        h.avatar.setText(msg.getUsername(),h.avatar,url);
        h.time.setVisibility(msg.isShowTime() ? View.VISIBLE : View.GONE);
        Utils.setLoginTime(mContext, h.time, msg.getMsgTime());
        if (h.mNickname != null) {
            if (!isGroup && !TextUtils.equals("everyone",msg.getChatId())) {
                h.mNickname.setVisibility(View.GONE);
            } else {//group
                h.mNickname.setVisibility(View.VISIBLE);
                String name = msg.getUsername();
                if (!TextUtils.isEmpty(tempAvatar.get(msg.getUserId()))) {
                    name = tempName.get(msg.getUserId());
                }
                h.mNickname.setText(name);
            }
        }
        return convertView;
    }

    /**
     * The message display and operating system
     * @param msg The message
     * */
    private void showSystemMsg(Holder h, ChatMsg msg) {
        h.time.setVisibility(View.VISIBLE);
        if (msg.getType() == 50000) {
            h.icon.setVisibility(View.GONE);
            if (TextUtils.isEmpty(msg.getContent())) {
                h.time.setText(mContext.getResources().getString(R.string.msg_msgunknown, msg.getUsername(), mContext.getString(R.string.msg_incompatible)));
            } else {
                h.time.setText(mContext.getResources().getString(R.string.msg_msgunknown, msg.getUsername(), msg.getContent()));
            }
        } else {
            h.icon.setVisibility(View.GONE);
            h.time.setText(msg.getContent());
        }
    }

    /**
     * Display and manipulate share information
     * @param msg message
     * */
    private void showShareMsg(Holder h, final ChatMsg msg) {
        if (TextUtils.isEmpty(msg.getShareTitle())) {
            h.shopName.setVisibility(View.GONE);
            h.shopAddress.setTextSize(14);
        } else {
            h.shopName.setVisibility(View.VISIBLE);
            h.shopName.setText(msg.getShareTitle());
            h.shopAddress.setTextSize(12);
        }

        h.shopImage.setText(msg.getShareFriendName(),h.shopImage, msg.getShareThumb());
        h.shopAddress.setText(msg.getContent());
        h.leftLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Utils.clickUrl(msg.getShareUrl(), mContext);
                } catch (Exception e) {
                    Utils.clickUrl(msg.getShareUrl(), mContext);
                }

            }
        });
    }

    /**
     * Display and operation business card message
     * @param h ViewHolder
     * @param msg message
     * */
    private void showCardMsg(Holder h, final ChatMsg msg) {
        h.shopImage.setText(msg.getThirdName(),h.shopImage, msg.getThirdImage());
//        h.shopAddress.setText(msg.getCardSign());
        h.shopAddress.setText(mContext.getString(R.string.chatting_card_hint));
        h.shopName.setText(msg.getThirdName());
        h.leftLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserBaseVo vo = new UserBaseVo();
                vo.setUsername(msg.getThirdName());
                vo.setLocalId(msg.getThirdId());
                vo.setThumb(msg.getThirdImage());
                vo.setGender(msg.getThirdGender());
                vo.setFriendLog(msg.getFriendLog());
                intentPeopleDetailUI(vo);

            }
        });
    }

    /**
     * The message display and manipulate images
     * @param h ViewHolder
     * @param msg message
     * */
    private void showImgMsg(Holder h, final ChatMsg msg) {
        Bitmap bitmap = BitmapFillet.fillet(BitmapFillet.ALL, BitmapUtils.Base64StringToBitmap(msg.getCover()), 15);
        h.msgImage.setImageBitmap(bitmap);
        if (h.imageUploadLinear != null) {
            h.imageUploadLinear.setTag(msg.getMessageId());
            if (msg.getSend() != 2) {
                h.imageUploadLinear.setVisibility(View.GONE);
            } else {
                h.imageUploadLinear.setVisibility(View.VISIBLE);
            }
        }
        h.leftLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ScanLargePic.class);
                intent.putStringArrayListExtra("picList", imagePathList);
                intent.putExtra("uid", NextApplication.myInfo.getLocalId());
                int position = 0;
                String url = msg.getLocalUrl();
                if (TextUtils.isEmpty(url)) {
                    url = msg.getContent();
                } else {
                    File file = new File(url);
                    if (file.exists()) {
                        url = "file://" + msg.getLocalUrl();
                    } else {
                        url = msg.getContent();
                    }
                }
                for (int i = 0; i < imagePathList.size(); i++) {
                    if (imagePathList.get(i).equals(url)) {
                        position = i;
                        break;
                    }
                }
                intent.putExtra("position", position);
                intent.putExtra("isOurSelf", false);
                mContext.startActivity(intent);
                Utils.openNewActivityAnim((Activity) mContext, false);
            }
        });

    }

    /**
     * Display and manipulate text messages
     * @param h ViewHolder
     * @param msg message
     * @param type text
     * */
    private void showTextMsg(Holder h, final ChatMsg msg,int type) {
        if (type == LEFT_TEXT) {
            h.leftLinear.setBackgroundResource(R.drawable.chatting_left_bg);
        } else {
            h.leftLinear.setBackgroundResource(R.drawable.chatting_right_bg);
        }
        final CharSequence charSequence = NextApplication.mSmileyParser.addSmileySpans1(msg.getContent());
        h.content.setText(charSequence);
        h.content.setMovementMethod(CustomLinkMovementMethod.getInstance(mContext));
        h.content.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                CustomLinkMovementMethod.isChattingLongClick = true;
                return onLongClickLogic(msg);
            }
        });
        h.leftLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (System.currentTimeMillis() - clickTimes < 500 && msg.getMsgTime() == currentMsgTime) {
                    Intent intent = new Intent(mContext, ShowTextUI.class);
                    intent.putExtra("text", charSequence.toString());
                    mContext.startActivity(intent);
                    Utils.openNewActivityAnim((Activity) mContext, false);
                }
                clickTimes = System.currentTimeMillis();
                currentMsgTime = msg.getMsgTime();
            }
        });
    }

    /**
     * In the chat list display file information
     * @param msg    message
     * @param isSend If the sender
     */
    private void showFileInfo(Holder h, final ChatMsg msg, boolean isSend) {
        if (msg != null) {
            // Zero cross 1 upload fails on 2 uploaded successfully cancel the upload | | download 4 not 5 6 7 download failed download successfully 8 cancel the download
            String state = "";
            h.leftLinear.setTag(msg.getMessageId());
            if (msg.isOffLineMsg()) {
                switch (msg.getInviteType()) {
                    case -1:
                        state = mContext.getString(R.string.chatting_waiting_receive);
                        break;
                    case 0:
                        state = mContext.getString(R.string.chatting_sending);
                        break;
                    case 1:
                        state = mContext.getString(R.string.chatting_send_failure);
                        break;
                    case 2:
                        state = mContext.getString(R.string.chatting_send_success);
                        break;
                    case 3:
                        state = mContext.getString(R.string.chatting_cancel_sending);
                        break;
                    case 4:
                        state = mContext.getString(R.string.chatting_did_not_receive);
                        break;
                    case 5:
                        state = mContext.getString(R.string.chatting_receiveing);
                        break;
                    case 6:
                        state = mContext.getString(R.string.chatting_accept_failure);
                        break;
                    case 7:
                        state = mContext.getString(R.string.chatting_receive_success);
                        break;
                    case 8:
                        state = mContext.getString(R.string.chatting_cancel_receiving);
                        break;
                }
            } else {
                switch (msg.getInviteType()) {
                    case 0:
                        state = mContext.getString(R.string.chatting_cross);
                        break;
                    case 1:
                        state = mContext.getString(R.string.chatting_send_failure);
                        break;
                    case 2:
                        state = mContext.getString(R.string.chatting_has_been_sent);
                        break;
                    case 3:
                        state = mContext.getString(R.string.chatting_cancel_sending);
                        break;
                    case 4:
                        state = mContext.getString(R.string.chatting_did_not_download);
                        break;
                    case 5:
                        state = mContext.getString(R.string.chatting_downloading);
                        break;
                    case 6:
                        state = mContext.getString(R.string.chatting_download_failed);
                        break;
                    case 7:
                        state = mContext.getString(R.string.chatting_have_download);
                        break;
                    case 8:
                        state = mContext.getString(R.string.chatting_cancel_download);
                        break;
                }
            }
            h.imgFileLogo.setImageResource(R.drawable.add_friend_by_contact);//File icon
            h.txtFileName.setText(msg.getShareTitle());
            h.txtFileSize.setText(FileSizeUtils.formatFileSize(Long.parseLong(msg.getNumber())));
            h.imgFileLogo.setTag(msg.getMessageId());
            Utils.showFileIcon(mContext, msg.getShareTitle(), msg.getLocalUrl(), h.imgFileLogo);
            if (msg.getInviteType() == 0 || msg.getInviteType() == 5)//Need to display the current progress on the cross and download
            {
                h.txtFileSendState.setVisibility(View.GONE);
                h.progressbarFile.setVisibility(View.VISIBLE);
                int percent = (int) ((msg.getNewprogress() * 100) / (Long.parseLong(msg.getNumber()) * 1.0f));
                h.progressbarFile.setProgress(percent);
            } else {
                h.progressbarFile.setVisibility(View.GONE);
                h.txtFileSendState.setVisibility(View.VISIBLE);
                if (msg.getInviteType() == 4 || msg.getInviteType() == 6 || msg.getInviteType() == 8) {   //Did not download, download failed, cancel the download
                    if (msg.getCreateTime() > 0) {     //One to one message file
                        long time = msg.getCreateTime() + 7 * 24 * 3600 - System.currentTimeMillis() / 1000;
                        if (time <= 0) {      //Has the failure
                            h.txtFileSendState.setText(mContext.getString(R.string.file_outdate));
                        } else {
                            h.txtFileSendState.setText(Utils.setExpireTime(mContext,time));
                        }
                    } else {      //Group group chat
                        h.txtFileSendState.setText(mContext.getString(R.string.permanent));
                    }
                } else {
                    h.txtFileSendState.setText(state);
                }

            }
            h.leftLinear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (msg.isOffLineMsg()) {                  //No network chat
                        Intent intent = new Intent(mContext, ChatFileInfoUI.class);
                        intent.putExtra("name", msg.getShareTitle());
                        intent.putExtra("fileUrl", msg.getContent());
                        intent.putExtra("isOffLine", true);
                        intent.putExtra("state", msg.getInviteType());
                        intent.putExtra("msgid", msg.getMessageId());
                        intent.putExtra("currentLength", msg.getNewprogress());
                        intent.putExtra("number", Long.parseLong(msg.getNumber()));

                        mContext.startActivity(intent);
                        Utils.openNewActivityAnim((Activity) mContext, false);
                    } else {                            //A web chat
                        Intent intent = new Intent(mContext, ChatFileInfoUI.class);
                        intent.putExtra("name", msg.getShareTitle());
                        intent.putExtra("time", msg.getCreateTime());
                        intent.putExtra("fileUrl", msg.getContent());
                        intent.putExtra("localUrl", msg.getLocalUrl());
                        intent.putExtra("number", Long.parseLong(msg.getNumber()));
                        intent.putExtra("state", msg.getInviteType());
                        intent.putExtra("msgid", msg.getMessageId());
                        intent.putExtra("currentLength", msg.getNewprogress());
                        intent.putExtra("chatid", msg.getChatId());
                        intent.putExtra("fileid", msg.getThirdId());
                        intent.putExtra("collectState", msg.getDatingSOSId());//Collect state
                        intent.putExtra("sourceType", msg.getCreateTime() > 0 ? 0 : 1);//Collect state
                        mContext.startActivity(intent);
                        Utils.openNewActivityAnim((Activity) mContext, false);
                    }
                }
            });
        }
    }


    /**
     * Long press event
     */
    private boolean onLongClickLogic(final ChatMsg msg) {
        String title = msg.getUsername();
        int itemArrayId = 0;
        if (msg.getType() == 0) {//The text
            itemArrayId = R.array.copy_del_text;
        } else if (msg.getType() == 1009) {  //The text
            if (msg.getInviteType() == 0 || msg.getInviteType() == 1 || msg.getInviteType() == 3)//On the cross and upload fail, cancel the upload Can't collect
            {
                itemArrayId = R.array.copy_del_only_text;
            } else if (msg.getDatingSOSId() == 1) {//Already collected
                itemArrayId = R.array.cancel_fav_del_list_file;
            } else {
                itemArrayId = R.array.fav_del_list_file;
            }
        } else if (msg.getType() == 2) {
            boolean is_mode_in_call = MySharedPrefs.readBooleanNormal(mContext, MySharedPrefs.FILE_USER, MySharedPrefs.AUDIO_MODE);
            if (is_mode_in_call) {
                itemArrayId = R.array.speaker_mode_list;
            } else {
                itemArrayId = R.array.receiver_mode_list;
            }
        }  else {
            itemArrayId = R.array.copy_del_only_text;
        }

        MyDialogFragment mdf = new MyDialogFragment(MyDialogFragment.DIALOG_LIST, itemArrayId);
        mdf.setItemClickCallback(new MyDialogFragment.ItemClickCallback() {
            @Override
            public void itemClickCallback(int position) {
                selectedList.clear();
                switch (position) {
                    case 0://copy
                        if (msg.getType() == 1009)//Document collection or cancel the collection
                        {
                            if (msg.getInviteType() == 0 || msg.getInviteType() == 1 || msg.getInviteType() == 3)//On the cross and upload fail, cancel the upload Can't collect
                            {
                                forwarding = false;
                                delete = true;
                                selectedItem(msg);
                                notifyDataSetChanged();
                            }
                            else{
                                requestFavFile(msg);
                            }
                            return;
                        }else if (msg.getType() == 2){
                            boolean is_mode_in_call = MySharedPrefs.readBooleanNormal(mContext, MySharedPrefs.FILE_USER, MySharedPrefs.AUDIO_MODE);
                            if (is_mode_in_call) {
                                MySharedPrefs.writeBoolean(mContext, MySharedPrefs.FILE_USER, MySharedPrefs.AUDIO_MODE, false);
                                MyToast.showToast(mContext, mContext.getString(R.string.change_speaker_mode));
                            } else {
                                MySharedPrefs.writeBoolean(mContext, MySharedPrefs.FILE_USER, MySharedPrefs.AUDIO_MODE, true);
                                MyToast.showToast(mContext, mContext.getString(R.string.change_receiver_mode));
                            }
                            return;
                        }
                        else if(msg.getType() == 0){
                            ClipboardManager cmb = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                            cmb.setText(msg.getContent().concat("  "));
                            MyToast.showToast(mContext, mContext.getString(R.string.copy_success));
                            return;
                        }
                        else{
                            forwarding = false;
                            delete = true;
                            selectedItem(msg);
                            notifyDataSetChanged();
                        }
                        break;
                    case 1://Delete
                        forwarding = false;
                        delete = true;
                        selectedItem(msg);
                        notifyDataSetChanged();
                        break;
                }
                if (listener != null) {
                    listener.onSceneListener(delete);
                }

            }
        });
        mdf.show(((FragmentActivity) mContext).getSupportFragmentManager(), "mdf");

        return true;
    }

    /**
     * resend
     */
    private void reSend(final ChatMsg msg, final int type) {
        MyPopupWindow.showDialogList(mContext, msg.getUsername(), new String[]{mContext.getString(R.string.resend)},new DialogItemClickListener(){
            @Override
            public void onItemClickListener(int position) {
                if (kick || dismiss) {
                    //If have been T or dissolution is not action
                    return;
                }
                msg.setSend(2);//Resend state
                boolean status = XmppMessageUtil.getInstance().reSend(type, msg);
                if (type == 3 || type == 11 || type == 19 || type == 29) {

                } else {
                    msg.setSend(status ? 1 : 0);//Resend state
                }
                notifyDataSetChanged();
            }
        });
    }

    /**
     * Voice broadcast logic
     * @param h ViewHolder
     * @param position position
     * @param type Message type
     */
    private void setAudioContent(Holder h, final int position, final int type) {
        h.audioTimes.setText(mList.get(position).getSecond() + "''");
        final ImageView audioIcon = h.audioIcon;
        if (type == LEFT_AUDIO) {
            if (mList.get(position).isAudioPlaying()) {
                audioIcon.setImageResource(R.drawable.anim_songs_voice_left_icon);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && audioIcon.getDrawable() instanceof AnimationDrawable) {
                    ((AnimationDrawable) audioIcon.getDrawable()).start();
                    ((AnimationDrawable) audioIcon.getDrawable()).setOneShot(false);
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && audioIcon.getDrawable() instanceof AnimationDrawable &&((AnimationDrawable) audioIcon.getDrawable()).isRunning()) {
                    ((AnimationDrawable) audioIcon.getDrawable()).stop();
                }
                audioIcon.setImageResource(R.drawable.icon_audio_left3);
            }

        } else {
            if (mList.get(position).isAudioPlaying()) {
                audioIcon.setImageResource(R.drawable.anim_songs_voice_right_icon);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && audioIcon.getDrawable() instanceof AnimationDrawable) {
                    ((AnimationDrawable) audioIcon.getDrawable()).start();
                    ((AnimationDrawable) audioIcon.getDrawable()).setOneShot(false);
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && audioIcon.getDrawable() instanceof AnimationDrawable &&((AnimationDrawable) audioIcon.getDrawable()).isRunning()) {
                    ((AnimationDrawable) audioIcon.getDrawable()).stop();
                }
                audioIcon.setImageResource(R.drawable.icon_audio_right3);
            }

        }
        h.leftLinear.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                try {
                    if (animationDrawable != null) {
                        animationDrawable.stop();
                        animationDrawable = null;
                    }

                    if (mList.get(position).isAudioPlaying())//Is there is the voice of click
                    {
                        mList.get(position).setAudioPlaying(false);
                        if (type == LEFT_AUDIO) {
                            audioIcon.setImageResource(R.drawable.icon_audio_left3);
                        } else {
                            audioIcon.setImageResource(R.drawable.icon_audio_right3);
                        }
                        mPlayer.stop();
                        return;
                    }

                    boolean is_mode_in_call = MySharedPrefs.readBooleanNormal(mContext, MySharedPrefs.FILE_USER, MySharedPrefs.AUDIO_MODE);
                    if (is_mode_in_call) {
                        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                        audioManager.setSpeakerphoneOn(false);
                    } else {
                        audioManager.setMode(AudioManager.MODE_NORMAL);
                        audioManager.setSpeakerphoneOn(true);
                    }


                    mPlayer.reset();
                    if (audioManager.getMode() == AudioManager.MODE_IN_COMMUNICATION) {
                        if (listener != null) {
                            listener.onShow_mode_in_call_tip();
                        }
                        mPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
                    } else {
                        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    }
                    mPlayer.setDataSource(TextUtils.isEmpty(mList.get(position).getLocalUrl()) ? mList.get(position).getContent() : mList.get(position).getLocalUrl());
                    mPlayer.prepare();
                    mPlayer.start();
                    for (int i = 0; i < mList.size(); i++) {
                        if (mList.get(i).getType() == 2 && mList.get(i).isAudioPlaying()) {
                            mList.get(i).setAudioPlaying(false);
                            break;
                        }
                    }
                    mList.get(position).setAudioPlaying(true);
                    notifyDataSetChanged();
                    if (type == LEFT_AUDIO) {
                        audioIcon.setImageResource(R.drawable.anim_songs_voice_left_icon);
                    } else {
                        audioIcon.setImageResource(R.drawable.anim_songs_voice_right_icon);
                    }

                    animationDrawable = (AnimationDrawable) audioIcon.getDrawable();
                    if (animationDrawable != null) {
                        animationDrawable.start();
                        animationDrawable.setOneShot(false);
                    }
                    mPlayer.setOnCompletionListener(new OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            if (audioManager != null) {
                                audioManager.setMode(AudioManager.MODE_NORMAL);
                                audioManager.setSpeakerphoneOn(true);
                            }
                            mPlayer.reset();
                            if (animationDrawable != null) {
                                animationDrawable.stop();
                                animationDrawable = null;
                            }
                            mList.get(position).setAudioPlaying(false);
                            if (type == LEFT_AUDIO) {
                                audioIcon.setImageResource(R.drawable.icon_audio_left3);
                            } else {
                                audioIcon.setImageResource(R.drawable.icon_audio_right3);
                            }
                            notifyDataSetChanged();
                        }

                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    MyToast.showToast(mContext, mContext.getString(R.string.chat_audio_failed));
                }

            }
        });
    }


    public void destory() {
        if (mPlayer != null) {
            if (audioManager != null) {
                audioManager.setMode(AudioManager.MODE_NORMAL);
                audioManager.setSpeakerphoneOn(true);
            }
            try {
                mPlayer.reset();
                if (mPlayer.isPlaying()){
                    mPlayer.stop();
                }
                mPlayer.release();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * Friends details page
     * @param vo The user information
     * */
    private void intentPeopleDetailUI(final UserBaseVo vo) {
        if (TextUtils.isEmpty(vo.getLocalId()) || "0".equals(vo.getLocalId())) {
            return;
        }
        Utils.intentFriendUserInfo((Activity) mContext, vo, false);
    }

    static class Holder {
        LinearLayout leftLinear;
        LinearLayout imageUploadLinear;
        CharAvatarView avatar;
        ImageView msgWarnning;
        ImageView msgImage;
        TextView mNickname;
        TextView time;
        ImageView icon;
        TextView content;
        ProgressBar mNotifBar;

        CharAvatarView shopImage;
        TextView shopName;
        TextView shopAddress;
        TextView audioTimes;
        ImageView audioIcon;

        ImageView selectBox;
        RelativeLayout relativeLayout;

        //file
        ImageView imgFileLogo;
        TextView txtFileName, txtFileSize, txtFileSendState;
        ProgressBar progressbarFile;
    }

    /**
     * Collect cancel collect file interface
     */
    private void requestFavFile(final ChatMsg msg) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
        mProgressDialog = LoadingDialog.showDialog(mContext, null, null);
        mProgressDialog.setCancelable(false);
        NetRequestImpl.getInstance().fileCollect(msg.getThirdId(), msg.getCreateTime(), msg.getContent(), msg.getDatingSOSId(), new RequestListener() {
            @Override
            public void start() {

            }

            @Override
            public void success(JSONObject response) {
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                    mProgressDialog = null;
                }
                if (msg.getDatingSOSId() == 1) {
                    MyToast.showToast(mContext, mContext.getString(R.string.chat_cancel_collection_failed));
                    msg.setDatingSOSId(0);
                    FinalUserDataBase.getInstance().updateChatMsgCollectState(msg.getMessageId(), 0);
                } else {
                    MyToast.showToast(mContext, mContext.getString(R.string.chat_collection_failed));
                    msg.setDatingSOSId(1);
                    FinalUserDataBase.getInstance().updateChatMsgCollectState(msg.getMessageId(), 1);
                }

                if (msg.getDatingSOSId() == 1)//collection
                {
                    if (msg.getInviteType() == 2 || msg.getInviteType() == 7)//Upload successfully, or download successful collection, keep the path, so as to collect the list into the don't have to download
                    {
                        JSONObject json = new JSONObject();
                        try {
                            json.put("state", msg.getInviteType());
                            json.put("localurl", msg.getLocalUrl());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        MySharedPrefs.write(mContext, MySharedPrefs.KEY_FIREFLY_FILEPATH, msg.getThirdId(), json.toString());
                    }
                }
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                    mProgressDialog = null;
                }
                MyToast.showToast(mContext, errorMsg);
            }
        });
    }

    /**
     * Chat page callback
     */
    public interface SceneListener {
        /**
         * @param isDelete Whether to delete Otherwise the forwarding
         */
        void onSceneListener(boolean isDelete);
        /**
         * When the receiver model play hints
         */
        void onShow_mode_in_call_tip();

    }

}
