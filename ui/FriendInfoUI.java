package com.lingtuan.firefly.ui;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.offline.AppNetService;
import com.lingtuan.firefly.offline.vo.WifiPeopleVO;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;
import com.lingtuan.firefly.vo.UserBaseVo;
import com.lingtuan.firefly.vo.UserInfoVo;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2017/10/20.
 * Friends information UI
 */

public class FriendInfoUI extends BaseActivity {

    private ImageView friendImg;
    private TextView addFriends,sendMsg,friendNote,friendAddress;
    private TextView friendSignature;//The signature

    private ImageView app_right;//Friends information set

    private AppNetService appNetService;

    private boolean dataHasLoad;

    UserInfoVo info = null;

    @Override
    protected void setContentView() {
        setContentView(R.layout.friend_info_layout);
        getPassData();
    }

    // The Activity and netService2 connection
    private ServiceConnection serviceConn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            // Binding service success
            AppNetService.NetServiceBinder binder = (AppNetService.NetServiceBinder) service;
            appNetService = binder.getService();
        }
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private void getPassData() {
        info = (UserInfoVo) getIntent().getSerializableExtra("info");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!dataHasLoad){
            loadFriendInfo();
            dataHasLoad = true;
        }

    }

    @Override
    protected void findViewById() {
        friendImg = (ImageView) findViewById(R.id.friendImg);
        addFriends = (TextView) findViewById(R.id.addFriends);
        sendMsg = (TextView) findViewById(R.id.sendMsg);
        friendNote = (TextView) findViewById(R.id.friendNote);
        friendAddress = (TextView) findViewById(R.id.friendAddress);
        friendSignature = (TextView) findViewById(R.id.friendSignature);
        app_right = (ImageView) findViewById(R.id.app_right);
    }

    @Override
    protected void setListener() {
        addFriends.setOnClickListener(this);
        sendMsg.setOnClickListener(this);
        app_right.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        bindService(new Intent(this, AppNetService.class), serviceConn,BIND_AUTO_CREATE);

        IntentFilter filter = new IntentFilter(Constants.ACTION_SELECT_CONTACT_REFRESH);
        LocalBroadcastManager.getInstance(FriendInfoUI.this).registerReceiver(mBroadcastReceiver,filter);

        app_right.setImageResource(R.drawable.icon_menu);
        loadData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(FriendInfoUI.this).unregisterReceiver(mBroadcastReceiver);
        unbindService(serviceConn);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && (Constants.ACTION_SELECT_CONTACT_REFRESH.equals(intent.getAction()))) {
                String userid = intent.getStringExtra("userid");
                if (info != null && TextUtils.equals(info.getLocalId(),userid)){
                    info.setFriendLog(1);
                    addFriends.setText(getString(R.string.contact_default));
                    addFriends.setTextColor(getResources().getColor(R.color.yellow));
                    addFriends.setEnabled(false);
                }
            }
        }
    };

    /**
     * Loading the page information
     */
    private void loadData() {
        if (info != null){
            UserBaseVo vo = FinalUserDataBase.getInstance().getUserBaseVoByUid(info.getLocalId());
            if(vo!=null){
                info.setFriendLog(1);
            }else if(TextUtils.equals(NextApplication.myInfo.getLocalId(),info.getLocalId())){
                info.setFriendLog(-1);
            }

            setTitle(info.getUsername());
            if (!info.getThumb().startsWith("http:")){
                NextApplication.displayCircleImage(friendImg,"file://".concat(info.getThumb()));
            }else{
                NextApplication.displayCircleImage(friendImg,info.getThumb());
            }

            if (TextUtils.isEmpty(info.getNote())){
                friendNote.setVisibility(View.GONE);
            }else{
                friendNote.setVisibility(View.VISIBLE);
                friendNote.setText(info.getNote());
            }
            if (TextUtils.isEmpty(info.getAddress())){
                friendAddress.setVisibility(View.GONE);
            }else{
                friendAddress.setVisibility(View.VISIBLE);
                friendAddress.setText(info.getAddress());
            }
            friendSignature.setText(info.getSightml());
            if (info.getFriendLog() == -1){
                app_right.setVisibility(View.GONE);
                addFriends.setVisibility(View.GONE);
                sendMsg.setVisibility(View.GONE);
            }else if (info.getFriendLog() == 1){
                addFriends.setText(getString(R.string.contact_default));
                addFriends.setTextColor(getResources().getColor(R.color.yellow));
                addFriends.setEnabled(false);
                if (Utils.isConnectNet(FriendInfoUI.this)){
                    app_right.setVisibility(View.VISIBLE);
                }else{
                    app_right.setVisibility(View.GONE);
                }
            }else{
                addFriends.setEnabled(true);
                addFriends.setText(getString(R.string.add_friends));
                addFriends.setTextColor(getResources().getColor(R.color.black));
                if (Utils.isConnectNet(FriendInfoUI.this)){
                    app_right.setVisibility(View.VISIBLE);
                }else{
                    app_right.setVisibility(View.GONE);
                }
            }

        }
    }

    /**
     * All information
     */
    private void loadFriendInfo(){
       NetRequestImpl.getInstance().requestUserInfo(info.getLocalId(), new RequestListener() {
           @Override
           public void start() {
               LoadingDialog.show(FriendInfoUI.this,"");
           }

           @Override
           public void success(JSONObject response) {
               JSONObject object = response.optJSONObject("data");
               String localid = info.getLocalId();
               info = new UserInfoVo().parse(object);
               if (info != null){
                   info.setLocalId(localid);
               }
               loadData();
               LoadingDialog.close();
           }

           @Override
           public void error(int errorCode, String errorMsg) {
               LoadingDialog.close();
           }
       });
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.app_right:
                Intent intent = new Intent(FriendInfoUI.this,FriendInfoDataSet.class);
                intent.putExtra("info",info);
                startActivityForResult(intent,100);
                Utils.openNewActivityAnim(FriendInfoUI.this,false);
                break;
            case R.id.addFriends:
                addFriendMethod();

                break;
            case R.id.sendMsg:
                Utils.intentChattingUI(FriendInfoUI.this,info.getLocalId(),info.getThumb(),info.getUsername(),info.getGender(),info.getFriendLog(),false,false,false,0,true);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 100){
            boolean isInBlack = data.getBooleanExtra("isInBlack",false);
            info.setInblack(isInBlack ? "1" : "0");
        }
    }

    /**
     * Add buddy method
     * */
    private void addFriendMethod() {
        if (Utils.isConnectNet(FriendInfoUI.this)){
            Intent addFriend = new Intent(FriendInfoUI.this,FriendAddUI.class);
            addFriend.putExtra("localId",info.getLocalId());
            startActivity(addFriend);
            Utils.openNewActivityAnim(FriendInfoUI.this,false);
        }else{
            List<WifiPeopleVO > wifiPeopleVOs = appNetService.getwifiPeopleList();
            boolean isFound = false;
            for (int i = 0 ; i < wifiPeopleVOs.size() ; i++){
                if (info.getLocalId().equals(wifiPeopleVOs.get(i).getLocalId())){
                    isFound = true;
                    break;
                }
            }
            if (isFound){
                appNetService.handleSendAddFriendCommend(info.getLocalId(),false);
                showToast(getString(R.string.offline_addffiend));
            }else{
                showToast(getString(R.string.net_unavailable));
            }
        }

    }


}
