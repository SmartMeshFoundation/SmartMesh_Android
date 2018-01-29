package com.lingtuan.firefly.raiden;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.raiden.vo.RaidenChannelVo;

import java.util.List;

/**
 * Created by Administrator on 2018/1/24.
 */

public class RaidenChannelListAdapter extends BaseAdapter{

    private Context context = null ;

    private List<RaidenChannelVo> source = null ;
    private ChangeChannelStateListener channelStateListener;

    public interface ChangeChannelStateListener {
        void changeChannel(int position,boolean isOpen);
        void deopsitChannel(int position);
        void transferChannel(int position);
    }

    public RaidenChannelListAdapter(Context c, List<RaidenChannelVo> source,ChangeChannelStateListener channelStateListener){
        this.context = c ;
        this.source = source;
        this.channelStateListener = channelStateListener;
    }

    @Override
    public int getCount() {
        if(source == null ){
            return 0 ;
        }
        return source.size();
    }

    @Override
    public Object getItem(int position) {
        return source.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void resetSource(List<RaidenChannelVo> source){
        this.source = source;
        notifyDataSetChanged();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        RaidenChannelHolder holder;
        if(convertView == null){
            holder = new RaidenChannelHolder();
            convertView = View.inflate(context, R.layout.raiden_channel_list_item, null);
            holder.partner = (TextView) convertView.findViewById(R.id.partner);
            holder.balance = (TextView) convertView.findViewById(R.id.balance);
            holder.pay = (TextView) convertView.findViewById(R.id.pay);
            holder.token = (TextView) convertView.findViewById(R.id.token);
            holder.state = (TextView) convertView.findViewById(R.id.state);
            holder.raidenPay = (TextView) convertView.findViewById(R.id.raidenPay);
            holder.raidenAdd = (TextView) convertView.findViewById(R.id.raidenAdd);
            holder.raidenClose = (TextView) convertView.findViewById(R.id.raidenClose);
            holder.channelLine =  convertView.findViewById(R.id.channelLine);
            holder.channelBody = (LinearLayout) convertView.findViewById(R.id.channelBody);
            convertView.setTag(holder);
        }else{
            holder = (RaidenChannelHolder)convertView.getTag();
        }
        final RaidenChannelVo vo = source.get(position);
        holder.partner.setText(vo.getPartnerAddress());
        holder.balance.setText(vo.getBalance());
        holder.token.setText(context.getString(R.string.smt));
        holder.state.setText(vo.getState());

        if (TextUtils.equals("closed",vo.getState())){
            holder.channelLine.setVisibility(View.GONE);
            holder.channelBody.setVisibility(View.GONE);
        }else{
            holder.channelLine.setVisibility(View.VISIBLE);
            holder.channelBody.setVisibility(View.VISIBLE);
        }

        holder.raidenPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (channelStateListener != null){
                    channelStateListener.transferChannel(position);
                }
            }
        });

        holder.raidenAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (channelStateListener != null){
                    channelStateListener.deopsitChannel(position);
                }
            }
        });

        holder.raidenClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (channelStateListener != null){
                    channelStateListener.changeChannel(position,TextUtils.equals("opened",vo.getState()));
                }
            }
        });

        return convertView;
    }

    static class RaidenChannelHolder{
        TextView partner;//partner
        TextView balance;//deposit
        TextView pay;//pay
        TextView token;//token
        TextView state;//state
        TextView raidenPay;//raidenPay
        TextView raidenAdd;//raidenAdd
        TextView raidenClose;//raidenClose

        View channelLine;
        LinearLayout channelBody;
    }
}
