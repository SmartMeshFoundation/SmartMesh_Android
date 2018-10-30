package com.lingtuan.firefly.raiden;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
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
import com.lingtuan.firefly.raiden.vo.RaidenChannelVo;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.MyToast;
import com.lingtuan.firefly.util.MyViewDialogFragment;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.spectrum.vo.StorableWallet;
import com.lingtuan.firefly.spectrum.vo.TokenVo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.lingtuan.firefly.NextApplication.raidenSwitch;

/**
 * Created on 2018/1/24.
 * raiden channel list ui
 */

public class RaidenChannelList extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener, ChangeChannelStateListener, CompoundButton.OnCheckedChangeListener {

    private static int RAIDEN_CHANNEL_CREATE = 100;

    private TextView emptyTextView;
    private RelativeLayout emptyRela;
    private ImageView mXmppStatus, mEthStatus;

    private ListView listView = null;
    private SwipeRefreshLayout swipeLayout;
    private ImageView createChannel;
    private TextView mPay;
    private SwitchButton mSwitchButton;
    private RaidenChannelListAdapter mAdapter = null;
    private List<RaidenChannelVo> source = null;

    private StorableWallet storableWallet;
    private TokenVo mTokenVo;
    private RaidenChannelVo channelVoClose;

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
        listView = (ListView) findViewById(R.id.listView);
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        mSwitchButton = (SwitchButton) findViewById(R.id.switchbutton);
        emptyRela = (RelativeLayout) findViewById(R.id.empty_like_rela);
        emptyTextView = (TextView) findViewById(R.id.empty_text);
        createChannel = (ImageView) findViewById(R.id.app_right);
        mPay = (TextView) findViewById(R.id.pay);
        mXmppStatus = findViewById(R.id.raidenXMPP);
        mEthStatus = findViewById(R.id.raidenETH);
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
                if (NextApplication.mRaidenStatusVo != null && RaidenStatusType.Connected == NextApplication.mRaidenStatusVo.getEthStatus()) {
                    mEthStatus.setImageResource(R.drawable.raiden_top_connected);
                } else if (NextApplication.mRaidenStatusVo != null && RaidenStatusType.Default == NextApplication.mRaidenStatusVo.getEthStatus()) {
                    mEthStatus.setImageResource(R.drawable.raiden_top_default);
                } else {
                    mEthStatus.setImageResource(R.drawable.raiden_top_no_connected);
                }
                if (NextApplication.mRaidenStatusVo != null && RaidenStatusType.Connected == NextApplication.mRaidenStatusVo.getXMPPStatus()) {
                    mXmppStatus.setImageResource(R.drawable.raiden_top_connected);
                } else if (NextApplication.mRaidenStatusVo != null && RaidenStatusType.Default == NextApplication.mRaidenStatusVo.getXMPPStatus()) {
                    mXmppStatus.setImageResource(R.drawable.raiden_top_default);
                } else {
                    mXmppStatus.setImageResource(R.drawable.raiden_top_no_connected);
                }
            }
        }
    };

    @Override
    protected void initData() {
        try {
            setTitle(getString(R.string.raiden_channel_name, mTokenVo.getTokenName()));
            swipeLayout.setColorSchemeResources(R.color.black);
            createChannel.setVisibility(View.VISIBLE);
            createChannel.setImageResource(R.drawable.raiden_creae_channel);
            source = new ArrayList<>();
            mAdapter = new RaidenChannelListAdapter(this, source, this);
            listView.setAdapter(mAdapter);
            if (NextApplication.mRaidenStatusVo != null && RaidenStatusType.Connected == NextApplication.mRaidenStatusVo.getEthStatus()) {
                mEthStatus.setImageResource(R.drawable.raiden_top_connected);
            } else if (NextApplication.mRaidenStatusVo != null && RaidenStatusType.Default == NextApplication.mRaidenStatusVo.getEthStatus()) {
                mEthStatus.setImageResource(R.drawable.raiden_top_default);
            } else {
                mEthStatus.setImageResource(R.drawable.raiden_top_no_connected);
            }
            if (NextApplication.mRaidenStatusVo != null && RaidenStatusType.Connected == NextApplication.mRaidenStatusVo.getXMPPStatus()) {
                mXmppStatus.setImageResource(R.drawable.raiden_top_connected);
            } else if (NextApplication.mRaidenStatusVo != null && RaidenStatusType.Default == NextApplication.mRaidenStatusVo.getXMPPStatus()) {
                mXmppStatus.setImageResource(R.drawable.raiden_top_default);
            } else {
                mXmppStatus.setImageResource(R.drawable.raiden_top_no_connected);
            }

            mTitle.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    MyViewDialogFragment mdf = new MyViewDialogFragment(MyViewDialogFragment.DIALOG_HINT_INPUT_WS_IP, new MyViewDialogFragment.EditWsUrlCallback() {
                        @Override
                        public void setWsUrl(String wsUrl) {
                            if (!TextUtils.isEmpty(wsUrl)) {
                                String resultStr = wsUrl;
                                if (wsUrl.startsWith("ws")) {
                                    resultStr = wsUrl;
                                } else {
                                    resultStr = "ws://" + wsUrl;
                                }
                                MySharedPrefs.write(RaidenChannelList.this, MySharedPrefs.FILE_USER, MySharedPrefs.RAIDEN_WS, resultStr);
                                RaidenNet.getInatance().startRaidenServer();
                            }
                        }
                    });
                    mdf.show(getSupportFragmentManager(), "mdf");
                    return false;
                }
            });
        } catch (Exception e) {
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.app_right:
                Intent intent = new Intent(this, RaidenCreateChannel.class);
                intent.putExtra("storableWallet", storableWallet);
                intent.putExtra("tokenVo", mTokenVo);
                startActivityForResult(intent, RAIDEN_CHANNEL_CREATE);
                Utils.openNewActivityAnim(this, false);
                break;
            case R.id.pay:
                try {
                    if (source != null && source.size() > 0) {
                        ArrayList<RaidenChannelVo> tempList = new ArrayList<>();
                        for (int i = 0; i < source.size(); i++) {
                            RaidenChannelVo vo = source.get(i);
                            if (RaidenConnectStatus.StateOpened == vo.getState() && !TextUtils.isEmpty(vo.getBalance())) {
                                tempList.add(vo);
                            }
                        }
                        if (tempList.size() > 0) {
                            Intent transfer = new Intent(RaidenChannelList.this, RaidenTransferUI.class);
                            transfer.putExtra("channelList", tempList);
                            startActivity(transfer);
                            Utils.openNewActivityAnim(RaidenChannelList.this, false);
                        } else {
                            showToast(getResources().getString(R.string.raiden_channel_no));
                        }
                    } else {

                    }
                } catch (Exception e) {
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(true);
                loadChannelList();
            }
        }, 500);
    }

    @Override
    public void onRefresh() {
        loadChannelList();
    }

    /**
     * load channel list
     */
    private void loadChannelList() {

//        try {
//            String aa = NextApplication.api.getTransferStatus(RaidenUrl.MESH_TOKEN_ADDRESS, "0xe155b80492df8c5f572727c4a6207b7200f1c34751aefef92ab2608e86efabac");
//            Log.i("xxxxxxxxxx状态", aa);
//        }catch (Exception e){
//            Log.i("xxxxxxxxxx状态异常", e.toString());
//        }
        NextApplication.mRaidenThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (NextApplication.api != null) {
                        String str = NextApplication.api.getChannelList();
                        Log.i("xxxxxxxxxxxxx", "通道列表成功==" + str);
                        mHandler.sendEmptyMessage(2);
                        if (!TextUtils.isEmpty(str)) {
                            Message mes = new Message();
                            mes.what = 1;
                            mes.obj = str;
                            mHandler.sendMessage(mes);
                        }
                    } else {
                        mHandler.sendEmptyMessage(0);
                        mHandler.sendEmptyMessage(2);
                    }
                } catch (Exception e) {
                    mHandler.sendEmptyMessage(0);
                    mHandler.sendEmptyMessage(2);
                }
            }
        });
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
                    loadChannelList();
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
            return;
        }
        try {
            source.clear();
            JSONArray array = new JSONArray(jsonString);
            if (array.length() > 0) {
                for (int i = 0; i < array.length(); i++) {
                    JSONObject object = array.optJSONObject(i);
                    RaidenChannelVo channelVo = new RaidenChannelVo().parse(object);
//                    if (TextUtils.equals(Constants.CONTACT_ADDRESS, channelVo.getTokenAddress().toLowerCase())) {
                    if (RaidenConnectStatus.StateOpened == channelVo.getState() || RaidenConnectStatus.StateClosed == channelVo.getState()) {
                        source.add(channelVo);
                    }
//                    }
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
        mdf.setOkCallback(new MyViewDialogFragment.OkCallback() {
            @Override
            public void okBtn() {
                channelCloseMethod(position);
            }
        });
        mdf.show(getSupportFragmentManager(), "mdf");
    }

    @Override
    public void deopsitChannel(int position) {
        if (source.size() <= 0) {
            return;
        }
        Intent intent = new Intent(RaidenChannelList.this, RaidenChannelDepositUI.class);
        intent.putExtra("raidenChannelVo", source.get(position));
        intent.putExtra("storableWallet", storableWallet);
        intent.putExtra("tokenVo", mTokenVo);

        startActivityForResult(intent, RAIDEN_CHANNEL_CREATE);
        Utils.openNewActivityAnim(RaidenChannelList.this, false);
    }

    @Override
    public void transferChannel(int position) {
        if (source.size() <= 0) {
            return;
        }
        Intent intent = new Intent(RaidenChannelList.this, RaidenTransferUI.class);
        intent.putExtra("raidenChannelVo", source.get(position));
        intent.putExtra("storableWallet", storableWallet);
        startActivityForResult(intent, RAIDEN_CHANNEL_CREATE);
        Utils.openNewActivityAnim(RaidenChannelList.this, false);
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
            NextApplication.mRaidenThreadPool.execute(new Runnable() {
                @Override
                public void run() {
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
            Log.i("xxxxxxxxx",e.getMessage());

        }
    }

    private void closeChannelSuccess(){
        try {
            mHandler.sendEmptyMessage(2);
            mHandler.sendEmptyMessage(3);
            JSONObject obj = new JSONObject();
            obj.put("address", channelVoClose.getChannelIdentifier());
            obj.put("time", System.currentTimeMillis());
            String aryString = MySharedPrefs.readString(RaidenChannelList.this, MySharedPrefs.FILE_RAIDEN_SETTLE, MySharedPrefs.KEY_RAIDEN_SETTLE);
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
                    MySharedPrefs.write(RaidenChannelList.this, MySharedPrefs.FILE_RAIDEN_SETTLE, MySharedPrefs.KEY_RAIDEN_SETTLE, ary.toString());
                }
            } else {
                JSONArray ary = new JSONArray();
                ary.put(obj);
                MySharedPrefs.write(RaidenChannelList.this, MySharedPrefs.FILE_RAIDEN_SETTLE, MySharedPrefs.KEY_RAIDEN_SETTLE, ary.toString());

            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == RAIDEN_CHANNEL_CREATE) {
            swipeLayout.setRefreshing(true);
            loadChannelList();
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
            mSwitchButton.setBackColor(getResources().getColorStateList(R.color.switch_button_green));
            NextApplication.raidenSwitch = true;
            RaidenNet.getInatance().RaidenNetSwitch(true);
        } else {
            mSwitchButton.setBackColor(getResources().getColorStateList(R.color.switch_button_gray));
            NextApplication.raidenSwitch = false;
            RaidenNet.getInatance().RaidenNetSwitch(false);
        }
    }
}
