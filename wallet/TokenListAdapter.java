package com.lingtuan.firefly.wallet;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.custom.SwitchButton;
import com.lingtuan.firefly.wallet.vo.TokenVo;

import java.util.ArrayList;

/**
 * Created on 2018/3/19.
 */

public class TokenListAdapter extends BaseAdapter {

    private Context context;

    private ArrayList<TokenVo> tokenVos;

    public TokenListAdapter(Context context,ArrayList<TokenVo> tokenVos){
        this.context = context;
        this.tokenVos = tokenVos;
    }

    public void resetSource(ArrayList<TokenVo> tokenVos){
        this.tokenVos = tokenVos;
        notifyDataSetChanged();
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

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null){
            holder = new ViewHolder();
            convertView = View.inflate(context, R.layout.token_list_item_layout,null);
            holder.tokenImg = (ImageView) convertView.findViewById(R.id.tokenImg);
            holder.tokenName = (TextView) convertView.findViewById(R.id.tokenName);
            holder.tokenInfo = (TextView) convertView.findViewById(R.id.tokenInfo);
            holder.tokenHasCheck = (SwitchButton) convertView.findViewById(R.id.tokenHasCheck);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        return convertView;
    }

    static class ViewHolder{
        private ImageView tokenImg;
        private TextView tokenName;
        private TextView tokenInfo;
        private SwitchButton tokenHasCheck;
    }
}
