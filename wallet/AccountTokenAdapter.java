package com.lingtuan.firefly.wallet;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.wallet.util.WalletStorage;
import com.lingtuan.firefly.wallet.vo.StorableWallet;
import com.lingtuan.firefly.wallet.vo.TokenVo;

import java.math.BigDecimal;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created on 2017/8/23.
 */

public class AccountTokenAdapter extends BaseAdapter {

    private Context context = null ;

    private ArrayList<TokenVo> tokenVos;

    private TokenOnItemClick tokenOnItemClick;

    public AccountTokenAdapter(Context context,ArrayList<TokenVo> tokenVos,TokenOnItemClick tokenOnItemClick){
        this.context = context;
        this.tokenVos = tokenVos;
        this.tokenOnItemClick = tokenOnItemClick;
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null){
            convertView = View.inflate(context, R.layout.wallet_token_item, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        TokenVo tokenVo = tokenVos.get(position);
        NextApplication.displayCircleToken(holder.tokenImg,Utils.buildThumb(tokenVo.getTokenLogo()));
        holder.tokenSymbol.setText(tokenVo.getTokenSymbol());

        int priceUnit = MySharedPrefs.readIntDefaultUsd(context,MySharedPrefs.FILE_USER,MySharedPrefs.KEY_TOKEN_PRICE_UNIT);//0 default  1 usd
        if (priceUnit == 0){
            if (TextUtils.isEmpty(tokenVo.getTokenPrice())){
                holder.tokenPrice.setText("━");
            }else{
                holder.tokenPrice.setText(context.getString(R.string.token_total_price,tokenVo.getTokenPrice()));
            }
            if (TextUtils.isEmpty(tokenVo.getUnitPrice())){
                holder.tokenTotalPrice.setText("━");
            }else{
                holder.tokenTotalPrice.setText(context.getString(R.string.token_total_price,tokenVo.getUnitPrice()));
            }

        }else{
            if (TextUtils.isEmpty(tokenVo.getUsdPrice())){
                holder.tokenPrice.setText("━");
            }else{
                holder.tokenPrice.setText(context.getString(R.string.token_total_usd_price,tokenVo.getUsdPrice()));
            }
            if (TextUtils.isEmpty(tokenVo.getUsdUnitPrice())){
                holder.tokenTotalPrice.setText("━");
            }else{
                holder.tokenTotalPrice.setText(context.getString(R.string.token_total_usd_price,tokenVo.getUsdUnitPrice()));
            }
        }
        if (TextUtils.isEmpty(tokenVo.getTokenStringBalance())){
            BigDecimal tokenBalanceDecimal = new BigDecimal(tokenVo.getTokenBalance()).setScale(6,BigDecimal.ROUND_CEILING);
            holder.tokenBalance.setText(tokenBalanceDecimal.toPlainString());
        }else{
            holder.tokenBalance.setText(tokenVo.getTokenStringBalance());
        }


        holder.walletTokenBody.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tokenOnItemClick != null){
                    tokenOnItemClick.setTokenOnItemClick(position);
                }
            }
        });

        return convertView;
    }

    public interface TokenOnItemClick{
        void setTokenOnItemClick(int position);
    }

    static class ViewHolder{
        @BindView(R.id.tokenImg)
        ImageView tokenImg;//token pic
        @BindView(R.id.tokenPrice)
        TextView tokenPrice;//token price
        @BindView(R.id.tokenSymbol)
        TextView tokenSymbol;//token name
        @BindView(R.id.tokenBalance)
        TextView tokenBalance;//token balance
        @BindView(R.id.tokenTotalPrice)
        TextView tokenTotalPrice;//token total price
        @BindView(R.id.walletTokenBody)
        LinearLayout walletTokenBody;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
