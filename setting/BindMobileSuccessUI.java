package com.lingtuan.firefly.setting;

import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.util.Utils;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * binding mobile phone number success page
 * binding mailbox success page
 * Created on 2017/10/24.
 */

public class BindMobileSuccessUI extends BaseActivity {

    @BindView(R.id.phoneNumber)
    TextView phoneNumber;
    @BindView(R.id.phoneImg)
    ImageView phoneImg;
    @BindView(R.id.phoneBook)
    TextView phoneBook;//Enter the address book
    @BindView(R.id.replacePhone)
    TextView replacePhone;//Replace a phone number

    private static final int BINDMOBLECODE = 100;

    private String phonenumber;//Mobile phone no.
    private String email;//email

    private int type;//0 binding mobile, 1 binding email, 2 forget password

    @Override
    protected void setContentView() {
        setContentView(R.layout.bind_mobile_success_layout);
        getPassData();
    }

    private void getPassData() {
        phonenumber = getIntent().getStringExtra("phonenumber");
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
        if (type == 0){
            setTitle(getString(R.string.bind_mobile));
            phoneNumber.setText(getString(R.string.bind_mobile_number,phonenumber));
            phoneImg.setImageResource(R.drawable.bind_mobile_success);
            replacePhone.setText(getString(R.string.replace_phone));
            phoneBook.setVisibility(View.VISIBLE);
        }else if (type == 1){
            setTitle(getString(R.string.bind_email));
            phoneNumber.setText(getString(R.string.bind_email_number,email));
            phoneImg.setImageResource(R.drawable.bind_email_success);
            replacePhone.setText(getString(R.string.replace_email));
            phoneBook.setVisibility(View.GONE);
        }
    }

    @OnClick({R.id.phoneBook,R.id.replacePhone})
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.phoneBook:
                Uri uri = Uri.parse("content://contacts/people");
                Intent addIntent = new Intent(Intent.ACTION_PICK, uri);
                startActivity(addIntent);
                Utils.openNewActivityAnim(BindMobileSuccessUI.this,false);
                break;
            case R.id.replacePhone:
                if (type == 1){
                    Intent intent = new Intent(BindMobileSuccessUI.this,BindEmailUI.class);
                    intent.putExtra("type",type);
                    startActivityForResult(intent,BINDMOBLECODE);
                    Utils.openNewActivityAnim(BindMobileSuccessUI.this,true);
                }else{
                    Intent intent = new Intent(BindMobileSuccessUI.this,BindMobileUI.class);
                    intent.putExtra("type",type);
                    startActivityForResult(intent,BINDMOBLECODE);
                    Utils.openNewActivityAnim(BindMobileSuccessUI.this,true);
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == BINDMOBLECODE) {
            finish();
        }
    }
}
