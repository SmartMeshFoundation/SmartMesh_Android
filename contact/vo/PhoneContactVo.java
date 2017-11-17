package com.lingtuan.firefly.contact.vo;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import org.json.JSONObject;

/**
 * Address book friends platform use showName display name with a third party
 */
public class PhoneContactVo implements Parcelable {

	private String id;//OpenId phone number or a third party
	private String name;//nickname
	private String note;//note
	private int type;//1. Mobile phone address book, 2. Sina weibo friends, 3. Tencent weibo friends
	private int relation;//Good friend relationship between 0 to users is not around you, 1, 2 friends for not friends
	private int uid;
	
	public PhoneContactVo(){}
	
	public String getShowName(){
		if(TextUtils.isEmpty(note)){
			return name;
		}else{
			return note;
		}
	}
	
	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public int getRelation() {
		return relation;
	}
	public void setRelation(int relation) {
		this.relation = relation;
	}
	public int getUid() {
		return uid;
	}
	public void setUid(int uid) {
		this.uid = uid;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	@Override
	public int describeContents() {

		return 0;
	}

	public PhoneContactVo parse(JSONObject json){
		if(json == null) return null;
		
		uid = json.optInt("destid");
		relation = json.optInt("relation");
		id = json.optString("friendid");
		return this;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(name);
		dest.writeString(note);
		dest.writeInt(type);
		dest.writeInt(relation);
		dest.writeInt(uid);
	}

	public static final Parcelable.Creator<PhoneContactVo> CREATOR = new Parcelable.Creator<PhoneContactVo>() {
		public PhoneContactVo createFromParcel(Parcel in) {
			return new PhoneContactVo(in);
		}

		public PhoneContactVo[] newArray(int size) {
			return new PhoneContactVo[size];
		}
	};

	private PhoneContactVo(Parcel in) {
		id = in.readString();
		name = in.readString();
		note = in.readString();
		type = in.readInt();
		relation = in.readInt();
		uid = in.readInt();
	}
}
