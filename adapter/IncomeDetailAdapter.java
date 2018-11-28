package com.lingtuan.firefly.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.util.TimeUtils;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingtuan.meshbox.R;
import com.lingtuan.meshbox.entity.IncomeDetailVo;
import com.lingtuan.meshbox.utils.DateUtil;

import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.zip.Inflater;

public class IncomeDetailAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<IncomeDetailVo.DataBean> dataBeans;

    public IncomeDetailAdapter(Context context,ArrayList<IncomeDetailVo.DataBean> dataBeans){
        this.context = context;
        this.dataBeans = dataBeans;
    }

    public void resetSource(ArrayList<IncomeDetailVo.DataBean> dataBeans){
        this.dataBeans = dataBeans;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (dataBeans!= null){
            return dataBeans.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int i) {
        if (dataBeans != null){
            return dataBeans.get(i);
        }
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null){
            holder = new ViewHolder();
            view = View.inflate(context,R.layout.item_income_details,null);
            holder.incomeTime = view.findViewById(R.id.incomeTime);
            holder.incomeLine = view.findViewById(R.id.incomeLine);
            holder.incomeBottomLine = view.findViewById(R.id.incomeBottomLine);
            holder.incomeImage = view.findViewById(R.id.incomeImage);
            holder.incomeContent = view.findViewById(R.id.incomeContent);
            view.setTag(holder);
        }else{
            holder = (ViewHolder) view.getTag();
        }
        IncomeDetailVo.DataBean dataBean =  dataBeans.get(i);
        holder.incomeTime.setText(DateUtil.getMMdd(dataBean.getDate() * 1000));
        String content = context.getString(R.string.income_detail_content,dataBean.getAction(),dataBean.getSource(),dataBean.getRevenue(),dataBean.getType().toUpperCase());
        SpannableStringBuilder style=new SpannableStringBuilder(content);
        int start = dataBean.getAction().length() + dataBean.getAction().length() + 2;
        int end = start + dataBean.getRevenue().length() + 1;
        style.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.text_color_3F7AE0)),start,end,Spannable.SPAN_EXCLUSIVE_INCLUSIVE);     //设置指定位置文字的颜色
        style.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start,end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE); //粗体
        style.setSpan(new RelativeSizeSpan(1.1f), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE); //1.1f表示默认字体大小的1.1倍
        style.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.text_color_323232)),0,dataBean.getAction().length(),Spannable.SPAN_EXCLUSIVE_INCLUSIVE);     //设置指定位置文字的颜色
        style.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0,dataBean.getAction().length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE); //粗体
        holder.incomeContent.setText(style);
        if (i == 0){
            holder.incomeLine.setVisibility(View.INVISIBLE);
            holder.incomeImage.setImageResource(R.mipmap.icon_income_checked);
            holder.incomeTime.setTextColor(context.getResources().getColor(R.color.text_color_3F7AE0));
            holder.incomeTime.setVisibility(View.VISIBLE);
            holder.incomeImage.setVisibility(View.VISIBLE);
        }else{
            holder.incomeLine.setVisibility(View.VISIBLE);
            holder.incomeImage.setImageResource(R.mipmap.icon_income_unchecked);
            holder.incomeTime.setTextColor(context.getResources().getColor(R.color.text_color_999999));
            if (dataBeans.get(i).getDate() == dataBeans.get(i - 1).getDate()){
                holder.incomeTime.setVisibility(View.INVISIBLE);
                holder.incomeImage.setVisibility(View.GONE);
            }else{
                holder.incomeTime.setVisibility(View.VISIBLE);
                holder.incomeImage.setVisibility(View.VISIBLE);
            }
        }

        if (i == dataBeans.size() - 1){
            holder.incomeBottomLine.setVisibility(View.INVISIBLE);
        }else{
            holder.incomeBottomLine.setVisibility(View.VISIBLE);
        }
        return view;
    }

    static class ViewHolder{
        TextView incomeTime;
        View incomeLine;
        View incomeBottomLine;
        ImageView incomeImage;
        TextView incomeContent;
    }
}
