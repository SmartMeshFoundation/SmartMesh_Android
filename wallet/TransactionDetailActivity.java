package com.lingtuan.firefly.wallet;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.custom.MonIndicator;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.ui.WebViewUI;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;
import com.lingtuan.firefly.wallet.contract.TransactionDetailContract;
import com.lingtuan.firefly.wallet.presenter.TransactionDetailPresenterImpl;
import com.lingtuan.firefly.wallet.vo.TransVo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * transaction record detail
 * Created by Administrator on 2017/12/19.
 */

public class TransactionDetailActivity extends BaseActivity implements TransactionDetailContract.View{

    @BindView(R.id.trans_detail_type_img)
    ImageView transDetailImg;//Transfer status img
    @BindView(R.id.trans_detail_money)
    TextView transDetailMoney;//Transfer amount
    @BindView(R.id.trans_detail_money_type)
    TextView transDetailMoneyType;//eth or smt
    @BindView(R.id.trans_detail_type)
    TextView transDetailType;//Transfer status
    @BindView(R.id.monindIcator)
    MonIndicator monindIcator;
    @BindView(R.id.transTypeBody)
    RelativeLayout transTypeBody;
    @BindView(R.id.trans_detail_from)
    TextView transDetailFrom;//from
    @BindView(R.id.trans_detail_to)
    TextView transDetailTo;//to
    @BindView(R.id.trans_detail_fee)
    TextView transDetailFee;//gas fee
    @BindView(R.id.trans_detail_number)
    TextView transDetailNumber;//ticket number
    @BindView(R.id.trans_detail_block_number)
    TextView transDetailBlockNumber;//block number
    @BindView(R.id.trans_detail_time)
    TextView transDetailTime;//transfer time
    @BindView(R.id.trans_detail_quick_mark)
    ImageView transDetailQuickMark;//transfer quickmark

    private TransactionDetailContract.Presenter mPresenter;

    private TransVo transVo;
    private boolean isSendTrans;//is from sendTrans

    private Timer timer;
    private TimerTask timerTask;

    @Override
    protected void setContentView() {
        setContentView(R.layout.transaction_detail_layout);
        getPassData();
    }

    private void getPassData() {
        transVo = (TransVo) getIntent().getSerializableExtra("transVo");
        isSendTrans = getIntent().getBooleanExtra("isSendTrans", false);
    }

