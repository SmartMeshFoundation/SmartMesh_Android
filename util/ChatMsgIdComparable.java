package com.lingtuan.firefly.util;

import com.lingtuan.firefly.vo.ChatMsg;

import java.util.Comparator;

/**
 * Chat content according to the time order
 */
public class ChatMsgIdComparable implements Comparator<ChatMsg> {
	@Override
	public int compare(ChatMsg lhs, ChatMsg rhs) {
		return lhs.getId() > rhs.getId()? 1 : -1;
	}
}
