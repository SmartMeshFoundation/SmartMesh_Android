package com.lingtuan.firefly.contact;

import android.app.Dialog;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.contact.vo.DiscussionGroupsVo;
import com.lingtuan.firefly.custom.DiscussGroupImageView;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;
import com.lingtuan.firefly.vo.UserBaseVo;
import com.lingtuan.firefly.vo.UserInfoVo;

import org.json.JSONObject;

import java.util.ArrayList;

public class DiscussGroupJoinUI extends BaseActivity {

	private String cid;
	private TextView joinBtn;
	private TextView nameTextView;
	private TextView numTextView;
	private DiscussGroupImageView groupImageView;
	private ArrayList<UserBaseVo> data = new ArrayList<>();
	private Dialog mProgressDialog;
	private String groupName;
	@Override
	protected void setContentView() {
		setContentView(R.layout.discuss_group_join_layout);
		try {
			if(NextApplication.myInfo == null || TextUtils.isEmpty(NextApplication.myInfo.getLocalId())){
				NextApplication.myInfo = new UserInfoVo().readMyUserInfo(this);
			}
			String s = getIntent().toURI();
	        Uri parse = Uri.parse(s);
	        String gid = parse.getQueryParameter("gid");
	        if(!TextUtils.isEmpty(gid)){
	        	cid = gid;
	        }else{
	        	String uid = parse.getQueryParameter("uid");
	        	UserBaseVo info = new UserBaseVo(); 
				info.setLocalId(uid);
	        	if(!TextUtils.isEmpty(uid)){
	        		Utils.intentFriendUserInfo(this, info, true);
	        	}
	        }
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
	}
	
	@Override
	protected void findViewById() {
		joinBtn=(TextView) findViewById(R.id.join_discuss_group);
		groupImageView=(DiscussGroupImageView) findViewById(R.id.join_discuss_group_avatar);
		nameTextView=(TextView) findViewById(R.id.join_discuss_group_member);
		numTextView=(TextView) findViewById(R.id.join_discuss_group_num);
	}

	@Override
	protected void setListener() {
		joinBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				joinDiscussGroup();
			}
		});
	}

	@Override
	protected void initData() {
		if(TextUtils.isEmpty(cid)){
			cid=getIntent().getStringExtra("groupid");
		}
		loadDicussGroupData();
	}
	
	
	private void loadDicussGroupData(){
		if(TextUtils.isEmpty(cid)){
			return;
		}
		NetRequestImpl.getInstance().getDiscussMumbers(cid, new RequestListener() {
			@Override
			public void start() {

			}

			@Override
			public void success(JSONObject response) {
				JSONObject jsonObject = response.optJSONObject("data");
				DiscussionGroupsVo vo = new DiscussionGroupsVo().parse(jsonObject);
				for(int i=0;i<vo.getMembers().size();i++){
					UserBaseVo info=vo.getMembers().get(i);
					data.add(info);
				}
				groupName=vo.getName();
				groupImageView.setMember(data);
				nameTextView.setText(groupName);
				numTextView.setText(getString(R.string.total_num, vo.getMembers().size()));
			}

			@Override
			public void error(int errorCode, String errorMsg) {

			}
		});
	}

	private void joinDiscussGroup(){
		if(TextUtils.isEmpty(cid))
		{
			return;
		}
		if (data.size() <= 0) {
			return;
		}
		
		if(mProgressDialog != null){
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
		mProgressDialog = LoadingDialog.showDialog(this, null, null);
		mProgressDialog.setCancelable(false);
		NetRequestImpl.getInstance().joinDiscussGroup(cid, new RequestListener() {
			@Override
			public void start() {

			}

			@Override
			public void success(JSONObject response) {
				if(mProgressDialog != null){
					mProgressDialog.dismiss();
					mProgressDialog = null;
					Utils.gotoGroupChat(DiscussGroupJoinUI.this,false,groupName,cid,data);
				}
			}

			@Override
			public void error(int errorCode, String errorMsg) {
				if(mProgressDialog != null){
					mProgressDialog.dismiss();
					mProgressDialog = null;
				}
				showToast(errorMsg);
				if(errorCode==1240922){
					Utils.gotoGroupChat(DiscussGroupJoinUI.this,true,groupName,cid,data);
				}
			}
		});
	}
}
