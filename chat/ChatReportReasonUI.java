package com.lingtuan.firefly.chat;

import android.app.Dialog;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;
import com.lingtuan.firefly.vo.ChatMsg;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 聊天投诉理由
 */
public class ChatReportReasonUI extends BaseActivity implements OnCheckedChangeListener {

	private RelativeLayout titleBar = null;
	private TextView sendTv = null;
	
	private Dialog mProgressDialog;

	/** The only receipt startActivityForResult */
	private static final int CHOICE_CHAT_MSG_REQUESTCODE = 0x01;

	private CheckBox fraudCb, sexyCb, cursingCb, adverseCb, politicalCb;
	
	private TextView selectedCount = null ;
	
	/** Complaints from the user id and related information */
	private String buid = null ;
	private String userName;
	private String avatarUrl;
	private String gender;
	private String remoteSource;
	private int mFriendLog = 0;//与聊天人的好友关系
	
	/** Types of reporting (7, fraud money, 8, pornography, 9, cursing, 10, advertising harassment, 11, political reactionary), multiple USES, split */
	private String type = null ;
	
	/** Report content, optional (json format chats, attention to the scheduling problem of the content of the chat) */
	private ArrayList<ChatMsg> msgList;
	
	/**The following for temporary variables, eventually joining together*/
	String seven = "-1";
	String eight = "-1" ;
	String nine = "-1" ;
	String ten = "-1" ;
	String eleven = "-1" ;

	@Override
	protected void setContentView() {
		setContentView(R.layout.report_reason_layout);
		getPassData();
	}

	private void getPassData() {
		if (getIntent() != null && getIntent().getExtras() != null ) {
				buid = getIntent().getStringExtra("buid");
				userName = getIntent().getExtras().getString("username");
				avatarUrl = getIntent().getExtras().getString("avatarurl");
				gender = getIntent().getExtras().getString("gender");
				mFriendLog = getIntent().getExtras().getInt("friendLog");
			    remoteSource = getIntent().getExtras().getString("remoteSource");
		}
	}

	@Override
	protected void findViewById() {

		sendTv = (TextView) findViewById(R.id.app_btn_right);
		sendTv.setVisibility(View.VISIBLE);
		sendTv.setText(getString(R.string.send));
		titleBar = (RelativeLayout) findViewById(R.id.privacyTitle);

		fraudCb = (CheckBox) findViewById(R.id.fraudCb);
		sexyCb = (CheckBox) findViewById(R.id.sexyCb);
		cursingCb = (CheckBox) findViewById(R.id.cursingCb);
		adverseCb = (CheckBox) findViewById(R.id.adverseCb);
		politicalCb = (CheckBox) findViewById(R.id.politicalCb);
		selectedCount = (TextView)findViewById(R.id.reportCount);
	}

	@Override
	protected void setListener() {
		sendTv.setOnClickListener(this);
		fraudCb.setOnCheckedChangeListener(this);
		sexyCb.setOnCheckedChangeListener(this);
		cursingCb.setOnCheckedChangeListener(this);
		adverseCb.setOnCheckedChangeListener(this);
		politicalCb.setOnCheckedChangeListener(this);
		findViewById(R.id.uploadBody).setOnClickListener(this);
		
		findViewById(R.id.publicBody).setOnClickListener(this);
		findViewById(R.id.friendsBody).setOnClickListener(this);
		findViewById(R.id.allBody).setOnClickListener(this);
		findViewById(R.id.peraonalBody).setOnClickListener(this);
		findViewById(R.id.politicalBody).setOnClickListener(this);
	}

