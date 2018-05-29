package com.lingtuan.firefly.contact;

import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.contact.adapter.ContactSearchNickAdapter;
import com.lingtuan.firefly.custom.LoadMoreListView;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.setting.BlackListAdapter;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;
import com.lingtuan.firefly.vo.UserBaseVo;
import com.lingtuan.firefly.vo.UserInfoVo;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ Search nickname page
 */
public class ContactSearchNickUI extends BaseActivity implements OnItemClickListener, OnRefreshListener, LoadMoreListView.RefreshListener {

	private ContactSearchNickAdapter mAdapter;
	

	private LoadMoreListView mListView;
	/** Refresh the controls */
	private SwipeRefreshLayout swipeLayout;
	
	private List<UserBaseVo> friendList;
	
	private EditText mInputSearch;
	
	private TextView emptyTextView;
	private ImageView emptyIcon;
	private RelativeLayout emptyRela;

	private String searchEdit;//Data from the address book


	private boolean isLoadingData = false;
	private int currentPage = 1 ;
	private int oldPage=1;

	@Override
	protected void setContentView() {
		setContentView(R.layout.search_nick_layout);
		getPassData();
	}

	private void getPassData(){
		searchEdit = getIntent().getStringExtra("searchEdit");
	}

	@Override
	protected void findViewById() {
		
		mListView = (LoadMoreListView) findViewById(R.id.refreshListView);
		swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
		
		mInputSearch = (EditText) findViewById(R.id.include_friends_search_content);
		
		emptyRela = (RelativeLayout) findViewById(R.id.empty_like_rela);
		emptyIcon = (ImageView) findViewById(R.id.empty_like_icon);
		emptyTextView = (TextView) findViewById(R.id.empty_text);
		
	}

	@Override
	protected void setListener() {
		mListView.setOnItemClickListener(this);
		mListView.setOnRefreshListener(this);
		mListView.setOnScrollListener(new PauseOnScrollListener(NextApplication.mImageLoader, true, true));
		swipeLayout.setOnRefreshListener(this);
		
		mInputSearch.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

				Utils.hiddenKeyBoard(ContactSearchNickUI.this);
				
				if ((actionId == 0 || actionId == 3) && event != null) {
					emptyRela.setVisibility(View.GONE);
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							swipeLayout.setRefreshing(true);
							searchNick(1,mInputSearch.getText().toString());
						}
					}, 500);
						
					}
				return false;
			}
		});
	}

	@Override
	protected void initData() {
		setTitle(getString(R.string.contact_add_contact));
		friendList = new ArrayList<>();
		mAdapter = new ContactSearchNickAdapter(friendList, this);
		mListView.setAdapter(mAdapter);
		swipeLayout.setColorSchemeResources(R.color.black);
		if (!TextUtils.isEmpty(searchEdit)){
			swipeLayout.setRefreshing(true);
			mInputSearch.setText(searchEdit);
			mInputSearch.setSelection(searchEdit.length());
			searchNick(1,searchEdit);
		}
	}
	private synchronized void searchNick(int page,final String text){
		if(isLoadingData){
			return;
		}
		isLoadingData=true;
		oldPage= page;
		NetRequestImpl.getInstance().friendSearch(page,text, new RequestListener() {
			@Override
			public void start() {

			}
			@Override
			public void success(JSONObject response) {
				currentPage = oldPage;
				if (currentPage == 1) {
					friendList.clear();
				}

				String data = response.optString("data");
				boolean flag = TextUtils.equals("[]", data);
				JSONArray jsonArray = response.optJSONArray("data");
				if (jsonArray != null && !flag) {
					int count = jsonArray.length();
					for (int i = 0; i < count; i++) {
						UserInfoVo uInfo = new UserInfoVo().parse(jsonArray.optJSONObject(i));
						if(uInfo != null){
							friendList.add(uInfo);
						}
					}
					mAdapter.updateList(friendList);
					checkListEmpty(false);
				}else{
					checkListEmpty(true);
				}
				isLoadingData=false;
				swipeLayout.setRefreshing(false);
				if (jsonArray!=null&&jsonArray.length()>=10) {
					mListView.resetFooterState(true);
				} else {
					mListView.resetFooterState(false);
				}
			}

			@Override
			public void error(int errorCode, String errorMsg) {
				isLoadingData=false;
				swipeLayout.setRefreshing(false);
				showToast(errorMsg);
				checkListEmpty(true);
			}
		});
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
		UserBaseVo baseVo = mAdapter.getItem(position);
		Utils.intentFriendUserInfo(this, baseVo , false);
		
	}
	
	@Override
	public void onBackPressed() {
		Utils.exitActivityAndBackAnim(this,true);
		super.onBackPressed();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			Utils.exitActivityAndBackAnim(this,true);
		}
		return super.onKeyDown(keyCode, event);
	}


	@Override
	public void onRefresh() {
		searchNick(1,mInputSearch.getText().toString());
	}
	/**
	 * To test whether the current list is empty
	 */
	private void checkListEmpty(boolean isEmpty) {
		if(isEmpty){
			emptyIcon.setImageResource(R.drawable.empty_search);
			emptyTextView.setText(R.string.hint_empty_search);
			emptyRela.setVisibility(View.VISIBLE);
		}else{
			emptyRela.setVisibility(View.GONE);
		}
	}


	@Override
	public void loadMore() {
		searchNick(currentPage + 1,mInputSearch.getText().toString());
	}
}
