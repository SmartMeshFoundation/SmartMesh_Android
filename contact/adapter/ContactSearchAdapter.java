package com.lingtuan.firefly.contact.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.custom.CharAvatarView;
import com.lingtuan.firefly.vo.UserBaseVo;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Contacts search page
 */
public class ContactSearchAdapter extends BaseAdapter {

	private List<UserBaseVo> friendList;
	private Context mContext;
	private boolean isMultipleChoice;


	public ContactSearchAdapter(List<UserBaseVo> friendList, Context mContext, boolean isMultipleChoice) {
		this.friendList = friendList;
		this.mContext = mContext;
		this.isMultipleChoice = isMultipleChoice;
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
	public View getView(final int position, View convertView, ViewGroup parent) {
		Holder h;
        
		if (convertView == null) {
			convertView = View.inflate(mContext, R.layout.contact_child_item,null);
			h = new Holder(convertView);
			convertView.setTag(h);
		} else {
			h = (Holder) convertView.getTag();
		}

		UserBaseVo vo = friendList.get(position);
		int nickColor = R.color.black;
		h.nickName.setTextColor(mContext.getResources().getColor(nickColor));
		h.nickName.setText(vo.getShowName());
		h.mAvatar.setText(vo.getUsername(),h.mAvatar, vo.getThumb());
		if (isMultipleChoice){// multi-select
			if(vo.isCantChecked()){
				h.checkBox.setVisibility(View.VISIBLE);
				h.checkBox.setButtonDrawable(R.drawable.checkbox_cant_selected);
			}else{
				h.checkBox.setVisibility(View.VISIBLE);
				if(vo.isChecked()){
					h.checkBox.setButtonDrawable(R.drawable.checkbox_selected);
				}else{
					h.checkBox.setButtonDrawable(R.drawable.checkbox_unselected);
				}
			}
		}
		return convertView;
	}

	static class Holder {
		@BindView(R.id.invite_avatar)
		CharAvatarView mAvatar;
		@BindView(R.id.nearby_nickname)
		TextView nickName;
		@BindView(R.id.checkbox)
		CheckBox checkBox;

		public Holder(View view) {
			ButterKnife.bind(this, view);
		}
	}
}
