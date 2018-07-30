package com.lingtuan.firefly.wallet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.custom.SwitchButtonLine;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.ui.WebViewUI;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.MyToast;
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

public class WalletSendPresenter {

    /**
     * start transaction
     * 开始转账
     * */
    private TextView send;

    /**
     *  show to value
     *  显示当前转账金额
     * */
    private EditText toValue;
    /**
     * show to address
     * 显示当前转入地址
     * */
    private EditText toAddress;

    /**
     * show from name
     * 显示当前账户名
     * */
    private TextView fromName;

    /**
     * show from address
     * 显示当前转账地址
     * */
    private TextView fromAddress;
    /**
     * show current gas
     * 显示当前gas
     * */
    private TextView gas;
    /**
     * show current wallet balance
     * 显示当前账户余额
     * */
    private TextView balance;


    private Context context;
    private final double FACTOR = 1000000000f;//1 Gwei
    private final String contractFunctionHex = "0xa9059cbb";

    /**
     * on eth gwei
     * 一个eth单位
     * */
    private BigDecimal ONE_ETHER = new BigDecimal("1000000000000000000");//one eth

    /**
     * wallet type
     * 0 ordinary wallets 1 observation wallet
     * */
    private int walletType;

    /**
     * current gas
     * 当前转账所需gas
     * */
    private double currentGas;

    /**
     * current gas limit
     * */
    private int currentLimit;

    /**
     * transaction value
     * 转账金额
     * */
    private float amount;

    /**
     * to address
     * 转账目标地址
     * */
    private String address;//to address  目标地址

    /**
     * wallet smt balance
     * 当前钱包SMT余额
     * */
    private double smtBalance;

    /**
     * 1.smt gas bean
     * 2.token gas bean
     * 1.SMT gas 内容
     * 2.Token gas 内容
     * */
    private GasVo smtGasVo,tokenGasVo;

    /**
     * mesh transfer is lock   true or false
     * MESH是否锁定
     * */
    private boolean spectrumHint;

    /**
     * wallet token bean
     * 钱包某一token对象
     * */
    private TokenVo tokenVo;

    public WalletSendPresenter(Context context){
        this.context = context;
    }

    public void initView(TextView send,EditText toValue,EditText toAddress,TextView fromName,TextView fromAddress, TextView gas, TextView balance){
        this.send = send;
        this.toValue = toValue;
        this.toAddress = toAddress;
        this.fromName = fromName;
        this.fromAddress = fromAddress;
        this.gas = gas;
        this.balance = balance;
    }

    public void getPassData(TokenVo tokenVo,double smtBalance , float amount,String address){
        this.tokenVo = tokenVo;
        this.smtBalance = smtBalance;
        this.amount = amount;
        this.address = address;
    }

    public TokenVo initData(){

        if(!TextUtils.isEmpty(address)){
            toAddress.setText(address);
        }

        if (tokenVo == null){
            ArrayList<TokenVo> tokenVos = FinalUserDataBase.getInstance().getOpenTokenList(fromAddress.getText().toString());
            if (tokenVos != null){
                for (int i = 0 ; i < tokenVos.size(); i++){
                    if (TextUtils.equals(tokenVos.get(i).getContactAddress(),Constants.SMT_CONTACT)){
                        tokenVo = tokenVos.get(i);
                        smtBalance = tokenVos.get(i).getTokenBalance();
                        break;
                    }
                }
            }
        }

        if (tokenVo != null){
            for(int i = 0; i< WalletStorage.getInstance(context.getApplicationContext()).get().size(); i++){
                if(WalletStorage.getInstance(context.getApplicationContext()).get().get(i).isSelect()){
                    fromName.setText(WalletStorage.getInstance(context.getApplicationContext()).get().get(i).getWalletName());
                    String address = WalletStorage.getInstance(context.getApplicationContext()).get().get(i).getPublicKey();
                    walletType = WalletStorage.getInstance(context.getApplicationContext()).get().get(i).getWalletType();
                    if(!TextUtils.isEmpty(address) && !address.startsWith("0x")) {
                        address= "0x"+ address;
                    }
                    fromAddress.setText(address);
                    break;
                }
            }

            if (walletType == 1){
                send.setEnabled(false);
                send.setText(context.getString(R.string.wallet_scan_cant_trans));
            }

            if(amount>0){
                toValue.setText(amount+"");
            }

            if (!TextUtils.isEmpty(toAddress.getText().toString())){
                toValue.setFocusable(true);
                toValue.setFocusableInTouchMode(true);
                toValue.requestFocus();
            }

            balance.setVisibility(View.GONE);
            if (tokenVo.getTokenBalance() > 0){
                BigDecimal decimal = new BigDecimal(tokenVo.getTokenBalance()).setScale(6,BigDecimal.ROUND_DOWN);
                balance.setText(context.getString(R.string.balance_er,decimal.toPlainString()));
            }else{
                balance.setText(context.getString(R.string.balance_er,tokenVo.getTokenBalance() + ""));
            }
        }
        return tokenVo;
    }

