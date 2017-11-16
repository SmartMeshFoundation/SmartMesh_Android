package com.lingtuan.firefly.service;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.chat.vo.GalleryVo;
import com.lingtuan.firefly.contact.vo.FriendRecommentVo;
import com.lingtuan.firefly.contact.vo.PhoneContactVo;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.util.BitmapUtils;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.MyToast;
import com.lingtuan.firefly.util.SDCardCtrl;
import com.lingtuan.firefly.util.SDFileUtils;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;
import com.lingtuan.firefly.vo.ChatMsg;
import com.lingtuan.firefly.xmpp.XmppAction;
import com.lingtuan.firefly.xmpp.XmppMessageUtil;
import com.nostra13.universalimageloader.utils.StorageUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.params.CoreConnectionPNames;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Upload and download
 */
public class LoadDataService extends Service implements OnGeocodeSearchListener, Runnable {

    private AMapLocationClient locationClient = null;
    private GeocodeSearch geocoderSearch;
    private Handler mHandler = new Handler();

    /**
     * Radio positioning
     * */
    public static final String ACTION_LOAD_LOCATION = "com.lingtuan.firefly.service.loaddataservice.location";

    /**
     * Upload chat messages
     * */
    public static final String ACTION_FILE_UPLOAD_CHAT = "com.lingtuan.firefly.service.loaddataservice.upload_chat";

    /**
     * Download the audio files
     * */
    public static final String ACTION_FILE_DOWNLOAD = "com.lingtuan.firefly.service.loaddataservice.download";

    /**
     * The download file
     */
    public static final String ACTION_DOWNLOAD_CHAT_FILE = "com.lingtuan.firefly.service.loaddataservice.download.chatfile";

    /**
     * Cancel the file download
     */
    public static final String ACTION_CANCEL_DOWNLOAD_CHAT_FILE = "com.lingtuan.firefly.service.loaddataservice.download.cancelchatfile";
    /**
     * Cancel the file upload
     */
    public static final String ACTION_CANCEL_UPLOAD_CHAT_FILE = "com.lingtuan.firefly.service.loaddataservice.upload.cancelchatfile";

    /**
     * Registration process friend recommendation service ACTION
     */
    public static final String ACTION_UPLOAD_CONTACT = "com.lingtuan.firefly.action_upload_contact";

    /**
     * Inform friends interface friend recommended refresh action
     */
    public static final String ACTION_REFRESH_UNRECOMMENT = "com.lingtuan.firefly.refreshContact.action";

    /**
     * Open the address book to monitor the ACTION
     */
    public static final String ACTION_START_CONTACT_LISTENER = "com.lingtuan.firefly.action_start_contact_listener";

    /**
     * Open video surveillance
     */
    public static final String ACTION_VIDEO_UPLOAD_CHAT = "com.lingtuan.firefly.service.loaddataservice.upload_chat_video";

    /**
     * Download the video
     */
    public static final String ACTION_DOWNLOAD_SOCIALCIRCLE_DATABASE_VIDEO = "com.lingtuan.firefly.service.loaddataservice.download.video";

    public static final String ACTION_UPLOAD_CHAT_FILE_DIR = "com.lingtuan.firefly.service.loaddataservice.upload_chat_file_dir";

    /**
     * Upload picture (avatar)
     */
    public static final String ACTION_FILE_UPLOAD_IMAGE = "com.lingtuan.firefly.service.loaddataservice.upload_image";


