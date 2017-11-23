package com.lingtuan.firefly.chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.chat.vo.FileChildVo;
import com.lingtuan.firefly.contact.ContactSelectedUI;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.service.LoadDataService;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.FileSizeUtils;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;
import com.lingtuan.firefly.vo.ChatMsg;
import com.lingtuan.firefly.xmpp.XmppAction;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * The file details interface
 * Created on 2016/6/7.
 */
public class ChatFileInfoUI extends BaseActivity implements View.OnClickListener {
    private ImageView imgFileLogo, imgCancel;
    private TextView txtFileName, txtExpireTime, bottomLeftBtn, bottomMiddleBtn, bottomRightBtn, txtProgress, offlineProgress;
    private String fileUrl, localUrl, chatid, name;
    private ProgressBar progressBar;
    private RelativeLayout downloadingRela;
    private LinearLayout notDownloadLena;


    private String msgid, fileId;
    private boolean isOffLine;//No net receiving and sending files
    private boolean isGroupOrFavFile;//Whether the group or collection files
    private int sourceType;//The file type  0 message file  1  group of files   2 group of files and expired
    private int state, collectState;
    private long number, currentLength, msgTime;
    private MsgBroadcastReceiver receiver = new MsgBroadcastReceiver();
    private String[][] MIME_MapTable = {
            {".3gp", "video/3gpp"},
            {".apk", "application/vnd.android.package-archive"},
            {".asf", "video/x-ms-asf"},
            {".avi", "video/x-msvideo"},
            {".bin", "application/octet-stream"},
            {".bmp", "image/bmp"},
            {".c", "text/plain"},
            {".class", "application/octet-stream"},
            {".conf", "text/plain"},
            {".cpp", "text/plain"},
            {".doc", "application/msword"},
            {".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"},
            {".xls", "application/vnd.ms-excel"},
            {".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"},
            {".exe", "application/octet-stream"},
            {".gif", "image/gif"},
            {".gtar", "application/x-gtar"},
            {".gz", "application/x-gzip"},
            {".h", "text/plain"},
            {".htm", "text/html"},
            {".html", "text/html"},
            {".jar", "application/java-archive"},
            {".java", "text/plain"},
            {".jpeg", "image/jpeg"},
            {".jpg", "image/jpeg"},
            {".js", "application/x-javascript"},
            {".log", "text/plain"},
            {".m3u", "audio/x-mpegurl"},
            {".m4a", "audio/mp4a-latm"},
            {".m4b", "audio/mp4a-latm"},
            {".m4p", "audio/mp4a-latm"},
            {".m4u", "video/vnd.mpegurl"},
            {".m4v", "video/x-m4v"},
            {".mov", "video/quicktime"},
            {".mp2", "audio/x-mpeg"},
            {".mp3", "audio/x-mpeg"},
            {".mp4", "video/mp4"},
            {".mpc", "application/vnd.mpohun.certificate"},
            {".mpe", "video/mpeg"},
            {".mpeg", "video/mpeg"},
            {".mpg", "video/mpeg"},
            {".mpg4", "video/mp4"},
            {".mpga", "audio/mpeg"},
            {".msg", "application/vnd.ms-outlook"},
            {".ogg", "audio/ogg"},
            {".pdf", "application/pdf"},
            {".png", "image/png"},
            {".pps", "application/vnd.ms-powerpoint"},
            {".ppt", "application/vnd.ms-powerpoint"},
            {".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"},
            {".prop", "text/plain"},
            {".rc", "text/plain"},
            {".rmvb", "audio/x-pn-realaudio"},
            {".rtf", "application/rtf"},
            {".sh", "text/plain"},
            {".tar", "application/x-tar"},
            {".tgz", "application/x-compressed"},
            {".txt", "text/plain"},
            {".wav", "audio/x-wav"},
            {".wma", "audio/x-ms-wma"},
            {".wmv", "audio/x-ms-wmv"},
            {".wps", "application/vnd.ms-works"},
            {".xml", "text/plain"},
            {".z", "application/x-compress"},
            {".zip", "application/zip"},
            {"", "*/*"}
    };

    @Override
    protected void setContentView() {
        setContentView(R.layout.chat_file_info_layout);
    }

