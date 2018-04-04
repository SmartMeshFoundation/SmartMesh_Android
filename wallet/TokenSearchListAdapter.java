package com.lingtuan.firefly.wallet;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.custom.switchbutton.SwitchButton;
import com.lingtuan.firefly.wallet.vo.TokenVo;

import java.util.ArrayList;

/**
 * Created on 2018/3/19.
 */

public class TokenSearchListAdapter extends BaseAdapter {

    private Context context;
    private AddTokenToList addTokenToList;
    private ArrayList<TokenVo> tokenVos;

    public TokenSearchListAdapter(Context context, ArrayList<TokenVo> tokenVos,AddTokenToList addTokenToList){
        this.context = context;
        this.tokenVos = tokenVos;
        this.addTokenToList = addTokenToList;
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null){
            holder = new ViewHolder();
            convertView = View.inflate(context, R.layout.token_search_list_item_layout,null);
            holder.tokenImg = (ImageView) convertView.findViewById(R.id.tokenImg);
            holder.tokenName = (TextView) convertView.findViewById(R.id.tokenName);
            holder.tokenSymbol = (TextView) convertView.findViewById(R.id.tokenSymbol);
            holder.tokenHasAdd = (ImageView) convertView.findViewById(R.id.tokenHasAdd);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        final TokenVo tokenVo = tokenVos.get(position);
        if (tokenVo.isChecked()){
            holder.tokenHasAdd.setImageResource(R.drawable.icon_has_add_token);
        }else{
            holder.tokenHasAdd.setImageResource(R.drawable.icon_add_token);
        }

        holder.tokenSymbol.setText(tokenVo.getTokenSymbol());
        holder.tokenName.setText(tokenVo.getTokenName());
        NextApplication.displayCircleImage(holder.tokenImg,tokenVo.getTokenLogo());

        if (tokenVo.isFixed()){
            holder.tokenHasAdd.setVisibility(View.GONE);
        }else{
            holder.tokenHasAdd.setVisibility(View.VISIBLE);
        }

        holder.tokenHasAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addTokenToList != null && !tokenVo.isChecked()){
                    addTokenToList.addTokenToList(position);
                }
            }
        });

        return convertView;
    }

    public interface AddTokenToList{
        void addTokenToList(int position);
    }

    static class ViewHolder{
        private ImageView tokenImg;
        private TextView tokenName;
        private TextView tokenSymbol;
        private ImageView tokenHasAdd;
    }
}
