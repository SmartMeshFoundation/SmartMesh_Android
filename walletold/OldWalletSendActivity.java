package com.lingtuan.firefly.walletold;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.lingtuan.firefly.quickmark.CaptureActivity;
import com.lingtuan.firefly.ui.WebViewUI;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.MyViewDialogFragment;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.util.netutil.NetRequestUtils;
import com.lingtuan.firefly.wallet.util.Sign2;
import com.lingtuan.firefly.wallet.util.WalletStorage;
import com.lingtuan.firefly.wallet.vo.GasVo;
import com.lingtuan.firefly.wallet.vo.TransVo;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Hash;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.core.methods.request.RawTransaction;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


/**
 * send token
 * Created on 2017/8/22.
 */

public class OldWalletSendActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener, TextWatcher, CompoundButton.OnCheckedChangeListener {
    private final double FACTOR = 1000000000f;
    private final String contractFunctionHex = "0xa9059cbb";//
    private BigDecimal ONE_ETHER = new BigDecimal("1000000000000000000");//one eth
    private LinearLayout customBody,helpBody,seekbarBody;
    private EditText customGasPrice,customGas;
    private TextView customGasNum;
    private ImageView walletHelp;
    private SwitchButtonLine switchButton;
    private TextView fromName;
    private TextView fromAddress;
    private EditText toValue;
    private EditText toAddress;
    private TextView send;
    private SeekBar seekbar;
    private TextView gas;
    private TextView gasTip;
    private TextView balance;
    private ImageView app_right;
    private double currentGas;//gas
    private int currentLimit;//limit
    private int sendtype  ;// 0  ETH   1 SMT  2 MESH
    private float amount;//Scan QR code passed over the amount
    private String address;
    private int walletType;//0 ordinary wallets 1 observation wallet
    private double ethBalance,smtBalance,meshBalance;
    private GasVo ethVo,smtVo,meshVo;
    private boolean meshTransferLock;//mesh transfer is lock   true or false
    private double smtProxy;//smt proxy
    private double meshProxy;//mesh proxy
    @Override
    protected void setContentView() {
        setContentView(R.layout.old_wallet_send_layout);
    }

    @Override
    protected void findViewById() {
        fromName = (TextView) findViewById(R.id.fromName);
        fromAddress = (TextView) findViewById(R.id.fromAddress);
        toValue = (EditText) findViewById(R.id.toValue);
        toAddress = (EditText) findViewById(R.id.toAddress);
        send =  (TextView) findViewById(R.id.send);
        gas =  (TextView) findViewById(R.id.gas);
        gasTip =  (TextView) findViewById(R.id.gasTip);
        seekbar  =  (SeekBar) findViewById(R.id.seekbar);
        balance =  (TextView) findViewById(R.id.balance);
        app_right =  (ImageView) findViewById(R.id.app_right);
        customBody =  (LinearLayout) findViewById(R.id.customBody);
        helpBody =  (LinearLayout) findViewById(R.id.helpBody);
        seekbarBody =  (LinearLayout) findViewById(R.id.seekbarBody);
        customGasPrice =  (EditText) findViewById(R.id.customGasPrice);
        customGas =  (EditText) findViewById(R.id.customGas);
        customGasNum =  (TextView) findViewById(R.id.customGasNum);
        walletHelp =  (ImageView) findViewById(R.id.walletHelp);
        switchButton =  (SwitchButtonLine) findViewById(R.id.sendSwitchButton);
    }

    @Override
    protected void setListener() {
        send.setOnClickListener(this);
        walletHelp.setOnClickListener(this);
        seekbar.setOnSeekBarChangeListener(this);
        app_right.setOnClickListener(this);
        toValue.addTextChangedListener(this);
        switchButton.setOnCheckedChangeListener(this);
        changedListenerMethod();
    }

