package com.lingtuan.firefly.raiden;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.quickmark.CaptureActivity;
import com.lingtuan.firefly.raiden.adapter.ChannelListAdapter;
import com.lingtuan.firefly.raiden.contract.PhotonTransferContract;
import com.lingtuan.firefly.raiden.presenter.PhotonTransferPresenterImpl;
import com.lingtuan.firefly.raiden.vo.RaidenChannelVo;
import com.lingtuan.firefly.spectrum.vo.StorableWallet;
import com.lingtuan.firefly.spectrum.vo.TokenVo;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.MyToast;
import com.lingtuan.firefly.util.MyViewDialogFragment;
import com.lingtuan.firefly.util.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import butterknife.OnLongClick;
import butterknife.OnTextChanged;

/**
 * Created on 2018/1/26.
 * raiden transfer ui
 */

public class PhotonTransferUI extends BaseActivity implements PhotonTransferContract.View {

    @BindView(R.id.channelPay)
    TextView channelPay;
    @BindView(R.id.toValue)
    EditText toValue;
    @BindView(R.id.address_spinner)
    Spinner mSpinner;
    @BindView(R.id.showText)
    EditText mShowText;
    @BindView(R.id.useOfflinePay)
    TextView useOfflinePay;
    @BindView(R.id.photonScan)
    ImageView channelQrImg;
    @BindView(R.id.photonCreate)
    ImageView photonCreate;
    @BindView(R.id.photonList)
    ImageView photonList;
    @BindView(R.id.raidenXMPP)
    ImageView mXmppStatus;
    @BindView(R.id.raidenETH)
    ImageView mEthStatus;

    private static int RAIDEN_CHANNEL_CREATE = 100;

    private ChannelListAdapter mAdapter;
    private ArrayList<RaidenChannelVo> channelList = new ArrayList<>();
    private RaidenChannelVo tempChannelVo;

    private StorableWallet storableWallet;
    private TokenVo mTokenVo;

    private PhotonTransferContract.Presenter mPresenter;

    private int timeOut = 0;

    @Override
    protected void setContentView() {
        setContentView(R.layout.raiden_transfer_layout);
        getPassData();
    }

    private void getPassData() {
        storableWallet = (StorableWallet) getIntent().getSerializableExtra("storableWallet");
        mTokenVo = (TokenVo) getIntent().getSerializableExtra("tokenVo");
    }
    
    @Override
    protected void findViewById() {

    }
    
    @Override
    protected void setListener() {

    }

