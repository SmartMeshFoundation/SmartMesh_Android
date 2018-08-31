package com.lingtuan.firefly.custom;

import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.custom.fallingview.FallingView;
import com.lingtuan.firefly.ui.UpdateVersionHandler;
import com.lingtuan.firefly.ui.WebViewUI;
import com.lingtuan.firefly.util.Utils;

import java.io.File;

import static android.content.Context.DOWNLOAD_SERVICE;


public class SubmitDialog extends Dialog {
 
    public SubmitDialog(Context context, int theme) {
        super(context, theme);
    }
 
    public SubmitDialog(Context context) {
        super(context);
    }

    /**
     * Helper class for creating a custom dialog
     */
    public static class Builder {
 
        private Context context;
        private String title;
        private String message;
        private String positiveButtonText;
        private String negativeButtonText;
        private View contentView;
        private CustomProgressBar transProgressBar;
        private DialogInterface.OnClickListener positiveButtonClickListener,negativeButtonClickListener;

        private mappingClosedListener mappingClosedListener;
        private SubmitDialog dialog;

        boolean hasSelect = false;

        public UpdateVersionHandler mHandler;
        private DownloadManager dm;
        private long downloadId;

        public Builder(Context context) {
            this.context = context;
        }

        public interface mappingClosedListener{
            void mappingClosed(DialogInterface dialog);
        };

        public void setMappingClosed(mappingClosedListener mappingClosedListener){
            this.mappingClosedListener = mappingClosedListener;
        }

        /**
         * Set the Dialog setCancelable
         * @return
         */
        public void setCancelable(boolean cancelable){
        	if(dialog!=null){
        		dialog.setCancelable(cancelable);
        		dialog.setCanceledOnTouchOutside(cancelable);
        	}
        }
        /**
         * Set the Dialog message from String
         * @return
         */
        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        /**
         * Set the Dialog message from resource
         * @return
         */
        public Builder setMessage(int message) {
            this.message = (String) context.getText(message);
            return this;
        }

        /**
         * Set the Dialog title from String
         * @param title
         * @return
         */
        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        /**
         * Set a custom content view for the Dialog.
         * If a message is set, the contentView is not
         * added to the Dialog...
         * @param v
         * @return
         */
        public Builder setContentView(View v) {
            this.contentView = v;
            return this;
        }

        /**
         * Set the positive button text and it's listener
         * @param positiveButtonText
         * @param listener
         * @return
         */
        public Builder setPositiveButton(String positiveButtonText,DialogInterface.OnClickListener listener) {
            this.positiveButtonText = positiveButtonText;
            this.positiveButtonClickListener = listener;
            return this;
        }


