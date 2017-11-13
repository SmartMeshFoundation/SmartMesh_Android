package com.lingtuan.firefly.setting;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.util.Utils;

/**
 * Created on 2017/10/11.
 * Security page
 */

public class SecurityUI extends BaseActivity {
    private RelativeLayout bindMobileBody,bindEmailBody;//Binding mobile phone number, binding mailbox
    @Override
    protected void setContentView() {
       setContentView(R.layout.security_layout);
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
        setTitle(getString(R.string.security));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bindMobileBody:
                if (NextApplication.myInfo != null && !TextUtils.isEmpty(NextApplication.myInfo.getMobile())){
                    Intent intent = new Intent(this,BindMobileSuccessUI.class);
                    intent.putExtra("phonenumber",NextApplication.myInfo.getMobile());
                    intent.putExtra("type",0);
                    startActivity(intent);
                }else{
                    startActivity(new Intent(this,BindMobileUI.class));
                }
                Utils.openNewActivityAnim(SecurityUI.this,false);
                break;
            case R.id.bindEmailBody:
                if (NextApplication.myInfo != null && !TextUtils.isEmpty(NextApplication.myInfo.getEmail())){
                    Intent intent = new Intent(this,BindMobileSuccessUI.class);
                    intent.putExtra("email",NextApplication.myInfo.getEmail());
                    intent.putExtra("type",1);
                    startActivity(intent);
                }else{
                    startActivity(new Intent(this,BindEmailUI.class));
                }
                Utils.openNewActivityAnim(SecurityUI.this,false);
                break;
            default:
                super.onClick(v);
                break;
        }
    }
}
