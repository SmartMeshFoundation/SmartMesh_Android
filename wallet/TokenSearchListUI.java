package com.lingtuan.firefly.wallet;

import android.content.Intent;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.language.LanguageType;
import com.lingtuan.firefly.language.MultiLanguageUtil;
import com.lingtuan.firefly.ui.WebViewUI;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.wallet.contract.TokenSearchContract;
import com.lingtuan.firefly.wallet.presenter.TokenSearchPresenterImpl;
import com.lingtuan.firefly.wallet.vo.TokenVo;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.OnTextChanged;

/**
 * Created on 2018/3/19.
 */

public class TokenSearchListUI extends BaseActivity implements AdapterView.OnItemClickListener, TokenSearchListAdapter.AddTokenToList,TokenSearchContract.View {

    @BindView(R.id.empty_like_icon)
    ImageView emptyImg;
    @BindView(R.id.empty_text)
    TextView emptyTextView;
    @BindView(R.id.empty_text_two)
    TextView emptyTextViewTwo;
    @BindView(R.id.empty_like_rela)
    RelativeLayout emptyRela;
    @BindView(R.id.tokenListView)
    ListView tokenListView;

    private TokenSearchContract.Presenter mPresenter;

    private TokenSearchListAdapter tokenListAdapter = null;
    private ArrayList<TokenVo> source = null;
    private ArrayList<TokenVo> localSource;
    private String address;

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

    }

    @Override
    protected void setListener() {
    }

    @OnEditorAction(R.id.searchEdit)
    boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            Utils.hiddenKeyBoard(TokenSearchListUI.this);
            return true;
        }
        return false;
    }

    @OnTextChanged(value = {R.id.searchEdit},callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void afterTextChanged(Editable s) {
        if (s.length() > 0) {
            mPresenter.searchToken(s.toString(),address,localSource);
        }
    }

    @Override
    protected void initData() {

        new TokenSearchPresenterImpl(this);

        localSource = FinalUserDataBase.getInstance().getTokenListAll(address);
        source = new ArrayList<>();
        tokenListAdapter = new TokenSearchListAdapter(this, source, this);
        tokenListView.setAdapter(tokenListAdapter);
    }


    @OnClick({R.id.searchCancel,R.id.submitToken})
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.searchCancel:
                Utils.exitActivityAndBackAnim(TokenSearchListUI.this, true);
                break;
            case R.id.submitToken:
                String result = "";
                int language = MultiLanguageUtil.getInstance().getLanguageType();
                if (LanguageType.LANGUAGE_CHINESE_SIMPLIFIED == language) {
                    result = Constants.SUBMIT_TOKEN_ZH;
                } else {
                    result = Constants.SUBMIT_TOKEN_EN;
                }
                Intent intent = new Intent(TokenSearchListUI.this, WebViewUI.class);
                intent.putExtra("loadUrl", result);
                intent.putExtra("title", getString(R.string.token_submit_token));
                startActivity(intent);
                Utils.openNewActivityAnim(TokenSearchListUI.this, false);
                break;
        }
    }

    /**
     * To test whether the current list is empty
     */
    private void checkListEmpty() {
        if (source == null || source.size() == 0) {
            emptyRela.setVisibility(View.VISIBLE);
            emptyImg.setVisibility(View.VISIBLE);
            emptyTextViewTwo.setVisibility(View.VISIBLE);
            emptyImg.setImageResource(R.drawable.icon_token_empty);
            emptyTextView.setText(getString(R.string.token_search_empty_hint));
            emptyTextViewTwo.setText(getString(R.string.token_search_empty_submit));
        } else {
            emptyRela.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void addTokenToList(final int position) {
        mPresenter.bindTokenToList(address,source.get(position).getContactAddress(),position);
    }

    @Override
    public void setPresenter(TokenSearchContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public void searchTokenSuccess(ArrayList<TokenVo> tempSource) {
        source.clear();
        source.addAll(tempSource);
        tokenListAdapter.resetSource(source);
        checkListEmpty();
    }

    @Override
    public void searchTokenError(int errorCode, String errorMsg) {
        showToast(errorMsg);
        checkListEmpty();
    }

    @Override
    public void bindTokenToListStart() {
        LoadingDialog.show(this,"");
    }

    @Override
    public void bindTokenToListSuccess(String message, int position) {
        LoadingDialog.close();
        showToast(message);
        FinalUserDataBase.getInstance().updateTokenList(source.get(position), address, true);
        source.get(position).setChecked(true);
        tokenListAdapter.resetSource(source);
        Intent intent = new Intent(Constants.WALLET_ADD_TOKEN);
        intent.putExtra("tokenVo", source.get(position));
        Utils.sendBroadcastReceiver(TokenSearchListUI.this, intent, false);
    }

    @Override
    public void bindTokenToListError(int errorCode, String errorMsg) {
        LoadingDialog.close();
        showToast(errorMsg);
    }
}
