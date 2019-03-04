package com.lingtuan.firefly.raiden;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.raiden.contract.PhotonTransferForNetContract;
import com.lingtuan.firefly.raiden.presenter.PhotonTransferForNetPresenterImpl;
import com.lingtuan.firefly.raiden.util.PhotonStartUtils;
import com.lingtuan.firefly.spectrum.vo.StorableWallet;
import com.lingtuan.firefly.util.CustomDialogFragment;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.MyViewDialogFragment;
import com.lingtuan.firefly.util.Utils;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnLongClick;

/**
 * Created on 2018/2/21.
 * 光子上网支付页面
 */

public class PhotonTransferForNetUI extends BaseActivity implements PhotonTransferForNetContract.View {

    @BindView(R.id.toValue)
    TextView toValue;
    @BindView(R.id.showText)
    TextView mShowText;
    @BindView(R.id.raidenXMPP)
    ImageView mXmppStatus;
    @BindView(R.id.raidenETH)
    ImageView mEthStatus;

    private String toAddress;
    private StorableWallet storableWallet;

    private PhotonTransferForNetContract.Presenter mPresenter;

    @Override
    protected void setContentView() {
        setContentView(R.layout.raiden_transfer_for_net_layout);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.setPhotonStatus(mEthStatus,mXmppStatus);
    }

    @Override
    protected void findViewById() {

    }
    
    @Override
    protected void setListener() {

    }

    @Override
    protected void initData() {
        new PhotonTransferForNetPresenterImpl(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(PhotonUrl.ACTION_RAIDEN_CONNECTION_STATE);
        registerReceiver(receiver, filter);
        storableWallet = mPresenter.getStorableWallet();
        setTitle(getString(R.string.send_blance));
        mXmppStatus.setVisibility(View.VISIBLE);
        mEthStatus.setVisibility(View.VISIBLE);
        mPresenter.setPhotonStatus(mEthStatus,mXmppStatus);

        if (NextApplication.payEntity != null){
            toValue.setText(NextApplication.payEntity.getTotal());
            mShowText.setText(NextApplication.payEntity.getToAddress());
            toAddress = NextApplication.payEntity.getToAddress();
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && (PhotonUrl.ACTION_RAIDEN_CONNECTION_STATE.equals(intent.getAction()))) {
                mPresenter.setPhotonStatus(mEthStatus,mXmppStatus);
            }
        }
    };

    @OnLongClick({R.id.app_title})
    public boolean onLongClickView(View view){
        boolean isXmppDefault = NextApplication.mRaidenStatusVo != null && RaidenStatusType.Default == NextApplication.mRaidenStatusVo.getXMPPStatus();
        boolean isEthDefault = NextApplication.mRaidenStatusVo != null && RaidenStatusType.Default == NextApplication.mRaidenStatusVo.getEthStatus();
        if (isXmppDefault && isEthDefault){
            return true;
        }
        MyViewDialogFragment mdf = new MyViewDialogFragment(MyViewDialogFragment.DIALOG_HINT_INPUT_WS_IP, (wsUrl, password) -> {
            if (!TextUtils.isEmpty(wsUrl)) {
                MySharedPrefs.write(PhotonTransferForNetUI.this, MySharedPrefs.FILE_USER, MySharedPrefs.PHOTON_ETH_RPC_END_POINT, wsUrl);
                mPresenter.checkWalletExist(PhotonTransferForNetUI.this,password,storableWallet.getPublicKey());
            }
        });
        mdf.setTitleAndContentText(getString(R.string.photon_start_up),"");
        mdf.show(getSupportFragmentManager(), "mdf");
        return false;
    }
    
