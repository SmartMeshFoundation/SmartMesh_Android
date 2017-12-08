package com.lingtuan.firefly.wallet;

import android.accounts.Account;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.quickmark.CaptureActivity;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.MyViewDialogFragment;
import com.lingtuan.firefly.util.SDCardCtrl;
import com.lingtuan.firefly.util.netutil.WalletRequestUtils;
import com.lingtuan.firefly.wallet.util.Sign2;
import com.lingtuan.firefly.wallet.util.WalletStorage;

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
import java.util.ArrayList;
import java.util.List;

import geth.Accounts;
import geth.Address;
import geth.BigInt;
import geth.Context;
import geth.Geth;
import geth.KeyStore;
import geth.SyncProgress;
import geth.Transaction;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created on 2017/8/22.
 */

public class WalletSendActivity extends BaseActivity implements  TextWatcher {
    private final double FACTOR = 1000000000f;
    private final String contractAddress = "0x4042698c5f4c7eb64035870feea5c316b913927f";//contract address
    private final String contractFunctionHex = "0xa9059cbb";
    private final int gasLimit = 21000;//gasLimit
    private final int tokenGasLimit = 80000;//gasLimit
    private BigInt gasPrice;

    BigDecimal ONE_ETHER = new BigDecimal("1000000000000000000");// one eth
    TextView fromName;
    TextView fromAddress;
    EditText toValue;
    EditText toAddress;
    TextView send;
    TextView gas;
    TextView blance_eth;
    TextView blance_fft;
    ImageView app_right;

    int sendtype  ;// 0  ETH   1 SMT
    private int gastype;// 0  ETH   1 FFP
    float amount;
    String address;

    int walletType;//wallet type scan or true

