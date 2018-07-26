package com.lingtuan.firefly.message;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.adapter.MsgTransAdapter;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.util.MyDialogFragment;
import com.lingtuan.firefly.util.MyToast;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.vo.ChatMsg;
import com.lingtuan.firefly.wallet.TransactionDetailActivity;
import com.lingtuan.firefly.wallet.vo.TransVo;
import com.lingtuan.firefly.xmpp.XmppAction;

import java.util.List;

import butterknife.BindView;
import butterknife.OnItemClick;
import butterknife.OnItemLongClick;

/**
 * Add buddy list page
 */
public class MsgTransListUI extends BaseActivity implements  OnRefreshListener{

	@BindView(R.id.refreshListView)
	ListView listView;
	@BindView(R.id.swipe_container)
	SwipeRefreshLayout swipeLayout;

	private List<ChatMsg> mList;
	private MsgTransAdapter mAdapter;
	private String chatId;
	private TransMsgRecriver transMsgRecriver;

	@Override
	protected void setContentView() {
		setContentView(R.layout.loadmore_list_layout);
	}

	@Override
	protected void findViewById() {
		chatId = getIntent().getExtras().getString("chatid");
	}

	@Override
	protected void setListener() {
		swipeLayout.setOnRefreshListener(this);
	}

	@Override
	protected void initData() {
		swipeLayout.setColorSchemeResources(R.color.black);
		setTitle(getString(R.string.chat_token_notify));
		FinalUserDataBase.getInstance().updateUnreadEventChat(chatId);
		mAdapter = new MsgTransAdapter(mList, this);
		listView.setAdapter(mAdapter);
		transMsgRecriver = new TransMsgRecriver();
		registerReceiver(transMsgRecriver, new IntentFilter(XmppAction.ACTION_TRANS));

		new Thread(new UpdateMessage()).start();
	}
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	if(transMsgRecriver!=null){
    		unregisterReceiver(transMsgRecriver);
    	}
    }


	@Override
	public void onRefresh() {
		swipeLayout.setRefreshing(false);
	}


	/**
	 * Add buddy news broadcast
	 */
	class TransMsgRecriver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			//Add buddy news broadcast
			if(intent != null && (XmppAction.ACTION_TRANS.equals(intent.getAction()))){
                Bundle bundle = intent.getBundleExtra(XmppAction.ACTION_TRANS);
				ChatMsg chatMsg = null ;
                if(bundle != null ){
					chatMsg = (ChatMsg) bundle.getSerializable(XmppAction.ACTION_TRANS);
                	if(chatMsg != null){
                		if(mList != null ){
                			mList.add(0,chatMsg);
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
			public void handleMessage(Message msg) {
				mList = (List<ChatMsg>) msg.obj;
				mAdapter.updateList(mList);
			}
		};
		
		@Override
		public void run() {
			List<ChatMsg> mList = FinalUserDataBase.getInstance().getChatMsgTransList();
			Message msg = new Message();
			msg.obj = mList;
			handler.sendMessage(msg);
		}
		
	}

	@OnItemClick(R.id.refreshListView)
	void onItemClick(AdapterView<?> parent, View view, int position,long id) {
		Intent intent = new Intent(MsgTransListUI.this,TransactionDetailActivity.class);
		ChatMsg chatmsg = mList.get(position);
		TransVo transVo = new TransVo();
		transVo.setTime(chatmsg.getCreateTime());
		transVo.setType(Integer.parseInt(chatmsg.getMode()));
		transVo.setTxBlockNumber(Integer.parseInt(chatmsg.getTxBlockNumber()));
		transVo.setFee(chatmsg.getFee());
		transVo.setValue(chatmsg.getMoney());
		transVo.setTxurl(chatmsg.getShareUrl());
		transVo.setTx(chatmsg.getNumber());
		transVo.setFromAddress(chatmsg.getFromAddress());
		transVo.setToAddress(chatmsg.getToAddress());
		transVo.setState(chatmsg.getInviteType() + 1);
		transVo.setNoticeType(chatmsg.getNoticeType());
		transVo.setTokenAddress(chatmsg.getTokenAddress());
		transVo.setName(chatmsg.getTokenName());
		transVo.setSymbol(chatmsg.getTokenSymbol());
		transVo.setLogo(chatmsg.getTokenLogo());
		intent.putExtra("transVo",transVo);
		startActivity(intent);
		Utils.openNewActivityAnim(MsgTransListUI.this, false);
	}

	@OnItemLongClick(R.id.refreshListView)
	public boolean onItemLongClick(AdapterView<?> parent, View view,final int position, long id) {
		MyDialogFragment mdf = new MyDialogFragment(MyDialogFragment.DIALOG_LIST,R.array.delete_trans_array);
		mdf.setItemClickCallback(new MyDialogFragment.ItemClickCallback() {
			@Override
			public void itemClickCallback(int which) {
				ChatMsg msg=mList.get(position);
				FinalUserDataBase.getInstance().deleteChatEventTransByMessageId(msg.getMessageId());
				MyToast.showToast(MsgTransListUI.this, getString(R.string.delete_success));
				mList.remove(position);
				mAdapter.updateList(mList);
			}
		});
		mdf.show(getSupportFragmentManager(), "mdf");
       return true;
	}

}
