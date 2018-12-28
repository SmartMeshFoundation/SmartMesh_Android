package com.lingtuan.firefly.raiden.contract;

import android.content.Context;
import android.widget.ImageView;

import com.lingtuan.firefly.base.BasePresenter;
import com.lingtuan.firefly.base.BaseView;

import org.json.JSONObject;

public interface PhotonTransferContract {

    interface Presenter extends BasePresenter {

        /***
         * 更改地址，上报服务器
         * @param address wallet address
         */
        void tokenAddressRequest(String address);

        /**
         * 设置光子网络状态
         * @param mEthStatus 光子状态
         * @param mXmppStatus xmpp状态
         * */
        void setPhotonStatus(ImageView mEthStatus, ImageView mXmppStatus);

        /**
         * 检测token状态
         * @param address 钱包地址
         * */
        boolean checkTokenStatus(String address);

        /**
         * 获取通道列表
         * */
        void loadChannelList();

        /**
         * 检测地址钱包是否存在
         * @param context 上下文
         * @param walletPwd 钱包密码
         * @param walletAddress 钱包地址
         * */
        void checkWalletExist(Context context , final String walletPwd, String walletAddress);

        /**
         * 光子网络转账
         * @param amount 转账金额
         * @param walletAddress 转账地址
         * */
        void photonTransferMethod(String amount,String walletAddress);


    }

    interface View extends BaseView<Presenter> {

        /** * Data request is successful*/
        void uploadTokenSuccess(JSONObject response);

        /*** Data request failed*/
        void uploadTokenError(int errorCode, String errorMsg);

        void loadChannelSuccess(String jsonString);

        void loadChannelError();

        void checkWalletExistSuccess(String walletPwd);

        void checkWalletExistError();

        void transferCheck();

        void transferLockSecretHash(String lockSecretHash);

        void transferError();
    }
}
