package com.lingtuan.firefly.db;

public class TableField {

	public static final String TABLE_GROUP = "_group";//Friends group table
	public static final String TABLE_FRIEND = "friend";//Friends list
	public static final String TABLE_USER_INFO = "userinfo";//The contact information
	public static final String TABLE_CHAT = "chat";//Chat table
	public static final String TABLE_CHAT_OFFLINE = "offlinechat";//Offline chat table
	public static final String TABLE_CHAT_EVENT = "chatevent";//The message list
	public static final String TABLE_CHAT_EVENT_OFFLINE = "offlinechatevent";//Offline message table
	public static final String TABLE_DYNAMIC_NOTIF = "dynamicnotif";//Dynamic notification form
	public static final String TABLE_DYNAMIC_CONTENT = "dynamiccontent";//Dynamic content table
	public static final String TABLE_DYNAMIC_COMMENT = "dynamiccomment";//Dynamic review table
	public static final String TABLE_DYNAMIC_PRAISE = "dynamicpraise";//A good dynamic table
	public static final String TABLE_FRIENDS_RECOMMENT = "friendsrecomment";//Friend recommended table
	public static final String TABLE_CONTACT = "contact";//The address book table
	public static final String TABLE_GIF_FAV= "giffav";//GIF collection

	public static final String TABLE_TRANS = "trans";//The trans list

	public static final String TABLE_TRANS_TEMP = "transtemp";//The trans list

	public static final String TABLE_TOKEN_LIST = "tokenlist";//The trans list

	public static final String TABLE_GROUP_UPLOAD_FILE= "groupuploadfile";//Group to upload files
	public static String _ID = "_id";
	public static String _MSGID = "_msgid";
	
	//--------------Group information
	public static final String FIELD_GROUP_GID  = "_gid";
	public static final String FIELD_GROUP_NAME = "_name";
	
	//--------------The contact information
		public static final String FIELD_USER_INFO		= "_info";
		
	//--------------Good friend information
	public static final String FIELD_FRIEND_UID			= "_uid";
	public static final String FIELD_FRIEND_UNAME		= "_uname";
	public static final String FIELD_FRIEND_DISTANCE 	= "_distance";
	public static final String FIELD_FRIEND_LOGINTIME 	= "_time";
	public static final String FIELD_FRIEND_SIGHTML	 	= "_sightml";
	public static final String FIELD_FRIEND_AGE		 	= "_age";
	public static final String FIELD_FRIEND_GENDER	 	= "_gender";
	public static final String FIELD_FRIEND_PIC		 	= "_pic";
	public static final String FIELD_FRIEND_THUMB 	 	= "_thumb";
	public static final String FIELD_FRIEND_NOTE 	 	= "_note";
	public static final String FIELD_FRIEND_FRIEND_LOG  = "_friend_log";//The relative relations (1: myself.Zero: strangers;1: friends;2: the second friend)
	public static final String FIELD_FRIEND_RELATION 	= "_relation";//The name of the buddy to bring two friends relationship
	public static final String FIELD_FRIEND_RELATION_ID	= "_relationid";// His best friend's ID
	//--------------Chat field
	public static final String FIELD_CHAT_MSGTIME	 	= "_time";
	public static final String FIELD_CHAT_TYPE			= "_type";
	public static final String FIELD_CHAT_BODY			= "_body";
	public static final String FIELD_CHAT_ID			= "_chatid";
	public static final String FIELD_CHAT_UNREAD		= "_unread";
	public static final String FIELD_CHAT_ISSEND		= "_send";
	public static final String FIELD_CHAT_SYSTEM		= "_system";
	public static final String FIELD_CHAT_HIDDEN		= "_hidden";
	public static final String FIELD_CHAT_AGREE			= "_agree";
	public static final String FIELD_CHAT_COVER			= "_cover";
	public static final String FIELD_CHAT_SECOND		= "_second";
	public static final String FIELD_CHAT_LON	 		= "_lon";
	public static final String FIELD_CHAT_LAT			= "_lat";
	public static final String FIELD_CHAT_THIRDNAME		= "_thirdname";
	public static final String FIELD_CHAT_THIRDIMAGE	= "_thirdImage";
	public static final String FIELD_CHAT_THIRDID		= "_thirdId";
	public static final String FIELD_CHAT_SHOPADDRESS	= "_shopAddress";
	public static final String FIELD_CHAT_CARDSIGN		= "_cardSign";
	public static final String FIELD_CHAT_LOCALURL		= "_localurl";
	public static final String FIELD_CHAT_SHOWTIME		= "_showtime";
	
	public static final String FIELD_CHAT_CREATEID		= "_createid";
	public static final String FIELD_CHAT_CREATENAME	= "_createname";
	public static final String FIELD_CHAT_CREATEIMAGE	= "_createimage";
	public static final String FIELD_CHAT_CREATEAGE		= "_createage";
	public static final String FIELD_CHAT_CREATEGENDER	= "_creategender";
	public static final String FIELD_CHAT_CREATEGSIGN	= "_creategsign";
	public static final String FIELD_CHAT_SORT			= "_sort";
	public static final String FIELD_CHAT_INVITEMSG		= "_invitemsg";
	public static final String FIELD_CHAT_GUEST			= "_guest";
	public static final String FIELD_CHAT_CREATETIME	= "_createtime";
	public static final String FIELD_CHAT_INVITEID		= "_inviteId";
	public static final String FIELD_CHAT_DISMISSGROUP	= "_remove";
	public static final String FIELD_CHAT_KICKGROUP		= "_kick";
	
