package com.lingtuan.firefly.quickmark;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.quickmark.camera.CameraManager;
import com.lingtuan.firefly.quickmark.control.AmbientLightManager;
import com.lingtuan.firefly.quickmark.control.BeepManager;
import com.lingtuan.firefly.quickmark.decode.CaptureActivityHandler;
import com.lingtuan.firefly.quickmark.decode.FinishListener;
import com.lingtuan.firefly.quickmark.decode.InactivityTimer;
import com.lingtuan.firefly.quickmark.view.ViewfinderView;
import com.lingtuan.firefly.wallet.WalletSendActivity;

import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

/**
 * Scan the page
 */
public final class CaptureActivity extends BaseActivity implements
		SurfaceHolder.Callback {
	private Button btn_back;
	private Button btn_torch;
	private boolean isTorchOn = false;
	private CameraManager cameraManager;
	private CaptureActivityHandler handler;
	private Result savedResultToShow;
	private ViewfinderView viewfinderView;
	private boolean hasSurface;
	private Collection<BarcodeFormat> decodeFormats;
	private Map<DecodeHintType, ?> decodeHints;
	private String characterSet;
	private InactivityTimer inactivityTimer;
	private BeepManager beepManager;
	private AmbientLightManager ambientLightManager;

	private String photo_path;
	private Bitmap scanBitmap;
	private Dialog mProgressDialog;
	
	private ImageView photoIcon;
	
	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public CameraManager getCameraManager() {
		return cameraManager;
	}

    private  int type;//   1 forresult 2 watch the purse
	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();
		cameraManager = new CameraManager(getApplication());
		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		viewfinderView.setCameraManager(cameraManager);
		handler = null;
		resetStatusView();
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(surfaceHolder);
		} else {
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

		beepManager.updatePrefs();
		ambientLightManager.start(cameraManager);

		inactivityTimer.onResume();

		decodeFormats = null;
		characterSet = null;
	}

	@Override
	protected void onPause() {
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		inactivityTimer.onPause();
		ambientLightManager.stop();
		cameraManager.closeDriver();
		if (!hasSurface) {
			SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
			SurfaceHolder surfaceHolder = surfaceView.getHolder();
			surfaceHolder.removeCallback(this);
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		viewfinderView.recycleLineDrawable();
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_CAMERA:
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void decodeOrStoreSavedBitmap(Bitmap bitmap, Result result) {
		if (handler == null) {
			savedResultToShow = result;
		} else {
			if (result != null) {
				savedResultToShow = result;
			}
			if (savedResultToShow != null) {
				Message message = Message.obtain(handler,
						R.id.decode_succeeded, savedResultToShow);
				handler.sendMessage(message);
			}
			savedResultToShow = null;
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
							   int height) {

	}

	public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
		inactivityTimer.onActivity();
		beepManager.playBeepSoundAndVibrate();

		String msg = rawResult.getText();
		if(!TextUtils.isEmpty(msg) && msg.startsWith("0x"))
		{
			if(msg.length() == 42)
			{
				if(type == 1)
				{
					Intent i = new Intent();
					i.putExtra("address",msg);
					i.putExtra("sendtype", 0);
					setResult(RESULT_OK,i);
					finish();
				}else if (type == 2){
					Intent i = new Intent();
					i.putExtra("address",msg);
					setResult(RESULT_OK,i);
					finish();
				}
				else{
					Intent intent = new Intent(this,WalletSendActivity.class);
					intent.putExtra("address", msg);
					intent.putExtra("sendtype", 0);
					startActivity(intent);
					finish();
				}
			}
			else if(msg.length() > 42)
			{
				if (type == 2){
					finish();
					return;
				}
				String address = msg.substring(0,msg.indexOf("?"));
				Uri parse = Uri.parse(msg);
				int sendtype = 0;
				float amount = 0f;
				if(!TextUtils.isEmpty(parse.getQueryParameter("token")))
				{
					if(parse.getQueryParameter("token").equals("SMT"))
					{
						sendtype = 1;
					}
				}
				if(!TextUtils.isEmpty(parse.getQueryParameter("amount")))
				{
					amount = Float.valueOf(parse.getQueryParameter("amount"));
				}

				if(type == 1)
				{
					Intent i = new Intent();
					i.putExtra("address",address);
					i.putExtra("sendtype", sendtype);
					i.putExtra("amount", amount);
					setResult(RESULT_OK,i);
					finish();
				}
				else{
					Intent intent = new Intent(this,WalletSendActivity.class);
					intent.putExtra("address", address);
					intent.putExtra("sendtype", sendtype);
					intent.putExtra("amount", amount);
					startActivity(intent);
					finish();
				}
			}
			else{
				finish();
			}

		}
		else{
			finish();
		}
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		if (surfaceHolder == null) {
			return;
		}
		if (cameraManager.isOpen()) {
			return;
		}
		try {
			cameraManager.openDriver(surfaceHolder);
			if (handler == null) {
				handler = new CaptureActivityHandler(this, decodeFormats,decodeHints, characterSet, cameraManager);
			}
			decodeOrStoreSavedBitmap(null, null);
		} catch (IOException ioe) {
			displayFrameworkBugMessageAndExit();
		} catch (RuntimeException e) {
			displayFrameworkBugMessageAndExit();
		}
	}

	private void displayFrameworkBugMessageAndExit() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("");
		builder.setMessage("");
		builder.setPositiveButton("", new FinishListener(this));
		builder.setOnCancelListener(new FinishListener(this));
		builder.show();
	}

	private void resetStatusView() {
		viewfinderView.setVisibility(View.VISIBLE);
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();
	}
	
	/**
	 * Scan the qr code image method
	 */
	public Result scanningImage(String path) {
		if(TextUtils.isEmpty(path)){
			return null;
		}
		Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
		hints.put(DecodeHintType.CHARACTER_SET, "UTF8"); //Set the qr code coding content

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true; // To obtain the original size
		scanBitmap = BitmapFactory.decodeFile(path, options);
		options.inJustDecodeBounds = false; // To get the new size
		int sampleSize = (int) (options.outHeight / (float) 200);
		if (sampleSize <= 0)
			sampleSize = 1;
		options.inSampleSize = sampleSize;
		scanBitmap = BitmapFactory.decodeFile(path, options);
		RGBLuminanceSource source = new RGBLuminanceSource(scanBitmap);
		BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
		QRCodeReader reader = new QRCodeReader();
		try {
			return reader.decode(bitmap1, hints);

		} catch (NotFoundException e) {
			e.printStackTrace();
		} catch (ChecksumException e) {
			e.printStackTrace();
		} catch (FormatException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void setContentView() {
		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.quick_mark_layout);
	}

	@Override
	protected void findViewById() {

	}

	@Override
	protected void setListener() {

	}


	protected void initData() {
		setTitle(getString(R.string.qm_qm));
		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);
		beepManager = new BeepManager(this);
		ambientLightManager = new AmbientLightManager(this);
		type = getIntent().getIntExtra("type",0);
		
	}

	
}
