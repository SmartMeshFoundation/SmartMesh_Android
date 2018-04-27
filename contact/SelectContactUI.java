package com.lingtuan.firefly.contact;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
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
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.contact.adapter.ContactSearchAdapter;
import com.lingtuan.firefly.contact.adapter.NewContactListAdapter;
import com.lingtuan.firefly.contact.vo.NewContactVO;
import com.lingtuan.firefly.custom.CharAvatarView;
import com.lingtuan.firefly.custom.contact.ContactItemComparator;
import com.lingtuan.firefly.custom.contact.ContactItemInterface;
import com.lingtuan.firefly.custom.contact.PinYin;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.vo.UserBaseVo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*Select the contact page, including share the contacts, groups*/
public class SelectContactUI extends BaseActivity implements OnItemClickListener, android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener{

	private TextView mTitle;
	private RelativeLayout mTitleRela;


	private List<UserBaseVo> mFriendInfoList = new ArrayList<>();
	private ListView mNewListView;

	private List<ContactItemInterface> mContactList=new ArrayList<>();

	private ImageView emptyImg;
	private TextView emptyText;

    /*
     * Search for related
    */
    private ContactSearchAdapter mSearchAdapter;
	private ListView mSearchListView;
	private List<UserBaseVo> friendFilterList=new ArrayList<>();
	private LinearLayout searchResultLayout;
	private FrameLayout searchListLayout;
	private EditText mInputSearch;
	private LinearLayout mInputSearchLayout;
	private TextView mInputSearchCancel;
	private TextView mInTextView;
	private LinearLayout mEmpty;
	private int initialHeight;
	private boolean isSearching=false;


	private Object searchLock = new Object();
	private SearchListTask curSearchTask = null;


    private SwipeRefreshLayout swipeLayout;

    private TextView contactNum;

    private NewContactListAdapter mNewContactListAdapter;
	private boolean isMultipleChoice=false;//A multiple-choice or radio
	private ArrayList<UserBaseVo> selectList = new ArrayList<>() ;  //The selected user list
	private ArrayList<String> cantSelectList;//Don't appear the list of users, that is, can not choose the list of users, not to

	private ArrayList<UserBaseVo> hasSelectList;//The selected user list already

	private LinearLayout mSelectContactBg;
	private HorizontalScrollView mHorizontalScrollView;
	private LinearLayout mHorizontalScrollViewContent;
	private TextView mFinishBtn;


	@Override
	protected void setContentView() {
		setContentView(R.layout.main_contact_layout);
	}

	@Override
	protected void findViewById() {
		if(getIntent()!= null){
	    	cantSelectList = (ArrayList<String>) getIntent().getSerializableExtra("cantSelectList");
	    	hasSelectList = (ArrayList<UserBaseVo>) getIntent().getSerializableExtra("hasSelectList");
	    	isMultipleChoice= getIntent().getBooleanExtra("isMultipleChoice", false);
		}

		mTitle = (TextView) findViewById(R.id.app_title);
		mTitleRela = (RelativeLayout) findViewById(R.id.app_title_rela);
		mNewListView = (ListView) findViewById(R.id.contact_list);
		mSelectContactBg = (LinearLayout) findViewById(R.id.contact_select_bg);
		mHorizontalScrollView = (HorizontalScrollView) findViewById(R.id.contact_horizontalscrollview);
		mHorizontalScrollViewContent  = (LinearLayout) findViewById(R.id.contact_horizontalscrollview_content);
		mFinishBtn = (TextView) findViewById(R.id.contact_select_finish);

		if(isMultipleChoice)
		{
			mSelectContactBg.setVisibility(View.VISIBLE);
		}
		if(hasSelectList!=null)
		{
			mFinishBtn.setEnabled(true);
		}
		swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);

	    swipeLayout.setColorSchemeResources(R.color.black);

	    View headerView0 = View.inflate(this, R.layout.include_friends_header_search, null);

	    initSearchData(headerView0);
	    mNewListView.addHeaderView(headerView0);

		View footerView = View.inflate(this, R.layout.include_friends_footer, null);

		contactNum=(TextView) footerView.findViewById(R.id.include_contact_num);
		emptyImg=(ImageView) footerView.findViewById(R.id.empty_like_icon);
		emptyText=(TextView) footerView.findViewById(R.id.empty_like_text);
		mNewListView.addFooterView(footerView);
		contactNum.setText(getString(R.string.contact_num, 0));


