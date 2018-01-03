package com.lingtuan.firefly.contact;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.chat.ChattingManager;
import com.lingtuan.firefly.contact.adapter.GroupMemberImageAdapter;
import com.lingtuan.firefly.contact.vo.DiscussGroupMemberVo;
import com.lingtuan.firefly.contact.vo.DiscussionGroupsVo;
import com.lingtuan.firefly.custom.GridViewWithHeaderAndFooter;
import com.lingtuan.firefly.custom.GridViewWithHeaderAndFooter.OnTouchBlankPositionListener;
import com.lingtuan.firefly.custom.SwitchButton;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.quickmark.GroupQuickMarkUI;
import com.lingtuan.firefly.quickmark.QuickMarkShowUI;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MyViewDialogFragment;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;
import com.lingtuan.firefly.vo.ChatMsg;
import com.lingtuan.firefly.vo.UserBaseVo;
import com.lingtuan.firefly.xmpp.XmppAction;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Group chat Settings
 */
public class DiscussGroupSettingUI extends BaseActivity implements GroupMemberImageAdapter.GroupMemberImageListener {

	private GridViewWithHeaderAndFooter grid;
	private View footerView;
	private TextView nameEdit;
	private RelativeLayout eidtNameBg;
	private SwitchButton switchBtn;
	private TextView dissmissBtn;

	private GroupMemberImageAdapter adapter;
	private boolean isAdmin = false;
	private int cid;
	private String nickname;
	private String avatarurl;
	private Dialog mProgressDialog;

	private RelativeLayout mQuickMarkRela;
	
	private ImageView notifyClock = null ; // by :KNothing
	private boolean hasMove=false;
	private LinearLayout allMember;
	private TextView allNum;
	private List<UserBaseVo> data  = new ArrayList<>();
	private List<UserBaseVo> showdata = new ArrayList<>();
	@Override
	protected void setContentView() {
		setContentView(R.layout.discuss_group_setting);
	}

	@Override
	protected void findViewById() {
		grid = (GridViewWithHeaderAndFooter) findViewById(R.id.discuss_group_settting_grid);
		footerView =  LayoutInflater.from(this).inflate(R.layout.discuss_group_setting_footer, null, false);
		
		nameEdit = (TextView) footerView.findViewById(R.id.discuss_group_setting_name);
		eidtNameBg = (RelativeLayout) footerView.findViewById(R.id.discuss_group_setting_edit);
		switchBtn = (SwitchButton) footerView.findViewById(R.id.discuss_group_setting_switch);
		dissmissBtn = (TextView) footerView.findViewById(R.id.discuss_group_setting_dissmiss);

		mQuickMarkRela = (RelativeLayout) footerView.findViewById(R.id.discuss_group_setting_quickmark);
		notifyClock = (ImageView) footerView.findViewById(R.id.notifyClock);
		allMember = (LinearLayout) footerView.findViewById(R.id.discuss_group_all_member);
		allNum = (TextView) footerView.findViewById(R.id.discuss_group_all_num);
	}