    /**
     * The current is sending the message list
     * */
    private ArrayList<ChatMsg> sendingMsgList = new ArrayList<>();


    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public void onCreate() {
        geocoderSearch = new GeocodeSearch(this);
        geocoderSearch.setOnGeocodeSearchListener(this);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (ACTION_LOAD_LOCATION.equals(intent.getAction())) {
                MySharedPrefs.write(this, MySharedPrefs.FILE_USER, MySharedPrefs.LOCATION_TIME, System.currentTimeMillis() + "");
                initLocation();
                if (mHandler != null) {
                    mHandler.postDelayed(this, 1000 * 12);
                }
            }else if (ACTION_FILE_UPLOAD_CHAT.equals(intent.getAction())) {//Chat or voice upload pictures
                Bundle bundle = intent.getBundleExtra(ACTION_FILE_UPLOAD_CHAT);
                int type = bundle.getInt("type");
                String uid = bundle.getString("uid");
                String username = bundle.getString("username");
                String avatarUrl = bundle.getString("avatarurl");
                ChatMsg chatMsg = (ChatMsg) bundle.getSerializable("chatmsg");
                if (hasSendedmsg(chatMsg)) {
                    return super.onStartCommand(intent, flags, startId);
                } else {
                    sendingMsgList.add(chatMsg);
                    new UploadThread(type, chatMsg, uid, username, avatarUrl).start();
                }

            }else if (ACTION_UPLOAD_CONTACT.equals(intent.getAction())) { // Address book friends upload process
                new UploadContactThread().start();
            }else if (ACTION_FILE_UPLOAD_IMAGE.equals(intent.getAction())) {//Upload avatar images
                Bundle bundle = intent.getBundleExtra(ACTION_FILE_UPLOAD_IMAGE);
                if (bundle != null) {
                    String imgPath = bundle.getString("imgPath");
                    String username = bundle.getString("username");
                    String sightml = bundle.getString("sightml");
                    String gender = bundle.getString("gender");
                    String birthcity = bundle.getString("birthcity");
                    if (imgPath != null) {
                        new UploadImageThread(imgPath,username,sightml,gender,birthcity).start();
                    }
                }
            }else if (ACTION_FILE_DOWNLOAD.equals(intent.getAction())) {

            }else if (ACTION_VIDEO_UPLOAD_CHAT.equals(intent.getAction())) {

            }else if (ACTION_DOWNLOAD_CHAT_FILE.equals(intent.getAction())) {     //The download file

            }else if (ACTION_CANCEL_DOWNLOAD_CHAT_FILE.equals(intent.getAction())) {  //Cancel the download

            } else if (ACTION_CANCEL_UPLOAD_CHAT_FILE.equals(intent.getAction())) {          //Cancel the upload

            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /*The existence of detection to send messages and there is not sent*/
    private boolean hasSendedmsg(ChatMsg sendedMsg) {
        for (ChatMsg msg : sendingMsgList) {
            if (sendedMsg.getChatId().equals(msg.getChatId()) && sendedMsg.getType() == msg.getType() && sendedMsg.getMessageId().equals(msg.getMessageId())) {
                return true;
            }
        }
        return false;
    }

    /*Remove the messages sent, send after the failure or success*/
    private void removeSendmsgList(ChatMsg sendedMsg) {
        for (int i = 0; i < sendingMsgList.size(); i++) {
            ChatMsg msg = sendingMsgList.get(i);
            if (sendedMsg.getChatId().equals(msg.getChatId()) && sendedMsg.getType() == msg.getType() && sendedMsg.getMessageId().equals( msg.getMessageId())) {
                sendingMsgList.remove(i);
                i--;
            }
        }
    }

    /**
     * Chat or voice upload pictures
     */
    class UploadThread extends Thread {
        private int type;
        private ChatMsg chatMsg;
        private String uid;
        private String username;
        private String avatarUrl;

        public UploadThread(int type, ChatMsg chatMsg, String uid, String username, String avatarUrl) {
            this.type = type;
            this.chatMsg = chatMsg;
            this.uid = uid;
            this.username = username;
            this.avatarUrl = avatarUrl;
        }

        @Override
        public void run() {

            try {
                updateFile(type, chatMsg,chatMsg.getContent());
            } catch (Exception e) {
                e.printStackTrace();
                chatMsg.setSend(0);
            }

            try {

                if (chatMsg.getSend() == 1) {
                    XmppMessageUtil.getInstance().sendImage(uid, username, avatarUrl, chatMsg, chatMsg.getChatId().startsWith("group-"), true);
                }
                FinalUserDataBase.getInstance().updateChatMsg(chatMsg, chatMsg.getChatId());
                removeSendmsgList(chatMsg);
                Bundle bundle = new Bundle();
                bundle.putSerializable(XmppAction.ACTION_MESSAGE_UPDATE_LISTENER, chatMsg);
                Utils.intentAction(getApplicationContext(), XmppAction.ACTION_MESSAGE_UPDATE_LISTENER, bundle);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * Upload picture only the internal compression for processing the url
     */
    class UploadImageThread extends Thread {

        private String imgPath;
        private String username ;
        private String sightml;
        private String gender ;
        private String birthcity;

        public UploadImageThread(String imgPath,String username,String sightml,String gender,String birthcity) {
            this.imgPath = imgPath;
            this.username = username;
            this.sightml = sightml;
            this.gender = gender;
            this.birthcity = birthcity;
        }

        @Override
        public void run() {

            try {
                NetRequestImpl.getInstance().editUserInfo(imgPath,username, sightml,gender,birthcity,new RequestListener() {
                    @Override
                    public void start() {

                    }

                    @Override
                    public void success(JSONObject response) {
                        if (!TextUtils.isEmpty(response.optString("pic"))){
                            SDCardCtrl.copyFile(imgPath, StorageUtils.getCacheDirectory(LoadDataService.this).getAbsolutePath() + "/uil-images/" + String.valueOf(response.optString("pic").hashCode()));
                        /*Generated thumbnail*/
                            Bitmap bmpThumb = BitmapUtils.extractThumbnail(imgPath, true);
                            String thumbUrl = BitmapUtils.saveBitmap2SD(bmpThumb, 20, true).getPath();
                            SDCardCtrl.copyFile(thumbUrl, StorageUtils.getCacheDirectory(LoadDataService.this).getAbsolutePath() + "/uil-images/" + String.valueOf(response.optString("thumb").hashCode()));
                        }
                        Intent intent = new Intent();
                        intent.setAction(ACTION_FILE_UPLOAD_IMAGE);
                        intent.putExtra("result",response.toString());
                        Utils.sendBroadcastReceiver(getApplicationContext(), intent, true);
                    }

                    @Override
                    public void error(int errorCode, String errorMsg) {
                        Intent intent = new Intent();
                        intent.setAction(ACTION_FILE_UPLOAD_IMAGE);
                        Utils.sendBroadcastReceiver(getApplicationContext(), intent, true);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Intent intent = new Intent();
                intent.setAction(ACTION_FILE_UPLOAD_IMAGE);
                Utils.sendBroadcastReceiver(getApplicationContext(), intent, true);
            }finally {
                SDFileUtils.deleteAll(new File(Environment.getExternalStorageDirectory().getPath() + "/.nextapp/NextUpload"));
            }
        }
    }

    /**
     * upload audio files
     * @ param path file path
     * @ param chatMsg chat entities
     * @ param type, file type
     * */
    private void updateFile(int type,final ChatMsg chatMsg,String path) throws Exception {
        NetRequestImpl.getInstance().uploadSpeak(type, path, chatMsg.getMessageId(),new RequestListener() {
            @Override
            public void start() {

            }

            @Override
            public void success(JSONObject response) {
                chatMsg.setContent(response.optString("url"));
                chatMsg.setSend(1);
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                chatMsg.setSend(0);
            }
        });
    }



    /**-----------------------------------------------Directory to upload related began---------------------------------------------------------------**/
    private String typeList = null;
    /**
     *Upload the address book
     */
    class UploadContactThread extends Thread {
        @Override
        public void run() {
            synchronized (this) {
                List<String> mList = getPeopleInPhone();
                List<PhoneContactVo> mPhoneContactList = FinalUserDataBase.getInstance().getPhoneContactList(1);//1:Mobile address book
                for (int j = 0; j < mPhoneContactList.size(); j++) {
                    PhoneContactVo vo = mPhoneContactList.get(j);
                    boolean found = false;
                    for (int m = 0; m < mList.size(); m++) {
                        if (vo.getId().equals(mList.get(m))) {
                            found = true;
                            break;
                        }
                    }
                    if (!found){//The phone number is deleted
                        FinalUserDataBase.getInstance().deletePhoneContact(vo.getId());
                    }
                }
                List<FriendRecommentVo> mRecommentList = FinalUserDataBase.getInstance().getFriendsRecomment();
                for (int i = 0; i < mRecommentList.size(); i++){//Friend recommended database
                    boolean found = false;
                    FriendRecommentVo vo = mRecommentList.get(i);
                    for (int m = 0; m < mList.size(); m++) {
                        if (vo.getType() == 1 && vo.getFriendId().equals(mList.get(m))) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        FinalUserDataBase.getInstance().deleteFriendsRecommentByFriendId(vo.getFriendId());
                    }
                }
                if (!TextUtils.isEmpty(typeList)) {
                    NetRequestImpl.getInstance().uploadContactFriend(typeList, new RequestListener() {
                        @Override
                        public void start() {

                        }

                        @Override
                        public void success(JSONObject response) {
                            JSONArray array = response.optJSONArray("list");
                            int count = array.length();
                            if (array != null && count > 0) {
                                for (int i = 0; i < count; i++) {
                                    PhoneContactVo vo = new PhoneContactVo();
                                    vo.parse(array.optJSONObject(i));
                                    FinalUserDataBase.getInstance().updatePhoneContact(vo);
                                    if (vo.getRelation() == 1) { //When users are friends and not to save
                                        PhoneContactVo vo1 = FinalUserDataBase.getInstance().getPhoneContactById(vo.getId(), 1);
                                        FriendRecommentVo frVo = new FriendRecommentVo();
                                        frVo.parseHttp(array.optJSONObject(i).toString());
                                        frVo.setType(1);
                                        frVo.setUnread(1);
                                        if (vo1 != null) {
                                            frVo.setThirdName(vo1.getShowName());
                                        }
                                        FinalUserDataBase.getInstance().saveFriendsReComment(frVo);
                                    }
                                }
                                Intent intent = new Intent(ACTION_REFRESH_UNRECOMMENT);
                                LocalBroadcastManager.getInstance(LoadDataService.this).sendBroadcast(intent);
                            }
                        }

                        @Override
                        public void error(int errorCode, String errorMsg) {
                            Intent intent = new Intent(ACTION_REFRESH_UNRECOMMENT);
                            LocalBroadcastManager.getInstance(LoadDataService.this).sendBroadcast(intent);
                        }
                    });
                }else{
                    Intent intent = new Intent(ACTION_REFRESH_UNRECOMMENT);
                    LocalBroadcastManager.getInstance(LoadDataService.this).sendBroadcast(intent);
                }
            }

        }
    }

    /**
     *The address book query
     */
    private List<String> getPeopleInPhone() {
        String select;
        int version = android.os.Build.VERSION.SDK_INT;
        if (version > 10) { //More than 2.3 version of the system
            select = "((" + ContactsContract.Contacts.DISPLAY_NAME + " NOTNULL) AND (" + ContactsContract.Contacts.HAS_PHONE_NUMBER + "=1) AND (" + ContactsContract.Contacts.DISPLAY_NAME + " != '' ))";
        } else { // Less than or equal to 2.3
            select = "((" + ContactsContract.Contacts.DISPLAY_NAME + " NOTNULL) AND (" + ContactsContract.Contacts.DISPLAY_NAME + " != '' ))";
        }
        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, select, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE LOCALIZED ASC");        // 获取手机联系人
        List<String> mList = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        while (cursor != null && cursor.moveToNext()) {
            int indexPeopleName = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);    // people name
            int indexPhoneNum = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);            // phone number
            String name = cursor.getString(indexPeopleName);
            String number = cursor.getString(indexPhoneNum);
            if (!TextUtils.isEmpty(number)) { // Some garbage data filtering
                number = number.replace("-", "");
                number = number.replace("*", "");
                number = number.replace("+86", "").trim();
                number = number.replace(" ", "").trim();
                if (!number.startsWith("400") && number.length() >= 11) { //
                    sb.append(number);
                    sb.append(",");
                    mList.add(number);
                }
            }
            PhoneContactVo vo = new PhoneContactVo();
            vo.setName(name);
            vo.setId(number);
            vo.setType(1);
            FinalUserDataBase.getInstance().savePhoneContact(vo, false);
        }
        if (!TextUtils.isEmpty(sb.toString())) {
            sb.deleteCharAt(sb.lastIndexOf(","));
        }

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
            cursor = null;
        }
        typeList = sb.toString();
        return mList;
    }

/**-----------------------------------------------Directory to upload related to an end---------------------------------------------------------------**/

/**-----------------------------------------Positioning related to start------------------------------------------*/
    /**
     * Initialize the positioning
     */
    private void initLocation() {
        //Initialize the client
        locationClient = new AMapLocationClient(this.getApplicationContext());
        //Setting up the positioning parameters
        locationClient.setLocationOption(getDefaultOption());
        // Establishment of listening
        locationClient.setLocationListener(locationListener);
        // Start position
        locationClient.startLocation();
    }

    /**
     * The default location parameters
     */
    private AMapLocationClientOption getDefaultOption() {
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//Optional, set positioning mode, the optional model has high precision, only equipment, network only.For high precision mode by default
        mOption.setGpsFirst(false);//Optional, set whether GPS priority, only under the mode of high precision effectively.Off by default
        mOption.setHttpTimeOut(30000);//Optional, set up the network request timeout.The default for 30 seconds.Under the mode of equipment only is invalid
        mOption.setInterval(2000);//Optional, establishment of intervals.The default to 2 seconds,
        mOption.setNeedAddress(true);//Optional, whether return inverse geographical address information.The default is true
        mOption.setOnceLocation(false);//Optional, setting is a single location.The default is false
        mOption.setOnceLocationLatest(false);//Optional Settings are waiting for the wifi refresh, the default is false. If set to true, will automatically into a single location, don't use continuous positioning
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);//Optional, set up the network protocol requests.Optional HTTP or HTTPS.The default for HTTP
        mOption.setSensorEnable(false);//Optional, set whether to use sensors.The default is false
        return mOption;
    }

    /**
     * Positioning to monitor
     */
    AMapLocationListener locationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation loc) {
            if (null != loc) {
                Double geoLat = loc.getLatitude();
                Double geoLng = loc.getLongitude();
                getAddress(new LatLonPoint(geoLat, geoLng));
                MySharedPrefs.write(getApplicationContext(),
                        MySharedPrefs.FILE_USER,
                        MySharedPrefs.KEY_LOCATION, geoLat + "," + geoLng);
            } else {
                MyToast.showToast(LoadDataService.this, getResources().getString(R.string.location_notify));
            }
        }
    };


    @Override
    public void onGeocodeSearched(GeocodeResult arg0, int arg1) {
    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
        if (rCode == 0) {
            if (result != null && result.getRegeocodeAddress() != null
                    && result.getRegeocodeAddress().getFormatAddress() != null) {
                String addressName = getString(R.string.nearby,result.getRegeocodeAddress().getFormatAddress());
                MySharedPrefs.write(getApplicationContext(),
                        MySharedPrefs.FILE_USER,
                        MySharedPrefs.KEY_LOCATION_ADDRESSNAME, addressName);
            }
        }

    }

    /**
     * In response to reverse geocoding
     */
    private void getAddress(final LatLonPoint latLonPoint) {
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200,
                GeocodeSearch.AMAP);// The first parameter indicates a Latlng, scope of the second parameter indicates how many meters, said the third parameter is the fire department coordinate system or GPS native
        geocoderSearch.getFromLocationAsyn(query);// Set the synchronous reverse geocoding request
    }


    /**
     * Destruction of positioning
     */
    private void destroyLocation() {
        if (null != locationClient) {
            /**
             * if AMapLocationClient is in the current Activity instantiation,
             * in the Activity onDestroy must perform AMapLocationClient onDestroy
             */
            locationClient.onDestroy();
            locationClient = null;
        }
    }

    @Override
    public void run() {
        destroyLocation();
    }
/**-----------------------------------------Locate relevant end-----------------------------------------*/
}
