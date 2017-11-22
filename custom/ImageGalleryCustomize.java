package com.lingtuan.firefly.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class ImageGalleryCustomize extends ImageView {

	public ImageGalleryCustomize(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = width;
		setMeasuredDimension(width, height);
	}

}
