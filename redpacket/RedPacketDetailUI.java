package com.lingtuan.firefly.redpacket;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.custom.CustomListView;
import com.lingtuan.firefly.redpacket.bean.RedPacketBean;
import com.lingtuan.firefly.redpacket.contract.RedPacketDetailContract;
import com.lingtuan.firefly.redpacket.presenter.RedPacketDetailPresenterImpl;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.Utils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * red packet detail
 * 红包详情
 * */
public class RedPacketDetailUI extends BaseActivity implements RedPacketDetailContract.View{

    @BindView(R.id.redPacketFromImg)
    ImageView redPacketFromImg;
    @BindView(R.id.redPacketFromName)
    TextView redPacketFromName;
    @BindView(R.id.redPacketAllValues)
    TextView redPacketAllValues;
    @BindView(R.id.redPacketReceived)
    TextView redPacketReceived;
    @BindView(R.id.app_back)
    ImageView appBack;
    @BindView(R.id.app_btn_right)
    TextView appBtnRight;
    @BindView(R.id.redPacketListView)
    CustomListView listView;

    private RedPacketDetailContract.Presenter mPresenter;

    private RedPacketDetailAdapter redPacketDetailAdapter;
    private ArrayList<RedPacketBean> redPacketBeans;

    @Override
    protected void setContentView() {
        setContentView(R.layout.red_packet_detail_layout);
    }

    @Override
    protected void findViewById() {

    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.red_packet));
        Utils.setStatusBar(this,3);
        new RedPacketDetailPresenterImpl(this);
        appBtnRight.setVisibility(View.VISIBLE);
        appBtnRight.setText(getString(R.string.red_balance_record));
        redPacketBeans = new ArrayList<>();
        redPacketDetailAdapter = new RedPacketDetailAdapter(this,redPacketBeans);
        listView.setAdapter(redPacketDetailAdapter);
        LoadingDialog.show(RedPacketDetailUI.this,"");
        mPresenter.start();
    }

    @OnClick({R.id.app_back,R.id.app_btn_right})
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.app_back:
                Utils.exitActivityAndBackAnim(RedPacketDetailUI.this,true);
                break;
            case R.id.app_btn_right:
                startActivity(new Intent(this,RedPacketTransactionRecordUI.class));
                Utils.openNewActivityAnim(this,false);
                break;
        }
    }

    @Override
    public void success(ArrayList<RedPacketBean> redPacketRecords) {
        LoadingDialog.close();
        redPacketBeans.clear();
        redPacketBeans.addAll(redPacketRecords);
        redPacketDetailAdapter.resetSource(redPacketBeans);
    }

    @Override
    public void error(int errorCode, String errorMsg) {
        LoadingDialog.close();
        if (errorCode == 0){
            showToast(getString(R.string.red_balance_record_empty));
        }else{
            showToast(errorMsg);
        }
    }

    @Override
    public void setPresenter(RedPacketDetailContract.Presenter presenter) {
        this.mPresenter = presenter;
    }
}
