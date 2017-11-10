package com.lingtuan.firefly.wallet.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.base.BaseFragment;
import com.lingtuan.firefly.login.LoginUtil;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.wallet.WalletCreateActivity;
import com.lingtuan.firefly.wallet.WalletImportActivity;




public class NewWalletFragment extends BaseFragment implements View.OnClickListener {

    
    private View view = null;
    private boolean isDataFirstLoaded;

    private TextView createWallet,importWallet;
    private TextView title;

    
    private LinearLayout syncblockBg;
    private TextView syncblockText;
    private TextView syncblockBtn;

   
    private LinearLayout syncMaskBg;
    private TextView syncMaskBtn;
    private ImageView syncMaskOk;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        isDataFirstLoaded = true;
        super.onCreate(savedInstanceState);
        setRetainInstance(false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (!isDataFirstLoaded && view != null) {
            ((ViewGroup) view.getParent()).removeView(view);
            return view;
        }
        view = inflater.inflate(R.layout.wallet_new_activity,container,false);
        findViewById();
        setListener();
        initData();
        initSyncBlockState();
        return view;
    }



    protected void findViewById() {
        createWallet = (TextView) view.findViewById(R.id.createWallet);
        importWallet = (TextView) view.findViewById(R.id.importWallet);
        title = (TextView) view.findViewById(R.id.app_title);

        syncblockBg = (LinearLayout) view.findViewById(R.id.syncblockBg);
        syncblockText = (TextView) view.findViewById(R.id.syncblockText);
        syncblockBtn = (TextView) view.findViewById(R.id.syncblockBtn);

        syncMaskBg = (LinearLayout) view.findViewById(R.id.syncMaskBg);
        syncMaskBtn = (TextView) view.findViewById(R.id.syncMaskBtn);
        syncMaskOk = (ImageView) view.findViewById(R.id.syncMaskOk);
    }

    protected void setListener() {
        syncMaskBtn.setOnClickListener(this);
        syncMaskOk.setOnClickListener(this);
        createWallet.setOnClickListener(this);
        importWallet.setOnClickListener(this);
        syncblockBg.setOnClickListener(this);
        view.findViewById(R.id.maskLink).setOnClickListener(this);
    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.createWallet:
                startActivityForResult(new Intent(getActivity(),WalletCreateActivity.class),100);
                break;
            case R.id.importWallet:
                startActivityForResult(new Intent(getActivity(),WalletImportActivity.class),100);
                break;
            case R.id.syncblockBg:
                int state = MySharedPrefs.readInt(getActivity(),MySharedPrefs.FILE_USER,MySharedPrefs.AGREE_SYNC_BLOCK);
                if(state == 0){
                    state = 1;
                    MySharedPrefs.writeInt(getActivity(),MySharedPrefs.FILE_USER,MySharedPrefs.AGREE_SYNC_BLOCK,state);
                    syncblockText.setText(getString(R.string.wallet_sync_ing,0,0));
                    syncblockBtn.setVisibility(View.GONE);
                    NextApplication application = (NextApplication) getActivity().getApplication();
                    application.startSync(state);
                }
                break;
            case R.id.syncMaskBtn:
                MySharedPrefs.writeBoolean(getActivity(),MySharedPrefs.FILE_USER,MySharedPrefs.AGREE_SYNC_BLOCK_MASK,true);
                syncMaskBg.setVisibility(View.GONE);
                int syncState = MySharedPrefs.readInt(getActivity(),MySharedPrefs.FILE_USER,MySharedPrefs.AGREE_SYNC_BLOCK);
                if(syncState == 0){
                    syncState = 1;
                    MySharedPrefs.writeInt(getActivity(),MySharedPrefs.FILE_USER,MySharedPrefs.AGREE_SYNC_BLOCK,syncState);
                    syncblockText.setText(getString(R.string.wallet_sync_ing,0,0));
                    syncblockBtn.setVisibility(View.GONE);
                    NextApplication application = (NextApplication) getActivity().getApplication();
                    application.startSync(syncState);
                }
                initSyncBlockState();
                break;
            case R.id.syncMaskOk:
                MySharedPrefs.writeBoolean(getActivity(),MySharedPrefs.FILE_USER,MySharedPrefs.AGREE_SYNC_BLOCK_MASK,true);
                syncMaskBg.setVisibility(View.GONE);
                initSyncBlockState();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected void initData() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.CHANGE_LANGUAGE);
        filter.addAction(Constants.SYNC_PROGRESS);
        getActivity().registerReceiver(mBroadcastReceiver, filter);
        view.findViewById(R.id.app_back).setVisibility(View.GONE);
        title.setText(getString(R.string.app_name));

        boolean hasClick = MySharedPrefs.readBooleanNormal(getActivity(),MySharedPrefs.FILE_USER,MySharedPrefs.AGREE_SYNC_BLOCK_MASK);
        int state = MySharedPrefs.readInt(getActivity(),MySharedPrefs.FILE_USER,MySharedPrefs.AGREE_SYNC_BLOCK);
        if (state == 0 && !hasClick){
            syncMaskBg.setVisibility(View.VISIBLE);
            syncblockBtn.setVisibility(View.GONE);
        }else{
            syncMaskBg.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mBroadcastReceiver);
        LoginUtil.getInstance().destory();
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && (Constants.CHANGE_LANGUAGE.equals(intent.getAction()))) {
                Utils.updateViewLanguage(view.findViewById(android.R.id.content));
            }else if (intent != null && (Constants.SYNC_PROGRESS.equals(intent.getAction()))) {
                long number = intent.getLongExtra("currentNumber",0);
                long totalNumber = intent.getLongExtra("totalNumber",0);
                syncblockText.setText(getString(R.string.wallet_sync_ing,number,totalNumber));
                if(number == 0 ||  number >= totalNumber){
                    syncblockBg.setVisibility(View.GONE);
                }
            }
        }
    };

    private void  initSyncBlockState(){
        int state = MySharedPrefs.readInt(getActivity(),MySharedPrefs.FILE_USER,MySharedPrefs.AGREE_SYNC_BLOCK);
        if(state == 0){
            syncblockBtn.setVisibility(View.VISIBLE);
            syncblockBg.setVisibility(View.VISIBLE);
            syncblockText.setText(getString(R.string.wallet_sync_start));
        }else if (state == 1){
            syncblockBtn.setVisibility(View.GONE);
            syncblockBg.setVisibility(View.VISIBLE);
            syncblockText.setText(getString(R.string.wallet_sync_ing,0,0));
        }else{
            syncblockBg.setVisibility(View.GONE);
        }
    }
}
