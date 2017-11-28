package com.lingtuan.firefly.xmpp;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.service.XmppService;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.Utils;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Mode;
import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smack.provider.PrivacyProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.GroupChatInvitation;
import org.jivesoftware.smackx.PrivateDataManager;
import org.jivesoftware.smackx.packet.ChatStateExtension;
import org.jivesoftware.smackx.packet.LastActivity;
import org.jivesoftware.smackx.packet.OfflineMessageInfo;
import org.jivesoftware.smackx.packet.OfflineMessageRequest;
import org.jivesoftware.smackx.packet.SharedGroupsInfo;
import org.jivesoftware.smackx.provider.AdHocCommandDataProvider;
import org.jivesoftware.smackx.provider.BytestreamsProvider;
import org.jivesoftware.smackx.provider.DataFormProvider;
import org.jivesoftware.smackx.provider.DelayInformationProvider;
import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverItemsProvider;
import org.jivesoftware.smackx.provider.MUCAdminProvider;
import org.jivesoftware.smackx.provider.MUCOwnerProvider;
import org.jivesoftware.smackx.provider.MUCUserProvider;
import org.jivesoftware.smackx.provider.MessageEventProvider;
import org.jivesoftware.smackx.provider.MultipleAddressesProvider;
import org.jivesoftware.smackx.provider.RosterExchangeProvider;
import org.jivesoftware.smackx.provider.StreamInitiationProvider;
import org.jivesoftware.smackx.provider.VCardProvider;
import org.jivesoftware.smackx.provider.XHTMLExtensionProvider;
import org.jivesoftware.smackx.search.UserSearch;


public class XmppUtils implements ConnectionListener {

	/**openfire服务器所在的ip */
	public static String SERVER_HOST = Constants.GLOBAL_SWITCH_OPEN ? "xmpp.yueni.cc" : "118.89.56.218" ;
	
	public static String SERVER_NAME = "firefly.com";//Set the openfire server name
	public static int    SERVER_PORT = 6222;//Can be set on the openfire service port
	public static String SERVER_SEARCH =  "search." + SERVER_NAME;
	public static String RESOURCE = "app_v1.0.0";
	public static final int LOGIN_ERROR_REPEAT = 409;//Repeat login
	public static final int LOGIN_ERROR_NET = 502;//The service is not available
	public static final int LOGIN_ERROR_PWD = 401;//Wrong password or other
	public static final int LOGIN_ERROR= 404;//An unknown error
	private static final String TAG = "XmppUtils";
	private static XMPPConnection conn;
	private static XmppUtils instance;
	
	public static boolean isLogining = false;//Login is in XmppService do judgment
	
	public static boolean isOnline;//Whether online
	
	public static XmppUtils getInstance(){
		
		if(instance == null){
			instance = new XmppUtils();
		}
		return instance;
	}
	
	/**
	 * Create a XMPP connection instance
	 * 
	 * @return
	 * @throws org.jivesoftware.smack.XMPPException
	 */
	public void createConnection() throws XMPPException {
		XMPPConnection.DEBUG_ENABLED = true;//Open the DEBUG mode
		//Configure the connection
		ConnectionConfiguration config = new ConnectionConfiguration(
				SERVER_HOST, SERVER_PORT,
				SERVER_NAME);
		config.setReconnectionAllowed(true);
		config.setSASLAuthenticationEnabled(true);
		
		config.setSendPresence(false);//Don't send the online status
		conn = null;
		conn = new XMPPConnection(config);
		conn.connect();//To connect to the server
		//Configuration of the Provider if not configured Will be unable to parse the data
		SERVER_NAME = conn.getServiceName();
		conn.addConnectionListener(this);
		configureConnection(ProviderManager.getInstance());

	}
	
	public XMPPConnection getConnection(){
		return conn;
	}
	
	/**
	 * login
	 * @throws XMPPException 
	 */
	public void login(String username, String password) throws XMPPException{
		if(NextApplication.myInfo != null){
//			RESOURCE = NextApplication.myInfo.getToken();
			RESOURCE = "app_v1.0.0"+ Utils.getIMEI(NextApplication.mContext);
			
		}
			getConnection().login(username, password, RESOURCE);
//			XFriendManager.getInstance().addRosterListener();
			isOnline = true;
	}
	
	public static void loginXmppForNextApp(Context mContext){
		Bundle mBundle = new Bundle();
		if(NextApplication.myInfo != null && !TextUtils.isEmpty(NextApplication.myInfo.getToken())){
			String username = NextApplication.myInfo.getLocalId();
//			XmppUtils.SERVER_HOST = NextApplication.myInfo.getHost();
//			XmppUtils.RESOURCE = NextApplication.myInfo.getToken();
			mBundle.putStringArray(XmppAction.ACTION_LOGIN, new String[]{username,NextApplication.myInfo.getToken()});
			
			Utils.intentService(mContext, XmppService.class, XmppAction.ACTION_LOGIN,
					XmppAction.ACTION_LOGIN,mBundle);
		}
	}
	
	/**
	 * Whether have the login
	 */
	public boolean isLogin(){
		if(conn == null) return false;//Connection is not generated
		else if(!conn.isConnected()) return false;//Connection is not effective
		else if(!conn.isAuthenticated()) return false;//Connection of unauthorized
		return true;
	}
	
	public void sendOnLine(){
		if(conn == null || !conn.isConnected() || !conn.isAuthenticated())
			throw new RuntimeException(NextApplication.mContext.getString(R.string.normal_connect_error));
		
		Presence presence = new Presence(Type.available);
		presence.setMode(Mode.chat);
		conn.sendPacket(presence);
		
	}
	
