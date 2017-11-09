package com.lingtuan.firefly.wallet;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewPager适配器
 */
public class SlidePagerAdapter extends FragmentPagerAdapter {

    private ArrayList<Fragment> list;
    private List<String> list_Title;     //tab名的列表

    public SlidePagerAdapter(FragmentManager fm, ArrayList<Fragment> list, List<String> list_Title) {
        super(fm);
        this.list = list;
        this.list_Title = list_Title;
    }

    @Override
    public Fragment getItem(int position) {
        return list.get(position);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    //此方法用来显示tab上的名字
    @Override
    public CharSequence getPageTitle(int position) {
        return list_Title.get(position % list_Title.size());
    }

}
