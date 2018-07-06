package com.lingtuan.firefly.util;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.ColorInt;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.chat.ChattingUI;
import com.lingtuan.firefly.contact.DiscussGroupJoinUI;
import com.lingtuan.firefly.custom.LanguageChangableView;
import com.lingtuan.firefly.service.LoadDataService;
import com.lingtuan.firefly.ui.FriendInfoUI;
import com.lingtuan.firefly.ui.WebViewUI;
import com.lingtuan.firefly.vo.UserBaseVo;
import com.lingtuan.firefly.vo.UserInfoVo;
import com.lingtuan.firefly.wallet.util.WalletStorage;
import com.lingtuan.firefly.wallet.vo.StorableWallet;
import com.lingtuan.firefly.xmpp.XmppAction;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.apache.http.util.EncodingUtils;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import io.reactivex.functions.Consumer;

/**
 * Created on 2017/8/18.
 */

public class Utils {

    /**
     * send broadcast
     * @ param action filters
     * @ param bundle parameters
     */
    public static void intentAction(Context mContext, String action, Bundle bundle) {
        if (!TextUtils.isEmpty(action)) {
            Intent intent = new Intent(action);
            if (bundle != null)
                intent.putExtra(action, bundle);
            if (mContext != null)
                mContext.sendBroadcast(intent);
        }
    }

    public static void intentAction(Context mContext, Intent intent) {
        mContext.sendBroadcast(intent);
    }

    /**
     * send broadcast
     * @ param intent parameters
     * @ param local is a local broadcast
     */
    public static void sendBroadcastReceiver(Context mContext, Intent intent, boolean local) {
        if (mContext != null) {
            if (local) {
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            } else {
                mContext.sendBroadcast(intent);
            }
        }
    }

    /**
     * Sends the service radio
     */
    public static void intentServiceAction(Context mContext, String action, Bundle bundle) {
        ComponentName mName = new ComponentName(mContext, LoadDataService.class);
        Intent service = new Intent(action);
        if (bundle != null) {
            service.putExtra(action, bundle);
        }
        service.setComponent(mName);
        mContext.startService(service);
    }

    /**
     * Start the service
     * */
    public static void intentService(Context mContext, Class serviceClass, String serviceAction, String bundleKey, Bundle bundle) {
        Intent service = new Intent(serviceAction);
        ComponentName mComponentName = new ComponentName(mContext, serviceClass);
        service.setComponent(mComponentName);
        if (!TextUtils.isEmpty(bundleKey) && bundle != null) {//Passing parameters
            service.putExtra(bundleKey, bundle);
        }
        mContext.startService(service);
    }

