package com.lingtuan.firefly.setting;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.login.JsonUtil;
import com.lingtuan.firefly.login.RegistUI;
import com.lingtuan.firefly.setting.contract.BindMobileCodeContract;
import com.lingtuan.firefly.setting.presenter.BindMobileCodePresenterImpl;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MyViewDialogFragment;
import com.lingtuan.firefly.util.Utils;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * binding mobile phone number message validation page
 * binding E-mail verification page
 * phone number to retrieve password page
 * email retrieve password page
 * Created on 2017/10/24.
 */

public class BindMobileCodeUI extends BaseActivity implements BindMobileCodeContract.View{

    @BindView(R.id.app_btn_right)
    TextView next;
    @BindView(R.id.codeEt)
    EditText codeEt;

    private String phoneNumber;
    private String email;//email
    private int type;//0 binding mobile phone number, 1 binding email, 2 phone number retrieve password, 3 email retrieve password

    private BindMobileCodeContract.Presenter mPresenter;

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

    }

    @Override
    protected void setListener() {
    }

    @Override
    protected void initData() {

        new BindMobileCodePresenterImpl(this);

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

    @OnClick(R.id.app_btn_right)
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.app_btn_right:
                if (type == 0 || type == 1){
                    checkPasswordMethod();
                }else{
                    if (type == 2){
                        verifySmsc();
                    }else{
                        verifyMail();
                    }
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
        mPresenter.verifySms(code,phoneNumber);
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
        mPresenter.verifyEmail(code,email);
    }



    /**
     * check pwd
     * */
    private void checkPasswordMethod(){
        MyViewDialogFragment mdf = new MyViewDialogFragment(MyViewDialogFragment.DIALOG_PWD, getString(R.string.verify_password),getString(R.string.account_input_pwd_warning),null);
        mdf.setVerifyPwd(true);
        mdf.setEditOkCallback(new MyViewDialogFragment.EditOkCallback() {
            @Override
            public void okBtn(String edittext) {
                if (TextUtils.equals(edittext,NextApplication.myInfo.getPassword())){
                    if (type == 0){
                        verifySmsc();
                    }else{
                        verifyMail();
                    }
                }else{
                    showToast(getString(R.string.wallet_copy_pwd_error));
                }
            }
        });
        mdf.show(getSupportFragmentManager(), "mdf");
    }

    @Override
    public void start() {
        LoadingDialog.show(BindMobileCodeUI.this,"");
    }

    @Override
    public void verifySmsSuccess() {
        if (type == 0){//Binding mobile phone number
            mPresenter.bindMobile(phoneNumber);
        }else{//Mobile phone number to retrieve password
            LoadingDialog.close();
            Intent intent = new Intent(BindMobileCodeUI.this, RegistUI.class);
            intent.putExtra("number",phoneNumber);
            intent.putExtra("type",type);
            startActivity(intent);
            Utils.openNewActivityAnim(BindMobileCodeUI.this,false);
        }
    }

    @Override
    public void verifyEmailSuccess() {
        if (type == 1){//Binding email
            mPresenter.bindEmail(email);
        }else{//Email retrieve password
            LoadingDialog.close();
            Intent intent = new Intent(BindMobileCodeUI.this, RegistUI.class);
            intent.putExtra("number",email);
            intent.putExtra("type",type);
            startActivity(intent);
            Utils.openNewActivityAnim(BindMobileCodeUI.this,false);
        }
    }

    @Override
    public void bindSuccess(String message,boolean isEmail) {
        LoadingDialog.close();
        showToast(message);
        if (isEmail){
            NextApplication.myInfo.updateJsonBindEmail(email, BindMobileCodeUI.this);
            NextApplication.myInfo.setEmail(email);
        }else{
            NextApplication.myInfo.updateJsonBindMobile(phoneNumber, BindMobileCodeUI.this);
            NextApplication.myInfo.setPhonenumber(phoneNumber);
        }
        JsonUtil.updateLocalInfo(BindMobileCodeUI.this,NextApplication.myInfo);
        Intent intent = new Intent(BindMobileCodeUI.this,BindMobileSuccessUI.class);
        intent.putExtra("phonenumber",phoneNumber);
        intent.putExtra("email", email);
        intent.putExtra("type",type);
        startActivity(intent);
        setResult(RESULT_OK);
        Utils.openNewActivityAnim(BindMobileCodeUI.this,true);
    }


    @Override
    public void error(int errorCode, String errorMsg) {
        LoadingDialog.close();
        showToast(errorMsg);
    }

    @Override
    public void setPresenter(BindMobileCodeContract.Presenter presenter) {
        this.mPresenter = presenter;
    }
}
