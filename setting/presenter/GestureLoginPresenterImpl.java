package com.lingtuan.firefly.setting.presenter;

import android.text.TextUtils;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.custom.gesturelock.ACache;
import com.lingtuan.firefly.custom.gesturelock.LockPatternUtil;
import com.lingtuan.firefly.custom.gesturelock.LockPatternView;
import com.lingtuan.firefly.setting.GestureLoginActivity;
import com.lingtuan.firefly.setting.contract.GestureLoginContract;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.wallet.util.WalletStorage;
import com.lingtuan.firefly.wallet.vo.StorableWallet;

import java.util.ArrayList;
import java.util.List;

public class GestureLoginPresenterImpl implements GestureLoginContract.Presenter{

    private GestureLoginContract.View mView;

    public GestureLoginPresenterImpl(GestureLoginContract.View view){
        this.mView = view;
        mView.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public StorableWallet initWalletInfo() {
        StorableWallet storableWallet = null;
        ArrayList<StorableWallet> storableWallets = WalletStorage.getInstance(NextApplication.mContext).get();
        int index = -1;
        for (int i = 0 ; i < storableWallets.size(); i++){
            if (storableWallets.get(i).isSelect() ){
                index = i;
                storableWallet = storableWallets.get(i);
                if (TextUtils.isEmpty(storableWallet.getWalletImageId()) || !storableWallet.getWalletImageId().startsWith("icon_static_")){
                    String imgId = Utils.getWalletImg(NextApplication.mContext,i);
                    storableWallet.setWalletImageId(imgId);
                }
                break;
            }
        }
        if (index == -1 && storableWallets.size() > 0){
            String imgId = Utils.getWalletImg(NextApplication.mContext,0);
            storableWallet = storableWallets.get(0);
            storableWallet.setWalletImageId(imgId);
        }
        return storableWallet;
    }

    @Override
    public byte[] getAsBinary(ACache aCache) {
        int walletMode = MySharedPrefs.readInt(NextApplication.mContext, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_IS_WALLET_PATTERN);
        if (walletMode != 0 && NextApplication.myInfo == null){
            return aCache.getAsBinary(Constants.GESTURE_PASSWORD);
        }else{
            return aCache.getAsBinary(Constants.GESTURE_PASSWORD + NextApplication.myInfo.getLocalId());
        }
    }

    @Override
    public void putAsBinary(ACache aCache,int type) {
        int walletMode = MySharedPrefs.readInt(NextApplication.mContext, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_IS_WALLET_PATTERN);
        if (type == 2){
            if (walletMode == 0 && NextApplication.myInfo != null){
                aCache.put(Constants.GESTURE_PASSWORD + NextApplication.myInfo.getLocalId(),"");
            }else{
                aCache.put(Constants.GESTURE_PASSWORD,"");
            }
        }
    }

    @Override
    public GestureLoginActivity.Status getStatus(List<LockPatternView.Cell> pattern,byte[] gesturePassword) {
        int number;
        if (NextApplication.myInfo != null){
            number =  MySharedPrefs.readInt(NextApplication.mContext,MySharedPrefs.FILE_USER,MySharedPrefs.GESTIRE_ERROR + NextApplication.myInfo.getLocalId());
        }else{
            number =  MySharedPrefs.readInt(NextApplication.mContext,MySharedPrefs.FILE_USER,MySharedPrefs.GESTIRE_ERROR);
        }
        if (number > 5){
            return GestureLoginActivity.Status.ERRORMORE;
        }else{
            if(LockPatternUtil.checkPattern(pattern, gesturePassword)) {
                return GestureLoginActivity.Status.CORRECT;
            } else {
                return GestureLoginActivity.Status.ERROR;
            }
        }
    }

    @Override
    public void putGestureErrorNum(int errorNum) {
        if (NextApplication.myInfo != null){
            MySharedPrefs.writeInt(NextApplication.mContext,MySharedPrefs.FILE_USER,MySharedPrefs.GESTIRE_ERROR + NextApplication.myInfo.getLocalId(),errorNum);
        }else{
            MySharedPrefs.writeInt(NextApplication.mContext,MySharedPrefs.FILE_USER,MySharedPrefs.GESTIRE_ERROR,errorNum);
        }
    }
}
