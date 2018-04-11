package com.lingtuan.firefly.wallet;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.custom.SwitchButton;
import com.lingtuan.firefly.custom.SwitchView;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.offline.AppNetService;
import com.lingtuan.firefly.setting.SettingUI;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.wallet.vo.TokenVo;

import java.util.ArrayList;

/**
 * Created on 2018/3/19.
 */

public class TokenListAdapter extends BaseAdapter {

    private Context context;
    private String address;
    private ArrayList<TokenVo> tokenVos;

    public TokenListAdapter(Context context,ArrayList<TokenVo> tokenVos,String address){
        this.context = context;
        this.address = address;
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
        final ViewHolder holder;
        if (convertView == null){
            holder = new ViewHolder();
            convertView = View.inflate(context, R.layout.token_list_item_layout,null);
            holder.tokenImg = (ImageView) convertView.findViewById(R.id.tokenImg);
            holder.tokenName = (TextView) convertView.findViewById(R.id.tokenName);
            holder.tokenSymbol = (TextView) convertView.findViewById(R.id.tokenSymbol);
            holder.tokenHasCheck = (SwitchView) convertView.findViewById(R.id.tokenHasCheck);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        final TokenVo tokenVo = tokenVos.get(position);
        holder.tokenSymbol.setText(tokenVo.getTokenSymbol());
        holder.tokenName.setText(tokenVo.getTokenName());
        NextApplication.displayCircleToken(holder.tokenImg,tokenVo.getTokenLogo());
        if (tokenVo.isFixed()){
            holder.tokenHasCheck.setVisibility(View.GONE);
        }else{
            holder.tokenHasCheck.setVisibility(View.VISIBLE);
        }
        if (tokenVo.isChecked()){
            holder.tokenHasCheck.setOpened(true);
        }else{
            holder.tokenHasCheck.setOpened(false);
        }

        holder.tokenHasCheck.setOnStateChangedListener(new SwitchView.OnStateChangedListener() {
            @Override
            public void toggleToOn(SwitchView view) {
                holder.tokenHasCheck.setOpened(true);
                tokenVo.setChecked(true);
                FinalUserDataBase.getInstance().updateTokenList(tokenVo,address,false);
                Utils.sendBroadcastReceiver(context,  new Intent(Constants.WALLET_BIND_TOKEN), false);
            }

            @Override
            public void toggleToOff(SwitchView view) {
                holder.tokenHasCheck.setOpened(false);
                tokenVo.setChecked(false);
                FinalUserDataBase.getInstance().updateTokenList(tokenVo,address,false);
                Utils.sendBroadcastReceiver(context,  new Intent(Constants.WALLET_BIND_TOKEN), false);
            }
        });
        return convertView;
    }

    static class ViewHolder{
        private ImageView tokenImg;
        private TextView tokenName;
        private TextView tokenSymbol;
        private SwitchView tokenHasCheck;
    }
}
