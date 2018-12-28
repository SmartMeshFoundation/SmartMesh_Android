package com.lingtuan.firefly.raiden.presenter;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.network.NetRequestImpl;
import com.lingtuan.firefly.raiden.RaidenStatusType;
import com.lingtuan.firefly.raiden.contract.PhotonChannelListContract;
import com.lingtuan.firefly.spectrum.util.WalletStorage;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.MyToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.web3j.crypto.CipherException;

import java.io.IOException;
import java.math.BigDecimal;

public class PhotonChannelListPresenterImpl implements PhotonChannelListContract.Presenter{

    private PhotonChannelListContract.View mView;

    public PhotonChannelListPresenterImpl(PhotonChannelListContract.View view){
        this.mView = view;
        mView.setPresenter(this);
    }

    @Override
    public void setPhotonStatus(ImageView mEthStatus,ImageView mXmppStatus) {
        if (NextApplication.mRaidenStatusVo != null && RaidenStatusType.Connected == NextApplication.mRaidenStatusVo.getEthStatus()) {
            mEthStatus.setImageResource(R.drawable.raiden_top_connected);
        } else if (NextApplication.mRaidenStatusVo != null && RaidenStatusType.Default == NextApplication.mRaidenStatusVo.getEthStatus()) {
            mEthStatus.setImageResource(R.drawable.raiden_top_default);
        } else {
            mEthStatus.setImageResource(R.drawable.raiden_top_no_connected);
        }
        if (NextApplication.mRaidenStatusVo != null && RaidenStatusType.Connected == NextApplication.mRaidenStatusVo.getXMPPStatus()) {
            mXmppStatus.setImageResource(R.drawable.raiden_top_connected);
        } else if (NextApplication.mRaidenStatusVo != null && RaidenStatusType.Default == NextApplication.mRaidenStatusVo.getXMPPStatus()) {
            mXmppStatus.setImageResource(R.drawable.raiden_top_default);
        } else {
            mXmppStatus.setImageResource(R.drawable.raiden_top_no_connected);
        }
    }

    @Override
    public void checkWalletExist(Context context, String walletPwd, String walletAddress) {
        if (TextUtils.isEmpty(walletPwd)){
            MyToast.showToast(context,context.getString(R.string.wallet_copy_pwd_error));
            return;
        }
        LoadingDialog.show(context,"");
        new Thread(() -> {
            try {
                String address = walletAddress;
                if (!TextUtils.isEmpty(address) && !address.contains("0x")){
                    address = "0x" + address;
                }
                WalletStorage.getInstance(NextApplication.mContext).getFullWallet(context,walletPwd,address);
                mView.checkWalletExistSuccess(walletPwd);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (CipherException e) {
                e.printStackTrace();
                mView.checkWalletExistError();
            }
        }).start();
    }

    /**
     * load channel list
     */
    @Override
    public void loadChannelList() {
        NextApplication.mRaidenThreadPool.execute(() -> {
            try {
                if (NextApplication.api != null) {
                    String str = NextApplication.api.getChannelList();
                    if (!TextUtils.isEmpty(str)) {
                        mView.loadChannelSuccess(str);
                    }else{
                        mView.loadChannelError(false);
                    }
                } else {
                    mView.loadChannelError(true);
                }
            } catch (Exception e) {
                mView.loadChannelError(true);
            }
        });
    }

    @Override
    public void start() {

    }


}