		mNewContactListAdapter = new NewContactListAdapter(this, R.layout.contact_child_item, mContactList,isMultipleChoice);
		mNewListView.setAdapter(mNewContactListAdapter);

	}

	@Override
	protected void setListener() {
		swipeLayout.setOnRefreshListener(this);
		mNewListView.setOnItemClickListener(this);
		mFinishBtn.setOnClickListener(this);
	}

	@Override
	protected void initData() {
		mTitle.setText(getString(R.string.friend_invite_people));
		new LoadDatabasesThread().start();
	}

	/**
	 *  Initializes the search related
	 */
	private void initSearchData(View headerView){
        searchResultLayout=(LinearLayout)findViewById(R.id.contact_search_bg);
		searchResultLayout.setOnClickListener(this);

		searchListLayout=(FrameLayout)findViewById(R.id.contact_search_list_bg);

		mSearchListView = (ListView) findViewById(R.id.contact_search_lv);

		mSearchListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
				UserBaseVo baseVo = mSearchAdapter.getItem(arg2);
			    if(isMultipleChoice) {//multi-select
			    	if(baseVo.isCantChecked()){
			    		return;
			    	}
			    	baseVo.setChecked(!baseVo.isChecked());
			    	mSearchAdapter.notifyDataSetChanged();
			    	mNewContactListAdapter.notifyDataSetChanged();
			    	if(baseVo.isChecked()){
			    		 final CharAvatarView imageView=new CharAvatarView(SelectContactUI.this);
			    		 imageView.setTag(baseVo.getLocalId());
			    		 imageView.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								clickSelectVo(imageView);
							}
						 });
			 			 LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(Utils.dip2px(SelectContactUI.this,40), Utils.dip2px(SelectContactUI.this, 40));
			 			 lp.setMargins(Utils.dip2px(SelectContactUI.this, 10), Utils.dip2px(SelectContactUI.this, 5), 0, 0);
			 			 mHorizontalScrollViewContent.addView(imageView,lp);
						 imageView.setText(baseVo.getUsername(),imageView,baseVo.getThumb());
			             selectList.add(baseVo);
			             new Handler().postDelayed(new Runnable(){
			                 public void run() {
			                	 mHorizontalScrollView.smoothScrollTo(mHorizontalScrollViewContent.getChildCount()*Utils.dip2px(SelectContactUI.this,50), 0);
			                 }
						 }, 0);

			             if(hasSelectList==null&&!mFinishBtn.isEnabled()){
			            	 mFinishBtn.setEnabled(true);
			             }
			             if(hasSelectList!=null) {//Over the selected user data
			            	 hasSelectList.add(baseVo);
				         }
			        }else{
			        	int count  = mHorizontalScrollViewContent.getChildCount() ;
			        	 for(int i=0;i<count ;i++){
			        		 View imageView=mHorizontalScrollViewContent.getChildAt(i);
			        		 if(baseVo.getLocalId().equals(imageView.getTag())){
			        			 mHorizontalScrollViewContent.removeViewAt(i);
			        			 break;
			        		 }
			        	 }
						for(int i=0;i<selectList.size();i++){
							if(selectList.get(i).getLocalId().equals(baseVo.getLocalId())){
								selectList.remove(i);
								break;
							}
						}
			        	 if(hasSelectList==null&&selectList.size()<=0){
			        		 if(mFinishBtn.isEnabled()){
				            	 mFinishBtn.setEnabled(false);
				             }
			        	 }

			        	 if(hasSelectList!=null) {//Over the selected user data
			        		 int hasSelectCount = hasSelectList.size();
				        	 for(int i=0;i<hasSelectCount;i++){
				        		 UserBaseVo vo=hasSelectList.get(i);
				        		 if(vo.getLocalId().equals(baseVo.getLocalId())){
				        			 hasSelectList.remove(i);
				        			 break;
				        		 }
				        	 }
				         }
			        }
			    }else{
				  InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
				  imm.hideSoftInputFromWindow(mInputSearch.getWindowToken(),0);
				  clickSingleItem(baseVo);
			    }
		   }
		});



	    mEmpty = (LinearLayout) findViewById(R.id.empty);
		mSearchListView.setEmptyView(mEmpty);



		mInTextView=(TextView) headerView.findViewById(R.id.include_friends_search_text);



		mInputSearch = (EditText)findViewById(R.id.include_friends_search_content);
		mInputSearchLayout=(LinearLayout) findViewById(R.id.include_friends_search_content_bg);
		mInputSearchCancel=(TextView) findViewById(R.id.include_friends_search_cancel);


        mInputSearch.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {


			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
										  int after) {


			}

			@Override
			public synchronized void afterTextChanged(Editable s) {

				if(s != null){

					friendFilterList.clear();
					if(s.toString().length()<=0)
					{
						searchListLayout.setVisibility(View.GONE);
						searchResultLayout.setOnClickListener(SelectContactUI.this);
					}
					else{
						searchListLayout.setVisibility(View.VISIBLE);
						searchResultLayout.setOnClickListener(null);
						String searchString = s.toString().toUpperCase();

						if (curSearchTask != null
								&& curSearchTask.getStatus() != AsyncTask.Status.FINISHED)
						{
							try
							{
								curSearchTask.cancel(true);
							} catch (Exception e)
							{

							}

						}
						curSearchTask = new SearchListTask();
						curSearchTask.execute(searchString);
					}
				}
				else{
					searchListLayout.setVisibility(View.GONE);
					searchResultLayout.setOnClickListener(SelectContactUI.this);
				}

			}
		});
        mInputSearchCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				hideSearch();
			}
		});


	}


	private class SearchListTask extends AsyncTask<String, Void, String>{
		List<UserBaseVo> filterList=new ArrayList<>();
		@Override
		protected String doInBackground(String... params){
			String keyword = params[0];
			for (int i=0;i<mContactList.size();i++){
				NewContactVO contact=(NewContactVO) mContactList.get(i);
				boolean isPinyin = PinYin.getPinYin(contact.getShowName()).toUpperCase().contains(keyword);
				boolean isChinese = contact.getShowName().contains(keyword);
				if (isPinyin || isChinese){
					filterList.add(contact);
				}
			}
			return null;
		}

		protected void onPostExecute(String result){
			synchronized (searchLock){
				friendFilterList.clear();
				friendFilterList.addAll(filterList);
				mSearchAdapter.notifyDataSetChanged();
			}
		}
	}
	private void hideSearch(){
		mInputSearch.setText("");
		isSearching=false;
		searchResultLayout.setVisibility(View.GONE);
		mTitleRela.setVisibility(View.VISIBLE);
		mInputSearchLayout.setVisibility(View.GONE);
		mInTextView.setVisibility(View.VISIBLE);
		Animation anim = new Animation() {
			@Override
			protected void applyTransformation(float interpolatedTime,Transformation t) {
				if (interpolatedTime != 1) {
					LinearLayout.LayoutParams lp=(android.widget.LinearLayout.LayoutParams) mTitleRela.getLayoutParams();
					lp.setMargins(0, -initialHeight+(int) (initialHeight * interpolatedTime), 0, 0);
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
				LinearLayout.LayoutParams lp=(android.widget.LinearLayout.LayoutParams) mTitleRela.getLayoutParams();
				lp.setMargins(0, 0, 0, 0);
				mTitleRela.requestLayout();
				InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mInputSearch.getWindowToken(),0);
			}
		});
		mTitleRela.startAnimation(anim);
	}
	private void showSearch(){
		if(!isSearching){
			isSearching=true;
			searchListLayout.setVisibility(View.GONE);
			initialHeight = mTitleRela.getMeasuredHeight();
			Animation anim = new Animation() {
				@Override
				protected void applyTransformation(float interpolatedTime,Transformation t) {
					if (interpolatedTime != 1) {
						LinearLayout.LayoutParams lp=(android.widget.LinearLayout.LayoutParams) mTitleRela.getLayoutParams();
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
					LinearLayout.LayoutParams lp=(android.widget.LinearLayout.LayoutParams) mTitleRela.getLayoutParams();
					lp.setMargins(0, -initialHeight, 0, 0);
					mTitleRela.requestLayout();

					searchResultLayout.setVisibility(View.VISIBLE);
					mTitleRela.setVisibility(View.GONE);
					mInTextView.setVisibility(View.INVISIBLE);
					mInputSearchLayout.setVisibility(View.VISIBLE);
					mInputSearch.setFocusable(true);
					mInputSearch.setFocusableInTouchMode(true);
					mInputSearch.requestFocus();
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.showSoftInput(mInputSearch, InputMethodManager.SHOW_FORCED);
				}
			});
            mTitleRela.startAnimation(anim);

			mSearchAdapter = new ContactSearchAdapter(friendFilterList, this,isMultipleChoice);
			mSearchListView.setAdapter(mSearchAdapter);

		}
	}

	class LoadDatabasesThread extends Thread {

		private Handler handler = new Handler(){
			public void handleMessage(android.os.Message msg) {
				mFriendInfoList.clear();
				mFriendInfoList.addAll((List<UserBaseVo>) msg.obj);
				mContactList.clear();
				for(UserBaseVo voT : mFriendInfoList){
					boolean cantSelect=false;
					if(NextApplication.myInfo.getLocalId().equals(voT.getLocalId())){//Eliminate the user cannot choose
						cantSelect=true;
					}
					else{
						if(cantSelectList!=null&&!cantSelectList.isEmpty()){
							for(String uid :cantSelectList){
								String uidT = voT.getLocalId();
								if(uid.equals(uidT)){//Eliminate the user cannot choose
									cantSelect=true;
									break;
								}
							}
						}
					}
					boolean hasSelect=false;
					if(hasSelectList!=null&&!hasSelectList.isEmpty())
					{
						for(UserBaseVo vo :hasSelectList)
						{
							String uidT = voT.getLocalId();
							if(vo.getLocalId().equals(uidT))
							{
								hasSelect=true;
								break;
							}
						}
					}

					NewContactVO info=new NewContactVO();
					if(cantSelect){//cantSelect
						info.setCantChecked(true);
					}
					if(hasSelect){//hasSelect
						info.setChecked(true);
						final CharAvatarView imageView=new CharAvatarView(SelectContactUI.this);
						imageView.setTag(voT.getLocalId());
						imageView.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								clickSelectVo(imageView);
							}
						 });
						LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(Utils.dip2px(SelectContactUI.this,40), Utils.dip2px(SelectContactUI.this, 40));
						lp.setMargins(Utils.dip2px(SelectContactUI.this, 10), Utils.dip2px(SelectContactUI.this, 5), 0 , 0);
						mHorizontalScrollViewContent.addView(imageView,lp);
						imageView.setText(voT.getUsername(),imageView,voT.getThumb());
					}
					info.setAge(voT.getAge());
					info.setDistance(voT.getDistance());
					info.setFriendLog(voT.getFriendLog());
					info.setGender(voT.getGender());
					info.setSightml(voT.getSightml());
					info.setThumb(voT.getThumb());
					info.setLocalId(voT.getLocalId());
					info.setUsername(voT.getUserName());
					info.setNote(voT.getNote());
					info.setLogintime(voT.getLogintime());
					info.setAddress(voT.getAddress());
					info.setFullName(PinYin.getPinYin(voT.getShowName()));
					mContactList.add(info);

				}
				new Handler().postDelayed(new Runnable(){
					 public void run() {
						mHorizontalScrollView.smoothScrollTo(mHorizontalScrollViewContent.getChildCount()*Utils.dip2px(SelectContactUI.this,50), 0);
					 }
			        }, 0);
					mNewListView.setFastScrollEnabled(true);
					Collections.sort(mContactList, new ContactItemComparator());
					mNewContactListAdapter.updateList(mContactList);
					contactNum.setText(getString(R.string.contact_num, mContactList.size()));
					checkListEmpty();
			}
		};

		@Override
		public void run() {
			try {
				List<UserBaseVo> mList = FinalUserDataBase.getInstance().getFriendUserBaseAll();
				Message msg = new Message();
				msg.obj = mList;
				handler.sendMessage(msg);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
			case R.id.contact_select_finish://finish
				InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mInputSearch.getWindowToken(),0);
				Intent i=new Intent();
				if(hasSelectList!=null){
				  i.putExtra("selectList", hasSelectList);
				}else{
				  i.putExtra("selectList", selectList);
				}
				setResult(RESULT_OK,i);
				Utils.exitActivityAndBackAnim(this,true);
				break;
			case R.id.contact_search_bg:
			   hideSearch();
			   break;
		}

	}

	/*Click on the navigation bar at the bottom of a multiple-choice avatar to delete the user*/
	private void clickSelectVo(ImageView imageView){
		int count = mContactList.size();
		for(int i=0;i< count;i++){
			NewContactVO baseVo = (NewContactVO) mContactList.get(i);
			if(baseVo.getLocalId().equals(imageView.getTag())){
			  baseVo.setChecked(!baseVo.isChecked());
			  mNewContactListAdapter.notifyDataSetChanged();
			  if(mSearchAdapter!=null){
			     mSearchAdapter.notifyDataSetChanged();
			  }
			  break;
			}
		}
		mHorizontalScrollViewContent.removeView(imageView);
		int selectCount = selectList.size();
	    for(int i=0;i<selectCount;i++){
			UserBaseVo vo=selectList.get(i);
			if(vo.getLocalId().equals(imageView.getTag())){
				selectList.remove(i);
				break;
			}
   	   }

   	   if(hasSelectList==null&&selectList.size()<=0){
   		  if(mFinishBtn.isEnabled()){
           	 mFinishBtn.setEnabled(false);
		  }
   	   }

   	   if(hasSelectList!=null){//Over the selected user data
		   int hasSelectCount = hasSelectList.size();
       	   for(int i=0;i<hasSelectCount;i++){
       		   UserBaseVo vo=hasSelectList.get(i);
       		   if(vo.getLocalId().equals(imageView.getTag())){
       			   hasSelectList.remove(i);
       			   break;
       		   }
		   }
	   }
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
		if(position>=mContactList.size()+1){
			return;
		}else if(position==0){
			showSearch();
			return;
		}
		if(isMultipleChoice){//multi-select
			NewContactVO baseVo = (NewContactVO) mContactList.get(position-1);
			if(baseVo.isCantChecked()){
				return;
			}
			baseVo.setChecked(!baseVo.isChecked());
			mNewContactListAdapter.notifyDataSetChanged();
			if(baseVo.isChecked()){
				final CharAvatarView imageView=new CharAvatarView(SelectContactUI.this);
				imageView.setTag(baseVo.getLocalId());
				imageView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						clickSelectVo(imageView);
					}
				});
				LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(Utils.dip2px(SelectContactUI.this,40), Utils.dip2px(SelectContactUI.this, 40));
				lp.setMargins(Utils.dip2px(SelectContactUI.this, 10), Utils.dip2px(SelectContactUI.this, 5), 0 , 0);
				mHorizontalScrollViewContent.addView(imageView,lp);
				imageView.setText(baseVo.getUsername(),imageView,baseVo.getThumb());
				selectList.add(baseVo);
				new Handler().postDelayed(new Runnable(){
					public void run() {
						mHorizontalScrollView.smoothScrollTo(mHorizontalScrollViewContent.getChildCount()*Utils.dip2px(SelectContactUI.this,50), 0);
					}
				}, 0);
				if(hasSelectList==null&&!mFinishBtn.isEnabled()){
					mFinishBtn.setEnabled(true);
				}
				if(hasSelectList!=null){
					hasSelectList.add(baseVo);
				}
			}else{
				for(int i=0;i<mHorizontalScrollViewContent.getChildCount();i++){
					View imageView=mHorizontalScrollViewContent.getChildAt(i);
					if(baseVo.getLocalId().equals(imageView.getTag())){
						mHorizontalScrollViewContent.removeViewAt(i);
						break;
					}
				}
				for(int i=0;i<selectList.size();i++){
					if(selectList.get(i).getLocalId().equals(baseVo.getLocalId())){
						selectList.remove(i);
						break;
					}
				}

				if(hasSelectList==null&&selectList.size()<=0) {
					if(mFinishBtn.isEnabled()){
						mFinishBtn.setEnabled(false);
					}
				}
				if(hasSelectList!=null){
					int hasSelectCount = hasSelectList.size();
					for(int i=0;i<hasSelectCount;i++){
						UserBaseVo vo=hasSelectList.get(i);
						if(vo.getLocalId().equals(baseVo.getLocalId())){
							hasSelectList.remove(i);
							break;
						}
					}
				}
			}
		}else{
			NewContactVO baseVo = (NewContactVO) mContactList.get(position-1);
			clickSingleItem(baseVo);
		}
	}

	@Override
	public void onRefresh() {
		swipeLayout.setRefreshing(false);
	}

	/*Choose to complete one*/
	private void clickSingleItem(UserBaseVo vo){
		selectList.add(vo);
		Intent i=new Intent();
		i.putExtra("selectList", selectList);
		setResult(RESULT_OK,i);
	    Utils.exitActivityAndBackAnim(this,true);
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode==0&&resultCode==RESULT_OK){
			final String uid = data.getStringExtra("uid");
			final String avatarUrl = data.getStringExtra("avatarurl");
			final String username = data.getStringExtra("username");
			final String gender = data.getStringExtra("gender");
			UserBaseVo vo = new UserBaseVo();
			vo.setLocalId(uid);
			vo.setThumb(avatarUrl);
			vo.setUsername(username);
			vo.setGender(gender);
			clickSingleItem(vo);
		}
		else if(requestCode==10&&resultCode==RESULT_OK){
			final String uid = data.getStringExtra("uid");
			final String avatarUrl = data.getStringExtra("avatarurl");
			final String username = data.getStringExtra("username");
			final String gender = data.getStringExtra("gender");
			UserBaseVo vo=new UserBaseVo();
			vo.setLocalId(uid);
			vo.setThumb(avatarUrl);
			vo.setUsername(username);
			vo.setGender(gender);
			clickSingleItem(vo);
		}else if(requestCode==100&&resultCode==RESULT_OK){
			final String gid = data.getStringExtra("gid");
			final String avatarUrl = data.getStringExtra("avatarurl");
			final String username = data.getStringExtra("groupName");
			final String gender = data.getStringExtra("gender");
			UserBaseVo vo=new UserBaseVo();
			vo.setLocalId(gid);
			vo.setThumb(avatarUrl);
			vo.setUsername(username);
			vo.setGender(gender);
			clickSingleItem(vo);
		}else if(requestCode==1000&&resultCode==RESULT_OK){//Choose common contact back
			ArrayList<UserBaseVo> selectListT = (ArrayList<UserBaseVo>) data.getSerializableExtra("selectList");

			if(selectListT!=null){
				selectList.clear();
				selectList.addAll(selectListT);
				if(selectList.size()<=0){
					mFinishBtn.setEnabled(false);
				}else{
					mFinishBtn.setEnabled(true);
				}
				for(int i=0;i<mContactList.size();i++){
					NewContactVO item = (NewContactVO) mContactList.get(i);
					boolean found = false;
					for(int m=0;m<selectList.size();m++) {
						if (selectList.get(m).getLocalId().equals(item.getLocalId())) {
							item.setChecked(true);
							found = true;
							break;
						}
					}
					if(!found){
						item.setChecked(false);
					}
				}
				mNewContactListAdapter.notifyDataSetChanged();

				mHorizontalScrollViewContent.removeAllViews();
				for(int i=0;i<selectList.size();i++){
					final CharAvatarView imageView=new CharAvatarView(SelectContactUI.this);
					imageView.setTag(selectList.get(i).getLocalId());
					imageView.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							clickSelectVo(imageView);
						}
					});
					LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(Utils.dip2px(SelectContactUI.this,40), Utils.dip2px(SelectContactUI.this, 40));
					lp.setMargins(Utils.dip2px(SelectContactUI.this, 10), Utils.dip2px(SelectContactUI.this, 5), 0, 0);
					mHorizontalScrollViewContent.addView(imageView, lp);
					imageView.setText(selectList.get(i).getUsername(),imageView,selectList.get(i).getThumb());
				}

				new Handler().postDelayed(new Runnable() {
					public void run() {
						mHorizontalScrollView.smoothScrollTo(mHorizontalScrollViewContent.getChildCount() * Utils.dip2px(SelectContactUI.this, 50), 0);
					}
				}, 0);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * To test whether the current list is empty
	 */
	private void checkListEmpty() {
		if(mContactList == null || mContactList.size() <= 1){
			emptyImg.setVisibility(View.VISIBLE);
			emptyText.setVisibility(View.VISIBLE);
			contactNum.setVisibility(View.GONE);
			emptyText.setText(getString(R.string.contact_default_empty));
			emptyImg.setImageResource(R.drawable.icon_contact_empty);
		}else {
			emptyImg.setVisibility(View.GONE);
			emptyText.setVisibility(View.GONE);
			contactNum.setVisibility(View.VISIBLE);
		}
	}

}
