package com.lingtuan.firefly.chat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.lingtuan.firefly.BuildConfig;
import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.chat.adapter.ChatAdapter;
import com.lingtuan.firefly.chat.audio.LineWaveVoiceView;
import com.lingtuan.firefly.chat.audio.RecordAudioView;
import com.lingtuan.firefly.chat.vo.FileChildVo;
import com.lingtuan.firefly.contact.SelectContactUI;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.mesh.MeshDiscover;
import com.lingtuan.firefly.mesh.MeshMessageConfig;
import com.lingtuan.firefly.mesh.MeshUserInfo;
import com.lingtuan.firefly.mesh.MeshUtils;
import com.lingtuan.firefly.mesh.MessageVo;
import com.lingtuan.firefly.offline.AppNetService;
import com.lingtuan.firefly.offline.vo.WifiPeopleVO;
import com.lingtuan.firefly.redpacket.RedPacketSendUI;
import com.lingtuan.firefly.service.LoadDataService;
import com.lingtuan.firefly.util.BitmapUtils;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.MyToast;
import com.lingtuan.firefly.util.SDCardCtrl;
import com.lingtuan.firefly.util.SDFileUtils;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.vo.ChatMsg;
import com.lingtuan.firefly.vo.UserBaseVo;
import com.lingtuan.firefly.xmpp.XmppMessageUtil;
import com.lingtuan.firefly.xmpp.XmppUtils;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.MsgType;
import org.jivesoftware.smack.packet.Message.Type;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import me.iwf.photopicker.PhotoPicker;

import static android.app.Activity.RESULT_OK;

/**
 * Chat management class
 */
public class ChattingManager implements RecordAudioView.IRecordAudioListener, LineWaveVoiceView.ILineWaveVoiceListener {
    
    private static final int ACTION_PHOTO_RESULT = 1000;
    private static final int ACTION_CAMERA_RESULT = 1001;
    public static final int ACTION_PHOTO_MORE_RESULT = 1002;
    public static final int ACTION_MANY_FILE_RESULT = 1004;
    
    private static ChattingManager instance;
    private Context mContext;
    private String userName;
    private String avatarUrl;
    private String uid;
    private ChatAdapter mAdapter;
    private ListView listView;
    private boolean isFinish;
    private Uri imageUri;
    private boolean isGroup = false;
    private boolean isSend = true;
    private AppNetService appNetService;
    
    public Set<String> sbAtGroupSelectIds = new HashSet<>();
    
    private View mAduioView;
    
    /**
     * Voice related
     */
    private long recordTotalTime;//The recording time
    private Timer timer;
    private TimerTask timerTask;
    private Handler mainHandler;
    private String audioName;
    private MediaRecorder mRecorder;
    private RecordAudioView recordAudioView;
    private LineWaveVoiceView mHorVoiceView;
    private TextView record_tips;
    private long startRecordTIme = 0;
    protected static final int DEFAULT_MIN_TIME_UPDATE_TIME = 1000;
    public static final long DEFAULT_MAX_RECORD_TIME = 60000;
    public static final long DEFAULT_MIN_RECORD_TIME = 2000;
    public static final int PERMISSIONS_REQUEST_AUDIO = 0x2;
    private long maxRecordTime = DEFAULT_MAX_RECORD_TIME;
    private long minRecordTime = DEFAULT_MIN_RECORD_TIME;
    
    private EditText mInputContent;
    
    private int groupAtSelectIndex = 0;
    
    private ChattingManager(Context mContext) {
        this.mContext = mContext;
    }
    
    public void setmInputContent(EditText mInputContent) {
        this.mInputContent = mInputContent;
    }
    
    public void setGroupAtSelectIndex(int groupAtSelectIndex) {
        this.groupAtSelectIndex = groupAtSelectIndex;
    }
    
    public void setAppNetService(AppNetService appNetService) {
        this.appNetService = appNetService;
    }
    
    public void updateAtGroupIds(String ids) {
        if (ids == null || ids.isEmpty()) {
            sbAtGroupSelectIds.clear();
        } else {
            if (sbAtGroupSelectIds.size() != 0) {
                Set<String> tempIds = new HashSet<>();
                for (String uid : sbAtGroupSelectIds) {
                    if (!ids.toString().contains(uid)) {
                        tempIds.add(ids);
                    }
                }
                for (String uid : tempIds) {
                    sbAtGroupSelectIds.remove(uid);
                }
            } else {
                return;
            }
        }
    }
    
    public String getSbAtGroupSelectIds() {
        StringBuilder sb = new StringBuilder();
        if (!sbAtGroupSelectIds.isEmpty()) {
            for (String id : sbAtGroupSelectIds) {
                sb.append(id).append(",");
            }
            sb.delete(sb.lastIndexOf(","), sb.length());
        }
        return sb.toString();
    }
    
    public boolean isAtAll(String content) {
        if (content.contains(mContext.getString(R.string.chat_at_all_two))) {
            return true;
        } else {
            return false;
        }
        
    }
    
    
    public boolean isFinish() {
        return isFinish;
    }
    
    public void setFinish(boolean isFinish) {
        this.isFinish = isFinish;
    }
    
    
    public void setSend(boolean isSend) {
        this.isSend = isSend;
    }
    
    public static ChattingManager getInstance(Context mContext) {
        if (instance == null) {
            instance = new ChattingManager(mContext);
        }
        return instance;
    }
    
    public void setGroup(boolean isGroup) {
        this.isGroup = isGroup;
    }
    
    public void setUserInfo(String userName, String avatarUrl, String uid, ChatAdapter mAdapter, ListView listView) {
        this.userName = userName;
        this.avatarUrl = avatarUrl;
        this.uid = uid;
        this.mAdapter = mAdapter;
        this.listView = listView;
    }
    
