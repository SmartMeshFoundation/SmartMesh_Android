package com.lingtuan.firefly.util;

/**
 * Created on 2017/8/22.
 * APP constants
 */

public class Constants {

    /**
     * The global total switch online
     * */
    public static final boolean GLOBAL_SWITCH_OPEN = false;

    /**
     * Refresh the friends list
     */
    public static boolean isRefresh = false;

    /**
     * Cut images returned by the callback url
     */
    public static final int REQUEST_CODE_PHOTO_URL = 60000;

    /**
     * Photo modified head
     */
    public static final int CAMERA_WITH_DATA = 0x02;


    /**
     * From revision picture album
     */
    public static final int PHOTO_PICKED_WITH_DATA = 0x03;


    /**
     * Cut the image size
     */
    public static final int MAX_IMAGE_WIDTH = 1280;

    public static final int MAX_IMAGE_HEIGHT = 1280;

    /**
     * Cut the picture quality
     */
    public static final int MAX_KB = 150;
    /**
     * Cut the picture quality
     */
    public static final int MIN_KB = 20;

    /**
     * Wide high minimum cut images
     */
    public static final int MAX_SCALE = 3;

    /**
     * Chat to send small icon file
     */
    public static final String CHAT_SEND_FILE = "chat_send_file";

    public static final int COUNTRY_CODE_RESULT = 900;// Choose the country code callback


    /**
     * SMT connection address identifier
     */
    public static final String APP_URL_FLAG = "smartmesh.io";

    /*password*/
    public static final String PASSWORD = "password";
    /*Name of the wallet*/
    public static final String WALLET_NAME= "wallet_name";
    /*The wallet address*/
    public static final String WALLET_ADDRESS= "wallet_address";

    /*Wallet marked whether I will be able to export the private key can 0 can't 1*/
    public static final String WALLET_EXTRA= "wallet_extra";

    /*The private key*/
    public static final String PRIVATE_KEY = "private_key";

    /*KeyStore*/
    public static final String KEYSTORE = "keystore";

    /*The wallet information*/
    public static final String WALLET_INFO = "wallet_info";

    /*The wallet icon*/
    public static final String WALLET_ICON = "wallet_icon";

    /*Backup the wallet type*/
    public static final String WALLET_TYPE= "wallet_type";

    /*Create (import) wallet success*/
    public static final String WALLET_SUCCESS = "com.lingtuan.firefly.wallet_success";
    /*Failed to create (import) purse*/
    public static final String WALLET_ERROR = "com.lingtuan.firefly.wallet_error";

    /*Create (import) wallet out of memory*/
    public static final String NO_MEMORY = "com.lingtuan.firefly.no_memory";
    /*Import the wallet password mistake*/
    public static final String WALLET_PWD_ERROR = "com.lingtuan.firefly.wallet_pwd_error";

    /*Import the wallet to repeat*/
    public static final String WALLET_REPEAT_ERROR = "com.lingtuan.firefly.wallet_repeat_error";

    /*To delete the wallet success refresh the page*/
    public static final String WALLET_REFRESH_DEL = "com.lingtuan.firefly.wallet_refresh_del";

    /*Switch in both Chinese and English*/
    public static final String CHANGE_LANGUAGE = "com.lingtuan.firefly.change_language";

    /*Synchronous node progress*/
    public static final String SYNC_PROGRESS = "com.lingtuan.firefly.sync_progress";

    public static final String ACTION_SELECT_CONTACT_REFRESH = "com.lingtuan.firefly.contact.refresh";

    /**
     * Version update
     */
    public static final String ACTION_UPDATE_VERSION = "com.lingtuan.firefly.updateversion";

    /**
     * Unread messages scroll
     */
    public static final String ACTION_SCROLL_TO_NEXT_UNREAD_MSG = "com.lingtuan.firefly.scrolltonextunreadmsg";

    /**
     * More pictures to send
     */
    public static final String ACTION_CHATTING_PHOTO_LIST = "com.lingtuan.firefly.chatting.photolist";

    /**
     * Album group browse pictures broadcast the action
     */
    public static final String BROADCAST_ACTION = "com.lingtuan.firefly.child_photo_filter";

    /**
     * User to update, upload pictures broadcast the action
     */
    public static final String BROADCAST_UPDATE_USER = "com.lingtuan.firefly.broadcast_update_user";

    /**
     * More pictures to send
     */
    public static final String ACTION_CHATTING_FRIEND_NOTE = "com.lingtuan.firefly.chattingui.friendnote";

    public static final String ACTION_START_UPLOAD_FILE = "com.lingtuan.firefly.upload.file.start";//To upload
    public static final String ACTION_CANCEL_UPLOAD_FILE = "com.lingtuan.firefly.upload.file.cancel";//Cancel the upload

