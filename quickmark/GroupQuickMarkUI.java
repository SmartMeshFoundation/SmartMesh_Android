package com.lingtuan.firefly.quickmark;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.contact.ContactSelectedUI;
import com.lingtuan.firefly.custom.DiscussGroupImageView;
import com.lingtuan.firefly.util.BitmapUtils;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.MyPopupWindow;
import com.lingtuan.firefly.util.MyToast;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.vo.ChatMsg;
import com.lingtuan.firefly.vo.ShareVo;
import com.lingtuan.firefly.vo.UserBaseVo;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * group quick mark ui<br>
 */
public class GroupQuickMarkUI extends BaseActivity {

	private List<UserBaseVo> avatarList;
	private String avatarUrl;
	private String id;
	private String nickname;
	private int number;

	private TextView mNickname;
	private DiscussGroupImageView mGroupAvatar;
	private ImageView mQuickMark;
	private LinearLayout quickmarkBody;
	private LinearLayout discussGroupBody;
	private LinearLayout discussScan;
	private TextView discussNumber;

	private int width;
	private int type;

	private TextView shareTv;


	@Override
	protected void setContentView() {
		setContentView(R.layout.discuss_group_quickmark);
	}

	@SuppressLint("NewApi")
	@Override
	protected void findViewById() {

		mNickname = (TextView) findViewById(R.id.nickname);
		mQuickMark = (ImageView) findViewById(R.id.quickmark);
		quickmarkBody = (LinearLayout) findViewById(R.id.quickmarkBody);
		discussGroupBody = (LinearLayout) findViewById(R.id.discussGroupBody);
		discussScan = (LinearLayout) findViewById(R.id.discussScan);
		mGroupAvatar = (DiscussGroupImageView) findViewById(R.id.avatar_group);
		shareTv = (TextView) findViewById(R.id.app_btn_right);
		discussNumber = (TextView) findViewById(R.id.discussNumbers);
	}

	@Override
	protected void setListener() {
		shareTv.setOnClickListener(this);
		discussScan.setOnClickListener(this);
	}

	@Override
	protected void initData() {

		width = getResources().getDisplayMetrics().widthPixels;
		nickname = getIntent().getStringExtra("nickname");
		type = getIntent().getIntExtra("type",0);
		id = getIntent().getStringExtra("id");
		avatarUrl = getIntent().getStringExtra("avatarurl");
		number = getIntent().getIntExtra("number",0);
		avatarList = new ArrayList<>();

		if(type==1){
			setTitle(getString(R.string.title_quickmark));
		}

		mNickname.setText(nickname);

		discussNumber.setText(getString(R.string.discuss_group_member_num,number));
		LayoutParams paramsBody = new LayoutParams(width / 3 * 2 + Utils.dip2px(GroupQuickMarkUI.this,36), width / 3 * 2 + Utils.dip2px(GroupQuickMarkUI.this,36));
		paramsBody.gravity = Gravity.CENTER;
		quickmarkBody.setLayoutParams(paramsBody);

		LayoutParams params = new LayoutParams(width / 3 * 2 - Utils.dip2px(GroupQuickMarkUI.this,20), width / 3 * 2 - Utils.dip2px(GroupQuickMarkUI.this,20));
		params.gravity = Gravity.CENTER;
		params.setMargins(0,Utils.dip2px(GroupQuickMarkUI.this,18),0,0);
		mQuickMark.setLayoutParams(params);

		LayoutParams paramsDisucssBody = new LayoutParams(width / 3 * 2 + Utils.dip2px(GroupQuickMarkUI.this,36), Utils.dip2px(GroupQuickMarkUI.this,80));
		paramsDisucssBody.gravity = Gravity.CENTER;
		paramsDisucssBody.setMargins(0,Utils.dip2px(GroupQuickMarkUI.this,60),0,0);
		discussGroupBody.setLayoutParams(paramsDisucssBody);

		try {
			String content = "";
			if(type==1){
				content = Constants.URL_DISCUSS + id;
				setDiscussAvatar();
			}
			Bitmap qrBitmap= createQRCodeBitmap(content ,width);
			Bitmap logo = ((BitmapDrawable)getResources().getDrawable(R.mipmap.logo)).getBitmap();
			createQRCodeBitmapWithPortrait(qrBitmap, logo,width);
			mQuickMark.setImageBitmap(qrBitmap);

		} catch (Exception e) {
			e.printStackTrace();
			MyToast.showToast(this, getString(R.string.quickmark_error));
		}
		shareTv.setVisibility(View.VISIBLE);
		shareTv.setText(getString(R.string.share));
	}


	/**
	 * set group avatar
	 */
	private void setDiscussAvatar() {
		mGroupAvatar.setVisibility(View.VISIBLE);
		String[] split = avatarUrl.split("#");
		UserBaseVo vo ;
		for (int i = 0; i < split.length; i++) {
			vo = new UserBaseVo();
			String[] splitVo = split[i].split("___");
			String gender = "2";
			try {
				gender = splitVo[1];
			} catch (Exception e) {
				e.printStackTrace();
			}
			String avatar = "";
			try {
				avatar = splitVo[0];
			} catch (Exception e) {
				e.printStackTrace();
			}
			vo.setGender(gender);
			vo.setThumb(avatar);
			avatarList.add(vo);
		}

		mGroupAvatar.setMember(avatarList);
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
			case R.id.app_btn_right:
				showSharePop();
				break;
			case R.id.discussScan:
				Intent intent1 = new Intent(GroupQuickMarkUI.this, CaptureActivity.class);
				intent1.putExtra("type",3);
				startActivity(intent1);
				Utils.openNewActivityAnim(GroupQuickMarkUI.this, false);
				break;
		}
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
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Utils.hiddenKeyBoard(GroupQuickMarkUI.this);
	}


	@Override
	protected void onResume() {
		Utils.hiddenKeyBoard(GroupQuickMarkUI.this);
		super.onResume();
	}

	/**
	 * share group
	 **/
	public void showSharePop() {
		ArrayList<ShareVo> itemList = new ArrayList<>();
		itemList.add(new ShareVo(getString(R.string.app_name), R.drawable.icon_share_smartmesh));
		itemList.add(new ShareVo(getString(R.string.copy_link), R.drawable.icon_share_link));
		MyPopupWindow.showSharedGroupDialog(GroupQuickMarkUI.this, new MyPopupWindow.ShareCallback() {
			@Override
			public void shareCallback(int index) {
				switch (index) {
					case 0:// share smart mesh app
						shareToSmartMesh();
						break;
					case 1: // share other
						shareOtherApp();
						break;

					default:
						break;
				}
			}
		}, itemList);
	}

	private void shareOtherApp() {
		Utils.copyText(GroupQuickMarkUI.this,getString(R.string.share_group,NextApplication.myInfo.getUserName(),nickname,Constants.URL_DISCUSS + id));
	}

	private void shareToSmartMesh(){
		try {
			String qrPath = BitmapUtils.uploadZxing(GroupQuickMarkUI.this,BitmapUtils.takeScreenShot(GroupQuickMarkUI.this),false,true);
			ArrayList<ChatMsg> list = new ArrayList<>();
			ChatMsg msg = new ChatMsg();
			msg.setType(1);
			msg.setContent(qrPath);
			msg.setLocalUrl(qrPath);
			msg.parseUserBaseVo(NextApplication.myInfo);
			list.add(msg);
			Intent intent = new Intent(GroupQuickMarkUI.this, ContactSelectedUI.class);
			intent.putExtra("msglist", list);
			startActivity(intent);
			Utils.openNewActivityAnim(GroupQuickMarkUI.this, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
