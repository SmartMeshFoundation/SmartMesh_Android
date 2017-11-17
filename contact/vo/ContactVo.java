package com.lingtuan.firefly.contact.vo;


import com.lingtuan.firefly.custom.contact.ContactItemInterface;
import com.lingtuan.firefly.vo.UserBaseVo;

import java.util.List;

public class ContactVo {
	private List<UserBaseVo> mFriendInfoList;
	private List<ContactItemInterface> mContactList;
	public List<UserBaseVo> getmFriendInfoList() {
		return mFriendInfoList;
	}
	public void setmFriendInfoList(List<UserBaseVo> mGroupInfoList) {
		this.mFriendInfoList = mGroupInfoList;
	}
	public List<ContactItemInterface> getmContactList() {
		return mContactList;
	}
	public void setmContactList(List<ContactItemInterface> mContactList) {
		this.mContactList = mContactList;
	}
}
