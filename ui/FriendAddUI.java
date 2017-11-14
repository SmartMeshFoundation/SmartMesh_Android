package com.lingtuan.firefly.ui;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;

import org.json.JSONObject;

/**
 * Add buddy UI
 * Created on 2017/10/26.
 */

public class FriendAddUI extends BaseActivity {

    private EditText contentEt;
    private EditText addNoteEt;

    private TextView rightBtn;

    private String localId;

    @Override
    protected void setContentView() {
        setContentView(R.layout.friend_add_layout);
        getPassData();
    }

    private void getPassData() {
        localId = getIntent().getStringExtra("localId");
    }

    @Override
    protected void findViewById() {
        contentEt = (EditText) findViewById(R.id.contentEt);
        addNoteEt = (EditText) findViewById(R.id.addNoteEt);
        rightBtn = (TextView) findViewById(R.id.app_btn_right);
    }

    @Override
    protected void setListener() {
        rightBtn.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.add_friends_validation));
        rightBtn.setVisibility(View.VISIBLE);
        rightBtn.setText(getString(R.string.send));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.app_btn_right:
                addFriendsMethod();
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    /**
     * Add buddy method
     * */
    private void addFriendsMethod() {
        String content = contentEt.getText().toString().trim();
        String note = addNoteEt.getText().toString().trim();

        NetRequestImpl.getInstance().addFriend(localId,note ,content, new RequestListener() {
            @Override
            public void start() {
                LoadingDialog.show(FriendAddUI.this,"");
            }

            @Override
            public void success(JSONObject response) {
                LoadingDialog.close();
                showToast(response.optString("msg"));
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                LoadingDialog.close();
                showToast(errorMsg);
            }
        });
    }
}
