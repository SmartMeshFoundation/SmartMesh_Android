package com.lingtuan.firefly.raiden;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.custom.switchbutton.SwitchButton;
import com.lingtuan.firefly.raiden.contract.PhotonChannelListContract;
import com.lingtuan.firefly.raiden.presenter.PhotonChannelListPresenterImpl;
import com.lingtuan.firefly.raiden.util.ChannelNoteUtils;
import com.lingtuan.firefly.raiden.util.PhotonStartUtils;
import com.lingtuan.firefly.raiden.vo.PhotonChannelVo;
import com.lingtuan.firefly.raiden.vo.TxTypeStr;
import com.lingtuan.firefly.spectrum.vo.StorableWallet;
import com.lingtuan.firefly.spectrum.vo.TokenVo;
import com.lingtuan.firefly.util.CustomDialogFragment;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.MyViewDialogFragment;
import com.lingtuan.firefly.util.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnLongClick;

import static com.lingtuan.firefly.NextApplication.raidenSwitch;

/**
 * Created on 2018/1/24.
 * photon channel list ui
 */

public class PhotonChannelList extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener, ChangeChannelStateListener, CompoundButton.OnCheckedChangeListener,PhotonChannelListContract.View{

    private static int PHOTON_CHANNEL_CREATE = 100;

    @BindView(R.id.empty_text)
    TextView emptyTextView;
    @BindView(R.id.empty_like_rela)
    RelativeLayout emptyRela;
    @BindView(R.id.raidenXMPP)
    ImageView mXmppStatus;
    @BindView(R.id.raidenETH)
    ImageView mEthStatus;
    @BindView(R.id.listView)
    ListView listView;
    @BindView(R.id.swipe_container)
    SwipeRefreshLayout swipeLayout;
    @BindView(R.id.app_right)
    ImageView createChannel;
    @BindView(R.id.app_right_btn)
    ImageView transferList;
    @BindView(R.id.pay)
    TextView mPay;
    @BindView(R.id.switchbutton)
    SwitchButton mSwitchButton;

    private PhotonChannelListAdapter mAdapter = null;
    private List<PhotonChannelVo> source = null;

    private StorableWallet storableWallet;
    private TokenVo mTokenVo;
    private int type;//0 通道列表页面 1 转账页面 2 启动光子页面

    private Timer mTimer;

    private PhotonChannelListContract.Presenter mPresenter;

    @Override
    protected void setContentView() {
        setContentView(R.layout.raiden_channel_list);
        getPassData();
    }

    private void getPassData() {
        storableWallet = (StorableWallet) getIntent().getSerializableExtra("storableWallet");
        mTokenVo = (TokenVo) getIntent().getSerializableExtra("tokenVo");
        type = getIntent().getIntExtra("type",-1);
    }

    @Override
    protected void findViewById() {
        mXmppStatus.setVisibility(View.VISIBLE);
        mEthStatus.setVisibility(View.VISIBLE);
    }

