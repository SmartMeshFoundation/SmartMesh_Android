package com.lingtuan.firefly.contact;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.contact.adapter.DiscussGroupMemberListAdapter;
import com.lingtuan.firefly.contact.vo.DiscussGroupMemberVo;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MyViewDialogFragment;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;
import com.lingtuan.firefly.vo.ChatMsg;
import com.lingtuan.firefly.vo.UserBaseVo;
import com.lingtuan.firefly.xmpp.XmppAction;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Group chat Settings
 * @author caoyuting
 */
public class DiscussGroupMemberListUI extends BaseActivity implements OnItemClickListener,AdapterView.OnItemLongClickListener {

	private ListView listview;
	private TextView rightBtn;
	private DiscussGroupMemberListAdapter mAdapter;
	private Dialog mProgressDialog;
	private int cid;
	private String name;
	private boolean isAdmin;
	private List<UserBaseVo> data  = new ArrayList<>();
	private List<UserBaseVo> showdata = new ArrayList<>();
	@Override
	protected void setContentView() {
		setContentView(R.layout.discuss_group_member_list);
	}

	@Override
	protected void findViewById() {
		listview = (ListView)findViewById(R.id.listview);
		rightBtn = (TextView) findViewById(R.id.app_btn_right);
	}

	@Override
	protected void setListener() {
		listview.setOnItemClickListener(this);
		isAdmin  = getIntent().getBooleanExtra("isAdmin",false);
		if(isAdmin){
			listview.setOnItemLongClickListener(this);
		}
		rightBtn.setOnClickListener(this);

	}


	@Override
	protected void initData() {
		setTitle(getString(R.string.group_all_member));
		cid = getIntent().getIntExtra("cid", 0);
		name = getIntent().getStringExtra("name");
		rightBtn.setText(getString(R.string.add));
		rightBtn.setVisibility(View.VISIBLE);
		data.addAll(DiscussGroupMemberVo.getInstance().data1);
		showdata.addAll(DiscussGroupMemberVo.getInstance().showdata1);
		mAdapter = new DiscussGroupMemberListAdapter(this,data);
		listview.setAdapter(mAdapter);
	}

