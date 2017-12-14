package com.lingtuan.firefly;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.support.multidex.MultiDex;
import android.widget.ImageView;

import com.lingtuan.firefly.custom.BitmapFillet;
import com.lingtuan.firefly.custom.RoundBitmapDisplayer;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.CrashHandler;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.SDCardCtrl;
import com.lingtuan.firefly.util.SmileyParser;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;
import com.lingtuan.firefly.vo.UserInfoVo;
import com.lingtuan.firefly.xmpp.XmppUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Timer;
import java.util.TimerTask;

import geth.EthereumClient;
import geth.Geth;
import geth.Header;
import geth.NewHeadHandler;
import geth.Node;
import geth.NodeConfig;
import geth.SyncProgress;


/**
 * Created  on 2017/8/18.
 */

public class NextApplication extends Application implements NewHeadHandler {

    public static Context mContext;
    public static UserInfoVo myInfo;
    public static EthereumClient ec;

    public static ImageLoader mImageLoader = null;
    private static DisplayImageOptions mOptionsNull;
    private static DisplayImageOptions mOptionsCircle;

    /*expression*/
    public static SmileyParser mSmileyParser;

    private long totalNum = 2023108;
    private boolean hasGetTotalNum;
    private long currentNum;
    private Timer timer;
    private TimerTask task;
    @Override
    public void onCreate() {
        super.onCreate();

        /** Initialize the folder*/
        SDCardCtrl.initPath(this);

        XmppUtils.isLogining = false;
        mContext = this;
        /**init Ethereum Client*/
        initEthereumClient();
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

        initImageLoaderConfig();

        /**Initialize the expression*/
        SmileyParser.init(this);
        mSmileyParser = SmileyParser.getInstance();

        /**Initialization error log collection*/
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
    }

    /**
     * Initialize ethereum client
     * */
    private void initEthereumClient(){
        try {
            int state = MySharedPrefs.readInt(this, MySharedPrefs.FILE_USER, MySharedPrefs.AGREE_SYNC_BLOCK);
            if(state != 0) {
                startSync(state);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * start sync node method
     * @param state state  0 no sync  1 syncing  2 complete synchronously
     * */
    public void startSync(final int state){
        NetRequestImpl.getInstance().getSyncNode(new RequestListener() {
            @Override
            public void start() {

            }

            @Override
            public void success(JSONObject response) {
                try {
                    JSONArray array = response.optJSONArray("data");
                    writeAsset(array);
                    startNode(state);
                } catch (IOException e) {
                    e.printStackTrace();
                    startNode(state);
                }

            }

            @Override
            public void error(int errorCode, String errorMsg) {
                startNode(state);
            }
        });
    }

    /**
     * write to file
     * @param array  JsonArray file
     * */
    public void writeAsset(JSONArray array) throws IOException {
        File dir = new File(getFilesDir() + SDCardCtrl.DROIDPATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File f = new File(dir, "static-nodes.json");
        if (!f.exists()) {
            f.createNewFile();
        }
        copyString(array.toString(), f);
    }

    /**
     * copy string
     * @param fileContents file content
     * @param outputFile out put file
     * */
    public void copyString(String fileContents, File outputFile) throws FileNotFoundException {
        OutputStream output = new FileOutputStream(outputFile);
        PrintWriter p = new PrintWriter(output);
        p.println(fileContents);
        p.flush();
        p.close();
    }

    /**
     * start sync node
     * @param state  0 no sync  1 syncing  2 complete synchronously
     * */
    private void startNode(int state){
        try {
            NodeConfig nodeConfig = Geth.newNodeConfig();
            if (!Constants.GLOBAL_SWITCH_OPEN){
                nodeConfig.setEthereumDatabaseCache(16);
                nodeConfig.setEthereumEnabled(true);
                nodeConfig.setEthereumGenesis(Geth.testnetGenesis());
                nodeConfig.setEthereumNetworkID(3);
            }
            Node node = Geth.newNode(getFilesDir() + SDCardCtrl.ETHEREUM ,nodeConfig);
            node.start();
            ec = node.getEthereumClient();
            if(state ==1){
                ec.subscribeNewHead(new geth.Context(),this,16);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void initImageLoaderConfig() {
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;

        /** Use the default configuration*/
        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .memoryCacheExtraOptions(width, height)
                .threadPoolSize(3)
                .build();

        mImageLoader = ImageLoader.getInstance();
        mImageLoader.init(configuration);

        mOptionsCircle = new DisplayImageOptions.Builder()
                .bitmapConfig(Bitmap.Config.RGB_565)
                .showImageForEmptyUri(R.drawable.icon_default_avater)
                .showImageOnFail(R.drawable.icon_default_avater)
                .showImageOnLoading(R.drawable.icon_default_avater)
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


    private  void  getNumber(){
        long newValue = 0;
        try {
            newValue = NextApplication.ec.getHeaderByNumber(new geth.Context(),-1).getNumber();
            if(!hasGetTotalNum){
                SyncProgress syncProgress = NextApplication.ec.syncProgress(new geth.Context());
                if(syncProgress!=null){
                    hasGetTotalNum = true;
                    totalNum = syncProgress.getHighestBlock();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(newValue<=currentNum){
            return;
        }
        currentNum = newValue;
        if( currentNum>=totalNum){
            MySharedPrefs.writeInt(mContext,MySharedPrefs.FILE_USER,MySharedPrefs.AGREE_SYNC_BLOCK,2);
            if(timer!=null){
                timer.cancel();
            }
        }
        Intent intent = new Intent(Constants.SYNC_PROGRESS);
        intent.putExtra("currentNumber",currentNum);
        intent.putExtra("totalNumber",totalNum);
        Utils.sendBroadcastReceiver(mContext,intent,false);
    }


    @Override
    public void onError(String s) {

    }

    @Override
    public void onNewHead(Header header) {
        if(timer == null){
            timer = new Timer();
            task = new TimerTask() {
                @Override
                public void run() {
                    getNumber();
                }
            };
            timer.schedule(task,0,2000);
        }
    }
}

