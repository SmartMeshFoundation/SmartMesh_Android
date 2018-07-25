package com.lingtuan.firefly.quickmark;


import android.graphics.Bitmap;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.quickmark.contract.QuickMarkShowContract;
import com.lingtuan.firefly.quickmark.presenter.QuickMarkShowPresenterImpl;
import com.lingtuan.firefly.util.BitmapUtils;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.wallet.vo.StorableWallet;
import com.lingtuan.firefly.wallet.vo.TokenVo;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;

public class QuickMarkShowUI extends BaseActivity implements QuickMarkShowContract.View{

	@BindView(R.id.quickmark)
	ImageView mQuickMark;
	@BindView(R.id.walletImg)
	ImageView walletImg;
	@BindView(R.id.walletAddress)
	TextView walletAddress;

    private String address;
	private TokenVo tokenVo;
	private StorableWallet storableWallet;
	private Bitmap qrBitmap;

	private QuickMarkShowContract.Presenter mPresenter;

	@Override
	protected void setContentView() {
		setContentView(R.layout.quickmark_show);
	}


	@Override
	protected void findViewById() {

	}

	@Override
	protected void setListener() {

	}

	@Override
	protected void initData() {
		new QuickMarkShowPresenterImpl(this);
		setTitle(getString(R.string.gathering));
		Utils.setStatusBar(QuickMarkShowUI.this,1);
		tokenVo = (TokenVo) getIntent().getSerializableExtra("tokenVo");
		address = getIntent().getStringExtra("address");
		if(!TextUtils.isEmpty(address) && !address.startsWith("0x")){
			address = "0x"+address;
		}
		String content = address+"?amount=&token=" + tokenVo.getTokenSymbol();
		initWalletInfo();
		qrBitmap= mPresenter.createQRCodeBitmap(content ,getResources().getDisplayMetrics().widthPixels);
		mQuickMark.setImageBitmap(qrBitmap);
		Utils.settingLanguage(QuickMarkShowUI.this);
	}

	@OnClick({R.id.copyAddress,R.id.app_btn_right})
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()){
			case R.id.copyAddress:
				Utils.copyText(QuickMarkShowUI.this,walletAddress.getText().toString());
				break;
			case R.id.app_btn_right:
				try {
					String qrPath = BitmapUtils.uploadZxing(QuickMarkShowUI.this,qrBitmap,true,false);
					showToast(qrPath);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
		}
	}


	@OnTextChanged(value = {R.id.amount},callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
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
		String content = address+"?amount="+s.toString()+"&token=" + tokenVo.getTokenSymbol();
		qrBitmap= mPresenter.createQRCodeBitmap(content ,getResources().getDisplayMetrics().widthPixels);
		mQuickMark.setImageBitmap(qrBitmap);
	}

	/**
	 * Load or refresh the wallet information
	 * */
	private void initWalletInfo(){
		storableWallet = mPresenter.getWallet();
		if (storableWallet != null){
			walletImg.setImageResource(Utils.getWalletImageId(QuickMarkShowUI.this,storableWallet.getWalletImageId()));
			String address = storableWallet.getPublicKey();
			if(!address.startsWith("0x")){
				address = "0x"+address;
			}
			walletAddress.setText(address);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if(qrBitmap != null  && !qrBitmap.isRecycled()){
			qrBitmap.recycle();
			qrBitmap = null ;
		}
	}

	@Override
	public void setPresenter(QuickMarkShowContract.Presenter presenter) {
		this.mPresenter = presenter;
	}
}
