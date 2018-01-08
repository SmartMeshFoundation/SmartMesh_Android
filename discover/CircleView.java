package com.lingtuan.firefly.discover;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.custom.BitmapFillet;
import com.lingtuan.firefly.custom.CharAvatarView;
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

    // Color Sketchpad Set
    private static final int[] colors = {
            0xff1abc9c, 0xff16a085, 0xfff1c40f, 0xfff39c12, 0xff2ecc71,
            0xff27ae60, 0xffe67e22, 0xffd35400, 0xff3498db, 0xff2980b9,
            0xffe74c3c, 0xffc0392b, 0xff9b59b6, 0xff8e44ad, 0xffbdc3c7,
            0xff34495e, 0xff2c3e50, 0xff95a5a6, 0xff7f8c8d, 0xffec87bf,
            0xffd870ad, 0xfff69785, 0xff9ba37e, 0xffb49255, 0xffb49255, 0xffa94136
    };

    private Paint mPaintBackground;

    private String text;
    private int charHash;
    private Paint mPaintText;
    private Rect mRect;

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

        mPaintBackground = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRect = new Rect();
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
        }else{
            if (!TextUtils.isEmpty(text)){
                int color = colors[charHash % colors.length];
                // Draw a circle
                mPaintBackground.setColor(color);
                canvas.drawCircle(radius, radius, radius, mPaintBackground);
                // Write
                mPaintText.setColor(Color.WHITE);
                mPaintText.setTextSize(getWidth() / 2);
                mPaintText.setStrokeWidth(3);
                mPaintText.getTextBounds(text, 0, 1, mRect);
                // Center vertically
                Paint.FontMetricsInt fontMetrics = mPaintText.getFontMetricsInt();
                int baseline = (getMeasuredHeight() - fontMetrics.bottom - fontMetrics.top) / 2;
                // Center around
                mPaintText.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(text, getWidth() / 2, baseline, mPaintText);
            }

        }
    }

    public void setPortraitIcon(String path,String content) {
        if (TextUtils.isEmpty(content)) {
            content=" ";
        }
        this.text = String.valueOf(content.toCharArray()[0]);
        this.text = text.toUpperCase();
        charHash = this.text.hashCode();

        if (!TextUtils.isEmpty(path) && new File(path).exists()) {
            mBitmap = BitmapUtils.getimage(path);
        }
        invalidate();
    }

}
