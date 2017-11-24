package com.lingtuan.firefly.imagescan;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.imagescan.PhotoView;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.File;
import java.util.List;

/**
 * Preview picture adapter
 */
public class ScanPicAdapter extends PagerAdapter {


	private List<String> imageList;
	private Button btPhoto;
	
	private FinishCallBack fcb = null;
	
	private ShowTitleCallBack stcb = null ; 
	
	private  OnLongClickCallBack longClickCallback = null ; 
	
	/**Whether to show the Title*/
	private boolean isShowTitle = false ; 
	
	/**Whether in the preview of our own image, which is used to judge whether to close the current image preview, was shut down, or hide the Title*/
	private boolean isOurSelf = false ; 
	
	public interface FinishCallBack {
		void finishCallBack();
	}
	
	public interface DeleteCallBack{
		void deleteCallBack(int position);
	}
	
	public interface ShowTitleCallBack{
		void showTitleCallBack(String currentText, boolean isShowTitle);
	}
	
	public interface OnLongClickCallBack{
		void onLongClickCallback(String oldPath);
	}
	
	public void resetSource(List<String> imageList){
		this.imageList = imageList;
		notifyDataSetChanged();
	}
	
	public void setFinishCallback(FinishCallBack fcb) {
		this.fcb = fcb;
	}
	
	public void setShowTitleCallBack(ShowTitleCallBack stcb){
		this.stcb = stcb;
	}
	
	public void setOnLongClickCallBack(OnLongClickCallBack longClickCallback){
		this.longClickCallback = longClickCallback ; 
	}
	private Context context;
	public ScanPicAdapter(List<String> imageList, Context c, boolean isOurSelf){
		this.imageList = imageList;
		this.isOurSelf = isOurSelf;
		this.btPhoto = new Button(c);
		this.context=c;
	
	}
	@Override
	public int getCount() {
		if(imageList == null ){
			return 0 ;
		}
		return imageList.size();
	}

	@Override
	public View instantiateItem(ViewGroup container, final int position) {
        FrameLayout imageLayout = (FrameLayout) View.inflate(context,R.layout.item_photo_image,null);
        final PhotoView photoView = new PhotoView(context, btPhoto);
        ViewGroup.LayoutParams lp=new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        imageLayout.addView(photoView,lp);
        final ProgressBar spinner = (ProgressBar) imageLayout.findViewById(R.id.loading);
        btPhoto.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				if(isOurSelf && stcb != null ){
						isShowTitle = !isShowTitle;
						stcb.showTitleCallBack(String.valueOf(getCount()), isShowTitle);
				}else{
					//close
					if(fcb != null ){
						fcb.finishCallBack();
					}
				}
			}
		});
       
        photoView.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View arg0) {
				
				final String oldPath = NextApplication.mImageLoader.getDiscCache().get(imageList.get(position)).getPath();
				File oldfile = new File(oldPath);
				if (!oldfile.exists()) {//Picture has not been downloaded
					return true;
				}
				
				if(longClickCallback != null ){
					longClickCallback.onLongClickCallback(oldPath);
				}
				return true;
			}
		});
        NextApplication.displayNothing(photoView, imageList.get(position),new ImageLoadingListener() {
        	
			@Override
			public void onLoadingStarted(String imageUri, View view) {
				spinner.setVisibility(View.VISIBLE);
			}

			@Override
			public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
				spinner.setVisibility(View.GONE);
			}

			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				spinner.setVisibility(View.GONE);
			}

			@Override
			public void onLoadingCancelled(String imageUri, View view) {
				spinner.setVisibility(View.GONE);
			}
			});
		container.addView(imageLayout, 0);
		return imageLayout;
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();

	}

	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}

}
