package com.lingtuan.firefly.redpacket;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.redpacket.bean.RedPacketBean;
import com.lingtuan.firefly.redpacket.bean.RedPacketRecordBean;
import com.lingtuan.firefly.util.Utils;

import java.util.ArrayList;

public class RedPacketDetailAdapter extends BaseAdapter{

    private Context context;
    private ArrayList<RedPacketBean> redPacketRecords;

    public RedPacketDetailAdapter(Context context,ArrayList<RedPacketBean> redPacketRecords){
        this.context = context;
        this.redPacketRecords = redPacketRecords;
    }

    public void resetSource(ArrayList<RedPacketBean> redPacketRecords){
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
        RedPacketDetailHolder holder;
        if(view == null){
            holder = new RedPacketDetailHolder();
            view = View.inflate(context, R.layout.red_packet_detail_item, null);
            holder.redPacketToImage = view.findViewById(R.id.redPacketToImage);
            holder.redPacketToName = view.findViewById(R.id.redPacketToName);
            holder.redPacketAmount = view.findViewById(R.id.redPacketAmount);
            holder.redPacketTime = view.findViewById(R.id.redPacketTime);
            holder.redPacketBigValue = view.findViewById(R.id.redPacketBigValue);
            view.setTag(holder);
        }else{
            holder = (RedPacketDetailHolder) view.getTag();
        }
        RedPacketBean vo = redPacketRecords.get(i);

        return view;
    }

    static class RedPacketDetailHolder{
        TextView redPacketToImage = null ;
        TextView redPacketToName = null ;
        TextView redPacketAmount = null ;
        TextView redPacketTime = null ;
        TextView redPacketBigValue = null ;
    }
}
