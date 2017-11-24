package com.lingtuan.firefly.discover;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.custom.BitmapFillet;
import com.lingtuan.firefly.util.BitmapUtils;
import com.lingtuan.firefly.util.Utils;

import java.io.File;


/**
 * Draw the small round
 */
public class CircleView extends View {
    private Paint mPaint;
    private Bitmap mBitmap;
    private float radius = Utils.dip2px(getContext(),20);//The radius of

    public CircleView(Context context) {
        this(context, null);
    }

    public CircleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setColor(getResources().getColor(R.color.colorPrimary));
        mPaint.setAntiAlias(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(measureSize(widthMeasureSpec), measureSize(heightMeasureSpec));
    }

    private int measureSize(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = Utils.dip2px(getContext(),40);
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(radius, radius, radius, mPaint);
        if (mBitmap != null) {
            Bitmap bitmap = BitmapFillet.fillet(BitmapFillet.ROUND, mBitmap, 0);
            canvas.drawBitmap(bitmap, null, new Rect(0, 0, 2 * (int) radius, 2 * (int) radius), mPaint);
        }
    }

    public void setPortraitIcon(String path) {
        if (!TextUtils.isEmpty(path) && new File(path).exists()) {
            mBitmap = BitmapUtils.getimage(path);
            if (mBitmap == null){
                mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon_default_avater);
            }
        }else{
            mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon_default_avater);
        }
        invalidate();
    }

}