    @Override
    protected void setListener() {
        swipeLayout.setOnRefreshListener(this);
        createChannel.setOnClickListener(this);
        mPay.setOnClickListener(this);
        mSwitchButton.setOnCheckedChangeListener(null);
        if (!raidenSwitch) {
            mSwitchButton.setChecked(false);
            mSwitchButton.setBackColor(getResources().getColorStateList(R.color.switch_button_gray));
        } else {
            mSwitchButton.setChecked(true);
            mSwitchButton.setBackColor(getResources().getColorStateList(R.color.switch_button_green));
        }
        mSwitchButton.setOnCheckedChangeListener(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(PhotonUrl.ACTION_RAIDEN_CONNECTION_STATE);
        filter.addAction(PhotonUrl.ACTION_PHOTON_RECEIVER_TRANSFER);
        filter.addAction(PhotonUrl.ACTION_PHOTON_NOTIFY_CALL_ID);
        filter.addAction(PhotonUrl.ACTION_PHOTON_NOTIFY_CALL_CHANNEL_INFO);
        filter.addAction(PhotonUrl.ACTION_PHOTON_NOTIFY_CALL_CONTRACT_INFO);
        registerReceiver(receiver, filter);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && (PhotonUrl.ACTION_RAIDEN_CONNECTION_STATE.equals(intent.getAction()))) {
                mPresenter.setPhotonStatus(mEthStatus,mXmppStatus);
            }else if (intent != null && (PhotonUrl.ACTION_PHOTON_RECEIVER_TRANSFER.equals(intent.getAction()))) {
                mPresenter.loadChannelList();
            }else if (intent != null && (PhotonUrl.ACTION_PHOTON_NOTIFY_CALL_CHANNEL_INFO.equals(intent.getAction()))) {
                new Handler().postDelayed(() -> mPresenter.loadChannelList(),500);
            }else if (intent != null && (PhotonUrl.ACTION_PHOTON_NOTIFY_CALL_CONTRACT_INFO.equals(intent.getAction()))) {
                String txType = intent.getStringExtra("type");
                if (!TextUtils.isEmpty(txType)){
                    new Handler().postDelayed(() -> {
                        if (TextUtils.equals(txType,TxTypeStr.ChannelDeposit.name())){
                            showToast(getString(R.string.photon_tx_deposit_1));
                        }else if (TextUtils.equals(txType,TxTypeStr.Withdraw.name())){
                            showToast(getString(R.string.photon_tx_withdraw_1));
                        }else if (TextUtils.equals(txType,TxTypeStr.ChannelSettle.name()) || TextUtils.equals(txType,TxTypeStr.CooperateSettle.name())){
                            showToast(getString(R.string.photon_tx_close_1));
                        }
                    },500);
                }
                mPresenter.loadChannelList();
            }
        }
    };

    @Override
    protected void initData() {
        try {
            new PhotonChannelListPresenterImpl(this);
            setTitle(getString(R.string.raiden_channel_name, mTokenVo.getTokenSymbol()));
//            setTitle(getString(R.string.raiden_channel_name_1));
            swipeLayout.setColorSchemeResources(R.color.black);
            createChannel.setVisibility(View.VISIBLE);
            createChannel.setImageResource(R.drawable.photon_create_icon);
            transferList.setVisibility(View.VISIBLE);
            transferList.setImageResource(R.drawable.photon_transfer_list);
            if (mTimer == null){
                mTimer = new Timer();
            }
            mTimer.schedule(mTimerTask,0,14000);
            source = new ArrayList<>();
            mAdapter = new PhotonChannelListAdapter(this, source, this);
            listView.setAdapter(mAdapter);
            mPresenter.setPhotonStatus(mEthStatus,mXmppStatus);
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
                MySharedPrefs.write(PhotonChannelList.this, MySharedPrefs.FILE_USER, MySharedPrefs.PHOTON_ETH_RPC_END_POINT, wsUrl);
                mPresenter.checkWalletExist(PhotonChannelList.this,password,storableWallet.getPublicKey());
            }
        });
        mdf.setTitleAndContentText(getString(R.string.photon_start_up),"");
        mdf.show(getSupportFragmentManager(), "mdf");
        return false;
    }

    @OnClick(R.id.app_right_btn)
    public void onClickView(View view){
        Intent intent = new Intent(this, PhotonTransferQueryUI.class);
        intent.putExtra("fromType",1);
        startActivity(intent);
        Utils.openNewActivityAnim(this, false);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.app_right:
                Intent intent = new Intent(this, PhotonCreateChannel.class);
                intent.putExtra("storableWallet", storableWallet);
                intent.putExtra("tokenVo", mTokenVo);
                intent.putExtra("fromType", 1);
                startActivityForResult(intent, PHOTON_CHANNEL_CREATE);
                Utils.openNewActivityAnim(this, false);
                break;
            case R.id.pay:
                if (type == 1){
                    finish();
                }else{
                    Intent photonIntent = new Intent(PhotonChannelList.this, PhotonTransferUI.class);
                    photonIntent.putExtra("storableWallet", storableWallet);
                    photonIntent.putExtra("tokenVo", mTokenVo);
                    startActivity(photonIntent);
                    Utils.openNewActivityAnim(PhotonChannelList.this, true);
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Handler().postDelayed(() -> {
            swipeLayout.setRefreshing(true);
            mPresenter.loadChannelList();
        }, 500);
    }

    public TimerTask mTimerTask = new TimerTask() {
        @Override
        public void run() {
            mPresenter.loadChannelList();
        }
    };

    @Override
    public void onRefresh() {
        mPresenter.loadChannelList();
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (NextApplication.api == null){
                        showToast(getString(R.string.photon_start_up_ing));
                    }else{
                        showToast(getString(R.string.error_get_raiden_list));
                    }
                    if (swipeLayout != null){
                        swipeLayout.setRefreshing(false);
                    }
                    checkListEmpty();
                    break;
                case 1:
                    if (swipeLayout != null){
                        swipeLayout.setRefreshing(false);
                    }
                    parseJson((String) msg.obj);
                    break;
                case 2:
                    LoadingDialog.close();
                    break;
                case 4://Get the success of the next step
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
                case 5://Enter the wrong password
                    LoadingDialog.close();
                    showToast(getString(R.string.wallet_copy_pwd_error));
                    break;
            }
        }
    };

    /**
     * parse json
     *
     * @param jsonString response string
     */
    private void parseJson(String jsonString) {
        try {
            if (TextUtils.isEmpty(jsonString) || "null".equals(jsonString)) {
                source.clear();
                if (mAdapter != null){
                    mAdapter.resetSource(source);
                }
                return;
            }
            try {
                source.clear();
                JSONObject object = new JSONObject(jsonString);
                int errorCode = object.optInt("error_code");
                if (errorCode == 0){
                    JSONArray array = object.optJSONArray("data");
                    if (array != null && array.length() > 0) {
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject dataObject = array.optJSONObject(i);
                            PhotonChannelVo channelVo = new PhotonChannelVo().parse(dataObject);
                            if (TextUtils.equals(NextApplication.photonTokenAddress.toLowerCase(),channelVo.getTokenAddress().toLowerCase())){
                                source.add(channelVo);
                            }
                        }
                    }
                }
                if (mAdapter != null){
                    mAdapter.resetSource(source);
                }
                checkListEmpty();
            } catch (Exception e) {
                e.printStackTrace();
                if (mAdapter != null){
                    mAdapter.resetSource(source);
                }
                checkListEmpty();
            }
        }catch (Exception e){
            e.printStackTrace();
        } }

    /**
     * To test whether the current list is empty
     */
    private void checkListEmpty() {
        if (source == null || source.size() == 0) {
            if (emptyRela != null){
                emptyRela.setVisibility(View.VISIBLE);
            }
            if (emptyTextView != null){
               emptyTextView.setText(R.string.get_raiden_list_empty);
            }
        } else {
            if (emptyRela != null){
                emptyRela.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void changeChannel(final int position, final boolean isForced) {
        closeChannelMethod(position,isForced);
    }

    private void closeChannelMethod(final int position, final boolean isForced){
        if (source.size() <= 0) {
            return;
        }
        String title = isForced ? getString(R.string.photon_channel_forced_close_hint) : getString(R.string.photon_channel_close_hint);
        String content = isForced ? getString(R.string.photon_channel_forced_close_content) : getString(R.string.photon_channel_close_content);
        MyViewDialogFragment mdf = new MyViewDialogFragment();
        mdf.setTitleAndContentText(title,content);
        mdf.setOkCallback(() -> channelCloseMethod(position,isForced));
        mdf.show(getSupportFragmentManager(), "mdf");
    }

    @Override
    public void depositChannel(int position) {
        if (source.size() <= 0) {
            return;
        }
        Intent intent = new Intent(PhotonChannelList.this, PhotonChannelDepositUI.class);
        intent.putExtra("raidenChannelVo", source.get(position));
        intent.putExtra("storableWallet", storableWallet);
        intent.putExtra("tokenVo", mTokenVo);
        startActivityForResult(intent, PHOTON_CHANNEL_CREATE);
        Utils.openNewActivityAnim(PhotonChannelList.this, false);
    }

    @Override
    public void withdrawChannel(int position) {
        if (source.size() <= 0) {
            return;
        }

        MyViewDialogFragment mdf = new MyViewDialogFragment();
        mdf.setTitleAndContentText(getString(R.string.photon_channel_withdraw_hint), getString(R.string.photon_channel_withdraw_content,source.get(position).getBalance()));
        mdf.setOkCallback(() -> channelWithdrawMethod(position));
        mdf.show(getSupportFragmentManager(), "mdf");
    }

    @Override
    public void settleChannel(int position) {
        if (source.size() <= 0) {
            return;
        }
        MyViewDialogFragment mdf = new MyViewDialogFragment();
        mdf.setTitleAndContentText(getString(R.string.photon_channel_forced_settle_hint), getString(R.string.photon_channel_settle_content));
        mdf.setOkCallback(() -> settleChannelMethod(position));
        mdf.show(getSupportFragmentManager(), "mdf");
    }

    private void settleChannelMethod(int position){
        if (source.size() <= 0) {
            return;
        }
        PhotonChannelVo channelVoClose = source.get(position);
        LoadingDialog.show(this, "");
        try {
            if (NextApplication.api != null) {
                String jsonString = NextApplication.api.settleChannel(channelVoClose.getChannelIdentifier());
                updateChannelList(position,jsonString,false);
            } else {
                LoadingDialog.close();
            }
        } catch (Exception e) {
            showToast(e.getMessage());
            LoadingDialog.close();
        }
    }

    /**
     * 取钱
     * channelIdentifierHashStr 通道地址
     * amountstr 取钱的金额
     * op 选项
     *    preparewithdraw 当你准备withdraw的时候，可以把通道转态切换到'prepareForWithdraw'状态，此时通道不再发起或接受任何交易
     *    cancelprepare 取消withdraw,把通道转态从prepareForWithdraw 切回到opened
     * 当然，当amount大于0的时候，op参数是没有意义的，会直接取钱。
     */
    private void channelWithdrawMethod(int position) {
        if (source.size() <= 0) {
            return;
        }
        PhotonChannelVo withdrawChannel = source.get(position);
        if (PhotonConnectStatus.StateOpened == withdrawChannel.getState()) {
            LoadingDialog.show(this, "");
            try {
                if (NextApplication.api != null) {
                    BigDecimal ONE_ETHER = new BigDecimal("1000000000000000000");
                    String balance = new BigDecimal(source.get(position).getBalance()).multiply(ONE_ETHER).toBigInteger().toString();
                    String jsonString = NextApplication.api.withdraw(withdrawChannel.getChannelIdentifier(),balance ,"");
                    updateChannelList(position,jsonString,true);
                } else {
                    LoadingDialog.close();
                }
            } catch (Exception e) {
                showToast(e.getMessage());
                LoadingDialog.close();
            }
        }
    }

    /**
     * close channel
     * channelIdentifierHashStr 通道地址
     * force 为false,则会寻求和对方协商关闭通道,在协商一致的情况下可以立即(等待一两个块的时间)将Token返回到双方账户;
     * force 为true,则不会与对方协商,意味着会强制关闭通道,等待settleTimeout结算窗口期,然后才可以进行SettleChannel,最终Token才会返回双方的账户
     */
    private void channelCloseMethod(int position,boolean isForced) {
        if (source.size() <= 0 || position < 0) {
            return;
        }
        PhotonChannelVo channelVoClose = source.get(position);
        LoadingDialog.show(this, "");
        try {
            if (NextApplication.api != null) {
                String jsonString = NextApplication.api.closeChannel(channelVoClose.getChannelIdentifier(), isForced);
                ChannelNoteUtils.deleteChannelNote(channelVoClose.getPartnerAddress());
                updateChannelList(position,jsonString,!isForced);
            } else {
                LoadingDialog.close();
            }
        } catch (Exception e) {
            showToast(e.getMessage());
            LoadingDialog.close();
        }
    }

    /**
     * 合约交易列表
     * */
    private void intoContractQueryUI(){
        Intent intent = new Intent(this,PhotonTransferQueryUI.class);
        intent.putExtra("showContract",true);
        intent.putExtra("fromType",1);
        startActivity(intent);
        Utils.openNewActivityAnim(this,false);
    }

    /**
     * 更新通道列表状态
     * @param position 更新的项目
     * @param jsonString 具体数据
     * @param needForced 当接口调用失败时候 是否需要强制关闭通道
     * */
    private void updateChannelList(int position,String jsonString,boolean needForced){
        try {
            LoadingDialog.close();
            JSONObject object = new JSONObject(jsonString);
            int errorCode = object.optInt("error_code");
            if (errorCode  == 0){
                intoContractQueryUI();
                JSONObject dataObject = object.optJSONObject("data");
                int state = dataObject.optInt("state");
                source.get(position).setState(state);
                if (mAdapter != null){
                    mAdapter.resetSource(source);
                }
            }else{
                if (needForced){
                    closeChannelMethod(position,needForced);
                }
                showToast(object.optString("error_message"));
            }
        }catch (Exception e){
            showToast(e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PHOTON_CHANNEL_CREATE) {
            if (swipeLayout != null){
                swipeLayout.setRefreshing(true);
            }
            mPresenter.loadChannelList();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTimer != null){
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimerTask != null){
            mTimerTask.cancel();
            mTimerTask = null;
        }
        unregisterReceiver(receiver);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            if (mSwitchButton != null){
                mSwitchButton.setBackColor(getResources().getColorStateList(R.color.switch_button_green));
            }
            NextApplication.raidenSwitch = true;
            PhotonNetUtil.getInstance().photonNetSwitch(true,true);
            CustomDialogFragment customDialogFragment = new CustomDialogFragment(CustomDialogFragment.DIALOG_CUSTOM_TITLE_CENTER);
            customDialogFragment.setHindCancelButton(true);
            customDialogFragment.setTitle(getString(R.string.dialog_prompt));
            customDialogFragment.setConfirmButton(getString(R.string.ok));
            customDialogFragment.setContent2(getString(R.string.photon_channel_mesh_pay_1));
            customDialogFragment.show(getSupportFragmentManager(),"mdf");
        } else {
            if (mSwitchButton != null){
                mSwitchButton.setBackColor(getResources().getColorStateList(R.color.switch_button_gray));
            }
            NextApplication.raidenSwitch = false;
            PhotonNetUtil.getInstance().photonNetSwitch(false,true);
            CustomDialogFragment customDialogFragment = new CustomDialogFragment(CustomDialogFragment.DIALOG_CUSTOM_TITLE_CENTER);
            customDialogFragment.setHindCancelButton(true);
            customDialogFragment.setTitle(getString(R.string.dialog_prompt));
            customDialogFragment.setConfirmButton(getString(R.string.ok));
            customDialogFragment.setContent2(getString(R.string.photon_channel_mesh_pay_2));
            customDialogFragment.show(getSupportFragmentManager(),"mdf");
        }
    }

    @Override
    public void loadChannelSuccess(String jsonString) {
        if (mHandler != null){
            mHandler.sendEmptyMessage(2);
            Message mes = new Message();
            mes.what = 1;
            mes.obj = jsonString;
            mHandler.sendMessage(mes);
        }
    }

    @Override
    public void loadChannelError(boolean showToast) {
        if (mHandler != null){
            if (showToast){
                mHandler.sendEmptyMessage(0);
            }
            mHandler.sendEmptyMessage(2);
        }
    }

    @Override
    public void checkWalletExistSuccess(String walletPwd) {
        Message message = Message.obtain();
        message.what = 4;
        message.obj = walletPwd;
        if(mHandler != null){
            mHandler.sendMessage(message);
        }
    }

    @Override
    public void checkWalletExistError() {
        if(mHandler != null){
            mHandler.sendEmptyMessage(5);
        }
    }

    @Override
    public void setPresenter(PhotonChannelListContract.Presenter presenter) {
        this.mPresenter = presenter;
    }
}
