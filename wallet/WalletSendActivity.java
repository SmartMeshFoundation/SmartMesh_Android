package com.lingtuan.firefly.wallet;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.quickmark.CaptureActivity;
import com.lingtuan.firefly.ui.WebViewUI;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.MyViewDialogFragment;
import com.lingtuan.firefly.util.netutil.NetRequestUtils;
import com.lingtuan.firefly.wallet.util.WalletStorage;
import com.lingtuan.firefly.wallet.vo.GasVo;
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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * send token
 * Created on 2017/8/22.
 */

public class WalletSendActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener, TextWatcher {
    private final double FACTOR = 1000000000f;
    private final String contractFunctionHex = "0xa9059cbb";//
    BigDecimal ONE_ETHER = new BigDecimal("1000000000000000000");//one eth
    private TextView fromName;
    private TextView fromAddress;
    private EditText toValue;
    private EditText toAddress;
    private TextView send;
    private SeekBar seekbar;
    private TextView gas;
    private TextView blance_eth;
    private TextView blance_fft;
    private ImageView app_right;
    private double currentGas;//gas
    private int currentLimit;//limit
    private int sendtype  ;// 0  ETH   1 SMT
    private float amount;//Scan QR code passed over the amount
    private String address;
    private int walletType;//0 ordinary wallets 1 observation wallet
    private double ethBalance,fftBalance;
    private GasVo ethVo,fftVo;
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
        seekbar  =  (SeekBar) findViewById(R.id.seekbar);
        blance_eth =  (TextView) findViewById(R.id.blance_eth);
        blance_fft =  (TextView) findViewById(R.id.blance_fft);
        app_right =  (ImageView) findViewById(R.id.app_right);
    }

    @Override
    protected void setListener() {
        send.setOnClickListener(this);
        seekbar.setOnSeekBarChangeListener(this);
        app_right.setOnClickListener(this);
        toValue.addTextChangedListener(this);
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
            setTitle(getString(R.string.send_blance_title,getString(R.string.smt)));
        }else{
            blance_fft.setVisibility(View.GONE);
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

                if(!TextUtils.isEmpty(address) && !address.startsWith("0x")) {
                    address= "0x"+ address;
                }
                fromAddress.setText(address);

                ethBalance = WalletStorage.getInstance(getApplicationContext()).get().get(i).getEthBalance();
                fftBalance = WalletStorage.getInstance(getApplicationContext()).get().get(i).getFftBalance();

                if (ethBalance > 0){
                    BigDecimal ethDecimal = new BigDecimal(ethBalance).setScale(10,BigDecimal.ROUND_DOWN);
                    blance_eth.setText(getString(R.string.eth_balance,ethDecimal.toString()));
                }else{
                    blance_eth.setText(getString(R.string.eth_balance,ethBalance + ""));
                }
                if (fftBalance > 0){
                    BigDecimal fftDecimal = new BigDecimal(fftBalance).setScale(5,BigDecimal.ROUND_DOWN);
                    blance_fft.setText(getString(R.string.smt_balance,fftDecimal.toString()));
                }else{
                    blance_fft.setText(getString(R.string.smt_balance,fftBalance + ""));
                }
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
    }

    /**
     * get gas
     * */
    private void getGasMethod() {
        LoadingDialog.show(WalletSendActivity.this,null);
        try {
            NetRequestUtils.getInstance().getGas(WalletSendActivity.this,new Callback() {
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
                            ethVo = new GasVo().parse(ethObj);//eth  gas
                            fftVo = new GasVo().parse(fftObj);//smt gas
                            if (ethVo == null || fftVo == null){
                                mHandler.sendEmptyMessage(4);
                            }else{
                                mHandler.sendEmptyMessage(5);
                            }
                        }else{
                            if(errcod == -2){
                                long difftime = object.optJSONObject("data").optLong("difftime");
                                long tempTime =  MySharedPrefs.readLong(WalletSendActivity.this,MySharedPrefs.FILE_APPLICATION,MySharedPrefs.KEY_REQTIME);
                                MySharedPrefs.writeLong(WalletSendActivity.this,MySharedPrefs.FILE_APPLICATION,MySharedPrefs.KEY_REQTIME,difftime + tempTime);
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
                Intent i = new Intent(WalletSendActivity.this,CaptureActivity.class);
                i.putExtra("type",1);
                startActivityForResult(i,100);
                break;
            case R.id.send:
                sendtrans();
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
            double total = Double.valueOf(toValue.getText().toString())+currentGas;
            if(total>ethBalance){
                showToast(getString(R.string.bloance_not_enough));
                return;
            }
        }else{//send SMT
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
                getWalletPrivateKey(editText);
            }
        });
        mdf.show(this.getSupportFragmentManager(), "mdf");
    }


    /**
     * send SMT
     * @param keys Credentials
     * */
    private void sendtransToken(final Credentials keys) {
        try {
            NetRequestUtils.getInstance().getEthNonce(WalletSendActivity.this,fromAddress.getText().toString(), new Callback() {
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
                            String valueOld =  new BigDecimal(toValue.getText().toString()).multiply(ONE_ETHER).setScale(0,BigDecimal.ROUND_DOWN).toPlainString();//乘以一个以太坊
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
                            String address = Constants.CONTACT_ADDRESS;
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

                            NetRequestUtils.getInstance().sendRawTransaction(WalletSendActivity.this,"0x" +hexValue, new Callback() {
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
    private void sendtransEth(final Credentials keys) {
        try {
            NetRequestUtils.getInstance().getEthNonce(WalletSendActivity.this,fromAddress.getText().toString(), new Callback() {
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

                            NetRequestUtils.getInstance().sendRawTransaction(WalletSendActivity.this,"0x" +hexValue, new Callback() {
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

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int realprogress;
        if (sendtype == 0){//eth gas
            realprogress = progress + ethVo.getMinPrice();
            BigDecimal b1 = new BigDecimal(Double.toString(realprogress - ethVo.getMinPrice()));
            BigDecimal b2 = new BigDecimal(Double.toString(ethVo.getMaxPrice() - ethVo.getMinPrice()));
            double percent = b1.divide(b2,6, BigDecimal.ROUND_HALF_UP).doubleValue();
            currentLimit = (int)(percent * (ethVo.getMaxLimit() - ethVo.getMinLimit()) + ethVo.getMinLimit());
            currentGas = realprogress * currentLimit/FACTOR;
        }else{//smt gas
            realprogress = progress + fftVo.getMinPrice();
            BigDecimal b1 = new BigDecimal(Double.toString(realprogress - fftVo.getMinPrice()));
            BigDecimal b2 = new BigDecimal(Double.toString(fftVo.getMaxPrice() - fftVo.getMinPrice()));
            double percent = b1.divide(b2,6, BigDecimal.ROUND_HALF_UP).doubleValue();
            currentLimit = (int)(percent * (fftVo.getMaxLimit() - fftVo.getMinLimit()) + fftVo.getMinLimit());
            currentGas = realprogress * currentLimit/FACTOR;
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
     * Get private key
     * @param walletPwd wallet password
     * */
    private void getWalletPrivateKey(final String walletPwd){
        LoadingDialog.show(WalletSendActivity.this,"");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Credentials keys = WalletStorage.getInstance(getApplicationContext()).getFullWallet(WalletSendActivity.this,walletPwd,fromAddress.getText().toString());
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
                        sendtransEth(keys);
                    }else{//send SMT
                        sendtransToken(keys);
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

                    Intent intent = new Intent(WalletSendActivity.this,TransactionDetailActivity.class);
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
                        seekbar.setProgress(ethVo.getDefaultPrice() - ethVo.getMinPrice());
                    }else{//smt gas
                        seekbar.setMax(fftVo.getMaxPrice() - fftVo.getMinPrice());
                        seekbar.setProgress(fftVo.getDefaultPrice() - fftVo.getMinPrice());
                    }
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
            address = data.getStringExtra("address");
            sendtype  = data.getIntExtra("sendtype",0);
            amount = data.getFloatExtra("amount",0);

            if(sendtype == 1){//smt
                blance_fft.setVisibility(View.VISIBLE);
                setTitle(getString(R.string.send_blance_title,getString(R.string.smt)));
                getGasMethod(fftVo);
            }else{
                blance_fft.setVisibility(View.GONE);
                setTitle(getString(R.string.send_blance_title,getString(R.string.eth)));
                getGasMethod(ethVo);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * get gas method
     * @param gasVo gasVo
     * */
    private void getGasMethod(GasVo gasVo){
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
        }
        if (!TextUtils.isEmpty(toAddress.getText().toString())){
            toValue.setFocusable(true);
            toValue.setFocusableInTouchMode(true);
            toValue.requestFocus();
        }
    }
}
