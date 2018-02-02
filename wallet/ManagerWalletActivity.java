package com.lingtuan.firefly.wallet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.setting.GesturePasswordLoginActivity;
import com.lingtuan.firefly.ui.MainFragmentUI;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.wallet.util.WalletStorage;

/**
 * Created by cyt on 2017/8/23.
 */

public class ManagerWalletActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    ManagerWalletAdapter adapter;
    ListView listView;
    TextView walletNew;
    TextView walletImport;
    TextView walletLogin;
    @Override
    protected void setContentView() {
        setContentView(R.layout.wallet_manager_layout);
    }

    @Override
    protected void findViewById() {
        listView = (ListView) findViewById(R.id.list);
        walletNew = (TextView) findViewById(R.id.wallet_new);
        walletImport = (TextView) findViewById(R.id.wallet_import);
        walletLogin = (TextView) findViewById(R.id.wallet_login);
    }

    @Override
    protected void setListener() {
        listView.setOnItemClickListener(this);
        walletNew.setOnClickListener(this);
        walletImport.setOnClickListener(this);
        walletLogin.setOnClickListener(this);
    }

    @Override
    protected void initData() {

        setTitle(getString(R.string.account_manager));
        adapter = new ManagerWalletAdapter(this);
        listView.setAdapter(adapter);

        int walletMode = MySharedPrefs.readInt(ManagerWalletActivity.this, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_IS_WALLET_PATTERN);
        if (walletMode != 0 && NextApplication.myInfo == null){
            walletNew.setText(getString(R.string.wallet_create_new));
            walletImport.setText(getString(R.string.wallet_import_new));
            walletLogin.setText(getString(R.string.login));
            walletLogin.setVisibility(View.VISIBLE);
        }else{
            walletNew.setText(getString(R.string.wallet_create));
            walletImport.setText(getString(R.string.wallet_import));
            walletLogin.setVisibility(View.GONE);
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.WALLET_REFRESH_DEL);//Refresh the page
        filter.addAction(Constants.WALLET_SUCCESS);//Refresh the page
        filter.addAction(Constants.ACTION_GESTURE_LOGIN);//Refresh the page
        registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent copyIntent = new Intent(this,WalletCopyActivity.class);
        int imgId = Utils.getWalletImg(ManagerWalletActivity.this,position);
        copyIntent.putExtra(Constants.WALLET_INFO, WalletStorage.getInstance(getApplicationContext()).get().get(position));
        copyIntent.putExtra(Constants.WALLET_ICON, imgId);
        copyIntent.putExtra(Constants.WALLET_TYPE, 1);

        startActivity(copyIntent);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && (Constants.WALLET_REFRESH_DEL.equals(intent.getAction()))) {
                if (WalletStorage.getInstance(getApplicationContext()).get().size() <=0){
                    finish();
                }else{
                    adapter.notifyDataSetChanged();
                }
            }else if (intent != null && (Constants.WALLET_SUCCESS.equals(intent.getAction())) || Constants.ACTION_GESTURE_LOGIN.equals(intent.getAction())){
                adapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    public void onClick(View v) {
       switch (v.getId())
       {
           case R.id.wallet_new:
               startActivity(new Intent(this,WalletCreateActivity.class));
               break;
           case R.id.wallet_import:
               startActivity(new Intent(this,WalletImportActivity.class));
               break;
           case R.id.wallet_login:
               Intent intent = new Intent(this,GesturePasswordLoginActivity.class);
               intent.putExtra("type",4);
               startActivity(intent);
               Utils.openNewActivityAnim(this,false);
               break;
           default:
               super.onClick(v);
               break;
       }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }
}
