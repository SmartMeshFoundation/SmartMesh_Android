package com.lingtuan.firefly.custom;

import com.lingtuan.firefly.vo.ChatMsg;

import java.util.Comparator;

/**
 * Chat content according to the time order
 */
public class ChatMsgComparable implements Comparator<ChatMsg> {
	@Override
	public int compare(ChatMsg lhs, ChatMsg rhs) {
		if(lhs.isTop()&&rhs.isTop()){
			//Both are system information
			long lhsTime = lhs.getMsgTime()>lhs.getTopTime()?lhs.getMsgTime():lhs.getTopTime();
			long rhsTime = rhs.getMsgTime()>rhs.getTopTime()?rhs.getMsgTime():rhs.getTopTime();
			return lhsTime < rhsTime ? 1 : -1;
		}

		if(lhs.isTop()){
			return -1;
		}
		if(rhs.isTop()){
			return 1;
		}

		return lhs.getMsgTime() < rhs.getMsgTime() ? 1 : -1;
	}

	
}
