package com.lingtuan.firefly.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.util.Utils;

public class DropEditText extends FrameLayout implements  View.OnFocusChangeListener, View.OnClickListener, TextWatcher, AdapterView.OnItemClickListener {
	private EditText mEditText; // 输入框
	private PopupWindow mPopup; // 点击图片弹出popupwindow
	private WrapListView mPopView; // popupwindow的布局
	private String mHit;

	private ImageView walletAddressDelete;

	private ScrollView scrollView;


	public DropEditText(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		init(context);
	}

	public DropEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
		LayoutInflater.from(context).inflate(R.layout.edit_layout, this);
		mPopView = (WrapListView) LayoutInflater.from(context).inflate(R.layout.pop_view, null);
		TypedArray ta = context.obtainStyledAttributes(attrs,R.styleable.DropEditText, defStyle, 0);
		mHit = ta.getString(R.styleable.DropEditText_hint);
		ta.recycle();
	}

	private void init(Context context) {
		mPopup = new PopupWindow(mPopView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		mPopup.setOutsideTouchable(false);
		mPopup.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
		mPopup.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		mPopup.setFocusable(true); // 让popwin获取焦点
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mEditText = findViewById(R.id.dropview_edit);
		walletAddressDelete = findViewById(R.id.walletAddressDelete);
		mEditText.setSelectAllOnFocus(true);
		if (!TextUtils.isEmpty(mHit)) {
			mEditText.setHint(mHit);
		}
		mEditText.setOnFocusChangeListener(this);
		mEditText.addTextChangedListener(this);
		walletAddressDelete.setOnClickListener(this);
		mPopView.setOnItemClickListener(this);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
	}

	/**
	 * 设置Adapter
	 * 
	 * @param adapter
	 * ListView的Adapter
	 */
	public void setAdapter(BaseAdapter adapter, ScrollView scrollView,String address) {
		this.scrollView = scrollView;
		mPopView.setAdapter(adapter);
		if (!TextUtils.isEmpty(address)){
			if (mPopup.isShowing()) {
				mPopup.dismiss();
			}
			return;
		}

		if (mPopView.getAdapter() != null && mPopView.getAdapter().getCount() > 0) {
			if (mPopup.isShowing()) {
				mPopup.dismiss();
				return;
			}
			showPow();
		}
	}

	/**
	 * 设置Adapter
	 * ListView的Adapter
	 */
	public void onActivityResult(String address) {
		if (!TextUtils.isEmpty(address)){
			if (mPopup != null && mPopup.isShowing()) {
				mPopup.dismiss();
			}
		}
	}


	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		mPopup.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
		mPopup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	/**
	 * 获取输入框内的内容
	 * @return String content
	 */
	public String getText() {
		return mEditText.getText().toString();
	}

	public void setText(String text) {
		mEditText.setText(text);
		mEditText.setSelection(mEditText.getText().toString().length());
		walletAddressDelete.setVisibility(VISIBLE);
	}

	public void dismissPopup(){
		if (mPopup.isShowing()) {
			mPopup.dismiss();
		}
	}

	public void requestFocusAgain(){
		mEditText.setFocusable(true);
		mEditText.setFocusableInTouchMode(true);
		mEditText.requestFocus();
	}


	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (hasFocus){
			if (v.getId() == R.id.dropview_edit && mPopView.getAdapter() != null && mPopView.getAdapter().getCount() > 0) {
				showPow();
			}
		}
	}

	private void showPow(){
		mPopup.showAsDropDown(this, 0, 1);
		mPopup.setFocusable(false);
		mEditText.setFocusable(true);
		mEditText.setFocusableInTouchMode(true);
		mEditText.requestFocus();
		if (scrollView != null){
			scrollView.post(new Runnable() {
				@Override
				public void run() {
					if(scrollView != null){
						int offset = Utils.dip2px(NextApplication.mContext,100);//偏移值
						scrollView.smoothScrollTo(0, offset);
					}
				}
			});
		}
	}

	@Override
	public void onClick(View v) {
		mEditText.setText("");
		walletAddressDelete.setVisibility(GONE);
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {

	}

	@Override
	public void afterTextChanged(Editable s) {
		if (s != null && s.length() > 0){
			walletAddressDelete.setVisibility(VISIBLE);
		}else{
			walletAddressDelete.setVisibility(GONE);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (position == mPopView.getAdapter().getCount()) {
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					Utils.showKeyBoard(mEditText);
				}
			}, 200);
			mEditText.setText("");
			walletAddressDelete.setVisibility(GONE);
		} else {
			mEditText.setText(mPopView.getAdapter().getItem(position).toString());
			walletAddressDelete.setVisibility(VISIBLE);
			mEditText.setSelection(mEditText.getText().toString().length());
		}
		mPopup.dismiss();
	}
}
