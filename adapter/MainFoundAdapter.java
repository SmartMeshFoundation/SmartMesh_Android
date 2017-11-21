package com.lingtuan.firefly.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.offline.vo.WifiPeopleVO;
import com.lingtuan.firefly.vo.UserInfoVo;

import java.util.List;

/**
 * Near no network adapter
 */
public class MainFoundAdapter extends BaseAdapter {

	private Context c = null ;

	private List<WifiPeopleVO> source = null ;
	public MainFoundAdapter(Context c, List<WifiPeopleVO> source){
		this.c = c ; 
		this.source = source;
	}

	@Override
	public int getCount() {
		if(source == null ){
			return 0 ;
		}
		return source.size();
	}

	@Override
	public Object getItem(int position) {
		return source.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public void resetSource(List<WifiPeopleVO> source){
		this.source = source;
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		BlackHolder holder;
		if(convertView == null){
			holder = new BlackHolder();
			convertView = View.inflate(c, R.layout.black_list_item, null);
			holder.avatar = (ImageView)convertView.findViewById(R.id.invite_avatar);
			holder.nickName = (TextView)convertView.findViewById(R.id.name);
			convertView.setTag(holder);
		}else{
			holder = (BlackHolder)convertView.getTag();
		}

		WifiPeopleVO vo = source.get(position);
		NextApplication.displayCircleImage(holder.avatar, "file://".concat(vo.getThumb()));
		holder.nickName.setText(vo.getShowName());

		return convertView;
	}
	
	static class BlackHolder{
		ImageView avatar = null ;
		TextView nickName = null ;
	}

}
