package com.lingtuan.firefly.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.wallet.vo.StorableWallet;

import java.util.List;

/**
 *List adapter bounced
 */
public class DropTextViewAdapter extends BaseAdapter {

    private Context context;
    private List<StorableWallet> list;

    public DropTextViewAdapter(Context context, List<StorableWallet> list){
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(context, R.layout.spinner_list_item, null);
            holder.textView = (TextView) convertView.findViewById(R.id.tv_tinted_spinner);
            holder.imageview = (ImageView) convertView.findViewById(R.id.spinner_img);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.textView.setText(list.get(position).getWalletName());
        if (list.get(position).getImgId() == 0){
            holder.imageview.setImageResource(Utils.getWalletImg(context,position));
        }else{
            holder.imageview.setImageResource(list.get(position).getImgId());
        }
        return convertView;
    }

    static class ViewHolder {
        public TextView textView;
        public ImageView imageview;

    }
}
