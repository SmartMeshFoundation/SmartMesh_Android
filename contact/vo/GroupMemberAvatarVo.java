package com.lingtuan.firefly.contact.vo;

import com.lingtuan.firefly.vo.UserBaseVo;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Members of the group entity class
 */
public class GroupMemberAvatarVo implements Serializable {

	private int gender;
	private String image;
	private String username;

	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public GroupMemberAvatarVo parse(JSONObject obj){
		if(obj == null){
			return null;
		}
		
		setGender(obj.optInt("gender"));
		setImage(obj.optString("image"));
		setUsername(obj.optString("username"));

		return this;
	}
	
	/**
	 * The vo only thumb DiscussGroupImageView use with gender
	 */
	public UserBaseVo getUserBaseVo(){
		UserBaseVo vo = new UserBaseVo();
		vo.setThumb(image);
		vo.setGender(gender + "");
		vo.setUsername(username);
		return vo;
	}
	
}
