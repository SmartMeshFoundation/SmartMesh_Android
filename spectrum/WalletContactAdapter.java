package com.lingtuan.firefly.spectrum;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.spectrum.vo.AddressContactVo;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WalletContactAdapter extends BaseAdapter {

    private Context mContext = null;
    private List<AddressContactVo> source = null;

    public WalletContactAdapter(Context context, List<AddressContactVo> source) {
        this.mContext = context;
        this.source = source;
    }

    @Override
    public int getCount() {
        if (source == null) {
            return 0;
        }
        return source.size();
    }

    @Override
    public Object getItem(int position) {
        return source.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void resetSource(List<AddressContactVo> source) {
        this.source = source;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.wallet_contact_list_item, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        AddressContactVo addressContactVo = source.get(position);
        holder.walletContactName.setText(addressContactVo.getUserName());
        holder.walletContactAddress.setText(addressContactVo.getWalletAddress());
        if (TextUtils.isEmpty(addressContactVo.getRemarks())){
            holder.walletContactNote.setVisibility(View.GONE);
        }else{
            holder.walletContactNote.setVisibility(View.VISIBLE);
            holder.walletContactNote.setText(addressContactVo.getRemarks());
        }
        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.walletContactName)
        TextView walletContactName;
        @BindView(R.id.walletContactAddress)
        TextView walletContactAddress;
        @BindView(R.id.walletContactNote)
        TextView walletContactNote;
        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
