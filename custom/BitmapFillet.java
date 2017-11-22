package com.lingtuan.firefly.custom;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Photo fillet processing
 */
public class BitmapFillet {
	
	/**All rounded corners*/
	public static final int ALL = 347120;
	/**The upper corner*/
    public static final int TOP = 547120;
    /**On the left corner*/
    public static final int LEFT = 647120;
    /**On the right corner*/
    public static final int RIGHT = 747120;
    /**The rounded corners*/
    public static final int BOTTOM = 847120;
    /**Upper left corner*/
    public static final int LEFT_TOP = 1047120;
    /**The upper right corner*/
    public static final int RIGHT_TOP = 1147120;
    /**The lower left corner*/
    public static final int LEFT_BOTTOM = 1247120;
    /**The lower right corner*/
    public static final int RIGHT_BOTTOM = 1347120;
    /**round*/
    public static final int ROUND = 1447120;
    
    /**
     * 
     * Specify the trimming of the picture, the rounded images processing
     * @param type Specific see：{@link BitmapFillet#ALL} , {@link BitmapFillet#TOP} ,
     * 				{@link BitmapFillet#LEFT} , {@link BitmapFillet#RIGHT} , {@link BitmapFillet#BOTTOM}
     * 				{@link BitmapFillet#LEFT_TOP} , {@link BitmapFillet#RIGHT_TOP} ,  
     * 				{@link BitmapFillet#LEFT_BOTTOM} , {@link BitmapFillet#RIGHT_BOTTOM}
     * @param bitmap Need to be cut fillet pictures
     * @param roundPx To cut the pixel size
     * @return
     *
     */
    public static Bitmap fillet(int type, Bitmap bitmap, int roundPx) {
    	if(type == ROUND){
    		return toRoundBitmap(bitmap);
    	}
        try {
			// the principle is: to establish a transparent Bitmap drawing board with the same size and pictures
			// and then on the drawing board to draw out a desired shape.
			// the last post on the source images.
        	final int width = bitmap.getWidth();
        	final int height = bitmap.getHeight();
        	
            Bitmap paintingBoard = Bitmap.createBitmap(width,height, Config.ARGB_8888);
            Canvas canvas = new Canvas(paintingBoard);
            canvas.drawARGB(Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT);
            
            final Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(Color.BLACK);
            
            if( TOP == type ){
            	clipTop(canvas,paint,roundPx,width,height);
            }else if( LEFT == type ){
            	 clipLeft(canvas,paint,roundPx,width,height);
            }else if( RIGHT == type ){
            	clipRight(canvas,paint,roundPx,width,height);
            }else if( BOTTOM == type ){
            	clipBottom(canvas,paint,roundPx,width,height);
            }else if( LEFT_BOTTOM == type ){
            	clipLeftBottom(canvas,paint,roundPx,width,height);
            }else if( RIGHT_BOTTOM == type ){
            	clipRightBottom(canvas,paint,roundPx,width,height);
            }else if( RIGHT_TOP == type ){
            	clipRightTop(canvas,paint,roundPx,width,height);
            }else if( LEFT_TOP == type ){
            	clipLeftTop(canvas,paint,roundPx,width,height);
            }else{
            	clipAll(canvas,paint,roundPx,width,height);
            }
            
            paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
            //Post figure
            final Rect src = new Rect(0, 0, width, height);
            final Rect dst = src;
            canvas.drawBitmap(bitmap, src, dst, paint);   
            return paintingBoard;
        } catch (Exception exp) {
            return bitmap;
        }
    }
    
    private static void clipLeft(final Canvas canvas, final Paint paint, int offset, int width, int height){
        final Rect block = new Rect(offset,0,width,height);
        canvas.drawRect(block, paint);
        final RectF rectF = new RectF(0, 0, offset * 2 , height);
        canvas.drawRoundRect(rectF, offset, offset, paint);
    }
    
    private static void clipRight(final Canvas canvas, final Paint paint, int offset, int width, int height){
        final Rect block = new Rect(0, 0, width-offset, height);
        canvas.drawRect(block, paint);
        final RectF rectF = new RectF(width - offset * 2, 0, width , height);
        canvas.drawRoundRect(rectF, offset, offset, paint);
    }
    
    private static void clipTop(final Canvas canvas, final Paint paint, int offset, int width, int height){
        final Rect block = new Rect(0, offset, width, height);
        canvas.drawRect(block, paint);
        final RectF rectF = new RectF(0, 0, width , offset * 2);
        canvas.drawRoundRect(rectF, offset, offset, paint);
    }
    
