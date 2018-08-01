package com.lingtuan.firefly.wallet;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.custom.CustomProgressBar;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.wallet.vo.TransVo;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

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
            convertView = View.inflate(context, R.layout.wallet_trans_item, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        TransVo transVo = transVos.get(position);

        if (transVo.getNoticeType() == 0){
            holder.address.setText(transVo.getToAddress());
            holder.transIcon.setImageResource(R.drawable.icon_transfer_out);
            holder.value.setTextColor(context.getResources().getColor(R.color.colorRed));
            holder.value.setText(context.getString(R.string.token_less,transVo.getValue(),transVo.getSymbol()));
        }else{
            holder.address.setText(transVo.getFromAddress());
            holder.transIcon.setImageResource(R.drawable.icon_transfer_in);
            holder.value.setTextColor(context.getResources().getColor(R.color.yellow_wallet));
            holder.value.setText(context.getString(R.string.token_add,transVo.getValue(),transVo.getSymbol()));
        }

        holder.transFailed.setTextColor(context.getResources().getColor(R.color.colorRed));

        if (transVo.getState() == 2){
            holder.transFailed.setVisibility(View.VISIBLE);
            holder.transProgressBar.setVisibility(View.GONE);
        }else if (transVo.getState() == -1){
            holder.transFailed.setVisibility(View.VISIBLE);
            holder.transProgressBar.setVisibility(View.GONE);
            holder.transFailed.setText(context.getString(R.string.wallet_trans_detail_type_0));
        }else if (transVo.getState() == 0){
            holder.transProgressBar.setVisibility(View.VISIBLE);
            holder.transFailed.setVisibility(View.VISIBLE);
            holder.transFailed.setTextColor(context.getResources().getColor(R.color.yellow_wallet));
            if (transVo.getBlockNumber() - transVo.getTxBlockNumber() < 0){
                holder.transFailed.setText(context.getString(R.string.wallet_trans_detail_type_18,1));
                holder.transProgressBar.setProgress(1);
            }else{
                int blockNumber = transVo.getBlockNumber() - transVo.getTxBlockNumber() + 1;
                if (blockNumber >= Constants.NEED_CONFIRM_BLOCK){
                    holder.transFailed.setVisibility(View.INVISIBLE);
                    holder.transProgressBar.setVisibility(View.GONE);
                }else{
                    holder.transFailed.setText(context.getString(R.string.wallet_trans_detail_type_18,blockNumber));
                    holder.transProgressBar.setProgress(blockNumber);
                }
            }
        }else{
            int blockNumber = transVo.getBlockNumber() - transVo.getTxBlockNumber() + 1;
            if (blockNumber >= Constants.NEED_CONFIRM_BLOCK){
                holder.transFailed.setVisibility(View.INVISIBLE);
                holder.transProgressBar.setVisibility(View.GONE);
            }else if(blockNumber < 0){
                holder.transFailed.setVisibility(View.VISIBLE);
                holder.transProgressBar.setVisibility(View.VISIBLE);
                holder.transProgressBar.setProgress(1);
                holder.transFailed.setTextColor(context.getResources().getColor(R.color.yellow_wallet));
                holder.transFailed.setText(context.getString(R.string.wallet_trans_detail_type_18,1));
            }else{
                holder.transFailed.setVisibility(View.VISIBLE);
                holder.transProgressBar.setVisibility(View.VISIBLE);
                holder.transProgressBar.setProgress(blockNumber);
                holder.transFailed.setTextColor(context.getResources().getColor(R.color.yellow_wallet));
                holder.transFailed.setText(context.getString(R.string.wallet_trans_detail_type_18,blockNumber));
            }
        }

        holder.time.setText(Utils.formatTransTime(transVo.getTime()));
        return convertView;
    }

    static class ViewHolder{
        @BindView(R.id.address)
        TextView address;//address
        @BindView(R.id.time)
        TextView time;//time
        @BindView(R.id.value)
        TextView value;//Transfer amount
        @BindView(R.id.transFailed)
        TextView transFailed;//Transaction failed
        @BindView(R.id.transIcon)
        ImageView transIcon;//Transfer icon
        @BindView(R.id.transProgressBar)
        CustomProgressBar transProgressBar;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
