package com.lingtuan.firefly.util;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.lingtuan.firefly.R;

public class LoadingDialog {

    /**
     * Custom Dialog
     * @param context
     * @param msg
     */
    private static Dialog mDialog = null;

    public static void show(Context context, String msg) {
        close();
        try {
            if (context != null && !context.isRestricted()) {
                mDialog = new Dialog(context, R.style.loading_dialog);
                mDialog.setContentView(R.layout.loading_dialog);
                if (msg == null || msg.length() == 0) {
                    mDialog.findViewById(R.id.message).setVisibility(View.GONE);
                } else {
                    TextView txt = (TextView) mDialog.findViewById(R.id.message);
                    txt.setText(msg);
                }
                mDialog.setCanceledOnTouchOutside(false);
                mDialog.setCancelable(false);
                mDialog.getWindow().getAttributes().gravity = Gravity.CENTER;
                WindowManager.LayoutParams lp = mDialog.getWindow().getAttributes();
                lp.dimAmount = 0.2f;
                mDialog.getWindow().setAttributes(lp);
                mDialog.show();
            }
        } catch (Exception e) {
            mDialog = null;
        }
    }

    /**
     * @param cancelable Can cancel it cannot pass the true or false
     **/
    public static void show(Context context, boolean cancelable, String msg) {
        close();
        try {
            if (context != null && !context.isRestricted()) {
                mDialog = new Dialog(context, R.style.loading_dialog);
                mDialog.setContentView(R.layout.loading_dialog);
                if (msg == null || msg.length() == 0) {
                    mDialog.findViewById(R.id.message).setVisibility(View.GONE);
                } else {
                    TextView txt = (TextView) mDialog.findViewById(R.id.message);
                    txt.setText(msg);
                }
                mDialog.setCanceledOnTouchOutside(false);
                mDialog.setCancelable(cancelable);
                mDialog.getWindow().getAttributes().gravity = Gravity.CENTER;
                WindowManager.LayoutParams lp = mDialog.getWindow().getAttributes();
                lp.dimAmount = 0.2f;
                mDialog.getWindow().setAttributes(lp);
                mDialog.show();
            }
        } catch (Exception e) {
            mDialog = null;
        }
    }

    public static Dialog showDialog(Context context, String msg, String cat) {
        Dialog mDialog = new Dialog(context, R.style.loading_dialog);
        mDialog.setContentView(R.layout.loading_dialog);
        if (msg == null || msg.length() == 0) {
            mDialog.findViewById(R.id.message).setVisibility(View.GONE);
        } else {
            TextView txt = (TextView) mDialog.findViewById(R.id.message);
            txt.setText(msg);
        }
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);
        mDialog.getWindow().getAttributes().gravity = Gravity.CENTER;
        WindowManager.LayoutParams lp = mDialog.getWindow().getAttributes();
        lp.dimAmount = 0.2f;
        mDialog.getWindow().setAttributes(lp);
        mDialog.show();
        return mDialog;
    }

    /**
     * get a dialog
     * @ param MSG display information
     */
    public static void showSingleDialog(Context context, String msg) {
        try {
            if (context != null && !context.isRestricted()) {
                if (mDialog == null) {
                    mDialog = new Dialog(context, R.style.loading_dialog);
                    mDialog.setContentView(R.layout.loading_dialog);
                    mDialog.show();
                }
                if (msg == null || msg.length() == 0) {
                    mDialog.findViewById(R.id.message).setVisibility(View.GONE);
                } else {
                    TextView txt = (TextView) mDialog.findViewById(R.id.message);
                    txt.setText(msg);
                }
                mDialog.setCanceledOnTouchOutside(false);
                mDialog.setCancelable(false);
                mDialog.getWindow().getAttributes().gravity = Gravity.CENTER;
                WindowManager.LayoutParams lp = mDialog.getWindow().getAttributes();
                lp.dimAmount = 0.2f;
                mDialog.getWindow().setAttributes(lp);
            }
        } catch (Exception e) {
            mDialog = null;
        }
    }

    public static void show(Context context, String msg, String n) {
        show(context, msg);
    }

    public static void show(Context context, int resId) {
        show(context, context.getString(resId));
    }

    public static Dialog getMDialog() {
        return mDialog;
    }

    public static void close() {
        if (mDialog != null) {
            try {
                mDialog.dismiss();
            } catch (Exception exp) {

            } finally {
                mDialog = null;
            }
        }
    }

}
