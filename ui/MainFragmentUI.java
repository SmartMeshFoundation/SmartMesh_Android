package com.lingtuan.firefly.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.base.BaseFragment;
import com.lingtuan.firefly.contact.DiscussGroupJoinUI;
import com.lingtuan.firefly.custom.gesturelock.ACache;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.fragment.MainContactFragmentUI;
import com.lingtuan.firefly.fragment.MainMessageFragmentUI;
import com.lingtuan.firefly.fragment.MySelfFragment;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.login.LoginUtil;
import com.lingtuan.firefly.mesh.MeshService;
import com.lingtuan.firefly.network.NetRequestImpl;
import com.lingtuan.firefly.network.NetRequestUtils;
import com.lingtuan.firefly.offline.AppNetService;
import com.lingtuan.firefly.service.LoadDataService;
import com.lingtuan.firefly.service.UpdateVersionService;
import com.lingtuan.firefly.service.XmppService;
import com.lingtuan.firefly.setting.GestureLoginActivity;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.MyToast;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.vo.ChatMsg;
import com.lingtuan.firefly.vo.SystemNotice;
import com.lingtuan.firefly.vo.UserInfoVo;
import com.lingtuan.firefly.wallet.fragment.AccountFragment;
import com.lingtuan.firefly.wallet.fragment.NewWalletFragment;
import com.lingtuan.firefly.wallet.util.WalletStorage;
import com.lingtuan.firefly.xmpp.XmppAction;
import com.lingtuan.firefly.xmpp.XmppUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.functions.Consumer;

/**
 * Created on 2017/8/23.
 */