    /**
     * Advanced setting switch
     * 高级设置开关
     * @param isChecked          is checked advanced                  是否选中高级设置
     * @param walletHelp         wallet help                          钱包帮助图标
     * @param customBody         advanced custom body                 高级设置自定义布局
     * @param seekbarBody        seek bar body                        滑动组件布局
     * */
    public void setCheckedChange(boolean isChecked, ImageView walletHelp, LinearLayout customBody, LinearLayout seekbarBody,EditText customGas){
        if (isChecked){
            customGas.setText("");
            walletHelp.setVisibility(View.VISIBLE);
            customBody.setVisibility(View.VISIBLE);
            seekbarBody.setVisibility(View.GONE);
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.push_middle_to_left_out);
            Animation inFromRight = AnimationUtils.loadAnimation(context,R.anim.push_right_to_middle_in);
            seekbarBody.setAnimation(animation);
            customBody.setAnimation(inFromRight);
        }else{
            walletHelp.setVisibility(View.GONE);
            seekbarBody.setVisibility(View.VISIBLE);
            customBody.setVisibility(View.GONE);
            Animation animation = AnimationUtils.loadAnimation(context,R.anim.push_middle_to_right_out);
            Animation inFromLeft = AnimationUtils.loadAnimation(context,R.anim.push_left_to_middle_in);
            customBody.setAnimation(animation);
            seekbarBody.setAnimation(inFromLeft);
        }
    }

    public void setGasMethod(boolean needUpdateAddress ,SeekBar seekbar,boolean isSendSmt){
        GasVo tempGasVo;
        if (isSendSmt){
            tempGasVo = smtGasVo;
        }else{
            tempGasVo = tokenGasVo;
        }
        if (tempGasVo == null){
            return;
        }
        seekbar.setMax(tempGasVo.getMaxPrice() - tempGasVo.getMinPrice());
        seekbar.setProgress(tempGasVo.getDefaultPrice() - tempGasVo.getMinPrice());
        BigDecimal b1 = new BigDecimal(Double.toString(tempGasVo.getDefaultPrice() - tempGasVo.getMinPrice()));
        BigDecimal b2 = new BigDecimal(Double.toString(tempGasVo.getMaxPrice() - tempGasVo.getMinPrice()));
        double percent = b1.divide(b2,6, BigDecimal.ROUND_HALF_UP).doubleValue();
        currentLimit = (int)(percent* (tempGasVo.getMaxLimit() - tempGasVo.getMinLimit()) + tempGasVo.getMinLimit());
        currentGas = (seekbar.getProgress() + tempGasVo.getMinPrice()) * currentLimit/FACTOR;
        BigDecimal b = new BigDecimal(currentGas);
        currentGas = b.setScale(6,BigDecimal.ROUND_HALF_UP).doubleValue();
        setGasView(needUpdateAddress);
    }


    /**
     * get gas method
     * @param needUpdateAddress             is need update address                 是否需要更新地址
     * */
    public void setGasView(boolean needUpdateAddress){
        setGasText();
        if(amount>0){
            toValue.setText(amount+"");
        }

        if (needUpdateAddress){
            if(!TextUtils.isEmpty(address)){
                toAddress.setText(address);
            }else{
                toAddress.setText("");
            }
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

    public void setGasText(){
        BigDecimal b = new BigDecimal(currentGas);
        currentGas = b.setScale(6,BigDecimal.ROUND_HALF_UP).doubleValue();
        String showprogress = String.format("%f",currentGas);
        gas.setText(context.getString(R.string.smt_er,showprogress));
    }

    /**
     * Transfer amount limit
     * 转账金额限制方法
     * */
    public void  setToValueLimit(Editable s){
        String amountCash = s.toString();
        int length = amountCash.length();
        if(length >= 8 ){
            String s2 = amountCash.substring(length - 8, length -7) ;
            if(TextUtils.equals(".", s2)){
                s.delete(length-1, length);
            }
        }
    }

    public void setCustomGasPrice(Editable s,EditText customGas,TextView customGasNum){
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
        setCurrentGas(customGasNum);
    }

    public void setCustomGas(Editable s,EditText customGasPrice,TextView customGasNum){
        String gasLimit = s.toString();
        int gasLimitLenght = gasLimit.length();
        if (gasLimitLenght > 0){
            if(gasLimitLenght > 9 ){
                s.delete(gasLimitLenght -1, gasLimitLenght);
                return;
            }
            currentLimit = Integer.parseInt(gasLimit);
            String cGasPrice = customGasPrice.getText().toString().trim();
            long tempgas = currentLimit * 20;
            if (!TextUtils.isEmpty(cGasPrice)){
                tempgas =  currentLimit * Long.parseLong(cGasPrice);
            }
            BigDecimal b1 = new BigDecimal(tempgas);
            BigDecimal b2 = new BigDecimal(FACTOR);
            currentGas = b1.divide(b2,8, BigDecimal.ROUND_HALF_UP).doubleValue();
        }else{
            currentGas = 0;
        }
        setCurrentGas(customGasNum);
    }

    /**
     * set current gas
     * 设置当前gas
     * */
    public void setCurrentGas(TextView customGasNum){
        BigDecimal bigDecimal = new BigDecimal(currentGas);
        currentGas = bigDecimal.setScale(8,BigDecimal.ROUND_HALF_UP).doubleValue();
        String showProgress = String.format("%f",currentGas);
        customGasNum.setText(context.getString(R.string.smt_er,showProgress));
    }

    public void showGasPriceDialog(EditText customGasPrice){
        String gasPrice = customGasPrice.getText().toString().trim();
        if (!TextUtils.isEmpty(gasPrice)){
            int tempPrice = Integer.parseInt(gasPrice);
            if (tempPrice < 18){
                showDialogSingleButton(context.getString(R.string.custom_gas_notice_2),null);
            }else if (tempPrice > 100){
                showDialogSingleButton(context.getString(R.string.custom_gas_notice),null);
            }
        }
    }

    /**
     * show dialog single button
     * @param title  title
     * @param content content
     * */
    public void showDialogSingleButton(String title,String content){
        MyViewDialogFragment mdf = new MyViewDialogFragment(MyViewDialogFragment.DIALOG_SINGLE_BUTTON);
        mdf.setTitleAndContentText(title, content);
        mdf.show(((AppCompatActivity)context).getSupportFragmentManager(), "mdf");
    }

    public String setOnActivityResult(String toAddress,float toAmount,SeekBar seekbar){
        address = toAddress;
        amount = toAmount;
        balance.setVisibility(View.GONE);
        if (tokenVo.getTokenBalance() > 0){
            BigDecimal decimal = new BigDecimal(tokenVo.getTokenBalance()).setScale(6,BigDecimal.ROUND_DOWN);
            balance.setText(context.getString(R.string.balance_er,decimal.toPlainString()));
        }else{
            balance.setText(context.getString(R.string.balance_er,tokenVo.getTokenBalance() + ""));
        }

        if(TextUtils.equals(tokenVo.getContactAddress(),Constants.SMT_CONTACT)){//smt
            setGasMethod(true,seekbar,true);
        }else {//token
            setGasMethod(true,seekbar,false);
        }
        if (spectrumHint){
            LoadingDialog.close();
            try {
                showDialogSingleButton(context.getString(R.string.custom_spectrum_notice),null);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return tokenVo.getTokenSymbol();
    }

    /**
     * send trans
     * */
    public void sendTrans(EditText customGasPrice, boolean isChecked){
        String address = toAddress.getText().toString();
        if(TextUtils.isEmpty(address)){
            MyToast.showToast(context,context.getString(R.string.empty_address));
            return;
        }else if(!address.startsWith("0x") ||  address.length()!=42){
            MyToast.showToast(context,context.getString(R.string.error_address));
            return;
        }

        if (TextUtils.isEmpty(toValue.getText().toString())){
            MyToast.showToast(context,context.getString(R.string.money_empty));
            return;
        }

        if(TextUtils.equals(tokenVo.getTokenSymbol(),context.getString(R.string.smt))){//send SMT
            double total = Double.valueOf(toValue.getText().toString()) + currentGas;
            if(total > tokenVo.getTokenBalance()){
                MyToast.showToast(context,context.getString(R.string.balance_not_enough));
                return;
            }
        }else{//send ERC20 contact token
            if (checkBalanceMethod()){
                return;
            }
        }

        if (currentLimit <= 0 || currentGas <= 0){
            MyToast.showToast(context,context.getString(R.string.gas_value_to_small));
            return;
        }

        String customPrice = customGasPrice.getText().toString();
        try {
            if (isChecked && Integer.parseInt(customPrice) < 19){
                MyToast.showToast(context,context.getString(R.string.custom_gas_price));
                return;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        MyViewDialogFragment mdf = new MyViewDialogFragment(MyViewDialogFragment.DIALOG_INPUT_PWD, new MyViewDialogFragment.EditCallback() {
            @Override
            public void getEditText(String editText) {
                getWalletPrivateKey(editText);
            }
        });
        mdf.show(((AppCompatActivity)context).getSupportFragmentManager(), "mdf");
        return;
    }

    /**
     * Get private key
     * @param walletPwd wallet password
     * */
    private void getWalletPrivateKey(final String walletPwd){
        LoadingDialog.show(context,"");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Credentials keys = WalletStorage.getInstance(context.getApplicationContext()).getFullWallet(context,walletPwd,fromAddress.getText().toString());
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
                    if (TextUtils.equals(tokenVo.getContactAddress(),Constants.SMT_CONTACT)){//Send SMT
                        sendTransSMT(keys);
                    }else{// Send ERC20 Token
                        sendTransToken(keys);
                    }
                    break;
                case 1://Enter the wrong password
                    LoadingDialog.close();
                    MyToast.showToast(context,context.getString(R.string.wallet_copy_pwd_error));
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
                    transVo.setLogo(tokenVo.getTokenLogo());
                    transVo.setSymbol(tokenVo.getTokenSymbol());
                    transVo.setTokenAddress(tokenVo.getContactAddress());
                    transVo.setName(tokenVo.getTokenName());
                    Intent intent = new Intent(context,TransactionDetailActivity.class);
                    intent.putExtra("transVo",transVo);
                    intent.putExtra("isSendTrans",true);
                    context.startActivity(intent);
                    Utils.exitActivityAndBackAnim((Activity) context,true);
                    break;
                case 3://Request failed
                    LoadingDialog.close();
                    String errMsg = (String)msg.obj;
                    MyToast.showToast(context,errMsg);
                    break;
                case 4://Failed to get gas
                    LoadingDialog.close();
                    MyToast.showToast(context,context.getString(R.string.get_gas_error));
                    Utils.exitActivityAndBackAnim((Activity) context,true);
                    break;
            }
        }
    };


    /**
     * Check if the balance is sufficient
     * @return true  the amount is not enough
     * */
    private boolean checkBalanceMethod(){
        if(currentGas > smtBalance){
            MyToast.showToast(context,context.getString(R.string.balance_not_enough_smt));
            return true;
        }
        double tempValue = Double.valueOf(toValue.getText().toString());
        if (tempValue > tokenVo.getTokenBalance()){
            MyToast.showToast(context,context.getString(R.string.balance_not_enough_token,tokenVo.getTokenSymbol()));
            return true;
        }
        return false;
    }

    /**
     * change gas limit and gas price
     * @param progress progress
     * */
    public boolean gasChangeMethod(int progress,boolean isSendSmt){
        GasVo tempGasVo;
        if (isSendSmt){
            tempGasVo = smtGasVo;
        }else{
            tempGasVo = tokenGasVo;
        }
        int realprogress = 0;
        if (tempGasVo == null){
            return true;
        }
        realprogress = progress + tempGasVo.getMinPrice();
        BigDecimal b1 = new BigDecimal(Double.toString(realprogress - tempGasVo.getMinPrice()));
        BigDecimal b2 = new BigDecimal(Double.toString(tempGasVo.getMaxPrice() - tempGasVo.getMinPrice()));
        double percent = b1.divide(b2,6, BigDecimal.ROUND_HALF_UP).doubleValue();
        currentLimit = (int)(percent * (tempGasVo.getMaxLimit() - tempGasVo.getMinLimit()) + tempGasVo.getMinLimit());
        currentGas = realprogress * currentLimit/FACTOR;
        return false;
    }

    /**
     * get gas
     * */
    public void getGasMethod(final SeekBar seekbar) {
        NetRequestImpl.getInstance().getGas(new RequestListener() {
            @Override
            public void start() {
                LoadingDialog.show(context,null);
            }

            @Override
            public void success(JSONObject response) {
                JSONObject object = response.optJSONObject("data");
                smtGasVo = new GasVo().parse(object.optJSONObject("smt"));//smt  gas
                tokenGasVo = new GasVo().parse(object.optJSONObject("token"));//smt  gas
                spectrumHint = response.optInt("spectrum_hint",0) == 1;
                if (smtGasVo == null || tokenGasVo == null){
                    LoadingDialog.close();
                    MyToast.showToast(context,context.getString(R.string.get_gas_error));
                    Utils.exitActivityAndBackAnim((Activity) context,true);
                }else{
                    LoadingDialog.close();
                    if (TextUtils.equals(tokenVo.getContactAddress(),Constants.SMT_CONTACT)){//smt gas
                        seekbar.setMax(smtGasVo.getMaxPrice() - smtGasVo.getMinPrice());
                        if (smtGasVo.getDefaultPrice() - smtGasVo.getMinPrice() > 0){
                            seekbar.setProgress(smtGasVo.getDefaultPrice() - smtGasVo.getMinPrice());
                        }
                    }else{//token gas
                        seekbar.setMax(tokenGasVo.getMaxPrice() - tokenGasVo.getMinPrice());
                        if (tokenGasVo.getDefaultPrice() - tokenGasVo.getMinPrice() > 0){
                            seekbar.setProgress(tokenGasVo.getDefaultPrice() - tokenGasVo.getMinPrice());
                        }
                    }
                    if (spectrumHint){
                        LoadingDialog.close();
                        showDialogSingleButton(context.getString(R.string.custom_spectrum_notice),null);
                    }
                }
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                LoadingDialog.close();
                MyToast.showToast(context,context.getString(R.string.get_gas_error));
                Utils.exitActivityAndBackAnim((Activity) context,true);
            }
        });
    }

    /**
     * send eth
     * @param keys Credentials
     * */
    private void sendTransSMT(final Credentials keys) {

        NetRequestImpl.getInstance().getNonce(fromAddress.getText().toString(), new RequestListener() {
            @Override
            public void start() {

            }

            @Override
            public void success(JSONObject response) {
                BigInteger nonce = new BigInteger(response.optString("nonce"), 10);
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
                sendRawTransaction("0x" + hexValue);
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                LoadingDialog.close();
                MyToast.showToast(context,errorMsg);
            }
        });

    }


    /**
     * send ERC20 Token
     * @param keys Credentials
     * */
    private void sendTransToken(final Credentials keys) {
        NetRequestImpl.getInstance().getNonce(fromAddress.getText().toString(), new RequestListener() {
            @Override
            public void start() {

            }

            @Override
            public void success(JSONObject response) {
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
                BigInteger nonce = new BigInteger(response.optString("nonce"), 10);
                String address = tokenVo.getContactAddress();
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
                sendRawTransaction("0x" + hexValue);
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                LoadingDialog.close();
                MyToast.showToast(context,errorMsg);
            }
        });
    }


    private void sendRawTransaction(String data){
        NetRequestImpl.getInstance().sendTransaction(data, new RequestListener() {
            @Override
            public void start() {

            }

            @Override
            public void success(JSONObject response) {
                String txurl = response.optString("txurl");
                String tx = response.optString("tx");
                Message message = Message.obtain();
                Bundle bundle = new Bundle();
                bundle.putString("tx",tx);
                bundle.putString("txurl",txurl);
                message.setData(bundle);
                message.what = 2;
                mHandler.sendMessage(message);
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                LoadingDialog.close();
                MyToast.showToast(context,errorMsg);
            }
        });
    }

    public void setWalletHelp(){
        String result = "";
        String language = MySharedPrefs.readString(context,MySharedPrefs.FILE_APPLICATION,MySharedPrefs.KEY_LANGUAFE);
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
        Intent intent = new Intent(context, WebViewUI.class);
        intent.putExtra("loadUrl", result);
        intent.putExtra("title", context.getString(R.string.description));
        context.startActivity(intent);
        Utils.openNewActivityAnim((Activity) context,false);
    }

   public void onDestroy(){
        if (mHandler != null){
            mHandler.removeCallbacksAndMessages(null);
            mHandler=null;
        }
    }
}
