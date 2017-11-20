package com.lingtuan.firefly.vo;

import android.text.TextUtils;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.contact.vo.GroupMemberAvatarVo;
import com.lingtuan.firefly.util.ChatMsgIdComparable;
import com.lingtuan.firefly.xmpp.XmppUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.MsgType;

public class ChatMsg implements Serializable {

	private static final long serialVersionUID = 1L;
	private int _id;//Form on the field
	private String username;
	private String realname;
	private String userId;//The user's UID
	private String userImage;
	private int gender;
	private int type;
	private String content;

	private String cover;//Base64 pictures
	private String second;//The voice of seconds
	private String lon;//longitude
	private String lat;//latitude

	private String thirdName;
	private String thirdImage;
	private String thirdId;
	private String thirdGender;
	private String shopAddress;//Store address and location with unity

	private String cardSign;

	private long msgTime;

	private String chatId;//The current chat object id

	private int unread; //0 as read, 1 is unread

	private int send;//1 to send success, 0 for failure, 2 for sending

	private int fileSendState;//Upload 0 on the cross, 1 failed, 2 uploaded successfully, 3 cancel upload | | 4 download not, 5, 6 in the download failed, 7 download success, 8 cancel the download

	private boolean system;//1 is system, 0 is information
	private boolean hidden;//1 is hiding, 0 is display
	private boolean agree;//agree

	private String localUrl;

	private int createId;
	private String createName;
	private String createImage;
	private int createAge;
	private int createGender;
	private String creategSign;
	private long createTime;

	private int sort;//Invitation please way beer and skittles
	private String inviteMsg;//Invite language
	private int guest;//Treat way

	private int inviteId;

	private String groupName;
	private String groupImage;//Used to group the difference in the group chat
	private String groupId;
	private boolean group;//Whether the group
	private boolean groupMask;//Whether shielding remind information
	private List<GroupMemberAvatarVo> memberAvatarList;
	private List<UserBaseVo> memberAvatarUserBaseList;
	private boolean dismissGroup;//Whether the dissolution of groups
	private boolean kickGroup;//Whether the dissolution of groups

	private boolean isShowTime;

//	private int totalUnread;//In the list of the last data is correct

	private String messageId;//Message ID

	private int datingSOSId;//Pray about ID

	private String shareFriendName;//Mutual friend's nickname

	private int modifyType;//Modified the time 0, 1 changed the place, two modified time and changed the location
	private int sceneType;//Custom activities only address 0, normal business

	private int friendLog;//Good friend relationship now to help ask the relative relations (0: strangers;1: friends;2: friends of friends)
	private int inviteType;//Corresponding help about objects, msgtype, 0 1 invited object

	private boolean offLineMsg;//The default for a web chat true said no network chat

	private boolean isTop;//Placed at the top
	private long topTime;//The sticky time

	private String atGroupIds;//@group member function

	private int isAtGroupMe;//Whether, 0 not @, 1 is @ I, 2 @members


	private boolean isAtGroupAll;

	private String atGroupMeMsgId;//Corresponding messageid @ message
	private String shareUrl;//Share the connection
	private String shareTitle;//The title of the share
	private String shareThumb;//Share the thumbnails

	private String inviteName;//The group invited to join in the name of the person
	private String beinviteName;//Group was asked to join in the name of the person


	private String inviteImage;//Group invited to join in the image
	private int lefttimes;//General can modify the number of data
	private int is_manager;//Whether the administrator
	private Message.MsgType msgType;//Chat type Single, group chat, groups, and system information


	private JSONArray offlinmsgList;//Offline message set

	private String redpacketId;//red packet id

	private String money;//Top-up, refund amount
	private String number;//The order number
	private String mode;//Top-up way

	private String vip_type; //Member types, personal
	private String vip_level;//Membership grade, group and individual

	private boolean is_vip;//Is overdue

	private String videoPath;

	private int percent;

	private long newprogress;

	private String msgName;//msg name

	private String extra;//Additional information

	private boolean audioPlaying;//Is voice broadcast

	private int live_level;

	private String source;//source

	private String sendName;//The name of the person who sent gifts
	private int propNum;//Number of gifts
	private String propName;
	private String propId;//Gift Gid

	private String text;//Customize the runway show content
	private int num;//Custom track number
	private String link;//Customize the runway show links

	private String forwardLeaveMsg;//Forwarded message information


	private String remoteSource;  //The source of the chat object


	private String usersource;//Groups or discussion group message domain name

	private String userfrom;//The source of the group or a discussion group message

