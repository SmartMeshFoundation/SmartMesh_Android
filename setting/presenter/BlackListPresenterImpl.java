package com.lingtuan.firefly.setting.presenter;


import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.network.NetRequestImpl;
import com.lingtuan.firefly.setting.BlackListUI;
import com.lingtuan.firefly.setting.contract.BlackListContract;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.vo.UserInfoVo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BlackListPresenterImpl implements BlackListContract.Presenter{

    private BlackListContract.View mView;

    public BlackListPresenterImpl(BlackListContract.View view){
        this.mView = view;
        mView.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void getBlackList(final int page,final List<UserInfoVo> source) {

        NetRequestImpl.getInstance().getBlackList(page, new RequestListener() {
            @Override
            public void start() {

            }

            @Override
            public void success(JSONObject response) {
                if (page == 1) {
                    source.clear();
                }
                JSONArray jsonArray = response.optJSONArray("data");
                if (jsonArray != null) {
                    int count = jsonArray.length();
                    for (int i = 0; i < count; i++) {
                        UserInfoVo uInfo = new UserInfoVo().parse(jsonArray.optJSONObject(i));
                        source.add(uInfo);
                    }
                    if (jsonArray.length()>=10) {
                        mView.getBlackSuccess(source,page,true);
                    } else {
                        mView.getBlackSuccess(source,page,false);
                    }
                } else {
                    mView.getBlackError(0,"");
                }
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                mView.getBlackError(errorCode,errorMsg);
            }
        });
    }

    @Override
    public void updateBlackState(int state, final int position, String blocalid) {
        NetRequestImpl.getInstance().updateBlackState(state,blocalid, new RequestListener() {
            @Override
            public void start() {
                mView.updateBlackStart();
            }

            @Override
            public void success(JSONObject response) {
                mView.updateBlackSuccess(response.optString("msg"),position);
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                mView.updateBlackError(errorCode,errorMsg);
            }
        });
    }
}
