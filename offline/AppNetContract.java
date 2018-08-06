package com.lingtuan.firefly.offline;

import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;

import com.lingtuan.firefly.base.BasePresenter;
import com.lingtuan.firefly.base.BaseView;
import com.lingtuan.firefly.offline.vo.WifiPeopleVO;
import com.lingtuan.firefly.vo.ChatMsg;
import com.lingtuan.firefly.vo.UserBaseVo;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;

public interface AppNetContract {

    interface Presenter extends BasePresenter{

        //registered p2p server
        void registerP2pService(WifiP2pManager manager,WifiP2pManager.Channel channel,WifiP2pDnsSdServiceInfo serviceInfo);

        //To monitor search results
        void setServiceResponse(WifiP2pManager manager,WifiP2pManager.Channel channel);

        //Locate near peer device
        void discoverPeers(WifiP2pManager manager,WifiP2pManager.Channel channel);

        void setServiceListeners(WifiP2pManager manager,WifiP2pManager.Channel channel);

        void discoverService(WifiP2pManager manager,WifiP2pManager.Channel channel);

        void renameDeviceName(WifiP2pManager manager,WifiP2pManager.Channel channel);

        void removeServiceListeners(WifiP2pManager manager,WifiP2pManager.Channel channel);

        void cancelDisconnect(WifiP2pManager manager, WifiP2pManager.Channel channel);

        void removeGroup(WifiP2pManager manager, WifiP2pManager.Channel channel);

        void connect(WifiP2pManager manager, WifiP2pManager.Channel channel,WifiP2pConfig config);

        void requestGroupInfo(WifiP2pManager manager, WifiP2pManager.Channel channel,WifiP2pDevice device,int type);

        void removeP2pService(WifiP2pManager manager,WifiP2pManager.Channel channel,WifiP2pDnsSdServiceInfo serviceInfo);

        void saveExitOrEnterChatMsg(String chatId, String uid, String username, String usergender, int type, String messageId,String content);

        boolean handleReceiveSystemMsg(InputStream ins, InetSocketAddress sockAddr,WifiPeopleVO meInfo,ArrayList<WifiPeopleVO> peerInfoList);

        boolean handleReceiveChatMsg(InputStream ins, boolean isRoomChat, InetSocketAddress sockAddr,WifiPeopleVO meInfo,ArrayList<WifiPeopleVO> peerInfoList);

    }

    interface View extends BaseView<Presenter>{

        //Connected to the current search to the equipment
        void connectDevice(WifiP2pDevice resourceType);

        boolean discoverPeers();

        void onGroupInfoAvailableConnect(WifiP2pGroup group,WifiP2pDevice device);

        void onGroupInfoAvailableUpdateList(WifiP2pGroup group);

        void onGroupInfoAvailable(WifiP2pGroup group);

        void onConnectFailure(int reason);

        void notifyFunction(ChatMsg chatMsg);

        void hasConnected(boolean hasConnect);

        void updateRecvPeerList(ArrayList<WifiPeopleVO> peerInfoList);

        void updateNewPeopleState(WifiPeopleVO vo,boolean isCome);

        void handleBroadcastPeerList(WifiPeopleVO newuser);

        void handleFileCommend(String msgid, int commend);

        void handleAddFriendCommend(UserBaseVo vo, int type, String host, int port, String deviceaddress);

        void postSendBytes(long currentLength, String msgid);

        void postSendMsgResult(boolean successed,int state, String msgid);
    }
}
