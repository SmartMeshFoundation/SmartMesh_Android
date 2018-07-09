package com.lingtuan.firefly.redpacket;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.redpacket.bean.RedPacketRecordBean;
import com.lingtuan.firefly.util.Utils;

import java.util.ArrayList;

public class RedPacketBalanceRecordAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<RedPacketRecordBean> redPacketRecords;

    public RedPacketBalanceRecordAdapter(Context context,ArrayList<RedPacketRecordBean> redPacketRecords){
        this.context = context;
        this.redPacketRecords = redPacketRecords;
    }

    public void resetSource(ArrayList<RedPacketRecordBean> redPacketRecords){
        this.redPacketRecords = redPacketRecords;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (redPacketRecords != null){
            return redPacketRecords.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int i) {
        return redPacketRecords.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        RedPacketRecordHolder holder;
        if(view == null){
            holder = new RedPacketRecordHolder();
            view = View.inflate(context, R.layout.red_packet_balace_record_item, null);
            holder.redRecordType = view.findViewById(R.id.redRecordType);
            holder.redRecordAddress = view.findViewById(R.id.redRecordAddress);
            holder.redRecordAmount = view.findViewById(R.id.redRecordAmount);
            holder.redRecordTime = view.findViewById(R.id.redRecordTime);
            view.setTag(holder);
        }else{
            holder = (RedPacketRecordHolder) view.getTag();
        }
        RedPacketRecordBean vo = redPacketRecords.get(i);
        if (vo.getRedRecordType() == 0){
            holder.redRecordAddress.setVisibility(View.GONE);
        }else{
            holder.redRecordAddress.setVisibility(View.VISIBLE);
            holder.redRecordAddress.setText(vo.getRedRecordAddress());
        }

        holder.redRecordAmount.setText(vo.getRedRecordAmount());
        holder.redRecordTime.setText(Utils.formatTime(vo.getRedRecordTime()));

        return view;
    }

    static class RedPacketRecordHolder{
        TextView redRecordType = null ;
        TextView redRecordAddress = null ;
        TextView redRecordAmount = null ;
        TextView redRecordTime = null ;
    }
}
