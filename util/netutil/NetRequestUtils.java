package com.lingtuan.firefly.util.netutil;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.Rsa;
import com.lingtuan.firefly.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created on 2017/8/24.
 * The network interface
 */

public class NetRequestUtils {

    /**
     * root Url
     * */
    public static final String BASE_URL = Constants.GLOBAL_SWITCH_OPEN ? "http://open.smartmesh.io/index.php" : "http://beta.smartmesh.io/index.php";


    /** The request is successful */
    private final int SUCCESS = 10000;
    /** The request failed */
    public static final int ERROR = 10001;

    private static NetRequestUtils instance;

    public static NetRequestUtils getInstance(){
        if(instance == null ){
            instance = new NetRequestUtils();
        }
        return instance;
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
     * a post request only parameter
     * @ param json request parameters
     * @ return the result of the response and HTTP status code
     * @throws IOException
     */
    public void post(JSONObject json,Callback b) throws IOException {
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(mediaType, json.toString());
        postRequest(body,b);
    }

    /**
     * a post request parameters and files
     * @ param json request parameters
     * The key of @ param partName request body
     * @ param path file path
     * @ return the result of the response and HTTP status code
     * @throws IOException
     */
    public void postFile(JSONObject json,String partName,String path,Callback b) throws IOException {
        if (TextUtils.isEmpty(path)){
            return;
        }
        File file = new File(path);
        RequestBody multipartBody = new MultipartBody.Builder()
                .setType(MultipartBody.ALTERNATIVE)
                .addFormDataPart("body",json.toString())
                .addFormDataPart(partName,file.getName(),RequestBody.create(null, file))
                .build();
        postRequest(multipartBody,b);
    }


    /**
     * a post request
     * @ param body request body
     * @ param b callback interface
     * */
    private void postRequest(RequestBody body,Callback b){
        Request request = new Request.Builder()
                .url(BASE_URL)
                .post(body)
                .build();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        client.newCall(request).enqueue(b);
    }


    /**
     * is converted to a json request parameters
     * @ param service request to the server name
     * @ param method request method
     * @ param params request parameters
     * */
    public JSONObject getJsonRequest(String service,String method,Map<String,String> params){
        String tempToken = null ;
        if(params != null){
            tempToken = params.get("token");
            if(!TextUtils.isEmpty(tempToken)){
                params.remove("token");
            }
        }
        boolean isEncrpyt = false ; // The default is not encrypted
        if(params != null && params.containsKey("encrypt")){
            String encrypt = params.get("encrypt");
            // 1: encryption, 0: unencrypted
            isEncrpyt = TextUtils.equals("1", encrypt);
            params.remove("encrypt");
        }

        JSONObject jsonParams = new JSONObject();
        JSONObject jsonBody = new JSONObject();
        try {
            if(params != null){
                for (Map.Entry<String, String> s : params.entrySet()) {
                    jsonParams.put(s.getKey(), s.getValue());
                }
            }
            String location = MySharedPrefs.readString(NextApplication.mContext, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_LOCATION);
            String language = MySharedPrefs.readString(NextApplication.mContext,MySharedPrefs.FILE_APPLICATION,MySharedPrefs.KEY_LANGUAFE);

            if (TextUtils.isEmpty(language)){//The default language
                jsonParams.put("lang","1");
            }else{
                if(TextUtils.equals("zh",language)){
                    jsonParams.put("lang","0");
                }else{
                    jsonParams.put("lang","1");
                }
            }

            if(!TextUtils.isEmpty(location) && location.contains(",")){
                String[] split = location.split(",");
                jsonParams.put("lat", split[0]);
                jsonParams.put("lng", split[1]);
            }else{
                jsonParams.put("lat", 0);
                jsonParams.put("lng", 0);
            }

            jsonParams.put("system", "android" + android.os.Build.VERSION.RELEASE);
            jsonParams.put("model", android.os.Build.MODEL);
            try {
                jsonParams.put("imei", Utils.getIMEI(NextApplication.mContext));
                jsonParams.put("resolution", Utils.getScreenPixels(NextApplication.mContext));
                jsonParams.put("version", Utils.getVersionCode(NextApplication.mContext));
//                jsonParams.put("channel", Utils.getChannel(NextApplication.mContext));
            } catch (Exception e) {
                e.printStackTrace();
            }
            jsonBody.putOpt("service", service);
            jsonBody.putOpt("method", method);
            jsonBody.putOpt("encrypt", isEncrpyt ? "1":"0");
            jsonBody.putOpt("sn", UUID.randomUUID().toString());

            if(NextApplication.myInfo != null ){
                String token = NextApplication.myInfo.getToken();
                if(!TextUtils.isEmpty(token)){
                    jsonBody.putOpt("token", Rsa.encryptByPublic(token) );
                }
            }else if(!TextUtils.isEmpty(tempToken)){
                jsonBody.putOpt("token", Rsa.encryptByPublic(tempToken) );
            }
            jsonBody.putOpt("tokenencrypt", "1");
            if(isEncrpyt){ // encryption
                String paramsString = jsonParams.toString();
                jsonBody.putOpt("params", Rsa.encryptByPublic(paramsString));
            }else{
                jsonBody.putOpt("params", jsonParams);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonBody;
    }


    /**
     * a post request data
     * @ param jsonRequest request parameters
     * @ param listener callback interface
     * */
    public void requestJsonObject(JSONObject jsonRequest, final RequestListener listener){
        try {

            if (listener != null){
                listener.start();
            }

            NetRequestUtils.getInstance().post(jsonRequest, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    //Move to the main thread
                    Message message = Message.obtain();
                    message.obj = listener;
                    message.what  = ERROR;
                    mHandler.sendMessage(message);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    //Move to the main thread
                    Message message = Message.obtain();
                    message.what = SUCCESS;
                    Bundle bundle = new Bundle();
                    bundle.putString("jsonString",response.body().string());
                    message.setData(bundle);
                    message.obj = listener;
                    mHandler.sendMessage(message);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * a post request data (upload document)
     * @ param jsonRequest request parameters
     * @ param listener callback interface
     * The name of the @ param partName request body
     * @ param path file path
     * */
    public void requestFile(JSONObject jsonRequest,String partName,String path ,final RequestListener listener){
        try {

            if (listener != null){
                listener.start();
            }

            NetRequestUtils.getInstance().postFile(jsonRequest,partName,path ,new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    //Move to the main thread
                    Message message = Message.obtain();
                    message.obj = listener;
                    message.what  = ERROR;
                    mHandler.sendMessage(message);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    //Move to the main thread
                    Message message = Message.obtain();
                    message.what = SUCCESS;
                    Bundle bundle = new Bundle();
                    bundle.putString("jsonString",response.body().string());
                    message.setData(bundle);
                    message.obj = listener;
                    mHandler.sendMessage(message);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SUCCESS:
                {
                    RequestListener listener = (RequestListener) msg.obj;
                    Bundle bundle = msg.getData();
                    String jsonString = "";
                    if (bundle != null){
                        jsonString = bundle.getString("jsonString");
                    }
                    parseJson(jsonString,listener);
                }
                break;
                case ERROR:
                    RequestListener listener = (RequestListener) msg.obj;
                    if (listener != null){
                        listener.error(-1,"Error");
                    }
                    break;
            }
        }
    };

    /**
     * Analytical data
     * @ param jsonString request results
     * @ param listener callback interface
     * */
    private void parseJson(String jsonString,RequestListener listener){
        if (TextUtils.isEmpty(jsonString)){
            if (listener != null){
                listener.error(-1,"Error");
            }
            return;
        }
        try {
            JSONObject object = new JSONObject(jsonString);
            int errcod = object.optInt("errcode", -1);
            if (errcod == 0){//The request is successful
                if (listener != null){
                    listener.success(object);
                }
            }else{
                if (listener != null){
                    if (TextUtils.isEmpty(object.optString("msg"))){
                        listener.error(errcod,object.optString("message"));
                    }else{
                        listener.error(errcod,object.optString("msg"));
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            if (listener != null){
                listener.error(-1,"Error");
            }
        }
    }


    public void destory(){
        instance = null;
    }
}