    @Override
    protected void findViewById() {
        imgFileLogo = (ImageView) findViewById(R.id.img_file_logo);
        txtFileName = (TextView) findViewById(R.id.txt_file_name);
        txtExpireTime = (TextView) findViewById(R.id.txt_expire_time);
        bottomLeftBtn = (TextView) findViewById(R.id.bottom_left_btn);
        bottomMiddleBtn = (TextView) findViewById(R.id.bottom_middle_btn);
        bottomRightBtn = (TextView) findViewById(R.id.bottom_right_btn);
        txtProgress = (TextView) findViewById(R.id.bottom_progress);
        offlineProgress = (TextView) findViewById(R.id.offline_bottom_progress);
        imgCancel = (ImageView) findViewById(R.id.img_cancel);
        progressBar = (ProgressBar) findViewById(R.id.progress_download);
        notDownloadLena = (LinearLayout) findViewById(R.id.not_download_lena);
        downloadingRela = (RelativeLayout) findViewById(R.id.file_downloading_rela);
    }

    @Override
    protected void setListener() {
        bottomLeftBtn.setOnClickListener(this);
        bottomMiddleBtn.setOnClickListener(this);
        bottomRightBtn.setOnClickListener(this);
        imgCancel.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        name = getIntent().getStringExtra("name");
        fileUrl = getIntent().getStringExtra("fileUrl");
        number = getIntent().getLongExtra("number", 0);
        currentLength = getIntent().getLongExtra("currentLength", 0);
        msgTime = getIntent().getLongExtra("time", 0);
        msgid = getIntent().getStringExtra("msgid");
        isOffLine = getIntent().getBooleanExtra("isOffLine", false);
        isGroupOrFavFile = getIntent().getBooleanExtra("isGroupOrFavFile", false);
        sourceType = getIntent().getIntExtra("sourceType", 0);
        setTitle(name);
        txtFileName.setText(name);
        IntentFilter filter = new IntentFilter();
        if (isOffLine) {
            state = getIntent().getIntExtra("state", -1);
            File file = new File(fileUrl);
            txtExpireTime.setText(file.getParent());
            resetOffLineView();
            filter.addAction(Constants.MSG_REPORT_SEND_MSG_RESULT);
            filter.addAction(Constants.MSG_REPORT_SEND_MSG_PROGRESS);
            Utils.showFileIcon(this, name, fileUrl, imgFileLogo);
        } else {
            filter.addAction(Constants.ACTION_DOWNLOAD_CHAT_FILE);
            filter.addAction(Constants.ACTION_DOWNLOAD_CHAT_FILE_SUCCESS);
            filter.addAction(Constants.ACTION_DOWNLOAD_CHAT_FILE_FAILED);
            filter.addAction(XmppAction.ACTION_MESSAGE_UPDATE_LISTENER);
            filter.addAction(XmppAction.ACTION_MESSAGE_FILE_PERCENT);
            fileId = getIntent().getStringExtra("fileid");
            state = getIntent().getIntExtra("state", 4);
            collectState = getIntent().getIntExtra("collectState", 0);
            localUrl = getIntent().getStringExtra("localUrl");
            chatid = getIntent().getStringExtra("chatid");

            bottomRightBtn.setText(collectState == 0 ? getString(R.string.chat_file_collected) : getString(R.string.chat_file_already_collected));
            resetExpiredView();
            resetView();
            Utils.showFileIcon(this, name, localUrl, imgFileLogo);
        }

        registerReceiver(receiver, filter);
    }

    private void resetExpiredView() {
        long expireTime = msgTime + 7 * 24 * 3600;
        String time = Utils.formatTime(expireTime);
        if (state == 0 || state == 1 || state == 3) {   //Don't show time
            txtExpireTime.setVisibility(View.GONE);
        } else if (sourceType == 1) {
            txtExpireTime.setVisibility(View.VISIBLE);
            txtExpireTime.setText(getString(R.string.permanent));
        } else if (sourceType == 2) {
            txtExpireTime.setVisibility(View.VISIBLE);
            txtExpireTime.setText(getString(R.string.file_delete));
            bottomMiddleBtn.setEnabled(false);
            if (state == 4 || state == 6 || state == 8){//Did not download. Cancel the download. Download failed
                bottomLeftBtn.setEnabled(false);
            }
        } else {
            txtExpireTime.setVisibility(View.VISIBLE);
            if (msgTime <= 0) {
                txtExpireTime.setText(getString(R.string.permanent));
            } else {
                if (System.currentTimeMillis() / 1000 > expireTime) {   //Have failed
                    if (state == 4 || state == 6 || state == 8)//Download download/cancel/failure
                    {
                        bottomLeftBtn.setEnabled(false);
                    }
                    bottomMiddleBtn.setEnabled(false);
                    txtExpireTime.setText(getString(R.string.file_outdate));
                } else {
                    txtExpireTime.setText(getString(R.string.outdate_time, time));
                }

            }
        }
    }

