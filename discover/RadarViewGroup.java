package com.lingtuan.firefly.discover;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.offline.vo.WifiPeopleVO;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.vo.UserInfoVo;

import java.util.ArrayList;


/**
 * Radar control
 */
public class RadarViewGroup extends ViewGroup implements RadarView.IScanningListener {
    //The viewgroup wide high
    private int mWidth, mHeight;
    //Records show the item of scan Angle position
    private SparseArray<Float> scanAngleList = new SparseArray<>();
    //The data source
    private ArrayList<WifiPeopleVO> mDatas;
    //The length of the data source
    private int dataLength;
    //The positions of the minimum distance of the item in the data source
    private int minItemPosition;
    //The current display of the item
    private CircleView currentShowChild;
    //The minimum distance of the item
    private CircleView minShowChild;
    //In the radar map click callback interface to monitor CircleView dots
    private IRadarClickListener iRadarClickListener;

    //The percentage of each circle
    private static float[] circleProportion = {1 / 13f, 2 / 13f, 3 / 13f, 4 / 13f, 5 / 13f, 6 / 13f};
    //Whether to reload the data
    private boolean resetData;

    public void setiRadarClickListener(IRadarClickListener iRadarClickListener) {
        this.iRadarClickListener = iRadarClickListener;
    }

    public RadarViewGroup(Context context) {
        this(context, null);
    }

