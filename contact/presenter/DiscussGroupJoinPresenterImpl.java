package com.lingtuan.firefly.contact.presenter;

import android.text.TextUtils;

import com.lingtuan.firefly.contact.contract.DiscussGroupJoinContract;
import com.lingtuan.firefly.contact.vo.DiscussionGroupsVo;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.network.NetRequestImpl;
import com.lingtuan.firefly.vo.UserBaseVo;

import org.json.JSONObject;

import java.util.ArrayList;

public class DiscussGroupJoinPresenterImpl implements DiscussGroupJoinContract.Presenter {

    private DiscussGroupJoinContract.View mView;

    public DiscussGroupJoinPresenterImpl(DiscussGroupJoinContract.View view){
        this.mView = view;
        mView.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void getDiscussMembers(String cid,final ArrayList<UserBaseVo> data) {
        if(TextUtils.isEmpty(cid)){
            return;
        }
        NetRequestImpl.getInstance().getDiscussMumbers(cid, new RequestListener() {
            @Override
            public void start() {

            }

            @Override
            public void success(JSONObject response) {
                JSONObject jsonObject = response.optJSONObject("data");
                DiscussionGroupsVo vo = new DiscussionGroupsVo().parse(jsonObject);
                data.clear();
                if (vo.getMembers() != null){
                    data.addAll(vo.getMembers());
                }
                mView.getDiscussMembersSuccess(vo,data);
            }

            @Override
            public void error(int errorCode, String errorMsg) {
               mView.getDiscussMembersError(errorCode,errorMsg);
            }
        });
    }

    @Override
    public void joinDiscussGroup(String cid, ArrayList<UserBaseVo> data) {
        if(TextUtils.isEmpty(cid)){
            return;
        }

        if (data.size() <= 0) {
            return;
        }

        NetRequestImpl.getInstance().joinDiscussGroup(cid, new RequestListener() {
            @Override
            public void start() {
                mView.joinDiscussGroupStart();
            }

            @Override
            public void success(JSONObject response) {
               mView.joinDiscussGroupSuccess();
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                mView.joinDiscussGroupError(errorCode,errorMsg);
            }
        });
    }
}
