package com.lingtuan.firefly.contact.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.custom.SquareImageView;
import com.lingtuan.firefly.vo.UserBaseVo;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GroupMemberImageAdapter extends BaseAdapter {
	
    boolean isAdmin;
    boolean removeState=false;
    private Context mContext;
	List<UserBaseVo> data;
    GroupMemberImageListener listener;
	public int getAddFriendPosition(){
		if(isAdmin){
		    return data.size()-2;
		}else{
			return data.size()-1;	
		}
	}
	public int getDelFriendPosition(){
		return data.size()-1;	
	}
	public UserBaseVo get(int position)
	{
		return data.get(position);
	}
	public void remove(int position) {
		data.remove(position);
		this.notifyDataSetChanged();
	}

	public void updateList(ArrayList<UserBaseVo> data){
		this.data = data;
		notifyDataSetChanged();
	}
	
	public GroupMemberImageAdapter(Context c, List<UserBaseVo> data,boolean isAdmin, GroupMemberImageListener listener) {
		mContext = c;
		this.data=data;
		this.isAdmin=isAdmin;
		this.listener=listener;
	}
    public void setAdmin(boolean isAdmin){
    	this.isAdmin=isAdmin;
    }
    public void setRemoveState(boolean removeState){
    	this.removeState = removeState;
    }
    public boolean isRemmoveState(){
    	return removeState;
    }
	public int getCount() {
		return data.size();
	}

	public Object getItem(int position) {
		return data.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		MemberHolder holder;
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.discuss_group_manage_grid_item, null);
			holder = new MemberHolder(convertView);
			convertView.setTag(holder);
		}else{
			holder = (MemberHolder)convertView.getTag();
		}
		holder.imageview.setImageResource(0);
		if(isAdmin&&position==data.size()-1){//-
			NextApplication.displayCircleImage(holder.imageview, null);
			holder.imageview.setText("",holder.imageview,data.get(position).getThumb());
			holder.imageview.setImageResource(R.drawable.discuss_del);
			holder.name.setText("");
		}else if(isAdmin&&position==data.size()-2){//+
			NextApplication.displayCircleImage(holder.imageview, null);
			holder.imageview.setText("",holder.imageview,data.get(position).getThumb());
			holder.imageview.setImageResource(R.drawable.discuss_add);
			holder.name.setText("");
		}else if(position==data.size()-1){//+
			NextApplication.displayCircleImage(holder.imageview, null);
			holder.imageview.setText("",holder.imageview,data.get(position).getThumb());
			holder.imageview.setImageResource(R.drawable.discuss_add);
			holder.name.setText("");
		}else{
			holder.imageview.setText(data.get(position).getUsername(),holder.imageview,data.get(position).getThumb());
			holder.name.setText(data.get(position).getShowName());
		}
		holder.imageview.setVisibility(View.VISIBLE);
			if (removeState) {
				holder.deleteImg.setVisibility(View.VISIBLE);
                if(isAdmin){
					if (position==data.size()-1) {
						holder.imageview.setVisibility(View.INVISIBLE);
						holder.deleteImg.setVisibility(View.INVISIBLE);
					}else if (position==data.size()-2) {
						holder.imageview.setVisibility(View.INVISIBLE);
						holder.deleteImg.setVisibility(View.INVISIBLE);
					}
                }else{
                	if (position==data.size()-1) {
                		holder.imageview.setVisibility(View.INVISIBLE);
                		holder.deleteImg.setVisibility(View.INVISIBLE);
					}
                }
                if(position==0){
                	holder.deleteImg.setVisibility(View.INVISIBLE);
                }
			} else {
				holder.deleteImg.setVisibility(View.INVISIBLE);
				holder.imageview.setVisibility(View.VISIBLE);
			}
		    final int arg2=position;
			convertView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					clickItem(arg2);
				}
			});
		return convertView;
	}
	 private void clickItem(int arg2){

		if (removeState) {
			if (isAdmin&&arg2 >= data.size()-2) {
				removeState=false;
				this.notifyDataSetChanged();
			}else if(arg2 >= data.size()-1){
				removeState=false;
				this.notifyDataSetChanged();
			}else{
				if(arg2==0){
					return;
				}
				listener.removeMember(arg2);
			}
		} else { // Not delete status click event
			if(arg2==this.getAddFriendPosition()){
				//adapter.add();
				listener.addMembers();
			}else if(isAdmin&&arg2==this.getDelFriendPosition()){
				removeState=true;
				this.notifyDataSetChanged();
			}else{
				listener.clickMember(arg2);
			}
		}
	 }
	
	 static class MemberHolder{
		@BindView(R.id.grid_item_image)
		SquareImageView imageview;
		@BindView(R.id.deleteLayout)
		LinearLayout deleteImg;
		@BindView(R.id.grid_item_text)
		TextView name;

		public MemberHolder(View view) {
			ButterKnife.bind(this, view);
		}
	 }

	 public interface GroupMemberImageListener{
		void addMembers();
		void removeMember(int arg2);
		void clickMember(int arg2);
	 }
	 
}




