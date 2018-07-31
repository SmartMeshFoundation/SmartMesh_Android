package com.lingtuan.firefly.network;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.MD5Util;
import com.lingtuan.firefly.util.MySharedPrefs;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created on 2017/8/24.
 * Wallet network interface
 */

public class NetRequestUtils {

    private static NetRequestUtils instance;

    public static NetRequestUtils getInstance(){
        if(instance == null ){
            instance = new NetRequestUtils();
        }
        return instance;
    }

    /**
     * Root Url
     * */
    private static final String ROOTURL = Constants.GLOBAL_SWITCH_OPEN  ? "http://wallet.smartmesh.io/?module=account&action=" : "http://119.28.59.209/?module=account&action=";

    /**
     * encryption key
     * */
    private static final String REQ_KEY = "6758a433d438110587e002b20a7bf310";

    /**
     * Account Balance
     * */
    private static final String BALANCE_URL = "balance&address=";

    /**
     * Get the Eth transaction number
     * */
    private static final String GET_ERHNONCE_URL = "getEthNonce&address=";

    /**
     * Get transaction number
     * */
    private static final String GET_NONCE_URL = "getNonce&address=";


    /**
     * eth transfer
     * */
    private static final String SEND_RAWTRANS = "sendRawTransaction&data=";

    /**
     * Acting payment tokens
     * */
    private static final String TRANSFERPROXY = "transferProxy&from=";

    /**
     * eth transfer records
     * */
    private static final String TRANSLIST = "txlist";


    /**
     * smt transfer records
     * */
    private static final String SMT_TRANSLIST = "smtTxlist";

    /**
     * mesh transfer records
     * */
    private static final String MESH_TRANSLIST = "meshTxlist";

    /**
     * Get gas
     * */
    private static final String GAT_GAS = "getGas";

    /**
     * Get server time
     * */
    private static final String GET_SERVER_TIME = "systemTime";

    /**
     * Get the latest block number
     * */
    private static final String GET_BLOCK_NUMBER = "getBlockNumber";

    /**
     * Get the block number of the transaction hash
     * */
    private static final String GET_TX_BLOCK_NUMBER = "txBlockNumber";

    /**
     * Get the latest block number
     * @param callback callback method
     * */
    public void getBlockNumber(Context context,Callback callback) throws IOException{
        get(context,ROOTURL + GET_BLOCK_NUMBER,callback);
    }

    /**
     * Get the block number of the transaction hash
     * @param callback callback method
     * */
    public void getTxBlockNumber(Context context,String tx,Callback callback) throws IOException{
        get(context,ROOTURL + GET_TX_BLOCK_NUMBER + "&tx=" + tx,callback);
    }



    /**
     * Get server time
     * @param callback callback method
     * */
    private void getServerTime(Callback callback) throws IOException{
        get(ROOTURL + GET_SERVER_TIME,callback);
    }

    /**
     *Get the gas interface
   * @param callback callback method
   * @ Param address The address of the account to be checked
     * */
    public void getBalance(Context context,String address,Callback callback) throws IOException{
        get(context,ROOTURL + BALANCE_URL + address,callback);
    }

    /**
     * Get user Ethereum next payment number
     * @param callback callback method
     * @param address The sender's address
     * */
    public void getEthNonce(Context context,String address,Callback callback) throws IOException{
        get(context,ROOTURL + GET_ERHNONCE_URL + address,callback);
    }

    /**
     * Get next payment number
     * @param callback callback method
     * @param address The sender's address
     * */
    public void getNonce(Context context,String address,Callback callback) throws IOException{
        get(context,ROOTURL + GET_NONCE_URL + address,callback);
    }

    /**
     * Send a token or Ethereum transfer
     * @param callback callback method
     * @param data Signed payment data
     * */
    public void sendRawTransaction(Context context,String data,Callback callback) throws IOException{
        get(context,ROOTURL + SEND_RAWTRANS + data,callback);
    }

    /**
     * Send a proxy payment transaction
     * @param callback callback method
     * @param from the originator's address
     * @param to recipient's address
     * @param value Number of transfers
     * @param fee agency fee
     * @param v Elliptic encryption parameters
     * @param r Elliptic encryption parameters
     * @param s Elliptic encryption parameters
     * */
    public void sendTransferProxy(Context context,String from,String to,String value,String fee,String r,String s,byte v,Callback callback) throws IOException{
        get(context,ROOTURL + TRANSFERPROXY + from + "&to=" + to + "&value=" + value + "&fee=" + fee + "&r=" + r + "&s=" + s + "&v=" + v + "&is_new=1",callback);
    }

