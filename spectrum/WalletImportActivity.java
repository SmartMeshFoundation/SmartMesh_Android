package com.lingtuan.firefly.spectrum;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.spectrum.fragment.WalletMnemonicFragment;
import com.lingtuan.firefly.spectrum.fragment.WalletOfficalFragment;
import com.lingtuan.firefly.spectrum.fragment.WalletPrivateFragment;
import com.lingtuan.firefly.spectrum.fragment.WalletScanFragment;
import com.lingtuan.firefly.ui.MainFragmentUI;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.Utils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * Created on 2017/8/21.
 * Import the wallet
 * {@link WalletCreateActivity}
 */

public class WalletImportActivity extends BaseActivity {

    @BindView(R.id.walletTabs)
    TabLayout walletTabs;
    @BindView(R.id.viewPager)
    ViewPager mViewPager;

    /*The official import purse wallet import a private key*/
    WalletMnemonicFragment walletMnemonicFragment;
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

    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void initData() {

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.WALLET_SUCCESS);
        filter.addAction(Constants.WALLET_ERROR);
        filter.addAction(Constants.NO_MEMORY);
        filter.addAction(Constants.WALLET_PWD_ERROR);
        filter.addAction(Constants.WALLET_REPEAT_ERROR);
        registerReceiver(mBroadcastReceiver, filter);

        setTitle(getString(R.string.wallet_import));
        setupViewPager();
        walletTabs.addTab(walletTabs.newTab().setText(getString(R.string.wallet_mnemonic)));
        walletTabs.addTab(walletTabs.newTab().setText(getString(R.string.wallet_official)));
        walletTabs.addTab(walletTabs.newTab().setText(getString(R.string.wallet_private_key)));
        walletTabs.addTab(walletTabs.newTab().setText(getString(R.string.wallet_scan)));
        walletTabs.setupWithViewPager(mViewPager);
        walletTabs.post(new Runnable() {
            @Override
            public void run() {
                Utils.setIndicator(walletTabs,8,8);
//                Utils.setIndicator(walletTabs,30,30);
            }
        });
    }

    /**
     * Set the ViewPager content
     * */
    private void setupViewPager() {
        walletMnemonicFragment = new WalletMnemonicFragment();
        walletOfficalFragment = new WalletOfficalFragment();
        walletPrivateFragment = new WalletPrivateFragment();
        walletScanFragment = new WalletScanFragment();
        frameList.add(walletMnemonicFragment);
        frameList.add(walletOfficalFragment);
        frameList.add(walletPrivateFragment);
        frameList.add(walletScanFragment);
        frameTitle.add(getString(R.string.wallet_mnemonic));
        frameTitle.add(getString(R.string.wallet_official));
        frameTitle.add(getString(R.string.wallet_private_key));
        frameTitle.add(getString(R.string.wallet_scan));
        mPagerAdapter = new SlidePagerAdapter(getSupportFragmentManager(),frameList,frameTitle);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOffscreenPageLimit(4);
        mViewPager.setCurrentItem(0);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && (Constants.WALLET_SUCCESS.equals(intent.getAction()))) {
                createWalletSuccess();
            }else if (intent != null && (Constants.WALLET_ERROR.equals(intent.getAction()))) {
                LoadingDialog.close();
                showToast(getString(R.string.notification_wallimp_failure));
            }else if (intent != null && (Constants.WALLET_PWD_ERROR.equals(intent.getAction()))) {
                LoadingDialog.close();
                showToast(getString(R.string.wallet_copy_pwd_error));
            }else if (intent != null && (Constants.WALLET_REPEAT_ERROR.equals(intent.getAction()))) {
                LoadingDialog.close();
                showToast(getString(R.string.notification_wallimp_repeat));
            }else if (intent != null && (Constants.NO_MEMORY.equals(intent.getAction()))) {
                LoadingDialog.close();
                showToast(getString(R.string.notification_wallgen_no_memory));
            }
        }
    };

    private void createWalletSuccess(){
        LoadingDialog.close();
        showToast(getString(R.string.notification_wallimp_finished));
        Intent intent1 = new Intent(WalletImportActivity.this,MainFragmentUI.class);
        setResult(RESULT_OK,intent1);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }
}
