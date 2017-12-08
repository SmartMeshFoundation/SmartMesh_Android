package com.lingtuan.firefly.login;

import android.content.Intent;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MyToast;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;

import org.json.JSONObject;

import java.util.UUID;

/**
 * The sign-up page
 * Created on 2017/10/13.
 */

public class RegistUI extends BaseActivity {

    //login util
    private LoginUtil loginUtil;

    private EditText userName,password,againPwd;//The user name, password, enter the password, the password prompt again
    private ImageView clearUserName,isShowPass;//Clean up the user name, and show/hide the password
    private TextView regist;//register

    /**
     * show password true or false
     */
    private boolean isShowPassWorld = false;

    private int type;//1 phone number to retrieve password 3 mailbox retrieve password
    private String number;//Email or phone number


    @Override
    protected void setContentView() {
        setContentView(R.layout.regist_layout);
        getPaddData();
    }

    private void getPaddData() {
        type = getIntent().getIntExtra("type",-1);
        number = getIntent().getStringExtra("number");
    }

    @Override
    protected void findViewById() {
        userName = (EditText) findViewById(R.id.userName);
        password = (EditText) findViewById(R.id.password);
        againPwd = (EditText) findViewById(R.id.againPwd);
        clearUserName = (ImageView) findViewById(R.id.clearUserName);
        isShowPass = (ImageView) findViewById(R.id.isShowPass);
        regist = (TextView) findViewById(R.id.regist);
    }

    @Override
    protected void setListener() {
        regist.setOnClickListener(this);
        isShowPass.setOnClickListener(this);
        clearUserName.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.regist));
        if (type != -1){
            findViewById(R.id.userNameBg).setVisibility(View.GONE);
            findViewById(R.id.userNameLine).setVisibility(View.GONE);
            setTitle(getString(R.string.forgot_password_hint));
        }
        loginUtil = LoginUtil.getInstance();
        loginUtil.initContext(RegistUI.this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.regist:
                registMethod();
                break;
            case R.id.clearUserName:
                userName.setText("");
                break;
            case R.id.isShowPass:
                isShowPassWorld = !isShowPassWorld;
                if (isShowPassWorld) { /* Set the EditText content is visible */
                    password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    isShowPass.setImageResource(R.drawable.eye_open);
                } else {/* The content of the EditText set as hidden*/
                    password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    isShowPass.setImageResource(R.drawable.eye_close);
                }
                break;
        }
    }

    /**
     * retrieve password
     * registration related
     * */
    private void registMethod(){
        if (type == 2){//Mobile phone number back
            smscForgetMethod();
        }else if (type == 3){//Email back
            emailForgetMethod();
        }else{
            String mName = userName.getText().toString();
            String mPwd = password.getText().toString();
            String mAgainPwd = againPwd.getText().toString();
            String locailid = UUID.randomUUID().toString();
            LoginUtil.getInstance().registMethod(mPwd,mAgainPwd,mName,locailid);
        }
    }

    /**
     * Email retrieve password
     * */
    private void emailForgetMethod() {
        String mPwd = password.getText().toString();
        String mAgainPwd = againPwd.getText().toString();
        //Authentication codes are consistent
        if (!TextUtils.equals(mPwd,mAgainPwd)){
            MyToast.showToast(RegistUI.this,getString(R.string.account_pwd_again_warning));
            return;
        }
        NetRequestImpl.getInstance().emailForget(number, mPwd, new RequestListener() {
            @Override
            public void start() {
                LoadingDialog.show(RegistUI.this,"");
            }

            @Override
            public void success(JSONObject response) {
                LoadingDialog.close();
                showToast(response.optString("msg"));
                Intent intent = new Intent(RegistUI.this,LoginUI.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                showToast(errorMsg);
                LoadingDialog.close();
            }
        });
    }

    /**
     * Mobile phone number to retrieve password
     * */
    private void smscForgetMethod() {
        String mPwd = password.getText().toString();
        String mAgainPwd = againPwd.getText().toString();
        //Authentication codes are consistent
        if (!TextUtils.equals(mPwd,mAgainPwd)){
            MyToast.showToast(RegistUI.this,getString(R.string.account_pwd_again_warning));
            return;
        }
        NetRequestImpl.getInstance().smscForget(number, mPwd, new RequestListener() {
            @Override
            public void start() {
                LoadingDialog.show(RegistUI.this,"");
            }

            @Override
            public void success(JSONObject response) {
                LoadingDialog.close();
                showToast(response.optString("msg"));
                Intent intent = new Intent(RegistUI.this,LoginUI.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                showToast(errorMsg);
                LoadingDialog.close();
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        LoginUtil.getInstance().destory();
    }
}
