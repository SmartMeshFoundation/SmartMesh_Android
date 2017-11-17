package com.lingtuan.firefly.contact;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.contact.adapter.RecommentContactListAdapter;
import com.lingtuan.firefly.contact.vo.FriendRecommentVo;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.service.LoadDataService;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.MyViewDialogFragment;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.vo.UserBaseVo;
import com.lingtuan.firefly.xmpp.XmppAction;

import java.util.List;

/**
 * Recommend friends list page
 */
public class RecommentContactListUI extends BaseActivity implements OnItemClickListener {

	private ListView listView;
	private List<FriendRecommentVo> mList;
	private RecommentContactListAdapter mAdapter;
	private AddContactMsgReceiverListener addContactMsgReceiverListener;
	
	private TextView toAdd = null ;
	
	@Override
	protected void setContentView() {
		setContentView(R.layout.public_list_layout);
	}

	@Override
	protected void findViewById() {
		listView = (ListView) findViewById(R.id.listview);
		toAdd = (TextView)findViewById(R.id.toAdd);
	}

	@Override
	protected void setListener() {
		listView.setOnItemClickListener(this);
		listView.setEmptyView(findViewById(R.id.emptyBody));
		toAdd.setOnClickListener(this);
	}

	@Override
	protected void initData() {
		setTitle(getString(R.string.contact_new_friends));
		
		
		mAdapter = new RecommentContactListAdapter(mList, this);
		listView.setAdapter(mAdapter);
		
		addContactMsgReceiverListener = new AddContactMsgReceiverListener();
		LocalBroadcastManager.getInstance(this).registerReceiver(addContactMsgReceiverListener, new IntentFilter(XmppAction.ACTION_FRIEND_RECOMMENT));
		new Thread(new UpdateMessage()).start();
	}
	
	@Override
	protected void onResume() {
		super.onResume();

	}
	
    @Override
    protected void onDestroy() {
    	if(addContactMsgReceiverListener!=null){
			LocalBroadcastManager.getInstance(this).unregisterReceiver(addContactMsgReceiverListener);
    	}
    	super.onDestroy();
    }
	
    @Override
    protected void onPause() {
    	super.onPause();
    	FinalUserDataBase.getInstance().updateFriendsRecommentUnread();
    }
    
    @Override
    public void onClick(View v) {
    	super.onClick(v);
    	switch (v.getId()) {
		case R.id.toAdd:
			final String tempGxb = "tempGxb" ;
			boolean flag = MySharedPrefs.readBooleanNormal(RecommentContactListUI.this, MySharedPrefs.FILE_USER, tempGxb);
			if(flag){
				startActivity(new Intent(RecommentContactListUI.this,AddContactFriendsNewUI.class));
				Utils.openNewActivityAnim(RecommentContactListUI.this, false);
			}else{
				MyViewDialogFragment mdf = new MyViewDialogFragment();
				mdf.setTitleAndContentText(getString(R.string.notif), getString(R.string.contact_smt_accect));
				mdf.setOkCallback(new MyViewDialogFragment.OkCallback() {
					@Override
					public void okBtn() {
						/**Send the address book to monitor radio*/
						Intent intent = new Intent(LoadDataService.ACTION_START_CONTACT_LISTENER);
						Utils.sendBroadcastReceiver(RecommentContactListUI.this, intent, false);
						MySharedPrefs.writeBoolean(RecommentContactListUI.this, MySharedPrefs.FILE_USER, tempGxb, true);
						startActivity(new Intent(RecommentContactListUI.this,AddContactFriendsNewUI.class));
						Utils.openNewActivityAnim(RecommentContactListUI.this, false);
					}
				});
				mdf.show(getSupportFragmentManager(), "mdf");
			}
			break;

		default:
			break;
		}
    }
    
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
		UserBaseVo info = new UserBaseVo();
		info.setLocalId(mList.get(position).getUid());
		info.setUsername(mList.get(position).getUsername());
		info.setThumb(mList.get(position).getThumb());		
		Utils.intentFriendUserInfo(this, info, false);
	}

	/**
	 * Add buddy news broadcast
	 */
	class AddContactMsgReceiverListener extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent != null && (XmppAction.ACTION_FRIEND_RECOMMENT.equals(intent.getAction()))){
				Bundle bundle = intent.getExtras();
                FriendRecommentVo msg = bundle.getParcelable(XmppAction.ACTION_FRIEND_RECOMMENT);
				if(msg != null){
					mList.add(0,msg);
					mAdapter.updateList(mList);
				}
			}
		}
	}
	
	class UpdateMessage implements Runnable {
		@SuppressLint("HandlerLeak")
		@SuppressWarnings("unchecked")
		private Handler handler = new Handler(){
			public void handleMessage(android.os.Message msg) {
				mList = (List<FriendRecommentVo>) msg.obj;
				mAdapter.updateList(mList);
			}
		};
		
		@Override
		public void run() {
			List<FriendRecommentVo> mList = FinalUserDataBase.getInstance().getFriendsRecomment();
			Message msg = new Message();
			msg.obj = mList;
			handler.sendMessage(msg);
		}
	}
}
