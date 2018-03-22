package com.lingtuan.firefly.quickmark;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Handler;
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
import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.ui.AlertActivity;
import com.lingtuan.firefly.util.BitmapUtils;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.wallet.util.WalletStorage;
import com.lingtuan.firefly.wallet.vo.StorableWallet;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Hashtable;

public class QuickMarkShowUI extends BaseActivity implements TextWatcher {

	private ImageView mQuickMark;
	private EditText mAmount;
	private int type;//Qr code 0, 1 ETH qr code, 2 FFT qr code
    private String address;
	private int index = -1;//Which one is selected

	private ImageView walletImg;
	private TextView walletAddress;
	private TextView copyAddress;

	private StorableWallet storableWallet;

	private TextView appRight;

	private Bitmap qrBitmap;

	@Override
	protected void setContentView() {
		setContentView(R.layout.quickmark_show);
	}


	@Override
	protected void findViewById() {

		mQuickMark = (ImageView) findViewById(R.id.quickmark);
		walletImg = (ImageView) findViewById(R.id.walletImg);
		walletAddress = (TextView) findViewById(R.id.walletAddress);
		copyAddress = (TextView) findViewById(R.id.copyAddress);
		mAmount = (EditText) findViewById(R.id.amount);
		appRight = (TextView) findViewById(R.id.app_btn_right);
	}

	@Override
	protected void setListener() {
		mAmount.addTextChangedListener(this);
		copyAddress.setOnClickListener(this);
		appRight.setOnClickListener(this);
	}

	@Override
	protected void initData() {
		setTitle(getString(R.string.gathering));
		type = getIntent().getIntExtra("type",0);
		address = getIntent().getStringExtra("address");
		if(!TextUtils.isEmpty(address) && !address.startsWith("0x")){
			address = "0x"+address;
		}
		String content = address;
		if(type == 0){

		}else if (type == 1){
			content = address+"?amount=&token=ETH";
		}else if (type == 2){
			content = address+"?amount=&token=SMT";
		}else if (type == 3){
			content = address+"?amount=&token=MESH";
		}
		initWalletInfo();
		qrBitmap= createQRCodeBitmap(content ,getResources().getDisplayMetrics().widthPixels);
		mQuickMark.setImageBitmap(qrBitmap);
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()){
			case R.id.copyAddress:
				if (storableWallet != null && !storableWallet.isBackup()){
					Intent intent = new Intent(QuickMarkShowUI.this, AlertActivity.class);
					intent.putExtra("type", 5);
					intent.putExtra("strablewallet", storableWallet);
					startActivity(intent);
					overridePendingTransition(0, 0);
					return;
				}
				Utils.copyText(QuickMarkShowUI.this,walletAddress.getText().toString());
				break;
			case R.id.app_btn_right:
				try {
					String qrPath = BitmapUtils.uploadZxing(QuickMarkShowUI.this,qrBitmap,true,true);
					showToast(qrPath);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
		}
	}

	private Bitmap createQRCodeBitmap(String content , int widthAndHeight) {
		Hashtable<EncodeHintType, Object> qrParam = new Hashtable<>();
		qrParam.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
		qrParam.put(EncodeHintType.CHARACTER_SET, "utf-8");
		try {
			BitMatrix bitMatrix = new MultiFormatWriter().encode(content,BarcodeFormat.QR_CODE, widthAndHeight, widthAndHeight, qrParam);
			int w = bitMatrix.getWidth();
			int h = bitMatrix.getHeight();
			int[] data = new int[w * h];

			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					if (bitMatrix.get(x, y)){
						data[y * w + x] = 0xff000000;
					}else{
						data[y * w + x] = -1;
					}
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
		if(type == 0){
			content = address+"?amount="+s.toString();
		}else if (type == 1){
			content = address+"?amount="+s.toString()+"&token=ETH";
		}
		else if (type == 2){
			content = address+"?amount="+s.toString()+"&token=SMT";
		}else if (type == 3){
			content = address+"?amount="+s.toString()+"&token=MESH";
		}
		qrBitmap= createQRCodeBitmap(content ,getResources().getDisplayMetrics().widthPixels);
		mQuickMark.setImageBitmap(qrBitmap);
	}

	/**
	 * Load or refresh the wallet information
	 * */
	private void initWalletInfo(){
		ArrayList<StorableWallet> storableWallets = WalletStorage.getInstance(getApplicationContext()).get();
		for (int i = 0 ; i < storableWallets.size(); i++){
			if (storableWallets.get(i).isSelect() ){
				WalletStorage.getInstance(NextApplication.mContext).updateWalletToList(NextApplication.mContext,storableWallets.get(i).getPublicKey(),false);
				index = i;
				int imgId = Utils.getWalletImg(QuickMarkShowUI.this,i);
				walletImg.setImageResource(imgId);
				storableWallet = storableWallets.get(i);
				storableWallet.setImgId(imgId);
				break;
			}
		}
		if (index == -1 && storableWallets.size() > 0){
			int imgId = Utils.getWalletImg(QuickMarkShowUI.this,0);
			walletImg.setImageResource(imgId);
			storableWallet = storableWallets.get(0);
			storableWallet.setImgId(imgId);
		}
		if (storableWallet != null){
			String address = storableWallet.getPublicKey();
			if(!address.startsWith("0x")){
				address = "0x"+address;
			}
			walletAddress.setText(address);
		}
	}
}
