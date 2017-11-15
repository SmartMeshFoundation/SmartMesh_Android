package com.lingtuan.firefly.util.netutil;

import android.app.NotificationManager;
import android.content.Intent;
import android.text.TextUtils;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.chat.vo.GalleryVo;
import com.lingtuan.firefly.custom.CustomMultipartEntity;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.login.LoginUI;
import com.lingtuan.firefly.offline.AppNetService;
import com.lingtuan.firefly.service.XmppService;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.MyToast;
import com.lingtuan.firefly.util.Rsa;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.xmpp.XmppUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Created on 2017/10/17.
 * Volley request framework
 */

public class VolleyUtils {

    private RequestQueue mQueue;
    private static VolleyUtils instance;

    private long totalSize;

    private VolleyUtils(){
        mQueue = Volley.newRequestQueue(NextApplication.mContext);
    }

    public static VolleyUtils getInstance(){
        if(instance == null ){
            instance = new VolleyUtils();
        }
        return instance;
    }

    public void requestJsonObject(final JSONObject jsonRequest, final RequestListener listener){

        if(listener != null){
            listener.start();
        }

        if (jsonRequest == null){
            if(listener != null){
                listener.error(404,"");
            }
            return;
        }

        if (!Utils.isConnectNet(NextApplication.mContext)){
            if(listener != null){
                listener.error(404,NextApplication.mContext.getString(R.string.net_unavailable));
            }
            return;
        }

        Map<String ,String> map = new HashMap<>();
        map.put("body", jsonRequest.toString());
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, NetRequestUtils.BASE_URL, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                int code = response.optInt("errcode");
                if(listener != null){
                    if(code == 0){
                        listener.success(response);
                    }else{
                        if(code == 1010101){
                            try {
                                Utils.writeToFile(jsonRequest, "xxx"+System.currentTimeMillis()+".json");

                                MySharedPrefs.clearUserInfo(NextApplication.mContext);
                                FinalUserDataBase.getInstance().close();
                                MyToast.showToast(NextApplication.mContext, NextApplication.mContext.getString(R.string.login_state_failure));
                                //Empty the notification
                                NotificationManager notificationManager = (NotificationManager) NextApplication.mContext.getSystemService(NextApplication.NOTIFICATION_SERVICE);
                                notificationManager.cancelAll();

                                //Exit the XMPP service
                                XmppUtils.getInstance().destroy();
                                Intent xmppservice = new Intent(NextApplication.mContext, XmppService.class);
                                NextApplication.mContext.stopService(xmppservice);
                                //Exit without social network service
                                int version =android.os.Build.VERSION.SDK_INT;
                                if(version >= 16){
                                    Intent offlineservice = new Intent(NextApplication.mContext, AppNetService.class);
                                    NextApplication.mContext.stopService(offlineservice);
                                }

                                NetRequestUtils.getInstance().destory();
                                BaseActivity.exit();
                                Intent intent = new Intent(NextApplication.mContext,LoginUI.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                NextApplication.mContext.startActivity(intent);
                                NextApplication.myInfo=null;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }else{
                            listener.error( code, response.optString("msg"));
                        }
                    }
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                if(listener != null){
                    listener.error(404,"");
                }
                if(error instanceof TimeoutError){
                    if(listener != null){
                        listener.error(404, NextApplication.mContext.getString(R.string.net_request_timeout));
                    }
                }else if(error instanceof NoConnectionError){
                    if(listener != null){
                        listener.error(404,NextApplication.mContext.getString(R.string.net_unavailable));
                    }
                }
            }
        }, map);
        req.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mQueue.add(req);
        mQueue.start();
    }

    public JSONObject getJsonRequest(String service,String method,boolean needToken,Map<String,String> params){

        if (needToken && (NextApplication.myInfo == null || TextUtils.isEmpty(NextApplication.myInfo.getToken()))){
            return null;
        }

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
            } catch (Exception e) {
            }

            String language = MySharedPrefs.readString(NextApplication.mContext,MySharedPrefs.FILE_APPLICATION,MySharedPrefs.KEY_LANGUAFE);
            if (TextUtils.isEmpty(language)){//The default language
                if (Locale.getDefault().getLanguage().equals("zh")){
                    jsonBody.put("lang","0");
                }else{
                    jsonBody.put("lang","1");
                }
            }else{
                if(TextUtils.equals("zh",language)){
                    jsonBody.put("lang","0");
                }else{
                    jsonBody.put("lang","1");
                }
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
            }else{ //No encryption
                jsonBody.putOpt("params", jsonParams);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonBody;
    }


    /**
     * upload files
     * @ param jsonRequest upload some fixed parameters
     * @ param addPartName upload server folder
     * @ param messageId message id
     * @ param local path path files
     * */
    public void requestFile(JSONObject jsonRequest,String addPartName, String path, final String messageId,RequestListener listener) throws Exception {

        String urlReturn = null;
        HttpClient httpclient = new DefaultHttpClient();
        ((DefaultHttpClient) httpclient).setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(0, false));
        HttpPost httpPost = new HttpPost(NetRequestUtils.BASE_URL);
        CustomMultipartEntity mulentity = new CustomMultipartEntity(
                new CustomMultipartEntity.ProgressListener() {
                    @Override
                    public void transferred(long num) {
                        Utils.intentImagePrecent(NextApplication.mContext, messageId, (int) ((num / (float) totalSize) * 100));
                    }
                });

        mulentity.addPart("body", new StringBody(jsonRequest.toString(), Charset.forName("UTF-8")));
        //Add images form data
        FileBody filebody = new FileBody(new File(path));
        mulentity.addPart(addPartName, filebody);
        totalSize = mulentity.getContentLength();
        httpPost.setEntity(mulentity);
        HttpResponse response = httpclient.execute(httpPost);
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            HttpEntity httpEntity = response.getEntity();
            InputStream in = httpEntity.getContent();
            urlReturn = Utils.read(in);
            in.close();
        }
        JSONObject obj = new JSONObject(urlReturn);
        int errorcode = obj.optInt("errcode");

        if (errorcode == 0) {
            listener.success(obj);
        } else {
            listener.error(errorcode,obj.optString("message"));
        }
    }

    /**
     * upload pictures
     * @ param jsonRequest upload some fixed parameters
     * @ param addPartName upload server folder
     * @ param imgPath image path
     * */
    public void requestImage(JSONObject jsonRequest, String addPartName, String imgPath, RequestListener listener) throws Exception {
        String urlReturn = null;
        HttpClient httpclient = new DefaultHttpClient();
        ((DefaultHttpClient) httpclient).setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(0, false));
        HttpPost httpPost = new HttpPost(NetRequestUtils.BASE_URL);
        MultipartEntity mulentity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        mulentity.addPart("body", new StringBody(jsonRequest.toString(), Charset.forName("UTF-8")));
        //Add images form data
        File f = new File(imgPath);
        org.apache.http.entity.mime.content.FileBody filebody = new org.apache.http.entity.mime.content.FileBody(f);
        mulentity.addPart(addPartName, filebody);
        httpPost.setEntity(mulentity);
        HttpResponse response = httpclient.execute(httpPost);
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            HttpEntity httpEntity = response.getEntity();
            InputStream in = httpEntity.getContent();
            urlReturn = Utils.read(in);
            in.close();
        }
        JSONObject obj = new JSONObject(urlReturn);
        int errorcode = obj.optInt("errcode");
        if (errorcode == 0) {
            listener.success(obj);
        } else {
            listener.error(errorcode,obj.optString("msg"));
        }
    }

    public void destory(){
        instance = null;
        mQueue = null;
    }
}
