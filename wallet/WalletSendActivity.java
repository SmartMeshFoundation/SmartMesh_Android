package com.lingtuan.firefly.wallet;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.quickmark.CaptureActivity;
import com.lingtuan.firefly.ui.WebViewUI;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.MyViewDialogFragment;
import com.lingtuan.firefly.util.SDCardCtrl;
import com.lingtuan.firefly.util.netutil.VolleyUtils;
import com.lingtuan.firefly.wallet.util.WalletStorage;

import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import geth.Account;
import geth.Accounts;
import geth.Address;
import geth.BigInt;
import geth.Context;
import geth.Geth;
import geth.Hash;
import geth.KeyStore;
import geth.Transaction;

/**
 * Created on 2017/8/22.
 */

public class WalletSendActivity extends BaseActivity implements  TextWatcher, SeekBar.OnSeekBarChangeListener {
    private final double FACTOR = 1000000000f;
    private final String contractFunctionHex = "0xa9059cbb";//Contract function
    private final int minEthGasLimit = 21000;//min eth gasLimit
    private final int defaultEthGasLimit = 24625;//current eth gasLimit
    private final int maxEthGasLimit = 50000;//max eth gasLimit
    private final int minTokenGasLimit = 60000;//min token gasLimit
    private final int defaultTokenGasLimit = 63750;//current token gasLimit
    private final int maxTokenGasLimit = 90000;//max token gasLimit

    private int currentGasLimit;//current gasLimit

    BigDecimal ONE_ETHER = new BigDecimal("1000000000000000000");//1个eth

    private BigInt gasPrice;
    private TextView fromName;
    private TextView fromAddress;
    private EditText toValue;
    private EditText toAddress;
    private TextView send;
    private TextView gas;
    private TextView blance_eth;
    private TextView blance_fft;
    private ImageView app_right;
    private  SeekBar seekbar;

    private int sendtype  ;// 0  ETH   1 SMT
    private float amount;//The sum of the scanning qr code passed
    private String address;
    private int walletType;//0 ordinary purse, 1 observe the purse
    private double ethBalance,fftBalance;

    @Override
    protected void setContentView() {
       setContentView(R.layout.wallet_send_layout);
    }

    @Override
    protected void findViewById() {
        fromName = (TextView) findViewById(R.id.fromName);
        fromAddress = (TextView) findViewById(R.id.fromAddress);
        toValue = (EditText) findViewById(R.id.toValue);
        toAddress = (EditText) findViewById(R.id.toAddress);
        send =  (TextView) findViewById(R.id.send);
        gas =  (TextView) findViewById(R.id.gas);
        blance_eth =  (TextView) findViewById(R.id.blance_eth);
        blance_fft =  (TextView) findViewById(R.id.blance_fft);
        app_right =  (ImageView) findViewById(R.id.app_right);
        seekbar  =  (SeekBar) findViewById(R.id.seekbar);
    }

    @Override
    protected void setListener() {
        send.setOnClickListener(this);
        app_right.setOnClickListener(this);
        toValue.addTextChangedListener(this);
        seekbar.setOnSeekBarChangeListener(this);
    }

    @Override
    protected void initData() {

        getGasMethod();

        sendtype  = getIntent().getIntExtra("sendtype",0);
        amount = getIntent().getFloatExtra("amount",0);
        address = getIntent().getStringExtra("address");

        if(!TextUtils.isEmpty(address)){
            toAddress.setText(address);
        }

        if(sendtype == 1){//SMT
            blance_fft.setVisibility(View.VISIBLE);
            setTitle(getString(R.string.send_blance_title,"SMT"));
        }
        else{
            setTitle(getString(R.string.send_blance_title,"ETH"));
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

                if(!TextUtils.isEmpty(address) && !address.startsWith("0x")){
                    address= "0x"+ address;
                }
                fromAddress.setText(address);

                ethBalance = WalletStorage.getInstance(getApplicationContext()).get().get(i).getEthBalance();
                fftBalance = WalletStorage.getInstance(getApplicationContext()).get().get(i).getFftBalance();
                blance_eth.setText(ethBalance + "  ETH");
                blance_fft.setText(fftBalance + "  SMT");
                break;
            }
        }

        if (walletType == 1){
            send.setEnabled(false);
            send.setText(getString(R.string.wallet_scan_cant_trans));
        }

        if (!TextUtils.isEmpty(toAddress.getText().toString())){
            toValue.setFocusable(true);
            toValue.setFocusableInTouchMode(true);
            toValue.requestFocus();
        }

