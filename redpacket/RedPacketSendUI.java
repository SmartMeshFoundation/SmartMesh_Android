package com.lingtuan.firefly.redpacket;

import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.ui.FriendInfoUI;
import com.lingtuan.firefly.util.Utils;

import java.math.BigDecimal;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 发送红包页面
 * send red packet ui
 * @see com.lingtuan.firefly.chat.ChattingUI
 * */
public class RedPacketSendUI extends BaseActivity{

    @BindView(R.id.redPacketValue)
    EditText redPacketValue;
    @BindView(R.id.redPacketNumber)
    EditText redPacketNumber;
    @BindView(R.id.redPacketLeaveMessage)
    EditText redPacketLeaveMessage;
    @BindView(R.id.redPacketGroupBody)
    LinearLayout redPacketGroupBody;
    @BindView(R.id.redPacketChangeType)
    TextView redPacketChangeType;
    @BindView(R.id.redPacketName)
    TextView redPacketName;
    @BindView(R.id.redPacketBalance)
    TextView redPacketBalance;
    @BindView(R.id.redPacketTotalBalance)
    TextView redPacketTotalBalance;
    @BindView(R.id.redPacketGroupNumber)
    TextView redPacketGroupNumber;
    @BindView(R.id.redPacketSend)
    TextView redPacketSend;

    private boolean type;

    private RedPacketSendPresenter redPacketSendPresenter;

    @Override
    protected void setContentView() {
        setContentView(R.layout.red_packet_send_layout);
    }

    @Override
    protected void findViewById() {
        redPacketSendPresenter = new RedPacketSendPresenter(RedPacketSendUI.this);
    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.red_packet_send));
    }

    @OnClick({R.id.app_back,R.id.redPacketChangeType,R.id.redPacketSend})
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.app_back:
                Utils.exitActivityAndBackAnim(RedPacketSendUI.this,true);
                break;
            case R.id.redPacketChangeType:
                if (type){
                    redPacketChangeType.setText(getString(R.string.red_packet_send_uniform));
                    redPacketName.setText(getString(R.string.red_packet_send_currently_handy));
                }else{
                    redPacketChangeType.setText(getString(R.string.red_packet_send_handy));
                    redPacketName.setText(getString(R.string.red_packet_send_currently_uniform));
                }
                type = !type;
                break;
            case R.id.redPacketSend:
                String singleAmount = redPacketValue.getText().toString().trim();
                if (TextUtils.isEmpty(singleAmount)){
                    showToast(getString(R.string.red_packet_send_value_hint));
                    return;
                }else{
                    BigDecimal singleAmount1 = new BigDecimal(singleAmount);
                    BigDecimal singleAmount2 = new BigDecimal("0.01");
                    if (singleAmount1.compareTo(singleAmount2) < 0){
                        showToast(getString(R.string.red_packet_send_value_hint));
                        return;
                    }
                }
                String redNumber = redPacketNumber.getText().toString().trim();
                if (TextUtils.isEmpty(redNumber) || Integer.parseInt(redNumber) < 1){
                    showToast(getString(R.string.red_packet_send_number_hint));
                    return;
                }
                String redLeaveMessage = redPacketLeaveMessage.getText().toString().trim();
                redPacketSendPresenter.sendRedPacketMethod(singleAmount,redNumber,redLeaveMessage,type);
                break;
        }
    }
}