    public void setVoiceInfo(int second) {
        boolean successed = true;
        if (uid.equals(Constants.APP_EVERYONE)) {
            ChatMsg msg = createAudioChatMsg(uid, SDCardCtrl.getAudioPath() + File.separator + audioName, userName, avatarUrl, second + "", isSend);
            if (appNetService != null) {
                successed = appNetService.handleSendVoice(second, SDCardCtrl.getAudioPath() + File.separator + audioName, true, uid, msg.getMessageId());
            }
            if (msg.getSend() == 0 && !successed) {
                msg.setSend(0);
            }
            mAdapter.addChatMsg(msg, true);
            
        } else if (isGroup) {
            ChatMsg msg = createAudioChatMsg(uid, SDCardCtrl.getAudioPath() + File.separator + audioName, userName, avatarUrl, second + "", isSend);
            mAdapter.addChatMsg(msg, true);
        } else {
            boolean foundPeople = false;
            if (appNetService != null && appNetService.getwifiPeopleList() != null) {
                for (WifiPeopleVO vo : appNetService.getwifiPeopleList())// All users need to traverse, find out the corresponding touid users
                {
                    if (uid.equals(vo.getLocalId())) {
                        foundPeople = true;
                        break;
                    }
                }
            }
            if (foundPeople)//With no net with no net send messages
            {
                ChatMsg msg = new ChatMsg();
                msg.setType(2);
                msg.setContent(SDCardCtrl.getAudioPath() + File.separator + audioName);
                msg.setLocalUrl(SDCardCtrl.getAudioPath() + File.separator + audioName);
                msg.setSecond(second + "");
                msg.parseUserBaseVo(NextApplication.myInfo.getUserBaseVo());
                msg.setChatId(uid);
                msg.setOffLineMsg(true);
                msg.setSend(1);
                msg.setMessageId(UUID.randomUUID().toString());
                msg.setMsgTime(System.currentTimeMillis() / 1000);
                msg.setShowTime(FinalUserDataBase.getInstance().isOffLineShowTime(uid, msg.getMsgTime()));
                if (appNetService != null) {
                    successed = appNetService.handleSendVoice(second, SDCardCtrl.getAudioPath() + File.separator + audioName, false, uid, msg.getMessageId());
                }
                if (!successed) {
                    msg.setSend(0);
                }
                FinalUserDataBase.getInstance().saveChatMsg(msg, uid, userName, avatarUrl);
                msg.parseUserBaseVo(NextApplication.myInfo.getUserBaseVo());
                mAdapter.addChatMsg(msg, true);
            } else {
                ChatMsg msg = createAudioChatMsg(uid, SDCardCtrl.getAudioPath() + File.separator + audioName, userName, avatarUrl, second + "", isSend);
                mAdapter.addChatMsg(msg, true);
            }
        }
        listView.setSelection(mAdapter.getCount());
    }
    
    public void setCardInfo(UserBaseVo cardVo) {
        boolean successed = true;
        if (uid.equals(Constants.APP_EVERYONE)) {
            ChatMsg msg = XmppMessageUtil.getInstance().sendCard(uid, userName, avatarUrl, cardVo.getShowName(), cardVo.getSightml(), cardVo.getThumb(), cardVo.getLocalId(), isGroup, isSend);
            if (appNetService != null) {
                WifiPeopleVO cardInfo = new WifiPeopleVO();
                cardInfo.setLocalId(cardVo.getLocalId());
                cardInfo.setUsername(cardVo.getUsername());
                cardInfo.setSightml(cardVo.getSightml());
                String path;
                if (cardVo.getThumb().startsWith("http") || cardVo.getThumb().startsWith("www")) {
                    path = NextApplication.mImageLoader.getDiscCache().get(cardVo.getThumb()).getPath();
                } else {
                    path = cardVo.getThumb();
                }
                cardInfo.setThumb(path);
                successed = appNetService.handleSendCard(cardInfo, true, uid, msg.getMessageId());
            }
            if (msg.getSend() == 0 && !successed) {
                msg.setSend(0);
            }
            mAdapter.addChatMsg(msg, true);
            
        } else if (isGroup) {
            ChatMsg msg = XmppMessageUtil.getInstance().sendCard(uid, userName, avatarUrl, cardVo.getShowName(), cardVo.getSightml(), cardVo.getThumb(), cardVo.getLocalId(), isGroup, isSend);
            mAdapter.addChatMsg(msg, true);
        } else {
            boolean foundPeople = false;
            if (appNetService != null && appNetService.getwifiPeopleList() != null) {
                for (WifiPeopleVO vo : appNetService.getwifiPeopleList())// All users need to traverse, find out the corresponding touid users
                {
                    if (uid.equals(vo.getLocalId())) {
                        foundPeople = true;
                        break;
                    }
                }
            }
            if (foundPeople)//With no net with no net send messages
            {
                
                WifiPeopleVO cardInfo = new WifiPeopleVO();
                cardInfo.setLocalId(cardVo.getLocalId());
                cardInfo.setUsername(cardVo.getUsername());
                cardInfo.setSightml(cardVo.getSightml());
                String path;
                if (cardVo.getThumb().startsWith("http") || cardVo.getThumb().startsWith("www")) {
                    path = NextApplication.mImageLoader.getDiscCache().get(cardVo.getThumb()).getPath();
                } else {
                    path = cardVo.getThumb();
                }
                cardInfo.setThumb(path);
                
                ChatMsg msg = new ChatMsg();
                msg.parseUserBaseVo(NextApplication.myInfo.getUserBaseVo());
                msg.setChatId(uid);
                msg.setType(6);
                msg.setThirdId(cardInfo.getLocalId());
                msg.setThirdImage(cardInfo.getThumb());
                msg.setThirdName(cardInfo.getUserName());
                msg.setCardSign(cardInfo.getSightml());
                msg.setOffLineMsg(true);
                msg.setSend(1);
                msg.setMessageId(UUID.randomUUID().toString());
                msg.setMsgTime(System.currentTimeMillis() / 1000);
                msg.setShowTime(FinalUserDataBase.getInstance().isOffLineShowTime(uid, msg.getMsgTime()));
                successed = appNetService.handleSendCard(cardInfo, false, uid, msg.getMessageId());
                if (!successed) {
                    msg.setSend(0);
                }
                FinalUserDataBase.getInstance().saveChatMsg(msg, uid, userName, avatarUrl);
                msg.parseUserBaseVo(NextApplication.myInfo.getUserBaseVo());
                mAdapter.addChatMsg(msg, true);
            } else {
                ChatMsg msg = XmppMessageUtil.getInstance().sendCard(uid, userName, avatarUrl, cardVo.getShowName(), cardVo.getSightml(), cardVo.getThumb(), cardVo.getLocalId(), isGroup, isSend);
                mAdapter.addChatMsg(msg, true);
            }
        }
        listView.setSelection(mAdapter.getCount());
    }
    