	/**
	 * Close the XmppConnection connection
	 */
	public void closeConn() {
		if (null != conn && conn.isConnected()) {
			Presence pres = new Presence(Presence.Type.unavailable);
			conn.disconnect(pres);
			conn = null;
		}
		isOnline = false;
	}
	
	/**
	 * XMPP configuration
	 */
	private void configureConnection(ProviderManager pm) {
		// Private Data Storage
		pm.addIQProvider("query", "jabber:iq:private",
				new PrivateDataManager.PrivateDataIQProvider());

		// Time
		try {
			pm.addIQProvider("query", "jabber:iq:time",
					Class.forName("org.jivesoftware.smackx.packet.Time"));
		} catch (Exception e) {
			e.printStackTrace();
			// Logger.v(TAG,
			// "Can't load class for org.jivesoftware.smackx.packet.Time");
		}

		// Roster Exchange
		pm.addExtensionProvider("x", "jabber:x:roster",
				new RosterExchangeProvider());

		// Message Events
		pm.addExtensionProvider("x", "jabber:x:event",
				new MessageEventProvider());

		// Chat State
		pm.addExtensionProvider("active",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());

		pm.addExtensionProvider("composing",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());

		pm.addExtensionProvider("paused",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());

		pm.addExtensionProvider("inactive",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());

		pm.addExtensionProvider("gone",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());

		// XHTML
		pm.addExtensionProvider("html", "http://jabber.org/protocol/xhtml-im",
				new XHTMLExtensionProvider());

		// Group Chat Invitations
		pm.addExtensionProvider("x", "jabber:x:conference",
				new GroupChatInvitation.Provider());

		// Service Discovery # Items //Analytical room list
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#items",
				new DiscoverItemsProvider());

		// Service Discovery # Info //A single room
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#info",
				new DiscoverInfoProvider());

		// Data Forms
		pm.addExtensionProvider("x", "jabber:x:data", new DataFormProvider());

		// MUC User
		pm.addExtensionProvider("x", "http://jabber.org/protocol/muc#user",
				new MUCUserProvider());

		// MUC Admin
		pm.addIQProvider("query", "http://jabber.org/protocol/muc#admin",
				new MUCAdminProvider());

		// MUC Owner
		pm.addIQProvider("query", "http://jabber.org/protocol/muc#owner",
				new MUCOwnerProvider());

		// Delayed Delivery
		pm.addExtensionProvider("x", "jabber:x:delay",
				new DelayInformationProvider());

		// Version
		try {
			pm.addIQProvider("query", "jabber:iq:version",
					Class.forName("org.jivesoftware.smackx.packet.Version"));
		} catch (ClassNotFoundException e) {
			// Not sure what's happening here.
		}
		// VCard
		pm.addIQProvider("vCard", "vcard-temp", new VCardProvider());

		// Offline Message Requests
		pm.addIQProvider("offline", "http://jabber.org/protocol/offline",
				new OfflineMessageRequest.Provider());

		// Offline Message Indicator
		pm.addExtensionProvider("offline",
				"http://jabber.org/protocol/offline",
				new OfflineMessageInfo.Provider());

		// Last Activity
		pm.addIQProvider("query", "jabber:iq:last", new LastActivity.Provider());

		// User Search
		pm.addIQProvider("query", "jabber:iq:search", new UserSearch.Provider());

		// SharedGroupsInfo
		pm.addIQProvider("sharedgroup",
				"http://www.jivesoftware.org/protocol/sharedgroup",
				new SharedGroupsInfo.Provider());

		// JEP-33: Extended Stanza Addressing
		pm.addExtensionProvider("addresses",
				"http://jabber.org/protocol/address",
				new MultipleAddressesProvider());
		// FileTransfer
		pm.addIQProvider("si", "http://jabber.org/protocol/si",
				new StreamInitiationProvider());

		pm.addIQProvider("query", "http://jabber.org/protocol/bytestreams",
				new BytestreamsProvider());

		// pm.addIQProvider("open", "http://jabber.org/protocol/ibb",
		// new IBBProviders.Open());
		//
		// pm.addIQProvider("close", "http://jabber.org/protocol/ibb",s
		// new IBBProviders.Close());
		//
		// pm.addExtensionProvider("data", "http://jabber.org/protocol/ibb",
		// new IBBProviders.Data());

		// Privacy
		pm.addIQProvider("query", "jabber:iq:privacy", new PrivacyProvider());

		pm.addIQProvider("command", "http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider());
		pm.addExtensionProvider("malformed-action",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.MalformedActionError());
		pm.addExtensionProvider("bad-locale",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.BadLocaleError());
		pm.addExtensionProvider("bad-payload",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.BadPayloadError());
		pm.addExtensionProvider("bad-sessionid",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.BadSessionIDError());
		pm.addExtensionProvider("session-expired",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.SessionExpiredError());
		
	}

	@Override
	public void connectionClosed() {
		isOnline = false;
	}

	@Override
	public void connectionClosedOnError(Exception e) {
		isOnline = false;
	}

	@Override
	public void reconnectingIn(int seconds) {
		
	}

	@Override
	public void reconnectionSuccessful() {
		
	}

	@Override
	public void reconnectionFailed(Exception e) {
		
	}
	
	public void destroy(){
		if(conn != null){
			try {
				conn.disconnect();
				conn.removeConnectionListener(this);
				conn = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		instance = null;
	}
}
