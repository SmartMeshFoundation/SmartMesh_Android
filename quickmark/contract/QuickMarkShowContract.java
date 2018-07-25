package com.lingtuan.firefly.quickmark.contract;

import android.graphics.Bitmap;

import com.lingtuan.firefly.base.BasePresenter;
import com.lingtuan.firefly.base.BaseView;
import com.lingtuan.firefly.wallet.vo.StorableWallet;

public interface QuickMarkShowContract {

    interface Presenter extends BasePresenter{
        Bitmap createQRCodeBitmap(String content , int widthAndHeight);

        StorableWallet getWallet();
    }

    interface View extends BaseView<Presenter>{

    }
}
