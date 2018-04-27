package com.lingtuan.firefly.walletold;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.custom.CustomProgressBar;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.wallet.vo.TransVo;

import java.util.ArrayList;

/**
 * Created on 2017/8/23.
 */

public class OldTransAdapter extends BaseAdapter {

    private Context context = null ;
    private ArrayList<TransVo> transVos;
    private String selectedAddress;

    public OldTransAdapter(Context context, ArrayList<TransVo> transVos, String selectedAddress){
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
            convertView = View.inflate(context, R.layout.old_wallet_trans_item, null);
            holder.address = (TextView) convertView.findViewById(R.id.address);
            holder.time = (TextView) convertView.findViewById(R.id.time);
            holder.value = (TextView) convertView.findViewById(R.id.value);
            holder.transFailed = (TextView) convertView.findViewById(R.id.transFailed);
            holder.transIcon = (ImageView) convertView.findViewById(R.id.transIcon);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        TransVo transVo = transVos.get(position);
        if (transVo.getValue().startsWith("-")){
            holder.transIcon.setImageResource(R.drawable.icon_transfer_out);
            holder.value.setTextColor(context.getResources().getColor(R.color.colorRed));
        }else{
            holder.transIcon.setImageResource(R.drawable.icon_transfer_in);
            holder.value.setTextColor(context.getResources().getColor(R.color.yellow_wallet));
        }
        holder.address.setText(transVo.getToAddress());
        if (!TextUtils.isEmpty(transVo.getSymbol())){
            holder.value.setText(context.getString(R.string.token_trans,transVo.getValue(),transVo.getSymbol()));
        }else{
            holder.value.setText(context.getString(R.string.token_trans,transVo.getValue(),transVo.getSymbol()));
        }

        holder.transFailed.setTextColor(context.getResources().getColor(R.color.colorRed));
        if (transVo.getState() == 2){
            holder.transFailed.setVisibility(View.VISIBLE);
        }else if (transVo.getState() == -1){
            holder.transFailed.setVisibility(View.VISIBLE);
            holder.transFailed.setText(context.getString(R.string.wallet_trans_detail_type_0));
        }else{
            holder.transFailed.setVisibility(View.INVISIBLE);
        }

        holder.time.setText(Utils.formatTransTime(transVo.getTime()));
        return convertView;
    }

    static class ViewHolder{
        TextView address;//address
        TextView time;//time
        TextView value;//Transfer amount
        TextView transFailed;//Transaction failed
        ImageView transIcon;//Transfer icon
    }
}
