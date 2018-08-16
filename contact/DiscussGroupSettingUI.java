package com.lingtuan.firefly.contact;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
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
import com.lingtuan.firefly.contact.contract.DiscussGroupSettingContract;
import com.lingtuan.firefly.contact.presenter.DiscussGroupSettingPresenterImpl;
import com.lingtuan.firefly.contact.vo.DiscussGroupMemberVo;
import com.lingtuan.firefly.contact.vo.DiscussionGroupsVo;
import com.lingtuan.firefly.custom.GridViewWithHeaderAndFooter;
import com.lingtuan.firefly.custom.GridViewWithHeaderAndFooter.OnTouchBlankPositionListener;
import com.lingtuan.firefly.custom.switchbutton.SwitchButton;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.network.NetRequestImpl;
import com.lingtuan.firefly.quickmark.GroupQuickMarkUI;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MyViewDialogFragment;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.vo.UserBaseVo;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Group chat Settings
 */
public class DiscussGroupSettingUI extends BaseActivity implements GroupMemberImageAdapter.GroupMemberImageListener,DiscussGroupSettingContract.View {

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
	private RelativeLayout mQuickMarkRela;
	private ImageView notifyClock = null ; // by :KNothing
	private boolean hasMove=false;
	private LinearLayout allMember;
	private TextView allNum;
	private List<UserBaseVo> data  = new ArrayList<>();
	private List<UserBaseVo> showdata = new ArrayList<>();

	private DiscussGroupSettingContract.Presenter mPresenter;

	@Override
	protected void setContentView() {
		setContentView(R.layout.discuss_group_setting);
	}

	@Override
	protected void findViewById() {
		grid = findViewById(R.id.discuss_group_settting_grid);
		footerView =  LayoutInflater.from(this).inflate(R.layout.discuss_group_setting_footer, null, false);
		nameEdit = footerView.findViewById(R.id.discuss_group_setting_name);
		eidtNameBg = footerView.findViewById(R.id.discuss_group_setting_edit);
		switchBtn = footerView.findViewById(R.id.discuss_group_setting_switch);
		dissmissBtn = footerView.findViewById(R.id.discuss_group_setting_dissmiss);
		mQuickMarkRela = footerView.findViewById(R.id.discuss_group_setting_quickmark);
		notifyClock = footerView.findViewById(R.id.notifyClock);
		allMember = footerView.findViewById(R.id.discuss_group_all_member);
		allNum = footerView.findViewById(R.id.discuss_group_all_num);

		new DiscussGroupSettingPresenterImpl(this);
	}

