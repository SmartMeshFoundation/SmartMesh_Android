package com.lingtuan.firefly.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created  on 16/9/14.
 */
public class CharAvatarView extends ImageView {
    private static final String TAG = CharAvatarView.class.getSimpleName();
    // Color Sketchpad Set
    private static final int[] colors = {
        0xff1abc9c, 0xff16a085, 0xfff1c40f, 0xfff39c12, 0xff2ecc71,
        0xff27ae60, 0xffe67e22, 0xffd35400, 0xff3498db, 0xff2980b9,
        0xffe74c3c, 0xffc0392b, 0xff9b59b6, 0xff8e44ad, 0xffbdc3c7,
        0xff34495e, 0xff2c3e50, 0xff95a5a6, 0xff7f8c8d, 0xffec87bf,
        0xffd870ad, 0xfff69785, 0xff9ba37e, 0xffb49255, 0xffb49255, 0xffa94136
    };

    private Paint mPaintBackground;
    private Paint mPaintText;
    private Rect mRect;

    private String text;

    private int charHash;

    public CharAvatarView(Context context) {
        this(context, null);
    }

    public CharAvatarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CharAvatarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaintBackground = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRect = new Rect();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec); // The same width and height
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(text!= null && text.trim().length()<=0){
            mPaintBackground.setColor(getResources().getColor(R.color.yellowPrimary));
            canvas.drawCircle(getWidth() / 2, getWidth() / 2, getWidth() / 2, mPaintBackground);
        }else if (null != text) {
            int color = colors[charHash % colors.length];
            // Draw a circle
            mPaintBackground.setColor(color);
            canvas.drawCircle(getWidth() / 2, getWidth() / 2, getWidth() / 2, mPaintBackground);
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
        }else{
            mPaintBackground.setColor(getResources().getColor(R.color.yellowPrimary));
            canvas.drawCircle(getWidth() / 2, getWidth() / 2, getWidth() / 2, mPaintBackground);
        }
        super.onDraw(canvas);
    }

    /**
     * @param content Incoming character content
     * Only take the first character of the content, if the letter is converted to uppercase
     */
    public void setText(String content,ImageView userImg,String pic) {
        if (TextUtils.isEmpty(content)) {
            content=" ";
        }
        this.text = String.valueOf(content.toCharArray()[0]);
        this.text = text.toUpperCase();
        charHash = this.text.hashCode();
        // Redraw
        invalidate();
        if (!TextUtils.isEmpty(pic) && !pic.contains("avatar/0/0_XXXXXX.png")){
            NextApplication.displayCircleImage(userImg,pic);
        }else{
            userImg.setImageResource(R.drawable.nothing);
        }
    }


}
