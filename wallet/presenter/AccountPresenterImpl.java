package com.lingtuan.firefly.wallet.presenter;

import android.text.TextUtils;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.custom.gesturelock.ACache;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.language.LanguageType;
import com.lingtuan.firefly.language.MultiLanguageUtil;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.network.NetRequestImpl;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.wallet.contract.AccountContract;
import com.lingtuan.firefly.wallet.util.WalletStorage;
import com.lingtuan.firefly.wallet.vo.StorableWallet;
import com.lingtuan.firefly.wallet.vo.TokenVo;
import com.lingtuan.firefly.wallet.vo.TransVo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class AccountPresenterImpl implements AccountContract.Presenter {

    private AccountContract.View mView;

    private Timer timer;
    private TimerTask timerTask;
    private int timerLine = 10;

    private Timer cnytimer;
    private TimerTask cnytimerTask;
    private int cnytimerLine = 5;

    public AccountPresenterImpl(AccountContract.View view){
        this.mView = view;
        mView.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public boolean setWalletGesture() {
        int walletMode = MySharedPrefs.readInt(NextApplication.mContext, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_IS_WALLET_PATTERN);
        byte[] gestureByte;
        if (walletMode == 0 && NextApplication.myInfo != null) {
            gestureByte = ACache.get(NextApplication.mContext).getAsBinary(Constants.GESTURE_PASSWORD + NextApplication.myInfo.getLocalId());
        } else {
            gestureByte = ACache.get(NextApplication.mContext).getAsBinary(Constants.GESTURE_PASSWORD);
        }

        if (gestureByte != null && gestureByte.length > 0) {
            return true;
        }
        return false;
    }

    /**
     * Get all transaction records for the specified address
     * @param address wallet address
     */
    @Override
    public void getAllTransactionList(String address) {
        if (!address.startsWith("0x")) {
            address = "0x" + address;
        }
        final String finalAddress = address;
        NetRequestImpl.getInstance().getAllTransactionList(address, new RequestListener() {
            @Override
            public void start() {

            }

            @Override
            public void success(JSONObject response) {
                MySharedPrefs.writeBoolean(NextApplication.mContext, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_WALLET_ALL_TRANS + finalAddress, true);
                JSONArray array = response.optJSONArray("data");
                int blockNumber = response.optInt("blockNumber");
                if (array != null) {
                    FinalUserDataBase.getInstance().beginTransaction();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obiect = array.optJSONObject(i);
                        TransVo transVo = new TransVo().parse(obiect);
                        transVo.setBlockNumber(blockNumber);
                        transVo.setState(1);
                        FinalUserDataBase.getInstance().updateTrans(transVo);
                    }
                    FinalUserDataBase.getInstance().endTransactionSuccessful();
                }
            }

            @Override
            public void error(int errorCode, String errorMsg) {

            }
        });
    }

    @Override
    public void getTokenList(final ArrayList<TokenVo> tokenVos,final String address,final boolean isShowToast) {
        NetRequestImpl.getInstance().getTokenList(address, new RequestListener() {
            @Override
            public void start() {

            }

            @Override
            public void success(JSONObject response) {
                tokenVos.clear();
                JSONArray array = response.optJSONArray("data");
                if (array != null) {
                    for (int i = 0; i < array.length(); i++) {
                        TokenVo tokenVo = new TokenVo().parse(array.optJSONObject(i));
                        if (tokenVo.isFixed()) {
                            tokenVo.setChecked(true);
                        }
                        tokenVos.add(tokenVo);
                    }
                }
                FinalUserDataBase.getInstance().beginTransaction();
                for (int i = 0; i < tokenVos.size(); i++) {
                    FinalUserDataBase.getInstance().updateTokenList(tokenVos.get(i), address, true);
                }
                FinalUserDataBase.getInstance().endTransactionSuccessful();
                MySharedPrefs.writeBoolean(NextApplication.mContext, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_FIRST_GET_TOKEN_LIST + address, false);
                mView.getTokenListSuccess(tokenVos,isShowToast);
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                mView.getTokenListError(isShowToast);
            }
        });
    }

    @Override
    public void getBalance(final ArrayList<TokenVo> tokens, final String address, final boolean isShowToast) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < tokens.size(); i++) {
            builder.append(tokens.get(i).getContactAddress()).append(",");
        }
        if (builder.length() > 0) {
             builder.deleteCharAt(builder.length() - 1);
        }
        NetRequestImpl.getInstance().getBalance(address, builder.toString(), new RequestListener() {
            @Override
            public void start() {

            }

            @Override
            public void success(JSONObject response) {
                tokens.clear();
                String total = response.optString("total");
                String usdTotal = response.optString("usd_total");
                JSONArray array = response.optJSONArray("data");
                if (array != null) {
                    for (int i = 0; i < array.length(); i++) {
                        TokenVo tokenVo = new TokenVo().parse(array.optJSONObject(i));
                        tokenVo.setChecked(true);
                        tokens.add(tokenVo);
                    }
                }

                FinalUserDataBase.getInstance().beginTransaction();
                for (int i = 0; i < tokens.size(); i++) {
                    FinalUserDataBase.getInstance().updateTokenList(tokens.get(i), address, false);
                }
                FinalUserDataBase.getInstance().endTransactionSuccessful();
                mView.getBalanceSuccess(tokens,total,usdTotal);
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                mView.getBalanceError(errorCode,errorMsg,isShowToast,tokens);
            }
        });
    }


    @Override
    public void showPowTimer() {
        if (timer == null) {
            timer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    timerLine--;
                    if (timerLine < 0) {
                        mView.cancelHomePop();
                        cancelTimer();
                    }
                }
            };
            timer.schedule(timerTask, 1000, 1000);
        }
    }

    @Override
    public void cancelTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    /**
     * Control the popup countdown
     */
    @Override
    public void showCnyTimer() {
        if (cnytimer == null) {
            cnytimer = new Timer();
            cnytimerTask = new TimerTask() {
                @Override
                public void run() {
                    cnytimerLine--;
                    if (cnytimerLine < 0) {
                        mView.cancelCnyView();
                        cancelCnyTimer();
                    }
                }
            };
            cnytimer.schedule(cnytimerTask, 1000, 1000);
        }
    }

    @Override
    public void cancelCnyTimer() {
        if (cnytimer != null) {
            cnytimer.cancel();
            cnytimer = null;
        }
        if (cnytimerTask != null) {
            cnytimerTask.cancel();
            cnytimerTask = null;
        }
    }

    @Override
    public boolean checkLanguage() {
        int language = MultiLanguageUtil.getInstance().getLanguageType();
        return LanguageType.LANGUAGE_CHINESE_SIMPLIFIED == language;
    }


    /**
     * Load or refresh the wallet information
     */
    @Override
    public StorableWallet getStorableWallet() {
        int index = -1;//Which one is selected
        ArrayList<StorableWallet> storableWallets = WalletStorage.getInstance(NextApplication.mContext).get();
        StorableWallet storableWallet = null;
        for (int i = 0; i < storableWallets.size(); i++) {
            if (storableWallets.get(i).isSelect()) {
                WalletStorage.getInstance(NextApplication.mContext).updateWalletToList(NextApplication.mContext, storableWallets.get(i).getPublicKey(), false);
                index = i;
                storableWallet = storableWallets.get(i);
                if (TextUtils.isEmpty(storableWallet.getWalletImageId()) || !storableWallet.getWalletImageId().startsWith("icon_static_")) {
                    String imgId = Utils.getWalletImg(NextApplication.mContext, i);
                    storableWallet.setWalletImageId(imgId);
                }
                break;
            }
        }
        if (index == -1 && storableWallets.size() > 0) {
            storableWallet = storableWallets.get(0);
            if (TextUtils.isEmpty(storableWallet.getWalletImageId()) || !storableWallet.getWalletImageId().startsWith("icon_static_")) {
                String imgId = Utils.getWalletImg(NextApplication.mContext, 0);
                storableWallet.setWalletImageId(imgId);
            }
        }
        return storableWallet;
    }

}
