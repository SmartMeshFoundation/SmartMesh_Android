package com.lingtuan.firefly.redpacket;

import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.util.Utils;

/**
 * 红包充值页面
 * red packet recharge
 * @see RedPacketBalance
 * @see RedPacketBalanceRecord
 * */
public class RedPacketRecharge extends BaseActivity {

    private TextView appBtnRight;
    private TextView redRecharge;
    private EditText redRechargeAmount;

    @Override
    protected void setContentView() {
        setContentView(R.layout.red_packet_recharge_layout);
    }

    @Override
    protected void findViewById() {
        appBtnRight = findViewById(R.id.app_btn_right);
        redRecharge = findViewById(R.id.redRecharge);
        redRechargeAmount = findViewById(R.id.redRechargeAmount);
    }

    @Override
    protected void setListener() {
        appBtnRight.setOnClickListener(this);
        redRecharge.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.red_balance_recharge));
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
            case R.id.redRecharge:
                String rechargeAmount = redRechargeAmount.getText().toString().trim();
                if (Float.parseFloat(rechargeAmount) < 0.1){
                    showToast(getString(R.string.red_balance_recharge_amount_hint));
                    return;
                }
                startActivity(new Intent(RedPacketRecharge.this,RedPacketRechargeFinish.class));
                Utils.openNewActivityAnim(RedPacketRecharge.this,false);
                break;
        }
    }
}