    private void resetOffLineView() {

        bottomMiddleBtn.setVisibility(View.GONE);
        bottomRightBtn.setVisibility(View.GONE);
        offlineProgress.setVisibility(View.GONE);
        bottomLeftBtn.setVisibility(View.GONE);
        if (state == 0 || state == 5)//Need to display the current progress on the cross and download
        {
            offlineProgress.setText(FileSizeUtils.formatFileSize(currentLength) + "/" + FileSizeUtils.formatFileSize(number));
            offlineProgress.setVisibility(View.VISIBLE);
        }

        if (state == -1)//Is waiting for receiving the sender
        {
            bottomRightBtn.setVisibility(View.VISIBLE);
            bottomRightBtn.setText(getString(R.string.chatting_cancel_sending));
        } else if (state == 0)//The sender is the sender

        {
            bottomRightBtn.setVisibility(View.VISIBLE);
            bottomRightBtn.setText(getString(R.string.chatting_cancel_sending));
        } else if (state == 4)//Not to download, receiver
        {
            bottomRightBtn.setVisibility(View.VISIBLE);
            bottomRightBtn.setText(getString(R.string.chatting_receive));
        } else if (state == 5)//Received, the receiving party
        {
            bottomRightBtn.setVisibility(View.VISIBLE);
            bottomRightBtn.setText(getString(R.string.chatting_cancel_receiving));
        }

        if (state == -1 || state == 0 || state == 1 || state == 2 || state == 3 || state == 7) {
            bottomLeftBtn.setVisibility(View.VISIBLE);
        }


    }

