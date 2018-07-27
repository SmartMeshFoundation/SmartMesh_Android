package com.lingtuan.firefly.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.vo.CountryCodeVo;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CountryCodeAdapter extends BaseAdapter {
	private List<CountryCodeVo> list = null;
	private Context mContext;
	
	public CountryCodeAdapter(Context mContext, List<CountryCodeVo> list) {
		this.mContext = mContext;
		this.list = list;
	}


	public int getCount() {
		if(list != null)
			return list.size();
		return 0;
	}

	public CountryCodeVo getItem(int position) {
		return list.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(final int position, View view, ViewGroup arg2) {
		ViewHolder viewHolder = null;
		final CountryCodeVo mContent = list.get(position);
		if (view == null) {
			view = LayoutInflater.from(mContext).inflate(R.layout.country_code_item, null);
			viewHolder = new ViewHolder(view);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}
		
		//According to the position classification for Char ASCII value of the first letter
		int section = getSectionForPosition(position);
		
		//If the current position classification is equal to the first letter of the location of the Char, is considered to be the first time
		if(position == getPositionForSection(section)){
			viewHolder.tvLetter.setVisibility(View.VISIBLE);
			viewHolder.line.setVisibility(View.VISIBLE);
			viewHolder.tvLetter.setText(mContent.getSortLetters());
		}else{
			viewHolder.tvLetter.setVisibility(View.GONE);
			viewHolder.line.setVisibility(View.GONE);
		}
	
		viewHolder.tvTitle.setText(mContent.getName());
		
		return view;

	}


	static class ViewHolder {
		@BindView(R.id.reg_country_code_catalog)
		TextView tvLetter;
		@BindView(R.id.reg_country_code_name)
		TextView tvTitle;
		@BindView(R.id.reg_country_code_line)
		View line;
		public ViewHolder(View view) {
			ButterKnife.bind(this, view);
		}
	}


	/**
	 * According to the current position of ListView for classification of Char ASCII value of the first letter
	 */
	public int getSectionForPosition(int position) {
		return list.get(position).getSortLetters().charAt(0);
	}

	/**
	 * According to the classification of the initials of the Char ASCII value get its first appeared the initials of the position
	 */
	@SuppressLint("DefaultLocale")
	public int getPositionForSection(int section) {
		for (int i = 0; i < getCount(); i++) {
			String sortStr = list.get(i).getSortLetters();
			char firstChar = sortStr.toUpperCase().charAt(0);
			if (firstChar == section) {
				return i;
			}
		}
		
		return -1;
	}

}