    private void changedListenerMethod(){
        toValue.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    balance.setVisibility(View.VISIBLE);
                }else{
                    balance.setVisibility(View.GONE);
                }
            }
        });

        customGasPrice.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){
                    String gasPrice = customGasPrice.getText().toString().trim();
                    if (!TextUtils.isEmpty(gasPrice)){
                        int tempPrice = Integer.parseInt(gasPrice);
                        if (tempPrice < 8){
                            showDialogSingleButton(getString(R.string.custom_gas_notice_1),null);
                        }else if (tempPrice > 100){
                            showDialogSingleButton(getString(R.string.custom_gas_notice),null);
                        }
                    }
                }
            }
        });

        customGasPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String gasPrice = s.toString();
                int gasPriceLength = gasPrice.length();
                if (gasPriceLength > 0){
                    if(gasPriceLength > 9 ){
                        s.delete(gasPriceLength -1, gasPriceLength);
                        return;
                    }
                    Long tempGasPrice = Long.parseLong(gasPrice);
                    String gasLimit = customGas.getText().toString().trim();
                    if (TextUtils.isEmpty(gasLimit)){
                        currentGas = 0;
                    }else{
                        currentLimit = Integer.parseInt(gasLimit);
                        long tempgas =  currentLimit * tempGasPrice;
                        BigDecimal b1 = new BigDecimal(tempgas);
                        BigDecimal b2 = new BigDecimal(FACTOR);
                        currentGas = b1.divide(b2,8, BigDecimal.ROUND_HALF_UP).doubleValue();
                    }
                }else{
                    long tempgas =  currentLimit * 20;
                    BigDecimal b1 = new BigDecimal(tempgas);
                    BigDecimal b2 = new BigDecimal(FACTOR);
                    currentGas = b1.divide(b2,8, BigDecimal.ROUND_HALF_UP).doubleValue();
                }
                BigDecimal b = new BigDecimal(currentGas);
                currentGas = b.setScale(8,BigDecimal.ROUND_HALF_UP).doubleValue();
                String showprogress = String.format("%f",currentGas);
                customGasNum.setText(getString(R.string.eth_er,showprogress));
            }
        });

        customGas.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String gasLimit = s.toString();
                int gasLimitLenght = gasLimit.length();
                if (gasLimitLenght > 0){
                    if(gasLimitLenght > 9 ){
                        s.delete(gasLimitLenght -1, gasLimitLenght);
                        return;
                    }
                    currentLimit = Integer.parseInt(gasLimit);
                    String gasPrice = customGasPrice.getText().toString().trim();
                    if (TextUtils.isEmpty(gasPrice)){
                        long tempgas =  currentLimit * 20;
                        BigDecimal b1 = new BigDecimal(tempgas);
                        BigDecimal b2 = new BigDecimal(FACTOR);
                        currentGas = b1.divide(b2,8, BigDecimal.ROUND_HALF_UP).doubleValue();
                    }else{
                        long tempgas =  currentLimit * Long.parseLong(gasPrice);
                        BigDecimal b1 = new BigDecimal(tempgas);
                        BigDecimal b2 = new BigDecimal(FACTOR);
                        currentGas = b1.divide(b2,8, BigDecimal.ROUND_HALF_UP).doubleValue();
                    }
                }else{
                    currentGas = 0;
                }
                BigDecimal b = new BigDecimal(currentGas);
                currentGas = b.setScale(8,BigDecimal.ROUND_HALF_UP).doubleValue();
                String showprogress = String.format("%f",currentGas);
                customGasNum.setText(getString(R.string.eth_er,showprogress));
            }
        });
    }

    @Override
    protected void initData() {
        sendtype  = getIntent().getIntExtra("sendtype",0);
        amount = getIntent().getFloatExtra("amount",0);
        address = getIntent().getStringExtra("address");

        if(!TextUtils.isEmpty(address)){
            toAddress.setText(address);
        }

        if(sendtype == 0){//ETH
            setTitle(getString(R.string.send_blance_title,getString(R.string.eth)));
        }else if (sendtype == 1){//SMT
            setTitle(getString(R.string.send_blance_title,getString(R.string.smt)));
        }else if (sendtype == 2){//MESH
            setTitle(getString(R.string.send_blance_title,getString(R.string.mesh)));
        }

        if(amount>0){
            toValue.setText(amount+"");
        }

        app_right.setVisibility(View.VISIBLE);
        app_right.setImageResource(R.drawable.scan_black);
        for(int i=0;i<WalletStorage.getInstance(getApplicationContext()).get().size();i++){
            if(WalletStorage.getInstance(getApplicationContext()).get().get(i).isSelect()){
                fromName.setText(WalletStorage.getInstance(getApplicationContext()).get().get(i).getWalletName());
                String address = WalletStorage.getInstance(getApplicationContext()).get().get(i).getPublicKey();
                walletType = WalletStorage.getInstance(getApplicationContext()).get().get(i).getWalletType();

                if(!TextUtils.isEmpty(address) && !address.startsWith("0x")) {
                    address= "0x"+ address;
                }
                fromAddress.setText(address);

                ethBalance = WalletStorage.getInstance(getApplicationContext()).get().get(i).getEthBalance();
                smtBalance = WalletStorage.getInstance(getApplicationContext()).get().get(i).getFftBalance();
                meshBalance = WalletStorage.getInstance(getApplicationContext()).get().get(i).getMeshBalance();
                setBalanceMethod();
                break;
            }
        }

        getGasMethod();

        if (walletType == 1){
            send.setEnabled(false);
            send.setText(getString(R.string.wallet_scan_cant_trans));
        }

        if (!TextUtils.isEmpty(toAddress.getText().toString())){
            toValue.setFocusable(true);
            toValue.setFocusableInTouchMode(true);
            toValue.requestFocus();
        }
    }

    /**
     * set balance
     * */
    private void setBalanceMethod(){
        balance.setVisibility(View.GONE);
        if (sendtype == 0){
            BigDecimal ethDecimal = new BigDecimal(ethBalance).setScale(10,BigDecimal.ROUND_CEILING);
            balance.setText(getString(R.string.balance_er,ethDecimal.toPlainString()));
        }else if (sendtype == 1){
            BigDecimal fftDecimal = new BigDecimal(smtBalance).setScale(5,BigDecimal.ROUND_CEILING);
            balance.setText(getString(R.string.balance_er,fftDecimal.toPlainString()));
        }else if (sendtype == 2){
            BigDecimal meshDecimal = new BigDecimal(meshBalance).setScale(5,BigDecimal.ROUND_CEILING);
            balance.setText(getString(R.string.balance_er,meshDecimal.toPlainString()));
        }
    }

    /**
     * get gas
     * */
    private void getGasMethod() {
        LoadingDialog.show(OldWalletSendActivity.this,null);
        try {
            NetRequestUtils.getInstance().getGas(OldWalletSendActivity.this,fromAddress.getText().toString(),new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    mHandler.sendEmptyMessage(4);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    try {
                        String jsonString = response.body().string();
                        JSONObject object = new JSONObject(jsonString);
                        int errcod = object.optInt("errcod");
                        if (errcod == 0){
                            JSONObject obj = object.optJSONObject("data");
                            JSONObject ethObj = obj.optJSONObject("eth");
                            JSONObject fftObj = obj.optJSONObject("smt");
                            JSONObject meshObj = obj.optJSONObject("mesh");
                            smtProxy = obj.optDouble("smt_proxy");
                            meshProxy = obj.optDouble("mesh_proxy");
                            meshTransferLock = obj.optInt("mesh_transfer_lock",0) == 1;
                            ethVo = new GasVo().parse(ethObj);//eth  gas
                            smtVo = new GasVo().parse(fftObj);//smt gas
                            meshVo = new GasVo().parse(meshObj);//mesh gas
                            if ((sendtype == 0 && ethVo == null) || (sendtype == 1 && smtVo == null) || (sendtype == 2 && meshVo == null)){
                                mHandler.sendEmptyMessage(4);
                            }else{
                                mHandler.sendEmptyMessage(5);
                                if (meshTransferLock && sendtype == 2){
                                    mHandler.sendEmptyMessage(6);
                                }
                            }
                        }else{
                            if(errcod == -2){
                                long difftime = object.optJSONObject("data").optLong("difftime");
                                long tempTime =  MySharedPrefs.readLong(OldWalletSendActivity.this,MySharedPrefs.FILE_APPLICATION,MySharedPrefs.KEY_REQTIME);
                                MySharedPrefs.writeLong(OldWalletSendActivity.this,MySharedPrefs.FILE_APPLICATION,MySharedPrefs.KEY_REQTIME,difftime + tempTime);
                            }
                            mHandler.sendEmptyMessage(4);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.app_right:
                Intent i = new Intent(OldWalletSendActivity.this,CaptureActivity.class);
                i.putExtra("type",1);
                startActivityForResult(i,100);
                break;
            case R.id.send:
                sendtrans();
                break;
            case R.id.walletHelp:
                String result = "";
                String language = MySharedPrefs.readString(OldWalletSendActivity.this,MySharedPrefs.FILE_APPLICATION,MySharedPrefs.KEY_LANGUAFE);
                if (TextUtils.isEmpty(language)){
                    Locale locale = new Locale(Locale.getDefault().getLanguage());
                    if (TextUtils.equals(locale.getLanguage(),"zh")){
                        result = Constants.TRANSFER_MODE_ZH;
                    }else{
                        result = Constants.TRANSFER_MODE_EN;
                    }
                }else{
                    if (TextUtils.equals(language,"zh")){
                        result = Constants.TRANSFER_MODE_ZH;
                    }else{
                        result = Constants.TRANSFER_MODE_EN;
                    }
                }
                Intent intent = new Intent(OldWalletSendActivity.this, WebViewUI.class);
                intent.putExtra("loadUrl", result);
                intent.putExtra("title", getString(R.string.description));
                startActivity(intent);
                Utils.openNewActivityAnim(OldWalletSendActivity.this,false);
                break;
            default:
                super.onClick(v);
                break;
        }

    }


    //send trans
    private void  sendtrans(){
        String address = toAddress.getText().toString();
        if(TextUtils.isEmpty(address)){
            showToast(getString(R.string.empty_address));
            return;
        }else if(!address.startsWith("0x") ||  address.length()!=42){
            showToast(getString(R.string.error_address));
            return;
        }

        if (TextUtils.isEmpty(toValue.getText().toString())){
            showToast(getString(R.string.money_empty));
            return;
        }

        if(sendtype == 0){//send ETH
            double total = Double.valueOf(toValue.getText().toString()) + currentGas;
            if(total > ethBalance){
                showToast(getString(R.string.balance_not_enough_token,getString(R.string.eth)));
                return;
            }
        }else{//send SMT or MESH
            if (checkBalanceMethod()){
                return;
            }
        }

        if (currentLimit <= 0){
            return;
        }

        MyViewDialogFragment mdf = new MyViewDialogFragment(MyViewDialogFragment.DIALOG_INPUT_PWD, new MyViewDialogFragment.EditCallback() {
            @Override
            public void getEditText(String editText) {
                getWalletPrivateKey(editText);
            }
        });
        mdf.show(this.getSupportFragmentManager(), "mdf");
    }

    /**
     * Check if the balance is sufficient
     * @return true  the amount is not enough
     * */
    private boolean checkBalanceMethod(){
        if(currentGas > ethBalance){
            showToast(getString(R.string.balance_not_enough_token,getString(R.string.eth)));
            return true;
        }
        double tempValue = Double.valueOf(toValue.getText().toString());
        if(sendtype == 1 && tempValue > smtBalance){
            showToast(getString(R.string.balance_not_enough_smt));
            return true;
        }else if (sendtype == 2 && tempValue > meshBalance){
            showToast(getString(R.string.balance_not_enough_token,getString(R.string.mesh)));
            return true;
        }
        return false;
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (sendtype == 0){//eth gas
            if(gasChangeMethod(progress,ethVo)){
                return;
            }
        }else if(sendtype == 1){//smt gas
            if(gasChangeMethod(progress,smtVo)){
                return;
            }
        }else if (sendtype == 2){//mesh gas
            if(gasChangeMethod(progress,meshVo)){
                return;
            }
        }
        BigDecimal b = new BigDecimal(currentGas);
        currentGas = b.setScale(6,BigDecimal.ROUND_HALF_UP).doubleValue();
        String showprogress = String.format("%f",currentGas);
        gas.setText(getString(R.string.eth_er,showprogress));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    /**
     * change gas limit and gas price
     * @param gasVo gas bean
     * @param progress progress
     * */
    private boolean gasChangeMethod(int progress,GasVo gasVo){
        int realprogress = 0;
        if (gasVo == null){
            return true;
        }
        realprogress = progress + gasVo.getMinPrice();
        BigDecimal b1 = new BigDecimal(Double.toString(realprogress - gasVo.getMinPrice()));
        BigDecimal b2 = new BigDecimal(Double.toString(gasVo.getMaxPrice() - gasVo.getMinPrice()));
        double percent = b1.divide(b2,6, BigDecimal.ROUND_HALF_UP).doubleValue();
        currentLimit = (int)(percent * (gasVo.getMaxLimit() - gasVo.getMinLimit()) + gasVo.getMinLimit());
        currentGas = realprogress * currentLimit/FACTOR;
        return false;
    }

    /**
     * Get private key
     * @param walletPwd wallet password
     * */
    private void getWalletPrivateKey(final String walletPwd){
        LoadingDialog.show(OldWalletSendActivity.this,"");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Credentials keys = WalletStorage.getInstance(getApplicationContext()).getFullWallet(OldWalletSendActivity.this,walletPwd,fromAddress.getText().toString());
                    Message message = Message.obtain();
                    message.what = 0;
                    message.obj = keys;
                    mHandler.sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (CipherException e) {
                    e.printStackTrace();
                    mHandler.sendEmptyMessage(1);
                }
            }
        }).start();
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 0://Get the success of the next step
                    Credentials keys = (Credentials)msg.obj;
                    if (sendtype == 0){//send eth
                        sendTransEth(keys);
                    }else{//send SMT or MESH
                        sendTransToken(keys);
                    }
                    break;
                case 1://Enter the wrong password
                    LoadingDialog.close();
                    showToast(getString(R.string.wallet_copy_pwd_error));
                    break;
                case 2://Transfer successful
                    LoadingDialog.close();
                    Bundle bundle = msg.getData();
                    String txurl = bundle.getString("txurl");
                    String tx = bundle.getString("tx");
                    TransVo transVo = new TransVo();
                    transVo.setTx(tx);
                    transVo.setTxurl(txurl);
                    transVo.setState(-1);
                    transVo.setFee(String.format("%f",currentGas));
                    transVo.setToAddress(toAddress.getText().toString());
                    transVo.setFromAddress(fromAddress.getText().toString());
                    transVo.setValue(toValue.getText().toString());
                    transVo.setTime(System.currentTimeMillis()/1000);
                    transVo.setType(sendtype);

                    Intent intent = new Intent(OldWalletSendActivity.this,OldTransactionDetailActivity.class);
                    intent.putExtra("transVo",transVo);
                    intent.putExtra("isSendTrans",true);
                    startActivity(intent);
                    finish();
                    break;
                case 3://Request failed
                    LoadingDialog.close();
                    String errMsg = (String)msg.obj;
                    showToast(errMsg);
                    break;
                case 4://Failed to get gas
                    LoadingDialog.close();
                    showToast(getString(R.string.get_gas_error));
                    finish();
                    break;
                case 5://eth gas
                    LoadingDialog.close();
                    if (sendtype == 0){//eth gas
                        seekbar.setMax(ethVo.getMaxPrice() - ethVo.getMinPrice());
                        if (ethVo.getDefaultPrice() - ethVo.getMinPrice() > 0){
                            seekbar.setProgress(ethVo.getDefaultPrice() - ethVo.getMinPrice());
                        }
                    }else if (sendtype == 1){//smt gas
                        seekbar.setMax(smtVo.getMaxPrice() - smtVo.getMinPrice());
                        if (smtVo.getDefaultPrice() - smtVo.getMinPrice() > 0){
                            seekbar.setProgress(smtVo.getDefaultPrice() - smtVo.getMinPrice());
                        }
                    }else if (sendtype == 2){//mesh gas
                        if (meshVo != null){
                            seekbar.setMax(meshVo.getMaxPrice() - meshVo.getMinPrice());
                            if (meshVo.getDefaultPrice() - meshVo.getMinPrice() > 0){
                                seekbar.setProgress(meshVo.getDefaultPrice() - meshVo.getMinPrice());
                            }
                        }
                    }
                    break;
                case 6://mesh transfer lock
                    LoadingDialog.close();
                    showDialogSingleButton(getString(R.string.custom_gas_mesh_notice),null);
                    break;
            }
        }
    };

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        String amountCash = s.toString();
        int length = amountCash.length();
        if(length >= 8 ){
            String s2 = amountCash.substring(length - 8, length -7) ;
            if(TextUtils.equals(".", s2)){
                s.delete(length-1, length);
            }
        }
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
            setBalanceMethod();
            if(sendtype == 0){//eth
                setTitle(getString(R.string.send_blance_title,getString(R.string.eth)));
                setGasMethod(ethVo);
            }else if (sendtype == 1){//smt
                setTitle(getString(R.string.send_blance_title,getString(R.string.smt)));
                setGasMethod(smtVo);
            }else if (sendtype == 2){//mesh
                setTitle(getString(R.string.send_blance_title,getString(R.string.mesh)));
                setGasMethod(meshVo);
                if (meshTransferLock){
                    mHandler.sendEmptyMessage(6);
                }
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * get gas method
     * @param gasVo gasVo
     * */
    private void setGasMethod(GasVo gasVo){
        if (gasVo == null){
            return;
        }
        seekbar.setMax(gasVo.getMaxPrice() - gasVo.getMinPrice());
        seekbar.setProgress(gasVo.getDefaultPrice() - gasVo.getMinPrice());
        BigDecimal b1 = new BigDecimal(Double.toString(gasVo.getDefaultPrice() - gasVo.getMinPrice()));
        BigDecimal b2 = new BigDecimal(Double.toString(gasVo.getMaxPrice() - gasVo.getMinPrice()));
        double percent = b1.divide(b2,6, BigDecimal.ROUND_HALF_UP).doubleValue();
        currentLimit = (int)(percent* (gasVo.getMaxLimit() - gasVo.getMinLimit()) + gasVo.getMinLimit());
        currentGas = (seekbar.getProgress() + gasVo.getMinPrice()) * currentLimit/FACTOR;

        BigDecimal b = new BigDecimal(currentGas);
        currentGas = b.setScale(6,BigDecimal.ROUND_HALF_UP).doubleValue();
        String showprogress = String.format("%f",currentGas);
        gas.setText(getString(R.string.eth_er,showprogress));
        if(amount>0){
            toValue.setText(amount+"");
        }

        if(!TextUtils.isEmpty(address)){
            toAddress.setText(address);
        }else{
            toAddress.setText("");
        }
        if (!TextUtils.isEmpty(toAddress.getText().toString())){
            toValue.setFocusable(true);
            toValue.setFocusableInTouchMode(true);
            toValue.requestFocus();
            balance.setVisibility(View.VISIBLE);
        }else{
            toAddress.setFocusable(true);
            toAddress.setFocusableInTouchMode(true);
            toAddress.requestFocus();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked){
            walletHelp.setVisibility(View.VISIBLE);
            customBody.setVisibility(View.VISIBLE);
            seekbarBody.setVisibility(View.GONE);
            Animation animation = AnimationUtils.loadAnimation(OldWalletSendActivity.this,R.anim.push_middle_to_left_out);
            Animation inFromRight = AnimationUtils.loadAnimation(OldWalletSendActivity.this,R.anim.push_right_to_middle_in);
            seekbarBody.setAnimation(animation);
            customBody.setAnimation(inFromRight);
        }else{
            walletHelp.setVisibility(View.GONE);
            seekbarBody.setVisibility(View.VISIBLE);
            customBody.setVisibility(View.GONE);
            Animation animation = AnimationUtils.loadAnimation(OldWalletSendActivity.this,R.anim.push_middle_to_right_out);
            Animation inFromLeft = AnimationUtils.loadAnimation(OldWalletSendActivity.this,R.anim.push_left_to_middle_in);
            customBody.setAnimation(animation);
            seekbarBody.setAnimation(inFromLeft);
            if (sendtype == 0){
                setGasMethod(ethVo);
            }else if (sendtype == 1){
                setGasMethod(smtVo);
            }else if (sendtype == 2){
                setGasMethod(meshVo);
            }
        }
    }


    /**
     * send SMT
     * @param keys Credentials
     * */
    private void sendTransToken(final Credentials keys) {
        try {
            NetRequestUtils.getInstance().getEthNonce(OldWalletSendActivity.this,fromAddress.getText().toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Message message = Message.obtain();
                    message.obj = "Error";
                    message.what = 3;
                    mHandler.sendMessage(message);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String body = response.body().string();
                        JSONObject object = new JSONObject(body);
                        int errcod = object.optInt("errcod");
                        String msg = object.optString("msg");
                        if (errcod != 0){//error
                            Message message = Message.obtain();
                            message.obj = msg;
                            message.what = 3;
                            mHandler.sendMessage(message);
                        }else{
                            String valueOld =  new BigDecimal(toValue.getText().toString()).multiply(ONE_ETHER).setScale(0,BigDecimal.ROUND_DOWN).toPlainString();//multiply on ether
                            String valueHex  =  new BigDecimal(valueOld).toBigInteger().toString(16);//Into hexadecimal
                            for(int i=0;i<64;i++){
                                if(valueHex.length()>=64){
                                    break;
                                }else{
                                    valueHex = "0"+ valueHex;
                                }
                            }
                            String toHex  = toAddress.getText().toString().substring(2);
                            for(int i=0;i<64;i++){
                                if(toHex.length()>=64){
                                    break;
                                }else{
                                    toHex = "0"+ toHex;
                                }
                            }
                            //toHex missing 64-bit valueHex padded 64-bit
                            String data = contractFunctionHex + toHex + valueHex;
                            BigInteger nonce = new BigInteger(object.optJSONObject("data").optString("nonce"), 10);
                            String address = sendtype == 1 ? Constants.CONTACT_ADDRESS : Constants.CONTACT_MESH_ADDRESS;
                            String gasPrice = new BigDecimal(currentGas+"").multiply(ONE_ETHER).divide(new BigDecimal(currentLimit),0,BigDecimal.ROUND_DOWN).toPlainString();
                            RawTransaction tx = RawTransaction.createTransaction(
                                    nonce,
                                    new BigInteger(gasPrice),//gasPrice
                                    new BigInteger(currentLimit+""),
                                    address,
                                    new BigDecimal(0).toBigInteger(),
                                    data
                            );
                            byte[] signed = TransactionEncoder.signMessage(tx,keys);
                            String hexValue = Hex.toHexString(signed);
                            NetRequestUtils.getInstance().sendRawTransaction(OldWalletSendActivity.this,"0x" +hexValue, new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    Message message = Message.obtain();
                                    message.obj = "Error";
                                    message.what = 3;
                                    mHandler.sendMessage(message);
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    try {
                                        String body = response.body().string();
                                        JSONObject object = new JSONObject(body);
                                        int errcod = object.optInt("errcod");
                                        String msg = object.optString("msg");
                                        String txurl = object.optJSONObject("data").optString("txurl");
                                        String tx = object.optJSONObject("data").optString("tx");
                                        if (errcod != 0){//error
                                            Message message = Message.obtain();
                                            message.obj = msg;
                                            message.what = 3;
                                            mHandler.sendMessage(message);
                                        }else{
                                            Message message = Message.obtain();
                                            Bundle bundle = new Bundle();
                                            bundle.putString("tx",tx);
                                            bundle.putString("txurl",txurl);
                                            message.setData(bundle);
                                            message.what = 2;
                                            mHandler.sendMessage(message);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * send eth
     * @param keys Credentials
     * */
    private void sendTransEth(final Credentials keys) {
        try {
            NetRequestUtils.getInstance().getEthNonce(OldWalletSendActivity.this,fromAddress.getText().toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Message message = Message.obtain();
                    message.obj = "Error";
                    message.what = 3;
                    mHandler.sendMessage(message);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String body = response.body().string();
                        JSONObject object = new JSONObject(body);
                        int errcod = object.optInt("errcod");
                        String msg = object.optString("msg");
                        if (errcod != 0){//error
                            Message message = Message.obtain();
                            message.obj = msg;
                            message.what = 3;
                            mHandler.sendMessage(message);
                        }else{
                            BigInteger nonce = new BigInteger(object.optJSONObject("data").optString("nonce"), 10);
                            String address = toAddress.getText().toString();
                            String gasPrice = new BigDecimal(currentGas+"").multiply(ONE_ETHER).divide(new BigDecimal(currentLimit),0,BigDecimal.ROUND_DOWN).toPlainString();
                            RawTransaction tx = RawTransaction.createTransaction(
                                    nonce,
                                    new BigInteger(gasPrice),//gasPrice
                                    new BigInteger(currentLimit+""),
                                    address,
                                    new BigDecimal(toValue.getText().toString()).multiply(ONE_ETHER).toBigInteger(),
                                    ""
                            );
                            byte[] signed = TransactionEncoder.signMessage(tx,keys);
                            String hexValue = Hex.toHexString(signed);
                            NetRequestUtils.getInstance().sendRawTransaction(OldWalletSendActivity.this,"0x" +hexValue, new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    Message message = Message.obtain();
                                    message.obj = "Error";
                                    message.what = 3;
                                    mHandler.sendMessage(message);
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    try {
                                        String body = response.body().string();
                                        JSONObject object = new JSONObject(body);
                                        int errcod = object.optInt("errcod");
                                        String msg = object.optString("msg");
                                        String txurl = object.optJSONObject("data").optString("txurl");
                                        String tx = object.optJSONObject("data").optString("tx");
                                        if (errcod != 0){//error
                                            Message message = Message.obtain();
                                            message.obj = msg;
                                            message.what = 3;
                                            mHandler.sendMessage(message);
                                        }else{
                                            Message message = Message.obtain();
                                            Bundle bundle = new Bundle();
                                            bundle.putString("tx",tx);
                                            bundle.putString("txurl",txurl);
                                            message.setData(bundle);
                                            message.what = 2;
                                            mHandler.sendMessage(message);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * send trans token with proxy
     * */
    private void sendTransTokenWithProxy(final Credentials keys){
        final String valueOld =  new BigDecimal(toValue.getText().toString()).multiply(ONE_ETHER).setScale(0,BigDecimal.ROUND_DOWN).toPlainString();
        final String feeSmtOld = new BigDecimal(smtProxy).multiply(ONE_ETHER).setScale(0,BigDecimal.ROUND_DOWN).toPlainString();
        final String from = fromAddress.getText().toString();
        final String to = toAddress.getText().toString();
        String value  =  new BigDecimal(valueOld).toBigInteger().toString(16);
        for(int i=0;i<64;i++){
            if(value.length()>=64){
                break;
            }else{
                value = "0"+ value;
            }
        }
        final String newvalue = value;
        String feeSmt =  new BigDecimal(feeSmtOld).toBigInteger().toString(16);
        for(int i=0;i<64;i++){
            if(feeSmt.length()>=64){
                break;
            }else{
                feeSmt = "0"+ feeSmt;
            }
        }
        final String newfeeFft = feeSmt;
        try {
            NetRequestUtils.getInstance().getNonce(OldWalletSendActivity.this,fromAddress.getText().toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Message message = Message.obtain();
                    message.obj = "Error";
                    message.what = 3;
                    mHandler.sendMessage(message);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String body = response.body().string();
                        JSONObject object = new JSONObject(body);
                        int errcod = object.optInt("errcod");
                        String msg = object.optString("msg");
                        if (errcod != 0){//error
                            Message message = Message.obtain();
                            message.obj = msg;
                            message.what = 3;
                            mHandler.sendMessage(message);
                        }else{

                            String nonce = new BigDecimal(object.optJSONObject("data").optString("nonce")).toBigInteger().toString(16);
                            for(int i=0;i<64;i++){
                                if(nonce.length()>=64){
                                    break;
                                }else{
                                    nonce = "0"+ nonce;
                                }
                            }
                            //mesh to hex 4d657368426f78   smt to hex 21f15966e07a10554c364b988e91dab01d32794a
                            String smtHex = Constants.GLOBAL_SWITCH_OPEN ? "21f15966e07a10554c364b988e91dab01d32794a" : "b1f2464fc8564533a114a879fb1348fc095381b8";
                            String message = from.substring(2) + to.substring(2) + newvalue + newfeeFft + nonce + smtHex;
                            byte[] srtbyte = Hash.sha3(Numeric.hexStringToByteArray(message));
                            Sign2.SignatureData data = Sign2.signMessage(srtbyte,keys.getEcKeyPair());
                            String R = "0x" + Hex.toHexString(data.getR());
                            String S = "0x" + Hex.toHexString(data.getS());
                            byte V = data.getV();
                            NetRequestUtils.getInstance().sendTransferProxy(OldWalletSendActivity.this,from,to,valueOld,feeSmtOld,R,S,V, new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    Message message = Message.obtain();
                                    message.obj = "Error";
                                    message.what = 3;
                                    mHandler.sendMessage(message);
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    try {
                                        String body = response.body().string();
                                        JSONObject object = new JSONObject(body);
                                        int errcod = object.optInt("errcod");
                                        String msg = object.optString("msg");
                                        String txurl = object.optJSONObject("data").optString("txurl");
                                        String tx = object.optJSONObject("data").optString("tx");
                                        if (errcod != 0){//error
                                            Message message = Message.obtain();
                                            message.obj = msg;
                                            message.what = 3;
                                            mHandler.sendMessage(message);
                                        }else{
                                            Message message = Message.obtain();
                                            Bundle bundle = new Bundle();
                                            bundle.putString("tx",tx);
                                            bundle.putString("txurl",txurl);
                                            message.setData(bundle);
                                            message.what = 2;
                                            mHandler.sendMessage(message);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * show dialog single button
     * @param title  title
     * @param content content
     * */
    private void showDialogSingleButton(String title,String content){
        MyViewDialogFragment mdf = new MyViewDialogFragment(MyViewDialogFragment.DIALOG_SINGLE_BUTTON);
        mdf.setTitleAndContentText(title, content);
        mdf.show(getSupportFragmentManager(), "mdf");
    }
}
