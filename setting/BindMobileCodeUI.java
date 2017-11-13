package com.lingtuan.firefly.setting;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.login.RegistUI;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;

import org.jivesoftware.smack.packet.Bind;
import org.json.JSONObject;

/**
 * binding mobile phone number message validation page
 * binding E-mail verification page
 * phone number to retrieve password page
 * email retrieve password page
 * Created on 2017/10/24.
 */

public class BindMobileCodeUI extends BaseActivity {

    private TextView next;

    private EditText codeEt;

    private String phoneNumber;
    private String email;//email
    private int type;//0 binding mobile phone number, 1 binding inbox, 2 phone number retrieve password, 3 retrieve password

    @Override
    protected void setContentView() {
        setContentView(R.layout.bind_mobile_code_layout);
        getPassData();
    }

    private void getPassData() {
        phoneNumber = getIntent().getStringExtra("phonemubmer");
        email = getIntent().getStringExtra("email");
        type = getIntent().getIntExtra("type",0);
    }

    @Override
    protected void findViewById() {
        next = (TextView) findViewById(R.id.app_btn_right);
        codeEt = (EditText) findViewById(R.id.codeEt);
    }

    @Override
    protected void setListener() {
        next.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        if (type == 0){
            setTitle(getString(R.string.bind_mobile));
        }else if (type == 1){
            setTitle(getString(R.string.bind_email));
        }else{
            setTitle(getString(R.string.forgot_password_hint));
        }
        next.setVisibility(View.VISIBLE);
        next.setText(getString(R.string.next));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.app_btn_right:
                if (type == 0 || type == 2){
                    verifySmsc();
                }else{
                    verifyMail();
                }

                break;
            default:
                super.onClick(v);
                break;
        }
    }

    /**
     * Verify the message
     * */
    private void verifySmsc() {
        String code = codeEt.getText().toString().trim();
        if (TextUtils.isEmpty(code)){
            showToast(getString(R.string.code_number_not_empty));
            return;
        }
        NetRequestImpl.getInstance().verifySmsc(code, phoneNumber, new RequestListener() {
            @Override
            public void start() {
                LoadingDialog.show(BindMobileCodeUI.this,"");
            }

            @Override
            public void success(JSONObject response) {
                if (type == 0){//Binding mobile phone number
                    bindMobileMethod();
                }else{//Mobile phone number to retrieve password
                    Intent intent = new Intent(BindMobileCodeUI.this, RegistUI.class);
                    intent.putExtra("number",phoneNumber);
                    intent.putExtra("type",type);
                    startActivity(intent);
                    Utils.openNewActivityAnim(BindMobileCodeUI.this,false);
                }

            }

            @Override
            public void error(int errorCode, String errorMsg) {
                LoadingDialog.close();
                showToast(errorMsg);
            }
        });
    }

    /**
     * Interface binding mobile phone number
     * */
    private void bindMobileMethod() {
        NetRequestImpl.getInstance().bindMobile(phoneNumber, new RequestListener() {
            @Override
            public void start() {

            }

            @Override
            public void success(JSONObject response) {
                LoadingDialog.close();
                showToast(response.optString("msg"));
                NextApplication.myInfo.updateJsonBindMobile(phoneNumber, BindMobileCodeUI.this);
                NextApplication.myInfo.setPhonenumber(phoneNumber);
                Intent intent = new Intent(BindMobileCodeUI.this,BindMobileSuccessUI.class);
                intent.putExtra("phonenumber",phoneNumber);
                intent.putExtra("email", email);
                intent.putExtra("type",type);
                startActivity(intent);
                Utils.openNewActivityAnim(BindMobileCodeUI.this,true);
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                LoadingDialog.close();
                showToast(errorMsg);
            }
        });
    }

    /**
     * Validation email
     * */
    private void verifyMail() {
        String code = codeEt.getText().toString().trim();
        if (TextUtils.isEmpty(code)){
            showToast(getString(R.string.code_number_not_empty));
            return;
        }
        NetRequestImpl.getInstance().verifyMail(code, email, new RequestListener() {
            @Override
            public void start() {
                LoadingDialog.show(BindMobileCodeUI.this,"");
            }

            @Override
            public void success(JSONObject response) {
                if (type == 1){//Binding email
                    bindEmailMethod();
                }else{//Email retrieve password
                    Intent intent = new Intent(BindMobileCodeUI.this, RegistUI.class);
                    intent.putExtra("number",email);
                    intent.putExtra("type",type);
                    startActivity(intent);
                    Utils.openNewActivityAnim(BindMobileCodeUI.this,false);
                }

            }

            @Override
            public void error(int errorCode, String errorMsg) {
                LoadingDialog.close();
                showToast(errorMsg);
            }
        });
    }

    /**
     * Binding email interface
     * */
    private void bindEmailMethod() {
        NetRequestImpl.getInstance().bindEmail(email, new RequestListener() {
            @Override
            public void start() {

            }

            @Override
            public void success(JSONObject response) {
                LoadingDialog.close();
                showToast(response.optString("msg"));
                NextApplication.myInfo.updateJsonBindEmail(email, BindMobileCodeUI.this);
                NextApplication.myInfo.setEmail(email);
                Intent intent = new Intent(BindMobileCodeUI.this,BindMobileSuccessUI.class);
                intent.putExtra("phonenumber",phoneNumber);
                intent.putExtra("email", email);
                intent.putExtra("type",type);
                startActivity(intent);
                Utils.openNewActivityAnim(BindMobileCodeUI.this,true);
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                LoadingDialog.close();
                showToast(errorMsg);
            }
        });
    }

}
