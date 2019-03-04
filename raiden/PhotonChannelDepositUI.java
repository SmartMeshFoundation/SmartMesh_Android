package com.lingtuan.firefly.raiden;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.network.NetRequestImpl;
import com.lingtuan.firefly.raiden.util.PhotonThreadPoolUtils;
import com.lingtuan.firefly.raiden.vo.PhotonChannelVo;
import com.lingtuan.firefly.spectrum.vo.StorableWallet;
import com.lingtuan.firefly.spectrum.vo.TokenVo;
import com.lingtuan.firefly.util.CustomDialogFragment;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.concurrent.Executors;

/**
 * Created  on 2018/1/26.
 * photon channel add ui
 */

public class PhotonChannelDepositUI extends BaseActivity {
    private TextView partner;//partner
    private TextView deposit;//deposit
    private TextView balance;//pay
    private TextView token;//token
    private TextView channelTokenName;
    private TextView channelAddNumber;//add number
    private TextView channelAdd;//to add
    private View closeKeyWord;
    
    private PhotonChannelVo channelVo;
    private StorableWallet storableWallet;
    private TokenVo mTokenVo;
    
    @Override
    protected void setContentView() {
        setContentView(R.layout.raiden_channel_add_layout);
        getPassData();
    }
    
    private void getPassData() {
        channelVo = (PhotonChannelVo) getIntent().getSerializableExtra("raidenChannelVo");
        storableWallet = (StorableWallet) getIntent().getSerializableExtra("storableWallet");
        mTokenVo = (TokenVo) getIntent().getSerializableExtra("tokenVo");
    }
    
    @Override
    protected void findViewById() {
        partner = (TextView) findViewById(R.id.partner);
        deposit = (TextView) findViewById(R.id.deposit);
        balance = (TextView) findViewById(R.id.balance);
        token = (TextView) findViewById(R.id.token);
        closeKeyWord = findViewById(R.id.closeKeyWord);
        channelTokenName = (TextView) findViewById(R.id.channel_token_name);
        channelAddNumber = (TextView) findViewById(R.id.channelAddNumber);
        channelAdd = (TextView) findViewById(R.id.channelAdd);
    }
    
