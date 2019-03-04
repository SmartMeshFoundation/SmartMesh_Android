package com.lingtuan.firefly.raiden;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.quickmark.CaptureActivity;
import com.lingtuan.firefly.raiden.contract.PhotonCreateContract;
import com.lingtuan.firefly.raiden.presenter.PhotonCreatePresenterImpl;
import com.lingtuan.firefly.raiden.util.ChannelNoteUtils;
import com.lingtuan.firefly.raiden.util.PhotonPayUtils;
import com.lingtuan.firefly.spectrum.vo.StorableWallet;
import com.lingtuan.firefly.spectrum.vo.TokenVo;
import com.lingtuan.firefly.util.CustomDialogFragment;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MyToast;
import com.lingtuan.firefly.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;

/**
 * Created on 2018/1/24.
 * cheate raiden channel ui
 */

public class PhotonCreateChannel extends BaseActivity implements PhotonCreateContract.View {

    @BindView(R.id.partner)
    EditText partner;//partner
    @BindView(R.id.deposit)
    EditText deposit;//deposit
    @BindView(R.id.balance)
    TextView balance;//balance
    @BindView(R.id.tokenType)
    TextView mTokenType;
    @BindView(R.id.channelNote)
    EditText channelNote;
    @BindView(R.id.app_right)
    ImageView channelQrImg;
    @BindView(R.id.channelEnter)
    TextView channelEnter;

    private StorableWallet storableWallet;
    private TokenVo mTokenVo;
    private int fromType;//0转账页面 1通道列表页面

    private PhotonCreateContract.Presenter mPresenter;

    @Override
    protected void setContentView() {
        setContentView(R.layout.raiden_channel_create);
        getPassData();
    }

    private void getPassData() {
        storableWallet = (StorableWallet) getIntent().getSerializableExtra("storableWallet");
        mTokenVo = (TokenVo) getIntent().getSerializableExtra("tokenVo");
        fromType = getIntent().getIntExtra("fromType",-1);
    }

    @Override
    protected void findViewById() {

    }

    @Override
    protected void setListener() {

    }

    @OnFocusChange({R.id.deposit})
    public void onFocusChange(View v, boolean hasFocus) {
        final String depositBalance = deposit.getText().toString();
        if (hasFocus) {
            if (TextUtils.isEmpty(depositBalance)) {
                if (mTokenVo != null){
                    deposit.setHint(getString(R.string.raiden_create_balance, String.valueOf(mTokenVo.getTokenBalance())));
                }else{
                    deposit.setHint(getString(R.string.set_amount));
                }

            }
        } else {
            if (TextUtils.isEmpty(depositBalance)) {
                deposit.setHint(getString(R.string.set_amount));
            }
        }
    }

