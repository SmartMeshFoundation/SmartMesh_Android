package com.lingtuan.firefly.contact.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.lingtuan.firefly.R;
import com.lingtuan.firefly.contact.vo.PhoneContactGroupVo;
import com.lingtuan.firefly.contact.vo.PhoneContactVo;
import com.lingtuan.firefly.listener.AddContactListener;

import java.util.List;

/**
 * Newsletter record add buddy adapter
 */
public class AddContactFriendsAdapter extends BaseExpandableListAdapter {

	private List<PhoneContactGroupVo> mGroupInfoList;
	
	private Context mContext;
	
	public AddContactListener listener = null ; 
	
	private int addType = 0; // This temporary variables is used to control whether to invite, only can send information in the newsletter record friends, other do not send...
	
	public AddContactFriendsAdapter(List<PhoneContactGroupVo> mGroupInfoList, Context mContext) {
		super();
		this.mGroupInfoList = mGroupInfoList;
		this.mContext = mContext;
	}
	

	public void setContactListener(AddContactListener Callback){
		this.listener = Callback;
	}

	public void setAddFriendsType(int addType){
		this.addType = addType;
	}
	
	public void updateList(List<PhoneContactGroupVo> mGroupInfoList){
		this.mGroupInfoList = mGroupInfoList;
		notifyDataSetChanged();
	}
	
	@Override
	public int getGroupCount() {
		if(mGroupInfoList == null){
			return 0;
		}
		return mGroupInfoList.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		
		if(mGroupInfoList == null ){
			return 0;
		}
		
		if(mGroupInfoList.get(groupPosition).getContactList() == null){
			return 0;
		}
		return mGroupInfoList.get(groupPosition).getContactList().size();
	}

	@Override
	public PhoneContactGroupVo getGroup(int groupPosition) {
		return mGroupInfoList.get(groupPosition);
	}

	@Override
	public PhoneContactVo getChild(int groupPosition, int childPosition) {
		return mGroupInfoList.get(groupPosition).getContactList().get(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
							 View convertView, ViewGroup parent) {
		
		HolderGroup h;
		if(convertView == null){
			h = new HolderGroup();
			convertView = View.inflate(mContext, R.layout.contact_group_item, null);
			h.childCount = (TextView) convertView.findViewById(R.id.contact_group_child_count);
			h.groupName =  (TextView) convertView.findViewById(R.id.contact_group_name);
			h.expandIcon =  (ImageView) convertView.findViewById(R.id.contact_group_expand_icon);
			convertView.setTag(h);
		}else{
			h = (HolderGroup) convertView.getTag();
		}
		PhoneContactGroupVo vo = mGroupInfoList.get(groupPosition);
		h.childCount.setText(vo.getContactList() != null ? vo.getContactList().size() + "" : "0");
		if(vo.getType() == 0){
			vo.setGroupName(mContext.getString(R.string.friend_can_invite));
		}else if(vo.getType() == 1){
			vo.setGroupName(mContext.getString(R.string.friend_can_add));
		}else{
			vo.setGroupName(mContext.getString(R.string.friend_have_add));
		}
		h.groupName.setText(vo.getGroupName());
		if(isExpanded){
			h.expandIcon.setImageResource(R.drawable.contact_open_tips);
		}else{
			h.expandIcon.setImageResource(R.drawable.contact_close_tips);
		}
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
							 boolean isLastChild, View convertView, ViewGroup parent) {
		
		HolderChild h;
		if(convertView == null){
			h = new HolderChild();
			convertView = View.inflate(mContext, R.layout.contact_add_child_item, null);
			h.nickName =  (TextView) convertView.findViewById(R.id.nearby_nickname);
			h.addFriend =  (TextView) convertView.findViewById(R.id.nearby_time);
			convertView.setTag(h);
		}else{
			h = (HolderChild) convertView.getTag();
		}
		
		Resources resources = mContext.getResources();
		
		final PhoneContactVo vo = mGroupInfoList.get(groupPosition).getContactList().get(childPosition);
		h.nickName.setText(vo.getShowName());
		
		if(vo.getRelation() == 0){  // Can invite
			h.addFriend.setText(resources.getString(R.string.invite));
			h.addFriend.setEnabled(true);
			h.addFriend.setTextColor(resources.getColor(R.color.textColor));
			h.addFriend.setBackgroundResource(R.drawable.selector_round_black_5);
		}else if(vo.getRelation() == 1){  // You can add
			h.addFriend.setText(resources.getString(R.string.add_friends));
			h.addFriend.setEnabled(true);
			h.addFriend.setTextColor(resources.getColor(R.color.textColor));
			h.addFriend.setBackgroundResource(R.drawable.selector_round_black_5);
		}else {  // Has been added
			h.addFriend.setText(resources.getString(R.string.contact_already_friends));
			h.addFriend.setEnabled(false);
			h.addFriend.setTextColor(resources.getColor(R.color.textColorHint));
			h.addFriend.setBackgroundDrawable(null);
		}
		h.addFriend.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(listener!=null){
					listener.addContactCallback(vo.getRelation() == 1 ?  vo.getUid() + "":vo.getId() , vo.getRelation() + "",addType,vo.getName());
				}
			}
		});
		
		return convertView;
	}


	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		
		return true;
	}

	static class HolderGroup{
		TextView groupName;
		TextView childCount;
		ImageView expandIcon;
		
	}
	
	static class HolderChild{
		TextView nickName;
		TextView addFriend;
	}
}
