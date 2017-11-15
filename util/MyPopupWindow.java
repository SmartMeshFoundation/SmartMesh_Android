package com.lingtuan.firefly.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.listener.DialogItemClickListener;

/**
 * dialog
 */
public class MyPopupWindow extends LinearLayout implements OnClickListener {

    private Context context;
    private static PopupWindow mPopupWindow;

    public MyPopupWindow(Context context, Type type) {
        super(context);
        cheak();
        this.context = context;
        if (Type.format_list.equals(type)) {
            View.inflate(context, R.layout.dialog_format_list, this);
        }
    }

    /**
     * Display dialog list
     */
    public static void showDialogList(Context context, String title, String[] itemList, DialogItemClickListener listener) {
        new MyPopupWindow(context, Type.format_list).showDialogList(title, itemList, listener);
    }


    private void showDialogList(String title, String[] itemList, final DialogItemClickListener listener) {
        popupModeAlpha();
        TextView titleTv = (TextView) findViewById(R.id.dialog_format_title);
        ListView listView = (ListView) findViewById(R.id.dialog_format_list);
        if (TextUtils.isEmpty(title)) {
            titleTv.setVisibility(View.GONE);
        } else {
            titleTv.setText(title);
        }
        listView.setAdapter(new ArrayAdapter<>(context, R.layout.item_dialog_format_list, R.id.item_dialog_format_text, itemList));
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                dismiss();
                if (listener != null) {
                    listener.onItemClickListener(position);
                }
            }
        });
        titleTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        findViewById(R.id.dialog_cancel_linear).setOnClickListener(this);
    }


    /**
     * The fading display
     */
    private void popupModeAlpha() {
        mPopupWindow = new PopupWindow(this, LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT, true);
        mPopupWindow.setWindowLayoutMode(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mPopupWindow.showAtLocation(((Activity) context).getWindow().getDecorView(), Gravity.CENTER, 0, 0);
        mPopupWindow.setAnimationStyle(R.style.animationmsg);
        mPopupWindow.setFocusable(true);
        mPopupWindow.setTouchable(true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        mPopupWindow.update();
    }

    public void dismiss() {
        if (mPopupWindow != null) {
            mPopupWindow.dismiss();
            mPopupWindow = null;
        }
    }



    public void cheak() {
        if (mPopupWindow != null) {
            if (mPopupWindow.isShowing()) {
                mPopupWindow.dismiss();
            }
        }
        mPopupWindow = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_cancel_linear:
                dismiss();
                break;

        }
    }

    public enum Type {
        /**
         * Dialog list
         */
        format_list,
    }
}