    @OnTextChanged(value = R.id.deposit,callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void afterTextChanged(Editable s){
        try {
            if (mTokenVo != null){
                mPresenter.checkDepositValue(s, (int) mTokenVo.getTokenBalance(),deposit);
            }
            checkEnableCreate();
        } catch (Exception e) {
            e.printStackTrace();
            checkEnableCreate();
        }
    }

    @OnTextChanged(value = R.id.partner,callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void afterPartnerTextChanged(Editable s){
        checkEnableCreate();
    }

    private void checkEnableCreate(){
        try {
            String depositValue = deposit.getText().toString();
            String partnerValue = partner.getText().toString();
            if (TextUtils.isEmpty(depositValue) || TextUtils.isEmpty(partnerValue)){
                channelEnter.setEnabled(false);
            }else{
                channelEnter.setEnabled(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initData() {
        new PhotonCreatePresenterImpl(this);
        setTitle(getString(R.string.raiden_channel_create));
        if (mTokenVo != null){
            mTokenType.setText(mTokenVo.getTokenSymbol());
        }
        if (storableWallet != null) {
            if (storableWallet.getFftBalance() > 0) {
                balance.setText(getString(R.string.smt_er, String.valueOf(storableWallet.getFftBalance())));
            } else {
                balance.setText(getString(R.string.smt_er, String.valueOf(storableWallet.getFftBalance())));
            }
        }

        if (NextApplication.payEntity != null){
            channelQrImg.setVisibility(View.GONE);
            partner.setEnabled(false);
            deposit.setEnabled(false);
            partner.setText(NextApplication.payEntity.getToAddress());
            deposit.setText(NextApplication.payEntity.getTotal());
            channelNote.requestFocus();
        }else{
            channelQrImg.setVisibility(View.VISIBLE);
            channelQrImg.setImageResource(R.drawable.scan_black);
        }
    }

    @OnClick({R.id.channelEnter,R.id.app_right,R.id.closeKeyWord})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.channelEnter:
                createChannel();
                break;
            case R.id.app_right:
                Intent i = new Intent(PhotonCreateChannel.this, CaptureActivity.class);
                i.putExtra("type", 1);
                startActivityForResult(i, 100);
                break;
            case R.id.closeKeyWord:
                Utils.hiddenKeyBoard(PhotonCreateChannel.this);
                break;
            default:
                super.onClick(v);
                break;

        }
    }

    /**
     * 创建通道
     * */
    private void createChannel(){
        String partnerAddress = partner.getText().toString();
        String depositBalance = deposit.getText().toString();
        if (TextUtils.isEmpty(depositBalance) || TextUtils.isEmpty(partnerAddress)) {
            MyToast.showToast(PhotonCreateChannel.this, getResources().getString(R.string.raiden_open_channel_error));
            return;
        }
        float tempValue;
        try {
            tempValue = Float.parseFloat(depositBalance);
        } catch (Exception e) {
            e.printStackTrace();
            tempValue = 0;
        }
        if (tempValue <= 0) {
            MyToast.showToast(PhotonCreateChannel.this, getResources().getString(R.string.raiden_open_channel_error));
            return;
        }
        CustomDialogFragment customDialogFragment = new CustomDialogFragment(CustomDialogFragment.DIALOG_CUSTOM_TITLE_CENTER);
        customDialogFragment.setHindCancelButton(true);
        customDialogFragment.setTitle(getString(R.string.dialog_prompt));
        customDialogFragment.setConfirmButton(getString(R.string.ok));
        customDialogFragment.setContent(getString(R.string.dialog_create_content));
        customDialogFragment.setPublicButtonListener(new CustomDialogFragment.onPublicButtonListener() {
            @Override
            public void cancel() {

            }

            @Override
            public void submit() {
                mPresenter.createChannelMethod(partnerAddress,depositBalance);
            }
        });
        customDialogFragment.show(getSupportFragmentManager(),"mdf");
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            String address = data.getStringExtra("address");
            partner.setText(address);
            partner.setSelection(partner.getText().length());
        }
    }

    @Override
    public void setPresenter(PhotonCreateContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public void createChannelStart() {
        mHandler.sendEmptyMessage(0);
    }

    @Override
    public void createChannelSuccess(String response) {
        try {
            JSONObject object = new JSONObject(response);
            int errorCode = object.optInt("error_code");
            if (errorCode == 0){
                if (mHandler != null){
                    mHandler.sendEmptyMessage(1);
                }
            }else{
                String errorMessage = object.optString("error_message");
                if (TextUtils.isEmpty(errorMessage)){
                    if (mHandler != null){
                        mHandler.sendEmptyMessage(2);
                    }
                }else{
                    if (mHandler != null){
                        Message message = Message.obtain();
                        message.obj = errorMessage;
                        message.what = 3;
                        mHandler.sendMessage(message);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            if (mHandler != null){
                mHandler.sendEmptyMessage(2);
            }
        }
    }

    @Override
    public void createChannelError() {
        if (mHandler != null){
            mHandler.sendEmptyMessage(2);
        }
    }

    @Override
    public void photonNotStart() {
        if (mHandler != null){
            mHandler.sendEmptyMessage(4);
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    LoadingDialog.show(PhotonCreateChannel.this, getString(R.string.raiden_channel_create_enter_ing));
                    break;
                case 1:
                    LoadingDialog.close();
                    ChannelNoteUtils.insertChannelNote(storableWallet.getPublicKey(),partner.getText().toString(),channelNote.getText().toString());
                    MyToast.showToast(PhotonCreateChannel.this, getResources().getString(R.string.raiden_open_channel_success));
                    if (NextApplication.payEntity != null){
                        PhotonPayUtils.insertPayAddress(NextApplication.payEntity.getToAddress(),NextApplication.payEntity.getTotal());
                    }
                    intoContractQueryUI();
                    break;
                case 2:
                    LoadingDialog.close();
                    MyToast.showToast(PhotonCreateChannel.this, getResources().getString(R.string.raiden_open_channel_error));
                    break;
                case 3:
                    LoadingDialog.close();
                    String errorMessage = (String) msg.obj;
                    MyToast.showToast(PhotonCreateChannel.this, errorMessage);
                    break;
                case 4:
                    LoadingDialog.close();
                    showToast(getString(R.string.photon_restart));
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
        intent.putExtra("fromType",fromType);
        intent.putExtra("storableWallet", storableWallet);
        intent.putExtra("tokenVo", mTokenVo);
        startActivity(intent);
        Utils.openNewActivityAnim(this,true);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
