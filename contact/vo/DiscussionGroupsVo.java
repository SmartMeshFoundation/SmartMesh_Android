package com.lingtuan.firefly.contact.vo;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.vo.UserBaseVo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Group chat entity class
 */
public class DiscussionGroupsVo {

	private int cid;
	private String name;
	private int mask;
	private List<UserBaseVo> members;
	private int maxNum;
	public int getCid() {
		return cid;
	}

	public void setCid(int cid) {
		this.cid = cid;
	}
	public int getMaxNum() {
		return maxNum;
	}

	public void setMaxNum(int maxNum) {
		this.maxNum = maxNum;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getMask() {
		return mask;
	}

	public void setMask(int mask) {
		this.mask = mask;
	}

	public List<UserBaseVo> getMembers() {
		return members;
	}

	public void setMembers(List<UserBaseVo> members) {
		this.members = members;
	}

	public DiscussionGroupsVo parse(JSONObject obj){
		parse(obj,false);
		return this;
	}
	
	public DiscussionGroupsVo parse(JSONObject obj, boolean withOutMe){
		
		if(obj == null){
			return null;
		}
		
		setCid(obj.optInt("cid"));
		setName(obj.optString("name"));
		setMask(obj.optInt("mask"));
		setMaxNum(obj.optInt("max_num"));
		List<UserBaseVo> mList = new ArrayList<UserBaseVo>();
		UserBaseVo vo;
		JSONArray array = obj.optJSONArray("members");
		
		for (int i = 0; i < array.length(); i++) {
			vo = new UserBaseVo().parse(array.optJSONObject(i));
			if(vo != null){
				if(withOutMe){//Is your own
					if(vo.getLocalId().equals(NextApplication.myInfo.getLocalId())){
						withOutMe = false;
						continue;
					}
				}
				mList.add(vo);
			}
		}
		
		setMembers(mList);
		return this;
	}
	
}
