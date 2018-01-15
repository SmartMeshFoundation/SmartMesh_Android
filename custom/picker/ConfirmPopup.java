package com.lingtuan.firefly.custom.picker;

import android.app.Activity;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lingtuan.firefly.util.Utils;



public abstract class ConfirmPopup<V extends View> extends BasicPopup<View> {
    protected boolean topLineVisible = true;
    protected int topLineColor = 0xFF33B5E5;
    protected int topLineHeightPixels = 1;//px
    protected int topBackgroundColor = Color.WHITE;
    protected int topHeight = 40;//dp
    protected int topPadding = 15;//dp
    protected boolean cancelVisible = true;
    protected CharSequence cancelText = "";
    protected CharSequence submitText = "";
    protected CharSequence titleText = "";
    protected int cancelTextColor = 0xFF33B5E5;
    protected int submitTextColor = 0xFF33B5E5;
    protected int titleTextColor = Color.BLACK;
    protected int pressedTextColor = 0XFF0288CE;
    protected int cancelTextSize = 0;
    protected int submitTextSize = 0;
    protected int titleTextSize = 0;
    protected int backgroundColor = Color.WHITE;
    private TextView cancelButton, submitButton;
    private View titleView;
    private View headerView,footerView;

    public ConfirmPopup(Activity activity) {
        super(activity);
        cancelText = activity.getString(android.R.string.cancel);
        submitText = activity.getString(android.R.string.ok);
    }

    /**
     * Set the top title bar underline color
     */
    public void setTopLineColor(@ColorInt int topLineColor) {
        this.topLineColor = topLineColor;
    }

    /**
     * Set the top title bar underline height in px
     */
    public void setTopLineHeight(int topLineHeightPixels) {
        this.topLineHeightPixels = topLineHeightPixels;
    }

    /**
     * Set the top title bar background color
     */
    public void setTopBackgroundColor(@ColorInt int topBackgroundColor) {
        this.topBackgroundColor = topBackgroundColor;
    }

    /**
     * Set the top title bar height (in dp)
     */
    public void setTopHeight(@IntRange(from = 10, to = 80) int topHeight) {
        this.topHeight = topHeight;
    }

    /**
     * Set the top button left and right margin (in dp)
     */
    public void setTopPadding(int topPadding) {
        this.topPadding = topPadding;
    }

    /**
     * Set the top title bar underline is displayed
     */
    public void setTopLineVisible(boolean topLineVisible) {
        this.topLineVisible = topLineVisible;
    }

    /**
     * Set the top title bar Cancel button is displayed
     */
    public void setCancelVisible(boolean cancelVisible) {
        if (null != cancelButton) {
            cancelButton.setVisibility(cancelVisible ? View.VISIBLE : View.GONE);
        } else {
            this.cancelVisible = cancelVisible;
        }
    }

    /**
     * Set the top title bar to cancel the button text
     */
    public void setCancelText(CharSequence cancelText) {
        if (null != cancelButton) {
            cancelButton.setText(cancelText);
        } else {
            this.cancelText = cancelText;
        }
    }

    /**
     * Set the top title bar to cancel the button text
     */
    public void setCancelText(@StringRes int textRes) {
        setCancelText(activity.getString(textRes));
    }

    /**
     * Set the top title bar OK button text
     */
    public void setSubmitText(CharSequence submitText) {
        if (null != submitButton) {
            submitButton.setText(submitText);
        } else {
            this.submitText = submitText;
        }
    }

    /**
     * Set the top title bar OK button text
     */
    public void setSubmitText(@StringRes int textRes) {
        setSubmitText(activity.getString(textRes));
    }

    /**
     * Set the top title bar title text
     */
    public void setTitleText(CharSequence titleText) {
        if (titleView != null && titleView instanceof TextView) {
            ((TextView) titleView).setText(titleText);
        } else {
            this.titleText = titleText;
        }
    }

    /**
     * Set the top title bar title text
     */
    public void setTitleText(@StringRes int textRes) {
        setTitleText(activity.getString(textRes));
    }

    /**
     * Set the top title bar to cancel the button text color
     */
    public void setCancelTextColor(@ColorInt int cancelTextColor) {
        if (null != cancelButton) {
            cancelButton.setTextColor(cancelTextColor);
        } else {
            this.cancelTextColor = cancelTextColor;
        }
    }

    /**
     * Set the top title bar to determine the button text color
     */
    public void setSubmitTextColor(@ColorInt int submitTextColor) {
        if (null != submitButton) {
            submitButton.setTextColor(submitTextColor);
        } else {
            this.submitTextColor = submitTextColor;
        }
    }

    /**
     * Set the top title bar title text color
     */
    public void setTitleTextColor(@ColorInt int titleTextColor) {
        if (null != titleView && titleView instanceof TextView) {
            ((TextView) titleView).setTextColor(titleTextColor);
        } else {
            this.titleTextColor = titleTextColor;
        }
    }

    /**
     * Set the text color when pressed
     */
    public void setPressedTextColor(int pressedTextColor) {
        this.pressedTextColor = pressedTextColor;
    }

    /**
     * Set the top title bar cancel button text size (in sp)
     */
    public void setCancelTextSize(@IntRange(from = 10, to = 40) int cancelTextSize) {
        this.cancelTextSize = cancelTextSize;
    }

