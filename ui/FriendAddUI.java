package com.lingtuan.firefly.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.offline.AppNetService;
import com.lingtuan.firefly.offline.vo.WifiPeopleVO;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;

import org.json.JSONObject;

import java.util.List;

/**
 * Add buddy UI
 * Created on 2017/10/26.
 */

public class FriendAddUI extends BaseActivity {

    private EditText contentEt;
    private EditText addNoteEt;

    private TextView rightBtn;

    private String localId;
    private boolean isOfflineFound;

    private AppNetService appNetService;

    @Override
    protected void setContentView() {
        setContentView(R.layout.friend_add_layout);
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
        localId = getIntent().getStringExtra("localId");
        isOfflineFound = getIntent().getBooleanExtra("offlineFound",false);
    }

    @Override
    protected void findViewById() {
        contentEt = (EditText) findViewById(R.id.contentEt);
        addNoteEt = (EditText) findViewById(R.id.addNoteEt);
        rightBtn = (TextView) findViewById(R.id.app_btn_right);
    }

    @Override
    protected void setListener() {
        rightBtn.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.add_friends_validation));
        rightBtn.setVisibility(View.VISIBLE);
        rightBtn.setText(getString(R.string.send));

        int openSmartMesh = MySharedPrefs.readInt1(NextApplication.mContext,MySharedPrefs.FILE_USER,MySharedPrefs.KEY_NO_NETWORK_COMMUNICATION + NextApplication.myInfo.getLocalId());
        if (openSmartMesh == 1){
            bindService(new Intent(this, AppNetService.class), serviceConn,BIND_AUTO_CREATE);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.app_btn_right:
                addFriendsMethod();
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    /**
     * Add buddy method
     * */
    private void addFriendsMethod() {
        if (isOfflineFound){
            boolean isFound = false;
            if (appNetService != null){
                List<WifiPeopleVO > wifiPeopleVOs = appNetService.getwifiPeopleList();
                for (int i = 0 ; i < wifiPeopleVOs.size() ; i++){
                    if (localId.equals(wifiPeopleVOs.get(i).getLocalId())){
                        isFound = true;
                        break;
                    }
                }
            }
            if (isFound && appNetService != null){
                appNetService.handleSendAddFriendCommend(localId,false);
                showToast(getString(R.string.offline_addffiend));
                finish();
            }else{
                addFriend();
            }
        }else{
            addFriend();
        }
    }

    private void addFriend() {
        String content = contentEt.getText().toString().trim();
        String note = addNoteEt.getText().toString().trim();
        NetRequestImpl.getInstance().addFriend(localId,note ,content, new RequestListener() {
            @Override
            public void start() {
                LoadingDialog.show(FriendAddUI.this,"");
            }

            @Override
            public void success(JSONObject response) {
                LoadingDialog.close();
                showToast(response.optString("msg"));
                finish();
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                LoadingDialog.close();
                showToast(errorMsg);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        int openSmartMesh = MySharedPrefs.readInt1(NextApplication.mContext,MySharedPrefs.FILE_USER,MySharedPrefs.KEY_NO_NETWORK_COMMUNICATION + NextApplication.myInfo.getLocalId());
        if (openSmartMesh == 1 && serviceConn != null){
            unbindService(serviceConn);
        }
    }
}
