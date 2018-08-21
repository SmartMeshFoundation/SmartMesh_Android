package com.lingtuan.firefly.contact.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.custom.CharAvatarView;
import com.lingtuan.firefly.vo.UserBaseVo;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

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
			convertView = View.inflate(mContext, R.layout.discuss_group_member_list_item, null);
			h = new Holder(convertView);
			convertView.setTag(h);
		}else{
			h = (Holder) convertView.getTag();
		}
		UserBaseVo uInfo = mList.get(position);
		h.mNickname.setText(uInfo.getShowName());
		h.mAvatar.setText(uInfo.getUsername(),h.mAvatar,uInfo.getThumb());
		return convertView;
	}
	
	static class Holder{

		@BindView(R.id.invite_avatar)
		CharAvatarView mAvatar;
		@BindView(R.id.invite_nickname)
		TextView mNickname;

		public Holder(View view) {
			ButterKnife.bind(this, view);
		}
	}
}
