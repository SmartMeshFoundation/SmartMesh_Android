package com.lingtuan.firefly.util.netutil;

import android.content.Context;
import android.text.TextUtils;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.util.MD5Util;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.Rsa;
import com.lingtuan.firefly.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created on 2017/8/24.
 * The network interface
 */

public class WalletRequestUtils {

    private static WalletRequestUtils instance;

    public static WalletRequestUtils getInstance(){
        if(instance == null ){
            instance = new WalletRequestUtils();
        }
        return instance;
    }

    /**
     * root Url
     * */
    private static final String ROOTURL = "http://123.207.127.38/?module=account&action=";

    /**
     * The encryption key
     * */
    private static final String REQ_KEY = "6758a433d438110587e002b20a7bf310";


    /**
     * The eth transfer record
     * */
    private static final String TRANSLIST = "txlist";


    /**
     * SMT transfer record
     * */
    private static final String FFT_TRANSLIST = "fftTxlist";


    /**
     * get the trade record
     * @ param callback callback methods
     * @ param address user address
     * @ param type 0 1 SMT eth
     * */
    public void getTxlist(Context context,int type,String address,Callback callback) throws IOException{
        String tempList;
        if (type == 0){
            tempList = TRANSLIST;
        }else{
            tempList = FFT_TRANSLIST;
        }
        get(context,ROOTURL + tempList + "&address=" + address,callback);
    }


    /**
     * OkHttp request
     * @ param context context object
     * @ param tempUrl temporary url splicing encryption parameters is needed
     * */
    public void get(Context context,String tempUrl, Callback b) throws IOException {
        String oldUrl = tempUrl + "&req_time=" + getReqTime(context) + "&req_randnum=" + getReqRandom(8);
        String value = getUrlValue(oldUrl);
        String url =  oldUrl + "&req_key=" + getReqKey(value);
        get(url, b);
    }

    /**
     * OkHttp request
     * @ param url eventually request url does not need to stitching parameters
     * */
    public void get(String url, Callback b) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        client.newCall(request).enqueue(b);
    }

    /**
     * @ param length key length less than or equal to eight
     * @ return String
     * immediately generated specifies the length of the key
     */
    private String getReqRandom(int length) {
        String str = "0123456789";
        Random random = new Random();
        if (length > 8) {
            length = 8;
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; ++i) {
            int number = random.nextInt(10);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

    /**
     * used to encrypt the timestamp
     * 1. Start the APP access to the server time, save the server and the local time difference value locally, every time is used to calculate md5 encryption timestamp and this difference by local time
     * 2. If the user manually changing the system time again, fixed interface will return error code 2,
     * and return the data contained within the client request service time and end time value, the client update this time difference to local, prompt err, for users to manually initiate the request again
     * */
    private long getReqTime(Context context) {
        long serverTime = MySharedPrefs.readLong(context.getApplicationContext(),MySharedPrefs.FILE_APPLICATION,MySharedPrefs.KEY_REQTIME);
        return System.currentTimeMillis()/1000 + serverTime;
    }

    /**
     * the Md5 encryption
     * @ return Md5 encrypted string
     * */
    private String getReqKey(String value) {
        String key =  value + REQ_KEY ;
        return MD5Util.MD5Encode(key,null);
    }

    /**
     * to get all the value in the url;
     * @ param url specified in the url
     * @ return argument string
     */
    public static String getUrlValue(String url) {
        StringBuilder builder = new StringBuilder();
        int index = url.indexOf("?");
        String temp = url.substring(index + 1);
        String[] keyValue = temp.split("&");
        for (String str : keyValue) {
            if (str.contains("=")) {
                String[] temps = str.split("=");
                if (temps.length == 2){
                    builder.append(temps[1]);
                }else{
                    builder.append(str.substring(temps[0].length()+1,str.length()));
                }
            }
        }
        return builder.toString();
    }

}
