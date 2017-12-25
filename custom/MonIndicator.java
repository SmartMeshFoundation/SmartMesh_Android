package com.lingtuan.firefly.custom;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.lingtuan.firefly.R;

public class MonIndicator extends View{
	private ParamsCreator paramsCreator = new ParamsCreator(this.getContext());
	private List<CircleWrapper> wrappers;
	private int[] colors = new int[]{0xFF942909, 0xFF577B8C, 0xFF201289, 0xFF7E207C, 0xFF38B549};
	private Paint paint = new Paint();
	private RectF oval=new RectF();

	private int circleRadius;//circle radius
	private int circleSpacing;//circle spacing
	private int increment = 2;//increment
	
	
	public MonIndicator(Context context) {
		super(context);
	}
	public MonIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.monindicator);
		circleRadius = (int)a.getDimension(R.styleable.monindicator_circleRadius, paramsCreator.getDefaultCircleRadius());
		circleSpacing = (int)a.getDimension(R.styleable.monindicator_circleSpacing, paramsCreator.getDefaultCircleSpacing());
		int cycle = a.getInt(R.styleable.monindicator_cycle, 2000);//moinddicator cycle
		cycle = cycle/2;
		int number = (int)(cycle*1.0/1000 * 83);
		this.increment = (int)(this.circleRadius * 2.0 / number);
		this.increment = this.increment<=0?1:this.increment;
		createWrappers();
	}
	/**
	 * create wrappers
	 */
	private void createWrappers(){
		wrappers = new ArrayList<CircleWrapper>();
		int diameter = this.circleRadius * 2;
		//first circle
		CircleWrapper wrapper = new CircleWrapper();
		wrapper.diameter = diameter;
		wrapper.initDiameter = diameter;
		wrapper.dynamicDiameter = wrapper.initDiameter;
		wrapper.orientation = -1;
		wrappers.add(wrapper);
		//two cricle
		wrapper = new CircleWrapper();
		wrapper.diameter = diameter;
		wrapper.initDiameter = (int)(diameter*0.75);
		wrapper.dynamicDiameter = wrapper.initDiameter;
		wrapper.orientation = 1;
		wrappers.add(wrapper);
		//three circle
		wrapper = new CircleWrapper();
		wrapper.diameter = diameter;
		wrapper.initDiameter = (int)(diameter*0.5);
		wrapper.dynamicDiameter = wrapper.initDiameter;
		wrapper.orientation = 1;
		wrappers.add(wrapper);
		//four circle
		wrapper = new CircleWrapper();
		wrapper.diameter = diameter;
		wrapper.initDiameter = (int)(diameter*0.25);
		wrapper.dynamicDiameter = wrapper.initDiameter;
		wrapper.orientation = 1;
	    wrappers.add(wrapper);
		//five circle
		wrapper = new CircleWrapper();
		wrapper.diameter = diameter;
		wrapper.initDiameter = 0;
		wrapper.dynamicDiameter = 0;
		wrapper.orientation = 1;
		wrappers.add(wrapper);
	}
	/**
	 * on measure
	 */
	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }	
	/**
     * measure width
     */
    private int measureWidth(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
        	result = getDefaultWidth();
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }
    /**
     * measure height
     */
    private int measureHeight(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = getDefaultHeight();
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }
    /**
     * get default width
     */
    private int getDefaultWidth(){
    	int defaultWidth = this.circleRadius * 2 * this.wrappers.size() + (this.wrappers.size()-1) * this.circleSpacing;
    	return defaultWidth;
    }
    /**
     * get default height
     */
    private int getDefaultHeight(){
    	return this.circleRadius * 2;
    }
    @Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.FILL);
		//canvas.drawColor(0xFF00FF33);
		drawCircle01(canvas);
		drawCircle02(canvas);
		drawCircle03(canvas);
		drawCircle04(canvas);
		drawCircle05(canvas);
		this.invalidate();
	}
    /**
     * draw circle
     */
    private void drawCircle01(Canvas canvas){
    	paint.setColor(colors[0]);
    	CircleWrapper wrapper = wrappers.get(0);
    	wrapper.dynamicDiameter = wrapper.dynamicDiameter + wrapper.orientation * this.increment;
    	if(wrapper.dynamicDiameter >= wrapper.diameter){
    		wrapper.orientation = -1;
    		wrapper.dynamicDiameter = wrapper.diameter;
    	}
    	if(wrapper.dynamicDiameter <= 0){
    		wrapper.orientation = 1;
    		wrapper.dynamicDiameter = 0;
    	}
    	int totalWidth = this.circleRadius * 2 * this.wrappers.size() + (this.wrappers.size()-1) * this.circleSpacing;
    	int centerX = this.getWidth()/2 - totalWidth/2 + this.circleRadius;
    	int centerY = this.getHeight()/2;
    	
		oval.left = centerX - wrapper.dynamicDiameter/2 ;
		oval.top = centerY - wrapper.dynamicDiameter/2;
		oval.right = oval.left + wrapper.dynamicDiameter;
		oval.bottom = oval.top + wrapper.dynamicDiameter;
		canvas.drawArc(oval, 0, 360, false, paint);
    }
    /**
     * draw circle
     */
    private void drawCircle02(Canvas canvas){
    	paint.setColor(colors[1]);
    	CircleWrapper wrapper = wrappers.get(1);
    	CircleWrapper wrapper01 = wrappers.get(0);
    	if(wrapper01.dynamicDiameter == wrapper01.initDiameter){
    		wrapper.dynamicDiameter = wrapper.initDiameter;
    	}else{
    		wrapper.dynamicDiameter = wrapper.dynamicDiameter + wrapper.orientation * this.increment;
        	if(wrapper.dynamicDiameter >= wrapper.diameter){
        		wrapper.orientation = -1;
        		wrapper.dynamicDiameter = wrapper.diameter;
        	}
        	if(wrapper.dynamicDiameter <= 0){
        		wrapper.orientation = 1;
        		wrapper.dynamicDiameter = 0;
        	}
    	}
    	
    	int totalWidth = this.circleRadius * 2 * this.wrappers.size() + (this.wrappers.size()-1) * this.circleSpacing;
    	int centerX = this.getWidth()/2 - totalWidth/2 + (wrapper.diameter + this.circleSpacing) * 1 + this.circleRadius;
    	int centerY = this.getHeight()/2;
    	
		oval.left = centerX - wrapper.dynamicDiameter/2 ;
		oval.top = centerY - wrapper.dynamicDiameter/2;
		oval.right = oval.left + wrapper.dynamicDiameter;
		oval.bottom = oval.top + wrapper.dynamicDiameter;
		canvas.drawArc(oval, 0, 360, false, paint);
    }
    /**
     * draw circle
     */
    private void drawCircle03(Canvas canvas){
    	paint.setColor(colors[2]);
    	CircleWrapper wrapper = wrappers.get(2);
    	CircleWrapper wrapper01 = wrappers.get(0);
    	if(wrapper01.dynamicDiameter == wrapper01.initDiameter){
    		wrapper.dynamicDiameter = wrapper.initDiameter;
    	}else{
    		wrapper.dynamicDiameter = wrapper.dynamicDiameter + wrapper.orientation * this.increment;
        	if(wrapper.dynamicDiameter >= wrapper.diameter){
        		wrapper.orientation = -1;
        		wrapper.dynamicDiameter = wrapper.diameter;
        	}
        	if(wrapper.dynamicDiameter <= 0){
        		wrapper.orientation = 1;
        		wrapper.dynamicDiameter = 0;
        	}
    	}
    	
    	int totalWidth = this.circleRadius * 2 * this.wrappers.size() + (this.wrappers.size()-1) * this.circleSpacing;
    	int centerX = this.getWidth()/2 - totalWidth/2 + (wrapper.diameter + this.circleSpacing) * 2 + this.circleRadius;
    	int centerY = this.getHeight()/2;
    	
		oval.left = centerX - wrapper.dynamicDiameter/2 ;
		oval.top = centerY - wrapper.dynamicDiameter/2;
		oval.right = oval.left + wrapper.dynamicDiameter;
		oval.bottom = oval.top + wrapper.dynamicDiameter;
		canvas.drawArc(oval, 0, 360, false, paint);
    }
    /**
     * draw circle
     */
    private void drawCircle04(Canvas canvas){
    	paint.setColor(colors[3]);
    	CircleWrapper wrapper = wrappers.get(3);
    	CircleWrapper wrapper01 = wrappers.get(0);
    	if(wrapper01.dynamicDiameter == wrapper01.initDiameter){
    		wrapper.dynamicDiameter = wrapper.initDiameter;
    	}else{
    		wrapper.dynamicDiameter = wrapper.dynamicDiameter + wrapper.orientation * this.increment;
        	if(wrapper.dynamicDiameter >= wrapper.diameter){
        		wrapper.orientation = -1;
        		wrapper.dynamicDiameter = wrapper.diameter;
        	}
        	if(wrapper.dynamicDiameter <= 0){
        		wrapper.orientation = 1;
        		wrapper.dynamicDiameter = 0;
        	}
    	}
    	
    	int totalWidth = this.circleRadius * 2 * this.wrappers.size() + (this.wrappers.size()-1) * this.circleSpacing;
    	int centerX = this.getWidth()/2 - totalWidth/2 + (wrapper.diameter + this.circleSpacing) * 3 + this.circleRadius;
    	int centerY = this.getHeight()/2;
    	
		oval.left = centerX - wrapper.dynamicDiameter/2 ;
		oval.top = centerY - wrapper.dynamicDiameter/2;
		oval.right = oval.left + wrapper.dynamicDiameter;
		oval.bottom = oval.top + wrapper.dynamicDiameter;
		canvas.drawArc(oval, 0, 360, false, paint);
    }
    /**
     * draw circle
     */
    private void drawCircle05(Canvas canvas){
    	paint.setColor(colors[4]);
    	CircleWrapper wrapper = wrappers.get(4);
    	CircleWrapper wrapper01 = wrappers.get(0);
    	wrapper.dynamicDiameter = wrapper.diameter - wrapper01.dynamicDiameter;
    	int totalWidth = this.circleRadius * 2 * this.wrappers.size() + (this.wrappers.size()-1) * this.circleSpacing;
    	int centerX = this.getWidth()/2 - totalWidth/2 + (wrapper.diameter + this.circleSpacing) * 4 + this.circleRadius;
    	int centerY = this.getHeight()/2;
    	
		oval.left = centerX - wrapper.dynamicDiameter/2 ;
		oval.top = centerY - wrapper.dynamicDiameter/2;
		oval.right = oval.left + wrapper.dynamicDiameter;
		oval.bottom = oval.top + wrapper.dynamicDiameter;
		canvas.drawArc(oval, 0, 360, false, paint);
    }
    /**
     * set color
     * @param colors
     */
	public void setColors(int[] colors) {
		if(colors == null || colors.length == 0)
			return ;
		for(int i = 0; i<colors.length&&i<this.colors.length; i++){
			this.colors[i] = colors[i];
		}
	}

    private class CircleWrapper{
    	private int diameter;//circle diameter
    	private int initDiameter;//default diameter
    	private int dynamicDiameter;//dynamic diameter
    	private int orientation;//Direction, that is, increase or decrease 1: Increase -1 to decrease
    }
}
