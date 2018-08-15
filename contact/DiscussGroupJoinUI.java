package com.lingtuan.firefly.contact;

import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.contact.contract.DiscussGroupJoinContract;
import com.lingtuan.firefly.contact.presenter.DiscussGroupJoinPresenterImpl;
import com.lingtuan.firefly.contact.vo.DiscussionGroupsVo;
import com.lingtuan.firefly.custom.DiscussGroupImageView;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.vo.UserBaseVo;
import com.lingtuan.firefly.vo.UserInfoVo;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

public class DiscussGroupJoinUI extends BaseActivity implements DiscussGroupJoinContract.View{

	@BindView(R.id.join_discuss_group)
	TextView joinBtn;
	@BindView(R.id.join_discuss_group_member)
	TextView nameTextView;
	@BindView(R.id.join_discuss_group_num)
	TextView numTextView;
	@BindView(R.id.join_discuss_group_avatar)
	DiscussGroupImageView groupImageView;
	@BindView(R.id.groupNotexist)
	TextView groupNotexist;

	private DiscussGroupJoinContract.Presenter mPresenter;

	private String cid;
	private ArrayList<UserBaseVo> data = new ArrayList<>();
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

	}

	@Override
	protected void setListener() {

	}

	@OnClick(R.id.join_discuss_group)
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()){
			case R.id.join_discuss_group:
				mPresenter.joinDiscussGroup(cid,data);
				break;
		}
	}

	@Override
	protected void initData() {
		new DiscussGroupJoinPresenterImpl(this);
		setTitle(getString(R.string.join_group));
		if(TextUtils.isEmpty(cid)){
			cid=getIntent().getStringExtra("groupid");
		}
		mPresenter.getDiscussMembers(cid,data);
	}

	@Override
	public void setPresenter(DiscussGroupJoinContract.Presenter presenter) {
		this.mPresenter = presenter;
	}

	@Override
	public void getDiscussMembersSuccess(DiscussionGroupsVo vo, ArrayList<UserBaseVo> data) {
		this.data = data;
		groupName= vo.getName();
		groupImageView.setMember(data);
		nameTextView.setText(groupName);
		numTextView.setText(getString(R.string.total_num, vo.getMembers().size()));
	}

	@Override
	public void getDiscussMembersError(int errorCode, String errorMsg) {
		if (errorCode == 1240820){
			groupNotexist.setVisibility(View.VISIBLE);
			joinBtn.setEnabled(false);
		}
	}

	@Override
	public void joinDiscussGroupStart() {
		LoadingDialog.show(this,"");
	}

	@Override
	public void joinDiscussGroupSuccess() {
		LoadingDialog.close();
		Utils.gotoGroupChat(DiscussGroupJoinUI.this,false,groupName,cid,data);
	}

	@Override
	public void joinDiscussGroupError(int errorCode, String errorMsg) {
		LoadingDialog.close();
		showToast(errorMsg);
		if(errorCode==1240922){
			Utils.gotoGroupChat(DiscussGroupJoinUI.this,true,groupName,cid,data);
		}
	}
}
