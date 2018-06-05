package com.lingtuan.firefly.ui;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MyViewDialogFragment;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;

import org.json.JSONObject;

/**
 * Report friend interface
 * Created on 2017/10/26.
 */

public class FriendReportUI extends BaseActivity {

    private TextView reportReason1;
    private TextView reportReason2;
    private TextView reportReason3;
    private TextView reportReason4;
    private TextView reportReason5;
    private TextView reportReason6;
    private TextView reportReason7;
    private TextView reportReason8;
    private TextView reportReason9;

    private String localid;//The user id

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
        reportReason1 = (TextView) findViewById(R.id.reportReason1);
        reportReason2 = (TextView) findViewById(R.id.reportReason2);
        reportReason3 = (TextView) findViewById(R.id.reportReason3);
        reportReason4 = (TextView) findViewById(R.id.reportReason4);
        reportReason5 = (TextView) findViewById(R.id.reportReason5);
        reportReason6 = (TextView) findViewById(R.id.reportReason6);
        reportReason7 = (TextView) findViewById(R.id.reportReason7);
        reportReason8 = (TextView) findViewById(R.id.reportReason8);
        reportReason9 = (TextView) findViewById(R.id.reportReason9);
    }

    @Override
    protected void setListener() {
        reportReason1.setOnClickListener(this);
        reportReason2.setOnClickListener(this);
        reportReason3.setOnClickListener(this);
        reportReason4.setOnClickListener(this);
        reportReason5.setOnClickListener(this);
        reportReason6.setOnClickListener(this);
        reportReason7.setOnClickListener(this);
        reportReason8.setOnClickListener(this);
        reportReason9.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.report));
    }

    @Override
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
        NetRequestImpl.getInstance().reportFriend(type, localid, new RequestListener() {
            @Override
            public void start() {
                LoadingDialog.show(FriendReportUI.this,"");
            }

            @Override
            public void success(JSONObject response) {
                LoadingDialog.close();
                addBlackMethod();
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                LoadingDialog.close();
                showToast(errorMsg);
            }
        });
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
                toAddBlack();
            }
        });
        mdf.show(getSupportFragmentManager(), "mdf");
    }

    /**
     * Add a blacklist
     * */
    private void toAddBlack() {
        NetRequestImpl.getInstance().updateBlackState(0, localid, new RequestListener() {
            @Override
            public void start() {
                LoadingDialog.show(FriendReportUI.this,"");
            }

            @Override
            public void success(JSONObject response) {
                LoadingDialog.close();
                FinalUserDataBase.getInstance().deleteFriendByUid(localid);
                showToast(response.optString("msg"));
                Intent intent = new Intent();
                setResult(RESULT_OK,intent);
                finish();
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                LoadingDialog.close();
                showToast(errorMsg);
            }
        });
    }
}
