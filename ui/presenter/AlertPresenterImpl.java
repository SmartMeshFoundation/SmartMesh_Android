package com.lingtuan.firefly.ui.presenter;

import android.content.Context;
import android.content.DialogInterface;

import com.lingtuan.firefly.custom.SubmitDialog;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.network.NetRequestImpl;
import com.lingtuan.firefly.ui.contract.AlertContract;
import com.lingtuan.firefly.wallet.vo.StorableWallet;

import org.json.JSONObject;

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
        builder.showUpdateDialog();
        builder.setCancelable(false);
    }

    @Override
    public void updateVersionNowDialog(Context context,String title, String describe, final String url, String negativeMsg) {
        SubmitDialog.Builder builder = new SubmitDialog.Builder(context);
        builder.setTitle(title)
               .setMessage(describe.replace("\\n", "\n"))
               .setNegativeButton(negativeMsg,new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mView.updateVersionDialogSubmit(url);
                    }
               });
        builder.showUpdateNowDialog();
        builder.setCancelable(false);
    }

    @Override
    public void showOfflineDialog(Context context,String title,String msg,String negativeMsg) {
        SubmitDialog.Builder builder = new SubmitDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(msg)
                .setNegativeButton(negativeMsg,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                mView.offlineDialogSubmit();
                            }
                        });
        builder.showOfflineDialog();
        builder.setCancelable(false);
    }

    @Override
    public void showSmartMeshDialog(Context context,String title, String message, String positiveMsg, String negativeMsg) {
        SubmitDialog.Builder builder = new SubmitDialog.Builder(context);
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
        builder.showSmartMeshDialog();
        builder.setCancelable(false);
    }

    @Override
    public void showWalletDialog( Context context,String title, String message, String negativeMsg) {
        SubmitDialog.Builder builder = new SubmitDialog.Builder(context);
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

    @Override
    public void mappingDialog(Context context,String negativeMsg,String smtBalance, String title, String url) {
        SubmitDialog.Builder builder = new SubmitDialog.Builder(context);
        builder.setNegativeButton(negativeMsg,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mView.walletMappingSubmit(dialog);
                            }
                        });
        builder.setMappingClosed(new SubmitDialog.Builder.mappingClosedListener() {
            @Override
            public void mappingClosed(DialogInterface dialog) {
                dialog.dismiss();
                mView.walletMappingClose();
            }
        });
        builder.showMapping(smtBalance,title,url);
        builder.setCancelable(false);
    }

    @Override
    public void mappingSuccessDialog(Context context,String negativeMsg,String mappingId, String content) {
        SubmitDialog.Builder builder = new SubmitDialog.Builder(context);
        builder.setNegativeButton(negativeMsg,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mView.walletMappingSuccessSubmit();
                    }
                });
        builder.setMappingClosed(new SubmitDialog.Builder.mappingClosedListener() {
            @Override
            public void mappingClosed(DialogInterface dialog) {
                dialog.dismiss();
                mView.walletMappingSuccessSubmit();
            }
        });
        builder.showMappingSuccess(mappingId,content);
        builder.setCancelable(false);
    }

    @Override
    public void startMappingMethod(String address , String r , String s , byte v) {
        NetRequestImpl.getInstance().addressMapping(address, r, s, v, new RequestListener() {
            @Override
            public void start() {

            }

            @Override
            public void success(JSONObject response) {
                String mappingId = response.optString("mappingid");
                String content = response.optString("content");
                mView.mappingSuccess(mappingId,content);
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                mView.mappingError(errorCode,errorMsg);
            }
        });
    }
}