    /**
     * Determine whether connected network
     */
    public static boolean isConnectNet(Context context) {
        if (context != null) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo Mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            NetworkInfo Wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            return Mobile != null && Wifi != null && NetworkInfo.State.CONNECTED.equals(Mobile.getState()) || NetworkInfo.State.CONNECTED.equals(Wifi.getState());
        }
        return false;
    }

    /**
     * A - > B page, A slide out of the screen to the left, B from sliding screen on the right side of the screen
     */
    public static void openNewActivityAnim(Activity act, boolean finish) {
        if (act != null) {
            act.overridePendingTransition(R.anim.push_right_to_middle_in, R.anim.push_middle_to_left_out);
            if (finish) {
                act.finish();
            }
        }
    }

    /**
     * A-->B页面,A不动,B从屏幕中间进入全屏
     */
    public static void openNewActivityFullScreenAnim(Activity act, boolean finish) {
        if (act != null) {
            act.overridePendingTransition(R.anim.push_miss_to_fullscreen, R.anim.push_fullscreen_to_miss);
            if (finish) {
                act.finish();
            }
        }
    }


    /**
     * Page B - > A, B from the screen to the right measure, A slide from left to right to screen the middle,
     */

    public static void exitActivityAndBackAnim(Activity act, boolean finish) {
        if (act != null) {
            if (finish) {
                act.finish();
            }
            act.overridePendingTransition(R.anim.push_left_to_middle_in, R.anim.push_middle_to_right_out);
        }
    }

    /**
     * @ param loginUserUid currently logged in user's uid
     * @ SharedPreferences return no network chat room key
     * stitching no network chat room key
     */
    public static String buildOffLineRoomPreferencesKey(String loginUserUid) {
        StringBuilder sb = new StringBuilder();
        String prefix = "OffLine_";
        sb.append(prefix);
        sb.append("Room_");
        sb.append(loginUserUid);
        return sb.toString();
    }

    /**
     * Access to program version number
     * */
    public static int getVersionCode(Context mContext) {
        PackageManager packageManager = mContext.getPackageManager();
        // getPackageName()Is your current class package name. 0 is represented for version information
        PackageInfo packInfo;
        try {
            packInfo = packageManager.getPackageInfo(mContext.getPackageName(), 0);
            return packInfo.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 110;
    }

    /**
     * Access to program version name
     * */
    public static String getVersionName(Context mContext) {
        PackageManager packageManager = mContext.getPackageManager();
        PackageInfo packInfo;
        try {
            packInfo = packageManager.getPackageInfo(mContext.getPackageName(), 0);
            return packInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "1.0.0";
    }

    /**
     * Get their Allies channel number
     *
     * @param mContext
     * @return
     */
    public static String getChannel(Context mContext) {
        String channel = "wsc";
        try {
            ApplicationInfo appInfo;
            appInfo = mContext.getPackageManager().getApplicationInfo(mContext.getPackageName(), PackageManager.GET_META_DATA);
            channel = appInfo.metaData.getString("UMENG_CHANNEL");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return channel;
    }

    /**
     * Access to mobile phone IMEI number
     */
    public static String getIMEI(Context mContext) {
        final TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            try {
                RxPermissions rxPermissions = new RxPermissions((Activity) mContext);
                rxPermissions.request(Manifest.permission.READ_PHONE_STATE);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return telephonyManager.getDeviceId();
    }

    /**
     * Sending pictures percentage
     *
     * @param messageId
     * @param percent
     */
    public static void intentImagePrecent(Context mContext, String messageId, int percent) {
        if (mContext != null) {
            Intent intent = new Intent(XmppAction.ACTION_MESSAGE_IMAGE_PERCENT);
            intent.putExtra("time", messageId);
            intent.putExtra("percent", percent);
            mContext.sendBroadcast(intent);
        }
    }

    public static String getScreenPixels(Context mContext) {
        if (mContext == null) return "720*1280";
        int width = mContext.getResources().getDisplayMetrics().widthPixels;
        int height = mContext.getResources().getDisplayMetrics().heightPixels;
        return String.valueOf(width).concat("*").concat(String.valueOf(height));
    }

    /**
     * Number formatting unread items
     */
    public static void formatUnreadCount(TextView tv, int unread) {
        if (unread < 1) {
            tv.setVisibility(View.GONE);
        } else {
            tv.setVisibility(View.VISIBLE);
            if (unread < 100) {//Open the notes reveal all unread item number
                tv.setText(unread + "");
            } else {
                tv.setText("99+");
            }
        }

    }

    /**
     * last login time
     * @ param time is ten digits if not require / 1000
     */
    public static void setLoginTime(Context mContext, TextView tv, long time) {
        long times = System.currentTimeMillis() / 1000 - time;
         if (times < 3600 * 24) {//hours
            tv.setText(formatHourMinTime(time));
        } else {
            tv.setText(convertMonthToDay(time));
        }
    }

    public static String read(InputStream in) throws IOException {
        byte[] data;
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int len = 0;
        while ((len = in.read(buf)) != -1) {
            bout.write(buf, 0, len);
        }
        data = bout.toByteArray();
        return new String(data, "UTF-8");
    }


    /**
     * Recycling each guide page memory
     */
    public static void recycleImageBg(View iv) {
        try {
            BitmapDrawable bd = (BitmapDrawable) iv.getBackground();
            if (bd.getBitmap() != null) {
                Bitmap bm = bd.getBitmap();
                if (bm != null && !bm.isRecycled()) {
                    bm.recycle();
                    bm = null;
                    iv.setBackgroundResource(0);
                }
            }
        } catch (Exception e) {
        }
    }


    //Turn on,
    public static String convertMonthToDay(long time) {
        SimpleDateFormat format = new SimpleDateFormat("MM-dd");
        return format.format(time * 1000);
    }

    public static String formatTime(long time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return format.format(time * 1000);
    }

    /**
     * transfer record time
     * */
    public static String formatTransTime(long time) {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        return format.format(time * 1000);
    }

    /**
     * System time is a wonderful work of 10 digits and 10 digits seconds value < / font >
     */
    public static String eventDetailTime(long time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm");
        return format.format(time * 1000);
    }

    /**
     * System time is a wonderful work of 10 digits and 10 digits seconds value < / font >
     */
    public static String transDetailTime(long time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return format.format(time * 1000);
    }

    /**
     * Record application error log when used
     */
    public static final String getSimpDate() {
        Date currentDate = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        currentDate = Calendar.getInstance().getTime();
        return formatter.format(currentDate);
    }

    /**
     * System time is a wonderful work of 10 digits and 10 digits seconds value < / font >
     */
    public static String formatHourMinTime(long time) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        return format.format(time * 1000);
    }

    /**
     * Format the time display for 5 '10"
     */
    public static String formatRecordTime(long recTime, long maxRecordTime) {
        int time = (int) ((maxRecordTime - recTime) / 1000);
        int minute = time / 60;
        int second = time % 60;
        if (second < 10){
            return String.format("%d:0%d", minute, second);
        }else{
            return String.format("%d:%d", minute, second);
        }

    }


    /**
     * Settings file expiration time
     *
     * @param time
     * @return
     */
    public static String setExpireTime(Context context,long time) {
        if (time > 3600 * 24) {      //day
            int day = ((int) (time / (3600 * 24))) + 1;
            day = day > 7 ? 7 : day;
            return context.getString(R.string.chat_file_days_failure,day);
        } else if (time > 3600) {         //hours
            int hour = ((int) (time / 3600)) + 1;
            return context.getString(R.string.chat_file_hours_failure,hour);
        } else if (time > 60) {
            int minute = ((int) (time / 60)) + 1;
            return context.getString(R.string.chat_file_minutes_failure,minute);
        } else {
            return context.getString(R.string.chat_file_as_failure);
        }
    }

    /**
     * Hide the soft keyboard
     */
    public static void hiddenKeyBoard(Activity activity) {

        try {
            if (activity == null) return;
            // Cancel the pop-up dialog box
            InputMethodManager manager = (InputMethodManager) activity.getBaseContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (manager.isActive()) { //Only when the keyboard is in the state of the pop-up to hide by: KNothing
                if (activity.getCurrentFocus() == null) {
                    return;
                }
                manager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Forced the pop-up
     */
    public static void showKeyBoard(EditText edit) {
        try {
            if (edit == null) return;
            edit.requestFocus();
            InputMethodManager m = (InputMethodManager) edit.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            m.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * According to the resolution of the mobile phone from dp units become px (pixels)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


    public static void notifySystemUpdateFolder(Context context, File file) {
        //Comrade system update photo album
        int version = Build.VERSION.SDK_INT;
        if (version < 19) {
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + file.getParentFile().getAbsolutePath())));
        } else {
            MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()}, null, null);
        }
    }

    /**
     * by : cyt
     * @ param name json file name
     * @ return void
     * @ TODO json to local
     */
    public static void writeToFile(JSONObject response, String name) {
        try {//Saved to the local
            File jsonFile = new File(SDCardCtrl.getFilePath() + File.separator + name);
            File dirs = new File(jsonFile.getParent());
            dirs.mkdirs();
            jsonFile.createNewFile();
            FileOutputStream fileOutS = new FileOutputStream(jsonFile);
            byte[] buf = response.toString().getBytes();
            fileOutS.write(buf);
            fileOutS.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * by : cyt
     * @ param name json file name
     * @ return void
     * @ TODO read json from a local
     */
    public static String readFromFile(String name) {
        String res = "";
        try {
            File file = new File(SDCardCtrl.getFilePath() + File.separator + name);
            if(!file.exists())
            {
                return res;
            }
            FileInputStream fin = new FileInputStream(file);
            int length = fin.available();
            byte[] buffer = new byte[length];
            fin.read(buffer);
            res = EncodingUtils.getString(buffer, "UTF-8");
            fin.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * by : cyt
     * @return void
     * @ TODO delete local cache files
     */
    public static void deleteFiles(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        }
        if (file.isDirectory()) {
            File[] childFile = file.listFiles();
            if (childFile == null || childFile.length == 0) {
                file.delete();
                return;
            }
            for (File f : childFile) {
                deleteFiles(f);
            }
            file.delete();
        }
    }



    /**
     * set TabLayout bottom line width
     * @ param tabs TabLayout
     * @ param leftDip distance from the left
     * @ param rightDip distance from the right
     * */
    public static void setIndicator (TabLayout tabs, int leftDip, int rightDip){
        Class<?> tabLayout = tabs.getClass();
        Field tabStrip = null;
        try {
            tabStrip = tabLayout.getDeclaredField("mTabStrip");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        tabStrip.setAccessible(true);
        LinearLayout llTab = null;
        try {
            llTab = (LinearLayout) tabStrip.get(tabs);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        int left = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, leftDip, Resources.getSystem().getDisplayMetrics());
        int right = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, rightDip, Resources.getSystem().getDisplayMetrics());

        for (int i = 0; i < llTab.getChildCount(); i++) {
            View child = llTab.getChildAt(i);
            child.setPadding(0, 0, 0, 0);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
            params.leftMargin = left;
            params.rightMargin = right;
            child.setLayoutParams(params);
            child.invalidate();
        }

    }


    /**
     * get the purse
     * @ param context context
     * What a purse @ param position
     * */
    public static String getWalletImg(Context context,int position){
        int index = position%100+1;
        String ids = "icon_static_00"+index;
        if(index>=10 && index<100)
        {
            ids = "icon_static_0"+index;
        }
        else if(index==100){
            ids = "icon_static_"+index;
        }
        return ids;
    }

    /**
     * get the purse
     * @ param context context
     * What a purse @ param position
     * */
    public static  int getWalletImageId(Context context,String ids){
        return context.getResources().getIdentifier(context.getPackageName() + ":drawable/" + ids, null,
                null);
    }

    /**
     * get the purse
     * @ param context context
     * What a purse @ param position
     * */
    public static String setWalletImageId(){
        int index = Integer.parseInt(Utils.makeRandomInt(2));
        String ids = "icon_static_0" + index;
        return ids;
    }


    /**
     * Name to get the wallet
     * @ param context context Wallet name name is empty
     * */
    public static String getWalletName(Context context){
        int index= 1;
        String walletName;
        while (true){
            walletName  = context.getString(R.string.account)	+index;
            boolean foundSameName =false;
            for(StorableWallet storableWallet : WalletStorage.getInstance(context).get()){
                if(walletName.equals(storableWallet.getWalletName())){
                    foundSameName = true;
                    break;
                }
            }
            if(foundSameName){
                index++;
            }else{
                break;
            }
        }
        return walletName;
    }

    /**
     * Replace the carriage returns
     */
    public static String replyEnter(String value) {
        if (value == null) {
            value = "";
        }
        return value.replace("\\n", "").replace("\\r", "");
    }


    /**
     * 设置状态栏颜色
     * setting status bar color
     * */
    public static void setStatusBar(Context context,int colorPrimary){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = ((Activity)context).getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            if (colorPrimary == 0){
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                window.setStatusBarColor(context.getResources().getColor(R.color.colorPrimary));
            }else if (colorPrimary == 1){
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                window.setStatusBarColor(context.getResources().getColor(R.color.wallet_copy_bg));
            }else{
                window.getDecorView().setSystemUiVisibility(View.STATUS_BAR_VISIBLE);
                window.setStatusBarColor(context.getResources().getColor(R.color.colorFound));
            }
        }
    }


    /**
     * copy the text to the clipboard
     * @ param context
     * @ param text text content
     * */
    public static  void copyText(Context context,String text) {
        // since API11 android is recommended to use android. The content. ClipboardManager
        // in order to compatible with low version. Here we use the old version of the android text. ClipboardManager, although deprecated, but not influence use.
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        // The text content on the system clipboard。
        cm.setPrimaryClip(ClipData.newPlainText(null, text));
        MyToast.showToast(context,context.getString(R.string.copy_end));
    }

    /**
     * Set up multiple languages
     * */
    public static void settingLanguage(Context context){
        String language = MySharedPrefs.readString(context,MySharedPrefs.FILE_APPLICATION,MySharedPrefs.KEY_LANGUAFE);
        Locale locale;
        if (TextUtils.isEmpty(language)){
            if (Build.VERSION.SDK_INT >= 24 && TextUtils.equals(Locale.getDefault().toString(),"en")){
                locale = new Locale(Locale.getDefault().toString());
            }else{
                locale = new Locale(Locale.getDefault().getLanguage());
            }
        }else{
            locale = new Locale(language);
        }
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            conf.setLocale(locale);
        } else {
            conf.locale = locale;
        }
        res.updateConfiguration(conf, dm);
    }


    public static void updateViewMethod(TextView uploadRegisterInfo,Context context){
        if (uploadRegisterInfo != null){
            if ((NextApplication.myInfo == null || TextUtils.isEmpty(NextApplication.myInfo.getMid())) && isConnectNet(context)){
                uploadRegisterInfo.setVisibility(View.VISIBLE);
            }else{
                uploadRegisterInfo.setVisibility(View.GONE);
            }
        }
    }

    /**
     * String to an int
     * */
    public static int string2int (String str) {
        try {
            return Integer.valueOf(str);
        } catch (Exception e) {
        }
        return 0;
    }

    /**
     * by : cyt
     * @ param length key length less than or equal to 16
     * @ return String
     * @ TODO immediately generated specifies the length of the key
     */
    public static String makeRandomKey(int length) {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        if (length > 16) {
            length = 16;
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; ++i) {
            int number = random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

    /**
     * by : cyt
     * @ param length key length less than or equal to 16
     * @ return String
     * @ TODO immediately generated specifies the length of the key
     */
    public static String makeRandomInt(int length) {
        String str = "123456789";
        Random random = new Random();
        if (length > 2) {
            length = 2;
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; ++i) {
            int number = random.nextInt(9);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

    /**
     * The language switch after the update language
     * */
    public static void updateViewLanguage(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            int count = vg.getChildCount();
            for (int i = 0; i < count; i++) {
                updateViewLanguage(vg.getChildAt(i));
            }
        } else if (view instanceof LanguageChangableView) {
            LanguageChangableView tv = (LanguageChangableView) view;
            tv.reLoadLanguage();
        }
    }

    /**
     * by : cyt
     * @return void
     * @ TODO dynamic parsing the url jump
     */
    public static void clickUrl(String url, Context c) {
        try {
            if (url.contains(Constants.APP_URL_FLAG) && url.contains("/qrgroup.html")) {//加入群聊页面
                Uri parse = Uri.parse(url);
                String gid = parse.getQueryParameter("gid");
                Intent intent = new Intent(c, DiscussGroupJoinUI.class);
                intent.putExtra("groupid", gid);
                c.startActivity(intent);
                openNewActivityAnim((Activity) c, false);

            }else{
                Intent intent = new Intent(c, WebViewUI.class);
                intent.putExtra("loadUrl", url);
                c.startActivity(intent);
                openNewActivityAnim((Activity) c, false);
            }
        } catch (Exception e) {
            Intent intent = new Intent(c, WebViewUI.class);
            intent.putExtra("loadUrl", url);
            c.startActivity(intent);
            openNewActivityAnim((Activity) c, false);
        }
    }

    /**
     * into the group chat
     * @ param cid group chat id
     * @ param member group of members
     */
    public static void gotoGroupChat(Context context,boolean hasJoined,String groupName,String cid, List<UserBaseVo> member) {
        StringBuilder url = new StringBuilder();
        StringBuilder username = new StringBuilder();
        try {
            if(!hasJoined){
                url.append(NextApplication.myInfo.getThumb()).append("___").append(NextApplication.myInfo.getGender()).append("#");
            }
            for (UserBaseVo vo : member) {
                url.append(vo.getThumb()).append("___").append(vo.getGender()).append("#");
            }
            url.deleteCharAt(url.lastIndexOf("#"));

            if (TextUtils.isEmpty(groupName)){
                username.append(NextApplication.myInfo.getUserName()).append("、");
                for (UserBaseVo vo : member) {
                    username.append(vo.getUserName()).append("、");
                }
                username.deleteCharAt(username.lastIndexOf("、"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        Utils.intentChattingUI(context, "group-" + cid, url.toString(), TextUtils.isEmpty(groupName) ? username.toString() : groupName, "1",0,true, false, false, 0, true);
    }


    /**
     * into the group chat
     * @ param cid group chat id
     * @ param member group of members
     */
    public static void gotoGroupChatFalse(Context context,boolean hasJoined,String groupName,String cid, List<UserBaseVo> member) {
        StringBuilder url = new StringBuilder();
        StringBuilder username = new StringBuilder();
        try {
            if(!hasJoined){
                url.append(NextApplication.myInfo.getThumb()).append("___").append(NextApplication.myInfo.getGender()).append("#");
            }
            for (UserBaseVo vo : member) {
                url.append(vo.getThumb()).append("___").append(vo.getGender()).append("#");
            }
            url.deleteCharAt(url.lastIndexOf("#"));

            if (TextUtils.isEmpty(groupName)){
                username.append(NextApplication.myInfo.getUserName()).append("、");
                for (UserBaseVo vo : member) {
                    username.append(vo.getUserName()).append("、");
                }
                username.deleteCharAt(username.lastIndexOf("、"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        Utils.intentChattingUI(context, "group-" + cid, url.toString(), TextUtils.isEmpty(groupName) ? username.toString() : groupName, "1",0,true, false, false, 0, false);
    }


    /**
     * jump to chat page
     * @ param mContext context
     * @ param uid user id
     * @ param avatarUrl head
     * @ param uName nickname
     * @ param gender gender
     * @ param isGroup whether group
     * @ param isDismissGroup group is disbanded
     * @ param isKickGroup Whether group T
     * @ param isFinish whether to close the current page
     */
    public static void intentChattingUI(Context mContext, String uid, String avatarUrl, String uName, String gender,int friendLog,
                                        boolean isGroup, boolean isDismissGroup, boolean isKickGroup, int unreadNum, boolean isFinish) {
        Intent i = new Intent(mContext, ChattingUI.class);
        i.putExtra("uid", uid);
        i.putExtra("avatarurl", avatarUrl);
        i.putExtra("username", uName);
        i.putExtra("gender", gender);
        i.putExtra("friendLog", friendLog);
        i.putExtra("isgroup", isGroup);
        i.putExtra("dismissgroup", isDismissGroup);
        i.putExtra("kickgroup", isKickGroup);
        i.putExtra("unreadNum", unreadNum);
        mContext.startActivity(i);
        openNewActivityAnim((Activity) mContext, isFinish);
    }

    /**
     * Jump to the personal information page
     */
    public static void intentFriendUserInfo(Activity act, UserBaseVo vo, boolean finish) {
        Intent intent = new Intent(act, FriendInfoUI.class);
        UserInfoVo info = new UserInfoVo();
        info.setLocalId(vo.getLocalId());
        info.setMid(vo.getMid());
        info.setUsername(vo.getUserName());
        info.setNote(vo.getNote());
        info.setThumb(vo.getThumb());
        info.setGender(vo.getGender());
        info.setSightml(vo.getSightml());
        info.setAge(vo.getAge());
        info.setFriendLog(vo.getFriendLog());
        info.setOffLineFound(vo.isOffLineFound());
        intent.putExtra("info", info);
        act.startActivity(intent);
        openNewActivityAnim(act, finish);
    }

    public static void showFileIcon(Context mContext, String fileName, String filePath, ImageView imageView) {

        if (!TextUtils.isEmpty(fileName)) {
            fileName = fileName.toLowerCase();
            if (fileName.endsWith(".doc") || fileName.endsWith(".docx") || fileName.endsWith(".wps")) {
                imageView.setImageResource(R.drawable.file_icon_word);
            } else if (fileName.endsWith(".pdf")) {
                imageView.setImageResource(R.drawable.file_icon_pdf);
            } else if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) {
                imageView.setImageResource(R.drawable.file_icon_excel);
            } else if (fileName.endsWith(".txt") || fileName.endsWith(".html") || fileName.endsWith(".htm")) {
                imageView.setImageResource(R.drawable.file_icon_txt);
            } else if (fileName.endsWith(".ppt") || fileName.endsWith(".pptx")) {
                imageView.setImageResource(R.drawable.file_icon_ppt);
            } else if (fileName.endsWith(".dat") || fileName.endsWith(".vob")
                    || fileName.endsWith(".avi") || fileName.endsWith(".rm")
                    || fileName.endsWith(".asf") || fileName.endsWith(".wmv")
                    || fileName.endsWith(".mov") || fileName.endsWith(".mp4")) {
                imageView.setImageResource(R.drawable.file_icon_video);
            } else if (fileName.endsWith(".cda") || fileName.endsWith(".wav")
                    || fileName.endsWith(".mp3") || fileName.endsWith(".wma")
                    || fileName.endsWith(".ra") || fileName.endsWith(".rma")
                    || fileName.endsWith(".MID") || fileName.endsWith(".MIDI")
                    || fileName.endsWith(".RMI") || fileName.endsWith(".XMI")
                    || fileName.endsWith(".mid") || fileName.endsWith(".OGG")
                    || fileName.endsWith(".vqf") || fileName.endsWith(".mod")
                    || fileName.endsWith(".ape") || fileName.endsWith(".aiff")
                    || fileName.endsWith(".aac") || fileName.endsWith(".au")) {
                imageView.setImageResource(R.drawable.file_icon_music);
            } else if (fileName.endsWith(".apk")) {
                imageView.setImageResource(R.drawable.file_icon_apk);
                new Thread(new LoadApkIcon(mContext, filePath, imageView)).start();
            } else if (fileName.endsWith(".zip") || fileName.endsWith(".rar") || fileName.endsWith(".7z")) {
                imageView.setImageResource(R.drawable.file_icon_other);
            } else {
                imageView.setImageResource(R.drawable.file_icon_other);
            }
        } else {
            imageView.setImageResource(R.drawable.file_icon_other);
        }
    }

    /**
     * Set the file icon
     */

    static class LoadApkIcon implements Runnable {
        private ImageView imageView;
        private Context mContext;
        private String filePath;
        private Object tag;

        LoadApkIcon(Context mContext, String filePath, ImageView imageView) {
            this.imageView = imageView;
            this.mContext = mContext;
            this.filePath = filePath;
            tag = imageView.getTag();
        }

        private Handler handler = new Handler() {
            public void handleMessage(Message msg) {

                if (tag != null && tag.equals(imageView.getTag())) {
                    if (msg.obj == null) {
                        imageView.setImageResource(R.drawable.file_icon_apk);
                    } else {
                        imageView.setImageDrawable((Drawable) msg.obj);
                    }
                }
            }
        };

        @Override
        public void run() {
            try {
                PackageManager pm = mContext.getPackageManager();
                PackageInfo info = pm.getPackageArchiveInfo(filePath, PackageManager.GET_ACTIVITIES);
                ApplicationInfo appInfo = info.applicationInfo;
                appInfo.publicSourceDir = filePath;//add-absolute path of app
                Drawable icon = pm.getApplicationIcon(appInfo);
                Message msg = handler.obtainMessage();
                msg.obj = icon;
                handler.sendMessage(msg);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * TextView、Button set different color
     */
    public static ColorStateList toColorStateList(@ColorInt int normalColor, @ColorInt int pressedColor,
                                                  @ColorInt int focusedColor, @ColorInt int unableColor) {
        int[] colors = new int[]{pressedColor, focusedColor, normalColor, focusedColor, unableColor, normalColor};
        int[][] states = new int[6][];
        states[0] = new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled};
        states[1] = new int[]{android.R.attr.state_enabled, android.R.attr.state_focused};
        states[2] = new int[]{android.R.attr.state_enabled};
        states[3] = new int[]{android.R.attr.state_focused};
        states[4] = new int[]{android.R.attr.state_window_focused};
        states[5] = new int[]{};
        return new ColorStateList(states, colors);
    }

    public static ColorStateList toColorStateList(@ColorInt int normalColor, @ColorInt int pressedColor) {
        return toColorStateList(normalColor, pressedColor, pressedColor, normalColor);
    }



    /**
     * thumbnail splicing
     * stitching thumbnail request rules, format similar to: http://beta.iyueni.com/Uploads/avatar/2/13955_cbdwfI.jpg_200_200_2_80.jpg
     */
    public static String buildThumb(String thumb) {
        if (TextUtils.isEmpty(thumb)) {
            return "";
        }
        try {
            /** Image width */
            String reqWidth = "200";
            /**Picture height  */
            String reqHeight = "200";
            /** Image cropping rules (1: geometric scaling;2: a square cut;Other: fixed size)*/
            String reqCropType = "2";
            /** Image quality (generally for 75;Depending on the image size and demand, the best range of 75 ~ 85)*/
            String quality = "80";
            StringBuilder sb = new StringBuilder();
            sb.append(thumb);
            sb.append("_");
            sb.append(reqWidth);
            sb.append("_");
            sb.append(reqHeight);
            sb.append("_");
            sb.append(reqCropType);
            sb.append("_");
            sb.append(quality);
            sb.append(thumb.substring(thumb.lastIndexOf(".")));
            return sb.toString();
        } catch (Exception e) {
            return thumb;
        }
    }

    //安装apk，兼容7.0  Install apk, compatible with 7.0
    public static void installApk8(Context context,File file) {
        if (file == null){
            return;
        }
        if (!file.exists()){
            return;
        }
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            // 由于没有在Activity环境下启动Activity,设置下面的标签   setFlags要放在addFlags之前
            // Since the Activity is not started in the Activity environment, set the following label setFlags to be placed before addFlags
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //版本在7.0以上是不能直接通过uri访问的
            //Versions above 7.0 are not directly accessible via uri
            //参数1 上下文, 参数2 Provider主机地址和清单文件中保持一致   参数3 共享的文件
            //Parameter 1 context, parameter 2 Provider host address and keep consistent in the manifest file Parameter 3 shared file
            Uri apkUri = FileProvider.getUriForFile(context, "com.lingtuan.firefly.fileProvider", file);
            //添加这一句表示对目标应用临时授权该Uri所代表的文件
            //Adding this sentence means temporarily authorizing the file represented by the Uri to the target application.
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            context.startActivity(intent);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public static Integer[] getFaceListRes() {
        Integer[] ids = new Integer[]{
                R.drawable.smiley_0, R.drawable.smiley_1, R.drawable.smiley_2, R.drawable.smiley_3, R.drawable.smiley_4
                , R.drawable.smiley_5, R.drawable.smiley_6, R.drawable.smiley_7, R.drawable.smiley_8, R.drawable.smiley_9
                , R.drawable.smiley_10, R.drawable.smiley_11, R.drawable.smiley_12, R.drawable.smiley_13, R.drawable.smiley_14
                , R.drawable.smiley_15, R.drawable.smiley_16, R.drawable.smiley_17, R.drawable.smiley_18, R.drawable.smiley_19
                , R.drawable.smiley_20, R.drawable.smiley_21, R.drawable.smiley_22, R.drawable.smiley_23, R.drawable.smiley_24
                , R.drawable.smiley_25, R.drawable.smiley_26, R.drawable.smiley_27, R.drawable.smiley_28, R.drawable.smiley_29
                , R.drawable.smiley_30, R.drawable.smiley_31, R.drawable.smiley_32, R.drawable.smiley_33, R.drawable.smiley_34
                , R.drawable.smiley_35, R.drawable.smiley_36, R.drawable.smiley_37, R.drawable.smiley_38, R.drawable.smiley_39
                , R.drawable.smiley_40, R.drawable.smiley_41, R.drawable.smiley_42, R.drawable.smiley_43, R.drawable.smiley_44
                , R.drawable.smiley_45, R.drawable.smiley_46, R.drawable.smiley_47, R.drawable.smiley_48, R.drawable.smiley_49
                , R.drawable.smiley_50, R.drawable.smiley_51, R.drawable.smiley_52, R.drawable.smiley_53, R.drawable.smiley_68
                , R.drawable.smiley_69, R.drawable.smiley_70, R.drawable.smiley_71, R.drawable.smiley_72, R.drawable.smiley_73
                , R.drawable.smiley_74, R.drawable.smiley_75, R.drawable.smiley_76, R.drawable.smiley_77, R.drawable.smiley_78
                , R.drawable.smiley_79, R.drawable.smiley_80, R.drawable.smiley_81, R.drawable.smiley_54, R.drawable.smiley_55
                , R.drawable.smiley_56, R.drawable.smiley_57, R.drawable.smiley_58, R.drawable.smiley_59, R.drawable.smiley_60
                , R.drawable.smiley_61, R.drawable.smiley_62, R.drawable.smiley_63, R.drawable.smiley_64, R.drawable.smiley_65
                , R.drawable.smiley_66, R.drawable.smiley_67
        };
        return ids;
    }

}
