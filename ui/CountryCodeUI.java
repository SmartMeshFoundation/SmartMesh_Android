package com.lingtuan.firefly.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.adapter.CountryCodeAdapter;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.custom.CountryCodeComparator;
import com.lingtuan.firefly.custom.CountryCodeSideBar;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.vo.CountryCodeVo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Choose the country code
 */
public class CountryCodeUI extends BaseActivity {

	private ListView mListView;
	private CountryCodeSideBar mSideBar;
	private TextView mCatalog;
	private TextView mCountryCodeSelected;
	private CountryCodeAdapter mAdapter;
	
	private List<CountryCodeVo> mList;
	private CountryCodeVo mCountryCode;
	
	@Override
	protected void setContentView() {
		setContentView(R.layout.country_code_layout);
	}

	@Override
	protected void findViewById() {
		mListView = (ListView) findViewById(R.id.country_code_list);
		mSideBar = (CountryCodeSideBar) findViewById(R.id.country_code_sidrbar);
		mCatalog = (TextView) findViewById(R.id.country_code_catalog);
		mCountryCodeSelected = (TextView) findViewById(R.id.country_code_selected);
	}

	@Override
	protected void setListener() {
		mSideBar.setOnTouchingLetterChangedListener(new CountryCodeSideBar.OnTouchingLetterChangedListener() {
			
			@Override
			public void onTouchingLetterChanged(String s) {
				//The letter for the first time in position
				int position = mAdapter.getPositionForSection(s.charAt(0));
				if(position != -1){
					mListView.setSelection(position);
				}
			}
		});
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				mCountryCode = mAdapter.getItem(position);
//				MyToast.showToast(CountryCodeUI.this,mCountryCode.getName());
				Intent intent = new Intent();
				intent.putExtra("countrycode", mCountryCode);
				setResult(RESULT_OK, intent);
//				mCountryCodeSelected.setText(getString(R.string.reg_country_code, mCountryCode.getName(),mCountryCode.getCode()));
				Utils.exitActivityAndBackAnim(CountryCodeUI.this,true);
			}
		});
	}

	@Override
	protected void initData() {
		mCountryCode = (CountryCodeVo) getIntent().getExtras().getSerializable("countrycode");
		mSideBar.setTextView(mCatalog);
		mList = filledData(getResources().getString(R.string.country_code).split(","));
		Collections.sort(mList,new CountryCodeComparator());
		mAdapter = new CountryCodeAdapter(this, mList);
		mListView.setAdapter(mAdapter);
		mCountryCodeSelected.setText(getString(R.string.reg_country_code, mCountryCode.getName(),mCountryCode.getCode()));
		
		setTitle(getString(R.string.reg_country_code_title));
		
	}
	
	/**
	 * For ListView populate the data
	 * @param date
	 * @return
	 */
	@SuppressLint("DefaultLocale")
	private List<CountryCodeVo> filledData(String[] date){
		List<CountryCodeVo> mSortList = new ArrayList<CountryCodeVo>();
		
		for(int i=0; i<date.length; i++){
			CountryCodeVo mCityCode = new CountryCodeVo();
			mCityCode.parseDate(date[i]);
			String sortString = mCityCode.getSortLetters().substring(0, 1).toUpperCase();
			// Regular expressions, to determine whether the initials in English letters
			if(!sortString.matches("[A-Z]")){
				mCityCode.setSortLetters("#");
			} 
			mSortList.add(mCityCode);
		}
		return mSortList;
	}
}
