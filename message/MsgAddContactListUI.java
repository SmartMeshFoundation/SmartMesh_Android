package com.lingtuan.firefly.message;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.contact.adapter.ContactAddAdapter;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.offline.AppNetService;
import com.lingtuan.firefly.offline.vo.WifiPeopleVO;
import com.lingtuan.firefly.util.MyDialogFragment;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.MyToast;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;
import com.lingtuan.firefly.vo.ChatMsg;
import com.lingtuan.firefly.vo.UserBaseVo;
import com.lingtuan.firefly.xmpp.XmppAction;

import org.json.JSONObject;

import java.util.List;

/**
 * Add buddy list page
 */
public class MsgAddContactListUI extends BaseActivity implements OnItemClickListener, OnRefreshListener, OnItemLongClickListener, ContactAddAdapter.ContactAddAdapterListener {

	private ListView listView;
	private List<ChatMsg> mList;
	private ContactAddAdapter mAdapter;
	
	private String chatId;
	private SwipeRefreshLayout swipeLayout;
	private AddContactMsgReceiverListener addContactMsgReceiverListener;


	private AppNetService appNetService;

	@Override
	protected void setContentView() {
		setContentView(R.layout.loadmore_list_layout);
	}

	@Override
	protected void findViewById() {
		listView = (ListView) findViewById(R.id.refreshListView);
		chatId = getIntent().getExtras().getString("chatid");
		swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
	}

	@Override
	protected void setListener() {
		swipeLayout.setOnRefreshListener(this);
		listView.setOnItemClickListener(this);
		listView.setOnItemLongClickListener(this);
	}

	//The Activity and netService2 connection
	private ServiceConnection serviceConn = new ServiceConnection() {
		public void onServiceConnected(ComponentName name, IBinder service) {
			// Binding service success
			AppNetService.NetServiceBinder binder = (AppNetService.NetServiceBinder) service;
			appNetService = binder.getService();
		}
		public void onServiceDisconnected(ComponentName name) {
		}
	};

	@Override
	protected void initData() {
		if (NextApplication.myInfo != null){
			int openSmartMesh = MySharedPrefs.readInt1(NextApplication.mContext,MySharedPrefs.FILE_USER,MySharedPrefs.KEY_NO_NETWORK_COMMUNICATION + NextApplication.myInfo.getLocalId());
			if (openSmartMesh == 1){
				bindService(new Intent(this, AppNetService.class), serviceConn,BIND_AUTO_CREATE);
			}
		}
		swipeLayout.setColorSchemeResources(R.color.black);
		setTitle(getString(R.string.contact_add_contact));
		
		FinalUserDataBase.getInstance().updateUnreadEventChat(chatId);
		
		mAdapter = new ContactAddAdapter(mList, this,this);
		listView.setAdapter(mAdapter);
		
		
		addContactMsgReceiverListener = new AddContactMsgReceiverListener();
		registerReceiver(addContactMsgReceiverListener, new IntentFilter(XmppAction.ACTION_MESSAGE_ADDCONTACT));
	    
		new Thread(new UpdateMessage()).start();
	}
    @Override
    protected void onDestroy() {
    	super.onDestroy();

		int openSmartMesh = MySharedPrefs.readInt1(NextApplication.mContext,MySharedPrefs.FILE_USER,MySharedPrefs.KEY_NO_NETWORK_COMMUNICATION + NextApplication.myInfo.getLocalId());
		if (openSmartMesh == 1 && serviceConn != null){
			unbindService(serviceConn);
		}
    	if(addContactMsgReceiverListener!=null)
    	{
    		unregisterReceiver(addContactMsgReceiverListener);
    	}
    }
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
		UserBaseVo info = new UserBaseVo();
		info.setLocalId(mList.get(position).getUserId());
		info.setUsername(mList.get(position).getUsername());
		info.setThumb(mList.get(position).getUserImage());
		Utils.intentFriendUserInfo(this, info, false);
	}

	@Override
	public void onRefresh() {
		swipeLayout.setRefreshing(false);
	}

	/**
	 * Best friend information
	 * */
	@Override
	public boolean addFriendConfim(String uid) {
		boolean isFound = false;
		if (appNetService != null){
			List<WifiPeopleVO> wifiPeopleVOs = appNetService.getwifiPeopleList();
			for (int i = 0 ; i < wifiPeopleVOs.size() ; i++){
				if (uid.equals(wifiPeopleVOs.get(i).getLocalId())){
					isFound = true;
					break;
				}
			}
		}
		if (isFound){
			appNetService.handleSendAddFriendCommend(uid,true);
			if (Utils.isConnectNet(MsgAddContactListUI.this)){
				uploadOfflineFriends(uid);
			}
			return true;
		}else{
			return false;
		}
	}

	/**
	 * Synchronous no net friends
	 * */
	private void uploadOfflineFriends(String uid) {
		NetRequestImpl.getInstance().addOfflineFriend(uid, new RequestListener() {

			@Override
			public void start() {

			}

			@Override
			public void success(JSONObject response) {

			}

			@Override
			public void error(int errorCode, String errorMsg) {

			}
		});
	}

	/**
	 * Add buddy news broadcast
	 */
	class AddContactMsgReceiverListener extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			//Add buddy news broadcast
			if(intent != null && (XmppAction.ACTION_MESSAGE_ADDCONTACT.equals(intent.getAction()))){
                Bundle bundle = intent.getBundleExtra(XmppAction.ACTION_MESSAGE_ADDCONTACT);
                ChatMsg msg = null ; 
                if(bundle != null ){
                	msg = (ChatMsg) bundle.getSerializable(XmppAction.ACTION_MESSAGE_ADDCONTACT);
                	if(msg != null){
                		if(mList != null ){
							for(int i=0;i<mList.size();i++)
							{
								if(msg.getUserId().equals(mList.get(i).getUserId()))
								{
									mList.remove(i);
								}
							}
                			mList.add(0,msg);
                			mAdapter.updateList(mList);
                		}
    					FinalUserDataBase.getInstance().updateUnreadEventChat(chatId);
    				}
                }
			}
		}
	}
	class UpdateMessage implements Runnable {
		
		private Handler handler = new Handler(){
			public void handleMessage(android.os.Message msg) {
					mList = (List<ChatMsg>) msg.obj;
					mAdapter.updateList(mList);
			}
		};
		
		@Override
		public void run() {
			List<ChatMsg> mList = FinalUserDataBase.getInstance().getChatMsgAddContactList();
			Message msg = new Message();
			msg.obj = mList;
			handler.sendMessage(msg);
			
		}
		
	}
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,final int position, long id) {
		
		MyDialogFragment mdf = new MyDialogFragment(MyDialogFragment.DIALOG_LIST,R.array.delete_chat_array);
		mdf.setItemClickCallback(new MyDialogFragment.ItemClickCallback() {
			@Override
			public void itemClickCallback(int which) {
				ChatMsg msg=mList.get(position);
				FinalUserDataBase.getInstance().deleteChatEventAddContactByMessageId(msg.getMessageId());
				MyToast.showToast(MsgAddContactListUI.this, getString(R.string.delete_success));
				mList.remove(position);
				mAdapter.updateList(mList);
			}
		});
		mdf.show(getSupportFragmentManager(), "mdf");
       return true;
	}

}
