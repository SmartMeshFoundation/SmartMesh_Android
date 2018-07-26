package com.lingtuan.firefly.redpacket;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.redpacket.fragment.RedPacketWithdrawRecordFragment;
import com.lingtuan.firefly.util.Utils;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * red packer withdraw
 * 红包提现页面
 * */
public class RedPacketWithdrawUI extends BaseActivity {

    @BindView(R.id.app_btn_right)
    TextView appBtnRight;
    @BindView(R.id.redWithdrawAmount)
    TextView redWithdrawAmount;

    @Override
    protected void setContentView() {
        setContentView(R.layout.red_packet_withdraw_layout);
    }

    @Override
    protected void findViewById() {

    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.red_balance_withdraw));
        appBtnRight.setVisibility(View.VISIBLE);
        appBtnRight.setText(getString(R.string.red_balance_record));
    }

    @OnClick({R.id.app_btn_right,R.id.redWithdraw})
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.app_btn_right:
                startActivity(new Intent(RedPacketWithdrawUI.this,RedPacketWithdrawRecordUI.class));
                Utils.openNewActivityAnim(RedPacketWithdrawUI.this,false);
                break;
            case R.id.redWithdraw:
                String rechargeAmount = redWithdrawAmount.getText().toString().trim();
                startActivity(new Intent(RedPacketWithdrawUI.this,RedPacketWithdrawFinishUI.class));
                Utils.openNewActivityAnim(RedPacketWithdrawUI.this,false);
                break;
        }
    }
}