	@Override
	protected void initData() {
		setTitle(getString(R.string.report_reason));
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		
		case R.id.app_btn_right: // send
			sendReportChatMsg();
			break;
			
		case R.id.uploadBody: // Select the chat logs and upload
			Intent report = new Intent(ChatReportReasonUI.this,ChatHistoryUI.class);
			report.putExtra("buid", buid);
			report.putExtra("avatarurl", avatarUrl);
			report.putExtra("username", userName);
			report.putExtra("gender", gender);
			report.putExtra("friendLog", mFriendLog);
			report.putExtra("selectedList", msgList);
			report.putExtra("remoteSource",remoteSource);
			startActivityForResult(report, CHOICE_CHAT_MSG_REQUESTCODE);
			Utils.openNewActivityAnim(ChatReportReasonUI.this, false);
			break;
			
		case R.id.publicBody:
			fraudCb.setChecked(!fraudCb.isChecked());
			break;
			
		case R.id.friendsBody:
			sexyCb.setChecked(!sexyCb.isChecked());
			break;
			
		case R.id.allBody:
			cursingCb.setChecked(!cursingCb.isChecked());
			break;
			
		case R.id.peraonalBody:
			adverseCb.setChecked(!adverseCb.isChecked());
			break;
			
		case R.id.politicalBody:
			politicalCb.setChecked(!politicalCb.isChecked());
			break;
			
		default:
			break;
		}
	}

	private void sendReportChatMsg() {
		
		if(TextUtils.isEmpty(buid)){
			return ; 
		}
		if(TextUtils.equals("-1", seven) && TextUtils.equals("-1", eight)&& TextUtils.equals("-1", nine)&& TextUtils.equals("-1", ten)&& TextUtils.equals("-1", eleven)){
			showToast(getResources().getString(R.string.report_reason_choice));
			return ;
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append(seven);
		sb.append(",");
		sb.append(eight);
		sb.append(",");
		sb.append(nine);
		sb.append(",");
		sb.append(ten);
		sb.append(",");
		sb.append(eleven);
		sb.append(",");
		String replace0String =sb.toString().replace("-1,", "");
		type = replace0String.substring(0, replace0String.length()-1);
		
		if(msgList == null ){
			showToast(getResources().getString(R.string.chat_please_choice_msg));
			return ;
		}
		
		/** Chat logs together*/
		ChatMsg cmBuild = new ChatMsg();
		String contentString = cmBuild.parseChatMsgListToJsonString(msgList);
		if(TextUtils.isEmpty(contentString)){
			showToast(getResources().getString(R.string.chat_please_choice_msg));
			return ;
		}

		NetRequestImpl.getInstance().sendReportChatMsg(type, buid, contentString, new RequestListener() {
			@Override
			public void start() {
				if(mProgressDialog == null ){
					mProgressDialog = LoadingDialog.showDialog(ChatReportReasonUI.this, null, getString(R.string.chatting_sending_two));
				}
			}

			@Override
			public void success(JSONObject response) {
				if(mProgressDialog != null){
					mProgressDialog.dismiss();
				}
				showToast(response.optString("msg"));
				setResult(RESULT_OK);
				Utils.exitActivityAndBackAnim(ChatReportReasonUI.this,true);
			}

			@Override
			public void error(int errorCode, String errorMsg) {
				if(mProgressDialog != null){
					mProgressDialog.dismiss();
				}
				showToast(errorMsg);
			}
		});
	}
	

	@SuppressWarnings("unchecked")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == CHOICE_CHAT_MSG_REQUESTCODE && resultCode == RESULT_OK){ // Has chosen to chat content
			if(data != null ){
				if(msgList != null && !msgList.isEmpty()){
					msgList.clear();
				}
				msgList = (ArrayList<ChatMsg>) data.getSerializableExtra("msglist");
				if(msgList != null && msgList.size() > 0 ){
					selectedCount.setText(getResources().getString(R.string.report_msg_count,msgList.size()));
				}else{
					selectedCount.setText(getResources().getString(R.string.report_no_choice_guest));
				}
			}else{
				showToast(getString(R.string.chat_please_choice_msg));
			}
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {

		case R.id.fraudCb: // Fraud cheat money
			if (isChecked) {
				seven = "7" ;
			} else {
				seven = "-1" ;
			}
			break;

		case R.id.sexyCb: // Porn related
			if (isChecked) {
				eight = "8";
			} else {
				eight = "-1";
			}
			break;

		case R.id.cursingCb: // cursing
			if (isChecked) {
				nine = "9";
			} else {
				nine = "-1";
			}
			break;

		case R.id.adverseCb: // >The ads
			if (isChecked) {
				ten = "10" ;
			} else {
				ten = "-1" ;
			}
			break;

		case R.id.politicalCb: // Political reactionary
			if (isChecked) {
				eleven = "11";
			} else {
				eleven = "-1";
			}
			break;
		default:
			break;
		}
	}
}
