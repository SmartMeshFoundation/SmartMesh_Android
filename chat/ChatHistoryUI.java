package com.lingtuan.firefly.chat;

import android.content.Intent;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.chat.adapter.ChatAdapter;
import com.lingtuan.firefly.custom.LoadMoreListView;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.util.MyToast;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.vo.ChatMsg;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @TODO  Chat chat information reporting page (the difference between with page, as complex functions do not need to chat page, you just choose to report)
 */
public class ChatHistoryUI extends BaseActivity implements OnRefreshListener {


	private ListView listView;
	private SwipeRefreshLayout swipeLayout;
	private ChatAdapter mAdapter;
	private ChattingManager chattingManager;
	private TextView mRightBtn;
	
	/** Complaints from the user id and related information */
	private String buid = null ;
	private String userName;
	private String avatarUrl;
	private String gender;
	private String remoteSource;
	private ArrayList<ChatMsg> selectedList = null ;

	@Override
	protected void setContentView() {
		setContentView(R.layout.loadmore_list_layout);
		getPassData();
	}

	private void getPassData() {
		if (getIntent() != null && getIntent().getExtras() != null ) {
			buid = getIntent().getStringExtra("buid");
			userName = getIntent().getExtras().getString("username");
			avatarUrl = getIntent().getExtras().getString("avatarurl");
			gender = getIntent().getExtras().getString("gender");
			selectedList = (ArrayList<ChatMsg>)(getIntent().getSerializableExtra("selectedList"));
			remoteSource = getIntent().getExtras().getString("remoteSource");
		}
	}

	@Override
	protected void findViewById() {
		mRightBtn = (TextView) findViewById(R.id.app_btn_right);
		listView = (LoadMoreListView) findViewById(R.id.refreshListView);
		swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
	}

	@Override
	protected void setListener() {
		mRightBtn.setOnClickListener(this);
		swipeLayout.setOnRefreshListener(this);
	}

	@Override
	protected void initData() {
		swipeLayout.setColorSchemeResources(R.color.black);
		
		setTitle(getResources().getString(R.string.report_chat_title));
		mRightBtn.setText(getResources().getString(R.string.submit));
		mRightBtn.setVisibility(View.VISIBLE);
		
		ChattingManager.getInstance(this).destory();
		chattingManager = ChattingManager.getInstance(this);
		
		List<ChatMsg> mList = FinalUserDataBase.getInstance().getChatMsgListByChatId(buid,0,20);
		mAdapter = new ChatAdapter(mList, this,null,listView);
		chattingManager.setUserInfo(userName, avatarUrl, buid,mAdapter,listView);
		mAdapter.addSelectedList(selectedList, true);
		listView.setAdapter(mAdapter);
		listView.setSelection(mList.size());
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
		FinalUserDataBase.getInstance().updateUnreadEventChat(buid);
		
		if(ChattingManager.getInstance(this).isFinish()){
			ChattingManager.getInstance(this).setFinish(false);
			Utils.exitActivityAndBackAnim(this,true);
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		FaceUtils.getInstance(this).destory();
		FinalUserDataBase.getInstance().updateChatEventMsgGender(buid, gender);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mAdapter.destory();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		chattingManager.onActivityResult(requestCode, resultCode, data);
	}
	

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
           new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				List<ChatMsg> mTempList = FinalUserDataBase.getInstance().getChatMsgListByChatId(buid,mAdapter.getCount(),20);
				final int position = mTempList.size();
				mTempList.addAll(mAdapter.getList());
				mAdapter.updateList(mTempList);
				swipeLayout.setRefreshing(false);
				listView.setSelection(position + 1);
			}
		}, 1000);
	}
	
	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.app_btn_right:
				if(mAdapter.getSelectedList() == null || mAdapter.getSelectedList().size() == 0){
					MyToast.showToast(this, getString(R.string.chat_file_empty));
					return;
				}
				ArrayList<ChatMsg> list = new ArrayList<>();
				for(Map.Entry<String ,ChatMsg> s : mAdapter.getSelectedList().entrySet()){
					list.add(s.getValue());
				}
				Intent data = new Intent();
				data.putExtra("msglist", list);
				setResult(RESULT_OK, data);
				finish();
			break;
		default:
			break;
		}
	}

}
