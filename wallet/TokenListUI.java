package com.lingtuan.firefly.wallet;

import android.content.Intent;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
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

public class TokenListUI extends BaseActivity implements AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener, LoadMoreListView.RefreshListener {

    private TextView emptyTextView;
    private RelativeLayout emptyRela;

    private LoadMoreListView tokenListView = null;

    /** Refresh the controls */
    private SwipeRefreshLayout swipeLayout;

    private boolean isLoadingData = false;

    private TokenListAdapter tokenListAdapter = null;

    private TextView submitToken;

    private int currentPage = 1 ;
    private int oldPage=1;
    private ArrayList<TokenVo> source = null ;

    private String address;//wallet address

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

        emptyRela = (RelativeLayout) findViewById(R.id.empty_like_rela);
        emptyTextView = (TextView) findViewById(R.id.empty_text);
        submitToken = (TextView) findViewById(R.id.submitToken);
    }

    @Override
    protected void setListener() {
        tokenListView.setOnRefreshListener(this);
        swipeLayout.setOnRefreshListener(this);
        tokenListView.setOnItemClickListener(this);
        submitToken.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        setTitle("添加新资产");
        swipeLayout.setColorSchemeResources(R.color.black);
        source = new ArrayList<>();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(true);
                loadTokenList();
            }
        }, 500);
        tokenListAdapter = new TokenListAdapter(this, source);
        tokenListView.setAdapter(tokenListAdapter);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.submitToken:
                String result = "";
                String language = MySharedPrefs.readString(TokenListUI.this,MySharedPrefs.FILE_APPLICATION,MySharedPrefs.KEY_LANGUAFE);
                if (TextUtils.isEmpty(language)){
                    Locale locale = new Locale(Locale.getDefault().getLanguage());
                    if (TextUtils.equals(locale.getLanguage(),"zh")){
                        result = Constants.USE_AGREE_ZH;
                    }else{
                        result = Constants.USE_AGREE_EN;
                    }
                }else{
                    if (TextUtils.equals(language,"zh")){
                        result = Constants.USE_AGREE_ZH;
                    }else{
                        result = Constants.USE_AGREE_EN;
                    }
                }
                Intent intent = new Intent(TokenListUI.this, WebViewUI.class);
                intent.putExtra("loadUrl", result);
                intent.putExtra("title", getString(R.string.use_agreement));
                startActivity(intent);
                Utils.openNewActivityAnim(TokenListUI.this,false);
                break;
        }
    }

    /**
     * get token list
     * */
    private void loadTokenList(){
        boolean isFirstGet = MySharedPrefs.readBoolean(TokenListUI.this,MySharedPrefs.FILE_USER,MySharedPrefs.KEY_FIRST_GET_TOKEN_LIST);
        ArrayList<TokenVo> tokenList = FinalUserDataBase.getInstance().getTokenListAll(address);
        if (!isFirstGet && tokenList != null &&  tokenList.size() > 0){
            tokenListAdapter.resetSource(tokenList);
            swipeLayout.setRefreshing(false);
        }else{
            NetRequestImpl.getInstance().getTokenList(address, new RequestListener() {
                @Override
                public void start() {

                }

                @Override
                public void success(JSONObject response) {
                    source.clear();
                    JSONArray array = response.optJSONArray("data");
                    if (array != null){
                        for (int i = 0 ; i < array.length() ; i++){
                            TokenVo tokenVo = new TokenVo().parse(array.optJSONObject(i));
                            source.add(tokenVo);
                        }
                    }
                    FinalUserDataBase.getInstance().beginTransaction();
                    for (int i = 0 ; i < source.size() ; i++){
                        FinalUserDataBase.getInstance().updateTokenList(source.get(i),address);
                    }
                    FinalUserDataBase.getInstance().endTransactionSuccessful();
                    MySharedPrefs.writeBoolean(TokenListUI.this,MySharedPrefs.FILE_USER,MySharedPrefs.KEY_FIRST_GET_TOKEN_LIST,false);
                    tokenListAdapter.resetSource(source);
                    swipeLayout.setRefreshing(false);
                    checkListEmpty();
                }

                @Override
                public void error(int errorCode, String errorMsg) {
                    swipeLayout.setRefreshing(false);
                    showToast(errorMsg);
                    checkListEmpty();
                }
            });
        }

    }



    /**
     * To test whether the current list is empty
     */
    private void checkListEmpty() {
        if(source == null || source.size() == 0){
            emptyRela.setVisibility(View.VISIBLE);
            emptyTextView.setText("暂时没有新的Token");
        }else{
            emptyRela.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onRefresh() {
        loadTokenList();
    }

    @Override
    public void loadMore() {

    }
}