	public void addMembers() {
		ArrayList<String> alreadySelected = new ArrayList<String>();
		for (int i = 0; i < data.size(); i++) {
			UserBaseVo info = data.get(i);
			if(!TextUtils.isEmpty(info.getLocalId())&&!info.getLocalId().equals(NextApplication.myInfo.getLocalId()))
			{
				alreadySelected.add(info.getLocalId());
			}
		}
		Intent intent  = new Intent(DiscussGroupMemberListUI.this,SelectContactUI.class);
		intent.putExtra("cantSelectList", alreadySelected);
		intent.putExtra("isMultipleChoice", true);
		startActivityForResult(intent, 0);
		Utils.openNewActivityAnim(DiscussGroupMemberListUI.this, false);
	}
	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId())
		{
			case R.id.app_btn_right:
				addMembers();
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if(view != null ){
			Utils.intentFriendUserInfo(this, data.get(position), false);
		}
	}
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,final int position, long id) {

		if(position == 0){
			return  true;
		}
		MyViewDialogFragment mdf = new MyViewDialogFragment();
		mdf.setTitleAndContentText(getString(R.string.notif), getString(R.string.tip_del_member));
		mdf.setOkCallback(new MyViewDialogFragment.OkCallback() {
			@Override
			public void okBtn() {
				removeMember(position);
			}
		});
		mdf.show(getSupportFragmentManager(), "mdf");
		return true;
	}

	public void removeMember(final int arg2) {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
		mProgressDialog = LoadingDialog.showDialog(this, null, null);
		mProgressDialog.setCancelable(false);
		NetRequestImpl.getInstance().removeDiscussMember(data.get(arg2).getLocalId(), cid, new RequestListener() {
			@Override
			public void start() {

			}

			@Override
			public void success(JSONObject response) {
				if (mProgressDialog != null) {
					mProgressDialog.dismiss();
					mProgressDialog = null;
				}
				showToast(response.optString("msg"));
				UserBaseVo user = data.get(arg2);
				data.remove(user);
				showdata.remove(user);
				removeMemberChatMsg(user.getShowName());
				mAdapter.notifyDataSetChanged();
			}

			@Override
			public void error(int errorCode, String errorMsg) {
				if (mProgressDialog != null) {
					mProgressDialog.dismiss();
					mProgressDialog = null;
				}
				showToast(errorMsg);
			}
		});
	}

	private void inviteOthersChatMsg(final String name) {
		StringBuilder sb = new StringBuilder();
		int index=1;
		for (UserBaseVo vo : data) {
			if(index>4)//Most need four pictures
			{
				break;
			}
			sb.append(vo.getThumb()).append("___").append(vo.getGender()).append("#");
			index++;
		}
		sb.deleteCharAt(sb.lastIndexOf("#"));
		String url = sb.toString();

		ChatMsg chatmsg = new ChatMsg();
		chatmsg.setChatId("group-" + cid);
		chatmsg.setGroupName(name);
		chatmsg.setGroup(true);
		chatmsg.setType(13);
		chatmsg.setSend(1);
		chatmsg.setContent(getString(R.string.discuss_group_invite_others,name));
		chatmsg.setMsgTime(System.currentTimeMillis() / 1000);
		chatmsg.setMessageId(UUID.randomUUID().toString());
		chatmsg.parseUserBaseVo(NextApplication.myInfo.getUserBaseVo());
		Bundle bundle = new Bundle();
		bundle.putSerializable(XmppAction.ACTION_MESSAGE_LISTENER, chatmsg);
		Utils.intentAction(getApplicationContext(),XmppAction.ACTION_MESSAGE_LISTENER, bundle);

		FinalUserDataBase.getInstance().saveChatMsg(chatmsg, chatmsg.getChatId(), name ,url, false);
	}
	/**
	 * Forge a delete members of the group chat message
	 */
	private void removeMemberChatMsg(final String name) {
		StringBuilder sb = new StringBuilder();
		int index=1;
		for (UserBaseVo vo : data) {
			if(index>4)//Most need four pictures
			{
				break;
			}
			sb.append(vo.getThumb()).append("___").append(vo.getGender()).append("#");
			index++;
		}
		sb.deleteCharAt(sb.lastIndexOf("#"));
		String url = sb.toString();

		ChatMsg chatmsg = new ChatMsg();
		chatmsg.setChatId("group-" + cid);
		chatmsg.setGroupName(name);
		chatmsg.setGroup(true);
		chatmsg.setType(14);
		chatmsg.setSend(1);
		chatmsg.setContent(getString(R.string.discuss_group_remove_other,name));
		chatmsg.setMsgTime(System.currentTimeMillis() / 1000);
		chatmsg.setMessageId(UUID.randomUUID().toString());
		chatmsg.parseUserBaseVo(NextApplication.myInfo.getUserBaseVo());
		Bundle bundle = new Bundle();
		bundle.putSerializable(XmppAction.ACTION_MESSAGE_LISTENER, chatmsg);
		Utils.intentAction(getApplicationContext(),XmppAction.ACTION_MESSAGE_LISTENER, bundle);

		FinalUserDataBase.getInstance().saveChatMsg(chatmsg, chatmsg.getChatId(), name, url, false);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode==0&&resultCode==RESULT_OK){//Select the contact to return
			ArrayList<UserBaseVo> selectList = (ArrayList<UserBaseVo>) data.getSerializableExtra("selectList");
			if(selectList!=null&&!selectList.isEmpty()){
				addMembersRequest(selectList);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	private void addMembersRequest(final ArrayList<UserBaseVo> continueList) {
		if (continueList.size() <= 0) {
			return;
		}

		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
		mProgressDialog = LoadingDialog.showDialog(this, null, null);
		mProgressDialog.setCancelable(false);

		StringBuffer touids = new StringBuffer();
		final StringBuffer tonames = new StringBuffer();
		for (int i = 0; i < continueList.size(); i++) {
			if (i == continueList.size() - 1) {
				touids.append(continueList.get(i).getLocalId());
				tonames.append(continueList.get(i).getShowName());
			} else {
				touids.append(continueList.get(i).getLocalId() + ",");
				tonames.append(continueList.get(i).getShowName()+ ",");
			}
		}

		NetRequestImpl.getInstance().addDiscussMembers(touids.toString(), cid, new RequestListener() {
			@Override
			public void start() {

			}

			@Override
			public void success(JSONObject response) {
				if (mProgressDialog != null) {
					mProgressDialog.dismiss();
					mProgressDialog = null;
				}
				showToast(response.optString("msg"));
				for (int m = 0; m < continueList.size(); m++) {
					if (isAdmin) {
						data.add(continueList.get(m));
						showdata.add(showdata.size() - 2, continueList.get(m));
					} else {
						data.add(continueList.get(m));
						showdata.add(showdata.size() - 1, continueList.get(m));
					}
				}
				inviteOthersChatMsg(tonames.toString());
				mAdapter.notifyDataSetChanged();
			}

			@Override
			public void error(int errorCode, String errorMsg) {
				if (mProgressDialog != null) {
					mProgressDialog.dismiss();
					mProgressDialog = null;
				}
				showToast(errorMsg);
			}
		});
	}

	@Override
	public void finish() {
		super.finish();
		DiscussGroupMemberVo.getInstance().data1.clear();
		DiscussGroupMemberVo.getInstance().data1.addAll(data);
		DiscussGroupMemberVo.getInstance().showdata1.clear();
		DiscussGroupMemberVo.getInstance().showdata1.addAll(showdata);
	}
}
