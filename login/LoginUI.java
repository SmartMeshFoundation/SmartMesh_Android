package com.lingtuan.firefly.login;

import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.vo.UserInfoVo;

import java.util.ArrayList;

/**
 * Landing page
 * Created on 2017/10/13.
 */

public class LoginUI extends BaseActivity implements TextWatcher {

    private LoginUtil loginUtil;

    private EditText userName,password;//Username  password
    private ImageView clearUserName,isShowPass;//Remove the user name   show/hide the password
    private TextView login;//register

    private TextView forgotPassword;
    private TextView regist;

    /**
     * Show the password
     */
    boolean isShowPassWorld = false;

    @Override
    protected void setContentView() {
        setContentView(R.layout.login_layout);
    }

    @Override
    protected void findViewById() {
        forgotPassword = (TextView) findViewById(R.id.forgotPassword);
        userName = (EditText) findViewById(R.id.userName);
        password = (EditText) findViewById(R.id.password);
        clearUserName = (ImageView) findViewById(R.id.clearUserName);
        isShowPass = (ImageView) findViewById(R.id.isShowPass);
        login = (TextView) findViewById(R.id.login);
        regist = (TextView) findViewById(R.id.app_btn_right);
    }

    @Override
    protected void setListener() {
        login.setOnClickListener(this);
        isShowPass.setOnClickListener(this);
        clearUserName.setOnClickListener(this);
        forgotPassword.setOnClickListener(this);
        regist.setOnClickListener(this);
        userName.addTextChangedListener(this);
        password.addTextChangedListener(this);
    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.login));
        regist.setVisibility(View.VISIBLE);
        regist.setText(getString(R.string.regist));
        loginUtil = LoginUtil.getInstance();
        loginUtil.initContext(LoginUI.this);
        String username = MySharedPrefs.readString(this, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_LOGIN_NAME);
        if (!TextUtils.isEmpty(username)){
            userName.setText(username);
        }

        checkInputContent();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.login:
                String mName = userName.getText().toString();
                String mPwd = password.getText().toString();
                MySharedPrefs.write(this, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_LOGIN_NAME, mName);
                if (loginUtil.checkUser(mName,mPwd)){
                    if (Utils.isConnectNet(LoginUI.this)){
                        loginUtil.login(mName,mPwd,true);
                    }else{
                       LoginUtil.getInstance().intoMainUI(LoginUI.this,mName,mPwd);
                    }
                }else{
                    showToast(getString(R.string.login_pwd_error));
                }
                break;
            case R.id.clearUserName:
                userName.setText("");
                break;
            case R.id.app_btn_right:
                startActivity(new Intent(LoginUI.this, RegistUI.class));
                Utils.openNewActivityAnim(LoginUI.this, false);
                break;
            case R.id.forgotPassword:
                Intent intent = new Intent(LoginUI.this, ForgotPwdUI.class);
                startActivity(intent);
                Utils.openNewActivityAnim(LoginUI.this,false);
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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        LoginUtil.getInstance().destory();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        checkInputContent();
    }

    private void checkInputContent(){
        if (TextUtils.isEmpty(userName.getText().toString()) && TextUtils.isEmpty(password.getText().toString())){
            login.setEnabled(false);
        }else{
            login.setEnabled(true);
        }
    }
}