    /**
     * Set the top title bar OK button text size (in sp)
     */
    public void setSubmitTextSize(@IntRange(from = 10, to = 40) int submitTextSize) {
        this.submitTextSize = submitTextSize;
    }

    /**
     * Set the title bar header text size (in sp)
     */
    public void setTitleTextSize(@IntRange(from = 10, to = 40) int titleTextSize) {
        this.titleTextSize = titleTextSize;
    }

    /**
     * Set the selector body background color
     */
    public void setBackgroundColor(@ColorInt int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public void setTitleView(View titleView) {
        this.titleView = titleView;
    }

    public View getTitleView() {
        if (null == titleView) {
            throw new NullPointerException("please call show at first");
        }
        return titleView;
    }

    public TextView getCancelButton() {
        if (null == cancelButton) {
            throw new NullPointerException("please call show at first");
        }
        return cancelButton;
    }

    public TextView getSubmitButton() {
        if (null == submitButton) {
            throw new NullPointerException("please call show at first");
        }
        return submitButton;
    }

    public void setHeaderView(View headerView) {
        this.headerView = headerView;
    }

    public void setFooterView(View footerView) {
        this.footerView = footerView;
    }

    /**
     * @see #makeHeaderView()
     * @see #makeCenterView()
     * @see #makeFooterView()
     */
    @Override
    protected final View makeContentView() {
        LinearLayout rootLayout = new LinearLayout(activity);
        rootLayout.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        rootLayout.setBackgroundColor(backgroundColor);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setGravity(Gravity.CENTER);
        rootLayout.setPadding(0, 0, 0, 0);
        rootLayout.setClipToPadding(false);
        View headerView = makeHeaderView();
        if (headerView != null) {
            rootLayout.addView(headerView);
        }
        if (topLineVisible) {
            View lineView = new View(activity);
            lineView.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, topLineHeightPixels));
            lineView.setBackgroundColor(topLineColor);
            rootLayout.addView(lineView);
        }
        rootLayout.addView(makeCenterView(), new LinearLayout.LayoutParams(MATCH_PARENT, 0, 1.0f));
        View footerView = makeFooterView();
        if (footerView != null) {
            rootLayout.addView(footerView);
        }
        return rootLayout;
    }

    @Nullable
    protected View makeHeaderView() {
        if (null != headerView) {
            return headerView;
        }
        RelativeLayout topButtonLayout = new RelativeLayout(activity);
        int height = Utils.dip2px(activity, topHeight);
        topButtonLayout.setLayoutParams(new RelativeLayout.LayoutParams(MATCH_PARENT, height));
        topButtonLayout.setBackgroundColor(topBackgroundColor);
        topButtonLayout.setGravity(Gravity.CENTER_VERTICAL);

        cancelButton = new TextView(activity);
        cancelButton.setVisibility(cancelVisible ? View.VISIBLE : View.GONE);
        RelativeLayout.LayoutParams cancelParams = new RelativeLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT);
        cancelParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        cancelParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        cancelButton.setLayoutParams(cancelParams);
        cancelButton.setBackgroundColor(Color.TRANSPARENT);
        cancelButton.setGravity(Gravity.CENTER);
        int padding = Utils.dip2px(activity, topPadding);
        cancelButton.setPadding(padding, 0, padding, 0);
        if (!TextUtils.isEmpty(cancelText)) {
            cancelButton.setText(cancelText);
        }
        cancelButton.setTextColor(Utils.toColorStateList(cancelTextColor, pressedTextColor));
        if (cancelTextSize != 0) {
            cancelButton.setTextSize(cancelTextSize);
        }
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                onCancel();
            }
        });
        topButtonLayout.addView(cancelButton);

        if (null == titleView) {
            TextView textView = new TextView(activity);
            RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            int margin = Utils.dip2px(activity, topPadding);
            titleParams.leftMargin = margin;
            titleParams.rightMargin = margin;
            titleParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
            titleParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
            textView.setLayoutParams(titleParams);
            textView.setGravity(Gravity.CENTER);
            if (!TextUtils.isEmpty(titleText)) {
                textView.setText(titleText);
            }
            textView.setTextColor(titleTextColor);
            if (titleTextSize != 0) {
                textView.setTextSize(titleTextSize);
            }
            titleView = textView;
        }
        topButtonLayout.addView(titleView);

        submitButton = new TextView(activity);
        RelativeLayout.LayoutParams submitParams = new RelativeLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT);
        submitParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        submitParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        submitButton.setLayoutParams(submitParams);
        submitButton.setBackgroundColor(Color.TRANSPARENT);
        submitButton.setGravity(Gravity.CENTER);
        submitButton.setPadding(padding, 0, padding, 0);
        if (!TextUtils.isEmpty(submitText)) {
            submitButton.setText(submitText);
        }
        submitButton.setTextColor(Utils.toColorStateList(submitTextColor, pressedTextColor));
        if (submitTextSize != 0) {
            submitButton.setTextSize(submitTextSize);
        }
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                onSubmit();
            }
        });
        topButtonLayout.addView(submitButton);

        return topButtonLayout;
    }

    @NonNull
    protected abstract V makeCenterView();

    @Nullable
    protected View makeFooterView() {
        if (null != footerView) {
            return footerView;
        }
        return null;
    }

    protected void onSubmit() {

    }

    protected void onCancel() {

    }

}
