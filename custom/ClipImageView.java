package com.lingtuan.firefly.custom;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

/**
 * Used for scaling tailoring custom ImageView view
 */
public class ClipImageView extends ImageView implements View.OnTouchListener, ViewTreeObserver.OnGlobalLayoutListener{

	private final int BORDERDISTANCE = ClipView.BORDERDISTANCE;
	
    public final float DEFAULT_MAX_SCALE = 4.0f;
    public final float DEFAULT_MID_SCALE = 2.0f;
    public final float DEFAULT_MIN_SCALE = 1.0f;

    private float minScale = DEFAULT_MIN_SCALE;
    private float midScale = DEFAULT_MID_SCALE;
    private float maxScale = DEFAULT_MAX_SCALE;
	
	private MultiGestureDetector multiGestureDetector;
	
	private int borderlength;
	
	private boolean isJusted;
	
	private final Matrix baseMatrix = new Matrix();
	private final Matrix drawMatrix = new Matrix();
	private final Matrix suppMatrix = new Matrix();
	private final RectF displayRect = new RectF();
	private final float[] matrixValues = new float[9];
	
	public ClipImageView(Context context) {
        this(context, null);
    }

    public ClipImageView(Context context, AttributeSet attr) {
        this(context, attr, 0);
    }

    public ClipImageView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);

        super.setScaleType(ScaleType.MATRIX);

        setOnTouchListener(this);

        multiGestureDetector = new MultiGestureDetector(context);
        
    }

    /**
     * Based on image high, wide set initial zoom level image and position
     */
    private void configPosition(){
    	super.setScaleType(ScaleType.MATRIX);
    	Drawable d = getDrawable();
    	if(d == null){
    		return;
    	}
    	final float viewWidth = getWidth();
        final float viewHeight = getHeight();
        final int drawableWidth = d.getIntrinsicWidth();
        final int drawableHeight = d.getIntrinsicHeight();

        borderlength = (int) (viewWidth - BORDERDISTANCE *2);
        float scale = 1.0f;
        /**
         * Judge pictures wide high proportion, adjust the display position and zooming
         */
        // Image width less than or equal to height
        if(drawableWidth <= drawableHeight){
        	// To determine whether a width less than the borders, zoom paved with cutting border
        	if(drawableWidth < borderlength){
        		baseMatrix.reset();
        		scale = (float)borderlength / drawableWidth;
        		// The zoom
        		baseMatrix.postScale(scale, scale);
        	}
        	else{
        		minScale = (float)borderlength / drawableWidth;
        	}
        	// Image width is greater than the height
        }else{
        	if(drawableHeight < borderlength){
        		baseMatrix.reset();
        		scale = (float)borderlength / drawableHeight;
        		// The zoom
        		baseMatrix.postScale(scale, scale);
        	}
        	else{
        		minScale = (float)borderlength / drawableHeight;
        	}
        }
        // Mobile center
        baseMatrix.postTranslate((viewWidth - drawableWidth * scale) / 2, (viewHeight - drawableHeight * scale)/2);
        
        resetMatrix();
        isJusted = true;
    }
    
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return multiGestureDetector.onTouchEvent(event);
	}
	
	private class MultiGestureDetector extends GestureDetector.SimpleOnGestureListener implements
            OnScaleGestureListener {
		
		private final ScaleGestureDetector scaleGestureDetector;
        private final GestureDetector gestureDetector;
        private final float scaledTouchSlop;
        
        private VelocityTracker velocityTracker;
        private boolean isDragging;
        
        private float lastTouchX;
        private float lastTouchY;
        private float lastPointerCount;
        
		public MultiGestureDetector(Context context) {
			scaleGestureDetector = new ScaleGestureDetector(context, this);

            gestureDetector = new GestureDetector(context, this);
            gestureDetector.setOnDoubleTapListener(this);

            final ViewConfiguration configuration = ViewConfiguration.get(context);
            scaledTouchSlop = configuration.getScaledTouchSlop();
		}

		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			float scale = getScale();
			float scaleFactor = detector.getScaleFactor();
			if(getDrawable() != null && ((scale < maxScale && scaleFactor > 1.0f) || (scale > minScale && scaleFactor < 1.0f))){
				if(scaleFactor * scale < minScale){
					scaleFactor = minScale / scale;
				}
				if(scaleFactor * scale > maxScale){
					scaleFactor = maxScale / scale;
				}
				suppMatrix.postScale(scaleFactor, scaleFactor, getWidth()/2, getHeight()/2);
				checkAndDisplayMatrix();
			}
			return true;
		}

		@Override
		public boolean onScaleBegin(ScaleGestureDetector detector) {
			return true;
		}

		@Override
		public void onScaleEnd(ScaleGestureDetector detector) {
		}
		
		public boolean onTouchEvent(MotionEvent event) {
            if (gestureDetector.onTouchEvent(event)) {
                return true;
            }

            scaleGestureDetector.onTouchEvent(event);
            
            /*
             * Get the center x, y of all the pointers
             */
            float x = 0, y = 0;
            final int pointerCount = event.getPointerCount();
            for (int i = 0; i < pointerCount; i++) {
                x += event.getX(i);
                y += event.getY(i);
            }
            x = x / pointerCount;
            y = y / pointerCount;

            /*
             * If the pointer count has changed cancel the drag
             */
            if (pointerCount != lastPointerCount) {
                isDragging = false;
                if (velocityTracker != null) {
                    velocityTracker.clear();
                }
                lastTouchX = x;
                lastTouchY = y;
            }
            lastPointerCount = pointerCount;
            
            switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (velocityTracker == null) {
                    velocityTracker = VelocityTracker.obtain();
                } else {
                    velocityTracker.clear();
                }
                velocityTracker.addMovement(event);

                lastTouchX = x;
                lastTouchY = y;
                isDragging = false;
                break;

            case MotionEvent.ACTION_MOVE: {
                final float dx = x - lastTouchX, dy = y - lastTouchY;

                if (isDragging == false) {
                    // Use Pythagoras to see if drag length is larger than
                    // touch slop
                    isDragging = Math.sqrt((dx * dx) + (dy * dy)) >= scaledTouchSlop;
                }

                if (isDragging) {
                    if (getDrawable() != null) {
                        suppMatrix.postTranslate(dx, dy);
                        checkAndDisplayMatrix();
                    }

                    lastTouchX = x;
                    lastTouchY = y;

                    if (velocityTracker != null) {
                        velocityTracker.addMovement(event);
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                lastPointerCount = 0;
                if (velocityTracker != null) {
                    velocityTracker.recycle();
                    velocityTracker = null;
                }
                break;
        }
            
            return true;
        }
		
		@Override
		public boolean onDoubleTap(MotionEvent event) {
			try {
                float scale = getScale();
                float x = getWidth() / 2;
                float y = getHeight() / 2;

                if (scale < midScale) {
                    post(new AnimatedZoomRunnable(scale, midScale, x, y));
                } else if ((scale >= midScale) && (scale < maxScale)) {
                    post(new AnimatedZoomRunnable(scale, maxScale, x, y));
                } else {
                    post(new AnimatedZoomRunnable(scale, minScale, x, y));
                }
            } catch (Exception e) {
                // Can sometimes happen when getX() and getY() is called
            }

            return true;
		}
	}

	private class AnimatedZoomRunnable implements Runnable {
        // These are 'postScale' values, means they're compounded each iteration
        static final float ANIMATION_SCALE_PER_ITERATION_IN = 1.07f;
        static final float ANIMATION_SCALE_PER_ITERATION_OUT = 0.93f;

        private final float focalX, focalY;
        private final float targetZoom;
        private final float deltaScale;

        public AnimatedZoomRunnable(final float currentZoom, final float targetZoom,
                final float focalX, final float focalY) {
            this.targetZoom = targetZoom;
            this.focalX = focalX;
            this.focalY = focalY;

            if (currentZoom < targetZoom) {
                deltaScale = ANIMATION_SCALE_PER_ITERATION_IN;
            } else {
                deltaScale = ANIMATION_SCALE_PER_ITERATION_OUT;
            }
        }

        public void run() {
            suppMatrix.postScale(deltaScale, deltaScale, focalX, focalY);
            checkAndDisplayMatrix();

            final float currentScale = getScale();

            if (((deltaScale > 1f) && (currentScale < targetZoom))
                    || ((deltaScale < 1f) && (targetZoom < currentScale))) {
                // We haven't hit our target scale yet, so post ourselves
                // again
                postOnAnimation(ClipImageView.this, this);

            } else {
                // We've scaled past our target zoom, so calculate the
                // necessary scale so we're back at target zoom
                final float delta = targetZoom / currentScale;
                suppMatrix.postScale(delta, delta, focalX, focalY);
                checkAndDisplayMatrix();
            }
        }
    }

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void postOnAnimation(View view, Runnable runnable) {
        if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
            view.postOnAnimation(runnable);
        } else {
            view.postDelayed(runnable, 16);
        }
    }
	
    /**
     * Returns the current scale value
     * 
     * @return float - current scale value
     */
    public final float getScale() {
        suppMatrix.getValues(matrixValues);
        return matrixValues[Matrix.MSCALE_X];
    }
	
	@Override
	public void onGlobalLayout() {
		if(isJusted){
			return;
		}
		// Adjust the position of view
		configPosition();
	}
	
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeGlobalOnLayoutListener(this);
    }
    
    /**
     * Helper method that simply checks the Matrix, and then displays the result
     */
    private void checkAndDisplayMatrix() {
        checkMatrixBounds();
        setImageMatrix(getDisplayMatrix());
    }
    
    private void checkMatrixBounds() {
    	final RectF rect = getDisplayRect(getDisplayMatrix());
        if (null == rect) {
            return;
        }

        float deltaX = 0, deltaY = 0;
        final float viewWidth = getWidth();
        final float viewHeight = getHeight();
        // Judge moved or resized, pictures showed is beyond cutting frame boundary
        if(rect.top > (viewHeight - borderlength) / 2){
        	deltaY = (viewHeight - borderlength) / 2 - rect.top;
        }
        if(rect.bottom < (viewHeight + borderlength) / 2){
        	deltaY = (viewHeight + borderlength) / 2 - rect.bottom;
        }
        if(rect.left > (viewWidth - borderlength) / 2){
        	deltaX = (viewWidth - borderlength) / 2 - rect.left;
        }
        if(rect.right < (viewWidth + borderlength) / 2){
        	deltaX = (viewWidth + borderlength) / 2 - rect.right;
        }
        // Finally actually translate the matrix
        suppMatrix.postTranslate(deltaX, deltaY);
    }
    
    /**
     * Helper method that maps the supplied Matrix to the current Drawable
     * 
     * @param matrix
     *            - Matrix to map Drawable against
     * @return RectF - Displayed Rectangle
     */
    private RectF getDisplayRect(Matrix matrix) {
        Drawable d = getDrawable();
        if (null != d) {
            displayRect.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            matrix.mapRect(displayRect);
            return displayRect;
        }

        return null;
    }
    
    /**
     * Resets the Matrix back to FIT_CENTER, and then displays it.s
     */
    private void resetMatrix() {
    	if(suppMatrix == null){
    		return;
    	}
        suppMatrix.reset();
        setImageMatrix(getDisplayMatrix());
    }
    
    protected Matrix getDisplayMatrix() {
        drawMatrix.set(baseMatrix);
        drawMatrix.postConcat(suppMatrix);
        return drawMatrix;
    }
    
    /**
     * Shear pictures, after returning to shear the bitmap object
	 * 
     * @return
     */
    public Bitmap clip(){
    	Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
    	Canvas canvas = new Canvas(bitmap);
    	draw(canvas);
        Bitmap bmp = Bitmap.createBitmap(bitmap, (getWidth() - borderlength) / 2, (getHeight() - borderlength) / 2, borderlength, borderlength);
        if (bitmap != null && !bitmap.isRecycled()){
            bitmap.recycle();
            bitmap = null;
        }
    	return bmp;
    }
}
