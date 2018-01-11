package com.lingtuan.firefly.setting;

import android.view.View;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.custom.gesturelock.ACache;
import com.lingtuan.firefly.custom.gesturelock.LockPatternIndicator;
import com.lingtuan.firefly.custom.gesturelock.LockPatternUtil;
import com.lingtuan.firefly.custom.gesturelock.LockPatternView;
import com.lingtuan.firefly.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * create gesture activity
 */
public class CreateGestureActivity extends BaseActivity {

	LockPatternIndicator lockPatternIndicator;
	LockPatternView lockPatternView;
	TextView resetBtn;
	TextView messageTv;

	private List<LockPatternView.Cell> mChosenPattern = null;
	private ACache aCache;
	private static final long DELAYTIME = 600L;
	private static final String TAG = "CreateGestureActivity";

	@Override
	protected void setContentView() {
		setContentView(R.layout.activity_create_gesture);
	}

	@Override
	protected void findViewById() {
		lockPatternView = (LockPatternView) findViewById(R.id.lockPatternView);
		lockPatternIndicator = (LockPatternIndicator) findViewById(R.id.lockPatterIndicator);
		resetBtn = (TextView) findViewById(R.id.resetBtn);
		messageTv = (TextView) findViewById(R.id.messageTv);
	}

	@Override
	protected void setListener() {
		resetBtn.setOnClickListener(this);
	}

	@Override
	protected void initData() {
		setTitle(getString(R.string.create_gesture_set));
		aCache = ACache.get(NextApplication.mContext);
		lockPatternView.setOnPatternListener(patternListener);
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()){
			case R.id.resetBtn:
				mChosenPattern = null;
				lockPatternIndicator.setDefaultIndicator();
				updateStatus(Status.DEFAULT, null);
				lockPatternView.setPattern(LockPatternView.DisplayMode.DEFAULT);
				break;
		}
	}

	/**
	 * Gusture
	 */
	private LockPatternView.OnPatternListener patternListener = new LockPatternView.OnPatternListener() {

		@Override
		public void onPatternStart() {
			lockPatternView.removePostClearPatternRunnable();
			//updateStatus(Status.DEFAULT, null);
			lockPatternView.setPattern(LockPatternView.DisplayMode.DEFAULT);
		}

		@Override
		public void onPatternComplete(List<LockPatternView.Cell> pattern) {
			//Log.e(TAG, "--onPatternDetected--");
			if(mChosenPattern == null && pattern.size() >= 4) {
				mChosenPattern = new ArrayList<>(pattern);
				updateStatus(Status.CORRECT, pattern);
			} else if (mChosenPattern == null && pattern.size() < 4) {
				updateStatus(Status.LESSERROR, pattern);
			} else if (mChosenPattern != null) {
				if (mChosenPattern.equals(pattern)) {
					updateStatus(Status.CONFIRMCORRECT, pattern);
				} else {
					updateStatus(Status.CONFIRMERROR, pattern);
				}
			}
		}
	};

	/**
	 * update status
	 * @param status
	 * @param pattern
     */
	private void updateStatus(Status status, List<LockPatternView.Cell> pattern) {
		messageTv.setTextColor(getResources().getColor(status.colorId));
		messageTv.setText(status.strId);
		switch (status) {
			case DEFAULT:
				lockPatternView.setPattern(LockPatternView.DisplayMode.DEFAULT);
				break;
			case CORRECT:
				updateLockPatternIndicator();
				lockPatternView.setPattern(LockPatternView.DisplayMode.DEFAULT);
				break;
			case LESSERROR:
				lockPatternView.setPattern(LockPatternView.DisplayMode.DEFAULT);
				break;
			case CONFIRMERROR:
				lockPatternView.setPattern(LockPatternView.DisplayMode.ERROR);
				lockPatternView.postClearPatternRunnable(DELAYTIME);
				break;
			case CONFIRMCORRECT:
				saveChosenPattern(pattern);
				lockPatternView.setPattern(LockPatternView.DisplayMode.DEFAULT);
				setLockPatternSuccess();
				break;
		}
	}

	/**
	 * update Indicator
	 */
	private void updateLockPatternIndicator() {
		if (mChosenPattern == null)
			return;
		lockPatternIndicator.setIndicator(mChosenPattern);
	}

	/**
	 * success
     */
	private void setLockPatternSuccess() {
		finish();
	}

	/**b
	 * save pattern
	 */
	private void saveChosenPattern(List<LockPatternView.Cell> cells) {
		byte[] bytes = LockPatternUtil.patternToHash(cells);
		aCache.put(Constants.GESTURE_PASSWORD + NextApplication.myInfo.getLocalId(), bytes);
	}

	private enum Status {
		//init default
		DEFAULT(R.string.create_gesture_default, R.color.textColorHint),
		//first success
		CORRECT(R.string.create_gesture_correct, R.color.textColorHint),
		//At least 4 points required
		LESSERROR(R.string.create_gesture_less_error, R.color.colorRed),
		//Confirm the failure
		CONFIRMERROR(R.string.create_gesture_confirm_error, R.color.colorRed),
		//Confirm the success
		CONFIRMCORRECT(R.string.create_gesture_confirm_correct, R.color.textColorHint);

		private Status(int strId, int colorId) {
			this.strId = strId;
			this.colorId = colorId;
		}
		private int strId;
		private int colorId;
	}
}
