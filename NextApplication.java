package com.lingtuan.firefly;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.wifi.WifiManager;
import android.support.multidex.MultiDex;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.ImageView;

import com.lingtuan.firefly.custom.BitmapFillet;
import com.lingtuan.firefly.custom.RoundBitmapDisplayer;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.raiden.API;
import com.lingtuan.firefly.raiden.RaidenNet;
import com.lingtuan.firefly.raiden.RaidenUrl;
import com.lingtuan.firefly.util.CrashHandler;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.SDCardCtrl;
import com.lingtuan.firefly.util.SmileyParser;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;
import com.lingtuan.firefly.util.netutil.NetRequestUtils;
import com.lingtuan.firefly.vo.UserInfoVo;
import com.lingtuan.firefly.wallet.util.WalletStorage;
import com.lingtuan.firefly.wallet.vo.StorableWallet;
import com.lingtuan.firefly.wallet.vo.TokenVo;
import com.lingtuan.firefly.xmpp.XmppUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.jivesoftware.smackx.pubsub.Subscription;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



/**
 * Created  on 2017/8/18.
 */

public class NextApplication extends Application {
    
    public static Context mContext;
    public static UserInfoVo myInfo;
    public static API api;
    public static String mRaidenStatus;
    public static Subscription mRaidenSubscribe;
    public static ExecutorService mRaidenThreadPool;
    
    public static boolean raidenSwitch = false;
    
    public static ImageLoader mImageLoader = null;
    private static DisplayImageOptions mOptionsNull;
    
    private static DisplayImageOptions mOptionsCircle;
    private static DisplayImageOptions mOptionsCircleToken;
    
    /*expression*/
    public static SmileyParser mSmileyParser;

