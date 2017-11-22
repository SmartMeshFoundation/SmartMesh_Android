package com.lingtuan.firefly.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Cutting border
 */
public class ClipView extends View {
	
	/**
	 * Border around from boundary distance, is used to adjust frame length
	 */
	public static final int BORDERDISTANCE = 50;

	private Paint mPaint;
	
	public ClipView(Context context) {
		this(context, null);
	}

	public ClipView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ClipView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mPaint = new Paint();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int width = this.getWidth();
		int height = this.getHeight();

		// Border length, according to the screen edge around 50 px
		int borderlength = width - BORDERDISTANCE *2;
				
		mPaint.setColor(0xaa000000);

		// The following paint transparent grey areas
		// top
		canvas.drawRect(0, 0, width, (height - borderlength) / 2, mPaint);
		// bottom
		canvas.drawRect(0, (height + borderlength) / 2, width, height, mPaint);
		// left
		canvas.drawRect(0, (height - borderlength) / 2, BORDERDISTANCE,
				(height + borderlength) / 2, mPaint);
		// right
		canvas.drawRect(borderlength + BORDERDISTANCE, (height - borderlength) / 2, width,
				(height + borderlength) / 2, mPaint);
		
		// The following drawing border lines
		mPaint.setColor(Color.WHITE);
		mPaint.setStrokeWidth(2.0f);
		// top
		canvas.drawLine(BORDERDISTANCE, (height - borderlength) / 2, width - BORDERDISTANCE, (height - borderlength) / 2, mPaint);
		// bottom
		canvas.drawLine(BORDERDISTANCE, (height + borderlength) / 2, width - BORDERDISTANCE, (height + borderlength) / 2, mPaint);
		// left
		canvas.drawLine(BORDERDISTANCE, (height - borderlength) / 2, BORDERDISTANCE, (height + borderlength) / 2, mPaint);
		// right
		canvas.drawLine(width - BORDERDISTANCE, (height - borderlength) / 2, width - BORDERDISTANCE, (height + borderlength) / 2, mPaint);
	}

}