	//    private String beinviteSource;//The inviter domain name
	private String inviteSource;//Inviter's domain name

	private int videotype;
	private String livingUrl;//Url/live webcam live or vr
	public int getVideotype() {
		return videotype;
	}

	public void setVideotype(int videotype) {
		this.videotype = videotype;
	}



	public String getLivingUrl() {
		return livingUrl;
	}

	public void setLivingUrl(String livingUrl) {
		this.livingUrl = livingUrl;
	}



	public String getInviteSource() {
		return inviteSource;
	}

	public void setInviteSource(String inviteSource) {
		this.inviteSource = inviteSource;
	}

	public String getUserfrom() {
		return userfrom;
	}

	public void setUserfrom(String userfrom) {
		this.userfrom = userfrom;
	}


	public String getUsersource() {
		return usersource;
	}

	public void setUsersource(String usersource) {
		this.usersource = usersource;
	}


	public String getForwardLeaveMsg() {
		return forwardLeaveMsg;
	}

	public void setForwardLeaveMsg(String forwardLeaveMsg) {
		this.forwardLeaveMsg = forwardLeaveMsg;
	}

	public String getRemoteSource() {
		return remoteSource;
	}

	public void setRemoteSource(String remoteSource) {
		this.remoteSource = remoteSource;
	}

	public String getPropId() {
		return propId;
	}

	public void setPropId(String propId) {
		this.propId = propId;
	}

	public String getPropName() {
		return propName;
	}

	public void setPropName(String propName) {
		this.propName = propName;
	}

	public int getPropNum() {
		return propNum;
	}

	public void setPropNum(int propNum) {
		this.propNum = propNum;
	}
	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getSendName() {
		return sendName;
	}

	public void setSendName(String sendName) {
		this.sendName = sendName;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}


	public int getLive_level() {
		return live_level;
	}

	public void setLive_level(int live_level) {
		this.live_level = live_level;
	}


	public boolean isAudioPlaying() {
		return audioPlaying;
	}

	public void setAudioPlaying(boolean audioPlaying) {
		this.audioPlaying = audioPlaying;
	}


	public String getInviteImage() {
		return inviteImage;
	}

	public void setInviteImage(String inviteImage) {
		this.inviteImage = inviteImage;
	}

	public String getExtra() {
		return extra;
	}

	public void setExtra(String extra) {
		this.extra = extra;
	}

	public boolean isAtGroupAll() {
		return isAtGroupAll;
	}

	public void setIsAtGroupAll(boolean isAtGroupAll) {
		this.isAtGroupAll = isAtGroupAll;
	}

	public int getPercent() {
		return percent;
	}

	public void setPercent(int percent) {
		this.percent = percent;
	}
	public long getNewprogress() {
		return newprogress;
	}

	public void setNewprogress(long newprogress) {
		this.newprogress = newprogress;
	}
	public Message.MsgType getMsgType() {
		return msgType;
	}

	public String getMsgName() {
		return msgName;
	}

	public void setMsgName(String msgName) {
		this.msgName = msgName;
	}


	public String getVideoPath() {
		return videoPath;
	}

	public void setVideoPath(String videoPath) {
		this.videoPath = videoPath;
	}


	public int getMsgTypeInt() {
		if (MsgType.system.equals(msgType)) {//System information
			return 0;
		} else if (MsgType.groupchat.equals(msgType)) {//Group chat
			return 2;
		} else if (MsgType.super_groupchat.equals(msgType)) {//group
			return 3;
		} else {//Single chat
			return 1;
		}
	}


	public void setMsgType(Message.MsgType msgType) {
		this.msgType = msgType;
	}

	public void setMsgTypeInt(int msgTypeInt) {
		switch (msgTypeInt) {
			case 0://system

				this.msgType = MsgType.system;
				break;
			case 1://Single chat

				this.msgType = MsgType.normalchat;
				break;
			case 2://Group chat

				this.msgType = MsgType.groupchat;
				break;
			case 3://group

				this.msgType = MsgType.super_groupchat;
				break;

			default:
				this.msgType = MsgType.normalchat;
				break;
		}
	}

	public String getAtGroupMeMsgId() {
		return atGroupMeMsgId;
	}

	public void setAtGroupMeMsgId(String atGroupMeMsgId) {
		this.atGroupMeMsgId = atGroupMeMsgId;
	}

	public boolean isTop() {
		return isTop;
	}

	public void setTop(boolean isTop) {
		this.isTop = isTop;
	}

