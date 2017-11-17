package com.lingtuan.firefly.contact;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.quickmark.CaptureActivity;
import com.lingtuan.firefly.service.LoadDataService;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.MyViewDialogFragment;
import com.lingtuan.firefly.util.Utils;


/**
 * Revised page add contact
 */
public class AddFriendsUI extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void setContentView() {
        setContentView(R.layout.add_friends_layout);
    }

    @Override
    protected void findViewById() {
    }

    @Override
    protected void setListener() {
        findViewById(R.id.byContactLinear).setOnClickListener(this);
        findViewById(R.id.byScanLinear).setOnClickListener(this);
        findViewById(R.id.searchRel).setOnClickListener(this);
    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.add));
    }


    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.searchRel: // And search contacts < nickname MainContactFragmentUI. Java onClick in search?>
                startActivity(new Intent(AddFriendsUI.this, ContactSearchNickUI.class));
                Utils.openNewActivityAnim(AddFriendsUI.this, false);
                break;
            case R.id.byContactLinear: // Contacts to add

                final String tempGxb = "tempGxb";
                boolean flag = MySharedPrefs.readBooleanNormal(AddFriendsUI.this, MySharedPrefs.FILE_USER, tempGxb);
                if (flag) {    // Has been suggested before (directory permissions have been authorized)
                    startActivity(new Intent(AddFriendsUI.this, AddContactFriendsNewUI.class));
                    Utils.openNewActivityAnim(AddFriendsUI.this, false);
                } else { //Do not prompt
                    MyViewDialogFragment mdf = new MyViewDialogFragment();
                    mdf.setTitleAndContentText(getString(R.string.notif), getString(R.string.contact_smt_accect));
                    mdf.setOkCallback(new MyViewDialogFragment.OkCallback() {
                        @Override
                        public void okBtn() {
                            MySharedPrefs.writeBoolean(AddFriendsUI.this, MySharedPrefs.FILE_USER, tempGxb, true);
                            /**Send the address book to monitor radio*/
                            Intent intent = new Intent(LoadDataService.ACTION_START_CONTACT_LISTENER);
                            Utils.sendBroadcastReceiver(AddFriendsUI.this, intent, false);
                            startActivity(new Intent(AddFriendsUI.this, AddContactFriendsNewUI.class));
                            Utils.openNewActivityAnim(AddFriendsUI.this, false);
                        }
                    });
                    mdf.show(getSupportFragmentManager(), "mdf");
                }
                break;
            case R.id.byScanLinear:// scan
                startActivity(new Intent(this, CaptureActivity.class));
                Utils.openNewActivityAnim(this, false);
                break;
            default:
                break;
        }
    }


    @Override
    public void onBackPressed() {
        Utils.exitActivityAndBackAnim(this, true);
        super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Utils.exitActivityAndBackAnim(this, true);
        }
        return super.onKeyDown(keyCode, event);
    }

}
