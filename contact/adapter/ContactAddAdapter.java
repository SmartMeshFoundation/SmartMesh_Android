package com.lingtuan.firefly.contact.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.MyToast;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.vo.ChatMsg;
import com.lingtuan.firefly.vo.UserBaseVo;

import org.json.JSONObject;

import java.util.List;

/**
 * Add buddy adapter
 */
public class ContactAddAdapter extends BaseAdapter {

	private List<ChatMsg> mList;
	private Context mContext;

	private  ContactAddAdapterListener listener;
	public ContactAddAdapter(List<ChatMsg> mList, Context mContext,ContactAddAdapterListener listener) {
		this.mList = mList;
		this.mContext = mContext;
		this.listener= listener;
	}

	public void updateList(List<ChatMsg> mList){
		this.mList = mList;
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		if (mList == null) {
			return 0;
		}
		return mList.size();
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
	public View getView(int position, View convertView, ViewGroup parent) {
		
		final Holder h;
		final ChatMsg msg = mList.get(position);
		
		if(convertView == null){
			h = new Holder();
			convertView = View.inflate(mContext, R.layout.item_msg_contact, null);
			h.avatar = (ImageView) convertView.findViewById(R.id.item_avatar);
			h.content = (TextView) convertView.findViewById(R.id.item_content);
			h.time = (TextView) convertView.findViewById(R.id.item_times);
			h.nickname = (TextView) convertView.findViewById(R.id.item_nickname);
			h.agreeBtn = (TextView) convertView.findViewById(R.id.item_msg_event_agree);
			convertView.setTag(h);
		}else{
			h = (Holder) convertView.getTag();
		}
		
		String url = "";
		String nickname = "";
		String content = "";
		
		nickname = msg.getUsername();
		url = msg.getUserImage();
		if(!TextUtils.isEmpty(msg.getContent())){
			content = msg.getContent();
			Utils.setLoginTime(mContext, h.time, msg.getMsgTime());
		} else if(!TextUtils.isEmpty(msg.getShareFriendName())){
			content = mContext.getResources().getString(R.string.contact_add_content_share,msg.getShareFriendName());
			h.time.setText(mContext.getString(R.string.friend_hope_add));

		} else{
			content = mContext.getResources().getString(R.string.contact_add_content,msg.getUsername());
			Utils.setLoginTime(mContext, h.time, msg.getMsgTime());
		}
		
		NextApplication.displayCircleImage(h.avatar, url);
		h.nickname.setText(nickname);
		h.content.setText(content);
		h.agreeBtn.setVisibility(View.VISIBLE);

		
		
		if(msg.isAgree()){
			h.agreeBtn.setText(R.string.agreeed);
			h.agreeBtn.setTextColor(mContext.getResources().getColor(R.color.textColorHint));
			h.agreeBtn.setEnabled(false);
			h.agreeBtn.setBackgroundDrawable(null);
		}else{
			h.agreeBtn.setEnabled(true);
			h.agreeBtn.setText(R.string.agree);
			h.agreeBtn.setTextColor(mContext.getResources().getColor(R.color.textColor));
			h.agreeBtn.setBackgroundResource(R.drawable.selector_round_black_5);
		}
		h.agreeBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				h.agreeBtn.setEnabled(false);
				requestAgree(h,msg);
				
			}
		});

		return convertView;
	}
	
	static  class Holder {
		ImageView avatar;
		TextView time;
		TextView agreeBtn;
		TextView content;
		TextView nickname;
	}

	/**
	 * Agree to add buddy
	 * */
	private void requestAgree(final Holder h, final ChatMsg msg){
		if(msg.isOffLineMsg())
		{
			if(listener!=null)
			{
				boolean successed =  listener.addFriendConfim(msg.getUserId());
				if(successed)
				{
					FinalUserDataBase.getInstance().updateChatEventAgree(msg.getUserId(), true,msg.getType());

					UserBaseVo vo = new UserBaseVo();
					vo.setFriendLog(1);
					vo.setThumb(msg.getUserImage());
					vo.setLocalId(msg.getUserId());
					vo.setUsername(msg.getUsername());
					vo.setGender(msg.getGender()+"");
					vo.setOffLine(true);
					FinalUserDataBase.getInstance().saveFriendUserBase(vo);

					msg.setAgree(true);
					notifyDataSetChanged();
				}
				else{
					h.agreeBtn.setEnabled(true);
                    if(Utils.isConnectNet(mContext))
					{
						NetRequestImpl.getInstance().addFriendAgree(msg.getUserId(), new RequestListener() {
							@Override
							public void start() {

							}

							@Override
							public void success(JSONObject response) {
								try {
									MyToast.showToast(mContext, response.optString("msg"));
									FinalUserDataBase.getInstance().updateChatEventAgree(msg.getUserId(), true,msg.getType());
									msg.setAgree(true);
									Constants.isRefresh = true;
									notifyDataSetChanged();
								} catch (Exception e) {
									e.printStackTrace();
								}
							}

							@Override
							public void error(int errorCode, String errorMsg) {
								try {
									if(1211122 == errorCode){
										FinalUserDataBase.getInstance().updateChatEventAgree(msg.getUserId(), true, msg.getType());
										msg.setAgree(true);
									}else{
										MyToast.showToast(mContext,errorMsg);
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
								notifyDataSetChanged();
							}
						});
					}
				}

			}
		}
		else{
			NetRequestImpl.getInstance().addFriendAgree(msg.getUserId(), new RequestListener() {
				@Override
				public void start() {

				}

				@Override
				public void success(JSONObject response) {
					try {
						MyToast.showToast(mContext, response.optString("msg"));
						FinalUserDataBase.getInstance().updateChatEventAgree(msg.getUserId(), true,msg.getType());
						msg.setAgree(true);
						Constants.isRefresh = true;
						notifyDataSetChanged();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				@Override
				public void error(int errorCode, String errorMsg) {
					try {
						if(1211121 == errorCode){
							FinalUserDataBase.getInstance().updateChatEventAgree(msg.getUserId(), true, msg.getType());
							msg.setAgree(true);
						}else{
							MyToast.showToast(mContext,errorMsg);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					notifyDataSetChanged();
				}
			});
		}
	}
	public  interface  ContactAddAdapterListener
	{
		boolean addFriendConfim(String uid);
	}
}
