package com.lingtuan.firefly.discover;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;

import com.lingtuan.firefly.R;


/**
 * A circle
 */
public class RadarView extends View {
    private Paint mPaintLine;//Circle line need to use paint
    private Paint mPaintDottedLine;//Draw a dotted line need to use paint
    private Paint mPaintCircle;//Draw circles need to use paint
    private Paint mPaintDotCircle;//Draw a solid circle need paint
    private Paint mPaintScan;//Scan need to use paint

    private int mWidth, mHeight;//Length and width of the graphics

    private Matrix matrix = new Matrix();//Need rotation matrix
    private int scanAngle;//The scan Angle rotation
    private Shader scanShader;//Scanning rendering shader


    //The percentage of each circle
    private static float[] circleProportion = {1 / 13f, 2 / 13f, 3 / 13f, 4 / 13f, 5 / 13f, 6 / 13f};
    private int scanSpeed = 5;

    private int currentScanningCount;//The number of times the current scan
    private int currentScanningItem;//The current scan display item
    private int maxScanItemCount;//Maximum number of scanning
    private boolean startScan = false;//Only set up after the data will start scanning
    private IScanningListener iScanningListener;//When scanning to monitor callback interface

    public void setScanningListener(IScanningListener iScanningListener) {
        this.iScanningListener = iScanningListener;
    }

    private Runnable run = new Runnable() {
        @Override
        public void run() {
            scanAngle = (scanAngle + scanSpeed) % 360;
            matrix.postRotate(scanSpeed, mWidth / 2, mHeight / 2);
            invalidate();
            postDelayed(run, 130);
            //Start scanning display signs to true and only scan a circle
            if (startScan && currentScanningCount <= (360 / scanSpeed)) {
                if (iScanningListener != null && currentScanningCount % scanSpeed == 0 && currentScanningItem < maxScanItemCount) {
                    iScanningListener.onScanning(currentScanningItem, scanAngle);
                    currentScanningItem++;
                } else if (iScanningListener != null && currentScanningItem == maxScanItemCount) {
                    iScanningListener.onScanSuccess();
                }
                currentScanningCount++;
            }
        }
    };

    public RadarView(Context context) {
        this(context, null);
    }

    public RadarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RadarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        post(run);
    }


    private void init() {
        mPaintLine = new Paint();
        mPaintLine.setColor(getResources().getColor(R.color.yellowPrimary));
        mPaintLine.setAntiAlias(true);
        mPaintLine.setStrokeWidth(2);
        mPaintLine.setStyle(Paint.Style.STROKE);

        mPaintDottedLine = new Paint();
        mPaintDottedLine.setColor(getResources().getColor(R.color.tab_sep_normal));
        mPaintDottedLine.setAntiAlias(true);
        mPaintDottedLine.setStrokeWidth(1);
        mPaintDottedLine.setStyle(Paint.Style.STROKE);
        mPaintDottedLine.setPathEffect(new DashPathEffect(new float[] {4,4}, 0));

        mPaintCircle = new Paint();
        mPaintCircle.setColor(Color.WHITE);
        mPaintCircle.setAntiAlias(true);

        mPaintDotCircle = new Paint();
        mPaintDotCircle.setColor(getResources().getColor(R.color.yellowPrimary));
        mPaintDotCircle.setStyle(Paint.Style.FILL);
        mPaintDotCircle.setAntiAlias(true);

        mPaintScan = new Paint();
        mPaintScan.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureSize(widthMeasureSpec), measureSize(widthMeasureSpec));
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        mWidth = mHeight = Math.min(mWidth, mHeight);
        //Set the scan rendering of the shader
        scanShader = new SweepGradient(mWidth / 2, mHeight / 2,new int[]{Color.TRANSPARENT, getResources().getColor(R.color.yellowPrimary)}, null);
    }

    private int measureSize(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = 300;
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawCircle(canvas);
        drawLine(canvas);
        drawDotCircle(canvas);
        drawScan(canvas);
    }

    /**
     * Draw a dotted line
     * */
    private void drawLine(Canvas canvas) {

        //Horizontal line
        Path path = new Path();
        path.moveTo(mWidth/2 - mWidth*circleProportion[5],mHeight / 2);
        path.lineTo(mWidth/2 + mWidth*circleProportion[5],mHeight / 2);
        canvas.drawPath(path,mPaintDottedLine);

        //vertical line
        path.moveTo(mWidth/2, mHeight / 2 - mHeight*circleProportion[5]);
        path.lineTo(mWidth/2 ,mHeight / 2 + mHeight*circleProportion[5]);
        canvas.drawPath(path,mPaintDottedLine);

        //Slash the x axis coordinates
        float tempX = mWidth*circleProportion[5]*(float)Math.sin(Math.toRadians(45));
        //Slash y coordinates
        float tempY = mHeight*circleProportion[5]*(float)Math.sin(Math.toRadians(45));

        //To the lower right 45-degree diagonal lines
        path.moveTo(mWidth/2 - tempX , mHeight / 2 - tempY);
        path.lineTo(mWidth/2 + tempX ,mHeight / 2 + tempY);
        canvas.drawPath(path,mPaintDottedLine);

        //45-degree diagonal lines on the left
        path.moveTo(mWidth/2 - tempX , mHeight / 2 + tempY);
        path.lineTo(mWidth/2 + tempX ,mHeight / 2 - tempY);
        canvas.drawPath(path,mPaintDottedLine);

    }

    /**
     * 绘制圆线圈
     *
     * @param canvas
     */
    private void drawCircle(Canvas canvas) {
        canvas.drawCircle(mWidth / 2, mHeight / 2, mWidth * circleProportion[1], mPaintLine); // Draw the small circle
        canvas.drawCircle(mWidth / 2, mHeight / 2, mWidth * circleProportion[3], mPaintLine); // Draw the middle circle
        canvas.drawCircle(mWidth / 2, mHeight / 2, mWidth * circleProportion[5], mPaintLine); // Draw the big circle
    }

    private void drawDotCircle(Canvas canvas){
        canvas.drawCircle(mWidth / 2, mHeight / 2, mWidth * circleProportion[0] * 0.3f, mPaintDotCircle); // Draw the small circle
    }



    /**
     * Map scanning
     *
     * @param canvas
     */
    private void drawScan(Canvas canvas) {
        canvas.save();
        mPaintScan.setShader(scanShader);
        canvas.concat(matrix);
        canvas.drawCircle(mWidth / 2, mHeight / 2, mWidth * circleProportion[5], mPaintScan);
        canvas.restore();
    }



    public interface IScanningListener {
        //When is scanning (haven't scan to complete at this time)
        void onScanning(int position, float scanAngle);

        //Scan the success callback
        void onScanSuccess();
    }

    public void setMaxScanItemCount(int maxScanItemCount) {
        this.maxScanItemCount = maxScanItemCount;
    }

    /**
     * Start scanning
     */
    public void startScan() {
        this.startScan = true;
    }

    /**
    * reset data
    */
    public void resetData(){
        scanAngle = 0;
        currentScanningCount = 0;
        currentScanningItem = 0;
        this.startScan = true;
    }
}
