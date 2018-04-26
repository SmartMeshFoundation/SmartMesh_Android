package com.lingtuan.firefly.walletold;


import android.graphics.Bitmap;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.lingtuan.firefly.util.BitmapUtils;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.wallet.util.WalletStorage;
import com.lingtuan.firefly.wallet.vo.StorableWallet;

import java.util.ArrayList;
import java.util.Hashtable;

public class OldQuickMarkShowUI extends BaseActivity implements TextWatcher {

	private ImageView mQuickMark;
	private EditText mAmount;
	private int type;//0 二维码  1   ETH 二维码   2  SMT二维码
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
		mAmount = (EditText) findViewById(R.id.amount);
		walletImg = (ImageView) findViewById(R.id.walletImg);
		walletAddress = (TextView) findViewById(R.id.walletAddress);
		copyAddress = (TextView) findViewById(R.id.copyAddress);
		appRight = (TextView) findViewById(R.id.app_btn_right);
	}

	@Override
	protected void setListener() {
		mAmount.addTextChangedListener(this);
		copyAddress.setOnClickListener(this);
		appRight.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()){
			case R.id.copyAddress:
				Utils.copyText(OldQuickMarkShowUI.this,walletAddress.getText().toString());
				break;
			case R.id.app_btn_right:
				try {
					String qrPath = BitmapUtils.uploadZxing(OldQuickMarkShowUI.this,qrBitmap,true,false);
					showToast(qrPath);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
		}
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
		if (type == 0){
			content = address + Constants.AOMUNT_ETH;
		}else if (type == 1){
			content = address + Constants.AOMUNT_SMT;
		}else if (type == 2){
			content = address + Constants.AOMUNT_MESH;
		}
		initWalletInfo();
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
				int imgId = Utils.getWalletImg(OldQuickMarkShowUI.this,i);
				storableWallet = storableWallets.get(i);
				if (storableWallet.getImgId() == 0){
					storableWallet.setImgId(imgId);
					walletImg.setImageResource(imgId);
				}else{
					walletImg.setImageResource(storableWallet.getImgId());
				}
				break;
			}
		}
		if (index == -1 && storableWallets.size() > 0){
			int imgId = Utils.getWalletImg(OldQuickMarkShowUI.this,0);
			storableWallet = storableWallets.get(0);
			if (storableWallet.getImgId() == 0){
				storableWallet.setImgId(imgId);
				walletImg.setImageResource(imgId);
			}else{
				walletImg.setImageResource(storableWallet.getImgId());
			}
		}
		if (storableWallet != null){
			String address = storableWallet.getPublicKey();
			if(!address.startsWith("0x")){
				address = "0x"+address;
			}
			walletAddress.setText(address);
		}
	}


	private Bitmap createQRCodeBitmap(String content , int widthAndHeight) {
		Hashtable<EncodeHintType, Object> qrParam = new Hashtable<>();
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

	//change qecode
	private void chengeQrCode(Editable s){
		String content = address;
		if (type == 0){
			content = address+"?amount="+s.toString()+"&token=ETH";
		}else if (type == 1){
			content = address+"?amount="+s.toString()+"&token=SMT";
		}else if (type == 2){
			content = address+"?amount="+s.toString()+"&token=MESH";
		}
		qrBitmap= createQRCodeBitmap(content ,getResources().getDisplayMetrics().widthPixels);
		mQuickMark.setImageBitmap(qrBitmap);
	}
}
