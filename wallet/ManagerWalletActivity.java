package com.lingtuan.firefly.wallet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.util.Constants;
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
    @Override
    protected void setContentView() {
        setContentView(R.layout.wallet_manager_layout);
    }

    @Override
    protected void findViewById() {
        listView = (ListView) findViewById(R.id.list);
        walletNew = (TextView) findViewById(R.id.wallet_new);
        walletImport = (TextView) findViewById(R.id.wallet_import);
    }

    @Override
    protected void setListener() {
        listView.setOnItemClickListener(this);
        walletNew.setOnClickListener(this);
        walletImport.setOnClickListener(this);
    }

    @Override
    protected void initData() {

        setTitle(getString(R.string.account_manager));
        adapter = new ManagerWalletAdapter(this);
        listView.setAdapter(adapter);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.WALLET_REFRESH_DEL);
        filter.addAction(Constants.WALLET_SUCCESS);
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
            }else if (intent != null && (Constants.WALLET_SUCCESS.equals(intent.getAction()))){
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