        /**
         * Set the negative button text and it's listener
         * @param negativeButtonText
         * @param listener
         * @return
         */
        public Builder setNegativeButton(String negativeButtonText,DialogInterface.OnClickListener listener) {
            this.negativeButtonText = negativeButtonText;
            this.negativeButtonClickListener = listener;
            return this;
        }
 
      
        /**
         * Create the custom dialog
         */
        public void showSmartMeshDialog() {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final SubmitDialog dialog = new SubmitDialog(context, R.style.SubmitDialog);
            this.dialog=dialog;
            View layout = inflater.inflate(R.layout.submit_dialog_layout, null);
            dialog.addContentView(layout, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            TextView titleView = layout.findViewById(R.id.title);
            TextView negativeButton = layout.findViewById(R.id.negativeButton);
            TextView positiveButton = layout.findViewById(R.id.positiveButton);
            TextView messageView = layout.findViewById(R.id.message);
            setDialogTitle(titleView,title);
            setNegativeListener(negativeButton,negativeButtonText,negativeButtonClickListener,dialog);
            setPositiveListener(positiveButton,positiveButtonText,positiveButtonClickListener,dialog);
            setMessageView(messageView,message,context,layout,contentView);
            showDialog(dialog,layout);
        }

        /**
         * Create the custom dialog
         */
        public void showOfflineDialog() {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final SubmitDialog dialog = new SubmitDialog(context, R.style.SubmitDialog);
            this.dialog=dialog;
            View layout = inflater.inflate(R.layout.submit_dialog_offline_layout, null);
            dialog.addContentView(layout, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            TextView titleView = layout.findViewById(R.id.title);
            TextView negativeButton = layout.findViewById(R.id.negativeButton);
            TextView positiveButton = layout.findViewById(R.id.positiveButton);
            TextView messageView = layout.findViewById(R.id.message);
            setDialogTitle(titleView,title);
            setNegativeListener(negativeButton,negativeButtonText,negativeButtonClickListener,dialog);
            setPositiveListener(positiveButton,positiveButtonText,positiveButtonClickListener,dialog);
            setMessageView(messageView,message,context,layout,contentView);
            showDialog(dialog,layout);
        }

        /**
         * Create the custom dialog
         */
        public void showUpdateDialog() {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final SubmitDialog dialog = new SubmitDialog(context, R.style.SubmitDialog);
            this.dialog=dialog;
            View layout = inflater.inflate(R.layout.submit_dialog_update_layout, null);
            dialog.addContentView(layout, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            TextView titleView = layout.findViewById(R.id.title);
            TextView negativeButton = layout.findViewById(R.id.negativeButton);
            TextView positiveButton = layout.findViewById(R.id.positiveButton);
            TextView messageView = layout.findViewById(R.id.message);
            setDialogTitle(titleView,title);
            setNegativeListener(negativeButton,negativeButtonText,negativeButtonClickListener,dialog);
            setPositiveListener(positiveButton,positiveButtonText,positiveButtonClickListener,dialog);
            setMessageView(messageView,message,context,layout,contentView);
            showDialog(dialog,layout);
        }


        /**
         * show update dialog  now
         */
        public void showUpdateNowDialog(final String url) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final SubmitDialog dialog = new SubmitDialog(context, R.style.SubmitDialog);
            this.dialog=dialog;
            View layout = inflater.inflate(R.layout.submit_dialog_update_now_layout, null);
            dialog.addContentView(layout, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            final TextView titleView = layout.findViewById(R.id.title);
            final TextView negativeButton = layout.findViewById(R.id.negativeButton);
            TextView messageView = layout.findViewById(R.id.message);
            transProgressBar = layout.findViewById(R.id.transProgressBar);
            setDialogTitle(titleView,title);
            if (negativeButtonText != null) {
                negativeButton.setText(negativeButtonText);
                if (negativeButtonClickListener != null) {
                    negativeButton.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            transProgressBar.setVisibility(View.VISIBLE);
                            try {
                                if (TextUtils.isEmpty(url)){
                                    negativeButtonClickListener.onClick(dialog,DialogInterface.BUTTON_NEGATIVE);
                                    return;
                                }
                                int index = url.lastIndexOf("/")+1;
                                String apkName = url.substring(index,url.length());
                                dm = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
                                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                request.setMimeType("application/vnd.android.package-archive");
                                request.allowScanningByMediaScanner();
                                request.setVisibleInDownloadsUi(true);
                                Utils.deleteFiles(new File(Environment.getExternalStorageDirectory() + "/download/"+apkName));
                                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, apkName);
                                downloadId = dm.enqueue(request);
                                SharedPreferences sPreferences = context.getSharedPreferences("downloadplato", 0);
                                sPreferences.edit().putLong("plato", downloadId).commit();
                                negativeButton.setVisibility(View.GONE);
                                mHandler = new UpdateVersionHandler();
                                mHandler.postDelayed(runnable1, 200);
                                getBytesAndStatus(downloadId,dm);
                            }catch (Exception e){
                                negativeButtonClickListener.onClick(dialog,DialogInterface.BUTTON_NEGATIVE);
                                e.printStackTrace();

                            }
                        }
                    });
                }
            } else {
                negativeButton.setVisibility( View.GONE);
            }
            layout.findViewById(R.id.positiveButton).setVisibility(View.GONE);
            setMessageView(messageView,message,context,layout,contentView);
            showDialog(dialog,layout);
        }

