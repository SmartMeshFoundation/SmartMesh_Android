package com.lingtuan.firefly.chat.audio;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;

import java.util.LinkedList;
import java.util.List;

/**
 * Voice recording of animation effects
 * Created on 17-6-12.
 */
public class LineWaveVoiceView extends View {
    private static final String TAG = LineWaveVoiceView.class.getSimpleName();

    private ILineWaveVoiceListener lineWaveVoiceListener;

    private Paint paint;
    //Rectangular corrugated color
    private int lineColor;
    //Rectangular corrugated width
    private float lineWidth;
    private float textSize;
    private String text = "0";
    private int textColor;
    private boolean isStart = false;
    private Runnable mRunable;

    private int LINE_W = 9;//By default, the width of the rectangular corrugated 9 pixels, in principle from layout attr
    private int MIN_WAVE_H = 2;//The smallest rectangle line is high, is twice the line width, line width from our lineWidth
    private int MAX_WAVE_H = 7;//The highest peak, four times as great as that of line width

    //By default the height of the rectangular corrugated, a total of 10 rectangular, each have about 10
    private int[] DEFAULT_WAVE_HEIGHT = {2, 3, 4, 3, 2, 2, 2, 2, 2, 2};
    private LinkedList<Integer> mWaveList = new LinkedList<>();

    private RectF rectRight = new RectF();//On the right side of the corrugated rectangular data, 10 a rectF rectangular reuse
    private RectF rectLeft = new RectF();//Corrugated rectangular data on the left

    LinkedList<Integer> list = new LinkedList<>();

    private static final int UPDATE_INTERVAL_TIME = 100;//Updated once 100 ms

    public LineWaveVoiceView(Context context) {
        super(context);
    }

    public LineWaveVoiceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LineWaveVoiceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint = new Paint();
        resetList(list, DEFAULT_WAVE_HEIGHT);

        mRunable = new Runnable() {
            @Override
            public void run() {
                while (isStart){
                    refreshElement();
                    try {
                        Thread.sleep(UPDATE_INTERVAL_TIME);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    postInvalidate();
                }
            }
        };
        TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.LineWaveVoiceView);
        lineColor = mTypedArray.getColor(R.styleable.LineWaveVoiceView_voiceLineColor, Color.parseColor("#ff9c00"));
        lineWidth = mTypedArray.getDimension(R.styleable.LineWaveVoiceView_voiceLineWidth, LINE_W);
        textSize = mTypedArray.getDimension(R.styleable.LineWaveVoiceView_voiceTextSize, 42);
        textColor = mTypedArray.getColor(R.styleable.LineWaveVoiceView_voiceTextColor, Color.parseColor("#666666"));
        mTypedArray.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int widthcentre = getWidth() / 2;
        int heightcentre = getHeight() / 2;

        //Update time
        paint.setStrokeWidth(0);
        paint.setColor(textColor);
        paint.setTextSize(textSize);
        float textWidth = paint.measureText(text);
        canvas.drawText(text, widthcentre - textWidth / 2, heightcentre - (paint.ascent() + paint.descent())/2, paint);

        //Update the two sides of the corrugated rectangle
        paint.setColor(lineColor);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(lineWidth);
        paint.setAntiAlias(true);
        for(int i = 0 ; i < 10 ; i++) {
            //On the right side of the rectangle
            rectRight.left = widthcentre + 2 * i * lineWidth + textWidth / 2 + lineWidth;
            rectRight.top = heightcentre - list.get(i) * lineWidth / 2;
            rectRight.right = widthcentre  + 2 * i * lineWidth +2 * lineWidth + textWidth / 2;
            rectRight.bottom = heightcentre + list.get(i) * lineWidth /2;

            //On the left side of the rectangle
            rectLeft.left = widthcentre - (2 * i * lineWidth +  textWidth / 2 + 2 * lineWidth );
            rectLeft.top = heightcentre - list.get(i) * lineWidth / 2;
            rectLeft.right = widthcentre  -( 2 * i * lineWidth + textWidth / 2 + lineWidth);
            rectLeft.bottom = heightcentre + list.get(i) * lineWidth / 2;

            canvas.drawRoundRect(rectRight, 6, 6, paint);
            canvas.drawRoundRect(rectLeft, 6, 6, paint);
        }
    }

    private synchronized void refreshElement() {
        float maxAmp = getMaxAmplitude();
        int waveH = MIN_WAVE_H + Math.round(maxAmp * (MAX_WAVE_H -2));//wave 在 2 ~ 7 之间
        list.add(0, waveH);
        list.removeLast();
    }

    /**
     * Get the volume of the recording, the range of 0-32767, normalized to 0 ~ 1
     * @return
     */
    public float getMaxAmplitude() {
        if (lineWaveVoiceListener != null){
            return  lineWaveVoiceListener.getMaxAmplitude();
        }
        return 0;
    }

    public void setLineWaveVoiceListener(ILineWaveVoiceListener lineWaveVoiceListener){
        this.lineWaveVoiceListener = lineWaveVoiceListener;
    }

    public interface ILineWaveVoiceListener {
        float getMaxAmplitude();
    }

    public synchronized void setText(String text) {
        this.text = text;
        postInvalidate();
    }

    public synchronized void startRecord(){
        isStart = true;
        ThreadPool.getInstance().getCachedPools().execute(mRunable);
    }

    public synchronized void stopRecord(){
        isStart = false;
        mWaveList.clear();
        resetList(list, DEFAULT_WAVE_HEIGHT);
        text = NextApplication.mContext.getString(R.string.chatting_audio_last_say);
        postInvalidate();
    }

    private void resetList(List list, int[] array) {
        list.clear();
        for(int i = 0 ; i < array.length; i++ ){
            list.add(array[i]);
        }
    }
}