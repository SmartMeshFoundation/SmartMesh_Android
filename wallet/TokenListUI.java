package com.lingtuan.firefly.wallet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.custom.LoadMoreListView;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.setting.SettingUI;
import com.lingtuan.firefly.ui.WebViewUI;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;
import com.lingtuan.firefly.wallet.vo.TokenVo;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created on 2018/3/19.
 *
 */

public class TokenListUI extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener, LoadMoreListView.RefreshListener {

    private TextView emptyTextView;
    private RelativeLayout emptyRela;

    private LoadMoreListView tokenListView = null;

    /** Refresh the controls */
    private SwipeRefreshLayout swipeLayout;

    private TokenListAdapter tokenListAdapter = null;

    private ArrayList<TokenVo> source = null ;

    private String address;//wallet address

    private ImageView searchToken;

    @Override
    protected void setContentView() {
        setContentView(R.layout.token_list_layout);
        getPassData();
    }

    private void getPassData() {
        address = getIntent().getStringExtra("address");
    }

    @Override
    protected void findViewById() {
        tokenListView = (LoadMoreListView) findViewById(R.id.refreshListView);
        tokenListView.setOnScrollListener(new PauseOnScrollListener(NextApplication.mImageLoader, true, true));
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        searchToken = (ImageView) findViewById(R.id.app_right);

        emptyRela = (RelativeLayout) findViewById(R.id.empty_like_rela);
        emptyTextView = (TextView) findViewById(R.id.empty_text);
    }

    @Override
    protected void setListener() {
        tokenListView.setOnRefreshListener(this);
        swipeLayout.setOnRefreshListener(this);
        searchToken.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.WALLET_ADD_TOKEN);
        registerReceiver(mBroadcastReceiver, filter);
        setTitle(getString(R.string.token_add_assets));
        swipeLayout.setColorSchemeResources(R.color.black);
        searchToken.setVisibility(View.VISIBLE);
        searchToken.setImageResource(R.drawable.icon_token_search);
        source = new ArrayList<>();
        tokenListAdapter = new TokenListAdapter(this, source,address);
        tokenListView.setAdapter(tokenListAdapter);
        loadTokenList();
    }

    private BroadcastReceiver mBroadcastReceiver =  new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && (Constants.WALLET_ADD_TOKEN.equals(intent.getAction()))) {
                TokenVo tokenVo = (TokenVo) intent.getSerializableExtra("tokenVo");
                if (tokenVo != null){
                    tokenVo.setChecked(false);
                    source.add(tokenVo);
                }
                tokenListAdapter.resetSource(source);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.app_right:
                Intent searchIntent = new Intent(TokenListUI.this,TokenSearchListUI.class);
                searchIntent.putExtra("address",address);
                startActivity(searchIntent);
                Utils.openNewActivityAnim(TokenListUI.this,false);
                break;
        }
    }

    /**
     * get token list
     * */
    private void loadTokenList(){
        source = FinalUserDataBase.getInstance().getTokenListAll(address);
        tokenListAdapter.resetSource(source);
        swipeLayout.setRefreshing(false);
        checkListEmpty();
    }



    /**
     * To test whether the current list is empty
     */
    private void checkListEmpty() {
        if(source == null || source.size() == 0){
            emptyRela.setVisibility(View.VISIBLE);
            emptyTextView.setText(getString(R.string.token_submit_no_token));
        }else{
            emptyRela.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRefresh() {
        loadTokenList();
    }

    @Override
    public void loadMore() {

    }
}