        /**
         * Create the custom dialog
         */
        public void showWallet() {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final SubmitDialog dialog = new SubmitDialog(context, R.style.SubmitDialog);
            this.dialog=dialog;
            View layout = inflater.inflate(R.layout.submit_dialog_wallet_layout, null);
            dialog.addContentView(layout, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

            TextView negativeButton = layout.findViewById(R.id.negativeButton);
            TextView titleView = layout.findViewById(R.id.title);
            TextView messageView = layout.findViewById(R.id.message);
            setDialogTitle(titleView,title);
            setNegativeListener(negativeButton,negativeButtonText,negativeButtonClickListener,dialog);
            layout.findViewById(R.id.positiveButton).setVisibility(View.GONE);
            setMessageView(messageView,message,context,layout,contentView);
            showDialog(dialog,layout);
        }


        /**
         * Create the custom dialog
         */
        public void showWalletBackup() {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final SubmitDialog dialog = new SubmitDialog(context, R.style.SubmitDialog);
            this.dialog=dialog;
            View layout = inflater.inflate(R.layout.submit_dialog_wallet_backup_layout, null);
            dialog.addContentView(layout, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

            TextView negativeButton = layout.findViewById(R.id.negativeButton);
            TextView titleView = layout.findViewById(R.id.title);

            layout.findViewById(R.id.positiveButton).setVisibility(View.GONE);
            setDialogTitle(titleView,title);
            setNegativeListener(negativeButton,negativeButtonText,negativeButtonClickListener,dialog);
            showDialog(dialog,layout);
        }

        /**
         * Create the custom dialog
         */
        public void showMapping(final String smtBalance,final String title,final String url) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final SubmitDialog dialog = new SubmitDialog(context, R.style.SubmitDialog);
            this.dialog=dialog;
            View layout = inflater.inflate(R.layout.submit_dialog_wallet_mapping_layout, null);
            dialog.addContentView(layout, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

            final TextView negativeButton = layout.findViewById(R.id.negativeButton);
            TextView spectrumBalance = layout.findViewById(R.id.spectrumBalance);
            TextView ethereumBalance = layout.findViewById(R.id.ethereumBalance);
            final ImageView mappingSelect = layout.findViewById(R.id.mappingSelect);
            TextView mappingInformation = layout.findViewById(R.id.mappingInformation);
            ImageView mappingClosed = layout.findViewById(R.id.mappingClosed);

            spectrumBalance.setText(context.getString(R.string.smt_er,smtBalance));
            ethereumBalance.setText(context.getString(R.string.smt_er,smtBalance));

            mappingClosed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mappingClosedListener != null){
                        mappingClosedListener.mappingClosed(dialog);
                    }
                }
            });

            mappingSelect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hasSelect = !hasSelect;
                    if (hasSelect){
                        if (TextUtils.equals(smtBalance,"0")){
                            negativeButton.setEnabled(false);
                            negativeButton.setTextColor(context.getResources().getColor(R.color.textColor));
                        }else{
                            negativeButton.setEnabled(true);
                            negativeButton.setTextColor(context.getResources().getColor(R.color.color_cb0f1e));
                        }
                        mappingSelect.setImageResource(R.drawable.mapping_start_select);
                    }else{
                        negativeButton.setEnabled(false);
                        negativeButton.setTextColor(context.getResources().getColor(R.color.textColor));
                        mappingSelect.setImageResource(R.drawable.mapping_start_unselect);
                    }
                }
            });

            mappingInformation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, WebViewUI.class);
                    intent.putExtra("loadUrl", url);
                    intent.putExtra("title", title);
                    context.startActivity(intent);
                    Utils.openNewActivityAnim((Activity) context,false);
                }
            });
            setNegativeListener(negativeButton,negativeButtonText,negativeButtonClickListener,dialog);
            showDialog(dialog,layout);
        }

        /**
         * Create the custom dialog
         */
        public void showMappingSuccess(String mappingId, String content) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final SubmitDialog dialog = new SubmitDialog(context, R.style.SubmitDialog);
            this.dialog=dialog;
            View layout = inflater.inflate(R.layout.submit_dialog_wallet_mapping_success_layout, null);
            dialog.addContentView(layout, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

            final TextView negativeButton = layout.findViewById(R.id.negativeButton);
            TextView mappingInformation = layout.findViewById(R.id.mappingInformation);
            TextView mappingTextId = layout.findViewById(R.id.mappingId);
            ImageView mappingClosed = layout.findViewById(R.id.mappingClosed);

            final FallingView tempFalling = layout.findViewById(R.id.tempFalling);

            mappingInformation.setText(content);
            mappingTextId.setText(context.getString(R.string.mapping_id_number,mappingId));

            mappingClosed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mappingClosedListener != null){
                        tempFalling.clear();
                        mappingClosedListener.mappingClosed(dialog);
                    }
                }
            });

            if (negativeButtonText != null) {
                negativeButton.setText(negativeButtonText);
                if (negativeButtonClickListener != null) {
                    negativeButton.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            tempFalling.clear();
                            negativeButtonClickListener.onClick(dialog,DialogInterface.BUTTON_NEGATIVE);
                        }
                    });
                }
            } else {
                negativeButton.setVisibility( View.GONE);
            }
            showDialog(dialog,layout);
        }


        Runnable runnable1 = new Runnable() {
            @Override
            public void run() {
                try {
                    if (mHandler != null) {
                        getBytesAndStatus(downloadId,dm);
                        mHandler.postDelayed(this, 200);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        /**
         * Download status through query query, including downloaded data size, total size, download status
         * 通过query查询下载状态，包括已下载数据大小，总大小，下载状态
         */
        private void getBytesAndStatus(long downloadId,DownloadManager downloadManager) {
            int hasDownloadSize = 0;
            int totalSize = 0;
            DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
            Cursor cursor = null;
            try {
                cursor = downloadManager.query(query);
                if (cursor != null && cursor.moveToFirst()) {
                    //File size has been downloaded   已经下载文件大小
                    hasDownloadSize = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    //The total size of the downloaded file  下载文件的总大小
                    totalSize = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                }
                if (hasDownloadSize >= 0 && totalSize > 0) {
                    int progress = (int)(hasDownloadSize / (float) totalSize *100);
                    if (progress < 100){
                        transProgressBar.setProgress(progress);
                    }else{
                        if (mHandler != null) {
                            mHandler.removeCallbacks(runnable1);
                            mHandler = null;
                        }
                        if (negativeButtonClickListener != null){
                            negativeButtonClickListener.onClick(dialog,DialogInterface.BUTTON_NEGATIVE);
                        }
                    }
                }

            } catch (Exception e){
                e.printStackTrace();
                if (mHandler != null) {
                    mHandler.removeCallbacks(runnable1);
                    mHandler = null;
                }
                if (negativeButtonClickListener != null){
                    negativeButtonClickListener.onClick(dialog,DialogInterface.BUTTON_NEGATIVE);
                }
            }finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }

    private static void  setPositiveListener(TextView positiveButton,String positiveButtonText,final DialogInterface.OnClickListener positiveButtonClickListener,final DialogInterface dialog){
        if (positiveButtonText != null) {
            positiveButton.setText(positiveButtonText);
            if (positiveButtonClickListener != null) {
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        positiveButtonClickListener.onClick(dialog,DialogInterface.BUTTON_POSITIVE);
                    }
                });
            }
        } else {
            positiveButton.setVisibility(View.GONE);
        }
    }

    private static void setNegativeListener(TextView negativeButton,String negativeButtonText,final DialogInterface.OnClickListener negativeButtonClickListener,final DialogInterface dialog){
        if (negativeButtonText != null) {
            negativeButton.setText(negativeButtonText);
            if (negativeButtonClickListener != null) {
                negativeButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        negativeButtonClickListener.onClick(dialog,DialogInterface.BUTTON_NEGATIVE);
                    }
                });
            }
        } else {
            negativeButton.setVisibility( View.GONE);
        }
    }

    private static void setDialogTitle(TextView titleView,String title){
        if(TextUtils.isEmpty(title)){
            titleView.setVisibility(View.GONE);
        }else{
            titleView.setText(title);
        }
    }

    private static void setMessageView(TextView messageView,String message,Context context,View layout,View contentView){
        if (message != null) {
            messageView.setText(message);
        } else if (contentView != null) {
            ((LinearLayout) layout.findViewById(R.id.content)).removeAllViews();
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
            lp.setMargins(Utils.dip2px(context, 15), 0, Utils.dip2px(context, 15), 0);
            ((LinearLayout) layout.findViewById(R.id.content)).addView(contentView, lp);
        }
    }

    private static void showDialog(SubmitDialog dialog,View layout){
        if (dialog != null){
            dialog.setContentView(layout);
            dialog.show();
            if (dialog.getWindow() != null){
                WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
                params.width = WindowManager.LayoutParams.MATCH_PARENT;
                params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                dialog.getWindow().setAttributes(params);
            }
        }
    }
}
