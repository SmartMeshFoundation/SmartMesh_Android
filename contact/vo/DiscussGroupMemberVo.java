package com.lingtuan.firefly.contact.vo;

import com.lingtuan.firefly.vo.UserBaseVo;

import java.util.ArrayList;

public class DiscussGroupMemberVo {

	public ArrayList<UserBaseVo> data1 ;
	public ArrayList<UserBaseVo> showdata1 ;
	private static DiscussGroupMemberVo instance;
	private DiscussGroupMemberVo() {
		data1 = new ArrayList<>();
		showdata1 = new ArrayList<>();
	}
	public static synchronized DiscussGroupMemberVo getInstance() {
		if (instance == null) {
			instance = new DiscussGroupMemberVo();
		}
		return instance;
	}
}
