package com.lingtuan.firefly.ui;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.custom.switchbutton.SwitchButton;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;
import com.lingtuan.firefly.vo.UserInfoVo;

import org.json.JSONObject;

/**
 * Friends' profile Settings
 * Created on 2017/10/24.
 */

public class FriendInfoDataSet extends BaseActivity implements CompoundButton.OnCheckedChangeListener {

    private RelativeLayout addNoteBody,report;//Add notes, to join the blacklist, report it
    private SwitchButton joinBlackSb;
    private TextView userName;//The user name

    private static final int FRIEND_NOTE = 100;
    private static final int FRIEND_ADD_BLACK = FRIEND_NOTE + 1;

    private UserInfoVo info;

    private boolean isInBlack;

    @Override
    protected void setContentView() {
        setContentView(R.layout.friend_data_set_layout);
        getPassData();
    }

    private void getPassData() {
        info = (UserInfoVo) getIntent().getSerializableExtra("info");
    }

    @Override
    protected void findViewById() {
        addNoteBody = (RelativeLayout) findViewById(R.id.addNoteBody);
        report = (RelativeLayout) findViewById(R.id.report);
        joinBlackSb = (SwitchButton) findViewById(R.id.joinBlackSb);
        userName = (TextView) findViewById(R.id.userName);
    }

    @Override
    protected void setListener() {
        joinBlackSb.setOnCheckedChangeListener(null);
        addNoteBody.setOnClickListener(this);
        report.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.info_data_set));
        if (info != null){
            userName.setText(info.getNote());
            if (TextUtils.equals(info.getInblack(),"1")){
                isInBlack = true;
                joinBlackSb.setChecked(true);
                joinBlackSb.setBackColor(getResources().getColorStateList(R.color.switch_button_green));
            }else{
                isInBlack = false;
                joinBlackSb.setChecked(false);
                joinBlackSb.setBackColor(getResources().getColorStateList(R.color.switch_button_gray));
            }
            joinBlackSb.setOnCheckedChangeListener(this);
            if (info.getFriendLog() == 0){
                addNoteBody.setVisibility(View.GONE);
                findViewById(R.id.addNotesLine).setVisibility(View.GONE);
            }else{
                addNoteBody.setVisibility(View.VISIBLE);
                findViewById(R.id.addNotesLine).setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.addNoteBody:
                Intent intent = new Intent(FriendInfoDataSet.this,FriendNoteUI.class);
                intent.putExtra("localId",info.getLocalId());
                intent.putExtra("note",info.getNote());
                startActivityForResult(intent,FRIEND_NOTE);
                Utils.openNewActivityAnim(FriendInfoDataSet.this,false);
                break;
            case R.id.report:
                Intent reportIntent = new Intent(FriendInfoDataSet.this,FriendReportUI.class);
                reportIntent.putExtra("localId",info.getLocalId());
                startActivityForResult(reportIntent,FRIEND_ADD_BLACK);
                Utils.openNewActivityAnim(FriendInfoDataSet.this,false);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && requestCode == FRIEND_NOTE){
            String note = data.getStringExtra("note");
            userName.setText(note);
            info.setNote(note);
        } else if (data != null && requestCode == FRIEND_ADD_BLACK){
            isInBlack = true;
            joinBlackSb.setOnCheckedChangeListener(null);
            joinBlackSb.setChecked(true);
            joinBlackSb.setBackColor(getResources().getColorStateList(R.color.switch_button_green));
            joinBlackSb.setOnCheckedChangeListener(FriendInfoDataSet.this);
            Intent intent = new Intent(Constants.ACTION_REFRESH_FRIEND_INBLACK);
            intent.putExtra("isInBlack",true);
            Utils.sendBroadcastReceiver(FriendInfoDataSet.this,intent,true);
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked){
            joinBlackSb.setBackColor(getResources().getColorStateList(R.color.switch_button_green));
        }else{
            joinBlackSb.setBackColor(getResources().getColorStateList(R.color.switch_button_gray));
        }
        updateBlackState(isChecked);
    }

    /**
     * Modify the blacklist
     * */
    private void updateBlackState(final boolean isChecked){
        NetRequestImpl.getInstance().updateBlackState(isChecked ? 0 : 1, info.getLocalId(), new RequestListener() {
            @Override
            public void start() {

            }

            @Override
            public void success(JSONObject response) {
                showToast(response.optString("msg"));
                if (isInBlack) {//Unblock success
                    if (response.optInt("friend_log") == 1) {
                        Constants.isRefresh = true;
                    }
                } else {//Shielding success
                    FinalUserDataBase.getInstance().deleteFriendByUid(info.getLocalId());
                }
                isInBlack = !isInBlack;
                info.setInblack(isInBlack ? "1" : "0");
                Intent intent = new Intent(Constants.ACTION_REFRESH_FRIEND_INBLACK);
                intent.putExtra("isInBlack",isInBlack);
                Utils.sendBroadcastReceiver(FriendInfoDataSet.this,intent,true);
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                showToast(errorMsg);
                joinBlackSb.setOnCheckedChangeListener(null);
                joinBlackSb.setChecked(!isChecked);
                if (!isChecked){
                    joinBlackSb.setBackColor(getResources().getColorStateList(R.color.switch_button_green));
                }else{
                    joinBlackSb.setBackColor(getResources().getColorStateList(R.color.switch_button_gray));
                }
                joinBlackSb.setOnCheckedChangeListener(FriendInfoDataSet.this);
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
    }
}
