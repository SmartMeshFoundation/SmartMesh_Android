package com.lingtuan.firefly.vo;

import android.text.TextUtils;


import com.lingtuan.firefly.util.Utils;

import org.json.JSONObject;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * User information base class
 */
public class UserBaseVo  extends ImageResizer implements Serializable {


	/**The user id*/
	private String localId;

	/**SMT ID*/
	private String mid;

	/**The user name*/
	protected String username;

	/**The distance between the user*/
	private String distance;

	/**The user signature*/
	private String sightml;

	/**address*/
	private String address;

	/**Login time*/
	private long logintime;

	/**age*/
	private String age;

	/**1 is the male  2 for women*/
	private String gender;

	/**Head portrait*/
	private String pic;

	/**The thumbnail*/
	private String thumb;

	/***/
	private String note;

	/**Mobile phone no.*/
	private String phonenumber;

	/**The time stamp*/
	private long dateline  ;

	/**The relative relations (-1: myself.0: strangers;1: friends;2: the second friend)*/
	private int friendLog;

	/**If a man without a net*/
	private boolean offLine = false;

	/**offline information*/
	private boolean offLineFound = false;

	/**Contact person with multi-select*/
	private boolean isChecked=false;

	/**Multiple contacts, can't choose*/
	private boolean cantChecked = false;

	public boolean isOffLineFound() {
		return offLineFound;
	}

	public void setOffLineFound(boolean offLineFound) {
		this.offLineFound = offLineFound;
	}

	public String getLocalId() {
		return localId;
	}

	public void setLocalId(String localId) {
		this.localId = localId;
	}

	public String getMid() {
		return mid;
	}

	public void setMid(String mid) {
		this.mid = mid;
	}

	public String getUsername() {
		return username;
	}

	public void setSightml(String sightml) {
		this.sightml = sightml;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public long getLogintime() {
		return logintime;
	}

	public void setLogintime(long logintime) {
		this.logintime = logintime;
	}

	public String getAge() {
		return age;
	}

	public void setAge(String age) {
		this.age = age;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getPic() {
		return pic;
	}

	public void setPic(String pic) {
		this.pic = pic;
	}

	public String getThumb() {
		return thumb;
	}

	public void setThumb(String thumb) {
		this.thumb = thumb;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getPhonenumber() {
		return phonenumber;
	}

	public void setPhonenumber(String phonenumber) {
		this.phonenumber = phonenumber;
	}

	public long getDateline() {
		return dateline;
	}

	public void setDateline(long dateline) {
		this.dateline = dateline;
	}

	public int getFriendLog() {
		return friendLog;
	}

	public void setFriendLog(int friendLog) {
		this.friendLog = friendLog;
	}

	public boolean isOffLine() {
		return offLine;
	}

	public void setOffLine(boolean offLine) {
		this.offLine = offLine;
	}

	public boolean isChecked() {
		return isChecked;
	}

	public void setChecked(boolean checked) {
		isChecked = checked;
	}

	public boolean isCantChecked() {
		return cantChecked;
	}

	public void setCantChecked(boolean cantChecked) {
		this.cantChecked = cantChecked;
	}

	public String getShowName() {
		if(!TextUtils.isEmpty(getNote())){
			return getNote();
		}
		return username;
	}


	public String getUserName() {
		return Utils.replyEnter(username);
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getDistance() {
		//Conversion accuracy
		distance = TextUtils.isEmpty(distance) ? "0" : distance;
		BigDecimal bg = new BigDecimal(distance);
		double j = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		return String.valueOf(j);
	}

	public void setDistance(String distance) {
		this.distance = distance;
	}

	public String getSightml() {
		if(sightml == null){
			sightml = "";
		}
		return Utils.replyEnter(sightml);
	}

	public UserBaseVo getUserBaseVo(){
		return this;
	}
	
	public UserBaseVo parse(JSONObject obj){
		
		if (obj == null){
			 return null;
		}
		setMid(obj.optString("mid"));
		setLocalId(obj.optString("localid"));
		setAddress(TextUtils.isEmpty(obj.optString("address")) ? obj.optString("birthcity") : obj.optString("address"));
		setUsername(obj.optString("username"));
		if(!TextUtils.isEmpty(getUserName())){
			setUsername(getUserName().replace("\\r", ""));
			setUsername(getUserName().replace("\\n", " "));
		}
		setSightml(obj.optString("sightml"));
		setAge(obj.optString("age"));
		setGender(obj.optString("gender"));
		setPic(obj.optString("pic"));
		setThumb(TextUtils.isEmpty(obj.optString("thumb")) ? buildThumb(obj.optString("pic")) : obj.optString("thumb"));
		setLogintime(obj.optLong("lastlogintime"));
		setDateline(obj.optLong("dateline"));
		setDistance(obj.optString("distance"));
		setNote(obj.optString("note"));
		if(!TextUtils.isEmpty(getNote())){
			setNote(getNote().replace("\\r", ""));
			setNote(getNote().replace("\\n", " "));
		}
		setFriendLog(obj.optInt("friend_log"));
		return this;
	}
}