    /**
     * Specifies the upper left corner for the rounded corners
     */
    private static void clipLeftTop(final Canvas canvas, final Paint paint, int offset, int width, int height){
    	final Rect block = new Rect(0, offset, width, height);
    	canvas.drawRect(block, paint);
    	final RectF rectF = new RectF(0, 0, width / 2 , offset * 2);
    	canvas.drawRoundRect(rectF, offset, offset, paint);
    	final Rect block1 = new Rect(width / 2 - offset * 2 , 0, width, height);
    	canvas.drawRect(block1, paint);
    }
    
    /**
     * Specify the upper right corner to rounded
     */
    private static void clipRightTop(final Canvas canvas, final Paint paint, int offset, int width, int height){
    	final Rect block = new Rect(0, offset, width, height);
    	canvas.drawRect(block, paint);
    	final RectF rectF = new RectF(width / 2 - offset * 2, 0, width, offset * 2);
    	canvas.drawRoundRect(rectF, offset, offset, paint);
    	final Rect block1 = new Rect(0, 0, width / 2, height);
    	canvas.drawRect(block1, paint);
    }
    
    private static void clipBottom(final Canvas canvas, final Paint paint, int offset, int width, int height){
        final Rect block = new Rect(0, 0, width, height - offset);
        canvas.drawRect(block, paint);
        final RectF rectF = new RectF(0, height - offset * 2 , width , height);
        canvas.drawRoundRect(rectF, offset, offset, paint);
    }
    
    /**
     * Specify the lower left corner for the rounded corners
     */
    private static void clipLeftBottom(final Canvas canvas, final Paint paint,
									   int offset, int width, int height) {
		final Rect block = new Rect(0, 0, width, height - offset);
		canvas.drawRect(block, paint);
		final RectF rectF = new RectF(0, height - offset * 2, width / 2, height);
		canvas.drawRoundRect(rectF, offset, offset, paint);
		final Rect block1 = new Rect(width / 2 - offset * 2, height - offset * 2, width, height);
		canvas.drawRect(block1, paint);
	}
    
    /**
     * Specify the lower right corner of the rounded
     */
    private static void clipRightBottom(final Canvas canvas, final Paint paint,
										int offset, int width, int height) {
    	final Rect block = new Rect(0, 0, width, height - offset);
    	canvas.drawRect(block, paint);
    	final RectF rectF = new RectF(width / 2, height - offset * 2, width , height);
    	canvas.drawRoundRect(rectF, offset, offset, paint);
    	final Rect block1 = new Rect(0, height - offset * 2, width / 2 + offset * 2, height);
    	canvas.drawRect(block1, paint);
    }
    
    private static void clipAll(final Canvas canvas, final Paint paint, int offset, int width, int height){
    	final RectF rectF = new RectF(0, 0, width , height);
        canvas.drawRoundRect(rectF, offset, offset, paint);
    }
    
    /** 
     * Convert images into a round shape
     * @param bitmap The incoming Bitmap object
     * @return 
     */  
    private static Bitmap toRoundBitmap(Bitmap bitmap) {
    	int width = bitmap.getWidth();  
    	int height = bitmap.getHeight();  
    	float roundPx;  
    	float left,top,right,bottom,dst_left,dst_top,dst_right,dst_bottom;  
    	if (width <= height) {  
    		roundPx = width / 2;  
    		top = 0;  
    		bottom = width;  
    		left = 0;  
    		right = width;  
    		height = width;  
    		dst_left = 0;  
    		dst_top = 0;  
    		dst_right = width;  
    		dst_bottom = width;  
    	} else {  
    		roundPx = height / 2;  
    		float clip = (width - height) / 2;  
    		left = clip;  
    		right = width - clip;  
    		top = 0;  
    		bottom = height;  
    		width = height;  
    		dst_left = 0;  
    		dst_top = 0;  
    		dst_right = height;  
    		dst_bottom = height;  
    	}  
    	
    	Bitmap output = Bitmap. createBitmap(width,
    			height, Config. ARGB_8888 );
    	Canvas canvas = new Canvas(output);
    	
    	final int color = 0xff424242;  
    	final Paint paint = new Paint();
    	final Rect src = new Rect(( int)left, ( int )top, ( int)right, (int )bottom);
    	final Rect dst = new Rect(( int)dst_left, ( int )dst_top, (int )dst_right, ( int)dst_bottom);
    	final RectF rectF = new RectF(dst);
    	
    	paint.setAntiAlias( true );  
    	
    	canvas.drawARGB(0, 0, 0, 0);  
    	paint.setColor(color);  
    	canvas.drawRoundRect(rectF, roundPx, roundPx, paint);  
    	
    	paint.setXfermode( new PorterDuffXfermode(Mode. SRC_IN));
    	canvas.drawBitmap(bitmap, src, dst, paint);  
    	return output;  
    }
    
}
