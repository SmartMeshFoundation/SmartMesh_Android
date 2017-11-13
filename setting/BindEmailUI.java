package com.lingtuan.firefly.setting;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MyViewDialogFragment;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;

import org.json.JSONObject;

/**
 * Created on 2017/10/11.
 * Binding email
 */

public class BindEmailUI extends BaseActivity {

    private TextView btnRight;

    private EditText emailEt;

    private String aeskey = null ;

    private int type;//0 binding mobile phone number, 1 binding inbox, 2 phone number retrieve password, 3 retrieve password

    @Override
    protected void setContentView() {
        setContentView(R.layout.bind_email_layout);
        getPassData();
    }

    private void getPassData() {
        type = getIntent().getIntExtra("type",0);
    }

    @Override
    protected void findViewById() {
        btnRight = (TextView) findViewById(R.id.app_btn_right);
        emailEt = (EditText) findViewById(R.id.emailEt);
    }

    @Override
    protected void setListener() {
        btnRight.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        if (type == 3){
            setTitle(getString(R.string.forgot_password_hint));
        }else{
            setTitle(getString(R.string.bind_email));
        }
        btnRight.setVisibility(View.VISIBLE);
        btnRight.setText(getString(R.string.next));
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.app_btn_right:
                sendVerificationCode();
                break;
        }
    }

    /**
     * Send verification code
     * */
    private void sendVerificationCode() {
        String phoneNumber = emailEt.getText().toString().trim();
        if (TextUtils.isEmpty(phoneNumber)){
            showToast(getString(R.string.enter_email_number));
            return;
        }
        MyViewDialogFragment mdf = new MyViewDialogFragment();
        mdf.setTitleAndContentText(getString(R.string.confirm_email_number), getString(R.string.send_verification_warning,phoneNumber));
        mdf.setOkCallback(new MyViewDialogFragment.OkCallback() {
            @Override
            public void okBtn() {
                sendSmsc();
            }
        });
        mdf.show(getSupportFragmentManager(), "mdf");
    }

    /**
     * Verify the signature for text messages
     * */
    private void sendSmsc() {
        this.aeskey = Utils.makeRandomKey(16);
        final String emailNumber = emailEt.getText().toString().trim();
        NetRequestImpl.getInstance().sendMail(aeskey, type == 1 ? 0 : 1,emailNumber,new RequestListener() {
            @Override
            public void start() {
                LoadingDialog.show(BindEmailUI.this,"");
            }

            @Override
            public void success(JSONObject response) {
                LoadingDialog.close();
                showToast(response.optString("msg"));
                Intent intent = new Intent(BindEmailUI.this,BindMobileCodeUI.class);
                intent.putExtra("email",emailNumber);
                intent.putExtra("type",type);
                startActivity(intent);
                Utils.openNewActivityAnim(BindEmailUI.this,false);
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                LoadingDialog.close();
                showToast(errorMsg);
            }
        });
    }

}
