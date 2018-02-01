package com.lingtuan.firefly.setting;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.custom.gesturelock.ACache;
import com.lingtuan.firefly.custom.gesturelock.LockPatternUtil;
import com.lingtuan.firefly.custom.gesturelock.LockPatternView;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.wallet.util.WalletStorage;
import com.lingtuan.firefly.wallet.vo.StorableWallet;
import com.lingtuan.firefly.xmpp.XmppAction;

import java.util.ArrayList;
import java.util.List;
/**
 * gesture login
 */
public class GestureLoginActivity extends BaseActivity {

    private LockPatternView lockPatternView;
    private LinearLayout walletBody;
    private View gestureView;
    private TextView messageTv;
    private TextView forgetGestureBtn;
    private int type ; //0 login  1 open gesture  2 close gesture

    private ImageView walletImg;
    private TextView walletName;
    private TextView walletAddress;

    private ACache aCache;
    private static final long DELAYTIME = 600l;
    private byte[] gesturePassword;

    private int errorNum = 0;

    private int index = -1;//Which one is selected


    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_gesture_login);
        getPassData();
    }

    private void getPassData() {
        type = getIntent().getIntExtra("type",0);
    }

    @Override
    protected void findViewById() {
        lockPatternView = (LockPatternView) findViewById(R.id.lockPatternView);
        walletBody = (LinearLayout) findViewById(R.id.walletBody);
        forgetGestureBtn = (TextView) findViewById(R.id.forgetGestureBtn);
        messageTv = (TextView) findViewById(R.id.messageTv);
        walletImg = (ImageView) findViewById(R.id.walletImg);
        walletName = (TextView) findViewById(R.id.walletName);
        walletAddress = (TextView) findViewById(R.id.walletAddress);
        gestureView = findViewById(R.id.gestureView);
    }

    @Override
    protected void setListener() {
        forgetGestureBtn.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.gesture_checking));
        aCache = ACache.get(NextApplication.mContext);
        //get current pwd
        int walletMode = MySharedPrefs.readInt(GestureLoginActivity.this, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_IS_WALLET_PATTERN);
        if (walletMode != 0 && NextApplication.myInfo == null){
            gesturePassword = aCache.getAsBinary(Constants.GESTURE_PASSWORD);
        }else{
            gesturePassword = aCache.getAsBinary(Constants.GESTURE_PASSWORD + NextApplication.myInfo.getLocalId());
        }
        lockPatternView.setOnPatternListener(patternListener);
        updateStatus(Status.DEFAULT);
        initWalletInfo();
        if (type == 2){
            forgetGestureBtn.setText(getString(R.string.gesture_pwd_close));
        }else{
            forgetGestureBtn.setText(getString(R.string.gesture_forget_gesture));
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.WALLET_REFRESH_GESTURE);//Refresh the page
        registerReceiver(mBroadcastReceiver, filter);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && (Constants.WALLET_REFRESH_GESTURE.equals(intent.getAction()))) {
                initWalletInfo();
            }
        }
    };


    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.forgetGestureBtn:
                ArrayList<StorableWallet> storableWallets = WalletStorage.getInstance(NextApplication.mContext).get();
                if (storableWallets.size() <= 0){
                    showToast(getString(R.string.gesture_no_wallet));
                    return;
                }
                Intent intent = new Intent(GestureLoginActivity.this, GesturePasswordLoginActivity.class);
                intent.putExtra("type",type);
                startActivityForResult(intent,100);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
       unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100 && resultCode == RESULT_OK){
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private LockPatternView.OnPatternListener patternListener = new LockPatternView.OnPatternListener() {

        @Override
        public void onPatternStart() {
            lockPatternView.removePostClearPatternRunnable();
        }

        @Override
        public void onPatternComplete(List<LockPatternView.Cell> pattern) {
            if(pattern != null){
               int number =  MySharedPrefs.readInt(GestureLoginActivity.this,MySharedPrefs.FILE_USER,MySharedPrefs.GESTIRE_ERROR + NextApplication.myInfo.getLocalId());
                if (number > 5){
                    updateStatus(Status.ERRORMORE);
                }else{
                    if(LockPatternUtil.checkPattern(pattern, gesturePassword)) {
                        updateStatus(Status.CORRECT);
                    } else {
                        updateStatus(Status.ERROR);
                    }
                }
            }
        }
    };

    /**
     * update status
     * @param status
     */
    private void updateStatus(Status status) {
        messageTv.setText(status.strId);
        messageTv.setTextColor(getResources().getColor(status.colorId));
        switch (status) {
            case DEFAULT:
                lockPatternView.setPattern(LockPatternView.DisplayMode.DEFAULT);
                break;
            case ERROR:
                lockPatternView.setPattern(LockPatternView.DisplayMode.ERROR);
                lockPatternView.postClearPatternRunnable(DELAYTIME);
                errorNum ++;
                MySharedPrefs.writeInt(GestureLoginActivity.this,MySharedPrefs.FILE_USER,MySharedPrefs.GESTIRE_ERROR + NextApplication.myInfo.getLocalId(),errorNum);
                break;
            case ERRORMORE:
                lockPatternView.setPattern(LockPatternView.DisplayMode.ERROR);
                lockPatternView.postClearPatternRunnable(DELAYTIME);
                break;
            case CORRECT:
                lockPatternView.setPattern(LockPatternView.DisplayMode.DEFAULT);
                MySharedPrefs.writeInt(GestureLoginActivity.this,MySharedPrefs.FILE_USER,MySharedPrefs.GESTIRE_ERROR + NextApplication.myInfo.getLocalId(),0);
                loginGestureSuccess();
                break;
        }
    }

    /**
     * gesture success
     */
    private void loginGestureSuccess() {
        Intent intent = new Intent(Constants.ACTION_GESTURE_LOGIN);
        Utils.sendBroadcastReceiver(GestureLoginActivity.this,intent,false);
        int walletMode = MySharedPrefs.readInt(GestureLoginActivity.this, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_IS_WALLET_PATTERN);
        if (type == 2){
            if (walletMode == 0 && NextApplication.myInfo != null){
                aCache.put(Constants.GESTURE_PASSWORD + NextApplication.myInfo.getLocalId(),"");
            }else{
                aCache.put(Constants.GESTURE_PASSWORD,"");
            }
        }
        finish();
    }


    private enum Status {
        //defaut
        DEFAULT(R.string.create_gesture_default, R.color.textColorHint),
        //pwd error
        ERROR(R.string.gesture_error, R.color.colorRed),
        //pwd right
        CORRECT(R.string.gesture_correct, R.color.textColorHint),
        //error more
        ERRORMORE(R.string.gesture_error_login, R.color.colorRed);

        Status(int strId, int colorId) {
            this.strId = strId;
            this.colorId = colorId;
        }
        private int strId;
        private int colorId;
    }

    /**
     * Load or refresh the wallet information
     * */
    private void initWalletInfo(){
        StorableWallet storableWallet;
        ArrayList<StorableWallet> storableWallets = WalletStorage.getInstance(NextApplication.mContext).get();
        if (storableWallets.size() <= 0){
            walletBody.setVisibility(View.GONE);
            gestureView.setVisibility(View.VISIBLE);
        }else{
            gestureView.setVisibility(View.GONE);
            walletBody.setVisibility(View.VISIBLE);
        }
        for (int i = 0 ; i < storableWallets.size(); i++){
            if (storableWallets.get(i).isSelect() ){
                index = i;
                int imgId = Utils.getWalletImg(GestureLoginActivity.this,i);
                walletImg.setImageResource(imgId);
                storableWallet = storableWallets.get(i);
                storableWallet.setImgId(imgId);
                walletName.setText(storableWallet.getWalletName());
                String address = storableWallet.getPublicKey();
                if(!address.startsWith("0x")){
                    address = "0x"+address;
                }
                walletAddress.setText(address);
                break;
            }
        }
        if (index == -1 && storableWallets.size() > 0){
            int imgId = Utils.getWalletImg(GestureLoginActivity.this,0);
            walletImg.setImageResource(imgId);
            storableWallet = storableWallets.get(0);
            storableWallet.setImgId(imgId);
            walletName.setText(storableWallet.getWalletName());
            String address = storableWallet.getPublicKey();
            if(!address.startsWith("0x")){
                address = "0x"+address;
            }
            walletAddress.setText(address);
        }
    }


}
