package com.lingtuan.firefly.contact.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.contact.vo.DiscussionGroupsVo;
import com.lingtuan.firefly.custom.DiscussGroupImageView;
import com.lingtuan.firefly.vo.UserBaseVo;

import java.util.List;

public class DiscussGroupAdapter extends BaseAdapter {

	private Context c = null ;
	private List<DiscussionGroupsVo> sourceList = null ;
	public DiscussGroupAdapter(Context c, List<DiscussionGroupsVo> source){
		sourceList = source;
		this.c = c ; 
	}
	
	public void resetSource(List<DiscussionGroupsVo> source){
		sourceList = source;
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		if(sourceList != null)
			return sourceList.size();
		return 0;
	}

	@Override
	public Object getItem(int position) {
		return sourceList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		NearHolder holder ; 
		if(convertView == null){
			holder = new NearHolder();
			convertView = View.inflate(c, R.layout.discuss_group_item,null);
			holder.nickName = (TextView)convertView.findViewById(R.id.group_nickname);
			holder.avatar = (DiscussGroupImageView)convertView.findViewById(R.id.group_avatar);
			convertView.setTag(holder);
		}else{
			holder = (NearHolder)convertView.getTag();
		}
		DiscussionGroupsVo group = sourceList.get(position);
		holder.nickName.setText(group.getName());
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<group.getMembers().size();i++)
		{
			UserBaseVo member=group.getMembers().get(i);
			if(i==group.getMembers().size()-1)
			{
			    sb.append(member.getShowName());
			}
			else{
				 sb.append(member.getShowName()+",");
			}
		}
		holder.avatar.setMember(group.getMembers());
		
		return convertView;
	}
	
	
	
	static class NearHolder{
		DiscussGroupImageView avatar = null ; 
		TextView nickName = null ;
	}

}
