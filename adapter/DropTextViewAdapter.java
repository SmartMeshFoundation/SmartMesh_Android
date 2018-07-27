package com.lingtuan.firefly.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.quickmark.QuickMarkShowUI;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.wallet.vo.StorableWallet;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

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
            convertView = View.inflate(context, R.layout.spinner_list_item, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.textView.setText(list.get(position).getWalletName());
        if (TextUtils.isEmpty(list.get(position).getWalletImageId()) || !list.get(position).getWalletImageId().startsWith("icon_static_")){
            holder.imageview.setImageResource(Utils.getWalletImageId(context,Utils.getWalletImg(context,position)));
        }else{
            holder.imageview.setImageResource(Utils.getWalletImageId(context,list.get(position).getWalletImageId()));
        }
        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.tv_tinted_spinner)
        TextView textView;
        @BindView(R.id.spinner_img)
        ImageView imageview;
        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }

    }
}
