package com.lingtuan.firefly.redpacket;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.redpacket.bean.RedPacketMessageBean;
import com.lingtuan.firefly.redpacket.listener.SetOnClickListener;
import com.lingtuan.firefly.util.Utils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RedPacketMessageAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<RedPacketMessageBean> redPacketMessages;
    private SetOnClickListener setOnClickListener;

    public RedPacketMessageAdapter(Context context,ArrayList<RedPacketMessageBean> redPacketMessages,SetOnClickListener setOnClickListener){
        this.context = context;
        this.redPacketMessages = redPacketMessages;
        this.setOnClickListener = setOnClickListener;
    }

    public void resetSource(ArrayList<RedPacketMessageBean> redPacketMessages){
        this.redPacketMessages = redPacketMessages;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (redPacketMessages != null){
            return redPacketMessages.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int i) {
        return redPacketMessages.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        RedPacketMessageHolder holder;
        if(view == null){
            view = View.inflate(context, R.layout.red_packet_message_item, null);
            holder = new RedPacketMessageHolder(view);
            view.setTag(holder);
        }else{
            holder = (RedPacketMessageHolder) view.getTag();
        }
        RedPacketMessageBean vo = redPacketMessages.get(i);
        holder.redMessageAmount.setText(context.getString(R.string.smt_er,vo.getWithdrawAmount()));
        holder.redMessageTime.setText(Utils.formatTime(vo.getMessageTime()));
        holder.redMessageAddress.setText(vo.getMessageToAddress());
        holder.redMessageDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (setOnClickListener != null){
                    setOnClickListener.onItemClickListener(i);
                }
            }
        });
        return view;
    }

    static class RedPacketMessageHolder{
        @BindView(R.id.redMessageType)
        TextView redMessageType = null ;
        @BindView(R.id.redMessageAmount)
        TextView redMessageAmount = null ;
        @BindView(R.id.redMessageAddress)
        TextView redMessageAddress = null ;
        @BindView(R.id.redMessageTime)
        TextView redMessageTime = null ;
        @BindView(R.id.redMessageState)
        TextView redMessageState = null ;
        @BindView(R.id.redMessageDetails)
        TextView redMessageDetails = null ;

        public RedPacketMessageHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
