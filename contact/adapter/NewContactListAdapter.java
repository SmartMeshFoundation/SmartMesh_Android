package com.lingtuan.firefly.contact.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.contact.vo.NewContactVO;
import com.lingtuan.firefly.custom.CharAvatarView;
import com.lingtuan.firefly.custom.contact.ContactItemComparator;
import com.lingtuan.firefly.custom.contact.ContactItemInterface;
import com.lingtuan.firefly.custom.contact.ContactsSectionIndexer;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewContactListAdapter extends ArrayAdapter<ContactItemInterface>{

	private int resource; // store the resource layout id for 1 row
	private Context mContext;
	private ContactsSectionIndexer indexer = null;
	private ContactsSectionIndexer sectionIndex;
	private boolean isMultipleChoice;

	
	public NewContactListAdapter(Context _context, int _resource,List<ContactItemInterface> _items, boolean _isMultipleChoice){
		super(_context, _resource, _items);
		resource = _resource;
		mContext = _context;
		isMultipleChoice = _isMultipleChoice;
		// need to sort the items array first, then pass it to the indexer
		Collections.sort(_items, new ContactItemComparator());
		sectionIndex= new ContactsSectionIndexer(_items);
		setIndexer(sectionIndex);

	}
	
	public void updateList(List<ContactItemInterface> _items){
		sectionIndex.resetSectionIndexer(_items);
		notifyDataSetChanged();
	}

	public TextView getSectionTextView(View rowView){
		TextView sectionTextView = (TextView) rowView.findViewById(R.id.sectionTextView);
		return sectionTextView;
	}

	public void showSectionViewIfFirstItem(View rowView,ContactItemInterface item, int position){
		TextView sectionTextView = getSectionTextView(rowView);
		if (indexer.isFirstItemInSection(position)){
			String sectionTitle = indexer.getSectionTitle(item.getItemForIndex());
			sectionTextView.setText(sectionTitle);
			sectionTextView.setVisibility(View.VISIBLE);
		} else {
			sectionTextView.setVisibility(View.GONE);
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		ContactHolder holder ; 
		if(convertView == null){
			convertView = View.inflate(mContext, resource, null);
			holder = new ContactHolder(convertView);
			convertView.setTag(holder);
		}else{
			holder = (ContactHolder)convertView.getTag();
		}

		NewContactVO item = (NewContactVO) getItem(position);
		showSectionViewIfFirstItem(convertView, item , position);
		int nickColor = R.color.black;
		holder.nickName.setTextColor(mContext.getResources().getColor(nickColor));
		holder.nickName.setText(item.getShowName());
		if (item.isOffLine()){
			holder.offlineImg.setVisibility(View.VISIBLE);
		}else {
			holder.offlineImg.setVisibility(View.GONE);
		}
		holder.mAvatar.setText(item.getUsername(),holder.mAvatar, item.getThumb());
		if(isMultipleChoice){//multiple
			if(item.isCantChecked()){
				holder.checkBox.setVisibility(View.VISIBLE);
				holder.checkBox.setButtonDrawable(R.drawable.checkbox_cant_selected);
			}else{
				holder.checkBox.setVisibility(View.VISIBLE);
				if(item.isChecked()){
					holder.checkBox.setButtonDrawable(R.drawable.checkbox_selected);
				}else{
					holder.checkBox.setButtonDrawable(R.drawable.checkbox_unselected);
				}
			}		
		}
		return convertView;
	}


	public ContactsSectionIndexer getIndexer(){
		return indexer;
	}

	public void setIndexer(ContactsSectionIndexer indexer){
		this.indexer = indexer;
	}
	
	 static class ContactHolder{
		@BindView(R.id.nearby_nickname)
		TextView nickName;
		@BindView(R.id.offlineImg)
		ImageView offlineImg;
		@BindView(R.id.invite_avatar)
		CharAvatarView mAvatar;
		@BindView(R.id.checkbox)
		CheckBox checkBox;

		public ContactHolder(View view) {
			ButterKnife.bind(this, view);
		}
	}
}
