package com.lingtuan.firefly.contact.vo;

import java.util.List;

/**
 * The address book group
 */
public class PhoneContactGroupVo {

	private String groupName;
	private int type;
	private List<PhoneContactVo> mContactList;
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}


	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public List<PhoneContactVo> getContactList() {
		return mContactList;
	}

	public void setContactList(List<PhoneContactVo> mList) {
		this.mContactList = mList;
	}

}
