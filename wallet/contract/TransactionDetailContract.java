package com.lingtuan.firefly.wallet.contract;

import android.graphics.Bitmap;
import android.os.Message;

import com.lingtuan.firefly.base.BasePresenter;
import com.lingtuan.firefly.base.BaseView;
import com.lingtuan.firefly.wallet.vo.TransVo;

import java.util.ArrayList;

public interface TransactionDetailContract {

    interface Presenter extends BasePresenter{

        /**
         * create qr code bitmap
         * @param content content
         * @param widthAndHeight with and height
         * */
        Bitmap createQRCodeBitmap(String content, int widthAndHeight);

        /**
         * get transaction block
         * @param tx transaction tx
         * */
        void getTransactionBlock(String tx);
    }

    interface View extends BaseView<Presenter>{

        /**
         * get transaction block success
         * @param transBlockNumber trans block number
         * @param lastBlockNumber last block number
         * @param state state   -1 unpackaged 、0 Waits for 12 blocks to be confirmed 、1 Transaction Completed 、2 Transaction failed
         * @param message  message
         * */
        void success(int transBlockNumber,int lastBlockNumber,int state,Message message);

        /**
         * get transaction block error
         * @param errorCode error code
         * @param errorMsg  error message
         * */
        void error(int errorCode, String errorMsg);
    }

}
