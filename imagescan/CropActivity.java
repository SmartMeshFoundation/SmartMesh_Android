package com.lingtuan.firefly.imagescan;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.custom.ClipImageView;
import com.lingtuan.firefly.util.BitmapUtils;
import com.lingtuan.firefly.util.Utils;

/**
 * Callback key = photourl cut images
 */
public class CropActivity extends Activity {

	private ClipImageView imageView;
	private String photoUri;
	private boolean hasClickRightBtn;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.crop_layout);
		TextView title=(TextView) findViewById(R.id.app_title);
		title.setText(getResources().getString(R.string.title_crop_image));
		TextView rightBtn = (TextView) findViewById(R.id.app_btn_right);
		if(getIntent().getBooleanExtra("isCaptureQR", false))
		{
			 rightBtn.setText(getString(R.string.submit));
		}
		else{
			 rightBtn.setText(getString(R.string.save));
		}
		rightBtn.setVisibility(View.VISIBLE);
		photoUri=getIntent().getStringExtra("photoUri");
		if(photoUri != null)
		{
			try {
				Bitmap bitmap = BitmapUtils.getimage(photoUri);
				Bitmap compressBit = BitmapUtils.loadBitmap(photoUri, BitmapUtils.compressImage(bitmap));
				if (bitmap != null) {
					bitmap.recycle();
					bitmap = null;
				}
				imageView = (ClipImageView) findViewById(R.id.src_pic);
				imageView.setImageBitmap(compressBit);
				rightBtn.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(hasClickRightBtn)
						{
							return;
						}
						hasClickRightBtn=true;
						Bitmap bitmap = imageView.clip();
						String url = BitmapUtils.saveBitmap2SD(bitmap,true).getPath();
						
						Intent data = new Intent();
						data.putExtra("photourl", url);
						setResult(RESULT_OK, data);
						Utils.exitActivityAndBackAnim(CropActivity.this, true);
					}
				});
			} catch (Error e) {
				e.printStackTrace();
				finish();
			}catch (Exception e) {
				e.printStackTrace();
				finish();
			}
		}
		findViewById(R.id.app_back).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Utils.exitActivityAndBackAnim(CropActivity.this, true);
			}
		});
		
	}
}

