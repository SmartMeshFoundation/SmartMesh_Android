package com.lingtuan.firefly.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.custom.gesturelock.ACache;
import com.lingtuan.firefly.wallet.util.WalletStorage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class MySharedPrefs {

    /*List stored purse*/
    public static final String FILE_WALLET = "walletlist";

    /*Global information is stored*/
    public static final String FILE_APPLICATION = "application";

    /*Language is the key value*/
    public static final String KEY_LANGUAFE = "key_language";

    /*The system time key values*/
    public static final String KEY_REQTIME = "key_reqtime";


    /*The wallet key values*/
    public static final String KEY_WALLET = "key_wallet";


    /*The all wallet key values*/
    public static final String KEY_ALL_WALLET = "key_all_wallet";

    /*Storing user information*/
    public static final String FILE_USER = "userinfo";

    /*Is the first time you login*/
    public static final String IS_FIRST_LOGIN = "is_first_login";

    /*Store the current position time information*/
    public static final String LOCATION_TIME = "location_time";

    /*positioning*/
    public static final String KEY_LOCATION = "location";

    /*location*/
    public static final String KEY_LOCATION_ADDRESSNAME = "location_addressname";

    /*Whether it is the first time you install and use the software*/
    public static final String KEY_IS_FIRST_WALLET_USE = "key_is_first_wallet_use";

    /*Whether it is the wallet pattern   0 not wallet pattern   1 is wallet pattern but is not login     2 has login*/
    public static final String KEY_IS_WALLET_PATTERN = "key_is_wallet_pattern";

    /*update version*/
    public static final String KEY_UPDATE_VERSION = "key_update_version";

    /*Personal information*/
    public static final String KEY_LOGIN_USERINFO = "user_userinfo";

    /*Login user name*/
    public static final String KEY_LOGIN_NAME = "user_login_name";

    /*Local personal information*/
    public static final String KEY_LOCAL_USERINFO = "user_local";


    /*Whether shielding information*/
    public static final String IS_MASK_MSG = "is_mask_msg";

    /*Gesture error*/
    public static final String GESTIRE_ERROR = "gesture_error";

    /*show wallet dialog*/
    public static final String IS_SHOW_WALLET_DIALOG = "show_wallet_dialog";

    /**
     * The receiver model
     */
    public static final String AUDIO_MODE = "audio_mode";

    /**
     * Voice message
     */
    public static final String MSG_SOUND = "switch_receive_new_msg";

    /**
     * You are the one model
     */
    public static final String NO_DISURB_MODE = "switch_no_disturb_mode";

    /**
     * You are the one pattern start time (hour)
     */
    public static final String NO_DISURB_MODE_BEGIN_TIME_HOUR = "switch_no_disturb_mode_begin_time_hour";

    /**
     * You are the one model The start time (minute)
     */
    public static final String NO_DISURB_MODE_BEGIN_TIME_MINUTE = "switch_no_disturb_mode_begin_time_minute";
    /**
     * You are the one model over time (hour)
     */
    public static final String NO_DISURB_MODE_END_TIME_HOUR = "switch_no_disturb_mode_end_time_hour";

    /**
     * You are the one model over time (minute)
     */
    public static final String NO_DISURB_MODE_END_TIME_MINUTE = "switch_no_disturb_mode_end_time_minute";

    /**
     * The message of vibration
     */
    public static final String MSG_VIBRATION = "vibration";


    /**
     * Receive new messages to remind
     */
    public static final String RECEIVE_NEW_MSG = "receive_new_msg";

    /**
     * Friends note
     * */
    public static final String KEY_FRIEND_NOTE = "friend_note";

    /**
     * The local path to upload the files
     * */
    public static final String KEY_FIREFLY_FILEPATH = "firefly_filepath";

    /**
     * Upload the local path of files
    */
    public static final String KEY_GROUP_FILEPATH = "group_filepath";

    /**
     * no net work communication
     * */
    public static final String KEY_NO_NETWORK_COMMUNICATION = "no_net_work_communication_";

    /**
     * read the wallet list
     * @ param context context
     * */
    public static String readWalletList(Context context){
        int walletMode = MySharedPrefs.readInt(NextApplication.mContext, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_IS_WALLET_PATTERN);
        if (walletMode == 0 && NextApplication.myInfo != null){
            return readString(context,FILE_WALLET,NextApplication.myInfo.getLocalId());
        }else if (walletMode == 1){
            return readString(context,FILE_WALLET,KEY_ALL_WALLET);
        }else {
            return readString(context,FILE_WALLET,KEY_WALLET);
        }
    }

    /**
     * reload wallet list
     * */
    public static void reLoadWalletList(){
        int walletMode = MySharedPrefs.readInt(NextApplication.mContext, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_IS_WALLET_PATTERN);
        if (walletMode != 0){
            MySharedPrefs.writeInt(NextApplication.mContext, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_IS_WALLET_PATTERN,0);
            if (walletMode == 2){
                try {
                    WalletStorage.getInstance(NextApplication.mContext).reLoad(NextApplication.mContext);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                byte[] gestureByte = ACache.get(NextApplication.mContext).getAsBinary(Constants.GESTURE_PASSWORD);
                if (gestureByte != null && gestureByte.length > 0 && NextApplication.myInfo != null){
                    ACache.get(NextApplication.mContext).put(Constants.GESTURE_PASSWORD + NextApplication.myInfo.getLocalId(), gestureByte);
                }
                Utils.sendBroadcastReceiver(NextApplication.mContext, new Intent(Constants.WALLET_REFRESH_SHOW_HINT), false);
            }else{
                try {
                    WalletStorage.getInstance(NextApplication.mContext).reLoadUserWallet(NextApplication.mContext);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            Utils.sendBroadcastReceiver(NextApplication.mContext, new Intent(Constants.WALLET_REFRESH_GESTURE), false);
        }
    }

    /**
     * read the wallet list
     * @ param context context
     * */
    public static String readWalletModeList(Context context){
        return readString(context,FILE_WALLET,KEY_WALLET);
    }

    /**
     * read the wallet list
     * @ param context context
     * */
    public static String readWalletModeAllList(Context context){
        return readString(context,FILE_WALLET,KEY_ALL_WALLET);
    }


    /**
     * read the wallet list
     * @ param context context
     * */
    public static String readUserWalletList(Context context){
        if (NextApplication.myInfo == null){
            return null;
        }
        return readString(context,FILE_WALLET,NextApplication.myInfo.getLocalId());
    }



    /**
     * read the String
     * @ param fileName file name
     * @ param key key values
     * */
    public static String readString(Context context, String fileName, String key) {
        if (context == null) return "";
        SharedPreferences sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        String value = sharedPreferences.getString(key, "");
        return value;
    }

    public static void write(Context context, String fileName, String key, String value) {
        if (context == null)
            return;
        SharedPreferences sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static void writeLong(Context context, String fileName, final String key, final long value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    /**
     * write a Boolean switch value
     * @ param fileName file name
     * @ param key key values
     * @ param value content
     */
    public static void writeBoolean(Context context, String fileName, String key, boolean value) {
        if (context == null)
            return;
        SharedPreferences sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    /**
     * write int value
     * @ param fileName file name
     * @ param key key values
     * @ param value content
     */
    public static void writeInt(Context context, String fileName, final String key, final int value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }


    /**
     * read the String
     * @ param fileName file name
     * @ param key key values
     * */
    public static long readLong(Context context, String fileName, String key) {
        if (context == null) return 0;
        SharedPreferences sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        long value = sharedPreferences.getLong(key,0);
        return value;
    }

    /**
     * read an int value 1 by default
     * @ param fileName file name
     * @ param key key values
     */
    public static int readInt1(Context context, String fileName, String key) {
        if (context == null) return -1;
        SharedPreferences sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(key, -1);
    }

    /**
     * read an int value zero by default
     * @ param fileName file name
     * @ param key key values
     */
    public static int readInt(Context context, String fileName, String key) {
        if (context == null) return 0;
        SharedPreferences sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(key, 0);
    }


    /**
     * read the Boolean value of true by default
     * @ param fileName file name
     * @ param key key values
     */
    @Deprecated
    public static boolean readBoolean(Context context, String fileName, String key) {
        if (context == null) return true;
        SharedPreferences sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(key, true);
    }

    /**
     * read Boolean false by default
     * @ param fileName file name
     * @ param key key values
     */
    public static boolean readBooleanNormal(Context context, String fileName, String key) {
        if (context == null) return false;
        SharedPreferences sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(key, false);
    }

    /**
     * delete key value
     */
    public static void removeCache(Context c, String fileName, String key) {
        if (c == null) return;
        SharedPreferences sharedPreferences = c.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.commit();
    }

    public static void clearUserInfo(Context context) {
        if (context == null) return;
        SharedPreferences sharedPreferences = context.getSharedPreferences(FILE_USER, Context.MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();
        editor.putString(KEY_LOGIN_USERINFO, "");
        editor.putInt(KEY_IS_WALLET_PATTERN, 0);
        if (NextApplication.myInfo != null){
            String jsonString  = MySharedPrefs.readString(context, MySharedPrefs.FILE_USER, NextApplication.myInfo.getLocalId());
            try {
                JSONObject object = new JSONObject(jsonString);
                JSONObject oldObject = object.optJSONObject(NextApplication.myInfo.getLocalId());
                oldObject.put("token","");
                object.put(NextApplication.myInfo.getLocalId(),oldObject);
                MySharedPrefs.write(context, MySharedPrefs.FILE_USER, NextApplication.myInfo.getLocalId(),object.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        editor.commit();
    }


    /**
     * Delete the key value
     */
    public static void removeFriendNote(Context c, String key) {
        if (c == null) return;
        SharedPreferences sharedPreferences = c.getSharedPreferences(MySharedPrefs.KEY_FRIEND_NOTE + NextApplication.myInfo.getLocalId(), Context.MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.commit();
    }

    /**
     * Delete the key value
     */
    public static void remove(Context c, String key) {
        if (c == null) return;
        SharedPreferences sharedPreferences = c.getSharedPreferences(MySharedPrefs.FILE_USER, Context.MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.commit();
    }


}
