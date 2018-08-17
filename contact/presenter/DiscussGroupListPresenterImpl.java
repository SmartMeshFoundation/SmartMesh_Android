package com.lingtuan.firefly.contact.presenter;

import com.lingtuan.firefly.contact.contract.DiscussGroupListContract;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.network.NetRequestImpl;
import com.lingtuan.firefly.vo.UserBaseVo;

import org.json.JSONObject;

import java.util.List;

public class DiscussGroupListPresenterImpl implements DiscussGroupListContract.Presenter {

    private DiscussGroupListContract.View mView;

    public DiscussGroupListPresenterImpl(DiscussGroupListContract.View view){
        this.mView = view;
        mView.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void createDiscussionGroup(String touids, final List<UserBaseVo> member) {
        NetRequestImpl.getInstance().createDiscussionGroups(touids, new RequestListener() {
            @Override
            public void start() {
                mView.createDiscussionGroupStart();
            }

            @Override
            public void success(JSONObject response) {
                mView.createDiscussionGroupSuccess(response.optString("msg"),response.optString("cid"), member);
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                mView.createDiscussionGroupError(errorCode,errorMsg);
            }
        });
    }

    @Override
    public void loadGroupList() {
        NetRequestImpl.getInstance().loadGroupList(new RequestListener() {
            @Override
            public void start() {

            }

            @Override
            public void success(JSONObject response) {
               mView.loadGroupListSuccess(response);
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                mView.loadGroupListError(errorCode,errorMsg);
            }
        });
    }
}
