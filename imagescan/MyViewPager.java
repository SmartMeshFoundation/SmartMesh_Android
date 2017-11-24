package com.lingtuan.firefly.imagescan;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * To prevent ...
 */
public class MyViewPager extends ViewPager {

	public MyViewPager(Context context) {
		super(context);
	}

	public MyViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		try {
			return super.onInterceptTouchEvent(ev);
		} catch (IllegalArgumentException e) {
		} catch (ArrayIndexOutOfBoundsException e) {

		}
		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		try {
			return super.onTouchEvent(ev);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

}
