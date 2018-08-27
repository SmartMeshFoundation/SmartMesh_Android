package com.lingtuan.firefly.ui.contract;

import android.content.Context;
import android.content.DialogInterface;

import com.lingtuan.firefly.base.BasePresenter;
import com.lingtuan.firefly.base.BaseView;
import com.lingtuan.firefly.wallet.vo.StorableWallet;

public interface AlertContract {

    interface Presenter extends BasePresenter {

        /**
         * update version dialog
         * @param context      context
         * @param title        dialog title
         * @param describe     dialog describe
         * @param url           apk url
         * @param positiveMsg  cancel
         * @param negativeMsg  submit
         * */
        void updateVersionDialog(Context context,String title, String describe, final String url,String positiveMsg,String negativeMsg);

        /**
         * update version dialog now
         * @param context      context
         * @param title        dialog title
         * @param describe     dialog describe
         * @param url           apk url
         * @param negativeMsg  submit
         * */
        void updateVersionNowDialog(Context context,String title, String describe, final String url,String negativeMsg);

        /**
         * show offline dialog
         * @param context      context
         * @param title        dialog title
         * @param msg          dialog message
         * @param negativeMsg  submit
         * */
        void showOfflineDialog(Context context,String title,String msg,String negativeMsg);

        /**
         * show smart mesh dialog
         * @param context      context
         * @param title        dialog title
         * @param message          dialog message
         * @param positiveMsg  cancel
         * @param negativeMsg  submit
         * */
        void showSmartMeshDialog(Context context,String title ,String message,String positiveMsg,String negativeMsg);

        /**
         * show wallet dialog
         * @param context      context
         * @param title        dialog title
         * @param message      dialog message
         * @param negativeMsg  submit
         * */
        void showWalletDialog(Context context,String title , String message, String negativeMsg);

        /**
         * show backup dialog
         * @param context          context
         * @param title            dialog title
         * @param message          dialog message
         * @param negativeMsg      submit
         * @param storableWallet   wallet info
         * */
        void showBackupDialog(Context context,String title , String message, String negativeMsg, StorableWallet storableWallet);

        void mappingDialog(Context context,String negativeMsg,String smtBalance, String title, String url);

        void mappingSuccessDialog(Context context,String negativeMsg,String mappingId, String content);

        /**
         * start mapping
         * @param address wallet address
         * */
        void startMappingMethod(String address ,String r ,String s ,byte v);
    }

    interface View extends BaseView<Presenter> {

        /**
         * update version submit
         * @param url    apk url
         * */
        void updateVersionDialogSubmit(String url);

        /**
         * update version cancel
         * */
        void updateVersionDialogCancel();

        /**
         * offline submit
         * */
        void offlineDialogSubmit();

        /**
         * smart mesh submit
         * @param isNegative   true submit  false  cancel
         * */
        void smartMeshDialogSubmit(boolean isNegative);

        /**
         * wallet dialog submit
         * */
        void walletDialogSubmit();

        /**
         * backup submit
         * @param storableWallet   wallet info
         * */
        void backUpDialogSubmit(StorableWallet storableWallet);

        void walletMappingSubmit(DialogInterface dialog);

        void walletMappingClose();

        void walletMappingSuccessSubmit();

        /**
         * start mapping success
         * */
        void mappingSuccess(String mappingId,String content);

        /**
         * start mapping success
         * */
        void mappingError(int errorCode, String errorMsg);


    }
}
