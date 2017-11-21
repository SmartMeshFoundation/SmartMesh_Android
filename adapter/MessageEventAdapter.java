package com.lingtuan.firefly.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.custom.ChatMsgComparable;
import com.lingtuan.firefly.custom.DiscussGroupImageView;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.vo.ChatMsg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * My information page adapter
 */
public class MessageEventAdapter extends BaseAdapter {

    private List<ChatMsg> mList;
    private Context mContext;

    public MessageEventAdapter(List<ChatMsg> mList, Context mContext) {
        this.mList = mList;
        this.mContext = mContext;
        if (mList == null) {
            mList = new ArrayList<>();
        }
    }

    public void updateList(List<ChatMsg> mList) {
        this.mList = mList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (mList == null) {
            return 0;
        }
        return mList.size();
    }

    public void addChatMsg(ChatMsg msg) {
        if (msg == null) {
            return;
        }
        boolean isAdd = false;
        if (mList == null) {
            mList = new ArrayList<>();
            mList.add(msg);
            notifyDataSetChanged();
            return;
        } else if (mList.isEmpty()) {
            mList.add(msg);
            notifyDataSetChanged();
            return;
        }
        int unRead = 0;
        int count = mList.size();
        for (int i = 0; i < count; i++) {
            if (TextUtils.equals(msg.getChatId(), mList.get(i).getChatId())) {
                unRead = mList.get(i).getUnread();
                int isAtGroupMeOld = mList.get(i).isAtGroupMe();
                boolean isTop = mList.get(i).isTop();
                long topTime = mList.get(i).getTopTime();
                mList.remove(i);
                msg.setTop(isTop);
                msg.setTopTime(topTime);
                unRead = unRead + msg.getUnread();
                msg.setUnread(unRead);
                if (msg.isAtGroupMe() <= 0) {
                    msg.setAtGroupMe(isAtGroupMeOld);
                }
                mList.add(0, msg);
                isAdd = true;
                break;
            }
        }
        if (!isAdd) {
            mList.add(0, msg);
        }

        Collections.sort(mList, new ChatMsgComparable());
        notifyDataSetChanged();
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
    public View getView(int position, View convertView, ViewGroup parent) {

        Holder h;
        ChatMsg msg = mList.get(position);
        if (convertView == null) {
            h = new Holder();
            convertView = View.inflate(mContext, R.layout.item_msg_evnet, null);
            h.bg = (LinearLayout) convertView.findViewById(R.id.item_bg);
            h.avatarShape = (ImageView) convertView.findViewById(R.id.item_avatar);
            h.listenerIcon = (ImageView) convertView.findViewById(R.id.item_msg_event_listener);
            h.content = (TextView) convertView.findViewById(R.id.item_content);
            h.time = (TextView) convertView.findViewById(R.id.item_times);
            h.nickname = (TextView) convertView.findViewById(R.id.item_nickname);
            h.unread = (TextView) convertView.findViewById(R.id.item_unread);
            h.atGroupMe = (TextView) convertView.findViewById(R.id.item_at);
            h.unReadIcon = (ImageView) convertView.findViewById(R.id.item_unread_icon);
            h.groupImageView = (DiscussGroupImageView) convertView.findViewById(R.id.group_avatar);
            h.y_m_d = (ImageView) convertView.findViewById(R.id.y_m_d);
            h.vipLevel = (ImageView) convertView.findViewById(R.id.vipLevel);
            convertView.setTag(h);
        } else {
            h = (Holder) convertView.getTag();
        }
        if (msg.isTop()) {
            h.bg.setBackgroundResource(R.drawable.scrollview_top_item_bg);
        } else {
            h.bg.setBackgroundResource(R.drawable.scrollview_item_bg);
        }

        String nickname = msg.getUsername();
        String content = msg.getContent();
        String url = msg.getUserImage();
        int resId = 0;
        h.groupImageView.setVisibility(View.GONE);
        h.avatarShape.setVisibility(View.VISIBLE);
        if (msg.isAtGroupMe() == 1) {
            h.atGroupMe.setVisibility(View.VISIBLE);//I was @
            h.atGroupMe.setText(mContext.getString(R.string.chat_at_me));
        } else if (msg.isAtGroupMe() == 2) {
            h.atGroupMe.setVisibility(View.VISIBLE);//Someone @ all members
            h.atGroupMe.setText(mContext.getString(R.string.chat_at_all));
        } else {
            h.atGroupMe.setVisibility(View.GONE);//No one @ I
        }
        if (msg.isSystem()) {
            int type = msg.getType();
            if (type == 0) {//A friend request
                url = "drawable://" + R.drawable.icon_msg_contact_add;
                resId = R.drawable.icon_msg_contact_add;
                nickname = mContext.getString(R.string.chat_friend_notify);
                content = mContext.getResources().getString(R.string.contact_add_content, msg.getUsername());
            }
        } else {
            if ("everyone".equals(msg.getChatId()))
            {
                url = "drawable://" + R.drawable.icon_everyone;
                nickname = mContext.getResources().getString(R.string.everyone);
                resId = R.drawable.icon_everyone;
            }
            content = getContent(msg, content);
        }
        Utils.formatUnreadCount(h.unread, msg.getUnread());
        h.unReadIcon.setVisibility(View.GONE);
        h.listenerIcon.setVisibility(View.GONE);
        h.unread.setVisibility(msg.getUnread() > 0 ? View.VISIBLE : View.GONE);
        if (msg.getGroupMask()) {
            if ("everyone".equals(msg.getChatId()) || "system-0".equals(msg.getChatId()) || "system-1".equals(msg.getChatId()) || "system-3".equals(msg.getChatId()) || "system-4".equals(msg.getChatId())) {
                //If is the invitation message or group system, there is no shielding function
            } else {
                h.listenerIcon.setVisibility(View.VISIBLE);
                h.unread.setVisibility(View.GONE);
                if (msg.getUnread() > 0) {
                    h.unReadIcon.setVisibility(View.VISIBLE);

                }
            }
        }
        if (msg.isGroup() || msg.getMsgTypeInt() == 3) {
            h.avatarShape.setVisibility(View.GONE);
            h.groupImageView.setVisibility(View.VISIBLE);
            if (msg.getMemberAvatarUserBaseList() != null) {
                h.groupImageView.setMember(msg.getMemberAvatarUserBaseList());
            }
        } else {
            if (!TextUtils.isEmpty(url)) {
                if (url.startsWith("drawable://")) {
                    NextApplication.displayCircleImage(h.avatarShape, null);
                    h.avatarShape.setImageResource(resId);
                } else {
                    NextApplication.displayCircleImage(h.avatarShape, url);
                }
            }else{
                h.avatarShape.setImageResource(R.drawable.icon_default_avater);
            }
        }
        h.nickname.setText(nickname);
        final CharSequence charSequence = NextApplication.mSmileyParser.addSmileySpans1(content);
        h.content.setText(charSequence);

        Utils.setLoginTime(mContext, h.time, msg.getMsgTime());

        return convertView;
    }

    private String getContent(ChatMsg msg, String content) {
        switch (msg.getType()) {
            case 1:
                content = mContext.getString(R.string.msg_event_show_image);

                break;
            case 2:
                content = mContext.getString(R.string.msg_event_show_audio);

                break;

            case 6:
                content = mContext.getString(R.string.msg_event_show_card);

                break;

            case 15:
                content = mContext.getString(R.string.discuss_group_dismiss);

                break;

            case 103://Share information
                content = mContext.getResources().getString(R.string.msg_event_invite_msg103, msg.getContent());

                break;

            case 105:
                content = mContext.getResources().getString(R.string.msg_event_invite_msg105);
                break;

            case 1009://file
                content = mContext.getResources().getString(R.string.msg_event_invite_msg1009);
                break;
            case 50000://Not compatible with high version of the news
                if (TextUtils.isEmpty(content)) {
                    content = mContext.getResources().getString(R.string.msg_event_invite_msgunknown, mContext.getString(R.string.chatting_incompatible_message));
                } else {
                    content = mContext.getResources().getString(R.string.msg_event_invite_msgunknown, content);
                }
                break;
            case 50001://Unified news across app group cannot receive
            case 50002:
                content = mContext.getResources().getString(R.string.msg_event_invite_msgunknown, content);
                break;
        }
        return content;
    }

    static class Holder {
        LinearLayout bg;
        ImageView avatarShape, y_m_d, vipLevel;
        ImageView listenerIcon;
        TextView time;
        TextView content;
        TextView nickname;
        TextView unread;
        ImageView unReadIcon;
        DiscussGroupImageView groupImageView;
        TextView atGroupMe;
    }
}
