package com.lingtuan.firefly.chat;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.chat.contract.ChattingSetContract;
import com.lingtuan.firefly.chat.presenter.ChattingSetPresenterImpl;
import com.lingtuan.firefly.custom.switchbutton.SwitchButton;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.vo.UserBaseVo;

/**
 * Chat page top right corner (stranger Settings page)
 */
public class ChattingSetUI extends BaseActivity implements OnCheckedChangeListener ,ChattingSetContract.View{

	private SwitchButton notNotifySb  = null ;

	
	private ImageView notifyClock = null ;
	
	public static final int CHOICE_REASON_REQUEST_CODE = 0x11;
	
	/** People who are chatting id */
	private String uid = null ;
	private String userName;
	private String avatarUrl;
	private String gender;

	private ChattingSetContract.Presenter mPresenter;

	@Override
	protected void setContentView() {
		setContentView(R.layout.chatting_set_layout);
		getPassData();
	}

	private void getPassData() {
		if(getIntent() != null && getIntent().getExtras() != null ){
			uid = getIntent().getExtras().getString("uid");
			userName = getIntent().getExtras().getString("username");
			avatarUrl = getIntent().getExtras().getString("avatarurl");
			gender = getIntent().getExtras().getString("gender");
		}
	}

	@Override
	protected void findViewById() {
		notNotifySb = findViewById(R.id.notNotifySb);
		notifyClock = findViewById(R.id.notifyClock);
	}

	@Override
	protected void setListener() {
		findViewById(R.id.chatUserInfo).setOnClickListener(this);
		findViewById(R.id.chatInfoReport).setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		
		case R.id.chatUserInfo: // The personal data
			UserBaseVo info=new UserBaseVo();
			info.setLocalId(uid);
			info.setUsername(userName);
			info.setGender(gender);
			info.setThumb(avatarUrl);
			Utils.intentFriendUserInfo(this, info, false);
			break;
			
		case R.id.chatInfoReport: // Chat to report
			Intent report = new Intent(ChattingSetUI.this,ChatReportReasonUI.class);
			report.putExtra("buid", uid);
			report.putExtra("avatarurl", avatarUrl);
			report.putExtra("username", userName);
			report.putExtra("gender", gender);
			startActivityForResult(report, CHOICE_REASON_REQUEST_CODE);
			Utils.openNewActivityAnim(ChattingSetUI.this, false);
			break;
			
		default:
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK && requestCode == CHOICE_REASON_REQUEST_CODE){
			Utils.exitActivityAndBackAnim(this,true);
		}
	}

	@Override
	protected void initData() {
		setTitle(getResources().getString(R.string.chatting_set));
		new ChattingSetPresenterImpl(this);
		mPresenter.getMaskUser(uid);
	}


	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.notNotifySb:
			if (isChecked){
				notNotifySb.setBackColor(getResources().getColorStateList(R.color.switch_button_green));
			}else{
				notNotifySb.setBackColor(getResources().getColorStateList(R.color.switch_button_gray));
			}
			mPresenter.maskUser(uid,isChecked);
			break;
		default:
			break;
		}
	}

	@Override
	public void setPresenter(ChattingSetContract.Presenter presenter) {
		this.mPresenter = presenter;
	}

	@Override
	public void getMaskUserSuccess(String statusString) {
		if (!TextUtils.isEmpty(statusString) && TextUtils.equals("1", statusString)) {
			notifyClock.setVisibility(View.VISIBLE);
			notNotifySb.setChecked(true);
			notNotifySb.setBackColor(getResources().getColorStateList(R.color.switch_button_green));
		} else {
			notNotifySb.setChecked(false);
			notNotifySb.setBackColor(getResources().getColorStateList(R.color.switch_button_gray));
		}
		try {
			MySharedPrefs.writeBoolean(NextApplication.mContext, MySharedPrefs.FILE_USER, MySharedPrefs.IS_MASK_MSG + NextApplication.myInfo.getLocalId() + "_" + uid, TextUtils.equals("1", statusString));
			FinalUserDataBase.getInstance().updateChatEventMask(uid, TextUtils.equals("1", statusString));
		} catch (Exception e) {
			e.printStackTrace();
		}
		notNotifySb.setOnCheckedChangeListener(ChattingSetUI.this); // The listener must be written request to the state, otherwise you will be prompted extra information
	}

	@Override
	public void getMaskUserError(int errorCode, String errorMsg) {
		showToast(errorMsg);
	}

	@Override
	public void maskUserSuccess(boolean isChecked) {
		if(isChecked){
			notNotifySb.setBackColor(getResources().getColorStateList(R.color.switch_button_green));
			notifyClock.setVisibility(View.VISIBLE);
		}else{
			notNotifySb.setBackColor(getResources().getColorStateList(R.color.switch_button_gray));
			notifyClock.setVisibility(View.GONE);
		}
		try {
			MySharedPrefs.writeBoolean(NextApplication.mContext, MySharedPrefs.FILE_USER, MySharedPrefs.IS_MASK_MSG + NextApplication.myInfo.getLocalId() + "_" + uid, isChecked);
			FinalUserDataBase.getInstance().updateChatEventMask(uid, isChecked);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void maskUserError(int errorCode, String errorMsg,boolean isChecked) {
		notNotifySb.setOnCheckedChangeListener(null);
		notNotifySb.setChecked(!isChecked);
		if (!isChecked){
			notNotifySb.setBackColor(getResources().getColorStateList(R.color.switch_button_green));
		}else{
			notNotifySb.setBackColor(getResources().getColorStateList(R.color.switch_button_gray));
		}
		notNotifySb.setOnCheckedChangeListener(ChattingSetUI.this);
		showToast(errorMsg);
	}
}
