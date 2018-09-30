package com.lingtuan.firefly.spectrum;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingtuan.firefly.R;

import java.util.List;

/**
 *列表适配  弹框
 */
public class WalletSendAddressAdapter extends BaseAdapter {

    private Context context;
    private List<String> list;

    private SetClickAddress setClickAddress;

    public interface SetClickAddress{
        void deleteWalletAddress(int position);
    }


    public WalletSendAddressAdapter(Context context, List<String> list,SetClickAddress setClickAddressListener){
        this.context = context;
        this.list = list;
        this.setClickAddress = setClickAddressListener;
    }

    public void resetSource(List<String> source){
        list = source;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (list == null){
            return 0;
        }
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = View.inflate(context, R.layout.spinner_grid_item, null);
            viewHolder.walletAddress = convertView.findViewById(R.id.walletAddress);
            viewHolder.walletDelete = convertView.findViewById(R.id.walletDelete);
            viewHolder.bottomLine = convertView.findViewById(R.id.bottomLine);
            viewHolder.bottomShadow = convertView.findViewById(R.id.bottomShadow);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = ((ViewHolder) convertView.getTag());
        }
        viewHolder.walletAddress.setText(list.get(position));

        if (list.size() >= 3){
            if (position >= 2){
                viewHolder.bottomShadow.setVisibility(View.VISIBLE);
                viewHolder.bottomLine.setVisibility(View.GONE);
            }else{
                viewHolder.bottomShadow.setVisibility(View.GONE);
                viewHolder.bottomLine.setVisibility(View.VISIBLE);
            }
        }else if (list.size() == 2){
            if (position >= 1){
                viewHolder.bottomShadow.setVisibility(View.VISIBLE);
                viewHolder.bottomLine.setVisibility(View.GONE);
            }else{
                viewHolder.bottomShadow.setVisibility(View.GONE);
                viewHolder.bottomLine.setVisibility(View.VISIBLE);
            }
        }else{
            viewHolder.bottomShadow.setVisibility(View.VISIBLE);
            viewHolder.bottomLine.setVisibility(View.GONE);
        }

        viewHolder.walletDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (setClickAddress != null){
                    setClickAddress.deleteWalletAddress(position);
                }
            }
        });
        return convertView;
    }

    static class ViewHolder {
        public TextView walletAddress;
        public ImageView walletDelete;
        public View bottomLine;
        public View bottomShadow;
    }
}
