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

import butterknife.BindView;
import butterknife.ButterKnife;

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
			convertView = View.inflate(mContext, R.layout.item_msg_trans, null);
			h = new Holder(convertView);
			convertView.setTag(h);
		}else{
			h = (Holder) convertView.getTag();
		}
		Utils.setLoginTime(mContext,h.transTime,chatMsg.getMsgTime());
		if (chatMsg.getNoticeType() == 0){
			if (TextUtils.equals("0",chatMsg.getMode())){//eth
				if (chatMsg.getInviteType() == 0){
					h.transNotify.setText(mContext.getString(R.string.wallet_trans_msg,chatMsg.getMoney(),chatMsg.getTokenSymbol()));
				}else{
					h.transNotify.setText(mContext.getString(R.string.wallet_trans_msg_failed,chatMsg.getMoney(),chatMsg.getTokenSymbol()));
				}
			}else{
				if (chatMsg.getInviteType() == 0){
					h.transNotify.setText(mContext.getString(R.string.wallet_trans_msg,chatMsg.getMoney(),chatMsg.getTokenSymbol()));
				}else{
					h.transNotify.setText(mContext.getString(R.string.wallet_trans_msg_failed,chatMsg.getMoney(),chatMsg.getTokenSymbol()));
				}
			}
			h.transFromAddress.setText(mContext.getString(R.string.wallet_trans_to,chatMsg.getToAddress()));
		}else{
			if (TextUtils.equals("0",chatMsg.getMode())){//eth
				if (chatMsg.getInviteType() == 0){
					h.transNotify.setText(mContext.getString(R.string.wallet_trans_collect,chatMsg.getMoney(),chatMsg.getTokenSymbol()));
				}else{
					h.transNotify.setText(mContext.getString(R.string.wallet_trans_collect_failed,chatMsg.getMoney(),chatMsg.getTokenSymbol()));
				}
			}else{
				if (chatMsg.getInviteType() == 0){
					h.transNotify.setText(mContext.getString(R.string.wallet_trans_collect,chatMsg.getMoney(),chatMsg.getTokenSymbol()));
				}else{
					h.transNotify.setText(mContext.getString(R.string.wallet_trans_collect_failed,chatMsg.getMoney(),chatMsg.getTokenSymbol()));
				}
			}
			h.transFromAddress.setText(mContext.getString(R.string.wallet_trans_from,chatMsg.getFromAddress()));
		}
		return convertView;
	}
	
	static  class Holder {

		@BindView(R.id.transNotify)
		TextView transNotify;
		@BindView(R.id.transTime)
		TextView transTime;
		@BindView(R.id.transFromAddress)
		TextView transFromAddress;

		public Holder(View view) {
			ButterKnife.bind(this, view);
		}
	}

}
