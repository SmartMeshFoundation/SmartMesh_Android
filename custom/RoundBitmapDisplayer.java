package com.lingtuan.firefly.custom;

import android.graphics.Bitmap;

import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.display.BitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;

/**
 * Photo fillet processing
 */
public class RoundBitmapDisplayer implements BitmapDisplayer {

	private int type;
	private int roundPx;
	
	/**
     * @param roundPx To cut the pixel size
     */
	public RoundBitmapDisplayer(int type, int roundPx) {
		this.type = type;
		this.roundPx = roundPx;
	}

	@Override
	public void display(Bitmap bitmap, ImageAware imageAware,LoadedFrom loadedFrom) {
		try {
			imageAware.setImageBitmap(BitmapFillet.fillet(type, bitmap, roundPx));
		} catch (Exception e) {
			e.printStackTrace();
		}catch (Error e) {
			e.printStackTrace();
		}
	}

}