	public long getTopTime() {
		return topTime;
	}

	public void setTopTime(long topTime) {
		this.topTime = topTime;
	}

	public String getRealname() {
		return realname;
	}

	public void setRealname(String realname) {
		this.realname = realname;
	}

	public String getGroupImage() {
		return groupImage;
	}

	public void setGroupImage(String groupImage) {
		this.groupImage = groupImage;
	}

	public String getShareUrl() {
		return shareUrl;
	}

	public void setShareUrl(String shareUrl) {
		this.shareUrl = shareUrl;
	}

	public String getShareTitle() {
		return shareTitle;
	}

	public void setShareTitle(String shareTitle) {
		this.shareTitle = shareTitle;
	}

	public String getShareThumb() {
		return shareThumb;
	}

	public void setShareThumb(String shareThumb) {
		this.shareThumb = shareThumb;
	}

	public String getInviteName() {
		return inviteName;
	}

	public void setInviteName(String inviteName) {
		this.inviteName = inviteName;
	}

	public String getBeinviteName() {
		return beinviteName;
	}

	public void setBeinviteName(String beinviteName) {
		this.beinviteName = beinviteName;
	}

	public int getLefttimes() {
		return lefttimes;
	}

	public void setLefttimes(int lefttimes) {
		this.lefttimes = lefttimes;
	}

	public int isAtGroupMe() {
		return isAtGroupMe;
	}

	public void setAtGroupMe(int isAtGroupMe) {
		this.isAtGroupMe = isAtGroupMe;
	}

	public String getAtGroupIds() {
		return atGroupIds;
	}

	public void setAtGroupIds(String atGroupIds) {
		this.atGroupIds = atGroupIds;
	}

	/**
	 *The default for a web chat true said no network chat
	 */
	public boolean isOffLineMsg() {
		return offLineMsg;
	}

	/**
	 * The default for a web chat true said no network chat
	 */
	public void setOffLineMsg(boolean offLineMsg) {
		this.offLineMsg = offLineMsg;
	}

	public int getId() {
		return _id;
	}

	public void setId(int _id) {
		this._id = _id;
	}

	public int getInviteType() {
		return inviteType;
	}

	public void setInviteType(int inviteType) {
		this.inviteType = inviteType;
	}

	public int getFriendLog() {
		return friendLog;
	}

	public void setFriendLog(int friendLog) {
		this.friendLog = friendLog;
	}

	public int getModifyType() {
		return modifyType;
	}

	public void setModifyType(int modifyType) {
		this.modifyType = modifyType;
	}

	public int getSceneType() {
		return sceneType;
	}

	public void setSceneType(int sceneType) {
		this.sceneType = sceneType;
	}

	public String getShareFriendName() {
		return shareFriendName;
	}

	public void setShareFriendName(String shareFriendName) {
		this.shareFriendName = shareFriendName;
	}

	public int getDatingSOSId() {
		return datingSOSId;
	}

