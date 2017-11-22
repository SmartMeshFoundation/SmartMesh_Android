package com.lingtuan.firefly.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.lingtuan.firefly.R;


public class CountryCodeSideBar extends View {
	// Touch events
	private OnTouchingLetterChangedListener onTouchingLetterChangedListener;
	// 26 letters
	public static String[] b = { "A", "B", "C", "D", "E", "F", "G", "H", "I",
			"J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
			"W", "X", "Y", "Z","#"};
	private int choose = -1;// selected
	private Paint paint = new Paint();

	private TextView mTextDialog;

	private Context context;
	private float mScaledDensity;
	public void setTextView(TextView mTextDialog) {
		this.mTextDialog = mTextDialog;
	}


	public CountryCodeSideBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context=context;
		mScaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
	}

	public CountryCodeSideBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context=context;
		mScaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
	}

	public CountryCodeSideBar(Context context) {
		super(context);
		this.context=context;
		mScaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
	}

	/**
	 * Rewrite the method
	 */
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// Get focus change background color.
		int height = getHeight();// To obtain corresponding height
		int width = getWidth(); // To obtain corresponding to the width
		int singleHeight = height / b.length;// Get the height of each letter

		for (int i = 0; i < b.length; i++) {
			paint.setColor(context.getResources().getColor(R.color.textColorHintPrimary));
			// paint.setColor(Color.WHITE);
//			paint.setTypeface(Typeface.DEFAULT_BOLD);
			paint.setAntiAlias(true);
			paint.setTextSize(12*mScaledDensity);
			// The state of the selected
			if (i == choose) {
				paint.setColor(context.getResources().getColor(R.color.black));
				paint.setFakeBoldText(true);
			}
			//X-coordinate of the middle - half the width of a string.
			float xPos = width / 2 - paint.measureText(b[i]) / 2;
			float yPos = singleHeight * i + singleHeight;
			canvas.drawText(b[i], xPos, yPos, paint);
			paint.reset();// Reset brushes
		}

	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		final int action = event.getAction();
		final float y = event.getY();// Click on the y coordinate
		final int oldChoose = choose;
		final OnTouchingLetterChangedListener listener = onTouchingLetterChangedListener;
		final int c = (int) (y / getHeight() * b.length);// Click on the y coordinate of the proportion of the total height * b is equal to the length of the array click on the number of b.
		switch (action) {
		case MotionEvent.ACTION_UP:
			setBackgroundDrawable(new ColorDrawable(0x00000000));
			choose = -1;//
			invalidate();
			if (mTextDialog != null) {
				mTextDialog.setVisibility(View.INVISIBLE);
			}
			break;

		default:
//TODO		setBackgroundResource(R.drawable.sidelbar_background);
			if (oldChoose != c) {
				if (c >= 0 && c < b.length) {
					if (listener != null) {
						listener.onTouchingLetterChanged(b[c]);
					}
					if (mTextDialog != null) {
						mTextDialog.setText(b[c]);
						mTextDialog.setVisibility(View.VISIBLE);
					}
					
					choose = c;
					invalidate();
				}
			}

			break;
		}
		return true;
	}

	/**
	 * The method of open outward
	 * 
	 * @param onTouchingLetterChangedListener
	 */
	public void setOnTouchingLetterChangedListener(
			OnTouchingLetterChangedListener onTouchingLetterChangedListener) {
		this.onTouchingLetterChangedListener = onTouchingLetterChangedListener;
	}

	/**
	 * interface
	 * @author coder
	 */
	public interface OnTouchingLetterChangedListener {
		void onTouchingLetterChanged(String s);
	}

}