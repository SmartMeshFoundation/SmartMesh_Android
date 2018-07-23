package com.lingtuan.firefly.wallet.presenter;

import android.text.TextUtils;

import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;
import com.lingtuan.firefly.wallet.contract.WalletSendDetailContract;
import com.lingtuan.firefly.wallet.vo.TransVo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class WalletSendDetailPresenterImpl implements WalletSendDetailContract.Presenter{

    private WalletSendDetailContract.View mView;

    private Timer timer;
    private TimerTask timerTask;
    final StringBuilder builder = new StringBuilder();
    private ArrayList<TransVo> transVos = new ArrayList<>();

    public WalletSendDetailPresenterImpl(WalletSendDetailContract.View view){
        this.mView = view;
        mView.setPresenter(this);
    }


    @Override
    public void start() {

    }

    /**
     * Turn on the timer to call the interface every 12 seconds
     * */
    @Override
    public void loadData(final String contactAddress, final String walletAddress) {
        if (timer == null) {
            timer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    List<TransVo> mlist = FinalUserDataBase.getInstance().getTransTempList(contactAddress,walletAddress, true);
                    if (mlist == null || mlist.size() <= 0) {
                        if (timer != null) {
                            timer.cancel();
                            timer = null;
                        }
                        if (timerTask != null) {
                            timerTask.cancel();
                            timerTask = null;
                        }
                        return;
                    }
                    builder.delete(0, builder.length());
                    for (int i = 0; i < mlist.size(); i++) {
                        builder.append(mlist.get(i).getTx()).append(",");
                    }
                    if (builder.length() > 0) {
                        builder.deleteCharAt(builder.length() - 1);
                    }
                    getTranscationBlock(builder.toString());
                }
            };
            timer.schedule(timerTask, 0, 10000);
        }
    }

    /**
     * Get the block number of the transaction hash
     */
    private void getTranscationBlock(String txList) {
        NetRequestImpl.getInstance().getTxBlockNumber(txList, new RequestListener() {
            @Override
            public void start() {

            }

            @Override
            public void success(JSONObject response) {
                JSONArray array = response.optJSONArray("data");
                int lastBlockNumber = response.optInt("blockNumber", 0);
                if (array != null) {
                    FinalUserDataBase.getInstance().beginTransaction();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = array.optJSONObject(i);
                        int transBlockNumber = object.optInt("txBlockNumber", 0);
                        int state = object.optInt("state", 0);
                        String tx = object.optString("tx");
                        for (int j = 0; j < transVos.size(); j++) {
                            if (TextUtils.equals(tx, transVos.get(j).getTx())) {
                                transVos.get(j).setTxBlockNumber(transBlockNumber);
                                transVos.get(j).setBlockNumber(lastBlockNumber);
                                transVos.get(j).setState(state);
                                FinalUserDataBase.getInstance().updateTransTemp(transVos.get(j));
                            }
                        }
                    }
                    FinalUserDataBase.getInstance().endTransactionSuccessful();
                    mView.success(transVos);
                }
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                mView.error(errorCode,errorMsg);
            }
        });
    }

    @Override
    public void onDestroy() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }
}
