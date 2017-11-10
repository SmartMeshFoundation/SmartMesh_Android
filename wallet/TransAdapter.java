package com.lingtuan.firefly.wallet;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.wallet.vo.TransVo;

import java.util.ArrayList;

/**
 * Created on 2017/8/23.
 */

public class TransAdapter extends BaseAdapter {

    private Context context = null ;
    private ArrayList<TransVo> transVos;


    public TransAdapter(Context context,ArrayList<TransVo> transVos){
        this.context = context;
        this.transVos = transVos;
    }

    public void resetSource(ArrayList<TransVo> transVos){
        this.transVos = transVos;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (transVos == null){
            return 0;
        }
        return transVos.size();
    }

    @Override
    public Object getItem(int position) {
        return transVos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null){
            holder = new ViewHolder();
            convertView = View.inflate(context, R.layout.wallet_trans_item, null);
            holder.address = (TextView) convertView.findViewById(R.id.address);
            holder.time = (TextView) convertView.findViewById(R.id.time);
            holder.value = (TextView) convertView.findViewById(R.id.value);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        TransVo transVo = transVos.get(position);
        holder.address.setText(transVo.getAddress());
        holder.time.setText(Utils.formatTime(transVo.getTime()));
        if (transVo.getType() == 0){//eth
            holder.value.setText(context.getString(R.string.eth_er,transVo.getValue()));
        }else{//smt
            holder.value.setText(context.getString(R.string.smt_er,transVo.getValue()));
        }
        return convertView;
    }

    static class ViewHolder{
        TextView address;
        TextView time;
        TextView value;
    }
}
