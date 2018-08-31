package com.lingtuan.firefly.ui;

import android.content.Intent;
import android.view.View;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.ui.contract.FriendReportContract;
import com.lingtuan.firefly.ui.presenter.FriendReportPresenterImpl;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MyViewDialogFragment;

import butterknife.OnClick;

/**
 * Report friend interface
 * Created on 2017/10/26.
 */

public class FriendReportUI extends BaseActivity implements FriendReportContract.View{

    //The user id
    private String localid;

    private FriendReportContract.Presenter mPresenter;

    @Override
    protected void setContentView() {
        setContentView(R.layout.friend_report_layout);
        getPassData();
    }

    private void getPassData() {
        localid = getIntent().getStringExtra("localId");
    }

    @Override
    protected void findViewById() {

    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void initData() {
        new FriendReportPresenterImpl(this);
        setTitle(getString(R.string.report));
    }

    @OnClick({R.id.reportReason1,R.id.reportReason2,R.id.reportReason3,R.id.reportReason4,R.id.reportReason5
            ,R.id.reportReason6,R.id.reportReason7,R.id.reportReason8,R.id.reportReason9})
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.reportReason1:
                reportMethod(0);
                break;
            case R.id.reportReason2:
                reportMethod(1);
                break;
            case R.id.reportReason3:
                reportMethod(2);
                break;
            case R.id.reportReason4:
                reportMethod(3);
                break;
            case R.id.reportReason5:
                reportMethod(4);
                break;
            case R.id.reportReason6:
                reportMethod(5);
                break;
            case R.id.reportReason7:
                reportMethod(6);
                break;
            case R.id.reportReason8:
                reportMethod(7);
                break;
            case R.id.reportReason9:
                reportMethod(8);
                break;
        }
    }

    /**
     * report users
     * @ param type report type
     * */
    private void reportMethod(int type){
        mPresenter.reportFriendMethod(type,localid);
    }

    /**
     * Add a blacklist
     * */
    private void addBlackMethod(){
        MyViewDialogFragment mdf = new MyViewDialogFragment();
        mdf.setTitleAndContentText(getString(R.string.report_sent), getString(R.string.info_shield_user));
        mdf.setSubmitNamContentText(getString(R.string.info_block));
        mdf.setCancelCallback(new MyViewDialogFragment.CancelCallback() {
            @Override
            public void cancelBtn() {
                finish();
            }
        });
        mdf.setOkCallback(new MyViewDialogFragment.OkCallback() {
            @Override
            public void okBtn() {
                mPresenter.updateBlackState(0,localid);
            }
        });
        mdf.show(getSupportFragmentManager(), "mdf");
    }

    @Override
    public void setPresenter(FriendReportContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public void addBlackStart() {
        LoadingDialog.show(FriendReportUI.this,"");
    }

    @Override
    public void addBlackSuccess(String message) {
        LoadingDialog.close();
        FinalUserDataBase.getInstance().deleteFriendByUid(localid);
        showToast(message);
        Intent intent = new Intent();
        setResult(RESULT_OK,intent);
        finish();
    }

    @Override
    public void addBlackError(int errorCode, String errorMsg) {
        LoadingDialog.close();
        showToast(errorMsg);
    }

    @Override
    public void reportFriendStart() {
        LoadingDialog.show(FriendReportUI.this,"");
    }

    @Override
    public void reportFriendSuccess() {
        LoadingDialog.close();
        addBlackMethod();
    }

    @Override
    public void reportFriendError(int errorCode, String errorMsg) {
        LoadingDialog.close();
        showToast(errorMsg);
    }
}
