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
import android.util.Log;
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
import com.lingtuan.firefly.raiden.vo.RaidenChannelVo;
import com.lingtuan.firefly.spectrum.vo.StorableWallet;
import com.lingtuan.firefly.spectrum.vo.TokenVo;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.MyViewDialogFragment;
import com.lingtuan.firefly.util.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnLongClick;

import static com.lingtuan.firefly.NextApplication.raidenSwitch;

/**
 * Created on 2018/1/24.
 * raiden channel list ui
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
    @BindView(R.id.pay)
    TextView mPay;
    @BindView(R.id.switchbutton)
    SwitchButton mSwitchButton;

    private RaidenChannelListAdapter mAdapter = null;
    private List<RaidenChannelVo> source = null;

    private StorableWallet storableWallet;
    private TokenVo mTokenVo;
    private RaidenChannelVo channelVoClose;

    private PhotonChannelListContract.Presenter mPresenter;

    @Override
    protected void setContentView() {
        setContentView(R.layout.raiden_channel_list);
        getPassData();
    }

    private void getPassData() {
        storableWallet = (StorableWallet) getIntent().getSerializableExtra("storableWallet");
        mTokenVo = (TokenVo) getIntent().getSerializableExtra("tokenVo");
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
        filter.addAction(RaidenUrl.ACTION_RAIDEN_CONNECTION_STATE);
        registerReceiver(receiver, filter);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && (RaidenUrl.ACTION_RAIDEN_CONNECTION_STATE.equals(intent.getAction()))) {
                mPresenter.setPhotonStatus(mEthStatus,mXmppStatus);
            }
        }
    };

    @Override
    protected void initData() {
        try {
            new PhotonChannelListPresenterImpl(this);
            setTitle(getString(R.string.raiden_channel_name, mTokenVo.getTokenName()));
            swipeLayout.setColorSchemeResources(R.color.black);
            createChannel.setVisibility(View.VISIBLE);
            createChannel.setImageResource(R.drawable.raiden_creae_channel);
            source = new ArrayList<>();
            mAdapter = new RaidenChannelListAdapter(this, source, this);
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
                String resultStr = wsUrl;
                if (wsUrl.startsWith("ws")) {
                    resultStr = wsUrl;
                } else {
                    resultStr = "ws://" + wsUrl;
                }
                MySharedPrefs.write(PhotonChannelList.this, MySharedPrefs.FILE_USER, MySharedPrefs.RAIDEN_WS, resultStr);
                mPresenter.checkWalletExist(PhotonChannelList.this,password,storableWallet.getPublicKey());
            }
        });
        mdf.show(getSupportFragmentManager(), "mdf");
        return false;
    }
    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.app_right:
                Intent intent = new Intent(this, PhotonCreateChannel.class);
                intent.putExtra("storableWallet", storableWallet);
                intent.putExtra("tokenVo", mTokenVo);
                startActivityForResult(intent, PHOTON_CHANNEL_CREATE);
                Utils.openNewActivityAnim(this, false);
                break;
            case R.id.pay:
                finish();
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

    @Override
    public void onRefresh() {
        mPresenter.loadChannelList();
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    showToast(getString(R.string.error_get_raiden_list));
                    swipeLayout.setRefreshing(false);
                    checkListEmpty();
                    break;
                case 1:
                    swipeLayout.setRefreshing(false);
                    parseJson((String) msg.obj);
                    break;
                case 2:
                    LoadingDialog.close();
                    break;
                case 3:
                    LoadingDialog.close();
                    swipeLayout.setRefreshing(true);
                    mPresenter.loadChannelList();
                    break;
                case 4://Get the success of the next step
                    RaidenNet.getInatance().startPhotonServer((String) msg.obj);
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
        if (TextUtils.isEmpty(jsonString) || "null".equals(jsonString)) {
            source.clear();
            mAdapter.resetSource(source);
            return;
        }
        try {
            source.clear();
            JSONArray array = new JSONArray(jsonString);
            if (array.length() > 0) {
                for (int i = 0; i < array.length(); i++) {
                    JSONObject object = array.optJSONObject(i);
                    RaidenChannelVo channelVo = new RaidenChannelVo().parse(object);
                    source.add(channelVo);
                }
            }
            mAdapter.resetSource(source);
            checkListEmpty();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * To test whether the current list is empty
     */
    private void checkListEmpty() {
        if (source == null || source.size() == 0) {
            emptyRela.setVisibility(View.VISIBLE);
            emptyTextView.setText(R.string.get_raiden_list_empty);
        } else {
            emptyRela.setVisibility(View.GONE);
        }
    }

    @Override
    public void changeChannel(final int position, boolean isOpen) {
        MyViewDialogFragment mdf = new MyViewDialogFragment();
        mdf.setTitleAndContentText(getString(R.string.raiden_channel_close_hint), getString(R.string.raiden_channel_close_content));
        mdf.setOkCallback(() -> channelCloseMethod(position));
        mdf.show(getSupportFragmentManager(), "mdf");
    }

    @Override
    public void deopsitChannel(int position) {
        if (source.size() <= 0) {
            return;
        }
        Intent intent = new Intent(PhotonChannelList.this, RaidenChannelDepositUI.class);
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
        mdf.setTitleAndContentText(getString(R.string.raiden_channel_withdraw_hint), getString(R.string.raiden_channel_close_content));
        mdf.setOkCallback(() -> channelWithdrawMethod(position));
        mdf.show(getSupportFragmentManager(), "mdf");
    }

    /**
     * close channel
     */
    private void channelWithdrawMethod(int position) {
        if (source.size() <= 0) {
            return;
        }
        channelVoClose = source.get(position);
        if (RaidenConnectStatus.StateOpened == channelVoClose.getState()) {
            LoadingDialog.show(this, "");
            NextApplication.mRaidenThreadPool.execute(() -> {
                try {
                    if (NextApplication.api != null) {
                        BigDecimal ONE_ETHER = new BigDecimal("1000000000000000000");
                        String balance = new BigDecimal(source.get(position).getBalance()).multiply(ONE_ETHER).toBigInteger().toString();
                        String str = NextApplication.api.withdraw(channelVoClose.getChannelIdentifier(),balance ,"preparewithdraw");
                        checkChannelState(str);
                    } else {
                        LoadingDialog.close();
                    }
                } catch (Exception e) {
                    LoadingDialog.close();
                }
            });
        }
    }

    /**
     * close channel
     */
    private void channelCloseMethod(int position) {
        if (source.size() <= 0) {
            return;
        }
        channelVoClose = source.get(position);
        if (RaidenConnectStatus.StateOpened == channelVoClose.getState()) {
            LoadingDialog.show(this, "");
            NextApplication.mRaidenThreadPool.execute(() -> {
                try {
                    if (NextApplication.api != null) {
                        String str = NextApplication.api.closeChannel(channelVoClose.getChannelIdentifier(), false);
                        checkChannelState(str);
                        Log.i("xxxx通道关闭===", str);
                    } else {
                        LoadingDialog.close();
                    }
                } catch (Exception e) {
                    LoadingDialog.close();
                    Log.i("xxxx通道关闭失败===", e.toString());
                }
            });
        }
    }

    private void checkChannelState(String callId){
        try {
            Thread.sleep(2000);
            NextApplication.api.getCallResult(callId);
            closeChannelSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            if (TextUtils.equals(e.getMessage(),"cannot withdraw when has lock")){
                showToast(e.getMessage());
                mHandler.sendEmptyMessage(2);
            }else if (TextUtils.equals(e.getMessage(),"dealing")){
                checkChannelState(callId);
            }else{
                mHandler.sendEmptyMessage(2);
            }
        }
    }

    private void closeChannelSuccess(){
        try {
            mHandler.sendEmptyMessage(2);
            mHandler.sendEmptyMessage(3);
            JSONObject obj = new JSONObject();
            obj.put("address", channelVoClose.getChannelIdentifier());
            obj.put("time", System.currentTimeMillis());
            String aryString = MySharedPrefs.readString(PhotonChannelList.this, MySharedPrefs.FILE_RAIDEN_SETTLE, MySharedPrefs.KEY_RAIDEN_SETTLE);
            if (!TextUtils.isEmpty(aryString)) {
                JSONArray ary = new JSONArray(aryString);
                boolean isHas = false;
                for (int i = 0; i < ary.length(); i++) {
                    JSONObject tempObj = ary.getJSONObject(i);
                    if (tempObj.optString("address").equals(channelVoClose.getChannelIdentifier())) {
                        isHas = true;
                        break;
                    }
                }
                if (!isHas) {
                    ary.put(obj);
                    MySharedPrefs.write(PhotonChannelList.this, MySharedPrefs.FILE_RAIDEN_SETTLE, MySharedPrefs.KEY_RAIDEN_SETTLE, ary.toString());
                }
            } else {
                JSONArray ary = new JSONArray();
                ary.put(obj);
                MySharedPrefs.write(PhotonChannelList.this, MySharedPrefs.FILE_RAIDEN_SETTLE, MySharedPrefs.KEY_RAIDEN_SETTLE, ary.toString());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PHOTON_CHANNEL_CREATE) {
            swipeLayout.setRefreshing(true);
            mPresenter.loadChannelList();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            if (mSwitchButton != null){
                mSwitchButton.setBackColor(getResources().getColorStateList(R.color.switch_button_green));
            }
            NextApplication.raidenSwitch = true;
            RaidenNet.getInatance().RaidenNetSwitch(true,true);
        } else {
            if (mSwitchButton != null){
                mSwitchButton.setBackColor(getResources().getColorStateList(R.color.switch_button_gray));
            }
            NextApplication.raidenSwitch = false;
            RaidenNet.getInatance().RaidenNetSwitch(false,true);
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
