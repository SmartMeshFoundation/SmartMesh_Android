package com.lingtuan.firefly.wallet.presenter;

import android.text.TextUtils;

import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.network.NetRequestImpl;
import com.lingtuan.firefly.wallet.contract.TokenSearchContract;
import com.lingtuan.firefly.wallet.vo.TokenVo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class TokenSearchPresenterImpl implements TokenSearchContract.Presenter{

    private TokenSearchContract.View mView;
    private ArrayList<TokenVo> source ;

    public TokenSearchPresenterImpl(TokenSearchContract.View view){
        this.mView = view;
        source = new ArrayList<>();
        mView.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void searchToken(String keyword, String address,final ArrayList<TokenVo> localSource) {
        NetRequestImpl.getInstance().searchToken(keyword, address, new RequestListener() {
            @Override
            public void start() {

            }

            @Override
            public void success(JSONObject response) {
                source.clear();
                JSONArray array = response.optJSONArray("data");
                if (array != null) {
                    for (int i = 0; i < array.length(); i++) {
                        TokenVo tokenVo = new TokenVo().parse(array.optJSONObject(i));
                        if (localSource != null) {
                            for (int j = 0; j < localSource.size(); j++) {
                                if (TextUtils.equals(tokenVo.getContactAddress(), localSource.get(j).getContactAddress())) {
                                    tokenVo.setChecked(true);
                                }
                            }
                        }
                        source.add(tokenVo);
                    }
                }
                mView.searchTokenSuccess(source);
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                mView.searchTokenError(errorCode,errorMsg);
            }
        });
    }

    @Override
    public void bindTokenToList(String address, String token_address,final int position) {
        NetRequestImpl.getInstance().bindTokenToList(address, token_address, new RequestListener() {
            @Override
            public void start() {
                mView.bindTokenToListStart();
            }

            @Override
            public void success(JSONObject response) {
                mView.bindTokenToListSuccess(response.optString("msg"),position);
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                mView.bindTokenToListError(errorCode,errorMsg);
            }
        });
    }
}