    double ethBalance,fftBalance;

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
    }

    @Override
    protected void setListener() {
        send.setOnClickListener(this);
        app_right.setOnClickListener(this);
        toValue.addTextChangedListener(this);
    }

    @Override
    protected void initData() {

        getGasMethod();

        sendtype  = getIntent().getIntExtra("sendtype",0);
        amount = getIntent().getFloatExtra("amount",0);
        address = getIntent().getStringExtra("address");

        if(!TextUtils.isEmpty(address))
        {
            toAddress.setText(address);
        }

        if(sendtype == 1)//SMT
        {
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
        for(int i=0;i<WalletStorage.getInstance(getApplicationContext()).get().size();i++)
        {
            if(WalletStorage.getInstance(getApplicationContext()).get().get(i).isSelect())
            {
                fromName.setText(WalletStorage.getInstance(getApplicationContext()).get().get(i).getWalletName());
                String address = WalletStorage.getInstance(getApplicationContext()).get().get(i).getPublicKey();
                walletType = WalletStorage.getInstance(getApplicationContext()).get().get(i).getWalletType();

                if(!TextUtils.isEmpty(address) && !address.startsWith("0x"))
                {
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

        List<String> currencyList = new ArrayList<>();
        currencyList.add(getString(R.string.eth));
        currencyList.add(getString(R.string.smt));
        ArrayAdapter<String> curAdapter = new ArrayAdapter<>(WalletSendActivity.this, android.R.layout.simple_spinner_item, currencyList);
        curAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

  
    /**
    *get gas method
    */
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
        switch(v.getId())
        {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK)
        {
            address = data.getStringExtra("address");
            sendtype  = data.getIntExtra("sendtype",0);
            amount = data.getFloatExtra("amount",0);
            gastype = 0;
            if(sendtype == 1)//SMT
            {
                blance_fft.setVisibility(View.VISIBLE);
                setTitle(getString(R.string.send_blance_title,"SMT"));
                String currentGas = new BigDecimal(gasPrice.toString()).multiply(new BigDecimal(tokenGasLimit)).divide(ONE_ETHER,0,BigDecimal.ROUND_DOWN).toPlainString();
                gas.setText(getString(R.string.eth_er,currentGas));
            }
            else{
                setTitle(getString(R.string.send_blance_title,"ETH"));
                String currentGas = new BigDecimal(gasPrice.toString()).multiply(new BigDecimal(gasLimit)).divide(ONE_ETHER,0,BigDecimal.ROUND_DOWN).toPlainString();
                gas.setText(getString(R.string.eth_er,currentGas));
            }



            if(amount>0)
            {
                toValue.setText(amount+"");
            }
            if(!TextUtils.isEmpty(address))
            {
                toAddress.setText(address);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

   
    /**
    *send trans 
    */
    private void  sendtrans(){
        String address = toAddress.getText().toString();
        double currentGas;
        if(sendtype == 0)
        {
            currentGas = new BigDecimal(gasPrice.toString()).multiply(new BigDecimal(gasLimit)).divide(ONE_ETHER,0,BigDecimal.ROUND_DOWN).doubleValue();
        }else{
            currentGas = new BigDecimal(gasPrice.toString()).multiply(new BigDecimal(tokenGasLimit)).divide(ONE_ETHER,0,BigDecimal.ROUND_DOWN).doubleValue();
        }

        if(TextUtils.isEmpty(address))
        {
            showToast(getString(R.string.empty_address));
            return;
        }
        else if(!address.startsWith("0x") ||  address.length()!=42)
        {
            showToast(getString(R.string.error_address));
            return;
        }

        if (TextUtils.isEmpty(toValue.getText().toString())){
            showToast(getString(R.string.money_empty));
            return;
        }

        if(sendtype == 0)//
        {
            double total = Double.valueOf(toValue.getText().toString())+currentGas;
            if(total>ethBalance)
            {
                showToast(getString(R.string.bloance_not_enough));
                return;
            }
        }else{
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
                    sendtransEth(editText);
                }else{
                    sendtransToken(editText);
                }

            }
        });
        mdf.show(this.getSupportFragmentManager(), "mdf");
    }

    /**send smart token*/
    private void sendtransToken(final String password) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String root =  getFilesDir() + SDCardCtrl.WALLERPATH;
                    KeyStore keyStore = new KeyStore(root,Geth.StandardScryptN, Geth.StandardScryptP);
                    Accounts accounts = keyStore.getAccounts();
                    geth.Account account = null;
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
                    long nonce =  NextApplication.ec.getNonceAt(new Context(),new Address(fromAddress.getText().toString()),-1);
                    BigInt value = Geth.newBigInt(0);
                    BigInt gasLimitB = Geth.newBigInt(tokenGasLimit);
                    Context ctx = new Context();
                    String valueOld =  new BigDecimal(toValue.getText().toString()).multiply(ONE_ETHER).setScale(0,BigDecimal.ROUND_DOWN).toPlainString();//乘以一个以太坊
                    String valueHex  =  new BigDecimal(valueOld).toBigInteger().toString(16);//转为16进制
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
                   
                    String data = contractFunctionHex + toHex + valueHex;
                    byte[] srtbyte = Numeric.hexStringToByteArray(data);
                    Transaction transaction = Geth.newTransaction(nonce, new Address(contractAddress), value, gasLimitB, gasPrice,srtbyte);
                    keyStore.unlock(account, password);
                    transaction = keyStore.signTx(account, transaction, new BigInt(Constants.GLOBAL_SWITCH_OPEN ? 1 : 3));
                    NextApplication.ec.sendTransaction(ctx,transaction);
                    mHandler.sendEmptyMessage(2);
                }catch (Exception e) {
                    if(!TextUtils.isEmpty(e.getMessage()) && e.getMessage().contains("could not decrypt key with given passphrase")){
                        mHandler.sendEmptyMessage(1);
                        e.printStackTrace();
                    }
                    else{
                        Message message = Message.obtain();
                        message.obj = "Error";
                        message.what = 3;
                        mHandler.sendMessage(message);
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }


    /**send eth*/
    private void sendtransEth(final String password) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String root =  getFilesDir() + SDCardCtrl.WALLERPATH;
                    KeyStore keyStore = new KeyStore(root,Geth.StandardScryptN, Geth.StandardScryptP);
                    Accounts accounts = keyStore.getAccounts();
                    geth.Account account = null;
                    for (int i= 0 ; i < accounts.size(); i++){
                        if (accounts.get(i).getAddress().getHex().toLowerCase().equals(fromAddress.getText().toString().toLowerCase())){
                            account = accounts.get(i);
                            break;
                        }
                    }
                    if(account ==null)
                    {
                        Message message = Message.obtain();
                        message.obj = "Error";
                        message.what = 3;
                        mHandler.sendMessage(message);
                        return;
                    }
                    long nonce =  NextApplication.ec.getNonceAt(new Context(),new Address(fromAddress.getText().toString()),-1);
                    BigInt value = Geth.newBigInt(new BigDecimal(toValue.getText().toString()).multiply(ONE_ETHER).longValue());
                    BigInt gasLimitB = Geth.newBigInt(gasLimit);
                    Context ctx = new Context();
                    Transaction transaction = Geth.newTransaction(nonce, new Address(toAddress.getText().toString()), value, gasLimitB, gasPrice, null);
                    keyStore.unlock(account, password);
                    transaction = keyStore.signTx(account, transaction, new BigInt(Constants.GLOBAL_SWITCH_OPEN ? 1 : 3));
                    NextApplication.ec.sendTransaction(ctx,transaction);
                    mHandler.sendEmptyMessage(2);
                } catch (Exception e) {
                    if(!TextUtils.isEmpty(e.getMessage()) && e.getMessage().contains("could not decrypt key with given passphrase")){
                        mHandler.sendEmptyMessage(1);
                        e.printStackTrace();
                    }
                    else{
                        Message message = Message.obtain();
                        message.obj = "Error";
                        message.what = 3;
                        mHandler.sendMessage(message);
                        e.printStackTrace();
                    }
                }
            }
        }).start();

    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0://
                    if(sendtype == 1)//SMT
                    {
                        String currentGas = new BigDecimal(gasPrice.toString()).multiply(new BigDecimal(tokenGasLimit)).divide(ONE_ETHER,0,BigDecimal.ROUND_DOWN).toPlainString();
                        gas.setText(getString(R.string.eth_er,currentGas));
                    }
                    else{
                        String currentGas = new BigDecimal(gasPrice.toString()).multiply(new BigDecimal(gasLimit)).divide(ONE_ETHER,0,BigDecimal.ROUND_DOWN).toPlainString();
                        gas.setText(getString(R.string.eth_er,currentGas));
                    }
                    LoadingDialog.close();
                    break;
                case 1://
                    LoadingDialog.close();
                    showToast(getString(R.string.wallet_copy_pwd_error));
                    break;
                case 2://
                    LoadingDialog.close();
                    showToast(getString(R.string.trans_success));
                    finish();
                    break;
                case 3://
                    LoadingDialog.close();
                    String errMsg = (String)msg.obj;
                    showToast(errMsg);
                    break;
                case 4://
                    LoadingDialog.close();
                    showToast(getString(R.string.get_gas_error));
                    finish();
                    break;
                case 5://
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
}
