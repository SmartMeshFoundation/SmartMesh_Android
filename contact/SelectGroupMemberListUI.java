package com.lingtuan.firefly.contact;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Transformation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.chat.AtGroupParser;
import com.lingtuan.firefly.contact.adapter.SelectGroupMemberListAdapter;
import com.lingtuan.firefly.contact.vo.DiscussionGroupsVo;
import com.lingtuan.firefly.custom.contact.PinYin;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;
import com.lingtuan.firefly.vo.UserBaseVo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Select group of member list UI need a group ID cid=?
 */
public class SelectGroupMemberListUI extends BaseActivity implements  OnItemClickListener {
	private ListView mListView;
	private SelectGroupMemberListAdapter mAdapter;
	private RelativeLayout mTitleRela;
	
	private boolean isSearch;
	private SelectGroupMemberListAdapter mSearchAdapter;
	private ListView mSearchListView;
	private List<UserBaseVo> friendFilterList=new ArrayList<>();
	private LinearLayout searchResultLayout;
	private FrameLayout searchListLayout;
	private EditText mInputSearch;
	private LinearLayout mInputSearchLayout;
	private TextView mInputSearchCancel;
	private TextView mInTextView;
	private TextView mEmpty;
	private Object searchLock = new Object();
	private SearchListTask curSearchTask = null;
	
	@Override
	protected void setContentView() {
		setContentView(R.layout.layout_select_group_member_list);
	}

	@Override
	protected void findViewById() {
		mTitleRela = (RelativeLayout) findViewById(R.id.app_title_linear);
		mListView = (ListView) findViewById(R.id.listview);
		View view = View.inflate(this, R.layout.include_search_bar, null);
		mInTextView=((TextView)view.findViewById(R.id.include_friends_search_text));
		mListView.addHeaderView(view);
		initSearchData(view);
	}

