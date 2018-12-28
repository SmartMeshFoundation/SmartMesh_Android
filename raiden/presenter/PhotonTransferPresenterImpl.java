package com.lingtuan.firefly.raiden.presenter;

import android.content.Context;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.network.NetRequestImpl;
import com.lingtuan.firefly.raiden.PhotonTransferUI;
import com.lingtuan.firefly.raiden.RaidenStatusType;
import com.lingtuan.firefly.raiden.contract.PhotonTransferContract;
import com.lingtuan.firefly.setting.contract.BindMobileContract;
import com.lingtuan.firefly.spectrum.util.WalletStorage;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.MyToast;
import com.lingtuan.firefly.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.web3j.crypto.CipherException;

import java.io.IOException;
import java.math.BigDecimal;

public class PhotonTransferPresenterImpl implements PhotonTransferContract.Presenter{

    private PhotonTransferContract.View mView;

    public PhotonTransferPresenterImpl(PhotonTransferContract.View view){
        this.mView = view;
        mView.setPresenter(this);
    }

    @Override
    public void tokenAddressRequest(String address) {
        NetRequestImpl.getInstance().tokenAddressRequest(address.toLowerCase(), new RequestListener() {
            @Override
            public void start() {

            }

            @Override
            public void success(JSONObject response) {
                mView.uploadTokenSuccess(response);
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                mView.uploadTokenError(errorCode,errorMsg);
            }
        });
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
    public boolean checkTokenStatus(String address) {
        String jsonString = MySharedPrefs.readString(NextApplication.mContext,MySharedPrefs.FILE_USER,MySharedPrefs.KEY_PHOTON_TOKEN_LIST);
        boolean hasFound = false;
        try {
            if (!TextUtils.isEmpty(jsonString)){
                JSONArray array = new JSONArray(jsonString);
                if (array.length() > 0){
                    for (int i = 0 ; i < array.length() ; i++){
                        JSONObject arrayObject = array.optJSONObject(i);
                        if (!TextUtils.isEmpty(address) && address.equalsIgnoreCase(arrayObject.optString("token"))){
                            hasFound = true;
                            break;
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return hasFound;
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
                    }
                } else {
                    mView.loadChannelError();
                }
            } catch (Exception e) {
                mView.loadChannelError();
            }
        });
    }

    /**
     * Get private key
     * @param walletPwd wallet password
     * */
    @Override
    public void checkWalletExist(Context context ,final String walletPwd,final String walletAddress){
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

    @Override
    public void photonTransferMethod(String amount,String walletAddress) {
        if (TextUtils.isEmpty(amount)) {
            return;
        }
        mView.transferCheck();
        NextApplication.mRaidenThreadPool.execute(() -> {
            try {
                if (NextApplication.api != null) {
                    BigDecimal ONE_ETHER = new BigDecimal("1000000000000000000");
                    String balance = new BigDecimal(amount).multiply(ONE_ETHER).toBigInteger().toString();
                    boolean isNet;
                    if (NextApplication.mRaidenStatusVo != null) {
                        try {
                            if (RaidenStatusType.Connected == NextApplication.mRaidenStatusVo.getEthStatus() && RaidenStatusType.Connected == NextApplication.mRaidenStatusVo.getXMPPStatus() && NextApplication.netWorkOnline) {
                                isNet = true;
                            }else{
                                isNet = false;
                            }
                        } catch (Exception e) {
                            isNet = true;
                        }
                    }else{
                        isNet = false;
                    }
                    String str = NextApplication.api.transfers(NextApplication.myInfo.getTokenAddress(), walletAddress, balance, "0", "", !isNet);
                    JSONObject obj = new JSONObject(str);
                    if(obj.has("lockSecretHash")){
                        mView.transferLockSecretHash(obj.optString("lockSecretHash"));
                    }else{
                        mView.transferError();
                    }
                } else {
                    mView.transferError();
                }
            } catch (Exception e) {
                mView.transferError();
            }
        });
    }

    @Override
    public void start() {

    }


}
