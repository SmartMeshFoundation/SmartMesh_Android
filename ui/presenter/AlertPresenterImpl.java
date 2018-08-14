package com.lingtuan.firefly.ui.presenter;

import android.content.Context;
import android.content.DialogInterface;

import com.lingtuan.firefly.custom.SubmitDialog;
import com.lingtuan.firefly.ui.contract.AlertContract;
import com.lingtuan.firefly.wallet.vo.StorableWallet;

public class AlertPresenterImpl implements AlertContract.Presenter{

    private AlertContract.View mView;

    public AlertPresenterImpl(AlertContract.View view){
        this.mView = view;
        mView.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void updateVersionDialog(Context context,String title, String describe, final String url,String positiveMsg,String negativeMsg) {
        final SubmitDialog.Builder builder = new SubmitDialog.Builder(context);
        builder.setDialogType(0);
        builder.setTitle(title)
               .setMessage(describe.replace("\\n", "\n"))
               .setPositiveButton(positiveMsg, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mView.updateVersionDialogCancel();
                    }
                })
               .setNegativeButton(negativeMsg,new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mView.updateVersionDialogSubmit(url);
                    }
               });
        builder.show();
        builder.setCancelable(false);
    }

    @Override
    public void updateVersionNowDialog(Context context,String title, String describe, final String url, String negativeMsg) {
        SubmitDialog.Builder builder = new SubmitDialog.Builder(context);
        builder.setDialogType(0);
        builder.setTitle(title)
               .setMessage(describe.replace("\\n", "\n"))
               .setNegativeButton(negativeMsg,new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mView.updateVersionDialogSubmit(url);
                    }
               });
        builder.show();
        builder.setCancelable(false);
    }

    @Override
    public void showOfflineDialog(Context context,String title,String msg,String negativeMsg) {
        SubmitDialog.Builder builder = new SubmitDialog.Builder(context);
        builder.setDialogType(1);
        builder.setTitle(title)
                .setMessage(msg)
                .setNegativeButton(negativeMsg,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                mView.offlineDialogSubmit();
                            }
                        });
        builder.show();
        builder.setCancelable(false);
    }

    @Override
    public void showSmartMeshDialog(Context context,String title, String message, String positiveMsg, String negativeMsg) {
        SubmitDialog.Builder builder = new SubmitDialog.Builder(context);
        builder.setDialogType(2);
        builder.setTitle(title)
                .setMessage(message)
                .setNegativeButton(negativeMsg,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                mView.smartMeshDialogSubmit(true);
                            }
                        })
                .setPositiveButton(positiveMsg,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                mView.smartMeshDialogSubmit(false);
                            }
                        });
        builder.show();
        builder.setCancelable(false);
    }

    @Override
    public void showWalletDialog( Context context,String title, String message, String negativeMsg) {
        SubmitDialog.Builder builder = new SubmitDialog.Builder(context);
        builder.setDialogType(4);
        builder.setTitle(title)
                .setMessage(message)
                .setNegativeButton(negativeMsg,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                mView.walletDialogSubmit();
                            }
                        });
        builder.showWallet();
        builder.setCancelable(false);
    }

    @Override
    public void showBackupDialog(Context context, String title, String message, String negativeMsg, final StorableWallet storableWallet) {
        SubmitDialog.Builder builder = new SubmitDialog.Builder(context);
        builder.setDialogType(5);
        builder.setTitle(title)
                .setMessage(message)
                .setNegativeButton(negativeMsg,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                mView.backUpDialogSubmit(storableWallet);
                            }
                        });
        builder.showWalletBackup();
        builder.setCancelable(false);
    }
}
