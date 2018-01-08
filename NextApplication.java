package com.lingtuan.firefly;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.support.multidex.MultiDex;
import android.widget.ImageView;

import com.lingtuan.firefly.custom.BitmapFillet;
import com.lingtuan.firefly.custom.RoundBitmapDisplayer;
import com.lingtuan.firefly.util.CrashHandler;
import com.lingtuan.firefly.util.SDCardCtrl;
import com.lingtuan.firefly.util.SmileyParser;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.util.netutil.NetRequestUtils;
import com.lingtuan.firefly.vo.UserInfoVo;
import com.lingtuan.firefly.xmpp.XmppUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;



/**
 * Created  on 2017/8/18.
 */

public class NextApplication extends Application{

    public static Context mContext;
    public static UserInfoVo myInfo;

    public static ImageLoader mImageLoader = null;
    private static DisplayImageOptions mOptionsNull;
    private static DisplayImageOptions mOptionsCircle;

    /*expression*/
    public static SmileyParser mSmileyParser;

    @Override
    public void onCreate() {
        super.onCreate();

        /** Initialize the folder*/
        SDCardCtrl.initPath(this);

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
    }


    private void initImageLoaderConfig() {
        int width = getResources().getDisplayMetrics().widthPixels;
        int  height = getResources().getDisplayMetrics().heightPixels;

        /** Use the default configuration*/
        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .memoryCacheExtraOptions(width, height)
                .threadPoolSize(3)
                .build();

        mImageLoader = ImageLoader.getInstance();
        mImageLoader.init(configuration);

        mOptionsCircle = new DisplayImageOptions.Builder()
                .bitmapConfig(Bitmap.Config.RGB_565)
                .showImageForEmptyUri(R.drawable.nothing)
                .showImageOnFail(R.drawable.nothing)
                .showImageOnLoading(R.drawable.nothing)
                .cacheOnDisc(true)
                .cacheInMemory(true)
                .displayer(new RoundBitmapDisplayer(BitmapFillet.ROUND, 0))
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                .build();

        mOptionsNull = new DisplayImageOptions.Builder()
                .bitmapConfig(Bitmap.Config.RGB_565)
                .showImageForEmptyUri(R.drawable.nothing)
                .showImageOnFail(R.drawable.nothing)
                .showImageOnLoading(R.drawable.nothing)
                .cacheOnDisc(true)
                .cacheInMemory(true)
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                .build();

    }

    /*According to circular head*/
    public static void displayCircleImage(ImageView avatarView, String avatarUrl) {
        display(avatarView, avatarUrl, mOptionsCircle, null, null);
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

