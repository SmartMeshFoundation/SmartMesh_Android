package com.lingtuan.firefly.custom.picker;

import android.app.Activity;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * wheelpicker
 */
public abstract class WheelPicker extends ConfirmPopup<View> {
    protected float lineSpaceMultiplier = WheelView.LINE_SPACE_MULTIPLIER;
    protected int padding = WheelView.TEXT_PADDING;
    protected int textSize = WheelView.TEXT_SIZE;
    protected int textColorNormal = WheelView.TEXT_COLOR_NORMAL;
    protected int textColorFocus = WheelView.TEXT_COLOR_FOCUS;
    protected int offset = WheelView.ITEM_OFF_SET;
    protected boolean cycleDisable = true;
    protected WheelView.DividerConfig dividerConfig = new WheelView.DividerConfig();
    protected View contentView;

    public WheelPicker(Activity activity) {
        super(activity);
    }

    /**
     * Can be used to set the height of each item in the range of 2-4
     */
    public final void setLineSpaceMultiplier(@FloatRange(from = 2, to = 4) float multiplier) {
        lineSpaceMultiplier = multiplier;
    }

    /**
     * Can be used to set the width of each item in dp
     */
    public void setPadding(int padding) {
        this.padding = padding;
    }

    /**
     * Set the text size
     */
    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    /**
     * Set the text color
     */
    public void setTextColor(@ColorInt int textColorFocus, @ColorInt int textColorNormal) {
        this.textColorFocus = textColorFocus;
        this.textColorNormal = textColorNormal;
    }

    /**
     * Set the text color
     */
    public void setTextColor(@ColorInt int textColor) {
        this.textColorFocus = textColor;
    }

    /**
     * Set whether to separate the shadow is visible
     */
    public void setShadowVisible(boolean shadowVisible) {
        if (null == dividerConfig) {
            dividerConfig = new WheelView.DividerConfig();
        }
        dividerConfig.setShadowVisible(shadowVisible);
    }

    /**
     * Set the shadow color and transparency
     */
    public void setShadowColor(@ColorInt int color) {
        setShadowColor(color, 100);
    }

    /**
     * Set the shadow color and transparency
     */
    public void setShadowColor(@ColorInt int color, @IntRange(from = 1, to = 255) int alpha) {
        if (null == dividerConfig) {
            dividerConfig = new WheelView.DividerConfig();
        }
        dividerConfig.setShadowColor(color);
        dividerConfig.setShadowAlpha(alpha);
    }

    /**
     * Set whether the divider is visible
     */
    public void setDividerVisible(boolean visible) {
        if (null == dividerConfig) {
            dividerConfig = new WheelView.DividerConfig();
        }
        dividerConfig.setVisible(visible);
    }

    /**
     * @deprecated use {@link #setDividerVisible(boolean)} instead
     */
    @Deprecated
    public void setLineVisible(boolean visible) {
        setDividerVisible(visible);
    }

    /**
     * @deprecated use {@link #setDividerColor(int)} instead
     */
    @Deprecated
    public void setLineColor(@ColorInt int color) {
        setDividerColor(color);
    }

    /**
     * Set the dividing line color
     */
    public void setDividerColor(@ColorInt int lineColor) {
        if (null == dividerConfig) {
            dividerConfig = new WheelView.DividerConfig();
        }
        dividerConfig.setVisible(true);
        dividerConfig.setColor(lineColor);
    }

    /**
     * Set the divider length ratio
     */
    public void setDividerRatio(float ratio) {
        if (null == dividerConfig) {
            dividerConfig = new WheelView.DividerConfig();
        }
        dividerConfig.setRatio(ratio);
    }

    /**
     * Set the line item, setting null will hide the line and shadow
     */
    public void setDividerConfig(@Nullable WheelView.DividerConfig config) {
        if (null == config) {
            dividerConfig = new WheelView.DividerConfig();
            dividerConfig.setVisible(false);
            dividerConfig.setShadowVisible(false);
        } else {
            dividerConfig = config;
        }
    }

    /**
     * @deprecated use {@link #setDividerConfig(WheelView.DividerConfig)} instead
     */
    @Deprecated
    public void setLineConfig(WheelView.DividerConfig config) {
        setDividerConfig(config);
    }

    /**
     * Set the option offset, which can be used to set the number of entries to display, in the range of 1-5.
   * 1 shows 3, 2 shows 5, 3 shows 7
     */
    public void setOffset(@IntRange(from = 1, to = 5) int offset) {
        this.offset = offset;
    }

    /**
     * Set whether to disable the loop
     */
    public void setCycleDisable(boolean cycleDisable) {
        this.cycleDisable = cycleDisable;
    }

    /**
     * Get selector view, can be embedded into other view containers
     */
    @Override
    public View getContentView() {
        if (null == contentView) {
            contentView = makeCenterView();
        }
        return contentView;
    }

    protected WheelView createWheelView() {
        WheelView wheelView = new WheelView(activity);
        wheelView.setLineSpaceMultiplier(lineSpaceMultiplier);
        wheelView.setPadding(padding);
        wheelView.setTextSize(textSize);
        wheelView.setTextColor(textColorNormal, textColorFocus);
        wheelView.setDividerConfig(dividerConfig);
        wheelView.setOffset(offset);
        wheelView.setCycleDisable(cycleDisable);
        return wheelView;
    }

    protected TextView createLabelView() {
        TextView labelView = new TextView(activity);
        labelView.setLayoutParams(new ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        labelView.setTextColor(textColorFocus);
        labelView.setTextSize(textSize);
        return labelView;
    }

}
