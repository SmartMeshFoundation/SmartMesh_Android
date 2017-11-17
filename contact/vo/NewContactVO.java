package com.lingtuan.firefly.contact.vo;


import com.lingtuan.firefly.custom.contact.ContactItemInterface;
import com.lingtuan.firefly.vo.UserBaseVo;

public class NewContactVO extends UserBaseVo implements ContactItemInterface {

	private static final long serialVersionUID = 1L;
	private String fullName;
	@Override
	public String getItemForIndex() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName=fullName;
	}

	@Override
	public String getDisplayInfo() {
		return username;
	}

}
