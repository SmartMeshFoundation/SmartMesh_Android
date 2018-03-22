package com.lingtuan.firefly.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.View;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.quickmark.GroupQuickMarkUI;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Image tools
 */
public class BitmapUtils {
	
	public  static final int CAMERA_WITH_DATA = 10; // Taking pictures
	public  static final int PHOTO_PICKED_WITH_DATA = 11;  //gallery
	public  static final int PHOTO_CROP_RESULT = 12;  //Gallery choose photos of shear returned after identification number
	public  static final int PHOTO_CROP_CAPTURE_RESULT = 13 ; //From the camera take pictures after shear pins
	
	public static File upload   = null ;  //Photo files stored after taking pictures

	private static int mDesiredWidth;
	private static int mDesiredHeight;

	public static Bitmap getimage(String srcPath) {
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		//Began to read the pictures and the options at this time. InJustDecodeBounds set to true
		newOpts.inJustDecodeBounds = true;
		newOpts.inPreferredConfig = Bitmap.Config.RGB_565;
		newOpts.inPurgeable = true;
		newOpts.inInputShareable = true;
		Bitmap bitmap = BitmapFactory.decodeFile(srcPath,newOpts);//Returns the bm is empty at this time
		
		newOpts.inJustDecodeBounds = false;
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
		//There are many mainstream mobile phone nowadays is 800 * 480 resolution, so we set to high and wide
		float hh = Constants.MAX_IMAGE_HEIGHT;//Here setting height is 800 f
		float ww = Constants.MAX_IMAGE_WIDTH;//Here set the width to 480 f
		//Zoom ratio.Because it is fixed scaling, only one high or wide data can be calculated
		int be = 1;//be=1 don't zoom
		if (w > h && w > ww) {//If large width according to the width of the fixed size scale
			be = (int) (newOpts.outWidth / ww);
		} else if (w < h && h > hh) {//If the height according to the width of the fixed size scale
			be = (int) (newOpts.outHeight / hh);
		}
		if (be <= 0)
			be = 1;
		newOpts.inSampleSize = be;//Set the scaling
		//To read the pictures, note has put options at this time. The inJustDecodeBounds set to false
		bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
		return compressImage(bitmap);//Good compression ratio size quality again after compression
	}
	
	public static Bitmap getimage(String srcPath, int width , int height, int size) {
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		//Began to read the pictures and the options at this time. InJustDecodeBounds set to true
		newOpts.inJustDecodeBounds = true;
		newOpts.inPreferredConfig = Bitmap.Config.ARGB_8888;
		newOpts.inPurgeable = true;
		newOpts.inInputShareable = true;
		Bitmap bitmap = BitmapFactory.decodeFile(srcPath,newOpts);//Returns the bm is empty at this time
		
		newOpts.inJustDecodeBounds = false;
		int srcWidth = newOpts.outWidth;
		int srcHeight = newOpts.outHeight;

		
		
		float outHeight = height;
		float outWidth = width;
		//Zoom ratio.Because it is fixed scaling, only one high or wide data can be calculated
		int inSampleSize = 1;//be=1 don't zoom
		if (srcHeight > outHeight || srcWidth > outWidth) {//If large width according to the width of the fixed size scale
			float heightRatio = (float) srcHeight/ outHeight;
			float widthRatio = (float) srcWidth / outWidth;
		    inSampleSize = heightRatio < widthRatio ? Math.round(heightRatio) : Math.round(widthRatio);
		}
		if (inSampleSize <= 0)
			inSampleSize = 1;
		newOpts.inSampleSize = inSampleSize;//Set the scaling
		int w ,h;
		if((size==Constants.MAX_KB&&(srcWidth/srcHeight>=Constants.MAX_SCALE||srcHeight/srcWidth>=Constants.MAX_SCALE))||(srcWidth <= outWidth && srcHeight <= outHeight)){
			w = srcWidth;
			h = srcHeight;
		}else if(srcWidth > outWidth && srcHeight > outHeight){
			 float heightRatio = (float) srcHeight/ outHeight;
		     float widthRatio = (float) srcWidth / outWidth;
		    
			if(heightRatio < widthRatio){
				w = (int) outWidth;
				h = (int) (outWidth * srcHeight / srcWidth);
				
			}else{
				h = (int) outHeight;
				w = (int) (outHeight * srcWidth / srcHeight);
			}
		}
		else if(srcWidth > outWidth){
			w = (int) outWidth;
			h = (int) (outWidth * srcHeight / srcWidth);
		}
		else{
			h = (int) outHeight;
			w = (int) (outHeight * srcWidth / srcHeight);
		}

		 //To read the pictures, note has put options at this time. The inJustDecodeBounds set to false
		 bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
		 if(bitmap == null)
		 {
			return null;
		 }
		 if(size == 10 || !(srcWidth < outWidth && srcHeight < outHeight)){
			bitmap = Bitmap.createScaledBitmap(bitmap, w, h, true);
		 }
		 return loadBitmap(srcPath, compressImage(bitmap, size));//Good compression ratio size quality again after compression
		 
		 
	}

