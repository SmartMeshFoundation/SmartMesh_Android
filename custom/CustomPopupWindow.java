package com.lingtuan.firefly.custom;

import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.widget.PopupWindow;

/**
 * Created by Administrator on 2018/1/5.
 */

public class CustomPopupWindow extends PopupWindow {

    public CustomPopupWindow(View mMenuView, int matchParent, int matchParent1) {
        super(mMenuView, matchParent,matchParent1);
    }

    @Override
    public void showAsDropDown(View anchor) {
        if (Build.VERSION.SDK_INT == 24) {
            Rect rect = new Rect();
            anchor.getGlobalVisibleRect(rect);
            int h = anchor.getResources().getDisplayMetrics().heightPixels - rect.bottom;
            setHeight(h);
        }
        super.showAsDropDown(anchor);
    }
}
