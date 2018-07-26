package com.lingtuan.firefly.redpacket;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.util.Utils;

import java.math.BigDecimal;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 红包充值页面
 * red packet recharge
 * @see RedPacketBalanceUI
 * @see RedPacketBalanceRecordUI
 * */
public class RedPacketRechargeUI extends BaseActivity {

    @BindView(R.id.app_btn_right)
    TextView appBtnRight;
    @BindView(R.id.redRechargeAmount)
    EditText redRechargeAmount;

    @Override
    protected void setContentView() {
        setContentView(R.layout.red_packet_recharge_layout);
    }

    @Override
    protected void findViewById() {

    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.red_balance_recharge));
        appBtnRight.setVisibility(View.VISIBLE);
        appBtnRight.setText(getString(R.string.red_balance_record));
    }

    @OnClick({R.id.app_btn_right,R.id.redRecharge})
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.app_btn_right:
                startActivity(new Intent(RedPacketRechargeUI.this,RedPacketRechargeRecordUI.class));
                Utils.openNewActivityAnim(RedPacketRechargeUI.this,false);
                break;
            case R.id.redRecharge:
                String rechargeAmount = redRechargeAmount.getText().toString().trim();
                if (TextUtils.isEmpty(rechargeAmount)){
                    showToast(getString(R.string.red_balance_recharge_amount_hint));
                    return;
                }else{
                    BigDecimal rechargeAmount1 = new BigDecimal(rechargeAmount);
                    BigDecimal rechargeAmount2 = new BigDecimal("0.1");
                    if (rechargeAmount1.compareTo(rechargeAmount2) < 0){
                        showToast(getString(R.string.red_balance_recharge_amount_hint));
                        return;
                    }
                }
                startActivity(new Intent(RedPacketRechargeUI.this,RedPacketRechargeFinishUI.class));
                Utils.openNewActivityAnim(RedPacketRechargeUI.this,false);
                break;
        }
    }
}
