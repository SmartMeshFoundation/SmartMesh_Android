package com.lingtuan.firefly.contact;

import android.app.Dialog;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.contact.adapter.DiscussGroupAdapter;
import com.lingtuan.firefly.contact.vo.DiscussionGroupsVo;
import com.lingtuan.firefly.custom.LoadMoreListView;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;
import com.lingtuan.firefly.vo.UserBaseVo;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Group chat list Need radio transmission key = single, value = true < br >
 * @author caoyuting
 */
public class DiscussGroupListUI extends BaseActivity implements OnItemClickListener, OnRefreshListener {

	private ListView groupLv = null;
	/** Refresh the controls */
	private SwipeRefreshLayout swipeLayout;
	
	private TextView rightBtn = null;
	private DiscussGroupAdapter adapter = null;
	private List<DiscussionGroupsVo> source;

	private TextView emptyTextView;
	private ImageView emptyIcon;
	private RelativeLayout emptyRela;
	
	private Dialog mProgressDialog;
	
	private boolean isSingleSelect = false;
	
	@Override
	protected void setContentView() {
		setContentView(R.layout.discuss_group_list);
	}

	@Override
	protected void findViewById() {
		rightBtn = (TextView) findViewById(R.id.app_btn_right);
		groupLv = (LoadMoreListView) findViewById(R.id.refreshListView);
		groupLv.setOnScrollListener(new PauseOnScrollListener(NextApplication.mImageLoader, true, true));
		swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);

		emptyRela = (RelativeLayout) findViewById(R.id.empty_like_rela);
		emptyIcon = (ImageView) findViewById(R.id.empty_like_icon);
		emptyTextView = (TextView) findViewById(R.id.empty_text);
		
	}

	@Override
	protected void setListener() {
		swipeLayout.setOnRefreshListener(this);
		rightBtn.setOnClickListener(this);
		groupLv.setOnItemClickListener(this);
	}
	
	@Override
	protected void initData() {
		
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
		adapter = new DiscussGroupAdapter(this, source);
		groupLv.setAdapter(adapter);
		
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				swipeLayout.setRefreshing(true);
				loadGroupList();
			}
		}, 500);

		
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.app_btn_right:
			Intent intent  = new Intent(DiscussGroupListUI.this,SelectContactUI.class);
			intent.putExtra("isMultipleChoice", true);
			startActivityForResult(intent, 0);
			Utils.openNewActivityAnim(DiscussGroupListUI.this, false);
			break;
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	

	/**
	 * Create a group chat
	 * @param touids members id
	 */
	private void createDiscussionGroups(String touids, final List<UserBaseVo> member){
		NetRequestImpl.getInstance().createDiscussionGroups(touids, new RequestListener() {
			@Override
			public void start() {
				showProgressDialog();
			}

			@Override
			public void success(JSONObject response) {
				showToast(response.optString("msg"));
				dismissProgressDialog();
				Utils.gotoGroupChat(DiscussGroupListUI.this,false,null,response.optString("cid"), member);
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						swipeLayout.setRefreshing(true);
						loadGroupList();
					}
				}, 500);
			}

			@Override
			public void error(int errorCode, String errorMsg) {
				dismissProgressDialog();
				showToast(errorMsg);
			}
		});
	}
	
	private void showProgressDialog(){
		dismissProgressDialog();
		mProgressDialog = LoadingDialog.showDialog(this, null,null);
		mProgressDialog.setCancelable(false);

	}
	
	private void dismissProgressDialog(){
		if(mProgressDialog != null){
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}
	
    private void loadGroupList() {
		NetRequestImpl.getInstance().loadGroupList(new RequestListener() {
			@Override
			public void start() {

			}

			@Override
			public void success(JSONObject response) {
				Utils.writeToFile(response, "conversation-get_conversation"+NextApplication.myInfo.getLocalId()+".json");
				source.clear();
				JSONArray jsonArray = response.optJSONArray("data");
				parseJson(jsonArray);
				adapter.resetSource(source);
				swipeLayout.setRefreshing(false);
				checkListEmpty();
			}

			@Override
			public void error(int errorCode, String errorMsg) {
				swipeLayout.setRefreshing(false);
				showToast(errorMsg);
			}
		});
    }

	
	@Override
	public void onRefresh() {
		loadGroupList();
	}
	@Override
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
			for(int i=0;i<max;i++)
			{
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
				createDiscussionGroups(touids.toString(),selectList);
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
}
