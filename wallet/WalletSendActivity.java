package com.lingtuan.firefly.wallet;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.custom.SwitchButtonLine;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.quickmark.CaptureActivity;
import com.lingtuan.firefly.ui.WebViewUI;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.MyViewDialogFragment;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;
import com.lingtuan.firefly.wallet.util.WalletStorage;
import com.lingtuan.firefly.wallet.vo.GasVo;
import com.lingtuan.firefly.wallet.vo.TokenVo;
import com.lingtuan.firefly.wallet.vo.TransVo;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.core.methods.request.RawTransaction;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;

/**
 * send token
 * Created on 2017/8/22.
 */

public class WalletSendActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener {

    @BindView(R.id.customBody)
    LinearLayout customBody;
    @BindView(R.id.helpBody)
    LinearLayout helpBody;
    @BindView(R.id.seekbarBody)
    LinearLayout seekbarBody;
    @BindView(R.id.customGasPrice)
    EditText customGasPrice;
    @BindView(R.id.customGas)
    EditText customGas;
    @BindView(R.id.customGasNum)
    TextView customGasNum;
    @BindView(R.id.walletHelp)
    ImageView walletHelp;
    @BindView(R.id.sendSwitchButton)
    SwitchButtonLine switchButton;
    @BindView(R.id.fromName)
    TextView fromName;
    @BindView(R.id.fromAddress)
    TextView fromAddress;
    @BindView(R.id.toValue)
    EditText toValue;
    @BindView(R.id.toAddress)
    EditText toAddress;
    @BindView(R.id.send)
    TextView send;
    @BindView(R.id.seekbar)
    SeekBar seekbar;
    @BindView(R.id.gas)
    TextView gas;
    @BindView(R.id.gasTip)
    TextView gasTip;
    @BindView(R.id.balance)
    TextView balance;
    @BindView(R.id.app_right)
    ImageView app_right;


    private float amount;//Scan QR code passed over the amount
    private String address;
    private double smtBalance;
    private TokenVo tokenVo;

    private WalletSendPresenter walletSendPresenter;

    @Override
    protected void setContentView() {
        setContentView(R.layout.wallet_send_layout);
        getPassData();
    }

    private void getPassData() {
        tokenVo = (TokenVo) getIntent().getSerializableExtra("tokenVo");
        smtBalance = getIntent().getDoubleExtra("smtBalance",0);
        amount = getIntent().getFloatExtra("amount",0);
        address = getIntent().getStringExtra("address");
    }

    @Override
    protected void findViewById() {
        walletSendPresenter = new WalletSendPresenter(WalletSendActivity.this);
        walletSendPresenter.getPassData(tokenVo,smtBalance,amount,address);
        walletSendPresenter.initView(send,toValue,toAddress,fromName,fromAddress,gas,balance);
    }


    @Override
    protected void initData() {
        app_right.setVisibility(View.VISIBLE);
        app_right.setImageResource(R.drawable.scan_black);
        tokenVo = walletSendPresenter.initData();
        if (tokenVo == null){
            Utils.exitActivityAndBackAnim(WalletSendActivity.this,true);
            return;
        }
        setTitle(getString(R.string.send_blance_title,tokenVo.getTokenSymbol()));
        walletSendPresenter.getGasMethod(seekbar);
    }

    @Override
    protected void setListener() {
        seekbar.setOnSeekBarChangeListener(this);
    }

    /**
     * Advanced setting switch
     * 高级设置开关
     * */
    @OnCheckedChanged(R.id.sendSwitchButton)
    void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        walletSendPresenter.setCheckedChange(isChecked,walletHelp,customBody,seekbarBody,customGas);
        if (!isChecked){
            if (TextUtils.equals(tokenVo.getContactAddress(),Constants.SMT_CONTACT)){
                walletSendPresenter.setGasMethod(false,seekbar,true);
            }else{
                walletSendPresenter.setGasMethod(false,seekbar,false);
            }
        }
    }


    /**
     * Transfer amount limit
     * 转账金额限制方法
     * */
    @OnTextChanged(value = {R.id.toValue},callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void toValueAfterTextChanged(Editable s) {
        walletSendPresenter.setToValueLimit(s);
    }

    /**
     * set custom gas price
     * 设置自定义gas price
     * */
    @OnTextChanged(value = {R.id.customGasPrice},callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void gasPriceAfterTextChanged(Editable s) {
        walletSendPresenter.setCustomGasPrice(s,customGas,customGasNum);
    }

    /**
     * set custom gas
     * 设置自定义gas
     * */
    @OnTextChanged(value = {R.id.customGas},callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void afterTextChanged(Editable s) {
        walletSendPresenter.setCustomGas(s,customGasPrice,customGasNum);
    }

    /**
     * wallet value listener
     * 钱包转账金额监听
     * set custom gas price listener
     * 自定义gas price监听
     * */
    @OnFocusChange({R.id.toValue,R.id.customGasPrice})
    void onFocusChange(View v, boolean hasFocus){
        switch (v.getId()){
            case R.id.toValue:
                if (hasFocus){
                    balance.setVisibility(View.VISIBLE);
                }else{
                    balance.setVisibility(View.GONE);
                }
                break;
            case R.id.customGasPrice:
                if (!hasFocus){
                    walletSendPresenter.showGasPriceDialog(customGasPrice);
                }
                break;
        }
    }

    /**
     * 1.into scan qr code
     * 2.send transaction
     * 3.wallet help
     * 1.进入扫描二维码页面
     * 2.转账点击事件
     * 3.高级设置帮助按钮
     * */
    @OnClick({R.id.app_right,R.id.send,R.id.walletHelp,})
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.app_right:
                Intent i = new Intent(WalletSendActivity.this,CaptureActivity.class);
                i.putExtra("type",1);
                startActivityForResult(i,100);
                break;
            case R.id.send:
                walletSendPresenter.sendTrans(customGasPrice,switchButton.isChecked());
                break;
            case R.id.walletHelp:
                walletSendPresenter.setWalletHelp();
                break;
            default:
                super.onClick(v);
                break;
        }

    }


    /**
     * seek bar on progress changed
     * 滑块滑动事件
     * */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (TextUtils.equals(tokenVo.getContactAddress(),Constants.SMT_CONTACT)){//smt gas
            if(walletSendPresenter.gasChangeMethod(progress,true)){
                return;
            }
        }else{//token gas
            if(walletSendPresenter.gasChangeMethod(progress,false)){
                return;
            }
        }
        walletSendPresenter.setGasText();
    }



    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            seekbarBody.setVisibility(View.VISIBLE);
            seekbar.setVisibility(View.VISIBLE);
            helpBody.setVisibility(View.VISIBLE);
            customBody.setVisibility(View.GONE);
            gasTip.setVisibility(View.GONE);
            switchButton.setChecked(false);
            customGasPrice.setText("");
            customGas.setText("");
            address = data.getStringExtra("address");
            amount = data.getFloatExtra("amount",0);
            String tokenSymbol = walletSendPresenter.setOnActivityResult(address,amount,seekbar);
            setTitle(getString(R.string.send_blance_title,tokenSymbol));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        walletSendPresenter.onDestroy();
    }
}