	@Override
	protected void setListener() {
		mListView.setOnItemClickListener(this);
		
		mInputSearch.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {

			}
			
			@Override
			public synchronized void afterTextChanged(Editable s) {
				if(s != null){
					friendFilterList.clear();
					if(s.toString().length()<=0){
						searchListLayout.setVisibility(View.GONE);
						searchResultLayout.setOnClickListener(SelectGroupMemberListUI.this);
					}else{
						searchListLayout.setVisibility(View.VISIBLE);
						searchResultLayout.setOnClickListener(null);
						String searchString = s.toString().toUpperCase();
						
						if (curSearchTask != null && curSearchTask.getStatus() != AsyncTask.Status.FINISHED){
							try{
								curSearchTask.cancel(true);
							} catch (Exception e){
								e.printStackTrace();
							}
						}
						curSearchTask = new SearchListTask();
						curSearchTask.execute(searchString); 
					}
				}else{
					searchListLayout.setVisibility(View.GONE);
					searchResultLayout.setOnClickListener(SelectGroupMemberListUI.this);
				}
				
			}
		});
		
	}

	@Override
	protected void initData() {
		setTitle(R.string.group_member_select_list);
		
		String response = Utils.readFromFile("conversation-get_conversation" + NextApplication.myInfo.getLocalId() + ".json");

		mAdapter = new SelectGroupMemberListAdapter(null, this);
		mSearchAdapter = new SelectGroupMemberListAdapter(null, this);
		if(!TextUtils.isEmpty(response)){
			try {
				JSONObject json = new JSONObject(response);
				parserJson(json);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		mInTextView.setHint(R.string.search);
		mListView.setAdapter(mAdapter);
		loadData();
	}

	private void loadData() {
		int cid = getIntent().getIntExtra("cid", 0);
		NetRequestImpl.getInstance().getDiscussMumbers(cid+"", new RequestListener() {
			@Override
			public void start() {

			}

			@Override
			public void success(JSONObject response) {
				Utils.writeToFile(response, "conversation-get_conversation"+NextApplication.myInfo.getLocalId()+".json");
				parserJson(response);
			}

			@Override
			public void error(int errorCode, String errorMsg) {

			}
		});
	}
	
	@Override
	public void onClick(View v) {
		super.onClick(v);
		
		switch (v.getId()) {
		
		case R.id.contact_search_bg:
			
			hideSearch();
			
			break;
			
		}
	}

	private void parserJson(JSONObject response) {
		JSONObject jsonObject = response.optJSONObject("data");
		DiscussionGroupsVo vo = new DiscussionGroupsVo().parse(jsonObject,true);
		if(vo != null){
			mAdapter.updateList(vo.getMembers());
			//Add a group of members The nickname and the uid data generated for @ function
			String[] usernames = new String[vo.getMembers().size()];
			String[] ids = new String[vo.getMembers().size()];
			for (int i = 0; i < vo.getMembers().size(); i++) {
				UserBaseVo uvo = vo.getMembers().get(i);
				usernames[i] = uvo.getUserName() + " ";
				ids[i] = uvo.getLocalId();
			}
			AtGroupParser.init(usernames,ids);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
							long id) {
		if(position != 0){
			UserBaseVo vo = mAdapter.getItem(position - 1);
			Intent data = new Intent();
			data.putExtra("data", vo);
			setResult(Activity.RESULT_OK, data);
			Utils.exitActivityAndBackAnim(this, true);
		}else{
			//TODO Search...
			if(!isSearch){
				showSearch();
			}else{
				hideSearch();
			}
		}
	}
	
	@Override
	public void onBackPressed() {
		if(mInputSearchLayout.getVisibility() == View.VISIBLE){
			hideSearch();
			return;
		}
		super.onBackPressed();
	}
	
	private void showSearch(){
		isSearch = true;
		final int initialHeight = mTitleRela.getHeight();
		Animation anim = new Animation() {
			@Override
			protected void applyTransformation(float interpolatedTime,
					Transformation t) {
				if (interpolatedTime == 1) {

				} else {
					RelativeLayout.LayoutParams lp=(LayoutParams) mTitleRela.getLayoutParams();
					lp.setMargins(0, - (int) (initialHeight * interpolatedTime), 0, 0);
					mTitleRela.requestLayout();
				}
			}

			@Override
			public boolean willChangeBounds() {
				return true;
			}

		};

		anim.setDuration(300);
        anim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				RelativeLayout.LayoutParams lp=(LayoutParams) mTitleRela.getLayoutParams();
				lp.setMargins(0, -initialHeight, 0, 0);
				mTitleRela.requestLayout();

				searchResultLayout.setVisibility(View.VISIBLE);
				mTitleRela.setVisibility(View.GONE);
				mInputSearchLayout.setVisibility(View.VISIBLE);
				mInTextView.setVisibility(View.INVISIBLE);
				mInputSearch.setFocusable(true);
				mInputSearch.setFocusableInTouchMode(true);
				mInputSearch.requestFocus();
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(mInputSearch, InputMethodManager.SHOW_FORCED);
			}
		});
        mTitleRela.startAnimation(anim);
        mSearchAdapter = new SelectGroupMemberListAdapter(friendFilterList, this);
		mSearchListView.setAdapter(mSearchAdapter);
	}

	private void hideSearch(){

		mInputSearch.setText("");

		isSearch = false;

		mTitleRela.setVisibility(View.VISIBLE);
		mInputSearchLayout.setVisibility(View.GONE);
		searchResultLayout.setVisibility(View.GONE);
		mInTextView.setVisibility(View.VISIBLE);
		final int initialHeight = mTitleRela.getHeight();
		Animation anim = new Animation() {
			@Override
			protected void applyTransformation(float interpolatedTime,
					Transformation t) {
				if (interpolatedTime == 1) {

				} else {
					RelativeLayout.LayoutParams lp=(LayoutParams) mTitleRela.getLayoutParams();
					lp.setMargins(0, -initialHeight+(int) (initialHeight * interpolatedTime), 0, 0);
					mTitleRela.requestLayout();
				}
			}

			@Override
			public boolean willChangeBounds() {
				return false;
			}

		};
		anim.setDuration(300);
		anim.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				RelativeLayout.LayoutParams lp=(LayoutParams) mTitleRela.getLayoutParams();
				lp.setMargins(0, 0, 0, 0);
				mTitleRela.requestLayout();
				InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mInputSearch.getWindowToken(),0);
			}
		});
		mTitleRela.startAnimation(anim);	
	}
	private void initSearchData(View headerView){
		searchResultLayout=(LinearLayout)findViewById(R.id.contact_search_bg);
		searchResultLayout.setOnClickListener(this);
		
		searchListLayout=(FrameLayout)findViewById(R.id.contact_search_list_bg);
		
		mSearchListView = (ListView) findViewById(R.id.contact_search_lv);
		
		mSearchListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				UserBaseVo vo = mSearchAdapter.getItem(position);
				Intent data = new Intent();
				data.putExtra("data", vo);
				setResult(Activity.RESULT_OK, data);
				Utils.exitActivityAndBackAnim(SelectGroupMemberListUI.this, true);
				InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mInputSearch.getWindowToken(),0);
			}
		});

		mEmpty = (TextView) findViewById(R.id.empty);
		mSearchListView.setEmptyView(mEmpty);
		
		mInputSearch = (EditText) findViewById(R.id.include_friends_search_content);
		mInputSearchLayout=(LinearLayout) findViewById(R.id.include_friends_search_content_bg);
		mInputSearchCancel=(TextView) findViewById(R.id.include_friends_search_cancel);
		
		mInputSearch.setHint(R.string.search);
		mInputSearch.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {

			}
			
			@Override
			public synchronized void afterTextChanged(Editable s) {
				if(s != null){
					friendFilterList.clear();
					if(s.toString().length()<=0){
						searchListLayout.setVisibility(View.GONE);
					}else{
						searchListLayout.setVisibility(View.VISIBLE);
						searchResultLayout.setOnClickListener(null);
						String searchString = s.toString().toUpperCase();
						if (curSearchTask != null && curSearchTask.getStatus() != AsyncTask.Status.FINISHED){
							try{
								curSearchTask.cancel(true);
							} catch (Exception e){
								e.printStackTrace();
							}
						}
						curSearchTask = new SearchListTask();
						curSearchTask.execute(searchString); 
					}
				}else{
					searchListLayout.setVisibility(View.GONE);
				}
			}
		});
		mInputSearchCancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				hideSearch();
			}
		});
	}

	private class SearchListTask extends AsyncTask<String, Void, String> {
		
		List<UserBaseVo> filterList=new ArrayList<>();
		@Override
		protected String doInBackground(String... params){
			String keyword = params[0];
			for (int i=0;i<mAdapter.getList().size();i++){
				UserBaseVo vo = mAdapter.getList().get(i);
				boolean isPinyin = PinYin.getPinYin(vo.getShowName()).toUpperCase().contains(keyword);
				boolean isChinese = vo.getShowName().contains(keyword);
				if (isPinyin || isChinese){
					filterList.add(vo);
				}
			}
			return null;
		}
		
		protected void onPostExecute(String result){
			synchronized (searchLock){
				friendFilterList.clear();
				friendFilterList.addAll(filterList);
				mSearchAdapter.updateList(friendFilterList);
			}
		}
	}

}
