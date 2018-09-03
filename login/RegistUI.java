package com.lingtuan.firefly.login;

import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.login.contract.RegisterContract;
import com.lingtuan.firefly.login.presenter.RegisterPresenterImpl;
import com.lingtuan.firefly.network.NetRequestImpl;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MyToast;

import org.json.JSONObject;

import java.util.UUID;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;

/**
 * The sign-up page
 * Created on 2017/10/13.
 */

public class RegistUI extends BaseActivity implements RegisterContract.View{

    private LoginUtil loginUtil;

    @BindView(R.id.userName)
    EditText userName;//The user name, password
    @BindView(R.id.password)
    EditText password;//enter the password
    @BindView(R.id.againPwd)
    EditText againPwd;//the password prompt again
    @BindView(R.id.clearUserName)
    ImageView clearUserName;//Clean up the user name
    @BindView(R.id.isShowPass)
    ImageView isShowPass;//show/hide the password
    @BindView(R.id.regist)
    TextView regist;//register

    private RegisterContract.Presenter mPresenter;

    /**
     * show password
     */
    private boolean isShowPassWorld = false;

    private int type;//1 phone number to get the password    3 email to get the password
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

    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.regist));
        new RegisterPresenterImpl(this);
        checkInputContent();
        if (type != -1){
            findViewById(R.id.userNameBg).setVisibility(View.GONE);
            findViewById(R.id.userNameLine).setVisibility(View.GONE);
            setTitle(getString(R.string.forgot_password_hint));
            regist.setText(getString(R.string.account_pwd_input));
        }

        loginUtil = LoginUtil.getInstance();
        loginUtil.initContext(RegistUI.this);
    }

    @OnClick({R.id.regist,R.id.clearUserName,R.id.isShowPass})
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.regist:
                registerMethod();
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
    private void registerMethod(){
        if (type == 2){//Mobile phone number back
            smscForgetMethod();
        }else if (type == 3){//Email back
            emailForgetMethod();
        }else{
            String mName = userName.getText().toString();
            String mPwd = password.getText().toString();
            String mAgainPwd = againPwd.getText().toString();
            String localId = UUID.randomUUID().toString();
            LoginUtil.getInstance().registMethod(mPwd,mAgainPwd,mName,localId);
        }
    }

    /**
     * Email retrieve password
     * */
    private void emailForgetMethod() {
        String mPwd = password.getText().toString();
        String mAgainPwd = againPwd.getText().toString();
        if (!TextUtils.equals(mPwd,mAgainPwd)){
            MyToast.showToast(RegistUI.this,getString(R.string.account_pwd_again_warning));
            return;
        }
        mPresenter.emailForget(number,mPwd);
    }

    /**
     * Mobile phone number to retrieve password
     * */
    private void smscForgetMethod() {
        String mPwd = password.getText().toString();
        String mAgainPwd = againPwd.getText().toString();
        if (!TextUtils.equals(mPwd,mAgainPwd)){
            MyToast.showToast(RegistUI.this,getString(R.string.account_pwd_again_warning));
            return;
        }
        mPresenter.smsForget(number,mPwd);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        LoginUtil.getInstance().destory();
    }

    @OnTextChanged(value = {R.id.userName},callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void userNameTextChanged(Editable s) {
        checkInputContent();
    }

    @OnTextChanged(value = {R.id.password},callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void passwordTextChanged(Editable s) {
        checkInputContent();
    }

    @OnTextChanged(value = {R.id.againPwd},callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void againPwdTextChanged(Editable s) {
        checkInputContent();
    }

    private void checkInputContent(){
        if (TextUtils.isEmpty(userName.getText().toString()) && TextUtils.isEmpty(password.getText().toString()) && TextUtils.isEmpty(againPwd.getText().toString())){
            regist.setEnabled(false);
        }else{
            regist.setEnabled(true);
        }
    }

    @Override
    public void setPresenter(RegisterContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public void retrievePasswordStart() {
        LoadingDialog.show(RegistUI.this,"");
    }

    @Override
    public void retrievePasswordSuccess(String message) {
        LoadingDialog.close();
        showToast(message);
        exit();
        Intent intent = new Intent(RegistUI.this,LoginUI.class);
        startActivity(intent);
    }

    @Override
    public void retrievePasswordError(int errorCode, String errorMsg) {
        showToast(errorMsg);
        LoadingDialog.close();
    }
}