    @Override
    protected void findViewById() {

    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void initData() {
        new TransactionDetailPresenterImpl(this);

        setTitle(getString(R.string.transcation_detail));
        if (transVo != null) {
            if (transVo.getValue().startsWith("+") || transVo.getValue().startsWith("-")) {
                transDetailMoney.setText(transVo.getValue().substring(1, transVo.getValue().length()));
            } else {
                transDetailMoney.setText(transVo.getValue());
            }
            transDetailNumber.setText(transVo.getTx());
            transDetailTime.setText(Utils.transDetailTime(transVo.getTime()));
            transDetailQuickMark.setImageBitmap(mPresenter.createQRCodeBitmap(transVo.getTxurl(), Utils.dip2px(TransactionDetailActivity.this, 120)));
            transDetailFee.setText(getString(R.string.smt_er_lower, transVo.getFee()));

            if (transVo.getTxBlockNumber() <= 0) {
                transDetailBlockNumber.setText(getString(R.string.wallet_trans_detail_block_none));
            } else {
                transDetailBlockNumber.setText(transVo.getTxBlockNumber() + "");
            }
            transDetailMoneyType.setText(transVo.getSymbol());
            transDetailTo.setText(transVo.getToAddress());
            if (!TextUtils.isEmpty(transVo.getFromAddress())) {
                transDetailFrom.setText(transVo.getFromAddress());
            }

            if (transVo.getState() != -1) {
                if (transVo.getBlockNumber() - transVo.getTxBlockNumber() < 12) {
                    transVo.setState(0);
                }
            }

            switch (transVo.getState()) {
                case -1:
                    transTypeBody.setVisibility(View.VISIBLE);
                    transDetailType.setVisibility(View.VISIBLE);
                    monindIcator.setVisibility(View.VISIBLE);
                    transDetailType.setText(getString(R.string.wallet_trans_detail_type_0));
                    transDetailImg.setImageResource(R.drawable.trans_detail_wait);
                    transDetailState();
                    break;
                case 0:
                    transTypeBody.setVisibility(View.VISIBLE);
                    monindIcator.setVisibility(View.GONE);
                    transDetailType.setVisibility(View.VISIBLE);
                    if (transVo.getBlockNumber() - transVo.getTxBlockNumber() < 0) {
                        transDetailType.setText(getString(R.string.wallet_trans_detail_type_1, 1));
                    } else {
                        transDetailType.setText(getString(R.string.wallet_trans_detail_type_1, transVo.getBlockNumber() - transVo.getTxBlockNumber() + 1));
                    }
                    transDetailImg.setImageResource(R.drawable.trans_detail_wait);
                    transDetailState();
                    break;
                case 1:
                    transDetailImg.setImageResource(R.drawable.trans_detail_success);
                    transTypeBody.setVisibility(View.GONE);
                    break;
                case 2:
                    transDetailImg.setImageResource(R.drawable.trans_detail_failed);
                    transTypeBody.setVisibility(View.GONE);
                    break;
            }
        }
    }


    @OnClick({R.id.trans_detail_copy,R.id.trans_detail_number})
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.trans_detail_copy:
                Utils.copyText(TransactionDetailActivity.this, transVo.getTxurl());
                break;
            case R.id.trans_detail_number:
                if (!TextUtils.isEmpty(transVo.getTxurl())) {
                    Intent intent = new Intent(TransactionDetailActivity.this, WebViewUI.class);
                    intent.putExtra("loadUrl", transVo.getTxurl());
                    intent.putExtra("title", getString(R.string.transcation_search));
                    startActivity(intent);
                    Utils.openNewActivityAnim(TransactionDetailActivity.this, false);
                }
                break;
        }
    }


    /**
     * Turn on the timer to call the interface every 12 seconds
     */
    private void transDetailState() {
        if (timer == null) {
            timer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    mPresenter.getTransactionBlock(transVo.getTx());
                }
            };
            timer.schedule(timerTask, 0, 10000);
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    transTypeBody.setVisibility(View.VISIBLE);
                    transDetailType.setVisibility(View.VISIBLE);
                    monindIcator.setVisibility(View.GONE);
                    transDetailType.setText(getString(R.string.wallet_trans_detail_type_1, 1));
                    if (transVo.getTxBlockNumber() <= 0) {
                        transDetailBlockNumber.setText(getString(R.string.wallet_trans_detail_block_none));
                    } else {
                        int blockNumberDifference = transVo.getBlockNumber() - transVo.getTxBlockNumber();
                        transDetailBlockNumber.setText(transVo.getTxBlockNumber() + "");
                        if (blockNumberDifference >= 11) {
                            transTypeBody.setVisibility(View.GONE);
                            transDetailImg.setImageResource(R.drawable.trans_detail_success);
                            transVo.setState(1);
                            cancelTimer();
                        } else {
                            transTypeBody.setVisibility(View.VISIBLE);
                            transDetailType.setVisibility(View.VISIBLE);
                            transDetailType.setText(getString(R.string.wallet_trans_detail_type_1, blockNumberDifference + 1));
                        }
                    }
                    break;
                case 2:
                    transDetailImg.setImageResource(R.drawable.trans_detail_failed);
                    transTypeBody.setVisibility(View.GONE);
                    if (transVo.getTxBlockNumber() <= 0) {
                        transDetailBlockNumber.setText(getString(R.string.wallet_trans_detail_block_none));
                    } else {
                        transDetailBlockNumber.setText(transVo.getTxBlockNumber() + "");
                    }
                    cancelTimer();
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelTimer();
        if (isSendTrans) {
            FinalUserDataBase.getInstance().insertTrans(transVo, true);
        } else {
            FinalUserDataBase.getInstance().updateTransTemp(transVo);
        }

        if (mHandler != null){
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
    }

    private void cancelTimer(){
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    @Override
    public void setPresenter(TransactionDetailContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public void success(int transBlockNumber, int lastBlockNumber, int state, Message message) {
        transVo.setState(state);
        transVo.setTxBlockNumber(transBlockNumber);
        transVo.setBlockNumber(lastBlockNumber);
        mHandler.sendMessage(message);
    }

    @Override
    public void error(int errorCode, String errorMsg) {
        showToast(errorMsg);
    }

}
