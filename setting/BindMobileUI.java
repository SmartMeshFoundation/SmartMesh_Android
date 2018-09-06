package com.lingtuan.firefly.setting;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.network.NetRequestImpl;
import com.lingtuan.firefly.setting.contract.BindMobileContract;
import com.lingtuan.firefly.setting.presenter.BindMobilePresenterImpl;
import com.lingtuan.firefly.ui.CountryCodeUI;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MyViewDialogFragment;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.vo.CountryCodeVo;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created on 2017/10/11.
 * Binding mobile phone number
 */

public class BindMobileUI extends BaseActivity implements BindMobileContract.View {

    private static final int BINDMOBLECODE = 100;

    @BindView(R.id.app_btn_right)
    TextView btnRight;
    @BindView(R.id.countyName)
    TextView countyName;
    @BindView(R.id.countyCode)
    TextView countyCode;
    @BindView(R.id.phoneEt)
    EditText phoneEt;
    @BindView(R.id.bindMobileHint)
    TextView bindMobileHint;

    private CountryCodeVo mCountryCode;
    //0 binding mobile, 1 binding email, 2 phone number retrieve password, 3 email retrieve password
    private int type;

    private BindMobileContract.Presenter mPresenter;

    @Override
    protected void setContentView() {
        setContentView(R.layout.bind_mobile_layout);
        getPassData();
    }

    private void getPassData() {
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
        new BindMobilePresenterImpl(this);
        if (type == 2){
            setTitle(getString(R.string.forgot_password_hint));
            bindMobileHint.setVisibility(View.GONE);
        }else{
            setTitle(getString(R.string.bind_mobile));
            bindMobileHint.setVisibility(View.VISIBLE);
        }
        btnRight.setVisibility(View.VISIBLE);
        btnRight.setText(getString(R.string.next));
        mCountryCode = new CountryCodeVo();
        mCountryCode.setCode("65");
        mCountryCode.setName(getString(R.string.singapore));

    }

    @OnClick({R.id.app_btn_right,R.id.countyName})
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.app_btn_right:
                sendVerificationCode();
                break;
            case R.id.countyName:
                Intent countryIntent = new Intent(this, CountryCodeUI.class);
                countryIntent.putExtra("countrycode", mCountryCode);
                startActivityForResult(countryIntent, Constants.COUNTRY_CODE_RESULT);
                Utils.openNewActivityAnim(this, false);
                break;
        }
    }

    /**
     * Send verification code
     * */
    private void sendVerificationCode() {
        String phoneNumber = phoneEt.getText().toString().trim();
        if (TextUtils.isEmpty(phoneNumber)){
            showToast(getString(R.string.enter_phone_number));
            return;
        }
        MyViewDialogFragment mdf = new MyViewDialogFragment();
        mdf.setTitleAndContentText(getString(R.string.confirm_phone_number), getString(R.string.send_verification_warning,phoneNumber));
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
        final String phoneNumber = "+" + mCountryCode.getCode() + " " + phoneEt.getText().toString().trim();
        mPresenter.sendSms(type,phoneNumber);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode){
                case Constants.COUNTRY_CODE_RESULT:
                    if (data != null && data.getSerializableExtra("countrycode") != null) {
                        mCountryCode = (CountryCodeVo) data.getSerializableExtra("countrycode");
                        countyName.setText(mCountryCode.getName());
                        countyCode.setText(getString(R.string.county_code,mCountryCode.getCode()));
                    }
                    break;
                case BINDMOBLECODE:
                    setResult(RESULT_OK);
                    finish();
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void setPresenter(BindMobileContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public void sendSmsStart() {
        LoadingDialog.show(BindMobileUI.this,"");
    }

    @Override
    public void sendSmsSuccess(String message, int type, String phoneNumber) {
        LoadingDialog.close();
        showToast(message);
        Intent intent = new Intent(BindMobileUI.this,BindMobileCodeUI.class);
        intent.putExtra("phonemubmer",phoneNumber);
        intent.putExtra("type",type);
        startActivityForResult(intent,BINDMOBLECODE);
        Utils.openNewActivityAnim(BindMobileUI.this,false);
    }

    @Override
    public void sendSmsError(int errorCode, String errorMsg) {
        LoadingDialog.close();
        showToast(errorMsg);
    }
}