public class MainFragmentUI extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.main_tab_chats)
    TextView main_tab_chats;//The message
    @BindView(R.id.main_tab_contact)
    TextView main_tab_contact; //The address book
    @BindView(R.id.main_tab_account)
    TextView main_tab_account;//The wallet
    @BindView(R.id.main_tab_setting)
    TextView main_tab_setting;//my
    @BindView(R.id.mainBottomTab)
    LinearLayout mainBottomTab;//main tab
    @BindView(R.id.main_unread)
    TextView mMsgUnread;

    @BindView(R.id.noticeClockBody)
    RelativeLayout noticeClockBody;
    @BindView(R.id.noticeTitle)
    TextView noticeTitle;
    
    private MsgReceiverListener msgReceiverListener;
    private Stack<String> mStack = new Stack<>();
    private Map<String, BaseFragment> map = new HashMap<>();
    private int totalUnreadCount = 0;
    private boolean showAnimation;
    private File installFile;

    private SystemNotice systemNotice;
    
    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_main);
        
        //清除通知
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {

    }

    /**
     * show offline alert
     */
    private void showOfflineAlert(String msg) {
        try {
            MySharedPrefs.clearUserInfo(NextApplication.mContext);
            FinalUserDataBase.getInstance().close();
            
            //Exit the XMPP service
            XmppUtils.getInstance().destroy();
            NetRequestUtils.getInstance().destory();
            NetRequestImpl.getInstance().destory();
            Intent xmppservice = new Intent(NextApplication.mContext, XmppService.class);
            stopService(xmppservice);
            stopService(new Intent(this, MeshService.class));
            //Exit without social network service
            int version = android.os.Build.VERSION.SDK_INT;
            if (version >= 16) {
                Intent offlineservice = new Intent(NextApplication.mContext, AppNetService.class);
                stopService(offlineservice);
            }

            WalletStorage.destroy();
            Intent intent = new Intent(this, AlertActivity.class);
            intent.putExtra("type", 2);
            intent.putExtra("msg", msg);
            startActivity(intent);
            overridePendingTransition(0, 0);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    protected void findViewById() {

    }
    
    @Override
    protected void setListener() {

    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent == null) {
            return;
        }
        try {
            Utils.settingLanguage();
            setService(intent);
            setIntent(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    protected void initData() {

        Intent versionService = new Intent(MainFragmentUI.this, UpdateVersionService.class);
        stopService(versionService);
        startService(versionService);

        getSystemNotice();
        setService(getIntent());

        IntentFilter filter = new IntentFilter();
        filter.addAction(LoadDataService.ACTION_START_CONTACT_LISTENER);
        filter.addAction(XmppAction.ACTION_MAIN_UNREADMSG_UPDATE_LISTENER);
        filter.addAction(XmppAction.ACTION_MAIN_OFFLINE_LISTENER);
        filter.addAction(Constants.ACTION_NETWORK_RECEIVER);//Network monitoring
        filter.addAction(Constants.WALLET_SUCCESS);//Refresh the page
        filter.addAction(Constants.WALLET_REFRESH_GESTURE);//Refresh the page
        filter.addAction(Constants.WALLET_REFRESH_DEL);//Refresh the page
        filter.addAction(Constants.ACTION_GESTURE_LOGIN);//gesture success
        filter.addAction(Constants.REQUEST_INSTALL_PACKAGE);//gesture success
        registerReceiver(mBroadcastReceiver, filter);
        if (Utils.isConnectNet(MainFragmentUI.this) && NextApplication.myInfo != null && TextUtils.isEmpty(NextApplication.myInfo.getToken()) && TextUtils.isEmpty(NextApplication.myInfo.getMid()) && TextUtils.isEmpty(NextApplication.myInfo.getMobile()) && TextUtils.isEmpty(NextApplication.myInfo.getEmail())) {
            LoginUtil.getInstance().initContext(MainFragmentUI.this);
            LoginUtil.getInstance().register(NextApplication.myInfo.getUsername(), NextApplication.myInfo.getPassword(), null, NextApplication.myInfo.getLocalId(), null);
        }
    }


    /**
     * init data
     * */
    private void setService(Intent intent){
        if (intent != null && intent.getExtras() != null) {
            if(NextApplication.myInfo != null){
                if (intent.getExtras().getBoolean("isOfflineMsg", false)) {//jump main
                    onClick(main_tab_chats);
                    showOfflineAlert(intent.getExtras().getString("offlineContent"));
                    return;
                }

                if (intent.getExtras().getBoolean("isNewMsg", false)) {//jump message
                    onClick(main_tab_chats);
                }
            }
            showAnimation = intent.getBooleanExtra("showAnimation", false);
        }

        int walletMode = MySharedPrefs.readInt(MainFragmentUI.this, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_IS_WALLET_PATTERN);
        if (walletMode != 0) {
            onClick(main_tab_account);
        } else {
            main_tab_chats.setSelected(true);
            showFragment(MainMessageFragmentUI.class, false);
        }

        if (NextApplication.myInfo != null) {
            int smartMeshNetWork = MySharedPrefs.readInt1(NextApplication.mContext, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_NO_NETWORK_COMMUNICATION + NextApplication.myInfo.getLocalId());
            if (smartMeshNetWork == 1) {
                startService(new Intent(MainFragmentUI.this, AppNetService.class));
            } else if (walletMode == 0 && smartMeshNetWork == -1 ) {
                MySharedPrefs.writeInt(NextApplication.mContext, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_NO_NETWORK_COMMUNICATION + NextApplication.myInfo.getLocalId(), 1);
                startService(new Intent(MainFragmentUI.this, AppNetService.class));
            }
            startService(new Intent(MainFragmentUI.this, MeshService.class));
        }
        MySharedPrefs.writeBoolean(MainFragmentUI.this, MySharedPrefs.FILE_USER, MySharedPrefs.IS_SHOW_WALLET_DIALOG, false);

        if(NextApplication.myInfo != null){
            try {
                Uri parse = intent.getData();
                if (parse != null){
                    String gid = parse.getQueryParameter("gid");
                    String scheme = parse.getScheme();
                    if (!TextUtils.isEmpty(scheme) && "joingroup".equals(scheme)) {//join group
                        Intent joinGroup = new Intent(this, DiscussGroupJoinUI.class);
                        joinGroup.putExtra("groupid", gid);
                        startActivity(joinGroup);
                        Utils.openNewActivityAnim(this, false);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * get system notice
     * */
    private void getSystemNotice() {
        NetRequestImpl.getInstance().getSystemNotice(new RequestListener() {
            @Override
            public void start() {

            }

            @Override
            public void success(JSONObject response) {
                systemNotice = new SystemNotice().parse(response.optJSONObject("data"));
                if (systemNotice == null || noticeClockBody == null){
                    return;
                }

                if (TextUtils.isEmpty(systemNotice.getTitle())){
                    noticeClockBody.setVisibility(View.GONE);
                }else{
                    noticeClockBody.setVisibility(View.VISIBLE);
                    if (noticeTitle != null){
                        noticeTitle.setText(systemNotice.getTitle());
                    }
                }

            }

            @Override
            public void error(int errorCode, String errorMsg) {
                if (noticeClockBody != null){
                    noticeClockBody.setVisibility(View.GONE);
                }
            }
        });
    }

    private void registerReceive() {
        IntentFilter filter = new IntentFilter(XmppAction.ACTION_MESSAGE_EVENT_LISTENER);
        filter.addAction(XmppAction.ACTION_OFFLINE_MESSAGE_LIST_EVENT_LISTENER);
        msgReceiverListener = new MsgReceiverListener();
        registerReceiver(msgReceiverListener, filter);
    }
    
    class MsgReceiverListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && XmppAction.ACTION_MESSAGE_EVENT_LISTENER.equals(intent.getAction())) {
                Bundle bundle = intent.getBundleExtra(XmppAction.ACTION_MESSAGE_EVENT_LISTENER);
                if (bundle != null) {
                    ChatMsg msg = (ChatMsg) bundle.getSerializable("chat");
                    if (msg != null) {
                        if (TextUtils.equals("system-0", msg.getChatId()) && msg.getUnread() == 0)//已有的添加好友消息
                        {
                            return;
                        } else if (msg.getGroupMask() && !TextUtils.equals("system-0", msg.getChatId()) && !TextUtils.equals("system-1", msg.getChatId()) && !TextUtils.equals("system-3", msg.getChatId()) && !TextUtils.equals("system-4", msg.getChatId()) && !TextUtils.equals("system-5", msg.getChatId())) {//屏蔽的信息不与管理
                            return;
                        }
                    }
                    int unread = bundle.getInt("unread", 1000);
                    if (unread != 1000) {
                        totalUnreadCount = totalUnreadCount + unread;
                        Utils.formatUnreadCount(mMsgUnread, totalUnreadCount);
                        return;
                    }
                }
                
                Utils.formatUnreadCount(mMsgUnread, ++totalUnreadCount);
            } else if (intent != null && XmppAction.ACTION_OFFLINE_MESSAGE_LIST_EVENT_LISTENER.equals(intent.getAction())) {
                int totalCount = intent.getIntExtra("totalCount", 0);
                totalUnreadCount += totalCount;
                Utils.formatUnreadCount(mMsgUnread, totalUnreadCount);
            }
        }
    }
    
    private void getUnreadCount() {
        Map<String, Integer> map = FinalUserDataBase.getInstance().getUnreadMap();
        totalUnreadCount = map.get("totalunread");
        Utils.formatUnreadCount(mMsgUnread, totalUnreadCount);
    }
    
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null){
                switch (intent.getAction()){
                    case Constants.ACTION_GESTURE_LOGIN:
                    case Constants.WALLET_SUCCESS:
                    case Constants.WALLET_REFRESH_GESTURE:
                    case Constants.WALLET_REFRESH_DEL:
                        setStatusBarMethod();
                        break;
                    case LoadDataService.ACTION_START_CONTACT_LISTENER:
                        getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, mObserver);
                        break;
                    case XmppAction.ACTION_MAIN_UNREADMSG_UPDATE_LISTENER:
                        getUnreadCount();
                        break;
                    case XmppAction.ACTION_MAIN_OFFLINE_LISTENER:
                        Bundle bundle = intent.getBundleExtra(XmppAction.ACTION_MAIN_OFFLINE_LISTENER);
                        showOfflineAlert(bundle.getString("offlineContent"));
                        break;
                    case Constants.ACTION_NETWORK_RECEIVER:
                        loginMethod();
                        break;
                    case Constants.REQUEST_INSTALL_PACKAGE:
                        installPackage(intent);
                        break;
                }
            }
        }
    };

    /**
     * set status bar method
     * */
    private void setStatusBarMethod(){
        if (WalletStorage.getInstance(getApplicationContext()).get().size() > 0) {
            Utils.setStatusBar(MainFragmentUI.this,1);
            showFragment(AccountFragment.class, false);
        } else {
            Utils.setStatusBar(MainFragmentUI.this,0);
            showFragment(NewWalletFragment.class, false);
        }
        selectChanged(R.id.main_tab_account);
    }

    /**
     * install app method
     * */
    @SuppressLint("CheckResult")
    private void installPackage(Intent intent){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            String apkName = intent.getStringExtra("apkName");
            installFile = new File(Environment.getExternalStorageDirectory() + "/download/" + apkName);
            RxPermissions rxPermissions = new RxPermissions(MainFragmentUI.this);
            rxPermissions.request(Manifest.permission.REQUEST_INSTALL_PACKAGES).subscribe(new Consumer<Boolean>() {
                @Override
                public void accept(Boolean aBoolean) throws Exception {
                    if (aBoolean){
                        Utils.installApk8(MainFragmentUI.this,installFile);
                    }else{
                        MyToast.showToast(NextApplication.mContext,getString(R.string.open_permission_install_package));
                        Uri packageURI = Uri.parse("package:"+getPackageName());
                        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,packageURI);
                        startActivityForResult(intent, 200);
                    }
                }
            });
        }
    }

    /**
     * login app method
     * */
    private void loginMethod(){
        if (Constants.isConnectNet && NextApplication.myInfo != null) {
            String mobile = NextApplication.myInfo.getMobile();
            String email = NextApplication.myInfo.getEmail();
            String mid = NextApplication.myInfo.getMid();
            String token = NextApplication.myInfo.getToken();
            if (TextUtils.isEmpty(token)) {
                LoginUtil.getInstance().initContext(MainFragmentUI.this);
                if (!TextUtils.isEmpty(mid)) {
                    LoginUtil.getInstance().login(mid, NextApplication.myInfo.getPassword(), false);
                } else if (!TextUtils.isEmpty(mobile)) {
                    LoginUtil.getInstance().login(mobile, NextApplication.myInfo.getPassword(), false);
                } else if (!TextUtils.isEmpty(email)) {
                    LoginUtil.getInstance().login(email, NextApplication.myInfo.getPassword(), false);
                } else {
                    LoginUtil.getInstance().register(NextApplication.myInfo.getUsername(), NextApplication.myInfo.getPassword(), null, NextApplication.myInfo.getLocalId(), null);
                }
            }

        }
    }

    //The monitoring objects to monitor contact data
    private ContentObserver mObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            // When change contacts table for the corresponding operation
            /**Open directory upload service*/
            Utils.intentServiceAction(MainFragmentUI.this, LoadDataService.ACTION_UPLOAD_CONTACT, null);
        }
    };
    
    @OnClick({R.id.main_tab_chats,R.id.main_tab_contact,R.id.main_tab_account,R.id.main_tab_setting,R.id.noticeClockBody,R.id.noticeClose})
    public void onClick(View v) {
        int walletMode = MySharedPrefs.readInt(MainFragmentUI.this, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_IS_WALLET_PATTERN);
        switch (v.getId()) {
            case R.id.main_tab_chats:
                Utils.setStatusBar(MainFragmentUI.this,0);
                if (walletMode != 0 && NextApplication.myInfo == null) {
                    startActivity(new Intent(MainFragmentUI.this, WalletModeLoginUI.class));
                    Utils.openNewActivityAnim(this, false);
                } else {
                    showFragment(MainMessageFragmentUI.class, false);
                    selectChanged(v.getId());
                }
                break;
            case R.id.main_tab_contact:
                Utils.setStatusBar(MainFragmentUI.this,0);
                if (walletMode != 0 && NextApplication.myInfo == null) {
                    startActivity(new Intent(MainFragmentUI.this, WalletModeLoginUI.class));
                    Utils.openNewActivityAnim(this, false);
                } else {
                    showFragment(MainContactFragmentUI.class, false);
                    selectChanged(v.getId());
                }
                break;
            case R.id.main_tab_account:
                clickAccount(walletMode,v.getId());
                break;
            case R.id.main_tab_setting:
                Utils.setStatusBar(MainFragmentUI.this,1);
                if (walletMode != 0 && NextApplication.myInfo == null) {
                    startActivity(new Intent(MainFragmentUI.this, WalletModeLoginUI.class));
                    Utils.openNewActivityAnim(this, false);
                } else {
                    showFragment(MySelfFragment.class, false);
                    selectChanged(v.getId());
                }
                break;
            case R.id.noticeClockBody:
                if (systemNotice == null || TextUtils.isEmpty(systemNotice.getUrl())){
                    return;
                }
                Intent intent = new Intent(MainFragmentUI.this, WebViewUI.class);
                intent.putExtra("loadUrl", systemNotice.getUrl());
                intent.putExtra("title", systemNotice.getTitle());
                startActivity(intent);
                Utils.openBottomToTopActivityAnim(MainFragmentUI.this,false);
                break;
            case R.id.noticeClose:
                noticeClockBody.setVisibility(View.GONE);
                break;
        }
        XmppUtils.loginXmppForNextApp(this);
    }

    /**
     * click account
     * @param walletMode  1 wallet mode   other default
     * @param resId        res id
     * */
    private void clickAccount(int walletMode,int resId){
        if (walletMode == 1) {
            Utils.setStatusBar(MainFragmentUI.this,0);
            showFragment(NewWalletFragment.class, showAnimation);
            selectChanged(resId);
        } else {
            if (WalletStorage.getInstance(getApplicationContext()).get().size() > 0) {
                Utils.setStatusBar(MainFragmentUI.this,1);
                byte[] gestureByte;
                if (walletMode != 0 && NextApplication.myInfo == null) {
                    gestureByte = ACache.get(NextApplication.mContext).getAsBinary(Constants.GESTURE_PASSWORD);
                } else {
                    gestureByte = ACache.get(NextApplication.mContext).getAsBinary(Constants.GESTURE_PASSWORD + NextApplication.myInfo.getLocalId());
                }
                if (gestureByte != null && gestureByte.length > 0) {
                    startActivity(new Intent(MainFragmentUI.this, GestureLoginActivity.class));
                    Utils.openNewActivityAnim(MainFragmentUI.this, false);
                } else {
                    showFragment(AccountFragment.class, showAnimation);
                    selectChanged(resId);
                }
            } else {
                Utils.setStatusBar(MainFragmentUI.this,0);
                showFragment(NewWalletFragment.class, showAnimation);
                selectChanged(resId);
            }
        }
    }
    
    private void selectChanged(final int resId) {
        main_tab_chats.setSelected(false);
        main_tab_contact.setSelected(false);
        main_tab_account.setSelected(false);
        main_tab_setting.setSelected(false);
        main_tab_chats.setEnabled(true);
        main_tab_contact.setEnabled(true);
        main_tab_account.setEnabled(true);
        main_tab_setting.setEnabled(true);
        
        switch (resId) {
            case R.id.main_tab_chats:
                main_tab_chats.setSelected(true);
                main_tab_chats.setEnabled(false);
                break;
            case R.id.main_tab_contact:
                main_tab_contact.setSelected(true);
                main_tab_contact.setEnabled(false);
                break;
            case R.id.main_tab_setting:
                main_tab_setting.setSelected(true);
                main_tab_setting.setEnabled(false);
                break;
            case R.id.main_tab_account:
                main_tab_account.setSelected(true);
                main_tab_account.setEnabled(false);
                break;
        }
    }
    
    /**
     * show animation    true or false
     */
    public boolean getShowAnimation() {
        return showAnimation;
    }
    
    /**
     * Show the new fragments
     *
     * @param c
     */
    private void showFragment(Class<? extends BaseFragment> c, boolean showAnimation) {
        try {
            BaseFragment fragment;
            // If mStack greater than 0, the hidden
            if (mStack.size() > 0) {
                getSupportFragmentManager().beginTransaction().hide(map.get(mStack.pop())).commitAllowingStateLoss();
            }
            mStack.add(c.getSimpleName());
            // If it already exists before the show, if not, create new one
            if (map.containsKey(c.getSimpleName())) {
                fragment = map.get(c.getSimpleName());
                getSupportFragmentManager().beginTransaction().show(fragment).commitAllowingStateLoss();
            } else {
                fragment = c.newInstance();
                map.put(c.getSimpleName(), fragment);
                getSupportFragmentManager().beginTransaction().add(R.id.main_frame, fragment, c.getSimpleName()).commitAllowingStateLoss();
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        if (showAnimation) {
            mainBottomTab.setVisibility(View.GONE);
            Animation transanim = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    mainBottomTab.setVisibility(View.VISIBLE);
                    FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mainBottomTab.getLayoutParams();
                    lp.setMargins(0, (int) (Utils.dip2px(MainFragmentUI.this, 55) * (1 - interpolatedTime)), 0, 0);
                    mainBottomTab.requestLayout();
                }
                
                @Override
                public boolean willChangeBounds() {
                    return true;
                }
            };
            transanim.setDuration(1000);
            mainBottomTab.startAnimation(transanim);
        }
    }
    
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 100) {
            if (WalletStorage.getInstance(getApplicationContext()).get().size() > 0) {
                Utils.setStatusBar(MainFragmentUI.this,1);
                showFragment(AccountFragment.class, false);
            } else {
                Utils.setStatusBar(MainFragmentUI.this,0);
                showFragment(NewWalletFragment.class, false);
            }
        }else if (resultCode == RESULT_OK && requestCode == 200) {
            if (installFile != null){
                Utils.installApk8(MainFragmentUI.this,installFile);
            }
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (NextApplication.myInfo == null) {//When the application memory is being reclaimed myinfo is empty to copy
            NextApplication.myInfo = new UserInfoVo().readMyUserInfo(this);
        }
        registerReceive();
        XmppUtils.loginXmppForNextApp(this);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (msgReceiverListener != null) {
            unregisterReceiver(msgReceiverListener);
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
        LoginUtil.getInstance().destory();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.MAIN");
            intent.addCategory("android.intent.category.HOME");
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
