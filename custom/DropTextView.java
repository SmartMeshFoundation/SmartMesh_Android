package com.lingtuan.firefly.custom;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.util.Utils;


public class DropTextView extends FrameLayout implements View.OnClickListener,OnItemClickListener{
	private TextView mTextView; // Display box
	private ImageView mDropImage; // Button to the right of the image
	private PopupWindow mPopup; // Click on the image popupwindow
	private WrapListView mPopView; // The layout of the popupwindow
	private int mDrawableLeft;
	private int mDropMode; // flow_parent or wrap_content
	private String mHit;

	private OnItemListener onItemListener ;

	public DropTextView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		init(context);
	}

	public DropTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
		LayoutInflater.from(context).inflate(R.layout.textview_layout, this);
		mPopView = (WrapListView) LayoutInflater.from(context).inflate(R.layout.pop_view, null);

		TypedArray ta = context.obtainStyledAttributes(attrs,R.styleable.DropTextView, defStyle, 0);
		mDrawableLeft = ta.getResourceId(R.styleable.DropTextView_drawableRight, R.drawable.arrow);
		mDropMode = ta.getInt(R.styleable.DropTextView_dropMode, 0);
		mHit = ta.getString(R.styleable.DropTextView_hint);
		ta.recycle();
	}

	private void init(Context context) {
		mPopup = new PopupWindow(mPopView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		mPopup.setBackgroundDrawable(getResources().getDrawable(R.color.transparent));
		mPopup.setFocusable(true); // Let popwin get focus
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mTextView = (TextView) findViewById(R.id.dropview_edit);
		mDropImage = (ImageView) findViewById(R.id.dropview_image);
		mTextView.setSelectAllOnFocus(true);
		mDropImage.setImageResource(mDrawableLeft);

		if (!TextUtils.isEmpty(mHit)) {
			mTextView.setHint(mHit);
		}

		mTextView.setOnClickListener(this);
		mDropImage.setOnClickListener(this);
		mPopView.setOnItemClickListener(this);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		// if the layout change
		// and dropMode is flower_parent
		// set the width of the ListView
		if (changed && 0 == mDropMode) {
			mPopView.setListWidth(getMeasuredWidth());
		}
	}

	/**
	 * Set the Adapter
	 */
	public void setAdapter(BaseAdapter adapter) {
		mPopView.setAdapter(adapter);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		mPopup.setWidth(MeasureSpec.getSize(widthMeasureSpec));
		mPopup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	/**
	 * Access to the content of the input box
	 * @return String content
	 */
	public String getText() {
		return mTextView.getText().toString();
	}

	public void setText(String text) {
		mTextView.setText(text);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.dropview_image || v.getId() == R.id.dropview_edit) {
			if (mPopup.isShowing()) {
				mPopup.dismiss();
				return;
			}
			mPopup.showAsDropDown(this, 0, 0);
			Utils.hiddenKeyBoard((Activity)getContext());
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//		mTextView.setText(mPopView.getAdapter().getItem(position).toString());
		if (onItemListener != null){
			onItemListener.onItemListener(position);
		}
		mPopup.dismiss();
	}


	public interface OnItemListener{
		void onItemListener(int position);
	}
	public void setOnItemListener(OnItemListener onItemListener){
         this.onItemListener = onItemListener;
	}
}
