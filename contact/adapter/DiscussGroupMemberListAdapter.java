package com.lingtuan.firefly.contact.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.vo.UserBaseVo;

import java.util.List;

/**
 * Invited users list adapter
 */
public class DiscussGroupMemberListAdapter extends BaseAdapter {

	private Context mContext;
	private List<UserBaseVo> mList;
	public DiscussGroupMemberListAdapter(Context mContex, List<UserBaseVo> mList) {
		this.mContext = mContex;
		this.mList = mList;

	}

	
	@Override
	public int getCount() {
		if(mList != null)
			return mList.size();
		return 0;
	}

	@Override
	public Object getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		Holder h;
		if(convertView == null){
			h = new Holder();
			convertView = View.inflate(mContext, R.layout.discuss_group_member_list_item, null);
			h.mAvatar = (ImageView) convertView.findViewById(R.id.invite_avatar);
			h.mNickname = (TextView) convertView.findViewById(R.id.invite_nickname);
			convertView.setTag(h);
		}else{
			h = (Holder) convertView.getTag();
		}
		UserBaseVo uInfo = mList.get(position);
		h.mNickname.setText(uInfo.getShowName());
		NextApplication.displayCircleImage(h.mAvatar, uInfo.getThumb());
		

		return convertView;
	}
	
	static class Holder{
		ImageView mAvatar;
		TextView mNickname;

	}
}
