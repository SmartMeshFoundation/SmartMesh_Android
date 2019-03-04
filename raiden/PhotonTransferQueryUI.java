package com.lingtuan.firefly.raiden;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.raiden.fragment.PhotonContractCallFragment;
import com.lingtuan.firefly.raiden.fragment.PhotonTransferListFragment;
import com.lingtuan.firefly.raiden.vo.TxTypeStr;
import com.lingtuan.firefly.spectrum.SlidePagerAdapter;
import com.lingtuan.firefly.spectrum.vo.StorableWallet;
import com.lingtuan.firefly.spectrum.vo.TokenVo;
import com.lingtuan.firefly.util.Utils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class PhotonTransferQueryUI extends BaseActivity {

    @BindView(R.id.photonTransferTabs)
    TabLayout photonTransferTabs;
    @BindView(R.id.viewPager)
    ViewPager mViewPager;

    PhotonTransferListFragment transferListFragment;
    PhotonContractCallFragment contractCallFragment;

    private ArrayList<Fragment> frameList = new ArrayList<>();
    private List<String> frameTitle = new ArrayList<>();
    private SlidePagerAdapter mPagerAdapter;

    private boolean showContract = false;
    private int fromType;//0 转账页面 1 通道列表 2 存款
    private StorableWallet storableWallet;
    private TokenVo mTokenVo;

    @Override
    protected void setContentView() {
        setContentView(R.layout.photon_transfer_query_layout);
        getPassData();
    }

    private void getPassData() {
        storableWallet = (StorableWallet) getIntent().getSerializableExtra("storableWallet");
        mTokenVo = (TokenVo) getIntent().getSerializableExtra("tokenVo");
        showContract = getIntent().getBooleanExtra("showContract",false);
        fromType = getIntent().getIntExtra("fromType",-1);
    }

    @Override
    protected void findViewById() {

    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void initData() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(PhotonUrl.ACTION_PHOTON_NOTIFY_CALL_CONTRACT_INFO);
        registerReceiver(receiver, filter);
        setTitle(getString(R.string.photon_transfer_title));
        setupViewPager();
        photonTransferTabs.addTab(photonTransferTabs.newTab().setText(getString(R.string.photon)));
        photonTransferTabs.addTab(photonTransferTabs.newTab().setText(getString(R.string.mapping_spectrum)));
        photonTransferTabs.setupWithViewPager(mViewPager);
        if (showContract){
            mViewPager.setCurrentItem(1);
        }
    }

    /**
     * Set the ViewPager content
     * */
    private void setupViewPager() {
        transferListFragment = new PhotonTransferListFragment();
        contractCallFragment = new PhotonContractCallFragment();
        frameList.add(transferListFragment);
        frameList.add(contractCallFragment);
        frameTitle.add(getString(R.string.photon));
        frameTitle.add(getString(R.string.mapping_spectrum));
        mPagerAdapter = new SlidePagerAdapter(getSupportFragmentManager(),frameList,frameTitle);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setCurrentItem(0);
        photonTransferTabs.post(new Runnable() {
            @Override
            public void run() {
                Utils.setIndicator(photonTransferTabs,50,50);
            }
        });
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && (PhotonUrl.ACTION_PHOTON_NOTIFY_CALL_CONTRACT_INFO.equals(intent.getAction()))) {
                String txType = intent.getStringExtra("type");
                if (contractCallFragment != null && !TextUtils.equals(txType,TxTypeStr.ApproveDeposit.name())){
                    contractCallFragment.onRefresh();
                    if (fromType == 0){
                        Intent photonIntent = new Intent(PhotonTransferQueryUI.this, PhotonChannelList.class);
                        photonIntent.putExtra("storableWallet", storableWallet);
                        photonIntent.putExtra("tokenVo", mTokenVo);
                        photonIntent.putExtra("type", 1);
                        startActivity(photonIntent);
                        Utils.openNewActivityAnim(PhotonTransferQueryUI.this, true);
                    }else{
                        finish();
                    }
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}
