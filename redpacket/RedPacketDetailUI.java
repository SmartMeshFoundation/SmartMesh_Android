package com.lingtuan.firefly.redpacket;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.custom.CustomListView;
import com.lingtuan.firefly.util.Utils;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * red packet detail
 * 红包详情
 * */
public class RedPacketDetailUI extends BaseActivity{

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

    private RedPacketDetailPresenter redPacketDetailPresenter;

    @Override
    protected void setContentView() {
        setContentView(R.layout.red_packet_detail_layout);
    }

    @Override
    protected void findViewById() {
        redPacketDetailPresenter = new RedPacketDetailPresenter(RedPacketDetailUI.this);
        redPacketDetailPresenter.init(listView);
    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.red_packet));
        appBtnRight.setVisibility(View.VISIBLE);
        appBtnRight.setText(getString(R.string.red_balance_record));
    }

    @OnClick({R.id.app_back,R.id.app_btn_right})
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.app_back:
                Utils.exitActivityAndBackAnim(RedPacketDetailUI.this,true);
                break;
            case R.id.app_btn_right:
                redPacketDetailPresenter.redPacketRecord();
                break;
        }
    }
}