    /**
     * Get transaction history
     * @param callback callback method
     * @param address user address
     * @param type 0 eth 1 smt
     * */
    public void getTxlist(Context context,int type,String address,Callback callback) throws IOException{
        String tempList;
        if (type == 1){
            tempList = SMT_TRANSLIST;
        }else if (type == 2){
            tempList = MESH_TRANSLIST;
        }else{
            tempList = TRANSLIST;
        }
        if (!address.startsWith("0x")){
            address = "0x" + address;
        }
        get(context,ROOTURL + tempList + "&address=" + address,callback);
    }

    /**
     * Get the gas interface
     * @param address wallet address
     * @param callback callback method
     * */
    public void getGas(Context context,String address,Callback callback) throws IOException{
        get(context,ROOTURL + GAT_GAS + "&address=" + address,callback);
    }




    /**
     * OkHttp request
     * @param context context object
     * @param tempUrl Temporary url behind the need to stitching encryption parameters
     * */
    public void get(Context context,String tempUrl, Callback b) throws IOException {
        String language = MySharedPrefs.readString(context,MySharedPrefs.FILE_APPLICATION,MySharedPrefs.KEY_LANGUAFE);
        String lang;
        if (TextUtils.isEmpty(language)){
            if (Build.VERSION.SDK_INT >= 24 && TextUtils.equals(Locale.getDefault().toString(),"en")){
                lang = "1";
            }else{
                if(TextUtils.equals("zh",Locale.getDefault().getLanguage())){
                    lang = "0";
                }else{
                    lang = "1";
                }
            }
        }else{
            if(TextUtils.equals("zh",language)){
                lang = "0";
            }else{
                lang = "1";
            }
        }
        String oldUrl = tempUrl + "&req_time=" + getReqTime(context) + "&req_randnum=" + getReqRandom(8) + "&lang=" + lang;
        String value = getUrlValue(oldUrl);
        String url =  oldUrl + "&req_key=" + getReqKey(value);
        get(url,b);
    }

    /**
     * OkHttp request
     * @param url Final request URL does not require splicing parameters
     * */
    public void get(String url, Callback b) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
//                //Ignore domain verification
//                .hostnameVerifier(new HostnameVerifier() {
//                    @Override
//                    public boolean verify(String hostname, SSLSession session) {
//                        //Add domain verification, here is the default of all trust
//                        return false;
//                    }
//                })
//                //Add a security certificate
//                .sslSocketFactory(SSLHelper.getSSLCertificate(), new X509TrustManager() {
//                    @Override
//                    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//
//                    }
//
//                    @Override
//                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//
//                    }
//
//                    @Override
//                    public X509Certificate[] getAcceptedIssuers() {
//                        return new X509Certificate[0];
//                    }
//                })
                .build();
        client.newCall(request).enqueue(b);
    }

    /**
     * Get system time
   * Take the local time and system time difference and then save to local
   * Request interface requires time difference
     * */
    public void getServerTime(final Context context) {
        try {
            getServerTime(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String jsonString = response.body().string();
                        JSONObject obj = new JSONObject(jsonString);
                        long time = obj.optJSONObject("data").optLong("time") - System.currentTimeMillis()/1000;
                        MySharedPrefs.writeLong(context.getApplicationContext(),MySharedPrefs.FILE_APPLICATION,MySharedPrefs.KEY_REQTIME,time);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param length Key length less than or equal to 8 bits
     * @return String
     * Generate the specified length of the key
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
     * Time stamp for encryption
     * 1. Get APP server time when starting APP, save server and local time difference locally, each use for md5 encryption timestamp calculated by adding the local time difference
     * 2. If the user manually changes the system time again, the interface will return a fixed error code of -2,
     * And the return data contains the client request time and server time difference, the client update this time difference to the local, prompt err, to be manually initiated again by the user request
     * */
    private long getReqTime(Context context) {
        long serverTime = MySharedPrefs.readLong(context.getApplicationContext(),MySharedPrefs.FILE_APPLICATION,MySharedPrefs.KEY_REQTIME);
        return System.currentTimeMillis()/1000 + serverTime;
    }

    /**
     * Md5 encryption
     * @return Md5 encrypted string
     * */
    private String getReqKey(String value) {
        String key =  value + REQ_KEY ;
        return MD5Util.MD5Encode(key,null);
    }

    /***
     * Get all the url value;
     * @param url specified url
     * @return parameter string
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

    public void destory(){
        instance = null;
    }

}
