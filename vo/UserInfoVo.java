package com.lingtuan.firefly.vo;

import android.content.Context;
import android.text.TextUtils;

import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Users' personal information
 */
public class UserInfoVo extends UserBaseVo implements Serializable {

	private static final long serialVersionUID = 1L;

	/**user token*/
	private String token;

	/**The user password*/
	private String password;

	/**Whether in the blacklist (0 not, 1)*/
	private String inblack ;

	/**Strangers blocking state: 1 is a block (true), 0 is open (false) */
	private boolean strangerMask = false ;

	/**Whether it is friends*/
	private int is_friend = 0;

	private String mobile;//Phone number (only return when check your information, if is empty, is not binding mobile phone number)

	private String email;//Email (only in return, check your data if the empty the unbounded email)

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isStrangerMask() {
		return strangerMask;
	}

	public void setStrangerMask(boolean strangerMask) {
		this.strangerMask = strangerMask;
	}

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}


	public String getInblack() {
		return inblack;
	}

	public void setInblack(String inblack) {
		this.inblack = inblack;
	}

	public int getIs_friend() {
		return is_friend;
	}

	public void setIs_friend(int is_friend) {
		this.is_friend = is_friend;
	}

	public UserInfoVo readMyUserInfo(Context mContext){

		String userinfodata = MySharedPrefs.readString(mContext, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_LOGIN_USERINFO);

		if(TextUtils.isEmpty(userinfodata)){
			return null ;
		}
		try {
			JSONObject infoData = new JSONObject(userinfodata);

			setToken(infoData.optString("token"));
			setMid(infoData.optString("mid"));
			setPassword(infoData.optString("password"));
			setLocalId(infoData.optString("localid"));
			setUsername(infoData.optString("username"));

			setSightml(infoData.optString("sightml"));
			setFriendLog(infoData.optInt("friend_log"));
			setAddress(infoData.optString("birthcity"));

			setAge(infoData.optString("age"));
			setGender(infoData.optString("gender"));
			setPic(infoData.optString("pic"));
			setThumb(Utils.buildThumb(infoData.optString("pic")));
			setPhonenumber(infoData.optString("phonenumber"));
			setStrangerMask(TextUtils.equals("1", infoData.optString("stranger_mask")));

			setMobile(infoData.optString("mobile"));
			setEmail(infoData.optString("email"));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return this;
	}

	public UserInfoVo parse(JSONObject obj) {
		if (obj == null){
			return null;
		}
		setToken(obj.optString("token"));
		setMid(obj.optString("mid"));
		setPassword(obj.optString("password"));
		setUsername(obj.optString("username"));
		setLocalId(obj.optString("localid"));

		setNote(obj.optString("note"));
		setSightml(obj.optString("sightml"));
		setAddress(obj.optString("birthcity") );
		setAge(obj.optString("age"));
		setGender(obj.optString("gender"));
		setPic(obj.optString("pic"));
		setThumb(TextUtils.isEmpty(obj.optString("thumb")) ? buildThumb(obj.optString("pic")) : obj.optString("thumb"));
		setLogintime(obj.optLong("lastlogintime"));
		setDistance(obj.optString("distance"));
		setFriendLog(obj.optInt("friend_log"));
		setInblack(obj.optString("inbalck"));
		setPhonenumber(obj.optString("phonenumber"));
		setDateline(obj.optLong("dateline"));
		setStrangerMask(TextUtils.equals("1", obj.optString("stranger_mask")));
		//Whether the current user's friends
		setIs_friend(obj.optInt("is_friend"));

		setMobile(obj.optString("mobile"));
		setEmail(obj.optString("email"));
		return this;
	}

	/**
	 * modify head
	 * @ param thumb thumbnails
	 * @ param PIC a larger version
	 * @ param mContext context
 	 */
	public void updateJsonAvatar(String thumb,String pic,Context mContext){
		String userinfodata = MySharedPrefs.readString(mContext, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_LOGIN_USERINFO);
		try {
			JSONObject obj = new JSONObject(userinfodata);
			obj.put("pic", pic);
			obj.put("thumb", thumb);
			setPic(pic);
			setThumb(thumb);
			MySharedPrefs.write(mContext, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_LOGIN_USERINFO, obj.toString());
			readMyUserInfo(mContext);
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}


	/**
	 * modify head
	 * @ param note a larger version
	 * @ param mContext context
	 */
	public void updateJsonNote(String note,Context mContext){
		String userinfodata = MySharedPrefs.readString(mContext, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_LOGIN_USERINFO);
		try {
			JSONObject obj = new JSONObject(userinfodata);
			obj.put("note", note);
			setNote(note);
			MySharedPrefs.write(mContext, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_LOGIN_USERINFO, obj.toString());
			readMyUserInfo(mContext);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Modify the user information json string
	 */
	public void updateJsonUserInfo(String username, String sightml,String gender,String birthcity,Context mContext){
		String userinfodata = MySharedPrefs.readString(mContext, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_LOGIN_USERINFO);
		try {
			JSONObject obj = new JSONObject(userinfodata);
			obj.put("username", username);
			obj.put("sightml", sightml);
			obj.put("gender", gender);
			obj.put("birthcity", birthcity);
			MySharedPrefs.write(mContext, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_LOGIN_USERINFO, obj.toString());
			readMyUserInfo(mContext);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Modify the binding mobile phone number information
	 */
	public void updateJsonBindMobile(String mobile,Context mContext){
		String userinfodata = MySharedPrefs.readString(mContext, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_LOGIN_USERINFO);
		try {
			JSONObject obj = new JSONObject(userinfodata);
			obj.put("mobile", mobile);
			MySharedPrefs.write(mContext, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_LOGIN_USERINFO, obj.toString());
			readMyUserInfo(mContext);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Modify the binding E-mail information
	 */
	public void updateJsonBindEmail(String email,Context mContext){
		String userinfodata = MySharedPrefs.readString(mContext, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_LOGIN_USERINFO);
		try {
			JSONObject obj = new JSONObject(userinfodata);
			obj.put("email", email);
			MySharedPrefs.write(mContext, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_LOGIN_USERINFO, obj.toString());
			readMyUserInfo(mContext);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
