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
 * Search nickname page adapter
 */
public class ContactSearchNickAdapter extends BaseAdapter {

	private List<UserBaseVo> friendList;
	private Context mContext;

	public ContactSearchNickAdapter(List<UserBaseVo> friendList,
									Context mContext) {
		this.friendList = friendList;
		this.mContext = mContext;
	}

	public void updateList(List<UserBaseVo> friendList) {
		this.friendList = friendList;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		if (friendList != null) {
			return friendList.size();
		}
		return 0;
	}

	@Override
	public UserBaseVo getItem(int position) {
		return friendList.get(position);
	}

	@Override
	public long getItemId(int position) {

		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		Holder h;

		if (convertView == null) {
			h = new Holder();
			convertView = View.inflate(mContext, R.layout.contact_search_nick_item, null);
			h.mAvatar = (ImageView) convertView.findViewById(R.id.invite_avatar);
			h.nickName = (TextView) convertView.findViewById(R.id.nearby_nickname);
			convertView.setTag(h);
		} else {
			h = (Holder) convertView.getTag();
		}
		UserBaseVo vo = friendList.get(position);
		h.nickName.setText(vo.getShowName());
		NextApplication.displayCircleImage(h.mAvatar, vo.getThumb());
		return convertView;
	}

	static class Holder {
		ImageView mAvatar;
		TextView nickName;
	}
}
