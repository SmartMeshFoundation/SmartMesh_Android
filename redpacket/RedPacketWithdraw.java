package com.lingtuan.firefly.redpacket;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.util.Utils;

/**
 * red packer withdraw
 * 红包提现页面
 * */
public class RedPacketWithdraw extends BaseActivity {

    private TextView appBtnRight;
    private TextView redWithdraw;
    private TextView redWithdrawAmount;

    @Override
    protected void setContentView() {
        setContentView(R.layout.red_packet_withdraw_layout);
    }

    @Override
    protected void findViewById() {
        appBtnRight = findViewById(R.id.app_btn_right);
        redWithdraw = findViewById(R.id.redWithdraw);
        redWithdrawAmount = findViewById(R.id.redWithdrawAmount);
    }

    @Override
    protected void setListener() {
        appBtnRight.setOnClickListener(this);
        redWithdraw.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.red_balance_withdraw));
        appBtnRight.setVisibility(View.VISIBLE);
        appBtnRight.setText(getString(R.string.red_balance_record));
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.app_btn_right:
                showToast(getString(R.string.red_balance_record));
                break;
            case R.id.redWithdraw:
                String rechargeAmount = redWithdrawAmount.getText().toString().trim();
                startActivity(new Intent(RedPacketWithdraw.this,RedPacketWithdrawFinish.class));
                Utils.openNewActivityAnim(RedPacketWithdraw.this,false);
                break;
        }
    }
}
