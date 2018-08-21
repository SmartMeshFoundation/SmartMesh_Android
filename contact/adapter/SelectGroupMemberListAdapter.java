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
 * Select group of members of the adapter
 */
public class SelectGroupMemberListAdapter extends BaseAdapter {

	private List<UserBaseVo> mList;
	private Context mContext;
	
	public SelectGroupMemberListAdapter(List<UserBaseVo> mList, Context mContext) {
		this.mList = mList;
		this.mContext = mContext;
	}

	@Override
	public int getCount() {
		if(mList != null){
			return mList.size();
		}
		return 0;
	}

	public void updateList(List<UserBaseVo> mList){
		this.mList = mList;
		notifyDataSetChanged();
	}
	
	public List<UserBaseVo> getList(){
		return mList;
	}
	
	@Override
	public UserBaseVo getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder h = null;
		if(convertView == null){
			convertView = View.inflate(mContext, R.layout.contact_child_item, null);
			h = new Holder(convertView);
			convertView.setTag(h);
		}else{
			h = (Holder) convertView.getTag();
		}
		
		UserBaseVo vo = mList.get(position);
		h.mAvatar.setText(vo.getUsername(),h.mAvatar, vo.getThumb());
		h.mNickname.setText(vo.getShowName());
		
		return convertView;
	}

	static class Holder{
		@BindView(R.id.invite_avatar)
		CharAvatarView mAvatar;
		@BindView(R.id.nearby_nickname)
		TextView mNickname;
		public Holder(View view) {
			ButterKnife.bind(this, view);
		}
	}
	
}
