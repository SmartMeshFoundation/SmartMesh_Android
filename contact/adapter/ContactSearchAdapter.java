package com.lingtuan.firefly.contact.adapter;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.vo.UserBaseVo;

import java.util.List;

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
			h = new Holder();
			convertView = View.inflate(mContext, R.layout.contact_child_item,null);
			h.mAvatar = (ImageView) convertView.findViewById(R.id.invite_avatar);
			h.nickName = (TextView) convertView.findViewById(R.id.nearby_nickname);
			h.checkBox = (CheckBox) convertView.findViewById(R.id.checkbox);
			convertView.setTag(h);
		} else {
			h = (Holder) convertView.getTag();
		}

		UserBaseVo vo = friendList.get(position);
		int nickColor = R.color.black;
		h.nickName.setTextColor(mContext.getResources().getColor(nickColor));

		h.nickName.setText(vo.getShowName());
		NextApplication.displayCircleImage(h.mAvatar, vo.getThumb());
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
		ImageView mAvatar;
		TextView nickName;
		CheckBox checkBox;
	}
}