	public void setDatingSOSId(int datingSOSId) {
		this.datingSOSId = datingSOSId;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

//	public int getTotalUnread() {
//		return totalUnread;
//	}

//	public void setTotalUnread(int totalUnread) {
//		this.totalUnread = totalUnread;
//	}

	public boolean isShowTime() {
		return isShowTime;
	}

	public void setShowTime(boolean isShowTime) {
		this.isShowTime = isShowTime;
	}

	public List<UserBaseVo> getMemberAvatarUserBaseList() {
		if (memberAvatarUserBaseList == null && memberAvatarList != null) {
			setMemberAvatarList(memberAvatarList);
		}
		return memberAvatarUserBaseList;
	}

	public boolean isDismissGroup() {
		return dismissGroup;
	}


	public void setDismissGroup(boolean dismissGroup) {
		this.dismissGroup = dismissGroup;
	}

	public boolean isKickGroup() {
		return kickGroup;
	}

	public void setKickGroup(boolean kickGroup) {
		this.kickGroup = kickGroup;
	}


	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public void setGroupMask(boolean groupMask) {
		this.groupMask = groupMask;
	}

	public boolean getGroupMask() {
		return groupMask;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public boolean isGroup() {
		return group;
	}

	public void setGroup(boolean group) {
		this.group = group;
	}

	public List<GroupMemberAvatarVo> getMemberAvatarList() {
		return memberAvatarList;
	}

	public void setMemberAvatarList(List<GroupMemberAvatarVo> memberAvatarList) {
		this.memberAvatarList = memberAvatarList;
		memberAvatarUserBaseList = new ArrayList<UserBaseVo>();
		if (memberAvatarList != null) {
			for (int i = 0; i < memberAvatarList.size(); i++) {
				memberAvatarUserBaseList.add(memberAvatarList.get(i).getUserBaseVo());
			}
		}
	}

	public int getInviteId() {
		return inviteId;
	}

	public void setInviteId(int inviteId) {
		this.inviteId = inviteId;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public long getCreateTime() {
		return createTime;
	}


	public String getInviteMsg() {
		return inviteMsg;
	}

	public void setInviteMsg(String inviteMsg) {
		this.inviteMsg = inviteMsg;
	}

	public int getGuest() {
		return guest;
	}

	public void setGuest(int guest) {
		this.guest = guest;
	}

	public int getSort() {
		return sort;
	}

	public void setSort(int sort) {
		this.sort = sort;
	}

	public int getCreateId() {
		return createId;
	}

	public void setCreateId(int createId) {
		this.createId = createId;
	}

	public int getCreateAge() {
		return createAge;
	}

	public void setCreateAge(int createAge) {
		this.createAge = createAge;
	}

	public int getCreateGender() {
		return createGender;
	}

	public void setCreateGender(int createGender) {
		this.createGender = createGender;
	}

	public String getCreateName() {
		return createName;
	}

	public void setCreateName(String createName) {
		this.createName = createName;
	}

	public String getCreateImage() {
		return createImage;
	}

	public void setCreateImage(String createImage) {
		this.createImage = createImage;
	}

	public String getCreategSign() {
		return creategSign;
	}

	public void setCreategSign(String creategSign) {
		this.creategSign = creategSign;
	}

	public String getLocalUrl() {
		return localUrl;
	}

	public void setLocalUrl(String localUrl) {
		this.localUrl = localUrl;
	}

	public String getThirdName() {
		return thirdName;
	}

	public void setThirdName(String thirdName) {
		this.thirdName = thirdName;
	}

	public String getThirdImage() {
		return thirdImage;
	}

	public void setThirdImage(String thirdImage) {
		this.thirdImage = thirdImage;
	}

	public String getThirdId() {
		return thirdId;
	}

	public void setThirdId(String thirdId) {
		this.thirdId = thirdId;
	}

	public String getThirdGender() {
		return thirdGender;
	}

	public void setThirdGender(String thirdGender) {
		this.thirdGender = thirdGender;
	}

	public String getCover() {
		return cover;
	}

	public void setCover(String cover) {
		this.cover = cover;
	}

	public String getSecond() {
		return second;
	}

	public void setSecond(String second) {
		this.second = second;
	}

	public String getLon() {
		return lon;
	}

	public void setLon(String lon) {
		this.lon = lon;
	}

	public String getLat() {
		return lat;
	}

	public void setLat(String lat) {
		this.lat = lat;
	}


	public String getShopAddress() {
		return shopAddress;
	}

	public void setShopAddress(String shopAddress) {
		this.shopAddress = shopAddress;
	}

	public String getCardSign() {
		return cardSign;
	}

	public void setCardSign(String cardSign) {
		this.cardSign = cardSign;
	}

	public boolean isAgree() {
		return agree;
	}

	public void setAgree(boolean agree) {
		this.agree = agree;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public boolean isSystem() {
		return system;
	}

	public void setSystem(boolean system) {
		this.system = system;
	}

	public int getSend() {
		return send;
	}

	public void setSend(int send) {
		this.send = send;
	}

	public boolean isMe() {
		try {
			return userId.equals(NextApplication.myInfo.getLocalId());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public int getUnread() {
		return unread;
	}

	public void setUnread(int unread) {
		this.unread = unread;
	}

	public String getChatId() {
		return chatId;
	}


	public void setChatId(String chatId) {
		this.chatId = chatId;
	}


	public long getMsgTime() {
		return msgTime;
	}

	public void setMsgTime(long msgTime) {
		this.msgTime = msgTime;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}


	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserImage() {
		return userImage;
	}

	public void setUserImage(String userImage) {
		this.userImage = userImage;
	}

	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getIs_manager() {
		return is_manager;
	}

	public void setIs_manager(int is_manager) {
		this.is_manager = is_manager;
	}

	public JSONArray getOfflinmsgList() {
		return offlinmsgList;
	}

	public void setOfflinmsgList(JSONArray offlinmsgList) {
		this.offlinmsgList = offlinmsgList;
	}


	public String getMoney() {
		return money;
	}

	public void setMoney(String money) {
		this.money = money;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getRedpacketId() {
		return redpacketId;
	}

	public void setRedpacketId(String redpacketId) {
		this.redpacketId = redpacketId;
	}

	public String getVip_type() {
		return vip_type;
	}

	public void setVip_type(String vip_type) {
		this.vip_type = vip_type;
	}

	public String getVip_level() {
		return vip_level;
	}

	public void setVip_level(String vip_level) {
		this.vip_level = vip_level;
	}

	public boolean isIs_vip() {
		return is_vip;
	}

	public void setIs_vip(boolean is_vip) {
		this.is_vip = is_vip;
	}

	/**
	 * Into the chat record
	 */
	public String parseChatMsgListToJsonString(List<ChatMsg> msgList) {
		if (msgList == null || msgList.isEmpty()) {
			return "";
		}
		Collections.sort(msgList, new ChatMsgIdComparable());
		JSONArray array = new JSONArray();
		JSONObject obj = null;
		for (int i = 0; i < msgList.size(); i++) {
			try {
				obj = new JSONObject(msgList.get(i).toChatJsonObject());
				switch (obj.optInt("type")) {
					case 100:
						obj.put("type", 10);
						break;
					case 101:
						obj.put("type", 11);
						break;
					case 102:
						obj.put("type", 12);
						break;
				}
				array.put(i, obj);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		return array.toString();
	}

	public ChatMsg parse(JSONObject obj) {
		if (obj == null) {
			return null;
		}

		setUserId(obj.optString("userid"));
		setUsername(obj.optString("username"));
		setUserImage(obj.optString("userimage"));
		setGender(obj.optInt("usergender"));
		setUsersource(obj.optString("usersource"));
		setUserfrom(obj.optString("userfrom"));
		setType(obj.optInt("type"));
		setGroupMask(obj.optInt("mask") == 1 ? true : false);
		setFriendLog(obj.optInt("friend_log"));


		setGroupName(obj.optString("groupname"));
		setGroupImage(obj.optString("groupimage"));
		setGroupId(obj.optString("groupid"));

		JSONArray array = obj.optJSONArray("groupmember");
		if (array != null && array.length() > 0) {//Group chat image resolution
			memberAvatarList = new ArrayList<>();
			GroupMemberAvatarVo vo;
			for (int i = 0; i < array.length(); i++) {
				vo = new GroupMemberAvatarVo().parse(array.optJSONObject(i));
				memberAvatarList.add(vo);
			}

		}
		JSONArray arrayAt = obj.optJSONArray("at");
		JSONArray arrayAtSource = obj.optJSONArray("atsource");


		if (arrayAt != null && arrayAt.length() > 0) {//Group chat if @ I parse
			for (int i = 0; i < arrayAt.length(); i++) {
				try {
					String uid = arrayAt.optString(i);
					String meuid = NextApplication.myInfo.getLocalId();
					if(arrayAtSource!=null && arrayAtSource.length() == arrayAt.length())
					{
						uid = uid+"@"+arrayAtSource.optString(i);
						meuid = meuid + "@" + XmppUtils.SERVER_NAME;
					}
					if (meuid.equals(uid)) {
						if (obj.optInt("atall", 0) == 1) {
							setAtGroupMe(2);
						} else {
							setAtGroupMe(1);
						}
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			setAtGroupMe(0);

		}
		if (getType() == 10000 && obj.has("offlinelist"))//Offline message set
		{
			setOfflinmsgList(obj.optJSONArray("offlinelist"));
			return this;
		}

		setContent(obj.optString("content"));

		setCover(obj.optString("cover"));
		setSecond(obj.optString("second"));
		setLon(obj.optString("lon"));
		setLat(obj.optString("lat"));

		setThirdName(obj.optString("name"));
		setShopAddress(obj.optString("address"));
		setThirdImage(obj.optString("image"));
		setThirdId(obj.optString("id"));
		setThirdGender(obj.optString("gender"));

		setCardSign(obj.optString("sign"));

		setCreateAge(obj.optInt("createrid"));
		setCreateGender(obj.optInt("creatergender"));
		setCreategSign(obj.optString("creatersightml"));
		setCreateId(obj.optInt("createrid"));
		setCreateImage(obj.optString("createrimage"));
		setCreateName(obj.optString("creatername"));
		setSort(obj.optInt("sort"));
		setGuest(obj.optInt("guest"));
		setInviteMsg(obj.optString("message"));
		setCreateTime(obj.optLong("time"));
		setInviteId(obj.optInt("id"));

		if (obj.has("inviteid")) {
			setInviteId(obj.optInt("inviteid"));
		}
		if(obj.has("invitesource")) {
			setInviteSource(obj.optString("invitesource"));
		}

		setInviteImage(obj.optString("inviteimage"));


		setDatingSOSId(obj.optInt("invite_id"));

		setShareFriendName(obj.optString("friendname"));

		setModifyType(obj.optInt("modifytype"));
		setSceneType(obj.optInt("scenetype"));

		setInviteType(obj.optInt("msgtype"));

		//Share share news
		setShareUrl(obj.optString("url"));
		setShareTitle(obj.optString("title"));
		setShareThumb(obj.optString("thumb"));

		//The url of the live
		setVideotype(obj.optInt("videotype"));
		setLivingUrl(obj.optString("livingUrl"));

		//Group name of the inviter
		setInviteName(obj.optString("invitename"));

		//Group by the name of the inviter
		setBeinviteName(obj.optString("beinvitename"));
		//Whether the administrator
		setIs_manager(obj.optInt("is_manager"));

		//The total number can be modified
		setLefttimes(obj.optInt("lefttimes", 0));


		setMoney(obj.optString("money"));
		setNumber(obj.optString("number"));
		setMode(obj.optString("mode"));

		setRedpacketId(obj.optString("rid"));

		setVip_level(obj.optString("vip_level"));
		setVip_type(obj.optString("vip_type"));
		setIs_vip(obj.optBoolean("is_vip", false));


		setMsgName(obj.optString("msgname"));

		setExtra(obj.optString("extra"));

		setLive_level(obj.optInt("live_level"));

		setSource(obj.optString("msgsource"));
		setRemoteSource(obj.optString("msgsource"));

		setSendName(obj.optString("sendname"));

		setPropName(obj.optString("propname"));

		setPropNum(obj.optInt("propnum"));

		setPropId(obj.optString("propid"));
		setNum(obj.optInt("num"));
		setText(obj.optString("text"));
		setLink(obj.optString("link"));
		return this;
	}

	public ChatMsg parse(JSONObject obj, ChatMsg msg, MsgType type) {
		if (obj == null) {
			return null;
		}
		setChatId(msg.getChatId());
		setUserId(msg.getUserId());
		setUsername(msg.getUsername());
		setUserImage(msg.getUserImage());
		setGender(msg.getGender());
		setUsersource(msg.getUsersource());
		setUserfrom(msg.getUserfrom());
		setGroupName(msg.getGroupName());
		setGroupImage(msg.getGroupImage());
		setGroupId(msg.getGroupId());

		setGroupMask(msg.getGroupMask());
		setFriendLog(msg.getFriendLog());
		setMemberAvatarList(msg.getMemberAvatarList());
		setAtGroupMe(msg.isAtGroupMe);

		setVip_level(msg.vip_level);
		setVip_type(msg.vip_type);
		setIs_vip(msg.isIs_vip());

		if (type == MsgType.groupchat)//Group chat
		{
			setUserId(obj.optString("userid"));
			setUsername(obj.optString("username"));
			setUserImage(obj.optString("userimage"));
			setGender(obj.optInt("usergender"));
			setUsersource(obj.optString("usersource"));
			setUserfrom(obj.optString("userfrom"));
		} else if (type == MsgType.super_groupchat) {//group
			setUserId(obj.optString("userid"));
			setUsername(obj.optString("username"));
			setUserImage(obj.optString("userimage"));
			setGender(obj.optInt("usergender"));
			setUsersource(obj.optString("usersource"));
			setUserfrom(obj.optString("userfrom"));
		}
		setMsgTime(obj.optLong("msgTime") / 1000);
		setMessageId(obj.optString("msgid"));
		setType(obj.optInt("type"));
		setContent(obj.optString("content"));

		setCover(obj.optString("cover"));
		setSecond(obj.optString("second"));
		setLon(obj.optString("lon"));
		setLat(obj.optString("lat"));

		setThirdName(obj.optString("name"));
		setShopAddress(obj.optString("address"));
		setThirdImage(obj.optString("image"));
		setThirdId(obj.optString("id"));
		setThirdGender(obj.optString("gender"));

		setCardSign(obj.optString("sign"));

		setCreateAge(obj.optInt("createrid"));
		setCreateGender(obj.optInt("creatergender"));
		setCreategSign(obj.optString("creatersightml"));
		setCreateId(obj.optInt("createrid"));
		setCreateImage(obj.optString("createrimage"));
		setCreateName(obj.optString("creatername"));
		setSort(obj.optInt("sort"));
		setGuest(obj.optInt("guest"));
		setInviteMsg(obj.optString("message"));
		setCreateTime(obj.optLong("time"));
		setInviteId(obj.optInt("id"));
		if (obj.has("inviteid")) {
			setInviteId(obj.optInt("inviteid"));
		}
		if(obj.has("invitesource")) {
			setInviteSource(obj.optString("invitesource"));
		}

		setInviteImage(obj.optString("inviteimage"));

		setDatingSOSId(obj.optInt("invite_id"));

		setShareFriendName(obj.optString("friendname"));

		setModifyType(obj.optInt("modifytype"));
		setSceneType(obj.optInt("scenetype"));
		setInviteType(obj.optInt("msgtype"));

		//Share share news
		setShareUrl(obj.optString("url"));
		setShareTitle(obj.optString("title"));
		setShareThumb(obj.optString("thumb"));

		//The url of the live
		setVideotype(obj.optInt("videotype"));
		setLivingUrl(obj.optString("livingUrl"));

		//Group name of the inviter
		setInviteName(obj.optString("invitename"));

		//Group by the name of the inviter
		setBeinviteName(obj.optString("beinvitename"));


		//Whether the administrator
		setIs_manager(obj.optInt("is_manager"));

		//The total number can be modified
		setLefttimes(obj.optInt("lefttimes", 0));

		setMoney(obj.optString("money"));
		setNumber(obj.optString("number"));
		setMode(obj.optString("mode"));

		setRedpacketId(obj.optString("rid"));

		setMsgName(obj.optString("msgname"));

		setLive_level(obj.optInt("live_level"));

		setSource(obj.optString("msgsource"));

		setRemoteSource(obj.optString("msgsource"));

		setSendName(obj.optString("sendname"));

		setPropName(obj.optString("propname"));

		setPropNum(obj.optInt("propnum"));

		setPropId(obj.optString("propid"));

		return this;
	}

	public String toChatJsonObject() {
		String json = "{}";
		try {
			JSONObject obj = new JSONObject();
			obj.put("userid", getUserId());
			obj.put("username", getUsername());
			obj.put("userimage", getUserImage());
			obj.put("usergender", getGender());
			obj.put("type", getType());

			if (!TextUtils.isEmpty(getContent()))
				obj.put("content", getContent());

			if (!TextUtils.isEmpty(getCover()))
				obj.put("cover", getCover());

			if (!TextUtils.isEmpty(getSecond()))
				obj.put("second", getSecond());

			if (!TextUtils.isEmpty(getLon()))
				obj.put("lon", getLon());

			if (!TextUtils.isEmpty(getLat()))
				obj.put("lat", getLat());

			if (!TextUtils.isEmpty(getThirdName()))
				obj.put("name", getThirdName());

			if (!TextUtils.isEmpty(getShopAddress()))
				obj.put("address", getShopAddress());

			if (!TextUtils.isEmpty(getThirdImage()))
				obj.put("image", getThirdImage());

			if (!TextUtils.isEmpty(getThirdId()))
				obj.put("id", getThirdId());

			if (!TextUtils.isEmpty(getCardSign()))
				obj.put("sign", getCardSign());

			if (!TextUtils.isEmpty(getShareUrl()))
				obj.put("url", getShareUrl());


			obj.put("videotype", getVideotype());
			if (!TextUtils.isEmpty(getLivingUrl()))
				obj.put("livingUrl", getLivingUrl());

			if (!TextUtils.isEmpty(getShareTitle()))
				obj.put("title", getShareTitle());

			if (!TextUtils.isEmpty(getShareThumb()))
				obj.put("thumb", getShareThumb());

			if (!TextUtils.isEmpty(getRedpacketId()))
				obj.put("rid", getRedpacketId());

			if (!TextUtils.isEmpty(getNumber()))
				obj.put("number", getNumber());

			if (!TextUtils.isEmpty(getMsgName()))
				obj.put("msgname", getMsgName());

			if (!TextUtils.isEmpty(getCreateName()))
				obj.put("creatername", getCreateName());

			if (!TextUtils.isEmpty(getSource()))
				obj.put("msgsource", getSource());

			if (getCreateTime()>0)
			{
				obj.put("time", getCreateTime());
			}

			if (!TextUtils.isEmpty(getUsersource()))
			{
				obj.put("usersource", getUsersource());
			}

			if (!TextUtils.isEmpty(getUserfrom()))
			{
				obj.put("userfrom", getUserfrom());
			}

			obj.put("msgtype", getInviteType() + "");


			json = obj.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return json;
	}

	public String toGroupChatJsonObject() {
		String json = "{}";
		try {
			JSONObject obj = new JSONObject();
			String groupid = getChatId().replace("group-", "").replace("superGroup-", "");
			String[] groupids = groupid.split("@");//Other domain name was sent message uid will directly with @ domain name suffix
			if(groupids.length>1)//This app group
			{
				obj.put("groupid", groupids[0]);
			}
			else{
				obj.put("groupid", groupid);
			}
			obj.put("userid", getUserId());
			obj.put("username", getUsername());
			obj.put("userimage", getUserImage());
			obj.put("usergender", getGender());
			obj.put("type", getType());
			if (!TextUtils.isEmpty(getContent()))
				obj.put("content", getContent());

			if (!TextUtils.isEmpty(getCover()))
				obj.put("cover", getCover());

			if (!TextUtils.isEmpty(getSecond()))
				obj.put("second", getSecond());

			if (!TextUtils.isEmpty(getLon()))
				obj.put("lon", getLon());

			if (!TextUtils.isEmpty(getLat()))
				obj.put("lat", getLat());

			if (!TextUtils.isEmpty(getThirdName()))
				obj.put("name", getThirdName());

			if (!TextUtils.isEmpty(getShopAddress()))
				obj.put("address", getShopAddress());

			if (!TextUtils.isEmpty(getThirdImage()))
				obj.put("image", getThirdImage());

			if (!TextUtils.isEmpty(getThirdId()))
				obj.put("id", getThirdId());

			if (!TextUtils.isEmpty(getCardSign()))
				obj.put("sign", getCardSign());

			if (!TextUtils.isEmpty(getShareUrl()))
				obj.put("url", getShareUrl());

			obj.put("videotype", getVideotype());
			if (!TextUtils.isEmpty(getLivingUrl()))
				obj.put("livingUrl", getLivingUrl());

			if (!TextUtils.isEmpty(getShareTitle()))
				obj.put("title", getShareTitle());

			if (!TextUtils.isEmpty(getShareThumb()))
				obj.put("thumb", getShareThumb());

			if (isAtGroupAll()) {
				obj.put("atall", "1");
			}
			if (!TextUtils.isEmpty(getAtGroupIds())) {
				JSONArray arrayid = new JSONArray();
				JSONArray arraysource = new JSONArray();
				if (getAtGroupIds().contains(",")) {
					String[] ids = getAtGroupIds().split(",");
					for (String uid : ids) {
						String[] uids = uid.split("@");
						if(uids.length>1)//The users of this app
						{
							arrayid.put(uids[0]);
							arraysource.put(uids[1]);
						}
						else{
							arrayid.put(uid);
							arraysource.put(XmppUtils.SERVER_NAME);
						}
					}
				} else {
					String[] uids =getAtGroupIds().split("@");
					if(uids.length>1)//The users of this app
					{
						arrayid.put(uids[0]);
						arraysource.put(uids[1]);
					}
					else{
						arrayid.put(getAtGroupIds());
						arraysource.put(XmppUtils.SERVER_NAME);
					}
				}
				obj.put("at", arrayid);
				obj.put("atsource", arraysource);
			}


			if (!TextUtils.isEmpty(getRedpacketId()))
				obj.put("rid", getRedpacketId());
			if (!TextUtils.isEmpty(getNumber()))
				obj.put("number", getNumber());

			if (!TextUtils.isEmpty(getMsgName()))
				obj.put("msgname", getMsgName());

			if (!TextUtils.isEmpty(getCreateName()))
				obj.put("creatername", getCreateName());

			if (!TextUtils.isEmpty(getSource()))
				obj.put("msgsource", getSource());

			if (getCreateTime()>0)
			{
				obj.put("time", getCreateTime());
			}

			if (!TextUtils.isEmpty(getUsersource()))
			{
				obj.put("usersource", getUsersource());
			}

			if (!TextUtils.isEmpty(getUserfrom()))
			{
				obj.put("userfrom", getUserfrom());
			}

			obj.put("msgtype", getInviteType() + "");
			json = obj.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return json;
	}

	public void parseUserBaseVo(UserBaseVo vo) {
		setUsername(vo.getShowName());
//		try {
//			setGender(Integer.parseInt(vo.getGender()));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		setUserId(vo.getLocalId());
		setUserImage(vo.getThumb());
	}


}
