package com.lingtuan.firefly.quickmark;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;

import java.util.Hashtable;

public class QuickMarkShowUI extends BaseActivity implements TextWatcher {

	private ImageView mQuickMark;
	private EditText mAmount;
	private LinearLayout mAmountBg;
	private TextView unit;
	private int type;//Qr code 0, 1 ETH qr code, 2 FFT qr code
    private String address;
	@Override
	protected void setContentView() {
		setContentView(R.layout.quickmark_show);
	}


	@Override
	protected void findViewById() {

		mQuickMark = (ImageView) findViewById(R.id.quickmark);
		mAmount = (EditText) findViewById(R.id.amount);
		mAmountBg = (LinearLayout) findViewById(R.id.amount_bg);
		unit = (TextView) findViewById(R.id.unit);
	}

	@Override
	protected void setListener() {
		mAmount.addTextChangedListener(this);
	}

	@Override
	protected void initData() {
		setTitle(getString(R.string.gathering));
		type = getIntent().getIntExtra("type",0);
		address = getIntent().getStringExtra("address");
		if(!TextUtils.isEmpty(address) && !address.startsWith("0x"))
		{
			address = "0x"+address;
		}
		String content = address;
		if(type == 0)
		{
			mAmountBg.setVisibility(View.GONE);
		}else if (type == 1){
			mAmountBg.setVisibility(View.VISIBLE);
			unit.setText("ETH");
			content = address+"?amount=&token=ETH";
		}
		else if (type == 2){
			mAmountBg.setVisibility(View.VISIBLE);
			unit.setText("SMT");
			content = address+"?amount=&token=SMT";
		}

		Bitmap qrBitmap= createQRCodeBitmap(content ,getResources().getDisplayMetrics().widthPixels);
		mQuickMark.setImageBitmap(qrBitmap);
	}
	private Bitmap createQRCodeBitmap(String content , int widthAndHeight) {
		Hashtable<EncodeHintType, Object> qrParam = new Hashtable<EncodeHintType, Object>();
		qrParam.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
		qrParam.put(EncodeHintType.CHARACTER_SET, "utf-8");
		try {
			BitMatrix bitMatrix = new MultiFormatWriter().encode(content,
					BarcodeFormat.QR_CODE, widthAndHeight, widthAndHeight, qrParam);
			int w = bitMatrix.getWidth();
			int h = bitMatrix.getHeight();
			int[] data = new int[w * h];

			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					if (bitMatrix.get(x, y))
						data[y * w + x] = 0xff000000;// ��ɫ
					else
						data[y * w + x] = -1;
				}
			}

			Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
			bitmap.setPixels(data, 0, w, 0, 0, w, h);
			return bitmap;
		} catch (WriterException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void createQRCodeBitmapWithPortrait(Bitmap qr, Bitmap portrait,int widthAndHeight) {
		int portrait_W = portrait.getWidth();
		int portrait_H = portrait.getHeight();

		int left = (widthAndHeight - portrait_W) / 2;
		int top = (widthAndHeight - portrait_H) / 2;
		int right = left + portrait_W;
		int bottom = top + portrait_H;
		Rect rect1 = new Rect(left, top, right, bottom);
		Canvas canvas = new Canvas(qr);
		Rect rect2 = new Rect(0, 0, portrait_W, portrait_H);
		canvas.drawBitmap(portrait, rect2, rect1, null);
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {

	}

	@Override
	public void afterTextChanged(Editable s) {

		String amountCash = s.toString();
		int length = amountCash.length();

		if(length >= 8 ){
			String s2 = amountCash.substring(length - 8, length -7) ;
			if(TextUtils.equals(".", s2)){
				s.delete(length-1, length);
			}else{
				chengeQrCode(s);
			}
		}else{
			chengeQrCode(s);
		}
	}

	//Change the qr code
	private void chengeQrCode(Editable s){
		String content = address;
		if(type == 0)
		{

		}else if (type == 1){
			content = address+"?amount="+s.toString()+"&token=ETH";
		}
		else if (type == 2){
			content = address+"?amount="+s.toString()+"&token=SMT";
		}
		Bitmap qrBitmap= createQRCodeBitmap(content ,getResources().getDisplayMetrics().widthPixels);
		mQuickMark.setImageBitmap(qrBitmap);
	}
}
