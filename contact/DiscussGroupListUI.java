package com.lingtuan.firefly.contact;

import android.content.Intent;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.contact.adapter.DiscussGroupAdapter;
import com.lingtuan.firefly.contact.contract.DiscussGroupListContract;
import com.lingtuan.firefly.contact.presenter.DiscussGroupListPresenterImpl;
import com.lingtuan.firefly.contact.vo.DiscussionGroupsVo;
import com.lingtuan.firefly.custom.LoadMoreListView;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.vo.UserBaseVo;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnItemClick;

/**
 * Group chat list Need radio transmission key = single, value = true < br >
 * @author caoyuting
 */
public class DiscussGroupListUI extends BaseActivity implements OnRefreshListener, DiscussGroupListContract.View {

	@BindView(R.id.refreshListView)
	LoadMoreListView groupLv = null;
	/** Refresh the controls */
	@BindView(R.id.swipe_container)
	SwipeRefreshLayout swipeLayout;
	@BindView(R.id.app_btn_right)
	TextView rightBtn;
	@BindView(R.id.empty_text)
	TextView emptyTextView;
	@BindView(R.id.empty_like_icon)
	ImageView emptyIcon;
	@BindView(R.id.empty_like_rela)
	RelativeLayout emptyRela;

	private DiscussGroupListContract.Presenter mPresenter;

	private DiscussGroupAdapter adapter = null;
	private List<DiscussionGroupsVo> source;
	private boolean isSingleSelect = false;
	
	@Override
	protected void setContentView() {
		setContentView(R.layout.discuss_group_list);
	}

	@Override
	protected void findViewById() {

	}

	@Override
	protected void setListener() {
		swipeLayout.setOnRefreshListener(this);
	}
	
	@Override
	protected void initData() {

		new DiscussGroupListPresenterImpl(this);

		groupLv.setOnScrollListener(new PauseOnScrollListener(NextApplication.mImageLoader, true, true));
		if(getIntent() != null ){
			isSingleSelect = getIntent().getBooleanExtra("single", false);
		}
		swipeLayout.setColorSchemeResources(R.color.black);
		setTitle(getString(R.string.group_chat));
		rightBtn.setText(getString(R.string.create));
		rightBtn.setVisibility(View.VISIBLE);
		if(isSingleSelect){
			rightBtn.setVisibility(View.GONE);
		}
		source = new ArrayList<>();
		adapter = new DiscussGroupAdapter(DiscussGroupListUI.this, source);
		groupLv.setAdapter(adapter);
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				String response= Utils.readFromFile("conversation-get_conversation"+NextApplication.myInfo.getLocalId()+".json");
				if(!TextUtils.isEmpty(response)){
					try {
						JSONObject json=new JSONObject(response);
						JSONArray array = json.optJSONArray("data");
						parseJson(array);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				adapter.resetSource(source);
				swipeLayout.setRefreshing(true);
				mPresenter.loadGroupList();
			}
		}, 100);

		
	}

	@OnClick(R.id.app_btn_right)
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.app_btn_right:
			Intent intent  = new Intent(DiscussGroupListUI.this,SelectContactUI.class);
			intent.putExtra("isMultipleChoice", true);
			intent.putExtra("isCreateGroup", true);
			startActivityForResult(intent, 0);
			Utils.openNewActivityAnim(DiscussGroupListUI.this, false);
			break;
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}

	
	@Override
	public void onRefresh() {
		mPresenter.loadGroupList();
	}

	@OnItemClick(R.id.refreshListView)
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		DiscussionGroupsVo gVo = source.get(position);
		String uid = "group-" + gVo.getCid();
		String username = gVo.getName();
		StringBuilder sb = new StringBuilder();
		for (UserBaseVo vo : gVo.getMembers()) {
			sb.append(vo.getThumb()).append("___").append(vo.getGender()).append("#");
		}
		sb.deleteCharAt(sb.lastIndexOf("#"));
		String url = sb.toString();
		if(isSingleSelect){
			Intent intent = new Intent();
			intent.putExtra("uid", uid);
			intent.putExtra("username", username);
			intent.putExtra("avatarurl", url);
			intent.putExtra("isgroup", true);
			ArrayList<UserBaseVo> members = new ArrayList<>();
			int max = gVo.getMembers().size()<=5?gVo.getMembers().size():5;
			for(int i=0;i<max;i++){
				members.add(gVo.getMembers().get(i));
			}
			intent.putExtra("member", members);
			setResult(RESULT_OK, intent);
			Utils.exitActivityAndBackAnim(this,true);
			return;
		}
		Utils.intentChattingUI(this, uid, url,username, "1",0,true, false, false, 0,true);
	
	}

	
	/*Parsing json*/
	private void parseJson(JSONArray jsonArray){
		if (jsonArray != null) {
			int count = jsonArray.length();
			for (int i = 0; i < count; i++) {
				DiscussionGroupsVo uInfo = new DiscussionGroupsVo().parse(jsonArray.optJSONObject(i));
				source.add(uInfo);
			}
		}
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode==0&&resultCode==RESULT_OK){//Select the contact to return
			ArrayList<UserBaseVo> selectList = (ArrayList<UserBaseVo>) data.getSerializableExtra("selectList");
			if(selectList!=null&&selectList.size()==1){
				UserBaseVo vo = selectList.get(0);
				Utils.intentChattingUI(DiscussGroupListUI.this, vo.getLocalId(), vo.getThumb(), vo.getShowName(), vo.getGender(),vo.getFriendLog(),false, false, false, 0,true);
			}else if(selectList!=null&&selectList.size()>1){
				StringBuilder touids = new StringBuilder();
				for(UserBaseVo vo :selectList){
					touids.append(vo.getLocalId()).append(",");
				}
				touids.deleteCharAt(touids.lastIndexOf(","));
				mPresenter.createDiscussionGroup(touids.toString(),selectList);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * To test whether the current list is empty
	 */
	private void checkListEmpty() {
		if(source == null || source.size() == 0){
			emptyRela.setVisibility(View.VISIBLE);
			emptyIcon.setImageResource(R.drawable.empty_group);
			emptyTextView.setText(R.string.group_empty);
		}else{
			emptyRela.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

	}

	@Override
	public void setPresenter(DiscussGroupListContract.Presenter presenter) {
		this.mPresenter = presenter;
	}

	@Override
	public void createDiscussionGroupStart() {
		LoadingDialog.show(this,"");
	}

	@Override
	public void createDiscussionGroupSuccess(String message,String cid,List<UserBaseVo> member) {
		LoadingDialog.close();
		showToast(message);
		Utils.gotoGroupChat(DiscussGroupListUI.this,false,null,cid, member);
	}

	@Override
	public void createDiscussionGroupError(int errorCode, String errorMsg) {
		LoadingDialog.close();
		showToast(errorMsg);
	}

	@Override
	public void loadGroupListSuccess(JSONObject response) {
		if (swipeLayout!= null){
			swipeLayout.setRefreshing(false);
		}
		source.clear();
		JSONArray jsonArray = response.optJSONArray("data");
		parseJson(jsonArray);
		adapter.resetSource(source);
		checkListEmpty();
		Utils.writeToFile(response, "conversation-get_conversation"+ NextApplication.myInfo.getLocalId()+".json");
	}

	@Override
	public void loadGroupListError(int errorCode, String errorMsg) {
		swipeLayout.setRefreshing(false);
		showToast(errorMsg);
	}
}
