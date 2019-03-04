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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.quickmark.CaptureActivity;
import com.lingtuan.firefly.raiden.adapter.ChannelListAdapter;
import com.lingtuan.firefly.raiden.contract.PhotonTransferContract;
import com.lingtuan.firefly.raiden.presenter.PhotonTransferPresenterImpl;
import com.lingtuan.firefly.raiden.util.ChannelNoteUtils;
import com.lingtuan.firefly.raiden.util.PhotonStartUtils;
import com.lingtuan.firefly.raiden.vo.PhotonChannelVo;
import com.lingtuan.firefly.raiden.vo.PhotonFeeEntity;
import com.lingtuan.firefly.spectrum.vo.StorableWallet;
import com.lingtuan.firefly.spectrum.vo.TokenVo;
import com.lingtuan.firefly.util.CustomDialogFragment;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.MyViewDialogFragment;
import com.lingtuan.firefly.util.Utils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.web3j.utils.Convert;

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

    @BindView(R.id.useOfflinePay)
    TextView useOfflinePay;
    @BindView(R.id.refreshOfflinePay)
    TextView refreshOfflinePay;
    @BindView(R.id.channelPay)
    TextView channelPay;
    @BindView(R.id.toValue)
    EditText toValue;
    @BindView(R.id.address_spinner)
    Spinner mSpinner;
    @BindView(R.id.showText)
    TextView mShowText;
    @BindView(R.id.showTextAddress)
    TextView showTextAddress;
    @BindView(R.id.showNoteBody)
    LinearLayout showNoteBody;
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

    @BindView(R.id.getChannel)
    TextView getChannel;
    @BindView(R.id.down_flg)
    ImageView down_flg;
    @BindView(R.id.walletAddressDelete)
    ImageView walletAddressDelete;

    private String toAddress;

    private String scanResultAddress = "";

    private static int RAIDEN_CHANNEL_CREATE = 1001;

    private ChannelListAdapter mAdapter;
    private ArrayList<PhotonChannelVo> channelList = new ArrayList<>();
    private PhotonChannelVo tempChannelVo;

    private StorableWallet storableWallet;
    private TokenVo mTokenVo;

    private PhotonTransferContract.Presenter mPresenter;

    private boolean notShowSpinner;

    private String sendAmount = "-1";

    @Override
    protected void setContentView() {
        setContentView(R.layout.raiden_transfer_layout);
        getPassData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (toAddress != null && !TextUtils.isEmpty(toAddress)){
            tokenAddressRequestMethod(toAddress);
        }
        if (toValue != null){
            toValue.setText("");
            toValue.setHint(getString(R.string.set_amount));
        }
        mPresenter.loadChannelList(false);
        mPresenter.setPhotonStatus(mEthStatus,mXmppStatus);
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
        filter.addAction(PhotonUrl.ACTION_RAIDEN_CONNECTION_STATE);
        filter.addAction(PhotonUrl.ACTION_PHOTON_RECEIVER_TRANSFER);
        filter.addAction(PhotonUrl.ACTION_PHOTON_SENT_TRANSFER);
        registerReceiver(receiver, filter);

        setTitle(getString(R.string.send_blance));
        mXmppStatus.setVisibility(View.VISIBLE);
        mEthStatus.setVisibility(View.VISIBLE);
        mAdapter = new ChannelListAdapter(this, channelList);
        mSpinner.setAdapter(mAdapter);
        checkChannelEmpty();
        mPresenter.setPhotonStatus(mEthStatus,mXmppStatus);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && (PhotonUrl.ACTION_RAIDEN_CONNECTION_STATE.equals(intent.getAction()))) {
                mPresenter.setPhotonStatus(mEthStatus,mXmppStatus);
            }else if (intent != null && (PhotonUrl.ACTION_PHOTON_RECEIVER_TRANSFER.equals(intent.getAction()))) {
                mPresenter.loadChannelList(false);
            }
        }
    };

    /**
     * parse json
     * @param jsonString response string
     */
    private void parseJson(String jsonString,int showToast) {
        LoadingDialog.close();
        if (TextUtils.isEmpty(jsonString) || "null".equals(jsonString)) {
            channelList.clear();
            mAdapter.resetSource(channelList);
            if (mSpinner != null && mSpinner.isActivated()){
                mSpinner.performClick();
            }
            checkChannelEmpty();
            if (showToast == 1){
                showToast(getString(R.string.raiden_channel_no_use));
            }
            return;
        }
        try {
            channelList.clear();
            JSONObject object = new JSONObject(jsonString);
            int errorCode = object.optInt("error_code");
            if (errorCode == 0){
                JSONArray array = object.optJSONArray("data");
                if (array != null && array.length() > 0) {
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject dataObject = array.optJSONObject(i);
                        PhotonChannelVo channelVo = new PhotonChannelVo().parse(dataObject);
                        if (channelVo.getState() == 1){
                            if (mTokenVo != null && TextUtils.equals(NextApplication.photonTokenAddress.toLowerCase(),channelVo.getTokenAddress().toLowerCase())){
                                channelList.add(channelVo);
                            }
                        }
                    }
                }
            }
            if (channelList.size() <= 0 && showToast == 1){
                showToast(getString(R.string.raiden_channel_no_use));
            }
            checkCurrentAmount();
            mAdapter.resetSource(channelList);
            checkChannelEmpty();
            if (mSpinner != null && mSpinner.isActivated()){
                mSpinner.performClick();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取当前选中通道中的抵押余额
     * */
    private void checkCurrentAmount(){
        if (!TextUtils.isEmpty(toAddress) && channelList != null && channelList.size() > 0){
            for (int i = 0 ; i < channelList.size() ; i++){
                if (TextUtils.equals(channelList.get(i).getPartnerAddress().toLowerCase(),toAddress.toLowerCase())){
                    toValue.setText("");
                    toValue.setHint(channelList.get(i).getBalance());
                    break;
                }
            }
        }
    }

    /**
     * 获取当前选中通道中的抵押余额
     * */
    private String getCurrentAmount(){
        String balance = "";
        if (!TextUtils.isEmpty(toAddress) && channelList != null && channelList.size() > 0){
            for (int i = 0 ; i < channelList.size() ; i++){
                if (TextUtils.equals(channelList.get(i).getPartnerAddress().toLowerCase(),toAddress.toLowerCase())){
                    balance = channelList.get(i).getBalance();
                    break;
                }
            }
        }
        return balance;
    }

    /**
     * 检测通道显示状态
     * */
    private void checkChannelEmpty(){
        if (TextUtils.isEmpty(scanResultAddress)){
            normalAddress();
        }else{
            isScanAddress();
        }
        scanResultAddress = "";
    }

    /**
     * 正常显示UI
     * */
    private void normalAddress(){
        if (channelList == null || channelList.size() <= 0){
            if (getChannel != null){
                getChannel.setVisibility(View.VISIBLE);
            }
            if (down_flg != null){
                down_flg.setVisibility(View.GONE);
            }

            if (walletAddressDelete != null){
                walletAddressDelete.setVisibility(View.GONE);
            }
            if (!TextUtils.isEmpty(toAddress)){
                toAddress = "";
                if (mShowText != null){
                    mShowText.setText("");
                }
                if (showNoteBody != null){
                    showNoteBody.setVisibility(View.GONE);
                }
            }
        }else{
            if (walletAddressDelete != null){
                walletAddressDelete.setVisibility(View.GONE);
            }

            if (getChannel != null){
                getChannel.setVisibility(View.GONE);
            }

            if (down_flg != null){
                down_flg.setVisibility(View.VISIBLE);
            }

            if (!TextUtils.isEmpty(toAddress)){
                boolean hasExist = false;
                for (int i = 0 ; i < channelList.size() ; i++){
                    if (TextUtils.equals(toAddress.toLowerCase(),channelList.get(i).getPartnerAddress().toLowerCase())){
                        mSpinner.setSelection(i,true);
                        hasExist = true;
                        break;
                    }
                }

                if (!hasExist){
                    toAddress = "";
                    if (mShowText != null){
                        mShowText.setText("");
                    }
                    if (showNoteBody != null){
                        showNoteBody.setVisibility(View.GONE);
                    }
                    if (channelList != null && channelList.size() >0){
                        mSpinner.setSelection(0,true);
                        setChannelNote(channelList.get(0).getPartnerAddress(),true);
                        tokenAddressRequestMethod(channelList.get(0).getPartnerAddress());
                    }
                }
            }
        }
    }

    /**
     * 是扫描二维码回来
     * */
    private void isScanAddress(){
        if (channelList == null || channelList.size() <= 0){
            if (getChannel != null){
                getChannel.setVisibility(View.GONE);
            }
            if (down_flg != null){
                down_flg.setVisibility(View.GONE);
            }

            if (walletAddressDelete != null){
                walletAddressDelete.setVisibility(View.VISIBLE);
            }
        }else{
            boolean hasExist = false;
            for (int i = 0 ; i < channelList.size() ; i++){
                if (TextUtils.equals(scanResultAddress.toLowerCase(),channelList.get(i).getPartnerAddress().toLowerCase())){
                    mSpinner.setSelection(i,true);
                    hasExist = true;
                    break;
                }
            }
            if (hasExist){
                if (walletAddressDelete != null){
                    walletAddressDelete.setVisibility(View.GONE);
                }

                if (getChannel != null){
                    getChannel.setVisibility(View.GONE);
                }
                if (down_flg != null){
                    down_flg.setVisibility(View.VISIBLE);
                }
            }else{
                if (walletAddressDelete != null){
                    walletAddressDelete.setVisibility(View.VISIBLE);
                }

                if (getChannel != null){
                    getChannel.setVisibility(View.GONE);
                }
                if (down_flg != null){
                    down_flg.setVisibility(View.GONE);
                }
            }
        }
    }

    @OnItemSelected(value = R.id.address_spinner , callback = OnItemSelected.Callback.ITEM_SELECTED)
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
        if (channelList != null && channelList.size() > 0){
            String address = channelList.get(position).getPartnerAddress();
            tempChannelVo = channelList.get(position);
            if (toValue != null){
                toValue.setText("");
                if (tempChannelVo != null && !TextUtils.isEmpty(tempChannelVo.getBalance())){
                    toValue.setHint(tempChannelVo.getBalance());
                }else {
                    toValue.setHint(getString(R.string.set_amount));
                }
            }
            setChannelNote(address,true);
            tokenAddressRequestMethod(address);
            notShowSpinner = true;
        }
    }

    @OnTextChanged(value = R.id.toValue,callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void afterTextChanged(Editable s){
        try {
            String temp = s.toString();
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
                MySharedPrefs.write(PhotonTransferUI.this, MySharedPrefs.FILE_USER, MySharedPrefs.PHOTON_ETH_RPC_END_POINT, wsUrl);
                mPresenter.checkWalletExist(PhotonTransferUI.this,password,storableWallet.getPublicKey());
            }
        });
        mdf.setTitleAndContentText(getString(R.string.photon_start_up),"");
        mdf.show(getSupportFragmentManager(), "mdf");
        return false;
    }
    
    @OnClick({R.id.channelPay,R.id.down_flg,R.id.walletAddressDelete,R.id.getChannel
            ,R.id.photonScan,R.id.photonCreate,R.id.photonList,
            R.id.closeKeyWord,R.id.refreshOfflinePay})
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.channelPay:
                sendTransferMethod();
                break;
            case R.id.down_flg:
                if (channelList != null && channelList.size() > 0){
                    if (mSpinner != null){
                        mSpinner.performClick();
                    }
                }else{
                    LoadingDialog.show(this,"");
                    mPresenter.loadChannelList(true);
                }
                break;
            case R.id.getChannel:
                LoadingDialog.show(this,"");
                mPresenter.loadChannelList(true);
                break;
            case R.id.walletAddressDelete:
                toAddress = "";
                if (mShowText != null){
                    mShowText.setText("");
                }
                if (showNoteBody != null){
                    showNoteBody.setVisibility(View.GONE);
                }
                if (channelList != null && channelList.size() >0){
                    mSpinner.setSelection(0,true);
                    setChannelNote(channelList.get(0).getPartnerAddress(),true);
                    tokenAddressRequestMethod(channelList.get(0).getPartnerAddress());
                }
                checkChannelEmpty();
                break;
            case R.id.photonScan:
                Intent photonScan = new Intent(this, CaptureActivity.class);
                photonScan.putExtra("type", 1);
                startActivityForResult(photonScan, 100);
                break;
            case R.id.photonCreate:
                Intent intent = new Intent(this, PhotonCreateChannel.class);
                intent.putExtra("storableWallet", storableWallet);
                intent.putExtra("tokenVo", mTokenVo);
                intent.putExtra("fromType", 0);
                startActivityForResult(intent, RAIDEN_CHANNEL_CREATE);
                Utils.openNewActivityAnim(this, false);
                break;
            case R.id.photonList:
                intoPhotonChannelList();
                break;
            case R.id.closeKeyWord:
                Utils.hiddenKeyBoard(PhotonTransferUI.this);
                break;
            case R.id.refreshOfflinePay:
                LoadingDialog.show(this,"");
                mPresenter.tokenAddressAsk(true);
                break;
        }
    }

    /**
     * 转账相关
     * */
    private void sendTransferMethod(){

        if (TextUtils.isEmpty(toAddress)){
            showToast(getString(R.string.input_address));
            return;
        }

        String amount = toValue.getText().toString().trim();
        if (TextUtils.isEmpty(amount)){
            showToast(getString(R.string.input_vaule));
            return;
        }
        boolean canTransfer = checkTransferAmount(amount);
        if (!canTransfer){
            showToast(getString(R.string.balance_not_enough));
            return;
        }

        if (NextApplication.raidenSwitch){
            if (useOfflinePay.getVisibility() == View.VISIBLE){
                photonTransfer(amount,"0",true,true,"");
            }else{
                CustomDialogFragment customDialogFragment = new CustomDialogFragment(CustomDialogFragment.DIALOG_CUSTOM_TITLE_CENTER);
                customDialogFragment.setHindCancelButton(true);
                customDialogFragment.setTitle(getString(R.string.dialog_prompt));
                customDialogFragment.setConfirmButton(getString(R.string.ok));
                customDialogFragment.setContent(getString(R.string.photon_channel_mesh_pay_3));
                customDialogFragment.show(getSupportFragmentManager(),"mdf");
            }
        }else{
            mPresenter.getFeeFindPath(NextApplication.photonTokenAddress,amount,toAddress);
        }
    }

    /**
     * 检测转账金额是否超出可用余额
     * @param amount  转账金额
     * */
    private boolean checkTransferAmount(String amount){
        try {
            if (channelList != null){
                boolean canTransfer = false;
                for (int i = 0 ; i < channelList.size();i++){
                    String balance = channelList.get(i).getBalance();
                    String lockedAmount = channelList.get(i).getLockedAmount();
                    if (Float.parseFloat(balance) - Float.parseFloat(lockedAmount) - Float.parseFloat(amount) >= 0){
                        canTransfer = true;
                        break;
                    }
                }
                return canTransfer;
            }else{
                return true;
            }
        }catch (Exception e){
            return true;
        }
    }

    private void intoPhotonChannelList(){
        Intent photonIntent = new Intent(PhotonTransferUI.this, PhotonChannelList.class);
        photonIntent.putExtra("storableWallet", storableWallet);
        photonIntent.putExtra("tokenVo", mTokenVo);
        photonIntent.putExtra("type", 1);
        startActivity(photonIntent);
        Utils.openNewActivityAnim(PhotonTransferUI.this, false);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == RAIDEN_CHANNEL_CREATE){
                mPresenter.loadChannelList(true);
            }else{
                String address = data.getStringExtra("address");
                scanResultAddress = address;
                toAddress = address;
                if (showNoteBody != null){
                    showNoteBody.setVisibility(View.GONE);
                }
                if (mShowText != null){
                    mShowText.setText(address);
                }
                tokenAddressRequestMethod(address);
                if (toValue != null){
                    toValue.setHint(getString(R.string.set_amount));
                }
            }
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
            JSONObject object = new JSONObject(jsonString);
            int errorCode = object.optInt("error_code");
            if (errorCode == 0) {
                showToast(getResources().getString(R.string.raiden_transfer_success));
                intoPhotonChannelList();
            }else{
                showToast(object.optString("error_message"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            LoadingDialog.close();
        }
    }

    //切换地址请求接口告知服务器
    private void tokenAddressRequestMethod(String address){
        if (NextApplication.raidenSwitch){
            boolean hasFound = mPresenter.checkTokenStatus(address);
            if (hasFound){
                mPresenter.tokenAddressRequest(address.toLowerCase());
            }else{
                mPresenter.tokenAddressAsk(true);
            }
        }else{
            useOfflinePay.setVisibility(View.GONE);
            refreshOfflinePay.setVisibility(View.GONE);
        }
    }

    @Override
    public void uploadTokenSuccess(JSONObject response) {
        useOfflinePay.setVisibility(View.VISIBLE);
        refreshOfflinePay.setVisibility(View.GONE);
    }

    @Override
    public void uploadTokenError(int errorCode, String errorMsg) {
        useOfflinePay.setVisibility(View.GONE);
        if (NextApplication.raidenSwitch){
            refreshOfflinePay.setVisibility(View.VISIBLE);
        }else{
            refreshOfflinePay.setVisibility(View.GONE);
        }
    }

    @Override
    public void loadChannelSuccess(String jsonString,boolean showToast) {
        if(mHandler != null){
            Message mes = new Message();
            mes.what = 4;
            mes.obj = jsonString;
            mes.arg1 = showToast ? 1 : 0;
            mHandler.sendMessage(mes);
        }
    }

    @Override
    public void loadChannelError(boolean showToast) {
        if(mHandler != null){
            if (showToast){
                mHandler.sendEmptyMessage(3);
            }else{
                mHandler.sendEmptyMessage(7);
            }
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
    public void transferSuccess(String jsonString) {
        if(mHandler != null){
            Message message = Message.obtain();
            message.obj = jsonString;
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
    public void loadFindPathSuccess(String jsonString,String amount) {
        if (TextUtils.isEmpty(jsonString)){
            photonTransfer(amount,"0",false,false,"");
        }else{
            try {
                String fee =  "0";
                JSONObject object = new JSONObject(jsonString);
                int errorCode = object.optInt("error_code",-1);
                if (errorCode == 0){
                    JSONArray array = object.optJSONArray("data");
                    if (array != null && array.length() > 0){
                        JSONObject objectFee = array.optJSONObject(0);
                        fee = objectFee.optString("fee");
                    }
                    if (TextUtils.equals(fee,"0")){
                        photonTransfer(amount,fee,false,false,"");
                    }else{
                        PhotonFeeEntity feeEntity = new PhotonFeeEntity();
                        feeEntity.setFee(fee);
                        feeEntity.setAmount(amount);
                        feeEntity.setFilePath(array == null ? "" : array.toString());
                        if(mHandler != null){
                            sendAmount = amount;
                            Message message = Message.obtain();
                            message.obj = feeEntity;
                            message.what = 8;
                            mHandler.sendMessage(message);
                        }else{
                            photonTransfer(amount,fee,false,false,"");
                        }
                    }
                }else{
                    photonTransfer(amount,fee,false,false,"");
                }

            }catch (Exception e){
                e.printStackTrace();
                photonTransfer(amount,"0",false,false,"");
            }
        }
    }

    @Override
    public void loadFindPathError(String amount) {
        photonTransfer(amount,"0",false,false,"");
    }

    /**
     * 转账付费弹框
     * */
    private void photonTransferDialog(PhotonFeeEntity feeEntity ){
        try {
            if (feeEntity == null){
                photonTransfer(sendAmount,"0",false,false,"");
            }else{
                LoadingDialog.close();
                CustomDialogFragment customDialogFragment = new CustomDialogFragment(CustomDialogFragment.DIALOG_CUSTOM);
                customDialogFragment.setTitle(getString(R.string.dialog_prompt));
                customDialogFragment.setConfirmButton(getString(R.string.photon_channel_transfer_type_2));
                String tokenSymbol = "";
                if (mTokenVo == null || TextUtils.isEmpty(mTokenVo.getTokenSymbol())){
                    tokenSymbol = TextUtils.equals(NextApplication.photonTokenAddress,PhotonUrl.PHOTON_SMT_TOKEN_ADDRESS) ? "SMT" : "MESH";
                }else{
                    tokenSymbol = mTokenVo.getTokenSymbol();
                }
                String tempFee = Convert.fromWei(feeEntity.getFee(),Convert.Unit.ETHER).stripTrailingZeros().toPlainString();
                customDialogFragment.setContent(getString(R.string.photon_channel_transfer_type_1,tempFee,tokenSymbol));
                customDialogFragment.setPublicButtonListener(new CustomDialogFragment.onPublicButtonListener() {
                    @Override
                    public void cancel() {

                    }

                    @Override
                    public void submit() {
                        photonTransfer(feeEntity.getAmount(),feeEntity.getFee(),true,false,feeEntity.getFilePath());
                    }
                });
                customDialogFragment.show(getSupportFragmentManager(),"mdf");
            }
        }catch (Exception e){
            e.printStackTrace();
            LoadingDialog.close();
        }
    }

    /**
     * 光子转账方法
     * */
    private void photonTransfer(String amount,String fee,boolean showDialog,boolean isDirect,String filePath){
        mPresenter.photonTransferMethod(NextApplication.photonTokenAddress,amount,fee,toAddress,showDialog,isDirect,filePath);
    }

    @Override
    public void tokenAddressAskSuccess(JSONObject response,boolean isOffline) {
        LoadingDialog.close();
        PhotonNetUtil.getInstance().parseAddressAsk(response,isOffline);
        if (NextApplication.raidenSwitch){
            boolean hasFound = mPresenter.checkTokenStatus(toAddress);
            if (hasFound){
                mPresenter.tokenAddressRequest(toAddress.toLowerCase());
            }else{
                useOfflinePay.setVisibility(View.GONE);
                if (NextApplication.raidenSwitch){
                    refreshOfflinePay.setVisibility(View.VISIBLE);
                }else{
                    refreshOfflinePay.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public void tokenAddressAskError(boolean isOffline) {
        LoadingDialog.close();
        try {
            if (NextApplication.api != null) {
                String jsonString = PhotonNetUtil.getInstance().getWifiJson();
                NextApplication.api.updateMeshNetworkNodes(isOffline ? jsonString : new JSONArray().toString());
                mPresenter.tokenAddressRequest(toAddress.toLowerCase());
            }
        } catch (Exception e) {
            e.printStackTrace();
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
                    LoadingDialog.show(PhotonTransferUI.this, getString(R.string.photon_trans_ing));
                    break;
                case 1:
                    String jsonString = (String) msg.obj;
                    inquiryTransferStatus(jsonString);
                    break;
                case 2://fail
                    LoadingDialog.close();
                    if (NextApplication.api == null){
                        NextApplication.photonStatus = false;
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
                case 4:
                    parseJson((String) msg.obj,msg.arg1);
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
                case 8:
                    PhotonFeeEntity feeEntity = (PhotonFeeEntity) msg.obj;
                    photonTransferDialog(feeEntity);
                    break;
            }
        }
    };

    /**
     * 设置通道地址备注
     * @param address 通道地址
     * @param onSpinnerSelect 是否检测显示spinner
     * */
    private void setChannelNote(String address,boolean onSpinnerSelect){
        String channelNote = "";
        if (!TextUtils.isEmpty(address)){
            channelNote = ChannelNoteUtils.getChannelNote(address);
        }
        toAddress = address;
        if (TextUtils.isEmpty(channelNote)){
            if (showNoteBody != null){
                showNoteBody.setVisibility(View.GONE);
            }
            if (mShowText != null){
                mShowText.setText(address);
            }
        }else{
            if (showNoteBody != null){
                showNoteBody.setVisibility(View.VISIBLE);
                showTextAddress.setText(address);
            }
            if (mShowText != null){
                mShowText.setText(channelNote);
            }
        }

        if (onSpinnerSelect && !notShowSpinner){
            new Handler().postDelayed(() -> {
                if (mSpinner != null){
                    mSpinner.performClick();
                }
            },100);
        }
    }

}