    /**
     * According to the state of the state to reset the view
     */
    private void resetView() {
        switch (state) {
            case 0:      //On the cross
                notDownloadLena.setVisibility(View.GONE);
                downloadingRela.setVisibility(View.VISIBLE);
                int percent = (int) ((currentLength * 100) / (number * 1.0f));
                progressBar.setProgress(percent);
                txtProgress.setText(getString(R.string.txt_uploading_progress, FileSizeUtils.formatFileSize(currentLength), FileSizeUtils.formatFileSize(number)));
                break;
            case 1:       //Upload failed
            case 3:       //Cancel the upload
                notDownloadLena.setVisibility(View.VISIBLE);
                downloadingRela.setVisibility(View.GONE);
                bottomLeftBtn.setText(getString(R.string.re_upload_file));
                break;
            case 2:       //Uploaded successfully
                notDownloadLena.setVisibility(View.VISIBLE);
                downloadingRela.setVisibility(View.GONE);
                bottomLeftBtn.setText(getString(R.string.use_other_app_open));
                break;
            case 4:       //Did not download
                notDownloadLena.setVisibility(View.VISIBLE);
                downloadingRela.setVisibility(View.GONE);
                bottomLeftBtn.setText(getString(R.string.download_file, FileSizeUtils.formatFileSize(number)));
                break;
            case 5:       //In the download
                notDownloadLena.setVisibility(View.GONE);
                downloadingRela.setVisibility(View.VISIBLE);
                int percen = (int) ((currentLength * 100) / (number * 1.0f));
                progressBar.setProgress(percen);
                txtProgress.setText(getString(R.string.txt_downloading_progress, FileSizeUtils.formatFileSize(currentLength), FileSizeUtils.formatFileSize(number)));
                break;
            case 6:       //Download failed
            case 8:       //Cancel the download
                notDownloadLena.setVisibility(View.VISIBLE);
                downloadingRela.setVisibility(View.GONE);
                bottomLeftBtn.setText(getString(R.string.re_download_file));
                break;
            case 7:       //Download successful
                notDownloadLena.setVisibility(View.VISIBLE);
                downloadingRela.setVisibility(View.GONE);
                bottomLeftBtn.setText(getString(R.string.use_other_app_open));
                break;
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.bottom_left_btn:
                if (isOffLine) {
                    File file = new File(fileUrl);
                    openFile(file);
                } else {
                    if (state == 1 || state == 3) {       //Failed to upload or cancel the upload
                        /**********Send a broadcast to the chat interface, update the transfer status and upload a file again***************/
                        sendBroadcast(msgid, 0, Constants.ACTION_START_UPLOAD_FILE);
                        state = 0;
                        resetView();
                    } else if (state == 2) {           //Uploaded successfully
                        if (!TextUtils.isEmpty(localUrl)) {
                            File file = new File(localUrl);
                            openFile(file);
                        } else {
                            showToast(getString(R.string.chat_file_deleted));
                        }
                    } else if (state == 4 || state == 6 || state == 8) {      //Did not download or failed to download or cancel the download
                        /**********step1: To download the logic***************/
                        final Bundle bundle = new Bundle();
                        bundle.putString("url", fileUrl);
                        bundle.putString("msgid", msgid);
                        bundle.putString("fileid", fileId);

                        bundle.putBoolean("isGroupOrFavFile", isGroupOrFavFile);
                        Utils.intentServiceAction(this, LoadDataService.ACTION_DOWNLOAD_CHAT_FILE, bundle);
                        /**********step2: Send a broadcast to the chat interface, update the transfer status***************/
                        sendBroadcast(msgid, 5, Constants.ACTION_START_DOWNLOAD_FILE);
                        state = 5;
                        resetView();
                    } else if (state == 7) {           //Download successful
                        if (!TextUtils.isEmpty(localUrl)) {
                            File file = new File(localUrl);
                            openFile(file);
                        }
                    }
                }
                break;
            case R.id.bottom_right_btn:
                if (isOffLine) {      //Offline files
                    if (state == -1 || state == 0) {           //Is waiting for receiving the sender
                        state = 3;
                        sendBroadcast(msgid, 2, Constants.MSG_REPORT_CANCEL_SEND_FILE);
                    } else if (state == 4) {    //Did not download the receiver
                        state = 5;
                        sendBroadcast(msgid, 0, Constants.MSG_REPORT_START_RECV_FILE);
                    } else if (state == 5) {          //Receive in the receiver
                        state = 8;
                        sendBroadcast(msgid, 1, Constants.MSG_REPORT_CANCEL_RECV_FILE);
                    }
                    resetOffLineView();
                } else {
                    if (state == 0 || state == 1 || state == 3) {
                        showToast(getString(R.string.fav_file_warning));
                        return;
                    }
                    if (collectState == 0) {        //Not to collect
                        requestToCollectFile();
                    } else {                         //Already collected
                        CancelCollectFile();
                    }


                }
                break;
            case R.id.img_cancel:                   //Cancel the upload or download
                if (state == 0) {           //On the cross
                    /**********step1: Cancel the upload logic***************/
                    final Bundle bundle = new Bundle();
                    bundle.putString("chatid", chatid);
                    bundle.putString("msgid", msgid);
                    Utils.intentServiceAction(this, LoadDataService.ACTION_CANCEL_UPLOAD_CHAT_FILE, bundle);
                    sendBroadcast(msgid, 3, Constants.ACTION_CANCEL_UPLOAD_FILE);
                    state = 3;
                    resetView();
                    /**********step2: Send a broadcast to the chat interface, update the transfer status***************/
                } else if (state == 5) {    //下载中
                    final Bundle bundle = new Bundle();
                    bundle.putString("url", fileUrl);
                    Utils.intentServiceAction(this, LoadDataService.ACTION_CANCEL_DOWNLOAD_CHAT_FILE, bundle);
                    sendBroadcast(msgid, 8, Constants.ACTION_CANCEL_DOWNLOAD_FILE);
                    state = 8;
                    resetView();
                }
                break;

            case R.id.bottom_middle_btn:    //forwarding
                ArrayList<ChatMsg> list = new ArrayList<>();
                /*****************************************/
                ChatMsg chatMsg = new ChatMsg();
                chatMsg.setType(1009);
                chatMsg.setShareTitle(name);
                chatMsg.setNumber(number + "");
                chatMsg.setLocalUrl(localUrl);
                chatMsg.setContent(TextUtils.isEmpty(fileUrl) ? localUrl : fileUrl);
                chatMsg.setInviteType(state);
                chatMsg.setMsgName(getString(R.string.chatting_file));
                chatMsg.setCreateTime(msgTime);
                chatMsg.setThirdId(fileId);
                chatMsg.parseUserBaseVo(NextApplication.myInfo.getUserBaseVo());
                /*****************************************/
                list.add(chatMsg);
                Intent intent = new Intent(this, ContactSelectedUI.class);
                intent.putExtra("msglist", list);
                intent.putExtra("needClose", true);
                startActivity(intent);
                Utils.openNewActivityAnim(this, false);


                break;
        }
    }

