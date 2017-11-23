package com.lingtuan.firefly.chat;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.custom.SwitchButton;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;
import com.lingtuan.firefly.vo.UserBaseVo;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Chat page top right corner (stranger Settings page)
 */
public class ChattingSetUI extends BaseActivity implements OnCheckedChangeListener {

	private SwitchButton notNotifySb  = null ;

	
	private ImageView notifyClock = null ;
	
	public static final int CHOICE_REASON_REQUEST_CODE = 0x11;
	
	/** People who are chatting id */
	private String uid = null ;
	private String userName;
	private String avatarUrl;
	private String gender;

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
		notNotifySb = (SwitchButton)findViewById(R.id.notNotifySb);
		notifyClock = (ImageView)findViewById(R.id.notifyClock);
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
		getChatNotifyStatus();
	}

	/**
	 * To get the user's screen
	 */
	private void getChatNotifyStatus() {
		NetRequestImpl.getInstance().getMaskUser(uid, new RequestListener() {
			@Override
			public void start() {

			}
			@Override
			public void success(JSONObject response) {
				JSONObject result = response.optJSONObject("data");
				if (result != null) {
					String statusString = result.optString("status");
					if (!TextUtils.isEmpty(statusString) && TextUtils.equals("1", statusString)) {
						notifyClock.setVisibility(View.VISIBLE);
						notNotifySb.setChecked(true);
					} else {
						notNotifySb.setChecked(false);
					}
					try {
						MySharedPrefs.writeBoolean(NextApplication.mContext, MySharedPrefs.FILE_USER, MySharedPrefs.IS_MASK_MSG + NextApplication.myInfo.getLocalId() + "_" + uid, TextUtils.equals("1", statusString));
						FinalUserDataBase.getInstance().updateChatEventMask(uid, TextUtils.equals("1", statusString));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				notNotifySb.setOnCheckedChangeListener(ChattingSetUI.this); // The listener must be written request to the state, otherwise you will be prompted extra information
			}

			@Override
			public void error(int errorCode, String errorMsg) {
				showToast(errorMsg);
			}
		});
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.notNotifySb:
			setNotify(isChecked);
			break;
		default:
			break;
		}
	}
	/*The message block*/
	private void setNotify(final boolean isChecked){
		NetRequestImpl.getInstance().maskUser(uid, isChecked, new RequestListener() {
			@Override
			public void start() {

			}

			@Override
			public void success(JSONObject response) {
				if(isChecked){
					notifyClock.setVisibility(View.VISIBLE);
				}else{
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
			public void error(int errorCode, String errorMsg) {
				notNotifySb.setOnCheckedChangeListener(null);
				notNotifySb.setChecked(!isChecked);
				notNotifySb.setOnCheckedChangeListener(ChattingSetUI.this);
				showToast(errorMsg);
			}
		});
	}

}