        List<String> currencyList = new ArrayList<>();
        currencyList.add(getString(R.string.eth));
        currencyList.add(getString(R.string.smt));
        ArrayAdapter<String> curAdapter = new ArrayAdapter<>(WalletSendActivity.this, android.R.layout.simple_spinner_item, currencyList);
        curAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    /**
     * To get gas
     * */
    private void getGasMethod() {
        int state = MySharedPrefs.readInt(WalletSendActivity.this,MySharedPrefs.FILE_USER,MySharedPrefs.AGREE_SYNC_BLOCK);
        if (state != 2){
            mHandler.sendEmptyMessage(5);
            return;
        }

        LoadingDialog.show(WalletSendActivity.this,null);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    gasPrice =  NextApplication.ec.suggestGasPrice(new Context());
                    mHandler.sendEmptyMessage(0);
                    LoadingDialog.close();
                } catch (Exception e) {
                    mHandler.sendEmptyMessage(4);
                    e.printStackTrace();
                }
            }
        }).start();

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.app_right:
                Intent i = new Intent(WalletSendActivity.this,CaptureActivity.class);
                i.putExtra("type",1);
                startActivityForResult(i,100);
                break;
            case R.id.send:
                sendTrans();
                break;
            default:
                super.onClick(v);
                break;
        }

    }

    //Begin to transfer
    private void  sendTrans(){
        String address = toAddress.getText().toString();
        double currentGas = new BigDecimal(gasPrice.toString()).multiply(new BigDecimal(currentGasLimit)).divide(ONE_ETHER,6,BigDecimal.ROUND_DOWN).doubleValue();
        if(TextUtils.isEmpty(address)){
            showToast(getString(R.string.empty_address));
            return;
        } else if(!address.startsWith("0x") ||  address.length()!=42){
            showToast(getString(R.string.error_address));
            return;
        }

        if (TextUtils.isEmpty(toValue.getText().toString())){
            showToast(getString(R.string.money_empty));
            return;
        }

        if(sendtype == 0){//Send the ETH
            double total = Double.valueOf(toValue.getText().toString()) + currentGas;
            if(total>ethBalance){
                showToast(getString(R.string.bloance_not_enough));
                return;
            }
        } else{//Send the SMT
            if(currentGas>ethBalance){
                showToast(getString(R.string.bloance_not_enough));
                return;
            }
            if(Double.valueOf(toValue.getText().toString())>fftBalance){
                showToast(getString(R.string.bloance_not_enough));
                return;
            }
        }
        MyViewDialogFragment mdf = new MyViewDialogFragment(MyViewDialogFragment.DIALOG_INPUT_PWD, new MyViewDialogFragment.EditCallback() {
            @Override
            public void getEditText(String editText) {
                LoadingDialog.show(WalletSendActivity.this,"");
                if(sendtype == 0){
                    sendTokenMethod(editText,true);
                }else{
                    sendTokenMethod(editText,false);
                }

            }
        });
        mdf.show(this.getSupportFragmentManager(), "mdf");
    }

    private Timer timer;
    private TimerTask task;
    private Hash transactionHash;

    /**
     * send token
     * @param isSendEth Is eth transfer ,true send ETH , false send SMT
     * @param password account password
     * */
    private void sendTokenMethod(final String password,final boolean isSendEth){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String root =  getFilesDir() + SDCardCtrl.WALLERPATH;
                    KeyStore keyStore = new KeyStore(root,Geth.StandardScryptN, Geth.StandardScryptP);
                    Accounts accounts = keyStore.getAccounts();
                    Account account = null;
                    for (int i= 0 ; i < accounts.size(); i++){
                        if (accounts.get(i).getAddress().getHex().toLowerCase().equals(fromAddress.getText().toString().toLowerCase())){
                            account = accounts.get(i);
                            break;
                        }
                    }
                    if(account ==null){
                        Message message = Message.obtain();
                        message.obj = "Error";
                        message.what = 3;
                        mHandler.sendMessage(message);
                        return;
                    }
                    Context ctx = new Context();
                    BigInt gasLimitB = Geth.newBigInt(currentGasLimit);
                    Transaction transaction;
                    long nonce =  NextApplication.ec.getNonceAt(new Context(),new Address(fromAddress.getText().toString()),-1);
                    if (isSendEth){
                        BigInt value = Geth.newBigInt(new BigDecimal(toValue.getText().toString()).multiply(ONE_ETHER).longValue());
                        transaction = Geth.newTransaction(nonce, new Address(toAddress.getText().toString()), value, gasLimitB, gasPrice, null);
                    }else{
                        BigInt value = Geth.newBigInt(0);
                        String valueOld =  new BigDecimal(toValue.getText().toString()).multiply(ONE_ETHER).setScale(0,BigDecimal.ROUND_DOWN).toPlainString();//multiply on ether
                        String valueHex  =  new BigDecimal(valueOld).toBigInteger().toString(16);//Converted to hexadecimal
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
                        //ToHex not neat 64 valueHex supplement 64
                        String data = contractFunctionHex + toHex + valueHex;
                        byte[] srtbyte = Numeric.hexStringToByteArray(data);
                        transaction = Geth.newTransaction(nonce, new Address(Constants.CONTACT_ADDRESS), value, gasLimitB, gasPrice,srtbyte);
                    }
                    keyStore.unlock(account, password);
                    transaction = keyStore.signTx(account, transaction, new BigInt(Constants.GLOBAL_SWITCH_OPEN ? 1 : 3));
                    NextApplication.ec.sendTransaction(ctx,transaction);
                    transactionHash = transaction.getHash();
//                    getTransactionReceipt();

                    Message message = Message.obtain();
                    message.obj = transaction.getHash().getHex();
                    message.what = 2;
                    mHandler.sendMessage(message);
                } catch (Exception e) {
                    if(!TextUtils.isEmpty(e.getMessage()) && e.getMessage().contains("could not decrypt key with given passphrase")){
                        mHandler.sendEmptyMessage(1);
                    }else{
                        Message message = Message.obtain();
                        message.obj = "Error";
                        message.what = 3;
                        mHandler.sendMessage(message);
                    }
                    e.printStackTrace();
                }
            }
        }).start();
    }

