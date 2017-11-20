package com.lingtuan.firefly.vo;

import android.text.TextUtils;

/**
 * Abstract class photo thumbnails splicing rules, later use to thumbnail must inherit this class, unified management rules of the thumbnail
 */
public class ImageResizer {
	
	/** Image width */
	private String reqWidth = "200";
	
	
	/**Picture height  */
	private String reqHeight = "200";
	
	/** Image cropping rules (1: geometric scaling;2: a square cut;Other: fixed size) */
	private String reqCropType = "2" ;
	
	/** Image quality (generally for 75;Depending on the image size and demand, the best range of 75 ~ 85) */
	private String quality = "80";
	
	public String getReqWidth() {
		return reqWidth;
	}

	public void setReqWidth(String reqWidth) {
		this.reqWidth = reqWidth;
	}

	public String getReqHeight() {
		return reqHeight;
	}

	public void setReqHeight(String reqHeight) {
		this.reqHeight = reqHeight;
	}

	public String getReqCropType() {
		return reqCropType;
	}

	public void setReqCropType(String reqCropType) {
		this.reqCropType = reqCropType;
	}

	public String getQuality() {
		return quality;
	}

	public void setQuality(String quality) {
		this.quality = quality;
	}


	/**
	 * Thumbnail images stitching
	 * Joining together thumbnail request rules, format similar to: http://beta.iyueni.com/Uploads/avatar/2/13955_cbdwfI.jpg_200_200_2_80.jpg
	 */
	protected String buildThumb(String thumb){
		if(TextUtils.isEmpty(thumb)){
			return "" ;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(thumb);
		sb.append("_");
		sb.append(reqWidth);
		sb.append("_");
		sb.append(reqHeight);
		sb.append("_");
		sb.append(reqCropType);
		sb.append("_");
		sb.append(quality);
		sb.append(thumb.substring(thumb.lastIndexOf(".")));
		return sb.toString();
	}
}