    /**
     * Document details page to do the corresponding operation after the chat interface to send radio update transmission state
     *
     * @param msgid
     * @param commend
     * @param Action
     */
    private void sendBroadcast(String msgid, int commend, String Action) {
        Bundle data = new Bundle();
        data.putString("msgid", msgid);
        data.putInt("commend", commend);
        Intent intent = new Intent();
        intent.setAction(Action);
        intent.putExtras(data);
        sendBroadcast(intent);
    }

    /**
     * Open the file
     *
     * @param file
     */

    private void openFile(File file) {

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //Set the intent of the Action attribute
        intent.setAction(Intent.ACTION_VIEW);
        //Access to file the MIME type of the file
        String type = getMIMEType(file);
        //Set the intent of the data and the Type attribute。
        intent.setDataAndType(Uri.fromFile(file), type);
        //jump
        startActivity(intent);

    }

    /**
     * According to the file suffix to obtain corresponding MIME type。
     *
     * @param file
     */
    private String getMIMEType(File file) {

        String type = "*/*";
        String fName = file.getName();
        //Before the suffix for separator ". "in the fName position.
        int dotIndex = fName.lastIndexOf(".");
        if (dotIndex < 0) {
            return type;
        }
        /* Obtain the file suffix */
        String end = fName.substring(dotIndex, fName.length()).toLowerCase();
        if (end == "") return type;
        //Found in MIME and file types match table corresponding MIME type.。
        for (int i = 0; i < MIME_MapTable.length; i++) {
            if (end.equals(MIME_MapTable[i][0]))
                type = MIME_MapTable[i][1];
        }
        return type;
    }

