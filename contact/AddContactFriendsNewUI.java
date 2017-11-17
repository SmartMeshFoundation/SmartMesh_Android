package com.lingtuan.firefly.contact;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.ExpandableListView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.contact.adapter.AddContactFriendsAdapter;
import com.lingtuan.firefly.contact.vo.PhoneContactGroupVo;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.listener.AddContactListener;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.service.LoadDataService;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The address book friends page
 */
public class AddContactFriendsNewUI extends BaseActivity implements AddContactListener {

    private ExpandableListView mListView;
    private AddContactFriendsAdapter mContactListAdapter;
    private Dialog mProgressDialog;
    private List<PhoneContactGroupVo> mList;
    private RefreshContactReceiver contactReceiver;
    boolean isBack = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void setContentView() {
        setContentView(R.layout.contact_add_friends_layout);
    }

    @Override
    protected void findViewById() {
        mListView = (ExpandableListView) findViewById(R.id.contact_list);
    }

    @Override
    protected void setListener() {
    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.contact_friends));
        mList = new ArrayList<>();
        mContactListAdapter = new AddContactFriendsAdapter(mList, AddContactFriendsNewUI.this);
        mContactListAdapter.setContactListener(this);
        mListView.setAdapter(mContactListAdapter);

        contactReceiver = new RefreshContactReceiver();
        IntentFilter filter = new IntentFilter(Constants.ACTION_SELECT_CONTACT_REFRESH);
        filter.addAction(LoadDataService.ACTION_REFRESH_UNRECOMMENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(contactReceiver, filter);


        mProgressDialog = LoadingDialog.showDialog(AddContactFriendsNewUI.this, null, null);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    isBack = true;
                    Utils.exitActivityAndBackAnim(AddContactFriendsNewUI.this, true);
                }
                return false;
            }
        });
        mProgressDialog.show();

        new ContactThread().start();

    }
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 0:
                    /**Open directory upload service*/
                    Utils.intentServiceAction(AddContactFriendsNewUI.this, LoadDataService.ACTION_UPLOAD_CONTACT, null);
                    break;
                case 1:
                    if(mProgressDialog != null){
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }
                    mContactListAdapter.updateList(mList);
                    break;
            }
        }
    };
    class ContactThread extends Thread {

        @Override
        public void run() {
            mList = FinalUserDataBase.getInstance().getPhoneContactGroup(1);
            if( mList== null || mList.size()<=0)
            {
                mHandler.sendEmptyMessage(0);
            }
            else{
                boolean hasContact = false;
                for(int i=0;i<mList.size();i++)
                {
                    PhoneContactGroupVo vo = mList.get(i);
                    if(vo.getContactList()!=null && vo.getContactList().size()>0)
                    {
                        hasContact = true;
                        break;
                    }
                }
                if(hasContact)
                {
                    mHandler.sendEmptyMessage(1);
                }
                else{
                    mHandler.sendEmptyMessage(0);
                }

            }

        }
    }
    class ContactThread2 extends Thread {

        @Override
        public void run() {
            mList = FinalUserDataBase.getInstance().getPhoneContactGroup(1);
            mHandler.sendEmptyMessage(1);
        }
    }
    @Override
    public void addContactCallback(String telephone, String relation,int addType, String friendNick) {
        if (TextUtils.equals("0", relation)) {
            if (addType == 0) {
//                Uri smsToUri = Uri.parse("smsto:".concat(telephone));
//                Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
//                intent.putExtra("sms_body", getString(R.string.invite_contact_friends_msg_content, Constants.APP_DOWNLOAD_ADDRESS));
//                startActivity(intent);
            } else {
                showToast(getString(R.string.chat_no_support));
            }
        } else if (TextUtils.equals("1", relation)) {
            // Request to add buddy
            NetRequestImpl.getInstance().addFriend(telephone, "","1", new RequestListener() {
                @Override
                public void start() {
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }
                    mProgressDialog = LoadingDialog.showDialog(AddContactFriendsNewUI.this, null, null);
                    mProgressDialog.setCancelable(false);
                }

                @Override
                public void success(JSONObject response) {
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }
                    if (response != null) {
                        showToast(response.optString("msg"));
                    }
                }

                @Override
                public void error(int errorCode, String errorMsg) {
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }
                    showToast(errorMsg);
                }
            });

        }
    }


    @Override
    protected void onDestroy() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
        super.onDestroy();
    }

    /**
     * The refresh friends broadcast
     */
    class RefreshContactReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && LoadDataService.ACTION_REFRESH_UNRECOMMENT.equals(intent.getAction())) {
                new ContactThread2().start();
            }
        }
    }

} 
