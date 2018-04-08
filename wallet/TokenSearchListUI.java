package com.lingtuan.firefly.wallet;

import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;
import com.lingtuan.firefly.wallet.vo.TokenVo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created on 2018/3/19.
 *
 */

public class TokenSearchListUI extends BaseActivity implements AdapterView.OnItemClickListener, TokenSearchListAdapter.AddTokenToList {

    private TextView emptyTextView;
    private TextView emptyTextViewTwo;
    private ImageView emptyImg;
    private RelativeLayout emptyRela;
    private ListView tokenListView = null;
    private TokenSearchListAdapter tokenListAdapter = null;
    private ArrayList<TokenVo> source = null ;
    private ArrayList<TokenVo> localSource;
    private EditText searchEdit;
    private String address;
    private TextView searchCancel;

    @Override
    protected void setContentView() {
        setContentView(R.layout.token_search_list_layout);
        getPassData();
    }

    private void getPassData() {
        address = getIntent().getStringExtra("address");
    }


    @Override
    protected void findViewById() {
        tokenListView = (ListView) findViewById(R.id.tokenListView);
        searchEdit = (EditText) findViewById(R.id.searchEdit);
        emptyRela = (RelativeLayout) findViewById(R.id.empty_like_rela);
        emptyTextView = (TextView) findViewById(R.id.empty_text);
        emptyTextViewTwo = (TextView) findViewById(R.id.empty_text_two);
        emptyImg = (ImageView) findViewById(R.id.empty_like_icon);
        searchCancel = (TextView) findViewById(R.id.searchCancel);
    }

    @Override
    protected void setListener() {
        searchCancel.setOnClickListener(this);
        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0){
                    loadTokenList(s.toString());
                }
            }
        });

        searchEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH){
                    Utils.hiddenKeyBoard(TokenSearchListUI.this);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void initData() {
        localSource = FinalUserDataBase.getInstance().getTokenListAll(address);
        source = new ArrayList<>();
        tokenListAdapter = new TokenSearchListAdapter(this, source,this);
        tokenListView.setAdapter(tokenListAdapter);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.searchCancel:
                Utils.exitActivityAndBackAnim(TokenSearchListUI.this,true);
                break;
        }
    }

    /**
     * get token list
     * */
    private void loadTokenList(String keyword){
        NetRequestImpl.getInstance().searchToken(keyword, address,new RequestListener() {
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
                        if (localSource != null){
                            for (int j = 0 ; j <localSource.size() ; j++){
                                if (TextUtils.equals(tokenVo.getContactAddress(),localSource.get(j).getContactAddress())){
                                    tokenVo.setChecked(true);
                                }
                            }
                        }
                        source.add(tokenVo);
                    }
                }
                tokenListAdapter.resetSource(source);
                checkListEmpty();
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                showToast(errorMsg);
                checkListEmpty();
            }
        });
    }

    /**
     * To test whether the current list is empty
     */
    private void checkListEmpty() {
        if(source == null || source.size() == 0){
            emptyRela.setVisibility(View.VISIBLE);
            emptyImg.setVisibility(View.VISIBLE);
            emptyTextViewTwo.setVisibility(View.VISIBLE);
            emptyImg.setImageResource(R.drawable.icon_token_empty);
            emptyTextView.setText("没找到匹配的结果？");
            emptyTextViewTwo.setText("试试在页面底部提交新token");
        }else{
            emptyRela.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void addTokenToList(final int position) {
        NetRequestImpl.getInstance().bindTokenToList(address, source.get(position).getContactAddress(), new RequestListener() {
            @Override
            public void start() {

            }

            @Override
            public void success(JSONObject response) {
                showToast(response.optString("msg"));
                FinalUserDataBase.getInstance().updateTokenList(source.get(position),address,true);
                source.get(position).setChecked(true);
                tokenListAdapter.resetSource(source);
                Intent intent = new Intent(Constants.WALLET_ADD_TOKEN);
                intent.putExtra("tokenVo",source.get(position));
                Utils.sendBroadcastReceiver(TokenSearchListUI.this, intent, false);
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                showToast(errorMsg);
            }
        });
    }
}
