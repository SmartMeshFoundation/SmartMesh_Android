package com.lingtuan.firefly.chat.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.lingtuan.firefly.base.BaseFragment;

import java.util.ArrayList;

/**
 * Created  on 2016/1/27.
 * Description: Group, group chat chat, one on one page, without a web page, such as the bottom of the Dialog in the Pager adapter
 */
public class BottomMorePagerAdapter extends FragmentPagerAdapter {

    private ArrayList<BaseFragment> mList = null ;

    public BottomMorePagerAdapter(FragmentManager fm) {
        super(fm);
    }



    @Override
    public Fragment getItem(int position) {
        if(position >= mList.size()){
            return mList.get(mList.size() -1);
        }

        return mList.get(position);
    }

    @Override
    public int getCount() {
        return mList == null || mList.isEmpty() ? 0 : mList.size();
    }

}
