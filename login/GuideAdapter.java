package com.lingtuan.firefly.login;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.lingtuan.firefly.R;


/**
 * @description TODO
 */
public class GuideAdapter extends PagerAdapter {

    protected final int[] IMAGE = new int[]{R.drawable.icon_static_001, R.drawable.icon_static_002, R.drawable.icon_static_003, R.drawable.icon_static_004};

    private Context context;

    public GuideAdapter(Context context){
        this.context = context;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.guid_item, null);
        ImageView ivPortrait = (ImageView) view.findViewById(R.id.guid_item);
        ivPortrait.setImageResource(IMAGE[position]);
        container.addView(view);
        return view;
    }


    @Override
    public int getCount() {
        return IMAGE.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        View view = (View) object;
        container.removeView(view);
    }
}