package com.lingtuan.firefly.setting.contract;

import com.lingtuan.firefly.base.BasePresenter;
import com.lingtuan.firefly.base.BaseView;
import com.lingtuan.firefly.custom.gesturelock.ACache;
import com.lingtuan.firefly.custom.gesturelock.LockPatternView;
import com.lingtuan.firefly.setting.GestureLoginActivity;
import com.lingtuan.firefly.wallet.vo.StorableWallet;

import java.util.List;

public interface GestureLoginContract {

    interface Presenter extends BasePresenter {

        /**
         * get current wallet mode
         * */
        StorableWallet initWalletInfo();

        /**
         * get gesture info
         * @param aCache ACache
         * */
        byte[] getAsBinary(ACache aCache);

        /**
         * put gesture info
         * @param aCache ACache
         * @param type   gesture mode
         * */
        void putAsBinary(ACache aCache,int type);

        /**
         * get gesture status
         * @param pattern LockPatternView.Cell
         * @param gesturePassword gesture info
         * */
        GestureLoginActivity.Status getStatus(List<LockPatternView.Cell> pattern,byte[] gesturePassword);

        /**
         * put gesture error number
         * @param errorNum  error number
         * */
        void putGestureErrorNum(int errorNum);
    }

    interface View extends BaseView<Presenter> {

    }

}