    @Override
    protected void initData() {
        new PhotonTransferPresenterImpl(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(RaidenUrl.ACTION_RAIDEN_CONNECTION_STATE);
        registerReceiver(receiver, filter);

        setTitle(getString(R.string.send_blance));
        mXmppStatus.setVisibility(View.VISIBLE);
        mEthStatus.setVisibility(View.VISIBLE);
        mAdapter = new ChannelListAdapter(this, channelList);
        mSpinner.setAdapter(mAdapter);
        mPresenter.setPhotonStatus(mEthStatus,mXmppStatus);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && (RaidenUrl.ACTION_RAIDEN_CONNECTION_STATE.equals(intent.getAction()))) {
                mPresenter.setPhotonStatus(mEthStatus,mXmppStatus);
            }
        }
    };

    /**
     * parse json
     * @param jsonString response string
     */
    private void parseJson(String jsonString) {
        LoadingDialog.close();
        if (TextUtils.isEmpty(jsonString) || "null".equals(jsonString)) {
            channelList.clear();
            mAdapter.resetSource(channelList);
            mSpinner.performClick();
            return;
        }
        try {
            channelList.clear();
            JSONArray array = new JSONArray(jsonString);
            if (array.length() > 0) {
                for (int i = 0; i < array.length(); i++) {
                    JSONObject object = array.optJSONObject(i);
                    RaidenChannelVo channelVo = new RaidenChannelVo().parse(object);
                    channelList.add(channelVo);
                }
            }
            mAdapter.resetSource(channelList);
            mSpinner.performClick();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnItemSelected(value = R.id.address_spinner , callback = OnItemSelected.Callback.ITEM_SELECTED)
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
        if (channelList != null && channelList.size() > 0){
            String address = channelList.get(position).getPartnerAddress();
            tempChannelVo = channelList.get(position);
            if (TextUtils.isEmpty(mShowText.getText().toString())){
                mSpinner.performClick();
            }
            mShowText.setText(address);
            tokenAddressRequest(address);
        }
    }

    @OnTextChanged(value = R.id.toValue,callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void afterTextChanged(Editable s){
        try {
            String temp = s.toString();
            int tempInt = Integer.parseInt(temp);
            int ban = Integer.parseInt(tempChannelVo.getBalance());
            if (tempInt > ban) {
                toValue.setText(tempChannelVo.getBalance());
                return;
            }
            int posDot = temp.indexOf(".");
            if (posDot <= 0) {
                if (temp.length() <= 8) {
                    return;
                } else {
                    s.delete(8, 9);
                    return;
                }
            }
            if (temp.length() - posDot - 1 > 2) {
                s.delete(posDot + 3, posDot + 4);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnLongClick({R.id.app_title})
    public boolean onLongClickView(View view){
        boolean isXmppDefault = NextApplication.mRaidenStatusVo != null && RaidenStatusType.Default == NextApplication.mRaidenStatusVo.getXMPPStatus();
        boolean isEthDefault = NextApplication.mRaidenStatusVo != null && RaidenStatusType.Default == NextApplication.mRaidenStatusVo.getEthStatus();
        if (isXmppDefault && isEthDefault){
            return true;
        }
        MyViewDialogFragment mdf = new MyViewDialogFragment(MyViewDialogFragment.DIALOG_HINT_INPUT_WS_IP, (wsUrl, password) -> {
            if (!TextUtils.isEmpty(wsUrl)) {
                String resultStr = wsUrl;
                if (wsUrl.startsWith("ws")) {
                    resultStr = wsUrl;
                } else {
                    resultStr = "ws://" + wsUrl;
                }
                MySharedPrefs.write(PhotonTransferUI.this, MySharedPrefs.FILE_USER, MySharedPrefs.RAIDEN_WS, resultStr);
                mPresenter.checkWalletExist(PhotonTransferUI.this,password,storableWallet.getPublicKey());
            }
        });
        mdf.show(getSupportFragmentManager(), "mdf");
        return false;
    }
    
    @OnClick({R.id.channelPay,R.id.down_flg,R.id.photonScan,R.id.photonCreate,R.id.photonList})
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.channelPay:
                String amount = toValue.getText().toString().trim();
                String address = mShowText.getText().toString();
                mPresenter.photonTransferMethod(amount,address);
                break;
            case R.id.down_flg:
                if (channelList != null && channelList.size() > 0){
                    mSpinner.performClick();
                }else{
                    LoadingDialog.show(this,"");
                    mPresenter.loadChannelList();
                }
                break;
            case R.id.photonScan:
                Intent photonScan = new Intent(this, CaptureActivity.class);
                photonScan.putExtra("type", 1);
                startActivityForResult(photonScan, 100);
                break;
            case R.id.photonCreate:
                Intent intent = new Intent(this, RaidenCreateChannel.class);
                intent.putExtra("storableWallet", storableWallet);
                intent.putExtra("tokenVo", mTokenVo);
                startActivityForResult(intent, RAIDEN_CHANNEL_CREATE);
                Utils.openNewActivityAnim(this, false);
                break;
            case R.id.photonList:
                Intent photonIntent = new Intent(PhotonTransferUI.this, RaidenChannelList.class);
                photonIntent.putExtra("storableWallet", storableWallet);
                photonIntent.putExtra("tokenVo", mTokenVo);
                startActivity(photonIntent);
                Utils.openNewActivityAnim(PhotonTransferUI.this, false);
                break;
        }
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == RAIDEN_CHANNEL_CREATE){
                mPresenter.loadChannelList();
            }else{
                String address = data.getStringExtra("address");
                mShowText.setText(address);
                tokenAddressRequest(address);
            }
        }
    }

    private void inquiryTransferStatus(final String lockSecretHash){
        NextApplication.mRaidenThreadPool.execute(() -> checkChannelState(lockSecretHash));
    }

    private void checkChannelState(String lockSecretHash){
        try {
            Thread.sleep(2000);
            if (timeOut > 40){
                timeOut = 0;
                mHandler.sendEmptyMessage(2);
            }else{
                String message =  NextApplication.api.getTransferStatus(RaidenUrl.MESH_TOKEN_ADDRESS,lockSecretHash);
                timeOut ++;
                if (!message.contains("交易成功")){
                    checkChannelState(lockSecretHash);
                }else{
                    LoadingDialog.close();
                    MyToast.showToast(PhotonTransferUI.this,getResources().getString(R.string.raiden_transfer_success));
                    finish();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LoadingDialog.close();
        }
    }

    //切换地址请求接口告知服务器
    private void tokenAddressRequest(String address){
        if (NextApplication.raidenSwitch || !NextApplication.netWorkOnline){
            boolean hasFound = mPresenter.checkTokenStatus(address);
            if (hasFound){
                mPresenter.tokenAddressRequest(address.toLowerCase());
            }else{
                useOfflinePay.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void uploadTokenSuccess(JSONObject response) {
        useOfflinePay.setVisibility(View.VISIBLE);
    }

    @Override
    public void uploadTokenError(int errorCode, String errorMsg) {
        useOfflinePay.setVisibility(View.GONE);
    }

    @Override
    public void loadChannelSuccess(String jsonString) {
        if(mHandler != null){
            Message mes = new Message();
            mes.what = 4;
            mes.obj = jsonString;
            mHandler.sendMessage(mes);
        }
    }

    @Override
    public void loadChannelError() {
        if(mHandler != null){
            mHandler.sendEmptyMessage(3);
        }
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
    public void transferLockSecretHash(String lockSecretHash) {
        if(mHandler != null){
            Message message = Message.obtain();
            message.obj = lockSecretHash;
            message.what = 1;
            mHandler.sendMessage(message);
        }
    }

    @Override
    public void transferError() {
        if(mHandler != null){
            mHandler.sendEmptyMessage(2);
        }
    }

    @Override
    public void setPresenter(PhotonTransferContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    LoadingDialog.show(PhotonTransferUI.this, "");
                    break;
                case 1:
                    String lockSecretHash = (String) msg.obj;
                    inquiryTransferStatus(lockSecretHash);
                    break;
                case 2://fail
                    LoadingDialog.close();
                    MyViewDialogFragment mdf = new MyViewDialogFragment();
                    mdf.setTitleAndContentText(getString(R.string.transfer_error),null);
                    mdf.show(getSupportFragmentManager(), "mdf");
                    break;
                case 3:
                    showToast(getString(R.string.error_get_raiden_list));
                    LoadingDialog.close();
                    break;
                case 4:
                    parseJson((String) msg.obj);
                    break;
                case 5://Get the success of the next step
                    LoadingDialog.close();
                    RaidenNet.getInatance().startPhotonServer((String) msg.obj);
                    break;
                case 6://Enter the wrong password
                    LoadingDialog.close();
                    showToast(getString(R.string.wallet_copy_pwd_error));
                    break;
            }
        }
    };

}
