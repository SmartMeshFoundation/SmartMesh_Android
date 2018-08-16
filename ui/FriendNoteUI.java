package com.lingtuan.firefly.ui;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.network.NetRequestImpl;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MySharedPrefs;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Modify the friends note
 * Created on 2017/10/24.
 */

public class FriendNoteUI extends BaseActivity{

    @BindView(R.id.friendNote)
    EditText friendNote;//Friends note
    @BindView(R.id.app_btn_right)
    TextView save;

    private String localId,note;


    @Override
    protected void setContentView() {
        setContentView(R.layout.friend_note_layout);
        getPassData();
    }

    private void getPassData() {
        localId = getIntent().getStringExtra("localId");
        note = getIntent().getStringExtra("note");
    }

    @Override
    protected void findViewById() {

    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.info_set_note));
        save.setText(getString(R.string.save));
        save.setVisibility(View.VISIBLE);
        if (!TextUtils.isEmpty(note)){
            friendNote.setText(note);
        }

    }

    @OnClick(R.id.app_btn_right)
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.app_btn_right:
                String note = friendNote.getText().toString().trim();
//                if (TextUtils.isEmpty(note)){
//                    showToast(getString(R.string.friend_note_not_empty));
//                    return;
//                }
                updateFriendNote(note);
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    private void updateFriendNote(final String note) {
        NetRequestImpl.getInstance().updateFriendNote(localId, note, new RequestListener() {
            @Override
            public void start() {
                LoadingDialog.show(FriendNoteUI.this,"");
            }

            @Override
            public void success(JSONObject response) {
                LoadingDialog.close();
                FinalUserDataBase.getInstance().updateFriendNote(localId,note);
                Intent intentBrocad = new Intent(Constants.ACTION_CHATTING_FRIEND_NOTE);
                intentBrocad.putExtra("showuid", localId);
                intentBrocad.putExtra("showname",note);
                sendBroadcast(intentBrocad);

//                if (!TextUtils.isEmpty(note)) {
                    MySharedPrefs.write(FriendNoteUI.this, MySharedPrefs.KEY_FRIEND_NOTE + NextApplication.myInfo.getLocalId(), localId, note);
//                }

                showToast(response.optString("msg"));
                Intent intent = new Intent();
                intent.putExtra("note",note);
                setResult(RESULT_OK,intent);
                finish();
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                LoadingDialog.close();
                showToast(errorMsg);
            }
        });
    }
}
