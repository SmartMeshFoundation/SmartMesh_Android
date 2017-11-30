package com.lingtuan.firefly.xmpp;

/**
 * XMPP special ACTION
 */
public class XmppAction {

    /**
     * The login
     */
    public static final String ACTION_LOGIN = "com.lingtuan.firefly.service.xmppservice.login";
    /**
     * Login successful
     */
    public static final String ACTION_LOGIN_SUCCESS = "com.lingtuan.firefly.service.xmppservice.login.success";
    /**
     * Login failed
     */
    public static final String ACTION_LOGIN_ERROR = "com.lingtuan.firefly.service.xmppservice.login.error";
    /**
     * Listen to the message listener
     */
    public static final String ACTION_LOGIN_MESSAGE_LISTENER = "com.lingtuan.firefly.service.xmppservice.login.messagelistener";
    /**
     * The message broadcast distribution
     */
    public static final String ACTION_MESSAGE_LISTENER = "com.lingtuan.firefly.service.xmppservice.message.listener";
    /**
     * The message list broadcast distribution
     */
    public static final String ACTION_OFFLINE_MESSAGE_LIST_LISTENER = "com.lingtuan.firefly.service.xmppservice.offlinemessagelist.listener";


    /**
     * The message everyone into the chat rooms
     */
    public static final String ACTION_ENTER_EVERYONE_LISTENER = "com.lingtuan.firefly.service.xmppservice.entereveryone.listener";


    /**
     * Update message broadcast distribution
     */
    public static final String ACTION_MESSAGE_UPDATE_LISTENER = "com.lingtuan.firefly.service.xmppservice.message.update.listener";

    /**
     * The message update T and resolution
     */
    public static final String ACTION_MESSAGE_GROUP_KICK_LISTENER = "com.lingtuan.firefly.service.xmppservice.message.kick.listener";
    /**
     * Message session record distribution
     */
    public static final String ACTION_MESSAGE_EVENT_LISTENER = "com.lingtuan.firefly.service.xmppservice.message.event.listener";
    /**
     * The message list record distribution session
     */
    public static final String ACTION_OFFLINE_MESSAGE_LIST_EVENT_LISTENER = "com.lingtuan.firefly.service.xmppservice.offlinemessagelist.event.listener";

    /**
     * The message offline
     */
    public static final String ACTION_MAIN_OFFLINE_LISTENER = "com.lingtuan.firefly.service.xmppservice.message.main.offline.listener";

    /**
     * Update message unread quantity
     */
    public static final String ACTION_MAIN_UNREADMSG_UPDATE_LISTENER = "com.lingtuan.firefly.service.xmppservice.message.unreadmsg.update.listener";

    /**
     * Offline message anchor force
     */
    public static final String ACTION_MAIN_LIVING_OFFLINE_LISTENER = "com.lingtuan.firefly.service.xmppservice.message.main.living.offline.listener";

    /**
     * Increase the level of a message
     */
    public static final String ACTION_MAIN_LIVING_UPDATE_LEVEL = "com.lingtuan.firefly.service.xmppservice.message.main.living.updatelevel.listener";


    /**
     * Message sending pictures percentage
     */
    public static final String ACTION_MESSAGE_IMAGE_PERCENT = "com.lingtuan.firefly.service.xmppservice.message.image.percent";


    /**
     * Message sending video percentage
     */
    public static final String ACTION_MESSAGE_VIDEO_PERCENT = "com.lingtuan.firefly.service.xmppservice.message.video.percent";

    /**
     * Message sending files percentage
     */
    public static final String ACTION_MESSAGE_FILE_PERCENT = "com.lingtuan.firefly.service.xmppservice.message.file.percent";

    /**
     * Distribute the AddContact notifications
     */
    public static final String ACTION_MESSAGE_ADDCONTACT = "com.lingtuan.firefly.service.xmppservice.message.addcontact";

    /**
     * Distribute AddInvite notifications
     */
    public static final String ACTION_MESSAGE_ADDINVITE = "com.lingtuan.firefly.service.xmppservice.message.addinvite";

    /**
     * AddGroup news distribution
     */
    public static final String ACTION_MESSAGE_ADDGROUP = "com.lingtuan.firefly.service.xmppservice.message.addgroup";


    /**
     * AddGroup news distribution
     */
    public static final String ACTION_MESSAGE_ADDMONEY = "com.lingtuan.firefly.service.xmppservice.message.addmoney";


    /**
     * XMPP friends recommend information distribution
     */
    public static final String ACTION_FRIEND_RECOMMENT = "com.lingtuan.firefly.service.xmppservice.friendrecomment";


}