	@Override
	protected void setListener() {
		allMember.setOnClickListener(this);
		mQuickMarkRela.setOnClickListener(this);
		eidtNameBg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showRename_Dialog(DiscussGroupSettingUI.this);
			}
		});
		dissmissBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dissmissDicusss();
			}
		});
		grid.setOnTouchBlankPositionListener(new OnTouchBlankPositionListener() {
			@Override
			public void onTouchBlank(MotionEvent event) {
				if (adapter.isRemmoveState()){
				    switch(event.getActionMasked()){
				      case MotionEvent.ACTION_DOWN:
				    	hasMove=false;
			    	    break;
				      case MotionEvent.ACTION_MOVE:
				    	hasMove=true;
			    	    break;
				      case MotionEvent.ACTION_UP:
				    	if(!hasMove){
				    	  adapter.setRemoveState(false);
						  adapter.notifyDataSetChanged();
				    	}
				    	break;
				   }
				}
				
				 
			}
		});
		//Sliding when not to download the images
	    grid.setOnScrollListener(new PauseOnScrollListener(NextApplication.mImageLoader, true, true));

	}

	@Override
	protected void initData() {
		setTitle(getString(R.string.setting));
		allNum.setText(getString(R.string.discuss_all,0));
		grid.addFooterView(footerView);
		cid = getIntent().getIntExtra("cid", 0);
		String response= Utils.readFromFile("conversation-get_mumbers"+cid+".json");//First loads the local cache of json
		if(!TextUtils.isEmpty(response)){
			try {
				JSONObject json=new JSONObject(response);
				parseJson(json);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		adapter = new GroupMemberImageAdapter(this, showdata, isAdmin, this);
		grid.setAdapter(adapter);
		nickname = getIntent().getStringExtra("nickname");
		avatarurl = getIntent().getStringExtra("avatarurl");
		loadSettingData();
		nameEdit.setText(nickname);
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.discuss_group_all_member: {
			DiscussGroupMemberVo.getInstance().data1.clear();
			DiscussGroupMemberVo.getInstance().data1.addAll(data);
			DiscussGroupMemberVo.getInstance().showdata1.clear();
			DiscussGroupMemberVo.getInstance().showdata1.addAll(showdata);

			Intent intent = new Intent(this, DiscussGroupMemberListUI.class);
			intent.putExtra("cid", cid);
			intent.putExtra("isAdmin",isAdmin);
			intent.putExtra("name",nameEdit.getText().toString());
			startActivityForResult(intent, 100);
			Utils.openNewActivityAnim(this, false);
		}
			break;
		case R.id.discuss_group_setting_quickmark: {
			Intent intent = new Intent(this,GroupQuickMarkUI.class);
			intent.putExtra("id", cid + "");
			intent.putExtra("nickname", nameEdit.getText().toString());
			intent.putExtra("avatarurl", avatarurl);
			intent.putExtra("number", data.size());
			intent.putExtra("type", 1);
			startActivity(intent);
			Utils.openNewActivityAnim(this, false);
		}
			break;

		}
	}
	
	private void loadSettingData() {
		NetRequestImpl.getInstance().getDiscussMumbers(cid+"", new RequestListener() {
			@Override
			public void start() {

			}

			@Override
			public void success(JSONObject response) {
				Utils.writeToFile(response,"conversation-get_mumbers"+cid+".json");
				parseJson(response);
				try {
					FinalUserDataBase.getInstance().updateChatEventMask("group-" + cid, TextUtils.equals("1", switchBtn.isChecked()? "1" : "0"));
				} catch (Exception e) {
					e.printStackTrace();
				}
				adapter.setAdmin(isAdmin);
				adapter.notifyDataSetChanged();
			}

			@Override
			public void error(int errorCode, String errorMsg) {

			}
		});
	}

	private void showRename_Dialog(Context context) {
		MyViewDialogFragment mdf = new MyViewDialogFragment(MyViewDialogFragment.DIALOG_TEXT_ENTRY, getString(R.string.discuss_modify_name), null,  nameEdit.getText().toString());
		mdf.setEditOkCallback(new MyViewDialogFragment.EditOkCallback() {
			@Override
			public void okBtn(String edittext) {
				renameDicsuss(edittext);
			}
		});
		mdf.show(getSupportFragmentManager(), "mdf");
	}

	private void renameDicsuss(final String name) {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
		mProgressDialog = LoadingDialog.showDialog(this, null, null);
		mProgressDialog.setCancelable(false);
		NetRequestImpl.getInstance().renameDicsuss(name, cid, new RequestListener() {
			@Override
			public void start() {

			}

			@Override
			public void success(JSONObject response) {
				nameEdit.setText(name);
				showToast(response.optString("msg"));
				if (mProgressDialog != null) {
					mProgressDialog.dismiss();
					mProgressDialog = null;
				}
				reNameChatMsg(name);
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

	/**
	 * Forge a system modification group chat information
	 */
	private void reNameChatMsg(final String name) {
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
		chatmsg.setType(17);
		chatmsg.setSend(1);
		chatmsg.setContent(getString(R.string.discuss_group_rename,chatmsg.getGroupName()));
		chatmsg.setMsgTime(System.currentTimeMillis() / 1000);
		chatmsg.setMessageId(UUID.randomUUID().toString());
		chatmsg.parseUserBaseVo(NextApplication.myInfo.getUserBaseVo());
		Bundle bundle = new Bundle();
		bundle.putSerializable(XmppAction.ACTION_MESSAGE_LISTENER, chatmsg);
		Utils.intentAction(getApplicationContext(),XmppAction.ACTION_MESSAGE_LISTENER, bundle);

		FinalUserDataBase.getInstance().saveChatMsg(chatmsg, chatmsg.getChatId(), name, url, false);
	}
	/**
	 * Forge a invite others to join group chat message
	 */
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
		chatmsg.setGroupName(nameEdit.getText().toString());
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

		FinalUserDataBase.getInstance().saveChatMsg(chatmsg, chatmsg.getChatId(), nameEdit.getText().toString(), url, false);
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
		chatmsg.setGroupName(nameEdit.getText().toString());
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

		FinalUserDataBase.getInstance().saveChatMsg(chatmsg, chatmsg.getChatId(), nameEdit.getText().toString(), url, false);
	}

	public void removeMember(final int arg2) {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
		mProgressDialog = LoadingDialog.showDialog(this, null, null);
		mProgressDialog.setCancelable(false);
		NetRequestImpl.getInstance().removeDiscussMember(showdata.get(arg2).getLocalId(), cid, new RequestListener() {
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
				UserBaseVo user = showdata.get(arg2);
				data.remove(user);
				showdata.remove(user);
				removeMemberChatMsg(user.getShowName());
				resetlist();
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

	private void resetlist(){
		int size = 0;
		if(isAdmin){
			size = data.size()<=38?data.size():38;
		}else{
			size = data.size()<=39?data.size():39;
		}
		showdata.clear();
		for(int i=0;i<size;i++){
			showdata.add(data.get(i));
		}
		if (isAdmin) {
			showdata.add(new UserBaseVo());
			showdata.add(new UserBaseVo());
		} else {
			showdata.add(new UserBaseVo());
		}
		adapter.notifyDataSetChanged();
	}

	/**
	 * Set the notification,
	 * */
	private void switchNotify() {
		NetRequestImpl.getInstance().switchNotify(switchBtn.isChecked(), cid, new RequestListener() {
			@Override
			public void start() {

			}

			@Override
			public void success(JSONObject response) {
				showToast(response.optString("msg"));

				try {
					FinalUserDataBase.getInstance().updateChatEventMask("group-" + cid, TextUtils.equals("1", switchBtn.isChecked()? "1" : "0"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public void error(int errorCode, String errorMsg) {
				showToast(errorMsg);
			}
		});
	}

	private void dissmissDicusss() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
		mProgressDialog = LoadingDialog.showDialog(this, null, null);
		mProgressDialog.setCancelable(false);
		NetRequestImpl.getInstance().removeDiscussMember(NextApplication.myInfo.getLocalId(), cid, new RequestListener() {
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
				ChattingManager.getInstance(DiscussGroupSettingUI.this).setFinish(true);
				Utils.exitActivityAndBackAnim(DiscussGroupSettingUI.this,true);
				FinalUserDataBase.getInstance().deleteChatMsgByChatId("group-" + cid);
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

	public void addMembers() {
		ArrayList<String> alreadySelected = new ArrayList<>();
		for (int i = 0; i < data.size(); i++) {
			UserBaseVo info =data.get(i);
			if(!TextUtils.isEmpty(info.getLocalId())&&!info.getLocalId().equals(NextApplication.myInfo.getLocalId()))
			{
			   alreadySelected.add(info.getLocalId());
			}
		}
		Intent intent  = new Intent(DiscussGroupSettingUI.this,SelectContactUI.class);
		intent.putExtra("cantSelectList", alreadySelected);
		intent.putExtra("isMultipleChoice", true);
		startActivityForResult(intent, 0);
		Utils.openNewActivityAnim(DiscussGroupSettingUI.this, false);
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
				resetlist();

				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						adapter.notifyDataSetChanged();
					}
				}, 100);
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


    /*Parsing json*/
	private void parseJson(JSONObject response){
		JSONObject jsonObject = response.optJSONObject("data");
		DiscussionGroupsVo vo = new DiscussionGroupsVo().parse(jsonObject);
		nameEdit.setText(vo.getName());
		data.clear();
		showdata.clear();
		for (int i = 0; i < vo.getMembers().size(); i++) {
			UserBaseVo info = vo.getMembers().get(i);
			if (i == 0 && info.getLocalId().equals(NextApplication.myInfo.getLocalId())) {
				isAdmin = true;
			}
			data.add(info);
			if(isAdmin){
				if(i<=37){
					showdata.add(info);
				}
			}else if(i<=38){
				showdata.add(info);
			}
		}
		if (isAdmin) {
			showdata.add(new UserBaseVo());
			showdata.add(new UserBaseVo());
			eidtNameBg.setClickable(true);
			dissmissBtn.setText(getResources().getString(R.string.group_dismiss_discussion));
		} else {
			showdata.add(new UserBaseVo());
			eidtNameBg.setClickable(false);
			dissmissBtn.setText(getResources().getString(R.string.group_out_discussion));
		}
		allNum.setText(getString(R.string.discuss_all,vo.getMembers().size()));
		switchBtn.setOnCheckedChangeListener(null);
		if (0 == vo.getMask()) {
			switchBtn.setChecked(false);
		} else {
			switchBtn.setChecked(true);
			notifyClock.setVisibility(View.VISIBLE);
		}

		switchBtn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
				if (isChecked) {
					notifyClock.setVisibility(View.VISIBLE);
				} else {
					notifyClock.setVisibility(View.GONE);
				}
				switchNotify();
			}
		});
		
		dissmissBtn.setVisibility(View.VISIBLE);
    }


	
	protected void onActivityResult(int requestCode, int resultCode, Intent data1) {
		if(requestCode==0&&resultCode==RESULT_OK){//Select the contact to return
			ArrayList<UserBaseVo> selectList = (ArrayList<UserBaseVo>) data1.getSerializableExtra("selectList");
			if(selectList!=null&&!selectList.isEmpty()){
			  addMembersRequest(selectList);
			}
		}else if(requestCode == 100){
			data.clear();
			data.addAll(DiscussGroupMemberVo.getInstance().data1);
			showdata.clear();
			showdata.addAll(DiscussGroupMemberVo.getInstance().showdata1);
			resetlist();
		}
		super.onActivityResult(requestCode, resultCode, data1);
	}

	@Override
	public void clickMember(int arg2) {
		Utils.intentFriendUserInfo(DiscussGroupSettingUI.this, showdata.get(arg2), false);
	}

}