    public RadarViewGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RadarViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureSize(widthMeasureSpec), measureSize(heightMeasureSpec));
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        mWidth = mHeight = Math.min(mWidth, mHeight);
        //Measure each of the children
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getId() == R.id.id_scan_circle) {
                //For the radar scan set required properties
                ((RadarView) child).setScanningListener(this);
                //Before considering the data is not add scan the scan, but not for CircleView layout
                if (mDatas != null && mDatas.size() > 0) {
                    ((RadarView) child).setMaxScanItemCount(mDatas.size());
                    ((RadarView) child).startScan();
                }
                continue;
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();
        //First place radar scan
        View view = findViewById(R.id.id_scan_circle);
        if (view != null) {
            view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        }

        //Need to show the item dot placed entirely
        for (int i = 0; i < childCount; i++) {
            final int j = i;
            final View child = getChildAt(i);
            if (child.getId() == R.id.id_scan_circle) {
                //Is RadarView
                if (resetData){
                    if (mDatas != null && mDatas.size() > 0) {
                        ((RadarView) child).setMaxScanItemCount(mDatas.size());
                    }
                    ((RadarView) child).resetData();
                }
                continue;
            }

            if (i == 0){
                continue;
            }


            if (scanAngleList.get(i - 1) == 0) {
                continue;
            }

            float tempX;//The great circle distance x coordinates
            float tempY;//The great circle distance y coordinates
            if (i <= 4){
                 tempX = mWidth*circleProportion[3];
                 tempY = mHeight*circleProportion[3];
            }else{
                 tempX = mWidth*circleProportion[5]*(float)Math.sin(Math.toRadians(45));
                 tempY = mHeight*circleProportion[5]*(float)Math.sin(Math.toRadians(45));
            }

            //Small circle radius
            float circleRadius = Utils.dip2px(getContext(),20);

            float tempL = tempX - circleRadius;
            float tempT = tempY - circleRadius;
            float tempR = tempX + circleRadius;
            float tempB = tempY + circleRadius;

            if (i <= 4){
                if (i == 1){
                    child.layout((int) (mWidth/2 - circleRadius), (int) (mHeight/2 - tempB),(int) (mWidth/2 + circleRadius),(int) (mHeight/2 - tempT));
                }else if (i == 2){
                    child.layout((int) (mWidth/2 + tempL), (int) (mHeight/2 - circleRadius),(int) (mWidth/2 + tempR),(int) (mHeight/2 + circleRadius));
                }else if (i == 3){
                    child.layout((int) (mWidth/2 - circleRadius), (int) (mHeight/2 + tempT),(int) (mWidth/2 + circleRadius),(int) (mHeight/2 + tempB));
                }else if (i == 4){
                    child.layout((int) (mWidth/2 - tempR), (int) (mHeight/2 - circleRadius),(int) (mWidth/2 - tempL),(int) (mHeight/2 + circleRadius));
                }
            }else{
                if (i == 5){
                    child.layout((int) (mWidth/2 + tempL), (int) (mHeight/2 - tempB),(int) (mWidth/2 + tempR),(int) (mHeight/2 - tempT));
                }else if (i == 6){
                    child.layout((int) (mWidth/2 + tempL), (int) (mHeight/2 + tempT),(int) (mWidth/2 + tempR),(int) (mHeight/2 + tempB));
                }else if (i == 7){
                    child.layout((int) (mWidth/2 - tempR), (int) (mHeight/2 + tempT),(int) (mWidth/2 - tempL),(int) (mHeight/2 + tempB));
                }else if (i == 8){
                    child.layout((int) (mWidth/2 - tempR), (int) (mHeight/2 - tempB),(int) (mWidth/2 - tempL),(int) (mHeight/2 - tempT));
                }
            }

            //Set the click event
            child.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    resetAnim(currentShowChild);
                    currentShowChild = (CircleView) child;
                    //因为雷达图是childAt(0),所以这里需要作-1才是正确的Circle
                    startAnim(currentShowChild, j - 1);
                    if (iRadarClickListener != null) {
                        iRadarClickListener.onRadarItemClick(j - 1);

                    }
                }
            });
        }


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

    /**
     * Set up the data
     *
     * @param mDatas
     */
    public void setDatas(ArrayList<WifiPeopleVO> mDatas) {
        this.mDatas = mDatas;
        dataLength = mDatas.size();
        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;
        //Find the maximum distance, the minimum corresponding minItemPosition
        for (int j = 0; j < dataLength; j++) {
            UserInfoVo item = mDatas.get(j);
            if (Float.parseFloat(item.getDistance()) < min) {
                min = Float.parseFloat(item.getDistance());
                minItemPosition = j;
            }
            if (Float.parseFloat(item.getDistance()) > max) {
                max = Float.parseFloat(item.getDistance());
            }
            scanAngleList.put(j, 0f);
        }
        //According to the data source information dynamically add CircleView
        for (int i = 0; i < dataLength; i++) {
            CircleView circleView = new CircleView(getContext());
            circleView.setPortraitIcon(mDatas.get(i).getThumb(),mDatas.get(i).getUsername());
            if (minItemPosition == i) {
                minShowChild = circleView;
            }
            addView(circleView);
        }
    }


    /**
     * Update the data
     * @param mDatas
     */
    public void updateDatas(ArrayList<WifiPeopleVO> mDatas) {
        int childCount = getChildCount();
        if (childCount > 1){
            removeViews(1,childCount - 1);
        }
        setDatas(mDatas);
        resetData = true;
    }


    /**
     * The callback when the radar map not scan
     *
     * @param position
     * @param scanAngle
     */
    @Override
    public void onScanning(int position, float scanAngle) {
        resetData = false;
        if (scanAngle == 0) {
            scanAngleList.put(position, 1f);
        } else {
            scanAngleList.put(position, scanAngle);
        }
        requestLayout();
    }

    /**
     * Back at the radar map scanning
     */
    @Override
    public void onScanSuccess() {
        resetAnim(currentShowChild);
        currentShowChild = minShowChild;
        startAnim(currentShowChild, minItemPosition);
    }

    /**
     * Restore original CircleView dots size
     *
     * @param object
     */
    private void resetAnim(CircleView object) {
        if (object != null) {
            ObjectAnimator.ofFloat(object, "scaleX", 1f).setDuration(300).start();
            ObjectAnimator.ofFloat(object, "scaleY", 1f).setDuration(300).start();
        }

    }

    /**
     * Enlarge CircleView dot size
     * @param object
     * @param position
     */
    private void startAnim(CircleView object, int position) {
        if (object != null) {
            ObjectAnimator.ofFloat(object, "scaleX", 1.2f).setDuration(300).start();
            ObjectAnimator.ofFloat(object, "scaleY", 1.2f).setDuration(300).start();
        }
    }

    /**
     * In the radar map click callback interface to monitor CircleView dots
     */
    public interface IRadarClickListener {
        void onRadarItemClick(int position);
    }

    /**
     * According to the position, enlarge the specified CircleView dots
     * @param position
     */
    public void setCurrentShowItem(int position) {
        CircleView child = (CircleView) getChildAt(position + 1);
        resetAnim(currentShowChild);
        currentShowChild = child;
        startAnim(currentShowChild, position);
    }


    public void onResume() {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getId() == R.id.id_scan_circle) {
                ((RadarView) child).resetMatrix(true);
                break;
            }
        }
    }


    public void onPause() {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getId() == R.id.id_scan_circle) {
                ((RadarView) child).resetMatrix(false);
                break;
            }
        }
    }

}
