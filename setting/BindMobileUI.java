package com.lingtuan.firefly.setting;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.ui.CountryCodeUI;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MyViewDialogFragment;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;
import com.lingtuan.firefly.vo.CountryCodeVo;

import org.json.JSONObject;

/**
 * Created on 2017/10/11.
 * Binding mobile phone number
 */

public class BindMobileUI extends BaseActivity {

    private TextView btnRight;

    private TextView countyName,countyCode;

    private EditText phoneEt;

    private CountryCodeVo mCountryCode;

    private String aeskey = null ;

    private int type;//0 binding mobile phone number, 1 binding inbox, 2 phone number retrieve password, 3 retrieve password

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
        btnRight = (TextView) findViewById(R.id.app_btn_right);
        countyName = (TextView) findViewById(R.id.countyName);
        countyCode = (TextView) findViewById(R.id.countyCode);
        phoneEt = (EditText) findViewById(R.id.phoneEt);
    }

    @Override
    protected void setListener() {
        btnRight.setOnClickListener(this);
        countyName.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        if (type == 2){
            setTitle(getString(R.string.forgot_password_hint));
        }else{
            setTitle(getString(R.string.bind_mobile));
        }
        btnRight.setVisibility(View.VISIBLE);
        btnRight.setText(getString(R.string.next));
        mCountryCode = new CountryCodeVo();
        mCountryCode.setCode("1");
        mCountryCode.setName(getString(R.string.usa));

    }

    @Override
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
        this.aeskey = Utils.makeRandomKey(16);
        final String phoneNumber = "+" + mCountryCode.getCode() + " " + phoneEt.getText().toString().trim();
        NetRequestImpl.getInstance().sendSmsc(aeskey, type == 0 ? 0 : 1,phoneNumber,new RequestListener() {
            @Override
            public void start() {
                LoadingDialog.show(BindMobileUI.this,"");
            }

            @Override
            public void success(JSONObject response) {
                LoadingDialog.close();
                showToast(response.optString("msg"));
                Intent intent = new Intent(BindMobileUI.this,BindMobileCodeUI.class);
                intent.putExtra("phonemubmer",phoneNumber);
                intent.putExtra("type",type);
                startActivity(intent);
                Utils.openNewActivityAnim(BindMobileUI.this,false);
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                LoadingDialog.close();
                showToast(errorMsg);
            }
        });
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
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
