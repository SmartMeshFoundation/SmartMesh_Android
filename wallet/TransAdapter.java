package com.lingtuan.firefly.wallet;

import android.content.Context;
import android.text.TextUtils;
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
    private String selectedAddress;

    public TransAdapter(Context context,ArrayList<TransVo> transVos,String selectedAddress){
        this.context = context;
        this.transVos = transVos;
        this.selectedAddress = selectedAddress;
    }

    public void resetSource(ArrayList<TransVo> transVos,String selectedAddress){
        this.transVos = transVos;
        this.selectedAddress = selectedAddress;
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
        if (TextUtils.equals(selectedAddress,transVo.getFromAddress())){
            holder.address.setText(transVo.getToAddress());
            if (transVo.getType() == 0){//eth
                holder.value.setText("-" + context.getString(R.string.eth_er,transVo.getValue()));
            }else{//smt
                holder.value.setText("-" + context.getString(R.string.smt_er,transVo.getValue()));
            }
        }else{
            holder.address.setText(transVo.getFromAddress());
            if (transVo.getType() == 0){//eth
                holder.value.setText("+" + context.getString(R.string.eth_er,transVo.getValue()));
            }else{//smt
                holder.value.setText("+" + context.getString(R.string.smt_er,transVo.getValue()));
            }
        }

        holder.time.setText(Utils.formatTime(transVo.getTime()));
        return convertView;
    }

    static class ViewHolder{
        TextView address;//address
        TextView time;//time
        TextView value;//Transfer amount
    }
}
