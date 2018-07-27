package com.lingtuan.firefly.redpacket;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.redpacket.contract.RedPacketSendContract;
import com.lingtuan.firefly.redpacket.presenter.RedPacketSendPresenterImpl;
import com.lingtuan.firefly.util.MyViewDialogFragment;
import com.lingtuan.firefly.util.Utils;

import java.math.BigDecimal;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 发送红包页面
 * send red packet ui
 * @see com.lingtuan.firefly.chat.ChattingUI
 * */
public class RedPacketSendUI extends BaseActivity implements RedPacketSendContract.View{

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

    private String uid;// user id   or group id
    private boolean isGroup;//is send group

    private RedPacketSendContract.Presenter mPresenter;

    @Override
    protected void setContentView() {
        setContentView(R.layout.red_packet_send_layout);
        Utils.setStatusBar(this,3);
        getPassData();
    }

    private void getPassData() {
        uid = getIntent().getStringExtra("uid");
        isGroup = getIntent().getBooleanExtra("isGroup",false);
    }

    @Override
    protected void findViewById() {
        new RedPacketSendPresenterImpl(this);
    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.red_packet_send));
        if (isGroup){
            redPacketGroupBody.setVisibility(View.VISIBLE);
        }else{
            redPacketGroupBody.setVisibility(View.GONE);
        }
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
                String redNumber = redPacketNumber.getText().toString().trim();
                mPresenter.checkRedPacket(singleAmount,redNumber,isGroup);
                break;
        }
    }


    @Override
    public void checkSuccess() {
        MyViewDialogFragment mdf = new MyViewDialogFragment(MyViewDialogFragment.DIALOG_INPUT_PWD, new MyViewDialogFragment.EditCallback() {
            @Override
            public void getEditText(String editText) {
                String singleAmount = redPacketValue.getText().toString().trim();
                String redNumber = redPacketNumber.getText().toString().trim();
                String redLeaveMessage = redPacketLeaveMessage.getText().toString().trim();
                mPresenter.sendRedPacket(singleAmount,redNumber,redLeaveMessage,type,isGroup);
            }
        });
        mdf.show(getSupportFragmentManager(), "mdf");
    }

    @Override
    public void success() {
        startActivity(new Intent(RedPacketSendUI.this,RedPacketDetailUI.class));
        Utils.openNewActivityAnim(RedPacketSendUI.this,false);
    }

    @Override
    public void error(int errorCode, String errorMsg) {
        showToast(errorMsg);
    }

    @Override
    public void setPresenter(RedPacketSendContract.Presenter presenter) {
        this.mPresenter = presenter;
    }
}