    /**
     * Collect files
     */
    private void requestToCollectFile() {
        NetRequestImpl.getInstance().collectFile(1, fileId, sourceType, fileUrl, new RequestListener() {
            @Override
            public void start() {

            }

            @Override
            public void success(JSONObject response) {
                FinalUserDataBase.getInstance().updateChatMsgCollectState(msgid, 1);
                sendBroadcast(msgid, -2, Constants.ACTION_COLLECT_FILE);
                collectState = 1;
                bottomRightBtn.setText(getString(R.string.chat_file_already_collected));
                showToast(getString(R.string.chat_collection_failed));
                if (state == 2 || state == 7)//Upload successfully, or download successful collection, keep the path, so as to collect the list into the don't have to download
                {
                    JSONObject json = new JSONObject();
                    try {
                        json.put("state", state);
                        json.put("localurl", localUrl);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    MySharedPrefs.write(ChatFileInfoUI.this, MySharedPrefs.KEY_GROUP_FILEPATH, fileId, json.toString());
                }
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                showToast(errorMsg);
            }
        });
    }

    /**
     * Cancel the collection file
     */
    private void CancelCollectFile() {
        NetRequestImpl.getInstance().collectFile(0, fileId, sourceType, null, new RequestListener() {
            @Override
            public void start() {

            }

            @Override
            public void success(JSONObject response) {
                FinalUserDataBase.getInstance().updateChatMsgCollectState(msgid, 0);
                sendBroadcast(msgid, -2, Constants.ACTION_CANCEL_COLLECT_FILE);
                collectState = 0;
                bottomRightBtn.setText(getString(R.string.chat_file_collected));
                showToast(getString(R.string.chat_cancel_collection_failed));
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                showToast(errorMsg);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);

    }

    public class MsgBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent i) {
            if (i.getAction().equals(Constants.MSG_REPORT_SEND_MSG_PROGRESS)) {
                String msgId = i.getExtras().getString("msgid");
                if (msgId.equals(msgid)) {
                    currentLength = i.getExtras().getLong("currentLength");
                    resetOffLineView();
                }
            } else if (i.getAction().equals(Constants.MSG_REPORT_SEND_MSG_RESULT)) {
                String msgId = i.getExtras().getString("msgid");
                if (msgId.equals(msgid)) {
                    state = i.getExtras().getInt("state");
                    resetOffLineView();
                }
            } else if (i.getAction().equals(Constants.ACTION_DOWNLOAD_CHAT_FILE)) {        //Download progress
                int percent = i.getIntExtra("percent", 0);
                long size = i.getExtras().getLong("size");
                progressBar.setProgress(percent);
                txtProgress.setText(getString(R.string.txt_downloading_progress, FileSizeUtils.formatFileSize(size), FileSizeUtils.formatFileSize(number)));
            } else if (i.getAction().equals(Constants.ACTION_DOWNLOAD_CHAT_FILE_SUCCESS)) {        //Download successful
                String messageid = i.getStringExtra("msgid");

                if (!TextUtils.isEmpty(messageid) && messageid.equals(msgid)) {
                    localUrl = i.getStringExtra("path");
                    downloadingRela.setVisibility(View.GONE);
                    notDownloadLena.setVisibility(View.VISIBLE);
                    bottomLeftBtn.setText(getString(R.string.use_other_app_open));
                    state = 7;
                    resetView();
                }
            } else if (i.getAction().equals(Constants.ACTION_DOWNLOAD_CHAT_FILE_FAILED)) {        //Download failed
                String messageid = i.getStringExtra("msgid");
                if (!TextUtils.isEmpty(messageid) && messageid.equals(msgid)) {
                    int percent = i.getIntExtra("percent", 0);
                    progressBar.setProgress(percent);
                    state = 6;
                    resetView();
                }
            } else if (i.getAction().equals(XmppAction.ACTION_MESSAGE_UPDATE_LISTENER)) {          //Upload the success or failure
                Bundle bundle = i.getBundleExtra(XmppAction.ACTION_MESSAGE_UPDATE_LISTENER);
                ChatMsg chatMsg = (ChatMsg) bundle.getSerializable(XmppAction.ACTION_MESSAGE_UPDATE_LISTENER);
                if (chatMsg != null && chatMsg.getMessageId().equals(msgid)) {
                    if (chatMsg.getInviteType() == 1) {      //failure
                        state = 1;
                    } else {                                //success
                        msgid = chatMsg.getMessageId();//After successful upload message id can change, leading to take less than the message, so back to replace
                        state = 2;
                        fileId = chatMsg.getThirdId();
                        fileUrl = chatMsg.getContent();
                        msgTime = chatMsg.getCreateTime();
                        sourceType = 0;
                    }
                    resetExpiredView();
                    resetView();
                }
                FileChildVo fileChildVo = bundle.getParcelable("fileChildVo");
                if (fileChildVo != null && fileChildVo.getFid().equals(msgid)) {
                    if (fileChildVo.getType() == 1)//failed
                    {
                        state = 1;
                    } else {
                        state = 2;
                        String newid = bundle.getString("newid");
                        fileId = newid;
                        fileUrl = fileChildVo.getFileUrl();
                        resetExpiredView();
                    }
                    resetView();
                }

            } else if (i.getAction().equals(XmppAction.ACTION_MESSAGE_FILE_PERCENT)) {          //Upload progress
                String messageId = i.getExtras().getString("messageId");
                long sendSize = i.getExtras().getLong("size");
                if (messageId.equals(msgid)) {
                    int percent = (int) ((sendSize * 100) / (number * 1.0f));
                    progressBar.setProgress(percent);
                    txtProgress.setText(getString(R.string.txt_uploading_progress, FileSizeUtils.formatFileSize(sendSize), FileSizeUtils.formatFileSize(number)));
                }
            }

        }
    }
}
