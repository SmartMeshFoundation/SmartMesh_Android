package com.lingtuan.firefly.setting;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lingtuan.firefly.R;

public class CountryAdapter extends BaseAdapter {
	private Context mContext;
	private String[] list;
	public CountryAdapter(Context mContext,String[] list) {
		this.mContext = mContext;
		this.list = list;
	}
	
	/**
	 * when the ListView data changes, this method is called to update the ListView
	 * @param list
	 */
	public void updateListView(String[] list){
		this.list = list;
		notifyDataSetChanged();
	}

	public int getCount() {
		if(list != null)
			return list.length;
		return 0;
	}

	public String getItem(int position) {
		return list[position];
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(final int position, View view, ViewGroup arg2) {
		ViewHolder viewHolder = null;
		if (view == null) {
			viewHolder = new ViewHolder();
			view = LayoutInflater.from(mContext).inflate(R.layout.country_item, null);
			viewHolder.tvTitle = (TextView) view.findViewById(R.id.country_name);

			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}
		

		viewHolder.tvTitle.setText(list[position]);
		
		return view;

	}

    class ViewHolder {
		TextView tvTitle;
	}

}