    public static final String ACTION_START_DOWNLOAD_FILE = "com.lingtuan.firefly.download.file.start";//Start the download
    public static final String ACTION_CANCEL_DOWNLOAD_FILE = "com.lingtuan.firefly.download.file.";//Cancel the download
    public static final String ACTION_COLLECT_FILE = "com.lingtuan.firefly.file.collect";//File collection successful
    public static final String ACTION_CANCEL_COLLECT_FILE = "com.lingtuan.firefly.file.cancel.collect";//Successfully cancelled file collection
    public static final String ACTION_DOWNLOAD_CHAT_FILE_SUCCESS = "com.lingtuan.firefly.action_download_chat_file_success";//File download successful radio action
    public static final String ACTION_DOWNLOAD_CHAT_FILE_FAILED = "com.lingtuan.firefly.action_download_chat_file_failed";//Broadcast action file download failure
    public static final String ACTION_DOWNLOAD_CHAT_FILE = "com.lingtuan.firefly.action_download_chat_file";//File download progress radio action
    // Micro video download micro video cache radio action
    public static final String ACTION_DOWNLOAD_SUCCESS_OR_CACHE_VIDEO = "com.lingtuan.firefly.action_download_success_or_cache_video";
    //Micro video download progress radio action
    public static final String ACTION_DOWNLOAD_PROGRESS_VIDEO = "com.lingtuan.firefly.action_download_progress_video";

    //Close the guide page
    public static final String ACTION_CLOSE_GUID = "com.lingtuan.firefly.action_close_guid";

    //Close the home page
    public static final String ACTION_CLOSE_MAIN = "com.lingtuan.firefly.action_close_main";

    //Network monitoring radio
    public static final String ACTION_NETWORK_RECEIVER = "com.lingtuan.firefly.newwork_receiver";


    public static final String MSG_REPORT_SEND_MSG_RESULT = "MSG_REPORT_SEND_MSG_RESULT";// Chat messages sent
    public static final String MSG_REPORT_SEND_MSG_PROGRESS = "MSG_REPORT_SEND_MSG_PROGRESS";// Send the file progress
    public static final String MSG_REPORT_START_RECV_FILE = "MSG_REPORT_START_RECV_FILE";// Begin to receive files
    public static final String MSG_REPORT_CANCEL_RECV_FILE = "MSG_REPORT_CANCEL_RECV_FILE";// Cancel the receiving documents
    public static final String MSG_REPORT_CANCEL_SEND_FILE = "MSG_REPORT_CANCEL_SEND_FILE";// Cancel the receiving documents

    public static final String MSG_REPORT_RECV_NORMAL_MSG_TEXT = "MSG_REPORT_RECV_NORMAL_MSG_TEXT";// Receiving plain text chat messages
    public static final String MSG_REPORT_RECV_NORMAL_MSG_IMAGE = "MSG_REPORT_RECV_NORMAL_MSG_IMAGE";// Receive regular picture chat messages
    public static final String MSG_REPORT_RECV_NORMAL_MSG_FILE = "MSG_REPORT_RECV_NORMAL_MSG_FILE";// Receive normal file chat messages

    /**
     * Choose members of the group chat
     */
    public static final int REQUEST_SELECT_GROUP_MEMBER = 1039;


    /***************************No net related*****************************/
    public static final int LISTEN_PORT = 8988;
    public static final int SOCKET_TIMEOUT = 5000;
    public static final int COMMAND_ID_SEND_TYPE_SYSTEM = 100;// Only the json data countless according to flow
    public static final int COMMAND_ID_SEND_TYPE_NORMALCHAT = 101;// Contains the data flow
    public static final int COMMAND_ID_SEND_TYPE_ROOMCHAT = 102;// Contains the data flow
    public static final String OFFLINE_MSG_REPORT_RECV_NORMAL_MSG_IMAGE = "OFFLINE_MSG_REPORT_RECV_NORMAL_MSG_IMAGE";// Receive regular picture chat messages
    public static final String OFFLINE_MSG_REPORT_RECV_NORMAL_MSG_FILE = "OFFLINE_MSG_REPORT_RECV_NORMAL_MSG_FILE";// Receive normal file chat messages
    public static final String OFFLINE_MSG_REPORT_RECV_ROOM_MSG_TEXT = "OFFLINE_MSG_REPORT_RECV_ROOM_MSG_TEXT";// Receive no text chat news network chat rooms
    public static final String OFFLINE_MSG_REPORT_RECV_ROOM_MSG_IMAGE = "OFFLINE_MSG_REPORT_RECV_ROOM_MSG_IMAGE";// Receive no network chat room picture chat messages
    public static final String OFFLINE_MSG_REPORT_RECV_NORMAL_MSG_TEXT = "OFFLINE_MSG_REPORT_RECV_NORMAL_MSG_TEXT";// Receiving plain text chat messages
    public static final String OFFLINE_MSG_REPORT_SEND_MSG_PEOPLE_NUM = "OFFLINE_MSG_REPORT_SEND_MSG_PEOPLE_NUM";// The current number of online
    public static final String OFFLINE_MSG_REPORT_SEND_MSG_PEOPLE_NEW_COME = "OFFLINE_MSG_REPORT_SEND_MSG_PEOPLE_NEW_COME";// A new online user
    public static final String OFFLINE_MSG_REPORT_SEND_MSG_PEOPLE_NEW_LEAVES = "OFFLINE_MSG_REPORT_SEND_MSG_PEOPLE_NEW_LEAVES";// New offline users
    public static final String OFFLINE_MEMBER_LIST = "com.lingtuan.firefly.offline_member_list";// New offline users
    public static final int MSG_SERVICE_POOL_START = 10; // Open shop number of threads
    public static final int MSG_REPORT_RECV_PEER_LIST = 11;// Receive the connected user information list
    public static final String USE_GUIDE_URL = "file:///android_asset/offline/offline.html";
    /**
     * Is connected to the network
     */
    public static boolean isConnectNet = true;


}
