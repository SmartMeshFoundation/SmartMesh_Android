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
import com.lingtuan.firefly.custom.gesturelock.LockPatternView;
import com.lingtuan.firefly.setting.contract.GestureLoginContract;
import com.lingtuan.firefly.setting.presenter.GestureLoginPresenterImpl;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.wallet.util.WalletStorage;
import com.lingtuan.firefly.wallet.vo.StorableWallet;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * gesture login
 */
public class GestureLoginActivity extends BaseActivity implements GestureLoginContract.View{

    @BindView(R.id.lockPatternView)
    LockPatternView lockPatternView;
    @BindView(R.id.walletBody)
    LinearLayout walletBody;
    @BindView(R.id.gestureView)
    View gestureView;
    @BindView(R.id.messageTv)
    TextView messageTv;
    @BindView(R.id.forgetGestureBtn)
    TextView forgetGestureBtn;
    @BindView(R.id.walletImg)
    ImageView walletImg;
    @BindView(R.id.walletName)
    TextView walletName;
    @BindView(R.id.walletAddress)
    TextView walletAddress;

    private GestureLoginContract.Presenter mPresenter;

    private int type ; //0 login  1 open gesture  2 close gesture
    private ACache aCache;
    private static final long DELAYTIME = 600l;
    private byte[] gesturePassword;

    private int errorNum = 0;

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

    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void initData() {
        new GestureLoginPresenterImpl(this);
        setTitle(getString(R.string.gesture_checking));
        aCache = ACache.get(NextApplication.mContext);
        gesturePassword = mPresenter.getAsBinary(aCache);
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


    @OnClick(R.id.forgetGestureBtn)
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
                Status status = mPresenter.getStatus(pattern,gesturePassword);
                updateStatus(status);
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
                mPresenter.putGestureErrorNum(errorNum);
                break;
            case ERRORMORE:
                lockPatternView.setPattern(LockPatternView.DisplayMode.ERROR);
                lockPatternView.postClearPatternRunnable(DELAYTIME);
                break;
            case CORRECT:
                lockPatternView.setPattern(LockPatternView.DisplayMode.DEFAULT);
                mPresenter.putGestureErrorNum(0);
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
        mPresenter.putAsBinary(aCache,type);
        finish();
    }

    @Override
    public void setPresenter(GestureLoginContract.Presenter presenter) {
        this.mPresenter = presenter;
    }


    public enum Status {
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
        StorableWallet storableWallet = mPresenter.initWalletInfo();
        if (storableWallet == null) {
            walletBody.setVisibility(View.GONE);
            gestureView.setVisibility(View.VISIBLE);
            return;
        }
        gestureView.setVisibility(View.GONE);
        walletBody.setVisibility(View.VISIBLE);
        walletImg.setImageResource(Utils.getWalletImageId(GestureLoginActivity.this,storableWallet.getWalletImageId()));
        walletName.setText(storableWallet.getWalletName());
        String address = storableWallet.getPublicKey();
        if(!address.startsWith("0x")){
            address = "0x"+address;
        }
        walletAddress.setText(address);
    }


}
