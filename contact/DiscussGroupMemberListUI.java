package com.lingtuan.firefly.contact;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.contact.adapter.DiscussGroupMemberListAdapter;
import com.lingtuan.firefly.contact.contract.DiscussGroupMemberListContract;
import com.lingtuan.firefly.contact.presenter.DiscussGroupMemberListPresenterImpl;
import com.lingtuan.firefly.contact.vo.DiscussGroupMemberVo;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MyViewDialogFragment;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.vo.UserBaseVo;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnItemClick;

/**
 * Group chat Settings
 * @author caoyuting
 */
public class DiscussGroupMemberListUI extends BaseActivity implements AdapterView.OnItemLongClickListener, DiscussGroupMemberListContract.View {

	@BindView(R.id.listview)
	ListView listview;
	@BindView(R.id.app_btn_right)
	TextView rightBtn;

	private DiscussGroupMemberListContract.Presenter mPresenter;

	private DiscussGroupMemberListAdapter mAdapter;
	private int cid;
	private String groupName;
	private boolean isAdmin;
	private List<UserBaseVo> data  = new ArrayList<>();
	private List<UserBaseVo> showdata = new ArrayList<>();
	@Override
	protected void setContentView() {
		setContentView(R.layout.discuss_group_member_list);
	}

	@Override
	protected void findViewById() {

	}

	@Override
	protected void setListener() {
		isAdmin  = getIntent().getBooleanExtra("isAdmin",false);
		if(isAdmin){
			listview.setOnItemLongClickListener(this);
		}
	}


	@Override
	protected void initData() {
		new DiscussGroupMemberListPresenterImpl(this);
		setTitle(getString(R.string.group_all_member));
		cid = getIntent().getIntExtra("cid", 0);
		groupName = getIntent().getStringExtra("name");
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
			if(!TextUtils.isEmpty(info.getLocalId())&&!info.getLocalId().equals(NextApplication.myInfo.getLocalId())){
				alreadySelected.add(info.getLocalId());
			}
		}
		Intent intent  = new Intent(DiscussGroupMemberListUI.this,SelectContactUI.class);
		intent.putExtra("cantSelectList", alreadySelected);
		intent.putExtra("isMultipleChoice", true);
		startActivityForResult(intent, 0);
		Utils.openNewActivityAnim(DiscussGroupMemberListUI.this, false);
	}

	@OnClick(R.id.app_btn_right)
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
			case R.id.app_btn_right:
				 addMembers();
			break;
		}
	}

	@OnItemClick(R.id.listview)
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
				mPresenter.removeDiscussMember(position,data.get(position).getLocalId(),cid);
			}
		});
		mdf.show(getSupportFragmentManager(), "mdf");
		return true;
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode==0&&resultCode==RESULT_OK){//Select the contact to return
			ArrayList<UserBaseVo> selectList = (ArrayList<UserBaseVo>) data.getSerializableExtra("selectList");
			if(selectList!=null&&!selectList.isEmpty()){
				mPresenter.addMembersRequest(selectList,cid);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void finish() {
		super.finish();
		DiscussGroupMemberVo.getInstance().data1.clear();
		DiscussGroupMemberVo.getInstance().data1.addAll(data);
		DiscussGroupMemberVo.getInstance().showdata1.clear();
		DiscussGroupMemberVo.getInstance().showdata1.addAll(showdata);
	}

	@Override
	public void setPresenter(DiscussGroupMemberListContract.Presenter presenter) {
		this.mPresenter = presenter;
	}

	@Override
	public void removeDiscussMemberStart() {
		LoadingDialog.show(this,"");
	}

	@Override
	public void removeDiscussMemberSuccess(int position,String message) {
		LoadingDialog.close();
		showToast(message);
		UserBaseVo user = data.get(position);
		data.remove(user);
		showdata.remove(user);
		mPresenter.removeMemberChatMsg(groupName,data,cid,getString(R.string.discuss_group_remove_other,user.getShowName()));
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void removeDiscussMemberError(int errorCode, String errorMsg) {
		LoadingDialog.close();
		showToast(errorMsg);
	}

	@Override
	public void addDiscussMembersStart() {
		LoadingDialog.show(this,"");
	}

	@Override
	public void addDiscussMembersSuccess(String message,ArrayList<UserBaseVo> continueList,String name) {
		LoadingDialog.close();
		showToast(message);
		for (int m = 0; m < continueList.size(); m++) {
			if (isAdmin) {
				data.add(continueList.get(m));
				showdata.add(showdata.size() - 2, continueList.get(m));
			} else {
				data.add(continueList.get(m));
				showdata.add(showdata.size() - 1, continueList.get(m));
			}
		}
		mPresenter.inviteOthersChatMsg(groupName,data,cid,getString(R.string.discuss_group_invite_others,name));
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void addDiscussMembersError(int errorCode, String errorMsg) {
		LoadingDialog.close();
		showToast(errorMsg);
	}
}
