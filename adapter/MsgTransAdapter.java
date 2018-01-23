package com.lingtuan.firefly.adapter;

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
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;
import com.lingtuan.firefly.vo.ChatMsg;
import com.lingtuan.firefly.vo.UserBaseVo;
import com.lingtuan.firefly.wallet.vo.TransVo;

import org.json.JSONObject;

import java.util.List;

/**
 * trans msg adapter
 */
public class MsgTransAdapter extends BaseAdapter {

	private List<ChatMsg> mList;
	private Context mContext;

	public MsgTransAdapter(List<ChatMsg> mList, Context mContext) {
		this.mList = mList;
		this.mContext = mContext;
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
		final ChatMsg chatMsg = mList.get(position);
		if(convertView == null){
			h = new Holder();
			convertView = View.inflate(mContext, R.layout.item_msg_trans, null);
			h.transNotify = (TextView) convertView.findViewById(R.id.transNotify);
			h.transTime = (TextView) convertView.findViewById(R.id.transTime);
			h.transFromAddress = (TextView) convertView.findViewById(R.id.transFromAddress);
			convertView.setTag(h);
		}else{
			h = (Holder) convertView.getTag();
		}
		Utils.setLoginTime(mContext,h.transTime,chatMsg.getMsgTime());
		if (chatMsg.getNoticeType() == 0){
			if (TextUtils.equals("0",chatMsg.getMode())){//eth
				if (chatMsg.getInviteType() == 0){
					h.transNotify.setText(mContext.getString(R.string.wallet_trans_msg_eth,chatMsg.getMoney()));
				}else{
					h.transNotify.setText(mContext.getString(R.string.wallet_trans_msg_eth_failed,chatMsg.getMoney()));
				}
			}else if (TextUtils.equals("1",chatMsg.getMode())){
				if (chatMsg.getInviteType() == 0){
					h.transNotify.setText(mContext.getString(R.string.wallet_trans_msg_smt,chatMsg.getMoney()));
				}else{
					h.transNotify.setText(mContext.getString(R.string.wallet_trans_msg_smt_failed,chatMsg.getMoney()));
				}
			}else if (TextUtils.equals("2",chatMsg.getMode())){
				if (chatMsg.getInviteType() == 0){
					h.transNotify.setText(mContext.getString(R.string.wallet_trans_msg_mesh,chatMsg.getMoney()));
				}else{
					h.transNotify.setText(mContext.getString(R.string.wallet_trans_msg_mesh_failed,chatMsg.getMoney()));
				}
			}
			h.transFromAddress.setText(mContext.getString(R.string.wallet_trans_to,chatMsg.getToAddress()));
		}else{
			if (TextUtils.equals("0",chatMsg.getMode())){//eth
				if (chatMsg.getInviteType() == 0){
					h.transNotify.setText(mContext.getString(R.string.wallet_trans_collect_eth,chatMsg.getMoney()));
				}else{
					h.transNotify.setText(mContext.getString(R.string.wallet_trans_collect_eth_failed,chatMsg.getMoney()));
				}
			}else if (TextUtils.equals("1",chatMsg.getMode())){
				if (chatMsg.getInviteType() == 0){
					h.transNotify.setText(mContext.getString(R.string.wallet_trans_collect_smt,chatMsg.getMoney()));
				}else{
					h.transNotify.setText(mContext.getString(R.string.wallet_trans_collect_smt_failed,chatMsg.getMoney()));
				}
			}else if (TextUtils.equals("2",chatMsg.getMode())){
				if (chatMsg.getInviteType() == 0){
					h.transNotify.setText(mContext.getString(R.string.wallet_trans_collect_mesh,chatMsg.getMoney()));
				}else{
					h.transNotify.setText(mContext.getString(R.string.wallet_trans_collect_mesh_failed,chatMsg.getMoney()));
				}
			}
			h.transFromAddress.setText(mContext.getString(R.string.wallet_trans_from,chatMsg.getFromAddress()));
		}
		return convertView;
	}
	
	static  class Holder {
		TextView transNotify;
		TextView transTime;
		TextView transFromAddress;
	}

}