    @Override
    protected void setListener() {
        channelAdd.setOnClickListener(this);
        closeKeyWord.setOnClickListener(this);
        channelAddNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                
            }
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                
            }
            
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    if (TextUtils.isEmpty(s.toString())){
                        channelAdd.setEnabled(false);
                        return;
                    }
                    String temp = s.toString();
                    double tempInt = Double.parseDouble(temp);
                    if (tempInt > 0){
                        channelAdd.setEnabled(true);
                    }else{
                        channelAdd.setEnabled(false);
                    }
                    if (mTokenVo == null){
                        return;
                    }
                    if (tempInt > mTokenVo.getTokenBalance()) {
                        channelAddNumber.setText(((int) mTokenVo.getTokenBalance()) + "");
                        return;
                    }
                    int posDot = temp.indexOf(".");//返回指定字符在此字符串中第一次出现处的索引
                    if (posDot <= 0) {//不包含小数点
                        if (temp.length() <= 8) {
                            return;//小于五位数直接返回
                        } else {
                            s.delete(8, 9);//大于8位数就删掉第9位（只会保留5位）
                            return;
                        }
                    }
                    if (temp.length() - posDot - 1 > 2) {//如果包含小数点
                        s.delete(posDot + 3, posDot + 4);//删除小数点后的第三位
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
    @Override
    protected void initData() {
        setTitle(getString(R.string.raiden_channel_add_title));
        getTokenBalance();
        if (mTokenVo != null) {
            balance.setText(String.format("%s", mTokenVo.getTokenBalance()));
            token.setText(mTokenVo.getTokenSymbol());
            channelTokenName.setText(mTokenVo.getTokenSymbol());
        }

        if (channelVo != null) {
            partner.setText(channelVo.getPartnerAddress());
            deposit.setText(channelVo.getBalance());
        }
    }
    
    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.channelAdd:
                channelAddBalanceMethod();
                break;
            case R.id.closeKeyWord:
                Utils.hiddenKeyBoard(PhotonChannelDepositUI.this);
                break;
        }
    }

    public void getTokenBalance() {
        LoadingDialog.show(this,"");
        if (mTokenVo == null){
            return;
        }
        String address = storableWallet.getPublicKey();
        if (!address.startsWith("0x")){
            address = "0x" + address;
        }
        NetRequestImpl.getInstance().getBalance(address, mTokenVo.getContactAddress(), new RequestListener() {
            @Override
            public void start() {

            }

            @Override
            public void success(JSONObject response) {
                LoadingDialog.close();
                JSONArray array = response.optJSONArray("data");
                if (array != null) {
                    for (int i = 0; i < array.length(); i++) {
                        TokenVo tokenVo = new TokenVo().parse(array.optJSONObject(i));
                        if (mTokenVo != null) {
                            if (TextUtils.equals(mTokenVo.getContactAddress(),tokenVo.getContactAddress())){
                                mTokenVo.setTokenBalance(tokenVo.getTokenBalance());
                                balance.setText(String.format("%s", mTokenVo.getTokenBalance()));
                                break;
                              }
                        }
                    }
                }
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                LoadingDialog.close();
            }
        });
    }

    /**
     * partnerAddress 	string 	partner_address 	通道对方地址
     * tokenAddress 	string 	token_address 	哪种token
     * settleTimeout 	string 	settle_timeout 	通道结算时间 存款为0
     * balanceStr 	big.Int 	balance 	存入金额，一定大于0
     * newChannel 	bool 	new_channel 	判断通道是否存在，决定此次行为是创建通道并存款还是只存款  false为存钱
     * */
    private void channelAddBalanceMethod() {
        final String channelNumber = channelAddNumber.getText().toString().trim();
        float tempValue;
        try {
            tempValue = Float.parseFloat(channelNumber);
        }catch (Exception e){
            e.printStackTrace();
            tempValue = 0;
        }
        if (TextUtils.isEmpty(channelNumber) || tempValue <= 0) {
            return;
        }

        CustomDialogFragment customDialogFragment = new CustomDialogFragment(CustomDialogFragment.DIALOG_CUSTOM_TITLE_CENTER);
        customDialogFragment.setHindCancelButton(true);
        customDialogFragment.setTitle(getString(R.string.dialog_prompt));
        customDialogFragment.setConfirmButton(getString(R.string.ok));
        customDialogFragment.setContent(getString(R.string.dialog_deposit_content));
        customDialogFragment.setPublicButtonListener(new CustomDialogFragment.onPublicButtonListener() {
            @Override
            public void cancel() {

            }

            @Override
            public void submit() {
                depositMethod(channelNumber);
            }
        });
        customDialogFragment.show(getSupportFragmentManager(),"mdf");
    }

    /**
     * 存款方法
     * @param channelNumber  存款金额
     * */
    private void depositMethod(String channelNumber){
        try {
            if (mHandler != null){
                mHandler.sendEmptyMessage(2);
            }
            PhotonThreadPoolUtils.getInstance().getPhotonThreadPool().execute(() -> {
                try {
                    if (NextApplication.api != null) {
                        BigDecimal ONE_ETHER = new BigDecimal("1000000000000000000");
                        String balance = new BigDecimal(channelNumber).multiply(ONE_ETHER).toBigInteger().toString();
                        String response = NextApplication.api.deposit(channelVo.getPartnerAddress(), NextApplication.photonTokenAddress, 0, balance,false);
                        try {
                            JSONObject object = new JSONObject(response);
                            int errorCode = object.optInt("error_code");
                            if (errorCode == 0){
                                if (mHandler != null){
                                    mHandler.sendEmptyMessage(0);
                                }
                            }else{
                                String errorMessage = object.optString("error_message");
                                if (TextUtils.isEmpty(errorMessage)){
                                    if (mHandler != null){
                                        mHandler.sendEmptyMessage(3);
                                    }
                                }else{
                                    if (mHandler != null){
                                        Message message = Message.obtain();
                                        message.obj = errorMessage;
                                        message.what = 4;
                                        mHandler.sendMessage(message);
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            if (mHandler != null){
                                mHandler.sendEmptyMessage(3);
                            }
                        }
                    }else{
                        if (mHandler != null){
                            mHandler.sendEmptyMessage(3);
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    if (mHandler != null){
                        mHandler.sendEmptyMessage(3);
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
            if (mHandler != null){
                mHandler.sendEmptyMessage(3);
            }
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    LoadingDialog.close();
                    intoContractQueryUI();
                    break;
                case 1:
                    LoadingDialog.close();
                    //Send to refresh the page
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    finish();
                    break;
                case 2:
                    LoadingDialog.show(PhotonChannelDepositUI.this, getString(R.string.raiden_channel_create_enter_add));
                    break;
                case 3:
                    LoadingDialog.close();
                    showToast(getString(R.string.raiden_channel_create_enter_add_error));
                    finish();
                    break;
                case 4:
                    LoadingDialog.close();
                    showToast((String) msg.obj);
                    finish();
                    break;
            }
        }
    };

    /**
     * 合约交易列表
     * */
    private void intoContractQueryUI(){
        Intent intent = new Intent(this,PhotonTransferQueryUI.class);
        intent.putExtra("showContract",true);
        intent.putExtra("fromType",2);
        startActivity(intent);
        Utils.openNewActivityAnim(this,true);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
