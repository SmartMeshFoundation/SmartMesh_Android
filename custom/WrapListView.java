package com.lingtuan.firefly.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class WrapListView extends ListView {
	private int mWidth = 0;//width
	
	public WrapListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public WrapListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	// Rewrite the onMeasure method solving the problem of the default horizontal fill the screen
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		int height = getMeasuredHeight();
		measureChildren(widthMeasureSpec, heightMeasureSpec);
		for(int i=0;i<getChildCount();i++) {
			int childWidth = getChildAt(i).getMeasuredWidth();
			mWidth = Math.max(mWidth, childWidth);
		}
		setMeasuredDimension(mWidth - 50, height);
	}
	
	/**
	 * Set the width, if not set, the default package content
	 * @param width width
	 */
	protected void setListWidth(int width) {
		mWidth = width;
		System.out.println("setWidth");
	}
}