	public static final String FIELD_CHAT_GROUP_IMAGE	= "_groupimage";
	/** Chat object 1 0 for system for single 2 for group chat 3 for group */
	public static final String FIELD_CHAT_OBJECT		= "_object";//Chat object 1 0 for system for single 2 for group chat 3 for group
	
	public static final String FIELD_DYNAMIC_ID			= "_dynamicid";
	public static final String FIELD_DYNAMIC_IMAGE		= "_dynamicimage";
	public static final String FIELD_DYNAMIC_COMMENT	= "_comment";
	public static final String FIELD_DYNAMIC_MSGTYPE	= "_msgtype";
	
	public static final String FIELD_DYNAMIC_REPLYNUM	= "_replynum";
	public static final String FIELD_DYNAMIC_PRAISE		= "_praise";
	public static final String FIELD_DYNAMIC_PRAISENUM	= "_praisenum";
	public static final String FIELD_DYNAMIC_FAV		= "_fav";
	public static final String FIELD_DYNAMIC_IMAGES		= "_images";
	public static final String FIELD_DYNAMIC_REPLY		= "_reply";//Is review all information json
	
	public static final String FIELD_DYNAMIC_COMMENT_ID	= "_commentid";//Social comment ID

	public static final String FIELD_GIF_ID	= "_gifid";//The id of the GIF

	//--------------The reserved field
	public static final String FIELD_RESERVED_DATA1  	= "_data1";// hat table messageId my friends information table messageId table is visible to Ta / / social circle who responded to the new information replyname/url/social extension
	public static final String FIELD_RESERVED_DATA2  	= "_data2";//Friends list visible to Ta friend my information table sharedFriendName // social extension type 0: dynamic, 1: sharing
	public static final String FIELD_RESERVED_DATA3  	= "_data3";//My table of information system information office workers modifytype // table is no network // social extended content
	public static final String FIELD_RESERVED_DATA4  	= "_data4";//My table of information system information chat invitations scenetype table friendlog / / friends are passing table number/address/social extension pictures
	public static final String FIELD_RESERVED_DATA5  	= "_data5";//Chat table msgtype my information tables friendlog // friends interests and hobbies
	public static final String FIELD_RESERVED_DATA6  	= "_data6";//My information table mask (whether) / / buddy list
	public static final String FIELD_RESERVED_DATA7  	= "_data7";//No network chat list my information table fields Table no network chat or no network chat friends / / / / table The geographical position
	public static final String FIELD_RESERVED_DATA8  	= "_data8";//Whether the information table of @ I chat / / table, shareUrl
	public static final String FIELD_RESERVED_DATA9  	= "_data9";//ShareTitle chat table
	public static final String FIELD_RESERVED_DATA10 	= "_data10";//ShareThumb chat table
	public static final String FIELD_RESERVED_DATA11 	= "_data11";//Chat table red envelope id
	
	public static final String FIELD_RESERVED_DATA12 	= "_data12";//Member type
	
	public static final String FIELD_RESERVED_DATA13 	= "_data13";//Membership grade
	
	public static final String FIELD_RESERVED_DATA14 	= "_data14";//Members expired
	public static final String FIELD_RESERVED_DATA15 	= "_data15";
	public static final String FIELD_RESERVED_DATA16 	= "_data16";
	public static final String FIELD_RESERVED_DATA17	= "_data17";
	public static final String FIELD_RESERVED_DATA18	= "_data18";
	public static final String FIELD_RESERVED_DATA19	= "_data19";
	public static final String FIELD_RESERVED_DATA20	= "_data20";
	public static final String FIELD_RESERVED_DATA21	= "_data21";
	public static final String FIELD_RESERVED_DATA22	= "_data22";
	public static final String FIELD_RESERVED_DATA23	= "_data23";
	public static final String FIELD_RESERVED_DATA24	= "_data24";
	public static final String FIELD_RESERVED_DATA25	= "_data25";
	public static final String FIELD_RESERVED_DATA26	= "_data26";
	//---------------------After giving out when data11 fields need to modify the table structure
//	public static final String FIELD_RESERVED_DATA12 	= "_data12";
//	public static final String FIELD_RESERVED_DATA13 	= "_data13";
//	public static final String FIELD_RESERVED_DATA14 	= "_data14";
//	public static final String FIELD_RESERVED_DATA15 	= "_data15";
//	public static final String FIELD_RESERVED_DATA16 	= "_data16";
//	public static final String FIELD_RESERVED_DATA17 	= "_data17";
//	public static final String FIELD_RESERVED_DATA18 	= "_data18";
//	public static final String FIELD_RESERVED_DATA19 	= "_data19";
//	public static final String FIELD_RESERVED_DATA20 	= "_data20";
//	public static final String FIELD_RESERVED_DATA21 	= "_data21";
//	public static final String FIELD_RESERVED_DATA22 	= "_data22";
//	public static final String FIELD_RESERVED_DATA23 	= "_data23";
//	public static final String FIELD_RESERVED_DATA24 	= "_data24";
//	public static final String FIELD_RESERVED_DATA25 	= "_data25";
//	public static final String FIELD_RESERVED_DATA26 	= "_data26";
//	public static final String FIELD_RESERVED_DATA27 	= "_data27";
//	public static final String FIELD_RESERVED_DATA28 	= "_data28";
//	public static final String FIELD_RESERVED_DATA29 	= "_data29";
//	public static final String FIELD_RESERVED_DATA30 	= "_data30";
	
}
