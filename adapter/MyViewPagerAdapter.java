package com.lingtuan.firefly.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lingtuan.meshbox.R;
import com.lingtuan.meshbox.entity.IncomeVo;

import java.util.ArrayList;
import java.util.List;

public class MyViewPagerAdapter extends PagerAdapter {

    private Context context;
    private List<View> views;
    private IncomeVo incomeVo;

    private IncomeOnClickListener incomeOnClickListener;

    public interface IncomeOnClickListener{
        void onClickIncome(int position);
    }



    public MyViewPagerAdapter(Context context,List<View> views, IncomeVo incomeVo,IncomeOnClickListener incomeOnClickListener) {
        this.context = context;
        this.views = views;
        this.incomeVo = incomeVo;
        this.incomeOnClickListener = incomeOnClickListener;
    }

    public void resetSource(List<View> views, IncomeVo incomeVo){
        this.views = views;
        this.incomeVo = incomeVo;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return views.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = views.get(position);
        try {
            TextView totalIncomeTitle = view.findViewById(R.id.totalIncomeTitle);
            TextView todayIncome = view.findViewById(R.id.todayIncome);
            TextView yesterdayIncome = view.findViewById(R.id.yesterdayIncome);
            totalIncomeTitle.setText(context.getString(R.string.home_income_total_title,incomeVo.getData().get(position).getType().toUpperCase()));
            String yesRevenue = incomeVo.getData().get(position).getYesterday_revenue();
            if (TextUtils.isEmpty(yesRevenue)){
                yesterdayIncome.setText(context.getString(R.string.home_income_yesterday_income,"0"));
            }else{
                yesterdayIncome.setText(context.getString(R.string.home_income_yesterday_income,yesRevenue));
            }
            String totalRevenue = incomeVo.getData().get(position).getTotal_revenue();
            if (TextUtils.isEmpty(totalRevenue)){
                todayIncome.setText("0");
            }else{
                todayIncome.setText(incomeVo.getData().get(position).getTotal_revenue());
            }
            container.addView(view);
            view.setOnClickListener(view1 -> {
                if (incomeOnClickListener != null){
                    incomeOnClickListener.onClickIncome(position);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, Object object) {
        container.removeView(views.get(position));
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "标题" + position;
    }

}