    /**
     * Send the face
     */
    public void showFaceView(View faceView, EditText mInputContent, View stubBottomBg) {
        FaceUtils.getInstance(mContext).showFaceView(faceView, mInputContent, stubBottomBg);
    }

    /**
     * Send the red packet
     */
    public void showRedPacketView(String uid,boolean isGroup) {
        Intent intent = new Intent(mContext, RedPacketSendUI.class);
        intent.putExtra("uid",uid);
        intent.putExtra("isGroup",isGroup);
        mContext.startActivity(intent);
        Utils.openNewActivityAnim((Activity) mContext, false);
    }
    
    
    /**
     * Choose photos
     */
    public void showPhotoView() {
        PhotoPicker.builder().setPhotoCount(9).setShowCamera(false).setShowGif(false).setPreviewEnabled(true).setGridColumnCount(4).start((Activity) mContext, PhotoPicker.REQUEST_CODE);
    }
    
    
    /**
     * Taking pictures
     */
    public void showCameraView() {
        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); //调用系统相机
        File tempFile = new File(SDCardCtrl.getChatImagePath() + "/", System.currentTimeMillis() + ".jpg");
        imageUri = Uri.fromFile(tempFile);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri photoUri = FileProvider.getUriForFile(mContext,"com.lingtuan.firefly.fileProvider",tempFile);
            camera.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        }else{
            camera.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        }
        ((Activity) mContext).startActivityForResult(camera, ACTION_CAMERA_RESULT);
    }
    
    
    /**
     * Choose a business card
     */
    public void showCardView() {
        Intent intent = new Intent(mContext, SelectContactUI.class);
        ((Activity) mContext).startActivityForResult(intent, 0);
        Utils.openNewActivityAnim((Activity) mContext, false);
    }
    
    /**
     * Send voice
     */
    public void showAudioView(View audioView, View stubBottomBg) {
        mainHandler = new Handler();
        if (mAduioView == null) {
            mAduioView = audioView;
            mAduioView.setVisibility(View.VISIBLE);
            stubBottomBg.setVisibility(View.VISIBLE);
        } else {
            boolean visibleState = audioView.getVisibility() == View.VISIBLE;
            audioView.setVisibility(visibleState ? View.GONE : View.VISIBLE);
            stubBottomBg.setVisibility(visibleState ? View.GONE : View.VISIBLE);
        }
        recordAudioView = (RecordAudioView) mAduioView.findViewById(R.id.iv_recording);
        mHorVoiceView = (LineWaveVoiceView) mAduioView.findViewById(R.id.horvoiceview);
        record_tips = (TextView) mAduioView.findViewById(R.id.record_tips);
        record_tips.setText(mContext.getString(R.string.chatting_audio_normal));
        if (mHorVoiceView.getVisibility() == View.VISIBLE) {
            mHorVoiceView.setVisibility(View.INVISIBLE);
        }
        
        recordAudioView.setRecordAudioListener(this);
        mHorVoiceView.setLineWaveVoiceListener(this);
    }
    
    /**
     * Select the file
     */
    public void showFileView() {
        boolean foundPeople = false;
        if (appNetService != null && appNetService.getwifiPeopleList() != null) {
            for (WifiPeopleVO wifiPeopleVO : appNetService.getwifiPeopleList())// All users need to traverse, find out the corresponding touid users
            {
                if (uid.equals(wifiPeopleVO.getLocalId())) {
                    foundPeople = true;
                    break;
                }
            }
        }
        if (!foundPeople) {
            MyToast.showToast(mContext, mContext.getString(R.string.offline_file_send_tip));
            return;
        }
        MySharedPrefs.writeBoolean(mContext, MySharedPrefs.FILE_USER, Constants.CHAT_SEND_FILE, true);
        Intent i = new Intent(mContext, SendFileUI.class);
        ((Activity) mContext).startActivityForResult(i, ACTION_MANY_FILE_RESULT);
        Utils.openNewActivityAnim((Activity) mContext, false);
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == RESULT_OK) {
            ArrayList<UserBaseVo> selectList = (ArrayList<UserBaseVo>) data.getSerializableExtra("selectList");
            if (selectList != null && !selectList.isEmpty()) {
                setCardInfo(selectList.get(0));
            }
        } else if (requestCode == ACTION_PHOTO_RESULT && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumns = {MediaStore.Images.Media.DATA};
            Cursor c = mContext.getContentResolver().query(selectedImage, filePathColumns, null, null, null);
            if (c.moveToNext()) {
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePathColumns[0]);
                String picturePath = c.getString(columnIndex);
                sendImgMethod(picturePath);
            }
            c.close();
            //To obtain and display picture
        } else if (requestCode == ACTION_CAMERA_RESULT && resultCode == RESULT_OK) {
            sendImgMethod(imageUri.getPath());
        } else if (requestCode == ACTION_MANY_FILE_RESULT && resultCode == RESULT_OK) {      //Select the file to send
            if (data != null) {
                Serializable listSer = data.getSerializableExtra("list");
                if (listSer != null) {
                    final ArrayList<FileChildVo> list = (ArrayList<FileChildVo>) listSer;
                    if (list != null && list.size() > 0) {
                        new SendFileThread(list);
                    }
                }
            }
        } else if (resultCode == RESULT_OK && requestCode == PhotoPicker.REQUEST_CODE) {
            if (data != null) {
                ArrayList<String> photos = data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
                if (photos != null && photos.size() > 0) {
                    new SendImageThread(photos);
                }
            }
        } else if (requestCode == Constants.REQUEST_SELECT_GROUP_MEMBER && resultCode == Activity.RESULT_OK && null != data) {
//            //@group member
//            boolean isMultipleChoice = data.getBooleanExtra("isMultipleChoice", false);
//            if (isMultipleChoice) {
//                ArrayList<GroupMemberVo> source = (ArrayList<GroupMemberVo>) data.getSerializableExtra("data");
//                if (source != null) {
//                    for (int i = 0; i < source.size(); i++) {
//
//                        UserBaseVo vo = source.get(i);
//                        if (vo.getUid().equals(NextApplication.myInfo.getUid())) {
//                            continue;
//                        }
//                        sbAtGroupSelectIds.add(vo.getUid());
//                    }
//                    Editable editable = mInputContent.getText();
//                    editable.insert(groupAtSelectIndex, mContext.getString(R.string.chat_at_all_three));
//                }
//
//            } else {
            UserBaseVo vo = (UserBaseVo) data.getSerializableExtra("data");
            Editable editable = mInputContent.getText();
            editable.insert(groupAtSelectIndex, vo.getUserName() + " ");
            sbAtGroupSelectIds.add(vo.getLocalId());
//            }
            Utils.showKeyBoard(mInputContent);
        }
        
    }
    
    
    /**
     * Sending pictures method
     *
     * @param picturePath Image path
     */
    private void sendImgMethod(String picturePath) {
        float density = mContext.getResources().getDisplayMetrics().density;
        int screenWidth = Constants.MAX_IMAGE_WIDTH;//mContext.getResources().getDisplayMetrics().widthPixels;
        int screenHeight = Constants.MAX_IMAGE_HEIGHT;//mContext.getResources().getDisplayMetrics().heightPixels;
        int width = (int) (120 * density);
        Bitmap bmp = BitmapUtils.getimage(picturePath, width, width, 10);
        Bitmap bmpUpload = BitmapUtils.getimage(picturePath, screenWidth, screenHeight, Constants.MAX_KB);
        BitmapUtils.saveBitmap2SD(bmp, 10, false);
        String uploadPath = BitmapUtils.saveBitmap2SD(bmpUpload, screenWidth, true).getPath();
        String url = uploadPath;
        boolean successed = true;
        if (uid.equals(Constants.APP_EVERYONE)) {
            ChatMsg msg = createImageChatMsg(uid, url, userName, avatarUrl, BitmapUtils.BitmapToBase64String(bmp), isGroup, isSend);
            if (appNetService != null) {
                successed = appNetService.handleSendPicutre(url, true, uid, msg.getMessageId());
            }
            if (msg.getSend() == 0 && !successed) {
                msg.setSend(0);
            }
            mAdapter.addChatMsg(msg, true);
            
        } else if (uid.equals(Constants.APP_MESH)) {// send mesh picture
            if (MeshUtils.getInatance().isConnectWifiSsid()) {
                ChatMsg msg = createMeshImageChatMsg(uid, 1, url, userName, avatarUrl, picturePath, isGroup, isSend);
                if (!TextUtils.isEmpty(msg.getCover())) {
                    mAdapter.addChatMsg(msg, true);
                }
            } else {
                MyToast.showToast(mContext, mContext.getString(R.string.mesh_chat_no_send_hint));
            }
        } else if (isGroup) {
            ChatMsg msg = createImageChatMsg(uid, url, userName, avatarUrl, BitmapUtils.BitmapToBase64String(bmp), isGroup, isSend);
            mAdapter.addChatMsg(msg, true);
        } else {
            if (MeshUtils.getInatance().isConnectWifiSsid()) {//mesh++
                ConcurrentHashMap<String, MeshUserInfo> mServiceMap = MeshDiscover.getInatance().mServiceMap;
                boolean has = false;
                for (final String key : mServiceMap.keySet()) {
                    if (key.equals(uid)) {
                        has = true;
                    }
                }
                if (has) {
                    ChatMsg msg = createMeshImageChatMsg(uid, 1, url, userName, avatarUrl, picturePath, isGroup, isSend);
                    if (!TextUtils.isEmpty(msg.getCover())) {
                        mAdapter.addChatMsg(msg, true);
                    }
                } else {
                    boolean foundPeople = false;
                    if (appNetService != null && appNetService.getwifiPeopleList() != null) {
                        for (WifiPeopleVO vo : appNetService.getwifiPeopleList())// All users need to traverse, find out the corresponding touid users
                        {
                            if (uid.equals(vo.getLocalId())) {
                                foundPeople = true;
                                break;
                            }
                        }
                    }
                    if (foundPeople)//With no net with no net send messages
                    {
                        ChatMsg msg = new ChatMsg();
                        msg.setType(1);
                        msg.setContent(url);
                        msg.setLocalUrl(url);
                        msg.setCover(BitmapUtils.BitmapToBase64String(bmp));
                        msg.parseUserBaseVo(NextApplication.myInfo.getUserBaseVo());
                        msg.setChatId(uid);
                        msg.setOffLineMsg(true);
                        msg.setSend(1);
                        msg.setMessageId(UUID.randomUUID().toString());
                        msg.setMsgTime(System.currentTimeMillis() / 1000);
                        msg.setShowTime(FinalUserDataBase.getInstance().isOffLineShowTime(uid, msg.getMsgTime()));
                        successed = appNetService.handleSendPicutre(url, false, uid, msg.getMessageId());
                        if (!successed) {
                            msg.setSend(0);
                        }
                        FinalUserDataBase.getInstance().saveChatMsg(msg, uid, userName, avatarUrl);
                        msg.parseUserBaseVo(NextApplication.myInfo.getUserBaseVo());
                        mAdapter.addChatMsg(msg, true);
                    } else {
                        ChatMsg msg = createImageChatMsg(uid, url, userName, avatarUrl, BitmapUtils.BitmapToBase64String(bmp), isGroup, isSend);
                        mAdapter.addChatMsg(msg, true);
                    }
                }
            } else {
                boolean foundPeople = false;
                if (appNetService != null && appNetService.getwifiPeopleList() != null) {
                    for (WifiPeopleVO vo : appNetService.getwifiPeopleList())// All users need to traverse, find out the corresponding touid users
                    {
                        if (uid.equals(vo.getLocalId())) {
                            foundPeople = true;
                            break;
                        }
                    }
                }
                if (foundPeople)//With no net with no net send messages
                {
                    ChatMsg msg = new ChatMsg();
                    msg.setType(1);
                    msg.setContent(url);
                    msg.setLocalUrl(url);
                    msg.setCover(BitmapUtils.BitmapToBase64String(bmp));
                    msg.parseUserBaseVo(NextApplication.myInfo.getUserBaseVo());
                    msg.setChatId(uid);
                    msg.setOffLineMsg(true);
                    msg.setSend(1);
                    msg.setMessageId(UUID.randomUUID().toString());
                    msg.setMsgTime(System.currentTimeMillis() / 1000);
                    msg.setShowTime(FinalUserDataBase.getInstance().isOffLineShowTime(uid, msg.getMsgTime()));
                    successed = appNetService.handleSendPicutre(url, false, uid, msg.getMessageId());
                    if (!successed) {
                        msg.setSend(0);
                    }
                    FinalUserDataBase.getInstance().saveChatMsg(msg, uid, userName, avatarUrl);
                    msg.parseUserBaseVo(NextApplication.myInfo.getUserBaseVo());
                    mAdapter.addChatMsg(msg, true);
                } else {
                    ChatMsg msg = createImageChatMsg(uid, url, userName, avatarUrl, BitmapUtils.BitmapToBase64String(bmp), isGroup, isSend);
                    mAdapter.addChatMsg(msg, true);
                }
            }
        }
        listView.setSelection(mAdapter.getCount());
        if (bmp != null && !bmp.isRecycled()) {
            bmp.recycle();
        }
        if (bmpUpload != null && !bmpUpload.isRecycled()) {
            bmpUpload.recycle();
        }
    }
    
    @Override
    public float getMaxAmplitude() {
        if (startRecordTIme > 0) {
            return mRecorder.getMaxAmplitude() * 1.0f / 32768;
        }
        return 0;
    }
    
    
    /**
     * Sending pictures thread
     */
    class SendImageThread extends Thread {
        private ArrayList<String> pathList;
        
        public SendImageThread(ArrayList<String> list) {
            this.pathList = list;
            start();
        }
        
        private Handler mHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                mAdapter.addChatMsg((ChatMsg) msg.obj, true);
                listView.setSelection(mAdapter.getCount());
            }
        };
        
        
        @Override
        public void run() {
            int count = pathList.size();
            for (int i = 0; i < count; i++) {
                try {
                    SystemClock.sleep(250);
                    sendImage(pathList.get(i));
                } catch (Exception e) {
                    e.printStackTrace();
                } catch (Error e) {
                    try {
                        sendImage(pathList.get(i));
                    } catch (Error e2) {
                        e2.printStackTrace();
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                    e.printStackTrace();
                }
            }
        }
        
        /**
         * Sending pictures method
         *
         * @param imageUrl Pictures of local path
         */
        private void sendImage(String imageUrl) {
            float density = mContext.getResources().getDisplayMetrics().density;
            int screenWidth = Constants.MAX_IMAGE_WIDTH;//mContext.getResources().getDisplayMetrics().widthPixels;
            int screenHeight = Constants.MAX_IMAGE_HEIGHT;//mContext.getResources().getDisplayMetrics().heightPixels;
            int width = (int) (120 * density);
            Bitmap bmp = BitmapUtils.getimage(imageUrl, width, width, 10);
            Bitmap bmpUpload = BitmapUtils.getimage(imageUrl, screenWidth, screenHeight, Constants.MAX_KB);
            String uploadPath = BitmapUtils.saveBitmap2SD(bmpUpload, screenWidth, true).getPath();
            String url = uploadPath;
            boolean successed = true;
            if (uid.equals(Constants.APP_EVERYONE)) {
                ChatMsg msg = createImageChatMsg(uid, url, userName, avatarUrl, BitmapUtils.BitmapToBase64String(bmp), isGroup, isSend);
                if (appNetService != null) {
                    successed = appNetService.handleSendPicutre(url, true, uid, msg.getMessageId());
                }
                if (msg.getSend() == 0 && !successed) {
                    msg.setSend(0);
                }
                android.os.Message m = android.os.Message.obtain();
                m.obj = msg;
                mHandler.sendMessage(m);
                
            } else if (uid.equals(Constants.APP_MESH)) {
                if (MeshUtils.getInatance().isConnectWifiSsid()) {
                    ChatMsg msg = createMeshImageChatMsg(uid, 1, url, userName, avatarUrl, imageUrl, isGroup, isSend);
                    if (!TextUtils.isEmpty(msg.getCover())) {
                        android.os.Message m = android.os.Message.obtain();
                        m.obj = msg;
                        mHandler.sendMessage(m);
                    }
                } else {
                    MyToast.showToast(mContext, mContext.getString(R.string.mesh_chat_no_send_hint));
                }
            } else if (isGroup) {
                ChatMsg msg = createImageChatMsg(uid, url, userName, avatarUrl, BitmapUtils.BitmapToBase64String(bmp), isGroup, isSend);
                android.os.Message m = android.os.Message.obtain();
                m.obj = msg;
                mHandler.sendMessage(m);
            } else {
                if (MeshUtils.getInatance().isConnectWifiSsid()) {//mesh++
                    ConcurrentHashMap<String, MeshUserInfo> mServiceMap = MeshDiscover.getInatance().mServiceMap;
                    boolean has = false;
                    for (final String key : mServiceMap.keySet()) {
                        if (key.equals(uid)) {
                            has = true;
                        }
                    }
                    if (has) {
                        ChatMsg msg = createMeshImageChatMsg(uid, 1, url, userName, avatarUrl, imageUrl, isGroup, isSend);
                        if (!TextUtils.isEmpty(msg.getCover())) {
                            android.os.Message m = android.os.Message.obtain();
                            m.obj = msg;
                            mHandler.sendMessage(m);
                        }
                    } else {
                        boolean foundPeople = false;
                        if (appNetService != null && appNetService.getwifiPeopleList() != null) {
                            for (WifiPeopleVO vo : appNetService.getwifiPeopleList()) {
                                if (uid.equals(vo.getLocalId())) {
                                    foundPeople = true;
                                    break;
                                }
                            }
                        }
                        if (foundPeople) {
                            ChatMsg msg = new ChatMsg();
                            msg.setType(1);
                            msg.setContent(url);
                            msg.setLocalUrl(url);
                            msg.setCover(BitmapUtils.BitmapToBase64String(bmp));
                            msg.parseUserBaseVo(NextApplication.myInfo.getUserBaseVo());
                            msg.setChatId(uid);
                            msg.setOffLineMsg(true);
                            msg.setSend(1);
                            msg.setMessageId(UUID.randomUUID().toString());
                            msg.setMsgTime(System.currentTimeMillis() / 1000);
                            msg.setShowTime(FinalUserDataBase.getInstance().isOffLineShowTime(uid, msg.getMsgTime()));
                            successed = appNetService.handleSendPicutre(url, false, uid, msg.getMessageId());
                            if (!successed) {
                                msg.setSend(0);
                            }
                            FinalUserDataBase.getInstance().saveChatMsg(msg, uid, userName, avatarUrl);
                            msg.parseUserBaseVo(NextApplication.myInfo.getUserBaseVo());
                            android.os.Message m = android.os.Message.obtain();
                            m.obj = msg;
                            mHandler.sendMessage(m);
                        } else {
                            ChatMsg msg = createImageChatMsg(uid, url, userName, avatarUrl, BitmapUtils.BitmapToBase64String(bmp), isGroup, isSend);
                            android.os.Message m = android.os.Message.obtain();
                            m.obj = msg;
                            mHandler.sendMessage(m);
                        }
                    }
                } else {
                    boolean foundPeople = false;
                    if (appNetService != null && appNetService.getwifiPeopleList() != null) {
                        for (WifiPeopleVO vo : appNetService.getwifiPeopleList()) {
                            if (uid.equals(vo.getLocalId())) {
                                foundPeople = true;
                                break;
                            }
                        }
                    }
                    if (foundPeople) {
                        ChatMsg msg = new ChatMsg();
                        msg.setType(1);
                        msg.setContent(url);
                        msg.setLocalUrl(url);
                        msg.setCover(BitmapUtils.BitmapToBase64String(bmp));
                        msg.parseUserBaseVo(NextApplication.myInfo.getUserBaseVo());
                        msg.setChatId(uid);
                        msg.setOffLineMsg(true);
                        msg.setSend(1);
                        msg.setMessageId(UUID.randomUUID().toString());
                        msg.setMsgTime(System.currentTimeMillis() / 1000);
                        msg.setShowTime(FinalUserDataBase.getInstance().isOffLineShowTime(uid, msg.getMsgTime()));
                        successed = appNetService.handleSendPicutre(url, false, uid, msg.getMessageId());
                        if (!successed) {
                            msg.setSend(0);
                        }
                        FinalUserDataBase.getInstance().saveChatMsg(msg, uid, userName, avatarUrl);
                        msg.parseUserBaseVo(NextApplication.myInfo.getUserBaseVo());
                        android.os.Message m = android.os.Message.obtain();
                        m.obj = msg;
                        mHandler.sendMessage(m);
                    } else {
                        ChatMsg msg = createImageChatMsg(uid, url, userName, avatarUrl, BitmapUtils.BitmapToBase64String(bmp), isGroup, isSend);
                        android.os.Message m = android.os.Message.obtain();
                        m.obj = msg;
                        mHandler.sendMessage(m);
                    }
                }
            }
            if (bmp != null && !bmp.isRecycled()) {
                bmp.recycle();
            }
            if (bmpUpload != null && !bmpUpload.isRecycled()) {
                bmpUpload.recycle();
            }
        }
    }
    
    /**
     * The child thread of ordinary sending files
     */
    class SendFileThread extends Thread {
        
        private ArrayList<FileChildVo> fileList;
        
        public SendFileThread(ArrayList<FileChildVo> list) {
            this.fileList = list;
            start();
        }
        
        private Handler mHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == 1) {
                    MyToast.showToast(mContext, mContext.getString(R.string.offline_file_send_tip));
                } else {
                    mAdapter.addChatMsg((ChatMsg) msg.obj, true);
                    listView.setSelection(mAdapter.getCount());
                }
                
            }
        };
        
        @Override
        public void run() {
            int count = fileList.size();
            for (int i = 0; i < count; i++) {
                try {
                    SystemClock.sleep(250);
                    sendFile(fileList.get(i));
                } catch (Exception e) {
                    e.printStackTrace();
                } catch (Error e) {
                    try {
                        sendFile(fileList.get(i));
                    } catch (Error e2) {
                        e2.printStackTrace();
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                    e.printStackTrace();
                }
            }
        }
        
        private void sendFile(FileChildVo vo) {
            boolean successed = true;
            if (!uid.equals(Constants.APP_EVERYONE) && !isGroup) {
                boolean foundPeople = false;
                if (appNetService != null && appNetService.getwifiPeopleList() != null) {
                    for (WifiPeopleVO wifiPeopleVO : appNetService.getwifiPeopleList())// All users need to traverse, find out the corresponding touid users
                    {
                        if (uid.equals(wifiPeopleVO.getLocalId())) {
                            foundPeople = true;
                            break;
                        }
                    }
                }
                if (foundPeople) {
                    ChatMsg msg = new ChatMsg();
                    msg.setType(1009);
                    msg.setContent(vo.getFilePath());
                    msg.setShareTitle(vo.getName());
                    msg.setNumber(vo.getReallySize() + "");
                    msg.parseUserBaseVo(NextApplication.myInfo.getUserBaseVo());
                    msg.setChatId(uid);
                    msg.setOffLineMsg(true);
                    msg.setSend(1);
                    msg.setInviteType(-1);//Waiting to receive
                    msg.setMessageId(UUID.randomUUID().toString());
                    msg.setMsgTime(System.currentTimeMillis() / 1000);
                    msg.setShowTime(FinalUserDataBase.getInstance().isOffLineShowTime(uid, msg.getMsgTime()));
                    successed = appNetService.handleSendFile(vo.getFilePath(), uid, msg.getMessageId());
                    if (!successed) {
                        msg.setSend(0);
                    }
                    FinalUserDataBase.getInstance().saveChatMsg(msg, uid, userName, avatarUrl);
                    msg.parseUserBaseVo(NextApplication.myInfo.getUserBaseVo());
                    android.os.Message m = android.os.Message.obtain();
                    m.obj = msg;
                    mHandler.sendMessage(m);
                } else {
                    mHandler.sendEmptyMessage(1);
                }
            }
        }
    }
    
    public void destory() {
        instance = null;
    }
    
    /**
     * Create photo chat entity class
     */
    private ChatMsg createImageChatMsg(String uid, String content, String uName, String avatarUrl, String cover, boolean isGroup, boolean isSend) {
        
        ChatMsg chatMsg = new ChatMsg();
        chatMsg.setType(1);
        chatMsg.setContent(content);//
        chatMsg.setLocalUrl(content);
        chatMsg.setCover(cover);
        chatMsg.parseUserBaseVo(NextApplication.myInfo.getUserBaseVo());
        String jid = uid + "@" + XmppUtils.SERVER_NAME;
        if (isGroup) {
            jid = uid.replace("group-", "") + "@group." + XmppUtils.SERVER_NAME;
        }
        
        
        Message msg = new Message(jid, Type.chat);
        chatMsg.setMessageId(msg.getPacketID());
        if (isGroup) {//
            msg.setMsgtype(MsgType.groupchat);
            chatMsg.setChatId(uid);
            chatMsg.setGroup(isGroup);
            chatMsg.setSource(NextApplication.mContext.getString(R.string.app_name));
            chatMsg.setUserfrom(mContext.getString(R.string.app_name));
            chatMsg.setUsersource(XmppUtils.SERVER_NAME);
            msg.setBody(chatMsg.toGroupChatJsonObject());
        } else {
            chatMsg.setSource(mContext.getString(R.string.app_name));
            msg.setBody(chatMsg.toChatJsonObject());
            
        }
        chatMsg.setMsgTime(System.currentTimeMillis() / 1000);
        if (isSend) {
            chatMsg.setSend(2);
        }
        FinalUserDataBase.getInstance().saveChatMsg(chatMsg, uid, uName, avatarUrl);
        chatMsg.parseUserBaseVo(NextApplication.myInfo.getUserBaseVo());
        if (isSend) {
            Bundle bundle = new Bundle();
            bundle.putInt("type", 2);
            bundle.putString("uid", uid);
            bundle.putString("username", uName);
            bundle.putString("avatarurl", avatarUrl);
            bundle.putSerializable("chatmsg", chatMsg);
            Utils.intentService(mContext, LoadDataService.class, LoadDataService.ACTION_FILE_UPLOAD_CHAT, LoadDataService.ACTION_FILE_UPLOAD_CHAT, bundle);
        }
        return chatMsg;
    }
    
    
    /**
     * Resend video
     */
    public void reSendVideo(ChatMsg chatMsg) {
        Bundle bundle = new Bundle();
        bundle.putString("uid", chatMsg.getChatId());
        bundle.putString("username", chatMsg.getUsername());
        bundle.putString("avatarurl", chatMsg.getUserImage());
        bundle.putSerializable("chatmsg", chatMsg);
        Utils.intentService(mContext, LoadDataService.class, LoadDataService.ACTION_VIDEO_UPLOAD_CHAT, LoadDataService.ACTION_VIDEO_UPLOAD_CHAT, bundle);
    }
    
    /**
     * Chat resend file
     */
    public void reSendFile(ChatMsg chatMsg) {
        mAdapter.updateSendStatus(2, 0, chatMsg.getMessageId(), null, -1);
        FinalUserDataBase.getInstance().updateChatMsgState(2, chatMsg.getMessageId(), 0, null);
        if (chatMsg != null) {
            Bundle bundle = new Bundle();
            bundle.putString("uid", uid);
            bundle.putString("username", userName);
            bundle.putString("avatarurl", avatarUrl);
            bundle.putSerializable("chatmsg", chatMsg);
            Utils.intentService(mContext, LoadDataService.class, LoadDataService.ACTION_UPLOAD_CHAT_FILE_DIR, LoadDataService.ACTION_UPLOAD_CHAT_FILE_DIR, bundle);
        }
    }
    
    /**
     * Resend image or voice
     */
    public void reSendAudioOrPic(ChatMsg chatMsg) {
        Bundle bundle = new Bundle();
        bundle.putInt("type", 2);
        bundle.putString("uid", chatMsg.getChatId());
        bundle.putString("username", chatMsg.getUsername());
        bundle.putString("avatarurl", chatMsg.getUserImage());
        bundle.putString("remoteSource", chatMsg.getRemoteSource());
        bundle.putSerializable("chatmsg", chatMsg);
        bundle.putBoolean("isSuperGroup", chatMsg.getChatId().startsWith("superGroup-"));
        Utils.intentService(mContext, LoadDataService.class, LoadDataService.ACTION_FILE_UPLOAD_CHAT, LoadDataService.ACTION_FILE_UPLOAD_CHAT, bundle);
    }
    
    /**
     * Create a voice chat entity class
     */
    private ChatMsg createAudioChatMsg(String uid, String content, String uName, String avatarUrl, String second, boolean isSend) {
        
        ChatMsg chatMsg = new ChatMsg();
        chatMsg.setType(2);
        chatMsg.setContent(content);
        chatMsg.setLocalUrl(content);
        chatMsg.setSecond(second);
        
        chatMsg.parseUserBaseVo(NextApplication.myInfo.getUserBaseVo());
        String jid = uid + "@" + XmppUtils.SERVER_NAME;
        if (isGroup) {
            jid = uid.replace("group-", "") + "@group." + XmppUtils.SERVER_NAME;
        }
        
        Message msg = new Message(jid, Type.chat);
        chatMsg.setMessageId(msg.getPacketID());
        if (isGroup) {//
            msg.setMsgtype(MsgType.groupchat);
            chatMsg.setGroup(isGroup);
            chatMsg.setChatId(uid);
            chatMsg.setSource(NextApplication.mContext.getString(R.string.app_name));
            chatMsg.setUserfrom(mContext.getString(R.string.app_name));
            chatMsg.setUsersource(XmppUtils.SERVER_NAME);
            msg.setBody(chatMsg.toGroupChatJsonObject());
        } else {
            chatMsg.setSource(mContext.getString(R.string.app_name));
            msg.setBody(chatMsg.toChatJsonObject());
            
        }
        chatMsg.setMsgTime(System.currentTimeMillis() / 1000);
        if (isSend) {
            chatMsg.setSend(2);
        }
        FinalUserDataBase.getInstance().saveChatMsg(chatMsg, uid, uName, avatarUrl);
        chatMsg.parseUserBaseVo(NextApplication.myInfo.getUserBaseVo());
        if (isSend) {
            Bundle bundle = new Bundle();
            bundle.putInt("type", 1);
            bundle.putString("uid", uid);
            bundle.putString("username", uName);
            bundle.putString("avatarurl", avatarUrl);
            bundle.putSerializable("chatmsg", chatMsg);
            Utils.intentService(mContext, LoadDataService.class, LoadDataService.ACTION_FILE_UPLOAD_CHAT, LoadDataService.ACTION_FILE_UPLOAD_CHAT, bundle);
        }
        return chatMsg;
    }
    
    
    @Override
    public boolean onRecordPrepare() {
        //Check the tape permissions
        try {
            boolean hasPermission = mContext.checkPermission(Manifest.permission.RECORD_AUDIO, android.os.Process.myPid(), android.os.Process.myUid()) == PackageManager.PERMISSION_GRANTED;
            if (!hasPermission) {
                String[] pp = new String[]{Manifest.permission.RECORD_AUDIO};
                ActivityCompat.requestPermissions((Activity) mContext, pp, PERMISSIONS_REQUEST_AUDIO);
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Start recording
     */
    @Override
    public String onRecordStart() {
        recordTotalTime = 0;
        mHorVoiceView.startRecord();
        stopVoice();
        SDFileUtils.getInstance().mkDir(new File(SDFileUtils.audioPath));
        audioName = System.currentTimeMillis() + ".amr";
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        mRecorder.setOutputFile(SDFileUtils.audioPath + audioName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecorder.setAudioSamplingRate(8000);
        mRecorder.setAudioChannels(1);
        mRecorder.setAudioEncodingBitRate(32);
        try {
            mRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mRecorder.start();
        initTimer();
        timer.schedule(timerTask, 0, DEFAULT_MIN_TIME_UPDATE_TIME);
        startRecordTIme = System.currentTimeMillis();
        return audioName;
    }
    
    /**
     * The end of the recording
     */
    @Override
    public boolean onRecordStop() {
        if (recordTotalTime >= minRecordTime) {
            timer.cancel();
            MediaPlayer mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(SDCardCtrl.getAudioPath() + audioName);
            } catch (Exception e) {
                e.printStackTrace();
            }
            int second = (int) ((System.currentTimeMillis() - startRecordTIme) / 1000);
            setVoiceInfo(second);
            mHorVoiceView.setVisibility(View.INVISIBLE);
            mHorVoiceView.stopRecord();
            startRecordTIme = 0;
            stopVoice();
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            if (timerTask != null) {
                timerTask = null;
            }
            record_tips.setText(mContext.getString(R.string.chatting_audio_normal));
        } else {
            onRecordCancel();
        }
        return false;
    }
    
    /**
     * Cancel the recording
     */
    @Override
    public boolean onRecordCancel() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) {
            timerTask = null;
        }
        startRecordTIme = 0;
        mHorVoiceView.setVisibility(View.INVISIBLE);
        mHorVoiceView.stopRecord();
        record_tips.setText(mContext.getString(R.string.chatting_audio_normal));
        stopVoice();
        deleteVoice();
        return false;
    }
    
    /**
     * Slide to cancel
     */
    @Override
    public void onSlideTop() {
        record_tips.setText(mContext.getString(R.string.chatting_audio_down));
    }
    
    /**
     * Finger to press the
     */
    @Override
    public void onFingerPress() {
        mHorVoiceView.setVisibility(View.VISIBLE);
        record_tips.setText(mContext.getString(R.string.chatting_audio_press));
    }
    
    /**
     * Fingers sliding
     */
    @Override
    public void onFingerSlid() {
        record_tips.setText(mContext.getString(R.string.chatting_audio_press));
    }
    
    /**
     * Initializes the timer is used to update the countdown
     */
    private void initTimer() {
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateTimerUI();
                        //Updated once every 1000 milliseconds UI
                        recordTotalTime += 1000;
                    }
                });
            }
        };
    }
    
    private void updateTimerUI() {
        if (recordTotalTime >= maxRecordTime) {
            recordAudioView.invokeStop();
        } else {
            String string = mContext.getString(R.string.chatting_audio_last_say_s, Utils.formatRecordTime(recordTotalTime, maxRecordTime));
            mHorVoiceView.setText(string);
        }
    }
    
    /**
     * Stop the recording
     */
    public void stopVoice() {
        try {
            if (mRecorder != null) {
                mRecorder.stop();
                mRecorder.release();
                mRecorder = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void deleteVoice() {
        try {
            File file = new File(SDCardCtrl.getAudioPath() + audioName);
            file.delete();
        } catch (Exception e) {
        }
        
    }
    
    /**
     * Create photo chat entity class
     */
    private ChatMsg createMeshImageChatMsg(String uid, int chatType, String content, String uName, String avatarUrl, String imageUrl, boolean isGroup, boolean isSend) {
        ChatMsg chatMsg = new ChatMsg();
        try {
            String cover = BitmapUtils.bitmapToString(imageUrl);
            chatMsg.setType(chatType);
            chatMsg.setContent(content);
            chatMsg.setLocalUrl(content);
            chatMsg.setCover(cover);
            chatMsg.setMessageId(UUID.randomUUID().toString());
            chatMsg.setUsername(NextApplication.myInfo.getUsername());
            chatMsg.setUserId(NextApplication.myInfo.getLocalId());
            chatMsg.setMsgTime(System.currentTimeMillis() / 1000);
            
            FinalUserDataBase.getInstance().saveChatMsg(chatMsg, uid, uName, avatarUrl);
            chatMsg.parseUserBaseVo(NextApplication.myInfo.getUserBaseVo());
            
            MessageVo data = new MessageVo();
            data.setChatType(chatType);
            data.setRequest(MeshMessageConfig.REQUEST_CLIENT_DATA);
            data.setFrom(MeshMessageConfig.SERVICE_NAME);
            data.setLocalId(NextApplication.myInfo.getLocalId());
            data.setMessageType(MeshMessageConfig.MESSAGE_IMAGE);
            data.setMessageId(UUID.randomUUID().toString());
            data.setImageLocalUrl(imageUrl);
            if (uid.equals(Constants.APP_MESH)) {
                MeshUtils.getInatance().sendAllImage(mContext, data);
            } else {
                MeshUtils.getInatance().sendImage(mContext, data, uid);
            }
        } catch (Exception e) {
        }
        
        return chatMsg;
    }
}
