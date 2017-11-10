package com.lingtuan.firefly.login;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.setting.BindEmailUI;
import com.lingtuan.firefly.setting.BindMobileUI;
import com.lingtuan.firefly.util.Utils;

/**
 * Created on 2017/10/11.
 * Forgot password page
 */

public class ForgotPwdUI extends BaseActivity {

    private RelativeLayout bindMobileBody,bindEmailBody;//Mobile phone number to retrieve password email retrieve password

    @Override
    protected void setContentView() {
       setContentView(R.layout.forget_layout);
    }

    @Override
    protected void findViewById() {
        bindMobileBody = (RelativeLayout) findViewById(R.id.bindMobileBody);
        bindEmailBody = (RelativeLayout) findViewById(R.id.bindEmailBody);
    }

    @Override
    protected void setListener() {
        bindMobileBody.setOnClickListener(this);
        bindEmailBody.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.forgot_password_hint));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bindMobileBody:
                Intent intentMobile = new Intent(this,BindMobileUI.class);
                intentMobile.putExtra("type",2);
                startActivity(intentMobile);
                Utils.openNewActivityAnim(ForgotPwdUI.this,false);
                break;
            case R.id.bindEmailBody:
                Intent intent = new Intent(this,BindEmailUI.class);
                intent.putExtra("type",3);
                startActivity(intent);
                Utils.openNewActivityAnim(ForgotPwdUI.this,false);
                break;
            default:
                super.onClick(v);
                break;
        }
    }
}
