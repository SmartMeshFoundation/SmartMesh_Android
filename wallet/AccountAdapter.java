package com.lingtuan.firefly.wallet;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.wallet.util.WalletStorage;

/**
 * Created on 2017/8/23.
 */

public class AccountAdapter extends BaseAdapter {

    private Context context = null ;


    public AccountAdapter(Context context){
        this.context = context;
    }


    @Override
    public int getCount() {
        return WalletStorage.getInstance(context.getApplicationContext()).get().size();
    }

    @Override
    public Object getItem(int position) {
        return WalletStorage.getInstance(context.getApplicationContext()).get().get(position);
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
            convertView = View.inflate(context, R.layout.wallet_account_item, null);
            holder.walletImg = (ImageView) convertView.findViewById(R.id.walletImg);
            holder.walletName = (TextView) convertView.findViewById(R.id.walletName);
            holder.walletItemBg = (LinearLayout) convertView.findViewById(R.id.walletItemBg);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        holder.walletImg.setImageResource(Utils.getWalletImg(context,position));
        if (WalletStorage.getInstance(context.getApplicationContext()).get().get(position).isSelect()){
            holder.walletItemBg.setBackgroundColor(context.getResources().getColor(R.color.item_selected));
        }else{
            holder.walletItemBg.setBackgroundColor(context.getResources().getColor(R.color.textColor));
        }
        holder.walletName.setText(WalletStorage.getInstance(context.getApplicationContext()).get().get(position).getWalletName());
        return convertView;
    }

    static class ViewHolder{
        ImageView walletImg;//钱包密码
        TextView walletName;//钱包名称
        LinearLayout walletItemBg;//根布局
    }
}