    @OnClick({R.id.channelPay,R.id.closeKeyWord})
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.channelPay:
                sendTransferMethod();
                break;
            case R.id.closeKeyWord:
                Utils.hiddenKeyBoard(PhotonTransferForNetUI.this);
                break;
        }
    }

    /**
     * 转账相关
     * */
    private void sendTransferMethod(){
        String amount = toValue.getText().toString().trim();
        if (TextUtils.isEmpty(amount)){
            showToast(getString(R.string.input_vaule));
            return;
        }
        if (NextApplication.raidenSwitch){
            CustomDialogFragment customDialogFragment = new CustomDialogFragment(CustomDialogFragment.DIALOG_CUSTOM_TITLE_CENTER);
            customDialogFragment.setHindCancelButton(true);
            customDialogFragment.setTitle(getString(R.string.dialog_prompt));
            customDialogFragment.setConfirmButton(getString(R.string.ok));
            customDialogFragment.setContent(getString(R.string.photon_channel_mesh_pay_3));
            customDialogFragment.show(getSupportFragmentManager(),"mdf");
        }else{
            mPresenter.photonTransferMethod(amount,"0",toAddress,true,false);
        }
    }

    /**
     * 转账正常接收到数据
     * 此时可能成功也可能失败
     * 不再等待，直接进入channel list 页面
     * */
    private void inquiryTransferStatus(final String jsonString) {
        try {
            LoadingDialog.close();
            NextApplication.payEntity = null;
            JSONObject object = new JSONObject(jsonString);
            int errorCode = object.optInt("error_code");
            if (errorCode == 0) {
                showToast(getResources().getString(R.string.raiden_transfer_success));
                finish();
            }else{
                showToast(object.optString("error_message"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            LoadingDialog.close();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        NextApplication.payEntity = null;
    }

    @Override
    public void checkWalletExistSuccess(String walletPwd) {
        Message message = Message.obtain();
        message.what = 5;
        message.obj = walletPwd;
        if(mHandler != null){
            mHandler.sendMessage(message);
        }
    }

    @Override
    public void checkWalletExistError() {
        if(mHandler != null){
            mHandler.sendEmptyMessage(6);
        }
    }

    @Override
    public void transferCheck() {
        if(mHandler != null){
            mHandler.sendEmptyMessage(0);
        }
    }

    @Override
    public void transferSuccess(String jsonString) {
        NextApplication.payEntity = null;
        if(mHandler != null){
            Message message = Message.obtain();
            message.obj = jsonString;
            message.what = 1;
            mHandler.sendMessage(message);
        }
    }

    @Override
    public void transferError() {
        NextApplication.payEntity = null;
        if(mHandler != null){
            mHandler.sendEmptyMessage(2);
        }
    }

    @Override
    public void setPresenter(PhotonTransferForNetContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    LoadingDialog.show(PhotonTransferForNetUI.this, getString(R.string.photon_trans_ing));
                    break;
                case 1:
                    String jsonString = (String) msg.obj;
                    inquiryTransferStatus(jsonString);
                    break;
                case 2://fail
                    LoadingDialog.close();
                    if (NextApplication.api == null){
                        showToast(getString(R.string.photon_restart));
                    }else{
                        MyViewDialogFragment mdf = new MyViewDialogFragment();
                        mdf.setTitleAndContentText(getString(R.string.transfer_error),null);
                        mdf.show(getSupportFragmentManager(), "mdf");
                    }
                    break;
                case 3:
                    showToast(getString(R.string.error_get_raiden_list_1));
                    LoadingDialog.close();
                    break;
                case 5://Get the success of the next step
                    LoadingDialog.close();
                    String ethRPCEndPoint = MySharedPrefs.readString(NextApplication.mContext, MySharedPrefs.FILE_USER, MySharedPrefs.PHOTON_ETH_RPC_END_POINT);
                    PhotonStartUtils.getInstance().startPhotonServer((String) msg.obj,ethRPCEndPoint);
//                    Bundle startPhotonBundle = new Bundle();
//                    startPhotonBundle.putString(PhotonService.START_PHOTON_PASSWORD,(String) msg.obj);
//                    startPhotonBundle.putString(PhotonService.START_PHOTON_URL,ethRPCEndPoint);
//                    Utils.intentService(
//                            getApplicationContext(),
//                            PhotonService.class,
//                            PhotonService.ACTION_PHOTON_START,
//                            PhotonService.ACTION_PHOTON_START,
//                            startPhotonBundle);
                    break;
                case 6://Enter the wrong password
                    LoadingDialog.close();
                    showToast(getString(R.string.wallet_copy_pwd_error));
                    break;
                case 7:
                    LoadingDialog.close();
                    break;
            }
        }
    };

}