	@Override
	protected void setListener() {
		allMember.setOnClickListener(this);
		mQuickMarkRela.setOnClickListener(this);
		eidtNameBg.setOnClickListener(this);
		dissmissBtn.setOnClickListener(this);
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
		mPresenter.getDiscussMembers(cid);
		nameEdit.setText(nickname);
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.discuss_group_all_member:
			DiscussGroupMemberVo.getInstance().data1.clear();
			DiscussGroupMemberVo.getInstance().data1.addAll(data);
			DiscussGroupMemberVo.getInstance().showdata1.clear();
			DiscussGroupMemberVo.getInstance().showdata1.addAll(showdata);
			Intent allMemberIntent = new Intent(this, DiscussGroupMemberListUI.class);
			allMemberIntent.putExtra("cid", cid);
			allMemberIntent.putExtra("isAdmin",isAdmin);
			allMemberIntent.putExtra("name",nameEdit.getText().toString());
			startActivityForResult(allMemberIntent, 100);
			Utils.openNewActivityAnim(this, false);
			break;
		case R.id.discuss_group_setting_quickmark:
			Intent intent = new Intent(this,GroupQuickMarkUI.class);
			intent.putExtra("id", cid + "");
			intent.putExtra("nickname", nameEdit.getText().toString());
			intent.putExtra("avatarurl", avatarurl);
			intent.putExtra("number", data.size());
			intent.putExtra("type", 1);
			startActivity(intent);
			Utils.openNewActivityAnim(this, false);
			break;
		case R.id.discuss_group_setting_edit:
			showRename_Dialog(DiscussGroupSettingUI.this);
			break;
		case R.id.discuss_group_setting_dissmiss:
			mPresenter.quitGroup(cid);
			break;
		}
	}

	private void showRename_Dialog(Context context) {
		MyViewDialogFragment mdf = new MyViewDialogFragment(MyViewDialogFragment.DIALOG_TEXT_ENTRY, getString(R.string.discuss_modify_name), null,  nameEdit.getText().toString());
		mdf.setEditOkCallback(new MyViewDialogFragment.EditOkCallback() {
			@Override
			public void okBtn(String edittext) {
				mPresenter.discussGroupRename(edittext,cid);
			}
		});
		mdf.show(getSupportFragmentManager(), "mdf");
	}

	@Override
	public void removeMember(final int arg2) {
		mPresenter.removeMember(arg2,showdata.get(arg2).getLocalId(),cid);
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
		allNum.setText(getString(R.string.discuss_all,data.size()));
		adapter.notifyDataSetChanged();
	}

	@Override
	public void addMembers() {
		ArrayList<String> alreadySelected = new ArrayList<>();
		for (int i = 0; i < data.size(); i++) {
			UserBaseVo info =data.get(i);
			if(!TextUtils.isEmpty(info.getLocalId())&&!info.getLocalId().equals(NextApplication.myInfo.getLocalId())){
			   alreadySelected.add(info.getLocalId());
			}
		}
		Intent intent  = new Intent(DiscussGroupSettingUI.this,SelectContactUI.class);
		intent.putExtra("cantSelectList", alreadySelected);
		intent.putExtra("isMultipleChoice", true);
		startActivityForResult(intent, 0);
		Utils.openNewActivityAnim(DiscussGroupSettingUI.this, false);
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
			switchBtn.setBackColor(getResources().getColorStateList(R.color.switch_button_gray));
		} else {
			switchBtn.setChecked(true);
			switchBtn.setBackColor(getResources().getColorStateList(R.color.switch_button_green));
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
				if (isChecked){
					switchBtn.setBackColor(getResources().getColorStateList(R.color.switch_button_green));
				}else{
					switchBtn.setBackColor(getResources().getColorStateList(R.color.switch_button_gray));
				}
				mPresenter.switchNotify(switchBtn.isChecked(), cid);
			}
		});
		dissmissBtn.setVisibility(View.VISIBLE);
    }

	protected void onActivityResult(int requestCode, int resultCode, Intent data1) {
		if(requestCode==0&&resultCode==RESULT_OK){//Select the contact to return
			ArrayList<UserBaseVo> selectList = (ArrayList<UserBaseVo>) data1.getSerializableExtra("selectList");
			if(selectList!=null&&!selectList.isEmpty()){
			  mPresenter.addMembersRequest(selectList,cid);
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

	@Override
	public void setPresenter(DiscussGroupSettingContract.Presenter presenter) {
		this.mPresenter = presenter;
	}

	@Override
	public void getDiscussMembersSuccess(JSONObject object) {
		Utils.writeToFile(object,"conversation-get_mumbers"+cid+".json");
		parseJson(object);
		try {
			FinalUserDataBase.getInstance().updateChatEventMask("group-" + cid, TextUtils.equals("1", switchBtn.isChecked()? "1" : "0"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		adapter.setAdmin(isAdmin);
		adapter.notifyDataSetChanged();
	}

	@Override
	public void discussGroupRenameStart() {
		LoadingDialog.show(this,"");
	}

	@Override
	public void discussGroupRenameSuccess(String name, String message) {
		LoadingDialog.close();
		nameEdit.setText(name);
		showToast(message);
		mPresenter.discussGroupRenameMessage(name,data,cid,getString(R.string.discuss_group_rename,name));
	}

	@Override
	public void discussGroupRenameError(int errorCode, String errorMsg) {
		LoadingDialog.close();
		showToast(errorMsg);
	}

	@Override
	public void addDiscussMembersStart() {
		LoadingDialog.show(this,"");
	}

	@Override
	public void addDiscussMembersSuccess(String message, ArrayList<UserBaseVo> continueList, String name) {
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
		mPresenter.inviteOthersChatMsg(nameEdit.getText().toString(),data,cid,getString(R.string.discuss_group_invite_others,name));
		resetlist();
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				adapter.notifyDataSetChanged();
			}
		}, 100);
	}

	@Override
	public void addDiscussMembersError(int errorCode, String errorMsg) {
		LoadingDialog.close();
		showToast(errorMsg);
	}

	@Override
	public void removeMemberStart() {
		LoadingDialog.show(this,"");
	}

	@Override
	public void removeMemberSuccess(int position, String message) {
		LoadingDialog.close();
		showToast(message);
		UserBaseVo user = showdata.get(position);
		data.remove(user);
		showdata.remove(user);
		mPresenter.removeMemberChatMsg(nameEdit.getText().toString(),data,cid,getString(R.string.discuss_group_remove_other,user.getShowName()));
		resetlist();
	}

	@Override
	public void removeMemberError(int errorCode, String errorMsg) {
		LoadingDialog.close();
		showToast(errorMsg);
	}

	@Override
	public void switchNotifySuccess(String message) {
		showToast(message);
		try {
			FinalUserDataBase.getInstance().updateChatEventMask("group-" + cid, TextUtils.equals("1", switchBtn.isChecked()? "1" : "0"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void switchNotifyError(int errorCode, String errorMsg) {
		showToast(errorMsg);
	}

	@Override
	public void quitGroupStart() {
		LoadingDialog.show(this,"");
	}

	@Override
	public void quitGroupSuccess(String message) {
		LoadingDialog.close();
		showToast(message);
		ChattingManager.getInstance(DiscussGroupSettingUI.this).setFinish(true);
		Utils.exitActivityAndBackAnim(DiscussGroupSettingUI.this,true);
		FinalUserDataBase.getInstance().deleteChatMsgByChatId("group-" + cid);
	}

	@Override
	public void quitGroupError(int errorCode, String errorMsg) {
		LoadingDialog.close();
		showToast(errorMsg);
	}
}
