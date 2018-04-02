package com.lingtuan.firefly.wallet;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.wallet.util.WalletStorage;
import com.lingtuan.firefly.wallet.vo.StorableWallet;
import com.lingtuan.firefly.wallet.vo.TokenVo;

import java.util.ArrayList;

/**
 * Created on 2017/8/23.
 */

public class AccountTokenAdapter extends BaseAdapter {

    private Context context = null ;

    private ArrayList<TokenVo> tokenVos;

    public AccountTokenAdapter(Context context,ArrayList<TokenVo> tokenVos){
        this.context = context;
        this.tokenVos = tokenVos;
    }



    @Override
    public int getCount() {
        if (tokenVos != null){
            return tokenVos.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return tokenVos.get(position);
    }

    public void resetSource(ArrayList<TokenVo> tokenVos){
        this.tokenVos = tokenVos;
        notifyDataSetChanged();
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
            convertView = View.inflate(context, R.layout.wallet_token_item, null);
            holder.tokenImg = (ImageView) convertView.findViewById(R.id.tokenImg);
            holder.tokenPrice = (TextView) convertView.findViewById(R.id.tokenPrice);
            holder.tokenSymbol = (TextView) convertView.findViewById(R.id.tokenSymbol);
            holder.tokenBalance = (TextView) convertView.findViewById(R.id.tokenBalance);
            holder.tokenTotalPrice = (TextView) convertView.findViewById(R.id.tokenTotalPrice);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        TokenVo tokenVo = tokenVos.get(position);
        NextApplication.displayCircleImage(holder.tokenImg,Utils.buildThumb(tokenVo.getTokenLogo()));
        holder.tokenPrice.setText(tokenVo.getTokenPrice());
        holder.tokenSymbol.setText(tokenVo.getTokenSymbol());
        holder.tokenBalance.setText(tokenVo.getTokenNumber());
        return convertView;
    }

    static class ViewHolder{
        ImageView tokenImg;//token pic
        TextView tokenPrice;//token price
        TextView tokenSymbol;//token name
        TextView tokenBalance;//token balance
        TextView tokenTotalPrice;//token total price
    }
}
