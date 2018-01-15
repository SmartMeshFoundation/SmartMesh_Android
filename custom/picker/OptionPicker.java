package com.lingtuan.firefly.custom.picker;

import android.app.Activity;

import java.util.List;

/**
 * single
 */
public class OptionPicker extends SinglePicker<String> {

    public OptionPicker(Activity activity, String[] items) {
        super(activity, items);
    }

    public OptionPicker(Activity activity, List<String> items) {
        super(activity, items);
    }

    public void setOnOptionPickListener(OnOptionPickListener listener) {
        super.setOnItemPickListener(listener);
    }

    public void setOnWheelListener(OnWheelListener onWheelListener) {
        super.setOnWheelListener(onWheelListener);
    }

    public interface OnWheelListener extends SinglePicker.OnWheelListener<String> {

    }

    public static abstract class OnOptionPickListener implements OnItemPickListener<String> {

        public abstract void onOptionPicked(int index, String item);

        @Override
        public final void onItemPicked(int index, String item) {
            onOptionPicked(index, item);
        }

    }

}
