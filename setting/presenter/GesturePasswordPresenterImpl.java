package com.lingtuan.firefly.setting.presenter;

import android.content.Intent;
import android.text.TextUtils;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.custom.gesturelock.ACache;
import com.lingtuan.firefly.setting.GesturePasswordLoginActivity;
import com.lingtuan.firefly.setting.contract.GesturePasswordLoginContract;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.wallet.util.WalletStorage;
import com.lingtuan.firefly.wallet.vo.StorableWallet;

import java.util.ArrayList;

public class GesturePasswordPresenterImpl implements GesturePasswordLoginContract.Presenter{

    private GesturePasswordLoginContract.View mView;

    public GesturePasswordPresenterImpl(GesturePasswordLoginContract.View view){
        this.mView = view;
        mView.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public StorableWallet initWalletInfo(int type) {
        ArrayList<StorableWallet> storableWallets;
        StorableWallet storableWallet = null;
        int index = -1;
        if (type == 4){
            storableWallets = WalletStorage.getInstance(NextApplication.mContext).getAll();
        }else{
            storableWallets = WalletStorage.getInstance(NextApplication.mContext).get();
        }
        if (storableWallets == null || storableWallets.size() <= 0){
            return null;
        }
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
            storableWallet = storableWallets.get(0);
            String imgId = Utils.getWalletImg(NextApplication.mContext,0);
            storableWallet.setWalletImageId(imgId);
        }
        return storableWallet;
    }

    @Override
    public void setWalletSelected(int type,int index) {
        ArrayList<StorableWallet> storableWallets;
        if (type == 4){
            storableWallets = WalletStorage.getInstance(NextApplication.mContext).getAll();
        }else{
            storableWallets = WalletStorage.getInstance(NextApplication.mContext).get();
        }
        for (int i = 0 ; i < storableWallets.size(); i++){
            if (i != index){
                if (type == 4){
                    WalletStorage.getInstance(NextApplication.mContext).getAll().get(i).setSelect(false);
                }else{
                    WalletStorage.getInstance(NextApplication.mContext).get().get(i).setSelect(false);
                }
            }else{
                if (type == 4){
                    WalletStorage.getInstance(NextApplication.mContext).getAll().get(i).setSelect(true);
                }else{
                    WalletStorage.getInstance(NextApplication.mContext).get().get(i).setSelect(true);
                }
            }
        }
    }

    @Override
    public void gesturePasswordSuccess(int type,StorableWallet storableWallet) {
        if (type == 3 || type == 4){
            MySharedPrefs.writeInt(NextApplication.mContext, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_IS_WALLET_PATTERN, 2);
            Intent intent = new Intent(Constants.ACTION_GESTURE_LOGIN);
            WalletStorage.getInstance(NextApplication.mContext).addWalletList(storableWallet,NextApplication.mContext);
            Utils.sendBroadcastReceiver(NextApplication.mContext,intent,false);
        }else{
            if (NextApplication.myInfo != null){
                MySharedPrefs.writeInt(NextApplication.mContext,MySharedPrefs.FILE_USER,MySharedPrefs.GESTIRE_ERROR + NextApplication.myInfo.getLocalId(),0);
                if (type == 2){
                    ACache.get(NextApplication.mContext).put(Constants.GESTURE_PASSWORD + NextApplication.myInfo.getLocalId(),"");
                }else{
                    Intent intent = new Intent(Constants.ACTION_GESTURE_LOGIN);
                    Utils.sendBroadcastReceiver(NextApplication.mContext,intent,false);
                }
            }
        }
    }
}
