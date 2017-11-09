package com.lingtuan.firefly.wallet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.wallet.util.WalletStorage;


/**
 * Created by cyt on 2017/8/23.
 */

public class ManagerWalletAdapter extends BaseAdapter {

    Context context;
    public ManagerWalletAdapter(Context context)
    {
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
        final ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.wallet_manager_item, null);
            holder = new ViewHolder();
            holder.icon = (ImageView) convertView.findViewById(R.id.icon);
            holder.title = (TextView) convertView.findViewById(R.id.title);
            holder.address = (TextView) convertView.findViewById(R.id.address);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.icon.setImageResource(Utils.getWalletImg(context,position));
        holder.title.setText(WalletStorage.getInstance(context.getApplicationContext()).get().get(position).getWalletName());
        String address = WalletStorage.getInstance(context.getApplicationContext()).get().get(position).getPublicKey();
        if(!address.startsWith("0x"))
        {
            address = "0x"+address;
        }
        holder.address.setText(address);
        return convertView;
    }
    static class ViewHolder {
        private ImageView icon;
        private TextView title;
        private TextView address;
    }
}