	/**
	 * Scaling not only rotate
	 * @param srcPath
	 * @param width
	 * @param height
	 * @param size
	 * @return
	 */
	public static Bitmap getimageNoDigree(String srcPath, int width , int height, int size) {
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		//Began to read the pictures and the options at this time. InJustDecodeBounds set to true
		newOpts.inJustDecodeBounds = true;
		newOpts.inPreferredConfig = Bitmap.Config.ARGB_8888;
		newOpts.inPurgeable = true;
		newOpts.inInputShareable = true;
		Bitmap bitmap = BitmapFactory.decodeFile(srcPath,newOpts);//Returns the bm is empty at this time

		newOpts.inJustDecodeBounds = false;
		int srcWidth = newOpts.outWidth;
		int srcHeight = newOpts.outHeight;



		float outHeight = height;
		float outWidth = width;
		//Zoom ratio.Because it is fixed scaling, only one high or wide data can be calculated
		int inSampleSize = 1;//be=1 don't zoom
		if (srcHeight > outHeight || srcWidth > outWidth) {//If large width according to the width of the fixed size scale
			float heightRatio = (float) srcHeight/ outHeight;
			float widthRatio = (float) srcWidth / outWidth;
			inSampleSize = heightRatio < widthRatio ? Math.round(heightRatio) : Math.round(widthRatio);
		}
		if (inSampleSize <= 0)
			inSampleSize = 1;
		newOpts.inSampleSize = inSampleSize;//Set the scaling
		int w ,h;
		if((size==Constants.MAX_KB&&(srcWidth/srcHeight>=Constants.MAX_SCALE||srcHeight/srcWidth>=Constants.MAX_SCALE))||(srcWidth <= outWidth && srcHeight <= outHeight)){
			w = srcWidth;
			h = srcHeight;
		}else if(srcWidth > outWidth && srcHeight > outHeight){
			float heightRatio = (float) srcHeight/ outHeight;
			float widthRatio = (float) srcWidth / outWidth;

			if(heightRatio < widthRatio){
				w = (int) outWidth;
				h = (int) (outWidth * srcHeight / srcWidth);

			}else{
				h = (int) outHeight;
				w = (int) (outHeight * srcWidth / srcHeight);
			}
		}
		else if(srcWidth > outWidth){
			w = (int) outWidth;
			h = (int) (outWidth * srcHeight / srcWidth);
		}
		else{
			h = (int) outHeight;
			w = (int) (outHeight * srcWidth / srcHeight);
		}

		//To read the pictures, note has put options at this time. The inJustDecodeBounds set to false
		bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
		if(size == 10 || !(srcWidth < outWidth && srcHeight < outHeight)){
			bitmap = Bitmap.createScaledBitmap(bitmap, w, h, true);
		}
		return compressImage(bitmap, size);


	}
	public static Bitmap loadBitmap(String imagePath, Bitmap bitmap){
         int digree = 0;  
         ExifInterface exif = null;
         try {  
             exif = new ExifInterface(imagePath);
         } catch (IOException e) {
             e.printStackTrace();  
             exif = null;  
         }  
         if (exif != null) {  
             // Reads the images in the camera direction information
             int ori = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                     ExifInterface.ORIENTATION_UNDEFINED);
             // To calculate rotation Angle
             switch (ori) {  
             case ExifInterface.ORIENTATION_ROTATE_90:
                 digree = 90;  
                 break;  
             case ExifInterface.ORIENTATION_ROTATE_180:
                 digree = 180;  
                 break;  
             case ExifInterface.ORIENTATION_ROTATE_270:
                 digree = 270;  
                 break;  
             default:  
                 digree = 0;  
                 break;  
             }  
         }  
         if (digree != 0) {  
             // Rotating images
             Matrix m = new Matrix();
             m.postRotate(digree);  
             bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),   bitmap.getHeight(), m, true);
         }  
         return bitmap;  
	}
	
	/**
	 * @param image
	 * @return The compressed image
	 */
	public static Bitmap compressImage(Bitmap image, int size) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int options = 80;
		if(image.hasAlpha())
		{
			image.compress(Bitmap.CompressFormat.PNG, options, baos);// Quality compression method, 100 said here without compression, the compressed data stored in the baos
		}
		else{
			image.compress(Bitmap.CompressFormat.JPEG, options, baos);// Quality compression method, 100 said here without compression, the compressed data stored in the baos
		}
		while (baos.toByteArray().length / 1024 >= size) { // Cycle to judge if the compressed image is larger than the size KB, greater than continue to compress
			if(options == 30){//Has been unable to compress again compression distortion
				break;
			}
			options -= 10;// Reduce 10 every time
			baos.reset();// Reset the baos namely empty baos
			if(image.hasAlpha())
			{
				image.compress(Bitmap.CompressFormat.PNG, options, baos);// Compression options here %, the compressed data stored in the baos
			}
			else{
				image.compress(Bitmap.CompressFormat.JPEG, options, baos);// Compression options here %, the compressed data stored in the baos
			}
			
		}
		
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// The compressed data baos deposit into a ByteArrayInputStream
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// The ByteArrayInputStream data generated images
		try {
			if(baos != null){
				baos.close();
			}
			if(isBm != null){
				isBm.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(image != null ){
			image.recycle();
			image = null ; 
		}
		return bitmap;
	}
	
	/**
	 * @param image
	 * @return The compressed image
	 */
	public static Bitmap compressImage(Bitmap image) {
		if(image == null){
			return null;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		if(image.hasAlpha())
		{
			image.compress(Bitmap.CompressFormat.PNG, 80, baos);// Quality compression method, 100 said here without compression, the compressed data stored in the baos
		}
		else{
			image.compress(Bitmap.CompressFormat.JPEG, 80, baos);//Quality compression method, 100 said here without compression, the compressed data stored in the baos
		}
		
		int options = 80;
		while (baos.toByteArray().length / 1024 > Constants.MAX_KB) { // Cycle to judge if the compressed image is larger than 100 KB, greater than continue to compress
			baos.reset();// Reset the baos namely empty baos
			if(image.hasAlpha())
			{
			    image.compress(Bitmap.CompressFormat.PNG, options, baos);// Compression options here %, the compressed data stored in the baos
			}
			else{
				image.compress(Bitmap.CompressFormat.JPEG, options, baos);// Compression options here %, the compressed data stored in the baos
			}
			options -= 10;// Reduce 10 every time
		}
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// The compressed data baos deposit into a ByteArrayInputStream
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// The ByteArrayInputStream data generated images
		if(image != null ){
			image.recycle();
			image = null ; 
		}
		try {
			if(baos != null){
				baos.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			if(isBm != null){
				isBm.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bitmap;
	}

	public static Uri saveBitmap2SD(Bitmap bitmap, boolean isNeedRecycle) {
		String filePath = SDCardCtrl.getUploadPath() ;
		String fileName = getPhotoFileName();
		if(!new File(filePath).exists()){
			new File(filePath).mkdir();
		}
		File file = new File(filePath, fileName);
		try {
			if(!file.exists()){
				file.createNewFile();
			}
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(file));
			if(bitmap.hasAlpha())
			{
			    bitmap.compress(Bitmap.CompressFormat.PNG, 80, bos);
			}
			else{
				bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
			}
			bos.flush();
			bos.close();
			if(isNeedRecycle){
				if(bitmap != null ){
					bitmap.recycle();
					bitmap = null ; 
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Uri.fromFile(file);
	}

	public static Uri saveBitmap2SD(Bitmap bitmap, String filePath, String fileName) {

		if(!new File(filePath).exists()){
			new File(filePath).mkdir();
		}
		File file = new File(filePath,fileName);
		try {
			if(!file.exists()){
				file.createNewFile();
			}
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(file));

			bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
			bos.flush();
			bos.close();
			if(bitmap != null ){
					bitmap.recycle();
					bitmap = null ; 
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Uri.fromFile(file);
	}
	
	public static Uri saveBitmap2SD(Bitmap bitmap, int size, boolean recycle) {
		String filePath =  SDCardCtrl.getChatImagePath() + "/" ;
		String fileName = getPhotoFileName(size);
		SDFileUtils.getInstance().mkDir(new File(filePath));
		File file = new File(filePath, fileName);
		try {
			if(!file.exists()){
				file.createNewFile();
			}
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(file));
			if(bitmap.hasAlpha())
			{
			    bitmap.compress(Bitmap.CompressFormat.PNG, 80, bos);
			}
			else{
				bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
			}
			bos.flush();
			bos.close();
			if(recycle && bitmap != null && !bitmap.isRecycled()){
				bitmap.recycle();
				bitmap = null ; 
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Uri.fromFile(file);
	}
	
	public static Uri saveBitmap2SDFormDynamic(Bitmap bitmap, int size, boolean recycle) {
		String filePath = SDCardCtrl.getTempPath();
		String fileName = getPhotoFileName(size);
		File file = new File(filePath, fileName);
		try {
			if(!file.exists()){
				file.createNewFile();
			}
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
			if(bitmap.hasAlpha())
			{
			    bitmap.compress(Bitmap.CompressFormat.PNG, 80, bos);
			}
			else{
				bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
			}
			bos.flush();
			bos.close();
			if(recycle && bitmap != null ){
				bitmap.recycle();
				bitmap = null ; 
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Uri.fromFile(file);
	}
	
	// With the current time to obtain the image name
	@SuppressLint("SimpleDateFormat")
	public static String getPhotoFileName() {
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"'IMG'_yyyyMMdd_HHmmssSSS");
		return dateFormat.format(date) + ".jpg";
		// return "1.jpg";
	}
	
	// When first time to take pictures of the name
		@SuppressLint("SimpleDateFormat")
		public static String getPhotoFileName(int size) {
			Date date = new Date(System.currentTimeMillis());
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"'IMG'_yyyyMMdd_HHmmssSSS" + size + "_" + size);
			return dateFormat.format(date) + ".jpg";
			// return "1.jpg";
		}
	
	/**
	 * 
	 * Get photo address list
	 * 
	 * @return list
	 */
	private ArrayList<String> getImgPathList(Context c) {
		ArrayList<String> list = new ArrayList<String>();
		Cursor cursor = c.getContentResolver().query(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				new String[] { "_id", "_data" }, null, null, null);
		while (cursor.moveToNext()) {
			list.add(cursor.getString(1));// Add image path to the list
		}
		cursor.close();
		return list;
	}
	
		// Cut images
		public static Intent getPhotoPickIntent() {
			Intent intent = new Intent(Intent.ACTION_PICK, null);
			intent.setDataAndType(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
					"image/*");
			return intent;
		}
				
	// Photo album
	public static void getPicFromContent(Activity a) {
		try {
			Intent intent = BitmapUtils.getPhotoPickIntent();
			a.startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// Taking pictures
		public static  void getPicFromCapture(boolean isAvatar,Activity a) {
			try {
				 upload = null ; 
				 upload = new File(SDCardCtrl.getUploadPath(), BitmapUtils.getPhotoFileName()); // With the current time to obtain the image name
				Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
				Uri fromFile = Uri.fromFile(upload);
				intent.putExtra(MediaStore.EXTRA_OUTPUT,fromFile);
				a.startActivityForResult(intent,  isAvatar ? PHOTO_CROP_CAPTURE_RESULT: CAMERA_WITH_DATA);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	
	public static String BitmapToBase64String(Bitmap bmp){
		if(bmp == null)
		{
			return  null;
		}
		 ByteArrayOutputStream baos = new ByteArrayOutputStream();
		 if(bmp.hasAlpha())
		 {
			 bmp.compress(Bitmap.CompressFormat.PNG, 80, baos);
		 }
		else{
				bmp.compress(Bitmap.CompressFormat.JPEG, 80, baos);
		 }
		 String result = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
		 try {
			if(baos != null){
				baos.close();
			}
		} catch (Exception e) {
		}
		 return result;
	}
	
	public static Bitmap Base64StringToBitmap(String cover){
		try {

			if(TextUtils.isEmpty(cover)){
				return null;
			}
			byte[] b = Base64.decode(cover, Base64.DEFAULT);
			
			if (b.length != 0) {
				Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
				float width = bitmap.getWidth();
				float height = bitmap.getHeight();
				float density = NextApplication.mContext.getResources().getDisplayMetrics().density;
				if(width > 90 * density){
					width = (int) (90 * density);
					height = width / bitmap.getWidth() * height;
				}else/* if(height > 160 * density)*/{
					height = (int) (120 * density);
					width = height / bitmap.getHeight() * width;
				}
				
				if(width < 50 * density){
					width = (int) (50 * density);
				} 
				if(height < 50 * density){
					height = (int) (50 * density);
				}
				bitmap = ThumbnailUtils.extractThumbnail(bitmap, (int)width, (int)height);// Bitmap.createScaledBitmap(bitmap, (int)width, (int)height, true);

	            return bitmap;
	        } else {
	            return null;
	        }
		
		} catch (Error e) {
			return null;
		}catch (Exception e) {
			return null;
		}
		
	}
	
	public static String uploadZxing(Context context, Bitmap bitmap, boolean isQrcode,boolean isNeedRecycle) throws Exception {
		String filePath = isQrcode ? SDCardCtrl.getQrcodePath() : SDCardCtrl.getUploadPath() ;
		String fileName = getPhotoFileName();
		if(!new File(filePath).exists()){
			new File(filePath).mkdir();
		}
		File file = new File(filePath, fileName);
		try {
			if(!file.exists()){
				file.createNewFile();
			}
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(file));
			if(bitmap.hasAlpha())
			{
			    bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
			}
			else{
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
			}
			bos.flush();
			bos.close();
			if(isNeedRecycle&&bitmap != null ){
				bitmap.recycle();
				bitmap = null ; 
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		//Notification system update photo album
		Utils.notifySystemUpdateFolder(context, file);
		return file.getAbsolutePath();
	}

	public static String saveZxing2SD(Context context, Bitmap bitmap, boolean isNeedRecycle) throws Exception {
		String filePath = SDCardCtrl.getQrcodePath() ;
		String fileName = getPhotoFileName();
		if(!new File(filePath).exists()){
			new File(filePath).mkdir();
		}
		File file = new File(filePath, fileName);
		try {
			if(!file.exists()){
				file.createNewFile();
			}
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(file));
			if(bitmap.hasAlpha())
			{
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
			}
			else{
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
			}
			bos.flush();
			bos.close();
			if(isNeedRecycle&&bitmap != null ){
				bitmap.recycle();
				bitmap = null ;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		//Notification system update photo album
		Utils.notifySystemUpdateFolder(context, file);
		return file.getAbsolutePath();
	}

	public static Bitmap takeScreenShot(Activity activity) {
        // The View is you need to capture the View
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);  
        view.buildDrawingCache();  
        Bitmap b1 = view.getDrawingCache();
   
        // To obtain the status bar height
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);  
        int statusBarHeight = frame.top;  
        int titleBarHeight=Utils.dip2px(activity, 48);

		int paddingLeft = frame.width() / 6 -  Utils.dip2px(activity , 18);
		int paddingTop = Utils.dip2px(activity,60);
		int contentHight = Utils.dip2px(activity,80);
        // Remove the title bar
        Bitmap b = Bitmap.createBitmap(b1, paddingLeft, statusBarHeight + titleBarHeight + paddingTop, b1.getWidth() - 2 * paddingLeft, b1.getWidth() - 2 * paddingLeft + contentHight);
        view.destroyDrawingCache();  
        return b;  
    }
	public static Bitmap takeBitmapRect(Bitmap bmp, Rect rect, boolean isNeedRecycle) {
		// The View is you need to capture the View


		// To obtain the status bar height

		Bitmap b = Bitmap.createBitmap(bmp, rect.left, rect.top, rect.width(), rect.height()
				);
		if(isNeedRecycle&&bmp != null ){
			bmp.recycle();
			bmp = null ;
		}
		return b;
	}

	/**
	 * get thumbnail images
	 * The path of the @ param imagePath images
	 * @ param width the width of the picture
	 * @ param height the height of the picture
	 * @ return thumbnail images
	 */
	public static Bitmap getImageThumbnail(String imagePath, int width,int height) {
		Bitmap bitmap = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		// To obtain the image width and height, pay attention to the bitmap is null
		bitmap = BitmapFactory.decodeFile(imagePath, options);
		options.inJustDecodeBounds = false; // Set to false
		// Computing scale than
		int h = options.outHeight;
		int w = options.outWidth;
		int beWidth = w / width;
		int beHeight = h / height;
		int be = 1;
		if (beWidth < beHeight) {
			be = beWidth;
		} else {
			be = beHeight;
		}
		if (be <= 0) {
			be = 1;
		}
		options.inSampleSize = be;
		// To read the pictures, read the bitmap after scaling, pay attention to the put options. InJustDecodeBounds set to false
		bitmap = BitmapFactory.decodeFile(imagePath, options);
		// Using ThumbnailUtils to create thumbnails, here to specify which to scale the Bitmap object
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}
	
	/**
	 * to obtain image grayscale
	 * @ param bmSrc original figure
	 * @ return the converted grayscale
	 */
	public static Bitmap bitmap2Gray(Bitmap bmSrc) {
		// Get the picture of long and wide
		int width = bmSrc.getWidth();
		int height = bmSrc.getHeight();
		// Create the target gray image
		Bitmap bmpGray = null;
		bmpGray = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		// Create a canvas
		Canvas c = new Canvas(bmpGray);
		Paint paint = new Paint();
		ColorMatrix cm = new ColorMatrix();
		cm.setSaturation(0);
		ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
		paint.setColorFilter(f);
		c.drawBitmap(bmSrc, 0, 0, paint);
		return bmpGray;
	}
	/**
	 * the size of the generated thumbnail zoom rules remain and the unity of the server-side zoom
	 * @ param srcPath original address
	 * @ param square zoom into a square
	 * @ return the converted thumbnails
	 */
	public static Bitmap extractThumbnail(String srcPath, boolean square) {
		Bitmap bmSrc = BitmapFactory.decodeFile(srcPath);
		int width = bmSrc.getWidth();
	    int height = bmSrc.getHeight();
		Bitmap bitmapThumb=null;
		DisplayMetrics dm = NextApplication.mContext.getResources().getDisplayMetrics();
		if(square)//Zoom into the square
		{
			int size=100;
			if(dm.widthPixels<=320)
			{
				if(width<=100||height<=100)
				{
					size=width<=height?width:height;
				}
				else{
				    size=100;
				}
			}
			else if(dm.widthPixels<=800)
			{
				if(width<=200||height<=200)
				{
					size=width<=height?width:height;
				}
				else{
				    size=200;
				}
			}
			else{
				if(width<=300||height<=300)
				{
					size=width<=height?width:height;
				}
				else{
				    size=300;
				}
			}
			bitmapThumb= ThumbnailUtils.extractThumbnail(bmSrc, size, size);
		}
		else{//scaling
		    int w,h;
			if(dm.widthPixels<=320)
			{
				if(width<=100&&height<=100)
				{
					w=height;
					h=height;
				}
				else{
					w=100;
					h=w*height/width;
					if(h>100)
					{
						h=100;
						w=h*width/height;
					}
				}
				
			}
			else if(dm.widthPixels<=800)
			{
				if(width<=200&&height<=200)
				{
					w=height;
					h=height;
				}
				else{
					w=200;
					h=w*height/width;
					if(h>200)
					{
						h=200;
						w=h*width/height;
					}
				}
			}
			else{
				if(width<=300&&height<=300)
				{
					w=height;
					h=height;
				}
				else{
					w=300;
					h=w*height/width;
					if(h>300)
					{
						h=300;
						w=h*width/height;
					}
				}
			}
		    bitmapThumb = ThumbnailUtils.extractThumbnail(bmSrc, width, height);
		}
		return bitmapThumb;
	}
	
	/**
	 * generated thumbnail size scaling
	 * @ param srcPath original address
	 * The size of the @ param size after the compression
	 * @ return the converted share with thumbnails
	 */
	public static Bitmap extractShareThumbnail(String srcPath, int size) {
		Bitmap bmSrc = BitmapFactory.decodeFile(srcPath);
		
		Bitmap bitmapThumb = ThumbnailUtils.extractThumbnail(bmSrc, size, size);
		
		if(bmSrc != null ){
			bmSrc.recycle();
			bmSrc = null ; 
		}
		return bitmapThumb;
	}
	public static String saveBitmapFile(Context context, String oldPath) {
	     try { 
	           int byteread = 0;    
	           String filePath = SDCardCtrl.getQrcodePath() ;
			 	SDFileUtils.getInstance().createDir(filePath);
	       	   String fileName = getPhotoFileName();
	       	   File file = new File(filePath, fileName);
	           InputStream inStream = new FileInputStream(oldPath); //Read the original file
	           FileOutputStream fs = new FileOutputStream(file.getAbsolutePath());
	           byte[] buffer = new byte[1024];     
	           while ( (byteread = inStream.read(buffer)) != -1) {    
	               fs.write(buffer, 0, byteread);   
	           }   
	           inStream.close();   
	           fs.close();
	           //Notification system update photo album
	       	   Utils.notifySystemUpdateFolder(context, file);
	       	   return file.getParentFile().getAbsolutePath();
	      }   
	      catch (Exception e) {
	           e.printStackTrace();   
	           return null;
	      }   
	 } 
	/**
	 * access to video thumbnails (here for the first frame)
	 * @param filePath
	 * @return
	 */
	public static Bitmap getVideoThumbnail(String filePath) {
		Bitmap bitmap = null;
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
		try {
			retriever.setDataSource(filePath);
			bitmap = retriever
					.getFrameAtTime(TimeUnit.MILLISECONDS.toMicros(1));
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} finally {
			try {
				retriever.release();
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
		}
		return bitmap;
	}
	/**
	 * compute Sample Size
	 *
	 * @param options
	 * @param minSideLength
	 * @param maxNumOfPixels
	 * @return
	 */
	public static int computeSampleSize(BitmapFactory.Options options,int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength,maxNumOfPixels);
		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}

		return roundedSize;
	}

	/**
	 * compute Initial Sample Size
	 *
	 * @param options
	 * @param minSideLength
	 * @param maxNumOfPixels
	 * @return
	 */
	private static int computeInitialSampleSize(BitmapFactory.Options options,
												int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;

		// Upper and lower range
		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
				.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(
				Math.floor(w / minSideLength), Math.floor(h / minSideLength));

		if (upperBound < lowerBound) {
			// return the larger one when there is no overlapping zone.
			return lowerBound;
		}

		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
			return 1;
		} else if (minSideLength == -1) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}

	public static Bitmap tryGetBitmap(String imgFile, int minSideLength,
									  int maxNumOfPixels) {
		if (imgFile == null || imgFile.length() == 0)
			return null;

		try {
			FileDescriptor fd = new FileInputStream(imgFile).getFD();
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			// BitmapFactory.decodeFile(imgFile, options);
			BitmapFactory.decodeFileDescriptor(fd, null, options);

			options.inSampleSize = computeSampleSize(options, minSideLength,
					maxNumOfPixels);
			try {
				// here be sure to set it back to false, because before we set it became true
				// inJustDecodeBounds set to true, decodeFile not allocate space, namely, BitmapFactory decoding the Bitmap is Null, but can calculate the length and width of the original image
				options.inJustDecodeBounds = false;

				Bitmap bmp = BitmapFactory.decodeFile(imgFile, options);
				return bmp == null ? null : bmp;
			} catch (OutOfMemoryError err) {
				return null;
			}
		} catch (Exception e) {
			return null;
		}
	}
	public static Bitmap blurBitmap(Context context, Bitmap sentBitmap)
	{
		if (Build.VERSION.SDK_INT > 16) {
			try {
				Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), false);
				final RenderScript rs = RenderScript.create(context);
				final Allocation input = Allocation.createFromBitmap(rs, sentBitmap, Allocation.MipmapControl.MIPMAP_NONE,
						Allocation.USAGE_SCRIPT);
				final Allocation output = Allocation.createTyped(rs, input.getType());
				final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
				script.setRadius(12.0f);
				script.setInput(input);
				script.forEach(output);
				output.copyTo(bitmap);
				return bitmap;
			}
			catch (Exception e)
			{
				return sentBitmap;
			}
			catch (OutOfMemoryError err) {
				return sentBitmap;
			}

		}
		else{
			return sentBitmap;
		}
	}

	/**
	 * The image from the SD card
	 */
	public static Bitmap decodeSampledBitmapFromFile(String pathName, int reqWidth, int reqHeight) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(pathName, options);
		options = getBestOptions(options, reqWidth, reqHeight);
		Bitmap src = BitmapFactory.decodeFile(pathName, options);
		return createScaleBitmap(src, mDesiredWidth, mDesiredHeight);
	}

	/**
	 * calculate target width, height of target, inSampleSize
	 * @ return BitmapFactory. Options object
	 */
	private static BitmapFactory.Options getBestOptions(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Reads the images aspect
		int actualWidth = options.outWidth;
		int actualHeight = options.outHeight;
		// Then compute the dimensions we would ideally like to decode to.
		mDesiredWidth = getResizedDimension(reqWidth, reqHeight, actualWidth, actualHeight);
		mDesiredHeight = getResizedDimension(reqHeight, reqWidth, actualHeight, actualWidth);
		// According to the current calculation inSampleSize
		options.inSampleSize = calculateBestInSampleSize(actualWidth, actualHeight, mDesiredWidth, mDesiredHeight);
		// Use get to inSampleSize values resolution picture again
		options.inJustDecodeBounds = false;
		return options;
	}


	/**
	 * By passing in the bitmap, compressing, conform to the standards of the bitmap
	 */
	private static Bitmap createScaleBitmap(Bitmap tempBitmap, int desiredWidth, int desiredHeight) {
		// If necessary, scale down to the maximal acceptable size.
		if (tempBitmap != null && (tempBitmap.getWidth() > desiredWidth || tempBitmap.getHeight() > desiredHeight)) {
			// If it is enlarge images, a filter to decide whether smooth, if it is to reduce, had no effect on the filter
			Bitmap bitmap = Bitmap.createScaledBitmap(tempBitmap, desiredWidth, desiredHeight, true);
			tempBitmap.recycle(); // Releases the native Bitmap pixel array
			return bitmap;
		} else {
			return tempBitmap; // If there is no zoom, so don't recycle
		}
	}

	private static int calculateBestInSampleSize(int actualWidth, int actualHeight, int desiredWidth, int desiredHeight) {
		double wr = (double) actualWidth / desiredWidth;
		double hr = (double) actualHeight / desiredHeight;
		double ratio = Math.min(wr, hr);
		float inSampleSize = 1.0f;
		while ((inSampleSize * 2) <= ratio) {
			inSampleSize *= 2;
		}
		return (int) inSampleSize;
	}

	/**
	 * Scales one side of a rectangle to fit aspect ratio. Eventually get to the size of the measurement
	 *
	 * @param maxPrimary      Maximum size of the primary dimension (i.e. width for max
	 *                        width), or zero to maintain aspect ratio with secondary
	 *                        dimension
	 * @param maxSecondary    Maximum size of the secondary dimension, or zero to maintain
	 *                        aspect ratio with primary dimension
	 * @param actualPrimary   Actual size of the primary dimension
	 * @param actualSecondary Actual size of the secondary dimension
	 */
	private static int getResizedDimension(int maxPrimary, int maxSecondary, int actualPrimary, int actualSecondary) {
		double ratio = (double) actualSecondary / (double) actualPrimary;
		int resized = maxPrimary;
		if (resized * ratio > maxSecondary) {
			resized = (int) (maxSecondary / ratio);
		}
		return resized;
	}

}
