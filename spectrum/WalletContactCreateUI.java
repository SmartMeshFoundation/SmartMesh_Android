package com.lingtuan.firefly.spectrum;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.quickmark.CaptureActivity;
import com.lingtuan.firefly.spectrum.vo.AddressContactVo;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.Utils;

import butterknife.BindView;
import butterknife.OnClick;

public class WalletContactCreateUI extends BaseActivity {

    @BindView(R.id.app_btn_right)
    TextView appBtnRight;
    @BindView(R.id.userName)
    EditText userName;
    @BindView(R.id.walletAddress)
    EditText walletAddress;
    @BindView(R.id.contactNote)
    EditText contactNote;

    private AddressContactVo addressContactVo;

    private static final int WALLET_CONTACT_SCAN = 100;

    @Override
    protected void setContentView() {
        setContentView(R.layout.wallet_contact_create_layout);
        getPassData();
    }

    private void getPassData() {
        addressContactVo = (AddressContactVo) getIntent().getSerializableExtra(Constants.WALLET_CONTACT);
    }

    @Override
    protected void findViewById() {

    }

    @Override
    protected void setListener() {

    }

    @OnClick({R.id.walletAddressScan,R.id.app_btn_right})
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.walletAddressScan:
                Intent i = new Intent(WalletContactCreateUI.this,CaptureActivity.class);
                i.putExtra("type",2);
                startActivityForResult(i,WALLET_CONTACT_SCAN);
                break;
            case R.id.app_btn_right:
                createWalletContact();
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    private void createWalletContact(){
        String address = walletAddress.getText().toString();
        String username = userName.getText().toString();
        if(!address.startsWith("0x") ||  address.length()!=42){
            showToast(getString(R.string.error_address_1));
            return;
        }
        if (TextUtils.isEmpty(username)){
            showToast(getString(R.string.wallet_contact_usernmae_empty));
            return;
        }
        LoadingDialog.show(WalletContactCreateUI.this,"");
        AddressContactVo contactVo = new AddressContactVo();
        contactVo.setWalletAddress(address);
        contactVo.setUserName(username);
        contactVo.setRemarks(contactNote.getText().toString());
        if (addressContactVo == null){
            boolean hasExist = FinalUserDataBase.getInstance().hasWalletContact(address);
            if (hasExist){
                showToast(getString(R.string.wallet_contact_exist));
                LoadingDialog.close();
                return;
            }
            FinalUserDataBase.getInstance().insertWalletContact(contactVo);
            showToast(getString(R.string.wallet_contact_create_success));
        }else{
            boolean oldHasExist = FinalUserDataBase.getInstance().hasWalletContact(addressContactVo.getWalletAddress());
            boolean newHasExist = FinalUserDataBase.getInstance().hasWalletContact(address);
            if (oldHasExist){
                if (newHasExist){
                    if (!TextUtils.equals(addressContactVo.getWalletAddress(),address)){
                        showToast(getString(R.string.wallet_contact_exist));
                        LoadingDialog.close();
                        return;
                    }
                    FinalUserDataBase.getInstance().updateWalletContact(addressContactVo.getWalletAddress(),contactVo);
                }else{
                    FinalUserDataBase.getInstance().updateWalletContact(addressContactVo.getWalletAddress(),contactVo);
                }
            }else{
                if (newHasExist){
                    FinalUserDataBase.getInstance().updateWalletContact(address,contactVo);
                }else{
                    FinalUserDataBase.getInstance().insertWalletContact(contactVo);
                }
            }
            showToast(getString(R.string.wallet_contact_edit_success));
        }
        LoadingDialog.close();
        Utils.exitActivityAndBackAnim(WalletContactCreateUI.this,true);
    }

    @Override
    protected void initData() {
        appBtnRight.setVisibility(View.VISIBLE);
        if (addressContactVo == null) {
            appBtnRight.setText(getString(R.string.finish));
            setTitle(getString(R.string.wallet_contact_create_title));
        } else {
            appBtnRight.setText(getString(R.string.save));
            setTitle(getString(R.string.wallet_contact_edit_title));
            userName.setText(addressContactVo.getUserName());
            contactNote.setText(addressContactVo.getRemarks());
            walletAddress.setText(addressContactVo.getWalletAddress());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && WALLET_CONTACT_SCAN == requestCode){
            String address = data.getStringExtra("address");
            walletAddress.setText(address);
        }
    }
}
