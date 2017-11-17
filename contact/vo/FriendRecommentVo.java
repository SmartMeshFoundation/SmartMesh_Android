package com.lingtuan.firefly.contact.vo;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

import java.util.UUID;

/**
 * Friend recommended entity class xmpp msgtype = 23
 */
public class FriendRecommentVo implements Parcelable {

	private String msgId;// The default message body ID is UUID
	private String friendId;// Address book or weibo id (corresponding typelist passed in the data, if is the address book transfer a phone number, if it is weibo friends, send weibo id)
	private String uid;//Is uid contacts, sina, tencent weibo about your sister account have been registered and binding the uid (if base value is 0, the user has not registered about your sister destid returns an empty)
	private String username;// User nickname (type value not only to 0)
	private String pic;// Avatars artwork (type value not only to 0)
	private String thumb;// User image thumbnails (type value not only to 0)
	private String thirdName;//The third party communications nickname Such as a directory name Or weibo nickname
	private int type;// 1.Mobile address book, 2. Sina weibo friends, 3. Tencent weibo friends
	private long time;//time
	private int unread;//Unread item number
	private boolean isAgree;//Whether have agreed to

	
	public String getThirdName() {
		return thirdName;
	}

	public void setThirdName(String thirdName) {
		this.thirdName = thirdName;
	}

	public boolean isAgree() {
		return isAgree;
	}

	public void setAgree(boolean isAgree) {
		this.isAgree = isAgree;
	}

	public long getUnread() {
		return unread;
	}

	public void setUnread(int unread) {
		this.unread = unread;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public FriendRecommentVo(){}
	
	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	public String getFriendId() {
		return friendId;
	}

	public void setFriendId(String friendId) {
		this.friendId = friendId;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
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

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	/**
	 * Information for parsing XMPP
	 */
	public FriendRecommentVo parseXmpp(String json) {
		try {

			JSONObject obj = new JSONObject(json);
			uid = obj.optString("userid");
			username = obj.optString("username");
			thumb = obj.optString("userimage");
			type = obj.optInt("soucetype");
			friendId = obj.optString("sourceid");

		} catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}

	/**
	 * Parsing the Http message is special
	 */
	public FriendRecommentVo parseHttp(String json) {
		try {
			JSONObject obj = new JSONObject(json);
			uid = obj.optString("destid");
			username = obj.optString("username");
			thumb = obj.optString("thumb");
			friendId = obj.optString("friendid");
			msgId = UUID.randomUUID().toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}

	@Override
	public int describeContents() {

		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(msgId);
		dest.writeString(friendId);
		dest.writeString(uid);
		dest.writeString(username);
		dest.writeString(pic);
		dest.writeString(thumb);
		dest.writeString(thirdName);
		dest.writeInt(type);
		dest.writeLong(time);
		dest.writeInt(unread);
		dest.writeInt(isAgree ? 1 : 0);
	}

	public static final Parcelable.Creator<FriendRecommentVo> CREATOR = new Parcelable.Creator<FriendRecommentVo>() {
		public FriendRecommentVo createFromParcel(Parcel in) {
			return new FriendRecommentVo(in);
		}

		public FriendRecommentVo[] newArray(int size) {
			return new FriendRecommentVo[size];
		}
	};

	private FriendRecommentVo(Parcel in) {
		msgId = in.readString();
		friendId = in.readString();
		uid = in.readString();
		username = in.readString();
		pic = in.readString();
		thumb = in.readString();
		thirdName = in.readString();
		type = in.readInt();
		time = in.readLong();
		unread = in.readInt();
		isAgree = in.readInt() == 1 ? true : false;
	}
}
