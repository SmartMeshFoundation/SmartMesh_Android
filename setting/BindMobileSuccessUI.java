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

/**
 * binding mobile phone number success page
 * binding mailbox success page
 * Created on 2017/10/24.
 */

public class BindMobileSuccessUI extends BaseActivity {

    private String phonenumber;//Mobile phone no.
    private String email;//email

    private int type;//0 binding mobile phone number, 1 binding mailbox, 2 forget password

    private TextView phoneNumber;
    private ImageView phoneImg;

    private TextView phoneBook;//Enter the address book
    private TextView replacePhone;//Replace a phone number

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
        phoneNumber = (TextView) findViewById(R.id.phoneNumber);
        phoneImg = (ImageView) findViewById(R.id.phoneImg);
        phoneBook = (TextView) findViewById(R.id.phoneBook);
        replacePhone = (TextView) findViewById(R.id.replacePhone);
    }

    @Override
    protected void setListener() {
        phoneBook.setOnClickListener(this);
        replacePhone.setOnClickListener(this);
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

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.phoneBook:
                Intent addIntent = new Intent(Intent.ACTION_INSERT,Uri.withAppendedPath(Uri.parse("content://com.android.contacts"), "contacts"));
                addIntent.setType("vnd.android.cursor.dir/person");
                addIntent.setType("vnd.android.cursor.dir/contact");
                addIntent.setType("vnd.android.cursor.dir/raw_contact");
                addIntent.putExtra(ContactsContract.Intents.Insert.NAME,phonenumber);
                addIntent.putExtra(android.provider.ContactsContract.Intents.Insert.PHONE, phonenumber);
                startActivity(addIntent);
                Utils.openNewActivityAnim(BindMobileSuccessUI.this,false);
                break;
            case R.id.replacePhone:
                if (type == 1){
                    startActivity(new Intent(BindMobileSuccessUI.this,BindEmailUI.class));
                    Utils.openNewActivityAnim(BindMobileSuccessUI.this,true);
                }else{
                    startActivity(new Intent(BindMobileSuccessUI.this,BindMobileUI.class));
                    Utils.openNewActivityAnim(BindMobileSuccessUI.this,true);
                }
                break;
        }
    }
}
