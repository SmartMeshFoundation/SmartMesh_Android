package com.lingtuan.firefly.raiden.contract;

import android.content.Context;
import android.widget.ImageView;

import com.lingtuan.firefly.base.BasePresenter;
import com.lingtuan.firefly.base.BaseView;

import org.json.JSONObject;

public interface PhotonChannelListContract {

    interface Presenter extends BasePresenter {

        /**
         * 设置光子网络状态
         * @param mEthStatus 光子状态
         * @param mXmppStatus xmpp状态
         * */
        void setPhotonStatus(ImageView mEthStatus, ImageView mXmppStatus);

        /**
         * 检测地址钱包是否存在
         * @param context 上下文
         * @param walletPwd 钱包密码
         * @param walletAddress 钱包地址
         * */
        void checkWalletExist(Context context , final String walletPwd, String walletAddress);

        /**
         * 获取通道列表
         * */
        void loadChannelList();

    }

    interface View extends BaseView<Presenter> {

        void loadChannelSuccess(String jsonString);

        void loadChannelError(boolean showToast);

        void checkWalletExistSuccess(String walletPwd);

        void checkWalletExistError();
    }
}