//    private void getTransactionReceipt(){
//        if(timer == null){
//            timer = new Timer();
//            task = new TimerTask() {
//                @Override
//                public void run() {
//                    try {
//                        Receipt receipt = NextApplication.ec.getTransactionReceipt(new Context(),transactionHash);
//                        Log.e("*****Receipt",receipt.string());
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            };
//            timer.schedule(task,0,2000);
//        }
//    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0://To get gas
                    if(sendtype == 1){//SMT
                        seekbar.setMax(maxTokenGasLimit - minTokenGasLimit);
                        seekbar.setProgress(defaultTokenGasLimit - minTokenGasLimit);
                    }else{
                        seekbar.setMax(maxEthGasLimit - minEthGasLimit);
                        seekbar.setProgress(defaultEthGasLimit - minEthGasLimit);
                    }
                    LoadingDialog.close();
                    break;
                case 1://Enter the password error
                    LoadingDialog.close();
                    showToast(getString(R.string.wallet_copy_pwd_error));
                    break;
                case 2://Transfer success
                    LoadingDialog.close();
                    showToast(getString(R.string.trans_success));
                    String url = (String) msg.obj;
                    if (!TextUtils.isEmpty(url)){
                        Intent intent = new Intent(WalletSendActivity.this, WebViewUI.class);
                        intent.putExtra("loadUrl", VolleyUtils.TRANS_DETAIL_URL + url);
                        intent.putExtra("needRefresh", true);
                        intent.putExtra("title", getString(R.string.transcation_detail));
                        startActivity(intent);
                    }
                    finish();
                    break;
                case 3://The request failed
                    LoadingDialog.close();
                    String errMsg = (String)msg.obj;
                    showToast(errMsg);
                    break;
                case 4://Failed to get gas
                    LoadingDialog.close();
                    showToast(getString(R.string.get_gas_error));
                    finish();
                    break;
                case 5://Synchronization, try again later
                    LoadingDialog.close();
                    showToast(getString(R.string.syning_later));
                    finish();
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
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (sendtype == 0){//eth gas
            currentGasLimit = progress + minEthGasLimit;
            String currentGas = new BigDecimal(gasPrice.toString()).multiply(new BigDecimal(currentGasLimit)).divide(ONE_ETHER,6,BigDecimal.ROUND_DOWN).toPlainString();
            gas.setText(getString(R.string.eth_er,currentGas));
        }else{//smt gas
            currentGasLimit = progress + minTokenGasLimit;
            String currentGas = new BigDecimal(gasPrice.toString()).multiply(new BigDecimal(currentGasLimit)).divide(ONE_ETHER,6,BigDecimal.ROUND_DOWN).toPlainString();
            gas.setText(getString(R.string.eth_er,currentGas));
        }
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
            address = data.getStringExtra("address");
            sendtype  = data.getIntExtra("sendtype",0);
            amount = data.getFloatExtra("amount",0);
            if(sendtype == 1){//SMT
                blance_fft.setVisibility(View.VISIBLE);
                setTitle(getString(R.string.send_blance_title,getString(R.string.smt)));
                getGasLimit(maxTokenGasLimit,defaultTokenGasLimit,minTokenGasLimit);
            }else{
                setTitle(getString(R.string.send_blance_title,getString(R.string.eth)));
                getGasLimit(maxEthGasLimit,defaultEthGasLimit,minEthGasLimit);
            }

            if(amount>0){
                toValue.setText(amount+"");
            }

            if(!TextUtils.isEmpty(address)){
                toAddress.setText(address);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * get current gasLimit
     * @param defaultGasLimit defaultGasLimit
     * @param maxGasLimit maxGasLimit
     * @param minGasLimit minGasLimit
     * */
    private void getGasLimit(int maxGasLimit,int defaultGasLimit,int minGasLimit){
        String currentGas;
        seekbar.setMax(maxGasLimit - minGasLimit);
        seekbar.setProgress(defaultGasLimit - minGasLimit);
        BigDecimal b1 = new BigDecimal(Double.toString(defaultGasLimit - minGasLimit));
        BigDecimal b2 = new BigDecimal(Double.toString(maxGasLimit - minGasLimit));
        double percent = b1.divide(b2,6, BigDecimal.ROUND_HALF_UP).doubleValue();
        currentGasLimit = (int)(percent* (maxGasLimit - minGasLimit) + minGasLimit);
        currentGas = new BigDecimal(gasPrice.toString()).multiply(new BigDecimal(currentGasLimit)).divide(ONE_ETHER,6,BigDecimal.ROUND_DOWN).toPlainString();
        gas.setText(getString(R.string.eth_er,currentGas));
    }
}