    public static Object lock;
    @Override
    public void onCreate() {
        super.onCreate();
        /** Initialize the folder*/
        SDCardCtrl.initPath(this);
        lock = new Object();
        XmppUtils.isLogining = false;
        mContext = this;
        try {
            myInfo = new UserInfoVo().readMyUserInfo(this);
            Resources res = getResources();
            Configuration config = new Configuration();
            config.setToDefaults();
            config.fontScale = 1;
            res.updateConfiguration(config, res.getDisplayMetrics());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        NetRequestUtils.getInstance().getServerTime(this);
        initImageLoaderConfig();
        
        /**Initialize the expression*/
        SmileyParser.init(this);
        mSmileyParser = SmileyParser.getInstance();
        
        /**Initialization error log collection*/
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
        getWalletChange();
        initKeystore();
//        initKeyData();
        raidenMobileStart();
    }
    
    private void initKeystore() {
        try {
            File file = new File(getFilesDir(), SDCardCtrl.WALLERPATH);
            file.listFiles(new FilenameFilter() {
                
                @Override
                public boolean accept(File dir, String name) {
                    if (!name.startsWith(RaidenUrl.keystorePath)) {
                        String okFilePth = dir.getAbsolutePath() + "/" + RaidenUrl.keystorePath + "0x" + name;
                        File oldFile = new File(dir.getAbsolutePath() + "/" + name);
                        oldFile.renameTo(new File(okFilePth));
                    } else {
                        String tempName;
                        tempName = name.substring(RaidenUrl.keystorePath.length());
                        if (!tempName.startsWith("0x")) {
                            tempName = "0x" + tempName;
                        }
                        String okFilePth = dir.getAbsolutePath() + "/" + RaidenUrl.keystorePath + tempName;
                        File oldFile = new File(dir.getAbsolutePath() + "/" + name);
                        oldFile.renameTo(new File(okFilePth));
                    }
                    return true;
                }
            });
        } catch (Exception e) {
            Log.i("xxxxxxxx异常", e.toString());
        }
    }
    
    private void initKeyData(){
        try {
            File file = new File(getFilesDir(), SDCardCtrl.RAIDEN_DATA);
            file.listFiles(new FilenameFilter() {
            
                @Override
                public boolean accept(File dir, String name) {
                    File oldFile = new File(dir.getAbsolutePath() + "/" + name);
                    oldFile.delete();
                    return true;
                }
            });
        } catch (Exception e) {
            Log.i("xxxxxxxx异常", e.toString());
        }
    }
    
    /**
     * change wallet
     */
    private void getWalletChange() {
        if (myInfo != null && myInfo.getLocalId() != null) {
            int version = MySharedPrefs.readInt(mContext, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_WALLET_CHANGE_VERSION + myInfo.getLocalId());
            NetRequestImpl.getInstance().getChange(version, new RequestListener() {
                @Override
                public void start() {
                    
                }
                
                @Override
                public void success(JSONObject response) {
                    int version = response.optInt("version");
                    MySharedPrefs.writeInt(mContext, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_WALLET_CHANGE_VERSION + myInfo.getLocalId(), version);
                    JSONArray listArray = response.optJSONArray("data");
                    if (listArray == null || listArray.length() <= 0) {
                        return;
                    }
                    FinalUserDataBase.getInstance().beginTransaction();
                    for (int i = 0; i < listArray.length(); i++) {
                        TokenVo token = new TokenVo().parse(listArray.optJSONObject(i));
                        FinalUserDataBase.getInstance().updateAllAddressToken(token);
                    }
                    FinalUserDataBase.getInstance().endTransactionSuccessful();
                }
                
                @Override
                public void error(int errorCode, String errorMsg) {
                    
                }
            });
        }
    }
    
    private void raidenMobileStart() {
//        try {
            mRaidenThreadPool = Executors.newFixedThreadPool(6);
//            ArrayList<StorableWallet> storableWallets = WalletStorage.getInstance(getApplicationContext()).get();
//            if (storableWallets != null && storableWallets.size() > 0) {
//                String selectAddress = "";
//                for (int i = 0; i < storableWallets.size(); i++) {
//                    if (storableWallets.get(i).isSelect()) {
//                        selectAddress = storableWallets.get(i).getPublicKey();
//                    }
//                }
//                if (TextUtils.isEmpty(selectAddress)) {
//                    selectAddress = storableWallets.get(0).getPublicKey();
//                }
//                if (!selectAddress.startsWith("0x")) {
//                    selectAddress = "0x" + selectAddress;
//                }
//                final String TempAddress = selectAddress;
//                new Thread(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        try {
//                            WifiManager wm = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//                            String clientIP = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
////                            "ws://192.168.0.152:18546",
////                            "ws://182.254.155.208:37171",
//                            api = SmartRaiden.startUp(
//                                    TempAddress,
//                                    getFilesDir().getAbsolutePath() + SDCardCtrl.WALLERPATH,
//                                    "ws://192.168.0.152:18546",
//                                    getFilesDir().getAbsolutePath() + SDCardCtrl.RAIDEN_DATA,
//                                    "111111",
//                                    "127.0.0.1:5001",
//                                    clientIP + ":40001",
//                                    "",
//                                    null);
//
//                             mRaidenSubscribe = api.subscribe(new NotifyHandler() {
//                                @Override
//                                public void onError(long l, String s) {
//                                    Log.i("xxxxxxxxxx==onError", "===" + l + "====" + s);
//                                }
//
//                                @Override
//                                public void onReceivedTransfer(String s) {
//                                    Log.i("xxxxxx==onRTransfer", "===" + s);
//                                }
//
//                                @Override
//                                public void onSentTransfer(String s) {
//                                    Log.i("xxxxxx==onSTransfer", "===" + s);
//                                }
//
//                                @Override
//                                public void onStatusChange(String s) {
//                                    Log.i("xxxxxx==onStatusChange", "===" + s);
//                                    mRaidenStatus = s;
//                                }
//                            });
//
//                        } catch (Exception e) {
//                            Log.i("ddddddddddddd异常", "====" + e.toString());
//                            mRaidenSubscribe.unsubscribe();
//                            RaidenNet.getInatance().stopRaiden();
//                        }
//                    }
//                }).start();
//            }
//
//        } catch (Exception e) {
//            Log.i("dddddddddd", e.toString());
//            mRaidenSubscribe.unsubscribe();
//            RaidenNet.getInatance().stopRaiden();
//        }
    }
    
    private void initImageLoaderConfig() {
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        
        /** Use the default configuration*/
        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(getApplicationContext()).memoryCacheExtraOptions(width, height).threadPoolSize(3).build();
        
        mImageLoader = ImageLoader.getInstance();
        mImageLoader.init(configuration);
        
        mOptionsCircle = new DisplayImageOptions.Builder().bitmapConfig(Bitmap.Config.RGB_565).showImageForEmptyUri(R.drawable.nothing).showImageOnFail(R.drawable.nothing).showImageOnLoading(R.drawable.nothing).cacheOnDisc(true).cacheInMemory(true).displayer(new RoundBitmapDisplayer(BitmapFillet.ROUND, 0)).imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2).build();
        
        mOptionsCircleToken = new DisplayImageOptions.Builder().bitmapConfig(Bitmap.Config.RGB_565).showImageForEmptyUri(R.drawable.icon_default_token).showImageOnFail(R.drawable.icon_default_token).showImageOnLoading(R.drawable.icon_default_token).cacheOnDisc(true).cacheInMemory(true).displayer(new RoundBitmapDisplayer(BitmapFillet.ROUND, 0)).imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2).build();
        
        mOptionsNull = new DisplayImageOptions.Builder().bitmapConfig(Bitmap.Config.RGB_565).showImageForEmptyUri(R.drawable.nothing).showImageOnFail(R.drawable.nothing).showImageOnLoading(R.drawable.nothing).cacheOnDisc(true).cacheInMemory(true).imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2).build();
        
    }
    
    /*According to circular head*/
    public static void displayCircleImage(ImageView avatarView, String avatarUrl) {
        display(avatarView, avatarUrl, mOptionsCircle, null, null);
    }
    
    /*According to circular head*/
    public static void displayCircleToken(ImageView avatarView, String avatarUrl) {
        display(avatarView, avatarUrl, mOptionsCircleToken, null, null);
    }
    
    
    public static void displayNothing(ImageView avatarView, String avatarUrl, ImageLoadingListener listener) {
        display(avatarView, avatarUrl, mOptionsNull, listener, null);
    }
    
    private static void display(ImageView avatarView, String avatarUrl, DisplayImageOptions options, ImageLoadingListener listener, ImageSize size) {
        if (avatarUrl == null) {
            avatarUrl = "";
        }
        if (size == null) {
            mImageLoader.displayImage(avatarUrl, avatarView, options, listener);
        } else {
            mImageLoader.displayImage(avatarUrl, new ImageViewAware(avatarView), options, size, null, null);
        }
    }
    
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        /**Set the language*/
        Utils.settingLanguage(NextApplication.this);
    }
    
}

