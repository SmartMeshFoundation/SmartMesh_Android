package com.lingtuan.firefly.wallet;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.wallet.fragment.WalletOfficalFragment;
import com.lingtuan.firefly.wallet.fragment.WalletPrivateFragment;
import com.lingtuan.firefly.wallet.fragment.WalletScanFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2017/8/21.
 * 导入钱包
 * {@link WalletCreateActivity}
 */

public class WalletImportActivity extends BaseActivity {

    TabLayout walletTabs;
    ViewPager mViewPager;

    /*官方钱包导入   私钥导入钱包*/
    WalletOfficalFragment walletOfficalFragment;
    WalletPrivateFragment walletPrivateFragment;
    WalletScanFragment walletScanFragment;
    private ArrayList<Fragment> frameList = new ArrayList<>();
    private List<String> frameTitle = new ArrayList<>();
    private SlidePagerAdapter mPagerAdapter;

    @Override
    protected void setContentView() {
        setContentView(R.layout.wallet_import_layout);
    }

    @Override
    protected void findViewById() {
        walletTabs = (TabLayout) findViewById(R.id.walletTabs);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.wallet_import));
        setupViewPager();
        walletTabs.addTab(walletTabs.newTab().setText(getString(R.string.wallet_official)));
        walletTabs.addTab(walletTabs.newTab().setText(getString(R.string.wallet_private_key)));
        walletTabs.addTab(walletTabs.newTab().setText(getString(R.string.wallet_scan)));
        walletTabs.setupWithViewPager(mViewPager);
        walletTabs.post(new Runnable() {
            @Override
            public void run() {
                Utils.setIndicator(walletTabs,30,30);
            }
        });
    }

    /**
     * 设置ViewPager内容
     * */
    private void setupViewPager() {
        walletOfficalFragment = new WalletOfficalFragment();
        walletPrivateFragment = new WalletPrivateFragment();
        walletScanFragment = new WalletScanFragment();
        frameList.add(walletOfficalFragment);
        frameList.add(walletPrivateFragment);
        frameList.add(walletScanFragment);
        frameTitle.add(getString(R.string.wallet_official));
        frameTitle.add(getString(R.string.wallet_private_key));
        frameTitle.add(getString(R.string.wallet_scan));
        mPagerAdapter = new SlidePagerAdapter(getSupportFragmentManager(),frameList,frameTitle);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setCurrentItem(0);
    }

}
